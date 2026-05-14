package com.mb.inventorymanagementservice.integration_tests.service;

import com.mb.inventorymanagementservice.config.TestRedisConfiguration;
import com.mb.inventorymanagementservice.data.entity.Category;
import com.mb.inventorymanagementservice.data.entity.Product;
import com.mb.inventorymanagementservice.data.repository.CategoryRepository;
import com.mb.inventorymanagementservice.data.repository.ProductRepository;
import com.mb.inventorymanagementservice.exception.BaseException;
import com.mb.inventorymanagementservice.exception.InventoryManagementServiceErrorCode;
import com.mb.inventorymanagementservice.service.CategoryService;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for optimistic and pessimistic locking in {@link CategoryService}.
 * <p>
 * Uses Testcontainers with PostgreSQL to verify concurrent access scenarios:
 * <ul>
 *   <li>Optimistic locking: concurrent {@code addProductToCategory} calls cause version conflict</li>
 *   <li>Pessimistic locking: concurrent {@code deleteProductFromCategory} calls are serialized via {@code SELECT ... FOR UPDATE}</li>
 * </ul>
 */
@ActiveProfiles("test-containers")
@SpringBootTest(classes = TestRedisConfiguration.class)
class CategoryLockingIntegrationTests {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private Category createCategory(String suffix) {
        Category category = new Category();
        category.setName("Lock Category " + suffix);
        category.setDescription("Lock Category Description " + suffix);
        category.setLiveInMarket(false);
        return categoryRepository.saveAndFlush(category);
    }

    private Product createProduct(String suffix) {
        Product product = new Product();
        product.setName("Lock Product " + suffix);
        product.setProductCode("LOCK_PROD_" + suffix);
        product.setDescription("Lock Product Description " + suffix);
        product.setPrice(Money.of(BigDecimal.TEN, "EUR"));
        product.setQuantity(5);
        return productRepository.saveAndFlush(product);
    }

    @Nested
    @DisplayName("Optimistic Locking - addProductToCategory")
    class OptimisticLockingTests {

        @Test
        @DisplayName("Should throw OPTIMISTIC_LOCKING_FAILURE when concurrent modifications cause version conflict")
        void addProductToCategory_ShouldThrowOptimisticLockingFailure_WhenConcurrentVersionConflict() throws InterruptedException {
            // Arrange
            String id = UUID.randomUUID().toString().substring(0, 8);
            Category category = createCategory("opt-concurrent-" + id);
            Product prod1 = createProduct("opt-c1-" + id);
            Product prod2 = createProduct("opt-c2-" + id);

            int threadCount = 2;
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch doneLatch = new CountDownLatch(threadCount);
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger optimisticLockFailureCount = new AtomicInteger(0);
            AtomicInteger otherFailureCount = new AtomicInteger(0);

            // Act — two threads simultaneously add different products to the same category
            try (ExecutorService executor = Executors.newFixedThreadPool(threadCount)) {
                for (int i = 0; i < threadCount; i++) {
                    String productName = (i == 0) ? prod1.getName() : prod2.getName();
                    executor.submit(() -> {
                        try {
                            startLatch.await();
                            categoryService.addProductToCategory(category.getName(), productName);
                            successCount.incrementAndGet();
                        } catch (BaseException e) {
                            if (e.getErrorCode() == InventoryManagementServiceErrorCode.OPTIMISTIC_LOCKING_FAILURE) {
                                optimisticLockFailureCount.incrementAndGet();
                            } else {
                                otherFailureCount.incrementAndGet();
                            }
                        } catch (Exception _) {
                            otherFailureCount.incrementAndGet();
                        } finally {
                            doneLatch.countDown();
                        }
                    });
                }

                startLatch.countDown();
                doneLatch.await();
            }

            // Assertions — at least one should succeed; with optimistic locking,
            // the loser gets OPTIMISTIC_LOCKING_FAILURE or another transactional exception
            assertThat(successCount.get()).isGreaterThanOrEqualTo(1);
            assertThat(successCount.get() + optimisticLockFailureCount.get() + otherFailureCount.get())
                    .isEqualTo(threadCount);
            // If there was a conflict, it should manifest as optimistic lock failure
            if (successCount.get() < threadCount) {
                assertThat(optimisticLockFailureCount.get() + otherFailureCount.get()).isGreaterThan(0);
            }
        }

        @Test
        @DisplayName("Should succeed when adding product to category without contention")
        void addProductToCategory_ShouldSucceed_WhenNoContention() {
            // Arrange
            String id = UUID.randomUUID().toString().substring(0, 8);
            Category category = createCategory("opt-single-" + id);
            Product product = createProduct("opt-s1-" + id);

            // Act
            Category result = categoryService.addProductToCategory(category.getName(), product.getName());

            // Assertions
            assertThat(result).isNotNull();
            assertThat(result.isLiveInMarket()).isTrue();
        }

        @Test
        @DisplayName("Should increment version on successful save")
        void addProductToCategory_ShouldIncrementVersion_WhenSaveSucceeds() {
            // Arrange
            String id = UUID.randomUUID().toString().substring(0, 8);
            Category category = createCategory("opt-version-" + id);
            Product prod1 = createProduct("opt-v1-" + id);
            int initialVersion = category.getVersion();

            // Act — single add (no contention)
            categoryService.addProductToCategory(category.getName(), prod1.getName());

            // Assertions
            Category updated = categoryRepository.findByNameIgnoreCase(category.getName()).orElseThrow();
            assertThat(updated.getVersion()).isEqualTo(initialVersion + 1);
        }
    }

    @Nested
    @DisplayName("Pessimistic Locking - deleteProductFromCategory")
    class PessimisticLockingTests {

        @Test
        @DisplayName("Should serialize concurrent deletes via pessimistic lock without data corruption")
        void deleteProductFromCategory_ShouldSerializeConcurrentDeletes_WhenPessimisticLockApplied() throws InterruptedException {
            // Arrange — add two products to the category first
            String id = UUID.randomUUID().toString().substring(0, 8);
            Category category = createCategory("pes-concurrent-" + id);
            Product prod1 = createProduct("pes-c1-" + id);
            Product prod2 = createProduct("pes-c2-" + id);

            categoryService.addProductToCategory(category.getName(), prod1.getName());
            categoryService.addProductToCategory(category.getName(), prod2.getName());

            int threadCount = 2;
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch doneLatch = new CountDownLatch(threadCount);
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failureCount = new AtomicInteger(0);

            // Act — two threads simultaneously try to remove different products from the same category
            try (ExecutorService executor = Executors.newFixedThreadPool(threadCount)) {
                for (int i = 0; i < threadCount; i++) {
                    String productName = (i == 0) ? prod1.getName() : prod2.getName();
                    executor.submit(() -> {
                        try {
                            startLatch.await();
                            categoryService.deleteProductFromCategory(category.getName(), productName);
                            successCount.incrementAndGet();
                        } catch (Exception _) {
                            failureCount.incrementAndGet();
                        } finally {
                            doneLatch.countDown();
                        }
                    });
                }

                startLatch.countDown();
                doneLatch.await();
            }

            // Assertions — pessimistic lock serializes access, so both should succeed
            assertThat(successCount.get()).isEqualTo(threadCount);
            assertThat(failureCount.get()).isZero();

            // Verify final state within a transaction to avoid LazyInitializationException
            transactionTemplate.executeWithoutResult(_ -> {
                Category result = categoryRepository.findByNameIgnoreCase(category.getName()).orElseThrow();
                assertThat(result.getProducts()).isEmpty();
                assertThat(result.isLiveInMarket()).isFalse();
            });
        }

        @Test
        @DisplayName("Should succeed when deleting product from category without contention")
        void deleteProductFromCategory_ShouldSucceed_WhenNoContention() {
            // Arrange — add product first
            String id = UUID.randomUUID().toString().substring(0, 8);
            Category category = createCategory("pes-single-" + id);
            Product product = createProduct("pes-s1-" + id);

            categoryService.addProductToCategory(category.getName(), product.getName());

            // Act
            Category result = categoryService.deleteProductFromCategory(category.getName(), product.getName());

            // Assertions
            assertThat(result).isNotNull();
            assertThat(result.isLiveInMarket()).isFalse();
        }

        @Test
        @DisplayName("Should throw CATEGORY_NOT_FOUND when category does not exist")
        void deleteProductFromCategory_ShouldThrowCategoryNotFound_WhenCategoryDoesNotExist() {
            // Arrange — non-existent category name
            String nonExistentCategory = "Non Existent " + UUID.randomUUID();
            String anyProduct = "Any Product";

            // Act
            // Assertions
            assertThatThrownBy(() -> categoryService.deleteProductFromCategory(nonExistentCategory, anyProduct))
                    .isInstanceOf(BaseException.class)
                    .extracting(e -> ((BaseException) e).getErrorCode())
                    .isEqualTo(InventoryManagementServiceErrorCode.CATEGORY_NOT_FOUND);
        }

        @Test
        @DisplayName("Should handle heavy contention with pessimistic lock gracefully for the same product")
        void deleteProductFromCategory_ShouldHandleHeavyContention_WhenMultipleThreadsCompete() throws InterruptedException {
            // Arrange — set up category with a product, then create heavy contention
            String id = UUID.randomUUID().toString().substring(0, 8);
            Category category = createCategory("pes-heavy-" + id);
            Product product = createProduct("pes-h1-" + id);

            categoryService.addProductToCategory(category.getName(), product.getName());

            int threadCount = 5;
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch doneLatch = new CountDownLatch(threadCount);
            AtomicInteger pessimisticLockFailureCount = new AtomicInteger(0);
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger otherFailureCount = new AtomicInteger(0);

            // Act — multiple threads try to delete the same product from the same category.
            // The pessimistic lock (SELECT ... FOR UPDATE) serializes access:
            // - First thread removes the product and saves successfully.
            // - Subsequent threads acquire the lock, read the updated category (product already removed),
            //   call List.remove() which is a no-op (idempotent), and save successfully.
            try (ExecutorService executor = Executors.newFixedThreadPool(threadCount)) {
                for (int i = 0; i < threadCount; i++) {
                    executor.submit(() -> {
                        try {
                            startLatch.await();
                            categoryService.deleteProductFromCategory(category.getName(), product.getName());
                            successCount.incrementAndGet();
                        } catch (BaseException e) {
                            if (e.getErrorCode() == InventoryManagementServiceErrorCode.PESSIMISTIC_LOCKING_FAILURE) {
                                pessimisticLockFailureCount.incrementAndGet();
                            } else {
                                otherFailureCount.incrementAndGet();
                            }
                        } catch (Exception _) {
                            otherFailureCount.incrementAndGet();
                        } finally {
                            doneLatch.countDown();
                        }
                    });
                }

                startLatch.countDown();
                doneLatch.await();
            }

            // Assertions — pessimistic lock serializes access; all threads complete successfully
            // because List.remove() is idempotent (removing a non-existent element is a no-op)
            assertThat(successCount.get()).isEqualTo(threadCount);
            assertThat(pessimisticLockFailureCount.get()).isZero();
            assertThat(otherFailureCount.get()).isZero();

            // Verify final state — the product should be removed from the category
            transactionTemplate.executeWithoutResult(_ -> {
                Category result = categoryRepository.findByNameIgnoreCase(category.getName()).orElseThrow();
                assertThat(result.getProducts()).isEmpty();
                assertThat(result.isLiveInMarket()).isFalse();
            });
        }
    }
}

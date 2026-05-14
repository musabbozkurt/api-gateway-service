package com.mb.stockexchangeservice.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionUtilsTest {

    @Nested
    @DisplayName("onCommit")
    class OnCommit {

        @Test
        @DisplayName("should execute runnable immediately when no active transaction")
        void onCommit_ShouldExecuteImmediately_WhenNoActiveTransaction() {
            // Arrange
            AtomicBoolean executed = new AtomicBoolean(false);

            // Act
            TransactionUtils.onCommit(() -> executed.set(true));

            // Assertions
            assertThat(executed).isTrue();
        }

        @Test
        @DisplayName("should register synchronization when transaction is active")
        void onCommit_ShouldRegisterSynchronization_WhenTransactionIsActive() {
            // Arrange
            AtomicBoolean executed = new AtomicBoolean(false);
            TransactionSynchronizationManager.initSynchronization();
            TransactionSynchronizationManager.setActualTransactionActive(true);

            try {
                // Act
                TransactionUtils.onCommit(() -> executed.set(true));

                // Assertions
                assertThat(executed).isFalse();
                assertThat(TransactionSynchronizationManager.getSynchronizations()).hasSize(1);
            } finally {
                TransactionSynchronizationManager.clearSynchronization();
                TransactionSynchronizationManager.setActualTransactionActive(false);
            }
        }

        @Test
        @DisplayName("should execute runnable after commit when transaction is active")
        void onCommit_ShouldExecuteRunnable_WhenTransactionCommits() {
            // Arrange
            AtomicBoolean executed = new AtomicBoolean(false);
            TransactionSynchronizationManager.initSynchronization();
            TransactionSynchronizationManager.setActualTransactionActive(true);

            try {
                TransactionUtils.onCommit(() -> executed.set(true));

                // Act
                TransactionSynchronizationManager.getSynchronizations()
                        .forEach(TransactionSynchronization::afterCommit);

                // Assertions
                assertThat(executed).isTrue();
            } finally {
                TransactionSynchronizationManager.clearSynchronization();
                TransactionSynchronizationManager.setActualTransactionActive(false);
            }
        }
    }

    @Nested
    @DisplayName("afterCompletion")
    class AfterCompletion {

        @Test
        @DisplayName("should execute runnable immediately when no active transaction")
        void afterCompletion_ShouldExecuteImmediately_WhenNoActiveTransaction() {
            // Arrange
            AtomicBoolean executed = new AtomicBoolean(false);

            // Act
            TransactionUtils.afterCompletion(() -> executed.set(true));

            // Assertions
            assertThat(executed).isTrue();
        }

        @Test
        @DisplayName("should register synchronization when transaction is active")
        void afterCompletion_ShouldRegisterSynchronization_WhenTransactionIsActive() {
            // Arrange
            AtomicBoolean executed = new AtomicBoolean(false);
            TransactionSynchronizationManager.initSynchronization();
            TransactionSynchronizationManager.setActualTransactionActive(true);

            try {
                // Act
                TransactionUtils.afterCompletion(() -> executed.set(true));

                // Assertions
                assertThat(executed).isFalse();
                assertThat(TransactionSynchronizationManager.getSynchronizations()).hasSize(1);
            } finally {
                TransactionSynchronizationManager.clearSynchronization();
                TransactionSynchronizationManager.setActualTransactionActive(false);
            }
        }

        @Test
        @DisplayName("should execute runnable after completion when transaction commits")
        void afterCompletion_ShouldExecuteRunnable_WhenTransactionCompletes() {
            // Arrange
            AtomicBoolean executed = new AtomicBoolean(false);
            TransactionSynchronizationManager.initSynchronization();
            TransactionSynchronizationManager.setActualTransactionActive(true);

            try {
                TransactionUtils.afterCompletion(() -> executed.set(true));

                // Act
                TransactionSynchronizationManager.getSynchronizations()
                        .forEach(sync -> sync.afterCompletion(0)); // STATUS_COMMITTED = 0

                // Assertions
                assertThat(executed).isTrue();
            } finally {
                TransactionSynchronizationManager.clearSynchronization();
                TransactionSynchronizationManager.setActualTransactionActive(false);
            }
        }

        @Test
        @DisplayName("should execute runnable after completion regardless of rollback status")
        void afterCompletion_ShouldExecuteRunnable_WhenTransactionRollsBack() {
            // Arrange
            AtomicInteger callCount = new AtomicInteger(0);
            TransactionSynchronizationManager.initSynchronization();
            TransactionSynchronizationManager.setActualTransactionActive(true);

            try {
                TransactionUtils.afterCompletion(callCount::incrementAndGet);

                // Act
                TransactionSynchronizationManager.getSynchronizations()
                        .forEach(sync -> sync.afterCompletion(1)); // STATUS_ROLLED_BACK = 1

                // Assertions
                assertThat(callCount).hasValue(1);
            } finally {
                TransactionSynchronizationManager.clearSynchronization();
                TransactionSynchronizationManager.setActualTransactionActive(false);
            }
        }
    }

    @Nested
    @DisplayName("Multiple synchronizations")
    class MultipleSynchronizations {

        @Test
        @DisplayName("should support registering multiple onCommit callbacks")
        void onCommit_ShouldExecuteAllCallbacks_WhenMultipleRegistered() {
            // Arrange
            AtomicInteger counter = new AtomicInteger(0);
            TransactionSynchronizationManager.initSynchronization();
            TransactionSynchronizationManager.setActualTransactionActive(true);

            try {
                TransactionUtils.onCommit(counter::incrementAndGet);
                TransactionUtils.onCommit(counter::incrementAndGet);
                TransactionUtils.onCommit(counter::incrementAndGet);

                // Act
                TransactionSynchronizationManager.getSynchronizations()
                        .forEach(TransactionSynchronization::afterCommit);

                // Assertions
                assertThat(TransactionSynchronizationManager.getSynchronizations()).hasSize(3);
                assertThat(counter).hasValue(3);
            } finally {
                TransactionSynchronizationManager.clearSynchronization();
                TransactionSynchronizationManager.setActualTransactionActive(false);
            }
        }
    }
}

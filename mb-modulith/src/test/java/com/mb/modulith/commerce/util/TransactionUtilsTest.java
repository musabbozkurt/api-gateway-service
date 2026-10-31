package com.mb.modulith.commerce.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionUtilsTest {

    @AfterEach
    void tearDown() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clear();
        }
    }

    @Test
    void onCommit_ShouldRunImmediately_WhenNoTransactionActive() {
        // Arrange
        AtomicBoolean ran = new AtomicBoolean(false);

        // Act
        TransactionUtils.onCommit(() -> ran.set(true));

        // Assertions
        assertTrue(ran.get());
    }

    @Test
    void onCommit_ShouldRunAfterCommit_WhenTransactionActive() {
        // Arrange
        AtomicBoolean ran = new AtomicBoolean(false);
        TransactionSynchronizationManager.initSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(true);

        // Act
        TransactionUtils.onCommit(() -> ran.set(true));
        assertFalse(ran.get());
        for (TransactionSynchronization synchronization : TransactionSynchronizationManager.getSynchronizations()) {
            synchronization.afterCommit();
        }

        // Assertions
        assertTrue(ran.get());
    }

    @Test
    void afterCompletion_ShouldRunImmediately_WhenNoTransactionActive() {
        // Arrange
        AtomicBoolean ran = new AtomicBoolean(false);

        // Act
        TransactionUtils.afterCompletion(() -> ran.set(true));

        // Assertions
        assertTrue(ran.get());
    }

    @Test
    void afterCompletion_ShouldRunAfterCompletion_WhenTransactionActive() {
        // Arrange
        AtomicBoolean ran = new AtomicBoolean(false);
        TransactionSynchronizationManager.initSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(true);

        // Act
        TransactionUtils.afterCompletion(() -> ran.set(true));
        assertFalse(ran.get());
        for (TransactionSynchronization synchronization : TransactionSynchronizationManager.getSynchronizations()) {
            synchronization.afterCompletion(TransactionSynchronization.STATUS_COMMITTED);
        }

        // Assertions
        assertTrue(ran.get());
    }
}
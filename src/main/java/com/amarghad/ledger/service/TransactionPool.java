package com.amarghad.ledger.service;

import com.amarghad.ledger.entities.Transaction;

import java.util.List;

public interface TransactionPool {
    void addTransaction(Transaction transaction);
    List<Transaction> getPendingTransactions();
    void removeTransaction(Transaction transaction);
}

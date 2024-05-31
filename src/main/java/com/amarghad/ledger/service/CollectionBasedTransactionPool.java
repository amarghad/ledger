package com.amarghad.ledger.service;

import com.amarghad.ledger.entities.Transaction;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Data @Service
public class CollectionBasedTransactionPool implements TransactionPool {

    private final List<Transaction> pendingTransactions = new ArrayList<>();

    @Override
    public void addTransaction(Transaction transaction) {
        pendingTransactions.add(transaction);
    }

    @Override
    public List<Transaction> getPendingTransactions() {
        return pendingTransactions;
    }

    @Override
    public void removeTransaction(Transaction transaction) {
        pendingTransactions.remove(transaction);
    }

}
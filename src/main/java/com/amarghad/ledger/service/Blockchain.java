package com.amarghad.ledger.service;

import com.amarghad.ledger.entities.Block;
import com.amarghad.ledger.entities.Transaction;

import java.util.List;

public interface Blockchain {

    Block addBlock(Block block);
    List<Block> getAllBlocks();
    boolean validateChain();
    Block mineBlock();

    void adjustDifficulty();

    void addTransaction(Transaction transaction);
    TransactionPool getTransactionPool();
}

package com.amarghad.ledger.service;

import com.amarghad.ledger.entities.Transaction;
import com.amarghad.ledger.entities.Block;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Getter @Service
public class WithPoolBlockchain implements Blockchain {

    private final List<Block> chain;

    @Autowired
    private TransactionPool transactionPool;

    private int difficulty = 10;

    public WithPoolBlockchain() {
        this.chain = new LinkedList<>();
        Block genesisBlock = createGenesisBlock();
        chain.add(genesisBlock);
    }


    private Block createGenesisBlock() {
        List<Transaction> transactions = new ArrayList<>();
        return new Block(0, "0", transactions, 0);
    }


    @Override
    public Block addBlock(Block block) {
        if (isValidBlock(block)) {
            chain.add(block);
            block.getTransactions().forEach(transactionPool::removeTransaction);
            return block;
        }
        throw new InvalidParameterException();
    }

    public boolean isValidBlock(Block block) {
        Block previousBlock = getAllBlocks().getLast();

        // Check if the index is incrementing by 1
        if (block.getIndex() != previousBlock.getIndex() + 1) {
            return false;
        }

        // Check if the previous hash matches
        if (!block.getPreviousHash().equals(previousBlock.getCurrentHash())) {
            return false;
        }

        // Check if the block hash meets the difficulty requirement
        String prefix = "0".repeat(difficulty);
        return block.getCurrentHash().startsWith(prefix);

    }

    @Override
    public Block mineBlock() {

        Block block = new Block(
                getChain().size(),
                getChain().getLast().getCurrentHash(),
                transactionPool.getPendingTransactions(),
                0
        );

        block.setPreviousHash(getChain().getLast().getCurrentHash());

        var calculatedHash = block.calculateHash();
        int nonce = 0;
        String requiredPrefix = "0".repeat(difficulty);

        while (!calculatedHash.startsWith(requiredPrefix)) {
            nonce++;
            calculatedHash = block.calculateHash();
        }

        block.setNonce(nonce);
        block.setCurrentHash(calculatedHash);

        Block x=addBlock(block);
        adjustDifficulty();
        return x;

    }


    @Override
    public boolean validateChain() {
        for (int i = 1; i < chain.size(); i++) {
            Block currentBlock = chain.get(i);
            Block previousBlock = chain.get(i - 1);

            // Check if the current hash of the block is valid
            if (!currentBlock.getCurrentHash().equals(currentBlock.calculateHash())) {
                return false;
            }

            // Check if the previous hash is equal to the hash of the previous block
            if (!currentBlock.getPreviousHash().equals(previousBlock.getCurrentHash())) {
                return false;
            }
        }
        return true;
    }

    private static final int DIFFICULTY_ADJUSTMENT_INTERVAL = 10; // Adjust this value as needed
    private static final long DESIRED_MINING_TIME = 10 * 60 * 1000; // 10 minutes in milliseconds

    @Override
    public void adjustDifficulty() {
        if (chain.size() < DIFFICULTY_ADJUSTMENT_INTERVAL + 1) {
            // Not enough blocks mined yet, skip difficulty adjustment
            return;
        }

        // Calculate the mining rate over the difficulty adjustment interval
        Block oldBlock = chain.get(chain.size() - DIFFICULTY_ADJUSTMENT_INTERVAL - 1);
        Block newBlock = chain.get(chain.size() - 1);
        long timeTaken = newBlock.getTimestamp().toEpochMilli() - oldBlock.getTimestamp().toEpochMilli();
        double miningRate = (double) DIFFICULTY_ADJUSTMENT_INTERVAL / timeTaken;

        // Adjust the mining difficulty based on the mining rate
        if (miningRate > 1) {
            // Mining too fast, increase difficulty
            difficulty += 1;
        } else {
            // Mining too slow, decrease difficulty
            difficulty = Math.max(1, difficulty - 1);
        }
    }

    @Override
    public void addTransaction(Transaction transaction) {
        transactionPool.addTransaction(transaction);
    }

    @Override
    public List<Block> getAllBlocks() {
        return chain;
    }
}
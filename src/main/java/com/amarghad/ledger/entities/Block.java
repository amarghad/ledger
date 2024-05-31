package com.amarghad.ledger.entities;

import com.amarghad.ledger.utils.HashUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
public class Block {
    private int index;
    private Instant timestamp;
    private String previousHash;
    private String currentHash;
    private List<Transaction> transactions;
    private int nonce;

    public Block(int index, String previousHash, List<Transaction> transactions, int nonce) {
        this.index = index;
        this.timestamp = Instant.now();
        System.out.println(this.timestamp);
        this.previousHash = previousHash;
        this.transactions = transactions;
        this.nonce = nonce;
        this.currentHash = calculateHash();
    }

    public String calculateHash() {
        String data = index + timestamp.toString() + previousHash + transactions.toString() + nonce;
        return HashUtil.calculateSHA256(data);
    }

    public boolean validateBlock() {
        String calculatedHash = calculateHash();
        return currentHash.equals(calculatedHash);
    }

}

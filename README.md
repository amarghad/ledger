# Rapport sur le workshop de Blockchain

## Introduction

Ce projet implémente une blockchain simple avec des fonctionnalités de transaction, de minage et de validation de la chaîne. L'architecture du système est conçue en utilisant le framework Spring Boot, et le code est structuré en différentes couches pour améliorer la lisibilité et la maintenabilité.

## Architecture

L'architecture du projet se compose des composants suivants :

1. **Entities (Entités)** : Représentent les éléments de base de la blockchain, tels que les blocs et les transactions.
2. **Services** : Contiennent la logique métier pour gérer la blockchain, les transactions et les portefeuilles.
3. **Controllers** : Gèrent les requêtes HTTP et servent de point d'entrée pour les opérations de la blockchain.
4. **Utils** : Fournissent des fonctions utilitaires telles que le hachage et le chiffrement.

### Détails du Code

#### Entities

##### Block.java
```java
package com.amarghad.ledger.entities;

import com.amarghad.ledger.utils.HashUtil;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Builder @Data
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
```

##### Transaction.java
```java
package com.amarghad.ledger.entities;

import lombok.*;

@Builder @Data @AllArgsConstructor @NoArgsConstructor
public class Transaction {
    private String sender;
    private String recipient;
    private double amount;
    private String signature;
}
```

#### Services

##### Blockchain.java
```java
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
}
```

##### CollectionBasedTransactionPool.java
```java
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
```

##### WithPoolBlockchain.java
```java
package com.amarghad.ledger.service;

import com.amarghad.ledger.entities.Transaction;
import com.amarghad.ledger.entities.Block;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Getter @Service
public class WithPoolBlockchain implements Blockchain {
    private final List<Block> chain;
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
        if (block.getIndex() != previousBlock.getIndex() + 1) {
            return false;
        }
        if (!block.getPreviousHash().equals(previousBlock.getCurrentHash())) {
            return false;
        }
        String prefix = "0".repeat(difficulty);
        return block.getCurrentHash().startsWith(prefix);
    }

    @Override
    public Block mineBlock() {
        Block block = Block.builder()
                .index(getChain().size())
                .previousHash(getChain().getLast().getCurrentHash())
                .transactions(transactionPool.getPendingTransactions())
                .build();

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

        Block x = addBlock(block);
        adjustDifficulty();
        return x;
    }

    @Override
    public boolean validateChain() {
        for (int i = 1; i < chain.size(); i++) {
            Block currentBlock = chain.get(i);
            Block previousBlock = chain.get(i - 1);
            if (!currentBlock.getCurrentHash().equals(currentBlock.calculateHash())) {
                return false;
            }
            if (!currentBlock.getPreviousHash().equals(previousBlock.getCurrentHash())) {
                return false;
            }
        }
        return true;
    }

    private static final int DIFFICULTY_ADJUSTMENT_INTERVAL = 10;
    private static final long DESIRED_MINING_TIME = 10 * 60 * 1000;

    @Override
    public void adjustDifficulty() {
        if (chain.size() < DIFFICULTY_ADJUSTMENT_INTERVAL + 1) {
            return;
        }
        Block oldBlock = chain.get(chain.size() - DIFFICULTY_ADJUSTMENT_INTERVAL - 1);
        Block newBlock = chain.get(chain.size() - 1);
        long timeTaken = newBlock.getTimestamp().toEpochMilli() - oldBlock.getTimestamp().toEpochMilli();
        double miningRate = (double) DIFFICULTY_ADJUSTMENT_INTERVAL / timeTaken;
        if (miningRate > 1) {
            difficulty += 1;
        } else {
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
```

#### Controllers

##### BlockchainController.java
```java
package com.amarghad.ledger.web;

import com.amarghad.ledger.entities.*;
import com.amarghad.ledger.service.Blockchain;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("blockchain")
@AllArgsConstructor
public class BlockchainController {
    private Blockchain blockchain;

    @GetMapping
    public List<Block> getBlockchain() {
        return blockchain.getAllBlocks();
    }

    @PostMapping
    public String addTransaction(@RequestBody Transaction transaction) {
        blockchain.addTransaction(transaction);
        return "Transaction added successfully.";
    }

    @PostMapping("mine")
    public Block mineBlock() {
        return blockchain.mineBlock();
    }

    @GetMapping("block/{index}")
    public Block getBlockByIndex(@PathVariable int index) {
        return blockchain.getAllBlocks().get(index);
    }

    @GetMapping("validate")
    public boolean validateChain() {
        return blockchain.validateChain();
    }
}
```

##### TransactionController.java
```java
package com.amarghad.ledger.web;

import com.amarghad.ledger.entities.Transaction;
import com.amarghad.ledger.service.Blockchain;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("transactions")
@AllArgsConstructor
@CrossOrigin("*")
public class TransactionController {
    private Blockchain blockchain;

    @GetMapping("/pending")
    public List<Transaction> getPendingTransactions() {
        return blockchain.getTransactionPool().getPendingTransactions();
    }

    @PostMapping
    public void addTransaction(@RequestBody Transaction transaction) {
        blockchain.addTransaction(transaction);
    }
}
```

##### WalletController.java
```java
package com.amarghad.ledger.web;

import com.amarghad.ledger.entities.Wallet;
import com.amarghad.ledger.service.WalletService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("wallet")
@AllArgsConstructor
@CrossOrigin("*")
public class WalletController {
    private WalletService walletService;

    @PostMapping
    public Wallet createWallet() {
        try {
            return walletService.createWallet();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping
    public Wallet getWallet()

 {
        return walletService.getWallet();
    }
}
```

#### Utils

##### HashUtil.java
```java
package com.amarghad.ledger.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtil {
    public static String calculateSHA256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
```

## Conclusion

Ce projet de blockchain simple implémente les concepts fondamentaux d'une blockchain, y compris les transactions, le minage et la validation des blocs. L'architecture est conçue pour être modulaire et extensible, ce qui permet d'ajouter facilement de nouvelles fonctionnalités ou d'améliorer les fonctionnalités existantes. Le code est structuré de manière à être facilement compréhensible et maintenable, ce qui est essentiel pour tout projet logiciel complexe.
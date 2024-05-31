# Compte Rendu du Projet Blockchain

## Introduction
Ce rapport présente une explication détaillée de l'architecture et du code d'un projet de blockchain. Le projet comprend plusieurs composants interconnectés, notamment des entités représentant les blocs et les transactions, des services pour gérer la chaîne de blocs et les transactions, ainsi qu'un contrôleur web pour interagir avec la blockchain via des requêtes HTTP.

## Architecture

### Structure
Le projet est organisé en plusieurs packages :

- **entities** : Contient les classes représentant les entités principales de la blockchain, notamment `Block` et `Transaction`.
- **service** : Contient les interfaces et les implémentations des services nécessaires pour gérer la blockchain et les transactions.
- **web** : Contient les contrôleurs REST pour exposer les fonctionnalités de la blockchain via une API HTTP.
- **utils** : Contient des classes utilitaires pour le hachage et le chiffrement symétrique.

### Diagramme de Classes
![Diagramme de Classes](class-diagram.png)

### Description des Classes et Interfaces

#### com.amarghad.ledger.entities.Block
```java
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
La classe `Block` représente un bloc dans la blockchain. Elle contient des attributs tels que l'index, le timestamp, le hash précédent, le hash actuel, la liste des transactions et le nonce. Elle possède des méthodes pour calculer le hash et valider le bloc.

#### com.amarghad.ledger.entities.Transaction
```java
@Builder @Data @AllArgsConstructor @NoArgsConstructor
public class Transaction {
    private String sender;
    private String recipient;
    private double amount;
    private String signature;
}
```
La classe `Transaction` représente une transaction avec des attributs pour l'expéditeur, le destinataire, le montant et la signature.

#### com.amarghad.ledger.service.Blockchain
```java
public interface Blockchain {
    Block addBlock(Block block);
    List<Block> getAllBlocks();
    boolean validateChain();
    Block mineBlock();
    void adjustDifficulty();
    void addTransaction(Transaction transaction);
}
```
L'interface `Blockchain` définit les méthodes pour ajouter un bloc, récupérer tous les blocs, valider la chaîne, miner un bloc, ajuster la difficulté et ajouter une transaction.

#### com.amarghad.ledger.service.WithPoolBlockchain
```java
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
La classe `WithPoolBlockchain` implémente l'interface `Blockchain` et gère les opérations sur la blockchain, comme l'ajout de blocs, la validation de la chaîne et l'ajustement de la difficulté.

#### com.amarghad.ledger.web.BlockchainController
```java
@RestController
@RequestMapping("blockchain")
@AllArgsConstructor
@CrossOrigin("*")
public class BlockchainController {

    private Blockchain blockchain;

    @GetMapping
    public List<Block> getBlockchain() {
        return blockchain.getAllBlocks();
    }

    @PostMapping("mine")
    public Block mineBlock() {
        return blockchain.mineBlock();
    }

    @GetMapping("{index}")
    public Block getBlockByIndex(@PathVariable int index) {
        return blockchain.getAllBlocks().get(index);
    }


    @GetMapping("validate")
    public boolean validateChain() {
        return blockchain.validateChain();
    }

}
```

Le contrôleur `BlockchainController` expose les fonctionnalités de la blockchain via une API REST, permettant de récupérer la chaîne de blocs, d'ajouter des transactions, de miner des blocs et de valider la chaîne.

```java
@RestController
@RequestMapping("transactions")
@AllArgsConstructor
@CrossOrigin("*")
public class TransactionController {

    private Blockchain blockchain;

    @PostMapping
    public void addTransaction(@RequestBody Transaction transaction) {
        blockchain.addTransaction(transaction);
    }

}
```

#### com.amarghad.ledger.utils.HashUtil
```java
public class HashUtil {
    private HashUtil() { throw new IllegalAccessError("Invalid call to constructor"); }

    public static String calculateSHA256(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes());

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
}
```
La classe `HashUtil` fournit une méthode utilitaire pour calculer le hash SHA-256 d'une chaîne de caractères.

#### com.amarghad.ledger.utils.SymmetricEncryption
```java
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import java.security.Key;
import java.security.SecureRandom;

public class SymmetricEncryption {
    private SymmetricEncryption() { }

    public static Key generateKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        SecureRandom secureRandom = new SecureRandom();
        keyGenerator.init(256, secureRandom);
        return keyGenerator.generateKey();
    }

    public static byte[] encrypt(byte[] plainText, Key key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(plainText);
    }

    public static byte[] decrypt(byte[] encryptedText, Key key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, key);
        return

 cipher.doFinal(encryptedText);
    }
}
```
La classe `SymmetricEncryption` fournit des méthodes pour générer des clés AES, chiffrer et déchiffrer des données.

## Vidéo Démonstrative
Pour voir une démonstration des fonctionnalités de cette blockchain, veuillez consulter la vidéo suivante :
[Vidéo de Démonstration](https://example.com/video)

Ce compte rendu fournit une vue d'ensemble complète de l'architecture et du code de notre projet de blockchain, avec des explications détaillées sur les composants principaux et leurs interactions. Si vous avez des questions ou des suggestions, n'hésitez pas à nous contacter.
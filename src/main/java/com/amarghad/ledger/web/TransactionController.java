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

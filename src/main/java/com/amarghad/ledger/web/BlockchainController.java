package com.amarghad.ledger.web;

import com.amarghad.ledger.entities.*;
import com.amarghad.ledger.service.Blockchain;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

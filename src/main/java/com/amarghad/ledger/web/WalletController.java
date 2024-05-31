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
    public Wallet getWallet() {
        return walletService.getWallet();
    }

}

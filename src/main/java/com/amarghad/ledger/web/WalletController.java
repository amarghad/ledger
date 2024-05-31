package com.amarghad.ledger.web;

import com.amarghad.ledger.dtos.WalletDto;
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

    @GetMapping
    public WalletDto getWallet() {
        return walletService.getWallet();
    }

}

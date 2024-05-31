package com.amarghad.ledger.service;

import com.amarghad.ledger.dtos.WalletDto;
import com.amarghad.ledger.entities.Wallet;
import com.amarghad.ledger.mappers.WalletMapper;
import com.amarghad.ledger.utils.KeyPairUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.KeyPair;
import java.util.UUID;

@Service
public class MemoryWalletService implements WalletService {

    private Wallet wallet = null;
    @Autowired
    private WalletMapper walletMapper;

    private void createWallet() {

        KeyPair pair = KeyPairUtil.generateKeyPair();
        String address = UUID.randomUUID().toString();
        this.wallet = Wallet.builder()
                .publicKey(pair.getPublic())
                .privateKey(pair.getPrivate())
                .balance(4)
                .address(address)
                .build();

    }

    @Override
    public WalletDto getWallet() {
        if (wallet == null) {
            createWallet();
        }
        return walletMapper.toDto(wallet);
    }
}

package com.amarghad.ledger.service;

import com.amarghad.ledger.entities.Wallet;
import com.amarghad.ledger.utils.KeyPairUtil;
import org.springframework.stereotype.Service;

import java.security.KeyPair;
import java.util.UUID;

@Service
public class MemoryWalletService implements WalletService {

    private Wallet wallet;

    @Override
    public Wallet createWallet() throws Exception {
        if (wallet != null) {
            throw new Exception("Cannot create double wallet on the node");
        }
        KeyPair pair = KeyPairUtil.generateKeyPair();
        String address = UUID.randomUUID().toString();
        Wallet w = Wallet.builder()
                .publicKey(pair.getPublic())
                .privateKey(pair.getPrivate())
                .balance(4)
                .address(address)
                .build();
        this.wallet = w;
        return w;

    }

    @Override
    public Wallet getWallet() {
        return wallet;
    }
}

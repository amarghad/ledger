package com.amarghad.ledger.service;

import com.amarghad.ledger.entities.Wallet;

public interface WalletService {
    Wallet createWallet() throws Exception;
    Wallet getWallet();
}

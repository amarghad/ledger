package com.amarghad.ledger.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.security.PrivateKey;
import java.security.PublicKey;

@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class Wallet {

    private PublicKey publicKey;
    private PrivateKey privateKey;
    private String address;
    private double balance;

}

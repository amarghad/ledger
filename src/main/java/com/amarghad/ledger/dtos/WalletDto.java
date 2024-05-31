package com.amarghad.ledger.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.security.PrivateKey;
import java.security.PublicKey;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WalletDto {

    private String publicKey;
    private String address;
    private double balance;

}

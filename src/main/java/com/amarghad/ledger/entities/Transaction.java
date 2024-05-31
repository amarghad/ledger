package com.amarghad.ledger.entities;


import lombok.*;

@Builder @Data @AllArgsConstructor @NoArgsConstructor
public class Transaction {

    private String sender;
    private String recipient;
    private double amount;
    private String signature;

}

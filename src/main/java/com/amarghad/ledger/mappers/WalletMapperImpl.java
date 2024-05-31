package com.amarghad.ledger.mappers;

import com.amarghad.ledger.dtos.WalletDto;
import com.amarghad.ledger.entities.Wallet;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
public class WalletMapperImpl implements WalletMapper{

    @Override
    public WalletDto toDto(Wallet entity) {
        return WalletDto.builder()
                .publicKey(Base64.getMimeEncoder().encodeToString( entity.getPublicKey().getEncoded() ))
                .address(entity.getAddress())
                .balance(entity.getBalance())
                .build();
    }
}

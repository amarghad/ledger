package com.amarghad.ledger.mappers;

public interface Mapper<T, T1> {

    T1 toDto(T entity);
}

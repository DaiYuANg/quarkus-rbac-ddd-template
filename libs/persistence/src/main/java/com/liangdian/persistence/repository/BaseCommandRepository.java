package com.liangdian.persistence.repository;

import java.util.Optional;

public interface BaseCommandRepository<E, ID> {
    Optional<E> findOptionalById(ID id);

    E findByIdOrThrow(ID id);

    E save(E entity);

    default E update(E entity) {
        return save(entity);
    }

    void remove(E entity);

    void removeById(ID id);
}

package com.github.DaiYuANg.persistence.repository;

import com.github.DaiYuANg.common.constant.ResultCode;
import com.github.DaiYuANg.common.exception.BizException;
import com.github.DaiYuANg.persistence.entity.BaseEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import java.util.Optional;

public abstract class BasePanacheCommandRepository<E extends BaseEntity>
    implements BaseRepositorySupport<E, Long>, PanacheRepository<E> {
  protected abstract ResultCode notFoundCode();

  @Override
  public Optional<E> findOptionalById(Long id) {
    return findByIdOptional(id);
  }

  @Override
  public E findByIdOrThrow(Long id) {
    return findByIdOptional(id).orElseThrow(() -> new BizException(notFoundCode()));
  }

  @Override
  public E save(E entity) {
    persist(entity);
    return entity;
  }

  @Override
  public void remove(E entity) {
    delete(entity);
  }

  @Override
  public void removeById(Long id) {
    findByIdOptional(id).ifPresent(this::delete);
  }
}

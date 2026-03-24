package com.github.DaiYuANg.modules.example.infrastructure.persistence;

import com.github.DaiYuANg.persistence.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "ex_product")
public class ExampleProductEntity extends BaseEntity {

  @Column(nullable = false, length = 255)
  public String name;

  @Column(name = "price_minor", nullable = false)
  public long priceMinor;

  @Column(nullable = false)
  public int stock;

  @Column(nullable = false)
  public boolean active = true;
}

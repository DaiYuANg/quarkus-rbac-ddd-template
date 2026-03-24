package com.github.DaiYuANg.modules.example.infrastructure.persistence;

import com.github.DaiYuANg.persistence.entity.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "ex_order_line")
public class ExampleOrderLineEntity extends BaseEntity {

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id", nullable = false)
  public ExampleOrderEntity order;

  @Column(name = "product_id", nullable = false)
  public long productId;

  @Column(nullable = false)
  public int quantity;

  @Column(name = "unit_price_minor", nullable = false)
  public long unitPriceMinor;
}

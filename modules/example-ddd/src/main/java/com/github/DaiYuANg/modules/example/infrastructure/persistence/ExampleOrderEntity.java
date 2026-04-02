package com.github.DaiYuANg.modules.example.infrastructure.persistence;

import com.github.DaiYuANg.modules.example.domain.model.order.ExampleOrderStatus;
import com.github.DaiYuANg.persistence.entity.BaseEntity;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ex_order")
public class ExampleOrderEntity extends BaseEntity {

  @Column(name = "buyer_username", nullable = false, length = 128)
  public String buyerUsername;

  @Convert(converter = ExampleOrderStatusConverter.class)
  @Column(nullable = false, columnDefinition = "SMALLINT")
  public ExampleOrderStatus status = ExampleOrderStatus.CREATED;

  @Column(name = "total_minor", nullable = false)
  public long totalMinor;

  @OneToMany(
      mappedBy = "order",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  public List<ExampleOrderLineEntity> lines = new ArrayList<>();
}

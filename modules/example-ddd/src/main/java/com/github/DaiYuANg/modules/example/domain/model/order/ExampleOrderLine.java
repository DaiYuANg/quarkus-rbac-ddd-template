package com.github.DaiYuANg.modules.example.domain.model.order;

public record ExampleOrderLine(long productId, int quantity, long unitPriceMinor) {

  public long lineTotalMinor() {
    return Math.multiplyExact(unitPriceMinor, quantity);
  }
}

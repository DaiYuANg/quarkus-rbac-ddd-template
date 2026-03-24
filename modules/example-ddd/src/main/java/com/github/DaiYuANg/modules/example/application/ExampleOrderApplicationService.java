package com.github.DaiYuANg.modules.example.application;

import com.github.DaiYuANg.common.constant.ResultCode;
import com.github.DaiYuANg.common.exception.BizException;
import com.github.DaiYuANg.modules.example.application.dto.ExampleOrderLineView;
import com.github.DaiYuANg.modules.example.application.dto.ExampleOrderView;
import com.github.DaiYuANg.modules.example.application.dto.ExampleProductView;
import com.github.DaiYuANg.modules.example.application.dto.PlaceExampleOrderCommand;
import com.github.DaiYuANg.modules.example.application.port.ExampleBuyerContext;
import com.github.DaiYuANg.modules.example.application.port.ExampleCatalogStore;
import com.github.DaiYuANg.modules.example.application.port.ExampleOrderStore;
import com.github.DaiYuANg.modules.example.application.port.ExampleUserLookupPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class ExampleOrderApplicationService {

  private final ExampleBuyerContext buyerContext;
  private final ExampleUserLookupPort userLookup;
  private final ExampleCatalogStore catalogStore;
  private final ExampleOrderStore orderStore;

  @Transactional
  public ExampleOrderView placeOrder(PlaceExampleOrderCommand command) {
    var buyer = buyerContext.requireBuyerUsername();
    userLookup.requireExistingUser(buyer);

    long totalMinor = 0;
    var pricedLines = new ArrayList<ExampleOrderLineView>();
    for (var line : command.lines()) {
      var product =
          catalogStore
              .findById(line.productId())
              .filter(ExampleProductView::active)
              .orElseThrow(() -> new BizException(ResultCode.DATA_NOT_FOUND, "product not available"));
      if (product.stock() < line.quantity()) {
        throw new BizException(ResultCode.BAD_REQUEST, "insufficient stock for product " + product.id());
      }
      var lineTotal = Math.multiplyExact(product.priceMinor(), line.quantity());
      totalMinor = Math.addExact(totalMinor, lineTotal);
      pricedLines.add(
          new ExampleOrderLineView(line.productId(), line.quantity(), product.priceMinor()));
      catalogStore.updateStock(product.id(), product.stock() - line.quantity());
    }
    return orderStore.create(buyer, pricedLines, totalMinor);
  }

  public List<ExampleOrderView> myOrders() {
    var buyer = buyerContext.requireBuyerUsername();
    return orderStore.listByBuyer(buyer);
  }
}

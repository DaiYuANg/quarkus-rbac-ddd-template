package com.github.DaiYuANg.modules.example.application;

import com.github.DaiYuANg.common.constant.ResultCode;
import com.github.DaiYuANg.common.exception.BizException;
import com.github.DaiYuANg.modules.example.application.command.PlaceExampleOrderCommand;
import com.github.DaiYuANg.modules.example.application.port.driven.ExampleBuyerContext;
import com.github.DaiYuANg.modules.example.application.port.driven.ExampleCatalogCommandRepository;
import com.github.DaiYuANg.modules.example.application.port.driven.ExampleCatalogReadRepository;
import com.github.DaiYuANg.modules.example.application.port.driven.ExampleOrderCommandRepository;
import com.github.DaiYuANg.modules.example.application.port.driven.ExampleOrderReadRepository;
import com.github.DaiYuANg.modules.example.application.port.driven.ExampleUserLookupPort;
import com.github.DaiYuANg.modules.example.application.port.in.ExampleOrderPlacementApi;
import com.github.DaiYuANg.modules.example.application.readmodel.ExampleOrderLineView;
import com.github.DaiYuANg.modules.example.application.readmodel.ExampleOrderView;
import com.github.DaiYuANg.modules.example.domain.model.catalog.ExampleProductSnapshot;
import com.github.DaiYuANg.modules.example.domain.model.order.ExampleOrder;
import com.github.DaiYuANg.modules.example.domain.model.order.ExampleOrderLine;
import com.github.DaiYuANg.persistence.outbox.DomainOutboxStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.val;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class ExampleOrderApplicationService implements ExampleOrderPlacementApi {

  private final ExampleBuyerContext buyerContext;
  private final ExampleUserLookupPort userLookup;
  private final ExampleCatalogReadRepository catalogReadRepository;
  private final ExampleCatalogCommandRepository catalogCommandRepository;
  private final ExampleOrderCommandRepository orderCommandRepository;
  private final ExampleOrderReadRepository orderReadRepository;
  private final DomainOutboxStore domainOutboxStore;

  @Transactional
  public ExampleOrderView placeOrder(PlaceExampleOrderCommand command) {
    val buyer = buyerContext.requireBuyerUsername();
    userLookup.requireExistingUser(buyer);

    val pricedLines =
        command.lines().stream()
            .map(
                line -> {
                  val product = requireAvailableProduct(line.productId());
                  if (product.stock() < line.quantity()) {
                    throw new BizException(
                        ResultCode.BAD_REQUEST, "insufficient stock for product " + product.id());
                  }
                  catalogCommandRepository.updateStock(
                      product.id(), product.stock() - line.quantity());
                  return new ExampleOrderLine(
                      line.productId(), line.quantity(), product.priceMinor());
                })
            .toList();
    val order = ExampleOrder.place(buyer, pricedLines);
    val persisted = orderCommandRepository.save(order);
    domainOutboxStore.append("ExampleOrder", persisted.id(), persisted.placedEvent());
    return toView(persisted);
  }

  public List<ExampleOrderView> myOrders() {
    val buyer = buyerContext.requireBuyerUsername();
    return orderReadRepository.listByBuyer(buyer);
  }

  private ExampleProductSnapshot requireAvailableProduct(Long productId) {
    return catalogReadRepository
        .findSnapshotById(productId)
        .filter(ExampleProductSnapshot::active)
        .orElseThrow(() -> new BizException(ResultCode.DATA_NOT_FOUND, "product not available"));
  }

  private ExampleOrderView toView(ExampleOrder order) {
    val lineViews =
        order.lines().stream()
            .map(
                line ->
                    new ExampleOrderLineView(
                        line.productId(), line.quantity(), line.unitPriceMinor()))
            .toList();
    return new ExampleOrderView(
        order.id(), order.buyerUsername(), order.status().name(), order.totalMinor(), lineViews);
  }
}

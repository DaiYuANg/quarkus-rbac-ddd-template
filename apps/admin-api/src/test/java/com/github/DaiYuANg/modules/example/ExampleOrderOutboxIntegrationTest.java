package com.github.DaiYuANg.modules.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;

import com.github.DaiYuANg.identity.entity.SysUser;
import com.github.DaiYuANg.modules.example.application.command.PlaceExampleOrderCommand;
import com.github.DaiYuANg.modules.example.application.command.PlaceExampleOrderLineCommand;
import com.github.DaiYuANg.modules.example.application.port.driven.ExampleBuyerContext;
import com.github.DaiYuANg.modules.example.application.port.in.ExampleOrderPlacementApi;
import com.github.DaiYuANg.modules.example.infrastructure.persistence.ExampleProductEntity;
import com.github.DaiYuANg.persistence.outbox.OutboxMessageEntity;
import com.github.DaiYuANg.persistence.outbox.OutboxStatus;
import com.github.DaiYuANg.testsupport.QuarkusPostgresValkeyTestProfile;
import com.github.DaiYuANg.testsupport.ValkeyTestResource;
import io.quarkus.test.InjectMock;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.junit.jupiter.api.Test;

@QuarkusTest
@QuarkusTestResource(ValkeyTestResource.class)
@TestProfile(QuarkusPostgresValkeyTestProfile.class)
class ExampleOrderOutboxIntegrationTest {

  @Inject ExampleOrderPlacementApi orderPlacementApi;
  @Inject EntityManager entityManager;
  @InjectMock ExampleBuyerContext buyerContext;

  @Test
  @TestTransaction
  void placingOrderPersistsOrderAndOutboxMessage() {
    when(buyerContext.requireBuyerUsername()).thenReturn("buyer-1");

    persistBuyer("buyer-1");
    var productId = persistProduct("Demo Product", 1999L, 8);

    var placedOrder =
        orderPlacementApi.placeOrder(
            new PlaceExampleOrderCommand(List.of(new PlaceExampleOrderLineCommand(productId, 2))));

    assertEquals("buyer-1", placedOrder.buyerUsername());
    assertEquals("CREATED", placedOrder.status());
    assertEquals(3998L, placedOrder.totalMinor());
    assertEquals(1, placedOrder.lines().size());

    var outboxEntries =
        entityManager
            .createQuery("select o from OutboxMessageEntity o", OutboxMessageEntity.class)
            .getResultList();
    assertEquals(1, outboxEntries.size());
    assertEquals("ExampleOrder", outboxEntries.get(0).aggregateType);
    assertEquals("example.order.created", outboxEntries.get(0).eventType);
    assertEquals(OutboxStatus.PENDING, outboxEntries.get(0).status);

    var myOrders = orderPlacementApi.myOrders();
    assertEquals(1, myOrders.size());
    assertFalse(myOrders.get(0).lines().isEmpty());
  }

  private void persistBuyer(String username) {
    var user = new SysUser();
    user.username = username;
    user.password = "{noop}ignored";
    entityManager.persist(user);
  }

  private Long persistProduct(String name, long priceMinor, int stock) {
    var product = new ExampleProductEntity();
    product.name = name;
    product.priceMinor = priceMinor;
    product.stock = stock;
    product.active = true;
    entityManager.persist(product);
    entityManager.flush();
    return product.id;
  }
}

package com.github.DaiYuANg.modules.example.application.port.in;

import com.github.DaiYuANg.modules.example.application.command.PlaceExampleOrderCommand;
import com.github.DaiYuANg.modules.example.application.readmodel.ExampleOrderView;
import java.util.List;

/** Inbound port: place and list orders for the current buyer. */
public interface ExampleOrderPlacementApi {

  ExampleOrderView placeOrder(PlaceExampleOrderCommand command);

  List<ExampleOrderView> myOrders();
}

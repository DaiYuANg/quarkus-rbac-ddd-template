package com.github.DaiYuANg.modules.example.application.port.driven;

import com.github.DaiYuANg.modules.example.application.dto.ExampleOrderLineView;
import com.github.DaiYuANg.modules.example.application.dto.ExampleOrderView;
import java.util.List;

public interface ExampleOrderStore {

  ExampleOrderView create(String buyerUsername, List<ExampleOrderLineView> lines, long totalMinor);

  List<ExampleOrderView> listByBuyer(String buyerUsername);
}

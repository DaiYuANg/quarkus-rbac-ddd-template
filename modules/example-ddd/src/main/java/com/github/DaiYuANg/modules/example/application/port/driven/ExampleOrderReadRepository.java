package com.github.DaiYuANg.modules.example.application.port.driven;

import com.github.DaiYuANg.modules.example.application.readmodel.ExampleOrderView;
import java.util.List;

public interface ExampleOrderReadRepository {

  List<ExampleOrderView> listByBuyer(String buyerUsername);
}

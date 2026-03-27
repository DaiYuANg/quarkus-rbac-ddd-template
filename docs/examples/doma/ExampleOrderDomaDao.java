package com.github.DaiYuANg.modules.example.infrastructure.persistence.doma;

import java.util.List;
import org.seasar.doma.Dao;
import org.seasar.doma.Select;

@Dao
public interface ExampleOrderDomaDao {

  @Select
  List<ExampleOrderSummaryRow> selectByBuyer(String buyerUsername);
}

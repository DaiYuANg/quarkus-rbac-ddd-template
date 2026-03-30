package com.github.DaiYuANg.modules.example.application.mapper;

import com.github.DaiYuANg.modules.example.application.readmodel.ExampleOrderLineView;
import com.github.DaiYuANg.modules.example.application.readmodel.ExampleOrderView;
import com.github.DaiYuANg.modules.example.domain.model.order.ExampleOrder;
import com.github.DaiYuANg.modules.example.domain.model.order.ExampleOrderLine;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI)
public interface ExampleOrderViewMapper {
  ExampleOrderLineView toLineView(ExampleOrderLine line);

  @Mapping(target = "status", expression = "java(order.status() == null ? null : order.status().name())")
  ExampleOrderView toView(ExampleOrder order);
}

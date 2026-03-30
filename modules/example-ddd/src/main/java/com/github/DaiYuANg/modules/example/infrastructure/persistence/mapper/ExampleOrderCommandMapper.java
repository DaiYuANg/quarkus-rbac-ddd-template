package com.github.DaiYuANg.modules.example.infrastructure.persistence.mapper;

import com.github.DaiYuANg.modules.example.domain.model.order.ExampleOrder;
import com.github.DaiYuANg.modules.example.domain.model.order.ExampleOrderLine;
import com.github.DaiYuANg.modules.example.infrastructure.persistence.ExampleOrderEntity;
import com.github.DaiYuANg.modules.example.infrastructure.persistence.ExampleOrderLineEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI)
public interface ExampleOrderCommandMapper {
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createAt", ignore = true)
  @Mapping(target = "updateAt", ignore = true)
  @Mapping(target = "createBy", ignore = true)
  @Mapping(target = "updateBy", ignore = true)
  @Mapping(target = "lines", ignore = true)
  ExampleOrderEntity toEntity(ExampleOrder order);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createAt", ignore = true)
  @Mapping(target = "updateAt", ignore = true)
  @Mapping(target = "createBy", ignore = true)
  @Mapping(target = "updateBy", ignore = true)
  @Mapping(target = "order", source = "order")
  ExampleOrderLineEntity toEntity(ExampleOrderLine line, ExampleOrderEntity order);
}

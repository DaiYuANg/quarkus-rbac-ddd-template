package com.github.DaiYuANg.modules.example.infrastructure.persistence.mapper;

import com.github.DaiYuANg.modules.example.application.readmodel.ExampleOrderLineView;
import com.github.DaiYuANg.modules.example.application.readmodel.ExampleOrderView;
import com.github.DaiYuANg.modules.example.infrastructure.persistence.ExampleOrderEntity;
import com.github.DaiYuANg.modules.example.infrastructure.persistence.ExampleOrderLineEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI)
public interface ExampleOrderReadMapper {
  ExampleOrderLineView toLineView(ExampleOrderLineEntity entity);

  @Mapping(target = "status", expression = "java(entity.status == null ? null : entity.status.name())")
  ExampleOrderView toView(ExampleOrderEntity entity);
}

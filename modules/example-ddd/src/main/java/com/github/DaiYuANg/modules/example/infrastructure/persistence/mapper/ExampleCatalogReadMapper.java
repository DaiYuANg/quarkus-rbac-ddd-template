package com.github.DaiYuANg.modules.example.infrastructure.persistence.mapper;

import com.github.DaiYuANg.modules.example.application.readmodel.ExampleProductView;
import com.github.DaiYuANg.modules.example.domain.model.catalog.ExampleProductSnapshot;
import com.github.DaiYuANg.modules.example.infrastructure.persistence.ExampleProductEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI)
public interface ExampleCatalogReadMapper {
  ExampleProductView toView(ExampleProductEntity entity);

  ExampleProductSnapshot toSnapshot(ExampleProductEntity entity);
}

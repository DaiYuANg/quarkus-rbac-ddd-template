package com.github.DaiYuANg.modules.example.infrastructure.persistence.mapper;

import com.github.DaiYuANg.modules.example.application.command.CreateExampleProductCommand;
import com.github.DaiYuANg.modules.example.infrastructure.persistence.ExampleProductEntity;
import lombok.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI)
public interface ExampleCatalogCommandMapper {
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createAt", ignore = true)
  @Mapping(target = "updateAt", ignore = true)
  @Mapping(target = "createBy", ignore = true)
  @Mapping(target = "updateBy", ignore = true)
  @Mapping(target = "active", constant = "true")
  ExampleProductEntity toEntity(@NonNull CreateExampleProductCommand command);
}

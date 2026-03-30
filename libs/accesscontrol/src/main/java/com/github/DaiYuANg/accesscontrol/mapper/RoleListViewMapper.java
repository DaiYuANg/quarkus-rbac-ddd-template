package com.github.DaiYuANg.accesscontrol.mapper;

import com.github.DaiYuANg.accesscontrol.projection.RoleListProjection;
import com.github.DaiYuANg.accesscontrol.view.RoleListView;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI)
public interface RoleListViewMapper {
  @Mapping(target = "status", expression = "java(view.getStatus() == null ? null : view.getStatus().name())")
  RoleListProjection toProjection(RoleListView view);
}

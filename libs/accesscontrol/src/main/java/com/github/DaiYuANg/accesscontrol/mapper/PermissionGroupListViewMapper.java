package com.github.DaiYuANg.accesscontrol.mapper;

import com.github.DaiYuANg.accesscontrol.projection.PermissionGroupListProjection;
import com.github.DaiYuANg.accesscontrol.view.PermissionGroupListView;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI)
public interface PermissionGroupListViewMapper {
  PermissionGroupListProjection toProjection(PermissionGroupListView view);
}

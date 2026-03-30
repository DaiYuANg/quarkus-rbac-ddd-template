package com.github.DaiYuANg.accesscontrol.mapper;

import com.github.DaiYuANg.accesscontrol.projection.PermissionListProjection;
import com.github.DaiYuANg.accesscontrol.view.PermissionListView;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI)
public interface PermissionListViewMapper {
  PermissionListProjection toProjection(PermissionListView view);
}

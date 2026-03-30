package com.github.DaiYuANg.identity.mapper;

import com.github.DaiYuANg.identity.projection.UserListProjection;
import com.github.DaiYuANg.identity.view.UserListView;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI)
public interface UserListViewMapper {
  @Mapping(target = "userStatus", expression = "java(view.getUserStatus() == null ? null : view.getUserStatus().name())")
  UserListProjection toProjection(UserListView view);
}

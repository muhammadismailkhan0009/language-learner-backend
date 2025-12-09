package com.myriadcode.languagelearner.user_management.infra.jpa.mappers;

import com.myriadcode.languagelearner.user_management.domain.model.UserInfo;
import com.myriadcode.languagelearner.user_management.infra.jpa.entities.UserInfoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserInfoEntityMapper {
    UserInfoEntityMapper INSTANCE = Mappers.getMapper(UserInfoEntityMapper.class);

    @Mappings({
            @Mapping(target = "id", ignore = true)
    })
    UserInfoEntity toEntity(UserInfo userInfo, String id);

    @Mappings({
            @Mapping(target = "id.id", source = "id")
    })
    UserInfo toDomain(UserInfoEntity userInfoEntity);

}

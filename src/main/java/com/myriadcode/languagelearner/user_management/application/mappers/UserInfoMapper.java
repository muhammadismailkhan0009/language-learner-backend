package com.myriadcode.languagelearner.user_management.application.mappers;

import com.myriadcode.languagelearner.user_management.application.endpoints.user_info.request.UserInfoRequest;
import com.myriadcode.languagelearner.user_management.domain.model.UserInfo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserInfoMapper {

    UserInfoMapper INSTANCE = Mappers.getMapper(UserInfoMapper.class);

    @Mappings({
            @Mapping(target = "id", ignore = true)
    })
    UserInfo toDomain(UserInfoRequest userInfo);
}

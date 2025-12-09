package com.myriadcode.languagelearner.user_management.infra.jpa.adapters;

import com.myriadcode.languagelearner.user_management.domain.model.UserInfo;
import com.myriadcode.languagelearner.user_management.domain.repo.UserInfoRepo;
import com.myriadcode.languagelearner.user_management.infra.jpa.mappers.UserInfoEntityMapper;
import com.myriadcode.languagelearner.user_management.infra.jpa.repos.UserInfoJpaRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class DaoRepo implements UserInfoRepo {

    @Autowired
    private UserInfoJpaRepo userInfoJpaRepo;

    @Override
    public Optional<UserInfo> findByUserName(String username) {
        var entity = userInfoJpaRepo.findByUsername(username);
        return entity.map(UserInfoEntityMapper.INSTANCE::toDomain);
    }

    @Override
    public UserInfo save(UserInfo userInfo) {
        var id = UUID.randomUUID().toString();
        var entity = UserInfoEntityMapper.INSTANCE.toEntity(userInfo, id);
        entity.setId(id);
        var saved = userInfoJpaRepo.save(entity);
        return UserInfoEntityMapper.INSTANCE.toDomain(saved);
    }

    @Override
    public List<UserInfo> findAll() {
        var entities = userInfoJpaRepo.findAll();
        return entities.parallelStream().map(UserInfoEntityMapper.INSTANCE::toDomain).toList();
    }
}

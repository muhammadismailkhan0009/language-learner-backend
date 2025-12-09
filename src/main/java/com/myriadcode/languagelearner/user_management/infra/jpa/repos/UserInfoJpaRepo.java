package com.myriadcode.languagelearner.user_management.infra.jpa.repos;

import com.myriadcode.languagelearner.user_management.infra.jpa.entities.UserInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserInfoJpaRepo extends JpaRepository<UserInfoEntity, String> {
    Optional<UserInfoEntity> findByUsername(String username);
}

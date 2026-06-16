package com.myriadcode.languagelearner.user_management.infra.jpa.repos;

import com.myriadcode.languagelearner.user_management.infra.jpa.entities.UserProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileJpaRepo extends JpaRepository<UserProfileEntity, String> {
}

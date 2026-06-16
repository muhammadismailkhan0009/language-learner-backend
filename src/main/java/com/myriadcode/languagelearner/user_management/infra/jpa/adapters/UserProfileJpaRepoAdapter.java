package com.myriadcode.languagelearner.user_management.infra.jpa.adapters;

import com.myriadcode.languagelearner.common.enums.LanguageLevel;
import com.myriadcode.languagelearner.user_management.domain.model.UserProfile;
import com.myriadcode.languagelearner.user_management.domain.repo.UserProfileRepo;
import com.myriadcode.languagelearner.user_management.infra.jpa.entities.UserProfileEntity;
import com.myriadcode.languagelearner.user_management.infra.jpa.entities.UserInfoEntity;
import com.myriadcode.languagelearner.user_management.infra.jpa.repos.UserProfileJpaRepo;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserProfileJpaRepoAdapter implements UserProfileRepo {

    private final UserProfileJpaRepo userProfileJpaRepo;
    private final EntityManager entityManager;

    public UserProfileJpaRepoAdapter(UserProfileJpaRepo userProfileJpaRepo, EntityManager entityManager) {
        this.userProfileJpaRepo = userProfileJpaRepo;
        this.entityManager = entityManager;
    }

    @Override
    public Optional<UserProfile> findByUserId(String userId) {
        return userProfileJpaRepo.findById(userId).map(this::toDomain);
    }

    @Override
    public UserProfile save(UserProfile profile) {
        var entity = new UserProfileEntity();
        entity.setUserId(profile.userId());
        entity.setUserInfo(entityManager.getReference(UserInfoEntity.class, profile.userId()));
        entity.setDifficultyLevel(profile.difficultyLevel().name());
        entity.setCreatedAt(profile.createdAt());
        var saved = userProfileJpaRepo.save(entity);
        return toDomain(saved);
    }

    private UserProfile toDomain(UserProfileEntity entity) {
        return new UserProfile(
                entity.getUserId(),
                LanguageLevel.from(entity.getDifficultyLevel()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}

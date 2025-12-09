package com.myriadcode.languagelearner.language_content.infra.jpa.repos;

import com.myriadcode.languagelearner.language_content.infra.jpa.entities.UserStatsForContentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface UserStatsJpaRepo extends JpaRepository<UserStatsForContentEntity, String> {
    List<UserStatsForContentEntity> findAllByUserIdIn(Collection<String> userIds);
}

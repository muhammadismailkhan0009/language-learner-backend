package com.myriadcode.languagelearner.language_content.infra.jpa.repos;

import com.myriadcode.languagelearner.language_content.infra.jpa.entities.ChunkEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChunkEntityJpaRepo extends JpaRepository<ChunkEntity, String> {
}

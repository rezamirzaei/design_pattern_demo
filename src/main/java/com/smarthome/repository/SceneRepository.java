package com.smarthome.repository;

import com.smarthome.domain.SceneEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SceneRepository extends JpaRepository<SceneEntity, Long> {
    Optional<SceneEntity> findByName(String name);
    List<SceneEntity> findByIsFavorite(Boolean isFavorite);
}

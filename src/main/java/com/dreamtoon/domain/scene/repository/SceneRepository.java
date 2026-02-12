package com.dreamtoon.domain.scene.repository;

import com.dreamtoon.domain.scene.entity.Scene;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SceneRepository extends JpaRepository<Scene, Long> {
    
    List<Scene> findByDreamIdOrderByCutOrderAsc(Long dreamId);
}

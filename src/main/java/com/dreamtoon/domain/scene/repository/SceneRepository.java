package com.dreamtoon.domain.scene.repository;

import com.dreamtoon.domain.scene.entity.Scene;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SceneRepository extends JpaRepository<Scene, Long> {

    List<Scene> findByDreamIdOrderByCutOrderAsc(Long dreamId);
}

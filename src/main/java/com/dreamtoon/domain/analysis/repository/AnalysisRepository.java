package com.dreamtoon.domain.analysis.repository;

import com.dreamtoon.domain.analysis.entity.Analysis;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnalysisRepository extends JpaRepository<Analysis, Long> {

    Optional<Analysis> findByDreamId(Long dreamId);
}

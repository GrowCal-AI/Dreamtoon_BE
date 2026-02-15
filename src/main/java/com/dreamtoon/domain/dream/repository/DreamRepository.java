package com.dreamtoon.domain.dream.repository;

import com.dreamtoon.domain.dream.entity.Dream;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DreamRepository extends JpaRepository<Dream, Long> {

        @Query("SELECT d FROM Dream d WHERE d.user.id = :userId ORDER BY d.createdAt DESC")
        Page<Dream> findByUserId(@Param("userId") Long userId, Pageable pageable);

        @Query(
                        "SELECT d FROM Dream d "
                                        + "LEFT JOIN FETCH d.scenes "
                                        + "LEFT JOIN FETCH d.analysis "
                                        + "WHERE d.id = :id")
        Dream findByIdWithDetails(@Param("id") Long id);
}

package com.dreamtoon.domain.dream.repository;

import com.dreamtoon.domain.dream.entity.Dream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DreamRepository extends JpaRepository<Dream, Long>, DreamRepositoryCustom {

    @Query("SELECT d FROM Dream d WHERE d.user.id = :userId ORDER BY d.createdAt DESC")
    Page<Dream> findByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query(
            "SELECT d FROM Dream d WHERE d.user.id = :userId AND d.createdAt BETWEEN :startDate AND"
                    + " :endDate ORDER BY d.createdAt DESC")
    List<Dream> findByUserIdAndCreatedAtBetween(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT d FROM Dream d WHERE d.id = :id AND d.user.id = :userId")
    Optional<Dream> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Query("SELECT d FROM Dream d WHERE d.id = :id AND d.guestToken = :guestToken")
    Optional<Dream> findByIdAndGuestToken(
            @Param("id") Long id, @Param("guestToken") String guestToken);

    @Query("SELECT COUNT(d) FROM Dream d WHERE d.user.id = :userId AND d.isInLibrary = true")
    long countLibraryByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(d) FROM Dream d WHERE d.user.id = :userId AND d.isFavorite = true")
    long countFavoritesByUserId(@Param("userId") Long userId);
}

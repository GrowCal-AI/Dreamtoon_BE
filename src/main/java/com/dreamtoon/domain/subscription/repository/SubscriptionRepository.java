package com.dreamtoon.domain.subscription.repository;

import com.dreamtoon.domain.subscription.entity.Subscription;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    @Query("SELECT s FROM Subscription s WHERE s.user.id = :userId")
    Optional<Subscription> findByUserId(@Param("userId") Long userId);

    @Query("SELECT s FROM Subscription s WHERE s.user.id = :userId")
    Subscription findByUserIdOrThrow(@Param("userId") Long userId);

    @Query("SELECT s FROM Subscription s WHERE s.polarSubscriptionId = :polarSubscriptionId")
    Optional<Subscription> findByPolarSubscriptionId(
            @Param("polarSubscriptionId") String polarSubscriptionId);

    /** 월별 쿼터 리셋 대상 조회 (리셋일이 오늘 이전인 활성 구독) */
    @Query("SELECT s FROM Subscription s WHERE s.quotaResetDate <= :today AND s.isActive = true")
    java.util.List<Subscription> findAllDueForReset(@Param("today") java.time.LocalDate today);

    /** 구독 종료일이 지난 유료 구독 조회 (FREE 외) */
    @Query(
            "SELECT s FROM Subscription s WHERE s.subscriptionEndDate IS NOT NULL AND"
                    + " s.subscriptionEndDate < :today AND s.tier <>"
                    + " com.dreamtoon.domain.subscription.entity.SubscriptionTier.FREE")
    java.util.List<Subscription> findAllExpired(@Param("today") java.time.LocalDate today);
}

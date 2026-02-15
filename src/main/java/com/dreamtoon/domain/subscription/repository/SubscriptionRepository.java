package com.dreamtoon.domain.subscription.repository;

import com.dreamtoon.domain.subscription.entity.Subscription;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    /**
     * 사용자 ID로 구독 정보 조회
     *
     * @param userId 사용자 ID
     * @return 구독 정보
     */
    @Query("SELECT s FROM Subscription s WHERE s.user.id = :userId")
    Optional<Subscription> findByUserId(@Param("userId") Long userId);

    /**
     * 사용자 ID로 구독 정보 조회 (없으면 예외 발생)
     *
     * @param userId 사용자 ID
     * @return 구독 정보
     */
    @Query("SELECT s FROM Subscription s WHERE s.user.id = :userId")
    Subscription findByUserIdOrThrow(@Param("userId") Long userId);
}

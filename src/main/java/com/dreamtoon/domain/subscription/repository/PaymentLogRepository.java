package com.dreamtoon.domain.subscription.repository;

import com.dreamtoon.domain.subscription.entity.PaymentEventType;
import com.dreamtoon.domain.subscription.entity.PaymentLog;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentLogRepository extends JpaRepository<PaymentLog, Long> {

    /** 특정 사용자의 결제 로그 (최신순, 페이지네이션) */
    Page<PaymentLog> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /** 이벤트 타입별 조회 */
    List<PaymentLog> findByEventTypeOrderByCreatedAtDesc(PaymentEventType eventType);

    /** 기간별 조회 */
    @Query(
            "SELECT p FROM PaymentLog p WHERE p.createdAt BETWEEN :start AND :end ORDER BY"
                    + " p.createdAt DESC")
    List<PaymentLog> findByCreatedAtBetween(
            @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}

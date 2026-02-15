package com.dreamtoon.domain.conversation.repository;

import com.dreamtoon.domain.conversation.entity.StressAssessment;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StressAssessmentRepository extends JpaRepository<StressAssessment, Long> {

        /**
         * 대화의 스트레스 평가 조회
         *
         * @param conversationId 대화 ID
         * @return 스트레스 평가
         */
        @Query("SELECT sa FROM StressAssessment sa WHERE sa.conversation.id = :conversationId")
        Optional<StressAssessment> findByConversationId(@Param("conversationId") Long conversationId);
}

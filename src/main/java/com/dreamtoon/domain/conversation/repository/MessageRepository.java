package com.dreamtoon.domain.conversation.repository;

import com.dreamtoon.domain.conversation.entity.Message;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MessageRepository extends JpaRepository<Message, Long> {

        /**
         * 대화의 모든 메시지 조회 (시간순)
         *
         * @param conversationId 대화 ID
         * @return 메시지 목록
         */
        @Query(
                        "SELECT m FROM Message m WHERE m.conversation.id = :conversationId ORDER BY m.createdAt"
                                        + " ASC")
        List<Message> findByConversationId(@Param("conversationId") Long conversationId);

        /**
         * 대화의 메시지 개수 조회
         *
         * @param conversationId 대화 ID
         * @return 메시지 개수
         */
        @Query("SELECT COUNT(m) FROM Message m WHERE m.conversation.id = :conversationId")
        long countByConversationId(@Param("conversationId") Long conversationId);
}

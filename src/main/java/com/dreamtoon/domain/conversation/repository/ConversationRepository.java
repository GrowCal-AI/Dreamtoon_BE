package com.dreamtoon.domain.conversation.repository;

import com.dreamtoon.domain.conversation.entity.Conversation;
import com.dreamtoon.domain.conversation.entity.ConversationStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    /**
     * 사용자의 모든 대화 조회 (최신순)
     *
     * @param userId 사용자 ID
     * @return 대화 목록
     */
    @Query("SELECT c FROM Conversation c WHERE c.user.id = :userId ORDER BY c.createdAt DESC")
    List<Conversation> findByUserId(@Param("userId") Long userId);

    /**
     * 사용자의 특정 상태 대화 조회
     *
     * @param userId 사용자 ID
     * @param status 대화 상태
     * @return 대화 목록
     */
    @Query(
            "SELECT c FROM Conversation c WHERE c.user.id = :userId AND c.status = :status ORDER BY"
                    + " c.createdAt DESC")
    List<Conversation> findByUserIdAndStatus(
            @Param("userId") Long userId, @Param("status") ConversationStatus status);

    /**
     * 사용자의 진행 중인 대화 조회 (가장 최근 1개)
     *
     * @param userId 사용자 ID
     * @return 진행 중인 대화
     */
    @Query(
            "SELECT c FROM Conversation c WHERE c.user.id = :userId AND c.status = 'IN_PROGRESS'"
                    + " ORDER BY c.createdAt DESC")
    Optional<Conversation> findInProgressConversation(@Param("userId") Long userId);
}

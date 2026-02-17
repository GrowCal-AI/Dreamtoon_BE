package com.dreamtoon.domain.dreamchat.repository;

import com.dreamtoon.domain.dreamchat.entity.ChatRole;
import com.dreamtoon.domain.dreamchat.entity.DreamChat;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DreamChatRepository extends JpaRepository<DreamChat, Long> {

    List<DreamChat> findByDreamIdOrderByCreatedAtAsc(Long dreamId);

    long countByDreamIdAndRole(Long dreamId, ChatRole role);
}

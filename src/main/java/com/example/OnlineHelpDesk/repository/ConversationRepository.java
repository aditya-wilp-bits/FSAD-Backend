package com.example.OnlineHelpDesk.repository;

import com.example.OnlineHelpDesk.model.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Integer> {

    List<Conversation> findAllByRequestId(Integer userId);
}

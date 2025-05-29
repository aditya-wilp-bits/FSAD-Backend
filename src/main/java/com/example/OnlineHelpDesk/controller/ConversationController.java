package com.example.OnlineHelpDesk.controller;

import com.example.OnlineHelpDesk.model.Conversation;
import com.example.OnlineHelpDesk.model.Facility;
import com.example.OnlineHelpDesk.model.User;
import com.example.OnlineHelpDesk.repository.ConversationRepository;
import com.example.OnlineHelpDesk.repository.RequestRepository;
import com.example.OnlineHelpDesk.repository.UserRepository;
import com.example.OnlineHelpDesk.vo.ConversationRequestVo;
import com.example.OnlineHelpDesk.vo.ConversationVo;
import com.example.OnlineHelpDesk.vo.MessageResponseVo;
import com.example.OnlineHelpDesk.vo.UserInfoResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
public class ConversationController {

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RequestRepository requestRepository;

    @GetMapping("/conversations/{id}")
    public ResponseEntity<?> getConversationByRequestId(@PathVariable("id") Integer id) {
        List<Conversation> conversations = conversationRepository.findAllByRequestId(id);
        List<ConversationVo> conversationVos = new ArrayList<>();
        for (Conversation conversation : conversations) {
            ConversationVo conversationVo = new ConversationVo();
            conversationVo.setId(conversation.getId());
            conversationVo.setRequestId(conversation.getRequestId());
            conversationVo.setText(conversation.getMessage());
            conversationVo.setUser(getUserInfoVo(conversation.getUserId()));
            conversationVo.setCreatedAt(conversation.getCreatedAt());
            conversationVos.add(conversationVo);
        }
        return ResponseEntity.ok(conversationVos);
    }

    @PostMapping("/conversations/{id}")
    public ResponseEntity<?> createConversation(@PathVariable("id") Integer id, @RequestBody ConversationRequestVo conversationRequestVo,
                                                Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Conversation conversation = new Conversation();
        conversation.setRequestId(id);
        conversation.setMessage(conversationRequestVo.getText());
        conversation.setCreatedAt(new Date());
        conversation.setUserId(user.getId());
        conversationRepository.save(conversation);
        return ResponseEntity.ok(new MessageResponseVo(false, "Conversation Created"));
    }

    private UserInfoResponseVo getUserInfoVo(Integer id){
        User user = userRepository.findById(id).get();
        UserInfoResponseVo userInfoResponseVo = new UserInfoResponseVo();
        userInfoResponseVo.setId(user.getId());
        userInfoResponseVo.setEmail(user.getUsername());
        userInfoResponseVo.setName(user.getFirstName() + " " + user.getLastName());
        userInfoResponseVo.setFirstName(user.getFirstName());
        userInfoResponseVo.setLastName(user.getLastName());
        userInfoResponseVo.setFacilityId(user.getFacilityId());
        return userInfoResponseVo;
    }
}

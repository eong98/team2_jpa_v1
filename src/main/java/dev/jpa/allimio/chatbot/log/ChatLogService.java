package dev.jpa.allimio.chatbot.log;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jpa.allimio.chatbot.session.ChatSessionRepository;
import dev.jpa.allimio.tool.Tool;

@Service
public class ChatLogService {
  @Autowired
  ChatLogRepository chatLogRepository;

  @Autowired
  ChatSessionRepository chatSessionRepository;

  /**
   * 로그 한 건 기록. 세션의 UDATE(마지막 활동 시각)도 같이 갱신합니다.
   */
  @Transactional
  public ChatLogDTO.Response create(ChatLogDTO.Request req) {
    ChatLog log = ChatLog.builder()
        .sno(req.getSno())
        .sender(req.getSender())
        .mtype(req.getMtype())
        .content(req.getContent())
        .cno(req.getCno())
        .cdate(Tool.getDate())
        .build();

    ChatLog saved = chatLogRepository.save(log);

    chatSessionRepository.findById(req.getSno()).ifPresent(session -> {
      session.setUdate(saved.getCdate()); // Dirty Checking으로 자동 반영
    });

    return ChatLogDTO.Response.from(saved);
  }

  /**
   * 특정 세션의 전체 대화 로그 (세션 재진입 시 bubbles 복원용).
   */
  @Transactional(readOnly = true)
  public List<ChatLogDTO.Response> findBySno(String sno) {
    return chatLogRepository.findBySnoOrderByNoAsc(sno)
        .stream().map(ChatLogDTO.Response::from).collect(Collectors.toList());
  }
}
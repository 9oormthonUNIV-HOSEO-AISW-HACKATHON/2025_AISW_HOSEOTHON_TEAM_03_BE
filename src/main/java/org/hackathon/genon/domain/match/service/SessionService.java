package org.hackathon.genon.domain.match.service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
@Service
public class SessionService {

    private final ConcurrentHashMap<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ExecutorService executorService = Executors.newFixedThreadPool(20);

    public void register(Long memberId, WebSocketSession session) {
        sessions.put(memberId, session);
    }

    public void remove(Long memberId) {
        sessions.remove(memberId);
    }

    public void sendTo(Long memberId, String payload) {
        WebSocketSession session = sessions.get(memberId);
        if (session == null || !session.isOpen()) {
            log.warn("[WS] 세션이 없거나 닫힘. memberId={}", memberId);
            return;
        }
        executorService.submit(() -> {
            try {
                session.sendMessage(new TextMessage(payload));
            } catch (Exception e) {
                log.error("[WS] 메시지 전송 실패. memberId={}, payload={}", memberId, payload, e);
            }
        });
    }
}

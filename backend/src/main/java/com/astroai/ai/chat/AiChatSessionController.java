package com.astroai.ai.chat;

import com.astroai.ai.chat.dto.ChatMessageResponseDto;
import com.astroai.ai.chat.dto.ChatSessionResponseDto;
import com.astroai.ai.chat.dto.CreateChatSessionRequestDto;
import com.astroai.ai.chat.dto.ExplainabilityTraceDto;
import com.astroai.ai.chat.dto.SendChatMessageRequestDto;
import com.astroai.common.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai/chat-sessions")
public class AiChatSessionController {

    private final AiChatSessionService chatSessionService;

    public AiChatSessionController(AiChatSessionService chatSessionService) {
        this.chatSessionService = chatSessionService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ChatSessionResponseDto>> createSession(
            @Valid @RequestBody CreateChatSessionRequestDto request
    ) {
        ChatSessionResponseDto session = chatSessionService.createSession(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Consultation chat session created", session));
    }

    @GetMapping("/profile/{birthProfileId}")
    public ResponseEntity<ApiResponse<List<ChatSessionResponseDto>>> getSessionsForProfile(
            @PathVariable UUID birthProfileId
    ) {
        List<ChatSessionResponseDto> sessions = chatSessionService.getSessionsForProfile(birthProfileId);
        return ResponseEntity.ok(ApiResponse.ok("Chat sessions retrieved", sessions));
    }

    @GetMapping("/{sessionId}/messages")
    public ResponseEntity<ApiResponse<List<ChatMessageResponseDto>>> getSessionMessages(
            @PathVariable UUID sessionId
    ) {
        List<ChatMessageResponseDto> messages = chatSessionService.getMessagesForSession(sessionId);
        return ResponseEntity.ok(ApiResponse.ok("Session messages retrieved", messages));
    }

    @PostMapping("/{sessionId}/messages")
    public ResponseEntity<ApiResponse<ChatMessageResponseDto>> sendMessage(
            @PathVariable UUID sessionId,
            @Valid @RequestBody SendChatMessageRequestDto request
    ) {
        ChatMessageResponseDto response = chatSessionService.sendMessage(sessionId, request);
        return ResponseEntity.ok(ApiResponse.ok("Consultation response generated", response));
    }

    @GetMapping("/messages/{messageId}/explain")
    public ResponseEntity<ApiResponse<ExplainabilityTraceDto>> getExplainabilityTrace(
            @PathVariable UUID messageId
    ) {
        ExplainabilityTraceDto trace = chatSessionService.getExplainabilityTrace(messageId);
        return ResponseEntity.ok(ApiResponse.ok("Explainability trace retrieved", trace));
    }
}

package com.astroai.ai.chat;

import com.astroai.ai.AiService;
import com.astroai.ai.chat.dto.ChatMessageResponseDto;
import com.astroai.ai.chat.dto.ChatSessionResponseDto;
import com.astroai.ai.chat.dto.CreateChatSessionRequestDto;
import com.astroai.ai.chat.dto.ExplainabilityTraceDto;
import com.astroai.ai.chat.dto.SendChatMessageRequestDto;
import com.astroai.ai.dto.AiChatRequestDto;
import com.astroai.ai.dto.AiChatResponseDto;
import com.astroai.birth.BirthProfileService;
import com.astroai.common.ResourceNotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AiChatSessionService {

    private static final Logger log = LoggerFactory.getLogger(AiChatSessionService.class);

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final BirthProfileService birthProfileService;
    private final AiService aiService;
    private final ObjectMapper objectMapper;

    public AiChatSessionService(
            ChatSessionRepository chatSessionRepository,
            ChatMessageRepository chatMessageRepository,
            BirthProfileService birthProfileService,
            AiService aiService,
            ObjectMapper objectMapper
    ) {
        this.chatSessionRepository = chatSessionRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.birthProfileService = birthProfileService;
        this.aiService = aiService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ChatSessionResponseDto createSession(CreateChatSessionRequestDto request) {
        UUID userId = request.userId() != null ? request.userId() : BirthProfileService.DEFAULT_SYSTEM_USER_ID;
        birthProfileService.getProfile(request.birthProfileId());

        String title = request.title() != null && !request.title().isBlank()
                ? request.title()
                : "Vedic Astrology Consultation";

        Instant now = Instant.now();
        ChatSession session = new ChatSession(
                UUID.randomUUID(),
                userId,
                request.birthProfileId(),
                title,
                "",
                now,
                now
        );

        ChatSession saved = chatSessionRepository.save(session);
        return toSessionDto(saved);
    }

    public List<ChatSessionResponseDto> getSessionsForProfile(UUID birthProfileId) {
        return chatSessionRepository.findByBirthProfileIdOrderByUpdatedAtDesc(birthProfileId).stream()
                .map(this::toSessionDto)
                .toList();
    }

    public List<ChatMessageResponseDto> getMessagesForSession(UUID sessionId) {
        return chatMessageRepository.findByChatSessionIdOrderByCreatedAtAsc(sessionId).stream()
                .map(this::toMessageDto)
                .toList();
    }

    @Transactional
    public ChatMessageResponseDto sendMessage(UUID sessionId, SendChatMessageRequestDto request) {
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("ChatSession not found with id: " + sessionId));

        Instant now = Instant.now();

        // 1. Record User message
        ChatMessage userMsg = new ChatMessage(
                UUID.randomUUID(),
                sessionId,
                null,
                "USER",
                request.message(),
                null,
                0,
                0,
                now
        );
        chatMessageRepository.save(userMsg);

        // 2. Invoke AI Service
        String domain = request.domainCategory() != null ? request.domainCategory() : "GENERAL";
        boolean includeReasoning = request.includeReasoning() == null || request.includeReasoning();

        AiChatRequestDto aiReq = new AiChatRequestDto(
                session.getBirthProfileId(),
                request.message(),
                domain,
                null,
                null,
                includeReasoning,
                Collections.emptyMap()
        );

        AiChatResponseDto aiRes = aiService.chat(aiReq);

        // 3. Assemble deep Why This Answer explainability trace
        UUID messageId = UUID.randomUUID();
        Map<String, Object> groundTruth = aiRes.groundTruthContext() != null ? aiRes.groundTruthContext() : Collections.emptyMap();

        String activeDasha = groundTruth.containsKey("active_dasha")
                ? groundTruth.get("active_dasha").toString()
                : "Standard Vimshottari Timeline";

        String overallVerdict = groundTruth.containsKey("overall_verdict")
                ? groundTruth.get("overall_verdict").toString()
                : "Traceable Classical Shastric Evaluation";

        double compositeScore = 0.85;
        if (groundTruth.containsKey("composite_score") && groundTruth.get("composite_score") instanceof Number num) {
            compositeScore = num.doubleValue();
        }

        @SuppressWarnings("unchecked")
        List<String> remedies = groundTruth.containsKey("classical_remedies") && groundTruth.get("classical_remedies") instanceof List<?> list
                ? (List<String>) list
                : List.of("Daily Gayatri Mantra chanting", "Annadana (Charity)", "Satya & Dharmic Conduct");

        ExplainabilityTraceDto trace = new ExplainabilityTraceDto(
                messageId,
                sessionId,
                session.getBirthProfileId(),
                groundTruth,
                List.of("D1 Natal Rashi", "D9 Navamsha (Dharma & Micro-potency)"),
                activeDasha,
                overallVerdict,
                compositeScore,
                List.of("Brihat Parashara Hora Shastra (BPHS)", "Phaladeepika", "Jaimini Upadesha Sutras"),
                remedies,
                aiRes.guardrailResult(),
                aiRes.provider(),
                aiRes.model()
        );

        String traceJson = null;
        try {
            traceJson = objectMapper.writeValueAsString(trace);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize explainability trace: {}", e.getMessage());
        }

        // 4. Save Assistant response message
        ChatMessage assistantMsg = new ChatMessage(
                messageId,
                sessionId,
                null,
                "ASSISTANT",
                aiRes.response(),
                traceJson,
                aiRes.promptTokens(),
                aiRes.completionTokens(),
                Instant.now()
        );
        chatMessageRepository.save(assistantMsg);

        // Update session
        session.setUpdatedAt(Instant.now());
        if ("Vedic Astrology Consultation".equals(session.getTitle()) && request.message().length() > 3) {
            String newTitle = request.message().length() > 40
                    ? request.message().substring(0, 40) + "..."
                    : request.message();
            session.setTitle(newTitle);
        }
        chatSessionRepository.save(session);

        return new ChatMessageResponseDto(
                assistantMsg.getId(),
                assistantMsg.getChatSessionId(),
                assistantMsg.getSenderRole(),
                assistantMsg.getMessageContent(),
                trace,
                assistantMsg.getPromptTokens(),
                assistantMsg.getCompletionTokens(),
                assistantMsg.getCreatedAt()
        );
    }

    public ExplainabilityTraceDto getExplainabilityTrace(UUID messageId) {
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("ChatMessage not found with id: " + messageId));

        if (message.getStructuredWhyThisAnswerJson() == null || message.getStructuredWhyThisAnswerJson().isBlank()) {
            return null;
        }

        try {
            return objectMapper.readValue(message.getStructuredWhyThisAnswerJson(), ExplainabilityTraceDto.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse explainability trace for message {}: {}", messageId, e.getMessage());
            return null;
        }
    }

    @Transactional
    public void deleteSession(UUID sessionId) {
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("ChatSession not found with id: " + sessionId));
        chatSessionRepository.delete(session);
    }

    public List<ChatSessionResponseDto> getSessionsForUser(UUID userId) {
        return chatSessionRepository.findByUserIdOrderByUpdatedAtDesc(userId).stream()
                .map(this::toSessionDto)
                .toList();
    }

    public List<ChatSessionResponseDto> searchSessions(UUID userId, String query) {
        if (query == null || query.isBlank()) {
            return getSessionsForUser(userId);
        }
        String q = query.toLowerCase().trim();
        return chatSessionRepository.findByUserIdOrderByUpdatedAtDesc(userId).stream()
                .filter(s -> s.getTitle().toLowerCase().contains(q) ||
                        (s.getRollingSummary() != null && s.getRollingSummary().toLowerCase().contains(q)))
                .map(this::toSessionDto)
                .toList();
    }

    public String exportSessionAsMarkdown(UUID sessionId) {
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("ChatSession not found with id: " + sessionId));
        var profile = birthProfileService.getProfile(session.getBirthProfileId());
        List<ChatMessage> messages = chatMessageRepository.findByChatSessionIdOrderByCreatedAtAsc(sessionId);

        StringBuilder sb = new StringBuilder();
        sb.append("# Vedic Astrology Consultation Report\n\n");
        sb.append("**Session Title:** ").append(session.getTitle()).append("\n");
        sb.append("**Date of Consultation:** ").append(session.getUpdatedAt()).append("\n\n");
        sb.append("## Native Birth Particulars\n");
        sb.append("- **Name:** ").append(profile.name()).append("\n");
        sb.append("- **Date of Birth:** ").append(profile.dateOfBirth()).append("\n");
        sb.append("- **Time of Birth:** ").append(profile.timeOfBirth() != null ? profile.timeOfBirth() : "N/A").append("\n");
        sb.append("- **Place of Birth:** ").append(profile.placeOfBirth()).append("\n\n");
        sb.append("---\n\n");
        sb.append("## Multi-Turn Shastric Dialogue\n\n");

        for (ChatMessage m : messages) {
            if ("USER".equalsIgnoreCase(m.getSenderRole())) {
                sb.append("### 👤 Native Question\n");
                sb.append(m.getMessageContent()).append("\n\n");
            } else {
                sb.append("### 🪐 AstroAI Shastric Synthesis\n");
                sb.append(m.getMessageContent()).append("\n\n");

                ExplainabilityTraceDto trace = null;
                if (m.getStructuredWhyThisAnswerJson() != null) {
                    try {
                        trace = objectMapper.readValue(m.getStructuredWhyThisAnswerJson(), ExplainabilityTraceDto.class);
                    } catch (Exception ignored) {}
                }

                if (trace != null) {
                    sb.append("> **Why This Answer?**\n");
                    sb.append("> - **Active Dasha:** ").append(trace.activeDashaPeriod()).append("\n");
                    sb.append("> - **Shastric Verdict:** ").append(trace.reasoningVerdict()).append("\n");
                    sb.append(String.format("> - **Composite Score:** %.2f / 1.00\n", trace.compositeScore()));
                    if (trace.shastricCitations() != null && !trace.shastricCitations().isEmpty()) {
                        sb.append("> - **Canonical Citations:** ").append(String.join(", ", trace.shastricCitations())).append("\n");
                    }
                    if (trace.classicalRemedies() != null && !trace.classicalRemedies().isEmpty()) {
                        sb.append("> - **Prescribed Remedies:** ").append(String.join("; ", trace.classicalRemedies())).append("\n");
                    }
                    sb.append("\n");
                }
            }
        }

        sb.append("---\n");
        sb.append("*Generated by AstroAI Platform. Calculations powered deterministically by Swiss Ephemeris. Governed by Brihat Parashara Hora Shastra.*\n");
        return sb.toString();
    }

    private ChatSessionResponseDto toSessionDto(ChatSession session) {
        return new ChatSessionResponseDto(
                session.getId(),
                session.getUserId(),
                session.getBirthProfileId(),
                session.getTitle(),
                session.getRollingSummary(),
                session.getCreatedAt(),
                session.getUpdatedAt()
        );
    }

    private ChatMessageResponseDto toMessageDto(ChatMessage message) {
        ExplainabilityTraceDto trace = null;
        if (message.getStructuredWhyThisAnswerJson() != null && !message.getStructuredWhyThisAnswerJson().isBlank()) {
            try {
                trace = objectMapper.readValue(message.getStructuredWhyThisAnswerJson(), ExplainabilityTraceDto.class);
            } catch (Exception ignored) {}
        }

        return new ChatMessageResponseDto(
                message.getId(),
                message.getChatSessionId(),
                message.getSenderRole(),
                message.getMessageContent(),
                trace,
                message.getPromptTokens(),
                message.getCompletionTokens(),
                message.getCreatedAt()
        );
    }
}

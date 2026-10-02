package com.astroai.ai.chat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "chat_messages")
public class ChatMessage {

    @Id
    private UUID id;

    @Column(name = "chat_session_id", nullable = false)
    private UUID chatSessionId;

    @Column(name = "analysis_session_id")
    private UUID analysisSessionId;

    @Column(name = "sender_role", nullable = false)
    private String senderRole; // "USER", "ASSISTANT", "SYSTEM"

    @Column(name = "message_content", nullable = false)
    private String messageContent;

    @Column(name = "structured_why_this_answer_json")
    private String structuredWhyThisAnswerJson;

    @Column(name = "prompt_tokens", nullable = false)
    private int promptTokens;

    @Column(name = "completion_tokens", nullable = false)
    private int completionTokens;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public ChatMessage() {}

    public ChatMessage(
            UUID id,
            UUID chatSessionId,
            UUID analysisSessionId,
            String senderRole,
            String messageContent,
            String structuredWhyThisAnswerJson,
            int promptTokens,
            int completionTokens,
            Instant createdAt
    ) {
        this.id = id;
        this.chatSessionId = chatSessionId;
        this.analysisSessionId = analysisSessionId;
        this.senderRole = senderRole;
        this.messageContent = messageContent;
        this.structuredWhyThisAnswerJson = structuredWhyThisAnswerJson;
        this.promptTokens = promptTokens;
        this.completionTokens = completionTokens;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getChatSessionId() {
        return chatSessionId;
    }

    public void setChatSessionId(UUID chatSessionId) {
        this.chatSessionId = chatSessionId;
    }

    public UUID getAnalysisSessionId() {
        return analysisSessionId;
    }

    public void setAnalysisSessionId(UUID analysisSessionId) {
        this.analysisSessionId = analysisSessionId;
    }

    public String getSenderRole() {
        return senderRole;
    }

    public void setSenderRole(String senderRole) {
        this.senderRole = senderRole;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }

    public String getStructuredWhyThisAnswerJson() {
        return structuredWhyThisAnswerJson;
    }

    public void setStructuredWhyThisAnswerJson(String structuredWhyThisAnswerJson) {
        this.structuredWhyThisAnswerJson = structuredWhyThisAnswerJson;
    }

    public int getPromptTokens() {
        return promptTokens;
    }

    public void setPromptTokens(int promptTokens) {
        this.promptTokens = promptTokens;
    }

    public int getCompletionTokens() {
        return completionTokens;
    }

    public void setCompletionTokens(int completionTokens) {
        this.completionTokens = completionTokens;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}

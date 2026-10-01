package com.astroai.ai;

import com.astroai.ai.context.AstroContext;
import com.astroai.ai.context.AstroContextManager;
import com.astroai.ai.dto.AiChatRequestDto;
import com.astroai.ai.dto.AiChatResponseDto;
import com.astroai.ai.dto.ToolDefinitionDto;
import com.astroai.ai.dto.ToolExecutionRequestDto;
import com.astroai.ai.dto.ToolExecutionResponseDto;
import com.astroai.ai.guardrail.GuardrailValidationResult;
import com.astroai.ai.guardrail.HallucinationGuardrail;
import com.astroai.ai.provider.LlmChatRequest;
import com.astroai.ai.provider.LlmChatResponse;
import com.astroai.ai.provider.LlmProvider;
import com.astroai.ai.provider.LlmProviderFactory;
import com.astroai.ai.tool.AstrologyTool;
import com.astroai.ai.tool.AstrologyToolRegistry;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AiService {

    private static final Logger log = LoggerFactory.getLogger(AiService.class);

    private final AstroContextManager contextManager;
    private final AstrologyToolRegistry toolRegistry;
    private final LlmProviderFactory providerFactory;
    private final HallucinationGuardrail hallucinationGuardrail;

    public AiService(
            AstroContextManager contextManager,
            AstrologyToolRegistry toolRegistry,
            LlmProviderFactory providerFactory,
            HallucinationGuardrail hallucinationGuardrail
    ) {
        this.contextManager = contextManager;
        this.toolRegistry = toolRegistry;
        this.providerFactory = providerFactory;
        this.hallucinationGuardrail = hallucinationGuardrail;
    }

    public List<ToolDefinitionDto> getRegisteredTools() {
        return toolRegistry.getAllTools().stream()
                .map(tool -> new ToolDefinitionDto(
                        tool.getName(),
                        tool.getDescription(),
                        tool.getParameterSchema()
                ))
                .toList();
    }

    public ToolExecutionResponseDto executeTool(ToolExecutionRequestDto request) {
        if (request == null || request.toolName() == null || request.toolName().isBlank()) {
            return ToolExecutionResponseDto.fail("UNKNOWN", "Tool name must not be blank");
        }

        try {
            Map<String, Object> result = toolRegistry.executeTool(
                    request.toolName(),
                    request.arguments() != null ? request.arguments() : Collections.emptyMap()
            );
            return ToolExecutionResponseDto.ok(request.toolName(), result);
        } catch (Exception e) {
            log.error("Error executing astrology tool {}: {}", request.toolName(), e.getMessage());
            return ToolExecutionResponseDto.fail(request.toolName(), e.getMessage());
        }
    }

    public AiChatResponseDto chat(AiChatRequestDto request) {
        boolean includeReasoning = request.includeReasoning() == null || request.includeReasoning();
        AstroContext astroContext = contextManager.buildContext(
                request.birthProfileId(),
                request.userMessage(),
                request.domainCategory(),
                includeReasoning
        );

        LlmProvider provider = providerFactory.getProvider();
        List<AstrologyTool> tools = toolRegistry.getAllTools();

        LlmChatRequest llmRequest = new LlmChatRequest(
                request.userMessage() != null ? request.userMessage() : "Please provide a general astrological reading.",
                astroContext.systemInstruction(),
                Collections.emptyList(),
                tools,
                request.parameters() != null ? request.parameters() : Collections.emptyMap()
        );

        LlmChatResponse llmResponse = provider.generateResponse(llmRequest);

        // Audit response against ground truth facts using Hallucination Guardrail
        GuardrailValidationResult guardrailResult = hallucinationGuardrail.validateAndSanitize(
                llmResponse.content(),
                astroContext.groundTruthContext()
        );

        return new AiChatResponseDto(
                guardrailResult.auditedContent() != null && !guardrailResult.auditedContent().isBlank()
                        ? guardrailResult.auditedContent()
                        : llmResponse.content(),
                llmResponse.provider(),
                llmResponse.model(),
                guardrailResult,
                llmResponse.toolCalls(),
                astroContext.groundTruthContext(),
                llmResponse.promptTokens(),
                llmResponse.completionTokens()
        );
    }
}

package com.astroai.ai;

import com.astroai.ai.dto.AiChatRequestDto;
import com.astroai.ai.dto.AiChatResponseDto;
import com.astroai.ai.dto.ToolDefinitionDto;
import com.astroai.ai.dto.ToolExecutionRequestDto;
import com.astroai.ai.dto.ToolExecutionResponseDto;
import com.astroai.common.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @GetMapping("/tools")
    public ResponseEntity<ApiResponse<List<ToolDefinitionDto>>> getRegisteredTools() {
        List<ToolDefinitionDto> tools = aiService.getRegisteredTools();
        return ResponseEntity.ok(ApiResponse.ok("Registered deterministic astrology tools retrieved", tools));
    }

    @PostMapping("/tools/execute")
    public ResponseEntity<ApiResponse<ToolExecutionResponseDto>> executeTool(
            @Valid @RequestBody ToolExecutionRequestDto request
    ) {
        ToolExecutionResponseDto result = aiService.executeTool(request);
        return ResponseEntity.ok(ApiResponse.ok("Tool execution completed", result));
    }

    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<AiChatResponseDto>> chat(
            @Valid @RequestBody AiChatRequestDto request
    ) {
        AiChatResponseDto response = aiService.chat(request);
        return ResponseEntity.ok(ApiResponse.ok("AI astrological consultation generated successfully", response));
    }
}

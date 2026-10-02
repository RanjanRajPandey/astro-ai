import axios from 'axios';
import type {
  AiChatRequest,
  AiChatResponse,
  AspectCalculationResponse,
  BhavaBalaCalculationResponse,
  BirthProfile,
  ChatMessage,
  ChatSession,
  CreateBirthProfilePayload,
  CreateChatSessionPayload,
  DashaCalculationResponse,
  DivisionalCalculationResponse,
  EvidenceGenerationResponse,
  ExplainabilityTrace,
  GazetteerCity,
  KundliChartResponse,
  NakshatraCalculationResponse,
  QuestionClassificationResponse,
  ReasoningSynthesisResponse,
  SendChatMessagePayload,
  ShadbalaCalculationResponse,
  TemporalAnalysisResponse,
  ToolDefinition,
  ToolExecutionRequest,
  ToolExecutionResponse,
  TransitCalculationResponse,
  YogaCalculationResponse,
} from '../types/astrology';

interface ApiEnvelope<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 15000,
});

export async function searchCities(query: string): Promise<GazetteerCity[]> {
  const res = await http.get<ApiEnvelope<GazetteerCity[]>>('/locations/search', {
    params: { q: query },
  });
  return res.data.data;
}

export async function listBirthProfiles(): Promise<BirthProfile[]> {
  const res = await http.get<ApiEnvelope<BirthProfile[]>>('/birth-profiles');
  return res.data.data;
}

export async function createBirthProfile(payload: CreateBirthProfilePayload): Promise<BirthProfile> {
  const res = await http.post<ApiEnvelope<BirthProfile>>('/birth-profiles', payload);
  return res.data.data;
}

export async function deleteBirthProfile(id: string): Promise<void> {
  await http.delete(`/birth-profiles/${id}`);
}

export async function getD1Chart(birthProfileId: string): Promise<KundliChartResponse> {
  const res = await http.get<ApiEnvelope<KundliChartResponse>>(`/charts/${birthProfileId}/d1`);
  return res.data.data;
}

export async function getNakshatraAnalysis(
  birthProfileId: string,
): Promise<NakshatraCalculationResponse> {
  const res = await http.get<ApiEnvelope<NakshatraCalculationResponse>>(
    `/nakshatra/${birthProfileId}`,
  );
  return res.data.data;
}

export async function getVimshottariDashas(
  birthProfileId: string,
  targetTime?: string,
): Promise<DashaCalculationResponse> {
  const res = await http.get<ApiEnvelope<DashaCalculationResponse>>(`/dashas/${birthProfileId}`, {
    params: targetTime ? { targetTime } : undefined,
  });
  return res.data.data;
}

export async function getAllDivisionalCharts(
  birthProfileId: string,
): Promise<DivisionalCalculationResponse> {
  const res = await http.get<ApiEnvelope<DivisionalCalculationResponse>>(
    `/charts/${birthProfileId}/divisional`,
  );
  return res.data.data;
}

export async function getPlanetaryAspects(
  birthProfileId: string,
): Promise<AspectCalculationResponse> {
  const res = await http.get<ApiEnvelope<AspectCalculationResponse>>(
    `/aspects/${birthProfileId}`,
  );
  return res.data.data;
}

export async function getPlanetaryShadbala(
  birthProfileId: string,
): Promise<ShadbalaCalculationResponse> {
  const res = await http.get<ApiEnvelope<ShadbalaCalculationResponse>>(
    `/strengths/planets/${birthProfileId}`,
  );
  return res.data.data;
}

export async function getHouseBhavaBala(
  birthProfileId: string,
): Promise<BhavaBalaCalculationResponse> {
  const res = await http.get<ApiEnvelope<BhavaBalaCalculationResponse>>(
    `/strengths/houses/${birthProfileId}`,
  );
  return res.data.data;
}

export async function getYogasAndDoshas(
  birthProfileId: string,
): Promise<YogaCalculationResponse> {
  const res = await http.get<ApiEnvelope<YogaCalculationResponse>>(
    `/yogas/${birthProfileId}`,
  );
  return res.data.data;
}

export async function getGocharTransits(
  birthProfileId: string,
  transitTime?: string,
): Promise<TransitCalculationResponse> {
  const res = await http.get<ApiEnvelope<TransitCalculationResponse>>(
    `/transits/${birthProfileId}`,
    {
      params: transitTime ? { transitTime } : undefined,
    },
  );
  return res.data.data;
}

export async function getTemporalForecast(
  birthProfileId: string,
  anchorTime?: string,
): Promise<TemporalAnalysisResponse> {
  const res = await http.get<ApiEnvelope<TemporalAnalysisResponse>>(
    `/temporal/${birthProfileId}`,
    {
      params: anchorTime ? { anchorTime } : undefined,
    },
  );
  return res.data.data;
}

export async function classifyQuestionAndGetFrameworks(
  questionText?: string,
): Promise<QuestionClassificationResponse> {
  const res = await http.get<ApiEnvelope<QuestionClassificationResponse>>('/frameworks', {
    params: questionText ? { question: questionText } : undefined,
  });
  return res.data.data;
}

export async function generateEvidenceChain(
  birthProfileId: string,
  questionText?: string,
  questionCategory?: string,
): Promise<EvidenceGenerationResponse> {
  const res = await http.post<ApiEnvelope<EvidenceGenerationResponse>>('/evidence/generate', {
    birth_profile_id: birthProfileId,
    question_text: questionText,
    question_category: questionCategory,
  });
  return res.data.data;
}

export async function getLatestEvidence(
  birthProfileId: string,
): Promise<EvidenceGenerationResponse | null> {
  const res = await http.get<ApiEnvelope<EvidenceGenerationResponse | null>>(
    `/evidence/latest/${birthProfileId}`,
  );
  return res.data.data;
}

export async function synthesizeReasoningChain(
  birthProfileId: string,
  questionText?: string,
  questionCategory?: string,
): Promise<ReasoningSynthesisResponse> {
  const res = await http.post<ApiEnvelope<ReasoningSynthesisResponse>>('/reasoning/synthesize', {
    birth_profile_id: birthProfileId,
    question_text: questionText,
    question_category: questionCategory,
  });
  return res.data.data;
}

export async function getLatestReasoning(
  birthProfileId: string,
): Promise<ReasoningSynthesisResponse | null> {
  const res = await http.get<ApiEnvelope<ReasoningSynthesisResponse | null>>(
    `/reasoning/latest/${birthProfileId}`,
  );
  return res.data.data;
}

export async function getRegisteredTools(): Promise<ToolDefinition[]> {
  const res = await http.get<ApiEnvelope<ToolDefinition[]>>('/ai/tools');
  return res.data.data;
}

export async function executeAstrologyTool(
  request: ToolExecutionRequest,
): Promise<ToolExecutionResponse> {
  const res = await http.post<ApiEnvelope<ToolExecutionResponse>>('/ai/tools/execute', request);
  return res.data.data;
}

export async function askAiConsultant(request: AiChatRequest): Promise<AiChatResponse> {
  const res = await http.post<ApiEnvelope<AiChatResponse>>('/ai/chat', request);
  return res.data.data;
}

export async function createChatSession(
  payload: CreateChatSessionPayload,
): Promise<ChatSession> {
  const res = await http.post<ApiEnvelope<ChatSession>>('/ai/chat-sessions', payload);
  return res.data.data;
}

export async function listChatSessions(birthProfileId: string): Promise<ChatSession[]> {
  const res = await http.get<ApiEnvelope<ChatSession[]>>(
    `/ai/chat-sessions/profile/${birthProfileId}`,
  );
  return res.data.data;
}

export async function getChatSessionMessages(sessionId: string): Promise<ChatMessage[]> {
  const res = await http.get<ApiEnvelope<ChatMessage[]>>(
    `/ai/chat-sessions/${sessionId}/messages`,
  );
  return res.data.data;
}

export async function sendChatMessage(
  sessionId: string,
  payload: SendChatMessagePayload,
): Promise<ChatMessage> {
  const res = await http.post<ApiEnvelope<ChatMessage>>(
    `/ai/chat-sessions/${sessionId}/messages`,
    payload,
  );
  return res.data.data;
}

export async function getExplainabilityTrace(messageId: string): Promise<ExplainabilityTrace> {
  const res = await http.get<ApiEnvelope<ExplainabilityTrace>>(
    `/ai/chat-sessions/messages/${messageId}/explain`,
  );
  return res.data.data;
}







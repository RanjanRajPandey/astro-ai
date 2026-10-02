import axios from 'axios';
import type {
  AiChatRequest,
  AiChatResponse,
  AspectCalculationResponse,
  AuthResponse,
  AuthUser,
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
  LoginPayload,
  NakshatraCalculationResponse,
  QuestionClassificationResponse,
  ReasoningSynthesisResponse,
  RegisterPayload,
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

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('astro_access_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
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

export async function updateBirthProfile(
  id: string,
  payload: CreateBirthProfilePayload,
): Promise<BirthProfile> {
  const res = await http.put<ApiEnvelope<BirthProfile>>(`/birth-profiles/${id}`, payload);
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

export async function deleteChatSession(sessionId: string): Promise<void> {
  await http.delete(`/ai/chat-sessions/${sessionId}`);
}

export async function listUserChatSessions(userId: string): Promise<ChatSession[]> {
  const res = await http.get<ApiEnvelope<ChatSession[]>>(`/ai/chat-sessions/user/${userId}`);
  return res.data.data;
}

export async function searchChatSessions(userId: string, query?: string): Promise<ChatSession[]> {
  const res = await http.get<ApiEnvelope<ChatSession[]>>('/ai/chat-sessions/search', {
    params: { userId, query: query || '' },
  });
  return res.data.data;
}

export async function exportChatSession(sessionId: string): Promise<string> {
  const res = await http.get<string>(`/ai/chat-sessions/${sessionId}/export`, {
    responseType: 'text',
  });
  return res.data;
}

export function getStoredAccessToken(): string | null {
  return localStorage.getItem('astro_access_token');
}

export function setStoredAuth(auth: AuthResponse): void {
  localStorage.setItem('astro_access_token', auth.access_token);
  localStorage.setItem('astro_refresh_token', auth.refresh_token);
  localStorage.setItem('astro_user', JSON.stringify(auth.user));
}

export function clearStoredAuth(): void {
  localStorage.removeItem('astro_access_token');
  localStorage.removeItem('astro_refresh_token');
  localStorage.removeItem('astro_user');
}

export function getStoredUser(): AuthUser | null {
  const userJson = localStorage.getItem('astro_user');
  if (!userJson) return null;
  try {
    return JSON.parse(userJson) as AuthUser;
  } catch {
    return null;
  }
}

export async function loginUser(payload: LoginPayload): Promise<AuthResponse> {
  const res = await http.post<ApiEnvelope<AuthResponse>>('/auth/login', payload);
  setStoredAuth(res.data.data);
  return res.data.data;
}

export async function registerUser(payload: RegisterPayload): Promise<AuthResponse> {
  const res = await http.post<ApiEnvelope<AuthResponse>>('/auth/register', payload);
  setStoredAuth(res.data.data);
  return res.data.data;
}

export async function refreshUserToken(refreshToken: string): Promise<AuthResponse> {
  const res = await http.post<ApiEnvelope<AuthResponse>>('/auth/refresh', {
    refresh_token: refreshToken,
  });
  setStoredAuth(res.data.data);
  return res.data.data;
}

export async function getCurrentUser(): Promise<AuthUser> {
  const res = await http.get<ApiEnvelope<AuthUser>>('/auth/me');
  return res.data.data;
}

export function logoutUser(): void {
  clearStoredAuth();
}

export async function deleteAccount(): Promise<void> {
  await http.delete('/auth/me');
  clearStoredAuth();
}

export async function anonymizeAccount(): Promise<AuthUser> {
  const res = await http.post<ApiEnvelope<AuthUser>>('/auth/me/anonymize');
  const user = res.data.data;
  const currentAuth = getStoredUser();
  if (currentAuth) {
    localStorage.setItem('astro_user', JSON.stringify(user));
  }
  return user;
}







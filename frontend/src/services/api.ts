import axios from 'axios';
import type {
  BirthProfile,
  CreateBirthProfilePayload,
  GazetteerCity,
  KundliChartResponse,
  NakshatraCalculationResponse,
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


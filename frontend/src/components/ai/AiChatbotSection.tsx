import React, { useState, useEffect, useRef } from 'react';
import {
  MessageSquare,
  Bot,
  User,
  Send,
  Sparkles,
  HelpCircle,
  Plus,
  RefreshCw,
  AlertTriangle,
} from 'lucide-react';
import type {
  ChatMessage,
  ChatSession,
  ExplainabilityTrace,
} from '../../types/astrology';
import {
  createChatSession,
  listChatSessions,
  getChatSessionMessages,
  sendChatMessage,
} from '../../services/api';
import { ExplainabilityModal } from './ExplainabilityModal';

interface AiChatbotSectionProps {
  birthProfileId?: string;
}

const PRESET_TOPICS = [
  {
    label: 'Career & Authority',
    domain: 'CAREER_AND_PROFESSION',
    prompt: 'What does my 10th house and active dasha indicate for leadership promotions and career ascension?',
  },
  {
    label: 'Wealth & Assets',
    domain: 'WEALTH_AND_FINANCE',
    prompt: 'Analyze my 2nd and 11th houses for financial accumulation, investments, and long-term stability.',
  },
  {
    label: 'Marriage & Dharma',
    domain: 'MARRIAGE_AND_RELATIONSHIPS',
    prompt: 'How are my 7th house lord and Venus placed for relationship harmony and marital partnership?',
  },
  {
    label: 'Spiritual Remedies',
    domain: 'SPIRITUALITY_AND_MOKSHA',
    prompt: 'Which classical shastric remedies (Dana, Mantra, Japa) are indicated to alleviate current karmic afflictions?',
  },
];

export const AiChatbotSection: React.FC<AiChatbotSectionProps> = ({
  birthProfileId,
}) => {
  const [sessions, setSessions] = useState<ChatSession[]>([]);
  const [activeSessionId, setActiveSessionId] = useState<string | null>(null);
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [inputMessage, setInputMessage] = useState('');
  const [domainCategory, setDomainCategory] = useState('CAREER_AND_PROFESSION');
  const [includeReasoning, setIncludeReasoning] = useState(true);

  const [isLoading, setIsLoading] = useState(false);
  const [isSending, setIsSending] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Explainability Modal State
  const [selectedTrace, setSelectedTrace] = useState<ExplainabilityTrace | null>(null);
  const [isModalOpen, setIsModalOpen] = useState(false);

  const messagesEndRef = useRef<HTMLDivElement>(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    if (birthProfileId) {
      loadSessions(birthProfileId);
    }
  }, [birthProfileId]);

  useEffect(() => {
    if (activeSessionId) {
      loadMessages(activeSessionId);
    } else {
      setMessages([]);
    }
  }, [activeSessionId]);

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const loadSessions = async (profileId: string) => {
    setIsLoading(true);
    try {
      const data = await listChatSessions(profileId);
      setSessions(data);
      if (data.length > 0) {
        setActiveSessionId(data[0].id);
      } else {
        // Auto-create initial session
        await handleCreateNewSession('Initial Vedic Consultation', profileId);
      }
    } catch (err) {
      console.warn('Could not load chat sessions:', err);
    } finally {
      setIsLoading(false);
    }
  };

  const loadMessages = async (sessionId: string) => {
    try {
      const msgs = await getChatSessionMessages(sessionId);
      setMessages(msgs);
    } catch (err) {
      console.warn('Could not load messages for session:', err);
    }
  };

  const handleCreateNewSession = async (title?: string, profileId?: string) => {
    const pId = profileId || birthProfileId;
    if (!pId) return;

    try {
      const newSession = await createChatSession({
        birth_profile_id: pId,
        title: title || 'New Astrological Consultation',
      });
      setSessions((prev) => [newSession, ...prev]);
      setActiveSessionId(newSession.id);
      setMessages([]);
    } catch (err: unknown) {
      console.error('Failed to create chat session:', err);
    }
  };

  const handleSendMessage = async (textToSend?: string, domainOverride?: string) => {
    const text = textToSend || inputMessage;
    const domain = domainOverride || domainCategory;
    if (!text.trim() || !activeSessionId) return;

    setIsSending(true);
    setError(null);

    // Optimistically add user message
    const tempUserMsg: ChatMessage = {
      id: 'temp-' + Date.now(),
      chat_session_id: activeSessionId,
      sender_role: 'USER',
      message_content: text,
      prompt_tokens: 0,
      completion_tokens: 0,
      created_at: new Date().toISOString(),
    };
    setMessages((prev) => [...prev, tempUserMsg]);
    setInputMessage('');

    try {
      await sendChatMessage(activeSessionId, {
        message: text,
        domain_category: domain,
        include_reasoning: includeReasoning,
      });

      // Replace or refresh messages
      await loadMessages(activeSessionId);
    } catch (err: unknown) {
      console.error('Send message error:', err);
      setError(err instanceof Error ? err.message : 'Failed to send consultation query.');
    } finally {
      setIsSending(false);
    }
  };

  const handleOpenExplainability = (trace?: ExplainabilityTrace) => {
    if (trace) {
      setSelectedTrace(trace);
      setIsModalOpen(true);
    }
  };

  return (
    <section className="bg-slate-900/85 border border-slate-800/90 rounded-2xl p-5 sm:p-6 shadow-2xl space-y-6">
      {/* Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 border-b border-slate-800/80 pb-5">
        <div className="flex items-center space-x-3">
          <div className="p-2.5 rounded-xl bg-gradient-to-br from-indigo-500/20 to-purple-500/10 border border-indigo-500/30 text-indigo-400">
            <MessageSquare className="w-6 h-6" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h2 className="text-xl font-bold text-slate-100">
                Phase 19: AI Chatbot &amp; "Why This Answer?" Explainability UI
              </h2>
              <span className="px-2 py-0.5 text-xs font-semibold rounded-full bg-purple-500/10 border border-purple-500/30 text-purple-300">
                Multi-Turn Shastric Dialogue
              </span>
            </div>
            <p className="text-sm text-slate-400 mt-0.5">
              Interactive consultation with live shastric provenance, verified ground-truth context, and anti-hallucination audits.
            </p>
          </div>
        </div>

        {/* New Session Button */}
        <button
          onClick={() => handleCreateNewSession()}
          disabled={!birthProfileId}
          className="px-3.5 py-1.5 rounded-xl bg-slate-800 hover:bg-slate-700 disabled:opacity-50 text-slate-200 text-xs font-medium border border-slate-700 flex items-center gap-2 transition-all cursor-pointer self-start md:self-auto"
        >
          <Plus className="w-3.5 h-3.5 text-purple-400" />
          <span>New Session</span>
        </button>
      </div>

      {/* Main Grid: Sessions Sidebar + Chat Thread */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-5">
        {/* Sessions Sidebar */}
        <div className="md:col-span-1 space-y-2">
          <div className="flex items-center justify-between text-xs font-semibold text-slate-400 uppercase tracking-wider">
            <span>Sessions ({sessions.length})</span>
            {isLoading && <RefreshCw className="w-3 h-3 text-purple-400 animate-spin" />}
          </div>
          <div className="space-y-1.5 max-h-96 overflow-y-auto pr-1">
            {sessions.map((s) => (
              <button
                key={s.id}
                onClick={() => setActiveSessionId(s.id)}
                className={`w-full text-left p-3 rounded-xl border text-xs transition-all ${
                  activeSessionId === s.id
                    ? 'bg-purple-950/70 border-purple-500/60 text-purple-200 shadow-sm'
                    : 'bg-slate-950/50 border-slate-800/80 text-slate-400 hover:bg-slate-800/50 hover:text-slate-200'
                }`}
              >
                <div className="font-semibold truncate">{s.title}</div>
                <div className="text-[10px] text-slate-500 mt-1">
                  {new Date(s.updated_at).toLocaleDateString()} • {new Date(s.updated_at).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                </div>
              </button>
            ))}
          </div>
        </div>

        {/* Chat Thread */}
        <div className="md:col-span-3 bg-slate-950 border border-slate-800/80 rounded-xl flex flex-col h-[520px]">
          {/* Preset Prompts Carousel */}
          <div className="p-3 border-b border-slate-800/80 bg-slate-900/40 flex items-center gap-2 overflow-x-auto text-xs">
            <span className="text-[11px] text-slate-400 font-medium shrink-0">Quick Inquiries:</span>
            {PRESET_TOPICS.map((topic, i) => (
              <button
                key={i}
                onClick={() => {
                  setDomainCategory(topic.domain);
                  handleSendMessage(topic.prompt, topic.domain);
                }}
                disabled={isSending || !activeSessionId}
                className="px-2.5 py-1 rounded-lg bg-slate-800/80 hover:bg-slate-700 border border-slate-700 text-[11px] text-slate-300 hover:text-white shrink-0 transition-all flex items-center gap-1 cursor-pointer"
              >
                <Sparkles className="w-3 h-3 text-purple-400" />
                <span>{topic.label}</span>
              </button>
            ))}
          </div>

          {/* Messages Container */}
          <div className="flex-1 p-4 overflow-y-auto space-y-4">
            {messages.length === 0 ? (
              <div className="h-full flex flex-col items-center justify-center text-center p-6 text-slate-500 space-y-3">
                <Bot className="w-10 h-10 text-purple-400/60" />
                <div>
                  <h4 className="text-sm font-semibold text-slate-300">
                    Welcome to Shastric AI Consultation
                  </h4>
                  <p className="text-xs text-slate-400 max-w-sm mt-1">
                    Ask any question regarding your natal potential, current Vimshottari dasha periods, classical yogas, or recommended remedies.
                  </p>
                </div>
              </div>
            ) : (
              messages.map((m) => {
                const isAssistant = m.sender_role === 'ASSISTANT';
                return (
                  <div
                    key={m.id}
                    className={`flex items-start gap-3 ${
                      isAssistant ? 'justify-start' : 'justify-end'
                    }`}
                  >
                    {isAssistant && (
                      <div className="p-2 rounded-xl bg-purple-950/80 border border-purple-800 text-purple-300 shrink-0 mt-1">
                        <Bot className="w-4 h-4" />
                      </div>
                    )}

                    <div
                      className={`max-w-[85%] rounded-2xl p-4 text-xs sm:text-sm leading-relaxed space-y-3 ${
                        isAssistant
                          ? 'bg-slate-900 border border-purple-900/30 text-slate-100 shadow-md'
                          : 'bg-gradient-to-r from-purple-700 to-indigo-700 text-white'
                      }`}
                    >
                      <div className="whitespace-pre-wrap">{m.message_content}</div>

                      {/* Assistant Explainability Button */}
                      {isAssistant && m.explainability_trace && (
                        <div className="pt-2 border-t border-slate-800/80 flex items-center justify-between gap-3">
                          <button
                            onClick={() => handleOpenExplainability(m.explainability_trace)}
                            className="px-3 py-1.5 rounded-lg bg-purple-950/80 hover:bg-purple-900/80 border border-purple-700/60 text-purple-300 hover:text-purple-100 text-xs font-semibold flex items-center gap-1.5 transition-all shadow-sm cursor-pointer"
                          >
                            <HelpCircle className="w-3.5 h-3.5 text-cyan-400" />
                            <span>Why This Answer?</span>
                          </button>

                          <span className="text-[10px] text-slate-400 font-mono">
                            Swiss Ephemeris • BPHS Verified
                          </span>
                        </div>
                      )}
                    </div>

                    {!isAssistant && (
                      <div className="p-2 rounded-xl bg-indigo-600 text-white shrink-0 mt-1">
                        <User className="w-4 h-4" />
                      </div>
                    )}
                  </div>
                );
              })
            )}

            {isSending && (
              <div className="flex items-start gap-3">
                <div className="p-2 rounded-xl bg-purple-950/80 border border-purple-800 text-purple-300 shrink-0">
                  <Bot className="w-4 h-4" />
                </div>
                <div className="bg-slate-900 border border-slate-800 rounded-2xl p-3 text-xs text-slate-300 flex items-center gap-2">
                  <RefreshCw className="w-3.5 h-3.5 text-purple-400 animate-spin" />
                  <span>Synthesizing classical shastras &amp; validating guardrails...</span>
                </div>
              </div>
            )}

            <div ref={messagesEndRef} />
          </div>

          {error && (
            <div className="mx-4 mb-2 p-3 rounded-lg bg-red-950/50 border border-red-800 text-red-200 text-xs flex items-center gap-2">
              <AlertTriangle className="w-4 h-4 text-red-400 shrink-0" />
              <span>{error}</span>
            </div>
          )}

          {/* Chat Input Bar */}
          <div className="p-3 border-t border-slate-800 bg-slate-900/60 space-y-2">
            <div className="flex flex-wrap items-center justify-between text-[11px] text-slate-400 px-1 gap-2">
              <label className="flex items-center gap-2 cursor-pointer hover:text-slate-200">
                <input
                  type="checkbox"
                  checked={includeReasoning}
                  onChange={(e) => setIncludeReasoning(e.target.checked)}
                  className="rounded border-slate-700 text-purple-500 focus:ring-purple-500"
                />
                <span>Synthesize 5-Step Reasoning DAG &amp; Remedies</span>
              </label>

              <select
                value={domainCategory}
                onChange={(e) => setDomainCategory(e.target.value)}
                className="bg-slate-950 border border-slate-800 rounded-lg px-2 py-1 text-slate-300 focus:outline-none"
              >
                <option value="CAREER_AND_PROFESSION">Career &amp; Profession</option>
                <option value="WEALTH_AND_FINANCE">Wealth &amp; Finance</option>
                <option value="MARRIAGE_AND_RELATIONSHIPS">Marriage &amp; Compatibility</option>
                <option value="HEALTH_AND_LONGEVITY">Health &amp; Vitality</option>
                <option value="EDUCATION_AND_INTELLECT">Education &amp; Intellect</option>
                <option value="SPIRITUALITY_AND_MOKSHA">Spirituality &amp; Moksha</option>
              </select>
            </div>

            <div className="flex items-center gap-2">
              <input
                type="text"
                value={inputMessage}
                onChange={(e) => setInputMessage(e.target.value)}
                onKeyDown={(e) => {
                  if (e.key === 'Enter' && !e.shiftKey) {
                    e.preventDefault();
                    handleSendMessage();
                  }
                }}
                placeholder="Ask about your dasha, yogas, house strengths, or classical remedies..."
                disabled={isSending || !activeSessionId}
                className="flex-1 bg-slate-950 border border-slate-700/80 rounded-xl px-3.5 py-2 text-xs sm:text-sm text-slate-100 placeholder-slate-500 focus:outline-none focus:ring-1 focus:ring-purple-500"
              />

              <button
                onClick={() => handleSendMessage()}
                disabled={isSending || !inputMessage.trim() || !activeSessionId}
                className="px-4 py-2 rounded-xl bg-purple-600 hover:bg-purple-500 disabled:opacity-50 text-white text-xs font-semibold flex items-center gap-1.5 transition-all cursor-pointer"
              >
                <Send className="w-3.5 h-3.5" />
                <span>Send</span>
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* Deep Why This Answer Explainability Modal */}
      <ExplainabilityModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        trace={selectedTrace}
      />
    </section>
  );
};

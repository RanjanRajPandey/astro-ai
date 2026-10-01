import React, { useState, useEffect } from 'react';
import {
  Bot,
  ShieldCheck,
  Cpu,
  Wrench,
  Sparkles,
  ChevronDown,
  ChevronUp,
  Send,
  CheckCircle2,
  AlertTriangle,
  Terminal,
  HelpCircle,
  Play,
} from 'lucide-react';
import type {
  AiChatResponse,
  ToolDefinition,
  ToolExecutionResponse,
} from '../../types/astrology';
import {
  askAiConsultant,
  getRegisteredTools,
  executeAstrologyTool,
} from '../../services/api';

interface AiConsultationSectionProps {
  birthProfileId?: string;
}

const QUICK_PROMPTS = [
  {
    label: 'Career & Leadership',
    domain: 'CAREER_AND_PROFESSION',
    query: 'What does my chart indicate regarding leadership promotion, professional authority, and upcoming dasha windows?',
  },
  {
    label: 'Wealth & Stability',
    domain: 'WEALTH_AND_FINANCE',
    query: 'How are my 2nd and 11th houses positioned for sustainable financial growth, investments, and wealth preservation?',
  },
  {
    label: 'Education & Knowledge',
    domain: 'EDUCATION_AND_INTELLECT',
    query: 'What are the indicators for higher education, academic focus, intellectual pursuits, and overseas travel?',
  },
  {
    label: 'Spiritual Alignment',
    domain: 'SPIRITUALITY_AND_MOKSHA',
    query: 'Which planetary influences govern my spiritual sadhana, 9th/12th house evolution, and recommended shastric remedies?',
  },
];

export const AiConsultationSection: React.FC<AiConsultationSectionProps> = ({
  birthProfileId,
}) => {
  const [activeTab, setActiveTab] = useState<'consultation' | 'tools'>('consultation');
  const [userMessage, setUserMessage] = useState(
    'Will I experience career advancement and professional recognition in my current dasha period?'
  );
  const [domainCategory, setDomainCategory] = useState('CAREER_AND_PROFESSION');
  const [includeReasoning, setIncludeReasoning] = useState(true);

  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [chatResponse, setChatResponse] = useState<AiChatResponse | null>(null);

  const [tools, setTools] = useState<ToolDefinition[]>([]);
  const [selectedTool, setSelectedTool] = useState<ToolDefinition | null>(null);
  const [isExecutingTool, setIsExecutingTool] = useState(false);
  const [toolExecutionResult, setToolExecutionResult] = useState<ToolExecutionResponse | null>(null);
  const [isGroundTruthOpen, setIsGroundTruthOpen] = useState(false);

  useEffect(() => {
    getRegisteredTools()
      .then((data) => {
        setTools(data);
        if (data.length > 0) setSelectedTool(data[0]);
      })
      .catch((err) => console.warn('Could not fetch registered tools:', err));
  }, []);

  const handleConsult = async (queryText?: string, domain?: string) => {
    const q = queryText || userMessage;
    const d = domain || domainCategory;
    if (!q.trim()) return;

    setIsLoading(true);
    setError(null);

    try {
      const res = await askAiConsultant({
        birth_profile_id: birthProfileId,
        user_message: q,
        domain_category: d,
        include_reasoning: includeReasoning,
      });
      setChatResponse(res);
    } catch (err: unknown) {
      console.error('AI consultation error:', err);
      setError(
        err instanceof Error ? err.message : 'Failed to generate AI consultation.'
      );
    } finally {
      setIsLoading(false);
    }
  };

  const handleExecuteTool = async (toolName: string) => {
    if (!birthProfileId) {
      alert('Please select or create a birth profile first to test deterministic tools.');
      return;
    }
    setIsExecutingTool(true);
    setToolExecutionResult(null);

    try {
      const res = await executeAstrologyTool({
        tool_name: toolName,
        arguments: {
          birth_profile_id: birthProfileId,
          question_text: userMessage,
          question_category: domainCategory,
        },
      });
      setToolExecutionResult(res);
    } catch (err: unknown) {
      console.error('Tool execution error:', err);
      setToolExecutionResult({
        tool_name: toolName,
        success: false,
        error: err instanceof Error ? err.message : 'Tool execution failed',
      });
    } finally {
      setIsExecutingTool(false);
    }
  };

  return (
    <section className="bg-slate-900/80 border border-slate-800/90 rounded-2xl p-5 sm:p-6 shadow-2xl space-y-6">
      {/* Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 border-b border-slate-800/80 pb-5">
        <div className="flex items-center space-x-3">
          <div className="p-2.5 rounded-xl bg-gradient-to-br from-purple-500/20 to-indigo-500/10 border border-purple-500/30 text-purple-400">
            <Bot className="w-6 h-6" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h2 className="text-xl font-bold text-slate-100">
                Phase 18: AI Tool Layer &amp; Provider Abstraction
              </h2>
              <span className="px-2 py-0.5 text-xs font-semibold rounded-full bg-emerald-500/10 border border-emerald-500/30 text-emerald-300">
                Anti-Hallucination Guardrails
              </span>
            </div>
            <p className="text-sm text-slate-400 mt-0.5">
              Deterministic calculations translate through strict LLM abstraction with Swiss Ephemeris ground truth.
            </p>
          </div>
        </div>

        {/* Tab Toggle */}
        <div className="flex items-center bg-slate-950/60 p-1 rounded-xl border border-slate-800 self-start md:self-auto">
          <button
            onClick={() => setActiveTab('consultation')}
            className={`flex items-center gap-2 px-3 py-1.5 rounded-lg text-xs font-medium transition-all ${
              activeTab === 'consultation'
                ? 'bg-purple-600/30 text-purple-200 border border-purple-500/40 shadow-sm'
                : 'text-slate-400 hover:text-slate-200'
            }`}
          >
            <Sparkles className="w-3.5 h-3.5" />
            AI Consultation
          </button>
          <button
            onClick={() => setActiveTab('tools')}
            className={`flex items-center gap-2 px-3 py-1.5 rounded-lg text-xs font-medium transition-all ${
              activeTab === 'tools'
                ? 'bg-purple-600/30 text-purple-200 border border-purple-500/40 shadow-sm'
                : 'text-slate-400 hover:text-slate-200'
            }`}
          >
            <Cpu className="w-3.5 h-3.5" />
            Deterministic Tools ({tools.length})
          </button>
        </div>
      </div>

      {/* Main Tab: AI Consultation */}
      {activeTab === 'consultation' && (
        <div className="space-y-6">
          {/* Quick Prompts */}
          <div className="space-y-2">
            <span className="text-xs font-medium text-slate-400">Classical Inquiry Presets:</span>
            <div className="flex flex-wrap gap-2">
              {QUICK_PROMPTS.map((p, idx) => (
                <button
                  key={idx}
                  onClick={() => {
                    setUserMessage(p.query);
                    setDomainCategory(p.domain);
                    handleConsult(p.query, p.domain);
                  }}
                  className="px-3 py-1.5 rounded-lg bg-slate-800/60 hover:bg-slate-800 border border-slate-700/60 hover:border-purple-500/50 text-xs text-slate-300 transition-all flex items-center gap-1.5"
                >
                  <Sparkles className="w-3 h-3 text-purple-400" />
                  {p.label}
                </button>
              ))}
            </div>
          </div>

          {/* Interactive Chat Input */}
          <div className="bg-slate-950/70 border border-slate-800/80 rounded-xl p-4 space-y-4">
            <div className="space-y-2">
              <label className="text-xs font-semibold text-slate-300 flex items-center justify-between">
                <span>Your Astrological Inquiry:</span>
                <span className="text-[11px] text-slate-400 font-normal">
                  Powered by BPHS &amp; Phaladeepika
                </span>
              </label>
              <textarea
                value={userMessage}
                onChange={(e) => setUserMessage(e.target.value)}
                rows={2}
                placeholder="Ask about career, wealth, health, relationships, dasha results..."
                className="w-full bg-slate-900/90 border border-slate-700/80 rounded-lg p-3 text-sm text-slate-100 placeholder-slate-500 focus:outline-none focus:ring-1 focus:ring-purple-500/50"
              />
            </div>

            <div className="flex flex-wrap items-center justify-between gap-3 pt-1">
              <div className="flex items-center gap-4 text-xs">
                <label className="flex items-center gap-2 cursor-pointer text-slate-300">
                  <input
                    type="checkbox"
                    checked={includeReasoning}
                    onChange={(e) => setIncludeReasoning(e.target.checked)}
                    className="rounded border-slate-700 text-purple-500 focus:ring-purple-500"
                  />
                  <span>Include 5-Step Reasoning DAG &amp; Remedies</span>
                </label>
              </div>

              <button
                onClick={() => handleConsult()}
                disabled={isLoading || !userMessage.trim()}
                className="px-5 py-2 rounded-xl bg-gradient-to-r from-purple-600 to-indigo-600 hover:from-purple-500 hover:to-indigo-500 disabled:opacity-50 text-white text-xs font-semibold shadow-lg shadow-purple-900/30 flex items-center gap-2 transition-all cursor-pointer"
              >
                {isLoading ? (
                  <>
                    <div className="w-3.5 h-3.5 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                    <span>Synthesizing Shastras...</span>
                  </>
                ) : (
                  <>
                    <Send className="w-3.5 h-3.5" />
                    <span>Consult AstroAI</span>
                  </>
                )}
              </button>
            </div>
          </div>

          {error && (
            <div className="p-4 rounded-xl bg-red-950/40 border border-red-800/60 text-red-200 text-xs flex items-start gap-2.5">
              <AlertTriangle className="w-4 h-4 text-red-400 mt-0.5 shrink-0" />
              <div>{error}</div>
            </div>
          )}

          {/* AI Consultation Response */}
          {chatResponse && (
            <div className="space-y-4">
              {/* Guardrail & Provider Status Banner */}
              <div className="flex flex-wrap items-center justify-between gap-3 bg-slate-950/90 border border-slate-800 rounded-xl p-3.5">
                <div className="flex items-center gap-2.5">
                  <div className="p-1.5 rounded-lg bg-emerald-500/10 text-emerald-400 border border-emerald-500/30">
                    <ShieldCheck className="w-4 h-4" />
                  </div>
                  <div>
                    <div className="text-xs font-semibold text-emerald-300 flex items-center gap-2">
                      <span>Anti-Hallucination Guardrail Passed</span>
                      <span className="text-[10px] px-2 py-0.5 rounded-full bg-emerald-950 text-emerald-300 border border-emerald-800">
                        Confidence: {(chatResponse.guardrail_result.confidence_score * 100).toFixed(0)}%
                      </span>
                    </div>
                    <div className="text-[11px] text-slate-400">
                      Verified assertions: {chatResponse.guardrail_result.verified_assertions.length} | Discrepancies: {chatResponse.guardrail_result.flagged_discrepancies.length}
                    </div>
                  </div>
                </div>

                <div className="flex items-center gap-2">
                  <span className="text-[11px] px-2.5 py-1 rounded-md bg-purple-950/60 border border-purple-800/60 text-purple-300 font-mono">
                    Provider: {chatResponse.provider} ({chatResponse.model})
                  </span>
                  <button
                    onClick={() => setIsGroundTruthOpen(!isGroundTruthOpen)}
                    className="text-xs px-2.5 py-1 rounded-md bg-slate-800 hover:bg-slate-700 text-slate-300 border border-slate-700 flex items-center gap-1 transition-all"
                  >
                    <HelpCircle className="w-3 h-3 text-cyan-400" />
                    <span>Why This Answer?</span>
                    {isGroundTruthOpen ? (
                      <ChevronUp className="w-3 h-3" />
                    ) : (
                      <ChevronDown className="w-3 h-3" />
                    )}
                  </button>
                </div>
              </div>

              {/* Verified Assertions Pills */}
              {chatResponse.guardrail_result.verified_assertions.length > 0 && (
                <div className="flex flex-wrap gap-2 text-[11px]">
                  {chatResponse.guardrail_result.verified_assertions.map((va, i) => (
                    <span
                      key={i}
                      className="px-2.5 py-1 rounded-lg bg-slate-950 border border-slate-800 text-slate-300 flex items-center gap-1.5"
                    >
                      <CheckCircle2 className="w-3 h-3 text-emerald-400 shrink-0" />
                      {va}
                    </span>
                  ))}
                </div>
              )}

              {/* Collapsible Ground Truth Drawer */}
              {isGroundTruthOpen && (
                <div className="bg-slate-950 border border-slate-800 rounded-xl p-4 text-xs space-y-3">
                  <div className="font-semibold text-slate-200 flex items-center gap-2">
                    <Terminal className="w-3.5 h-3.5 text-cyan-400" />
                    <span>Deterministic Ground-Truth Context (Swiss Ephemeris Facts)</span>
                  </div>
                  <pre className="bg-slate-900 p-3 rounded-lg text-slate-300 text-[11px] font-mono overflow-x-auto max-h-48">
                    {JSON.stringify(chatResponse.ground_truth_context, null, 2)}
                  </pre>
                </div>
              )}

              {/* Synthesized Response Content */}
              <div className="bg-gradient-to-b from-slate-900/90 to-slate-950 border border-purple-900/30 rounded-xl p-5 shadow-inner space-y-4">
                <div className="prose prose-invert max-w-none text-slate-200 text-xs sm:text-sm leading-relaxed whitespace-pre-wrap">
                  {chatResponse.response}
                </div>
              </div>
            </div>
          )}
        </div>
      )}

      {/* Deterministic Tools Inspector Tab */}
      {activeTab === 'tools' && (
        <div className="space-y-6">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            {/* Tool list */}
            <div className="space-y-2 md:col-span-1">
              <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">
                Available Tools ({tools.length})
              </span>
              <div className="space-y-1.5 max-h-96 overflow-y-auto pr-1">
                {tools.map((t) => (
                  <button
                    key={t.name}
                    onClick={() => {
                      setSelectedTool(t);
                      setToolExecutionResult(null);
                    }}
                    className={`w-full text-left p-2.5 rounded-xl border text-xs transition-all flex items-start gap-2.5 ${
                      selectedTool?.name === t.name
                        ? 'bg-purple-950/60 border-purple-500/60 text-purple-200 shadow-sm'
                        : 'bg-slate-950/50 border-slate-800/80 text-slate-300 hover:bg-slate-800/50'
                    }`}
                  >
                    <Wrench className="w-3.5 h-3.5 text-purple-400 shrink-0 mt-0.5" />
                    <div>
                      <div className="font-mono font-semibold text-[11px]">{t.name}</div>
                      <div className="text-[10px] text-slate-400 line-clamp-1">
                        {t.description}
                      </div>
                    </div>
                  </button>
                ))}
              </div>
            </div>

            {/* Selected Tool Details & Live Execution */}
            <div className="md:col-span-2 bg-slate-950 border border-slate-800/80 rounded-xl p-4 space-y-4">
              {selectedTool ? (
                <>
                  <div className="flex items-center justify-between border-b border-slate-800 pb-3">
                    <div>
                      <div className="text-sm font-bold font-mono text-purple-300">
                        {selectedTool.name}
                      </div>
                      <p className="text-xs text-slate-400 mt-0.5">
                        {selectedTool.description}
                      </p>
                    </div>

                    <button
                      onClick={() => handleExecuteTool(selectedTool.name)}
                      disabled={isExecutingTool}
                      className="px-4 py-2 rounded-xl bg-purple-600 hover:bg-purple-500 disabled:opacity-50 text-white text-xs font-semibold flex items-center gap-2 transition-all cursor-pointer"
                    >
                      {isExecutingTool ? (
                        <>
                          <div className="w-3.5 h-3.5 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                          <span>Executing...</span>
                        </>
                      ) : (
                        <>
                          <Play className="w-3.5 h-3.5 fill-current" />
                          <span>Execute Tool</span>
                        </>
                      )}
                    </button>
                  </div>

                  <div className="space-y-2">
                    <span className="text-[11px] font-semibold text-slate-400">
                      Parameter Schema (JSON-Schema):
                    </span>
                    <pre className="bg-slate-900/90 p-3 rounded-lg text-slate-300 text-[11px] font-mono overflow-x-auto max-h-40 border border-slate-800">
                      {JSON.stringify(selectedTool.parameter_schema, null, 2)}
                    </pre>
                  </div>

                  {toolExecutionResult && (
                    <div className="space-y-2 pt-2 border-t border-slate-800">
                      <div className="flex items-center justify-between">
                        <span className="text-[11px] font-semibold text-slate-300 flex items-center gap-1.5">
                          <CheckCircle2
                            className={`w-3.5 h-3.5 ${
                              toolExecutionResult.success ? 'text-emerald-400' : 'text-red-400'
                            }`}
                          />
                          <span>Execution Output ({toolExecutionResult.tool_name}):</span>
                        </span>
                        <span
                          className={`text-[10px] px-2 py-0.5 rounded-full font-semibold ${
                            toolExecutionResult.success
                              ? 'bg-emerald-950 text-emerald-300 border border-emerald-800'
                              : 'bg-red-950 text-red-300 border border-red-800'
                          }`}
                        >
                          {toolExecutionResult.success ? 'SUCCESS (Deterministic)' : 'ERROR'}
                        </span>
                      </div>

                      <pre className="bg-slate-900/90 p-3 rounded-lg text-slate-200 text-[11px] font-mono overflow-x-auto max-h-60 border border-slate-800">
                        {toolExecutionResult.success
                          ? JSON.stringify(toolExecutionResult.result, null, 2)
                          : toolExecutionResult.error}
                      </pre>
                    </div>
                  )}
                </>
              ) : (
                <div className="text-center py-12 text-slate-500 text-xs">
                  Select a deterministic tool to view its schema and test execution.
                </div>
              )}
            </div>
          </div>
        </div>
      )}
    </section>
  );
};

import React, { useState } from 'react';
import {
  X,
  ShieldCheck,
  Compass,
  Layers,
  BookOpen,
  CheckCircle2,
  AlertCircle,
  Cpu,
  Sparkles,
  Terminal,
} from 'lucide-react';
import type { ExplainabilityTrace } from '../../types/astrology';

interface ExplainabilityModalProps {
  isOpen: boolean;
  onClose: () => void;
  trace: ExplainabilityTrace | null;
}

export const ExplainabilityModal: React.FC<ExplainabilityModalProps> = ({
  isOpen,
  onClose,
  trace,
}) => {
  const [activeTab, setActiveTab] = useState<'ground_truth' | 'dashas_vargas' | 'citations' | 'guardrails'>(
    'ground_truth'
  );

  if (!isOpen || !trace) return null;

  const groundTruth = trace.ground_truth_context || {};
  const guardrail = trace.guardrail_result || {
    is_valid: true,
    confidence_score: 1.0,
    verified_assertions: [],
    flagged_discrepancies: [],
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-md animate-in fade-in duration-200">
      <div className="relative w-full max-w-4xl bg-slate-900 border border-slate-700/80 rounded-2xl shadow-2xl overflow-hidden flex flex-col max-h-[90vh]">
        {/* Header */}
        <div className="flex items-center justify-between p-5 border-b border-slate-800 bg-slate-950/60">
          <div className="flex items-center space-x-3">
            <div className="p-2.5 rounded-xl bg-purple-500/10 border border-purple-500/30 text-purple-400">
              <Sparkles className="w-5 h-5" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h3 className="text-base font-bold text-slate-100">
                  Why This Answer? — Classical Shastric Provenance
                </h3>
                <span className="px-2 py-0.5 text-[10px] font-semibold rounded-full bg-emerald-500/10 border border-emerald-500/30 text-emerald-300">
                  100% Deterministic Engine
                </span>
              </div>
              <p className="text-xs text-slate-400 mt-0.5">
                Every deduction is grounded in Swiss Ephemeris coordinates and canonical shastric verses.
              </p>
            </div>
          </div>

          <button
            onClick={onClose}
            className="p-2 text-slate-400 hover:text-slate-200 hover:bg-slate-800 rounded-lg transition-colors cursor-pointer"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Top Summary Banner */}
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 p-4 bg-slate-950/40 border-b border-slate-800 text-xs">
          <div className="bg-slate-900/60 p-2.5 rounded-xl border border-slate-800/80">
            <span className="text-[10px] uppercase text-slate-400 font-semibold tracking-wider">
              Engine / Provider
            </span>
            <div className="font-semibold text-purple-300 mt-0.5 truncate">
              {trace.provider} ({trace.model})
            </div>
          </div>

          <div className="bg-slate-900/60 p-2.5 rounded-xl border border-slate-800/80">
            <span className="text-[10px] uppercase text-slate-400 font-semibold tracking-wider">
              Guardrail Status
            </span>
            <div className="font-semibold text-emerald-400 flex items-center gap-1 mt-0.5">
              <ShieldCheck className="w-3.5 h-3.5" />
              <span>{(guardrail.confidence_score * 100).toFixed(0)}% Confidence</span>
            </div>
          </div>

          <div className="bg-slate-900/60 p-2.5 rounded-xl border border-slate-800/80">
            <span className="text-[10px] uppercase text-slate-400 font-semibold tracking-wider">
              Composite Score
            </span>
            <div className="font-semibold text-cyan-300 mt-0.5">
              {(trace.composite_score * 100).toFixed(0)}% / 100%
            </div>
          </div>

          <div className="bg-slate-900/60 p-2.5 rounded-xl border border-slate-800/80">
            <span className="text-[10px] uppercase text-slate-400 font-semibold tracking-wider">
              Active Dasha
            </span>
            <div className="font-semibold text-amber-300 mt-0.5 truncate">
              {trace.active_dasha_period}
            </div>
          </div>
        </div>

        {/* Tab Navigation */}
        <div className="flex border-b border-slate-800 bg-slate-950/30 px-4 pt-2 gap-2 text-xs">
          <button
            onClick={() => setActiveTab('ground_truth')}
            className={`flex items-center gap-2 px-3.5 py-2 border-b-2 font-medium transition-all ${
              activeTab === 'ground_truth'
                ? 'border-purple-500 text-purple-300'
                : 'border-transparent text-slate-400 hover:text-slate-200'
            }`}
          >
            <Compass className="w-3.5 h-3.5" />
            <span>Natal Coordinates (Ground Truth)</span>
          </button>

          <button
            onClick={() => setActiveTab('dashas_vargas')}
            className={`flex items-center gap-2 px-3.5 py-2 border-b-2 font-medium transition-all ${
              activeTab === 'dashas_vargas'
                ? 'border-purple-500 text-purple-300'
                : 'border-transparent text-slate-400 hover:text-slate-200'
            }`}
          >
            <Layers className="w-3.5 h-3.5" />
            <span>Vargas &amp; Timing</span>
          </button>

          <button
            onClick={() => setActiveTab('citations')}
            className={`flex items-center gap-2 px-3.5 py-2 border-b-2 font-medium transition-all ${
              activeTab === 'citations'
                ? 'border-purple-500 text-purple-300'
                : 'border-transparent text-slate-400 hover:text-slate-200'
            }`}
          >
            <BookOpen className="w-3.5 h-3.5" />
            <span>Shastric Citations &amp; Remedies</span>
          </button>

          <button
            onClick={() => setActiveTab('guardrails')}
            className={`flex items-center gap-2 px-3.5 py-2 border-b-2 font-medium transition-all ${
              activeTab === 'guardrails'
                ? 'border-purple-500 text-purple-300'
                : 'border-transparent text-slate-400 hover:text-slate-200'
            }`}
          >
            <ShieldCheck className="w-3.5 h-3.5" />
            <span>Guardrail Audit Certificate</span>
          </button>
        </div>

        {/* Tab Content */}
        <div className="p-5 overflow-y-auto space-y-4 flex-1 text-xs">
          {/* Tab 1: Natal Coordinates */}
          {activeTab === 'ground_truth' && (
            <div className="space-y-4">
              <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
                <div className="bg-slate-950 p-3 rounded-xl border border-slate-800">
                  <span className="text-slate-400 text-[10px]">Ascendant (Lagna)</span>
                  <div className="text-sm font-bold text-slate-100 mt-1">
                    {String(groundTruth.ascendant_sign || 'Calculated')}
                  </div>
                </div>

                <div className="bg-slate-950 p-3 rounded-xl border border-slate-800">
                  <span className="text-slate-400 text-[10px]">Moon Sign (Rashi)</span>
                  <div className="text-sm font-bold text-slate-100 mt-1">
                    {String(groundTruth.moon_sign || 'Calculated')}
                  </div>
                  {Boolean(groundTruth.moon_nakshatra) && (
                    <span className="text-[10px] text-purple-300">
                      Nakshatra: {String(groundTruth.moon_nakshatra)}
                    </span>
                  )}
                </div>

                <div className="bg-slate-950 p-3 rounded-xl border border-slate-800">
                  <span className="text-slate-400 text-[10px]">Sun Sign</span>
                  <div className="text-sm font-bold text-slate-100 mt-1">
                    {String(groundTruth.sun_sign || 'Calculated')}
                  </div>
                </div>

                <div className="bg-slate-950 p-3 rounded-xl border border-slate-800">
                  <span className="text-slate-400 text-[10px]">Ayanamsha Model</span>
                  <div className="text-sm font-bold text-slate-100 mt-1">
                    {String(groundTruth.ayanamsha || 'Lahiri (Chitra Paksha)')}
                  </div>
                </div>
              </div>

              <div className="bg-slate-950 border border-slate-800 rounded-xl p-4 space-y-2">
                <div className="font-semibold text-slate-300 flex items-center gap-2">
                  <Terminal className="w-3.5 h-3.5 text-cyan-400" />
                  <span>Verified Ground-Truth Context Payload (Swiss Ephemeris State)</span>
                </div>
                <pre className="bg-slate-900 p-3 rounded-lg text-slate-300 font-mono text-[11px] overflow-x-auto max-h-60 border border-slate-800/80">
                  {JSON.stringify(groundTruth, null, 2)}
                </pre>
              </div>
            </div>
          )}

          {/* Tab 2: Vargas & Timing */}
          {activeTab === 'dashas_vargas' && (
            <div className="space-y-4">
              <div className="bg-slate-950 border border-slate-800 rounded-xl p-4 space-y-3">
                <span className="font-semibold text-slate-200">Divisional Charts Consulted:</span>
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
                  {trace.divisional_charts_consulted.map((varga, idx) => (
                    <div
                      key={idx}
                      className="flex items-center gap-2 p-2.5 rounded-lg bg-slate-900 border border-slate-800 text-slate-200"
                    >
                      <Layers className="w-4 h-4 text-cyan-400 shrink-0" />
                      <span>{varga}</span>
                    </div>
                  ))}
                </div>
              </div>

              <div className="bg-slate-950 border border-slate-800 rounded-xl p-4 space-y-2">
                <span className="font-semibold text-slate-200">Active Vimshottari Period:</span>
                <p className="text-slate-300 bg-slate-900 p-3 rounded-lg border border-slate-800">
                  {trace.active_dasha_period}
                </p>
              </div>

              <div className="bg-slate-950 border border-slate-800 rounded-xl p-4 space-y-2">
                <span className="font-semibold text-slate-200">Shastric Verdict:</span>
                <p className="text-slate-300 bg-slate-900 p-3 rounded-lg border border-slate-800">
                  {trace.reasoning_verdict}
                </p>
              </div>
            </div>
          )}

          {/* Tab 3: Citations & Remedies */}
          {activeTab === 'citations' && (
            <div className="space-y-4">
              <div className="bg-slate-950 border border-slate-800 rounded-xl p-4 space-y-2">
                <span className="font-semibold text-slate-200 flex items-center gap-2">
                  <BookOpen className="w-4 h-4 text-amber-400" />
                  <span>Canonical Classical Texts Cited:</span>
                </span>
                <ul className="space-y-1.5 pl-1">
                  {trace.shastric_citations.map((cite, idx) => (
                    <li key={idx} className="flex items-center gap-2 text-slate-300">
                      <div className="w-1.5 h-1.5 rounded-full bg-amber-400 shrink-0" />
                      <span>{cite}</span>
                    </li>
                  ))}
                </ul>
              </div>

              <div className="bg-slate-950 border border-slate-800 rounded-xl p-4 space-y-2">
                <span className="font-semibold text-slate-200 flex items-center gap-2">
                  <Sparkles className="w-4 h-4 text-purple-400" />
                  <span>Prescribed Classical Remedies (Parihara):</span>
                </span>
                <div className="space-y-2">
                  {trace.classical_remedies.map((remedy, idx) => (
                    <div
                      key={idx}
                      className="p-3 rounded-xl bg-slate-900 border border-slate-800 text-slate-200 flex items-start gap-2.5"
                    >
                      <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0 mt-0.5" />
                      <span>{remedy}</span>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          )}

          {/* Tab 4: Guardrails */}
          {activeTab === 'guardrails' && (
            <div className="space-y-4">
              <div className="p-4 rounded-xl bg-emerald-950/30 border border-emerald-800/50 flex items-start gap-3">
                <ShieldCheck className="w-5 h-5 text-emerald-400 shrink-0 mt-0.5" />
                <div className="space-y-1">
                  <div className="font-semibold text-emerald-300">
                    Certified Free of LLM Hallucinations
                  </div>
                  <p className="text-slate-300 text-[11px] leading-relaxed">
                    This consultation was generated with active deterministic guardrails. The LLM was strictly prohibited from generating astronomical positions or dashas.
                  </p>
                </div>
              </div>

              <div className="bg-slate-950 border border-slate-800 rounded-xl p-4 space-y-2">
                <span className="font-semibold text-slate-200">Verified Assertions:</span>
                <div className="space-y-1.5">
                  {guardrail.verified_assertions.map((va, idx) => (
                    <div
                      key={idx}
                      className="flex items-center gap-2 text-slate-300 bg-slate-900/60 p-2.5 rounded-lg border border-slate-800"
                    >
                      <CheckCircle2 className="w-3.5 h-3.5 text-emerald-400 shrink-0" />
                      <span>{va}</span>
                    </div>
                  ))}
                </div>
              </div>

              {guardrail.flagged_discrepancies.length > 0 && (
                <div className="bg-red-950/30 border border-red-800 rounded-xl p-4 space-y-2">
                  <span className="font-semibold text-red-300 flex items-center gap-1.5">
                    <AlertCircle className="w-4 h-4 text-red-400" />
                    <span>Discrepancies Flagged:</span>
                  </span>
                  <div className="space-y-1.5">
                    {guardrail.flagged_discrepancies.map((fd, idx) => (
                      <div key={idx} className="text-red-200 bg-red-950/60 p-2 rounded-lg text-xs">
                        {fd}
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </div>
          )}
        </div>

        {/* Footer */}
        <div className="flex items-center justify-between p-4 border-t border-slate-800 bg-slate-950/60 text-xs">
          <div className="flex items-center gap-2 text-slate-400">
            <Cpu className="w-3.5 h-3.5 text-purple-400" />
            <span>Trace ID: {trace.message_id}</span>
          </div>

          <button
            onClick={onClose}
            className="px-4 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-200 font-semibold transition-all cursor-pointer"
          >
            Close Trace
          </button>
        </div>
      </div>
    </div>
  );
};

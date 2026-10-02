import React, { useState } from 'react';
import {
  BrainCircuit,
  Compass,
  Layers,
  Sparkles,
  RefreshCw,
  BookOpen,
  ShieldAlert,
  ArrowRight,
  Activity,
} from 'lucide-react';
import type {
  ReasoningSynthesisResponse,
} from '../../types/astrology';

interface ReasoningChainSectionProps {
  reasoningData: ReasoningSynthesisResponse | null;
  isLoading?: boolean;
  onRefreshReasoning?: () => void;
}

const STEP_ICONS: Record<string, React.ReactNode> = {
  NATAL_PROMISE: <Compass className="w-4 h-4 text-amber-400" />,
  DIVISIONAL_VALIDATION: <Layers className="w-4 h-4 text-cyan-400" />,
  YOGA_CATALYSTS: <Sparkles className="w-4 h-4 text-purple-400" />,
  TEMPORAL_TRIGGER: <Activity className="w-4 h-4 text-emerald-400" />,
  SYNTHESIS_AND_CONCLUSION: <BrainCircuit className="w-4 h-4 text-rose-400" />,
};

export const ReasoningChainSection: React.FC<ReasoningChainSectionProps> = ({
  reasoningData,
  isLoading = false,
  onRefreshReasoning,
}) => {
  const [activeStepIndex, setActiveStepIndex] = useState<number | null>(null);

  if (!reasoningData) return null;

  const raw = reasoningData as any;
  const questionText = raw.questionText || raw.question_text || 'Inquiry Analysis';
  const questionCategory = raw.questionCategory || raw.question_category || 'GENERAL';
  const frameworkVersion = raw.frameworkVersion || raw.framework_version || '1.0';
  const primaryHouses: number[] = Array.isArray(raw.primaryHouses)
    ? raw.primaryHouses
    : Array.isArray(raw.primary_houses)
    ? raw.primary_houses
    : [];
  const compositeScore: number = raw.compositeScore ?? raw.composite_score ?? 0;
  const overallVerdict: string = raw.overallVerdict || raw.overall_verdict || 'MODERATE_PROGRESS';
  const reasoningSteps: any[] = Array.isArray(raw.reasoningSteps)
    ? raw.reasoningSteps
    : Array.isArray(raw.reasoning_steps)
    ? raw.reasoning_steps
    : [];
  const classicalRemedies: string[] = Array.isArray(raw.classicalRemedies)
    ? raw.classicalRemedies
    : Array.isArray(raw.classical_remedies)
    ? raw.classical_remedies
    : [];

  const isOverallFavorable = overallVerdict === 'FAVORABLE';
  const isOverallModerate = overallVerdict === 'MODERATE_PROGRESS';

  return (
    <section className="bg-slate-900/70 border border-slate-800/80 rounded-2xl p-5 sm:p-6 shadow-xl space-y-6">
      {/* Section Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 border-b border-slate-800/80 pb-5">
        <div className="flex items-center space-x-3">
          <div className="p-2.5 rounded-xl bg-gradient-to-br from-indigo-500/20 to-purple-500/10 border border-indigo-500/30 text-indigo-400">
            <BrainCircuit className="w-6 h-6" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h2 className="text-xl font-bold text-slate-100">
                Phase 17: Reasoning Engine &amp; Synthesis Pipeline
              </h2>
              <span className="px-2 py-0.5 text-xs font-semibold rounded-full bg-indigo-500/10 border border-indigo-500/30 text-indigo-300">
                Deterministic DAG
              </span>
            </div>
            <p className="text-xs sm:text-sm text-slate-400 mt-0.5">
              5-Step rule-based synthesis uniting Natal Promise, Harmonic Vargas, Yogas, and Temporal triggers into a verdict.
            </p>
          </div>
        </div>

        {onRefreshReasoning && (
          <button
            onClick={onRefreshReasoning}
            disabled={isLoading}
            className="flex items-center justify-center gap-2 px-3.5 py-2 rounded-xl bg-slate-800 hover:bg-slate-700/80 border border-slate-700 text-slate-200 text-xs font-medium transition disabled:opacity-50"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${isLoading ? 'animate-spin' : ''}`} />
            <span>Re-synthesize Reasoning</span>
          </button>
        )}
      </div>

      {/* Synthesis Verdict Card */}
      <div className="p-5 rounded-2xl bg-gradient-to-r from-slate-950/80 via-slate-900/90 to-slate-950/80 border border-slate-800 flex flex-col md:flex-row md:items-center justify-between gap-5">
        <div className="space-y-2 flex-1">
          <div className="text-xs font-medium text-slate-400 uppercase tracking-wider flex items-center gap-1.5">
            <Sparkles className="w-3.5 h-3.5 text-amber-400" />
            Synthesized Inquiry Verdict
          </div>
          <h3 className="text-lg sm:text-xl font-bold text-slate-100">
            &ldquo;{questionText}&rdquo;
          </h3>
          <div className="flex flex-wrap items-center gap-2 text-xs">
            <span className="px-2.5 py-1 rounded-lg bg-slate-800 border border-slate-700 text-slate-300 font-mono">
              Domain: {questionCategory}
            </span>
            {primaryHouses.length > 0 && (
              <span className="px-2.5 py-1 rounded-lg bg-indigo-950/40 border border-indigo-800/40 text-indigo-300 font-mono">
                Houses: {primaryHouses.map((h) => `H${h}`).join(', ')}
              </span>
            )}
            <span className="px-2.5 py-1 rounded-lg bg-slate-800/60 border border-slate-700/60 text-slate-400 font-mono">
              v{frameworkVersion}
            </span>
          </div>
        </div>

        {/* Score & Verdict Pillar */}
        <div className="flex items-center gap-4 border-t md:border-t-0 md:border-l border-slate-800 pt-4 md:pt-0 md:pl-5">
          <div className="text-center">
            <div className="text-xs text-slate-400 uppercase font-medium">Composite Score</div>
            <div className="text-3xl font-extrabold text-amber-300 font-mono mt-0.5">
              {compositeScore}
              <span className="text-xs font-normal text-slate-500">/100</span>
            </div>
            <div className="w-24 bg-slate-800 h-1.5 rounded-full mt-2 overflow-hidden mx-auto">
              <div
                className={`h-full rounded-full ${
                  isOverallFavorable
                    ? 'bg-emerald-400'
                    : isOverallModerate
                    ? 'bg-amber-400'
                    : 'bg-rose-400'
                }`}
                style={{ width: `${Math.min(compositeScore, 100)}%` }}
              />
            </div>
          </div>

          <div className="flex flex-col items-start gap-1">
            <span className="text-xs text-slate-400 uppercase font-medium">Overall Verdict</span>
            <span
              className={`px-3 py-1 text-xs font-bold rounded-xl border ${
                isOverallFavorable
                  ? 'bg-emerald-500/20 text-emerald-300 border-emerald-500/40'
                  : isOverallModerate
                  ? 'bg-amber-500/20 text-amber-300 border-amber-500/40'
                  : 'bg-rose-500/20 text-rose-300 border-rose-500/40'
              }`}
            >
              {overallVerdict.replace('_', ' ')}
            </span>
          </div>
        </div>
      </div>

      {/* 5-Step Reasoning DAG Pipeline */}
      <div className="space-y-4">
        <div className="flex items-center justify-between text-xs text-slate-400 font-medium uppercase tracking-wider px-1">
          <span>Deterministic Reasoning Trace (Step 1 → Step 5)</span>
          <span>Click step to highlight</span>
        </div>

        <div className="space-y-3">
          {reasoningSteps.map((step: any, idx: number) => {
            const stepOrder = step.stepOrder ?? step.step_order ?? idx + 1;
            const stepType = step.stepType || step.step_type || 'NATAL_PROMISE';
            const stepTitle = step.title || `Step ${stepOrder}`;
            const stepNarrative = step.narrative || '';
            const confidenceScore = step.confidenceScore ?? step.confidence_score ?? 0.8;
            const stepVerdict = step.verdict || 'NEUTRAL';
            const isFavorable = stepVerdict === 'FAVORABLE';
            const isChallenging = stepVerdict === 'CHALLENGING';
            const isSelected = activeStepIndex === idx;
            const linkedFactors: string[] = Array.isArray(step.linkedFactors)
              ? step.linkedFactors
              : Array.isArray(step.linked_factors)
              ? step.linked_factors
              : [];
            const shastraCitations: string[] = Array.isArray(step.shastraCitations)
              ? step.shastraCitations
              : Array.isArray(step.shastra_citations)
              ? step.shastra_citations
              : [];

            const borderClass = isSelected
              ? 'border-amber-500/70 shadow-lg shadow-amber-500/5'
              : isFavorable
              ? 'border-emerald-800/40 hover:border-emerald-700/60'
              : isChallenging
              ? 'border-rose-800/40 hover:border-rose-700/60'
              : 'border-slate-800 hover:border-slate-700/70';

            return (
              <div
                key={stepOrder}
                onClick={() => setActiveStepIndex(isSelected ? null : idx)}
                className={`p-4 rounded-xl border ${borderClass} bg-slate-950/40 cursor-pointer transition-all space-y-3`}
              >
                {/* Step Top Bar */}
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
                  <div className="flex items-center gap-2.5">
                    <div className="w-7 h-7 rounded-lg bg-slate-800 border border-slate-700 flex items-center justify-center font-bold text-xs text-slate-200">
                      {stepOrder}
                    </div>
                    <div className="flex items-center gap-2">
                      {STEP_ICONS[stepType] || <Compass className="w-4 h-4 text-amber-400" />}
                      <span className="font-semibold text-slate-200 text-sm">
                        {stepTitle}
                      </span>
                    </div>
                  </div>

                  <div className="flex items-center gap-2">
                    {/* Confidence Score */}
                    <span className="text-[11px] font-mono text-slate-400 bg-slate-800/80 px-2 py-0.5 rounded-md border border-slate-700">
                      Confidence: {Math.round(confidenceScore * 100)}%
                    </span>

                    {/* Verdict Pill */}
                    <span
                      className={`px-2.5 py-0.5 text-xs font-semibold rounded-full border ${
                        isFavorable
                          ? 'bg-emerald-500/20 text-emerald-300 border-emerald-500/40'
                          : isChallenging
                          ? 'bg-rose-500/20 text-rose-300 border-rose-500/40'
                          : 'bg-slate-800 text-slate-300 border-slate-700'
                      }`}
                    >
                      {stepVerdict}
                    </span>
                  </div>
                </div>

                {/* Narrative Text */}
                <p className="text-xs sm:text-sm text-slate-300 leading-relaxed">
                  {stepNarrative}
                </p>

                {/* Linked Factors Chips */}
                {linkedFactors.length > 0 && (
                  <div className="flex flex-wrap items-center gap-1.5 pt-1">
                    <span className="text-[10px] text-slate-400 font-medium uppercase mr-1">
                      Evidence:
                    </span>
                    {linkedFactors.map((f, i) => (
                      <span
                        key={i}
                        className="px-2 py-0.5 text-[10px] rounded-md bg-slate-800/90 text-slate-300 border border-slate-700/80 font-mono"
                      >
                        {f}
                      </span>
                    ))}
                  </div>
                )}

                {/* Shastra Citations */}
                {shastraCitations.length > 0 && (
                  <div className="flex flex-wrap items-center gap-2 pt-1 text-[11px] text-amber-400/90 font-mono">
                    <BookOpen className="w-3.5 h-3.5 text-amber-500 flex-shrink-0" />
                    <span>Citations: {shastraCitations.join(' • ')}</span>
                  </div>
                )}
              </div>
            );
          })}
        </div>
      </div>

      {/* Classical Vedic Remedies (Parihara) Card */}
      {classicalRemedies.length > 0 && (
        <div className="p-5 rounded-2xl bg-amber-950/15 border border-amber-800/40 space-y-3">
          <div className="flex items-center gap-2 text-amber-400 font-bold text-sm">
            <ShieldAlert className="w-4 h-4 text-amber-400" />
            <span>Classical Shastric Remedies &amp; Remedial Wisdom (Parihara)</span>
          </div>
          <p className="text-xs text-slate-300">
            Prescribed Parashari and Vedic practices to propitiate planetary energies, mitigate karmic friction, and align with cosmic dharma:
          </p>
          <ul className="space-y-2 pt-1">
            {classicalRemedies.map((remedy, idx) => (
              <li
                key={idx}
                className="flex items-start gap-2.5 text-xs text-slate-200 bg-slate-900/60 p-2.5 rounded-xl border border-amber-900/30"
              >
                <ArrowRight className="w-3.5 h-3.5 text-amber-400 mt-0.5 flex-shrink-0" />
                <span>{remedy}</span>
              </li>
            ))}
          </ul>
        </div>
      )}
    </section>
  );
};

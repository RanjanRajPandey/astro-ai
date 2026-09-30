import React, { useState } from 'react';
import {
  Clock,
  TrendingUp,
  Sparkles,
  CheckCircle2,
  AlertTriangle,
  Zap,
  Award,
} from 'lucide-react';
import type {
  TemporalAnalysisResponse,
  WindowClassification,
} from '../../types/astrology';

interface TemporalForecastSectionProps {
  temporalData: TemporalAnalysisResponse | null;
}

const CLASSIFICATION_STYLES: Record<
  WindowClassification,
  { label: string; badgeCls: string; barColor: string }
> = {
  HIGH_OPPORTUNITY: {
    label: 'High Opportunity',
    badgeCls: 'bg-emerald-950/80 text-emerald-300 border-emerald-700/60',
    barColor: '#10b981',
  },
  FAVORABLE_GROWTH: {
    label: 'Favorable Growth',
    badgeCls: 'bg-sky-950/80 text-sky-300 border-sky-700/60',
    barColor: '#38bdf8',
  },
  STEADY_CONSOLIDATION: {
    label: 'Steady Consolidation',
    badgeCls: 'bg-amber-950/80 text-amber-300 border-amber-700/60',
    barColor: '#f59e0b',
  },
  CAUTION_AND_REMEDY: {
    label: 'Caution & Discipline',
    badgeCls: 'bg-rose-950/80 text-rose-300 border-rose-700/60',
    barColor: '#f43f5e',
  },
};

export const TemporalForecastSection: React.FC<TemporalForecastSectionProps> = ({
  temporalData,
}) => {
  const [selectedWindowIdx, setSelectedWindowIdx] = useState<number>(1);
  const [selectedDomainCode, setSelectedDomainCode] = useState<string>('CAREER_AND_AUTHORITY');

  if (!temporalData) return null;

  const activeWindow =
    temporalData.timelineWindows.find((w) => w.windowIndex === selectedWindowIdx) ||
    temporalData.timelineWindows[0];

  const activeDomainEval =
    activeWindow?.domainEvaluations.find((d) => d.domainCode === selectedDomainCode) ||
    activeWindow?.domainEvaluations[0];

  return (
    <section className="bg-slate-900/70 border border-slate-800/80 rounded-2xl p-5 sm:p-6 shadow-xl space-y-6">
      {/* Header & Top Summary KPIs */}
      <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-4 pb-5 border-b border-slate-800/80">
        <div>
          <div className="flex items-center gap-2.5">
            <TrendingUp className="w-5 h-5 text-amber-400" />
            <h2 className="text-lg font-semibold text-white tracking-tight">
              Temporal Analysis &amp; Dasha–Gochar Confluence Forecast
            </h2>
            <span className="px-2.5 py-0.5 text-xs font-medium rounded-full bg-amber-500/10 text-amber-300 border border-amber-500/30">
              12-Month Multi-Window Synthesis
            </span>
          </div>
          <p className="text-xs text-slate-400 mt-1">
            Synthesizes static Natal Promise (30% Bhava Bala &amp; Shadbala) + dynamic Vimshottari
            Dasha Activation (40% MD/AD/PD) + Gochar &amp; Double Transit Confluence (30%).
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-3">
          <div className="bg-emerald-950/40 border border-emerald-700/40 rounded-xl px-3.5 py-1.5 flex items-center gap-2">
            <Sparkles className="w-4 h-4 text-emerald-400" />
            <div>
              <div className="text-[10px] uppercase tracking-wider text-emerald-400/80">
                Peak Overall Window
              </div>
              <div className="text-xs font-bold text-emerald-300">
                {temporalData.bestOverallWindowLabel}
              </div>
            </div>
          </div>

          <div className="bg-amber-950/40 border border-amber-700/40 rounded-xl px-3.5 py-1.5 flex items-center gap-2">
            <Award className="w-4 h-4 text-amber-400" />
            <div>
              <div className="text-[10px] uppercase tracking-wider text-amber-400/80">
                Strongest Life Domain
              </div>
              <div className="text-xs font-bold text-amber-300">
                {temporalData.strongestDomainCode.replace(/_/g, ' ')}
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* 5 Life-Domain Executive Summary Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-3">
        {temporalData.domainSummaries.map((dom) => {
          const isSelected = selectedDomainCode === dom.domainCode;
          const styleMeta =
            CLASSIFICATION_STYLES[dom.currentClassification] ||
            CLASSIFICATION_STYLES.STEADY_CONSOLIDATION;

          return (
            <button
              key={dom.domainCode}
              type="button"
              onClick={() => setSelectedDomainCode(dom.domainCode)}
              className={`text-left p-3.5 rounded-xl border transition flex flex-col justify-between ${
                isSelected
                  ? 'bg-amber-500/15 border-amber-500/60 shadow-lg'
                  : 'bg-slate-950/70 border-slate-800 hover:border-slate-700'
              }`}
            >
              <div>
                <div className="flex items-center justify-between gap-1 mb-1.5">
                  <span className="text-[10px] font-mono text-amber-400 font-semibold">
                    H{dom.primaryHouses.join(', H')}
                  </span>
                  <span
                    className={`px-1.5 py-0.5 text-[9px] font-semibold rounded border ${styleMeta.badgeCls}`}
                  >
                    {dom.averageConfluenceScore.toFixed(1)}
                  </span>
                </div>
                <h3 className="text-xs font-bold text-white line-clamp-2">{dom.domainTitle}</h3>
              </div>

              <div className="mt-3 pt-2 border-t border-slate-800/80 text-[11px] text-slate-400 space-y-0.5">
                <div>
                  Peak: <strong className="text-slate-200">{dom.peakWindowLabel}</strong>
                </div>
                <div>
                  Natal Promise:{' '}
                  <strong className="text-amber-300">{dom.natalPromiseScore.toFixed(1)}</strong> /
                  100
                </div>
              </div>
            </button>
          );
        })}
      </div>

      {/* 6-Window Rolling Timeline Selector */}
      <div className="space-y-2.5">
        <div className="flex items-center justify-between">
          <span className="text-xs font-bold text-slate-200 flex items-center gap-1.5">
            <Clock className="w-3.5 h-3.5 text-amber-400" />
            <span>Select 60-Day Forecast Window (Click any window to inspect confluence proof):</span>
          </span>
        </div>

        <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-2.5">
          {temporalData.timelineWindows.map((win) => {
            const isWinSelected = win.windowIndex === selectedWindowIdx;
            const domEval = win.domainEvaluations.find((d) => d.domainCode === selectedDomainCode);
            const score = domEval ? domEval.overallConfluenceScore : win.overallWindowScore;
            const clsMeta = domEval
              ? CLASSIFICATION_STYLES[domEval.windowClassification]
              : CLASSIFICATION_STYLES.FAVORABLE_GROWTH;

            return (
              <button
                key={win.windowIndex}
                type="button"
                onClick={() => setSelectedWindowIdx(win.windowIndex)}
                className={`p-3 rounded-xl border text-left transition ${
                  isWinSelected
                    ? 'bg-amber-500/15 border-amber-500 shadow-md'
                    : 'bg-slate-950/70 border-slate-800 hover:border-slate-700'
                }`}
              >
                <div className="flex items-center justify-between text-[10px] text-slate-400 mb-1">
                  <span>Window {win.windowIndex}</span>
                  {domEval?.doubleTransitTriggered && (
                    <span className="text-amber-300 font-bold flex items-center gap-0.5">
                      <Zap className="w-2.5 h-2.5" /> DT
                    </span>
                  )}
                </div>
                <div className="text-xs font-bold text-white">{win.windowLabel}</div>
                <div className="text-[11px] text-amber-300 font-mono mt-1">
                  {win.mahadashaLord.slice(0, 2)}–{win.antardashaLord.slice(0, 2)}–
                  {win.pratyantardashaLord.slice(0, 2)}
                </div>
                <div className="mt-2 flex items-center justify-between">
                  <span
                    className={`px-1.5 py-0.5 text-[10px] font-semibold rounded border ${clsMeta.badgeCls}`}
                  >
                    {score.toFixed(1)}
                  </span>
                  <span className="text-[10px] text-slate-400">
                    Ju:{win.jupiterTransitSign.slice(0, 3)} Sa:{win.saturnTransitSign.slice(0, 3)}
                  </span>
                </div>
              </button>
            );
          })}
        </div>
      </div>

      {/* Selected Window + Domain Deep Confluence Inspector */}
      {activeWindow && activeDomainEval && (
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-5 bg-slate-950/80 border border-slate-800 rounded-2xl p-5">
          {/* Left 5 Cols: 3-Pillar Score Breakdown & Active Timing Stack */}
          <div className="lg:col-span-5 space-y-4">
            <div className="border-b border-slate-800 pb-3">
              <div className="flex items-center justify-between">
                <span className="text-[11px] font-mono uppercase tracking-wider text-amber-400">
                  {activeWindow.windowLabel} • Window #{activeWindow.windowIndex}
                </span>
                <span
                  className={`px-2.5 py-0.5 text-xs font-semibold rounded-lg border ${
                    CLASSIFICATION_STYLES[activeDomainEval.windowClassification].badgeCls
                  }`}
                >
                  {CLASSIFICATION_STYLES[activeDomainEval.windowClassification].label}
                </span>
              </div>
              <h3 className="text-sm font-bold text-white mt-1">{activeDomainEval.domainTitle}</h3>
              <p className="text-xs text-slate-400 mt-0.5">
                Active Dasha:{' '}
                <strong className="text-amber-300">
                  {activeWindow.mahadashaLord} (MD) → {activeWindow.antardashaLord} (AD) →{' '}
                  {activeWindow.pratyantardashaLord} (PD)
                </strong>
              </p>
            </div>

            {/* 3-Pillar Progress Bars */}
            <div className="space-y-3 text-xs">
              <div>
                <div className="flex justify-between mb-1">
                  <span className="text-slate-300">
                    1. Static Natal Promise (30% Weight • Bhava &amp; Shadbala)
                  </span>
                  <span className="font-bold text-white">
                    {activeDomainEval.natalPromiseScore.toFixed(1)} / 100
                  </span>
                </div>
                <div className="h-2 rounded-full bg-slate-800 overflow-hidden">
                  <div
                    className="h-full bg-amber-400 rounded-full"
                    style={{ width: `${Math.min(100, activeDomainEval.natalPromiseScore)}%` }}
                  />
                </div>
              </div>

              <div>
                <div className="flex justify-between mb-1">
                  <span className="text-slate-300">
                    2. Vimshottari Dasha Activation (40% Weight • MD/AD/PD)
                  </span>
                  <span className="font-bold text-white">
                    {activeDomainEval.dashaActivationScore.toFixed(1)} / 100
                  </span>
                </div>
                <div className="h-2 rounded-full bg-slate-800 overflow-hidden">
                  <div
                    className="h-full bg-purple-400 rounded-full"
                    style={{ width: `${Math.min(100, activeDomainEval.dashaActivationScore)}%` }}
                  />
                </div>
              </div>

              <div>
                <div className="flex justify-between mb-1">
                  <span className="text-slate-300">
                    3. Gochar &amp; Double Transit Confluence (30% Weight)
                  </span>
                  <span className="font-bold text-white">
                    {activeDomainEval.transitConfluenceScore.toFixed(1)} / 100
                  </span>
                </div>
                <div className="h-2 rounded-full bg-slate-800 overflow-hidden">
                  <div
                    className="h-full bg-emerald-400 rounded-full"
                    style={{ width: `${Math.min(100, activeDomainEval.transitConfluenceScore)}%` }}
                  />
                </div>
              </div>

              <div className="pt-2 border-t border-slate-800 flex items-center justify-between">
                <span className="font-bold text-slate-200">Synthesized Confluence Score:</span>
                <span className="text-base font-extrabold text-amber-300">
                  {activeDomainEval.overallConfluenceScore.toFixed(1)} / 100
                </span>
              </div>
            </div>
          </div>

          {/* Right 7 Cols: Supporting vs Challenging Astrological Evidence */}
          <div className="lg:col-span-7 grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div className="bg-slate-900/80 border border-slate-800 rounded-xl p-4 space-y-2.5">
              <div className="text-xs font-bold text-emerald-300 uppercase tracking-wider flex items-center gap-1.5">
                <CheckCircle2 className="w-4 h-4 text-emerald-400" />
                <span>Supporting Dasha &amp; Gochar Evidence</span>
              </div>
              <ul className="space-y-2">
                {activeDomainEval.supportingFactors.map((fac, idx) => (
                  <li key={idx} className="text-xs text-slate-200 leading-relaxed flex gap-2">
                    <span className="text-emerald-400 font-bold">•</span>
                    <span>{fac}</span>
                  </li>
                ))}
              </ul>
            </div>

            <div className="bg-slate-900/80 border border-slate-800 rounded-xl p-4 space-y-2.5">
              <div className="text-xs font-bold text-amber-300 uppercase tracking-wider flex items-center gap-1.5">
                <AlertTriangle className="w-4 h-4 text-amber-400" />
                <span>Challenging Factors &amp; Pacing Notes</span>
              </div>
              <ul className="space-y-2">
                {activeDomainEval.challengingFactors.map((fac, idx) => (
                  <li key={idx} className="text-xs text-slate-300 leading-relaxed flex gap-2">
                    <span className="text-amber-400 font-bold">•</span>
                    <span>{fac}</span>
                  </li>
                ))}
              </ul>
            </div>
          </div>
        </div>
      )}
    </section>
  );
};

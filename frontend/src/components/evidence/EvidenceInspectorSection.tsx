import React, { useState, useMemo } from 'react';
import {
  FileCheck2,
  Filter,
  CheckCircle2,
  AlertTriangle,
  HelpCircle,
  BookOpen,
  Layers,
  Sparkles,
  RefreshCw,
  Scale,
} from 'lucide-react';
import type {
  EvidenceGenerationResponse,
  EvidenceItem,
} from '../../types/astrology';

interface EvidenceInspectorSectionProps {
  evidenceData: EvidenceGenerationResponse | null;
  isLoading?: boolean;
  onRefreshEvidence?: () => void;
}

type FindingFilter = 'ALL' | 'FAVORABLE' | 'CHALLENGING' | 'NEUTRAL';
type CategoryFilter =
  | 'ALL'
  | 'HOUSE_AND_LORD'
  | 'KARAKA_STRENGTH'
  | 'DIVISIONAL_VARGA'
  | 'YOGA_OR_DOSHA'
  | 'DASHA_AND_GOCHAR';

const CATEGORY_LABELS: Record<string, string> = {
  HOUSE_AND_LORD: 'Bhava Bala & Lords',
  KARAKA_STRENGTH: 'Karaka & Shadbala',
  DIVISIONAL_VARGA: 'Shodashavarga (D9/D10)',
  YOGA_OR_DOSHA: 'Classical Yogas',
  DASHA_AND_GOCHAR: 'Dasha & Transits',
};

export const EvidenceInspectorSection: React.FC<EvidenceInspectorSectionProps> = ({
  evidenceData,
  isLoading = false,
  onRefreshEvidence,
}) => {
  const [selectedFinding, setSelectedFinding] = useState<FindingFilter>('ALL');
  const [selectedCategory, setSelectedCategory] = useState<CategoryFilter>('ALL');
  const [searchQuery, setSearchQuery] = useState<string>('');

  const raw = evidenceData as any;
  const questionText = raw?.questionText || raw?.question_text || 'Inquiry Evidence';
  const questionCategory = raw?.questionCategory || raw?.question_category || 'GENERAL';
  const frameworkVersion = raw?.frameworkVersion || raw?.framework_version || '1.0';
  const evidenceItems: any[] = Array.isArray(raw?.evidenceItems)
    ? raw.evidenceItems
    : Array.isArray(raw?.evidence_items)
    ? raw.evidence_items
    : [];
  const totalEvidenceCount = raw?.totalEvidenceCount ?? raw?.total_evidence_count ?? evidenceItems.length;
  const favorableCount = raw?.favorableCount ?? raw?.favorable_count ?? 0;
  const challengingCount = raw?.challengingCount ?? raw?.challenging_count ?? 0;
  const neutralCount = raw?.neutralCount ?? raw?.neutral_count ?? 0;
  const timeWindowsSummary: string[] = Array.isArray(raw?.timeWindowsSummary)
    ? raw.timeWindowsSummary
    : Array.isArray(raw?.time_windows_summary)
    ? raw.time_windows_summary
    : [];

  const filteredItems = useMemo(() => {
    if (!evidenceItems || !evidenceItems.length) return [];
    return evidenceItems.filter((item: any) => {
      const finding =
        item.finding || (item.classification === 'SUPPORTING' ? 'FAVORABLE' : item.classification || 'NEUTRAL');
      const ruleRef = item.ruleReference || item.rule_reference || item.rule || '';
      const matchFinding =
        selectedFinding === 'ALL' || finding === selectedFinding;
      const matchCategory =
        selectedCategory === 'ALL' || item.category === selectedCategory;
      const matchSearch =
        !searchQuery.trim() ||
        (item.factor && item.factor.toLowerCase().includes(searchQuery.toLowerCase())) ||
        (item.observation && item.observation.toLowerCase().includes(searchQuery.toLowerCase())) ||
        ruleRef.toLowerCase().includes(searchQuery.toLowerCase());
      return matchFinding && matchCategory && matchSearch;
    });
  }, [evidenceItems, selectedFinding, selectedCategory, searchQuery]);

  if (!evidenceData) return null;

  return (
    <section className="bg-slate-900/70 border border-slate-800/80 rounded-2xl p-5 sm:p-6 shadow-xl space-y-6">
      {/* Section Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 border-b border-slate-800/80 pb-5">
        <div className="flex items-center space-x-3">
          <div className="p-2.5 rounded-xl bg-gradient-to-br from-emerald-500/20 to-teal-500/10 border border-emerald-500/30 text-emerald-400">
            <FileCheck2 className="w-6 h-6" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h2 className="text-xl font-bold text-slate-100">
                Phase 16: Evidence Engine &amp; Structured Observation Pipeline
              </h2>
              <span className="px-2 py-0.5 text-xs font-semibold rounded-full bg-emerald-500/10 border border-emerald-500/30 text-emerald-300">
                Deterministic Shastras
              </span>
            </div>
            <p className="text-xs sm:text-sm text-slate-400 mt-0.5">
              Verified Jyotish findings, classical BPHS citations, and weighted evidentiary building blocks for reasoning.
            </p>
          </div>
        </div>

        {onRefreshEvidence && (
          <button
            onClick={onRefreshEvidence}
            disabled={isLoading}
            className="flex items-center justify-center gap-2 px-3.5 py-2 rounded-xl bg-slate-800 hover:bg-slate-700/80 border border-slate-700 text-slate-200 text-xs font-medium transition disabled:opacity-50"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${isLoading ? 'animate-spin' : ''}`} />
            <span>Re-evaluate Evidence</span>
          </button>
        )}
      </div>

      {/* Inquiry Context & Framework Pill */}
      <div className="p-4 rounded-xl bg-slate-800/40 border border-slate-700/60 flex flex-col md:flex-row md:items-center justify-between gap-3 text-xs sm:text-sm">
        <div className="space-y-1">
          <div className="text-slate-400">Active Inquiry &amp; Framework Target:</div>
          <div className="font-semibold text-amber-300 flex items-center gap-2">
            <Sparkles className="w-3.5 h-3.5 text-amber-400" />
            &ldquo;{questionText}&rdquo;
          </div>
        </div>
        <div className="flex flex-wrap items-center gap-2">
          <span className="px-2.5 py-1 rounded-lg bg-indigo-500/10 border border-indigo-500/30 text-indigo-300 font-mono text-xs">
            Category: {questionCategory}
          </span>
          <span className="px-2.5 py-1 rounded-lg bg-slate-700/50 border border-slate-600/50 text-slate-300 font-mono text-xs">
            v{frameworkVersion}
          </span>
        </div>
      </div>

      {/* Metric Counters */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
        <div className="p-4 rounded-xl bg-slate-800/50 border border-slate-700/60 flex flex-col justify-between">
          <span className="text-xs font-medium text-slate-400 uppercase tracking-wider flex items-center gap-1.5">
            <Layers className="w-3.5 h-3.5 text-slate-400" />
            Total Evidence
          </span>
          <span className="text-2xl font-bold text-slate-100 mt-2">
            {totalEvidenceCount}
          </span>
          <span className="text-[11px] text-slate-500 mt-1">
            Evaluated Shastra factors
          </span>
        </div>

        <div className="p-4 rounded-xl bg-emerald-950/20 border border-emerald-800/40 flex flex-col justify-between">
          <span className="text-xs font-medium text-emerald-400 uppercase tracking-wider flex items-center gap-1.5">
            <CheckCircle2 className="w-3.5 h-3.5 text-emerald-400" />
            Favorable
          </span>
          <span className="text-2xl font-bold text-emerald-300 mt-2">
            {favorableCount}
          </span>
          <span className="text-[11px] text-emerald-500/80 mt-1">
            Auspicious indicators
          </span>
        </div>

        <div className="p-4 rounded-xl bg-amber-950/20 border border-amber-800/40 flex flex-col justify-between">
          <span className="text-xs font-medium text-amber-400 uppercase tracking-wider flex items-center gap-1.5">
            <AlertTriangle className="w-3.5 h-3.5 text-amber-400" />
            Challenging
          </span>
          <span className="text-2xl font-bold text-amber-300 mt-2">
            {challengingCount}
          </span>
          <span className="text-[11px] text-amber-500/80 mt-1">
            Afflictions / remedial
          </span>
        </div>

        <div className="p-4 rounded-xl bg-slate-800/30 border border-slate-700/40 flex flex-col justify-between">
          <span className="text-xs font-medium text-slate-400 uppercase tracking-wider flex items-center gap-1.5">
            <HelpCircle className="w-3.5 h-3.5 text-slate-400" />
            Neutral / Baseline
          </span>
          <span className="text-2xl font-bold text-slate-300 mt-2">
            {neutralCount}
          </span>
          <span className="text-[11px] text-slate-500 mt-1">
            Stabilizing matrix
          </span>
        </div>
      </div>

      {/* Filter and Search Bar */}
      <div className="flex flex-col lg:flex-row items-stretch lg:items-center justify-between gap-3 pt-2">
        {/* Finding tabs */}
        <div className="flex items-center gap-1.5 p-1 bg-slate-950/60 border border-slate-800 rounded-xl overflow-x-auto">
          {(['ALL', 'FAVORABLE', 'CHALLENGING', 'NEUTRAL'] as FindingFilter[]).map((tab) => {
            const isActive = selectedFinding === tab;
            return (
              <button
                key={tab}
                onClick={() => setSelectedFinding(tab)}
                className={`px-3 py-1.5 rounded-lg text-xs font-medium whitespace-nowrap transition ${
                  isActive
                    ? 'bg-slate-800 text-amber-300 shadow-sm border border-slate-700'
                    : 'text-slate-400 hover:text-slate-200'
                }`}
              >
                {tab === 'ALL' ? 'All Findings' : tab.charAt(0) + tab.slice(1).toLowerCase()}
              </button>
            );
          })}
        </div>

        {/* Category & Search */}
        <div className="flex flex-wrap sm:flex-nowrap items-center gap-2">
          <div className="flex items-center gap-1.5 text-xs text-slate-400 bg-slate-950/60 border border-slate-800 rounded-xl px-3 py-1.5">
            <Filter className="w-3.5 h-3.5 text-slate-400" />
            <select
              value={selectedCategory}
              onChange={(e) => setSelectedCategory(e.target.value as CategoryFilter)}
              className="bg-transparent text-slate-200 outline-none text-xs cursor-pointer"
            >
              <option value="ALL" className="bg-slate-900 text-slate-200">
                All Categories
              </option>
              {Object.entries(CATEGORY_LABELS).map(([catKey, label]) => (
                <option key={catKey} value={catKey} className="bg-slate-900 text-slate-200">
                  {label}
                </option>
              ))}
            </select>
          </div>

          <input
            type="text"
            placeholder="Search factors or rules..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="flex-1 sm:w-56 px-3 py-1.5 text-xs bg-slate-950/60 border border-slate-800 rounded-xl text-slate-200 placeholder-slate-500 focus:outline-none focus:border-amber-500/50"
          />
        </div>
      </div>

      {/* Evidence Items Grid */}
      <div className="space-y-3">
        {filteredItems.length === 0 ? (
          <div className="p-8 text-center rounded-xl bg-slate-950/40 border border-slate-800 text-slate-400 text-sm">
            No evidence items match the selected filters.
          </div>
        ) : (
          filteredItems.map((item: EvidenceItem, idx: number) => {
            const findingStr =
              item.finding || (item.classification === 'SUPPORTING' ? 'FAVORABLE' : item.classification || 'NEUTRAL');
            const isFavorable = findingStr === 'FAVORABLE';
            const isChallenging = findingStr === 'CHALLENGING';
            const borderCol = isFavorable
              ? 'border-emerald-800/40 hover:border-emerald-700/60'
              : isChallenging
              ? 'border-rose-800/40 hover:border-rose-700/60'
              : 'border-slate-800 hover:border-slate-700/60';
            const bgCol = isFavorable
              ? 'bg-emerald-950/10'
              : isChallenging
              ? 'bg-rose-950/10'
              : 'bg-slate-950/30';

            return (
              <div
                key={`${item.factor}-${idx}`}
                className={`p-4 rounded-xl border ${borderCol} ${bgCol} transition space-y-2.5`}
              >
                {/* Header row */}
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
                  <div className="flex items-center gap-2">
                    <span className="font-semibold text-slate-200 text-sm">
                      {item.factor}
                    </span>
                    <span className="px-2 py-0.5 text-[10px] font-medium rounded-full bg-slate-800 text-slate-300 border border-slate-700">
                      {CATEGORY_LABELS[item.category] || item.category}
                    </span>
                  </div>

                  <div className="flex items-center gap-2">
                    {/* Weight pill */}
                    <span className="flex items-center gap-1 text-[11px] font-mono text-slate-400 bg-slate-800/80 px-2 py-0.5 rounded-md border border-slate-700/80">
                      <Scale className="w-3 h-3 text-slate-400" />
                      Weight: {Math.round((item.weight ?? 0.15) * 100)}%
                    </span>

                    {/* Finding badge */}
                    <span
                      className={`px-2.5 py-0.5 text-xs font-semibold rounded-full border ${
                        isFavorable
                          ? 'bg-emerald-500/20 text-emerald-300 border-emerald-500/40'
                          : isChallenging
                          ? 'bg-rose-500/20 text-rose-300 border-rose-500/40'
                          : 'bg-slate-800 text-slate-300 border-slate-700'
                      }`}
                    >
                      {findingStr}
                    </span>
                  </div>
                </div>

                {/* Observation narrative */}
                <p className="text-xs sm:text-sm text-slate-300 leading-relaxed">
                  {item.observation}
                </p>

                {/* Shastra Reference footer */}
                <div className="flex items-center gap-1.5 pt-1 text-[11px] text-amber-400/90 font-mono">
                  <BookOpen className="w-3.5 h-3.5 text-amber-500" />
                  <span>Classical Rule: {item.ruleReference || item.rule || 'BPHS & Phaladeepika'}</span>
                </div>
              </div>
            );
          })
        )}
      </div>

      {/* Time Windows & Temporal Summary */}
      {timeWindowsSummary.length > 0 && (
        <div className="p-4 rounded-xl bg-slate-950/50 border border-slate-800 space-y-2">
          <div className="text-xs font-semibold text-slate-300 flex items-center gap-1.5">
            <Layers className="w-3.5 h-3.5 text-indigo-400" />
            Temporal Confluence Windows Linked to Evidence:
          </div>
          <div className="flex flex-wrap gap-2 pt-1">
            {timeWindowsSummary.map((tw: string, i: number) => (
              <span
                key={i}
                className="px-2.5 py-1 text-xs rounded-lg bg-indigo-950/40 border border-indigo-800/40 text-indigo-300 font-mono"
              >
                {tw}
              </span>
            ))}
          </div>
        </div>
      )}
    </section>
  );
};

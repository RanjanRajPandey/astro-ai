import React, { useMemo, useState } from 'react';
import {
  Sparkles,
  ShieldAlert,
  ShieldCheck,
  CheckCircle2,
  XCircle,
  BookOpen,
  Filter,
  Award,
} from 'lucide-react';
import type {
  YogaCalculationResponse,
  YogaCategory,
  YogaEvaluationEntry,
} from '../../types/astrology';
import { PLANET_COLORS } from '../../utils/chartMath';

interface YogaExplorerSectionProps {
  yogaData: YogaCalculationResponse | null;
  loading: boolean;
}

const CATEGORY_META: Record<
  YogaCategory | 'ALL',
  { label: string; badgeClass: string }
> = {
  ALL: {
    label: 'All Categories',
    badgeClass: 'bg-slate-800 text-slate-300 border-slate-700',
  },
  PANCHA_MAHAPURUSHA: {
    label: 'Pancha Mahapurusha',
    badgeClass: 'bg-amber-950/60 text-amber-300 border-amber-700/50',
  },
  RAJA_YOGA: {
    label: 'Raja Yoga',
    badgeClass: 'bg-purple-950/60 text-purple-300 border-purple-700/50',
  },
  DHANA_YOGA: {
    label: 'Dhana Yoga (Wealth)',
    badgeClass: 'bg-emerald-950/60 text-emerald-300 border-emerald-700/50',
  },
  LUNAR_YOGA: {
    label: 'Chandra (Lunar) Yoga',
    badgeClass: 'bg-sky-950/60 text-sky-300 border-sky-700/50',
  },
  SOLAR_YOGA: {
    label: 'Surya (Solar) Yoga',
    badgeClass: 'bg-orange-950/60 text-orange-300 border-orange-700/50',
  },
  VIPARITA_RAJA_YOGA: {
    label: 'Viparita Raja Yoga',
    badgeClass: 'bg-indigo-950/60 text-indigo-300 border-indigo-700/50',
  },
  SPECIAL_YOGA: {
    label: 'Special & Neecha Bhanga',
    badgeClass: 'bg-teal-950/60 text-teal-300 border-teal-700/50',
  },
  NEECHA_BHANGA: {
    label: 'Neecha Bhanga',
    badgeClass: 'bg-teal-950/60 text-teal-300 border-teal-700/50',
  },
  DOSHA: {
    label: 'Doshas & Parihara',
    badgeClass: 'bg-rose-950/60 text-rose-300 border-rose-700/50',
  },
};

export const YogaExplorerSection: React.FC<YogaExplorerSectionProps> = ({
  yogaData,
  loading,
}) => {
  const [viewMode, setViewMode] = useState<'DETECTED' | 'ALL_CATALOG'>('DETECTED');
  const [selectedCategory, setSelectedCategory] = useState<YogaCategory | 'ALL'>('ALL');
  const [selectedYogaCode, setSelectedYogaCode] = useState<string | null>(null);

  const displayedYogas = useMemo(() => {
    if (!yogaData) return [];
    const baseList =
      viewMode === 'DETECTED'
        ? yogaData.allEvaluatedYogas.filter(
            (y) => y.status !== 'NOT_FORMED' && y.status !== 'ABSENT',
          )
        : yogaData.allEvaluatedYogas;

    if (selectedCategory === 'ALL') {
      return baseList;
    }
    return baseList.filter((y) => y.category === selectedCategory);
  }, [yogaData, viewMode, selectedCategory]);

  const focusedYoga: YogaEvaluationEntry | null = useMemo(() => {
    if (displayedYogas.length === 0) return null;
    if (selectedYogaCode) {
      const found = displayedYogas.find((y) => y.yogaCode === selectedYogaCode);
      if (found) return found;
    }
    return displayedYogas[0];
  }, [displayedYogas, selectedYogaCode]);

  if (loading && !yogaData) {
    return (
      <div className="bg-slate-900/70 border border-slate-800 rounded-2xl p-6 animate-pulse">
        <div className="h-6 w-72 bg-slate-800 rounded mb-4" />
        <div className="h-64 bg-slate-800/50 rounded-xl" />
      </div>
    );
  }

  if (!yogaData) return null;

  return (
    <section className="bg-slate-900/70 border border-slate-800/80 rounded-2xl p-5 sm:p-6 shadow-xl">
      {/* Header & Summary KPIs */}
      <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-4 pb-5 border-b border-slate-800/80">
        <div>
          <div className="flex items-center gap-2.5">
            <Sparkles className="w-5 h-5 text-amber-400" />
            <h2 className="text-lg font-semibold text-white tracking-tight">
              Classical Vedic Yogas &amp; Dosha Parihara Engine
            </h2>
            <span className="px-2.5 py-0.5 text-xs font-medium rounded-full bg-amber-500/10 text-amber-300 border border-amber-500/30">
              BPHS &amp; Phaladeepika ({yogaData.totalEvaluatedCount} Rules)
            </span>
          </div>
          <p className="text-xs text-slate-400 mt-1">
            Deterministic planetary combination detector verifying Pancha Mahapurusha, Raja, Dhana,
            Lunar, Solar, Viparita, Neecha Bhanga, and Dosha cancellations with full condition proof.
          </p>
        </div>

        {/* Summary KPI Pills */}
        <div className="flex flex-wrap items-center gap-2.5">
          <div className="bg-emerald-950/40 border border-emerald-700/40 rounded-xl px-3 py-1.5 flex items-center gap-2">
            <Award className="w-4 h-4 text-emerald-400" />
            <div>
              <div className="text-[10px] uppercase tracking-wider text-emerald-400/80">
                Active Benefic Yogas
              </div>
              <div className="text-xs font-semibold text-emerald-300">
                {yogaData.activeYogaCount} Active
              </div>
            </div>
          </div>

          <div className="bg-rose-950/40 border border-rose-700/40 rounded-xl px-3 py-1.5 flex items-center gap-2">
            <ShieldAlert className="w-4 h-4 text-rose-400" />
            <div>
              <div className="text-[10px] uppercase tracking-wider text-rose-400/80">
                Active Doshas
              </div>
              <div className="text-xs font-semibold text-rose-300">
                {yogaData.activeDoshaCount} Active
              </div>
            </div>
          </div>

          <div className="bg-sky-950/40 border border-sky-700/40 rounded-xl px-3 py-1.5 flex items-center gap-2">
            <ShieldCheck className="w-4 h-4 text-sky-400" />
            <div>
              <div className="text-[10px] uppercase tracking-wider text-sky-400/80">
                Mitigated / Parihara
              </div>
              <div className="text-xs font-semibold text-sky-300">
                {yogaData.mitigatedCount} Cancelled
              </div>
            </div>
          </div>

          {/* View Mode Switcher */}
          <div className="flex bg-slate-950/90 p-1 rounded-xl border border-slate-800">
            <button
              type="button"
              onClick={() => setViewMode('DETECTED')}
              className={`px-3 py-1.5 rounded-lg text-xs font-medium transition ${
                viewMode === 'DETECTED'
                  ? 'bg-amber-500/20 text-amber-300 border border-amber-500/40'
                  : 'text-slate-400 hover:text-slate-200'
              }`}
            >
              Detected ({yogaData.activeYogaCount + yogaData.activeDoshaCount + yogaData.mitigatedCount})
            </button>
            <button
              type="button"
              onClick={() => setViewMode('ALL_CATALOG')}
              className={`px-3 py-1.5 rounded-lg text-xs font-medium transition ${
                viewMode === 'ALL_CATALOG'
                  ? 'bg-amber-500/20 text-amber-300 border border-amber-500/40'
                  : 'text-slate-400 hover:text-slate-200'
              }`}
            >
              Full Classical Audit ({yogaData.totalEvaluatedCount})
            </button>
          </div>
        </div>
      </div>

      {/* Category Filter Bar */}
      <div className="mt-4 flex items-center gap-2 overflow-x-auto pb-2">
        <div className="flex items-center gap-1.5 text-xs text-slate-400 mr-1 shrink-0">
          <Filter className="w-3.5 h-3.5 text-amber-400" />
          <span>Category:</span>
        </div>
        {(Object.keys(CATEGORY_META) as Array<YogaCategory | 'ALL'>).map((cat) => {
          const isSelected = selectedCategory === cat;
          const meta = CATEGORY_META[cat];
          return (
            <button
              key={cat}
              type="button"
              onClick={() => setSelectedCategory(cat)}
              className={`px-2.5 py-1 rounded-lg text-xs font-medium border transition shrink-0 ${
                isSelected
                  ? 'bg-amber-500/20 text-amber-300 border-amber-500/50 shadow-sm'
                  : 'bg-slate-950/60 text-slate-400 border-slate-800 hover:text-slate-200 hover:border-slate-700'
              }`}
            >
              {meta.label}
            </button>
          );
        })}
      </div>

      {/* Main Content Split: Yoga List + Proof Inspector */}
      {displayedYogas.length === 0 ? (
        <div className="mt-6 bg-slate-950/50 border border-slate-800/80 rounded-xl p-8 text-center">
          <p className="text-sm text-slate-300 font-medium">
            No Yogas or Doshas matched the current filter.
          </p>
          <p className="text-xs text-slate-500 mt-1">
            Switch to &ldquo;Full 24-Rule Audit&rdquo; or select &ldquo;All Categories&rdquo; to inspect all classical rules.
          </p>
        </div>
      ) : (
        <div className="mt-5 grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
          {/* Left 7 Cols: Interactive Yoga & Dosha Cards */}
          <div className="lg:col-span-7 grid grid-cols-1 sm:grid-cols-2 gap-3 max-h-[560px] overflow-y-auto pr-1">
            {displayedYogas.map((yoga) => {
              const isSelected = focusedYoga?.yogaCode === yoga.yogaCode;
              const catMeta = CATEGORY_META[yoga.category] || CATEGORY_META.ALL;

              let statusBadge = {
                text: 'Absent',
                cls: 'bg-slate-800/90 text-slate-400 border-slate-700',
              };
              if (yoga.status === 'ACTIVE') {
                statusBadge = yoga.isBenefic
                  ? {
                      text: `Active • ${yoga.strength}`,
                      cls: 'bg-emerald-950/80 text-emerald-300 border-emerald-700/60',
                    }
                  : {
                      text: `Active Dosha • ${yoga.strength}`,
                      cls: 'bg-rose-950/80 text-rose-300 border-rose-700/60',
                    };
              } else if (yoga.status === 'CANCELLED_OR_MITIGATED') {
                statusBadge = {
                  text: 'Mitigated (Parihara)',
                  cls: 'bg-sky-950/80 text-sky-300 border-sky-700/60',
                };
              }

              return (
                <button
                  key={yoga.yogaCode}
                  type="button"
                  onClick={() => setSelectedYogaCode(yoga.yogaCode)}
                  className={`text-left p-4 rounded-xl border transition flex flex-col justify-between ${
                    isSelected
                      ? 'bg-amber-500/10 border-amber-500/50 shadow-md'
                      : 'bg-slate-950/60 border-slate-800/80 hover:bg-slate-900/80 hover:border-slate-700'
                  }`}
                >
                  <div>
                    <div className="flex items-center justify-between gap-2 mb-2">
                      <span
                        className={`px-2 py-0.5 text-[10px] font-semibold rounded border ${catMeta.badgeClass}`}
                      >
                        {catMeta.label}
                      </span>
                      <span
                        className={`px-2 py-0.5 text-[10px] font-semibold rounded border ${statusBadge.cls}`}
                      >
                        {statusBadge.text}
                      </span>
                    </div>

                    <h3 className="text-sm font-bold text-white">{yoga.name}</h3>
                    <p className="text-[11px] text-slate-400 italic mt-0.5">
                      {yoga.sanskritName}
                    </p>
                    <p className="text-xs text-slate-300 mt-2 line-clamp-2">
                      {yoga.definition}
                    </p>
                  </div>

                  {/* Involved Planets & Houses */}
                  <div className="mt-3 pt-2.5 border-t border-slate-800/70 flex flex-wrap items-center justify-between gap-2">
                    <div className="flex flex-wrap items-center gap-1">
                      {yoga.planetsInvolved.length > 0 ? (
                        yoga.planetsInvolved.map((p) => {
                          const pColor = PLANET_COLORS[p] || '#fbbf24';
                          return (
                            <span
                              key={p}
                              className="px-1.5 py-0.5 text-[10px] font-semibold rounded bg-slate-900 border border-slate-700/80"
                              style={{ color: pColor }}
                            >
                              {p}
                            </span>
                          );
                        })
                      ) : (
                        <span className="text-[10px] text-slate-500">No active grahas</span>
                      )}
                    </div>

                    {yoga.housesInvolved.length > 0 && (
                      <div className="flex items-center gap-1">
                        {yoga.housesInvolved.map((h) => (
                          <span
                            key={h}
                            className="px-1.5 py-0.5 text-[10px] font-mono rounded bg-slate-900 text-amber-300 border border-slate-800"
                          >
                            H{h}
                          </span>
                        ))}
                      </div>
                    )}
                  </div>
                </button>
              );
            })}
          </div>

          {/* Right 5 Cols: Deterministic "Why This Yoga Formed" Verification Inspector */}
          {focusedYoga && (
            <div className="lg:col-span-5 bg-slate-950/80 border border-slate-800 rounded-2xl p-5 sticky top-4">
              <div className="flex items-start justify-between gap-3 pb-3.5 border-b border-slate-800">
                <div>
                  <span className="text-[10px] font-mono uppercase tracking-wider text-amber-400">
                    {focusedYoga.yogaCode} • {focusedYoga.category.replace(/_/g, ' ')}
                  </span>
                  <h3 className="text-base font-bold text-white mt-0.5">
                    {focusedYoga.name}
                  </h3>
                  <p className="text-xs text-slate-400 italic">
                    {focusedYoga.sanskritName}
                  </p>
                </div>

                <span
                  className={`px-2.5 py-1 text-xs font-semibold rounded-lg border shrink-0 ${
                    focusedYoga.status === 'ACTIVE'
                      ? focusedYoga.isBenefic
                        ? 'bg-emerald-950/80 text-emerald-300 border-emerald-700/60'
                        : 'bg-rose-950/80 text-rose-300 border-rose-700/60'
                      : focusedYoga.status === 'CANCELLED_OR_MITIGATED'
                        ? 'bg-sky-950/80 text-sky-300 border-sky-700/60'
                        : 'bg-slate-900 text-slate-400 border-slate-800'
                  }`}
                >
                  {focusedYoga.status.replace(/_/g, ' ')}
                </span>
              </div>

              {/* Classical Rule Definition */}
              <div className="mt-4">
                <div className="flex items-center gap-1.5 text-xs font-semibold text-slate-200 mb-1.5">
                  <BookOpen className="w-3.5 h-3.5 text-amber-400" />
                  <span>Classical Shastra Definition</span>
                </div>
                <p className="text-xs text-slate-300 leading-relaxed bg-slate-900/70 border border-slate-800/80 rounded-xl p-3">
                  {focusedYoga.definition}
                </p>
              </div>

              {/* Required Conditions vs Detected Astrological Evidence */}
              <div className="mt-4">
                <div className="text-xs font-semibold text-slate-200 mb-2">
                  Deterministic Condition Verification (&ldquo;Why This Answer?&rdquo;)
                </div>

                <div className="space-y-2">
                  <div className="bg-slate-900/60 border border-slate-800/80 rounded-xl p-3">
                    <div className="text-[10px] uppercase tracking-wider text-slate-400 font-semibold mb-1.5">
                      Required Classical Criteria
                    </div>
                    <ul className="space-y-1">
                      {focusedYoga.requiredConditions.map((cond, idx) => (
                        <li
                          key={idx}
                          className="text-xs text-slate-300 flex items-start gap-2"
                        >
                          <span className="text-amber-400 font-mono text-[11px] mt-0.5">
                            {idx + 1}.
                          </span>
                          <span>{cond}</span>
                        </li>
                      ))}
                    </ul>
                  </div>

                  <div className="bg-slate-900/90 border border-slate-800 rounded-xl p-3">
                    <div className="text-[10px] uppercase tracking-wider text-amber-300 font-semibold mb-1.5">
                      Observed Chart Evidence
                    </div>
                    <ul className="space-y-1.5">
                      {focusedYoga.detectedConditions.map((ev, idx) => (
                        <li
                          key={idx}
                          className="text-xs text-slate-200 flex items-start gap-2"
                        >
                          {focusedYoga.status === 'NOT_FORMED' ||
                          focusedYoga.status === 'ABSENT' ? (
                            <XCircle className="w-3.5 h-3.5 text-slate-500 shrink-0 mt-0.5" />
                          ) : (
                            <CheckCircle2 className="w-3.5 h-3.5 text-emerald-400 shrink-0 mt-0.5" />
                          )}
                          <span>{ev}</span>
                        </li>
                      ))}
                    </ul>
                  </div>
                </div>
              </div>

              {/* Classical Phala (Interpretive Result) */}
              <div className="mt-4 pt-3.5 border-t border-slate-800">
                <div className="text-[11px] font-semibold text-amber-300 uppercase tracking-wider mb-1">
                  Classical Phala (Traditional Effect)
                </div>
                <p className="text-xs text-slate-300 leading-relaxed">
                  {focusedYoga.classicalEffect}
                </p>
              </div>
            </div>
          )}
        </div>
      )}
    </section>
  );
};

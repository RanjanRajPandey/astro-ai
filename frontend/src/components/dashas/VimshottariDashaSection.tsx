import React, { useEffect, useMemo, useState } from 'react';
import { CalendarClock, ChevronRight, Clock, Layers } from 'lucide-react';
import type {
  DashaCalculationResponse,
  DashaPeriodNode,
  GrahaName,
} from '../../types/astrology';
import { PLANET_COLORS } from '../../utils/chartMath';

interface VimshottariDashaSectionProps {
  dashaData: DashaCalculationResponse;
  onChangeTargetDate?: (isoUtc: string) => void;
}

const VIMSHOTTARI_ORDER: GrahaName[] = [
  'Ketu',
  'Venus',
  'Sun',
  'Moon',
  'Mars',
  'Rahu',
  'Jupiter',
  'Saturn',
  'Mercury',
];

const VIMSHOTTARI_YEARS: Record<GrahaName, number> = {
  Ketu: 7,
  Venus: 20,
  Sun: 6,
  Moon: 10,
  Mars: 7,
  Rahu: 18,
  Jupiter: 16,
  Saturn: 19,
  Mercury: 17,
};

function rotateOrder(startPlanet: GrahaName): GrahaName[] {
  const idx = VIMSHOTTARI_ORDER.indexOf(startPlanet);
  if (idx === -1) return VIMSHOTTARI_ORDER;
  return [...VIMSHOTTARI_ORDER.slice(idx), ...VIMSHOTTARI_ORDER.slice(0, idx)];
}

/**
 * Deterministic BPHS proportional subdivision for Level 4 (Sookshma) and Level 5 (Prana)
 * when the user clicks any arbitrary Level 3 Pratyantardasha in the 120-year tree.
 */
function subdivideClientSide(
  parentNode: DashaPeriodNode,
  childLevel: 4 | 5,
  birthUtcIso: string,
  targetUtcIso: string,
): DashaPeriodNode[] {
  const parentUnclampedMs = new Date(parentNode.unclampedStartDateTime).getTime();
  const parentEndMs = new Date(parentNode.endDateTime).getTime();
  const birthMs = new Date(birthUtcIso).getTime();
  const targetMs = new Date(targetUtcIso).getTime();
  const totalSpanMs = parentEndMs - parentUnclampedMs;

  const order = rotateOrder(parentNode.planet);
  const children: DashaPeriodNode[] = [];
  let cursorMs = parentUnclampedMs;

  order.forEach((planet, idx) => {
    const proportion = VIMSHOTTARI_YEARS[planet] / 120.0;
    const unclampedStartMs = cursorMs;
    const childEndMs =
      idx === order.length - 1 ? parentEndMs : Math.round(unclampedStartMs + totalSpanMs * proportion);
    cursorMs = childEndMs;

    if (childEndMs <= birthMs) {
      return;
    }

    const isBirthBalance = unclampedStartMs < birthMs;
    const clampedStartMs = isBirthBalance ? birthMs : unclampedStartMs;
    const durationDays = (childEndMs - clampedStartMs) / 86400000.0;
    const durationYears = durationDays / 365.2425;
    const isCurrentlyActive = clampedStartMs <= targetMs && targetMs < childEndMs;

    children.push({
      planet,
      level: childLevel,
      levelName: childLevel === 4 ? 'SOOKSHMA_DASHA' : 'PRANA_DASHA',
      startDateTime: new Date(clampedStartMs).toISOString().replace('.000Z', 'Z'),
      endDateTime: new Date(childEndMs).toISOString().replace('.000Z', 'Z'),
      unclampedStartDateTime: new Date(unclampedStartMs).toISOString().replace('.000Z', 'Z'),
      durationDays: Number(durationDays.toFixed(4)),
      durationYears: Number(durationYears.toFixed(6)),
      isCurrentlyActive,
      isBirthBalancePeriod: isBirthBalance,
      subPeriods: [],
    });
  });

  return children;
}

function formatDateShort(isoStr: string, includeTime = false): string {
  const d = new Date(isoStr);
  if (Number.isNaN(d.getTime())) return isoStr;
  const yyyy = d.getUTCFullYear();
  const mm = String(d.getUTCMonth() + 1).padStart(2, '0');
  const dd = String(d.getUTCDate()).padStart(2, '0');
  if (!includeTime) {
    return `${yyyy}-${mm}-${dd}`;
  }
  const hh = String(d.getUTCHours()).padStart(2, '0');
  const min = String(d.getUTCMinutes()).padStart(2, '0');
  return `${yyyy}-${mm}-${dd} ${hh}:${min} UTC`;
}

function formatDurationReadable(days: number): string {
  if (days >= 365.2425) {
    return `${(days / 365.2425).toFixed(2)} yrs`;
  }
  if (days >= 30) {
    return `${(days / 30.436875).toFixed(1)} mos`;
  }
  if (days >= 1) {
    return `${days.toFixed(1)} days`;
  }
  const hours = days * 24;
  if (hours >= 1) {
    return `${hours.toFixed(1)} hrs`;
  }
  return `${(hours * 60).toFixed(0)} mins`;
}

export const VimshottariDashaSection: React.FC<VimshottariDashaSectionProps> = ({
  dashaData,
  onChangeTargetDate,
}) => {
  const [selectedMahaIdx, setSelectedMahaIdx] = useState<number>(0);
  const [selectedAntarIdx, setSelectedAntarIdx] = useState<number>(0);
  const [selectedPratyantarIdx, setSelectedPratyantarIdx] = useState<number>(0);
  const [selectedSookshmaIdx, setSelectedSookshmaIdx] = useState<number>(0);
  const [targetDateInput, setTargetDateInput] = useState<string>(
    dashaData.targetUtcDatetimeIso.slice(0, 10),
  );

  // Sync initial selection to the currently active L1 -> L2 -> L3 -> L4 chain
  useEffect(() => {
    const mIdx = Math.max(
      0,
      dashaData.mahadashas.findIndex((m) => m.isCurrentlyActive),
    );
    setSelectedMahaIdx(mIdx);

    const maha = dashaData.mahadashas[mIdx];
    const aIdx = maha
      ? Math.max(
          0,
          maha.subPeriods.findIndex((a) => a.isCurrentlyActive),
        )
      : 0;
    setSelectedAntarIdx(aIdx);

    const antar = maha?.subPeriods[aIdx];
    const pIdx = antar
      ? Math.max(
          0,
          antar.subPeriods.findIndex((p) => p.isCurrentlyActive),
        )
      : 0;
    setSelectedPratyantarIdx(pIdx);

    const pratyantar = antar?.subPeriods[pIdx];
    if (pratyantar) {
      const sookshmas = subdivideClientSide(
        pratyantar,
        4,
        dashaData.birthUtcDatetimeIso,
        dashaData.targetUtcDatetimeIso,
      );
      const sIdx = Math.max(
        0,
        sookshmas.findIndex((s) => s.isCurrentlyActive),
      );
      setSelectedSookshmaIdx(sIdx);
    }
    setTargetDateInput(dashaData.targetUtcDatetimeIso.slice(0, 10));
  }, [dashaData]);

  const selectedMaha = dashaData.mahadashas[selectedMahaIdx] || dashaData.mahadashas[0];
  const antardashas = selectedMaha?.subPeriods || [];
  const selectedAntar = antardashas[selectedAntarIdx] || antardashas[0];
  const pratyantardashas = selectedAntar?.subPeriods || [];
  const selectedPratyantar = pratyantardashas[selectedPratyantarIdx] || pratyantardashas[0];

  const sookshmaDashas = useMemo(() => {
    if (!selectedPratyantar) return [];
    return subdivideClientSide(
      selectedPratyantar,
      4,
      dashaData.birthUtcDatetimeIso,
      dashaData.targetUtcDatetimeIso,
    );
  }, [selectedPratyantar, dashaData.birthUtcDatetimeIso, dashaData.targetUtcDatetimeIso]);

  const selectedSookshma = sookshmaDashas[selectedSookshmaIdx] || sookshmaDashas[0];

  const pranaDashas = useMemo(() => {
    if (!selectedSookshma) return [];
    return subdivideClientSide(
      selectedSookshma,
      5,
      dashaData.birthUtcDatetimeIso,
      dashaData.targetUtcDatetimeIso,
    );
  }, [selectedSookshma, dashaData.birthUtcDatetimeIso, dashaData.targetUtcDatetimeIso]);

  const handleApplyTargetDate = (e: React.FormEvent) => {
    e.preventDefault();
    if (onChangeTargetDate && targetDateInput) {
      onChangeTargetDate(`${targetDateInput}T12:00:00Z`);
    }
  };

  return (
    <section className="rounded-2xl bg-cosmic-900/80 border border-cosmic-700 p-5 shadow-xl space-y-5">
      {/* Header & Birth Balance Summary */}
      <div className="flex flex-wrap items-center justify-between gap-4 border-b border-cosmic-800 pb-4">
        <div>
          <h3 className="text-base font-bold text-white flex items-center gap-2">
            <Layers className="w-4 h-4 text-cosmic-gold" />
            <span>5-Level Vimshottari Dasha Engine (120-Year BPHS Cycle)</span>
          </h3>
          <p className="text-xs text-slate-400 mt-0.5">
            Mahadasha (L1) &rarr; Antardasha (L2) &rarr; Pratyantardasha (L3) &rarr; Sookshma Dasha
            (L4) &rarr; Prana Dasha (L5) • {dashaData.yearLengthDays}d Solar Year
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-3 text-xs">
          <div className="px-3 py-2 rounded-xl bg-cosmic-950 border border-cosmic-800">
            <span className="text-slate-400 block text-[10px]">Birth Dasha Balance</span>
            <span className="text-cosmic-gold font-bold">
              {dashaData.birthDashaLord} ({dashaData.birthBalanceFormatted} remaining)
            </span>
          </div>

          {onChangeTargetDate && (
            <form onSubmit={handleApplyTargetDate} className="flex items-center gap-1.5">
              <div className="relative">
                <CalendarClock className="w-3.5 h-3.5 text-slate-400 absolute left-2.5 top-2" />
                <input
                  type="date"
                  value={targetDateInput}
                  onChange={(e) => setTargetDateInput(e.target.value)}
                  aria-label="Target Dasha Date"
                  className="pl-8 pr-2.5 py-1.5 rounded-lg bg-cosmic-950 border border-cosmic-700 text-xs text-white focus:border-cosmic-gold focus:outline-none"
                />
              </div>
              <button
                type="submit"
                className="px-3 py-1.5 rounded-lg bg-cosmic-800 hover:bg-cosmic-700 text-cosmic-gold font-semibold text-xs border border-cosmic-700 transition-colors"
              >
                Inspect Date
              </button>
            </form>
          )}
        </div>
      </div>

      {/* Active 5-Level Dasha Stack Banner */}
      <div className="space-y-2">
        <div className="flex items-center justify-between text-xs">
          <span className="font-semibold text-slate-300 flex items-center gap-1.5">
            <Clock className="w-3.5 h-3.5 text-emerald-400" />
            <span>
              Active 5-Level Dasha Stack at{' '}
              <strong className="text-white font-mono">
                {formatDateShort(dashaData.targetUtcDatetimeIso, true)}
              </strong>
            </span>
          </span>
          <span className="text-cosmic-gold font-bold">
            {dashaData.activeStack.map((s) => s.planet).join(' → ')}
          </span>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-3">
          {dashaData.activeStack.map((item) => {
            const dotColor = PLANET_COLORS[item.planet] || '#f5b841';
            const showTime = item.level >= 4;
            return (
              <div
                key={item.level}
                className="p-3 rounded-xl bg-cosmic-950/90 border border-cosmic-800 flex flex-col justify-between space-y-2"
              >
                <div>
                  <div className="flex items-center justify-between text-[10px] text-slate-400">
                    <span className="uppercase tracking-wider font-semibold">
                      L{item.level} • {item.levelName.replace('_', ' ')}
                    </span>
                    <span className="font-mono text-emerald-400">{item.elapsedPercentage}%</span>
                  </div>
                  <div className="flex items-center gap-2 mt-1">
                    <span
                      className="w-2.5 h-2.5 rounded-full shrink-0"
                      style={{ backgroundColor: dotColor }}
                    />
                    <span className="text-sm font-bold text-white">{item.planet}</span>
                    <span className="text-[11px] text-slate-400 ml-auto">
                      {formatDurationReadable(item.durationDays)}
                    </span>
                  </div>
                </div>

                <div className="space-y-1">
                  <div className="w-full h-1.5 rounded-full bg-cosmic-800 overflow-hidden">
                    <div
                      className="h-full bg-emerald-400 rounded-full"
                      style={{
                        width: `${Math.min(100, Math.max(3, item.elapsedPercentage))}%`,
                      }}
                    />
                  </div>
                  <div className="text-[10px] text-slate-400 font-mono flex justify-between">
                    <span>{formatDateShort(item.startDateTime, showTime)}</span>
                    <span>&rarr;</span>
                    <span>{formatDateShort(item.endDateTime, showTime)}</span>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {/* Breadcrumb Trail for Selected 5-Level Drill-Down Path */}
      <div className="px-3.5 py-2 rounded-xl bg-cosmic-950 border border-cosmic-800 flex flex-wrap items-center gap-1.5 text-xs">
        <span className="text-slate-400 font-medium mr-1">Selected 5-Level Drill-Down Path:</span>
        <span className="px-2 py-0.5 rounded bg-cosmic-800 text-cosmic-gold font-bold">
          L1: {selectedMaha?.planet}
        </span>
        <ChevronRight className="w-3.5 h-3.5 text-slate-500" />
        <span className="px-2 py-0.5 rounded bg-cosmic-800 text-white font-semibold">
          L2: {selectedAntar?.planet}
        </span>
        <ChevronRight className="w-3.5 h-3.5 text-slate-500" />
        <span className="px-2 py-0.5 rounded bg-cosmic-800 text-white font-semibold">
          L3: {selectedPratyantar?.planet}
        </span>
        <ChevronRight className="w-3.5 h-3.5 text-slate-500" />
        <span className="px-2 py-0.5 rounded bg-cosmic-800 text-white font-semibold">
          L4: {selectedSookshma?.planet}
        </span>
        <ChevronRight className="w-3.5 h-3.5 text-slate-500" />
        <span className="px-2 py-0.5 rounded bg-emerald-500/15 text-emerald-300 border border-emerald-500/30 font-semibold">
          L5: All 9 Prana Dashas
        </span>
      </div>

      {/* 5-Column Interactive Drill-Down Explorer */}
      <div className="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-5 gap-3 text-xs">
        {/* Column 1: Level 1 Mahadasha */}
        <div className="rounded-xl bg-cosmic-950/80 border border-cosmic-800 overflow-hidden flex flex-col">
          <div className="px-3 py-2 bg-cosmic-950 border-b border-cosmic-800 font-bold text-cosmic-gold">
            1. Mahadasha (L1)
          </div>
          <div className="divide-y divide-cosmic-800/60 max-h-80 overflow-y-auto">
            {dashaData.mahadashas.map((m, idx) => {
              const isSelected = idx === selectedMahaIdx;
              return (
                <button
                  key={`${m.planet}-${idx}`}
                  type="button"
                  onClick={() => {
                    setSelectedMahaIdx(idx);
                    setSelectedAntarIdx(0);
                    setSelectedPratyantarIdx(0);
                    setSelectedSookshmaIdx(0);
                  }}
                  className={`w-full text-left px-3 py-2 transition-colors flex flex-col gap-0.5 ${
                    isSelected ? 'bg-cosmic-gold/15' : 'hover:bg-cosmic-800/50'
                  }`}
                >
                  <div className="flex items-center justify-between">
                    <span className="font-bold text-white flex items-center gap-1.5">
                      <span
                        className="w-2 h-2 rounded-full"
                        style={{ backgroundColor: PLANET_COLORS[m.planet] }}
                      />
                      {m.planet}
                    </span>
                    {m.isCurrentlyActive && (
                      <span className="px-1.5 py-0.2 rounded bg-emerald-500/20 text-emerald-300 text-[9px] font-bold">
                        ACTIVE
                      </span>
                    )}
                  </div>
                  <div className="text-[10px] text-slate-400 font-mono">
                    {formatDateShort(m.startDateTime)} &rarr; {formatDateShort(m.endDateTime)}
                  </div>
                </button>
              );
            })}
          </div>
        </div>

        {/* Column 2: Level 2 Antardasha */}
        <div className="rounded-xl bg-cosmic-950/80 border border-cosmic-800 overflow-hidden flex flex-col">
          <div className="px-3 py-2 bg-cosmic-950 border-b border-cosmic-800 font-bold text-white">
            2. Antardasha (L2 • {selectedMaha?.planet})
          </div>
          <div className="divide-y divide-cosmic-800/60 max-h-80 overflow-y-auto">
            {antardashas.map((a, idx) => {
              const isSelected = idx === selectedAntarIdx;
              return (
                <button
                  key={`${a.planet}-${idx}`}
                  type="button"
                  onClick={() => {
                    setSelectedAntarIdx(idx);
                    setSelectedPratyantarIdx(0);
                    setSelectedSookshmaIdx(0);
                  }}
                  className={`w-full text-left px-3 py-2 transition-colors flex flex-col gap-0.5 ${
                    isSelected ? 'bg-cosmic-gold/15' : 'hover:bg-cosmic-800/50'
                  }`}
                >
                  <div className="flex items-center justify-between">
                    <span className="font-semibold text-white">
                      {selectedMaha?.planet} - {a.planet}
                    </span>
                    {a.isCurrentlyActive && (
                      <span className="px-1.5 py-0.2 rounded bg-emerald-500/20 text-emerald-300 text-[9px] font-bold">
                        NOW
                      </span>
                    )}
                  </div>
                  <div className="text-[10px] text-slate-400 font-mono">
                    {formatDateShort(a.startDateTime)} &rarr; {formatDateShort(a.endDateTime)}
                  </div>
                </button>
              );
            })}
          </div>
        </div>

        {/* Column 3: Level 3 Pratyantardasha */}
        <div className="rounded-xl bg-cosmic-950/80 border border-cosmic-800 overflow-hidden flex flex-col">
          <div className="px-3 py-2 bg-cosmic-950 border-b border-cosmic-800 font-bold text-white">
            3. Pratyantardasha (L3 • {selectedAntar?.planet})
          </div>
          <div className="divide-y divide-cosmic-800/60 max-h-80 overflow-y-auto">
            {pratyantardashas.map((p, idx) => {
              const isSelected = idx === selectedPratyantarIdx;
              return (
                <button
                  key={`${p.planet}-${idx}`}
                  type="button"
                  onClick={() => {
                    setSelectedPratyantarIdx(idx);
                    setSelectedSookshmaIdx(0);
                  }}
                  className={`w-full text-left px-3 py-2 transition-colors flex flex-col gap-0.5 ${
                    isSelected ? 'bg-cosmic-gold/15' : 'hover:bg-cosmic-800/50'
                  }`}
                >
                  <div className="flex items-center justify-between">
                    <span className="font-semibold text-white">
                      {selectedAntar?.planet} - {p.planet}
                    </span>
                    {p.isCurrentlyActive && (
                      <span className="px-1.5 py-0.2 rounded bg-emerald-500/20 text-emerald-300 text-[9px] font-bold">
                        NOW
                      </span>
                    )}
                  </div>
                  <div className="text-[10px] text-slate-400 font-mono">
                    {formatDateShort(p.startDateTime)} &rarr; {formatDateShort(p.endDateTime)}
                  </div>
                </button>
              );
            })}
          </div>
        </div>

        {/* Column 4: Level 4 Sookshma Dasha */}
        <div className="rounded-xl bg-cosmic-950/80 border border-cosmic-800 overflow-hidden flex flex-col">
          <div className="px-3 py-2 bg-cosmic-950 border-b border-cosmic-800 font-bold text-white">
            4. Sookshma (L4 • {selectedPratyantar?.planet})
          </div>
          <div className="divide-y divide-cosmic-800/60 max-h-80 overflow-y-auto">
            {sookshmaDashas.map((s, idx) => {
              const isSelected = idx === selectedSookshmaIdx;
              return (
                <button
                  key={`${s.planet}-${idx}`}
                  type="button"
                  onClick={() => setSelectedSookshmaIdx(idx)}
                  className={`w-full text-left px-3 py-2 transition-colors flex flex-col gap-0.5 ${
                    isSelected ? 'bg-cosmic-gold/15' : 'hover:bg-cosmic-800/50'
                  }`}
                >
                  <div className="flex items-center justify-between">
                    <span className="font-semibold text-white">
                      {selectedPratyantar?.planet} - {s.planet}
                    </span>
                    {s.isCurrentlyActive && (
                      <span className="px-1.5 py-0.2 rounded bg-emerald-500/20 text-emerald-300 text-[9px] font-bold">
                        NOW
                      </span>
                    )}
                  </div>
                  <div className="text-[10px] text-slate-400 font-mono">
                    {formatDateShort(s.startDateTime, true)}
                  </div>
                </button>
              );
            })}
          </div>
        </div>

        {/* Column 5: Level 5 Prana Dasha */}
        <div className="rounded-xl bg-cosmic-950/80 border border-cosmic-800 overflow-hidden flex flex-col">
          <div className="px-3 py-2 bg-cosmic-950 border-b border-cosmic-800 font-bold text-emerald-300">
            5. Prana Dasha (L5 • {selectedSookshma?.planet})
          </div>
          <div className="divide-y divide-cosmic-800/60 max-h-80 overflow-y-auto">
            {pranaDashas.map((pr, idx) => (
              <div
                key={`${pr.planet}-${idx}`}
                className={`px-3 py-2 flex flex-col gap-0.5 ${
                  pr.isCurrentlyActive ? 'bg-emerald-500/15' : ''
                }`}
              >
                <div className="flex items-center justify-between">
                  <span className="font-semibold text-white">
                    {selectedSookshma?.planet} - {pr.planet}
                  </span>
                  <span className="text-[10px] text-slate-400">
                    {formatDurationReadable(pr.durationDays)}
                  </span>
                </div>
                <div className="text-[10px] text-slate-400 font-mono">
                  {formatDateShort(pr.startDateTime, true)} &rarr;{' '}
                  {formatDateShort(pr.endDateTime, true)}
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </section>
  );
};

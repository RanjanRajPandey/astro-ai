import React from 'react';
import { Building2, Compass, Crown } from 'lucide-react';
import type { BhavaBalaCalculationResponse, HouseStrengthEntry } from '../../types/astrology';
import { PLANET_COLORS } from '../../utils/chartMath';

interface BhavaBalaSectionProps {
  bhavaBalaData: BhavaBalaCalculationResponse | null;
  loading?: boolean;
}

function getHouseGradeBadge(grade: HouseStrengthEntry['strengthGrade']) {
  switch (grade) {
    case 'VERY_STRONG':
      return 'bg-emerald-500/20 text-emerald-300 border-emerald-500/40';
    case 'STRONG':
      return 'bg-sky-500/20 text-sky-300 border-sky-500/40';
    case 'MODERATE':
      return 'bg-amber-500/20 text-amber-300 border-amber-500/40';
    default:
      return 'bg-rose-500/20 text-rose-300 border-rose-500/40';
  }
}

const PURUSHARTHA_STYLES: Record<string, { badge: string; label: string }> = {
  DHARMA: {
    badge: 'bg-amber-500/15 text-amber-300 border-amber-500/30',
    label: 'Purpose, Righteousness &Higher Destiny (H1, H5, H9)',
  },
  ARTHA: {
    badge: 'bg-emerald-500/15 text-emerald-300 border-emerald-500/30',
    label: 'Wealth, Career & Material Sustenance (H2, H6, H10)',
  },
  KAMA: {
    badge: 'bg-pink-500/15 text-pink-300 border-pink-500/30',
    label: 'Relationships, Desires &Fulfillment (H3, H7, H11)',
  },
  MOKSHA: {
    badge: 'bg-purple-500/15 text-purple-300 border-purple-500/30',
    label: 'Inner Peace, Transformation & Liberation (H4, H8, H12)',
  },
};

export const BhavaBalaSection: React.FC<BhavaBalaSectionProps> = ({
  bhavaBalaData,
  loading = false,
}) => {
  if (loading && !bhavaBalaData) {
    return (
      <section className="rounded-2xl border border-slate-800/90 bg-slate-900/60 p-6 shadow-xl">
        <div className="animate-pulse space-y-4">
          <div className="h-6 w-72 rounded bg-slate-800" />
          <div className="h-64 rounded-xl bg-slate-800/50" />
        </div>
      </section>
    );
  }

  if (!bhavaBalaData) return null;

  const maxRupas = Math.max(
    10.0,
    ...bhavaBalaData.houses.map((h) => h.totalBhavaBalaRupas),
  );

  return (
    <section className="rounded-2xl border border-slate-800/90 bg-slate-900/60 p-5 sm:p-6 shadow-xl space-y-6">
      {/* Header */}
      <div className="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-4 border-b border-slate-800/80 pb-4">
        <div className="flex items-center gap-2.5">
          <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-purple-500/10 border border-purple-500/30 text-purple-400">
            <Building2 className="h-5 w-5" />
          </div>
          <div>
            <h2 className="text-lg font-bold tracking-tight text-slate-100 flex items-center gap-2">
              12-House Strength Engine (Bhava Bala &amp; Purushartha Synthesis)
              <span className="rounded-full bg-purple-500/15 border border-purple-500/30 px-2.5 py-0.5 text-xs font-semibold text-purple-300">
                BPHS Ch. 27
              </span>
            </h2>
            <p className="text-xs text-slate-400">
              Bhavadhipati Bala (Lord Shadbala) + Bhava Dig Bala (Nara/Jalachara/Keeta/Chatushpada) + Bhava Drishti Bala + Occupant Factor
            </p>
          </div>
        </div>

        <div className="flex flex-wrap items-center gap-2.5 text-xs">
          <div className="inline-flex items-center gap-1.5 rounded-xl bg-emerald-500/10 border border-emerald-500/30 px-3 py-1.5">
            <Crown className="h-4 w-4 text-emerald-400" />
            <span className="text-slate-300">Peak Bhava:</span>
            <strong className="text-emerald-300">House {bhavaBalaData.strongestHouse}</strong>
          </div>
          <div className="inline-flex items-center gap-1.5 rounded-xl bg-slate-950/90 border border-slate-800 px-3 py-1.5">
            <Compass className="h-4 w-4 text-amber-400" />
            <span className="text-slate-400">Mean Strength:</span>
            <strong className="text-amber-300">{bhavaBalaData.averageRupas.toFixed(2)} Rupas</strong>
          </div>
        </div>
      </div>

      {/* 4 Purushartha Trikona Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3">
        {bhavaBalaData.purusharthaSummaries.map((ps) => {
          const style = PURUSHARTHA_STYLES[ps.purushartha] || PURUSHARTHA_STYLES.DHARMA;
          return (
            <div
              key={ps.purushartha}
              className="rounded-xl border border-slate-800 bg-slate-950/75 p-3.5 space-y-2"
            >
              <div className="flex items-center justify-between">
                <span
                  className={`rounded-full border px-2.5 py-0.5 text-[11px] font-bold uppercase ${style.badge}`}
                >
                  {ps.purushartha} Trikona
                </span>
                <span className="text-xs font-mono font-bold text-slate-100">
                  {ps.averageRupas.toFixed(2)} Rupas avg
                </span>
              </div>
              <p className="text-[11px] text-slate-400 leading-snug">{style.label}</p>
              <div className="flex items-center justify-between text-[11px] text-slate-400 pt-1 border-t border-slate-800/70">
                <span>Houses: H{ps.houses.join(', H')}</span>
                <span>
                  Dominant: <strong className="text-amber-300">H{ps.dominantHouse}</strong>
                </span>
              </div>
            </div>
          );
        })}
      </div>

      {/* Visual 12-House Bar Chart */}
      <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6 gap-2.5">
        {bhavaBalaData.houses.map((h) => {
          const pct = Math.min(100, Math.round((h.totalBhavaBalaRupas / maxRupas) * 100));
          return (
            <div
              key={h.houseNumber}
              className="rounded-xl border border-slate-800/90 bg-slate-950/70 p-3 space-y-2"
            >
              <div className="flex items-center justify-between">
                <span className="text-xs font-bold text-amber-400">
                  H{h.houseNumber} · {h.sign}
                </span>
                <span className="text-[10px] font-mono text-slate-400">#{h.rank}</span>
              </div>
              <div>
                <div className="text-sm font-bold text-slate-100">
                  {h.totalBhavaBalaRupas.toFixed(2)}{' '}
                  <span className="text-[10px] font-normal text-slate-400">Rupas</span>
                </div>
                <div className="text-[10px] text-slate-400 truncate">
                  Lord:{' '}
                  <strong style={{ color: PLANET_COLORS[h.lordPlanet] }}>
                    {h.lordPlanet}
                  </strong>
                </div>
              </div>
              <div className="h-1.5 w-full rounded-full bg-slate-800 overflow-hidden">
                <div
                  className="h-full rounded-full bg-gradient-to-r from-amber-500 to-purple-400"
                  style={{ width: `${pct}%` }}
                />
              </div>
            </div>
          );
        })}
      </div>

      {/* Detailed 12-House Bhava Bala Breakdown Table */}
      <div className="overflow-x-auto rounded-xl border border-slate-800 bg-slate-950/60">
        <table className="w-full border-collapse text-left text-xs">
          <thead>
            <tr className="border-b border-slate-800 bg-slate-900/90 text-slate-400">
              <th className="p-3 font-semibold">House &amp; Domain</th>
              <th className="p-3 font-semibold">Sign &amp; Lord</th>
              <th className="p-3 font-semibold">Purushartha</th>
              <th className="p-3 font-semibold text-right">Bhavadhipati (Lord)</th>
              <th className="p-3 font-semibold text-right">Bhava Dig (Directional)</th>
              <th className="p-3 font-semibold text-right">Bhava Drishti (Aspects)</th>
              <th className="p-3 font-semibold text-right">Occupant Bonus</th>
              <th className="p-3 font-semibold text-right">Total Virupas</th>
              <th className="p-3 font-semibold text-right">Total Rupas</th>
              <th className="p-3 font-semibold text-center">Rank &amp; Grade</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-800/70">
            {bhavaBalaData.houses.map((h) => (
              <tr key={h.houseNumber} className="hover:bg-slate-900/40">
                <td className="p-3">
                  <div className="font-bold text-slate-100">House {h.houseNumber}</div>
                  <div className="text-[11px] text-slate-400">{h.domainTitle}</div>
                </td>
                <td className="p-3 whitespace-nowrap">
                  <div className="font-semibold text-slate-200">
                    {h.sign} ({h.sanskritSign})
                  </div>
                  <div className="text-[11px] text-slate-400">
                    Lord:{' '}
                    <strong style={{ color: PLANET_COLORS[h.lordPlanet] }}>
                      {h.lordPlanet}
                    </strong>
                  </div>
                </td>
                <td className="p-3 whitespace-nowrap">
                  <span
                    className={`rounded-md border px-2 py-0.5 text-[10px] font-bold ${
                      PURUSHARTHA_STYLES[h.purushartha]?.badge || ''
                    }`}
                  >
                    {h.purushartha}
                  </span>
                </td>
                <td className="p-3 text-right font-mono text-slate-200">
                  {h.bhavadhipatiBala.toFixed(1)} V
                </td>
                <td className="p-3 text-right font-mono text-slate-200">
                  {h.bhavaDigBala.toFixed(1)} V
                </td>
                <td
                  className={`p-3 text-right font-mono font-semibold ${
                    h.bhavaDrishtiBala >= 0 ? 'text-emerald-400' : 'text-rose-400'
                  }`}
                >
                  {h.bhavaDrishtiBala >= 0
                    ? `+${h.bhavaDrishtiBala.toFixed(1)}`
                    : h.bhavaDrishtiBala.toFixed(1)}{' '}
                  V
                </td>
                <td
                  className={`p-3 text-right font-mono font-semibold ${
                    h.occupantFactor >= 0 ? 'text-emerald-400' : 'text-rose-400'
                  }`}
                >
                  {h.occupantFactor >= 0
                    ? `+${h.occupantFactor.toFixed(1)}`
                    : h.occupantFactor.toFixed(1)}{' '}
                  V
                </td>
                <td className="p-3 text-right font-mono font-bold text-amber-300">
                  {h.totalBhavaBalaVirupas.toFixed(1)} V
                </td>
                <td className="p-3 text-right font-mono font-bold text-white">
                  {h.totalBhavaBalaRupas.toFixed(2)} R
                </td>
                <td className="p-3 text-center whitespace-nowrap">
                  <span className="font-mono text-xs font-bold text-amber-400 mr-2">
                    #{h.rank}
                  </span>
                  <span
                    className={`inline-block rounded-full border px-2.5 py-0.5 text-[10px] font-bold uppercase ${getHouseGradeBadge(
                      h.strengthGrade,
                    )}`}
                  >
                    {h.strengthGrade.replace('_', ' ')}
                  </span>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  );
};

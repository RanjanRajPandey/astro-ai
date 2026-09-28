import React, { useState } from 'react';
import { Award, BarChart3, ShieldCheck, TrendingDown, TrendingUp } from 'lucide-react';
import type { PlanetStrengthEntry, ShadbalaCalculationResponse } from '../../types/astrology';
import { PLANET_COLORS } from '../../utils/chartMath';

interface ShadbalaSectionProps {
  shadbalaData: ShadbalaCalculationResponse | null;
  loading?: boolean;
}

function getGradeBadge(grade: PlanetStrengthEntry['strengthGrade']) {
  switch (grade) {
    case 'VERY_STRONG':
      return 'bg-emerald-500/20 text-emerald-300 border-emerald-500/40';
    case 'ADEQUATE':
      return 'bg-sky-500/20 text-sky-300 border-sky-500/40';
    case 'MODERATE':
      return 'bg-amber-500/20 text-amber-300 border-amber-500/40';
    default:
      return 'bg-rose-500/20 text-rose-300 border-rose-500/40';
  }
}

export const ShadbalaSection: React.FC<ShadbalaSectionProps> = ({
  shadbalaData,
  loading = false,
}) => {
  const [selectedPlanetName, setSelectedPlanetName] = useState<string>('Sun');

  if (loading && !shadbalaData) {
    return (
      <section className="rounded-2xl border border-slate-800/90 bg-slate-900/60 p-6 shadow-xl">
        <div className="animate-pulse space-y-4">
          <div className="h-6 w-72 rounded bg-slate-800" />
          <div className="h-64 rounded-xl bg-slate-800/50" />
        </div>
      </section>
    );
  }

  if (!shadbalaData) return null;

  const selectedEntry =
    shadbalaData.planets.find((p) => p.planet === selectedPlanetName) ||
    shadbalaData.planets[0];

  return (
    <section className="rounded-2xl border border-slate-800/90 bg-slate-900/60 p-5 sm:p-6 shadow-xl space-y-6">
      {/* Header */}
      <div className="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-4 border-b border-slate-800/80 pb-4">
        <div className="flex items-center gap-2.5">
          <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-amber-500/10 border border-amber-500/30 text-amber-400">
            <BarChart3 className="h-5 w-5" />
          </div>
          <div>
            <h2 className="text-lg font-bold tracking-tight text-slate-100 flex items-center gap-2">
              Six-Fold Planetary Strength (Shadbala &amp; 16-Varga Vimshopaka Bala)
              <span className="rounded-full bg-amber-500/15 border border-amber-500/30 px-2.5 py-0.5 text-xs font-semibold text-amber-300">
                BPHS Ch. 27
              </span>
            </h2>
            <p className="text-xs text-slate-400">
              Sthana, Dig, Kala, Chesta, Naisargika &amp; Drik Bala in Virupas/Rupas (60 Virupas = 1 Rupa) + 20-Point Shodashavarga Vimshopaka Dignity
            </p>
          </div>
        </div>

        <div className="flex flex-wrap items-center gap-3">
          <div className="inline-flex items-center gap-1.5 rounded-xl bg-emerald-500/10 border border-emerald-500/30 px-3 py-1.5 text-xs">
            <TrendingUp className="h-4 w-4 text-emerald-400" />
            <span className="text-slate-300">Strongest Graha:</span>
            <strong className="text-emerald-300">{shadbalaData.strongestPlanet}</strong>
          </div>
          <div className="inline-flex items-center gap-1.5 rounded-xl bg-rose-500/10 border border-rose-500/30 px-3 py-1.5 text-xs">
            <TrendingDown className="h-4 w-4 text-rose-400" />
            <span className="text-slate-300">Needs Support:</span>
            <strong className="text-rose-300">{shadbalaData.weakestPlanet}</strong>
          </div>
        </div>
      </div>

      {/* Visual Shadbala Ratio & Vimshopaka Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-3 lg:grid-cols-9 gap-2.5">
        {shadbalaData.planets.map((p) => {
          const isSelected = p.planet === selectedEntry.planet;
          const pct = Math.min(100, Math.round((p.shadbalaRatio / 1.5) * 100));
          return (
            <button
              key={p.planet}
              type="button"
              onClick={() => setSelectedPlanetName(p.planet)}
              className={`rounded-xl p-3 text-left border transition flex flex-col justify-between ${
                isSelected
                  ? 'bg-amber-500/15 border-amber-500 shadow-lg'
                  : 'bg-slate-950/70 border-slate-800/90 hover:border-slate-700'
              }`}
            >
              <div className="flex items-center justify-between gap-1">
                <span
                  className="font-bold text-xs"
                  style={{ color: PLANET_COLORS[p.planet] || '#f59e0b' }}
                >
                  {p.planet}
                </span>
                <span className="text-[10px] font-mono text-slate-400">#{p.rank}</span>
              </div>

              <div className="my-2">
                <div className="text-sm font-bold text-slate-100">
                  {p.totalShadbalaRupas.toFixed(2)} <span className="text-[10px] font-normal text-slate-400">Rupas</span>
                </div>
                <div className="text-[10px] text-slate-400">
                  Min: {p.requiredMinimumRupas.toFixed(1)}R ({p.shadbalaRatio.toFixed(2)}x)
                </div>
              </div>

              <div className="w-full h-1.5 rounded-full bg-slate-800 overflow-hidden">
                <div
                  className={`h-full rounded-full ${
                    p.shadbalaRatio >= 1.0 ? 'bg-emerald-400' : 'bg-amber-400'
                  }`}
                  style={{ width: `${pct}%` }}
                />
              </div>
            </button>
          );
        })}
      </div>

      {/* Main Six-Fold Shadbala Table */}
      <div className="overflow-x-auto rounded-xl border border-slate-800 bg-slate-950/60">
        <table className="w-full border-collapse text-left text-xs">
          <thead>
            <tr className="border-b border-slate-800 bg-slate-900/90 text-slate-400">
              <th className="p-3 font-semibold">Rank</th>
              <th className="p-3 font-semibold">Graha (House · Sign)</th>
              <th className="p-3 font-semibold text-right">Sthana (Positional)</th>
              <th className="p-3 font-semibold text-right">Dig (Directional)</th>
              <th className="p-3 font-semibold text-right">Kala (Temporal)</th>
              <th className="p-3 font-semibold text-right">Chesta (Motional)</th>
              <th className="p-3 font-semibold text-right">Naisargika (Natural)</th>
              <th className="p-3 font-semibold text-right">Drik (Aspectual)</th>
              <th className="p-3 font-semibold text-right">Total Virupas</th>
              <th className="p-3 font-semibold text-right">Rupas / Min</th>
              <th className="p-3 font-semibold text-right">Ratio</th>
              <th className="p-3 font-semibold text-right">Vimshopaka (20)</th>
              <th className="p-3 font-semibold text-center">Grade</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-800/70">
            {shadbalaData.planets.map((p) => {
              const isSelected = p.planet === selectedEntry.planet;
              return (
                <tr
                  key={p.planet}
                  onClick={() => setSelectedPlanetName(p.planet)}
                  className={`cursor-pointer transition ${
                    isSelected ? 'bg-amber-500/10' : 'hover:bg-slate-900/50'
                  }`}
                >
                  <td className="p-3 font-mono font-bold text-amber-400">#{p.rank}</td>
                  <td className="p-3 whitespace-nowrap">
                    <span
                      className="font-bold"
                      style={{ color: PLANET_COLORS[p.planet] || '#f59e0b' }}
                    >
                      {p.planet}
                    </span>
                    <span className="ml-1.5 text-slate-400">
                      (H{p.house} · {p.sign})
                    </span>
                  </td>
                  <td className="p-3 text-right font-mono text-slate-200">
                    {p.sthanaBala.toFixed(1)}
                  </td>
                  <td className="p-3 text-right font-mono text-slate-200">
                    {p.digBala.toFixed(1)}
                  </td>
                  <td className="p-3 text-right font-mono text-slate-200">
                    {p.kalaBala.toFixed(1)}
                  </td>
                  <td className="p-3 text-right font-mono text-slate-200">
                    {p.chestaBala.toFixed(1)}
                  </td>
                  <td className="p-3 text-right font-mono text-slate-200">
                    {p.naisargikaBala.toFixed(1)}
                  </td>
                  <td
                    className={`p-3 text-right font-mono font-semibold ${
                      p.drikBala >= 0 ? 'text-emerald-400' : 'text-rose-400'
                    }`}
                  >
                    {p.drikBala >= 0 ? `+${p.drikBala.toFixed(1)}` : p.drikBala.toFixed(1)}
                  </td>
                  <td className="p-3 text-right font-mono font-bold text-amber-300">
                    {p.totalShadbalaVirupas.toFixed(1)} V
                  </td>
                  <td className="p-3 text-right font-mono whitespace-nowrap">
                    <strong className="text-white">{p.totalShadbalaRupas.toFixed(2)}</strong>
                    <span className="text-slate-500"> / {p.requiredMinimumRupas.toFixed(1)} R</span>
                  </td>
                  <td className="p-3 text-right font-mono font-bold">
                    <span className={p.shadbalaRatio >= 1.0 ? 'text-emerald-400' : 'text-amber-400'}>
                      {p.shadbalaRatio.toFixed(2)}x
                    </span>
                  </td>
                  <td className="p-3 text-right font-mono whitespace-nowrap">
                    <strong className="text-purple-300">{p.vimshopakaBala.toFixed(2)}</strong>
                    <span className="text-slate-500"> / 20 ({p.vimshopakaPercentage.toFixed(0)}%)</span>
                  </td>
                  <td className="p-3 text-center whitespace-nowrap">
                    <span
                      className={`inline-block rounded-full border px-2.5 py-0.5 text-[10px] font-bold uppercase ${getGradeBadge(
                        p.strengthGrade,
                      )}`}
                    >
                      {p.strengthGrade.replace('_', ' ')}
                    </span>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>

      {/* Selected Planet Sub-Bala Breakdown Drawer */}
      {selectedEntry && (
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-4 pt-1">
          {/* Sthana Bala 5-Part Breakdown */}
          <div className="rounded-xl border border-slate-800 bg-slate-950/80 p-4 space-y-3">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                <Award className="h-4 w-4 text-amber-400" />
                <h3 className="text-xs font-bold uppercase tracking-wider text-slate-200">
                  {selectedEntry.planet} — Sthana Bala Breakdown ({selectedEntry.sthanaBala.toFixed(2)} Virupas)
                </h3>
              </div>
              <span className="text-[11px] text-slate-400">5 Positional Sub-Balas</span>
            </div>
            <div className="grid grid-cols-2 sm:grid-cols-5 gap-2 text-xs">
              <div className="rounded-lg bg-slate-900/90 border border-slate-800 p-2.5">
                <span className="text-[10px] text-slate-400 block">Uchcha (Exaltation)</span>
                <strong className="text-amber-300 font-mono">
                  {selectedEntry.sthanaBreakdown.uchchaBala.toFixed(1)} V
                </strong>
              </div>
              <div className="rounded-lg bg-slate-900/90 border border-slate-800 p-2.5">
                <span className="text-[10px] text-slate-400 block">Saptavargaja (7-Varga)</span>
                <strong className="text-amber-300 font-mono">
                  {selectedEntry.sthanaBreakdown.saptavargajaBala.toFixed(1)} V
                </strong>
              </div>
              <div className="rounded-lg bg-slate-900/90 border border-slate-800 p-2.5">
                <span className="text-[10px] text-slate-400 block">Ojhayugma (Parity)</span>
                <strong className="text-amber-300 font-mono">
                  {selectedEntry.sthanaBreakdown.ojhayugmarasyamsaBala.toFixed(1)} V
                </strong>
              </div>
              <div className="rounded-lg bg-slate-900/90 border border-slate-800 p-2.5">
                <span className="text-[10px] text-slate-400 block">Kendradi (House)</span>
                <strong className="text-amber-300 font-mono">
                  {selectedEntry.sthanaBreakdown.kendradiBala.toFixed(1)} V
                </strong>
              </div>
              <div className="rounded-lg bg-slate-900/90 border border-slate-800 p-2.5">
                <span className="text-[10px] text-slate-400 block">Drekkana (Decanate)</span>
                <strong className="text-amber-300 font-mono">
                  {selectedEntry.sthanaBreakdown.drekkanaBala.toFixed(1)} V
                </strong>
              </div>
            </div>
          </div>

          {/* Kala Bala 5-Part Breakdown */}
          <div className="rounded-xl border border-slate-800 bg-slate-950/80 p-4 space-y-3">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                <ShieldCheck className="h-4 w-4 text-sky-400" />
                <h3 className="text-xs font-bold uppercase tracking-wider text-slate-200">
                  {selectedEntry.planet} — Kala Bala Breakdown ({selectedEntry.kalaBala.toFixed(2)} Virupas)
                </h3>
              </div>
              <span className="text-[11px] text-slate-400">5 Temporal Sub-Balas</span>
            </div>
            <div className="grid grid-cols-2 sm:grid-cols-5 gap-2 text-xs">
              <div className="rounded-lg bg-slate-900/90 border border-slate-800 p-2.5">
                <span className="text-[10px] text-slate-400 block">Nathonnatha (Day/Night)</span>
                <strong className="text-sky-300 font-mono">
                  {selectedEntry.kalaBreakdown.nathonnathaBala.toFixed(1)} V
                </strong>
              </div>
              <div className="rounded-lg bg-slate-900/90 border border-slate-800 p-2.5">
                <span className="text-[10px] text-slate-400 block">Paksha (Lunar Phase)</span>
                <strong className="text-sky-300 font-mono">
                  {selectedEntry.kalaBreakdown.pakshaBala.toFixed(1)} V
                </strong>
              </div>
              <div className="rounded-lg bg-slate-900/90 border border-slate-800 p-2.5">
                <span className="text-[10px] text-slate-400 block">Tribhaga (Watch)</span>
                <strong className="text-sky-300 font-mono">
                  {selectedEntry.kalaBreakdown.tribhagaBala.toFixed(1)} V
                </strong>
              </div>
              <div className="rounded-lg bg-slate-900/90 border border-slate-800 p-2.5">
                <span className="text-[10px] text-slate-400 block">Vara (Weekday Lord)</span>
                <strong className="text-sky-300 font-mono">
                  {selectedEntry.kalaBreakdown.varaBala.toFixed(1)} V
                </strong>
              </div>
              <div className="rounded-lg bg-slate-900/90 border border-slate-800 p-2.5">
                <span className="text-[10px] text-slate-400 block">Ayana (Declination)</span>
                <strong className="text-sky-300 font-mono">
                  {selectedEntry.kalaBreakdown.ayanaBala.toFixed(1)} V
                </strong>
              </div>
            </div>
          </div>
        </div>
      )}
    </section>
  );
};

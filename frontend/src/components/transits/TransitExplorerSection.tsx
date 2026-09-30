import React, { useState } from 'react';
import {
  Orbit,
  Calendar,
  CheckCircle2,
  AlertTriangle,
  ShieldAlert,
  Sparkles,
  Zap,
} from 'lucide-react';
import type { TransitCalculationResponse } from '../../types/astrology';
import { PLANET_COLORS } from '../../utils/chartMath';

interface TransitExplorerSectionProps {
  transitData: TransitCalculationResponse | null;
  onChangeTransitDate: (isoUtc: string) => void;
}

export const TransitExplorerSection: React.FC<TransitExplorerSectionProps> = ({
  transitData,
  onChangeTransitDate,
}) => {
  const [dateInput, setDateInput] = useState<string>(() => {
    if (transitData?.transitUtcDatetimeIso) {
      return transitData.transitUtcDatetimeIso.slice(0, 10);
    }
    return new Date().toISOString().slice(0, 10);
  });

  if (!transitData) return null;

  const activatedHouses = transitData.doubleTransitHouses.filter((h) => h.isActivated);
  const { sadeSati } = transitData;

  const handleApplyDate = (e: React.FormEvent) => {
    e.preventDefault();
    if (!dateInput) return;
    onChangeTransitDate(`${dateInput}T12:00:00Z`);
  };

  return (
    <section className="bg-slate-900/70 border border-slate-800/80 rounded-2xl p-5 sm:p-6 shadow-xl space-y-6">
      {/* Header & Transit Date Picker */}
      <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-4 pb-5 border-b border-slate-800/80">
        <div>
          <div className="flex items-center gap-2.5">
            <Orbit className="w-5 h-5 text-amber-400" />
            <h2 className="text-lg font-semibold text-white tracking-tight">
              Planetary Transits (Gochar), Vedha &amp; Double Transit Engine
            </h2>
            <span className="px-2.5 py-0.5 text-xs font-medium rounded-full bg-amber-500/10 text-amber-300 border border-amber-500/30">
              Chandra &amp; Udaya Lagna Gochar
            </span>
          </div>
          <p className="text-xs text-slate-400 mt-1">
            Real-time sidereal transits evaluated from Natal Moon ({transitData.natalMoonSign} •{' '}
            {transitData.natalMoonNakshatra}) &amp; Ascendant ({transitData.natalAscendantSign}),
            with Vedha obstruction, 9-fold Tara Bala, Sade Sati, and Guru–Shani Double Transit.
          </p>
        </div>

        {/* Interactive Transit Date Selector */}
        <form onSubmit={handleApplyDate} className="flex flex-wrap items-center gap-2">
          <div className="flex items-center gap-1.5 bg-slate-950 border border-slate-800 rounded-xl px-3 py-1.5">
            <Calendar className="w-3.5 h-3.5 text-amber-400" />
            <input
              type="date"
              value={dateInput}
              onChange={(e) => setDateInput(e.target.value)}
              aria-label="Select Transit Date"
              className="bg-transparent text-xs text-white focus:outline-none"
            />
          </div>
          <button
            type="submit"
            className="px-3.5 py-1.5 rounded-xl bg-amber-500/20 hover:bg-amber-500/30 text-amber-300 border border-amber-500/40 text-xs font-semibold transition"
          >
            Calculate Gochar
          </button>
        </form>
      </div>

      {/* Top Row: Sade Sati / Dhaiya Banner + Gochar KPI Summary */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-4">
        {/* Sade Sati & Dhaiya Status Card */}
        <div
          className={`lg:col-span-7 rounded-2xl p-4 border flex items-start gap-3.5 ${
            sadeSati.sadeSatiActive
              ? 'bg-rose-950/30 border-rose-700/50'
              : sadeSati.dhaiyaActive
                ? 'bg-amber-950/30 border-amber-700/50'
                : 'bg-emerald-950/25 border-emerald-700/40'
          }`}
        >
          <div className="p-2.5 rounded-xl bg-slate-950/80 border border-slate-800 shrink-0">
            {sadeSati.sadeSatiActive || sadeSati.dhaiyaActive ? (
              <ShieldAlert className="w-5 h-5 text-amber-400" />
            ) : (
              <CheckCircle2 className="w-5 h-5 text-emerald-400" />
            )}
          </div>
          <div>
            <div className="flex flex-wrap items-center gap-2">
              <span className="text-xs font-bold uppercase tracking-wider text-white">
                Shani Sade Sati &amp; Dhaiya Status
              </span>
              <span
                className={`px-2 py-0.5 text-[10px] font-semibold rounded border ${
                  sadeSati.sadeSatiActive
                    ? 'bg-rose-950 text-rose-300 border-rose-700'
                    : sadeSati.dhaiyaActive
                      ? 'bg-amber-950 text-amber-300 border-amber-700'
                      : 'bg-emerald-950 text-emerald-300 border-emerald-700'
                }`}
              >
                {sadeSati.phase.replace(/_/g, ' ')}
              </span>
            </div>
            <p className="text-xs text-slate-300 mt-1.5 leading-relaxed">{sadeSati.description}</p>
            <div className="mt-2 flex flex-wrap items-center gap-3 text-[11px] text-slate-400">
              <span>
                Saturn Transit: <strong className="text-white">{sadeSati.saturnTransitSign}</strong>
              </span>
              <span>•</span>
              <span>
                From Natal Moon: <strong className="text-amber-300">H{sadeSati.saturnHouseFromMoon}</strong>
              </span>
              <span>•</span>
              <span>
                From Natal Lagna: <strong className="text-sky-300">H{sadeSati.saturnHouseFromLagna}</strong>
              </span>
            </div>
          </div>
        </div>

        {/* Summary KPI Cards */}
        <div className="lg:col-span-5 grid grid-cols-3 gap-3">
          <div className="bg-slate-950/80 border border-slate-800 rounded-2xl p-3.5 flex flex-col justify-between">
            <span className="text-[10px] uppercase tracking-wider text-emerald-400 font-semibold">
              Unobstructed Benefic
            </span>
            <div className="text-xl font-bold text-white mt-1">
              {transitData.favorableTransitCount} <span className="text-xs text-slate-400">/ 9</span>
            </div>
            <span className="text-[11px] text-slate-400 mt-1">Auspicious Gochar</span>
          </div>

          <div className="bg-slate-950/80 border border-slate-800 rounded-2xl p-3.5 flex flex-col justify-between">
            <span className="text-[10px] uppercase tracking-wider text-amber-400 font-semibold">
              Vedha Obstructed
            </span>
            <div className="text-xl font-bold text-white mt-1">
              {transitData.vedhaObstructedCount} <span className="text-xs text-slate-400">Grahas</span>
            </div>
            <span className="text-[11px] text-slate-400 mt-1">Blocked by Vedha</span>
          </div>

          <div className="bg-slate-950/80 border border-slate-800 rounded-2xl p-3.5 flex flex-col justify-between">
            <span className="text-[10px] uppercase tracking-wider text-purple-400 font-semibold">
              Double Transit
            </span>
            <div className="text-xl font-bold text-white mt-1">
              {activatedHouses.length} <span className="text-xs text-slate-400">Houses</span>
            </div>
            <span className="text-[11px] text-slate-400 mt-1">Guru + Shani Trigger</span>
          </div>
        </div>
      </div>

      {/* Double Transit (Jupiter + Saturn) Activated Natal Houses Banner */}
      <div className="bg-slate-950/70 border border-slate-800 rounded-2xl p-4 space-y-3">
        <div className="flex items-center justify-between gap-2">
          <div className="flex items-center gap-2">
            <Zap className="w-4 h-4 text-amber-400" />
            <h3 className="text-sm font-bold text-white">
              Double Transit Activation (Jupiter + Saturn Conjoint Influence on Natal Houses)
            </h3>
          </div>
          <span className="text-[11px] text-slate-400">
            Transit Timestamp: <strong className="font-mono text-slate-200">{transitData.transitUtcDatetimeIso}</strong>
          </span>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
          {activatedHouses.map((dh) => (
            <div
              key={dh.houseNumber}
              className="p-3.5 rounded-xl bg-amber-500/10 border border-amber-500/40 space-y-1.5"
            >
              <div className="flex items-center justify-between">
                <span className="text-xs font-bold text-amber-300">
                  House {dh.houseNumber} • {dh.sign} ({dh.sanskritSign})
                </span>
                <span className="px-2 py-0.5 text-[10px] font-semibold rounded bg-amber-500/20 text-amber-200 border border-amber-500/40">
                  Activated
                </span>
              </div>
              <p className="text-xs font-medium text-white">{dh.domainTitle}</p>
              <div className="text-[11px] text-slate-300 space-y-0.5 pt-1 border-t border-amber-500/20">
                <div>
                  <strong className="text-amber-300">Guru:</strong> {dh.jupiterInfluence}
                </div>
                <div>
                  <strong className="text-sky-300">Shani:</strong> {dh.saturnInfluence}
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* 9-Graha Gochar, Vedha & Tara Bala Table */}
      <div className="space-y-2.5">
        <div className="flex items-center gap-2">
          <Sparkles className="w-4 h-4 text-amber-400" />
          <h3 className="text-sm font-bold text-white">
            9-Graha Gochar Positions, Vedha Obstruction &amp; 9-Fold Tara Bala Matrix
          </h3>
        </div>

        <div className="overflow-x-auto rounded-xl border border-slate-800">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-950 text-slate-400 border-b border-slate-800">
              <tr>
                <th className="py-2.5 px-3">Graha</th>
                <th className="py-2.5 px-3">Natal Sign (H)</th>
                <th className="py-2.5 px-3">Transit Sign &amp; Degree</th>
                <th className="py-2.5 px-3">Transit Nakshatra</th>
                <th className="py-2.5 px-3">From Moon</th>
                <th className="py-2.5 px-3">From Lagna</th>
                <th className="py-2.5 px-3">Gochar &amp; Vedha Status</th>
                <th className="py-2.5 px-3">Tara Bala</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/70">
              {transitData.planets.map((p) => {
                let statusPill = {
                  label: 'Neutral / Effort',
                  cls: 'bg-slate-800 text-slate-300 border-slate-700',
                };
                if (p.gocharStatus === 'FAVORABLE') {
                  statusPill = {
                    label: 'Favorable Gochar',
                    cls: 'bg-emerald-950/80 text-emerald-300 border-emerald-700/60',
                  };
                } else if (p.gocharStatus === 'VEDHA_OBSTRUCTED') {
                  statusPill = {
                    label: `Vedha by ${p.vedhaObstructor}`,
                    cls: 'bg-amber-950/80 text-amber-300 border-amber-700/60',
                  };
                }

                return (
                  <tr key={p.planet} className="hover:bg-slate-800/40 transition-colors">
                    <td className="py-2.5 px-3 font-bold">
                      <div className="flex items-center gap-2">
                        <span
                          className="w-2.5 h-2.5 rounded-full"
                          style={{ backgroundColor: PLANET_COLORS[p.planet] }}
                        />
                        <span className="text-white">{p.planet}</span>
                        {p.isRetrograde && (
                          <span className="px-1.5 py-0.5 rounded bg-amber-500/20 text-amber-300 text-[10px] font-semibold">
                            R
                          </span>
                        )}
                      </div>
                    </td>
                    <td className="py-2.5 px-3 text-slate-400">
                      {p.natalSign} (H{p.natalHouseFromLagna})
                    </td>
                    <td className="py-2.5 px-3 text-slate-200">
                      <span className="font-semibold text-white">{p.transitSign}</span>{' '}
                      <span className="font-mono text-[11px] text-slate-400">
                        {p.transitDegreeDms}
                      </span>
                    </td>
                    <td className="py-2.5 px-3 text-slate-200">
                      {p.transitNakshatra} (P{p.transitPada})
                    </td>
                    <td className="py-2.5 px-3 font-bold text-amber-300">H{p.houseFromMoon}</td>
                    <td className="py-2.5 px-3 font-semibold text-sky-300">H{p.houseFromLagna}</td>
                    <td className="py-2.5 px-3">
                      <span
                        className={`inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[10px] font-semibold border ${statusPill.cls}`}
                      >
                        {p.gocharStatus === 'VEDHA_OBSTRUCTED' && (
                          <AlertTriangle className="w-3 h-3 text-amber-400" />
                        )}
                        {statusPill.label}
                      </span>
                    </td>
                    <td className="py-2.5 px-3">
                      <span
                        className={`px-2 py-0.5 rounded text-[10px] font-semibold border ${
                          p.isTaraFavorable
                            ? 'bg-emerald-950/50 text-emerald-300 border-emerald-800/60'
                            : 'bg-slate-900 text-slate-400 border-slate-800'
                        }`}
                      >
                        {p.taraBalaCategory}
                      </span>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </div>
    </section>
  );
};

import React, { useState } from 'react';
import { Sparkles, Star } from 'lucide-react';
import type { NakshatraCalculationResponse, NakshatraPlacementItem } from '../../types/astrology';
import { PLANET_COLORS } from '../../utils/chartMath';

interface NakshatraExplorerSectionProps {
  nakshatraData: NakshatraCalculationResponse;
}

function getTaraBadgeStyle(quality: NakshatraPlacementItem['taraQuality']): string {
  switch (quality) {
    case 'FAVORABLE':
      return 'bg-emerald-500/15 text-emerald-300 border-emerald-500/30';
    case 'CHALLENGING':
      return 'bg-rose-500/15 text-rose-300 border-rose-500/30';
    default:
      return 'bg-amber-500/15 text-amber-300 border-amber-500/30';
  }
}

export const NakshatraExplorerSection: React.FC<NakshatraExplorerSectionProps> = ({
  nakshatraData,
}) => {
  const [selectedBody, setSelectedBody] = useState<string>('Moon');

  const activePlacement =
    nakshatraData.placements.find((p) => p.bodyName === selectedBody) ||
    nakshatraData.placements[0];

  const moonElapsedPct = (nakshatraData.moonElapsedFraction * 100).toFixed(2);
  const moonRemainingPct = (nakshatraData.moonRemainingFraction * 100).toFixed(2);

  return (
    <section className="rounded-2xl bg-cosmic-900/80 border border-cosmic-700 p-5 shadow-xl space-y-5">
      {/* Header & Janma Nakshatra Dasha Balance Banner */}
      <div className="flex flex-wrap items-center justify-between gap-4 border-b border-cosmic-800 pb-4">
        <div>
          <h3 className="text-base font-bold text-white flex items-center gap-2">
            <Star className="w-4 h-4 text-cosmic-gold" />
            <span>27-Nakshatra, Pada Navamsha &amp; 9-Fold Tara Bala Engine</span>
          </h3>
          <p className="text-xs text-slate-400 mt-0.5">
            Exact 13°20&apos; lunar mansion placements, 3°20&apos; Pada Navamsha (D9) mapping,
            Deity/Gana/Nadi/Yoni attributes, and Janma Tara Bala
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-3 text-xs">
          <div className="px-3 py-2 rounded-xl bg-cosmic-950 border border-cosmic-800">
            <span className="text-slate-400 block text-[10px]">Janma Nakshatra (Moon Star)</span>
            <span className="text-emerald-300 font-bold">
              {nakshatraData.janmaNakshatra} (Pada {nakshatraData.janmaPada})
            </span>
          </div>
          <div className="px-3 py-2 rounded-xl bg-cosmic-950 border border-cosmic-800">
            <span className="text-slate-400 block text-[10px]">Starting Dasha Ruler</span>
            <span className="text-cosmic-gold font-bold">
              {nakshatraData.janmaNakshatraLord} ({moonRemainingPct}% Balance Left)
            </span>
          </div>
        </div>
      </div>

      {/* Moon Traversal Progress Bar + Selected Nakshatra Deep-Dive Card */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-4 items-stretch">
        <div className="lg:col-span-5 p-4 rounded-xl bg-cosmic-950/90 border border-cosmic-800 flex flex-col justify-between space-y-3">
          <div>
            <div className="flex items-center justify-between text-xs mb-1.5">
              <span className="font-semibold text-slate-300">
                Moon Traversal in {nakshatraData.janmaNakshatra}
              </span>
              <span className="font-mono text-cosmic-gold">{moonElapsedPct}% Elapsed</span>
            </div>
            <div className="w-full h-2.5 rounded-full bg-cosmic-800 overflow-hidden">
              <div
                className="h-full bg-gradient-to-r from-cosmic-gold to-emerald-400 rounded-full"
                style={{ width: `${Math.min(100, Math.max(2, Number(moonElapsedPct)))}%` }}
              />
            </div>
            <div className="flex justify-between text-[11px] text-slate-400 mt-1.5">
              <span>Elapsed: {moonElapsedPct}%</span>
              <span>Remaining Balance at Birth: {moonRemainingPct}%</span>
            </div>
          </div>

          {activePlacement && (
            <div className="pt-3 border-t border-cosmic-800 space-y-2 text-xs">
              <div className="flex items-center justify-between">
                <span className="text-slate-400">Selected Body:</span>
                <span className="font-bold text-white">
                  {activePlacement.bodyName} in {activePlacement.nakshatraName} (Pada{' '}
                  {activePlacement.pada})
                </span>
              </div>
              <div className="grid grid-cols-2 gap-2 pt-1">
                <div className="p-2 rounded-lg bg-cosmic-900/80 border border-cosmic-800">
                  <span className="text-[10px] text-slate-400 block">Presiding Deity</span>
                  <span className="text-white font-semibold">{activePlacement.deity}</span>
                </div>
                <div className="p-2 rounded-lg bg-cosmic-900/80 border border-cosmic-800">
                  <span className="text-[10px] text-slate-400 block">Sacred Symbol</span>
                  <span className="text-white font-semibold">{activePlacement.symbol}</span>
                </div>
              </div>
            </div>
          )}
        </div>

        {/* Selected Body Panchanga & Tara Bala Metadata */}
        {activePlacement && (
          <div className="lg:col-span-7 p-4 rounded-xl bg-cosmic-950/90 border border-cosmic-800 grid grid-cols-2 sm:grid-cols-4 gap-3 text-xs">
            <div className="p-2.5 rounded-lg bg-cosmic-900/70 border border-cosmic-800">
              <span className="text-[10px] text-slate-400 block">Pada Navamsha (D9)</span>
              <span className="text-cosmic-gold font-bold text-sm">
                {activePlacement.padaNavamshaSign}
              </span>
              <span className="text-[10px] text-slate-400 block mt-0.5">
                Pada {activePlacement.pada} of 4
              </span>
            </div>

            <div className="p-2.5 rounded-lg bg-cosmic-900/70 border border-cosmic-800">
              <span className="text-[10px] text-slate-400 block">Nakshatra Lord</span>
              <span className="text-white font-bold text-sm">{activePlacement.rulerPlanet}</span>
              <span className="text-[10px] text-slate-400 block mt-0.5">
                Rel: {activePlacement.relationshipToNakshatraLord.replace('_', ' ')}
              </span>
            </div>

            <div className="p-2.5 rounded-lg bg-cosmic-900/70 border border-cosmic-800">
              <span className="text-[10px] text-slate-400 block">Gana / Nadi / Yoni</span>
              <span className="text-white font-bold text-sm">{activePlacement.gana}</span>
              <span className="text-[10px] text-slate-400 block mt-0.5">
                {activePlacement.nadi} Nadi • {activePlacement.yoni}
              </span>
            </div>

            <div className="p-2.5 rounded-lg bg-cosmic-900/70 border border-cosmic-800">
              <span className="text-[10px] text-slate-400 block">Tara Bala (from Moon)</span>
              <span className="text-white font-bold text-sm">
                #{activePlacement.taraNumberFromMoon} {activePlacement.taraNameFromMoon}
              </span>
              <span className="text-[10px] text-slate-400 block mt-0.5">
                {activePlacement.taraQuality.replace('_', ' ')}
              </span>
            </div>
          </div>
        )}
      </div>

      {/* Complete 10-Body Nakshatra & Tara Bala Table */}
      <div className="overflow-x-auto rounded-xl border border-cosmic-800">
        <table className="w-full text-left text-xs">
          <thead className="bg-cosmic-950 text-slate-400 border-b border-cosmic-800">
            <tr>
              <th className="py-2.5 px-3">Body</th>
              <th className="py-2.5 px-3">Rashi</th>
              <th className="py-2.5 px-3">Nakshatra (#1–27)</th>
              <th className="py-2.5 px-3">Pada</th>
              <th className="py-2.5 px-3">D9 Navamsha Sign</th>
              <th className="py-2.5 px-3">Nak. Ruler</th>
              <th className="py-2.5 px-3">In-Nakshatra DMS</th>
              <th className="py-2.5 px-3">Gana / Nadi / Yoni</th>
              <th className="py-2.5 px-3">9-Fold Tara Bala</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-cosmic-800/70">
            {nakshatraData.placements.map((item) => {
              const isSelected = selectedBody === item.bodyName;
              const dotColor =
                PLANET_COLORS[item.bodyName as keyof typeof PLANET_COLORS] || '#f5b841';
              return (
                <tr
                  key={item.bodyName}
                  onClick={() => setSelectedBody(item.bodyName)}
                  className={`cursor-pointer transition-colors ${
                    isSelected ? 'bg-cosmic-gold/15' : 'hover:bg-cosmic-800/60'
                  }`}
                >
                  <td className="py-2.5 px-3 font-bold text-white flex items-center gap-2">
                    <span
                      className="w-2.5 h-2.5 rounded-full shrink-0"
                      style={{ backgroundColor: dotColor }}
                    />
                    <span>
                      {item.bodyName.toUpperCase() === 'ASCENDANT'
                        ? 'Ascendant (Lagna)'
                        : item.bodyName}
                    </span>
                  </td>
                  <td className="py-2.5 px-3 text-slate-300">{item.rashiSign}</td>
                  <td className="py-2.5 px-3 font-semibold text-white">
                    #{item.nakshatraIndex} {item.nakshatraName}
                  </td>
                  <td className="py-2.5 px-3 text-cosmic-gold font-bold">P{item.pada}</td>
                  <td className="py-2.5 px-3 text-emerald-300 font-semibold">
                    {item.padaNavamshaSign}
                  </td>
                  <td className="py-2.5 px-3 text-slate-200">{item.rulerPlanet}</td>
                  <td className="py-2.5 px-3 font-mono text-slate-300">
                    {item.degreeInNakshatraDms} ({(item.elapsedFraction * 100).toFixed(1)}%)
                  </td>
                  <td className="py-2.5 px-3 text-slate-300">
                    {item.gana} • {item.nadi} • {item.yoni}
                  </td>
                  <td className="py-2.5 px-3">
                    <span
                      className={`inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[10px] font-semibold border ${getTaraBadgeStyle(
                        item.taraQuality,
                      )}`}
                    >
                      <Sparkles className="w-2.5 h-2.5" />
                      <span>
                        {item.taraNumberFromMoon}. {item.taraNameFromMoon}
                      </span>
                    </span>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </section>
  );
};

import React, { useMemo, useState } from 'react';
import { Eye, GitCompareArrows, Layers, ShieldAlert, Sparkles } from 'lucide-react';
import type { AspectCalculationResponse, GrahaName, PlanetToPlanetAspect } from '../../types/astrology';
import { PLANET_ABBR as GRAHA_ABBR, PLANET_COLORS as GRAHA_COLORS } from '../../utils/chartMath';

interface DrishtiMatrixSectionProps {
  aspectData: AspectCalculationResponse | null;
  loading?: boolean;
}

const NINE_GRAHAS: GrahaName[] = [
  'Sun',
  'Moon',
  'Mars',
  'Mercury',
  'Jupiter',
  'Venus',
  'Saturn',
  'Rahu',
  'Ketu',
];

type DrishtiTab = 'GRAHA_MATRIX' | 'BHAVA_GRID' | 'DETAILED_TABLE';

export const DrishtiMatrixSection: React.FC<DrishtiMatrixSectionProps> = ({
  aspectData,
  loading = false,
}) => {
  const [activeTab, setActiveTab] = useState<DrishtiTab>('GRAHA_MATRIX');
  const [showPadaAspects, setShowPadaAspects] = useState<boolean>(false);
  const [selectedPlanetFilter, setSelectedPlanetFilter] = useState<string>('ALL');

  const filteredPlanetAspects = useMemo(() => {
    if (!aspectData) return [];
    return aspectData.planetAspects.filter((pa) => {
      if (!showPadaAspects && !pa.isFullAspect) return false;
      if (
        selectedPlanetFilter !== 'ALL' &&
        pa.sourcePlanet !== selectedPlanetFilter &&
        pa.targetPlanet !== selectedPlanetFilter
      ) {
        return false;
      }
      return true;
    });
  }, [aspectData, showPadaAspects, selectedPlanetFilter]);

  // Lookup map: `${sourcePlanet}__${targetPlanet}` -> PlanetToPlanetAspect
  const planetMatrixMap = useMemo(() => {
    const map = new Map<string, PlanetToPlanetAspect>();
    if (!aspectData) return map;
    for (const pa of aspectData.planetAspects) {
      if (!showPadaAspects && !pa.isFullAspect) continue;
      map.set(`${pa.sourcePlanet}__${pa.targetPlanet}`, pa);
    }
    return map;
  }, [aspectData, showPadaAspects]);

  // Lookup map: `${sourcePlanet}__H${targetHouse}` -> HouseAspectEntry
  const houseGridMap = useMemo(() => {
    const map = new Map<string, AspectCalculationResponse['houseAspects'][number]>();
    if (!aspectData) return map;
    for (const ha of aspectData.houseAspects) {
      if (!showPadaAspects && !ha.isFullAspect) continue;
      map.set(`${ha.sourcePlanet}__H${ha.targetHouse}`, ha);
    }
    return map;
  }, [aspectData, showPadaAspects]);

  if (loading && !aspectData) {
    return (
      <section className="rounded-2xl border border-slate-800/90 bg-slate-900/60 p-6 shadow-xl">
        <div className="animate-pulse space-y-4">
          <div className="h-6 w-72 rounded bg-slate-800" />
          <div className="h-64 rounded-xl bg-slate-800/50" />
        </div>
      </section>
    );
  }

  if (!aspectData) return null;

  const fullPlanetAspectsCount = aspectData.planetAspects.filter((a) => a.isFullAspect).length;
  const specialAspectsCount = aspectData.planetAspects.filter((a) => a.isSpecialAspect).length;

  return (
    <section className="rounded-2xl border border-slate-800/90 bg-slate-900/60 p-5 sm:p-6 shadow-xl space-y-6">
      {/* Header */}
      <div className="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-4 border-b border-slate-800/80 pb-4">
        <div>
          <div className="flex items-center gap-2.5">
            <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-amber-500/10 border border-amber-500/30 text-amber-400">
              <Eye className="h-5 w-5" />
            </div>
            <div>
              <h2 className="text-lg font-bold tracking-tight text-slate-100 flex items-center gap-2">
                Planetary Aspects Engine (Graha Drishti &amp; Sphuta Virupas)
                <span className="rounded-full bg-amber-500/15 border border-amber-500/30 px-2.5 py-0.5 text-xs font-semibold text-amber-300">
                  BPHS Ch. 26–27
                </span>
              </h2>
              <p className="text-xs text-slate-400">
                Deterministic Parashari Rashi/Bhava Aspects, Vishesha Drishti (Mars 4/8, Jupiter 5/9, Saturn 3/10, Rahu/Ketu 5/9), &amp; Exact Longitudinal Sphuta Virupa Strength
              </p>
            </div>
          </div>
        </div>

        {/* Summary Counters & Controls */}
        <div className="flex flex-wrap items-center gap-2.5">
          <div className="rounded-xl bg-slate-950/80 border border-slate-800 px-3 py-1.5 text-xs">
            <span className="text-slate-400">Full Graha Aspects: </span>
            <span className="font-bold text-amber-400">{fullPlanetAspectsCount}</span>
          </div>
          <div className="rounded-xl bg-slate-950/80 border border-slate-800 px-3 py-1.5 text-xs">
            <span className="text-slate-400">Vishesha (Special): </span>
            <span className="font-bold text-purple-400">{specialAspectsCount}</span>
          </div>
          <button
            type="button"
            onClick={() => setShowPadaAspects((prev) => !prev)}
            className={`rounded-xl px-3 py-1.5 text-xs font-semibold border transition ${
              showPadaAspects
                ? 'bg-indigo-500/20 border-indigo-500/50 text-indigo-300'
                : 'bg-slate-950/80 border-slate-800 text-slate-300 hover:border-slate-700'
            }`}
          >
            {showPadaAspects ? 'Showing All Pada Drishti (4/4, 3/4, 1/2, 1/4)' : 'Full Aspects Only (60 Virupas)'}
          </button>
        </div>
      </div>

      {/* Mutual Relationships (Paraspara Drishti & Graha Yuti) */}
      {aspectData.mutualRelationships.length > 0 && (
        <div className="space-y-2.5">
          <div className="flex items-center gap-2 text-xs font-semibold uppercase tracking-wider text-slate-400">
            <GitCompareArrows className="h-4 w-4 text-amber-400" />
            <span>Mutual Planetary Locks (Paraspara Drishti &amp; Graha Yuti Sambandha)</span>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-3">
            {aspectData.mutualRelationships.map((rel, idx) => {
              const isYuti = rel.relationshipType === 'CONJUNCTION_YUTI';
              const isSpecialLock = rel.relationshipType === 'MUTUAL_SPECIAL_LOCK';
              return (
                <div
                  key={`${rel.planetA}-${rel.planetB}-${idx}`}
                  className="rounded-xl border border-slate-800/90 bg-slate-950/70 p-3.5 flex flex-col justify-between gap-2"
                >
                  <div className="flex items-center justify-between gap-2">
                    <div className="flex items-center gap-2">
                      <span
                        className="inline-flex items-center gap-1 rounded-md px-2 py-0.5 text-xs font-bold"
                        style={{
                          backgroundColor: `${GRAHA_COLORS[rel.planetA] || '#f59e0b'}22`,
                          color: GRAHA_COLORS[rel.planetA] || '#f59e0b',
                        }}
                      >
                        {rel.planetA} (H{rel.houseA})
                      </span>
                      <span className="text-slate-500 font-bold">⇄</span>
                      <span
                        className="inline-flex items-center gap-1 rounded-md px-2 py-0.5 text-xs font-bold"
                        style={{
                          backgroundColor: `${GRAHA_COLORS[rel.planetB] || '#38bdf8'}22`,
                          color: GRAHA_COLORS[rel.planetB] || '#38bdf8',
                        }}
                      >
                        {rel.planetB} (H{rel.houseB})
                      </span>
                    </div>
                    <span
                      className={`rounded-full px-2 py-0.5 text-[10px] font-bold uppercase tracking-wide border ${
                        isYuti
                          ? 'bg-emerald-500/15 border-emerald-500/30 text-emerald-300'
                          : isSpecialLock
                          ? 'bg-purple-500/15 border-purple-500/30 text-purple-300'
                          : 'bg-amber-500/15 border-amber-500/30 text-amber-300'
                      }`}
                    >
                      {isYuti
                        ? 'Graha Yuti (1st)'
                        : isSpecialLock
                        ? 'Special Lock'
                        : '1/7 Opposition'}
                    </span>
                  </div>
                  <p className="text-xs text-slate-300 leading-relaxed">{rel.description}</p>
                  <div className="flex items-center justify-between text-[11px] text-slate-400 pt-1 border-t border-slate-800/60">
                    <span>Exact Orb: <strong className="text-slate-200">{rel.exactOrbDeg.toFixed(2)}°</strong></span>
                    <span>Combined Strength: <strong className="text-amber-300">{rel.combinedVirupaStrength} Virupas</strong></span>
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      )}

      {/* Navigation Tabs */}
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div className="inline-flex rounded-xl bg-slate-950/90 p-1 border border-slate-800">
          <button
            type="button"
            onClick={() => setActiveTab('GRAHA_MATRIX')}
            className={`rounded-lg px-3.5 py-1.5 text-xs font-semibold transition ${
              activeTab === 'GRAHA_MATRIX'
                ? 'bg-amber-500 text-slate-950 shadow'
                : 'text-slate-400 hover:text-slate-200'
            }`}
          >
            9×9 Graha-to-Graha Matrix
          </button>
          <button
            type="button"
            onClick={() => setActiveTab('BHAVA_GRID')}
            className={`rounded-lg px-3.5 py-1.5 text-xs font-semibold transition ${
              activeTab === 'BHAVA_GRID'
                ? 'bg-amber-500 text-slate-950 shadow'
                : 'text-slate-400 hover:text-slate-200'
            }`}
          >
            Graha-to-Bhava (12 Houses) Grid
          </button>
          <button
            type="button"
            onClick={() => setActiveTab('DETAILED_TABLE')}
            className={`rounded-lg px-3.5 py-1.5 text-xs font-semibold transition ${
              activeTab === 'DETAILED_TABLE'
                ? 'bg-amber-500 text-slate-950 shadow'
                : 'text-slate-400 hover:text-slate-200'
            }`}
          >
            Sphuta Drishti Virupa Table ({filteredPlanetAspects.length})
          </button>
        </div>

        {activeTab === 'DETAILED_TABLE' && (
          <div className="flex items-center gap-2">
            <span className="text-xs text-slate-400">Filter Graha:</span>
            <select
              value={selectedPlanetFilter}
              onChange={(e) => setSelectedPlanetFilter(e.target.value)}
              className="rounded-lg border border-slate-700 bg-slate-950 px-2.5 py-1 text-xs text-slate-200 focus:border-amber-500 focus:outline-none"
            >
              <option value="ALL">All 9 Grahas</option>
              {NINE_GRAHAS.map((g) => (
                <option key={g} value={g}>
                  {g}
                </option>
              ))}
            </select>
          </div>
        )}
      </div>

      {/* TAB 1: 9x9 Graha-to-Graha Matrix */}
      {activeTab === 'GRAHA_MATRIX' && (
        <div className="overflow-x-auto rounded-xl border border-slate-800 bg-slate-950/60">
          <table className="w-full border-collapse text-left text-xs">
            <thead>
              <tr className="border-b border-slate-800 bg-slate-900/90 text-slate-400">
                <th className="p-3 font-semibold border-r border-slate-800">
                  Source ↓ / Target →
                </th>
                {NINE_GRAHAS.map((target) => (
                  <th key={target} className="p-2.5 text-center font-semibold border-r border-slate-800/60">
                    <span style={{ color: GRAHA_COLORS[target] }}>{GRAHA_ABBR[target]}</span>
                    <div className="text-[10px] font-normal text-slate-500">{target}</div>
                  </th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/70">
              {NINE_GRAHAS.map((source) => (
                <tr key={source} className="hover:bg-slate-900/40">
                  <td className="p-3 font-semibold border-r border-slate-800 bg-slate-900/40 whitespace-nowrap">
                    <span style={{ color: GRAHA_COLORS[source] }}>{source}</span>
                  </td>
                  {NINE_GRAHAS.map((target) => {
                    if (source === target) {
                      return (
                        <td
                          key={target}
                          className="p-2 text-center bg-slate-900/80 text-slate-600 border-r border-slate-800/60"
                        >
                          —
                        </td>
                      );
                    }
                    const cell = planetMatrixMap.get(`${source}__${target}`);
                    if (!cell) {
                      return (
                        <td
                          key={target}
                          className="p-2 text-center text-slate-600 border-r border-slate-800/60"
                        >
                          ·
                        </td>
                      );
                    }
                    const isBenefic = cell.aspectNature === 'BENEFIC';
                    return (
                      <td
                        key={target}
                        className="p-2 text-center border-r border-slate-800/60"
                        title={`${cell.sourcePlanet} (H${cell.sourceHouse}) → ${cell.targetPlanet} (H${cell.targetHouse}): ${cell.ruleApplied} | Sphuta Virupa: ${cell.sphutaVirupaStrength}`}
                      >
                        <div
                          className={`inline-flex flex-col items-center rounded-lg px-2 py-1 border ${
                            cell.isSpecialAspect
                              ? 'bg-purple-500/15 border-purple-500/40 text-purple-300'
                              : cell.isFullAspect
                              ? isBenefic
                                ? 'bg-emerald-500/15 border-emerald-500/40 text-emerald-300'
                                : 'bg-amber-500/15 border-amber-500/40 text-amber-300'
                              : 'bg-slate-800/70 border-slate-700 text-slate-300'
                          }`}
                        >
                          <span className="font-bold text-[11px]">
                            {cell.houseOffset}th ({cell.padaFraction})
                          </span>
                          <span className="text-[10px] opacity-80">
                            {cell.virupaStrength}V / {cell.sphutaVirupaStrength.toFixed(0)}s
                          </span>
                        </div>
                      </td>
                    );
                  })}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* TAB 2: Graha-to-Bhava (12 Houses) Grid */}
      {activeTab === 'BHAVA_GRID' && (
        <div className="overflow-x-auto rounded-xl border border-slate-800 bg-slate-950/60">
          <table className="w-full border-collapse text-left text-xs">
            <thead>
              <tr className="border-b border-slate-800 bg-slate-900/90 text-slate-400">
                <th className="p-3 font-semibold border-r border-slate-800">Graha ↓ / House →</th>
                {Array.from({ length: 12 }, (_, i) => i + 1).map((hNum) => (
                  <th key={hNum} className="p-2.5 text-center font-semibold border-r border-slate-800/60">
                    H{hNum}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/70">
              {NINE_GRAHAS.map((source) => (
                <tr key={source} className="hover:bg-slate-900/40">
                  <td className="p-3 font-semibold border-r border-slate-800 bg-slate-900/40 whitespace-nowrap">
                    <span style={{ color: GRAHA_COLORS[source] }}>{source}</span>
                  </td>
                  {Array.from({ length: 12 }, (_, i) => i + 1).map((hNum) => {
                    const ha = houseGridMap.get(`${source}__H${hNum}`);
                    if (!ha) {
                      return (
                        <td
                          key={hNum}
                          className="p-2 text-center text-slate-600 border-r border-slate-800/60"
                        >
                          ·
                        </td>
                      );
                    }
                    return (
                      <td
                        key={hNum}
                        className="p-2 text-center border-r border-slate-800/60"
                        title={ha.ruleApplied}
                      >
                        <span
                          className={`inline-block rounded-md px-2 py-0.5 text-[11px] font-bold border ${
                            ha.isSpecialAspect
                              ? 'bg-purple-500/20 border-purple-500/40 text-purple-300'
                              : ha.isFullAspect
                              ? 'bg-amber-500/20 border-amber-500/40 text-amber-300'
                              : 'bg-slate-800 border-slate-700 text-slate-400'
                          }`}
                        >
                          {ha.houseOffset}th ({ha.virupaStrength}V)
                        </span>
                      </td>
                    );
                  })}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* TAB 3: Detailed Planet-to-Planet & Sphuta Drishti Table */}
      {activeTab === 'DETAILED_TABLE' && (
        <div className="overflow-x-auto rounded-xl border border-slate-800 bg-slate-950/60">
          <table className="w-full border-collapse text-left text-xs">
            <thead>
              <tr className="border-b border-slate-800 bg-slate-900/90 text-slate-400">
                <th className="p-3 font-semibold">Source Graha</th>
                <th className="p-3 font-semibold">Target Graha</th>
                <th className="p-3 font-semibold">Aspect Type</th>
                <th className="p-3 font-semibold">Nature</th>
                <th className="p-3 font-semibold">Arc (Δ°) &amp; Orb</th>
                <th className="p-3 font-semibold">Bhava Virupas</th>
                <th className="p-3 font-semibold">Sphuta Virupas (Exact Degree)</th>
                <th className="p-3 font-semibold">Classical Rule Applied</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/70">
              {filteredPlanetAspects.map((pa, idx) => (
                <tr key={`${pa.sourcePlanet}-${pa.targetPlanet}-${idx}`} className="hover:bg-slate-900/40">
                  <td className="p-3 font-semibold whitespace-nowrap">
                    <span style={{ color: GRAHA_COLORS[pa.sourcePlanet] }}>{pa.sourcePlanet}</span>
                    <span className="ml-1.5 text-slate-400 font-normal">
                      (H{pa.sourceHouse} · {pa.sourceSign})
                    </span>
                  </td>
                  <td className="p-3 font-semibold whitespace-nowrap">
                    <span style={{ color: GRAHA_COLORS[pa.targetPlanet] }}>{pa.targetPlanet}</span>
                    <span className="ml-1.5 text-slate-400 font-normal">
                      (H{pa.targetHouse} · {pa.targetSign})
                    </span>
                  </td>
                  <td className="p-3 whitespace-nowrap">
                    <span
                      className={`rounded-md px-2 py-0.5 text-[11px] font-bold border ${
                        pa.isSpecialAspect
                          ? 'bg-purple-500/15 border-purple-500/40 text-purple-300'
                          : pa.isFullAspect
                          ? 'bg-amber-500/15 border-amber-500/40 text-amber-300'
                          : 'bg-slate-800 border-slate-700 text-slate-300'
                      }`}
                    >
                      {pa.aspectType} ({pa.padaFraction})
                    </span>
                  </td>
                  <td className="p-3 whitespace-nowrap">
                    {pa.aspectNature === 'BENEFIC' ? (
                      <span className="inline-flex items-center gap-1 text-emerald-400 font-semibold">
                        <Sparkles className="h-3.5 w-3.5" /> Benefic
                      </span>
                    ) : (
                      <span className="inline-flex items-center gap-1 text-rose-400 font-semibold">
                        <ShieldAlert className="h-3.5 w-3.5" /> Malefic
                      </span>
                    )}
                  </td>
                  <td className="p-3 whitespace-nowrap text-slate-300">
                    {pa.angularSeparationDeg.toFixed(2)}°{' '}
                    <span className="text-slate-500">(Orb {pa.orbFromExactAspectDeg.toFixed(2)}°)</span>
                  </td>
                  <td className="p-3 whitespace-nowrap font-bold text-amber-300">
                    {pa.virupaStrength.toFixed(1)} V
                  </td>
                  <td className="p-3 whitespace-nowrap">
                    <div className="flex items-center gap-2">
                      <div className="h-2 w-20 rounded-full bg-slate-800 overflow-hidden">
                        <div
                          className="h-full rounded-full bg-gradient-to-r from-amber-500 to-purple-400"
                          style={{ width: `${Math.min(100, (pa.sphutaVirupaStrength / 60) * 100)}%` }}
                        />
                      </div>
                      <span className="font-mono font-semibold text-slate-200">
                        {pa.sphutaVirupaStrength.toFixed(2)} V
                      </span>
                    </div>
                  </td>
                  <td className="p-3 text-slate-400 max-w-xs truncate" title={pa.ruleApplied}>
                    {pa.ruleApplied}
                  </td>
                </tr>
              ))}
              {filteredPlanetAspects.length === 0 && (
                <tr>
                  <td colSpan={8} className="p-6 text-center text-slate-500">
                    <Layers className="h-5 w-5 mx-auto mb-1 opacity-60" />
                    No matching aspects for the selected filter.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      )}
    </section>
  );
};

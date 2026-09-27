import React, { useMemo, useState } from 'react';
import { Award, Diamond, Grid, LayoutGrid, Sparkles } from 'lucide-react';
import type {
  DivisionalCalculationResponse,
  GrahaName,
  HouseDetail,
  PlanetPosition,
} from '../../types/astrology';
import { NorthIndianChart } from '../charts/NorthIndianChart';
import { SouthIndianChart } from '../charts/SouthIndianChart';
import { PLANET_COLORS, getDignityBadgeStyle } from '../../utils/chartMath';

interface DivisionalChartsSectionProps {
  divisionalData: DivisionalCalculationResponse;
  chartStyle: 'NORTH' | 'SOUTH';
  onToggleChartStyle: (style: 'NORTH' | 'SOUTH') => void;
}

export const DivisionalChartsSection: React.FC<DivisionalChartsSectionProps> = ({
  divisionalData,
  chartStyle,
  onToggleChartStyle,
}) => {
  // Default to D9 Navamsha so the user immediately sees D9 alongside the main D1 chart
  const [selectedVargaCode, setSelectedVargaCode] = useState<string>('D9');
  const [selectedPlanet, setSelectedPlanet] = useState<GrahaName | null>('Venus');
  const [selectedHouse, setSelectedHouse] = useState<number | null>(null);

  const activeVarga = useMemo(() => {
    return (
      divisionalData.charts.find((c) => c.vargaCode === selectedVargaCode) ||
      divisionalData.charts[5] ||
      divisionalData.charts[0]
    );
  }, [divisionalData.charts, selectedVargaCode]);

  // Adapt DivisionalPlanetPlacement[] into PlanetPosition[] for NorthIndianChart / SouthIndianChart SVG rendering
  const svgPlanets: PlanetPosition[] = useMemo(() => {
    if (!activeVarga) return [];
    return activeVarga.planets.map((p) => ({
      planet: p.planet,
      longitude: p.d1Longitude,
      latitude: 0,
      speedLongitude: 0,
      sign: p.vargaSign,
      sanskritSign: p.vargaSanskritSign,
      signIndex: p.vargaSignIndex,
      signLord: p.vargaSignLord,
      degreeInSign: p.d1Longitude % 30,
      degreeDms: `Div #${p.partNumber}`,
      house: p.vargaHouse,
      nakshatra: '',
      nakshatraIndex: 1,
      nakshatraLord: p.vargaSignLord,
      pada: 1,
      retrograde: p.retrograde,
      combust: p.combust,
      angularDistanceFromSun: null,
      exalted: p.dignityInVarga === 'EXALTED',
      debilitated: p.dignityInVarga === 'DEBILITATED',
      moolatrikona: p.dignityInVarga === 'MOOLATRIKONA',
      ownSign: p.dignityInVarga === 'OWN_SIGN',
      dignity: p.dignityInVarga,
      dispositorRelationship: p.dignityInVarga,
      planetaryRelationships: {},
    }));
  }, [activeVarga]);

  const svgHouses: HouseDetail[] = useMemo(() => {
    if (!activeVarga) return [];
    return activeVarga.houses.map((h) => ({
      houseNumber: h.houseNumber,
      sign: h.sign,
      sanskritSign: h.sanskritSign,
      signIndex: h.signIndex,
      lordPlanet: h.lordPlanet,
      lordPlacedInHouse: 1,
      lordPlacedInSign: h.sign,
      lordDignity: 'NEUTRAL',
      degreeCusp: 0,
      sripatiCuspLongitude: 0,
      sripatiStartLongitude: 0,
      sripatiEndLongitude: 0,
      occupants: h.occupants,
      chalitOccupants: h.occupants,
      aspectsReceived: [],
      purushartha: 'DHARMA',
      classifications: [],
      significations: [],
      baselineStrengthScore: 50,
      strengthGrade: 'MODERATE',
    }));
  }, [activeVarga]);

  if (!activeVarga) return null;

  return (
    <section className="rounded-2xl bg-cosmic-900/80 border border-cosmic-700 p-5 shadow-xl space-y-5">
      {/* Header & D9 Vargottama Banner */}
      <div className="flex flex-wrap items-center justify-between gap-4 border-b border-cosmic-800 pb-4">
        <div>
          <h3 className="text-base font-bold text-white flex items-center gap-2">
            <Grid className="w-4 h-4 text-cosmic-gold" />
            <span>Shodashavarga Divisional Charts Explorer (D1–D60 BPHS)</span>
          </h3>
          <p className="text-xs text-slate-400 mt-0.5">
            All 16 classical Parashari harmonic charts • Vargottama detection • Shashtiamsha (D60)
            deities
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-2 text-xs">
          <div className="px-3 py-1.5 rounded-xl bg-cosmic-950 border border-cosmic-800 flex items-center gap-2">
            <Award className="w-3.5 h-3.5 text-cosmic-gold" />
            <span className="text-slate-400">D9 Vargottama:</span>
            <span className="font-bold text-emerald-300">
              {divisionalData.d9VargottamaSummary.length > 0
                ? divisionalData.d9VargottamaSummary.join(', ')
                : 'None'}
            </span>
          </div>
        </div>
      </div>

      {/* 16-Varga Selector Pill Bar */}
      <div className="flex flex-wrap gap-1.5">
        {divisionalData.charts.map((vc) => {
          const isSelected = vc.vargaCode === selectedVargaCode;
          return (
            <button
              key={vc.vargaCode}
              type="button"
              onClick={() => setSelectedVargaCode(vc.vargaCode)}
              className={`px-3 py-1.5 rounded-lg text-xs font-bold border transition-all flex items-center gap-1.5 ${
                isSelected
                  ? 'bg-cosmic-gold text-cosmic-950 border-cosmic-gold shadow-md'
                  : 'bg-cosmic-950 border-cosmic-800 text-slate-300 hover:border-cosmic-700 hover:text-white'
              }`}
            >
              <span>{vc.vargaCode}</span>
              <span
                className={`text-[10px] font-normal ${
                  isSelected ? 'text-cosmic-950/80' : 'text-slate-400'
                }`}
              >
                {vc.sanskritName}
              </span>
            </button>
          );
        })}
      </div>

      {/* Selected Varga Metadata Banner */}
      <div className="p-3.5 rounded-xl bg-cosmic-950/90 border border-cosmic-800 flex flex-wrap items-center justify-between gap-4 text-xs">
        <div>
          <div className="flex items-center gap-2">
            <span className="text-sm font-bold text-cosmic-gold">{activeVarga.title}</span>
            {activeVarga.isAscendantVargottama && (
              <span className="px-2 py-0.5 rounded-full bg-emerald-500/20 border border-emerald-500/40 text-emerald-300 text-[10px] font-bold">
                VARGOTTAMA LAGNA
              </span>
            )}
          </div>
          <p className="text-slate-400 mt-0.5">
            Domain: <strong className="text-slate-200">{activeVarga.domainSignification}</strong>
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-4">
          <div>
            <span className="text-slate-400 block text-[10px]">{activeVarga.vargaCode} Ascendant</span>
            <span className="font-bold text-white">
              {activeVarga.ascendantSign} ({activeVarga.ascendantSanskritSign}) • Lord:{' '}
              {activeVarga.ascendantLord}
            </span>
          </div>
          <div>
            <span className="text-slate-400 block text-[10px]">
              Vargottama in {activeVarga.vargaCode}
            </span>
            <span className="font-bold text-emerald-300">
              {activeVarga.vargottamaPlanets.length > 0
                ? activeVarga.vargottamaPlanets.join(', ')
                : 'None'}
            </span>
          </div>
        </div>
      </div>

      {/* Split View: Left = Interactive Varga SVG Chart | Right = Varga Planetary Placements Table */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
        <div className="lg:col-span-5 rounded-xl bg-cosmic-950/70 border border-cosmic-800 p-4 space-y-3">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold text-white">
              {activeVarga.vargaCode} ({activeVarga.sanskritName}) Kundli Diagram
            </span>
            <div className="inline-flex rounded-lg bg-cosmic-900 p-0.5 border border-cosmic-800 text-[11px]">
              <button
                type="button"
                onClick={() => onToggleChartStyle('NORTH')}
                className={`inline-flex items-center gap-1 px-2.5 py-1 rounded font-semibold ${
                  chartStyle === 'NORTH'
                    ? 'bg-cosmic-gold text-cosmic-950'
                    : 'text-slate-400 hover:text-white'
                }`}
              >
                <Diamond className="w-3 h-3" />
                <span>North</span>
              </button>
              <button
                type="button"
                onClick={() => onToggleChartStyle('SOUTH')}
                className={`inline-flex items-center gap-1 px-2.5 py-1 rounded font-semibold ${
                  chartStyle === 'SOUTH'
                    ? 'bg-cosmic-gold text-cosmic-950'
                    : 'text-slate-400 hover:text-white'
                }`}
              >
                <LayoutGrid className="w-3 h-3" />
                <span>South</span>
              </button>
            </div>
          </div>

          {chartStyle === 'NORTH' ? (
            <NorthIndianChart
              ascendantSignIndex={activeVarga.ascendantSignIndex}
              planets={svgPlanets}
              houses={svgHouses}
              selectedHouse={selectedHouse}
              selectedPlanet={selectedPlanet}
              onSelectHouse={(hNum) => setSelectedHouse(hNum)}
              onSelectPlanet={(pName) => setSelectedPlanet(pName)}
            />
          ) : (
            <SouthIndianChart
              ascendantSignIndex={activeVarga.ascendantSignIndex}
              vargaTitle={`${activeVarga.vargaCode} ${activeVarga.sanskritName.toUpperCase()}`}
              planets={svgPlanets}
              selectedHouse={selectedHouse}
              selectedPlanet={selectedPlanet}
              onSelectHouse={(hNum) => setSelectedHouse(hNum)}
              onSelectPlanet={(pName) => setSelectedPlanet(pName)}
            />
          )}
        </div>

        {/* Right: Divisional Planetary Placements & Vargottama Comparison Table */}
        <div className="lg:col-span-7 overflow-x-auto rounded-xl border border-cosmic-800">
          <table className="w-full text-left text-xs">
            <thead className="bg-cosmic-950 text-slate-400 border-b border-cosmic-800">
              <tr>
                <th className="py-2.5 px-3">Graha</th>
                <th className="py-2.5 px-3">D1 Sign (House)</th>
                <th className="py-2.5 px-3">{activeVarga.vargaCode} Sign</th>
                <th className="py-2.5 px-3">{activeVarga.vargaCode} House</th>
                <th className="py-2.5 px-3">Division Part</th>
                <th className="py-2.5 px-3">{activeVarga.vargaCode} Dignity</th>
                {activeVarga.vargaCode === 'D60' && (
                  <th className="py-2.5 px-3">D60 Shashtiamsha Deity</th>
                )}
              </tr>
            </thead>
            <tbody className="divide-y divide-cosmic-800/70">
              {activeVarga.planets.map((p) => {
                const isSelected = selectedPlanet === p.planet;
                return (
                  <tr
                    key={p.planet}
                    onClick={() => setSelectedPlanet(p.planet)}
                    className={`cursor-pointer transition-colors ${
                      isSelected ? 'bg-cosmic-gold/15' : 'hover:bg-cosmic-800/60'
                    }`}
                  >
                    <td className="py-2.5 px-3 font-bold text-white flex items-center gap-2">
                      <span
                        className="w-2.5 h-2.5 rounded-full shrink-0"
                        style={{ backgroundColor: PLANET_COLORS[p.planet] }}
                      />
                      <span>{p.planet}</span>
                      {p.isVargottama && (
                        <span className="px-1.5 py-0.5 rounded bg-emerald-500/20 text-emerald-300 border border-emerald-500/30 text-[9px] font-bold">
                          VARGOTTAMA
                        </span>
                      )}
                    </td>
                    <td className="py-2.5 px-3 text-slate-400">
                      {p.d1Sign} (H{p.d1House})
                    </td>
                    <td className="py-2.5 px-3 font-semibold text-white">
                      {p.vargaSign} ({p.vargaSanskritSign})
                    </td>
                    <td className="py-2.5 px-3 font-bold text-cosmic-gold">H{p.vargaHouse}</td>
                    <td className="py-2.5 px-3 font-mono text-slate-300">
                      #{p.partNumber} / {activeVarga.divisionNumber}
                    </td>
                    <td className="py-2.5 px-3">
                      <span
                        className={`px-2 py-0.5 rounded-full text-[10px] font-semibold border ${getDignityBadgeStyle(
                          p.dignityInVarga,
                        )}`}
                      >
                        {p.dignityInVarga.replace('_', ' ')}
                      </span>
                    </td>
                    {activeVarga.vargaCode === 'D60' && (
                      <td className="py-2.5 px-3">
                        <span
                          className={`inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[10px] font-semibold border ${
                            p.shashtiamshaQuality === 'BENEFIC'
                              ? 'bg-emerald-500/15 text-emerald-300 border-emerald-500/30'
                              : 'bg-rose-500/15 text-rose-300 border-rose-500/30'
                          }`}
                        >
                          <Sparkles className="w-2.5 h-2.5" />
                          <span>
                            {p.shashtiamshaName} ({p.shashtiamshaQuality})
                          </span>
                        </span>
                      </td>
                    )}
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

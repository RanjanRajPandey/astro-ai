import { Orbit, Flame, RotateCcw, Star } from 'lucide-react';
import type { PlanetPosition } from '../../types/astrology';
import { PLANET_COLORS, getDignityBadgeStyle } from '../../utils/chartMath';

interface PlanetInspectorDrawerProps {
  planet: PlanetPosition;
}

export function PlanetInspectorDrawer({ planet }: PlanetInspectorDrawerProps) {
  const relationshipEntries = Object.entries(planet.planetaryRelationships || {});

  return (
    <div className="rounded-xl bg-cosmic-900/90 border border-cosmic-700 p-5 shadow-xl space-y-5">
      <div className="flex items-start justify-between gap-3 border-b border-cosmic-700/70 pb-4">
        <div className="flex items-center gap-3">
          <div
            className="w-10 h-10 rounded-lg flex items-center justify-center font-bold text-sm border"
            style={{
              borderColor: PLANET_COLORS[planet.planet],
              color: PLANET_COLORS[planet.planet],
              backgroundColor: 'rgba(13, 17, 36, 0.9)',
            }}
          >
            {planet.planet.slice(0, 2).toUpperCase()}
          </div>
          <div>
            <h3 className="text-lg font-bold text-white flex items-center gap-2">
              {planet.planet}
              <span
                className={`text-[11px] px-2.5 py-0.5 rounded-full border font-semibold ${getDignityBadgeStyle(
                  planet.dignity,
                )}`}
              >
                {planet.dignity.replace('_', ' ')}
              </span>
            </h3>
            <p className="text-xs text-slate-400">
              {planet.sign} ({planet.sanskritSign}) • {planet.degreeDms} • House {planet.house}
            </p>
          </div>
        </div>
      </div>

      {/* Key Astronomical & Nakshatra Metrics */}
      <div className="grid grid-cols-2 sm:grid-cols-3 gap-3 text-xs">
        <div className="p-3 rounded-lg bg-cosmic-950/70 border border-cosmic-800">
          <span className="text-slate-400 block mb-1">Sidereal Longitude</span>
          <span className="text-white font-semibold font-mono">{planet.longitude.toFixed(4)}°</span>
        </div>
        <div className="p-3 rounded-lg bg-cosmic-950/70 border border-cosmic-800">
          <span className="text-slate-400 block mb-1">Nakshatra &amp; Pada</span>
          <span className="text-cosmic-gold font-semibold">
            {planet.nakshatra} (Pada {planet.pada})
          </span>
        </div>
        <div className="p-3 rounded-lg bg-cosmic-950/70 border border-cosmic-800">
          <span className="text-slate-400 block mb-1">Nakshatra Ruler</span>
          <span className="text-white font-semibold">{planet.nakshatraLord}</span>
        </div>
        <div className="p-3 rounded-lg bg-cosmic-950/70 border border-cosmic-800">
          <span className="text-slate-400 block mb-1">Sign Dispositor</span>
          <span className="text-white font-semibold">
            {planet.signLord} ({planet.dispositorRelationship.replace('_', ' ')})
          </span>
        </div>
        <div className="p-3 rounded-lg bg-cosmic-950/70 border border-cosmic-800">
          <span className="text-slate-400 block mb-1">Motion Status</span>
          <span className="text-white font-semibold inline-flex items-center gap-1">
            {planet.retrograde ? (
              <>
                <RotateCcw className="w-3.5 h-3.5 text-amber-400" />
                <span className="text-amber-300">Retrograde (Vakri)</span>
              </>
            ) : (
              <>
                <Orbit className="w-3.5 h-3.5 text-emerald-400" />
                <span>Direct ({planet.speedLongitude.toFixed(2)}°/d)</span>
              </>
            )}
          </span>
        </div>
        <div className="p-3 rounded-lg bg-cosmic-950/70 border border-cosmic-800">
          <span className="text-slate-400 block mb-1">Combustion (Asta)</span>
          <span className="text-white font-semibold inline-flex items-center gap-1">
            {planet.combust ? (
              <>
                <Flame className="w-3.5 h-3.5 text-rose-400" />
                <span className="text-rose-300">
                  Combust ({planet.angularDistanceFromSun?.toFixed(1)}° from Sun)
                </span>
              </>
            ) : (
              <>
                <Star className="w-3.5 h-3.5 text-sky-400" />
                <span>Not Combust</span>
              </>
            )}
          </span>
        </div>
      </div>

      {/* Panchadha Maitri (5-Fold Planetary Relationship Matrix) */}
      <div>
        <h4 className="text-xs font-semibold uppercase tracking-wider text-slate-400 mb-2.5">
          Panchadha Maitri (5-Fold Compound Relationships)
        </h4>
        <div className="overflow-x-auto rounded-lg border border-cosmic-800">
          <table className="w-full text-left text-xs">
            <thead className="bg-cosmic-950/90 text-slate-400 border-b border-cosmic-800">
              <tr>
                <th className="py-2 px-3">Graha</th>
                <th className="py-2 px-3">Natural (Naisargika)</th>
                <th className="py-2 px-3">Temporal (Tatkalika)</th>
                <th className="py-2 px-3">Compound (Panchadha)</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-cosmic-800/60 bg-cosmic-950/40">
              {relationshipEntries.map(([otherPlanet, rel]) => (
                <tr key={otherPlanet}>
                  <td className="py-1.5 px-3 font-medium text-white">{otherPlanet}</td>
                  <td className="py-1.5 px-3 text-slate-300">{rel.natural}</td>
                  <td className="py-1.5 px-3 text-slate-300">{rel.temporal}</td>
                  <td className="py-1.5 px-3">
                    <span
                      className={`px-2 py-0.5 rounded text-[10px] font-semibold border ${getDignityBadgeStyle(
                        rel.compound,
                      )}`}
                    >
                      {rel.compound.replace('_', ' ')}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}

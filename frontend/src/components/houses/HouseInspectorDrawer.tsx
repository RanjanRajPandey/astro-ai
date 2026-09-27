import { Eye, Layers, Shield, Sparkles } from 'lucide-react';
import type { GrahaName, HouseDetail } from '../../types/astrology';
import { getDignityBadgeStyle } from '../../utils/chartMath';

interface HouseInspectorDrawerProps {
  house: HouseDetail;
  onSelectPlanet: (planet: GrahaName) => void;
}

export function HouseInspectorDrawer({ house, onSelectPlanet }: HouseInspectorDrawerProps) {
  const gradeColor =
    house.strengthGrade === 'STRONG'
      ? 'text-emerald-300 border-emerald-500/40 bg-emerald-500/15'
      : house.strengthGrade === 'MODERATE'
      ? 'text-amber-300 border-amber-500/40 bg-amber-500/15'
      : 'text-rose-300 border-rose-500/40 bg-rose-500/15';

  return (
    <div className="rounded-xl bg-cosmic-900/90 border border-cosmic-700 p-5 shadow-xl space-y-5">
      <div className="flex items-start justify-between gap-3 border-b border-cosmic-700/70 pb-4">
        <div>
          <div className="flex items-center gap-2.5">
            <span className="px-2.5 py-1 rounded-md bg-cosmic-gold/20 border border-cosmic-gold/40 text-cosmic-gold font-bold text-xs">
              House {house.houseNumber}
            </span>
            <h3 className="text-lg font-bold text-white">
              {house.sign} ({house.sanskritSign})
            </h3>
            <span className={`text-[11px] px-2.5 py-0.5 rounded-full border font-semibold ${gradeColor}`}>
              {house.strengthGrade} ({house.baselineStrengthScore.toFixed(0)}/100)
            </span>
          </div>
          <p className="text-xs text-slate-400 mt-1">
            Purushartha: <strong className="text-slate-200">{house.purushartha}</strong> •{' '}
            {house.classifications.join(' • ')}
          </p>
        </div>
      </div>

      {/* House Lordship & Sripati Cusps */}
      <div className="grid grid-cols-2 sm:grid-cols-3 gap-3 text-xs">
        <div className="p-3 rounded-lg bg-cosmic-950/70 border border-cosmic-800">
          <span className="text-slate-400 block mb-1">House Lord (Bhavesha)</span>
          <button
            type="button"
            onClick={() => onSelectPlanet(house.lordPlanet)}
            className="text-cosmic-gold hover:underline font-semibold"
          >
            {house.lordPlanet} (in H{house.lordPlacedInHouse} {house.lordPlacedInSign})
          </button>
        </div>
        <div className="p-3 rounded-lg bg-cosmic-950/70 border border-cosmic-800">
          <span className="text-slate-400 block mb-1">Lord Dignity</span>
          <span
            className={`inline-block px-2 py-0.5 rounded text-[10px] font-semibold border ${getDignityBadgeStyle(
              house.lordDignity,
            )}`}
          >
            {house.lordDignity.replace('_', ' ')}
          </span>
        </div>
        <div className="p-3 rounded-lg bg-cosmic-950/70 border border-cosmic-800">
          <span className="text-slate-400 block mb-1">Sripati Bhava Madhya</span>
          <span className="text-white font-mono font-semibold">
            {house.sripatiCuspLongitude.toFixed(2)}° ({house.degreeCusp.toFixed(2)}° in sign)
          </span>
        </div>
      </div>

      {/* Occupants (Whole-Sign vs Sripati Chalit) */}
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 text-xs">
        <div className="p-3.5 rounded-lg bg-cosmic-950/60 border border-cosmic-800">
          <div className="flex items-center gap-1.5 text-slate-400 font-semibold mb-2">
            <Layers className="w-3.5 h-3.5 text-cosmic-gold" />
            <span>Rashi (Whole Sign) Occupants</span>
          </div>
          {house.occupants.length === 0 ? (
            <span className="text-slate-500 italic">Unoccupied (Empty Bhava)</span>
          ) : (
            <div className="flex flex-wrap gap-1.5">
              {house.occupants.map((p) => (
                <button
                  key={p}
                  type="button"
                  onClick={() => onSelectPlanet(p)}
                  className="px-2.5 py-1 rounded bg-cosmic-800 hover:bg-cosmic-700 border border-cosmic-gold/30 text-white font-semibold"
                >
                  {p}
                </button>
              ))}
            </div>
          )}
        </div>

        <div className="p-3.5 rounded-lg bg-cosmic-950/60 border border-cosmic-800">
          <div className="flex items-center gap-1.5 text-slate-400 font-semibold mb-2">
            <Shield className="w-3.5 h-3.5 text-sky-400" />
            <span>Sripati Chalit Cusp Occupants</span>
          </div>
          {house.chalitOccupants.length === 0 ? (
            <span className="text-slate-500 italic">None in Sripati arc</span>
          ) : (
            <div className="flex flex-wrap gap-1.5">
              {house.chalitOccupants.map((p) => (
                <button
                  key={p}
                  type="button"
                  onClick={() => onSelectPlanet(p)}
                  className="px-2.5 py-1 rounded bg-cosmic-800 hover:bg-cosmic-700 border border-sky-500/30 text-white font-semibold"
                >
                  {p}
                </button>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* Parashari Graha Drishti (Aspects Received) */}
      <div>
        <div className="flex items-center gap-1.5 text-xs font-semibold uppercase tracking-wider text-slate-400 mb-2">
          <Eye className="w-3.5 h-3.5 text-cosmic-gold" />
          <span>Parashari Aspects (Drishti) Received ({house.aspectsReceived.length})</span>
        </div>
        {house.aspectsReceived.length === 0 ? (
          <p className="text-xs text-slate-500 italic">No full planetary aspects received on this house.</p>
        ) : (
          <div className="space-y-1.5">
            {house.aspectsReceived.map((asp, i) => (
              <div
                key={`${asp.sourcePlanet}-${i}`}
                className="p-2.5 rounded-lg bg-cosmic-950/60 border border-cosmic-800 text-xs flex items-center justify-between gap-2"
              >
                <div>
                  <button
                    type="button"
                    onClick={() => onSelectPlanet(asp.sourcePlanet)}
                    className="font-semibold text-cosmic-gold hover:underline"
                  >
                    {asp.sourcePlanet}
                  </button>{' '}
                  <span className="text-slate-300">
                    from House {asp.sourceHouse} ({asp.sourceSign})
                  </span>
                </div>
                <span className="px-2 py-0.5 rounded bg-cosmic-800 text-slate-300 font-mono text-[10px]">
                  {asp.aspectType.replace(/_/g, ' ')}
                </span>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Significations */}
      <div>
        <div className="flex items-center gap-1.5 text-xs font-semibold uppercase tracking-wider text-slate-400 mb-2">
          <Sparkles className="w-3.5 h-3.5 text-cosmic-gold" />
          <span>Classical Bhava Significations</span>
        </div>
        <div className="flex flex-wrap gap-1.5">
          {house.significations.map((sig) => (
            <span
              key={sig}
              className="px-2.5 py-1 rounded-full bg-cosmic-950 border border-cosmic-800 text-xs text-slate-300"
            >
              {sig}
            </span>
          ))}
        </div>
      </div>
    </div>
  );
}

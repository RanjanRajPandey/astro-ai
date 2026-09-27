import { useEffect, useState } from 'react';
import {
  Compass,
  ShieldCheck,
  Plus,
  Sparkles,
  MapPin,
  Clock,
  AlertTriangle,
  LayoutGrid,
  Diamond,
  Sun,
  Moon,
} from 'lucide-react';
import type {
  BirthProfile,
  CreateBirthProfilePayload,
  GrahaName,
  KundliChartResponse,
} from './types/astrology';
import { createBirthProfile, getD1Chart, listBirthProfiles } from './services/api';
import { NorthIndianChart } from './components/charts/NorthIndianChart';
import { SouthIndianChart } from './components/charts/SouthIndianChart';
import { PlanetInspectorDrawer } from './components/planets/PlanetInspectorDrawer';
import { HouseInspectorDrawer } from './components/houses/HouseInspectorDrawer';
import { BirthProfileFormModal } from './components/kundli/BirthProfileFormModal';
import { PLANET_COLORS, getDignityBadgeStyle } from './utils/chartMath';

export function App() {
  const [profiles, setProfiles] = useState<BirthProfile[]>([]);
  const [activeProfileId, setActiveProfileId] = useState<string | null>(null);
  const [chartData, setChartData] = useState<KundliChartResponse | null>(null);
  const [chartStyle, setChartStyle] = useState<'NORTH' | 'SOUTH'>('NORTH');
  const [selectedPlanet, setSelectedPlanet] = useState<GrahaName | null>('Sun');
  const [selectedHouse, setSelectedHouse] = useState<number | null>(null);
  const [inspectorTab, setInspectorTab] = useState<'PLANET' | 'HOUSE'>('PLANET');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    async function bootstrap() {
      setLoading(true);
      setError(null);
      try {
        let existing = await listBirthProfiles();
        if (existing.length === 0) {
          const seeded = await createBirthProfile({
            name: 'Aarav Sharma (Reference Kundli)',
            dateOfBirth: '1990-05-15',
            timeOfBirth: '14:30:00',
            placeOfBirth: 'New Delhi',
            gender: 'MALE',
          });
          existing = [seeded];
        }
        setProfiles(existing);
        setActiveProfileId(existing[0].id);
      } catch (err: any) {
        setError(
          'Backend API is not currently reachable on port 8080. Start the backend server or click "+ New Profile" once running.',
        );
      } finally {
        setLoading(false);
      }
    }
    bootstrap();
  }, []);

  useEffect(() => {
    if (!activeProfileId) return;
    let active = true;
    setLoading(true);
    setError(null);
    getD1Chart(activeProfileId)
      .then((res) => {
        if (!active) return;
        setChartData(res);
        setSelectedPlanet('Sun');
        setSelectedHouse(1);
      })
      .catch((err: any) => {
        if (!active) return;
        setError(err?.response?.data?.message || 'Failed to load D1 Kundli chart.');
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => {
      active = false;
    };
  }, [activeProfileId]);

  const handleCreateProfile = async (payload: CreateBirthProfilePayload) => {
    const created = await createBirthProfile(payload);
    setProfiles((prev) => [created, ...prev]);
    setActiveProfileId(created.id);
  };

  const activePlanetObj =
    chartData?.planets.find((p) => p.planet === selectedPlanet) || chartData?.planets[0] || null;
  const activeHouseObj =
    chartData?.houses.find((h) => h.houseNumber === (selectedHouse || 1)) ||
    chartData?.houses[0] ||
    null;

  return (
    <div className="min-h-screen bg-cosmic-950 text-slate-100 flex flex-col">
      {/* Top Navigation Bar */}
      <header className="sticky top-0 z-30 border-b border-cosmic-700/60 bg-cosmic-900/90 backdrop-blur px-6 py-3.5 flex flex-wrap items-center justify-between gap-4">
        <div className="flex items-center gap-3">
          <Compass className="w-7 h-7 text-cosmic-gold" />
          <div>
            <h1 className="text-base font-bold tracking-wider text-white flex items-center gap-2">
              ASTRO-AI
              <span className="text-[10px] font-semibold px-2 py-0.5 rounded bg-cosmic-800 text-cosmic-gold border border-cosmic-gold/30">
                SWISS EPHEMERIS • LAHIRI
              </span>
            </h1>
            <p className="text-xs text-slate-400">
              Deterministic Vedic Astrology &amp; Traceable AI Kundli Platform
            </p>
          </div>
        </div>

        <div className="flex items-center gap-3">
          {profiles.length > 0 && (
            <select
              value={activeProfileId || ''}
              onChange={(e) => setActiveProfileId(e.target.value)}
              aria-label="Select Saved Birth Profile"
              className="px-3 py-1.5 rounded-lg bg-cosmic-950 border border-cosmic-700 text-xs text-white focus:border-cosmic-gold focus:outline-none"
            >
              {profiles.map((p) => (
                <option key={p.id} value={p.id}>
                  {p.name} ({p.dateOfBirth} • {p.placeOfBirth})
                </option>
              ))}
            </select>
          )}

          <button
            type="button"
            onClick={() => setIsModalOpen(true)}
            className="inline-flex items-center gap-1.5 px-3.5 py-1.5 rounded-lg bg-cosmic-gold text-cosmic-950 hover:bg-amber-400 font-bold text-xs transition-colors"
          >
            <Plus className="w-4 h-4" />
            <span>New Birth Profile</span>
          </button>

          <div className="hidden md:flex items-center gap-1.5 text-xs px-3 py-1.5 rounded-full bg-cosmic-800 border border-cosmic-700 text-slate-300">
            <ShieldCheck className="w-3.5 h-3.5 text-emerald-400" />
            <span>Zero-LLM Calculation Engine</span>
          </div>
        </div>
      </header>

      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 py-6 space-y-6">
        {error && (
          <div className="p-4 rounded-xl bg-amber-500/10 border border-amber-500/30 text-amber-200 text-xs flex items-center gap-3">
            <AlertTriangle className="w-5 h-5 text-amber-400 shrink-0" />
            <span>{error}</span>
          </div>
        )}

        {loading && !chartData && (
          <div className="py-20 text-center space-y-3">
            <Sparkles className="w-8 h-8 text-cosmic-gold animate-spin mx-auto" />
            <p className="text-sm text-slate-300 font-medium">
              Computing Sidereal Ephemeris, Ascendant, Sripati Cusps &amp; D1 Rashi Chart...
            </p>
          </div>
        )}

        {chartData && (
          <>
            {/* Birth Profile & Astrological Pillars Summary Banner */}
            <section className="rounded-2xl bg-cosmic-900/80 border border-cosmic-700/80 p-5 shadow-xl space-y-4">
              <div className="flex flex-wrap items-center justify-between gap-4 border-b border-cosmic-800 pb-4">
                <div>
                  <h2 className="text-xl font-bold text-white">{chartData.birthProfile.name}</h2>
                  <div className="flex flex-wrap items-center gap-4 text-xs text-slate-400 mt-1">
                    <span className="inline-flex items-center gap-1">
                      <Clock className="w-3.5 h-3.5 text-cosmic-gold" />
                      {chartData.birthProfile.dateOfBirth} •{' '}
                      {chartData.birthProfile.timeOfBirth || '12:00:00 (Time Unknown)'} (
                      {chartData.birthProfile.timezone}, UTC
                      {chartData.birthProfile.utcOffsetHours >= 0
                        ? `+${chartData.birthProfile.utcOffsetHours}`
                        : chartData.birthProfile.utcOffsetHours}
                      h)
                    </span>
                    <span className="inline-flex items-center gap-1">
                      <MapPin className="w-3.5 h-3.5 text-cosmic-gold" />
                      {chartData.birthProfile.placeOfBirth} (
                      {chartData.birthProfile.latitude.toFixed(4)}°N,{' '}
                      {chartData.birthProfile.longitude.toFixed(4)}°E)
                    </span>
                  </div>
                </div>
                <div className="text-right text-xs text-slate-400">
                  <div>
                    Ayanamsha:{' '}
                    <strong className="text-white">
                      {chartData.ayanamshaType} ({chartData.ayanamshaValue.toFixed(4)}°)
                    </strong>
                  </div>
                  <div>
                    UTC Timestamp:{' '}
                    <span className="font-mono text-slate-300">
                      {chartData.birthProfile.utcBirthTime}
                    </span>
                  </div>
                </div>
              </div>

              {chartData.birthProfile.calculationWarnings.length > 0 && (
                <div className="p-3 rounded-lg bg-amber-500/10 border border-amber-500/30 text-amber-200 text-xs flex items-start gap-2">
                  <AlertTriangle className="w-4 h-4 text-amber-400 shrink-0 mt-0.5" />
                  <div>
                    {chartData.birthProfile.calculationWarnings.map((w, idx) => (
                      <p key={idx}>{w}</p>
                    ))}
                  </div>
                </div>
              )}

              {/* Key Vedic Lagna & Nakshatra Pillars */}
              <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-3 text-xs">
                <div className="p-3 rounded-xl bg-cosmic-950/80 border border-cosmic-800">
                  <span className="text-slate-400 block mb-1">Ascendant (Udaya Lagna)</span>
                  <span className="text-cosmic-gold font-bold text-sm">
                    {chartData.ascendant.sign} ({chartData.ascendant.sanskritSign})
                  </span>
                  <span className="text-slate-400 block text-[11px] mt-0.5">
                    {chartData.ascendant.degreeDms} • Lord: {chartData.ascendant.lagnaLord}
                  </span>
                </div>

                <div className="p-3 rounded-xl bg-cosmic-950/80 border border-cosmic-800">
                  <span className="text-slate-400 block mb-1 flex items-center gap-1">
                    <Moon className="w-3 h-3 text-sky-300" /> Moon Sign (Rashi)
                  </span>
                  <span className="text-white font-bold text-sm">
                    {chartData.moonSign} ({chartData.moonSanskritSign})
                  </span>
                  <span className="text-slate-400 block text-[11px] mt-0.5">Chandra Lagna</span>
                </div>

                <div className="p-3 rounded-xl bg-cosmic-950/80 border border-cosmic-800">
                  <span className="text-slate-400 block mb-1">Janma Nakshatra</span>
                  <span className="text-emerald-300 font-bold text-sm">
                    {chartData.moonNakshatra} (Pada {chartData.moonPada})
                  </span>
                  <span className="text-slate-400 block text-[11px] mt-0.5">
                    Lagna Nak: {chartData.ascendant.nakshatra} P{chartData.ascendant.pada}
                  </span>
                </div>

                <div className="p-3 rounded-xl bg-cosmic-950/80 border border-cosmic-800">
                  <span className="text-slate-400 block mb-1 flex items-center gap-1">
                    <Sun className="w-3 h-3 text-amber-400" /> Sun Sign (Surya Rashi)
                  </span>
                  <span className="text-white font-bold text-sm">{chartData.sunSign}</span>
                  <span className="text-slate-400 block text-[11px] mt-0.5">Sidereal Nirayana</span>
                </div>

                <div className="p-3 rounded-xl bg-cosmic-950/80 border border-cosmic-800">
                  <span className="text-slate-400 block mb-1">Arudha Lagna (AL)</span>
                  <span className="text-white font-bold text-sm">
                    {chartData.ascendant.arudhaLagnaSign}
                  </span>
                  <span className="text-slate-400 block text-[11px] mt-0.5">
                    House {chartData.ascendant.arudhaLagnaHouse} from Lagna
                  </span>
                </div>

                <div className="p-3 rounded-xl bg-cosmic-950/80 border border-cosmic-800">
                  <span className="text-slate-400 block mb-1">Upapada Lagna (UL)</span>
                  <span className="text-white font-bold text-sm">
                    {chartData.ascendant.upapadaLagnaSign}
                  </span>
                  <span className="text-slate-400 block text-[11px] mt-0.5">
                    House {chartData.ascendant.upapadaLagnaHouse} (12th Pada)
                  </span>
                </div>
              </div>
            </section>

            {/* Main Interactive Split View: Left = Kundli Chart | Right = Contextual Planet/House Inspector */}
            <section className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
              {/* Left Column: Interactive D1 Kundli SVG */}
              <div className="lg:col-span-6 rounded-2xl bg-cosmic-900/80 border border-cosmic-700 p-5 shadow-xl space-y-4">
                <div className="flex items-center justify-between gap-2">
                  <div>
                    <h3 className="text-base font-bold text-white">{chartData.vargaName}</h3>
                    <p className="text-xs text-slate-400">
                      Click any <strong>Planet</strong> or <strong>House</strong> chamber to inspect
                      rules &amp; strength
                    </p>
                  </div>

                  {/* North Indian / South Indian Chart Style Toggle */}
                  <div className="inline-flex rounded-lg bg-cosmic-950 p-1 border border-cosmic-800 text-xs">
                    <button
                      type="button"
                      onClick={() => setChartStyle('NORTH')}
                      className={`inline-flex items-center gap-1.5 px-3 py-1.5 rounded-md font-semibold transition-colors ${
                        chartStyle === 'NORTH'
                          ? 'bg-cosmic-gold text-cosmic-950'
                          : 'text-slate-400 hover:text-white'
                      }`}
                    >
                      <Diamond className="w-3.5 h-3.5" />
                      <span>North Indian</span>
                    </button>
                    <button
                      type="button"
                      onClick={() => setChartStyle('SOUTH')}
                      className={`inline-flex items-center gap-1.5 px-3 py-1.5 rounded-md font-semibold transition-colors ${
                        chartStyle === 'SOUTH'
                          ? 'bg-cosmic-gold text-cosmic-950'
                          : 'text-slate-400 hover:text-white'
                      }`}
                    >
                      <LayoutGrid className="w-3.5 h-3.5" />
                      <span>South Indian</span>
                    </button>
                  </div>
                </div>

                {chartStyle === 'NORTH' ? (
                  <NorthIndianChart
                    ascendantSignIndex={chartData.ascendant.signIndex}
                    planets={chartData.planets}
                    houses={chartData.houses}
                    selectedHouse={inspectorTab === 'HOUSE' ? selectedHouse : null}
                    selectedPlanet={inspectorTab === 'PLANET' ? selectedPlanet : null}
                    onSelectHouse={(hNum) => {
                      setSelectedHouse(hNum);
                      setInspectorTab('HOUSE');
                    }}
                    onSelectPlanet={(pName) => {
                      setSelectedPlanet(pName);
                      setInspectorTab('PLANET');
                    }}
                  />
                ) : (
                  <SouthIndianChart
                    ascendantSignIndex={chartData.ascendant.signIndex}
                    vargaTitle="D1 RASHI KUNDLI"
                    planets={chartData.planets}
                    selectedHouse={inspectorTab === 'HOUSE' ? selectedHouse : null}
                    selectedPlanet={inspectorTab === 'PLANET' ? selectedPlanet : null}
                    onSelectHouse={(hNum) => {
                      setSelectedHouse(hNum);
                      setInspectorTab('HOUSE');
                    }}
                    onSelectPlanet={(pName) => {
                      setSelectedPlanet(pName);
                      setInspectorTab('PLANET');
                    }}
                  />
                )}
              </div>

              {/* Right Column: Contextual Planet / House Inspector */}
              <div className="lg:col-span-6 space-y-4">
                <div className="flex items-center justify-between gap-2 bg-cosmic-900/80 border border-cosmic-700 rounded-xl p-2">
                  <div className="flex gap-1.5">
                    <button
                      type="button"
                      onClick={() => setInspectorTab('PLANET')}
                      className={`px-4 py-1.5 rounded-lg text-xs font-bold transition-colors ${
                        inspectorTab === 'PLANET'
                          ? 'bg-cosmic-gold text-cosmic-950'
                          : 'text-slate-400 hover:text-white'
                      }`}
                    >
                      Planet Inspector ({selectedPlanet})
                    </button>
                    <button
                      type="button"
                      onClick={() => setInspectorTab('HOUSE')}
                      className={`px-4 py-1.5 rounded-lg text-xs font-bold transition-colors ${
                        inspectorTab === 'HOUSE'
                          ? 'bg-cosmic-gold text-cosmic-950'
                          : 'text-slate-400 hover:text-white'
                      }`}
                    >
                      House Inspector (House {selectedHouse || 1})
                    </button>
                  </div>
                </div>

                {inspectorTab === 'PLANET' && activePlanetObj && (
                  <PlanetInspectorDrawer planet={activePlanetObj} />
                )}

                {inspectorTab === 'HOUSE' && activeHouseObj && (
                  <HouseInspectorDrawer
                    house={activeHouseObj}
                    onSelectPlanet={(pName) => {
                      setSelectedPlanet(pName);
                      setInspectorTab('PLANET');
                    }}
                  />
                )}
              </div>
            </section>

            {/* Complete 9-Graha Sidereal Planetary Positions Table */}
            <section className="rounded-2xl bg-cosmic-900/80 border border-cosmic-700 p-5 shadow-xl space-y-3">
              <h3 className="text-base font-bold text-white">
                Sidereal Planetary Positions (Navagraha Table)
              </h3>
              <div className="overflow-x-auto rounded-xl border border-cosmic-800">
                <table className="w-full text-left text-xs">
                  <thead className="bg-cosmic-950 text-slate-400 border-b border-cosmic-800">
                    <tr>
                      <th className="py-2.5 px-3">Graha</th>
                      <th className="py-2.5 px-3">Rashi (Sign)</th>
                      <th className="py-2.5 px-3">Degree (DMS)</th>
                      <th className="py-2.5 px-3">House</th>
                      <th className="py-2.5 px-3">Nakshatra (Pada)</th>
                      <th className="py-2.5 px-3">Nak. Lord</th>
                      <th className="py-2.5 px-3">Status</th>
                      <th className="py-2.5 px-3">Dignity</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-cosmic-800/70">
                    {chartData.planets.map((p) => {
                      const isRowSelected =
                        inspectorTab === 'PLANET' && selectedPlanet === p.planet;
                      return (
                        <tr
                          key={p.planet}
                          onClick={() => {
                            setSelectedPlanet(p.planet);
                            setInspectorTab('PLANET');
                          }}
                          className={`cursor-pointer transition-colors ${
                            isRowSelected ? 'bg-cosmic-gold/15' : 'hover:bg-cosmic-800/60'
                          }`}
                        >
                          <td className="py-2.5 px-3 font-bold flex items-center gap-2">
                            <span
                              className="w-2.5 h-2.5 rounded-full"
                              style={{ backgroundColor: PLANET_COLORS[p.planet] }}
                            />
                            <span className="text-white">{p.planet}</span>
                          </td>
                          <td className="py-2.5 px-3 text-slate-200">
                            {p.sign} ({p.sanskritSign})
                          </td>
                          <td className="py-2.5 px-3 font-mono text-slate-200">{p.degreeDms}</td>
                          <td className="py-2.5 px-3 font-semibold text-cosmic-gold">
                            H{p.house}
                          </td>
                          <td className="py-2.5 px-3 text-slate-200">
                            {p.nakshatra} (P{p.pada})
                          </td>
                          <td className="py-2.5 px-3 text-slate-300">{p.nakshatraLord}</td>
                          <td className="py-2.5 px-3 space-x-1">
                            {p.retrograde && (
                              <span className="px-1.5 py-0.5 rounded bg-amber-500/20 text-amber-300 text-[10px] font-semibold">
                                Retro
                              </span>
                            )}
                            {p.combust && (
                              <span className="px-1.5 py-0.5 rounded bg-rose-500/20 text-rose-300 text-[10px] font-semibold">
                                Combust
                              </span>
                            )}
                            {!p.retrograde && !p.combust && (
                              <span className="text-slate-500">Direct</span>
                            )}
                          </td>
                          <td className="py-2.5 px-3">
                            <span
                              className={`px-2 py-0.5 rounded-full text-[10px] font-semibold border ${getDignityBadgeStyle(
                                p.dignity,
                              )}`}
                            >
                              {p.dignity.replace('_', ' ')}
                            </span>
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>
            </section>

            {/* 12-Bhava House Overview Grid */}
            <section className="rounded-2xl bg-cosmic-900/80 border border-cosmic-700 p-5 shadow-xl space-y-3">
              <h3 className="text-base font-bold text-white">12-Bhava (House) Summary Grid</h3>
              <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-3">
                {chartData.houses.map((h) => {
                  const isCardSelected =
                    inspectorTab === 'HOUSE' && selectedHouse === h.houseNumber;
                  return (
                    <div
                      key={h.houseNumber}
                      onClick={() => {
                        setSelectedHouse(h.houseNumber);
                        setInspectorTab('HOUSE');
                      }}
                      className={`p-3.5 rounded-xl border cursor-pointer transition-all ${
                        isCardSelected
                          ? 'bg-cosmic-gold/15 border-cosmic-gold shadow-lg'
                          : 'bg-cosmic-950/70 border-cosmic-800 hover:border-cosmic-700'
                      }`}
                    >
                      <div className="flex items-center justify-between mb-1.5">
                        <span className="text-xs font-bold text-cosmic-gold">
                          House {h.houseNumber} • {h.sign}
                        </span>
                        <span className="text-[10px] px-2 py-0.5 rounded bg-cosmic-800 text-slate-300 font-semibold">
                          {h.purushartha}
                        </span>
                      </div>
                      <p className="text-xs text-slate-400">
                        Lord: <strong className="text-white">{h.lordPlanet}</strong> (in H
                        {h.lordPlacedInHouse})
                      </p>
                      <p className="text-xs text-slate-400 mt-1">
                        Occupants:{' '}
                        <strong className="text-slate-200">
                          {h.occupants.length > 0 ? h.occupants.join(', ') : 'None'}
                        </strong>
                      </p>
                    </div>
                  );
                })}
              </div>
            </section>
          </>
        )}
      </main>

      <BirthProfileFormModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onSubmit={handleCreateProfile}
      />
    </div>
  );
}

export default App;

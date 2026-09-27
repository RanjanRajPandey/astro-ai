import React, { useState, useEffect } from 'react';
import { X, MapPin, Calendar, Clock, User, Globe, SlidersHorizontal } from 'lucide-react';
import type { CreateBirthProfilePayload, GazetteerCity } from '../../types/astrology';
import { searchCities } from '../../services/api';

interface BirthProfileFormModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (payload: CreateBirthProfilePayload) => Promise<void>;
}

export function BirthProfileFormModal({
  isOpen,
  onClose,
  onSubmit,
}: BirthProfileFormModalProps) {
  const [name, setName] = useState('');
  const [dateOfBirth, setDateOfBirth] = useState('1995-08-24');
  const [timeOfBirth, setTimeOfBirth] = useState('10:30:00');
  const [timeUnknown, setTimeUnknown] = useState(false);
  const [placeOfBirth, setPlaceOfBirth] = useState('New Delhi');
  const [gender, setGender] = useState('MALE');
  const [citySuggestions, setCitySuggestions] = useState<GazetteerCity[]>([]);
  const [selectedCity, setSelectedCity] = useState<GazetteerCity | null>(null);
  const [searchingLocation, setSearchingLocation] = useState(false);

  // Manual / Custom Coordinates Mode
  const [useCustomCoordinates, setUseCustomCoordinates] = useState(false);
  const [customLat, setCustomLat] = useState<string>('28.6139');
  const [customLon, setCustomLon] = useState<string>('77.2090');
  const [customTimezone, setCustomTimezone] = useState<string>('Asia/Kolkata');

  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!isOpen) return;
    let active = true;
    const trimmed = placeOfBirth.trim();
    const primaryQuery = trimmed.split(',')[0].trim();

    const timer = setTimeout(async () => {
      setSearchingLocation(true);
      try {
        const res = await searchCities(primaryQuery);
        if (active) {
          setCitySuggestions(res);
        }
      } catch {
        if (active) {
          setCitySuggestions([]);
        }
      } finally {
        if (active) {
          setSearchingLocation(false);
        }
      }
    }, 200);

    return () => {
      active = false;
      clearTimeout(timer);
    };
  }, [placeOfBirth, isOpen]);

  if (!isOpen) return null;

  const handleSelectCity = (c: GazetteerCity) => {
    const label = c.stateOrRegion ? `${c.name}, ${c.stateOrRegion}` : c.name;
    setPlaceOfBirth(label);
    setSelectedCity(c);
    setCustomLat(c.latitude.toFixed(4));
    setCustomLon(c.longitude.toFixed(4));
    setCustomTimezone(c.timezoneId || 'Asia/Kolkata');
  };

  const handleFormSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      const normalizedTime = timeUnknown
        ? null
        : timeOfBirth.length === 5
        ? `${timeOfBirth}:00`
        : timeOfBirth;

      let latToUse = selectedCity?.latitude;
      let lonToUse = selectedCity?.longitude;
      let tzToUse = selectedCity?.timezoneId;

      if (useCustomCoordinates) {
        const parsedLat = parseFloat(customLat);
        const parsedLon = parseFloat(customLon);
        if (Number.isNaN(parsedLat) || parsedLat < -90 || parsedLat > 90) {
          throw new Error('Latitude must be a valid number between -90 and 90.');
        }
        if (Number.isNaN(parsedLon) || parsedLon < -180 || parsedLon > 180) {
          throw new Error('Longitude must be a valid number between -180 and 180.');
        }
        latToUse = parsedLat;
        lonToUse = parsedLon;
        tzToUse = customTimezone.trim() || 'Asia/Kolkata';
      }

      await onSubmit({
        name: name.trim(),
        dateOfBirth,
        timeOfBirth: normalizedTime,
        placeOfBirth: selectedCity
          ? selectedCity.stateOrRegion
            ? `${selectedCity.name}, ${selectedCity.stateOrRegion}`
            : selectedCity.name
          : placeOfBirth.trim(),
        gender,
        latitude: latToUse,
        longitude: lonToUse,
        timezone: tzToUse,
      });
      setName('');
      onClose();
    } catch (err: any) {
      setError(
        err?.response?.data?.message ||
          err?.message ||
          'Failed to create birth profile. Please check inputs.',
      );
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/75 backdrop-blur-sm p-4">
      <div className="w-full max-w-xl rounded-2xl bg-cosmic-900 border border-cosmic-700 shadow-2xl overflow-hidden max-h-[92vh] flex flex-col">
        <div className="flex items-center justify-between px-6 py-4 border-b border-cosmic-800 bg-cosmic-950/60">
          <div>
            <h2 className="text-base font-bold text-white">Create New Vedic Birth Profile</h2>
            <p className="text-[11px] text-slate-400">
              Supports any city, town, district, or village worldwide + manual coordinates
            </p>
          </div>
          <button
            type="button"
            onClick={onClose}
            className="text-slate-400 hover:text-white p-1 rounded-lg"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        <form onSubmit={handleFormSubmit} className="p-6 space-y-4 text-sm overflow-y-auto">
          {error && (
            <div className="p-3 rounded-lg bg-rose-500/15 border border-rose-500/40 text-rose-300 text-xs">
              {error}
            </div>
          )}

          <div>
            <label className="block text-xs font-semibold text-slate-300 mb-1.5">
              Full Name
            </label>
            <div className="relative">
              <User className="w-4 h-4 text-slate-400 absolute left-3 top-2.5" />
              <input
                type="text"
                required
                placeholder="e.g., Aarav Sharma"
                value={name}
                onChange={(e) => setName(e.target.value)}
                className="w-full pl-9 pr-3 py-2 rounded-lg bg-cosmic-950 border border-cosmic-700 text-white focus:border-cosmic-gold focus:outline-none"
              />
            </div>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1.5">
                Date of Birth
              </label>
              <div className="relative">
                <Calendar className="w-4 h-4 text-slate-400 absolute left-3 top-2.5" />
                <input
                  type="date"
                  required
                  value={dateOfBirth}
                  onChange={(e) => setDateOfBirth(e.target.value)}
                  className="w-full pl-9 pr-3 py-2 rounded-lg bg-cosmic-950 border border-cosmic-700 text-white focus:border-cosmic-gold focus:outline-none"
                />
              </div>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1.5">
                Exact Time of Birth
              </label>
              <div className="relative">
                <Clock className="w-4 h-4 text-slate-400 absolute left-3 top-2.5" />
                <input
                  type="time"
                  step="1"
                  disabled={timeUnknown}
                  value={timeOfBirth}
                  onChange={(e) => setTimeOfBirth(e.target.value)}
                  className="w-full pl-9 pr-3 py-2 rounded-lg bg-cosmic-950 border border-cosmic-700 text-white disabled:opacity-40 focus:border-cosmic-gold focus:outline-none"
                />
              </div>
              <label className="inline-flex items-center gap-2 mt-1.5 text-xs text-slate-400 cursor-pointer">
                <input
                  type="checkbox"
                  checked={timeUnknown}
                  onChange={(e) => setTimeUnknown(e.target.checked)}
                  className="rounded border-cosmic-700"
                />
                <span>Birth time is unknown (use Noon baseline)</span>
              </label>
            </div>
          </div>

          {/* Worldwide City / Town / Village Search + Custom Coordinates */}
          <div className="space-y-2">
            <div className="flex items-center justify-between">
              <label className="block text-xs font-semibold text-slate-300">
                Place of Birth (Any City, Town, District, or Village Worldwide)
              </label>
              <button
                type="button"
                onClick={() => setUseCustomCoordinates((prev) => !prev)}
                className="inline-flex items-center gap-1 text-[11px] text-cosmic-gold hover:underline font-medium"
              >
                <SlidersHorizontal className="w-3 h-3" />
                <span>
                  {useCustomCoordinates ? 'Hide Custom Lat/Lon' : 'Enter Custom Lat/Lon'}
                </span>
              </button>
            </div>

            <div className="relative">
              <MapPin className="w-4 h-4 text-slate-400 absolute left-3 top-2.5" />
              <input
                type="text"
                required
                placeholder="Type any city, town, or village (e.g., Gorakhpur, Muzaffarpur, Ujjain, Paris...)"
                value={placeOfBirth}
                onChange={(e) => {
                  setPlaceOfBirth(e.target.value);
                  setSelectedCity(null);
                }}
                className="w-full pl-9 pr-3 py-2 rounded-lg bg-cosmic-950 border border-cosmic-700 text-white focus:border-cosmic-gold focus:outline-none"
              />
            </div>

            {selectedCity && !useCustomCoordinates && (
              <div className="px-3 py-1.5 rounded-lg bg-emerald-500/10 border border-emerald-500/30 text-[11px] text-emerald-300 flex items-center justify-between">
                <span>
                  Resolved: <strong>{selectedCity.name}</strong>
                  {selectedCity.stateOrRegion ? `, ${selectedCity.stateOrRegion}` : ''} (
                  {selectedCity.countryCode})
                </span>
                <span className="font-mono">
                  {selectedCity.latitude.toFixed(4)}°N, {selectedCity.longitude.toFixed(4)}°E •{' '}
                  {selectedCity.timezoneId}
                </span>
              </div>
            )}

            <div className="flex items-center justify-between text-[11px] text-slate-400">
              <span>
                {searchingLocation
                  ? 'Searching worldwide geocoder...'
                  : 'Click a matching location below or type any city name directly:'}
              </span>
            </div>

            {citySuggestions.length > 0 && (
              <div className="flex flex-wrap gap-1.5 max-h-28 overflow-y-auto p-1 rounded-lg bg-cosmic-950/60 border border-cosmic-800">
                {citySuggestions.slice(0, 12).map((c, idx) => {
                  const isSelected =
                    selectedCity?.name === c.name &&
                    Math.abs((selectedCity?.latitude || 0) - c.latitude) < 0.05;
                  return (
                    <button
                      key={`${c.name}-${c.countryCode}-${idx}`}
                      type="button"
                      onClick={() => handleSelectCity(c)}
                      className={`px-2.5 py-1 rounded-md text-xs border transition-colors text-left ${
                        isSelected
                          ? 'bg-cosmic-gold/20 border-cosmic-gold text-cosmic-gold font-semibold'
                          : 'bg-cosmic-900 border-cosmic-800 text-slate-300 hover:border-cosmic-700'
                      }`}
                    >
                      {c.name}
                      {c.stateOrRegion ? `, ${c.stateOrRegion}` : ''} ({c.countryCode}){' '}
                      <span className="text-[10px] text-slate-400">
                        [{c.latitude.toFixed(2)}°, {c.longitude.toFixed(2)}°]
                      </span>
                    </button>
                  );
                })}
              </div>
            )}

            {useCustomCoordinates && (
              <div className="p-3.5 rounded-xl bg-cosmic-950 border border-cosmic-800 space-y-3 mt-2">
                <div className="flex items-center gap-1.5 text-xs font-semibold text-cosmic-gold">
                  <Globe className="w-3.5 h-3.5" />
                  <span>Custom Coordinates &amp; Timezone Override</span>
                </div>
                <div className="grid grid-cols-1 sm:grid-cols-3 gap-3 text-xs">
                  <div>
                    <label className="block text-slate-400 mb-1">Latitude (-90 to +90)</label>
                    <input
                      type="number"
                      step="any"
                      value={customLat}
                      onChange={(e) => setCustomLat(e.target.value)}
                      className="w-full px-2.5 py-1.5 rounded bg-cosmic-900 border border-cosmic-700 text-white font-mono"
                    />
                  </div>
                  <div>
                    <label className="block text-slate-400 mb-1">Longitude (-180 to +180)</label>
                    <input
                      type="number"
                      step="any"
                      value={customLon}
                      onChange={(e) => setCustomLon(e.target.value)}
                      className="w-full px-2.5 py-1.5 rounded bg-cosmic-900 border border-cosmic-700 text-white font-mono"
                    />
                  </div>
                  <div>
                    <label className="block text-slate-400 mb-1">IANA Timezone</label>
                    <input
                      type="text"
                      value={customTimezone}
                      onChange={(e) => setCustomTimezone(e.target.value)}
                      placeholder="Asia/Kolkata"
                      className="w-full px-2.5 py-1.5 rounded bg-cosmic-900 border border-cosmic-700 text-white font-mono"
                    />
                  </div>
                </div>
              </div>
            )}
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-300 mb-1.5">Gender</label>
            <select
              value={gender}
              onChange={(e) => setGender(e.target.value)}
              className="w-full px-3 py-2 rounded-lg bg-cosmic-950 border border-cosmic-700 text-white focus:border-cosmic-gold focus:outline-none"
            >
              <option value="MALE">Male</option>
              <option value="FEMALE">Female</option>
              <option value="OTHER">Other</option>
            </select>
          </div>

          <div className="pt-3 flex items-center justify-end gap-3 border-t border-cosmic-800">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 rounded-lg border border-cosmic-700 text-slate-300 hover:text-white text-xs font-semibold"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={submitting}
              className="px-5 py-2 rounded-lg bg-cosmic-gold text-cosmic-950 hover:bg-amber-400 font-bold text-xs transition-colors disabled:opacity-50"
            >
              {submitting ? 'Calculating Kundli...' : 'Generate Verified Kundli'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

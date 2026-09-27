import React, { useState, useEffect } from 'react';
import { X, MapPin, Calendar, Clock, User } from 'lucide-react';
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
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!isOpen) return;
    let active = true;
    searchCities(placeOfBirth)
      .then((res) => {
        if (active) setCitySuggestions(res);
      })
      .catch(() => {
        if (active) setCitySuggestions([]);
      });
    return () => {
      active = false;
    };
  }, [placeOfBirth, isOpen]);

  if (!isOpen) return null;

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

      await onSubmit({
        name: name.trim(),
        dateOfBirth,
        timeOfBirth: normalizedTime,
        placeOfBirth: selectedCity ? selectedCity.name : placeOfBirth.trim(),
        gender,
        latitude: selectedCity?.latitude,
        longitude: selectedCity?.longitude,
        timezone: selectedCity?.timezoneId,
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
      <div className="w-full max-w-lg rounded-2xl bg-cosmic-900 border border-cosmic-700 shadow-2xl overflow-hidden">
        <div className="flex items-center justify-between px-6 py-4 border-b border-cosmic-800 bg-cosmic-950/60">
          <h2 className="text-base font-bold text-white">Create New Vedic Birth Profile</h2>
          <button
            type="button"
            onClick={onClose}
            className="text-slate-400 hover:text-white p-1 rounded-lg"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        <form onSubmit={handleFormSubmit} className="p-6 space-y-4 text-sm">
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

          <div>
            <label className="block text-xs font-semibold text-slate-300 mb-1.5">
              Place of Birth (Automatic Coordinate &amp; Historical Timezone Lookup)
            </label>
            <div className="relative">
              <MapPin className="w-4 h-4 text-slate-400 absolute left-3 top-2.5" />
              <input
                type="text"
                required
                placeholder="Search city (e.g., New Delhi, Mumbai, Varanasi, London, New York)"
                value={placeOfBirth}
                onChange={(e) => {
                  setPlaceOfBirth(e.target.value);
                  setSelectedCity(null);
                }}
                className="w-full pl-9 pr-3 py-2 rounded-lg bg-cosmic-950 border border-cosmic-700 text-white focus:border-cosmic-gold focus:outline-none"
              />
            </div>
            {citySuggestions.length > 0 && (
              <div className="mt-2 flex flex-wrap gap-1.5 max-h-24 overflow-y-auto">
                {citySuggestions.slice(0, 8).map((c) => (
                  <button
                    key={`${c.name}-${c.countryCode}`}
                    type="button"
                    onClick={() => {
                      setPlaceOfBirth(`${c.name}, ${c.stateOrRegion}`);
                      setSelectedCity(c);
                    }}
                    className={`px-2.5 py-1 rounded-md text-xs border transition-colors ${
                      selectedCity?.name === c.name
                        ? 'bg-cosmic-gold/20 border-cosmic-gold text-cosmic-gold font-semibold'
                        : 'bg-cosmic-950 border-cosmic-800 text-slate-300 hover:border-cosmic-700'
                    }`}
                  >
                    {c.name}, {c.countryCode} ({c.latitude.toFixed(2)}°, {c.longitude.toFixed(2)}°)
                  </button>
                ))}
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

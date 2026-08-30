import React, { useState, useEffect } from 'react';
import { api } from '../api';
import { ShieldAlert, MapPin, CheckCircle, AlertCircle, X, Navigation } from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';

export default function SOSModal({ isOpen, onClose }) {
  const [emergencyType, setEmergencyType] = useState('MEDICAL');
  const [description, setDescription] = useState('');
  const [locationText, setLocationText] = useState('Block B, Corridor 2');
  const [coords, setCoords] = useState({ latitude: null, longitude: null });
  const [gettingLocation, setGettingLocation] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [success, setSuccess] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (isOpen) {
      fetchLocation();
    } else {
      setSuccess(false);
      setError('');
    }
  }, [isOpen]);

  const fetchLocation = () => {
    if ('geolocation' in navigator) {
      setGettingLocation(true);
      navigator.geolocation.getCurrentPosition(
        (pos) => {
          setCoords({
            latitude: pos.coords.latitude,
            longitude: pos.coords.longitude,
          });
          setGettingLocation(false);
        },
        (err) => {
          console.warn('Geolocation warning:', err.message);
          setGettingLocation(false);
        },
        { timeout: 5000, enableHighAccuracy: true }
      );
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    setError('');
    try {
      await api.triggerSOS({
        emergencyType,
        description,
        latitude: coords.latitude,
        longitude: coords.longitude,
        locationText,
      });
      setSuccess(true);
    } catch (err) {
      setError(err.message || 'Failed to dispatch SOS alert.');
    } finally {
      setSubmitting(false);
    }
  };

  if (!isOpen) return null;

  return (
    <AnimatePresence>
      <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-md">
        <motion.div
          initial={{ scale: 0.9, opacity: 0, y: 20 }}
          animate={{ scale: 1, opacity: 1, y: 0 }}
          exit={{ scale: 0.9, opacity: 0, y: 20 }}
          className="relative w-full max-w-lg glass-panel rounded-3xl p-6 sm:p-8 border border-red-500/30 shadow-2xl shadow-red-950/50 overflow-hidden"
        >
          <div className="absolute top-0 right-0 p-4">
            <button
              onClick={onClose}
              className="p-1.5 text-gray-400 hover:text-white rounded-lg hover:bg-white/10 transition-colors"
            >
              <X size={20} />
            </button>
          </div>

          <div className="flex items-center space-x-3 pb-4 border-b border-white/10">
            <div className="p-3 bg-red-600/20 text-red-500 rounded-2xl border border-red-500/40 animate-pulse">
              <ShieldAlert className="w-8 h-8" />
            </div>
            <div>
              <h2 className="text-xl font-bold text-white tracking-tight">SOS EMERGENCY ALERT</h2>
              <p className="text-xs text-red-300 font-medium">
                Instantly dispatches location telemetry to Security Command Center
              </p>
            </div>
          </div>

          {success ? (
            <div className="py-8 text-center space-y-4">
              <div className="w-16 h-16 bg-emerald-500/20 border border-emerald-500/40 text-emerald-400 rounded-full flex items-center justify-center mx-auto">
                <CheckCircle size={36} />
              </div>
              <h3 className="text-lg font-bold text-white">Distress Signal Dispatched!</h3>
              <p className="text-sm text-gray-300">
                Security Team has received your alert coordinates and contact info. Stay calm and stay where you are.
              </p>
              <button
                onClick={onClose}
                className="mt-4 px-6 py-2.5 rounded-xl bg-gradient-to-r from-emerald-600 to-teal-600 text-white font-semibold text-sm shadow-lg shadow-emerald-600/20"
              >
                Close & Return
              </button>
            </div>
          ) : (
            <form onSubmit={handleSubmit} className="mt-5 space-y-4">
              {error && (
                <div className="p-3 rounded-xl bg-red-500/10 border border-red-500/30 text-red-400 text-xs flex items-center space-x-2">
                  <AlertCircle size={16} />
                  <span>{error}</span>
                </div>
              )}

              <div>
                <label className="block text-xs font-semibold text-gray-300 uppercase tracking-wider mb-2">
                  Emergency Category
                </label>
                <div className="grid grid-cols-3 gap-2">
                  {['MEDICAL', 'FIRE', 'SECURITY', 'ACCIDENT', 'OTHER'].map((type) => (
                    <button
                      key={type}
                      type="button"
                      onClick={() => setEmergencyType(type)}
                      className={`py-2 px-3 rounded-xl text-xs font-semibold border transition-all ${
                        emergencyType === type
                          ? 'bg-gradient-to-r from-red-600 to-rose-600 text-white border-red-400 shadow-md shadow-red-600/30'
                          : 'glass-card text-gray-300 hover:text-white border-white/10'
                      }`}
                    >
                      {type}
                    </button>
                  ))}
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-300 uppercase tracking-wider mb-1.5">
                  Specific Campus Location
                </label>
                <div className="relative">
                  <input
                    type="text"
                    required
                    value={locationText}
                    onChange={(e) => setLocationText(e.target.value)}
                    placeholder="e.g. Science Block 2nd Floor, Room 204"
                    className="w-full glass-input rounded-xl px-4 py-2.5 text-sm pl-10"
                  />
                  <MapPin className="w-4 h-4 text-gray-400 absolute left-3.5 top-3" />
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-300 uppercase tracking-wider mb-1.5">
                  GPS Telemetry Coordinates
                </label>
                <div className="glass-card rounded-xl p-3 border border-white/10 text-xs flex items-center justify-between text-gray-300">
                  <div className="flex items-center space-x-2">
                    <Navigation className="w-4 h-4 text-indigo-400" />
                    <span>
                      {gettingLocation
                        ? 'Acquiring GPS fix...'
                        : coords.latitude
                        ? `Lat: ${coords.latitude.toFixed(5)}, Long: ${coords.longitude.toFixed(5)}`
                        : 'GPS Coordinates unavailable (Using Location text)'}
                    </span>
                  </div>
                  <button
                    type="button"
                    onClick={fetchLocation}
                    className="text-[11px] text-indigo-400 hover:underline"
                  >
                    Refresh
                  </button>
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-300 uppercase tracking-wider mb-1.5">
                  Additional Situation Note (Optional)
                </label>
                <textarea
                  rows="2"
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  placeholder="Need immediate first-aid / stretcher..."
                  className="w-full glass-input rounded-xl p-3 text-sm"
                />
              </div>

              <div className="pt-2">
                <button
                  type="submit"
                  disabled={submitting}
                  className="w-full py-3 rounded-xl bg-gradient-to-r from-red-600 via-rose-600 to-pink-600 text-white font-bold text-sm shadow-xl shadow-red-600/30 hover:scale-[1.01] active:scale-[0.99] transition-all flex items-center justify-center space-x-2"
                >
                  <ShieldAlert className="w-5 h-5 animate-pulse" />
                  <span>{submitting ? 'DISPATCHING ALERT...' : 'TRIGGER EMERGENCY DISTRESS'}</span>
                </button>
              </div>
            </form>
          )}
        </motion.div>
      </div>
    </AnimatePresence>
  );
}

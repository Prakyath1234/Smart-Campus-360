import React, { useState, useEffect } from 'react';
import { api } from '../api';
import { 
  ShieldAlert, 
  MapPin, 
  CheckCircle, 
  AlertCircle, 
  X, 
  Navigation, 
  Phone, 
  Building2, 
  Flame, 
  HeartPulse, 
  Shield, 
  Home, 
  Zap,
  Info
} from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';

const CAMPUS_EMERGENCY_CATEGORIES = [
  {
    id: 'CAMPUS_MEDICAL',
    label: 'Campus Medical & Clinic',
    subtext: 'Sudden illness, fainting, severe injury in class/lab',
    icon: HeartPulse,
    color: 'from-rose-600 to-red-600 border-red-500/40 text-red-300'
  },
  {
    id: 'LAB_HAZARD',
    label: 'Lab & Chemical Hazard',
    subtext: 'Chemical spill, gas leak, hazardous apparatus',
    icon: Zap,
    color: 'from-amber-600 to-orange-600 border-orange-500/40 text-orange-300'
  },
  {
    id: 'ELECTRICAL_FIRE',
    label: 'Campus Fire / Electrical',
    subtext: 'Smoke, short circuit in block or laboratory',
    icon: Flame,
    color: 'from-red-600 to-amber-600 border-amber-500/40 text-amber-300'
  },
  {
    id: 'ANTI_RAGGING',
    label: 'Anti-Ragging & Harassment',
    subtext: 'Bullying, intimidation, confidential safety squad',
    icon: Shield,
    color: 'from-purple-600 to-indigo-600 border-purple-500/40 text-purple-300'
  },
  {
    id: 'CAMPUS_SECURITY',
    label: 'Campus Intrusion & Patrol',
    subtext: 'Unauthorized trespasser, theft, physical safety',
    icon: ShieldAlert,
    color: 'from-blue-600 to-cyan-600 border-blue-500/40 text-blue-300'
  },
  {
    id: 'HOSTEL_DISTRESS',
    label: 'Hostel Night Emergency',
    subtext: 'Hostel room medical crisis, warden intervention',
    icon: Home,
    color: 'from-emerald-600 to-teal-600 border-emerald-500/40 text-emerald-300'
  }
];

const CAMPUS_ZONES = [
  'Academic Block A (Lecture Halls 101 - 308)',
  'Academic Block B (Computer & Electronics Labs)',
  'Engineering Mechanical & Civil Workshops',
  'Central Library & Digital Reading Halls',
  'Boys Hostel Block (Vindhya & Cauvery)',
  'Girls Hostel Block (Ganga & Sharavathi)',
  'Campus Health Clinic & First Aid Unit',
  'Student Cafeteria & Food Court',
  'Sports Arena & Athletic Ground',
  'Administrative Complex & Auditorium',
  'Main Entrance Gate & Security Cabin'
];

const CAMPUS_HOTLINES = [
  {
    role: 'Campus Security Control Room (24/7)',
    ext: 'Ext. 100 / 0820-2580123',
    tel: '08202580123',
    desc: 'Main Gate security guards & vehicle patrol dispatch'
  },
  {
    role: 'Campus Health Clinic & Ambulance',
    ext: 'Ext. 108 / 0820-2580108',
    tel: '08202580108',
    desc: 'Campus Medical Doctor, stretcher and nurse on duty'
  },
  {
    role: 'Campus Anti-Ragging Cell (Confidential)',
    ext: '0820-2580199',
    tel: '08202580199',
    desc: 'Proctorial board & student welfare council'
  },
  {
    role: 'Chief Hostel Warden (Hostel Emergency)',
    ext: '0820-2580150',
    tel: '08202580150',
    desc: 'Hostel superintendent & night caretaker'
  },
  {
    role: 'Campus Fire & Electrical Safety Cell',
    ext: 'Ext. 112 / 0820-2580144',
    tel: '08202580144',
    desc: 'Campus electrical engineers & lab emergency response'
  }
];

export default function SOSModal({ isOpen, onClose }) {
  const [modalTab, setModalTab] = useState('dispatch'); // 'dispatch' | 'hotlines'
  const [emergencyType, setEmergencyType] = useState('CAMPUS_MEDICAL');
  const [selectedZone, setSelectedZone] = useState(CAMPUS_ZONES[0]);
  const [roomDetails, setRoomDetails] = useState('');
  const [description, setDescription] = useState('');
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
          console.warn('Campus Geolocation warning:', err.message);
          setGettingLocation(false);
        },
        { timeout: 6000, enableHighAccuracy: true }
      );
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    setError('');

    const fullLocationText = roomDetails.trim() 
      ? `${selectedZone} [Room/Spot: ${roomDetails.trim()}]` 
      : selectedZone;

    try {
      await api.triggerSOS({
        emergencyType,
        description: description.trim() || 'Campus emergency assistance requested.',
        latitude: coords.latitude,
        longitude: coords.longitude,
        locationText: fullLocationText,
      });
      setSuccess(true);
    } catch (err) {
      setError(err.message || 'Failed to dispatch campus SOS alert.');
    } finally {
      setSubmitting(false);
    }
  };

  if (!isOpen) return null;

  return (
    <AnimatePresence>
      <div className="fixed inset-0 z-50 flex items-center justify-center p-3 sm:p-4 bg-slate-950/85 backdrop-blur-md">
        <motion.div
          initial={{ scale: 0.92, opacity: 0, y: 15 }}
          animate={{ scale: 1, opacity: 1, y: 0 }}
          exit={{ scale: 0.92, opacity: 0, y: 15 }}
          className="relative w-full max-w-xl max-h-[90vh] glass-panel rounded-3xl p-5 sm:p-7 border border-red-500/40 shadow-2xl shadow-red-950/60 overflow-y-auto"
        >
          {/* Close button */}
          <button
            onClick={onClose}
            className="absolute top-4 right-4 p-1.5 text-gray-400 hover:text-white rounded-lg hover:bg-white/10 transition-colors"
          >
            <X size={20} />
          </button>

          {/* Header */}
          <div className="flex items-center space-x-3.5 pb-4 border-b border-white/10">
            <div className="p-3 bg-red-600/20 text-red-500 rounded-2xl border border-red-500/40 animate-pulse">
              <ShieldAlert className="w-7 h-7" />
            </div>
            <div>
              <div className="flex items-center space-x-2">
                <span className="text-[10px] font-black uppercase tracking-widest px-2 py-0.5 rounded bg-red-500/20 text-red-400 border border-red-500/30">
                  Campus-Only Network
                </span>
                <span className="text-[10px] text-gray-400 font-medium">Internal Security Dispatch</span>
              </div>
              <h2 className="text-xl font-extrabold text-white tracking-tight">CAMPUS EMERGENCY SOS</h2>
            </div>
          </div>

          {/* Tab switcher: Dispatch vs Campus Hotlines */}
          <div className="flex space-x-2 mt-4 p-1 bg-slate-900/80 rounded-xl border border-white/10 text-xs">
            <button
              type="button"
              onClick={() => setModalTab('dispatch')}
              className={`flex-1 py-2 rounded-lg font-bold transition-all ${
                modalTab === 'dispatch'
                  ? 'bg-gradient-to-r from-red-600 to-rose-600 text-white shadow-md'
                  : 'text-gray-400 hover:text-white'
              }`}
            >
              🚨 Dispatch Campus Alert
            </button>
            <button
              type="button"
              onClick={() => setModalTab('hotlines')}
              className={`flex-1 py-2 rounded-lg font-bold transition-all ${
                modalTab === 'hotlines'
                  ? 'bg-gradient-to-r from-indigo-600 to-purple-600 text-white shadow-md'
                  : 'text-gray-400 hover:text-white'
              }`}
            >
              📞 Campus Safety Directory
            </button>
          </div>

          {/* Content */}
          {modalTab === 'hotlines' ? (
            /* Campus Hotlines Directory */
            <div className="mt-5 space-y-3">
              <div className="p-3 rounded-xl bg-indigo-500/10 border border-indigo-500/20 text-indigo-300 text-xs flex items-center space-x-2">
                <Info size={16} className="shrink-0" />
                <span>All numbers connect directly to internal on-duty campus personnel 24/7.</span>
              </div>

              <div className="space-y-2.5">
                {CAMPUS_HOTLINES.map((h, i) => (
                  <div key={i} className="glass-card rounded-2xl p-3.5 border border-white/10 flex items-center justify-between gap-3">
                    <div>
                      <div className="text-xs font-bold text-white">{h.role}</div>
                      <div className="text-[11px] text-gray-400">{h.desc}</div>
                      <div className="text-xs font-mono font-semibold text-rose-400 mt-1">{h.ext}</div>
                    </div>
                    <a
                      href={`tel:${h.tel}`}
                      className="px-3.5 py-2 rounded-xl bg-emerald-600/20 text-emerald-300 border border-emerald-500/30 hover:bg-emerald-600/30 text-xs font-bold flex items-center space-x-1.5 transition-all shrink-0"
                    >
                      <Phone size={14} />
                      <span>Call</span>
                    </a>
                  </div>
                ))}
              </div>

              <button
                type="button"
                onClick={() => setModalTab('dispatch')}
                className="w-full mt-3 py-2.5 rounded-xl bg-slate-800 text-gray-300 hover:text-white text-xs font-semibold"
              >
                Back to Alert Dispatch Form
              </button>
            </div>
          ) : success ? (
            /* Success confirmation */
            <div className="py-8 text-center space-y-4">
              <div className="w-16 h-16 bg-emerald-500/20 border border-emerald-500/40 text-emerald-400 rounded-full flex items-center justify-center mx-auto">
                <CheckCircle size={36} />
              </div>
              <h3 className="text-lg font-bold text-white">Campus Distress Signal Transmitted!</h3>
              <p className="text-xs text-gray-300 max-w-md mx-auto leading-relaxed">
                Campus Security Control Room and on-duty guards have received your incident report, building zone, and telemetry. Response personnel are being dispatched. Stay where you are if safe.
              </p>
              <div className="pt-2 flex justify-center space-x-3">
                <button
                  onClick={onClose}
                  className="px-6 py-2.5 rounded-xl bg-gradient-to-r from-emerald-600 to-teal-600 text-white font-semibold text-xs shadow-lg shadow-emerald-600/20 hover:scale-105 transition-all"
                >
                  Close Confirmation
                </button>
                <button
                  onClick={() => setModalTab('hotlines')}
                  className="px-4 py-2.5 rounded-xl bg-slate-800 border border-white/10 text-gray-200 text-xs font-semibold hover:bg-slate-700"
                >
                  View Campus Numbers
                </button>
              </div>
            </div>
          ) : (
            /* Dispatch Form */
            <form onSubmit={handleSubmit} className="mt-4 space-y-3.5">
              {error && (
                <div className="p-3 rounded-xl bg-red-500/10 border border-red-500/30 text-red-400 text-xs flex items-center space-x-2">
                  <AlertCircle size={16} className="shrink-0" />
                  <span>{error}</span>
                </div>
              )}

              {/* Campus Category Selector */}
              <div>
                <label className="block text-[11px] font-bold text-gray-300 uppercase tracking-wider mb-1.5">
                  Campus Emergency Category
                </label>
                <div className="grid grid-cols-2 sm:grid-cols-3 gap-2">
                  {CAMPUS_EMERGENCY_CATEGORIES.map((cat) => {
                    const Icon = cat.icon;
                    const isSelected = emergencyType === cat.id;
                    return (
                      <button
                        key={cat.id}
                        type="button"
                        onClick={() => setEmergencyType(cat.id)}
                        className={`p-2.5 rounded-xl text-left border transition-all ${
                          isSelected
                            ? `bg-gradient-to-r ${cat.color} shadow-lg shadow-red-900/30`
                            : 'glass-card border-white/10 text-gray-300 hover:text-white hover:border-white/20'
                        }`}
                      >
                        <Icon className="w-4 h-4 mb-1" />
                        <div className="text-[11px] font-bold leading-tight">{cat.label}</div>
                      </button>
                    );
                  })}
                </div>
              </div>

              {/* Campus Zone / Facility Dropdown */}
              <div>
                <label className="block text-[11px] font-bold text-gray-300 uppercase tracking-wider mb-1">
                  Campus Facility / Building Zone
                </label>
                <div className="relative">
                  <select
                    value={selectedZone}
                    onChange={(e) => setSelectedZone(e.target.value)}
                    className="w-full glass-input rounded-xl px-3 py-2 text-xs pl-9 appearance-none text-white bg-slate-900 border border-white/10 cursor-pointer"
                  >
                    {CAMPUS_ZONES.map((zone, i) => (
                      <option key={i} value={zone} className="bg-slate-900 text-white">
                        {zone}
                      </option>
                    ))}
                  </select>
                  <Building2 className="w-4 h-4 text-indigo-400 absolute left-3 top-2.5 pointer-events-none" />
                </div>
              </div>

              {/* Specific Room / Floor Landmark */}
              <div>
                <label className="block text-[11px] font-bold text-gray-300 uppercase tracking-wider mb-1">
                  Specific Room, Floor, or Landmark
                </label>
                <div className="relative">
                  <input
                    type="text"
                    required
                    value={roomDetails}
                    onChange={(e) => setRoomDetails(e.target.value)}
                    placeholder="e.g. Room 304, 3rd Floor / Near Physics Lab entrance"
                    className="w-full glass-input rounded-xl px-3 py-2 text-xs pl-9"
                  />
                  <MapPin className="w-4 h-4 text-rose-400 absolute left-3 top-2.5" />
                </div>
              </div>

              {/* Live Campus GPS Telemetry */}
              <div>
                <div className="glass-card rounded-xl p-2.5 border border-white/10 text-[11px] flex items-center justify-between text-gray-300">
                  <div className="flex items-center space-x-2">
                    <Navigation className="w-3.5 h-3.5 text-indigo-400 shrink-0" />
                    <span>
                      {gettingLocation
                        ? 'Acquiring GPS fix...'
                        : coords.latitude
                        ? `GPS: ${coords.latitude.toFixed(5)}, ${coords.longitude.toFixed(5)}`
                        : 'GPS Coordinates unavailable (Facility zone will be used)'}
                    </span>
                  </div>
                  <button
                    type="button"
                    onClick={fetchLocation}
                    className="text-[10px] text-indigo-400 hover:underline font-bold"
                  >
                    Refresh GPS
                  </button>
                </div>
              </div>

              {/* Situation Note */}
              <div>
                <label className="block text-[11px] font-bold text-gray-300 uppercase tracking-wider mb-1">
                  Urgent Situation Details (Optional)
                </label>
                <textarea
                  rows="2"
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  placeholder="Need stretcher, first-aid, or security intervention..."
                  className="w-full glass-input rounded-xl p-2.5 text-xs"
                />
              </div>

              {/* Action Submit */}
              <div className="pt-1">
                <button
                  type="submit"
                  disabled={submitting}
                  className="w-full py-3 rounded-xl bg-gradient-to-r from-red-600 via-rose-600 to-pink-600 text-white font-extrabold text-xs shadow-xl shadow-red-600/30 hover:scale-[1.01] active:scale-[0.99] transition-all flex items-center justify-center space-x-2 pulse-sos"
                >
                  <ShieldAlert className="w-4 h-4" />
                  <span>{submitting ? 'TRANSMITTING TO CAMPUS SECURITY...' : 'TRIGGER CAMPUS SOS DISTRESS'}</span>
                </button>
              </div>
            </form>
          )}
        </motion.div>
      </div>
    </AnimatePresence>
  );
}
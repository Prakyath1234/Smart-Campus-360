import React, { useState, useEffect } from 'react';
import { api } from '../../api';
import Navbar from '../../components/Navbar';
import Sidebar from '../../components/Sidebar';
import SOSModal from '../../components/SOSModal';
import { 
  Radio, 
  ShieldAlert, 
  MapPin, 
  Phone, 
  User, 
  CheckCircle2, 
  Clock, 
  AlertTriangle, 
  Navigation,
  HeartPulse,
  Flame,
  Shield,
  Home,
  Zap
} from 'lucide-react';
import { motion } from 'framer-motion';

const getCategoryBadge = (type) => {
  switch (type) {
    case 'CAMPUS_MEDICAL':
    case 'MEDICAL':
      return { label: '🩺 Campus Medical Emergency', color: 'bg-red-600/30 text-red-300 border-red-500/40' };
    case 'LAB_HAZARD':
      return { label: '🧪 Lab & Chemical Hazard', color: 'bg-orange-600/30 text-orange-300 border-orange-500/40' };
    case 'ELECTRICAL_FIRE':
    case 'FIRE':
      return { label: '🔥 Campus Fire / Electrical', color: 'bg-amber-600/30 text-amber-300 border-amber-500/40' };
    case 'ANTI_RAGGING':
      return { label: '🛡️ Anti-Ragging & Harassment', color: 'bg-purple-600/30 text-purple-300 border-purple-500/40' };
    case 'CAMPUS_SECURITY':
    case 'SECURITY':
      return { label: '🚨 Campus Intrusion & Patrol', color: 'bg-blue-600/30 text-blue-300 border-blue-500/40' };
    case 'HOSTEL_DISTRESS':
      return { label: '🏠 Hostel Night Emergency', color: 'bg-teal-600/30 text-teal-300 border-teal-500/40' };
    default:
      return { label: `🚨 ${type || 'Campus Incident'}`, color: 'bg-rose-600/30 text-rose-300 border-rose-500/40' };
  }
};

export default function SecurityDashboard() {
  const [activeTab, setActiveTab] = useState('sos_feed');
  const [isSidebarOpen, setIsSidebarOpen] = useState(false);
  const [isSOSOpen, setIsSOSOpen] = useState(false);

  const [alerts, setAlerts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [msg, setMsg] = useState({ type: '', text: '' });

  useEffect(() => {
    loadAlerts();
    const interval = setInterval(loadAlerts, 5000); // Polling every 5 seconds for live distress alerts
    return () => clearInterval(interval);
  }, []);

  const loadAlerts = async () => {
    try {
      const data = await api.getEmergencyAlerts();
      setAlerts(data || []);
    } catch (err) {
      console.error('Failed to load emergency alerts:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleStatusUpdate = async (id, status) => {
    try {
      await api.updateAlertStatus(id, status);
      setMsg({ type: 'success', text: `Alert #${id} marked as ${status}!` });
      loadAlerts();
    } catch (err) {
      setMsg({ type: 'error', text: err.message || 'Failed to update alert status.' });
    }
  };

  const activeAlertsCount = alerts.filter((a) => a.status === 'ACTIVE').length;

  return (
    <div className="min-h-screen bg-slate-950 text-gray-100 flex flex-col">
      <Navbar
        toggleSidebar={() => setIsSidebarOpen(!isSidebarOpen)}
        isSidebarOpen={isSidebarOpen}
        onOpenSOS={() => setIsSOSOpen(true)}
      />

      <div className="flex flex-1">
        <Sidebar
          isOpen={isSidebarOpen}
          activeTab={activeTab}
          setActiveTab={(tab) => {
            setActiveTab(tab);
            setIsSidebarOpen(false);
          }}
        />

        <main className="flex-1 p-4 sm:p-6 lg:p-8 max-w-7xl mx-auto overflow-y-auto">
          {/* Top Security Banner */}
          <div className="glass-panel rounded-3xl p-6 mb-8 border border-rose-500/30 bg-gradient-to-r from-rose-950/60 via-red-950/40 to-slate-900">
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
              <div className="flex items-center space-x-4">
                <div className="p-3 bg-red-600/20 text-red-500 rounded-2xl border border-red-500/40 animate-pulse">
                  <Radio className="w-8 h-8" />
                </div>
                <div>
                  <span className="text-xs font-bold text-rose-400 uppercase tracking-widest">
                    Campus Security Dispatch
                  </span>
                  <h1 className="text-2xl sm:text-3xl font-extrabold text-white">
                    Live SOS Telemetry Feed 🛡️
                  </h1>
                </div>
              </div>

              <div className="flex items-center space-x-3">
                <div className="px-4 py-2 rounded-2xl bg-red-500/20 border border-red-500/40 text-red-300 font-bold text-sm flex items-center space-x-2">
                  <span className="w-2.5 h-2.5 bg-red-500 rounded-full animate-ping" />
                  <span>{activeAlertsCount} ACTIVE SOS ALERTS</span>
                </div>
              </div>
            </div>
          </div>

          {msg.text && (
            <div className={`mb-6 p-4 rounded-2xl border text-xs flex items-center justify-between ${
              msg.type === 'success' ? 'bg-emerald-500/10 border-emerald-500/30 text-emerald-300' : 'bg-red-500/10 border-red-500/30 text-red-300'
            }`}>
              <span>{msg.text}</span>
              <button onClick={() => setMsg({ type: '', text: '' })} className="font-bold underline">Dismiss</button>
            </div>
          )}

          {/* SOS Feed List */}
          <div className="space-y-4">
            {loading ? (
              <div className="text-center py-12 text-sm text-gray-400 animate-pulse">
                Establishing live telemetry socket feed...
              </div>
            ) : alerts.length === 0 ? (
              <div className="glass-panel rounded-3xl p-12 text-center text-gray-400 border border-white/10">
                <CheckCircle2 className="w-12 h-12 text-emerald-400 mx-auto mb-3" />
                <h3 className="text-lg font-bold text-white">All Clear on Campus</h3>
                <p className="text-xs text-gray-400 mt-1">No active emergency distress signals at this time.</p>
              </div>
            ) : (
              alerts.map((a) => {
                const isActive = a.status === 'ACTIVE';
                const badge = getCategoryBadge(a.emergencyType);
                const studentDisplayName = a.studentName || a.student?.user?.name || 'Campus Student in Distress';
                const studentRollNumber = a.rollNumber || a.student?.rollNumber || 'Campus ID';
                const studentPhone = a.phone || a.student?.user?.phone;

                return (
                  <motion.div
                    key={a.id}
                    initial={{ opacity: 0, y: 10 }}
                    animate={{ opacity: 1, y: 0 }}
                    className={`glass-panel rounded-3xl p-6 border transition-all ${
                      isActive
                        ? 'border-red-500/50 bg-red-950/20 shadow-xl shadow-red-950/30 pulse-sos'
                        : 'border-white/10 bg-slate-900/60'
                    }`}
                  >
                    <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-6">
                      <div className="space-y-3 flex-1">
                        <div className="flex items-center space-x-3">
                          <span className={`px-3 py-1 rounded-full font-extrabold text-xs border ${badge.color}`}>
                            {badge.label}
                          </span>
                          <span className={`px-2.5 py-0.5 rounded-full font-bold text-[11px] border ${
                            a.status === 'ACTIVE' ? 'bg-red-500/20 text-red-300 border-red-500/40' :
                            a.status === 'ACKNOWLEDGED' ? 'bg-amber-500/20 text-amber-300 border-amber-500/40' :
                            a.status === 'IN_PROGRESS' ? 'bg-blue-500/20 text-blue-300 border-blue-500/40' :
                            a.status === 'RESOLVED' ? 'bg-emerald-500/20 text-emerald-300 border-emerald-500/40' :
                            'bg-gray-500/20 text-gray-300 border-gray-500/40'
                          }`}>
                            {a.status}
                          </span>
                          <span className="text-xs text-gray-400 flex items-center space-x-1">
                            <Clock size={13} />
                            <span>{new Date(a.createdAt).toLocaleTimeString()}</span>
                          </span>
                        </div>

                        <div>
                          <h3 className="text-base font-bold text-white flex items-center space-x-2">
                            <User className="text-indigo-400" size={18} />
                            <span>{studentDisplayName}</span>
                            <span className="text-xs text-indigo-300 font-normal">
                              ({studentRollNumber})
                            </span>
                          </h3>
                        </div>

                        <div className="flex flex-wrap gap-4 text-xs text-gray-300">
                          <div className="flex items-center space-x-1.5 bg-slate-900/80 px-3 py-1.5 rounded-xl border border-white/5">
                            <MapPin size={14} className="text-rose-400" />
                            <span><strong>Location:</strong> {a.locationText || 'Main Campus'}</span>
                          </div>

                          {studentPhone && (
                            <a
                              href={`tel:${studentPhone}`}
                              className="flex items-center space-x-1.5 bg-emerald-500/10 text-emerald-300 px-3 py-1.5 rounded-xl border border-emerald-500/30 hover:bg-emerald-500/20"
                            >
                              <Phone size={14} />
                              <span>Call: {studentPhone}</span>
                            </a>
                          )}

                          {a.latitude && a.longitude && (
                            <a
                              href={`https://maps.google.com/?q=${a.latitude},${a.longitude}`}
                              target="_blank"
                              rel="noreferrer"
                              className="flex items-center space-x-1.5 bg-blue-500/10 text-blue-300 px-3 py-1.5 rounded-xl border border-blue-500/30 hover:bg-blue-500/20"
                            >
                              <Navigation size={14} />
                              <span>View GPS Map</span>
                            </a>
                          )}
                        </div>

                        {a.description && (
                          <p className="text-xs text-gray-300 bg-slate-950/60 p-3 rounded-xl border border-white/5">
                            "{a.description}"
                          </p>
                        )}
                      </div>

                      {/* State Machine Action Buttons */}
                      <div className="flex lg:flex-col gap-2 justify-end shrink-0">
                        {a.status === 'ACTIVE' && (
                          <>
                            <button
                              onClick={() => handleStatusUpdate(a.id, 'ACKNOWLEDGED')}
                              className="px-4 py-2 bg-amber-600 hover:bg-amber-500 text-white rounded-xl font-bold text-xs shadow-lg transition-all"
                            >
                              ACKNOWLEDGE ALERT
                            </button>
                            <button
                              onClick={() => handleStatusUpdate(a.id, 'CANCELLED')}
                              className="px-4 py-2 bg-slate-800 hover:bg-slate-700 text-gray-300 rounded-xl font-bold text-xs border border-white/10 transition-all"
                            >
                              CANCEL ALERT
                            </button>
                          </>
                        )}
                        {a.status === 'ACKNOWLEDGED' && (
                          <>
                            <button
                              onClick={() => handleStatusUpdate(a.id, 'IN_PROGRESS')}
                              className="px-4 py-2 bg-blue-600 hover:bg-blue-500 text-white rounded-xl font-bold text-xs shadow-lg transition-all"
                            >
                              DISPATCH PATROL (IN PROGRESS)
                            </button>
                            <button
                              onClick={() => handleStatusUpdate(a.id, 'RESOLVED')}
                              className="px-4 py-2 bg-emerald-600 hover:bg-emerald-500 text-white rounded-xl font-bold text-xs shadow-lg transition-all"
                            >
                              MARK RESOLVED
                            </button>
                          </>
                        )}
                        {a.status === 'IN_PROGRESS' && (
                          <button
                            onClick={() => handleStatusUpdate(a.id, 'RESOLVED')}
                            className="px-4 py-2 bg-emerald-600 hover:bg-emerald-500 text-white rounded-xl font-bold text-xs shadow-lg transition-all"
                          >
                            MARK RESOLVED
                          </button>
                        )}
                      </div>
                    </div>
                  </motion.div>
                );
              })
            )}
          </div>
        </main>
      </div>

      <SOSModal isOpen={isSOSOpen} onClose={() => setIsSOSOpen(false)} />
    </div>
  );
}
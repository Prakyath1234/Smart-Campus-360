import React, { useEffect, useState } from 'react';
import { api } from '../api';
import { CheckCircle2, AlertTriangle, Bell, Info } from 'lucide-react';

export default function NotificationDropdown({ onClose }) {
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadNotifications();
  }, []);

  const loadNotifications = async () => {
    try {
      const data = await api.getNotifications();
      setNotifications(data || []);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleMarkRead = async (id) => {
    try {
      await api.markNotificationRead(id);
      setNotifications(prev =>
        prev.map(n => (n.id === id ? { ...n, isRead: true } : n))
      );
    } catch (err) {
      console.error(err);
    }
  };

  const getTypeIcon = (type) => {
    switch (type) {
      case 'SOS':
        return <AlertTriangle className="w-4 h-4 text-red-400" />;
      case 'ACADEMIC':
        return <CheckCircle2 className="w-4 h-4 text-blue-400" />;
      default:
        return <Info className="w-4 h-4 text-indigo-400" />;
    }
  };

  return (
    <div className="absolute right-0 mt-3 w-80 sm:w-96 glass-panel rounded-2xl shadow-2xl border border-white/10 p-4 z-50 animate-in fade-in zoom-in-95 duration-200">
      <div className="flex items-center justify-between pb-3 border-b border-white/10">
        <div className="flex items-center space-x-2">
          <Bell className="w-4 h-4 text-indigo-400" />
          <h3 className="text-sm font-semibold text-gray-200">Notifications</h3>
        </div>
        <button
          onClick={onClose}
          className="text-xs text-gray-400 hover:text-white transition-colors"
        >
          Close
        </button>
      </div>

      <div className="mt-3 max-h-72 overflow-y-auto space-y-2 pr-1">
        {loading ? (
          <div className="text-center py-6 text-xs text-gray-400 animate-pulse">
            Loading notifications...
          </div>
        ) : notifications.length === 0 ? (
          <div className="text-center py-6 text-xs text-gray-400">
            No notifications available.
          </div>
        ) : (
          notifications.map((n) => (
            <div
              key={n.id}
              onClick={() => !n.isRead && handleMarkRead(n.id)}
              className={`p-3 rounded-xl border text-xs transition-all cursor-pointer ${
                n.isRead
                  ? 'bg-slate-900/40 border-white/5 opacity-65'
                  : 'bg-indigo-950/40 border-indigo-500/30 hover:border-indigo-500/50'
              }`}
            >
              <div className="flex items-start justify-between">
                <div className="flex items-center space-x-2">
                  {getTypeIcon(n.type)}
                  <span className="font-semibold text-gray-200">{n.title}</span>
                </div>
                <span className="text-[10px] text-gray-500">
                  {new Date(n.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                </span>
              </div>
              <p className="mt-1 text-gray-300 pl-6 leading-relaxed">{n.message}</p>
            </div>
          ))
        )}
      </div>
    </div>
  );
}

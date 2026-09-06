import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { ShieldAlert, Bell, LogOut, User, Sparkles, Menu, X } from 'lucide-react';
import NotificationDropdown from './NotificationDropdown';

export default function Navbar({ toggleSidebar, isSidebarOpen, onOpenSOS }) {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [showNotifications, setShowNotifications] = useState(false);

  const roleColors = {
    ADMIN: 'from-purple-600 to-indigo-600 text-purple-200 border-purple-500/30',
    FACULTY: 'from-blue-600 to-cyan-600 text-blue-200 border-blue-500/30',
    STUDENT: 'from-emerald-600 to-teal-600 text-emerald-200 border-emerald-500/30',
    SECURITY: 'from-rose-600 to-amber-600 text-rose-200 border-rose-500/30',
  };

  const handleBrandClick = () => {
    if (!user) {
      navigate('/');
      return;
    }
    const roleRoutes = {
      ADMIN: '/admin',
      FACULTY: '/faculty',
      STUDENT: '/student',
      SECURITY: '/security'
    };
    navigate(roleRoutes[user.role] || '/');
  };

  return (
    <header className="sticky top-0 z-40 glass-panel border-b border-white/10 px-4 lg:px-8 py-3 transition-all duration-300">
      <div className="flex items-center justify-between">
        {/* Left: Mobile Menu Toggle & Brand */}
        <div className="flex items-center space-x-4">
          <button
            onClick={toggleSidebar}
            className="p-2 text-gray-300 hover:text-white lg:hidden rounded-lg hover:bg-white/5 transition-colors"
          >
            {isSidebarOpen ? <X size={22} /> : <Menu size={22} />}
          </button>
          
          <button
            type="button"
            onClick={handleBrandClick}
            className="flex items-center space-x-3 text-left group hover:opacity-90 transition-opacity"
          >
            <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-indigo-600 via-purple-600 to-pink-500 p-0.5 shadow-lg shadow-indigo-500/20 group-hover:scale-105 transition-transform">
              <div className="w-full h-full bg-slate-950 rounded-[10px] flex items-center justify-center">
                <Sparkles className="w-5 h-5 text-indigo-400 animate-pulse" />
              </div>
            </div>
            <div>
              <span className="text-xl font-bold bg-clip-text text-transparent bg-gradient-to-r from-white via-slate-200 to-indigo-300 tracking-tight">
                Smart Campus <span className="text-indigo-400">360</span>
              </span>
              <span className="hidden sm:block text-[10px] text-gray-400 font-medium tracking-wider uppercase">
                Unified Portal & Safety Network
              </span>
            </div>
          </button>
        </div>

        {/* Right Actions */}
        <div className="flex items-center space-x-3">
          {/* Quick SOS Trigger Button (Available for Student / All) */}
          <button
            onClick={onOpenSOS}
            className="relative px-3.5 py-1.5 rounded-lg bg-gradient-to-r from-red-600 to-rose-600 text-white font-semibold text-xs flex items-center space-x-1.5 shadow-lg shadow-red-500/25 hover:shadow-red-500/40 hover:scale-[1.02] active:scale-[0.98] transition-all pulse-sos"
          >
            <ShieldAlert className="w-4 h-4 animate-bounce" />
            <span className="hidden sm:inline">SOS EMERGENCY</span>
          </button>

          {/* Notifications */}
          <div className="relative">
            <button
              onClick={() => setShowNotifications(!showNotifications)}
              className="p-2 text-gray-400 hover:text-white rounded-lg hover:bg-white/5 transition-colors relative"
            >
              <Bell size={20} />
              <span className="absolute top-1.5 right-1.5 w-2 h-2 bg-indigo-500 rounded-full animate-ping" />
              <span className="absolute top-1.5 right-1.5 w-2 h-2 bg-indigo-500 rounded-full" />
            </button>
            {showNotifications && (
              <NotificationDropdown onClose={() => setShowNotifications(false)} />
            )}
          </div>

          {/* User Role Badge & Profile */}
          {user && (
            <div className="flex items-center space-x-3 pl-2 border-l border-white/10">
              <div className="hidden md:flex flex-col items-end">
                <span className="text-sm font-semibold text-gray-200">{user.name}</span>
                <span className={`text-[10px] font-bold px-2 py-0.5 rounded-full bg-gradient-to-r ${roleColors[user.role] || 'from-gray-600 to-gray-700'} border shadow-sm`}>
                  {user.role}
                </span>
              </div>

              <div className="w-9 h-9 rounded-full bg-slate-800 border border-indigo-500/30 flex items-center justify-center text-indigo-300 font-bold text-sm">
                {user.name ? user.name.charAt(0).toUpperCase() : <User size={18} />}
              </div>

              <button
                onClick={logout}
                title="Logout"
                className="p-2 text-gray-400 hover:text-red-400 hover:bg-red-500/10 rounded-lg transition-colors"
              >
                <LogOut size={19} />
              </button>
            </div>
          )}
        </div>
      </div>
    </header>
  );
}

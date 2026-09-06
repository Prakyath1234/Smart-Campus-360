import React from 'react';
import { Link } from 'react-router-dom';
import { 
  Sparkles, 
  ShieldAlert, 
  GraduationCap, 
  BookOpen, 
  Building, 
  ShieldCheck, 
  ArrowRight, 
  CheckCircle2, 
  Radio 
} from 'lucide-react';
import { motion } from 'framer-motion';

export default function LandingPage() {
  return (
    <div className="min-h-screen bg-slate-950 text-gray-100 flex flex-col relative overflow-hidden">
      {/* Background Lights */}
      <div className="absolute top-0 left-1/2 -translate-x-1/2 w-[800px] h-[400px] bg-gradient-to-b from-indigo-600/20 via-purple-600/10 to-transparent blur-3xl pointer-events-none" />

      {/* Header */}
      <header className="glass-panel border-b border-white/10 px-6 py-4 sticky top-0 z-50">
        <div className="max-w-7xl mx-auto flex items-center justify-between">
          <div className="flex items-center space-x-3">
            <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-indigo-600 via-purple-600 to-pink-500 p-0.5 shadow-lg">
              <div className="w-full h-full bg-slate-950 rounded-[10px] flex items-center justify-center">
                <Sparkles className="w-5 h-5 text-indigo-400 animate-pulse" />
              </div>
            </div>
            <span className="text-xl font-bold bg-clip-text text-transparent bg-gradient-to-r from-white via-slate-200 to-indigo-300">
              Smart Campus <span className="text-indigo-400">360</span>
            </span>
          </div>

          <div className="flex items-center space-x-3">
            <Link
              to="/login"
              className="px-4 py-2 rounded-xl text-xs font-semibold text-gray-300 hover:text-white hover:bg-white/5 transition-colors"
            >
              Sign In
            </Link>
            <Link
              to="/register"
              className="px-4 py-2 rounded-xl bg-gradient-to-r from-indigo-600 to-purple-600 text-white text-xs font-bold shadow-lg shadow-indigo-600/30 hover:scale-[1.02] active:scale-[0.98] transition-all"
            >
              Get Started
            </Link>
          </div>
        </div>
      </header>

      {/* Hero Section */}
      <main className="flex-1 max-w-7xl mx-auto px-6 py-16 flex flex-col items-center text-center relative z-10">
        <motion.div
          initial={{ opacity: 0, y: 30 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6 }}
          className="space-y-6 max-w-3xl"
        >
          <div className="inline-flex items-center space-x-2 px-3 py-1 rounded-full bg-indigo-500/10 border border-indigo-500/20 text-indigo-300 text-xs font-semibold">
            <Sparkles size={14} className="text-indigo-400 animate-pulse" />
            <span>Next-Generation Unified Campus & Emergency Ecosystem</span>
          </div>

          <h1 className="text-4xl sm:text-6xl font-extrabold text-white tracking-tight leading-tight">
            Academic Management Meets <span className="bg-clip-text text-transparent bg-gradient-to-r from-indigo-400 via-purple-400 to-pink-400">Real-Time Safety</span>
          </h1>

          <p className="text-base sm:text-lg text-gray-300 leading-relaxed">
            Smart Campus 360 consolidates class timetables, attendance logs, grades, leave approvals, and AI grievance routing with a high-priority browser GPS Distress SOS telemetry feed for instant security response.
          </p>

          <div className="flex flex-wrap items-center justify-center gap-4 pt-4">
            <Link
              to="/login"
              className="px-8 py-3.5 rounded-2xl bg-gradient-to-r from-indigo-600 via-purple-600 to-pink-600 text-white font-bold text-sm shadow-xl shadow-indigo-600/30 hover:scale-[1.03] active:scale-[0.98] transition-all flex items-center space-x-2"
            >
              <span>Explore Portal Workspaces</span>
              <ArrowRight size={18} />
            </Link>
          </div>
        </motion.div>

        {/* Feature Cards Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mt-20 w-full text-left">
          <Link
            to="/login"
            className="glass-panel rounded-3xl p-6 border border-white/10 space-y-3 hover:border-emerald-500/50 hover:bg-emerald-500/5 transition-all group block cursor-pointer"
          >
            <div className="w-12 h-12 rounded-2xl bg-emerald-500/10 text-emerald-400 flex items-center justify-center border border-emerald-500/20 group-hover:scale-110 transition-transform">
              <GraduationCap size={24} />
            </div>
            <div className="flex items-center justify-between">
              <h3 className="text-base font-bold text-white group-hover:text-emerald-300 transition-colors">Student Workspace</h3>
              <ArrowRight size={14} className="text-gray-500 group-hover:text-emerald-400 transition-colors" />
            </div>
            <p className="text-xs text-gray-400 leading-relaxed">
              Track attendance percentage, view subject-wise marks, view weekly timetables, and submit leave applications.
            </p>
          </Link>

          <Link
            to="/login"
            className="glass-panel rounded-3xl p-6 border border-white/10 space-y-3 hover:border-blue-500/50 hover:bg-blue-500/5 transition-all group block cursor-pointer"
          >
            <div className="w-12 h-12 rounded-2xl bg-blue-500/10 text-blue-400 flex items-center justify-center border border-blue-500/20 group-hover:scale-110 transition-transform">
              <BookOpen size={24} />
            </div>
            <div className="flex items-center justify-between">
              <h3 className="text-base font-bold text-white group-hover:text-blue-300 transition-colors">Faculty Portal</h3>
              <ArrowRight size={14} className="text-gray-500 group-hover:text-blue-400 transition-colors" />
            </div>
            <p className="text-xs text-gray-400 leading-relaxed">
              Log daily class attendance with instant student toggles, record grades, and review leave applications.
            </p>
          </Link>

          <Link
            to="/login"
            className="glass-panel rounded-3xl p-6 border border-white/10 space-y-3 hover:border-purple-500/50 hover:bg-purple-500/5 transition-all group block cursor-pointer"
          >
            <div className="w-12 h-12 rounded-2xl bg-purple-500/10 text-purple-400 flex items-center justify-center border border-purple-500/20 group-hover:scale-110 transition-transform">
              <Building size={24} />
            </div>
            <div className="flex items-center justify-between">
              <h3 className="text-base font-bold text-white group-hover:text-purple-300 transition-colors">Admin Control</h3>
              <ArrowRight size={14} className="text-gray-500 group-hover:text-purple-400 transition-colors" />
            </div>
            <p className="text-xs text-gray-400 leading-relaxed">
              System analytics dashboard, department and subject registration, timetable assigner, and complaint resolution hub.
            </p>
          </Link>

          <Link
            to="/login"
            className="glass-panel rounded-3xl p-6 border border-red-500/30 space-y-3 bg-red-950/20 hover:border-red-500/60 hover:bg-red-900/20 transition-all group block cursor-pointer"
          >
            <div className="w-12 h-12 rounded-2xl bg-red-500/20 text-red-400 flex items-center justify-center border border-red-500/40 animate-pulse group-hover:scale-110 transition-transform">
              <ShieldAlert size={24} />
            </div>
            <div className="flex items-center justify-between">
              <h3 className="text-base font-bold text-white group-hover:text-red-300 transition-colors">Security Command</h3>
              <ArrowRight size={14} className="text-red-400 group-hover:translate-x-1 transition-transform" />
            </div>
            <p className="text-xs text-gray-400 leading-relaxed">
              Real-time SOS distress telemetry feed with live GPS coordinates, student contact dialer, and dispatch management.
            </p>
          </Link>
        </div>
      </main>

      {/* Footer */}
      <footer className="glass-panel border-t border-white/10 py-6 text-center text-xs text-gray-400">
        Smart Campus 360 System &copy; {new Date().getFullYear()} — Built with React & Spring Boot.
      </footer>
    </div>
  );
}

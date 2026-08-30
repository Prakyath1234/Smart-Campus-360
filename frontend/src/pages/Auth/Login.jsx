import React, { useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import { useNavigate, Link } from 'react-router-dom';
import { Sparkles, Mail, Lock, ArrowRight, ShieldAlert, Key } from 'lucide-react';
import { motion } from 'framer-motion';

export default function Login() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const user = await login(email, password);
      if (user && user.role === 'ADMIN') navigate('/admin');
      else if (user && user.role === 'FACULTY') navigate('/faculty');
      else if (user && user.role === 'SECURITY') navigate('/security');
      else navigate('/student');
    } catch (err) {
      setError(err.message || 'Invalid credentials');
    } finally {
      setLoading(false);
    }
  };

  const handleDemoLogin = (demoEmail) => {
    setEmail(demoEmail);
    setPassword('password');
  };

  return (
    <div className="min-h-screen flex items-center justify-center p-4 relative overflow-hidden bg-slate-950">
      {/* Background Animated Gradients */}
      <div className="absolute top-1/4 left-1/4 w-96 h-96 bg-indigo-600/20 rounded-full blur-3xl pointer-events-none animate-pulse" />
      <div className="absolute bottom-1/4 right-1/4 w-96 h-96 bg-purple-600/20 rounded-full blur-3xl pointer-events-none animate-pulse" />

      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.5 }}
        className="w-full max-w-md glass-panel rounded-3xl p-8 border border-white/10 shadow-2xl z-10"
      >
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-14 h-14 rounded-2xl bg-gradient-to-tr from-indigo-600 via-purple-600 to-pink-500 p-0.5 mb-3 shadow-lg shadow-indigo-500/30">
            <div className="w-full h-full bg-slate-950 rounded-[14px] flex items-center justify-center">
              <Sparkles className="w-7 h-7 text-indigo-400" />
            </div>
          </div>
          <h1 className="text-2xl font-bold text-white tracking-tight">Smart Campus 360</h1>
          <p className="text-xs text-gray-400 mt-1">Sign in to your role-based unified portal</p>
        </div>

        {error && (
          <div className="mb-4 p-3 rounded-xl bg-red-500/10 border border-red-500/30 text-red-400 text-xs flex items-center space-x-2">
            <ShieldAlert size={16} />
            <span>{error}</span>
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold text-gray-300 uppercase tracking-wider mb-1.5">
              Email Address
            </label>
            <div className="relative">
              <input
                type="email"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="name@sode-edu.in"
                className="w-full glass-input rounded-xl px-4 py-2.5 text-sm pl-10"
              />
              <Mail className="w-4 h-4 text-gray-400 absolute left-3.5 top-3" />
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-gray-300 uppercase tracking-wider mb-1.5">
              Password
            </label>
            <div className="relative">
              <input
                type="password"
                required
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="••••••••"
                className="w-full glass-input rounded-xl px-4 py-2.5 text-sm pl-10"
              />
              <Lock className="w-4 h-4 text-gray-400 absolute left-3.5 top-3" />
            </div>
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full py-3 rounded-xl bg-gradient-to-r from-indigo-600 via-purple-600 to-pink-600 text-white font-bold text-sm shadow-xl shadow-indigo-600/30 hover:scale-[1.01] active:scale-[0.99] transition-all flex items-center justify-center space-x-2"
          >
            <span>{loading ? 'Authenticating...' : 'Sign In'}</span>
            <ArrowRight size={16} />
          </button>
        </form>

        {/* Demo Fast Autofill Credentials */}
        <div className="mt-6 pt-6 border-t border-white/10">
          <div className="flex items-center space-x-1.5 text-xs text-gray-400 font-semibold mb-3">
            <Key size={14} className="text-indigo-400" />
            <span>Quick Demo Auto-fill (@sode-edu.in):</span>
          </div>

          <div className="grid grid-cols-2 gap-2">
            <button
              onClick={() => handleDemoLogin('student@sode-edu.in')}
              className="px-3 py-1.5 rounded-lg glass-card text-[11px] font-semibold text-emerald-300 hover:bg-emerald-500/10 border-emerald-500/20 text-left"
            >
              🎓 Student
            </button>
            <button
              onClick={() => handleDemoLogin('faculty@sode-edu.in')}
              className="px-3 py-1.5 rounded-lg glass-card text-[11px] font-semibold text-blue-300 hover:bg-blue-500/10 border-blue-500/20 text-left"
            >
              👨‍🏫 Faculty
            </button>
            <button
              onClick={() => handleDemoLogin('admin@sode-edu.in')}
              className="px-3 py-1.5 rounded-lg glass-card text-[11px] font-semibold text-purple-300 hover:bg-purple-500/10 border-purple-500/20 text-left"
            >
              👑 Admin
            </button>
            <button
              onClick={() => handleDemoLogin('security@sode-edu.in')}
              className="px-3 py-1.5 rounded-lg glass-card text-[11px] font-semibold text-rose-300 hover:bg-rose-500/10 border-rose-500/20 text-left"
            >
              🛡️ Security
            </button>
          </div>
        </div>

        <p className="text-center text-xs text-gray-400 mt-6">
          Don't have an account?{' '}
          <Link to="/register" className="text-indigo-400 hover:underline font-semibold">
            Register here
          </Link>
        </p>
      </motion.div>
    </div>
  );
}

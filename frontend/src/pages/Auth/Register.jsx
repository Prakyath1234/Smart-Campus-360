import React, { useState, useEffect } from 'react';
import { api } from '../../api';
import { useNavigate, Link } from 'react-router-dom';
import { Sparkles, Mail, Lock, User, Phone, ArrowRight, ShieldAlert } from 'lucide-react';
import { motion } from 'framer-motion';

export default function Register() {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    password: '',
    phone: '',
    role: 'STUDENT',
    departmentId: '',
    rollNumber: '',
    employeeId: '',
    semester: 5,
    designation: 'Assistant Professor',
  });
  const [departments, setDepartments] = useState([]);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    loadDepartments();
  }, []);

  const loadDepartments = async () => {
    try {
      const data = await api.getDepartments();
      setDepartments(data || []);
      if (data && data.length > 0) {
        setFormData((prev) => ({ ...prev, departmentId: data[0].id }));
      }
    } catch (err) {
      console.error(err);
    }
  };

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const payload = {
        name: formData.name,
        email: formData.email,
        password: formData.password,
        phone: formData.phone,
        role: formData.role,
        departmentId: formData.departmentId ? Number(formData.departmentId) : null,
      };

      if (formData.role === 'STUDENT') {
        payload.rollNumber = formData.rollNumber || `CS${Date.now().toString().slice(-6)}`;
        payload.semester = Number(formData.semester);
      } else if (formData.role === 'FACULTY') {
        payload.employeeId = formData.employeeId || `EMP${Date.now().toString().slice(-6)}`;
        payload.designation = formData.designation;
      }

      await api.register(payload);
      navigate('/login');
    } catch (err) {
      setError(err.message || 'Registration failed.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center p-4 relative overflow-hidden bg-slate-950">
      <div className="absolute top-1/4 left-1/4 w-96 h-96 bg-indigo-600/20 rounded-full blur-3xl pointer-events-none" />
      <div className="absolute bottom-1/4 right-1/4 w-96 h-96 bg-purple-600/20 rounded-full blur-3xl pointer-events-none" />

      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.5 }}
        className="w-full max-w-lg glass-panel rounded-3xl p-8 border border-white/10 shadow-2xl z-10 my-8"
      >
        <div className="text-center mb-6">
          <div className="inline-flex items-center justify-center w-12 h-12 rounded-2xl bg-gradient-to-tr from-indigo-600 via-purple-600 to-pink-500 p-0.5 mb-2 shadow-lg">
            <div className="w-full h-full bg-slate-950 rounded-[14px] flex items-center justify-center">
              <Sparkles className="w-6 h-6 text-indigo-400" />
            </div>
          </div>
          <h1 className="text-2xl font-bold text-white tracking-tight">Create Account</h1>
          <p className="text-xs text-gray-400 mt-1">Register for Smart Campus 360 unified portal</p>
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
              Select Role
            </label>
            <div className="grid grid-cols-4 gap-2">
              {['STUDENT', 'FACULTY', 'ADMIN', 'SECURITY'].map((role) => (
                <button
                  key={role}
                  type="button"
                  onClick={() => setFormData({ ...formData, role })}
                  className={`py-2 text-[11px] font-bold rounded-xl border transition-all ${
                    formData.role === role
                      ? 'bg-gradient-to-r from-indigo-600 to-purple-600 text-white border-indigo-400 shadow-md'
                      : 'glass-card text-gray-400 border-white/10 hover:text-white'
                  }`}
                >
                  {role}
                </button>
              ))}
            </div>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-gray-300 uppercase tracking-wider mb-1.5">
                Full Name
              </label>
              <div className="relative">
                <input
                  type="text"
                  name="name"
                  required
                  value={formData.name}
                  onChange={handleChange}
                  placeholder="John Doe"
                  className="w-full glass-input rounded-xl px-4 py-2 text-sm pl-10"
                />
                <User className="w-4 h-4 text-gray-400 absolute left-3.5 top-2.5" />
              </div>
            </div>

            <div>
              <label className="block text-xs font-semibold text-gray-300 uppercase tracking-wider mb-1.5">
                Phone Number
              </label>
              <div className="relative">
                <input
                  type="text"
                  name="phone"
                  required
                  value={formData.phone}
                  onChange={handleChange}
                  placeholder="9876543210"
                  className="w-full glass-input rounded-xl px-4 py-2 text-sm pl-10"
                />
                <Phone className="w-4 h-4 text-gray-400 absolute left-3.5 top-2.5" />
              </div>
            </div>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-gray-300 uppercase tracking-wider mb-1.5">
                Email Address
              </label>
              <div className="relative">
                <input
                  type="email"
                  name="email"
                  required
                  value={formData.email}
                  onChange={handleChange}
                  placeholder="user@sode-edu.in"
                  className="w-full glass-input rounded-xl px-4 py-2 text-sm pl-10"
                />
                <Mail className="w-4 h-4 text-gray-400 absolute left-3.5 top-2.5" />
              </div>
            </div>

            <div>
              <label className="block text-xs font-semibold text-gray-300 uppercase tracking-wider mb-1.5">
                Password
              </label>
              <div className="relative">
                <input
                  type="password"
                  name="password"
                  required
                  value={formData.password}
                  onChange={handleChange}
                  placeholder="••••••••"
                  className="w-full glass-input rounded-xl px-4 py-2 text-sm pl-10"
                />
                <Lock className="w-4 h-4 text-gray-400 absolute left-3.5 top-2.5" />
              </div>
            </div>
          </div>

          {(formData.role === 'STUDENT' || formData.role === 'FACULTY') && (
            <div>
              <label className="block text-xs font-semibold text-gray-300 uppercase tracking-wider mb-1.5">
                Department
              </label>
              <select
                name="departmentId"
                value={formData.departmentId}
                onChange={handleChange}
                className="w-full glass-input rounded-xl px-4 py-2 text-sm"
              >
                {departments.map((d) => (
                  <option key={d.id} value={d.id} className="bg-slate-900 text-gray-200">
                    {d.name} ({d.code})
                  </option>
                ))}
              </select>
            </div>
          )}

          {formData.role === 'STUDENT' && (
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-xs font-semibold text-gray-300 uppercase tracking-wider mb-1.5">
                  Roll Number
                </label>
                <input
                  type="text"
                  name="rollNumber"
                  value={formData.rollNumber}
                  onChange={handleChange}
                  placeholder="CS2026001"
                  className="w-full glass-input rounded-xl px-4 py-2 text-sm"
                />
              </div>
              <div>
                <label className="block text-xs font-semibold text-gray-300 uppercase tracking-wider mb-1.5">
                  Semester
                </label>
                <select
                  name="semester"
                  value={formData.semester}
                  onChange={handleChange}
                  className="w-full glass-input rounded-xl px-4 py-2 text-sm"
                >
                  {[1, 2, 3, 4, 5, 6, 7, 8].map((s) => (
                    <option key={s} value={s} className="bg-slate-900 text-gray-200">
                      Semester {s}
                    </option>
                  ))}
                </select>
              </div>
            </div>
          )}

          <button
            type="submit"
            disabled={loading}
            className="w-full py-3 rounded-xl bg-gradient-to-r from-indigo-600 via-purple-600 to-pink-600 text-white font-bold text-sm shadow-xl shadow-indigo-600/30 hover:scale-[1.01] active:scale-[0.99] transition-all flex items-center justify-center space-x-2 mt-4"
          >
            <span>{loading ? 'Creating Account...' : 'Complete Registration'}</span>
            <ArrowRight size={16} />
          </button>
        </form>

        <p className="text-center text-xs text-gray-400 mt-6">
          Already registered?{' '}
          <Link to="/login" className="text-indigo-400 hover:underline font-semibold">
            Sign In
          </Link>
        </p>
      </motion.div>
    </div>
  );
}

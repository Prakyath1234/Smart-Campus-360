import React from 'react';
import { useAuth } from '../context/AuthContext';
import { 
  LayoutDashboard, 
  GraduationCap, 
  Calendar, 
  FileText, 
  AlertCircle, 
  ShieldAlert, 
  BookOpen, 
  Users, 
  Building, 
  ClipboardCheck, 
  Radio,
  Wrench,
  Calculator,
  FileCheck
} from 'lucide-react';

export default function Sidebar({ isOpen, activeTab, setActiveTab }) {
  const { user } = useAuth();
  if (!user) return null;

  const roleNavItems = {
    STUDENT: [
      { id: 'overview', label: 'Dashboard Overview', icon: LayoutDashboard },
      { id: 'academic', label: 'Attendance & Marks', icon: GraduationCap },
      { id: 'calculator', label: 'Academic Calculator', icon: Calculator },
      { id: 'ats', label: 'ATS Resume Analyzer', icon: FileCheck },
      { id: 'timetable', label: 'Class Timetable', icon: Calendar },
      { id: 'leave', label: 'Leave Requests', icon: FileText },
      { id: 'complaints', label: 'Grievance & Complaints', icon: AlertCircle },
      { id: 'sos', label: 'SOS Distress Signal', icon: ShieldAlert },
      { id: 'smarttools', label: 'SmartTools Utilities', icon: Wrench },
    ],
    FACULTY: [
      { id: 'overview', label: 'Faculty Overview', icon: LayoutDashboard },
      { id: 'attendance', label: 'Mark Attendance', icon: ClipboardCheck },
      { id: 'grades', label: 'Record Student Grades', icon: BookOpen },
      { id: 'leave_approvals', label: 'Review Leave Requests', icon: FileText },
      { id: 'smarttools', label: 'SmartTools Utilities', icon: Wrench },
    ],
    ADMIN: [
      { id: 'overview', label: 'System Analytics', icon: LayoutDashboard },
      { id: 'departments', label: 'Departments & Subjects', icon: Building },
      { id: 'timetable_manage', label: 'Schedule Timetable', icon: Calendar },
      { id: 'complaint_hub', label: 'Complaint Resolution Hub', icon: AlertCircle },
      { id: 'audit_logs', label: 'Security Audit Logs', icon: FileText },
      { id: 'smarttools', label: 'SmartTools Utilities', icon: Wrench },
    ],
    SECURITY: [
      { id: 'sos_feed', label: 'Live SOS Emergency Feed', icon: Radio },
      { id: 'overview', label: 'Security Stats', icon: LayoutDashboard },
      { id: 'smarttools', label: 'SmartTools Utilities', icon: Wrench },
    ],
  };

  const navItems = roleNavItems[user.role] || roleNavItems.STUDENT;

  return (
    <aside
      className={`fixed lg:static top-16 left-0 z-30 w-64 h-[calc(100vh-4rem)] glass-panel border-r border-white/10 p-4 transition-all duration-300 transform ${
        isOpen ? 'translate-x-0' : '-translate-x-full lg:translate-x-0'
      }`}
    >
      <div className="flex flex-col h-full justify-between">
        <div className="space-y-1.5">
          <div className="px-3 py-2 text-xs font-semibold text-gray-400 uppercase tracking-wider">
            {user.role} Navigation
          </div>

          {navItems.map((item) => {
            const Icon = item.icon;
            const isActive = activeTab === item.id;
            return (
              <button
                key={item.id}
                onClick={() => {
                  if (item.id === 'smarttools') {
                    window.location.href = '/tools';
                  } else {
                    setActiveTab(item.id);
                  }
                }}
                className={`w-full flex items-center space-x-3 px-3.5 py-2.5 rounded-xl font-medium text-sm transition-all duration-200 ${
                  isActive
                    ? 'bg-gradient-to-r from-indigo-600/80 to-purple-600/80 text-white shadow-lg shadow-indigo-500/20 border border-indigo-400/30'
                    : 'text-gray-400 hover:text-gray-100 hover:bg-white/5'
                }`}
              >
                <Icon className={`w-5 h-5 ${isActive ? 'text-indigo-200' : 'text-gray-400'}`} />
                <span>{item.label}</span>
              </button>
            );
          })}
        </div>

        {/* User Card info inside sidebar bottom */}
        <div className="glass-card rounded-xl p-3 border border-white/5 bg-slate-900/60">
          <div className="text-xs text-gray-400">Logged in as</div>
          <div className="text-sm font-semibold text-indigo-300 truncate">{user.email}</div>
          <div className="text-[11px] text-gray-400 mt-0.5">Role ID: #{user.id}</div>
        </div>
      </div>
    </aside>
  );
}

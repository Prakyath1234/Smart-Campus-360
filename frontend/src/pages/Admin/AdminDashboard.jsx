import React, { useState, useEffect } from 'react';
import { api } from '../../api';
import Navbar from '../../components/Navbar';
import Sidebar from '../../components/Sidebar';
import SOSModal from '../../components/SOSModal';
import { CheckCircle2 } from 'lucide-react';
import { motion } from 'framer-motion';

export default function AdminDashboard() {
  const [activeTab, setActiveTab] = useState('overview');
  const [isSidebarOpen, setIsSidebarOpen] = useState(false);
  const [isSOSOpen, setIsSOSOpen] = useState(false);

  const [stats, setStats] = useState(null);
  const [departments, setDepartments] = useState([]);
  const [subjects, setSubjects] = useState([]);
  const [complaints, setComplaints] = useState([]);

  // Audit Log states
  const [auditLogs, setAuditLogs] = useState([]);
  const [auditPage, setAuditPage] = useState(0);
  const [auditTotalPages, setAuditTotalPages] = useState(0);
  const [auditFilterActor, setAuditFilterActor] = useState('');
  const [auditFilterAction, setAuditFilterAction] = useState('');
  const [auditFilterEntity, setAuditFilterEntity] = useState('');
  const [auditLoading, setAuditLoading] = useState(false);

  const loadAuditLogs = async (page = 0) => {
    setAuditLoading(true);
    try {
      const res = await api.getAuditLogs(page, 15, auditFilterActor, auditFilterAction, auditFilterEntity);
      setAuditLogs(res.content || []);
      setAuditPage(res.number || 0);
      setAuditTotalPages(res.totalPages || 0);
    } catch (err) {
      console.error('Failed to load audit logs:', err);
    } finally {
      setAuditLoading(false);
    }
  };

  useEffect(() => {
    if (activeTab === 'audit_logs') {
      loadAuditLogs(0);
    }
  }, [activeTab, auditFilterActor, auditFilterAction, auditFilterEntity]);

  // Forms
  const [deptForm, setDeptForm] = useState({ name: '', code: '' });
  const [subjForm, setSubjForm] = useState({ name: '', code: '', departmentId: '', facultyId: '', credits: 3 });
  const [timeForm, setTimeForm] = useState({ dayOfWeek: 'MONDAY', startTime: '09:00', endTime: '10:00', subjectId: '', classroom: 'Lab 1', semester: 5 });
  const [msg, setMsg] = useState({ type: '', text: '' });

  const loadAdminData = async () => {
    try {
      const [statsData, deptData, subjData, compData] = await Promise.all([
        api.getAdminStats().catch(() => null),
        api.getDepartments().catch(() => []),
        api.getSubjects().catch(() => []),
        api.getAdminComplaints().catch(() => []),
      ]);

      setStats(statsData);
      setDepartments(deptData || []);
      setSubjects(subjData || []);
      setComplaints(compData || []);

      if (deptData && deptData.length > 0) {
        setSubjForm((prev) => ({ ...prev, departmentId: deptData[0].id }));
      }
      if (subjData && subjData.length > 0) {
        setTimeForm((prev) => ({ ...prev, subjectId: subjData[0].id }));
      }
    } catch (err) {
      console.error(err);
    }
  };

  useEffect(() => {
    loadAdminData();
  }, []);

  const handleCreateDepartment = async (e) => {
    e.preventDefault();
    try {
      await api.createDepartment(deptForm);
      setMsg({ type: 'success', text: 'Department registered successfully!' });
      setDeptForm({ name: '', code: '' });
      loadAdminData();
    } catch (err) {
      setMsg({ type: 'error', text: err.message || 'Failed to create department.' });
    }
  };

  const handleCreateSubject = async (e) => {
    e.preventDefault();
    try {
      await api.createSubject({
        name: subjForm.name,
        code: subjForm.code,
        departmentId: Number(subjForm.departmentId),
        facultyId: subjForm.facultyId ? Number(subjForm.facultyId) : null,
        credits: Number(subjForm.credits),
      });
      setMsg({ type: 'success', text: 'Subject created successfully!' });
      setSubjForm({ name: '', code: '', departmentId: departments[0]?.id || '', facultyId: '', credits: 3 });
      loadAdminData();
    } catch (err) {
      setMsg({ type: 'error', text: err.message || 'Failed to create subject.' });
    }
  };

  const handleCreateTimetable = async (e) => {
    e.preventDefault();
    try {
      await api.createTimetableSlot({
        dayOfWeek: timeForm.dayOfWeek,
        startTime: `${timeForm.startTime}:00`,
        endTime: `${timeForm.endTime}:00`,
        subjectId: Number(timeForm.subjectId),
        classroom: timeForm.classroom,
        semester: Number(timeForm.semester),
      });
      setMsg({ type: 'success', text: 'Timetable slot assigned!' });
    } catch (err) {
      setMsg({ type: 'error', text: err.message || 'Schedule conflict or error.' });
    }
  };

  const handleResolveComplaint = async (id) => {
    try {
      await api.updateComplaintStatus(id, 'RESOLVED', 'Resolved by campus administrator');
      setMsg({ type: 'success', text: 'Complaint marked RESOLVED!' });
      const updated = await api.getAdminComplaints();
      setComplaints(updated || []);
    } catch (err) {
      setMsg({ type: 'error', text: err.message || 'Failed to resolve complaint.' });
    }
  };

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
          <div className="glass-panel rounded-3xl p-6 mb-8 border border-white/10 bg-gradient-to-r from-purple-900/40 via-indigo-900/30 to-slate-900">
            <span className="text-xs font-bold text-purple-400 uppercase tracking-widest">Admin Control Center</span>
            <h1 className="text-2xl sm:text-3xl font-extrabold text-white mt-1">
              System Administration 👑
            </h1>
          </div>

          {msg.text && (
            <div className={`mb-6 p-4 rounded-2xl border text-xs flex items-center justify-between ${
              msg.type === 'success' ? 'bg-emerald-500/10 border-emerald-500/30 text-emerald-300' : 'bg-red-500/10 border-red-500/30 text-red-300'
            }`}>
              <span>{msg.text}</span>
              <button onClick={() => setMsg({ type: '', text: '' })} className="font-bold underline">Dismiss</button>
            </div>
          )}

          {/* OVERVIEW STATS */}
          {(activeTab === 'overview') && (
            <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="space-y-8">
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
                <div className="glass-card rounded-2xl p-5 border border-white/10">
                  <div className="text-xs text-gray-400 font-medium">TOTAL STUDENTS</div>
                  <div className="text-3xl font-bold text-indigo-400 mt-1">{stats?.totalStudents || 1}</div>
                </div>

                <div className="glass-card rounded-2xl p-5 border border-white/10">
                  <div className="text-xs text-gray-400 font-medium">TOTAL FACULTY</div>
                  <div className="text-3xl font-bold text-purple-400 mt-1">{stats?.totalFaculties || 1}</div>
                </div>

                <div className="glass-card rounded-2xl p-5 border border-white/10">
                  <div className="text-xs text-gray-400 font-medium">DEPARTMENTS</div>
                  <div className="text-3xl font-bold text-blue-400 mt-1">{stats?.totalDepartments || 3}</div>
                </div>

                <div className="glass-card rounded-2xl p-5 border border-white/10">
                  <div className="text-xs text-gray-400 font-medium">OPEN COMPLAINTS</div>
                  <div className="text-3xl font-bold text-amber-400 mt-1">{stats?.openComplaints || complaints.length}</div>
                </div>
              </div>
            </motion.div>
          )}

          {/* DEPARTMENTS & SUBJECTS */}
          {activeTab === 'departments' && (
            <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="grid grid-cols-1 lg:grid-cols-2 gap-8">
              {/* Register Dept */}
              <div className="glass-panel rounded-3xl p-6 border border-white/10">
                <h3 className="text-base font-bold text-white mb-4">Register Department</h3>
                <form onSubmit={handleCreateDepartment} className="space-y-4">
                  <div>
                    <label className="block text-xs font-semibold text-gray-300 uppercase mb-1">Department Name</label>
                    <input
                      type="text"
                      required
                      value={deptForm.name}
                      onChange={(e) => setDeptForm({ ...deptForm, name: e.target.value })}
                      placeholder="e.g. Electrical Engineering"
                      className="w-full glass-input rounded-xl p-2.5 text-xs"
                    />
                  </div>
                  <div>
                    <label className="block text-xs font-semibold text-gray-300 uppercase mb-1">Department Code</label>
                    <input
                      type="text"
                      required
                      value={deptForm.code}
                      onChange={(e) => setDeptForm({ ...deptForm, code: e.target.value })}
                      placeholder="e.g. EE"
                      className="w-full glass-input rounded-xl p-2.5 text-xs"
                    />
                  </div>
                  <button type="submit" className="w-full py-2.5 rounded-xl bg-gradient-to-r from-purple-600 to-indigo-600 text-white font-bold text-xs shadow-lg">
                    Add Department
                  </button>
                </form>
              </div>

              {/* Register Subject */}
              <div className="glass-panel rounded-3xl p-6 border border-white/10">
                <h3 className="text-base font-bold text-white mb-4">Create Subject Course</h3>
                <form onSubmit={handleCreateSubject} className="space-y-4">
                  <div>
                    <label className="block text-xs font-semibold text-gray-300 uppercase mb-1">Subject Name</label>
                    <input
                      type="text"
                      required
                      value={subjForm.name}
                      onChange={(e) => setSubjForm({ ...subjForm, name: e.target.value })}
                      placeholder="e.g. Data Structures"
                      className="w-full glass-input rounded-xl p-2.5 text-xs"
                    />
                  </div>
                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <label className="block text-xs font-semibold text-gray-300 uppercase mb-1">Code</label>
                      <input
                        type="text"
                        required
                        value={subjForm.code}
                        onChange={(e) => setSubjForm({ ...subjForm, code: e.target.value })}
                        placeholder="CS301"
                        className="w-full glass-input rounded-xl p-2.5 text-xs"
                      />
                    </div>
                    <div>
                      <label className="block text-xs font-semibold text-gray-300 uppercase mb-1">Department</label>
                      <select
                        value={subjForm.departmentId}
                        onChange={(e) => setSubjForm({ ...subjForm, departmentId: e.target.value })}
                        className="w-full glass-input rounded-xl p-2.5 text-xs"
                      >
                        {departments.map((d) => (
                          <option key={d.id} value={d.id} className="bg-slate-900">{d.name}</option>
                        ))}
                      </select>
                    </div>
                  </div>
                  <button type="submit" className="w-full py-2.5 rounded-xl bg-gradient-to-r from-indigo-600 to-blue-600 text-white font-bold text-xs shadow-lg">
                    Create Subject
                  </button>
                </form>
              </div>
            </motion.div>
          )}

          {/* TIMETABLE MANAGE */}
          {activeTab === 'timetable_manage' && (
            <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="glass-panel rounded-3xl p-6 border border-white/10 max-w-2xl mx-auto">
              <h3 className="text-base font-bold text-white mb-4">Assign Timetable Slot</h3>
              <form onSubmit={handleCreateTimetable} className="space-y-4">
                <div>
                  <label className="block text-xs font-semibold text-gray-300 uppercase mb-1">Subject</label>
                  <select
                    value={timeForm.subjectId}
                    onChange={(e) => setTimeForm({ ...timeForm, subjectId: e.target.value })}
                    className="w-full glass-input rounded-xl p-2.5 text-xs"
                  >
                    {subjects.map((s) => (
                      <option key={s.id} value={s.id} className="bg-slate-900">{s.name} ({s.code})</option>
                    ))}
                  </select>
                </div>
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-xs font-semibold text-gray-300 uppercase mb-1">Day</label>
                    <select
                      value={timeForm.dayOfWeek}
                      onChange={(e) => setTimeForm({ ...timeForm, dayOfWeek: e.target.value })}
                      className="w-full glass-input rounded-xl p-2.5 text-xs"
                    >
                      {['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY'].map((d) => (
                        <option key={d} value={d} className="bg-slate-900">{d}</option>
                      ))}
                    </select>
                  </div>
                  <div>
                    <label className="block text-xs font-semibold text-gray-300 uppercase mb-1">Classroom</label>
                    <input
                      type="text"
                      required
                      value={timeForm.classroom}
                      onChange={(e) => setTimeForm({ ...timeForm, classroom: e.target.value })}
                      className="w-full glass-input rounded-xl p-2.5 text-xs"
                    />
                  </div>
                </div>
                <button type="submit" className="w-full py-2.5 rounded-xl bg-gradient-to-r from-purple-600 to-indigo-600 text-white font-bold text-xs shadow-lg">
                  Assign Schedule
                </button>
              </form>
            </motion.div>
          )}

          {/* COMPLAINT HUB */}
          {activeTab === 'complaint_hub' && (
            <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="glass-panel rounded-3xl p-6 border border-white/10">
              <h3 className="text-lg font-bold text-white mb-4">Grievance Ticket Resolution Hub</h3>
              <div className="space-y-3">
                {complaints.map((c) => (
                  <div key={c.id} className="glass-card rounded-2xl p-4 border border-white/10 flex items-start justify-between">
                    <div>
                      <div className="flex items-center space-x-2">
                        <span className="font-bold text-sm text-white">{c.title}</span>
                        <span className="text-[10px] font-bold px-2 py-0.5 rounded bg-indigo-500/20 text-indigo-300">{c.category}</span>
                      </div>
                      <p className="text-xs text-gray-300 mt-1">{c.description}</p>
                      <div className="text-[10px] text-gray-400 mt-2">Student: {c.student?.user?.name || 'John Doe'}</div>
                    </div>

                    {c.status !== 'RESOLVED' ? (
                      <button
                        onClick={() => handleResolveComplaint(c.id)}
                        className="px-3 py-1.5 bg-emerald-600 text-white rounded-lg font-bold text-xs"
                      >
                        Resolve Ticket
                      </button>
                    ) : (
                      <span className="text-xs font-bold text-emerald-400 flex items-center space-x-1">
                        <CheckCircle2 size={14} />
                        <span>RESOLVED</span>
                      </span>
                    )}
                  </div>
                ))}
              </div>
            </motion.div>
          )}

          {/* TAB: AUDIT LOGS */}
          {activeTab === 'audit_logs' && (
            <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="glass-panel rounded-3xl p-6 border border-white/10 space-y-6">
              <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
                <div>
                  <h3 className="text-lg font-bold text-white">System Security Audit Logs</h3>
                  <p className="text-xs text-gray-400">View real-time event logs of sensitive operations across the campus management suite.</p>
                </div>
              </div>

              {/* Filters */}
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <input
                  type="text"
                  placeholder="Filter by Actor Email..."
                  value={auditFilterActor}
                  onChange={(e) => setAuditFilterActor(e.target.value)}
                  className="px-3 py-2 rounded-xl bg-slate-950 border border-white/10 text-xs text-gray-200 focus:outline-none focus:border-indigo-500"
                />
                <input
                  type="text"
                  placeholder="Filter by Action (e.g. LOGIN)..."
                  value={auditFilterAction}
                  onChange={(e) => setAuditFilterAction(e.target.value)}
                  className="px-3 py-2 rounded-xl bg-slate-950 border border-white/10 text-xs text-gray-200 focus:outline-none focus:border-indigo-500"
                />
                <input
                  type="text"
                  placeholder="Filter by Entity Type..."
                  value={auditFilterEntity}
                  onChange={(e) => setAuditFilterEntity(e.target.value)}
                  className="px-3 py-2 rounded-xl bg-slate-950 border border-white/10 text-xs text-gray-200 focus:outline-none focus:border-indigo-500"
                />
              </div>

              {/* Table */}
              <div className="overflow-x-auto">
                <table className="w-full text-xs text-left border-collapse">
                  <thead>
                    <tr className="border-b border-white/10 text-gray-400">
                      <th className="py-3 px-4">Timestamp</th>
                      <th className="py-3 px-4">Actor</th>
                      <th className="py-3 px-4">Role</th>
                      <th className="py-3 px-4">Action</th>
                      <th className="py-3 px-4">Entity</th>
                      <th className="py-3 px-4">Entity ID</th>
                      <th className="py-3 px-4">Description</th>
                    </tr>
                  </thead>
                  <tbody>
                    {auditLogs.length > 0 ? (
                      auditLogs.map((log) => (
                        <tr key={log.id} className="border-b border-white/5 hover:bg-white/5 transition-colors text-gray-300">
                          <td className="py-3 px-4 whitespace-nowrap">{new Date(log.timestamp).toLocaleString()}</td>
                          <td className="py-3 px-4 font-semibold text-indigo-300">{log.actor}</td>
                          <td className="py-3 px-4"><span className="px-2 py-0.5 rounded bg-slate-800 text-[10px]">{log.actorRole}</span></td>
                          <td className="py-3 px-4"><span className="px-2 py-0.5 rounded bg-blue-500/10 text-blue-300 font-bold border border-blue-500/20">{log.action}</span></td>
                          <td className="py-3 px-4 text-gray-400">{log.entityType}</td>
                          <td className="py-3 px-4 text-gray-400 font-mono">{log.entityId}</td>
                          <td className="py-3 px-4 text-gray-200 max-w-xs truncate">{log.description}</td>
                        </tr>
                      ))
                    ) : (
                      <tr>
                        <td colSpan="7" className="py-8 text-center text-gray-500">No matching security audit logs found.</td>
                      </tr>
                    )}
                  </tbody>
                </table>
              </div>

              {/* Pagination */}
              {auditTotalPages > 1 && (
                <div className="flex justify-between items-center text-xs text-gray-400 border-t border-white/10 pt-4">
                  <span>Page {auditPage + 1} of {auditTotalPages}</span>
                  <div className="flex space-x-2">
                    <button
                      disabled={auditPage === 0 || auditLoading}
                      onClick={() => loadAuditLogs(auditPage - 1)}
                      className="px-3 py-1.5 rounded-lg bg-slate-800 text-white font-bold disabled:opacity-50 hover:bg-slate-700"
                    >
                      Previous
                    </button>
                    <button
                      disabled={auditPage + 1 >= auditTotalPages || auditLoading}
                      onClick={() => loadAuditLogs(auditPage + 1)}
                      className="px-3 py-1.5 rounded-lg bg-slate-800 text-white font-bold disabled:opacity-50 hover:bg-slate-700"
                    >
                      Next
                    </button>
                  </div>
                </div>
              )}
            </motion.div>
          )}
        </main>
      </div>

      <SOSModal isOpen={isSOSOpen} onClose={() => setIsSOSOpen(false)} />
    </div>
  );
}

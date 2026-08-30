import React, { useState, useEffect } from 'react';
import { api } from '../../api';
import Navbar from '../../components/Navbar';
import Sidebar from '../../components/Sidebar';
import SOSModal from '../../components/SOSModal';
import { 
  Check, 
  X, 
  Save
} from 'lucide-react';
import { motion } from 'framer-motion';

export default function FacultyDashboard() {
  const [activeTab, setActiveTab] = useState('overview');
  const [isSidebarOpen, setIsSidebarOpen] = useState(false);
  const [isSOSOpen, setIsSOSOpen] = useState(false);

  const [profile, setProfile] = useState(null);
  const [subjects, setSubjects] = useState([]);
  const [selectedSubjectId, setSelectedSubjectId] = useState('');
  const [students, setStudents] = useState([]);
  const [attendanceDate, setAttendanceDate] = useState(new Date().toISOString().split('T')[0]);
  const [attendanceState, setAttendanceState] = useState({});
  const [marksState, setMarksState] = useState({});
  const [leaveRequests, setLeaveRequests] = useState([]);
  const [msg, setMsg] = useState({ type: '', text: '' });

  const loadFacultyData = async () => {
    try {
      const [profData, subjData, leaveData] = await Promise.all([
        api.getFacultyProfile().catch(() => null),
        api.getFacultySubjects().catch(() => []),
        api.getFacultyLeaveRequests().catch(() => []),
      ]);

      setProfile(profData);
      setSubjects(subjData || []);
      setLeaveRequests(leaveData || []);

      if (subjData && subjData.length > 0) {
        setSelectedSubjectId(subjData[0].id);
        fetchStudentsForSubject(subjData[0].id);
      }
    } catch (err) {
      console.error(err);
    }
  };

  useEffect(() => {
    loadFacultyData();
  }, []);

  const fetchStudentsForSubject = async (subjectId) => {
    try {
      const data = await api.getFacultyStudents(subjectId);
      setStudents(data || []);
      const attMap = {};
      const marksMap = {};
      (data || []).forEach((s) => {
        attMap[s.id] = 'PRESENT';
        marksMap[s.id] = { internalMarks: 18, assignmentMarks: 9, examMarks: 65 };
      });
      setAttendanceState(attMap);
      setMarksState(marksMap);
    } catch (err) {
      console.error(err);
    }
  };

  const handleSubjectChange = (e) => {
    const id = e.target.value;
    setSelectedSubjectId(id);
    fetchStudentsForSubject(id);
  };

  const handleSaveAttendance = async () => {
    if (!selectedSubjectId) return;
    try {
      const records = Object.keys(attendanceState).map((studentId) => ({
        studentId: Number(studentId),
        subjectId: Number(selectedSubjectId),
        date: attendanceDate,
        status: attendanceState[studentId],
      }));
      await api.saveAttendance(records);
      setMsg({ type: 'success', text: 'Attendance logged successfully!' });
    } catch (err) {
      setMsg({ type: 'error', text: err.message || 'Failed to save attendance.' });
    }
  };

  const handleSaveMarks = async (studentId) => {
    if (!selectedSubjectId) return;
    try {
      const markObj = marksState[studentId];
      await api.saveMarks({
        studentId: Number(studentId),
        subjectId: Number(selectedSubjectId),
        internalMarks: Number(markObj.internalMarks),
        assignmentMarks: Number(markObj.assignmentMarks),
        examMarks: Number(markObj.examMarks),
      });
      setMsg({ type: 'success', text: 'Marks saved successfully!' });
    } catch (err) {
      setMsg({ type: 'error', text: err.message || 'Failed to save marks.' });
    }
  };

  const handleLeaveDecision = async (id, status, comments) => {
    try {
      await api.updateLeaveStatus(id, status, comments);
      setMsg({ type: 'success', text: `Leave request ${status.toLowerCase()}!` });
      const updated = await api.getFacultyLeaveRequests();
      setLeaveRequests(updated || []);
    } catch (err) {
      setMsg({ type: 'error', text: err.message || 'Failed to update leave.' });
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
          <div className="glass-panel rounded-3xl p-6 mb-8 border border-white/10 bg-gradient-to-r from-blue-900/40 via-indigo-900/30 to-slate-900">
            <span className="text-xs font-bold text-blue-400 uppercase tracking-widest">Faculty Portal</span>
            <h1 className="text-2xl sm:text-3xl font-extrabold text-white mt-1">
              Welcome, {profile?.name || profile?.user?.name || 'Faculty Member'} 👨‍🏫
            </h1>
            <p className="text-xs text-gray-300 mt-1">
              Designation: <span className="text-blue-300 font-semibold">{profile?.designation || 'Associate Professor'}</span> | 
              Emp ID: <span className="text-purple-300 font-semibold">{profile?.employeeId || 'EMP2026101'}</span>
            </p>
          </div>

          {msg.text && (
            <div className={`mb-6 p-4 rounded-2xl border text-xs flex items-center justify-between ${
              msg.type === 'success' ? 'bg-emerald-500/10 border-emerald-500/30 text-emerald-300' : 'bg-red-500/10 border-red-500/30 text-red-300'
            }`}>
              <span>{msg.text}</span>
              <button onClick={() => setMsg({ type: '', text: '' })} className="font-bold underline">Dismiss</button>
            </div>
          )}

          {/* TAB: ATTENDANCE */}
          {(activeTab === 'overview' || activeTab === 'attendance') && (
            <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="space-y-6">
              <div className="glass-panel rounded-3xl p-6 border border-white/10">
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-6">
                  <div>
                    <h3 className="text-lg font-bold text-white">Class Attendance Logging</h3>
                    <p className="text-xs text-gray-400">Select course and date to record present/absent logs</p>
                  </div>

                  <div className="flex items-center space-x-3">
                    <select
                      value={selectedSubjectId}
                      onChange={handleSubjectChange}
                      className="glass-input rounded-xl px-3 py-2 text-xs"
                    >
                      {subjects.map((s) => (
                        <option key={s.id} value={s.id} className="bg-slate-900">
                          {s.name} ({s.code})
                        </option>
                      ))}
                    </select>

                    <input
                      type="date"
                      value={attendanceDate}
                      onChange={(e) => setAttendanceDate(e.target.value)}
                      className="glass-input rounded-xl px-3 py-2 text-xs"
                    />

                    <button
                      onClick={handleSaveAttendance}
                      className="px-4 py-2 rounded-xl bg-gradient-to-r from-blue-600 to-indigo-600 text-white font-bold text-xs flex items-center space-x-1.5 shadow-lg"
                    >
                      <Save size={14} />
                      <span>Save All</span>
                    </button>
                  </div>
                </div>

                <div className="overflow-x-auto">
                  <table className="w-full text-left text-xs text-gray-300">
                    <thead className="bg-slate-900/60 uppercase font-semibold text-gray-400 border-b border-white/10">
                      <tr>
                        <th className="p-3">Roll Number</th>
                        <th className="p-3">Student Name</th>
                        <th className="p-3">Status Toggle</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-white/5">
                      {students.map((s) => (
                        <tr key={s.id} className="hover:bg-white/5">
                          <td className="p-3 font-semibold text-blue-400">{s.rollNumber}</td>
                          <td className="p-3 text-white">{s.name || s.user?.name}</td>
                          <td className="p-3">
                            <button
                              onClick={() =>
                                setAttendanceState({
                                  ...attendanceState,
                                  [s.id]: attendanceState[s.id] === 'PRESENT' ? 'ABSENT' : 'PRESENT',
                                })
                              }
                              className={`px-3 py-1 rounded-lg text-xs font-bold transition-all border ${
                                attendanceState[s.id] === 'PRESENT'
                                  ? 'bg-emerald-500/20 text-emerald-300 border-emerald-500/30'
                                  : 'bg-red-500/20 text-red-300 border-red-500/30'
                              }`}
                            >
                              {attendanceState[s.id] || 'PRESENT'}
                            </button>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            </motion.div>
          )}

          {/* TAB: GRADES */}
          {activeTab === 'grades' && (
            <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="glass-panel rounded-3xl p-6 border border-white/10">
              <h3 className="text-lg font-bold text-white mb-4">Record & Update Student Grades</h3>
              <div className="space-y-4">
                {students.map((s) => {
                  const m = marksState[s.id] || { internalMarks: 18, assignmentMarks: 9, examMarks: 65 };
                  return (
                    <div key={s.id} className="glass-card rounded-2xl p-4 border border-white/10 flex flex-col md:flex-row md:items-center justify-between gap-4">
                      <div>
                        <div className="font-bold text-sm text-white">{s.name || s.user?.name}</div>
                        <div className="text-xs text-blue-400">Roll: {s.rollNumber}</div>
                      </div>

                      <div className="flex items-center space-x-3">
                        <div>
                          <label className="block text-[10px] text-gray-400">Internal (20)</label>
                          <input
                            type="number"
                            value={m.internalMarks}
                            onChange={(e) =>
                              setMarksState({
                                ...marksState,
                                [s.id]: { ...m, internalMarks: e.target.value },
                              })
                            }
                            className="w-16 glass-input rounded-lg p-1.5 text-xs text-center"
                          />
                        </div>
                        <div>
                          <label className="block text-[10px] text-gray-400">Assignment (10)</label>
                          <input
                            type="number"
                            value={m.assignmentMarks}
                            onChange={(e) =>
                              setMarksState({
                                ...marksState,
                                [s.id]: { ...m, assignmentMarks: e.target.value },
                              })
                            }
                            className="w-16 glass-input rounded-lg p-1.5 text-xs text-center"
                          />
                        </div>
                        <div>
                          <label className="block text-[10px] text-gray-400">Exam (70)</label>
                          <input
                            type="number"
                            value={m.examMarks}
                            onChange={(e) =>
                              setMarksState({
                                ...marksState,
                                [s.id]: { ...m, examMarks: e.target.value },
                              })
                            }
                            className="w-16 glass-input rounded-lg p-1.5 text-xs text-center"
                          />
                        </div>

                        <button
                          onClick={() => handleSaveMarks(s.id)}
                          className="px-3 py-2 rounded-xl bg-gradient-to-r from-blue-600 to-indigo-600 text-white font-bold text-xs shadow-md mt-3"
                        >
                          Save
                        </button>
                      </div>
                    </div>
                  );
                })}
              </div>
            </motion.div>
          )}

          {/* TAB: LEAVE APPROVALS */}
          {activeTab === 'leave_approvals' && (
            <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="glass-panel rounded-3xl p-6 border border-white/10">
              <h3 className="text-lg font-bold text-white mb-4">Review Student Leave Applications</h3>
              <div className="space-y-3">
                {leaveRequests.map((l) => (
                  <div key={l.id} className="glass-card rounded-2xl p-4 border border-white/10 flex items-start justify-between">
                    <div>
                      <div className="font-bold text-sm text-white">{l.studentName || l.student?.user?.name || 'Student'}</div>
                      <div className="text-xs text-gray-400 mt-0.5">Dates: {l.startDate} to {l.endDate}</div>
                      <p className="text-xs text-gray-300 mt-2">{l.reason}</p>
                    </div>

                    <div className="flex items-center space-x-2">
                      <button
                        onClick={() => handleLeaveDecision(l.id, 'APPROVED', 'Approved by faculty')}
                        className="px-3 py-1.5 bg-emerald-600 text-white rounded-lg font-bold text-xs flex items-center space-x-1"
                      >
                        <Check size={14} />
                        <span>Approve</span>
                      </button>
                      <button
                        onClick={() => handleLeaveDecision(l.id, 'REJECTED', 'Rejected due to low attendance')}
                        className="px-3 py-1.5 bg-red-600 text-white rounded-lg font-bold text-xs flex items-center space-x-1"
                      >
                        <X size={14} />
                        <span>Reject</span>
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            </motion.div>
          )}
        </main>
      </div>

      <SOSModal isOpen={isSOSOpen} onClose={() => setIsSOSOpen(false)} />
    </div>
  );
}

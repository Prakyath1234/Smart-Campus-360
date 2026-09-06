import React, { useState, useEffect } from 'react';
import { api } from '../../api';
import Navbar from '../../components/Navbar';
import Sidebar from '../../components/Sidebar';
import SOSModal from '../../components/SOSModal';
import { 
  Check, 
  X, 
  Save,
  Calendar,
  Clock,
  MapPin,
  Users,
  BookOpen,
  CheckCircle2,
  AlertCircle,
  ArrowRight,
  Sparkles,
  Layers,
  GraduationCap,
  ClipboardCheck
} from 'lucide-react';
import { motion } from 'framer-motion';

export default function FacultyDashboard() {
  const [activeTab, setActiveTab] = useState('overview');
  const [isSidebarOpen, setIsSidebarOpen] = useState(false);
  const [isSOSOpen, setIsSOSOpen] = useState(false);

  const [profile, setProfile] = useState(null);
  const [subjects, setSubjects] = useState([]);
  const [facultyClasses, setFacultyClasses] = useState([]);
  const [selectedSubjectId, setSelectedSubjectId] = useState('');
  const [students, setStudents] = useState([]);
  const [attendanceDate, setAttendanceDate] = useState(new Date().toISOString().split('T')[0]);
  const [attendanceState, setAttendanceState] = useState({});
  const [marksState, setMarksState] = useState({});
  const [leaveRequests, setLeaveRequests] = useState([]);
  const [selectedDay, setSelectedDay] = useState('ALL');
  const [isSavingAttendance, setIsSavingAttendance] = useState(false);
  const [msg, setMsg] = useState({ type: '', text: '' });

  const loadFacultyData = async () => {
    try {
      const [profData, subjData, leaveData, classData] = await Promise.all([
        api.getFacultyProfile().catch(() => null),
        api.getFacultySubjects().catch(() => []),
        api.getFacultyLeaveRequests().catch(() => []),
        api.getFacultyClasses().catch(() => []),
      ]);

      setProfile(profData);
      setSubjects(subjData || []);
      setLeaveRequests(leaveData || []);
      setFacultyClasses(classData || []);

      if (subjData && subjData.length > 0) {
        setSelectedSubjectId(subjData[0].id);
        fetchStudentsForSubject(subjData[0].id);
      }
    } catch (err) {
      console.error('Faculty data loading error:', err);
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

  const markAllAttendance = (status) => {
    const updated = {};
    students.forEach((s) => {
      updated[s.id] = status;
    });
    setAttendanceState(updated);
  };

  const handleTakeAttendanceForClass = (subjectId) => {
    setSelectedSubjectId(subjectId);
    fetchStudentsForSubject(subjectId);
    setActiveTab('attendance');
  };

  const handleSaveAttendance = async () => {
    if (!selectedSubjectId) {
      setMsg({ type: 'error', text: 'Please select a subject first.' });
      return;
    }
    setIsSavingAttendance(true);
    try {
      const records = Object.keys(attendanceState).map((studentId) => ({
        studentId: Number(studentId),
        subjectId: Number(selectedSubjectId),
        date: attendanceDate,
        status: attendanceState[studentId],
      }));
      await api.saveAttendance(records);
      setMsg({ type: 'success', text: `Attendance for ${records.length} students recorded successfully on ${attendanceDate}!` });
    } catch (err) {
      setMsg({ type: 'error', text: err.message || 'Failed to save attendance.' });
    } finally {
      setIsSavingAttendance(false);
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

          {/* Stats Bar */}
          <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
            <div className="glass-card rounded-2xl p-4 border border-white/10 flex items-center space-x-3">
              <div className="w-10 h-10 rounded-xl bg-blue-500/20 text-blue-400 flex items-center justify-center font-bold">
                <BookOpen size={20} />
              </div>
              <div>
                <div className="text-xl font-black text-white">{subjects.length}</div>
                <div className="text-[11px] text-gray-400 font-medium">Assigned Courses</div>
              </div>
            </div>

            <div className="glass-card rounded-2xl p-4 border border-white/10 flex items-center space-x-3">
              <div className="w-10 h-10 rounded-xl bg-indigo-500/20 text-indigo-400 flex items-center justify-center font-bold">
                <Calendar size={20} />
              </div>
              <div>
                <div className="text-xl font-black text-white">{facultyClasses.length}</div>
                <div className="text-[11px] text-gray-400 font-medium">Weekly Classes</div>
              </div>
            </div>

            <div className="glass-card rounded-2xl p-4 border border-white/10 flex items-center space-x-3">
              <div className="w-10 h-10 rounded-xl bg-emerald-500/20 text-emerald-400 flex items-center justify-center font-bold">
                <Users size={20} />
              </div>
              <div>
                <div className="text-xl font-black text-white">{students.length}</div>
                <div className="text-[11px] text-gray-400 font-medium">Enrolled Students</div>
              </div>
            </div>

            <div className="glass-card rounded-2xl p-4 border border-white/10 flex items-center space-x-3">
              <div className="w-10 h-10 rounded-xl bg-amber-500/20 text-amber-400 flex items-center justify-center font-bold">
                <AlertCircle size={20} />
              </div>
              <div>
                <div className="text-xl font-black text-white">
                  {leaveRequests.filter(l => l.status === 'PENDING').length}
                </div>
                <div className="text-[11px] text-gray-400 font-medium">Pending Leaves</div>
              </div>
            </div>
          </div>

          {/* TAB: MY CLASSES & SCHEDULE */}
          {(activeTab === 'classes' || (activeTab === 'overview' && facultyClasses.length > 0)) && (
            <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="space-y-6 mb-8">
              <div className="glass-panel rounded-3xl p-6 border border-white/10">
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-6">
                  <div>
                    <div className="flex items-center space-x-2">
                      <span className="p-1.5 rounded-lg bg-indigo-500/20 text-indigo-300">
                        <Calendar size={16} />
                      </span>
                      <h3 className="text-lg font-bold text-white">My Classes & Teaching Schedule</h3>
                    </div>
                    <p className="text-xs text-gray-400 mt-1">
                      View all scheduled class slots, locations, and take attendance directly
                    </p>
                  </div>

                  {/* Day Filters */}
                  <div className="flex flex-wrap gap-1.5 bg-slate-900/60 p-1 rounded-xl border border-white/5">
                    {['ALL', 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY'].map((d) => (
                      <button
                        key={d}
                        onClick={() => setSelectedDay(d)}
                        className={`px-2.5 py-1 rounded-lg text-[11px] font-bold transition-all ${
                          selectedDay === d
                            ? 'bg-indigo-600 text-white shadow'
                            : 'text-gray-400 hover:text-white hover:bg-white/5'
                        }`}
                      >
                        {d === 'ALL' ? 'All Days' : d.slice(0, 3)}
                      </button>
                    ))}
                  </div>
                </div>

                {facultyClasses.length === 0 ? (
                  <div className="p-8 text-center text-gray-400 text-xs bg-slate-900/40 rounded-2xl border border-white/5">
                    No classes scheduled for your faculty profile at this time.
                  </div>
                ) : (
                  <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                    {facultyClasses
                      .filter((c) => selectedDay === 'ALL' || c.dayOfWeek === selectedDay)
                      .map((c) => (
                        <div
                          key={c.id}
                          className="glass-card rounded-2xl p-4 border border-white/10 hover:border-indigo-500/40 transition-all flex flex-col justify-between"
                        >
                          <div>
                            <div className="flex items-center justify-between mb-2">
                              <span className="px-2 py-0.5 rounded-md bg-indigo-500/20 text-indigo-300 text-[10px] font-bold uppercase tracking-wider">
                                {c.dayOfWeek}
                              </span>
                              <span className="px-2 py-0.5 rounded-md bg-purple-500/20 text-purple-300 text-[10px] font-bold">
                                Sem {c.semester}
                              </span>
                            </div>

                            <h4 className="text-sm font-bold text-white mb-1">
                              {c.subjectName || c.subject?.name}
                            </h4>
                            <div className="text-xs text-blue-400 font-mono mb-3">
                              {c.subjectCode || c.subject?.code}
                            </div>

                            <div className="space-y-1.5 text-xs text-gray-300 mb-4 bg-slate-900/50 p-2.5 rounded-xl border border-white/5">
                              <div className="flex items-center space-x-2">
                                <Clock size={13} className="text-indigo-400" />
                                <span>{c.startTime} - {c.endTime}</span>
                              </div>
                              <div className="flex items-center space-x-2">
                                <MapPin size={13} className="text-pink-400" />
                                <span>Classroom: <strong className="text-white">{c.classroom}</strong></span>
                              </div>
                            </div>
                          </div>

                          <button
                            onClick={() => handleTakeAttendanceForClass(c.subjectId)}
                            className="w-full py-2 rounded-xl bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-500 hover:to-indigo-500 text-white font-bold text-xs flex items-center justify-center space-x-1.5 shadow transition-all"
                          >
                            <ClipboardCheck size={14} />
                            <span>Take Attendance</span>
                          </button>
                        </div>
                      ))}
                  </div>
                )}
              </div>
            </motion.div>
          )}

          {/* TAB: ATTENDANCE */}
          {(activeTab === 'overview' || activeTab === 'attendance') && (
            <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="space-y-6">
              <div className="glass-panel rounded-3xl p-6 border border-white/10">
                <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-4 mb-6">
                  <div>
                    <div className="flex items-center space-x-2">
                      <span className="p-1.5 rounded-lg bg-blue-500/20 text-blue-300">
                        <ClipboardCheck size={16} />
                      </span>
                      <h3 className="text-lg font-bold text-white">Student Attendance Logging</h3>
                    </div>
                    <p className="text-xs text-gray-400 mt-1">Select class course, pick date, and record attendance details</p>
                  </div>

                  <div className="flex flex-wrap items-center gap-3">
                    <div className="flex items-center space-x-2">
                      <label className="text-xs text-gray-400">Course:</label>
                      <select
                        value={selectedSubjectId}
                        onChange={handleSubjectChange}
                        className="glass-input rounded-xl px-3 py-2 text-xs font-semibold"
                      >
                        {subjects.map((s) => (
                          <option key={s.id} value={s.id} className="bg-slate-900">
                            {s.name} ({s.code})
                          </option>
                        ))}
                      </select>
                    </div>

                    <div className="flex items-center space-x-2">
                      <label className="text-xs text-gray-400">Date:</label>
                      <input
                        type="date"
                        value={attendanceDate}
                        onChange={(e) => setAttendanceDate(e.target.value)}
                        className="glass-input rounded-xl px-3 py-2 text-xs"
                      />
                    </div>

                    <button
                      onClick={handleSaveAttendance}
                      disabled={isSavingAttendance || students.length === 0}
                      className="px-5 py-2.5 rounded-xl bg-gradient-to-r from-emerald-600 to-teal-600 hover:from-emerald-500 hover:to-teal-500 disabled:opacity-50 text-white font-bold text-xs flex items-center space-x-1.5 shadow-lg transition-all"
                    >
                      <Save size={14} />
                      <span>{isSavingAttendance ? 'Saving...' : 'Save All Attendance'}</span>
                    </button>
                  </div>
                </div>

                {/* Attendance Summary Bar & Quick Actions */}
                <div className="bg-slate-900/70 p-4 rounded-2xl border border-white/5 flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-6">
                  <div className="flex flex-wrap items-center gap-4 text-xs">
                    <div>
                      <span className="text-gray-400">Total Students:</span>{' '}
                      <strong className="text-white font-bold">{students.length}</strong>
                    </div>
                    <div>
                      <span className="text-gray-400">Present:</span>{' '}
                      <strong className="text-emerald-400 font-bold">
                        {Object.values(attendanceState).filter((s) => s === 'PRESENT').length}
                      </strong>
                    </div>
                    <div>
                      <span className="text-gray-400">Absent:</span>{' '}
                      <strong className="text-red-400 font-bold">
                        {Object.values(attendanceState).filter((s) => s === 'ABSENT').length}
                      </strong>
                    </div>
                    <div>
                      <span className="text-gray-400">Attendance Rate:</span>{' '}
                      <strong className="text-blue-400 font-bold">
                        {students.length > 0
                          ? Math.round(
                              (Object.values(attendanceState).filter((s) => s === 'PRESENT').length /
                                students.length) *
                                100
                            )
                          : 0}
                        %
                      </strong>
                    </div>
                  </div>

                  <div className="flex items-center space-x-2">
                    <button
                      onClick={() => markAllAttendance('PRESENT')}
                      className="px-3 py-1.5 rounded-lg bg-emerald-500/10 hover:bg-emerald-500/20 text-emerald-300 border border-emerald-500/30 text-xs font-semibold transition-all flex items-center space-x-1"
                    >
                      <Check size={12} />
                      <span>Mark All Present</span>
                    </button>
                    <button
                      onClick={() => markAllAttendance('ABSENT')}
                      className="px-3 py-1.5 rounded-lg bg-red-500/10 hover:bg-red-500/20 text-red-300 border border-red-500/30 text-xs font-semibold transition-all flex items-center space-x-1"
                    >
                      <X size={12} />
                      <span>Mark All Absent</span>
                    </button>
                  </div>
                </div>

                {students.length === 0 ? (
                  <div className="p-8 text-center text-gray-400 text-xs bg-slate-900/40 rounded-2xl border border-white/5">
                    No enrolled students found for the selected course.
                  </div>
                ) : (
                  <div className="overflow-x-auto">
                    <table className="w-full text-left text-xs text-gray-300">
                      <thead className="bg-slate-900/80 uppercase font-semibold text-gray-400 border-b border-white/10">
                        <tr>
                          <th className="p-3">Roll Number</th>
                          <th className="p-3">Student Name</th>
                          <th className="p-3">Class / Semester</th>
                          <th className="p-3">Status</th>
                          <th className="p-3 text-right">Action</th>
                        </tr>
                      </thead>
                      <tbody className="divide-y divide-white/5">
                        {students.map((s) => {
                          const isPresent = (attendanceState[s.id] || 'PRESENT') === 'PRESENT';
                          return (
                            <tr key={s.id} className="hover:bg-white/5 transition-colors">
                              <td className="p-3 font-mono font-semibold text-blue-400">{s.rollNumber}</td>
                              <td className="p-3 font-medium text-white">{s.name || s.user?.name}</td>
                              <td className="p-3 text-gray-400">
                                {s.departmentCode ? `${s.departmentCode} - Sem ${s.semester}` : `Semester ${s.semester || 5}`}
                              </td>
                              <td className="p-3">
                                <span
                                  className={`px-2.5 py-0.5 rounded-full text-[11px] font-bold border ${
                                    isPresent
                                      ? 'bg-emerald-500/20 text-emerald-300 border-emerald-500/30'
                                      : 'bg-red-500/20 text-red-300 border-red-500/30'
                                  }`}
                                >
                                  {isPresent ? '● PRESENT' : '○ ABSENT'}
                                </span>
                              </td>
                              <td className="p-3 text-right">
                                <button
                                  onClick={() =>
                                    setAttendanceState({
                                      ...attendanceState,
                                      [s.id]: isPresent ? 'ABSENT' : 'PRESENT',
                                    })
                                  }
                                  className={`px-3 py-1 rounded-lg text-xs font-bold transition-all border ${
                                    isPresent
                                      ? 'bg-red-500/10 hover:bg-red-500/20 text-red-300 border-red-500/30'
                                      : 'bg-emerald-500/10 hover:bg-emerald-500/20 text-emerald-300 border-emerald-500/30'
                                  }`}
                                >
                                  Mark {isPresent ? 'Absent' : 'Present'}
                                </button>
                              </td>
                            </tr>
                          );
                        })}
                      </tbody>
                    </table>
                  </div>
                )}
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

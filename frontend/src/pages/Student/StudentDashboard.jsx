import React, { useState, useEffect } from 'react';
import { api } from '../../api';
import Navbar from '../../components/Navbar';
import Sidebar from '../../components/Sidebar';
import SOSModal from '../../components/SOSModal';
import { 
  GraduationCap, 
  Calendar, 
  Clock, 
  FileText, 
  AlertCircle, 
  ShieldAlert, 
  TrendingUp, 
  BookOpen,
  Calculator
} from 'lucide-react';
import { motion } from 'framer-motion';

export default function StudentDashboard() {
  const [activeTab, setActiveTab] = useState('overview');
  const [isSidebarOpen, setIsSidebarOpen] = useState(false);
  const [isSOSOpen, setIsSOSOpen] = useState(false);

  // Data states
  const [profile, setProfile] = useState(null);
  const [attendance, setAttendance] = useState([]);
  const [marks, setMarks] = useState([]);
  const [timetable, setTimetable] = useState([]);
  const [leaveRequests, setLeaveRequests] = useState([]);
  const [complaints, setComplaints] = useState([]);
  
  // Academic Calculator states
  const [academicSummary, setAcademicSummary] = useState(null);
  const [calcTab, setCalcTab] = useState('sgpa');
  const [sgpaRows, setSgpaRows] = useState([
    { subjectName: 'Subject 1', credits: 4, grade: 'A+' }
  ]);
  const [sgpaCalcResult, setSgpaCalcResult] = useState(null);

  const [cgpaRows, setCgpaRows] = useState([
    { semester: 1, sgpa: '8.50', credits: 24 }
  ]);
  const [cgpaCalcResult, setCgpaCalcResult] = useState(null);

  const [whatIfRows, setWhatIfRows] = useState([]);
  const [whatIfResult, setWhatIfResult] = useState(null);

  const [targetForm, setTargetForm] = useState({
    currentCgpa: '7.50',
    completedCredits: 100,
    futureCredits: 24,
    targetCgpa: '8.00'
  });
  const [targetResult, setTargetResult] = useState(null);

  // ATS Analyzer states
  const [atsFile, setAtsFile] = useState(null);
  const [atsJd, setAtsJd] = useState('');
  const [atsResult, setAtsResult] = useState(null);
  const [atsLoading, setAtsLoading] = useState(false);

  // Form states
  const [leaveForm, setLeaveForm] = useState({ startDate: '', endDate: '', reason: '' });
  const [complaintForm, setComplaintForm] = useState({ title: '', description: '', category: 'MAINTENANCE' });
  const [msg, setMsg] = useState({ type: '', text: '' });

  const loadAllData = async () => {
    try {
      const [profData, attData, marksData, timeData, leaveData, compData, summaryData] = await Promise.all([
        api.getStudentProfile().catch(() => null),
        api.getStudentAttendance().catch(() => []),
        api.getStudentMarks().catch(() => []),
        api.getStudentTimetable().catch(() => []),
        api.getStudentLeaveRequests().catch(() => []),
        api.getStudentComplaints().catch(() => []),
        api.getAcademicSummary().catch(() => null)
      ]);

      setProfile(profData);
      setAttendance(attData || []);
      setMarks(marksData || []);
      setTimetable(timeData || []);
      setLeaveRequests(leaveData || []);
      setComplaints(compData || []);
      setAcademicSummary(summaryData);
      
      if (marksData && marksData.length > 0) {
        setWhatIfRows(
          marksData.map(m => ({
            subjectName: m.subjectName,
            credits: 4,
            currentGrade: m.grade || 'F',
            projectedGrade: m.grade || 'F'
          }))
        );
      }
    } catch (err) {
      console.error('Data load error:', err);
    }
  };

  useEffect(() => {
    loadAllData();
  }, []);

  const handleCreateLeave = async (e) => {
    e.preventDefault();
    try {
      await api.createLeaveRequest(leaveForm);
      setMsg({ type: 'success', text: 'Leave request submitted successfully!' });
      setLeaveForm({ startDate: '', endDate: '', reason: '' });
      const updated = await api.getStudentLeaveRequests();
      setLeaveRequests(updated || []);
    } catch (err) {
      setMsg({ type: 'error', text: err.message || 'Failed to submit leave request.' });
    }
  };

  const handleCreateComplaint = async (e) => {
    e.preventDefault();
    try {
      await api.createComplaint(complaintForm);
      setMsg({ type: 'success', text: 'Complaint ticket created and AI categorized!' });
      setComplaintForm({ title: '', description: '', category: 'MAINTENANCE' });
      const updated = await api.getStudentComplaints();
      setComplaints(updated || []);
    } catch (err) {
      setMsg({ type: 'error', text: err.message || 'Failed to create complaint ticket.' });
    }
  };

  const handleCalculateSgpa = async () => {
    setMsg({ type: '', text: '' });
    try {
      const formatted = sgpaRows.map(r => ({
        subjectName: r.subjectName,
        credits: Number(r.credits),
        grade: r.grade
      }));
      const res = await api.calculateSGPA({ subjects: formatted });
      setSgpaCalcResult(res);
    } catch (err) {
      setMsg({ type: 'error', text: err.message || 'Failed to calculate SGPA.' });
    }
  };

  const handleCalculateCgpa = async () => {
    setMsg({ type: '', text: '' });
    try {
      const formatted = cgpaRows.map(r => ({
        semester: Number(r.semester),
        sgpa: Number(r.sgpa),
        credits: Number(r.credits)
      }));
      const res = await api.calculateCGPA({ semesters: formatted });
      setCgpaCalcResult(res);
    } catch (err) {
      setMsg({ type: 'error', text: err.message || 'Failed to calculate CGPA.' });
    }
  };

  const handleCalculateWhatIf = async () => {
    setMsg({ type: '', text: '' });
    try {
      const formatted = whatIfRows.map(r => ({
        subjectName: r.subjectName,
        credits: Number(r.credits),
        currentGrade: r.currentGrade,
        projectedGrade: r.projectedGrade
      }));
      const res = await api.calculateWhatIf({ subjects: formatted });
      setWhatIfResult(res);
    } catch (err) {
      setMsg({ type: 'error', text: err.message || 'Failed to simulate What-If SGPA.' });
    }
  };

  const handleCalculateTargetCgpa = async () => {
    setMsg({ type: '', text: '' });
    try {
      const req = {
        currentCGPA: Number(targetForm.currentCgpa),
        completedCredits: Number(targetForm.completedCredits),
        futureCredits: Number(targetForm.futureCredits),
        targetCGPA: Number(targetForm.targetCgpa)
      };
      const res = await api.calculateTargetCGPA(req);
      setTargetResult(res);
    } catch (err) {
      setMsg({ type: 'error', text: err.message || 'Failed to calculate required target SGPA.' });
    }
  };

  const handleAnalyzeAts = async (e) => {
    e.preventDefault();
    if (!atsFile) {
      setMsg({ type: 'error', text: 'Please upload a resume file.' });
      return;
    }
    if (!atsJd.trim()) {
      setMsg({ type: 'error', text: 'Please paste a job description.' });
      return;
    }
    setMsg({ type: '', text: '' });
    setAtsLoading(true);
    setAtsResult(null);
    try {
      const formData = new FormData();
      formData.append('file', atsFile);
      formData.append('jobDescription', atsJd);
      const res = await api.analyzeAts(formData);
      setAtsResult(res);
      setMsg({ type: 'success', text: 'ATS Resume Analysis completed successfully!' });
    } catch (err) {
      setMsg({ type: 'error', text: err.message || 'ATS Analysis failed.' });
    } finally {
      setAtsLoading(false);
    }
  };

  // Calculations
  const presentCount = attendance.filter((a) => a.status === 'PRESENT').length;
  const attendancePercentage = attendance.length ? Math.round((presentCount / attendance.length) * 100) : 100;

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
          {/* Header Banner */}
          <div className="glass-panel rounded-3xl p-6 mb-8 border border-white/10 relative overflow-hidden bg-gradient-to-r from-indigo-900/40 via-purple-900/30 to-slate-900">
            <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 relative z-10">
              <div>
                <span className="text-xs font-bold text-indigo-400 uppercase tracking-widest">
                  Student Workspace
                </span>
                <h1 className="text-2xl sm:text-3xl font-extrabold text-white mt-1">
                  Welcome Back, {profile?.name || profile?.user?.name || 'Student'}! 🎓
                </h1>
                <p className="text-xs text-gray-300 mt-1">
                  Roll No: <span className="text-indigo-300 font-semibold">{profile?.rollNumber || 'CS2026001'}</span> | 
                  Department: <span className="text-purple-300 font-semibold">{profile?.departmentName || profile?.department?.name || 'CSE'}</span> | 
                  Semester: <span className="text-emerald-300 font-semibold">Sem {profile?.semester || 5}</span>
                </p>
              </div>

              <button
                onClick={() => setIsSOSOpen(true)}
                className="px-5 py-2.5 rounded-xl bg-gradient-to-r from-red-600 to-rose-600 text-white font-bold text-sm shadow-xl shadow-red-600/30 hover:scale-[1.02] active:scale-[0.98] transition-all flex items-center space-x-2 pulse-sos"
              >
                <ShieldAlert className="w-5 h-5 animate-pulse" />
                <span>TRIGGER SOS DISTRESS</span>
              </button>
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

          {/* TAB 1: OVERVIEW */}
          {activeTab === 'overview' && (
            <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="space-y-8">
              {/* Stat Cards */}
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
                <div className="glass-card rounded-2xl p-5 border border-white/10 flex items-center justify-between">
                  <div>
                    <div className="text-xs text-gray-400 font-medium uppercase tracking-wider">Attendance Rate</div>
                    <div className="text-2xl font-bold text-emerald-400 mt-1">{attendancePercentage}%</div>
                    <div className="text-[11px] text-gray-400 mt-0.5">{presentCount} Present / {attendance.length} Classes</div>
                  </div>
                  <div className="p-3 bg-emerald-500/10 rounded-xl text-emerald-400">
                    <TrendingUp size={24} />
                  </div>
                </div>

                <div className="glass-card rounded-2xl p-5 border border-white/10 flex items-center justify-between">
                  <div>
                    <div className="text-xs text-gray-400 font-medium uppercase tracking-wider">Enrolled Subjects</div>
                    <div className="text-2xl font-bold text-indigo-400 mt-1">{marks.length || 3}</div>
                    <div className="text-[11px] text-gray-400 mt-0.5">Active Academic Load</div>
                  </div>
                  <div className="p-3 bg-indigo-500/10 rounded-xl text-indigo-400">
                    <BookOpen size={24} />
                  </div>
                </div>

                <div className="glass-card rounded-2xl p-5 border border-white/10 flex items-center justify-between">
                  <div>
                    <div className="text-xs text-gray-400 font-medium uppercase tracking-wider">Leave Applications</div>
                    <div className="text-2xl font-bold text-purple-400 mt-1">{leaveRequests.length}</div>
                    <div className="text-[11px] text-gray-400 mt-0.5">{leaveRequests.filter(l => l.status === 'PENDING').length} Pending Review</div>
                  </div>
                  <div className="p-3 bg-purple-500/10 rounded-xl text-purple-400">
                    <FileText size={24} />
                  </div>
                </div>

                <div className="glass-card rounded-2xl p-5 border border-white/10 flex items-center justify-between">
                  <div>
                    <div className="text-xs text-gray-400 font-medium uppercase tracking-wider">Grievance Tickets</div>
                    <div className="text-2xl font-bold text-amber-400 mt-1">{complaints.length}</div>
                    <div className="text-[11px] text-gray-400 mt-0.5">{complaints.filter(c => c.status === 'OPEN').length} Open Tickets</div>
                  </div>
                  <div className="p-3 bg-amber-500/10 rounded-xl text-amber-400">
                    <AlertCircle size={24} />
                  </div>
                </div>
              </div>

              {/* Quick Academic Marks & Timetable Overview Grid */}
              <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
                {/* Marks Summary */}
                <div className="glass-panel rounded-3xl p-6 border border-white/10">
                  <div className="flex items-center justify-between mb-4">
                    <h3 className="text-base font-bold text-white flex items-center space-x-2">
                      <GraduationCap className="text-indigo-400" size={20} />
                      <span>Academic Performance</span>
                    </h3>
                    <button onClick={() => setActiveTab('academic')} className="text-xs text-indigo-400 hover:underline">View All</button>
                  </div>

                  <div className="space-y-4">
                    {marks.map((m) => (
                      <div key={m.id} className="glass-card rounded-xl p-4 border border-white/5 space-y-2">
                        <div className="flex items-center justify-between">
                          <span className="font-semibold text-sm text-gray-200">{m.subjectName || m.subject?.name} ({m.subjectCode || m.subject?.code})</span>
                          <span className="text-xs font-bold px-2 py-0.5 rounded-md bg-indigo-500/20 text-indigo-300 border border-indigo-500/30">
                            Grade {m.grade}
                          </span>
                        </div>

                        <div className="w-full bg-slate-900 rounded-full h-2 overflow-hidden">
                          <div
                            className="bg-gradient-to-r from-indigo-500 to-purple-500 h-full rounded-full transition-all duration-500"
                            style={{ width: `${Math.min(100, (m.totalMarks / 100) * 100)}%` }}
                          />
                        </div>
                        <div className="flex justify-between text-[11px] text-gray-400">
                          <span>Internal: {m.internalMarks} | Assignment: {m.assignmentMarks} | Exam: {m.examMarks}</span>
                          <span className="font-bold text-white">{m.totalMarks} / 100</span>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>

                {/* Timetable Overview */}
                <div className="glass-panel rounded-3xl p-6 border border-white/10">
                  <div className="flex items-center justify-between mb-4">
                    <h3 className="text-base font-bold text-white flex items-center space-x-2">
                      <Calendar className="text-purple-400" size={20} />
                      <span>Class Schedule</span>
                    </h3>
                    <button onClick={() => setActiveTab('timetable')} className="text-xs text-purple-400 hover:underline">Full Timetable</button>
                  </div>

                  <div className="space-y-3">
                    {timetable.slice(0, 4).map((t) => (
                      <div key={t.id} className="glass-card rounded-xl p-3.5 border border-white/5 flex items-center justify-between">
                        <div>
                          <span className="text-xs font-bold text-indigo-400 uppercase tracking-wider block">{t.dayOfWeek}</span>
                          <span className="text-sm font-semibold text-gray-200">{t.subjectName || t.subject?.name}</span>
                          <span className="text-[11px] text-gray-400 block mt-0.5">Classroom: {t.classroom}</span>
                        </div>
                        <div className="text-right">
                          <span className="text-xs font-bold text-purple-300 flex items-center space-x-1">
                            <Clock size={12} />
                            <span>{t.startTime} - {t.endTime}</span>
                          </span>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            </motion.div>
          )}

          {/* TAB 2: ACADEMIC MARKS & ATTENDANCE */}
          {activeTab === 'academic' && (
            <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="space-y-6">
              <div className="glass-panel rounded-3xl p-6 border border-white/10">
                <h3 className="text-lg font-bold text-white mb-4">Subject Grades & Marks Breakdown</h3>
                <div className="overflow-x-auto">
                  <table className="w-full text-left text-xs text-gray-300">
                    <thead className="bg-slate-900/60 uppercase font-semibold text-gray-400 border-b border-white/10">
                      <tr>
                        <th className="p-3">Subject</th>
                        <th className="p-3">Code</th>
                        <th className="p-3">Internal (20)</th>
                        <th className="p-3">Assignment (10)</th>
                        <th className="p-3">Exam (70)</th>
                        <th className="p-3">Total (100)</th>
                        <th className="p-3">Grade</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-white/5">
                      {marks.map((m) => (
                        <tr key={m.id} className="hover:bg-white/5 transition-colors">
                          <td className="p-3 font-semibold text-white">{m.subjectName || m.subject?.name}</td>
                          <td className="p-3 text-indigo-400">{m.subjectCode || m.subject?.code}</td>
                          <td className="p-3">{m.internalMarks}</td>
                          <td className="p-3">{m.assignmentMarks}</td>
                          <td className="p-3">{m.examMarks}</td>
                          <td className="p-3 font-bold text-emerald-400">{m.totalMarks}</td>
                          <td className="p-3">
                            <span className="px-2 py-0.5 rounded bg-purple-500/20 text-purple-300 font-bold border border-purple-500/30">
                              {m.grade}
                            </span>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            </motion.div>
          )}

          {/* TAB 3: TIMETABLE */}
          {activeTab === 'timetable' && (
            <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="glass-panel rounded-3xl p-6 border border-white/10">
              <h3 className="text-lg font-bold text-white mb-4">Weekly Class Schedule</h3>
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                {['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY'].map((day) => {
                  const dayClasses = timetable.filter((t) => t.dayOfWeek === day);
                  return (
                    <div key={day} className="glass-card rounded-2xl p-4 border border-white/10">
                      <h4 className="text-xs font-bold text-indigo-400 uppercase tracking-wider mb-3">{day}</h4>
                      {dayClasses.length === 0 ? (
                        <p className="text-xs text-gray-500 py-2">No scheduled lectures</p>
                      ) : (
                        <div className="space-y-2">
                          {dayClasses.map((c) => (
                            <div key={c.id} className="p-2.5 bg-slate-900/60 rounded-xl border border-white/5">
                              <div className="font-semibold text-xs text-gray-200">{c.subjectName || c.subject?.name}</div>
                              <div className="text-[11px] text-gray-400 mt-1 flex justify-between">
                                <span>{c.classroom}</span>
                                <span className="text-indigo-300">{c.startTime} - {c.endTime}</span>
                              </div>
                            </div>
                          ))}
                        </div>
                      )}
                    </div>
                  );
                })}
              </div>
            </motion.div>
          )}

          {/* TAB 4: LEAVE REQUESTS */}
          {activeTab === 'leave' && (
            <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="grid grid-cols-1 lg:grid-cols-3 gap-8">
              {/* Request Form */}
              <div className="glass-panel rounded-3xl p-6 border border-white/10">
                <h3 className="text-base font-bold text-white mb-4">Submit Leave Application</h3>
                <form onSubmit={handleCreateLeave} className="space-y-4">
                  <div>
                    <label className="block text-xs font-semibold text-gray-300 uppercase mb-1">Start Date</label>
                    <input
                      type="date"
                      required
                      value={leaveForm.startDate}
                      onChange={(e) => setLeaveForm({ ...leaveForm, startDate: e.target.value })}
                      className="w-full glass-input rounded-xl p-2.5 text-xs"
                    />
                  </div>
                  <div>
                    <label className="block text-xs font-semibold text-gray-300 uppercase mb-1">End Date</label>
                    <input
                      type="date"
                      required
                      value={leaveForm.endDate}
                      onChange={(e) => setLeaveForm({ ...leaveForm, endDate: e.target.value })}
                      className="w-full glass-input rounded-xl p-2.5 text-xs"
                    />
                  </div>
                  <div>
                    <label className="block text-xs font-semibold text-gray-300 uppercase mb-1">Reason for Leave</label>
                    <textarea
                      rows="3"
                      required
                      value={leaveForm.reason}
                      onChange={(e) => setLeaveForm({ ...leaveForm, reason: e.target.value })}
                      placeholder="Specify personal / medical details..."
                      className="w-full glass-input rounded-xl p-2.5 text-xs"
                    />
                  </div>
                  <button type="submit" className="w-full py-2.5 rounded-xl bg-gradient-to-r from-indigo-600 to-purple-600 text-white font-bold text-xs shadow-lg">
                    Submit Application
                  </button>
                </form>
              </div>

              {/* Status List */}
              <div className="lg:col-span-2 glass-panel rounded-3xl p-6 border border-white/10">
                <h3 className="text-base font-bold text-white mb-4">Leave Application Tracker</h3>
                <div className="space-y-3">
                  {leaveRequests.map((l) => (
                    <div key={l.id} className="glass-card rounded-2xl p-4 border border-white/10 flex items-start justify-between">
                      <div>
                        <div className="flex items-center space-x-2">
                          <span className="text-xs font-bold text-indigo-300">{l.startDate} to {l.endDate}</span>
                          <span className={`text-[10px] font-bold px-2 py-0.5 rounded-full border ${
                            l.status === 'APPROVED' ? 'bg-emerald-500/20 text-emerald-300 border-emerald-500/30' :
                            l.status === 'REJECTED' ? 'bg-red-500/20 text-red-300 border-red-500/30' :
                            'bg-amber-500/20 text-amber-300 border-amber-500/30'
                          }`}>
                            {l.status}
                          </span>
                        </div>
                        <p className="text-xs text-gray-300 mt-2">{l.reason}</p>
                        {l.reviewComments && <p className="text-[11px] text-gray-400 mt-1 italic">Faculty note: "{l.reviewComments}"</p>}
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            </motion.div>
          )}

          {/* TAB 5: COMPLAINTS */}
          {activeTab === 'complaints' && (
            <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="grid grid-cols-1 lg:grid-cols-3 gap-8">
              {/* Complaint Form */}
              <div className="glass-panel rounded-3xl p-6 border border-white/10">
                <h3 className="text-base font-bold text-white mb-4">Lodge Grievance Ticket</h3>
                <form onSubmit={handleCreateComplaint} className="space-y-4">
                  <div>
                    <label className="block text-xs font-semibold text-gray-300 uppercase mb-1">Issue Category</label>
                    <select
                      value={complaintForm.category}
                      onChange={(e) => setComplaintForm({ ...complaintForm, category: e.target.value })}
                      className="w-full glass-input rounded-xl p-2.5 text-xs"
                    >
                      <option value="ACADEMIC" className="bg-slate-900">ACADEMIC</option>
                      <option value="INFRASTRUCTURE" className="bg-slate-900">INFRASTRUCTURE</option>
                      <option value="MAINTENANCE" className="bg-slate-900">MAINTENANCE</option>
                      <option value="HOSTEL" className="bg-slate-900">HOSTEL</option>
                      <option value="SAFETY" className="bg-slate-900">SAFETY</option>
                      <option value="TRANSPORT" className="bg-slate-900">TRANSPORT</option>
                      <option value="OTHER" className="bg-slate-900">OTHER</option>
                    </select>
                  </div>
                  <div>
                    <label className="block text-xs font-semibold text-gray-300 uppercase mb-1">Ticket Title</label>
                    <input
                      type="text"
                      required
                      value={complaintForm.title}
                      onChange={(e) => setComplaintForm({ ...complaintForm, title: e.target.value })}
                      placeholder="e.g. Broken bench in Lab 1"
                      className="w-full glass-input rounded-xl p-2.5 text-xs"
                    />
                  </div>
                  <div>
                    <label className="block text-xs font-semibold text-gray-300 uppercase mb-1">Detailed Description</label>
                    <textarea
                      rows="3"
                      required
                      value={complaintForm.description}
                      onChange={(e) => setComplaintForm({ ...complaintForm, description: e.target.value })}
                      placeholder="Explain the issue..."
                      className="w-full glass-input rounded-xl p-2.5 text-xs"
                    />
                  </div>
                  <button type="submit" className="w-full py-2.5 rounded-xl bg-gradient-to-r from-amber-600 to-orange-600 text-white font-bold text-xs shadow-lg">
                    Create Grievance Ticket
                  </button>
                </form>
              </div>

              {/* Complaints List */}
              <div className="lg:col-span-2 glass-panel rounded-3xl p-6 border border-white/10">
                <h3 className="text-base font-bold text-white mb-4">My Raised Grievance Tickets</h3>
                <div className="space-y-3">
                  {complaints.map((c) => (
                    <div key={c.id} className="glass-card rounded-2xl p-4 border border-white/10">
                      <div className="flex items-center justify-between">
                        <div className="flex items-center space-x-2">
                          <span className="text-sm font-semibold text-white">{c.title}</span>
                          <span className="text-[10px] font-bold px-2 py-0.5 rounded bg-indigo-500/20 text-indigo-300">
                            {c.category}
                          </span>
                        </div>
                        <span className={`text-[10px] font-bold px-2 py-0.5 rounded-full border ${
                          c.priority === 'CRITICAL' ? 'bg-red-500/20 text-red-300 border-red-500/30' :
                          c.priority === 'HIGH' ? 'bg-orange-500/20 text-orange-300 border-orange-500/30' :
                          'bg-blue-500/20 text-blue-300 border-blue-500/30'
                        }`}>
                          {c.priority} Priority
                        </span>
                      </div>
                      <p className="text-xs text-gray-300 mt-2">{c.description}</p>
                      <div className="mt-3 flex items-center justify-between text-[11px] text-gray-400 pt-2 border-t border-white/5">
                        <span>Status: <strong className="text-indigo-300">{c.status}</strong></span>
                        <span>Logged: {new Date(c.createdAt).toLocaleDateString()}</span>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            </motion.div>
          )}

          {/* TAB 7: ACADEMIC CALCULATOR */}
          {activeTab === 'calculator' && (
            <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="space-y-6">
              <div className="glass-panel rounded-3xl p-6 border border-white/10">
                <h2 className="text-xl font-bold text-white mb-2">Academic Grade & CGPA Calculator 📊</h2>
                <p className="text-xs text-gray-400">Perform SGPA, CGPA, target grade point estimations, and view your semester academic trend summary.</p>
                
                {/* Tabs inside calculator */}
                <div className="flex border-b border-white/10 my-6 space-x-6">
                  {['sgpa', 'cgpa', 'what-if', 'history'].map((t) => (
                    <button
                      key={t}
                      onClick={() => setCalcTab(t)}
                      className={`pb-2 text-xs font-bold transition-all border-b-2 uppercase tracking-wider ${
                        calcTab === t ? 'border-indigo-500 text-white' : 'border-transparent text-gray-400 hover:text-white'
                      }`}
                    >
                      {t.replace('-', ' ')}
                    </button>
                  ))}
                </div>

                {/* Sub Tab: SGPA */}
                {calcTab === 'sgpa' && (
                  <div className="space-y-4">
                    <h3 className="text-sm font-bold text-gray-300">SGPA Semester Estimator</h3>
                    <div className="space-y-2">
                      {sgpaRows.map((row, idx) => (
                        <div key={idx} className="flex flex-col sm:flex-row gap-2 items-center">
                          <input
                            type="text"
                            placeholder="Subject Name"
                            value={row.subjectName}
                            onChange={(e) => {
                              const arr = [...sgpaRows];
                              arr[idx].subjectName = e.target.value;
                              setSgpaRows(arr);
                            }}
                            className="flex-1 px-3 py-1.5 rounded-lg bg-slate-900 border border-white/10 text-xs focus:outline-none focus:border-indigo-500"
                          />
                          <input
                            type="number"
                            placeholder="Credits"
                            value={row.credits}
                            onChange={(e) => {
                              const arr = [...sgpaRows];
                              arr[idx].credits = e.target.value;
                              setSgpaRows(arr);
                            }}
                            className="w-20 px-3 py-1.5 rounded-lg bg-slate-900 border border-white/10 text-xs focus:outline-none focus:border-indigo-500"
                          />
                          <select
                            value={row.grade}
                            onChange={(e) => {
                              const arr = [...sgpaRows];
                              arr[idx].grade = e.target.value;
                              setSgpaRows(arr);
                            }}
                            className="w-24 px-3 py-1.5 rounded-lg bg-slate-900 border border-white/10 text-xs focus:outline-none focus:border-indigo-500"
                          >
                            {['A+', 'A', 'B+', 'B', 'C', 'D', 'E', 'F'].map(g => (
                              <option key={g} value={g}>{g}</option>
                            ))}
                          </select>
                          <button
                            onClick={() => setSgpaRows(sgpaRows.filter((_, i) => i !== idx))}
                            className="text-red-400 hover:text-red-300 text-xs font-bold px-2 py-1"
                          >
                            Remove
                          </button>
                        </div>
                      ))}
                    </div>
                    <div className="flex space-x-2">
                      <button
                        onClick={() => setSgpaRows([...sgpaRows, { subjectName: `Subject ${sgpaRows.length + 1}`, credits: 3, grade: 'A' }])}
                        className="px-3 py-1.5 rounded bg-slate-900 border border-white/15 text-xs text-indigo-400 font-semibold"
                      >
                        + Add Subject
                      </button>
                      <button
                        onClick={handleCalculateSgpa}
                        className="px-4 py-1.5 rounded bg-indigo-600 hover:bg-indigo-500 text-xs text-white font-bold"
                      >
                        Calculate SGPA
                      </button>
                    </div>

                    {sgpaCalcResult && (
                      <div className="p-4 rounded-xl bg-indigo-950/20 border border-indigo-500/20 mt-4 text-xs">
                        <div>Calculated SGPA: <strong className="text-white text-sm">{sgpaCalcResult.sgpa}</strong></div>
                        <div className="text-gray-400 mt-1">Total Credits: {sgpaCalcResult.totalCredits} | Total Weighted Points: {sgpaCalcResult.totalCreditPoints}</div>
                      </div>
                    )}
                  </div>
                )}

                {/* Sub Tab: CGPA */}
                {calcTab === 'cgpa' && (
                  <div className="space-y-4">
                    <h3 className="text-sm font-bold text-gray-300">CGPA Cumulative Estimator</h3>
                    <div className="space-y-2">
                      {cgpaRows.map((row, idx) => (
                        <div key={idx} className="flex flex-col sm:flex-row gap-2 items-center">
                          <input
                            type="number"
                            placeholder="Semester"
                            value={row.semester}
                            onChange={(e) => {
                              const arr = [...cgpaRows];
                              arr[idx].semester = e.target.value;
                              setCgpaRows(arr);
                            }}
                            className="w-24 px-3 py-1.5 rounded-lg bg-slate-900 border border-white/10 text-xs focus:outline-none focus:border-indigo-500"
                          />
                          <input
                            type="text"
                            placeholder="SGPA"
                            value={row.sgpa}
                            onChange={(e) => {
                              const arr = [...cgpaRows];
                              arr[idx].sgpa = e.target.value;
                              setCgpaRows(arr);
                            }}
                            className="w-24 px-3 py-1.5 rounded-lg bg-slate-900 border border-white/10 text-xs focus:outline-none focus:border-indigo-500"
                          />
                          <input
                            type="number"
                            placeholder="Credits"
                            value={row.credits}
                            onChange={(e) => {
                              const arr = [...cgpaRows];
                              arr[idx].credits = e.target.value;
                              setCgpaRows(arr);
                            }}
                            className="w-24 px-3 py-1.5 rounded-lg bg-slate-900 border border-white/10 text-xs focus:outline-none focus:border-indigo-500"
                          />
                          <button
                            onClick={() => setCgpaRows(cgpaRows.filter((_, i) => i !== idx))}
                            className="text-red-400 hover:text-red-300 text-xs font-bold px-2 py-1"
                          >
                            Remove
                          </button>
                        </div>
                      ))}
                    </div>
                    <div className="flex space-x-2">
                      <button
                        onClick={() => setCgpaRows([...cgpaRows, { semester: cgpaRows.length + 1, sgpa: '8.00', credits: 24 }])}
                        className="px-3 py-1.5 rounded bg-slate-900 border border-white/15 text-xs text-indigo-400 font-semibold"
                      >
                        + Add Semester
                      </button>
                      <button
                        onClick={handleCalculateCgpa}
                        className="px-4 py-1.5 rounded bg-indigo-600 hover:bg-indigo-500 text-xs text-white font-bold"
                      >
                        Calculate CGPA
                      </button>
                    </div>

                    {cgpaCalcResult && (
                      <div className="p-4 rounded-xl bg-indigo-950/20 border border-indigo-500/20 mt-4 text-xs">
                        <div>Calculated CGPA: <strong className="text-white text-sm">{cgpaCalcResult.cgpa}</strong></div>
                        <div className="text-gray-400 mt-1">Total Accumulated Credits: {cgpaCalcResult.totalCredits}</div>
                      </div>
                    )}
                  </div>
                )}

                {/* Sub Tab: What-If */}
                {calcTab === 'what-if' && (
                  <div className="space-y-6">
                    <div className="space-y-4">
                      <h3 className="text-sm font-bold text-gray-300">Grade Simulation (What-If SGPA)</h3>
                      {whatIfRows.length > 0 ? (
                        <div className="space-y-2">
                          {whatIfRows.map((row, idx) => (
                            <div key={idx} className="flex flex-col sm:flex-row gap-2 items-center bg-slate-900/40 p-2 rounded-lg border border-white/5">
                              <span className="flex-1 text-xs text-gray-300 truncate font-semibold">{row.subjectName}</span>
                              <div className="flex items-center space-x-2">
                                <span className="text-[10px] text-gray-500">Current Grade:</span>
                                <span className="px-1.5 py-0.5 bg-slate-800 text-gray-300 rounded font-bold text-[10px]">{row.currentGrade}</span>
                                <span className="text-[10px] text-gray-500">Projected Grade:</span>
                                <select
                                  value={row.projectedGrade}
                                  onChange={(e) => {
                                    const arr = [...whatIfRows];
                                    arr[idx].projectedGrade = e.target.value;
                                    setWhatIfRows(arr);
                                  }}
                                  className="px-2 py-1 rounded bg-slate-950 border border-white/10 text-xs text-white"
                                >
                                  {['A+', 'A', 'B+', 'B', 'C', 'D', 'E', 'F'].map(g => (
                                    <option key={g} value={g}>{g}</option>
                                  ))}
                                </select>
                              </div>
                            </div>
                          ))}
                          <button
                            onClick={handleCalculateWhatIf}
                            className="px-4 py-2 rounded bg-indigo-600 hover:bg-indigo-500 text-xs text-white font-bold mt-2"
                          >
                            Simulate What-If SGPA
                          </button>
                        </div>
                      ) : (
                        <p className="text-xs text-gray-500">No active marks records available for simulation.</p>
                      )}

                      {whatIfResult && (
                        <div className="p-4 rounded-xl bg-indigo-950/20 border border-indigo-500/20 mt-4 text-xs space-y-1">
                          <div>Current SGPA: <span className="text-gray-300">{whatIfResult.currentSGPA}</span></div>
                          <div>Projected SGPA: <strong className="text-white">{whatIfResult.projectedSGPA}</strong></div>
                          <div>
                            Difference: <span className={whatIfResult.difference >= 0 ? 'text-green-400 font-bold' : 'text-red-400 font-bold'}>
                              {whatIfResult.difference >= 0 ? `+${whatIfResult.difference}` : whatIfResult.difference}
                            </span>
                          </div>
                        </div>
                      )}
                    </div>

                    <div className="border-t border-white/10 pt-6 space-y-4">
                      <h3 className="text-sm font-bold text-gray-300">Target CGPA Required SGPA Estimator</h3>
                      <div className="grid grid-cols-2 gap-4">
                        <div>
                          <label className="block text-[10px] text-gray-400 uppercase tracking-wider mb-1">Current CGPA</label>
                          <input
                            type="text"
                            value={targetForm.currentCgpa}
                            onChange={(e) => setTargetForm({ ...targetForm, currentCgpa: e.target.value })}
                            className="w-full px-3 py-1.5 rounded-lg bg-slate-900 border border-white/10 text-xs"
                          />
                        </div>
                        <div>
                          <label className="block text-[10px] text-gray-400 uppercase tracking-wider mb-1">Completed Credits</label>
                          <input
                            type="number"
                            value={targetForm.completedCredits}
                            onChange={(e) => setTargetForm({ ...targetForm, completedCredits: e.target.value })}
                            className="w-full px-3 py-1.5 rounded-lg bg-slate-900 border border-white/10 text-xs"
                          />
                        </div>
                        <div>
                          <label className="block text-[10px] text-gray-400 uppercase tracking-wider mb-1">Future Credits</label>
                          <input
                            type="number"
                            value={targetForm.futureCredits}
                            onChange={(e) => setTargetForm({ ...targetForm, futureCredits: e.target.value })}
                            className="w-full px-3 py-1.5 rounded-lg bg-slate-900 border border-white/10 text-xs"
                          />
                        </div>
                        <div>
                          <label className="block text-[10px] text-gray-400 uppercase tracking-wider mb-1">Target CGPA</label>
                          <input
                            type="text"
                            value={targetForm.targetCgpa}
                            onChange={(e) => setTargetForm({ ...targetForm, targetCgpa: e.target.value })}
                            className="w-full px-3 py-1.5 rounded-lg bg-slate-900 border border-white/10 text-xs"
                          />
                        </div>
                      </div>
                      <button
                        onClick={handleCalculateTargetCgpa}
                        className="px-4 py-2 rounded bg-purple-600 hover:bg-purple-500 text-xs text-white font-bold"
                      >
                        Calculate Required future SGPA
                      </button>

                      {targetResult && (
                        <div className={`p-4 rounded-xl text-xs space-y-1 ${
                          targetResult.possible ? 'bg-green-950/20 border border-green-500/20 text-green-300' : 'bg-red-950/20 border border-red-500/20 text-red-300'
                        }`}>
                          <div className="font-bold">{targetResult.possible ? 'Achievable! ✅' : 'Impossible! ❌'}</div>
                          <p className="mt-1">{targetResult.explanation}</p>
                        </div>
                      )}
                    </div>
                  </div>
                )}

                {/* Sub Tab: Academic History */}
                {calcTab === 'history' && (
                  <div className="space-y-4">
                    <h3 className="text-sm font-bold text-gray-300">Semester Grade Summary Trend</h3>
                    {academicSummary && academicSummary.semesterHistory && academicSummary.semesterHistory.length > 0 ? (
                      <div className="overflow-x-auto">
                        <table className="w-full text-xs text-left border-collapse">
                          <thead>
                            <tr className="border-b border-white/10 text-gray-400">
                              <th className="py-2">Semester</th>
                              <th className="py-2">SGPA</th>
                              <th className="py-2">Credits Earned</th>
                            </tr>
                          </thead>
                          <tbody>
                            {academicSummary.semesterHistory.map((entry) => (
                              <tr key={entry.semester} className="border-b border-white/5 text-gray-200">
                                <td className="py-2 font-bold">Semester {entry.semester}</td>
                                <td className="py-2">{entry.sgpa}</td>
                                <td className="py-2">{entry.credits}</td>
                              </tr>
                            ))}
                          </tbody>
                        </table>
                        <div className="mt-4 p-4 rounded-xl bg-slate-900/60 border border-white/5">
                          <div className="flex justify-between text-xs text-gray-400">
                            <span>Cumulative CGPA: <strong className="text-indigo-400">{academicSummary.currentCGPA}</strong></span>
                            <span>Total Credits Accumulation: <strong className="text-indigo-400">{academicSummary.totalCredits}</strong></span>
                          </div>
                        </div>
                      </div>
                    ) : (
                      <p className="text-xs text-gray-500">No semester marks history records exist on database.</p>
                    )}
                  </div>
                )}

              </div>
            </motion.div>
          )}


          {/* TAB 8: ATS RESUME ANALYZER */}
          {activeTab === 'ats' && (
            <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="space-y-6">
              <div className="glass-panel rounded-3xl p-6 border border-white/10">
                <h2 className="text-xl font-bold text-white mb-2">Explainable ATS Resume Analyzer 🎯</h2>
                <p className="text-xs text-gray-400 mb-6">Match your structured resume against target job description requirements to identify skill gaps, experience alignment, and keyword matches.</p>

                <form onSubmit={handleAnalyzeAts} className="space-y-6">
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    {/* Resume Upload Column */}
                    <div className="glass-card rounded-2xl p-6 border border-white/5 bg-slate-900/40 space-y-4">
                      <h3 className="text-sm font-bold text-indigo-300">1. Upload Resume (PDF or DOCX)</h3>
                      <div className="border-2 border-dashed border-white/10 hover:border-indigo-500/50 rounded-2xl p-6 text-center cursor-pointer transition-all relative">
                        <input
                          type="file"
                          accept=".pdf,.docx"
                          onChange={(e) => setAtsFile(e.target.files[0])}
                          className="absolute inset-0 w-full h-full opacity-0 cursor-pointer"
                        />
                        <div className="space-y-2">
                          <p className="text-xs text-gray-300 font-semibold">
                            {atsFile ? `Selected: ${atsFile.name}` : "Drag and drop or click to upload resume"}
                          </p>
                          <p className="text-[10px] text-gray-500">Supports PDF & DOCX up to 10MB</p>
                        </div>
                      </div>
                    </div>

                    {/* Job Description Column */}
                    <div className="glass-card rounded-2xl p-6 border border-white/5 bg-slate-900/40 space-y-4">
                      <h3 className="text-sm font-bold text-indigo-300">2. Paste Job Description</h3>
                      <textarea
                        rows={6}
                        placeholder="Paste target job requirements details here..."
                        value={atsJd}
                        onChange={(e) => setAtsJd(e.target.value)}
                        className="w-full px-3 py-2 rounded-xl bg-slate-950 border border-white/10 text-xs text-gray-200 focus:outline-none focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500"
                      />
                    </div>
                  </div>

                  <button
                    type="submit"
                    disabled={atsLoading}
                    className="w-full py-3 rounded-2xl bg-gradient-to-r from-indigo-600 to-purple-600 text-white font-extrabold text-sm shadow-xl shadow-indigo-600/30 hover:scale-[1.01] active:scale-[0.99] transition-all disabled:opacity-50"
                  >
                    {atsLoading ? "Analyzing Compatibility..." : "Start ATS Matching Analysis"}
                  </button>
                </form>

                {/* ATS Results View */}
                {atsResult && (
                  <div className="mt-8 space-y-8 border-t border-white/10 pt-8">
                    {/* Score Dashboard */}
                    <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                      <div className="glass-card rounded-2xl p-6 border border-white/10 bg-gradient-to-br from-indigo-950/40 to-slate-900 flex flex-col items-center justify-center text-center">
                        <span className="text-[11px] text-gray-400 font-bold uppercase tracking-widest">ATS Match Score</span>
                        <div className="text-5xl font-extrabold text-transparent bg-clip-text bg-gradient-to-r from-emerald-400 to-teal-400 mt-2">
                          {atsResult.overallScore}%
                        </div>
                        <span className="text-[10px] text-gray-500 mt-2">Explainable heuristic compatibility index</span>
                      </div>

                      <div className="glass-card rounded-2xl p-6 border border-white/10 bg-slate-900/60 md:col-span-2 space-y-3">
                        <h4 className="text-xs font-bold text-gray-300 uppercase tracking-wider">Analysis Weight Score Breakdown</h4>
                        <div className="space-y-2 text-xs">
                          {Object.entries(atsResult.breakdown).map(([key, val]) => (
                            <div key={key} className="flex items-center justify-between">
                              <span className="capitalize text-gray-400">{key} Match</span>
                              <div className="flex items-center space-x-2 w-48">
                                <div className="flex-1 h-1.5 bg-slate-800 rounded-full overflow-hidden">
                                  <div
                                    className="h-full bg-indigo-500 rounded-full"
                                    style={{ width: `${(val / (key === 'skills' ? 40 : key === 'experience' ? 25 : key === 'education' ? 10 : key === 'keywords' ? 15 : 10)) * 100}%` }}
                                  />
                                </div>
                                <span className="text-gray-200 font-semibold w-8 text-right">
                                  {val}/{key === 'skills' ? 40 : key === 'experience' ? 25 : key === 'education' ? 10 : key === 'keywords' ? 15 : 10}
                                </span>
                              </div>
                            </div>
                          ))}
                        </div>
                      </div>
                    </div>

                    {/* Skill Match Panels */}
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                      <div className="glass-panel rounded-2xl p-5 border border-white/5 space-y-4">
                        <h4 className="text-xs font-bold text-emerald-400 uppercase tracking-wider">Matched Resume Skills</h4>
                        <div className="flex flex-wrap gap-2">
                          {atsResult.skillMatch.matchedRequiredSkills.length > 0 || atsResult.skillMatch.matchedPreferredSkills.length > 0 ? (
                            <>
                              {atsResult.skillMatch.matchedRequiredSkills.map(s => (
                                <span key={s} className="px-2.5 py-1 rounded bg-emerald-500/10 text-emerald-300 border border-emerald-500/20 text-[10px] font-medium">
                                  {s} (Required)
                                </span>
                              ))}
                              {atsResult.skillMatch.matchedPreferredSkills.map(s => (
                                <span key={s} className="px-2.5 py-1 rounded bg-blue-500/10 text-blue-300 border border-blue-500/20 text-[10px] font-medium">
                                  {s} (Preferred)
                                </span>
                              ))}
                            </>
                          ) : (
                            <span className="text-xs text-gray-500">No skills matched.</span>
                          )}
                        </div>
                      </div>

                      <div className="glass-panel rounded-2xl p-5 border border-white/5 space-y-4">
                        <h4 className="text-xs font-bold text-amber-400 uppercase tracking-wider">Missing Required/Preferred Skills</h4>
                        <div className="flex flex-wrap gap-2">
                          {atsResult.skillMatch.missingRequiredSkills.length > 0 || atsResult.skillMatch.missingPreferredSkills.length > 0 ? (
                            <>
                              {atsResult.skillMatch.missingRequiredSkills.map(s => (
                                <span key={s} className="px-2.5 py-1 rounded bg-red-500/10 text-red-300 border border-red-500/20 text-[10px] font-bold">
                                  {s} (Required)
                                </span>
                              ))}
                              {atsResult.skillMatch.missingPreferredSkills.map(s => (
                                <span key={s} className="px-2.5 py-1 rounded bg-amber-500/10 text-amber-300 border border-amber-500/20 text-[10px] font-medium">
                                  {s} (Preferred)
                                </span>
                              ))}
                            </>
                          ) : (
                            <span className="text-xs text-emerald-400">Excellent! All required and preferred skills are present in the resume.</span>
                          )}
                        </div>
                      </div>
                    </div>

                    {/* Experience, Education and Keyword Details */}
                    <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                      <div className="glass-panel rounded-2xl p-5 border border-white/5 space-y-2 text-xs">
                        <h4 className="text-xs font-bold text-gray-300 uppercase tracking-wider">Experience Verification</h4>
                        <div className="mt-2 text-gray-400">Required: <strong className="text-white">{atsResult.experienceMatch.requiredYears}</strong></div>
                        <div className="text-gray-400">Detected: <strong className="text-white">{atsResult.experienceMatch.detectedYears} years</strong></div>
                        <p className="mt-2 text-[11px] text-gray-300 bg-slate-950/40 p-2 rounded border border-white/5">{atsResult.experienceMatch.explanation}</p>
                      </div>

                      <div className="glass-panel rounded-2xl p-5 border border-white/5 space-y-2 text-xs">
                        <h4 className="text-xs font-bold text-gray-300 uppercase tracking-wider">Education Verification</h4>
                        <div className="mt-2 text-gray-400">Required Level: <strong className="text-white">{atsResult.educationMatch.requiredDegree}</strong></div>
                        <div className="text-gray-400">Detected Level: <strong className="text-white">{atsResult.educationMatch.detectedDegree}</strong></div>
                        <p className="mt-2 text-[11px] text-gray-300 bg-slate-950/40 p-2 rounded border border-white/5">{atsResult.educationMatch.explanation}</p>
                      </div>

                      <div className="glass-panel rounded-2xl p-5 border border-white/5 space-y-2 text-xs">
                        <h4 className="text-xs font-bold text-gray-300 uppercase tracking-wider">Keyword & Projects Relevance</h4>
                        <div className="mt-2 text-gray-400">Keyword Match Score: <strong className="text-white">{atsResult.keywordCoverage}/15</strong></div>
                        <div className="text-gray-400">Project Skills Score: <strong className="text-white">{atsResult.projectRelevance}/10</strong></div>
                        <p className="mt-2 text-[10px] text-gray-500 leading-relaxed">Unique keywords from the job description are matched once against resume segments to determine coverage.</p>
                      </div>
                    </div>

                    {/* Recommendations Panel */}
                    <div className="glass-panel rounded-2xl p-6 border border-white/10 space-y-4 bg-slate-900/20">
                      <h4 className="text-xs font-bold text-indigo-300 uppercase tracking-wider">ATS Improvement Recommendations</h4>
                      {atsResult.recommendations.length > 0 ? (
                        <div className="space-y-2 text-xs">
                          {atsResult.recommendations.map((rec, i) => (
                            <div key={i} className="flex items-start space-x-3 p-3 rounded-xl border border-white/5 bg-slate-950/40">
                              <span className={`px-2 py-0.5 rounded font-bold text-[9px] ${
                                rec.priority === 'HIGH' ? 'bg-red-500/10 text-red-400 border border-red-500/20' :
                                rec.priority === 'MEDIUM' ? 'bg-amber-500/10 text-amber-400 border border-amber-500/20' :
                                'bg-blue-500/10 text-blue-400 border border-blue-500/20'
                              }`}>
                                {rec.priority}
                              </span>
                              <div className="flex-1">
                                <span className="text-[10px] text-indigo-300 font-bold uppercase mr-1">[{rec.category}]</span>
                                <span className="text-gray-300">{rec.message}</span>
                              </div>
                            </div>
                          ))}
                        </div>
                      ) : (
                        <p className="text-xs text-green-400">No suggestions needed! Your resume has high alignment with the target requirements.</p>
                      )}
                    </div>
                  </div>
                )}
              </div>
            </motion.div>
          )}

          {/* TAB 6: SOS SAFETY INFO */}
          {activeTab === 'sos' && (
            <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="glass-panel rounded-3xl p-8 border border-red-500/30 text-center max-w-2xl mx-auto space-y-6">
              <div className="w-20 h-20 bg-red-600/20 text-red-500 rounded-full flex items-center justify-center mx-auto border border-red-500/40 animate-bounce">
                <ShieldAlert size={44} />
              </div>
              <h2 className="text-2xl font-bold text-white">Campus Distress SOS Signal</h2>
              <p className="text-xs text-gray-300 leading-relaxed">
                If you are in immediate physical danger, medical crisis, or safety distress anywhere on campus, triggering the SOS alert instantly dispatches your browser GPS coordinates to Security Command Center.
              </p>
              <button
                onClick={() => setIsSOSOpen(true)}
                className="px-8 py-3.5 rounded-2xl bg-gradient-to-r from-red-600 via-rose-600 to-pink-600 text-white font-extrabold text-base shadow-2xl shadow-red-600/40 hover:scale-105 active:scale-95 transition-all pulse-sos"
              >
                OPEN SOS DISPATCH PANEL
              </button>
            </motion.div>
          )}
        </main>
      </div>

      <SOSModal isOpen={isSOSOpen} onClose={() => setIsSOSOpen(false)} />
    </div>
  );
}

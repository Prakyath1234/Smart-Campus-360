const getApiBaseUrl = () => {
  const envUrl = import.meta.env.VITE_API_URL || import.meta.env.VITE_API_BASE_URL;
  if (!envUrl) {
    return '/api';
  }
  const cleanUrl = envUrl.replace(/\/+$/, '');
  return cleanUrl.endsWith('/api') ? cleanUrl : `${cleanUrl}/api`;
};

const API_BASE_URL = getApiBaseUrl();

const getAuthHeaders = () => {
  const token = localStorage.getItem('smart_campus_token');
  return token ? { 'Authorization': `Bearer ${token}` } : {};
};

const handleResponse = async (response) => {
  if (response.status === 401 || response.status === 403) {
    if (localStorage.getItem('smart_campus_token')) {
      localStorage.removeItem('smart_campus_token');
      localStorage.removeItem('smart_campus_user');
      window.location.href = '/login';
    }
  }

  const contentType = response.headers.get('content-type');
  let data = null;
  if (contentType && contentType.includes('application/json')) {
    data = await response.json();
  } else if (contentType && (contentType.includes('text/csv') || contentType.includes('pdf') || contentType.includes('zip'))) {
    data = await response.blob();
  } else {
    data = await response.text();
  }

  if (!response.ok) {
    const errorMsg = (data && data.message) || (data && data.error) || (typeof data === 'string' && data) || 'An error occurred';
    throw new Error(errorMsg);
  }

  return data;
};

export const api = {
  // Auth API
  login: async (credentials) => {
    const res = await fetch(`${API_BASE_URL}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(credentials),
    });
    return handleResponse(res);
  },

  register: async (userData) => {
    const res = await fetch(`${API_BASE_URL}/auth/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(userData),
    });
    return handleResponse(res);
  },

  resetPassword: async (email, newPassword) => {
    const res = await fetch(`${API_BASE_URL}/auth/reset-password`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, newPassword }),
    });
    return handleResponse(res);
  },

  getDepartments: async () => {
    const res = await fetch(`${API_BASE_URL}/auth/departments`);
    return handleResponse(res);
  },

  // Student API
  getStudentProfile: async () => {
    const res = await fetch(`${API_BASE_URL}/student/profile`, { headers: getAuthHeaders() });
    return handleResponse(res);
  },
  getStudentAttendance: async () => {
    const res = await fetch(`${API_BASE_URL}/student/attendance`, { headers: getAuthHeaders() });
    return handleResponse(res);
  },
  getStudentMarks: async () => {
    const res = await fetch(`${API_BASE_URL}/student/marks`, { headers: getAuthHeaders() });
    return handleResponse(res);
  },
  getStudentTimetable: async (semester) => {
    const url = semester ? `${API_BASE_URL}/student/timetable/${semester}` : `${API_BASE_URL}/student/timetable`;
    const res = await fetch(url, { headers: getAuthHeaders() });
    return handleResponse(res);
  },
  getStudentLeaveRequests: async () => {
    const res = await fetch(`${API_BASE_URL}/student/leave-requests`, { headers: getAuthHeaders() });
    return handleResponse(res);
  },
  createLeaveRequest: async (leaveData) => {
    const res = await fetch(`${API_BASE_URL}/student/leave-requests`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', ...getAuthHeaders() },
      body: JSON.stringify(leaveData),
    });
    return handleResponse(res);
  },
  getStudentComplaints: async () => {
    const res = await fetch(`${API_BASE_URL}/student/complaints`, { headers: getAuthHeaders() });
    return handleResponse(res);
  },
  createComplaint: async (complaintData) => {
    const res = await fetch(`${API_BASE_URL}/student/complaints`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', ...getAuthHeaders() },
      body: JSON.stringify(complaintData),
    });
    return handleResponse(res);
  },
  getStudentPerformance: async () => {
    const res = await fetch(`${API_BASE_URL}/student/performance`, { headers: getAuthHeaders() });
    return handleResponse(res);
  },
  getStudentServiceRequests: async () => {
    const res = await fetch(`${API_BASE_URL}/student/service-requests`, { headers: getAuthHeaders() });
    return handleResponse(res);
  },
  createServiceRequest: async (data) => {
    const res = await fetch(`${API_BASE_URL}/student/service-requests`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', ...getAuthHeaders() },
      body: JSON.stringify(data),
    });
    return handleResponse(res);
  },

  // Faculty API
  getFacultyProfile: async () => {
    const res = await fetch(`${API_BASE_URL}/faculty/profile`, { headers: getAuthHeaders() });
    return handleResponse(res);
  },
  getFacultySubjects: async () => {
    const res = await fetch(`${API_BASE_URL}/faculty/subjects`, { headers: getAuthHeaders() });
    return handleResponse(res);
  },
  getFacultyClasses: async () => {
    const res = await fetch(`${API_BASE_URL}/faculty/classes`, { headers: getAuthHeaders() });
    return handleResponse(res);
  },
  getFacultyTimetable: async () => {
    const res = await fetch(`${API_BASE_URL}/faculty/classes`, { headers: getAuthHeaders() });
    return handleResponse(res);
  },
  getFacultyStudents: async (subjectId) => {
    const res = await fetch(`${API_BASE_URL}/faculty/students?subjectId=${subjectId}`, { headers: getAuthHeaders() });
    return handleResponse(res);
  },
  saveAttendance: async (attendanceRecords) => {
    const res = await fetch(`${API_BASE_URL}/faculty/attendance`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', ...getAuthHeaders() },
      body: JSON.stringify(attendanceRecords),
    });
    return handleResponse(res);
  },
  saveMarks: async (marksData) => {
    const res = await fetch(`${API_BASE_URL}/faculty/marks`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', ...getAuthHeaders() },
      body: JSON.stringify(marksData),
    });
    return handleResponse(res);
  },
  getFacultyLeaveRequests: async () => {
    const res = await fetch(`${API_BASE_URL}/faculty/leave-requests`, { headers: getAuthHeaders() });
    return handleResponse(res);
  },
  updateLeaveStatus: async (id, status, reviewComments) => {
    const res = await fetch(`${API_BASE_URL}/faculty/leave-requests/${id}?status=${status}&reviewComments=${encodeURIComponent(reviewComments || '')}`, {
      method: 'PUT',
      headers: getAuthHeaders(),
    });
    return handleResponse(res);
  },
  getFacultyMentees: async () => {
    const res = await fetch(`${API_BASE_URL}/faculty/mentees`, { headers: getAuthHeaders() });
    return handleResponse(res);
  },
  getMenteePerformance: async (studentId) => {
    const res = await fetch(`${API_BASE_URL}/faculty/mentees/${studentId}/performance`, { headers: getAuthHeaders() });
    return handleResponse(res);
  },
  createMentorNote: async (noteData) => {
    const res = await fetch(`${API_BASE_URL}/faculty/mentor-notes`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', ...getAuthHeaders() },
      body: JSON.stringify(noteData),
    });
    return handleResponse(res);
  },
  getMentorNotes: async (studentId) => {
    const res = await fetch(`${API_BASE_URL}/faculty/mentees/${studentId}/notes`, { headers: getAuthHeaders() });
    return handleResponse(res);
  },

  // Admin API & Full CRUD
  getAdminStats: async () => {
    const res = await fetch(`${API_BASE_URL}/admin/stats`, { headers: getAuthHeaders() });
    return handleResponse(res);
  },
  getAuditLogs: async (page = 0, size = 15, actor = '', action = '', entityType = '') => {
    let url = `${API_BASE_URL}/admin/audit-logs?page=${page}&size=${size}`;
    if (actor) url += `&actor=${encodeURIComponent(actor)}`;
    if (action) url += `&action=${encodeURIComponent(action)}`;
    if (entityType) url += `&entityType=${encodeURIComponent(entityType)}`;
    const res = await fetch(url, { headers: getAuthHeaders() });
    return handleResponse(res);
  },
  getUsers: async (page = 0, size = 10, search = '', role = '', enabled = '') => {
    let url = `${API_BASE_URL}/admin/users?page=${page}&size=${size}`;
    if (search) url += `&search=${encodeURIComponent(search)}`;
    if (role) url += `&role=${encodeURIComponent(role)}`;
    if (enabled !== '') url += `&enabled=${enabled}`;
    const res = await fetch(url, { headers: getAuthHeaders() });
    return handleResponse(res);
  },
  createUser: async (userData) => {
    const res = await fetch(`${API_BASE_URL}/admin/users`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', ...getAuthHeaders() },
      body: JSON.stringify(userData),
    });
    return handleResponse(res);
  },
  updateUser: async (id, userData) => {
    const res = await fetch(`${API_BASE_URL}/admin/users/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json', ...getAuthHeaders() },
      body: JSON.stringify(userData),
    });
    return handleResponse(res);
  },
  toggleUserStatus: async (id, enabled) => {
    const res = await fetch(`${API_BASE_URL}/admin/users/${id}/status?enabled=${enabled}`, {
      method: 'PATCH',
      headers: getAuthHeaders(),
    });
    return handleResponse(res);
  },
  deleteUser: async (id) => {
    const res = await fetch(`${API_BASE_URL}/admin/users/${id}`, {
      method: 'DELETE',
      headers: getAuthHeaders(),
    });
    return handleResponse(res);
  },

  getStudents: async (page = 0, size = 10, search = '', departmentId = '', semester = '') => {
    let url = `${API_BASE_URL}/admin/students?page=${page}&size=${size}`;
    if (search) url += `&search=${encodeURIComponent(search)}`;
    if (departmentId) url += `&departmentId=${departmentId}`;
    if (semester) url += `&semester=${semester}`;
    const res = await fetch(url, { headers: getAuthHeaders() });
    return handleResponse(res);
  },
  createStudent: async (studentData) => {
    const res = await fetch(`${API_BASE_URL}/admin/students`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', ...getAuthHeaders() },
      body: JSON.stringify(studentData),
    });
    return handleResponse(res);
  },
  updateStudent: async (id, studentData) => {
    const res = await fetch(`${API_BASE_URL}/admin/students/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json', ...getAuthHeaders() },
      body: JSON.stringify(studentData),
    });
    return handleResponse(res);
  },
  toggleStudentStatus: async (id, enabled) => {
    const res = await fetch(`${API_BASE_URL}/admin/students/${id}/status?enabled=${enabled}`, {
      method: 'PATCH',
      headers: getAuthHeaders(),
    });
    return handleResponse(res);
  },

  getFaculties: async (page = 0, size = 10, search = '', departmentId = '') => {
    let url = `${API_BASE_URL}/admin/faculties?page=${page}&size=${size}`;
    if (search) url += `&search=${encodeURIComponent(search)}`;
    if (departmentId) url += `&departmentId=${departmentId}`;
    const res = await fetch(url, { headers: getAuthHeaders() });
    return handleResponse(res);
  },
  createFaculty: async (facultyData) => {
    const res = await fetch(`${API_BASE_URL}/admin/faculties`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', ...getAuthHeaders() },
      body: JSON.stringify(facultyData),
    });
    return handleResponse(res);
  },
  updateFaculty: async (id, facultyData) => {
    const res = await fetch(`${API_BASE_URL}/admin/faculties/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json', ...getAuthHeaders() },
      body: JSON.stringify(facultyData),
    });
    return handleResponse(res);
  },
  deleteFaculty: async (id) => {
    const res = await fetch(`${API_BASE_URL}/admin/faculties/${id}`, {
      method: 'DELETE',
      headers: getAuthHeaders(),
    });
    return handleResponse(res);
  },

  getAdminDepartments: async () => {
    const res = await fetch(`${API_BASE_URL}/admin/departments`, { headers: getAuthHeaders() });
    return handleResponse(res);
  },
  createDepartment: async (deptData) => {
    const res = await fetch(`${API_BASE_URL}/admin/departments`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', ...getAuthHeaders() },
      body: JSON.stringify(deptData),
    });
    return handleResponse(res);
  },
  deleteDepartment: async (id) => {
    const res = await fetch(`${API_BASE_URL}/admin/departments/${id}`, {
      method: 'DELETE',
      headers: getAuthHeaders(),
    });
    return handleResponse(res);
  },
  getSubjects: async () => {
    const res = await fetch(`${API_BASE_URL}/admin/subjects`, { headers: getAuthHeaders() });
    return handleResponse(res);
  },
  createSubject: async (subjectData) => {
    const res = await fetch(`${API_BASE_URL}/admin/subjects`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', ...getAuthHeaders() },
      body: JSON.stringify(subjectData),
    });
    return handleResponse(res);
  },
  createTimetableSlot: async (timetableData) => {
    const res = await fetch(`${API_BASE_URL}/admin/timetable`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', ...getAuthHeaders() },
      body: JSON.stringify(timetableData),
    });
    return handleResponse(res);
  },
  getAdminComplaints: async () => {
    const res = await fetch(`${API_BASE_URL}/admin/complaints`, { headers: getAuthHeaders() });
    return handleResponse(res);
  },
  updateComplaintStatus: async (id, status, resolutionComments) => {
    const res = await fetch(`${API_BASE_URL}/admin/complaints/${id}?status=${status}&resolutionComments=${encodeURIComponent(resolutionComments || '')}`, {
      method: 'PUT',
      headers: getAuthHeaders(),
    });
    return handleResponse(res);
  },
  getAdminServiceRequests: async (page = 0, size = 10, search = '', requestType = '', status = '') => {
    let url = `${API_BASE_URL}/admin/service-requests?page=${page}&size=${size}`;
    if (search) url += `&search=${encodeURIComponent(search)}`;
    if (requestType) url += `&requestType=${encodeURIComponent(requestType)}`;
    if (status) url += `&status=${encodeURIComponent(status)}`;
    const res = await fetch(url, { headers: getAuthHeaders() });
    return handleResponse(res);
  },
  updateServiceRequestStatus: async (id, status, comments) => {
    const res = await fetch(`${API_BASE_URL}/admin/service-requests/${id}/status?status=${status}&comments=${encodeURIComponent(comments || '')}`, {
      method: 'PUT',
      headers: getAuthHeaders(),
    });
    return handleResponse(res);
  },
  getAtRiskStudents: async () => {
    const res = await fetch(`${API_BASE_URL}/admin/analytics/at-risk`, { headers: getAuthHeaders() });
    return handleResponse(res);
  },
  assignMentor: async (mentorId, menteeId) => {
    const res = await fetch(`${API_BASE_URL}/admin/mentorship/assign?mentorId=${mentorId}&menteeId=${menteeId}`, {
      method: 'POST',
      headers: getAuthHeaders(),
    });
    return handleResponse(res);
  },

  // Announcements API
  getAnnouncements: async () => {
    const res = await fetch(`${API_BASE_URL}/announcements`, { headers: getAuthHeaders() });
    return handleResponse(res);
  },
  getAllAnnouncements: async () => {
    const res = await fetch(`${API_BASE_URL}/announcements/all`, { headers: getAuthHeaders() });
    return handleResponse(res);
  },
  createAnnouncement: async (data) => {
    const res = await fetch(`${API_BASE_URL}/announcements`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', ...getAuthHeaders() },
      body: JSON.stringify(data),
    });
    return handleResponse(res);
  },
  deleteAnnouncement: async (id) => {
    const res = await fetch(`${API_BASE_URL}/announcements/${id}`, {
      method: 'DELETE',
      headers: getAuthHeaders(),
    });
    return handleResponse(res);
  },

  // Emergency & Telemetry Analytics API
  triggerSOS: async (sosData) => {
    const res = await fetch(`${API_BASE_URL}/emergency/trigger`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', ...getAuthHeaders() },
      body: JSON.stringify(sosData),
    });
    return handleResponse(res);
  },
  getEmergencyAlerts: async () => {
    const res = await fetch(`${API_BASE_URL}/emergency/alerts`, { headers: getAuthHeaders() });
    return handleResponse(res);
  },
  getEmergencyAnalytics: async () => {
    const res = await fetch(`${API_BASE_URL}/emergency/analytics`, { headers: getAuthHeaders() });
    return handleResponse(res);
  },
  updateAlertStatus: async (id, status) => {
    const res = await fetch(`${API_BASE_URL}/emergency/${id}/status?status=${status}`, {
      method: 'PUT',
      headers: getAuthHeaders(),
    });
    return handleResponse(res);
  },

  // Reports API (CSV)
  downloadReportCsv: async (type) => {
    const token = localStorage.getItem('smart_campus_token');
    const res = await fetch(`${API_BASE_URL}/reports/csv?type=${type}`, {
      headers: token ? { 'Authorization': `Bearer ${token}` } : {},
    });
    if (!res.ok) throw new Error('Failed to download report');
    const blob = await res.blob();
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `${type}_report.csv`;
    document.body.appendChild(a);
    a.click();
    a.remove();
  },

  // Notifications API
  getNotifications: async () => {
    const res = await fetch(`${API_BASE_URL}/notifications`, { headers: getAuthHeaders() });
    return handleResponse(res);
  },
  markNotificationRead: async (id) => {
    const res = await fetch(`${API_BASE_URL}/notifications/${id}/read`, {
      method: 'PUT',
      headers: getAuthHeaders(),
    });
    return handleResponse(res);
  },

  // SmartTools APIs
  mergePdfs: async (formData) => {
    const res = await fetch(`${API_BASE_URL}/tools/pdf/merge`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: formData,
    });
    return handleResponse(res);
  },
  splitPdf: async (formData) => {
    const res = await fetch(`${API_BASE_URL}/tools/pdf/split`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: formData,
    });
    return handleResponse(res);
  },
  extractPages: async (formData) => {
    const res = await fetch(`${API_BASE_URL}/tools/pdf/extract`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: formData,
    });
    return handleResponse(res);
  },
  rotatePdf: async (formData) => {
    const res = await fetch(`${API_BASE_URL}/tools/pdf/rotate`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: formData,
    });
    return handleResponse(res);
  },
  reorderPdf: async (formData) => {
    const res = await fetch(`${API_BASE_URL}/tools/pdf/reorder`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: formData,
    });
    return handleResponse(res);
  },
  wordToPdf: async (formData) => {
    const res = await fetch(`${API_BASE_URL}/tools/convert/word-to-pdf`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: formData,
    });
    return handleResponse(res);
  },
  powerPointToPdf: async (formData) => {
    const res = await fetch(`${API_BASE_URL}/tools/convert/powerpoint-to-pdf`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: formData,
    });
    return handleResponse(res);
  },
  excelToPdf: async (formData) => {
    const res = await fetch(`${API_BASE_URL}/tools/convert/excel-to-pdf`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: formData,
    });
    return handleResponse(res);
  },
  imagesToPdf: async (formData) => {
    const res = await fetch(`${API_BASE_URL}/tools/image/to-pdf`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: formData,
    });
    return handleResponse(res);
  },
  pdfToWord: async (formData) => {
    const res = await fetch(`${API_BASE_URL}/tools/convert/pdf-to-word`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: formData,
    });
    return handleResponse(res);
  },
  resumeParse: async (formData) => {
    const res = await fetch(`${API_BASE_URL}/resume/parse`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: formData,
    });
    return handleResponse(res);
  },
  calculateSGPA: async (data) => {
    const res = await fetch(`${API_BASE_URL}/academic/calculator/sgpa`, {
      method: 'POST',
      headers: { ...getAuthHeaders(), 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    });
    return handleResponse(res);
  },
  calculateCGPA: async (data) => {
    const res = await fetch(`${API_BASE_URL}/academic/calculator/cgpa`, {
      method: 'POST',
      headers: { ...getAuthHeaders(), 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    });
    return handleResponse(res);
  },
  calculateWhatIf: async (data) => {
    const res = await fetch(`${API_BASE_URL}/academic/calculator/what-if`, {
      method: 'POST',
      headers: { ...getAuthHeaders(), 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    });
    return handleResponse(res);
  },
  calculateTargetCGPA: async (data) => {
    const res = await fetch(`${API_BASE_URL}/academic/calculator/target-cgpa`, {
      method: 'POST',
      headers: { ...getAuthHeaders(), 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    });
    return handleResponse(res);
  },
  getAcademicSummary: async () => {
    const res = await fetch(`${API_BASE_URL}/student/academic/summary`, {
      method: 'GET',
      headers: getAuthHeaders(),
    });
    return handleResponse(res);
  },
  analyzeAts: async (formData) => {
    const res = await fetch(`${API_BASE_URL}/ats/analyze`, {
      method: 'POST',
      headers: getAuthHeaders(), // Boundary is set automatically by fetch when body is FormData
      body: formData,
    });
    return handleResponse(res);
  },
};

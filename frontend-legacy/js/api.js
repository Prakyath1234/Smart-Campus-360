const BASE_URL = 'http://localhost:8080';

// API Utility functions
const API = {
    // Get authorization headers containing Bearer token
    getHeaders: function() {
        const token = localStorage.getItem('token');
        const headers = {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        };
        if (token) {
            headers['Authorization'] = 'Bearer ' + token;
        }
        return headers;
    },

    // Handle standard request responses
    handleResponse: async function(response) {
        if (response.status === 401 || response.status === 403) {
            // Unauthorized/Forbidden - clear token and send to login page
            this.logout();
            const currentPath = window.location.pathname;
            if (!currentPath.endsWith('login.html') && !currentPath.endsWith('register.html') && !currentPath.endsWith('index.html')) {
                window.location.href = '/login.html';
            }
            throw new Error('Authentication expired or unauthorized access.');
        }

        const data = await response.json();
        if (!response.ok) {
            throw new Error(data.message || 'Request failed with status ' + response.status);
        }
        return data;
    },

    // GET Request
    get: async function(endpoint) {
        try {
            const response = await fetch(`${BASE_URL}${endpoint}`, {
                method: 'GET',
                headers: this.getHeaders()
            });
            return await this.handleResponse(response);
        } catch (error) {
            console.error('GET Error:', error);
            throw error;
        }
    },

    // POST Request
    post: async function(endpoint, body) {
        try {
            const response = await fetch(`${BASE_URL}${endpoint}`, {
                method: 'POST',
                headers: this.getHeaders(),
                body: JSON.stringify(body)
            });
            return await this.handleResponse(response);
        } catch (error) {
            console.error('POST Error:', error);
            throw error;
        }
    },

    // PUT Request
    put: async function(endpoint, body = {}) {
        try {
            const response = await fetch(`${BASE_URL}${endpoint}`, {
                method: 'PUT',
                headers: this.getHeaders(),
                body: JSON.stringify(body)
            });
            return await this.handleResponse(response);
        } catch (error) {
            console.error('PUT Error:', error);
            throw error;
        }
    },

    // PUT Request with Query Parameters instead of body
    putWithParams: async function(endpoint) {
        try {
            const response = await fetch(`${BASE_URL}${endpoint}`, {
                method: 'PUT',
                headers: this.getHeaders()
            });
            return await this.handleResponse(response);
        } catch (error) {
            console.error('PUT Params Error:', error);
            throw error;
        }
    },

    // DELETE Request
    delete: async function(endpoint) {
        try {
            const response = await fetch(`${BASE_URL}${endpoint}`, {
                method: 'DELETE',
                headers: this.getHeaders()
            });
            return await this.handleResponse(response);
        } catch (error) {
            console.error('DELETE Error:', error);
            throw error;
        }
    },

    // User authentication status helpers
    isAuthenticated: function() {
        return localStorage.getItem('token') !== null;
    },

    getUserRole: function() {
        return localStorage.getItem('role');
    },

    getUserName: function() {
        return localStorage.getItem('name');
    },

    getUserId: function() {
        return localStorage.getItem('userId');
    },

    getProfileId: function() {
        return localStorage.getItem('profileId');
    },

    logout: function() {
        localStorage.clear();
        // Redirect to login at root
        const isSubdir = window.location.pathname.includes('/student/') || 
                         window.location.pathname.includes('/faculty/') || 
                         window.location.pathname.includes('/admin/') || 
                         window.location.pathname.includes('/security/');
        window.location.href = isSubdir ? '../login.html' : 'login.html';
    },

    checkAccess: function(allowedRoles) {
        if (!this.isAuthenticated()) {
            this.logout();
            return false;
        }
        const role = this.getUserRole();
        if (allowedRoles && !allowedRoles.includes(role)) {
            alert('Access Denied: You do not have the required permissions.');
            this.redirectToDashboard(role);
            return false;
        }
        return true;
    },

    redirectToDashboard: function(role) {
        const prefix = role.toLowerCase();
        window.location.href = `${prefix}/dashboard.html`;
    }
};

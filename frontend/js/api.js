// API_BASE_URL is set dynamically:
// - In production: set via env-config.js (injected by Vercel build)
// - In local dev: falls back to relative Tomcat context path
const API_BASE_URL = (window.__ENV__ && window.__ENV__.API_BASE_URL) || 'https://suraksha-scan.onrender.com';

async function authenticatedFetch(url, options = {}) {
    options.credentials = 'include';
    
    if (!options.headers) {
        options.headers = {};
    }
    
    if (options.body && typeof options.body === 'object') {
        options.body = JSON.stringify(options.body);
        options.headers['Content-Type'] = 'application/json';
    }

    try {
        const response = await fetch(`${API_BASE_URL}${url}`, options);
        if (response.status === 401 && !window.location.pathname.endsWith('login.html')) {
            window.location.href = 'login.html?expired=true';
            return null;
        }
        
        let data;
        try {
            data = await response.json();
        } catch (e) {
            data = { success: false, message: 'Invalid server response' };
        }
        
        if (!response.ok) {
            if (response.status === 429) {
                data.message = "Too many requests. Please wait and try again.";
            } else if (response.status === 403) {
                data.message = "You do not have permission to perform this action.";
            } else if (response.status === 500) {
                data.message = "Something went wrong. Please try again later.";
            }
        }
        return data;
    } catch (err) {
        console.error('API Error:', err);
        return { success: false, message: "Network error. Please check your connection." };
    }
}

async function checkAuthAndInit(callback) {
    const data = await authenticatedFetch('/api/auth/me');
    if (data && data.success) {
        if(callback) callback(data.user);
    }
}

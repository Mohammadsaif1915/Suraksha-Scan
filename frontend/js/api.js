const API_BASE_URL = '/surakshascan-backend';

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
        if (response.status === 401) {
            window.location.href = 'login.html';
            return null;
        }
        return await response.json();
    } catch (err) {
        console.error('API Error:', err);
        throw err;
    }
}

async function checkAuthAndInit(callback) {
    const data = await authenticatedFetch('/api/auth/me');
    if (data && data.success) {
        if(callback) callback(data.user);
    }
}

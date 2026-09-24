const API_BASE = '/surakshascan-backend';

document.addEventListener('DOMContentLoaded', async () => {
    try {
        const response = await fetch(${API_BASE}/api/auth/me, { credentials: 'include' });
        
        if (response.ok) {
            const data = await response.json();
            if (data.authenticated) {
                document.getElementById('dashboardBody').style.display = 'block';
                document.getElementById('welcomeMessage').innerText = Welcome,  + data.user.name +  ( + data.user.role + );
            } else {
                window.location.href = 'login.html';
            }
        } else {
            window.location.href = 'login.html';
        }
    } catch (err) {
        window.location.href = 'login.html';
    }

    const logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', async () => {
            logoutBtn.disabled = true;
            try {
                await fetch(${API_BASE}/api/auth/logout, { method: 'POST', credentials: 'include' });
                window.location.href = 'login.html';
            } catch (err) {
                window.location.href = 'login.html';
            }
        });
    }
});

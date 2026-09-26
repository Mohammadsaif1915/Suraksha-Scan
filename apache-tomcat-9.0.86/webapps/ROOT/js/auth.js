/* frontend/js/auth.js — SurakshaScan Login & Registration Logic */
/* Note: api.js must be loaded before this file — defines API_BASE_URL */

document.addEventListener('DOMContentLoaded', () => {
    const loginForm = document.getElementById('loginForm');
    const registerForm = document.getElementById('registerForm');

    // ── Login page ──────────────────────────────────────────────────────────────
    if (loginForm) {
        const urlParams = new URLSearchParams(window.location.search);
        if (urlParams.get('expired') === 'true') {
            const errorDiv = document.getElementById('loginError');
            if (errorDiv) {
                errorDiv.textContent = 'Your session has expired. Please sign in again.';
                errorDiv.style.display = 'block';
            }
        }

        loginForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const email    = document.getElementById('email').value.trim();
            const password = document.getElementById('password').value;
            const errorDiv = document.getElementById('loginError');
            const submitBtn = document.getElementById('loginBtn');

            errorDiv.style.display = 'none';
            submitBtn.disabled = true;
            submitBtn.textContent = 'Signing in...';

            try {
                const response = await fetch(`${API_BASE_URL}/api/auth/login`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    credentials: 'include',
                    body: JSON.stringify({ email, password })
                });

                let data;
                try { data = await response.json(); } catch (_) { data = {}; }

                if (response.ok && data.success) {
                    window.location.href = 'dashboard.html';
                } else if (response.status === 429) {
                    errorDiv.textContent = 'Too many login attempts. Please wait and try again.';
                    errorDiv.style.display = 'block';
                } else {
                    errorDiv.textContent = data.message || 'Invalid credentials. Please try again.';
                    errorDiv.style.display = 'block';
                }
            } catch (err) {
                errorDiv.textContent = 'Unable to connect. Please check your network and try again.';
                errorDiv.style.display = 'block';
            } finally {
                submitBtn.disabled = false;
                submitBtn.textContent = 'Sign In';
            }
        });
    }

    // ── Register page ───────────────────────────────────────────────────────────
    if (registerForm) {
        registerForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const name     = document.getElementById('regName').value.trim();
            const email    = document.getElementById('regEmail').value.trim();
            const password = document.getElementById('regPassword').value;
            const role     = document.getElementById('regRole').value;

            const errorDiv  = document.getElementById('registerError');
            const successDiv = document.getElementById('registerSuccess');
            const submitBtn = document.getElementById('registerBtn');

            errorDiv.style.display = 'none';
            successDiv.style.display = 'none';

            // Basic frontend validation
            if (!name) {
                errorDiv.textContent = 'Full name is required.';
                errorDiv.style.display = 'block';
                return;
            }
            if (!email || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
                errorDiv.textContent = 'Please enter a valid email address.';
                errorDiv.style.display = 'block';
                return;
            }
            if (password.length < 6) {
                errorDiv.textContent = 'Password must be at least 6 characters.';
                errorDiv.style.display = 'block';
                return;
            }

            submitBtn.disabled = true;
            submitBtn.textContent = 'Registering...';

            try {
                const response = await fetch(`${API_BASE_URL}/api/auth/register`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    credentials: 'include',
                    body: JSON.stringify({ name, email, password, role })
                });

                let data;
                try { data = await response.json(); } catch (_) { data = {}; }

                if (response.ok && data.success) {
                    successDiv.textContent = data.message || 'Registration successful! Redirecting to login...';
                    successDiv.style.display = 'block';
                    registerForm.reset();
                    setTimeout(() => { window.location.href = 'login.html'; }, 2000);
                } else if (response.status === 409) {
                    errorDiv.textContent = 'An account with this email already exists.';
                    errorDiv.style.display = 'block';
                } else {
                    errorDiv.textContent = data.message || 'Registration failed. Please try again.';
                    errorDiv.style.display = 'block';
                }
            } catch (err) {
                errorDiv.textContent = 'Unable to connect. Please check your network and try again.';
                errorDiv.style.display = 'block';
            } finally {
                submitBtn.disabled = false;
                submitBtn.textContent = 'Register';
            }
        });
    }
});

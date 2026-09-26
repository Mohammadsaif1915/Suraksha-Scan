function initGuardianPage(user) {
    const main = document.getElementById('mainContent');
    if (user.role === 'guardian') {
        renderGuardianView(main, user);
    } else {
        renderDependentView(main, user);
    }
}

// ─── GUARDIAN VIEW ─────────────────────────────────────────────────────────────
async function renderGuardianView(container, user) {
    container.innerHTML = `
        <div class="section-card">
            <h3>Add Family Member</h3>
            <div class="form-row">
                <input type="email" id="depEmail" placeholder="Dependent's email address" maxlength="255">
                <button class="btn btn-primary" id="sendRequestBtn">Send Link Request</button>
            </div>
            <div class="msg-box" id="requestMsg"></div>
        </div>

        <div class="section-card">
            <h3>Family Connections</h3>
            <div id="linksContainer"><div class="empty-state">Loading...</div></div>
        </div>

        <div class="section-card">
            <h3>Safety Alerts <span id="unreadBadge" style="display:none;" class="badge badge-unread">0 new</span></h3>
            <div id="alertsContainer"><div class="empty-state">Loading...</div></div>
        </div>`;

    document.getElementById('sendRequestBtn').addEventListener('click', sendLinkRequest);
    await loadGuardianLinks();
    await loadGuardianAlerts();
    await loadUnreadCount();
}

async function sendLinkRequest() {
    const email = document.getElementById('depEmail').value.trim();
    const msgBox = document.getElementById('requestMsg');
    if (!email) {
        showMsg(msgBox, 'Please enter a valid email address.', false);
        return;
    }
    const btn = document.getElementById('sendRequestBtn');
    btn.disabled = true;
    const data = await authenticatedFetch('/api/guardian/link/request', {
        method: 'POST',
        body: { dependentEmail: email }
    });
    btn.disabled = false;
    if (data && data.success) {
        showMsg(msgBox, 'Link request sent successfully!', true);
        document.getElementById('depEmail').value = '';
        await loadGuardianLinks();
    } else {
        showMsg(msgBox, (data && data.message) || 'Error sending request.', false);
    }
}

async function loadGuardianLinks() {
    const container = document.getElementById('linksContainer');
    const data = await authenticatedFetch('/api/guardian/links');
    if (data && data.success && data.links && data.links.length > 0) {
        let html = '<ul class="link-list">';
        data.links.forEach(link => {
            const name = link.dependent ? link.dependent.name : 'Unknown';
            html += `<li class="link-item">
                <div class="link-info">
                    <strong>${escHtml(name)}</strong>
                    <small>Status: <span class="badge badge-${link.status}">${link.status}</span></small>
                </div>
                <div class="link-actions">
                    <button class="btn btn-danger" onclick="removeLink(${link.linkId})">Remove</button>
                </div>
            </li>`;
        });
        html += '</ul>';
        container.innerHTML = html;
    } else {
        container.innerHTML = '<div class="empty-state">No family members linked yet.</div>';
    }
}

async function loadGuardianAlerts() {
    const container = document.getElementById('alertsContainer');
    const data = await authenticatedFetch('/api/guardian/alerts');
    if (data && data.success && data.alerts && data.alerts.length > 0) {
        let html = '<ul class="alert-list">';
        data.alerts.forEach(alert => {
            const depName = alert.dependent ? alert.dependent.name : 'Linked Member';
            const date = new Date(alert.createdAt).toLocaleString();
            const readStyle = alert.isRead ? 'color:#999;' : 'font-weight:bold;';
            html += `<li class="alert-item" style="${readStyle}">
                <div style="display:flex;align-items:center;">
                    <span class="alert-score">${alert.riskScore}</span>
                    <div class="alert-info">
                        <strong>${escHtml(alert.title)}</strong>
                        <small>${escHtml(depName)} · ${date}</small>
                        <small style="display:block;color:#555;margin-top:3px;">${escHtml(alert.message)}</small>
                    </div>
                </div>
                <div class="link-actions">
                    <span class="badge badge-high-risk">${escHtml(alert.verdict)}</span>
                    ${!alert.isRead ? `<button class="btn btn-secondary" onclick="markRead(${alert.alertId})">Mark Read</button>` : ''}
                </div>
            </li>`;
        });
        html += '</ul>';
        container.innerHTML = html;
    } else {
        container.innerHTML = '<div class="empty-state">No high-risk alerts. Linked members are safe.</div>';
    }
}

async function loadUnreadCount() {
    const data = await authenticatedFetch('/api/guardian/alerts/unread-count');
    const badge = document.getElementById('unreadBadge');
    if (data && data.success && data.unreadCount > 0) {
        badge.style.display = 'inline-block';
        badge.textContent = data.unreadCount + ' new';
    } else if (badge) {
        badge.style.display = 'none';
    }
}

async function markRead(alertId) {
    await authenticatedFetch(`/api/guardian/alerts/${alertId}/read`, { method: 'POST' });
    await loadGuardianAlerts();
    await loadUnreadCount();
}

// ─── DEPENDENT VIEW ─────────────────────────────────────────────────────────────
async function renderDependentView(container, user) {
    container.innerHTML = `
        <div class="section-card">
            <h3>Pending Guardian Requests</h3>
            <div id="requestsContainer"><div class="empty-state">Loading...</div></div>
        </div>

        <div class="section-card">
            <h3>Active Guardian</h3>
            <div id="activeGuardianContainer"><div class="empty-state">Loading...</div></div>
        </div>`;

    await loadPendingRequests();
    await loadActiveGuardian();
}

async function loadPendingRequests() {
    const container = document.getElementById('requestsContainer');
    const data = await authenticatedFetch('/api/guardian/requests');
    if (data && data.success && data.requests && data.requests.length > 0) {
        let html = '<ul class="link-list">';
        data.requests.forEach(link => {
            const g = link.guardian || {};
            html += `<li class="link-item">
                <div class="link-info">
                    <strong>${escHtml(g.name || 'Unknown')}</strong>
                    <small>${escHtml(g.email || '')}</small>
                </div>
                <div class="link-actions">
                    <button class="btn btn-success" onclick="respondRequest(${link.linkId}, 'accept')">Accept</button>
                    <button class="btn btn-danger" onclick="respondRequest(${link.linkId}, 'reject')">Reject</button>
                </div>
            </li>`;
        });
        html += '</ul>';
        container.innerHTML = html;
    } else {
        container.innerHTML = '<div class="empty-state">No pending requests.</div>';
    }
}

async function loadActiveGuardian() {
    const container = document.getElementById('activeGuardianContainer');
    const data = await authenticatedFetch('/api/guardian/links');
    if (data && data.success && data.links && data.links.length > 0) {
        let html = '<ul class="link-list">';
        data.links.forEach(link => {
            const g = link.guardian || {};
            html += `<li class="link-item">
                <div class="link-info">
                    <strong>${escHtml(g.name || 'Guardian')}</strong>
                    <small>Status: <span class="badge badge-active">active</span></small>
                </div>
                <div class="link-actions">
                    <button class="btn btn-danger" onclick="removeLink(${link.linkId})">Disconnect</button>
                </div>
            </li>`;
        });
        html += '</ul>';
        container.innerHTML = html;
    } else {
        container.innerHTML = '<div class="empty-state">No active guardian linked.</div>';
    }
}

async function respondRequest(linkId, action) {
    const data = await authenticatedFetch(`/api/guardian/link/${linkId}/${action}`, { method: 'POST' });
    if (data && data.success) {
        await loadPendingRequests();
        await loadActiveGuardian();
    }
}

async function removeLink(linkId) {
    if (!confirm('Are you sure you want to remove this family connection?')) return;
    const data = await authenticatedFetch(`/api/guardian/link/${linkId}`, { method: 'DELETE' });
    if (data && data.success) {
        // Re-render based on the current view
        const el = document.getElementById('linksContainer');
        if (el) await loadGuardianLinks();
        const el2 = document.getElementById('activeGuardianContainer');
        if (el2) await loadActiveGuardian();
    }
}

// ─── UTILS ─────────────────────────────────────────────────────────────────────
function showMsg(el, text, isSuccess) {
    el.textContent = text;
    el.className = 'msg-box ' + (isSuccess ? 'msg-success' : 'msg-error');
    el.style.display = 'block';
}

function escHtml(str) {
    if (!str) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;');
}

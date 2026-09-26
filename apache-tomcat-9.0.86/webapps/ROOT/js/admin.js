/* frontend/js/admin.js — SurakshaScan Admin Panel Logic */

let currentView = 'overview';
let patternPage = 1, communityPage = 1, usersPage = 1, scansPage = 1, auditPage = 1;

function initAdmin() {
    // Sidebar navigation
    document.querySelectorAll('.sidebar nav a[data-view]').forEach(link => {
        link.addEventListener('click', (e) => {
            e.preventDefault();
            switchView(link.dataset.view);
            document.getElementById('sidebar').classList.remove('open');
        });
    });

    // Mobile menu
    document.getElementById('menuToggle').addEventListener('click', () => {
        document.getElementById('sidebar').classList.toggle('open');
    });

    // Pattern buttons
    document.getElementById('btnCreatePattern').addEventListener('click', () => openPatternModal(null));
    document.getElementById('savePatternBtn').addEventListener('click', savePattern);

    // Community refresh
    document.getElementById('loadCommunityBtn').addEventListener('click', () => loadCommunity(1));

    // User search
    document.getElementById('searchUsersBtn').addEventListener('click', () => loadUsers(1));
    document.getElementById('userSearch').addEventListener('keydown', e => { if (e.key === 'Enter') loadUsers(1); });

    // Scan filter
    document.getElementById('filterScansBtn').addEventListener('click', () => loadScans(1));

    // Security refresh
    document.getElementById('refreshSecurityBtn').addEventListener('click', () => { loadHealth(); loadSecurityEvents(); });

    // Analytics Range
    document.getElementById('analyticsDateRange').addEventListener('change', () => loadAnalytics());

    // Load initial
    loadStats();
    loadRecentScans();
}

function switchView(view) {
    document.querySelectorAll('.view').forEach(v => v.classList.remove('active'));
    document.querySelectorAll('.sidebar nav a').forEach(a => a.classList.remove('active'));
    document.getElementById('view-' + view).classList.add('active');
    document.querySelector(`[data-view="${view}"]`).classList.add('active');
    document.getElementById('viewTitle').textContent = {
        overview: 'Overview', analytics: 'Threat Intelligence & Analytics', patterns: 'Scam Rules', community: 'Community Flags',
        users: 'Users', scans: 'Scan Activity', audit: 'Audit Log', security: 'Security & System'
    }[view] || view;
    currentView = view;
    
    const rangeSelect = document.getElementById('analyticsDateRange');
    rangeSelect.style.display = (view === 'analytics') ? 'block' : 'none';

    if (view === 'overview') { loadStats(); loadRecentScans(); }
    else if (view === 'analytics') loadAnalytics();
    else if (view === 'patterns') loadPatterns();
    else if (view === 'community') loadCommunity(1);
    else if (view === 'users') loadUsers(1);
    else if (view === 'scans') loadScans(1);
    else if (view === 'audit') loadAudit(1);
    else if (view === 'security') { loadHealth(); loadSecurityEvents(); }
}

// ── Stats ──────────────────────────────────────────────────────────────────────
async function loadStats() {
    const data = await authenticatedFetch('/api/admin/stats');
    if (data && data.success) {
        const s = data.stats;
        setText('s-users',   s.totalUsers);
        setText('s-scans',   s.totalScans);
        setText('s-highrisk',s.highRiskScans);
        setText('s-flags',   s.communityFlags);
        setText('s-links',   s.pendingFamilyLinks);
    }
}

async function loadRecentScans() {
    const data = await authenticatedFetch('/api/admin/scans?limit=8');
    const el = document.getElementById('overviewRecentScans');
    if (data && data.success && data.scans && data.scans.length > 0) {
        el.innerHTML = buildScansTable(data.scans, false);
    } else {
        el.innerHTML = '<div class="empty">No scans recorded yet.</div>';
    }
}

// ── Patterns ───────────────────────────────────────────────────────────────────
async function loadPatterns() {
    const data = await authenticatedFetch('/api/admin/patterns');
    const el = document.getElementById('patternsTable');
    if (data && data.success && data.patterns && data.patterns.length > 0) {
        let html = '<table><thead><tr><th>ID</th><th>Category</th><th>Type</th><th>Pattern</th><th>Weight</th><th>Status</th><th>Actions</th></tr></thead><tbody>';
        data.patterns.forEach(p => {
            const statusBadge = p.active
                ? '<span class="badge badge-on">Active</span>'
                : '<span class="badge badge-off">Disabled</span>';
            const toggleLabel = p.active ? 'Disable' : 'Enable';
            const toggleClass = p.active ? 'btn-warning' : 'btn-success';
            html += `<tr>
                <td>${p.patternId}</td>
                <td><span style="text-transform:uppercase;font-size:11px;font-weight:700;">${esc(p.category)}</span></td>
                <td>${esc(p.patternType)}</td>
                <td style="font-family:monospace;font-size:12px;max-width:200px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;" title="${esc(p.patternText)}">${esc(p.patternText)}</td>
                <td>${p.riskWeight}</td>
                <td>${statusBadge}</td>
                <td style="white-space:nowrap;">
                    <button class="btn btn-sm btn-secondary" onclick='openPatternModal(${JSON.stringify(p)})'>Edit</button>
                    <button class="btn btn-sm ${toggleClass}" onclick="togglePattern(${p.patternId}, ${!p.active})">${toggleLabel}</button>
                </td>
            </tr>`;
        });
        html += '</tbody></table>';
        el.innerHTML = html;
    } else {
        el.innerHTML = '<div class="empty">No patterns found.</div>';
    }
}

function openPatternModal(p) {
    document.getElementById('editPatternId').value = p ? p.patternId : '';
    document.getElementById('patternModalTitle').textContent = p ? 'Edit Rule' : 'New Rule';
    document.getElementById('fCategory').value = p ? p.category : 'sms';
    document.getElementById('fType').value = p ? p.patternType : 'keyword';
    document.getElementById('fText').value = p ? p.patternText : '';
    document.getElementById('fWeight').value = p ? p.riskWeight : 20;
    document.getElementById('fDesc').value = p ? p.description : '';
    document.getElementById('patternMsg').innerHTML = '';
    openModal('patternModal');
}

async function savePattern() {
    const id = document.getElementById('editPatternId').value;
    const body = {
        category:    document.getElementById('fCategory').value,
        patternType: document.getElementById('fType').value,
        patternText: document.getElementById('fText').value.trim(),
        riskWeight:  parseInt(document.getElementById('fWeight').value),
        description: document.getElementById('fDesc').value.trim(),
    };
    const url   = id ? `/api/admin/patterns/${id}` : '/api/admin/patterns';
    const method= id ? 'PUT' : 'POST';
    const data  = await authenticatedFetch(url, { method, body });
    const msgEl = document.getElementById('patternMsg');
    if (data && data.success) {
        msgEl.innerHTML = `<div class="msg msg-ok">${esc(data.message)}</div>`;
        setTimeout(() => { closeModal('patternModal'); loadPatterns(); }, 800);
    } else {
        msgEl.innerHTML = `<div class="msg msg-err">${esc((data && data.message) || 'Error saving')}</div>`;
    }
}

async function togglePattern(id, active) {
    const label = active ? 'enable' : 'disable';
    if (!confirm(`Are you sure you want to ${label} this rule?`)) return;
    const data = await authenticatedFetch(`/api/admin/patterns/${id}/status`, { method: 'PATCH', body: { active } });
    if (data && data.success) loadPatterns();
    else alert((data && data.message) || 'Error');
}

// ── Community ──────────────────────────────────────────────────────────────────
async function loadCommunity(page) {
    communityPage = page;
    const data = await authenticatedFetch(`/api/admin/community?page=${page}&limit=20`);
    const el = document.getElementById('communityTable');
    if (data && data.success && data.items && data.items.length > 0) {
        let html = '<table><thead><tr><th>ID</th><th>Flagged Value</th><th>Reports</th><th>First Reported</th><th>Last Reported</th><th>Status</th><th>Actions</th></tr></thead><tbody>';
        data.items.forEach(f => {
            const sBadge = (f.status === 'removed')
                ? '<span class="badge badge-removed">Removed</span>'
                : '<span class="badge badge-active">Active</span>';
            const actionBtn = (f.status === 'removed')
                ? `<button class="btn btn-sm btn-success" onclick="setFlagStatus(${f.flagId},'active')">Restore</button>`
                : `<button class="btn btn-sm btn-danger" onclick="setFlagStatus(${f.flagId},'removed')">Remove</button>`;
            html += `<tr>
                <td>${f.flagId}</td>
                <td style="font-family:monospace;font-size:12px;">${esc(f.flaggedValue)}</td>
                <td>${f.reportCount}</td>
                <td>${fmtDate(f.firstReported)}</td>
                <td>${fmtDate(f.lastReported)}</td>
                <td>${sBadge}</td>
                <td>${actionBtn}</td>
            </tr>`;
        });
        html += '</tbody></table>';
        el.innerHTML = html;
        renderPager('communityPager', data.pagination, loadCommunity);
    } else {
        el.innerHTML = '<div class="empty">No community flags found.</div>';
        document.getElementById('communityPager').innerHTML = '';
    }
}

async function setFlagStatus(id, status) {
    const label = status === 'removed' ? 'remove' : 'restore';
    if (!confirm(`Are you sure you want to ${label} this flag? Historical reports are preserved.`)) return;
    const data = await authenticatedFetch(`/api/admin/community/${id}/status`, { method: 'PATCH', body: { status } });
    if (data && data.success) loadCommunity(communityPage);
    else alert((data && data.message) || 'Error');
}

// ── Users ──────────────────────────────────────────────────────────────────────
async function loadUsers(page) {
    usersPage = page;
    const search = document.getElementById('userSearch').value.trim();
    const role   = document.getElementById('userRoleFilter').value;
    const params = new URLSearchParams({ page, limit: 20 });
    if (search) params.append('search', search);
    if (role && role !== 'all') params.append('role', role);
    const data = await authenticatedFetch('/api/admin/users?' + params.toString());
    const el = document.getElementById('usersTable');
    if (data && data.success && data.users && data.users.length > 0) {
        let html = '<table><thead><tr><th>ID</th><th>Name</th><th>Email</th><th>Role</th><th>Joined</th><th>Actions</th></tr></thead><tbody>';
        data.users.forEach(u => {
            const roleOpts = u.role === 'admin' ? `<span style="color:#888;font-size:12px;">Admin (no change)</span>` :
                `<select class="role-sel" onchange="changeRole(${u.userId}, this.value)">
                    <option value="normal" ${u.role==='normal'?'selected':''}>Normal</option>
                    <option value="guardian" ${u.role==='guardian'?'selected':''}>Guardian</option>
                </select>`;
            html += `<tr>
                <td>${u.userId}</td>
                <td>${esc(u.name)}</td>
                <td style="font-size:12px;">${esc(u.email)}</td>
                <td><span style="text-transform:capitalize;">${esc(u.role)}</span></td>
                <td>${fmtDate(u.createdAt)}</td>
                <td>${roleOpts}</td>
            </tr>`;
        });
        html += '</tbody></table>';
        el.innerHTML = html;
        renderPager('usersPager', data.pagination, loadUsers);
    } else {
        el.innerHTML = '<div class="empty">No users found.</div>';
        document.getElementById('usersPager').innerHTML = '';
    }
}

async function changeRole(userId, newRole) {
    if (!confirm(`Change this user's role to "${newRole}"?`)) { loadUsers(usersPage); return; }
    const data = await authenticatedFetch(`/api/admin/users/${userId}/role`, { method: 'PATCH', body: { role: newRole } });
    if (!data || !data.success) { alert((data && data.message) || 'Error changing role'); loadUsers(usersPage); }
}

// ── Scans ──────────────────────────────────────────────────────────────────────
async function loadScans(page) {
    scansPage = page;
    const verdict   = document.getElementById('scanVerdictFilter').value;
    const inputType = document.getElementById('scanTypeFilter').value;
    const params = new URLSearchParams({ page, limit: 20 });
    if (verdict && verdict !== 'all') params.append('verdict', verdict);
    if (inputType && inputType !== 'all') params.append('inputType', inputType);
    const data = await authenticatedFetch('/api/admin/scans?' + params.toString());
    const el = document.getElementById('scansTable');
    if (data && data.success && data.scans && data.scans.length > 0) {
        el.innerHTML = buildScansTable(data.scans, true);
        renderPager('scansPager', data.pagination, loadScans);
    } else {
        el.innerHTML = '<div class="empty">No scans match your filters.</div>';
        document.getElementById('scansPager').innerHTML = '';
    }
}

function buildScansTable(scans, showDetail) {
    let html = `<table><thead><tr><th>ID</th><th>User</th><th>Type</th><th>Verdict</th><th>Score</th><th>Date</th>${showDetail ? '<th>Detail</th>' : ''}</tr></thead><tbody>`;
    scans.forEach(r => {
        const v = r.verdict || '';
        const cls = v === 'SAFE' ? 'badge-safe' : (v.includes('HIGH') ? 'badge-risk' : 'badge-suspicious');
        html += `<tr>
            <td>${r.reportId}</td>
            <td>${esc(r.userName || 'Unknown')}</td>
            <td style="text-transform:uppercase;font-size:11px;">${esc(r.inputType)}</td>
            <td><span class="badge ${cls}">${esc(v)}</span></td>
            <td>${r.riskScore}</td>
            <td>${fmtDate(r.createdAt)}</td>
            ${showDetail ? `<td><button class="btn btn-sm btn-secondary" onclick="viewScanDetail(${r.reportId})">View</button></td>` : ''}
        </tr>`;
    });
    html += '</tbody></table>';
    return html;
}

async function viewScanDetail(id) {
    openModal('scanDetailModal');
    document.getElementById('scanDetailBody').innerHTML = 'Loading…';
    const data = await authenticatedFetch(`/api/admin/scans/${id}`);
    if (data && data.success && data.scan) {
        const s = data.scan;
        document.getElementById('scanDetailBody').innerHTML = `
            <div style="display:flex;justify-content:space-between;margin-bottom:16px;">
                <div><div style="font-size:11px;color:#888;font-weight:700;">VERDICT</div><span style="font-size:18px;font-weight:700;">${esc(s.verdict)}</span></div>
                <div style="text-align:right;"><div style="font-size:11px;color:#888;font-weight:700;">RISK SCORE</div><span style="font-size:24px;font-weight:700;color:#001f4d;">${s.riskScore}</span></div>
            </div>
            <div style="margin-bottom:10px;"><strong>User:</strong> ${esc(s.userName || 'Unknown')}</div>
            <div style="margin-bottom:10px;"><strong>Type:</strong> ${esc(s.inputType)}</div>
            <div style="margin-bottom:10px;"><strong>Date:</strong> ${fmtDate(s.createdAt)}</div>
            <div style="margin-bottom:10px;"><strong>Raw Input:</strong><div style="background:#f8f9fb;padding:10px;border-radius:4px;font-family:monospace;font-size:12px;margin-top:4px;white-space:pre-wrap;word-break:break-all;">${esc(s.rawInput)}</div></div>
        `;
    } else {
        document.getElementById('scanDetailBody').innerHTML = '<div style="color:red;">Scan not found or access denied.</div>';
    }
}

// ── Audit ──────────────────────────────────────────────────────────────────────
async function loadAudit(page) {
    auditPage = page;
    const data = await authenticatedFetch(`/api/admin/audit-logs?page=${page}&limit=20`);
    const el = document.getElementById('auditTable');
    if (data && data.success && data.logs && data.logs.length > 0) {
        let html = '<table><thead><tr><th>ID</th><th>Admin</th><th>Action</th><th>Entity</th><th>Details</th><th>Date</th></tr></thead><tbody>';
        data.logs.forEach(l => {
            html += `<tr>
                <td>${l.logId}</td>
                <td>${esc(l.adminName || 'Admin')}</td>
                <td style="font-family:monospace;font-size:12px;font-weight:700;">${esc(l.action)}</td>
                <td>${esc(l.entityType || '')} #${l.entityId}</td>
                <td style="font-size:12px;color:#666;">${esc(l.details || '–')}</td>
                <td>${fmtDate(l.createdAt)}</td>
            </tr>`;
        });
        html += '</tbody></table>';
        el.innerHTML = html;
        renderPager('auditPager', data.pagination, loadAudit);
    } else {
        el.innerHTML = '<div class="empty">No audit entries yet.</div>';
        document.getElementById('auditPager').innerHTML = '';
    }
}

// ── Security ───────────────────────────────────────────────────────────────────
async function loadHealth() {
    const data = await authenticatedFetch('/api/health');
    const appEl = document.getElementById('s-health-app');
    const dbEl = document.getElementById('s-health-db');
    if (data && data.success && data.status) {
        appEl.textContent = 'Operational';
        appEl.style.color = '#155724';
        if (data.status === 'UP') {
            dbEl.textContent = 'Operational';
            dbEl.style.color = '#155724';
        } else {
            dbEl.textContent = 'Degraded';
            dbEl.style.color = '#856404';
        }
    } else {
        appEl.textContent = 'Unavailable';
        appEl.style.color = '#721c24';
        dbEl.textContent = 'Unavailable';
        dbEl.style.color = '#721c24';
    }
}

async function loadSecurityEvents() {
    const data = await authenticatedFetch(`/api/admin/security-events`);
    const el = document.getElementById('securityEventsTable');
    if (data && data.success && data.events && data.events.length > 0) {
        let html = '<table><thead><tr><th>ID</th><th>User</th><th>Event</th><th>Endpoint</th><th>Result</th><th>Time</th></tr></thead><tbody>';
        data.events.forEach(l => {
            const resultBadge = l.success ? '<span class="badge badge-safe">Allowed</span>' : '<span class="badge badge-risk">Blocked</span>';
            html += `<tr>
                <td>${l.eventId}</td>
                <td>${l.userId || 'Guest'}</td>
                <td style="font-family:monospace;font-size:12px;font-weight:700;">${esc(l.eventType)}</td>
                <td>${esc(l.endpoint)}</td>
                <td>${resultBadge}</td>
                <td>${fmtDate(l.createdAt)}</td>
            </tr>`;
        });
        html += '</tbody></table>';
        el.innerHTML = html;
    } else {
        el.innerHTML = '<div class="empty">No security events recorded.</div>';
    }
}

// ── Analytics ──────────────────────────────────────────────────────────────────
let chartInstances = {};

async function loadAnalytics() {
    const period = document.getElementById('analyticsDateRange').value;
    const data = await authenticatedFetch(`/api/admin/analytics?period=${period}`);
    if (!data || !data.success) {
        alert('Unable to load analytics. Please try again.');
        return;
    }

    // KPIs
    const o = data.overview;
    const c = data.community;
    const s = data.security;
    document.getElementById('analyticsKpiGrid').innerHTML = `
        <div class="stat-card"><h4>Total Scans</h4><div class="num">${o.totalScans}</div></div>
        <div class="stat-card" style="border-left-color:#dc3545;"><h4>High Risk</h4><div class="num">${o.highRisk}</div></div>
        <div class="stat-card" style="border-left-color:#ffc107;"><h4>Suspicious</h4><div class="num">${o.suspicious}</div></div>
        <div class="stat-card" style="border-left-color:#17a2b8;"><h4>Community Flags</h4><div class="num">${c.activeFlags}</div></div>
        <div class="stat-card" style="border-left-color:#28a745;"><h4>Active Rules</h4><div class="num">${o.activeRules}</div></div>
        <div class="stat-card" style="border-left-color:#343a40;"><h4>Security Events</h4><div class="num">${s.loginFailures + s.unauthorized + s.forbidden + s.rateLimit}</div></div>
    `;

    // Charts
    renderLineChart('chartScanTrend', 'Scans', data.scanTrend);
    renderLineChart('chartHighRisk', 'High Risk Scans', data.highRiskTrend, '#dc3545');
    renderLineChart('chartCommunity', 'Community Reports', data.communityTrend, '#17a2b8');
    
    renderDonutChart('chartVerdict', data.verdictDistribution, {
        'SAFE': '#28a745', 'SUSPICIOUS': '#ffc107', 'HIGH RISK': '#dc3545'
    });
    renderDonutChart('chartTypes', data.scanTypes, {
        'sms': '#007bff', 'upi': '#6f42c1', 'link': '#17a2b8', 'call': '#fd7e14'
    });

    // Tables
    renderSimpleTable('tableScamCategories', data.scamCategories, ['category', 'count'], ['Category', 'Configured Count']);
    renderSimpleTable('tableTopRules', data.configuredRules, ['rule', 'category', 'type', 'weight', 'status'], ['Rule', 'Category', 'Type', 'Weight', 'Status']);
    renderSimpleTable('tableTopFlags', data.topFlags, ['value', 'type', 'reports', 'status'], ['Indicator', 'Type', 'Reports', 'Status']);
    
    // Security & Admin Activity Tables
    renderSimpleTable('tableSecurityActivity', [
        {metric: 'Login Failures', val: s.loginFailures}, {metric: 'Unauthorized', val: s.unauthorized},
        {metric: 'Forbidden', val: s.forbidden}, {metric: 'Rate Limit Triggers', val: s.rateLimit}
    ], ['metric', 'val'], ['Event Type', 'Count']);
    
    renderSimpleTable('tableAdminSummary', data.adminActivity, ['action', 'details', 'date'], ['Action', 'Details', 'Time'], l => {
        l.date = fmtDate(l.date);
        return l;
    });
}

function renderLineChart(canvasId, label, trendData, color = '#0056b3') {
    const ctx = document.getElementById(canvasId).getContext('2d');
    if (chartInstances[canvasId]) chartInstances[canvasId].destroy();
    if (!trendData || trendData.length === 0) {
        document.getElementById(canvasId).parentNode.innerHTML = '<div class="empty">No data available for this period.</div>';
        return;
    }
    const labels = trendData.map(d => d.date);
    const counts = trendData.map(d => d.count);
    chartInstances[canvasId] = new Chart(ctx, {
        type: 'line',
        data: { labels, datasets: [{ label, data: counts, borderColor: color, tension: 0.1, fill: false }] },
        options: { responsive: true, maintainAspectRatio: false, scales: { y: { beginAtZero: true, ticks: { precision: 0 } } } }
    });
}

function renderDonutChart(canvasId, distData, colorsMap) {
    const ctx = document.getElementById(canvasId).getContext('2d');
    if (chartInstances[canvasId]) chartInstances[canvasId].destroy();
    if (!distData || distData.length === 0) {
        document.getElementById(canvasId).parentNode.innerHTML = '<div class="empty">No data available for this period.</div>';
        return;
    }
    const labels = distData.map(d => d.category);
    const counts = distData.map(d => d.count);
    const bgColors = labels.map(l => colorsMap[l] || '#cccccc');
    chartInstances[canvasId] = new Chart(ctx, {
        type: 'doughnut',
        data: { labels, datasets: [{ data: counts, backgroundColor: bgColors }] },
        options: { responsive: true, maintainAspectRatio: false }
    });
}

function renderSimpleTable(containerId, dataArray, cols, headers, mapper = null) {
    const el = document.getElementById(containerId);
    if (!dataArray || dataArray.length === 0) {
        el.innerHTML = '<div class="empty">No data available.</div>';
        return;
    }
    let html = '<table><thead><tr>' + headers.map(h => `<th>${h}</th>`).join('') + '</tr></thead><tbody>';
    dataArray.forEach(row => {
        if (mapper) row = mapper(row);
        html += '<tr>' + cols.map(c => `<td>${esc(row[c])}</td>`).join('') + '</tr>';
    });
    html += '</tbody></table>';
    el.innerHTML = html;
}

// ── Helpers ────────────────────────────────────────────────────────────────────
function openModal(id) { document.getElementById(id).classList.add('open'); }
function closeModal(id) { document.getElementById(id).classList.remove('open'); }

function renderPager(containerId, pag, loadFn) {
    if (!pag) return;
    const el = document.getElementById(containerId);
    el.innerHTML = `
        <button class="btn btn-sm btn-secondary" onclick="(${loadFn.name})(${pag.page - 1})" ${pag.page <= 1 ? 'disabled' : ''}>← Prev</button>
        <span>Page ${pag.page} of ${pag.totalPages} &nbsp;(${pag.totalRecords} records)</span>
        <button class="btn btn-sm btn-secondary" onclick="(${loadFn.name})(${pag.page + 1})" ${pag.page >= pag.totalPages ? 'disabled' : ''}>Next →</button>
    `;
}

function setText(id, val) {
    const el = document.getElementById(id);
    if (el) el.textContent = val !== undefined && val !== null ? val : '–';
}

function fmtDate(ts) {
    if (!ts) return '–';
    try { return new Date(ts).toLocaleString(); } catch(e) { return ts; }
}

function esc(str) {
    if (str === null || str === undefined) return '';
    return String(str)
        .replace(/&/g,'&amp;').replace(/</g,'&lt;')
        .replace(/>/g,'&gt;').replace(/"/g,'&quot;');
}

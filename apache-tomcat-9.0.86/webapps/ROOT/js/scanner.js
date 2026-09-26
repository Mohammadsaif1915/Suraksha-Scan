/* frontend/js/scanner.js — SurakshaScan Scanner Logic */
let currentType = 'sms';

document.addEventListener('DOMContentLoaded', () => {
    const tabs = document.querySelectorAll('.tab');
    const inputSms = document.getElementById('scanContentSms');
    const inputUpi = document.getElementById('scanContentUpi');
    const inputLink = document.getElementById('scanContentLink');
    const inputTitle = document.getElementById('inputTitle');
    const scanBtn = document.getElementById('scanBtn');
    const errorMsg = document.getElementById('errorMsg');
    const resultCard = document.getElementById('resultCard');
    const scanBody = document.getElementById('scanBody');

    // Auth check — reuse api.js checkAuthAndInit
    checkAuthAndInit(() => {
        scanBody.style.display = 'block';
    });

    tabs.forEach(tab => {
        tab.addEventListener('click', () => {
            tabs.forEach(t => t.classList.remove('active'));
            tab.classList.add('active');
            currentType = tab.getAttribute('data-type');

            inputSms.style.display = 'none';
            inputUpi.style.display = 'none';
            inputLink.style.display = 'none';
            resultCard.style.display = 'none';
            errorMsg.style.display = 'none';

            if (currentType === 'sms') {
                inputSms.style.display = 'block';
                inputTitle.textContent = 'Scan SMS Text';
            } else if (currentType === 'upi') {
                inputUpi.style.display = 'block';
                inputTitle.textContent = 'Scan UPI ID';
            } else if (currentType === 'link') {
                inputLink.style.display = 'block';
                inputTitle.textContent = 'Scan URL / Link';
            }
        });
    });

    scanBtn.addEventListener('click', async () => {
        let content = '';
        if (currentType === 'sms') content = inputSms.value;
        else if (currentType === 'upi') content = inputUpi.value;
        else if (currentType === 'link') content = inputLink.value;

        if (!content || !content.trim()) {
            showError('Please provide input to scan.');
            return;
        }

        if (content.length > 1000) {
            showError('Input exceeds the maximum allowed length (1000 characters).');
            return;
        }

        hideError();
        resultCard.style.display = 'none';
        scanBtn.disabled = true;
        scanBtn.textContent = 'Analyzing...';

        try {
            const data = await authenticatedFetch('/api/scan', {
                method: 'POST',
                body: { inputType: currentType, content: content.trim() }
            });

            if (data && data.success) {
                displayResult(data.scan);
            } else {
                showError((data && data.message) || 'An error occurred during scanning.');
            }
        } catch (err) {
            showError('Network error. Please check your connection and try again.');
        } finally {
            scanBtn.disabled = false;
            scanBtn.textContent = 'Analyze Now';
        }
    });

    function showError(msg) {
        errorMsg.textContent = msg;
        errorMsg.style.display = 'block';
    }

    function hideError() {
        errorMsg.style.display = 'none';
    }

    function escHtml(str) {
        if (!str) return '';
        return String(str)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;');
    }

    function displayResult(scanData) {
        resultCard.className = 'result-card';
        const verdict = (scanData.verdict || '').toUpperCase().replace('_', ' ');

        if (verdict === 'SAFE') resultCard.classList.add('safe');
        else if (verdict === 'SUSPICIOUS') resultCard.classList.add('suspicious');
        else if (verdict === 'HIGH RISK') resultCard.classList.add('high-risk');

        document.getElementById('resultVerdict').textContent = verdict;
        document.getElementById('riskScore').textContent = scanData.riskScore;
        document.getElementById('resultSummary').textContent = scanData.summary || '';

        const rulesList = document.getElementById('rulesList');
        rulesList.innerHTML = '';

        if (scanData.matchedRules && scanData.matchedRules.length > 0) {
            scanData.matchedRules.forEach(rule => {
                const div = document.createElement('div');
                div.className = 'rule-item';
                const strong = document.createElement('strong');
                strong.textContent = 'Rule: ';
                div.appendChild(strong);
                div.appendChild(document.createTextNode(rule.description));
                div.appendChild(document.createElement('br'));
                const meta = document.createElement('span');
                meta.style.cssText = 'color:#666;font-size:12px;';
                meta.textContent = `Category: ${rule.category} | Risk Contribution: ${rule.scoreContribution}`;
                div.appendChild(meta);
                rulesList.appendChild(div);
            });
        } else {
            const div = document.createElement('div');
            div.className = 'rule-item';
            div.style.borderLeftColor = '#28a745';
            div.innerHTML = '<em>No suspicious indicators matched.</em>';
            rulesList.appendChild(div);
        }

        // Community Report + Awareness links (only for non-SAFE)
        let reportDiv = document.getElementById('reportActionDiv');
        if (!reportDiv) {
            reportDiv = document.createElement('div');
            reportDiv.id = 'reportActionDiv';
            resultCard.appendChild(reportDiv);
        }
        reportDiv.innerHTML = '';

        if (verdict !== 'SAFE') {
            const encodedValue = encodeURIComponent(
                currentType === 'sms' ? inputSms.value :
                currentType === 'upi' ? inputUpi.value : inputLink.value
            );

            const container = document.createElement('div');
            container.style.cssText = 'margin-top:20px;padding-top:15px;border-top:1px solid #eee;';

            const awarenessLink = document.createElement('a');
            awarenessLink.href = `awareness.html?category=${encodeURIComponent(currentType)}`;
            awarenessLink.textContent = 'Learn why this was flagged →';
            awarenessLink.style.cssText = 'color:#0056b3;font-weight:bold;text-decoration:none;font-size:14px;display:block;margin-bottom:10px;';

            const reportLink = document.createElement('a');
            reportLink.href = `community.html?value=${encodedValue}&type=${encodeURIComponent(currentType)}`;
            reportLink.textContent = `Report this ${currentType.toUpperCase()}`;
            reportLink.style.cssText = 'display:inline-block;background:#6c757d;color:#fff;padding:8px 16px;text-decoration:none;border-radius:4px;font-weight:bold;font-size:14px;';

            container.appendChild(awarenessLink);
            container.appendChild(reportLink);
            reportDiv.appendChild(container);
        }

        resultCard.style.display = 'block';
    }
});

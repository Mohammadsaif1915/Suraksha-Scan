const API_BASE = '/surakshascan-backend';
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
    
    // Auth check on load
    fetch(${API_BASE}/api/auth/me, { credentials: 'include' })
        .then(res => { 
            if(!res.ok) window.location.href = 'login.html'; 
            else scanBody.style.display = 'block';
        })
        .catch(() => window.location.href = 'login.html');

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
                inputTitle.innerText = 'Scan SMS Text';
            } else if (currentType === 'upi') {
                inputUpi.style.display = 'block';
                inputTitle.innerText = 'Scan UPI ID';
            } else if (currentType === 'link') {
                inputLink.style.display = 'block';
                inputTitle.innerText = 'Scan URL / Link';
            }
        });
    });

    scanBtn.addEventListener('click', async () => {
        let content = '';
        if (currentType === 'sms') content = inputSms.value;
        else if (currentType === 'upi') content = inputUpi.value;
        else if (currentType === 'link') content = inputLink.value;

        if (!content || !content.trim()) {
            errorMsg.innerText = 'Please provide input to scan.';
            errorMsg.style.display = 'block';
            return;
        }
        
        if (content.length > 1000) {
            errorMsg.innerText = 'Input exceeds the maximum allowed length (1000 characters).';
            errorMsg.style.display = 'block';
            return;
        }

        errorMsg.style.display = 'none';
        resultCard.style.display = 'none';
        scanBtn.disabled = true;
        scanBtn.innerText = 'Analyzing...';

        try {
            const response = await fetch(${API_BASE}/api/scan, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'include',
                body: JSON.stringify({ inputType: currentType, content: content })
            });

            if (response.status === 401) {
                window.location.href = 'login.html';
                return;
            }

            const data = await response.json();
            
            if (response.ok && data.success) {
                displayResult(data.scan);
            } else {
                errorMsg.innerText = data.message || 'Error occurred during scan.';
                errorMsg.style.display = 'block';
            }
        } catch (err) {
            errorMsg.innerText = 'Network error. Please try again later.';
            errorMsg.style.display = 'block';
        } finally {
            scanBtn.disabled = false;
            scanBtn.innerText = 'Analyze Now';
        }
    });

    function displayResult(scanData) {
        resultCard.className = 'result-card'; // reset classes
        if (scanData.verdict === 'SAFE') resultCard.classList.add('safe');
        else if (scanData.verdict === 'SUSPICIOUS') resultCard.classList.add('suspicious');
        else if (scanData.verdict === 'HIGH RISK' || scanData.verdict === 'HIGH_RISK') {
            resultCard.classList.add('high-risk');
            scanData.verdict = 'HIGH RISK';
        }

        document.getElementById('resultVerdict').innerText = scanData.verdict;
        document.getElementById('riskScore').innerText = scanData.riskScore;
        document.getElementById('resultSummary').innerText = scanData.summary;

        const rulesList = document.getElementById('rulesList');
        rulesList.innerHTML = '';

        if (scanData.matchedRules && scanData.matchedRules.length > 0) {
            scanData.matchedRules.forEach(rule => {
                const div = document.createElement('div');
                div.className = 'rule-item';
                div.innerHTML = <strong>Rule:</strong>  + rule.description + <br>
                                 <span style="color:#666; font-size:12px;">Category:  + rule.category +  | Contribution: + + rule.scoreContribution +  Risk</span>;
                rulesList.appendChild(div);
            });
        } else {
            const div = document.createElement('div');
            div.className = 'rule-item';
            div.style.borderLeftColor = '#28a745';
            div.innerHTML = '<em>No suspicious indicators matched.</em>';
            rulesList.appendChild(div);
        }
        
                // Add Community Report Option
                let reportHtml = '';
        let awarenessHtml = '';
        if (scanData.verdict !== 'SAFE') {
            reportHtml = \
                <div style="margin-top: 20px; padding-top: 15px; border-top: 1px solid #eee;">
                    <a href="community.html?value=\&type=\" style="display:inline-block; background:#6c757d; color:#fff; padding:8px 16px; text-decoration:none; border-radius:4px; font-weight:bold; font-size:14px;">Report this \</a>
                </div>
            \;
        }
        
        let reportDiv = document.getElementById('reportActionDiv');
        if (!reportDiv) {
            reportDiv = document.createElement('div');
            reportDiv.id = 'reportActionDiv';
            resultCard.appendChild(reportDiv);
        }
                if (scanData.verdict !== 'SAFE') {
            awarenessHtml = \
                <div style="margin-top: 10px; font-size:14px;">
                    <a href="awareness.html?category=\" style="color:#0056b3; font-weight:bold; text-decoration:none;">Learn why this was flagged &rarr;</a>
                </div>
            \;
        }
        reportDiv.innerHTML = awarenessHtml + reportHtml;

        resultCard.style.display = 'block';
    }
});



$base = "c:\Users\Administrator\Desktop\SurakshaScan"

# 1. Update navigation in all HTML files
function Update-Nav($filePath) {
    if (Test-Path $filePath) {
        $content = Get-Content $filePath -Raw
        # Replace the navigation block
        $oldNav = '<nav style="margin-left: 30px;">'
        # Since each file might have different active underlines, we will inject the Awareness link if not present
        if ($content -notmatch 'awareness.html') {
            $content = $content -replace '<a href="community.html"', '<a href="awareness.html">Awareness</a><a href="community.html"'
            $content = $content -replace '<a href="history.html"', '<a href="history.html">History</a><a href="awareness.html"'
            # Fix if the first replace missed because of attributes
            $content = $content -replace '<a href="community.html"(.*?)>Community</a>', '<a href="awareness.html">Awareness</a><a href="community.html"$1>Community</a>'
            $content = $content -replace '<a href="history.html"(.*?)>History</a>', '<a href="history.html"$1>History</a><a href="awareness.html">Awareness</a>'
            
            # A cleaner approach is to just regex replace the entire nav block, but preserving the active state is tricky.
            # Let's just do a manual string replace for each specific file's nav if possible, or standard replacement.
        }
        Set-Content -Path $filePath -Value $content -Encoding UTF8
    }
}

# Actually, it's safer to just replace the nav content generically and set the underline via JS, but we'll stick to string replacement for each known file.
$files = @("dashboard.html", "scan.html", "history.html", "community.html")
foreach ($f in $files) {
    $path = "$base\frontend\$f"
    if (Test-Path $path) {
        $c = Get-Content $path -Raw
        if ($c -notmatch 'awareness.html') {
            $c = $c -replace '<a href="community.html"', '<a href="awareness.html">Awareness</a>`n                <a href="community.html"'
            Set-Content -Path $path -Value $c -Encoding UTF8
        }
    }
}

# Ensure awareness.html is active in its own file (will generate below)

# 2. Update dashboard.html to add the card
$dashHtmlPath = "$base\frontend\dashboard.html"
$dashHtmlContent = Get-Content $dashHtmlPath -Raw
if ($dashHtmlContent -notmatch "Learn to Spot Scams") {
    $cardHtml = @"
        <div class="scan-action" style="background: #e9ecef; color: #333; margin-top: 30px;">
            <h3 style="margin-top: 0;">Learn to Spot Scams</h3>
            <p style="color: #555;">Understand common scam tactics and how to respond safely.</p>
            <a href="awareness.html" style="background: #002b5e; color: #fff;">Visit Awareness Center</a>
        </div>
"@
    # Insert before <div class="stats-grid">
    $dashHtmlContent = $dashHtmlContent -replace '<div class="stats-grid">', "$cardHtml`n        <div class=`"stats-grid`">"
    Set-Content -Path $dashHtmlPath -Value $dashHtmlContent -Encoding UTF8
}

# 3. Create awareness.html
$awareHtmlPath = "$base\frontend\awareness.html"
$awareHtmlContent = @"
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Awareness Center - SurakshaScan</title>
    <style>
        body { font-family: Arial, sans-serif; background-color: #f4f7f6; margin: 0; display: none; }
        header { background: #002b5e; color: #fff; padding: 15px 20px; display: flex; justify-content: space-between; align-items: center; }
        header h1 { margin: 0; font-size: 22px; }
        nav a { color: #fff; margin-right: 15px; text-decoration: none; font-size: 15px; }
        nav a:hover { text-decoration: underline; }
        .user-area { display: flex; align-items: center; }
        .logout-btn { background: #dc3545; color: white; border: none; padding: 6px 12px; border-radius: 4px; cursor: pointer; }
        
        main { padding: 20px; max-width: 1000px; margin: 0 auto; }
        
        .hero { background: #fff; padding: 30px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); margin-bottom: 20px; }
        .hero h2 { margin-top: 0; color: #002b5e; }
        .hero p { color: #555; line-height: 1.6; font-size: 16px; }
        
        .filters { display: flex; gap: 10px; margin-bottom: 20px; flex-wrap: wrap; }
        .filter-btn { padding: 8px 16px; border: 1px solid #0056b3; background: #fff; color: #0056b3; border-radius: 20px; cursor: pointer; font-weight: bold; }
        .filter-btn.active { background: #0056b3; color: #fff; }
        
        .grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap: 20px; margin-bottom: 30px; }
        .card { background: #fff; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); display: flex; flex-direction: column; }
        .card h3 { margin-top: 0; color: #333; border-bottom: 2px solid #eee; padding-bottom: 10px; }
        .card p { color: #666; font-size: 14px; line-height: 1.5; flex-grow: 1; }
        .example-box { background: #f8f9fa; border-left: 4px solid #dc3545; padding: 12px; margin: 15px 0; border-radius: 0 4px 4px 0; }
        .example-label { font-size: 11px; font-weight: bold; color: #dc3545; text-transform: uppercase; margin-bottom: 5px; }
        .example-text { font-family: monospace; font-size: 13px; color: #333; white-space: pre-wrap; word-wrap: break-word; }
        .try-btn { display: inline-block; background: #e9ecef; color: #333; padding: 8px 12px; text-decoration: none; border-radius: 4px; font-weight: bold; font-size: 13px; text-align: center; cursor: pointer; border: 1px solid #ccc; }
        .try-btn:hover { background: #dde0e3; }
        
        .info-section { background: #fff; padding: 25px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); margin-bottom: 30px; }
        .info-section h3 { margin-top: 0; color: #002b5e; }
        .info-list { list-style-type: none; padding: 0; }
        .info-list li { padding: 10px 0; border-bottom: 1px solid #eee; color: #444; display: flex; align-items: center; }
        .info-list li:before { content: '⚠️'; margin-right: 10px; }
        
        .sequence-list { list-style-type: decimal; padding-left: 20px; color: #444; line-height: 1.8; }
        .sequence-list li { margin-bottom: 10px; }
        
        .quiz-section { background: #002b5e; color: #fff; padding: 30px; border-radius: 8px; box-shadow: 0 4px 8px rgba(0,0,0,0.2); }
        .quiz-section h3 { margin-top: 0; }
        .quiz-question { font-size: 18px; margin-bottom: 20px; font-weight: bold; }
        .quiz-options { display: flex; flex-direction: column; gap: 10px; }
        .quiz-option { background: rgba(255,255,255,0.1); border: 1px solid rgba(255,255,255,0.3); color: #fff; padding: 12px; border-radius: 4px; cursor: pointer; text-align: left; font-size: 15px; transition: background 0.2s; }
        .quiz-option:hover { background: rgba(255,255,255,0.2); }
        .quiz-feedback { margin-top: 20px; padding: 15px; border-radius: 4px; display: none; font-weight: bold; }
        .feedback-correct { background: #28a745; color: #fff; }
        .feedback-incorrect { background: #dc3545; color: #fff; }
        .quiz-explanation { margin-top: 10px; font-weight: normal; font-size: 14px; }
        .quiz-next { margin-top: 20px; background: #fff; color: #002b5e; border: none; padding: 10px 20px; border-radius: 4px; font-weight: bold; cursor: pointer; display: none; }
        
        .community-cta { text-align: center; margin-top: 40px; padding: 20px; border-top: 1px solid #ccc; }
        
        @media (max-width: 600px) {
            .grid { grid-template-columns: 1fr; }
        }
    </style>
</head>
<body id="pageBody">
    <header>
        <div style="display:flex; align-items: center;">
            <h1>SurakshaScan</h1>
            <nav style="margin-left: 30px;">
                <a href="dashboard.html">Dashboard</a>
                <a href="scan.html">Scanner</a>
                <a href="history.html">History</a>
                <a href="awareness.html" style="text-decoration: underline;">Awareness</a>
                <a href="community.html">Community</a>
            </nav>
        </div>
        <div class="user-area">
            <button id="logoutBtn" class="logout-btn">Logout</button>
        </div>
    </header>

    <main>
        <div class="hero">
            <h2>Awareness Center</h2>
            <p>Recognize the warning signs before you click, pay, or share. Scams often rely on creating a false sense of urgency, impersonating authority figures, or offering unrealistic rewards to pressure you into making quick decisions.</p>
        </div>

        <div class="filters" id="categoryFilters">
            <button class="filter-btn active" data-cat="all">All</button>
            <button class="filter-btn" data-cat="sms">SMS & Messages</button>
            <button class="filter-btn" data-cat="links">Phishing Links</button>
            <button class="filter-btn" data-cat="upi">UPI & Payments</button>
            <button class="filter-btn" data-cat="otp">OTP & Credentials</button>
        </div>

        <div class="grid" id="contentGrid">
            <!-- Populated by JS -->
        </div>

        <div class="grid">
            <div class="info-section">
                <h3>Common Warning Signs</h3>
                <ul class="info-list">
                    <li>Unexpected urgency or threats of account suspension</li>
                    <li>Requests for your OTP, PIN, or password</li>
                    <li>Unknown payment requests or pressure to transfer money</li>
                    <li>Suspicious links from unknown numbers</li>
                    <li>Unrealistic rewards or prize claims</li>
                    <li>Requests to bypass normal banking procedures</li>
                </ul>
            </div>
            
            <div class="info-section">
                <h3>If You Receive a Suspicious Message</h3>
                <ol class="sequence-list">
                    <li><strong>Stop.</strong> Do not click the link or download attachments.</li>
                    <li><strong>Do not share</strong> your OTP, PIN, or passwords.</li>
                    <li><strong>Verify independently</strong> by contacting the organization directly using official channels.</li>
                    <li><strong>Scan the message</strong> or link using the SurakshaScan Scanner.</li>
                    <li><strong>Report</strong> suspicious identifiers through our Community Reporting tool.</li>
                    <li>If money has already been lost, use appropriate official government or banking fraud-reporting channels immediately.</li>
                </ol>
            </div>
        </div>

        <div class="quiz-section" id="quizContainer">
            <h3>Test Your Knowledge</h3>
            <div id="quizContent">
                <div class="quiz-question" id="qText">Loading quiz...</div>
                <div class="quiz-options" id="qOptions"></div>
                <div class="quiz-feedback" id="qFeedback"></div>
                <button class="quiz-next" id="qNextBtn">Next Question</button>
            </div>
            <div id="quizResults" style="display:none; text-align:center;">
                <h2 style="margin-top:0;">Quiz Completed!</h2>
                <p id="quizScore" style="font-size: 20px; font-weight: bold;"></p>
                <p>Knowledge is your first line of defense. Keep scanning and stay safe.</p>
                <button class="quiz-next" id="qRestartBtn" style="display:inline-block; margin-top:15px;">Restart Quiz</button>
            </div>
        </div>
        
        <div class="community-cta">
            <h3 style="color:#333; margin-top:0;">Seen something suspicious?</h3>
            <p style="color:#666;">Help protect others by reporting malicious numbers, UPI IDs, and domains.</p>
            <a href="community.html" style="display:inline-block; background:#6c757d; color:#fff; padding:10px 20px; text-decoration:none; border-radius:4px; font-weight:bold;">Report to Community</a>
        </div>
    </main>

    <script src="js/api.js"></script>
    <script src="js/awareness.js"></script>
    <script>
        document.addEventListener('DOMContentLoaded', () => {
            checkAuthAndInit(() => {
                document.getElementById('pageBody').style.display = 'block';
                initAwareness();
            });

            document.getElementById('logoutBtn').addEventListener('click', async () => {
                await authenticatedFetch('/api/auth/logout', { method: 'POST' });
                window.location.href = 'login.html';
            });
        });
    </script>
</body>
</html>
"@
Set-Content -Path $awareHtmlPath -Value $awareHtmlContent -Encoding UTF8

# 4. Create js/awareness.js
$awareJsPath = "$base\frontend\js\awareness.js"
$awareJsContent = @"
const contentData = [
    {
        category: 'sms',
        title: 'SMS & Message Scams',
        description: 'Scammers frequently use SMS to send urgent account warnings, fake KYC requests, or suspicious payment links. They rely on panic to make you act quickly.',
        example: 'Your bank account will be blocked today. Complete KYC immediately by clicking: http://example-bank-update.com/kyc',
        exampleType: 'sms'
    },
    {
        category: 'links',
        title: 'Phishing Links',
        description: 'Phishing involves lookalike domains or shortened URLs designed to trick you into entering login credentials or payment details on a fake website.',
        example: 'https://secure-login-update.example.com/auth',
        exampleType: 'link'
    },
    {
        category: 'upi',
        title: 'UPI & Payment Scams',
        description: 'A common tactic is requesting you to enter your UPI PIN to "receive" money. Remember: A UPI PIN is ONLY required to send money, never to receive it.',
        example: 'fraudulent.receiver@upi',
        exampleType: 'upi'
    },
    {
        category: 'otp',
        title: 'OTP & Credential Scams',
        description: 'Fraudsters may call or message pretending to be customer support, claiming they need an OTP to cancel a fraudulent transaction. Never share your OTP.',
        example: 'Dear Customer, your transaction of Rs.50,000 is processing. Share OTP 4921 to cancel if you did not authorize this.',
        exampleType: 'sms'
    },
    {
        category: 'impersonation',
        title: 'Impersonation',
        description: 'Scammers impersonate bank officials, government agents, or tech support. They combine authority with urgency to pressure you into transferring funds or sharing data.',
        example: 'Notice: Income Tax Department requires immediate verification of your pending refund. Click here: http://refund-tax.example.com',
        exampleType: 'sms'
    }
];

const quizData = [
    {
        q: "You receive an unexpected message asking for your OTP to cancel a transaction. What should you do?",
        options: [
            "Share the OTP quickly to stop the transaction.",
            "Do not share the OTP and contact your bank directly.",
            "Reply to the message asking for proof."
        ],
        answerIndex: 1,
        explanation: "OTP is intended for authorization. Banks and legitimate organizations will never ask you to share your OTP to cancel a transaction."
    },
    {
        q: "A stranger says they need you to enter your UPI PIN to receive money they owe you. What should you do?",
        options: [
            "Enter the PIN so the money can be deposited.",
            "Share a different PIN just in case.",
            "Do not enter the PIN. A PIN is only used for sending money."
        ],
        answerIndex: 2,
        explanation: "You never need to enter your UPI PIN to receive money. Entering it will deduct funds from your account."
    },
    {
        q: "You receive a message threatening immediate account suspension with an unfamiliar link. What is a safer first step?",
        options: [
            "Click the link to check if it looks legitimate.",
            "Do not click the link; independently verify the claim using official channels.",
            "Forward the link to all your contacts to warn them."
        ],
        answerIndex: 1,
        explanation: "Scammers use threats of account suspension to create urgency. Never click suspicious links; independently log in to your account via the official app or website."
    }
];

let currentQuizIndex = 0;
let score = 0;

function initAwareness() {
    renderContent('all');
    
    // Check URL for category pre-selection
    const params = new URLSearchParams(window.location.search);
    const cat = params.get('category');
    if (cat) {
        const btn = document.querySelector(`.filter-btn[data-cat="\${cat}"]`);
        if (btn) {
            document.querySelectorAll('.filter-btn').forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            renderContent(cat);
        }
    }

    document.getElementById('categoryFilters').addEventListener('click', (e) => {
        if (e.target.classList.contains('filter-btn')) {
            document.querySelectorAll('.filter-btn').forEach(b => b.classList.remove('active'));
            e.target.classList.add('active');
            renderContent(e.target.getAttribute('data-cat'));
        }
    });

    renderQuiz();
    
    document.getElementById('qNextBtn').addEventListener('click', () => {
        currentQuizIndex++;
        if (currentQuizIndex < quizData.length) {
            renderQuiz();
        } else {
            showQuizResults();
        }
    });
    
    document.getElementById('qRestartBtn').addEventListener('click', () => {
        currentQuizIndex = 0;
        score = 0;
        document.getElementById('quizResults').style.display = 'none';
        document.getElementById('quizContent').style.display = 'block';
        renderQuiz();
    });
}

function renderContent(filter) {
    const grid = document.getElementById('contentGrid');
    grid.innerHTML = '';
    
    contentData.forEach(item => {
        if (filter === 'all' || item.category === filter || (filter === 'sms' && item.category === 'impersonation') || (filter === 'sms' && item.category === 'otp')) {
            const card = document.createElement('div');
            card.className = 'card';
            
            const h3 = document.createElement('h3');
            h3.textContent = item.title;
            
            const p = document.createElement('p');
            p.textContent = item.description;
            
            const exBox = document.createElement('div');
            exBox.className = 'example-box';
            
            const exLabel = document.createElement('div');
            exLabel.className = 'example-label';
            exLabel.textContent = 'Illustrative Example';
            
            const exText = document.createElement('div');
            exText.className = 'example-text';
            exText.textContent = item.example;
            
            exBox.appendChild(exLabel);
            exBox.appendChild(exText);
            
            const btnBox = document.createElement('div');
            btnBox.style.marginTop = '15px';
            btnBox.style.textAlign = 'right';
            
            const btn = document.createElement('a');
            btn.className = 'try-btn';
            btn.textContent = 'Try Scanning ->';
            btn.href = `scan.html?type=\${item.exampleType}&text=\${encodeURIComponent(item.example)}`;
            
            btnBox.appendChild(btn);
            
            card.appendChild(h3);
            card.appendChild(p);
            card.appendChild(exBox);
            card.appendChild(btnBox);
            
            grid.appendChild(card);
        }
    });
}

function renderQuiz() {
    const qData = quizData[currentQuizIndex];
    document.getElementById('qText').textContent = `Question \${currentQuizIndex + 1} of \${quizData.length}: \${qData.q}`;
    
    const optionsContainer = document.getElementById('qOptions');
    optionsContainer.innerHTML = '';
    
    const feedback = document.getElementById('qFeedback');
    feedback.style.display = 'none';
    
    const nextBtn = document.getElementById('qNextBtn');
    nextBtn.style.display = 'none';
    
    qData.options.forEach((optText, index) => {
        const btn = document.createElement('button');
        btn.className = 'quiz-option';
        btn.textContent = optText;
        btn.onclick = () => handleAnswer(index, btn, qData);
        optionsContainer.appendChild(btn);
    });
}

function handleAnswer(selectedIndex, btn, qData) {
    const optionsContainer = document.getElementById('qOptions');
    // Disable all
    Array.from(optionsContainer.children).forEach(child => {
        child.disabled = true;
        child.style.cursor = 'default';
    });
    
    const feedback = document.getElementById('qFeedback');
    const explanationHtml = `<div class="quiz-explanation">\${escapeHtml(qData.explanation)}</div>`;
    
    if (selectedIndex === qData.answerIndex) {
        btn.style.background = '#28a745';
        btn.style.borderColor = '#28a745';
        score++;
        feedback.innerHTML = 'Correct! ' + explanationHtml;
        feedback.className = 'quiz-feedback feedback-correct';
    } else {
        btn.style.background = '#dc3545';
        btn.style.borderColor = '#dc3545';
        
        // Highlight correct answer
        optionsContainer.children[qData.answerIndex].style.border = '2px solid #28a745';
        
        feedback.innerHTML = 'Incorrect. ' + explanationHtml;
        feedback.className = 'quiz-feedback feedback-incorrect';
    }
    
    feedback.style.display = 'block';
    document.getElementById('qNextBtn').style.display = 'inline-block';
}

function showQuizResults() {
    document.getElementById('quizContent').style.display = 'none';
    const resDiv = document.getElementById('quizResults');
    resDiv.style.display = 'block';
    document.getElementById('quizScore').textContent = `You scored \${score} out of \${quizData.length}`;
}

function escapeHtml(unsafe) {
    if(!unsafe) return "";
    return unsafe.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
}
"@
Set-Content -Path $awareJsPath -Value $awareJsContent -Encoding UTF8

# 5. Update scanner.js to handle query params for pre-filling and add Awareness link
$scanJsPath = "$base\frontend\js\scanner.js"
$scanJsContent = Get-Content $scanJsPath -Raw

# Inject pre-filling logic inside DOMContentLoaded checkAuthAndInit
$scanJsContent = $scanJsContent -replace 'document.getElementById\(''pageBody''\).style.display = ''block'';', @"
                document.getElementById('pageBody').style.display = 'block';
                
                // Prefill logic
                const params = new URLSearchParams(window.location.search);
                const type = params.get('type');
                const text = params.get('text');
                if (type && text) {
                    const tabBtn = document.querySelector(`.tab-btn[data-target="scan\${type.charAt(0).toUpperCase() + type.slice(1)}"]`);
                    if (tabBtn) tabBtn.click();
                    
                    if (type === 'sms') document.getElementById('scanContentSms').value = text;
                    else if (type === 'upi') document.getElementById('scanContentUpi').value = text;
                    else if (type === 'link') document.getElementById('scanContentLink').value = text;
                }
"@

# Inject Awareness link in result generation
$scanJsContent = $scanJsContent -replace 'let reportHtml = '''';', @"
        let reportHtml = '';
        let awarenessHtml = '';
"@

$scanJsContent = $scanJsContent -replace 'reportDiv.innerHTML = reportHtml;', @"
        if (scanData.verdict !== 'SAFE') {
            awarenessHtml = \`
                <div style="margin-top: 10px; font-size:14px;">
                    <a href="awareness.html?category=\${scanData.inputType === 'link' ? 'links' : scanData.inputType}" style="color:#0056b3; font-weight:bold; text-decoration:none;">Learn why this was flagged &rarr;</a>
                </div>
            \`;
        }
        reportDiv.innerHTML = awarenessHtml + reportHtml;
"@

Set-Content -Path $scanJsPath -Value $scanJsContent -Encoding UTF8

# 6. README update
$readmePath = "$base\README.md"
$readmeContent = Get-Content $readmePath -Raw
$readmeContent += @"

## Awareness Center (Step 7)
A professional educational module teaching users to recognize warning signs before they act.

### Details
* **Educational Content**: Covers SMS, Phishing Links, UPI, OTP, and Impersonation scams. Content focuses on factual warning signs and explicitly labels examples as "Illustrative Example" to prevent panic or confusion.
* **Scan Integration**: Users can click "Try Scanning" on an illustrative example to pre-fill the `scan.html` utility, reinforcing the connection between education and active defense. Automatic submission is strictly blocked.
* **Interactive Quiz**: Includes a client-side quiz focused on teaching proper responses to threats (e.g., "Do not share your OTP"). It provides immediate feedback without saving metrics or creating fake statistics.
* **Safe DOM APIs**: Content and quiz rendering strictly utilize `.textContent` and safe sanitization to prevent accidental execution of example malicious payloads.
* **Responsive & Accessible**: Clean, semantic layout adopting SurakshaScan's core UI styling across all device widths.
"@
Set-Content -Path $readmePath -Value $readmeContent -Encoding UTF8

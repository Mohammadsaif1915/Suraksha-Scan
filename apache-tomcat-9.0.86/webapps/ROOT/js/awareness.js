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
        const btn = document.querySelector(.filter-btn[data-cat="\"]);
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
            btn.href = scan.html?type=\&text=\;
            
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
    document.getElementById('qText').textContent = Question \ of \: \;
    
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
    const explanationHtml = <div class="quiz-explanation">\</div>;
    
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
    document.getElementById('quizScore').textContent = You scored \ out of \;
}

function escapeHtml(unsafe) {
    if(!unsafe) return "";
    return unsafe.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
}

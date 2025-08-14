document.addEventListener('DOMContentLoaded', () => {
    const loginForm = document.getElementById('login-form');
    const signupForm = document.getElementById('signup-form');
    const loginCard = document.getElementById('login-card');
    const signupCard = document.getElementById('signup-card');
    const showSignupLink = document.getElementById('show-signup');
    const showLoginLink = document.getElementById('show-login');

    // Toggle between login and signup forms
    showSignupLink.addEventListener('click', (e) => {
        e.preventDefault();
        loginCard.classList.add('hidden');
        signupCard.classList.remove('hidden');
    });

    showLoginLink.addEventListener('click', (e) => {
        e.preventDefault();
        signupCard.classList.add('hidden');
        loginCard.classList.remove('hidden');
    });

    // Handle login form submission
    loginForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const username = document.getElementById('login-username').value;
        const password = document.getElementById('login-password').value;

        try {
            // CORRECTED: Send request to Java backend on port 8081
            const response = await fetch('http://localhost:8081/api/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username, password })
            });
            
            const data = await response.json();
            
            if (response.ok) {
                // Store auth token and redirect on success
                localStorage.setItem('authToken', data.token);
                window.location.href = 'index.html';
            } else {
                alert(data.error);
            }
        } catch (error) {
            alert("Login failed. Please check your network connection.");
        }
    });

    // Handle signup form submission
    signupForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const username = document.getElementById('signup-username').value;
        const password = document.getElementById('signup-password').value;

        try {
            // CORRECTED: Send request to Java backend on port 8081
            const response = await fetch('http://localhost:8081/api/signup', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username, password })
            });
            
            const data = await response.json();
            
            if (response.ok) {
                alert('Signup successful! Please log in.');
                signupCard.classList.add('hidden');
                loginCard.classList.remove('hidden');
            } else {
                alert(data.error);
            }
        } catch (error) {
            alert("Signup failed. Please check your network connection.");
        }
    });
});

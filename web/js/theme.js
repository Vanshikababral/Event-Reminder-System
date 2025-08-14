class ThemeManager {
    static init() {
        const themeToggle = document.getElementById('theme-toggle');
        if (!themeToggle) return;
        
        const themeIcon = themeToggle.querySelector('.theme-icon i');
        const savedTheme = localStorage.getItem('theme') || 'light';
        
        document.documentElement.setAttribute('data-theme', savedTheme);
        themeIcon.className = savedTheme === 'dark' ? 'fa-solid fa-sun' : 'fa-solid fa-moon';
        
        themeToggle.addEventListener('click', () => {
            const currentTheme = document.documentElement.getAttribute('data-theme');
            const newTheme = currentTheme === 'dark' ? 'light' : 'dark';
            
            document.documentElement.setAttribute('data-theme', newTheme);
            localStorage.setItem('theme', newTheme);

            themeIcon.className = newTheme === 'dark' ? 'fa-solid fa-sun' : 'fa-solid fa-moon';
        });
    }
}

// Call the init method on page load
document.addEventListener('DOMContentLoaded', () => {
    ThemeManager.init();
});

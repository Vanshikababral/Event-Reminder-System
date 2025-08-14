const API_URL = "http://localhost:8081/api/events";

// Utility function to get the authentication token
function getAuthToken() {
    return localStorage.getItem('authToken');
}

// Check if a user is authenticated
function isAuthenticated() {
    return !!getAuthToken();
}

class Toast {
    static show(message, type = 'info') {
        const toast = document.getElementById('toast');
        if (!toast) return;

        toast.textContent = message;
        toast.className = 'toast show';
        
        if (type === 'error') {
            toast.style.backgroundColor = 'var(--danger-color)';
        } else if (type === 'success') {
            toast.style.backgroundColor = 'var(--secondary-color)';
        } else {
            toast.style.backgroundColor = 'var(--primary-color)';
        }
        
        setTimeout(() => toast.classList.remove('show'), 3000);
    }
}

class EventService {
    static async addEvent(eventData) {
        try {
            const response = await fetch(API_URL, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(eventData)
            });
            
            if (!response.ok) {
                const error = await response.json();
                throw new Error(error.error || 'Failed to add event');
            }
            
            Toast.show('Event added!', 'success');
            return await response.json();
        } catch (error) {
            Toast.show(error.message, 'error');
            throw error;
        }
    }

    static async getEventsByCategory(category = 'all') {
        let url = API_URL;
        if (category !== 'all') {
            url += `?category=${category}`;
        }
        
        try {
            const response = await fetch(url);
            if (!response.ok) throw new Error('Failed to fetch events');
            const events = await response.json();
            return events.map(event => ({
                ...event,
                dateObject: new Date(event.eventTime)
            }));
        } catch (error) {
            Toast.show('Error fetching events: ' + error.message, 'error');
            throw error;
        }
    }

    static async deleteEvent(id) {
        try {
            const response = await fetch(`${API_URL}/${id}`, { method: 'DELETE' });
            if (!response.ok) throw new Error('Failed to delete event');
            Toast.show('Event deleted!', 'success');
        } catch (error) {
            Toast.show('Error deleting event: ' + error.message, 'error');
            throw error;
        }
    }
}

function formatDateTimeLocalToISO(dateTimeLocal) {
    return dateTimeLocal + ":00Z";
}

class EventRenderer {
    static renderEvents(events) {
        const container = document.getElementById('eventsContainer');
        if (!container) return;

        if (events.length === 0) {
            container.innerHTML = '<p class="no-events">No events found</p>';
            return;
        }
        
        container.innerHTML = events
            .sort((a, b) => a.dateObject - b.dateObject)
            .map((event, index) => this.createEventCard(event, index))
            .join('');
    }

    static createEventCard(event, index) {
        const options = {
            month: 'short',
            day: 'numeric',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        };
        
        const formattedDate = event.dateObject.toLocaleString('en-US', options);
        
        return `
            <div class="event-card ${event.priority.toLowerCase()}-priority" 
                 style="animation-delay: ${index * 50}ms">
                <div class="event-content">
                    <h3 class="event-title">${event.title}</h3>
                    <p class="event-time">${formattedDate}</p>
                    ${event.description ? `<p class="event-description">${event.description}</p>` : ''}
                    <p class="event-priority">Priority: ${event.priority.toLowerCase()}</p>
                    ${event.isRecurring ? '<p class="event-recurring">🔁 Recurring</p>' : ''}
                </div>
                <button class="delete-btn" data-id="${event.id}">Delete</button>
            </div>
        `;
    }
}

class CalendarManager {
    static async init() {
        const calendarEl = document.getElementById('calendar');
        if (!calendarEl) return;

        this.setupCategoryFiltering();
        await this.loadEvents();
    }

    static setupCategoryFiltering() {
        const filters = document.querySelectorAll('.category-filters .filter-btn');
        if (!filters) return;
        
        filters.forEach(button => {
            button.addEventListener('click', async (e) => {
                filters.forEach(btn => btn.classList.remove('active'));
                e.target.classList.add('active');
                const category = e.target.dataset.category;
                await this.loadEvents(category);
            });
        });
    }

    static async loadEvents(category = 'all') {
        const calendarEl = document.getElementById('calendar');
        if (!calendarEl) return;

        const events = await EventService.getEventsByCategory(category);
        const formattedEvents = events.map(event => ({
            id: event.id,
            title: event.title,
            start: event.eventTime,
            color: this.getPriorityColor(event.priority)
        }));

        const calendar = new FullCalendar.Calendar(calendarEl, {
            initialView: 'dayGridMonth',
            headerToolbar: {
                left: 'prev,next today',
                center: 'title',
                right: 'dayGridMonth,timeGridWeek,timeGridDay'
            },
            events: formattedEvents,
            eventClick: function(info) {
                alert(`Event: ${info.event.title}\nDate: ${info.event.startStr}`);
            }
        });
        calendar.render();
    }

    static getPriorityColor(priority) {
        switch (priority.toUpperCase()) {
            case 'HIGH': return '#dc2626';
            case 'MEDIUM': return '#f59e0b';
            case 'LOW': return '#16a34a';
            default: return '#2563eb';
        }
    }
}

class EventManager {
    static async init() {
        // Redirect to login page if not authenticated
        if (!isAuthenticated()) {
            window.location.href = 'login.html';
            return;
        }

        // Setup logout button
        const logoutBtn = document.getElementById('logout-btn');
        if (logoutBtn) {
            logoutBtn.addEventListener('click', () => {
                localStorage.removeItem('authToken');
                window.location.href = 'login.html';
            });
        }
        
        if (document.getElementById('eventsContainer')) {
            await this.loadEvents();
            this.setupEventListeners();
            this.startPolling();
        } else if (document.getElementById('calendar')) {
            await CalendarManager.init();
        }
    }

    static async loadEvents(category = 'all') {
        const events = await EventService.getEventsByCategory(category);
        EventRenderer.renderEvents(events);
    }

    static setupEventListeners() {
        document.getElementById('eventForm').addEventListener('submit', async (e) => {
            e.preventDefault();
            await this.handleFormSubmit();
        });

        document.getElementById('timeFilter').addEventListener('change', async (e) => {
            await this.handleFilterChange(e.target.value);
        });

        document.addEventListener('click', async (e) => {
            if (e.target.classList.contains('delete-btn')) {
                await this.handleDeleteEvent(e.target.dataset.id);
            }
        });

        document.querySelectorAll('.category-filters .filter-btn').forEach(button => {
            button.addEventListener('click', async (e) => {
                document.querySelectorAll('.category-filters .filter-btn').forEach(btn => {
                    btn.classList.remove('active');
                    btn.setAttribute('aria-pressed', 'false');
                });
                e.target.classList.add('active');
                e.target.setAttribute('aria-pressed', 'true');
                await this.loadEvents(e.target.dataset.category);
            });
        });
    }

    static async handleFormSubmit() {
        const form = document.getElementById('eventForm');
        const eventData = {
            title: form.title.value.trim(),
            description: form.description.value.trim(),
            eventTime: formatDateTimeLocalToISO(form.dateTime.value),
            priority: form.priority.value,
            isRecurring: form.recurring.checked,
            category: form.category.value
        };

        if (!eventData.title || !eventData.eventTime) {
            Toast.show('Please fill required fields', 'error');
            return;
        }

        try {
            await EventService.addEvent(eventData);
            await this.loadEvents();
            form.reset();
        } catch (error) {
            console.error('Error:', error);
        }
    }

    static async handleDeleteEvent(id) {
        try {
            await EventService.deleteEvent(id);
            await this.loadEvents();
        } catch (error) {
            console.error('Error:', error);
        }
    }

    static async handleFilterChange(filter) {
        const events = await EventService.getEventsByCategory();
        const now = new Date();
        
        const filtered = events.filter(event => {
            const date = event.dateObject;
            
            switch (filter) {
                case 'today':
                    return date.toDateString() === now.toDateString();
                case 'week':
                    const nextWeek = new Date(now);
                    nextWeek.setDate(now.getDate() + 7);
                    return date >= now && date <= nextWeek;
                case 'month':
                    const nextMonth = new Date(now);
                    nextMonth.setMonth(now.getMonth() + 1);
                    return date >= now && date <= nextMonth;
                default:
                    return true;
            }
        });
        
        EventRenderer.renderEvents(filtered);
    }

    static startPolling() {
        setInterval(async () => {
            try {
                await this.loadEvents();
            } catch (error) {
                console.error('Polling error:', error);
            }
        }, 10000);
    }
}

document.addEventListener('DOMContentLoaded', () => EventManager.init());

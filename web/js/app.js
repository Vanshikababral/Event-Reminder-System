const API_URL = "http://localhost:8081/api/events";

class Toast {
    static show(message, type = 'info') {
        const toast = document.getElementById('toast');
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

    static async getAllEvents() {
        try {
            const response = await fetch(API_URL);
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

// Convert datetime-local input to ISO format
function formatDateTimeLocalToISO(dateTimeLocal) {
    // Input: "YYYY-MM-DDTHH:mm" → Output: "YYYY-MM-DDTHH:mm:ssZ"
    return dateTimeLocal + ":00Z";
}

class EventRenderer {
    static renderEvents(events) {
        const container = document.getElementById('eventsContainer');
        
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

    static updateNextEvent(events) {
        const element = document.getElementById('nextEvent');
        if (events.length === 0) {
            element.textContent = 'None scheduled';
            return;
        }
        
        const nextEvent = events.reduce((prev, curr) => 
            prev.dateObject < curr.dateObject ? prev : curr);
        
        const options = {
            month: 'short',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        };
        
        element.textContent = `${nextEvent.title} at ${nextEvent.dateObject.toLocaleString('en-US', options)}`;
    }
}

class EventManager {
    static async init() {
        await this.loadEvents();
        this.setupEventListeners();
        this.startPolling(); // Start real-time updates
    }

    static async loadEvents() {
        const events = await EventService.getAllEvents();
        EventRenderer.renderEvents(events);
        EventRenderer.updateNextEvent(events);
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
    }

    static async handleFormSubmit() {
        const form = document.getElementById('eventForm');
        const eventData = {
            title: form.title.value.trim(),
            description: form.description.value.trim(),
            eventTime: formatDateTimeLocalToISO(form.dateTime.value),
            priority: form.priority.value,
            isRecurring: form.recurring.checked
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
        const events = await EventService.getAllEvents();
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
        // Poll every 10 seconds for real-time updates
        setInterval(async () => {
            try {
                await this.loadEvents();
            } catch (error) {
                console.error('Polling error:', error);
            }
        }, 10000); // 10 seconds
    }
}

document.addEventListener('DOMContentLoaded', () => EventManager.init());
export class Utils {
    static debounce(func, delay) {
        let timeoutId;
        return function (...args) {
            clearTimeout(timeoutId);
            timeoutId = setTimeout(() => func.apply(this, args), delay);
        };
    }

    static smoothScroll(elementId, duration = 800) {
        setTimeout(() => {
            const target = document.getElementById(elementId);
            if (!target) return;

            const targetPosition = target.getBoundingClientRect().top + window.pageYOffset;
            const startPosition = window.pageYOffset;
            const distance = targetPosition - startPosition;
            let startTime = null;

            const easeOutQuad = (t, b, c, d) => {
                t /= d;
                return -c * t * (t - 2) + b;
            };

            function animation(currentTime) {
                if (startTime === null) startTime = currentTime;
                const timeElapsed = currentTime - startTime;
                const run = easeOutQuad(timeElapsed, startPosition, distance, duration);
                window.scrollTo(0, run);
                if (timeElapsed < duration) {
                    requestAnimationFrame(animation);
                }
            }
            requestAnimationFrame(animation);
        }, 500);
    }

    static getMethodColor(method) {
        const colors = {
            'GET': 'bg-green-100 text-green-700',
            'POST': 'bg-blue-100 text-blue-700',
            'PUT': 'bg-yellow-100 text-yellow-700',
            'DELETE': 'bg-red-100 text-red-700'
        };
        return colors[method] || 'bg-gray-100 text-gray-700';
    }

    static formatDate(dateString) {
        return new Date(dateString).toLocaleDateString();
    }

    static formatCurrency(amount) {
        return `$${amount.toFixed(2)}`;
    }

    static getUserNameFromEmail(email) {
        return email ? email.split('@')[0] : 'Guest';
    }

    static previewImage(event) {
        const reader = new FileReader();
        const file = event.target.files[0];

        reader.onload = function () {
            const preview = document.getElementById('image-preview');
            const container = document.getElementById('image-preview-container');

            preview.src = reader.result; // Base64 data for the preview
            container.classList.remove('hidden');
        }

        if (file) {
            reader.readAsDataURL(file);
        }
    }
}
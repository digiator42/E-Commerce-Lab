export class ComponentStore {
    static instance = null;

    constructor() {
        this.templates = {};
    }

    static getInstance() {
        if (!ComponentStore.instance) {
            ComponentStore.instance = new ComponentStore();
        }
        return ComponentStore.instance;
    }

    async load(name) {
        if (!this.templates[name]) {
            try {
                const res = await fetch(`/components/${name}.html`);
                if (!res.ok) throw new Error(`Component ${name} not found (Status: ${res.status})`);
                this.templates[name] = await res.text();
            } catch (err) {
                console.error('Template Load Error:', err);
                return `<div class="p-8 text-red-500 bg-red-50 rounded-lg">
                            <strong>Error:</strong> Could not load component "${name}".
                        </div>`;
            }
        }
        return this.templates[name];
    }
}
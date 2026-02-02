/* Patterns Lab JavaScript */

const API_BASE = '/api';

// Helper to show result in specific demo-result div
function showResult(patternId, data) {
    const resultEl = document.getElementById(`${patternId}-result`);
    if (resultEl) {
        const timestamp = new Date().toLocaleTimeString();
        const text = typeof data === 'string' ? data : JSON.stringify(data, null, 2);
        resultEl.textContent = `[${timestamp}] ${text}`;
    }
    // Also log to global output (from app.js)
    if (typeof log === 'function') {
        log(`[${patternId.toUpperCase()}]`, data);
    }
}

// API call helper
async function patternApiCall(method, endpoint, params = {}) {
    let url = API_BASE + endpoint;
    const finalParams = { ...params };

    // Replace path variables
    url = url.replace(/\{(\w+)}/g, (_, key) => {
        if (finalParams[key] !== undefined) {
            const val = encodeURIComponent(String(finalParams[key]));
            delete finalParams[key];
            return val;
        }
        return '';
    });

    const options = { method };

    const searchParams = new URLSearchParams();
    for (const [k, v] of Object.entries(finalParams)) {
        if (v === undefined || v === null) continue;
        searchParams.set(k, String(v));
    }

    const qs = searchParams.toString();
    if (qs) {
        url += `?${qs}`;
    }

    const response = await fetch(url, options);
    const contentType = response.headers.get('content-type') || '';
    const body = contentType.includes('application/json') ? await response.json() : await response.text();

    if (!response.ok) {
        throw new Error(typeof body === 'string' ? body : (body?.message || 'Request failed'));
    }
    return body;
}

// ========== SCENE BUTTONS (Facade) ==========
document.querySelectorAll('.btn-scene').forEach(btn => {
    btn.addEventListener('click', async () => {
        const scene = btn.dataset.scene;
        try {
            const result = await patternApiCall('POST', `/patterns/facade/scene/${scene}`);
            showResult('facade', result);
        } catch (e) {
            showResult('facade', { error: e.message });
        }
    });
});

// ========== STRATEGY BUTTONS ==========
document.querySelectorAll('.btn-strategy').forEach(btn => {
    btn.addEventListener('click', async () => {
        const strategy = btn.dataset.strategy;
        try {
            const result = await patternApiCall('POST', '/patterns/strategy/apply', { strategy });
            showResult('strategy', result);
        } catch (e) {
            showResult('strategy', { error: e.message });
        }
    });
});

// ========== AUDIT BUTTONS (Visitor) ==========
document.querySelectorAll('.btn-audit').forEach(btn => {
    btn.addEventListener('click', async () => {
        const type = btn.dataset.audit;
        try {
            const result = await patternApiCall('GET', '/patterns/visitor/audit', { type });
            showResult('visitor', result);
        } catch (e) {
            showResult('visitor', { error: e.message });
        }
    });
});

// ========== PATTERN RUN BUTTONS ==========
document.querySelectorAll('.pattern-run-btn').forEach(btn => {
    btn.addEventListener('click', async () => {
        const patternId = btn.dataset.pattern;
        try {
            let result;
            switch (patternId) {
                case 'singleton':
                    result = await patternApiCall('GET', '/status');
                    showResult('singleton', result);
                    break;
                case 'flyweight':
                    result = await patternApiCall('GET', '/patterns/flyweight/demo');
                    showResult('flyweight', result);
                    break;
                case 'state':
                    result = await patternApiCall('GET', '/patterns/state/demo');
                    showResult('state', result);
                    break;
                default:
                    result = await patternApiCall('GET', '/patterns');
                    showResult(patternId || 'patterns', result);
            }
        } catch (e) {
            showResult(patternId || 'patterns', { error: e.message });
        }
    });
});

// ========== COMMAND: UNDO / REDO ==========
document.getElementById('undo-btn')?.addEventListener('click', async () => {
    try {
        const result = await patternApiCall('POST', '/patterns/command/undo');
        showResult('command', result);
    } catch (e) {
        showResult('command', { error: e.message });
    }
});

// Optional: if you add a redo button later, wire it here.
const redoBtn = document.getElementById('redo-btn');
redoBtn?.addEventListener('click', async () => {
    try {
        const result = await patternApiCall('POST', '/patterns/command/redo');
        showResult('command', result);
    } catch (e) {
        showResult('command', { error: e.message });
    }
});

// ========== MEMENTO: LIST / RESTORE ==========
document.getElementById('restore-btn')?.addEventListener('click', async () => {
    const sceneName = document.querySelector('#memento input[name="sceneName"]')?.value || 'My Snapshot';
    try {
        const result = await patternApiCall('POST', '/patterns/memento/restore', { sceneName });
        showResult('memento', result);
    } catch (e) {
        showResult('memento', { error: e.message });
    }
});

document.getElementById('list-mementos-btn')?.addEventListener('click', async () => {
    try {
        const result = await patternApiCall('GET', '/patterns/memento/list');
        showResult('memento', result);
    } catch (e) {
        showResult('memento', { error: e.message });
    }
});

// ========== OBSERVER: SUBSCRIBE / TRIGGER ==========
// If user clicks "Trigger Event" we will call /trigger (real backend).
document.getElementById('trigger-event-btn')?.addEventListener('click', async () => {
    const deviceId = document.querySelector('#observer input[name="deviceId"]')?.value || 'living-thermostat';
    const eventType = 'MOTION';
    try {
        const result = await patternApiCall('POST', '/patterns/observer/trigger', { deviceId, eventType });
        showResult('observer', result);
    } catch (e) {
        showResult('observer', { error: e.message });
    }
});

// ========== FORM SUBMISSIONS ==========
document.querySelectorAll('.demo-form').forEach(form => {
    form.addEventListener('submit', async (e) => {
        e.preventDefault();

        const endpoint = form.dataset.apiEndpoint;
        const method = (form.dataset.apiMethod || 'POST').toUpperCase();

        // Get form data
        const formData = new FormData(form);
        const params = {};
        for (const [key, value] of formData.entries()) {
            params[key] = value;
        }

        // Find pattern ID from closest section
        const section = form.closest('.pattern-section');
        const patternId = section?.id || 'unknown';

        try {
            // Prefer new endpoints where available.
            if (endpoint === '/patterns/observer/register') {
                // Treat register as subscribe in the UI.
                const result = await patternApiCall('POST', '/patterns/observer/subscribe', params);
                showResult(patternId, result);
                return;
            }

            if (endpoint === '/patterns/mediator/notify') {
                // Now supported.
                const result = await patternApiCall('POST', '/patterns/mediator/notify', params);
                showResult(patternId, result);
                return;
            }

            if (endpoint === '/patterns/template/init') {
                // Now supported.
                const result = await patternApiCall('POST', '/patterns/template/init', params);
                showResult(patternId, result);
                return;
            }

            const result = await patternApiCall(method, endpoint, params);
            showResult(patternId, result);
        } catch (err) {
            showResult(patternId, { error: err.message });
        }
    });
});

// ========== SMOOTH SCROLL FOR NAV LINKS ==========
document.querySelectorAll('.pattern-nav a').forEach(link => {
    link.addEventListener('click', (e) => {
        e.preventDefault();
        const targetId = link.getAttribute('href').substring(1);
        const target = document.getElementById(targetId);
        if (target) {
            target.scrollIntoView({ behavior: 'smooth', block: 'start' });
            target.style.transition = 'box-shadow 0.3s ease';
            target.style.boxShadow = '0 0 30px rgba(0, 210, 255, 0.5)';
            setTimeout(() => {
                target.style.boxShadow = '';
            }, 2000);
        }
    });
});

console.log('Patterns Lab loaded - All pattern demos wired to backend endpoints.');

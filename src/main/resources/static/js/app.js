/* global fetch */

const API_BASE = '/api';

function getOutputEl() {
    return document.getElementById('output');
}

function log(message, payload) {
    const output = getOutputEl();
    if (!output) return;
    const timestamp = new Date().toLocaleTimeString();
    const text = payload === undefined ? message : `${message}\n${JSON.stringify(payload, null, 2)}`;
    output.textContent = `[${timestamp}] ${text}\n\n` + output.textContent;
}

function cssEscape(value) {
    if (window.CSS && typeof window.CSS.escape === 'function') {
        return window.CSS.escape(value);
    }
    return String(value).replace(/[^a-zA-Z0-9_-]/g, '\\$&');
}

function updateDeviceCard(device) {
    if (!device?.id) return;
    const card = document.querySelector(`[data-device-id="${cssEscape(device.id)}"]`);
    if (!card) return;

    const statusEl = card.querySelector('.device-status');
    if (statusEl) {
        statusEl.textContent = device.isOn ? 'ON' : 'OFF';
        statusEl.classList.toggle('status-on', !!device.isOn);
        statusEl.classList.toggle('status-off', !device.isOn);
    }

    const powerEl = card.querySelector('.power-indicator .value');
    if (powerEl && typeof device.power === 'number') {
        powerEl.textContent = `${device.power}W`;
    }
}

async function apiCall(method, endpoint, params = {}) {
    let url = API_BASE + endpoint;
    const finalParams = { ...params };

    // Replace path variables e.g. {id}
    url = url.replace(/\{(\w+)\}/g, (_, key) => {
        if (finalParams[key] !== undefined) {
            const val = encodeURIComponent(String(finalParams[key]));
            delete finalParams[key];
            return val;
        }
        return ''; // Or maybe leave it? but usually we want to replace.
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

async function refreshDevices() {
    try {
        const devices = await apiCall('GET', '/devices');
        const activeCount = (devices || []).filter(d => d.isOn).length;
        const activeEl = document.getElementById('activeDevices');
        if (activeEl) activeEl.textContent = String(activeCount);
        (devices || []).forEach(updateDeviceCard);
        return devices;
    } catch (e) {
        log('Error refreshing devices', { error: e.message });
        return null;
    }
}

async function setHomeMode(mode) {
    try {
        const result = await apiCall('POST', `/mode/${encodeURIComponent(mode)}`);
        const homeModeEl = document.getElementById('homeMode');
        if (homeModeEl && result?.homeMode) {
            homeModeEl.textContent = String(result.homeMode);
        }
        log(`Home Mode -> ${mode}`, result);
    } catch (e) {
        log('Failed to set home mode', { error: e.message });
    }
}

async function controlDevice(deviceId, action) {
    try {
        const result = await apiCall('POST', `/devices/${encodeURIComponent(deviceId)}/control`, { action });
        log(`Device Control: ${deviceId} -> ${action}`, result);
        updateDeviceCard(result);
        await refreshDevices();
    } catch (e) {
        log('Device control failed', { error: e.message });
    }
}

async function controlRoom(roomId, action) {
    try {
        const result = await apiCall('POST', `/patterns/composite/rooms/${encodeURIComponent(roomId)}/control`, { action });
        log(`Room Control: ${roomId} -> ${action}`, result);
        await refreshDevices();
    } catch (e) {
        log('Room control failed', { error: e.message });
    }
}

async function activateScene(sceneName) {
    try {
        const result = await apiCall('POST', `/patterns/facade/scene/${encodeURIComponent(sceneName)}`);
        log(`Scene Activated: ${sceneName}`, result);
        await refreshDevices();
    } catch (e) {
        log('Scene activation failed', { error: e.message });
    }
}

async function getAnyDeviceId() {
    const devices = await refreshDevices();
    if (!devices || devices.length === 0) return null;
    return devices[0].id;
}

async function runPatternDemo(patternId) {
    try {
        let result;
        const anyDeviceId = await getAnyDeviceId();

        switch (patternId) {
            case 'singleton':
                result = await apiCall('GET', '/status');
                break;
            case 'factory':
                result = await apiCall('POST', '/patterns/factory/create', { type: 'LIGHT', name: 'New Light', location: 'Demo Room' });
                break;
            case 'abstract-factory':
                result = await apiCall('POST', '/patterns/abstract-factory/create', { ecosystem: 'SMARTTHINGS', location: 'Demo Room' });
                break;
            case 'builder':
                result = await apiCall('POST', '/patterns/builder/rule', { name: 'Motion Lights', trigger: 'motion', condition: 'night', action: 'light on' });
                break;
            case 'prototype':
                result = await apiCall('GET', '/patterns/prototype/templates');
                break;
            case 'adapter':
                result = await apiCall('POST', '/patterns/adapter/legacy', { name: 'Old Thermostat', location: 'Basement' });
                break;
            case 'bridge':
                result = await apiCall('GET', '/patterns/bridge/demo');
                break;
            case 'composite':
                result = await apiCall('GET', '/patterns/composite/rooms');
                break;
            case 'decorator':
                result = await apiCall('POST', '/patterns/decorator/wrap', { deviceId: anyDeviceId || 'living-light-1', decorators: 'LOGGING,SECURITY,CACHING' });
                break;
            case 'facade':
                result = await apiCall('POST', '/patterns/facade/scene/movie');
                break;
            case 'flyweight':
                result = await apiCall('GET', '/patterns/flyweight/demo');
                break;
            case 'proxy':
                result = await apiCall('POST', '/patterns/proxy/remote', { name: 'Remote Camera', address: '192.168.1.100' });
                break;
            case 'chain':
                result = await apiCall('POST', '/patterns/chain/alert', { deviceId: anyDeviceId || 'sensor-1', level: 'WARNING', message: 'Motion detected at front door' });
                break;
            case 'command':
                result = await apiCall('POST', '/patterns/command/execute', { deviceId: anyDeviceId || 'living-light-1', command: 'ON' });
                break;
            case 'interpreter':
                result = await apiCall('POST', '/patterns/interpreter/evaluate', { rule: 'motion AND hour >= 18', motion: true, hour: 20 });
                break;
            case 'iterator':
                result = await apiCall('GET', '/patterns/iterator/demo', { filterType: 'ROOM', filterValue: 'Living Room' });
                break;
            case 'mediator':
                result = await apiCall('GET', '/patterns/mediator/demo');
                break;
            case 'memento':
                result = await apiCall('POST', '/patterns/memento/save', { sceneName: 'Demo Scene' });
                break;
            case 'observer':
                result = await apiCall('POST', '/patterns/observer/register', { deviceId: anyDeviceId || 'sensor-1', observerType: 'MOBILE' });
                break;
            case 'state':
                result = await apiCall('GET', '/patterns/state/demo');
                break;
            case 'strategy':
                result = await apiCall('POST', '/patterns/strategy/apply', { strategy: 'ECO' });
                break;
            case 'template':
                result = await apiCall('GET', '/patterns/template/demo', { deviceType: 'LIGHT' });
                break;
            case 'visitor':
                result = await apiCall('GET', '/patterns/visitor/audit', { type: 'SECURITY' });
                break;
            default:
                result = await apiCall('GET', '/patterns');
        }

        log(`Pattern Demo: ${patternId}`, result);
        return result;
    } catch (e) {
        log(`Pattern demo failed: ${patternId}`, { error: e.message });
        return null;
    }
}

function toParamsFromForm(form) {
    const data = new FormData(form);
    const params = {};
    const seen = new Set();
    for (const [key] of data.entries()) {
        seen.add(key);
    }
    for (const key of seen) {
        const values = data.getAll(key);
        params[key] = values.length > 1 ? values.join(',') : values[0];
    }
    return params;
}

document.addEventListener('DOMContentLoaded', () => {
    refreshDevices();
    if (getOutputEl()) {
        log('UI loaded. Open the Patterns Lab to run all demos.');
    }
});

document.addEventListener('click', (event) => {
    const deviceBtn = event.target.closest('.device-control-btn');
    if (deviceBtn?.dataset?.deviceId && deviceBtn?.dataset?.action) {
        controlDevice(deviceBtn.dataset.deviceId, deviceBtn.dataset.action);
        return;
    }

    const roomBtn = event.target.closest('.room-control-btn');
    if (roomBtn?.dataset?.room && roomBtn?.dataset?.action) {
        controlRoom(roomBtn.dataset.room, roomBtn.dataset.action);
        return;
    }

    const modeBtn = event.target.closest('.mode-btn');
    if (modeBtn?.dataset?.mode) {
        setHomeMode(modeBtn.dataset.mode);
        return;
    }

    const sceneBtn = event.target.closest('.scene-btn');
    if (sceneBtn?.dataset?.scene) {
        activateScene(sceneBtn.dataset.scene);
        return;
    }

    const patternBtn = event.target.closest('.pattern-run-btn');
    if (patternBtn?.dataset?.pattern) {
        runPatternDemo(patternBtn.dataset.pattern);
        return;
    }

    const patternItem = event.target.closest('.pattern-list-item');
    if (patternItem?.dataset?.pattern) {
        runPatternDemo(patternItem.dataset.pattern);
    }
});

document.addEventListener('submit', async (event) => {
    const form = event.target.closest('form[data-api-endpoint]');
    if (!form) return;
    event.preventDefault();

    const endpoint = form.dataset.apiEndpoint;
    const method = (form.dataset.apiMethod || 'GET').toUpperCase();
    const refresh = String(form.dataset.refresh || '').toLowerCase() === 'true';
    const params = toParamsFromForm(form);

    try {
        const result = await apiCall(method, endpoint, params);
        log(`${method} ${endpoint}`, result);
        await refreshDevices();
        if (refresh) {
            window.location.reload();
        }
    } catch (e) {
        log(`Request failed: ${method} ${endpoint}`, { error: e.message });
    }
});

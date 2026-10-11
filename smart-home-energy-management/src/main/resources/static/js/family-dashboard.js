// TODO (Backend): resolve the family account from the session; load that account's permissions and return only authorized devices/energy data.
const storageKeys = {
    devices: "smartHomeDemoDevices",
    permissions: "smartHomeDemoPermissions",
};

const starterDevices = [
    {
        id: 1,
        name: "Living room light",
        type: "Smart light",
        location: "Living room",
        mark: "L",
        isOn: true,
    },
];

function readStoredValue(key, fallback) {
    try {
        const value = localStorage.getItem(key);
        return value ? JSON.parse(value) : fallback;
    } catch {
        return fallback;
    }
}

function saveDevices() {
    try {
        localStorage.setItem(storageKeys.devices, JSON.stringify(devices));
    } catch {
        return;
    }
}

const devices = readStoredValue(storageKeys.devices, starterDevices);
const permissionDefaults = {
    familyAccount: "alex.morgan",
    canViewDevices: true,
    canControlDevices: true,
    canViewEnergy: true,
};
const familyAccountNames = {
    "alex.morgan": "Alex Morgan",
    "sam.lee": "Sam Lee",
};
const legacyAccountNames = {
    "alex.morgan@example.com": "alex.morgan",
    "sam.lee@example.com": "sam.lee",
    "family.member@example.com": "alex.morgan",
};
const storedPermissions = readStoredValue(storageKeys.permissions, permissionDefaults);
const permissions = {
    ...permissionDefaults,
    ...(storedPermissions && typeof storedPermissions === "object" ? storedPermissions : {}),
};
permissions.familyAccount = legacyAccountNames[permissions.familyAccount] || permissions.familyAccount;
const familyAccountDisplay = document.getElementById("familyAccountDisplay");
const energySummary = document.getElementById("energySummary");
const deviceSection = document.getElementById("deviceSection");
const deviceList = document.getElementById("deviceList");
const deviceCount = document.getElementById("deviceCount");
const deviceEmpty = document.getElementById("deviceEmpty");
const noDeviceAccess = document.getElementById("noDeviceAccess");

function renderEnergyHistory() {
    const now = new Date();
    const today = document.getElementById("energyDate");
    const localDate = new Date(now.getTime() - now.getTimezoneOffset() * 60000)
        .toISOString()
        .slice(0, 10);
    today.dateTime = localDate;
    today.textContent = `Today · ${new Intl.DateTimeFormat(undefined, {
        month: "short",
        day: "numeric",
    }).format(now)}`;

    const chartBars = Array.from(document.querySelectorAll(".energy-chart-bar"));
    const dayLabels = Array.from(document.querySelectorAll(".energy-chart-day"));
    const maxUsage = Math.max(...chartBars.map((bar) => Number(bar.dataset.kwh)));
    const readings = [];

    chartBars.forEach((bar, index) => {
        const usage = Number(bar.dataset.kwh);
        const date = new Date(now);
        date.setDate(now.getDate() - (chartBars.length - index - 1));
        const day = new Intl.DateTimeFormat(undefined, { weekday: "short" }).format(date);

        dayLabels[index].textContent = day;
        bar.style.height = `${Math.max(8, usage / maxUsage * 100)}%`;
        readings.push(`${day}: ${usage} kWh`);
    });

    document.querySelector(".energy-chart").setAttribute(
        "aria-label",
        `Sample daily energy use for the last seven days: ${readings.join(", ")}.`
    );
}

familyAccountDisplay.textContent = familyAccountNames[permissions.familyAccount]
    || permissions.familyAccount
    || "Family account";
energySummary.hidden = !permissions.canViewEnergy;
deviceSection.hidden = !permissions.canViewDevices;
noDeviceAccess.hidden = Boolean(permissions.canViewDevices);

function createDeviceRow(device) {
    const row = document.createElement("article");
    row.className = "device-row";

    const identity = document.createElement("div");
    identity.className = "device-identity";
    const mark = document.createElement("span");
    mark.className = "device-mark";
    mark.setAttribute("aria-hidden", "true");
    mark.textContent = device.mark;

    const description = document.createElement("div");
    const name = document.createElement("h3");
    name.textContent = device.name;
    const detail = document.createElement("p");
    detail.textContent = `${device.type} · ${device.location}`;
    description.append(name, detail);
    identity.append(mark, description);

    const controls = document.createElement("div");
    controls.className = "device-control";
    const state = document.createElement("span");
    state.className = "device-state";
    state.textContent = device.isOn ? "On" : "Off";
    controls.append(state);

    if (permissions.canControlDevices) {
        const toggle = document.createElement("button");
        toggle.className = "device-toggle";
        toggle.type = "button";
        toggle.setAttribute("role", "switch");
        toggle.setAttribute("aria-checked", String(device.isOn));
        toggle.setAttribute("aria-label", `Turn ${device.name} ${device.isOn ? "off" : "on"}`);
        const thumb = document.createElement("span");
        thumb.className = "toggle-thumb";
        thumb.setAttribute("aria-hidden", "true");
        toggle.append(thumb);
        toggle.addEventListener("click", () => {
            device.isOn = !device.isOn;
            saveDevices();
            renderDevices();
        });
        controls.append(toggle);
    }

    row.append(identity, controls);
    return row;
}

function renderDevices() {
    const visibleDevices = Array.isArray(devices) ? devices : [];
    deviceCount.textContent = `${visibleDevices.length} ${visibleDevices.length === 1 ? "device" : "devices"}`;
    deviceEmpty.hidden = visibleDevices.length > 0;
    deviceList.replaceChildren(...visibleDevices.map(createDeviceRow));
}

renderDevices();
renderEnergyHistory();
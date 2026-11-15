let houses = [];
let devices = [];
let permissions = {};



const familyHouse = document.getElementById("familyHouse");
const familyHouseChoice = document.getElementById("familyHouseChoice");
const familySingleHouse = document.getElementById("familySingleHouse");
const familyHouseName = document.getElementById("familyHouseName");
const familyAccountDetails = document.getElementById("familyAccountDetails");
const dashboardTitle = document.getElementById("dashboard-title");
const familyAccountDisplay = document.getElementById("familyAccountDisplay");
const familySignIn = document.getElementById("familySignIn");
const energySummary = document.getElementById("energySummary");
const deviceSection = document.getElementById("deviceSection");
const deviceList = document.getElementById("deviceList");
const deviceCount = document.getElementById("deviceCount");
const deviceEmpty = document.getElementById("deviceEmpty");
const noDeviceAccess = document.getElementById("noDeviceAccess");

let energyRequest;

async function loadFamilyDashboard() {
    const response = await fetch("/api/family/dashboard");
    if (response.status === 401) {
        familyAccountDisplay.textContent = "Your session has ended. Please sign in again.";
        familySignIn.hidden = false;
        return;
    }
    if (!response.ok) {
        throw new Error(`Could not load family dashboard (${response.status}).`);
    }

    const accountResponse = await fetch("/api/me", { cache: "no-store" });
    if (accountResponse.status === 401) {
        familyAccountDisplay.textContent = "Your session has ended. Please sign in again.";
        familySignIn.hidden = false;
        return;
    }
    if (!accountResponse.ok) {
        throw new Error(`Could not load account details (${accountResponse.status}).`);
    }
    const account = await accountResponse.json();
    if (typeof account.name !== "string" || !account.name.trim()) {
        throw new Error("Could not load account name.");
    }

    const dashboard = await response.json();
    houses = dashboard.map(house => ({
        ...house,
        devices: house.devices.map(device => ({
            id: device.device_id,
            name: device.device_name,
            type: device.device_type,
            location: house.houseName,
            mark: device.device_name.charAt(0).toUpperCase(),
            isOn: device.powered_state,
        })),
    }));

    dashboardTitle.textContent = `Hi ${account.name}`;
    familyAccountDisplay.textContent = houses.length === 0
        ? "Ask an administrator to add you to a house." : "";

    familyHouse.replaceChildren(
        ...houses.map(house => new Option(house.houseName, house.houseId))
    );
    familyHouse.disabled = houses.length === 0;
    familyHouseChoice.hidden = houses.length <= 1;
    familySingleHouse.hidden = houses.length > 1;
    familyHouseName.textContent = houses.length === 1 ? houses[0].houseName : "Unassigned";
    familyAccountDetails.hidden = false;
    showSelectedHouse();
}
async function loadEnergyHistory(houseId) {
    energyRequest?.abort();
    const request = new AbortController();
    energyRequest = request;

    const chart = energySummary.querySelector(".energy-chart");
    const reading = energySummary.querySelector(".energy-reading-detail");
    const status = document.getElementById("energyError");

    chart.hidden = true;
    reading.hidden = true;
    status.textContent = "Loading energy history…";

    try {
        const response = await fetch(`/api/family/houses/${houseId}/energy/daily`, {
            signal: request.signal
        });
        if (!response.ok) throw new Error(`Energy request failed (${response.status})`);

        const days = await response.json();
        if (request.signal.aborted) return;

        renderEnergyHistory(days);
        chart.hidden = false;
        reading.hidden = false;
        status.textContent = "";
    } catch (error) {
        if (request.signal.aborted) return;

        status.textContent = "Could not load energy history.";
        console.error(error);
    }
}

function renderEnergyHistory(days) {
    const chart = energySummary.querySelector(".energy-chart");
    const bars = chart.querySelectorAll("[data-energy-bar]");
    const labels = chart.querySelectorAll("[data-energy-label]");
    const values = chart.querySelectorAll("[data-energy-energy]");
    const maxKwh = Math.max(0, ...days.map(day => day.kwh));
    const readings = [];

    days.forEach(({ date, kwh }, index) => {
        const label = formatEnergyDate(date, { weekday: "short", day: "numeric" });
        const value = `${kwh.toFixed(2)} kWh`;

        bars[index].style.height = `${maxKwh > 0 ? (kwh / maxKwh) * 100 : 0}%`;
        labels[index].textContent = label;
        values[index].textContent = value;
        readings.push(`${label}: ${value}`);
    });

    const today = days[days.length - 1];
    document.getElementById("energyToday").textContent = today.kwh.toFixed(2);

    const energyDate = document.getElementById("energyDate");
    energyDate.dateTime = today.date;
    energyDate.textContent = formatEnergyDate(today.date, { month: "short", day: "numeric" });
    chart.setAttribute("aria-label", `Daily energy use: ${readings.join(", ")}.`);
}

function formatEnergyDate(date, options) {
    return new Date(`${date}T00:00:00Z`).toLocaleDateString("en-CA", {
        ...options,
        timeZone: "UTC"
    });
}

function showSelectedHouse() {
    const house = houses.find(house => String(house.houseId) === familyHouse.value);

    permissions = house ?? {};
    devices = house?.devices ?? [];

    energySummary.hidden = !permissions.canViewEnergy;
    deviceSection.hidden = !permissions.canViewDevices;
    noDeviceAccess.hidden = !house || Boolean(permissions.canViewDevices);

    renderDevices();
    if (house?.canViewEnergy) {
        loadEnergyHistory(house.houseId);
    } else {
        energyRequest?.abort();
    }
}


familyHouse.addEventListener("change", showSelectedHouse);
loadFamilyDashboard().catch(error => {
    familyAccountDisplay.textContent = error.message;
});

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
        toggle.disabled = Boolean(device.saving);

        toggle.addEventListener("click", async () => {
            const nextState = !device.isOn;
            device.saving = true;
            toggle.disabled = true;

            try {
                const response = await fetchWithCsrf(`/api/family/devices/${device.id}`, {
                    method: "PATCH",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({ powered_state: nextState }),
                });

                if (!response.ok) {
                    throw new Error(`Could not control this device (${response.status}).`);
                }

                device.isOn = nextState;
            } catch (error) {
                alert(error.message);
            } finally {
                device.saving = false;
                renderDevices();
            }
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

let houses = [];
let devices = [];
let permissions = {};

const familyHouse = document.getElementById("familyHouse");
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

function showSelectedHouse() {
    const house = houses.find(house => String(house.houseId) === familyHouse.value);

    permissions = house ?? {};
    devices = house?.devices ?? [];

    energySummary.hidden = !permissions.canViewEnergy;
    deviceSection.hidden = !permissions.canViewDevices;
    noDeviceAccess.hidden = Boolean(permissions.canViewDevices);

    renderDevices();
    if (permissions.canViewEnergy) renderEnergyHistory();
}

async function loadFamilyDashboard() {
    const response = await fetch("/api/family/dashboard");
    if (!response.ok) {
        throw new Error(`Could not load family dashboard (${response.status}).`);
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

    familyAccountDisplay.textContent =
        houses[0]?.memberName ?? "No house assigned to this account.";

    familyHouse.replaceChildren(
        ...houses.map(house => new Option(house.houseName, house.houseId))
    );
    familyHouse.disabled = houses.length === 0;
    showSelectedHouse();
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
renderEnergyHistory();
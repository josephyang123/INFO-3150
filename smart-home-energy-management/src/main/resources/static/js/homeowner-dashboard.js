// TODO (Backend): replace this single shared preview permission object with username-keyed records; persist changes and enforce access server-side.
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

function saveStoredValue(key, value) {
    try {
        localStorage.setItem(key, JSON.stringify(value));
        return true;
    } catch {
        return false;
    }
}

let devices = [];

async function loadDevices(){
    const response = await fetch("/api/devices");

    if(!response.ok){
        throw new Error("Failed to load devices");
    }

    devices = await response.json();


    console.log("Devices:", devices);
    renderDevices();
}

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
let familyPermissions = {
    ...permissionDefaults,
    ...(storedPermissions && typeof storedPermissions === "object" ? storedPermissions : {}),
};
if (legacyAccountNames[familyPermissions.familyAccount]) {
    familyPermissions.familyAccount = legacyAccountNames[familyPermissions.familyAccount];
}

const deviceList = document.getElementById("deviceList");
const deviceCount = document.getElementById("deviceCount");
const deviceEmpty = document.getElementById("deviceEmpty");
const addDeviceForm = document.getElementById("addDeviceForm");
const newDeviceType = document.getElementById("newDeviceType");
const permissionsForm = document.getElementById("permissionsForm");
const familyAccount = document.getElementById("familyAccount");
const familyCanView = document.getElementById("familyCanView");
const familyCanControl = document.getElementById("familyCanControl");
const familyCanViewEnergy = document.getElementById("familyCanViewEnergy");
const permissionStatus = document.getElementById("permissionStatus");

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

let nextDeviceId = devices.reduce((nextId, device) => Math.max(nextId, device.device_id + 1), 1);

function createDeviceRow(device) {
    const row = document.createElement("article");
    row.className = "device-row";

    const identity = document.createElement("div");
    identity.className = "device-identity";

    const mark = document.createElement("span");
    mark.className = "device-mark";
    mark.setAttribute("aria-hidden", "true");
    mark.textContent = device.device_name.charAt(0).toUpperCase();

    const description = document.createElement("div");
    const name = document.createElement("h3");
    name.textContent = device.device_name;
    const detail = document.createElement("p");
    detail.textContent = `${device.device_type} · ${device.manufacturer}`;
    description.append(name, detail);
    identity.append(mark, description);

    const controls = document.createElement("div");
    controls.className = "device-control";

    const state = document.createElement("span");
    state.className = "device-state";
    state.textContent = device.powered_state ? "On" : "Off";

    const toggle = document.createElement("button");
    toggle.className = "device-toggle";
    toggle.type = "button";
    toggle.setAttribute("role", "switch");
    toggle.setAttribute("aria-checked", String(device.powered_state));
    toggle.setAttribute("aria-label", `Turn ${device.device_name} ${device.powered_state ? "off" : "on"}`);
    const thumb = document.createElement("span");
    thumb.className = "toggle-thumb";
    thumb.setAttribute("aria-hidden", "true");
    toggle.append(thumb);
    toggle.addEventListener("click", async () => {
        device.powered_state = !device.powered_state;
        saveStoredValue(storageKeys.devices, devices);
        renderDevices();
    });
    //     try {
    //         const response = await fetch(`/api/devices/${device.device_id}`, {
    //             method: "PATCH",
    //             headers: {
    //                 "Content-Type": "application/json"
    //             },
    //             body: JSON.stringify({
    //                 isOn: !device.powered_state
    //             })
    //         });

    //         if (!response.ok) {
    //             throw new Error("Failed to update device");
    //         }

    //         await loadDevices();

    //     } catch (error) {
    //         alert(error.message);
    //     }
    // });

    const remove = document.createElement("button");
    remove.className = "remove-device";
    remove.type = "button";
    remove.textContent = "Remove";
    remove.setAttribute("aria-label", `Remove ${device.device_name}`);
    remove.addEventListener("click", async () => {
        devices = devices.filter((item) => item.device_id !== device.device_id);
        saveStoredValue(storageKeys.devices, devices);
        renderDevices();
    });
    //     try {
    //         const response = await fetch(`/api/devices/${device.device_id}`, {
    //             method: "DELETE"
    //         });

    //         if (!response.ok) {
    //             throw new Error("Failed to remove device");
    //         }

    //         await loadDevices();

    //     } catch (error) {
    //         alert(error.message);
    //     }
    // });

    controls.append(state, toggle, remove);
    row.append(identity, controls);
    return row;
}

function renderDevices() {
	deviceCount.textContent = `${devices.length} ${devices.length === 1 ? "device" : "devices"}`;
    deviceEmpty.hidden = devices.length > 0;
    deviceList.replaceChildren(...devices.map(createDeviceRow));
}

familyAccount.value = familyPermissions.familyAccount || permissionDefaults.familyAccount;
familyCanView.checked = Boolean(familyPermissions.canViewDevices);
familyCanControl.checked = Boolean(familyPermissions.canControlDevices);
familyCanViewEnergy.checked = Boolean(familyPermissions.canViewEnergy);
familyCanControl.disabled = !familyCanView.checked;

familyCanView.addEventListener("change", () => {
    if (!familyCanView.checked) {
        familyCanControl.checked = false;
    }
    familyCanControl.disabled = !familyCanView.checked;
});

permissionsForm.addEventListener("submit", (event) => {
    event.preventDefault();
    familyPermissions = {
        familyAccount: familyAccount.value,
        canViewDevices: familyCanView.checked,
        canControlDevices: familyCanView.checked && familyCanControl.checked,
        canViewEnergy: familyCanViewEnergy.checked,
    };
    const saved = saveStoredValue(storageKeys.permissions, familyPermissions);
    permissionStatus.textContent = saved
        ? `Permissions saved for ${familyAccountNames[familyAccount.value] || familyAccount.value}.`
        : "Could not save these preview settings in this browser.";
});

addDeviceForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    const deviceTypes = {
        light: { type: "Smart light", name: "Hallway light", location: "Hallway", mark: "L" },
        thermostat: { type: "Smart thermostat", name: "Hallway thermostat", location: "Hallway", mark: "T" },
        plug: { type: "Smart plug", name: "Living room plug", location: "Living room", mark: "P" },
    };
    const selectedType = deviceTypes[newDeviceType.value];
    const matchingDevices = devices.filter((device) => device.device_type === selectedType.type).length;

    devices.push({
        id: nextDeviceId++,
        ...selectedType,
        name: matchingDevices ? `${selectedType.name} ${matchingDevices + 1}` : selectedType.name,
        isOn: false,
    });
    saveStoredValue(storageKeys.devices, devices);
    renderDevices();
});
//     if (!selectedType) return;

//     try {
//         const response = await fetch("/api/devices", {
//             method: "POST",
//             headers: {
//                 "Content-Type": "application/json"
//             },
//             body: JSON.stringify({
//                 ...selectedType,
//                 isOn: false
//             })
//         });

//         if (!response.ok) {
//             throw new Error("Failed to add device");
//         }

//         await loadDevices();

//     } catch (error) {
//         alert(error.message);
//     }
// });

loadDevices().catch(error=>{
    console.error(error);
    alert("Could not load devices from the server.");
});
renderEnergyHistory();
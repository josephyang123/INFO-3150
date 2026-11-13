//Declare global variables to hold devices and family members
let devices = [];
let familyMembers = [];

//Call backend API to get family members
async function loadFamilyMembers() {
    const response = await fetch("/api/family-members");

    if(!response.ok){
        throw new Error(`Failed to load family members(${response.status})`);
    }

    familyMembers = await response.json();
    familyAccount.replaceChildren(
        ...familyMembers.map(({ value, label }) => new Option(label, value))
    );


    showSelectedPermissions();
   
}

//Call backend API to get devices and populate the device list. for showing existing devices
async function loadDevices(){
    const response = await fetch("/api/devices");

    if(!response.ok){
        throw new Error("Failed to load devices");
    }

    devices = await response.json();


    //console.log("Devices:", devices);
    renderDevices();
}

//Call backend API to get device types and populate the dropdown. for adding new devices
async function loadDeviceTypes() {
    const response = await fetch("/api/device-types");
    if (!response.ok) {
        throw new Error(`Could not load device types (${response.status}).`);
    }

    const types = await response.json();
    newDeviceType.replaceChildren(
        ...types.map(({ value, label }) => new Option(label, value))
    );
}

//Call backend API to get energy history and render the chart. for showing energy history
async function loadEnergyHistory() {
    const error = document.getElementById("energyError");

    try {
        const response = await fetch("/api/energy/daily");

        if (!response.ok) {
            throw new Error(`Energy request failed (${response.status})`);
        }

        const days = await response.json();
        renderEnergyHistory(days);
        error.textContent = "";
    } catch (err) {
        error.textContent = "Could not load energy history.";
        error.style.color = "red";
        console.error(err);
    }
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

//Render energy history chart
function renderEnergyHistory(days) {
    const bars = document.querySelectorAll("[data-energy-bar]");
    const labels = document.querySelectorAll("[data-energy-label]");
    const energy= document.querySelectorAll("[data-energy-energy]");
    const maxKwh = Math.max(0, ...days.map(day => day.kwh));

    days.forEach(({ date, kwh }, index) => {
        const height = maxKwh > 0 ? (kwh / maxKwh) * 100 : 0;
        bars[index].style.height = `${height}%`;
        // bars[index].textContent = ;
        labels[index].textContent = formatEnergyDate(date, { weekday: "short", day: "numeric" });
        energy[index].textContent = `${kwh.toFixed(2)} kWh`;
    });
    // console.log(
    //     {bars: bars.length,
    //      labels: labels.length,
    //      days: days.length,
    //      maxKwh: maxKwh
    //     }
    // )

    const today = days[days.length - 1];
    document.getElementById("energyToday").textContent = today.kwh.toFixed(2);
    document.getElementById("energyDate").textContent = formatEnergyDate(
        today.date,
        { month: "short", day: "numeric" }
    );
}
//Format date for energy history chart
function formatEnergyDate(date, options) {
    return new Date(`${date}T00:00:00Z`).toLocaleDateString("en-CA", {
        ...options,
        timeZone: "UTC"
    });

}

let nextDeviceId = devices.reduce((nextId, device) => Math.max(nextId, device.device_id + 1), 1);

//Create device cards
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
    name.textContent = device.device_name.charAt(0).toUpperCase() + device.device_name.slice(1);
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
        const poweredState = !device.powered_state;
        toggle.disabled = true;

        try {
            const response = await fetchWithCsrf(`/api/devices/${device.device_id}`, {
                method: "PATCH",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ powered_state: poweredState }),
            });

            if (!response.ok) {
                throw new Error(`Could not update device (${response.status}).`);
            }

            device.powered_state = poweredState;
            state.textContent = poweredState ? "On" : "Off";
            toggle.setAttribute("aria-checked", String(poweredState));
            toggle.setAttribute(
                "aria-label",
                `Turn ${device.device_name} ${poweredState ? "off" : "on"}`
            );
        } catch (error) {
            alert(error.message);
        } finally {
            toggle.disabled = false;
        }
    });


    const remove = document.createElement("button");
    remove.className = "remove-device";
    remove.type = "button";
    remove.textContent = "Remove";
    remove.setAttribute("aria-label", `Remove ${device.device_name}`);
    remove.addEventListener("click", async () => {
        remove.disabled = true;

         try {
            const response = await fetchWithCsrf(`/api/devices/${device.device_id}`, {
                method: "DELETE",
            });

            if (!response.ok) {
                throw new Error(`Could not remove device (${response.status}).`);
            }

            devices = devices.filter((item) => item.device_id !== device.device_id);
            renderDevices();
        } catch (error) {
            alert(error.message);
        } finally {
            remove.disabled = false;
        }
    });
       

    controls.append(state, toggle, remove);
    row.append(identity, controls);
    return row;
}

//Render device cards
function renderDevices() {
	deviceCount.textContent = `${devices.length} ${devices.length === 1 ? "device" : "devices"}`;
    deviceEmpty.hidden = devices.length > 0;
    deviceList.replaceChildren(...devices.map(createDeviceRow));
}
//Show selected permissions for family members
function showSelectedPermissions() {
    const member = familyMembers.find(
        member => String(member.value) === familyAccount.value
    );

    familyCanView.checked = member?.canViewDevices ?? false;
    familyCanControl.checked = member?.canControlDevices ?? false;
    familyCanViewEnergy.checked = member?.canViewEnergy ?? false;
}

familyAccount.addEventListener("change", showSelectedPermissions);



permissionsForm.addEventListener("submit", async (event) => {
    event.preventDefault();

    const member = familyMembers.find(
        member => String(member.value) === familyAccount.value
    );
    if (!member) return;

    const permissions = {
        canViewDevices: familyCanView.checked,
        canControlDevices: familyCanControl.checked,
        canViewEnergy: familyCanViewEnergy.checked,
    };

    if (permissions.canControlDevices && !permissions.canViewDevices) {
        permissionStatus.textContent = "Enable View devices to allow Control devices.";
        return;
    }

    const saveButton = permissionsForm.querySelector('button[type="submit"]');
    const controls = [
        saveButton, familyAccount, familyCanView, familyCanControl, familyCanViewEnergy,
    ];
    controls.forEach(control => { control.disabled = true; });
    permissionStatus.textContent = "Saving…";

    try {
        const response = await fetchWithCsrf(
            `/api/family-members/${member.value}/permissions`, {
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(permissions),
            }
        );

        if (!response.ok) {
            throw new Error(`Could not save permissions (${response.status}).`);
        }

        Object.assign(member, permissions);
        permissionStatus.textContent = `Permissions saved for ${member.label}.`;
    } catch (error) {
        permissionStatus.textContent = error.message;
    } finally {
        controls.forEach(control => { control.disabled = false; });
    }
});


//the button to add a new device 
addDeviceForm.addEventListener("submit", async (event) => {
    event.preventDefault();

    const fields = new FormData(addDeviceForm);
    const submitButton = event.submitter;
    if (submitButton) submitButton.disabled = true;

    try {
        const response = await fetchWithCsrf("/api/devices", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                device_name: fields.get("device_name").trim(),
                device_type: fields.get("device_type"),
                manufacturer: fields.get("manufacturer").trim(),
                power_watts: Number(fields.get("power_watts")),
            }),
        });

        if (!response.ok) {
            throw new Error(`Could not add device (${response.status}).`);
        }

        addDeviceForm.reset();
    } catch (error) {
        alert(error.message);
        return;
    } finally {
        if (submitButton) submitButton.disabled = false;
    }

    loadDevices().catch(() => {
        alert("Device added, but the list could not refresh. Reload the page.");
    });
});

loadDevices().catch(error=>{
    console.error(error);
    alert("Could not load devices from the server.");
});
loadDeviceTypes().catch(error => alert(error.message));

loadFamilyMembers().catch(error => {
    console.error(error);
    permissionStatus.textContent = error.message;
});

loadEnergyHistory();
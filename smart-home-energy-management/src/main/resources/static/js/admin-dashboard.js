const houseList = document.getElementById("houseList");
const memberList = document.getElementById("memberList");
const houseName = document.getElementById("houseName");
const firstOwner = document.getElementById("firstOwner");
const addMember = document.getElementById("addMember");
const newOwner = document.getElementById("newOwner");
const message = document.getElementById("adminMessage");
const retryLoad = document.getElementById("retryLoad");
const signInLink = document.getElementById("signInLink");
const createDialog = document.getElementById("createDialog");
const addDialog = document.getElementById("addDialog");
const transferDialog = document.getElementById("transferDialog");
const confirmation = document.getElementById("confirmation");

let houses = [];
let available = [];
let selectedHouseId = null;
let loaded = false;
let busy = false;
let pendingAction = null;

function showMessage(text, isError = false, canRetry = false, needsSignIn = false) {
    message.textContent = text;
    message.classList.toggle("is-error", isError);
    retryLoad.hidden = !canRetry;
    signInLink.hidden = !needsSignIn;
}

function selectedHouse() {
    return houses.find(house => house.houseId === selectedHouseId);
}

function updateButtons() {
    const house = selectedHouse();
    document.getElementById("openCreate").disabled = busy || !loaded || available.length === 0;
    document.getElementById("openAdd").disabled = busy || !loaded || available.length === 0;
    document.getElementById("openTransfer").disabled =
        busy || !loaded || !house || house.members.length + available.length === 0;
    retryLoad.disabled = busy;
    houseList.querySelectorAll("button").forEach(button => { button.disabled = busy; });
    memberList.querySelectorAll("button").forEach(button => { button.disabled = busy; });
    document.querySelectorAll(".admin-dialog button").forEach(button => { button.disabled = busy; });
}

function setBusy(value) {
    busy = value;
    updateButtons();
}

function fillSelect(select, choices, placeholder) {
    select.replaceChildren();
    const option = document.createElement("option");
    option.value = "";
    option.textContent = placeholder;
    select.append(option);
    for (const person of choices) {
        const choice = document.createElement("option");
        choice.value = person.userId;
        choice.textContent = `${person.name} (${person.email})`;
        select.append(choice);
    }
}

function render() {
    houseList.replaceChildren();
    memberList.replaceChildren();
    document.getElementById("houseCount").textContent =
        loaded ? `${houses.length} ${houses.length === 1 ? "house" : "houses"}` : "";
    document.getElementById("availabilityNote").hidden = !loaded || available.length > 0;

    if (loaded && houses.length === 0) {
        const empty = document.createElement("p");
        empty.className = "admin-empty";
        empty.textContent = "No houses yet. Add a house to get started.";
        houseList.append(empty);
    }
    for (const house of houses) {
        const button = document.createElement("button");
        button.type = "button";
        button.className = "admin-house";
        button.setAttribute("aria-label", `Manage ${house.name}`);
        button.setAttribute("aria-pressed", String(house.houseId === selectedHouseId));
        const name = document.createElement("span");
        name.className = "admin-house-name";
        name.textContent = house.name;
        const summary = document.createElement("span");
        summary.className = "admin-house-summary";
        summary.textContent = `${house.homeowner.name} - ${house.members.length} ${house.members.length === 1 ? "member" : "members"}`;
        button.append(name, summary);
        button.addEventListener("click", () => {
            selectedHouseId = house.houseId;
            showMessage("");
            render();
            houseList.querySelector('[aria-pressed="true"]').focus();
        });
        houseList.append(button);
    }

    fillSelect(firstOwner, available, "Select a homeowner");
    fillSelect(addMember, available, "Select an account");
    const house = selectedHouse();
    document.getElementById("managePrompt").hidden = Boolean(house);
    document.getElementById("managePrompt").textContent = loaded
        ? "Choose a house from the overview to get started."
        : "House data is not available.";
    document.getElementById("manageHouse").hidden = !house;
    document.getElementById("houseHeading").textContent = house ? house.name : "Select a house";
    if (house) {
        document.getElementById("ownerName").textContent = house.homeowner.name;
        document.getElementById("ownerEmail").textContent = house.homeowner.email;
        const eligible = [...house.members, ...available];
        document.getElementById("transferNote").hidden = eligible.length > 0;
        fillSelect(newOwner, eligible, "Select a new homeowner");
        if (house.members.length === 0) {
            const empty = document.createElement("p");
            empty.className = "admin-empty";
            empty.textContent = "No family members in this house yet.";
            memberList.append(empty);
        }
        for (const person of house.members) {
            const row = document.createElement("div");
            row.className = "admin-member";
            const details = document.createElement("div");
            const name = document.createElement("p");
            name.className = "admin-person-name";
            name.textContent = person.name;
            const email = document.createElement("p");
            email.className = "admin-person-email";
            email.textContent = person.email;
            const remove = document.createElement("button");
            remove.type = "button";
            remove.className = "action action-secondary admin-small-action";
            remove.textContent = "Remove";
            remove.setAttribute("aria-label", `Remove ${person.name}`);
            remove.addEventListener("click", () => {
                confirmAction("Remove family member",
                    `Remove ${person.name} from ${house.name}? Their account will remain and they will lose access to this house.`,
                    { type: "remove", houseId: house.houseId, personId: person.userId, personName: person.name, houseName: house.name });
            });
            details.append(name, email);
            row.append(details, remove);
            memberList.append(row);
        }
    }
    updateButtons();
}

class RequestError extends Error {
    constructor(status) {
        super(`Request failed (${status}).`);
        this.status = status;
    }
}

async function readJson(url) {
    const response = await fetch(url, { credentials: "same-origin", cache: "no-store" });
    if (!response.ok) {
        throw new RequestError(response.status);
    }
    return response.json();
}

async function loadData(successMessage = "") {
    try {
        const [newHouses, newAvailable] = await Promise.all([
            readJson("/api/admin/houses"),
            readJson("/api/admin/houses/unassigned-accounts"),
        ]);
        houses = newHouses;
        available = newAvailable;
        loaded = true;
        if (!houses.some(house => house.houseId === selectedHouseId)) {
            selectedHouseId = houses[0]?.houseId ?? null;
        }
        render();
        showMessage(successMessage);
        return true;
    } catch (error) {
        houses = [];
        available = [];
        loaded = false;
        render();
        const prefix = successMessage ? `${successMessage} ` : "";
        if (error.status === 401) {
            showMessage(`${prefix}Your session has ended. Please sign in again to refresh the data.`, true, false, true);
        } else if (error.status === 403) {
            showMessage(`${prefix}You do not have permission to load admin data.`, true, true);
        } else {
            showMessage(successMessage
                ? `${successMessage} Could not refresh the data. Refresh before making another change.`
                : "Could not load houses and accounts. Please refresh the data.", true, true);
        }
        return false;
    }
}

function confirmAction(title, text, action) {
    pendingAction = action;
    document.getElementById("confirm-title").textContent = title;
    document.getElementById("confirmationText").textContent = text;
    document.getElementById("confirmError").textContent = "";
    confirmation.showModal();
    document.getElementById("cancelAction").focus();
}

async function performAction(url, options, dialog, errorId, successMessage, onSuccess) {
    const errorElement = document.getElementById(errorId);
    errorElement.textContent = "";
    setBusy(true);
    try {
        const response = await fetchWithCsrf(url, options);
        if (!response.ok) {
            throw new RequestError(response.status);
        }
        if (onSuccess) {
            await onSuccess(response);
        }
        dialog.close();
        pendingAction = null;
        await loadData(successMessage);
    } catch (error) {
        if (error instanceof CsrfPreparationError) {
            errorElement.textContent = "Could not prepare the request. No changes were made. Please try again.";
        } else if (error.status === 400) {
            errorElement.textContent = "Please check your selections and try again.";
        } else if (error.status === 401 || error.status === 403) {
            dialog.close();
            loaded = false;
            houses = [];
            available = [];
            render();
            showMessage(error.status === 401
                ? "Your session has ended. Please sign in again."
                : "The action was denied. Check your access and try again.", true,
                error.status === 403, error.status === 401);
        } else if (error.status === 404 || error.status === 409) {
            dialog.close();
            pendingAction = null;
            await loadData("The data changed. Review the current details before trying again.");
            message.classList.add("is-error");
        } else {
            dialog.close();
            pendingAction = null;
            loaded = false;
            houses = [];
            available = [];
            render();
            showMessage("Could not confirm whether the change was saved. Refresh the data and check before trying again.", true, true);
        }
    } finally {
        setBusy(false);
    }
}

const jsonOptions = body => ({
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
});

document.getElementById("openCreate").addEventListener("click", () => {
    document.getElementById("createError").textContent = "";
    createDialog.showModal();
});
document.getElementById("openAdd").addEventListener("click", () => {
    document.getElementById("addError").textContent = "";
    addDialog.showModal();
});
document.getElementById("openTransfer").addEventListener("click", () => transferDialog.showModal());
document.getElementById("cancelCreate").addEventListener("click", () => createDialog.close());
document.getElementById("cancelAdd").addEventListener("click", () => addDialog.close());
document.getElementById("cancelTransfer").addEventListener("click", () => transferDialog.close());
document.getElementById("cancelAction").addEventListener("click", () => {
    pendingAction = null;
    confirmation.close();
});
confirmation.addEventListener("cancel", () => { if (!busy) pendingAction = null; });
document.querySelectorAll(".admin-dialog").forEach(dialog => {
    dialog.addEventListener("cancel", event => { if (busy) event.preventDefault(); });
});
houseName.addEventListener("input", () => houseName.setCustomValidity(""));

document.getElementById("createHouseForm").addEventListener("submit", event => {
    event.preventDefault();
    const name = houseName.value.trim();
    if (!name) {
        houseName.setCustomValidity("Enter a house name.");
        houseName.reportValidity();
        return;
    }
    const owner = available.find(person => person.userId === Number(firstOwner.value));
    if (busy) return;
    if (!owner) {
        document.getElementById("createError").textContent = "Select an available homeowner.";
        return;
    }
    performAction("/api/admin/houses", jsonOptions({ name, homeownerId: owner.userId }),
        createDialog, "createError", `${name} was added with ${owner.name} as homeowner.`,
        async response => {
            const result = await response.json();
            selectedHouseId = result.houseId;
            event.target.reset();
        });
});

document.getElementById("addMemberForm").addEventListener("submit", event => {
    event.preventDefault();
    const house = selectedHouse();
    const person = available.find(item => item.userId === Number(addMember.value));
    if (busy) return;
    if (!house || !person) {
        document.getElementById("addError").textContent = "Select an available account and house.";
        return;
    }
    performAction(`/api/admin/houses/${house.houseId}/members`, jsonOptions({ userId: person.userId }),
        addDialog, "addError", `${person.name} was added to ${house.name} with no permissions.`);
});

document.getElementById("transferForm").addEventListener("submit", event => {
    event.preventDefault();
    const house = selectedHouse();
    if (!house || busy) return;
    const person = [...house.members, ...available].find(item => item.userId === Number(newOwner.value));
    if (!person) {
        newOwner.setCustomValidity("Select an eligible homeowner.");
        newOwner.reportValidity();
        return;
    }
    transferDialog.close();
    confirmAction("Transfer ownership",
        `Make ${person.name} the homeowner of ${house.name}? ${house.homeowner.name} will remain a family member with no permissions. Both users must sign in again after their roles change.`,
        { type: "transfer", houseId: house.houseId, personId: person.userId, personName: person.name, houseName: house.name });
});
newOwner.addEventListener("change", () => newOwner.setCustomValidity(""));

document.getElementById("confirmAction").addEventListener("click", () => {
    if (!pendingAction || busy) return;
    const action = pendingAction;
    const base = `/api/admin/houses/${action.houseId}`;
    if (action.type === "remove") {
        performAction(`${base}/members/${action.personId}`, { method: "DELETE" }, confirmation,
            "confirmError", `${action.personName} was removed from ${action.houseName}; their account remains.`);
    } else {
        performAction(`${base}/owner`, jsonOptions({ userId: action.personId }), confirmation,
            "confirmError", `${action.personName} is now the homeowner of ${action.houseName}.`);
    }
});

retryLoad.addEventListener("click", async () => {
    setBusy(true);
    showMessage("Loading houses and accounts...");
    await loadData();
    setBusy(false);
});

setBusy(false);
loadData().finally(() => setBusy(false));

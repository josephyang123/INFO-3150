document.getElementById("logoutButton")
    .addEventListener("click", () => {
        logout().catch(error => alert(error.message));
    });
    
async function logout() {
    const csrfResponse = await fetch("/api/csrf", {
        credentials: "same-origin",
        cache: "no-store",
    });

    if (!csrfResponse.ok) {
        throw new Error("Could not prepare logout.");
    }

    const csrf = await csrfResponse.json();

    const response = await fetch("/api/logout", {
        method: "POST",
        credentials: "same-origin",
        headers: {
            [csrf.headerName]: csrf.token,
        },
    });

    if (!response.ok) {
        throw new Error("Logout failed.");
    }

    window.location.href = "/html/login.html";
}

async function fetchWithCsrf(url, options = {}) {
    const csrfResponse = await fetch("/api/csrf");
    if (!csrfResponse.ok) {
        throw new Error(`Could not get CSRF token (${csrfResponse.status}).`);
    }

    const csrf = await csrfResponse.json();
    const headers = new Headers(options.headers);
    headers.set(csrf.headerName, csrf.token);

    return fetch(url, { ...options, headers });
}



document.getElementById("logoutButton")
    .addEventListener("click", () => {
        logout().catch(error => alert(error.message));
    });
    
async function logout() {

    const response = await fetchWithCsrf("/api/logout", {
        method: "POST",
        
    });

    if (!response.ok) {
        throw new Error("Logout failed.");
    }

    window.location.href = "/html/login.html";
}

class CsrfPreparationError extends Error {
    constructor(cause) {
        super(cause instanceof Error ? cause.message : String(cause), { cause });
    }
}

async function fetchWithCsrf(url, options = {}) {
    let headers;
    try {
        const csrfResponse = await fetch("/api/csrf");
        if (!csrfResponse.ok) {
            throw new Error(`Could not get CSRF token (${csrfResponse.status}).`);
        }

        const csrf = await csrfResponse.json();
        if (typeof csrf?.headerName !== "string" || !csrf.headerName
                || typeof csrf.token !== "string" || !csrf.token) {
            throw new Error("Invalid CSRF token response.");
        }
        headers = new Headers(options.headers);
        headers.set(csrf.headerName, csrf.token);
    } catch (error) {
        throw new CsrfPreparationError(error);
    }
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

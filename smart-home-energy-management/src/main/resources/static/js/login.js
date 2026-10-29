const form = document.getElementById("loginForm");
const loginError = document.getElementById("loginError");
const submitButton = form.querySelector('button[type="submit"]');

form.addEventListener("submit", async (event) => {
	event.preventDefault();
	loginError.textContent = "";
	submitButton.disabled = true;
	submitButton.textContent = "Signing in...";

	const email = document.getElementById("email").value.trim();
	const password = document.getElementById("password").value;

	
	try{

		const csrfResponse = await fetch("/api/csrf", {
			credentials:"same-origin",
			cache:"no-store",
		});

		if (!csrfResponse.ok) {
            throw new Error("Could not prepare login. Please try again.");
        }

        const csrf = await csrfResponse.json();


		const response = await fetch("/api/login", {
            method: "POST",
            credentials: "same-origin",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded",
                [csrf.headerName]: csrf.token,
            },
            body: new URLSearchParams({ email, password }),
        });

        if (response.status === 401) {
            throw new Error("Invalid email or password.");
        }

        if (response.status === 403) {
            throw new Error("Login security check failed. Please try again.");
        }

        if (!response.ok) {
            throw new Error("Login failed. Please try again.");
        }
		
		const userResponse = await fetch("/api/me", {
            credentials: "same-origin",
            cache: "no-store",
        });

        if (!userResponse.ok) {
            throw new Error("Could not confirm your login. Please try again.");
        }

        const user = await userResponse.json();

        const dashboards = {
            ADMIN: "/html/dashboard.html",
            FAMILY: "/html/family-dashboard.html",
            HOMEOWNER: "/html/homeowner-dashboard.html",
        };

        const destination = dashboards[user.role];

        if (!destination) {
            throw new Error("Your account has an unexpected role.");
        }

        window.location.href = destination;

	}catch (error) {
		loginError.textContent = error.message;
	}finally {
		submitButton.disabled = false;
		submitButton.textContent = "Sign In";
	}
		
		


});



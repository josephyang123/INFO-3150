const form = document.getElementById("loginForm");
const loginError = document.getElementById("loginError");
const submitButton = form.querySelector('button[type="submit"]');

form.addEventListener("submit", async (event) => {
	event.preventDefault();
	loginError.textContent = "";
	submitButton.disabled = true;
	submitButton.textContent = "Signing in...";

	const email = document.getElementById("email").value.trim();
	const password = document.getElementById("password").value.trim();

	// try {
		// TODO: Connect to the agreed login endpoint and route using its authenticated role.
		const response = await fetch("/api/login", {
			method: "POST",
			headers: {
				"Content-Type": "application/json",
			},
			body: JSON.stringify({ email, password }),
		});
		// await authenticate(email, password);

		

		console.log(response.status);
		console.log(response.ok);
		console.log(response.url);
		console.log(response.redirected);



		if (!response.ok) {
			loginError.textContent = "Invalid email or password. Please try again.";
			submitButton.disabled = false;
			submitButton.textContent = "Log in";
			throw new Error("Invalid credentials");
		}
		else{

			submitButton.disabled = true;
			window.location.href="/html/family-dashboard.html";
		}
		

			// Redirect based on the user's role
	// } catch (error) {
	// 	loginError.textContent = error.message;
	// 	console.log(error);
	// } finally {
	// 	submitButton.disabled = false;
	// 	submitButton.textContent = "Log in";
	// }
});

// async function authenticate(email, password) {
// 	// TODO: Implement the agreed request, session handling, and role-to-dashboard mapping.
// 	void email;
// 	void password;
// 	throw new Error("Login is not connected yet.");
// }

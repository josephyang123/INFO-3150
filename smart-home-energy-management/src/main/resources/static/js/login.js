const form = document.getElementById("loginForm");
const loginError = document.getElementById("loginError");
const submitButton = form.querySelector('button[type="submit"]');

form.addEventListener("submit", async (event) => {
	event.preventDefault();
	loginError.textContent = "";
	submitButton.disabled = true;
	submitButton.textContent = "Signing in...";

	const username = document.getElementById("username").value.trim();
	const password = document.getElementById("password").value;

	try {
		// TODO: Connect to the agreed login endpoint and route using its authenticated role.
		await authenticate(username, password);
	} catch (error) {
		loginError.textContent = error.message;
	} finally {
		submitButton.disabled = false;
		submitButton.textContent = "Log in";
	}
});

async function authenticate(username, password) {
	// TODO: Implement the agreed request, session handling, and role-to-dashboard mapping.
	void username;
	void password;
	throw new Error("Login is not connected yet.");
}

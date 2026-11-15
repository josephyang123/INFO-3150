const form = document.getElementById("registerForm");
const submitButton = form.querySelector('button[type="submit"]');
const successDialog = document.getElementById("registrationSuccess");

successDialog.addEventListener("cancel", (event) => event.preventDefault());

form.addEventListener("submit", async (event) => {
    event.preventDefault();

    //capture input values from the form fields
    const username = document.getElementById("username").value.trim();
    const email = document.getElementById("email").value.trim();
    const password = document.getElementById("password").value.trim();
    const confirmPassword = document.getElementById("confirmPassword").value.trim();

    //declare username, email, password, and confirmPassword error messages
    document.getElementById("usernameError").textContent = "";
    document.getElementById("emailError").textContent = "";
    document.getElementById("passwordError").textContent = "";
    document.getElementById("confirmPasswordError").textContent = "";
    document.getElementById("registerError").textContent = "";

    let isValid = true;
    if(!username) {
        document.getElementById("usernameError").textContent = "Username is required.";
        isValid = false;
    }

    if(!email) {
        document.getElementById("emailError").textContent = "Email is required.";
        isValid = false;
    }

    if(!password) {
        document.getElementById("passwordError").textContent = "Password is required.";
        isValid = false;
    } else if (password.length < 8) {
        document.getElementById("passwordError").textContent = "Password must be at least 8 characters long.";
        isValid = false;
    }
    if(!confirmPassword) {
        document.getElementById("confirmPasswordError").textContent = "Confirm Password is required.";
        isValid = false;
    }else if(password !== confirmPassword) {
        document.getElementById("confirmPasswordError").textContent = "Passwords do not match.";
        isValid = false;
    }

    if (!isValid) {
        return;
    }

    submitButton.disabled = true;
    try {
        const csrfResponse = await fetch("/api/csrf", {
            credentials: "same-origin",
            cache: "no-store",
        });
        if (!csrfResponse.ok) {
            throw new Error("Could not prepare registration. Please try again.");
        }
        const csrf = await csrfResponse.json();

        const response = await fetch("/api/register", {
            method: "POST",
            credentials: "same-origin",
            headers: {
                "Content-Type": "application/json",
                [csrf.headerName]: csrf.token,
            },
            body: JSON.stringify({ username, email, password }),
        });

        if (response.status === 403) {
            throw new Error("Registration security check failed. Please try again.");
        }
        if (response.status === 400 && response.headers.get("content-type")?.includes("application/json")) {
            const errorData = await response.json();
            if (errorData.code === "EMAIL_ALREADY_EXISTS") {
                document.getElementById("emailError").textContent = "Email already exists.";
                return;
            }
        }
        if (!response.ok) {
            throw new Error("Registration failed. Check your details and try again.");
        }

        successDialog.showModal();
    } catch (error) {
        console.error("Registration failed:", error);
        document.getElementById("registerError").textContent = "Registration failed. Please try again.";
    } finally {
        submitButton.disabled = false;
    }
});
const form = document.getElementById("registerForm");

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

    const response = await fetch("/api/register", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify({ username, email, password }),
    });

    if (response.ok) {
        alert("Registration successful! Redirecting to login page...");
        document.getElementById("registerError").textContent = "Registration successful! Redirecting to login page...";
        window.location.href = "/login";
    } else {
        
        const errorData = await response.json();
        alert(`Registration failed: ${errorData.message}`);
        console.error(`Registration failed: ${errorData.message}`);
        document.getElementById("registerError").textContent = `Registration failed: ${errorData.message}`;
    }
});
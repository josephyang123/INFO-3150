# INFO-3150 Smart Home Energy Management System

An INFO 3150 team prototype for managing smart-home devices and viewing energy use.

## Run locally

The Spring Boot application is in `smart-home-energy-management/` and targets Java 21.

1. Start a local MySQL server on `localhost:3306`.
2. Run `smart-home-energy-management/database/schema.sql` in MySQL to create the `smart_db` database and its table.
3. Create `smart-home-energy-management/src/main/resources/application.properties` locally with the datasource URL, username, and password. This file is ignored by Git; do not commit credentials.
4. From `smart-home-energy-management/`, run:

   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

The app serves static pages on port 8080:

- Welcome: `/html/index.html`
- Login: `/html/login.html`
- Registration: `/html/register.html`
- Dashboard entry: `/html/dashboard.html`
- Homeowner dashboard: `/html/homeowner-dashboard.html`
- Family dashboard: `/html/family-dashboard.html`

## Frontend prototype

The frontend uses shared HTML/CSS styling and page-specific vanilla JavaScript. The homeowner dashboard demonstrates a sample device list, device controls, family permission controls, and a sample seven-day energy chart. The family dashboard reflects the saved preview permissions. Device state, permissions, and energy readings are demo data stored in browser `localStorage`; they are not hardware readings or database-backed dashboard records.

Registration is the only current frontend API call: `POST /api/register` with JSON fields `username`, `email`, and `password`. The current backend requires email and a password of at least eight characters. Document frontend API calls in the team's Discord `#coding-related` channel when they are integrated.

## Current limitations

- Login is not connected to a backend endpoint, so credentials are not authenticated and users are not routed by role.
- Dashboard pages are static and directly accessible. The preview permissions do not provide security; backend authentication and authorization are still required.
- The homeowner preview stores one shared family-permission object, not separate permission records for each family account.
- The `Sign Out` link returns to the welcome page but does not invalidate a session; session logout must be implemented with authentication.
- The energy chart and device state are simulated. No physical devices or energy sensors are connected.

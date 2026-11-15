# INFO-3150 Smart Home Energy Management System

An INFO 3150 team prototype for managing smart-home devices and viewing energy use.

> **Work in progress:** revisit setup instructions and limitations before the second submission as the remaining features are completed.

## Run locally

The Spring Boot application is in `smart-home-energy-management/` and targets Java 21.

1. Start a local MySQL server on `localhost:3306`.
2. For a **fresh** installation, run `smart-home-energy-management/database/schema.sql` in MySQL to create the `smart_db` database and tables. It starts without sample accounts, houses, or devices. Do not run it against an existing database; it is not a migration.
3. Create `smart-home-energy-management/src/main/resources/application.properties` locally with the datasource URL, username, and password. This file is ignored by Git; do not commit credentials. For example:

   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/smart_db
   spring.datasource.username=YOUR_MYSQL_USERNAME
   spring.datasource.password=YOUR_MYSQL_PASSWORD
   spring.jpa.hibernate.ddl-auto=none
   spring.sql.init.mode=never
   ```

4. From `smart-home-energy-management/`, run:

   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

Existing databases need a separately reviewed migration; preserve their data.

Registration creates a FAMILY account, not an ADMIN. First-admin provisioning is a manual database operation; obtain the local-only guide from a teammate. Do not add shared passwords to the repository.

The app serves static pages on port 8080:

- Welcome: `/html/index.html`
- Login: `/html/login.html`
- Registration: `/html/register.html`
- Admin dashboard: `/html/admin-dashboard.html`
- Homeowner dashboard: `/html/homeowner-dashboard.html`
- Family dashboard: `/html/family-dashboard.html`

## Frontend

The frontend uses shared HTML/CSS styling and page-specific vanilla JavaScript. Registration, login, logout, and the admin, homeowner, and family dashboards use backend endpoints. Accounts register with a username, email, and password of at least eight characters. The authenticated `/api/me` response includes the account name and role. The registration form removes spaces at the beginning and end of passwords; login accepts those extra spaces while still supporting existing passwords that include them.

## Admin house creation

Admins can read house names, homeowners, and members with `GET /api/admin/houses`. Each house includes `houseId`, `name`, `homeowner`, and `members`; people contain `userId`, `name`, and `email`. `GET /api/admin/houses/unassigned-accounts` returns unassigned family accounts in the same person format. Both endpoints require an admin session and return an empty list when there are no results.

The backend accepts `POST /api/admin/houses` with an admin session, a CSRF token, and JSON such as `{"name":"Maple House","homeownerId":123}`. The homeowner must be an unassigned family account. A successful response is `201` with `{"houseId":1}`; house creation and promotion occur in one transaction. The admin dashboard uses the admin endpoints.

Admins can also add a family account with `POST /api/admin/houses/{houseId}/members` and `{"userId":123}`, or remove one with `DELETE /api/admin/houses/{houseId}/members/{userId}`. Both require a CSRF token. New members start with no permissions; removal keeps the account.

Ownership transfers use `POST /api/admin/houses/{houseId}/owner` with `{"userId":123}` and a CSRF token. The new owner must be an unassigned family account or a member of that house. The previous owner remains a family member without permissions, and affected users must sign in again.

The backend requires the `user_accounts.session_version` column. Existing databases need a separate migration before running this version.

## Current limitations

- Access-denied messaging and the reported missing-device behavior need review.
- No physical devices or energy sensors are connected.

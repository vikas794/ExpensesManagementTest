# Expense Manager Application

Last updated: 2025-05-28

## Description

This project is a Java-based backend application for managing user expenses. It allows users to register, login, and manage their personal expenses. Session-based authentication is used to ensure users can only access and manage their own data.

## Tech Stack

- Java 17
- Spring Boot 3.5.0
- Spring Security (with session-based authentication)
- Spring Data JPA
- H2 Database (for development and testing)
- Lombok
- Maven (Build Tool)
- JaCoCo (Code Coverage)

## Prerequisites

- Java Development Kit (JDK) 17 or later
- Apache Maven 3.6.x or later

## How to Build and Run

1.  **Clone the repository:**
    ```bash
    git clone <repository-url>
    cd expense-manager
    ```

2.  **Build the application using Maven:**
    ```bash
    mvn clean install
    ```
    This command will compile the code, run tests, and package the application into a JAR file located in the `target` directory (e.g., `expense-manager-0.0.1-SNAPSHOT.jar`).

3.  **Run the application:**
    ```bash
    java -jar target/expense-manager-0.0.1-SNAPSHOT.jar
    ```
    The application will start on the default port (usually 8080).

4.  **Accessing H2 Console (for development):**
    -   Once the application is running, you can access the H2 database console in your browser.
    -   URL: `http://localhost:8080/h2-console`
    -   JDBC URL: `jdbc:h2:mem:testdb` (This is the default if not overridden in `application.properties`)
    -   Username: `sa`
    -   Password: (leave blank by default)

## Authentication Flow

1.  **Register a new user:**
    -   Send a `POST` request to `/api/auth/register` with the user's details (username, password, email).
2.  **Login:**
    -   Send a `POST` request to `/api/auth/login` with `username` and `password` as form data or JSON.
    -   Upon successful authentication, a `JSESSIONID` cookie will be returned and used for session management. This cookie must be included in subsequent requests to access protected endpoints.
3.  **Logout:**
    -   Send a `POST` request to `/api/auth/logout`. The session will be invalidated.

## API Endpoints

All endpoints are prefixed with `/api`.

### Authentication (`/auth`)

-   **`POST /auth/register`**: Register a new user.
    -   Request Body: `UserRegistrationDto`
        ```json
        {
            "username": "testuser",
            "password": "password123",
            "email": "test@example.com"
        }
        ```
    -   Response: `UserViewDto` (Status 201 CREATED)

-   **`POST /auth/login`**: Login an existing user.
    -   Request Body (form-data or x-www-form-urlencoded): `username=<your_username>&password=<your_password>`
    -   Response: Varies by configuration, typically redirects or success status. Session cookie `JSESSIONID` is set.

-   **`POST /auth/logout`**: Logout the current user.
    -   Response: Success message (Status 200 OK)

### Users (`/users`)

-   **`GET /users/profile`**: Get the profile of the currently authenticated user.
    -   Requires Authentication.
    -   Response: `UserViewDto` (Status 200 OK)

### Expenses (`/expenses`)

-   **`POST /expenses`**: Add a new expense for the authenticated user.
    -   Requires Authentication.
    -   Request Body: `CreateExpenseDto`
        ```json
        {
            "description": "Lunch with colleagues",
            "amount": 25.50,
            "date": "YYYY-MM-DD",
            "category": "Food"
        }
        ```
    -   Response: `ExpenseDto` (Status 201 CREATED)

-   **`GET /expenses`**: View all expenses for the authenticated user.
    -   Requires Authentication.
    -   Response: `List<ExpenseDto>` (Status 200 OK)

-   **`GET /expenses/{id}`**: View a specific expense by its ID.
    -   Requires Authentication. User can only view their own expenses.
    -   Response: `ExpenseDto` (Status 200 OK)

-   **`PUT /expenses/{id}`**: Update an existing expense by its ID.
    -   Requires Authentication. User can only update their own expenses.
    -   Request Body: `CreateExpenseDto` (same structure as for creating an expense)
    -   Response: `ExpenseDto` (Status 200 OK)

-   **`DELETE /expenses/{id}`**: Delete an expense by its ID.
    -   Requires Authentication. User can only delete their own expenses.
    -   Response: (Status 204 NO_CONTENT)

## Sample Requests (using curl)

Replace `<JSESSIONID_COOKIE_VALUE>` with the actual cookie value obtained after login.

**1. Register User:**
```bash
curl -X POST -H "Content-Type: application/json" -d '{"username":"newuser", "password":"newpassword123", "email":"new@example.com"}' http://localhost:8080/api/auth/register
```

**2. Login User (form data):**
```bash
curl -X POST -c cookies.txt -d "username=newuser&password=newpassword123" http://localhost:8080/api/auth/login
```
*(The `-c cookies.txt` saves the session cookie to a file named `cookies.txt`)*

**3. Get User Profile:**
```bash
curl -X GET -b cookies.txt http://localhost:8080/api/users/profile
```
*(The `-b cookies.txt` sends cookies from the file)*

**4. Add New Expense:**
```bash
curl -X POST -b cookies.txt -H "Content-Type: application/json" -d '{"description":"Coffee", "amount":3.75, "date":"YYYY-MM-DD", "category":"Drinks"}' http://localhost:8080/api/expenses
```

**5. View All Expenses:**
```bash
curl -X GET -b cookies.txt http://localhost:8080/api/expenses
```

**6. Logout:**
```bash
curl -X POST -b cookies.txt http://localhost:8080/api/auth/logout
```

## Error Responses

The API uses a standardized error response format:
```json
{
    "timestamp": "YYYY-MM-DDTHH:mm:ss.ssssss",
    "message": "Error message summary",
    "details": "Request URI or specific error cause",
    "validationErrors": { // Only present for validation failures (HTTP 400)
        "fieldName1": "Error message for fieldName1",
        "fieldName2": "Error message for fieldName2"
    }
}
```
Common HTTP Status Codes:
- `200 OK`: Request successful.
- `201 CREATED`: Resource created successfully.
- `204 NO_CONTENT`: Request successful, no response body.
- `400 BAD_REQUEST`: Invalid input (e.g., validation errors).
- `401 UNAUTHORIZED`: Authentication failed or required.
- `403 FORBIDDEN`: Authenticated user does not have permission.
- `404 NOT_FOUND`: Resource not found.
- `409 CONFLICT`: Resource already exists (e.g., username/email conflict).
- `500 INTERNAL_SERVER_ERROR`: Unexpected server error.

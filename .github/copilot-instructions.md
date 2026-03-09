# Copilot Instructions – HRM Project

## Technical Stack

- **Backend:** Spring Boot with Java 21
- **Frontend:** Angular with TypeScript
- **Build Tool:** Gradle
- **Databases:** PostgreSQL (production), H2 (development/testing)
- **Database Versioning:** Flyway

---

## Java / Spring Boot Best Practices

### General

- Use Java 21 features: records, sealed classes, pattern matching, virtual threads, and text blocks where appropriate.
- Follow standard Java naming conventions: `camelCase` for variables and methods, `PascalCase` for classes, `UPPER_SNAKE_CASE` for constants.
- Prefer immutable objects and records for DTOs and value objects.
- Never return `null` from public methods; use `Optional` for values that may be absent.
- Use `var` for local variables only when the type is obvious from the right-hand side.

### Spring Boot

- Use constructor injection for all Spring-managed beans; avoid field injection.
- Annotate service classes with `@Service`, repositories with `@Repository`, and controllers with `@RestController`.
- Use `@Transactional` at the service layer, not at the controller or repository layer.
- Externalize all configuration using `application.yml`; never hard-code environment-specific values.
- Use Spring Profiles (`dev`, `test`, `prod`) to manage environment-specific configuration.
- Use `ResponseEntity` for REST controller return types to explicitly control HTTP status codes.
- Apply global exception handling with `@RestControllerAdvice` and custom exception classes.
- Validate all incoming request bodies using Bean Validation annotations (`@NotNull`, `@Size`, `@Valid`, etc.).

### API Design

- Follow RESTful conventions: use nouns for resource URIs, HTTP methods for actions.
- Version APIs via the URI path (e.g., `/api/v1/employees`).
- Return consistent response structures with appropriate HTTP status codes.
- Use pagination for all list endpoints (`Page` / `Pageable`).

### Database & Persistence

- Use Spring Data JPA repositories for data access.
- Write Flyway migration scripts in `src/main/resources/db/migration` following the naming convention `V{version}__{description}.sql`.
- Never modify or delete an existing Flyway migration script; always create a new migration.
- Use the H2 profile for local development and tests; use PostgreSQL for staging and production.
- Define entity relationships explicitly with proper cascade types and fetch strategies.
- Avoid lazy-loading issues by using DTOs or projections for API responses instead of returning entities directly.

### Logging

- Use SLF4J (`@Slf4j` via Lombok or manual logger creation) for all logging.
- Log at appropriate levels: `ERROR` for failures, `WARN` for recoverable issues, `INFO` for significant events, `DEBUG` for diagnostic detail.
- Never log sensitive data such as passwords, tokens, or personal identifiable information.

### Security

- Never commit secrets, passwords, or API keys into source code.
- Use Spring Security for authentication and authorization.
- Sanitize and validate all user inputs to prevent injection attacks.

---

## Angular Best Practices

### General

- Use strict TypeScript (`strict: true` in `tsconfig.json`).
- Follow the Angular style guide for file naming: `feature-name.component.ts`, `feature-name.service.ts`, etc.
- Use standalone components where possible; minimize the use of NgModules.
- Use reactive forms (`ReactiveFormsModule`) over template-driven forms for complex forms.
- Keep components small and focused; extract reusable logic into services or utility functions.

### Component Design

- Use `OnPush` change detection strategy for all components to improve performance.
- Use `input()` and `output()` signal-based APIs for component communication (Angular 17+).
- Avoid business logic in components; delegate to services.
- Unsubscribe from all observables in `ngOnDestroy` or use the `async` pipe / `takeUntilDestroyed()`.

### State Management

- Use Angular services with RxJS `BehaviorSubject` or Angular Signals for local state management.
- Use a state management library (e.g., NgRx) only when application complexity warrants it.

### HTTP & API Communication

- Use `HttpClient` for all API calls; centralize API base URLs in environment files.
- Use HTTP interceptors for attaching auth tokens, handling errors globally, and logging.
- Define TypeScript interfaces or types for all API request and response models.

### Routing

- Use lazy loading for all feature modules to reduce initial bundle size.
- Use route guards for authentication and authorization checks.

### Styling

- Use SCSS for stylesheets.
- Follow a consistent naming convention (e.g., BEM) for CSS classes.
- Use Angular Material or a design system for consistent UI components.

### Security

- Never trust user input; sanitize content displayed using Angular's built-in XSS protection.
- Never bypass Angular's `DomSanitizer` unless absolutely necessary and reviewed.
- Store tokens securely and never expose them in URLs or local storage without encryption.

---

## Code Coverage Requirements

- **Target: 100% code coverage** for all new code across both backend and frontend.
- Every public class, method, and branch must have corresponding unit tests.
- Use **JUnit 5** and **Mockito** for Java backend unit tests.
- Use **Jest** with **Jasmine** for Angular frontend unit tests.
- Write integration tests for all REST API endpoints using `@SpringBootTest` and `MockMvc` or `WebTestClient`.
- Write end-to-end tests for critical user workflows using **Cypress** or **Playwright**.
- Do not merge any pull request with coverage below 100% for new or changed lines.
- Generate coverage reports with **JaCoCo** (backend) and **Istanbul/nyc** (frontend); configure CI pipelines to enforce thresholds.
- Test edge cases, error paths, and boundary conditions — not just the happy path.

---

## General Guidelines

- Write clean, self-documenting code; add comments only when the intent is not obvious from the code itself.
- Follow the single responsibility principle in all classes, methods, and components.
- Keep methods short (aim for under 20 lines); extract helper methods when complexity grows.
- Use meaningful and descriptive names for variables, methods, classes, and files.
- Perform code reviews for every pull request; ensure all review comments are addressed before merging.
- Use Git commit messages that follow the Conventional Commits format (e.g., `feat:`, `fix:`, `docs:`, `test:`).

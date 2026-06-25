# Job Application Tracker

A personal Kanban-style job application tracker with an AI-powered "generate kit"
feature (cover letter, tailored resume, interview questions, company brief) backed
by the Claude API.


## Prerequisites

- Java 17+
- Maven (or use the included `./mvnw` wrapper — no separate install needed)
- A Claude API key from [console.anthropic.com](https://console.anthropic.com/) (only required once AI features are wired up; the app boots fine without it)

## Configuration

The app reads your Claude API key from the `ANTHROPIC_API_KEY` environment variable.

```bash
export ANTHROPIC_API_KEY=sk-ant-...
```

Other settings (model name, max tokens, DB location) live in
[application.properties](src/main/resources/application.properties) and can be
overridden via environment variables if needed.

## Running the app

From the project root:

```bash
./mvnw spring-boot:run
```

The first run will create a local H2 database file under `./data/jobtracker.mv.db`
(this directory is gitignored).

Once you see `Started JobtrackerApplication`, open your browser to:

- **Board**: http://localhost:8083/
- **H2 console** (for local DB inspection): http://localhost:8083/h2-console
  - JDBC URL: `jdbc:h2:file:./data/jobtracker`
  - User: `sa`, Password: *(blank)*

To stop the app, press `Ctrl+C` in the terminal.

## Running tests

```bash
./mvnw test
```

This runs the full JUnit test suite and enforces a minimum of 80% line and branch
coverage via JaCoCo. A coverage report is generated at
`target/site/jacoco/index.html`.

## Building a jar

```bash
./mvnw clean package
java -jar target/jobtracker-0.0.1-SNAPSHOT.jar
```

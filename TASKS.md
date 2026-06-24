# Job Application Tracker — Implementation Tasks

Status legend: `TODO` | `IN PROGRESS` | `DONE` | `BLOCKED`

## Phase 1: Project Scaffold
| # | Task | Status |
|---|---|---|
| 1.1 | Generate Spring Boot 3.x project (Java 17) via Spring Initializr — deps: Web, Data JPA, Thymeleaf, Validation, H2 | DONE |
| 1.2 | Add extra dependencies to `pom.xml`: Jsoup, Apache PDFBox, Flexmark, Anthropic Java SDK (or WebClient-based client) | DONE (Jsoup, PDFBox, Flexmark added; no official Anthropic Java SDK exists — will use Spring's `RestClient` against the Messages API directly) |
| 1.3 | Configure `application.properties` — H2 file datasource (`./data/jobtracker`), Thymeleaf settings, Claude API config (model, max tokens, `ANTHROPIC_API_KEY` from env) | DONE |
| 1.4 | Base layout template (`layout.html`) — Tailwind CDN + custom theme config (Linear dark palette tokens), htmx + SortableJS script includes, Google Fonts (Inter, JetBrains Mono) | DONE |
| 1.5 | `tokens.css` — CSS variables for colors not expressible as Tailwind utilities (focus-ring opacity, hairline top-edge highlight) | DONE |
| 1.6 | Verify app boots (`mvn spring-boot:run`) and serves a placeholder page | DONE |

## Phase 2: Data Model & Persistence
| # | Task | Status |
|---|---|---|
| 2.1 | `JobStatus` enum (WISHLIST, APPLIED, INTERVIEWING, OFFER, REJECTED) | DONE |
| 2.2 | `Job` entity + `JobRepository` (find by status ordered by `sortOrder`) | DONE |
| 2.3 | `GeneratedKit` entity + `GeneratedKitRepository` (one-to-one with Job) | DONE |
| 2.4 | `UserProfile` entity + `UserProfileRepository` + startup seed (single row) | DONE |
| 2.5 | JaCoCo coverage plugin (80% line/branch minimum, enforced via `mvn test`) | DONE |
| 2.6 | Unit + `@DataJpaTest` repository tests for all Phase 2 classes + `BoardController` test (100% line/branch achieved) | DONE |
| 2.7 | README with setup/run instructions | DONE |

## Phase 3: Board View (read-only)
| # | Task | Status |
|---|---|---|
| 3.1 | `BoardController` — `GET /` loads jobs grouped by status, renders `board.html` | DONE |
| 3.2 | `fragments/card.html` — job card markup with status dot, title, company, location | DONE |
| 3.3 | `fragments/column.html` — column container (surface-1 panel, header bar, cards) | DONE |
| 3.4 | Styling pass: board layout, columns, cards per design system (colors, radii, spacing) | DONE |

## Phase 4: Add Job Flow
| # | Task | Status |
|---|---|---|
| 4.1 | Add Job modal — tabs for "Paste URL" / "Paste Text" | DONE |
| 4.2 | `JobExtractionService` — Jsoup fetch + text cleanup for URL input | DONE |
| 4.3 | `ClaudeAiService.extractJobFields(text)` — extraction prompt → strict JSON → DTO | DONE |
| 4.4 | `JobController` — `POST /jobs/preview` (extraction + editable preview) and `POST /jobs` (save, status=WISHLIST) | DONE |
| 4.5 | Wire htmx so form submit refreshes Wishlist column without full reload | DONE |

## Phase 5: Drag & Drop
| # | Task | Status |
|---|---|---|
| 5.1 | Integrate SortableJS per column container | DONE |
| 5.2 | On drop, build payload (jobId, new status, reordered sibling ids) | DONE |
| 5.3 | `JobController` — `POST /jobs/{id}/move` updates status/sortOrder, resequences columns, returns updated fragments | DONE |

## Phase 6: Profile / Master Resume
| # | Task | Status |
|---|---|---|
| 6.1 | `profile.html` — textarea for resume text + PDF upload input | DONE |
| 6.2 | `ProfileService` — PDFBox extraction on upload, save text to `UserProfile` | DONE |
| 6.3 | `ProfileController` — `GET /profile`, `POST /profile` | DONE |

## Phase 7: Card Detail Panel
| # | Task | Status |
|---|---|---|
| 7.1 | `fragments/job-detail.html` — full description, Generate/Regenerate Kit button, kit section placeholders | DONE |
| 7.2 | `GET /jobs/{id}` returns detail fragment (htmx-loaded on card click), shows existing kit if present | DONE |

## Phase 8: Kit Generation (Claude)
| # | Task | Status |
|---|---|---|
| 8.1 | `ClaudeAiService.generateKit(job, profile)` — single combined prompt → JSON (coverLetter, tailoredResume, interviewQuestions[], companyBrief) | DONE |
| 8.2 | Parse response into `GeneratedKit`, persist (upsert), set `generatedAt` | DONE |
| 8.3 | `KitController` — `POST /jobs/{id}/kit/generate` → returns `kit-panel.html` fragment with rendered Markdown | DONE |
| 8.4 | Render sections via Flexmark; add per-section Copy / Download .md buttons | DONE |
| 8.5 | htmx loading indicator on Generate button during Claude call | DONE |

## Phase 9: Polish & Edge Cases
| # | Task | Status |
|---|---|---|
| 9.1 | Error handling: Claude API failures, malformed extraction JSON, unreachable URLs — inline user-facing messages | DONE |
| 9.2 | Empty states (no jobs in column, no profile resume yet) | DONE |
| 9.3 | Responsive check on board for smaller windows | DONE |
| 9.4 | README — setup instructions (API key env var, run command, H2 file location) | DONE |

---

## Design Reference (for all UI tasks)

**Colors**: canvas `#010102`, surface-1 `#0d0e10`, surface-2 `#161719`, surface-3 `#1f2023`, surface-4 `#292a2e`, hairline `#23252a`, hairline-strong `#34363c`, ink `#f7f8f8`, ink-muted `#d0d6e0`, ink-subtle `#8a8f98`, ink-tertiary `#62666d`, primary `#5e6ad2`, primary-hover `#828fff`, primary-focus `#5e69d1`, success `#27a644`.

**Status dot colors**: Wishlist `#6b7280`, Applied `#6b8fc4`, Interviewing `#c9a35c`, Offer `#27a644`, Rejected `#b06868`.

**Fonts**: Inter (400/500/600) for display/body, JetBrains Mono for timestamps/IDs.

**Radii**: buttons/inputs `8px`, cards `12px`, modals/panels `16px`, badges/tabs `pill`.

# CSA09 – Programming in Java – Assignment 4

**Student:** E. Lohitha  
**Register number:** 192411103  
**Course:** CSA09 – Programming in Java  
**Java validation environment:** OpenJDK 21.0.12  
**GitHub repository:** https://github.com/Lohitha-E/CSA09-192411103-Assignment4

## Project contents

```text
CSA09-192411103-Assignment4/
├── Task1/                         # Fee window, service, repository and tests
│   └── Screenshots/
├── Task2/                         # Outpatient appointment desk
│   └── Screenshots/
├── Task3/                         # Campus library search and layout
│   └── Screenshots/
├── Task4/                         # Broken/repaired/buffered drawing pad
│   ├── CompilerErrorDemo/
│   └── Screenshots/
├── Task5/                         # Converter styles A–D and A2–D2
│   └── Screenshots/
├── Task6/                         # Swing + JDBC event registration
│   ├── sql/                       # MySQL schema and optional 100k seed
│   ├── src/events/                # App, model, service, DAO and UI
│   ├── output/                    # Sample CSV exported from demo mode
│   └── Screenshots/
├── REPORT.md                      # Written report aligned to ASSIGNMENT4.pdf
├── docs/
│   └── ASSIGNMENT4.pdf             # Original assignment brief
├── verify.sh                      # Linux/macOS compile and test script
├── verify.ps1                     # Windows PowerShell compile and test script
├── verification-results.txt       # Fresh compile/test/benchmark output
└── README.md
```

Generated `.class` files and the local `out/` directory are excluded by `.gitignore`.

## Compile and verify

Requirements: JDK 21 (or a compatible newer JDK). From the repository root:

**Windows PowerShell**

```powershell
.\verify.ps1
```

**Linux / macOS**

```bash
chmod +x verify.sh
./verify.sh
```

The verification script compiles all six task source groups, runs `FeeServiceTest` and `events.ServiceTest`, executes the Task 1 dispatch benchmark, and confirms that the intentionally broken Task 4 compiler example is rejected with the expected `@Override` diagnostic.

## Run the applications

Run commands from the repository root after compilation with the `out` folder available.

| Task | Command |
|---|---|
| 1 – Fee payment | `java -cp out FeeWindow` |
| 1 – Dispatch benchmark | `java -cp out DispatchBench` |
| 2 – Appointment desk | `java -cp out AppointmentDesk` |
| 3 – Library search | `java -cp out LibrarySearch` |
| 4 – Broken drawing pad | `java -cp out DrawPadBroken` |
| 4 – Repaired drawing pad | `java -cp out DrawPad` |
| 4 – Buffered drawing pad | `java -cp out DrawPadBuffered` |
| 5 – Completed lambda converter | `java -cp out Converter` |
| 5 – Changed-requirement example | `java -cp out ConverterA2` (or `ConverterB2`, `ConverterC2`, `ConverterD2`) |
| 6 – Event registration | `java -cp out events.App` |
| 6 – Service tests | `java -cp out events.ServiceTest` |

The other Task 5 styles are `ConverterA`, `ConverterB`, `ConverterC`, `ConverterD`, and the changed-requirement variants `ConverterA2` through `ConverterD2`.

## Task 6 database setup (optional live MySQL mode)

1. Install MySQL 8 and MySQL Connector/J.
2. Run `Task6/sql/schema.sql` against the server.
3. Copy `Task6/db.properties.example` to `db.properties` in the repository root and set `db.url`, `db.user` and `db.password`.
4. Add the Connector/J JAR to the Java classpath and run `events.App`.

Without `db.properties`, the app deliberately starts in demo mode with an in-memory DAO. `Task6/sql/seed_100k.sql` is optional and is intended for the scale/paging experiment after the schema is installed. Never commit real database credentials.

## Evidence and submission checklist

Screenshots in each `TaskN/Screenshots/` folder were captured from actual program runs in a virtual desktop session. Some evidence that depends on a live database or machine-specific desktop behaviour remains a manual check. The original assignment brief is included at `docs/ASSIGNMENT4.pdf`. `REPORT.md` has been reviewed against its nine pages and includes the required analysis, event tables, component diagrams, pseudocode and alternative-concept complexity discussions.

| Task | Evidence present in this package | Still to do / qualification |
|---|---|---|
| 1 | Invalid amount, processing state, successful payment/receipt; original service-test and benchmark evidence | Capture fresh terminal screenshots from the target computer if the rubric requires local-machine evidence |
| 2 | Department/doctor list, booking success, invalid booking, close-confirmation dialog, event trace terminal | None identified in the included checklist |
| 3 | Small, default and large window screenshots | Re-run resize checks on the submission computer if local-machine evidence is expected |
| 4 | Broken click, repaired dots before and after a minimize/restore cycle, compiler-error evidence | Confirm native minimize/restore evidence on the submission computer |
| 5 | Valid conversion, invalid amount and Enter-key result dialog; line-change comparison text | Re-run all four styles if the instructor expects a live demonstration on the submission computer |
| 6 | Normal registration, invalid input, duplicate rejection, paged table, File menu, CSV export chooser/completion and service-test evidence | The MySQL unique-constraint screenshot requires a live MySQL setup |

The screenshots show the submitted app executing, but they do not prove that an external MySQL server was configured. See `REPORT.md` for design explanation, validation details and remaining checks.

## GitHub submission steps

1. Create a public or instructor-accessible repository named `CSA09-192411103-Assignment4`.
2. Add the complete contents of this folder.
3. Commit progressively (at least one commit per task, as indicated by the assignment notes in the supplied project).
4. Replace the repository placeholder above with the real URL.
5. Put that same URL on the report cover page if required by the PDF.

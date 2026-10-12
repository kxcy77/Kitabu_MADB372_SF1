# Kitabu — Library Book Rental & Reservation System

Public repository: <https://github.com/kxcy77/Kitabu_MADB372_SF1>

Kitabu is a native, offline-first Android application for browsing a local library catalogue, reserving books, tracking deadlines, and managing reservation lifecycles. It was built for MADB372/SF1 Task 1 using Kotlin, Jetpack Compose Material 3, Room, MVVM, Coroutines, Flow, and Navigation Compose.

## Features demonstrated

- Responsive `LazyVerticalGrid` catalogue with title/author search and All/Available/Borrowed filters.
- Book cards showing title, author, category, and availability badges.
- Material 3 reservation bottom sheet with validated student name and 7/14/21-day duration choices.
- PENDING pickup/cancellation and ACTIVE renewal/return workflows.
- Atomic Room transactions for reserve, pickup, renew, return, and cancel.
- Offline persistence with 12 seeded books and one PENDING plus one ACTIVE demonstration booking.

## Requirements and setup

- Android Studio Ladybug or newer
- JDK 17+
- Android SDK 37
- Android device/emulator API 26+

```bash
git clone https://github.com/kxcy77/Kitabu_MADB372_SF1.git
cd Kitabu_MADB372_SF1
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew assembleDebug
./gradlew connectedDebugAndroidTest
```

Open the folder in Android Studio, allow Gradle sync to finish, and run the `app` configuration. The app is offline-first; no Firebase, REST API, authentication, payment, or cloud account is required.

## Architecture and data flow

```text
Compose screen
    -> ViewModel + immutable StateFlow
    -> LibraryRepository
    -> LibraryDao
    -> Room / SQLite
    ^
    | Room Flow recomposition via collectAsStateWithLifecycle()
```

The single `MainActivity` hosts a `NavHost` with Catalog and My Reservations destinations. Manual `AppContainer` dependency injection keeps the project small while separating `ui`, `domain`, `data`, and `di` packages.

## Room schema and lifecycle rules

| Table | Important columns and constraints |
| --- | --- |
| `books` | `book_id` auto-generated primary key, `title`, `author`, `category`, `is_available` defaulting to true |
| `bookings` | `booking_id` auto-generated primary key, indexed `book_owner_id`, `user_name`, `booking_date`, `return_deadline`, `status` |
| Relationship | `bookings.book_owner_id` references `books.book_id` with `CASCADE` delete |
| Conversion | `BookingStatus` is persisted through a Room string converter; timestamps are epoch milliseconds |

1. Reserve succeeds only when the availability update affects one book; it then creates a PENDING booking in the same transaction.
2. Pickup permits only `PENDING -> ACTIVE`.
3. Renew permits only ACTIVE bookings and adds seven days from the later of today or the existing deadline.
4. Return permits only `ACTIVE -> RETURNED` and releases the book atomically.
5. Cancel deletes only a PENDING booking and releases the book atomically.

Invalid transitions and double reservations return an action failure without partial writes.

## CRUD and rubric mapping

| Rubric area | Evidence in the project |
| --- | --- |
| A — UI layout and navigation (40) | Compose catalogue grid, search/filter, availability badges, reservation sheet, dashboard, lifecycle buttons, single-activity `NavHost` |
| B — Room database and business logic (40) | `BookEntity`, `BookingEntity`, cascade foreign key, converter, combined `LibraryDao`, joined booking projection, seed callback, atomic transactions |
| C — Architecture and MVVM (15) | Compose screens -> ViewModels -> repository -> DAO, immutable `StateFlow`, manual `AppContainer`, KSP/Room and Navigation Compose configuration |
| D — Advanced features and quality (15) | Room `Flow` observation, `viewModelScope.launch` writes, validation, logical packages, offline operation |
| Monitor demonstrations (10) | Day 1 UI/navigation demonstration and Day 2 Room/database demonstration are represented by the screen sequence and lifecycle evidence |

### CRUD evidence

| Operation | App evidence | Source evidence |
| --- | --- | --- |
| Create | Reserve sheet creates a PENDING booking and removes the book from availability | `LibraryDao.reserveBook()` transaction |
| Read | Search/filter catalogue and joined My Reservations dashboard | `observeBooks()` and open-booking join query |
| Update | Pickup, renewal, and return controls update booking state/deadline | guarded DAO lifecycle transactions |
| Delete | Cancel removes only a PENDING booking and frees its book | `cancelBooking()` transaction |

## Verification record

The local verification record for the checked-in implementation contains five JVM tests, seven connected tests on `bkk_api26` (API 26), seven connected tests on `medium_phone` (API 36), `lintDebug`, and `assembleDebug`. These results were recorded on 24 August 2026; rerun the commands above if a fresh machine-level result is required.

The source and schema confirm the entities, LIKE search, joined dashboard query, cascade foreign key, seed data, guarded transactions, `collectAsStateWithLifecycle()`, `stateIn(WhileSubscribed(5_000))`, asynchronous writes, and 1–50 character name validation. The report distinguishes source evidence from execution evidence rather than treating source inspection as a passing test result.

## Screenshot evidence

The complete, uncropped emulator sequence is in [`docs/screenshots/`](docs/screenshots/). The source capture list is:

| File | Demonstrates |
| --- | --- |
| `01_catalog.png` | API 26 catalogue, badges, filters, and navigation |
| `02_search_filter.png` | Search field interaction with `Android` entered |
| `03_reservation_sheet.png` | Student name, duration choices, deadline preview, Reserve |
| `04_reservations_dashboard.png` | ACTIVE and PENDING cards with lifecycle controls |
| `05_renewed_deadline.png` | Seven-day renewal snackbar and updated deadline |
| `06_return_confirmation.png` | Confirmation required before returning an ACTIVE booking |
| `07_returned_available.png` | Filtered title visible as Available after return |
| `08_api36_catalog.png` | API 36 responsive grid with Available and Borrowed badges |

`02_search_filter.png` is an input-state capture; the filtered result and post-return availability are shown clearly in `07_returned_available.png`. A single frame does not prove the complete before/after reactive transition, so the report labels that limitation explicitly.

The monitor demonstration script is in [`docs/MONITOR_DEMO.md`](docs/MONITOR_DEMO.md), and the importable design board is [`docs/design/Kitabu_Figma_Import.svg`](docs/design/Kitabu_Figma_Import.svg). The SVG is an importable local design board; no public Figma URL is claimed.

## Scope and limitations

“Real time” means immediate reactive updates on the same device through Room `Flow`; cross-device synchronisation is out of scope. Authentication, cloud services, payments, Firebase, REST services, multi-user accounts, and late-fee processing are intentionally excluded by the assignment scope. A fresh before/after screenshot pair for reserve/return and a standalone completed-cancellation screenshot are recommended additions if the lecturer requires visual proof of every transition.

## Submission evidence

The project folder is `Kitabu_MADB372_SF1`. The generated screenshot report is named `StudName_MADB372_SF1.docx` with a matching PDF export and ZIP. Replace `StudName` with the real student name before Canvas submission; no name has been invented here.

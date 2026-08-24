# Kitabu monitor demonstration

Use the seeded database so the marker can see both PENDING and ACTIVE states immediately. Keep the demonstration under four minutes and perform the actions instead of describing them.

## Monitor Day 1 — UI and navigation

1. Launch on the Catalog destination and point out the adaptive Compose grid, category details, and Available/Borrowed badges.
2. Search for `kotlin`, clear it, then select the Available and Borrowed filters to prove state-driven filtering.
3. Select an available book. In the reservation sheet, show the editable name, 7/14/21-day choices, deadline preview, and blank-name validation.
4. Dismiss the sheet and switch between both bottom destinations. Explain that `launchSingleTop` prevents duplicate destinations.

## Monitor Day 2 — Room lifecycle

1. Reserve an available book and show its badge change immediately without manual refresh.
2. Open My Reservations and show the newly persisted PENDING record from the joined Room query.
3. Pick it up, then renew it. Point out the ACTIVE-only controls and seven-day deadline change.
4. Return it through the confirmation dialog, switch to Catalog, and show that the same book is available again.
5. Relaunch the app to prove persistence. Show `app/schemas/com.example.kitabu.data.local.KitabuDatabase/1.json` and the connected-test report as implementation evidence.

## Honest scope statement

Room `Flow` provides immediate reactive updates on one device. The project deliberately does not claim cross-device synchronization because the task excludes cloud services.

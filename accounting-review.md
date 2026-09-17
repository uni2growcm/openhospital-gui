# OpenHospital Accounting Module - Technical Review

## Executive Summary

The accounting module in OpenHospital contains critical business logic flaws that could lead to financial discrepancies, data corruption, and security vulnerabilities. Key issues include double stock decrements on sales, prescription quantity inflation on bill edits, orphaned payment records, and broken refund flows. The module suffers from fundamental architectural problems: `double` arithmetic for currency, inconsistent permission checks, and static listener memory leaks.

## Critical Issues (Immediate Action Required)

### C-01: Double Stock Decrement on Bill Creation
**Location:** `BillBrowserManager.java:238-249`
**Impact:** Every MED sale reduces warehouse stock by 2x the quantity
**Root Cause:** `updateMedicalStock` called twice in `newBill()` path
**Fix:** Remove duplicate stock update (lines 246-248)

### C-02: Prescription Quantity Inflation on Bill Edit
**Location:** `BillBrowserManager.java:263-295`
**Impact:** Repeated saves inflate `qtyBougth` for prescribed therapies
**Root Cause:** All bill items re-marked as billed on each save
**Fix:** Track already-billed prescriptions, only mark new items

### C-03: Sage Export Button Is No-Op
**Location:** `BillBrowser.java:1539-1581`
**Impact:** Primary export feature does nothing, shows silent success
**Root Cause:** Empty `doInBackground()` and catch-all exception handler
**Fix:** Implement JasperReports integration and proper error handling

### C-04: Saved Payment/Item Protection Bypassed
**Location:** `PatientBillEdit.java:534-535, 3630`
**Impact:** Already-saved payments can be deleted, receipts printed on every save
**Root Cause:** `*Saved` counters never initialized
**Fix:** Initialize counters at load time, restore protection logic

## High Priority Issues

### H-01: Closed Bill Reopens on Edit
**Location:** `PatientBillEdit.java:2040, 2093`
**Impact:** Billing status silently flipped from closed to open
**Root Cause:** Update constructor ignores original status, uses `paid` flag only
**Fix:** Preserve `thisBill.getStatus()` unless user explicitly changes it

### H-02: Floating-Point Balance Validation
**Location:** `BillBrowserManager.java:200-208`
**Impact:** Bills closed with `0.30000000000000004` balance
**Root Cause:** `double` comparison instead of decimal arithmetic
**Fix:** Use `BigDecimal` or tolerance-based comparison (`abs(balance) < 0.005`)

### H-03: Cashier Filter Does Nothing
**Location:** `BillBrowser.java:1615-1641`
**Impact:** Multi-user mode: "cashiers" selector visible but doesn't filter list
**Root Cause:** `user` parameter not passed to `getBillsWithFilters()`
**Fix:** Pass `user` into repository queries

### H-04: Bill Deletion Creates Orphaned Records
**Location:** `BillBrowserManager.java:309-311`
**Impact:** Deleting a paid bill erases payment/item history
**Root Cause:** `billRepository.deleteById(id)` without cascade
**Fix:** Implement soft-delete or cascade delete, warn on payments

### H-05: Null Pointer in Bill Rendering
**Location:** `BillBrowser.java:1935-1936`
**Impact:** Any non-patient bill breaks table rendering
**Root Cause:** Missing null check for `billPatient`
**Fix:** Null-guard `getBillPatient()`, show empty patient column

### H-06: Page Request Exception
**Location:** `BillBrowser.java:544-548, 570-574, 595-599`
**Impact:** `PageRequest.of(-1, size)` throws unchecked exception
**Root Cause:** `totalPages == 0` → `currentPage = -1`
**Fix:** Clamp to `max(0, totalPages - 1)`, validate `size > 0`

### H-07: Report Menu Wrong Dialog
**Location:** `BillBrowser.java:1295-1310`
**Impact:** "Today closure" opens reduction/grouped report instead of requested report
**Root Cause:** Fall-through in if-else chain with shared index `i`
**Fix:** Map each option to dedicated branch

### H-08: Totals Ignore Partner Filter
**Location:** `BillBrowser.java:336-344`
**Impact:** Partner-filtered list vs totals mismatch
**Root Cause:** `updateTotals()` uses different filter parameters
**Fix:** Pass selected partner into sum queries

## Medium Priority Issues

### M-01: Archive Pre-check Count Mismatch
**Location:** `BillBrowser.java:1032-1041`
**Impact:** User sees wrong bill count for archiving
**Root Cause:** Pre-check uses all closed bills, archive uses 365-day window
**Fix:** Apply same date filter in pre-check

### M-02: Refund Keeps Original Bill Open
**Location:** `BillRefund.java:302-316`
**Impact:** Refund creates separate pending bill instead of updating original
**Root Cause:** Refund bill status not linked to original bill
**Fix:** Update original bill balance/status, or mark as paid

### M-03: Inconsistent Authorization
**Location:** `BillBrowser.java:986 vs 1011`
**Impact:** Admin can edit in one tab, restricted user can't in another
**Root Cause:** Hardcoded `"admin"` check vs grant-based check
**Fix:** Use grant consistently

### M-04: Initial Date Range Wrong
**Location:** `BillBrowser.java:129-130`
**Impact:** Period totals show 0 while table shows today's bills
**Root Cause:** `dateFrom = dateTo = now` instead of day boundaries
**Fix:** Initialize to day boundaries, validate `from <= to`

### M-05: TreeSet Comparator Violates Contract
**Location:** `AccountingIoOperations.java:180`
**Impact:** Undefined ordering, potential `ConcurrentModificationException`
**Root Cause:** `compare(o1,o2) == -1` and `compare(o2,o1) == -1` both true
**Fix:** Use `Comparator.comparingInt(Bill::getId)`

### M-06: Edit-Item Stock One-Way
**Location:** `PatientBillEdit.java:1286-1293`
**Impact:** Decreasing quantity never returns stock to ward
**Root Cause:** Stock only decremented on `diffQty > 0`
**Fix:** Credit stock on `diffQty < 0`

### M-07: Overpayment Message Truncates Decimals
**Location:** `PatientBillEdit.java:2379-2384`
**Impact:** `€10.70` paid vs `€10.00` shows "5" instead of "0.70"
**Root Cause:** `amount.intValue() - balance.intValue()`
**Fix:** Use `BigDecimal` subtraction

### M-08: ItemPayments Computes with Double
**Location:** `BillBrowserManager.java:606-645`
**Impact:** Rounding drift in payment apportionment, wrong user attribution
**Root Cause:** `double` arithmetic, all payments stamped with first user
**Fix:** Use `BigDecimal` apportionment, per-payment user propagation

### M-09: Table Models Suppress Listeners
**Location:** `PatientBillEdit.java:3691-3701`
**Impact:** UI changes require full component repaint, selection lost
**Root Cause:** Empty `add/removeTableModelListener` implementations
**Fix:** Implement proper `fireTableDataChanged` events

### M-10: Archive Parameter Handling
**Location:** `ArchiveIoOperations.java:74-95`
**Impact:** Silent fallback from bad config, ambiguous `-2` return
**Root Cause:** Exception catch only for `OHServiceException`, conflated outcomes
**Fix:** Validate config at startup, distinct return codes

### M-11: Archive Deletes Recent Refunds
**Location:** `ArchiveRepository.java:42-51`
**Impact:** Refund issued today for old bill gets archived immediately
**Root Cause:** Archive window based on parent bill date
**Fix:** Use refund bill's own date for window

### M-12: Archive UX Issues
**Location:** `BillBrowser.java:1046-1053`
**Impact:** Wrong count displayed, user confused on what will be archived
**Root Cause:** Pre-check count doesn't match archive criteria
**Fix:** Report actual archive count

## Low Priority Issues

### L-01: Column Count Mismatch
**Location:** `BillBrowser.java:175`
**Impact:** Trailing `true` in 11 entries for 10 columns
**Fix:** Remove extra entry

### L-02: Empty No-Op Methods
**Location:** `BillBrowser.java:325-327`
**Impact:** Legacy `BillDataLoader` path no longer wired
**Fix:** Remove dead code or wire it back

### L-03: Status Column Shows Raw Codes
**Location:** `BillBrowser.java:564/589/1616`, `PatientBillEdit.java:2040`
**Impact:** Cryptic "O"/"C"/"D" displayed to users
**Fix:** Use localized status labels

### L-04: Print/Open Edit Ambiguity
**Location:** `BillBrowser.java:1212-1218`
**Impact:** Open bill print opens edit instead of informing user
**Fix:** Check `ALLOWPRINTOPENEDBILL` flag

### L-05: Price List Null Risk
**Location:** `PatientBillEdit.java:605-620`
**Impact:** `IndexOutOfBoundsException` if price list empty
**Fix:** Null-safe price list access

### L-06: Date-Change Cascading Reloads
**Location:** `BillBrowser.java: date listeners`
**Impact:** Up to 6 aggregate queries per UI change
**Fix:** Debounce date changes

### L-07: Reset/Selection Race Condition
**Location:** `BillBrowser.java:458/486/514`
**Impact:** Page combo reset interferes with user selection
**Fix:** Synchronize refresh/selection

### L-08: Static Listener Memory Leaks
**Locations:** `BillRefund.java:80`, `SelectPrescriptions.java:70`
**Impact:** Listeners accumulate across dialog instances
**Fix:** Add `removeListener()` methods

### L-09: Integer Type Errors
**Locations:** Various (`getWardComboBox`, reduction plan calls)
**Impact:** `RuntimeException` on EDT during UI interactions
**Fix:** Wrap in try-catch with `OHServiceExceptionUtil`

### L-10: Misleading Comments
**Location:** `PatientBillEdit.java:2043`
**Impact:** Code becomes confusing for future maintainers
**Fix:** Update comment to match actual parameter

## Cross-Cutting Technical Debt

### Money Handling
**Problem:** End-to-end `double` arithmetic despite `BigDecimal` usage in GUI
**Locations:** All models, repository queries, payment calculations
**Impact:** Rounding errors, incorrect balances
**Recommendation:** Migrate to `BigDecimal` throughout persistence layer

### Permission Model
**Problem:** Inconsistent access control (`"admin"` hardcoded vs grants)
**Locations:** `BillBrowser.java:986 vs 1011`, `BillItemGroupBrowser.java:259`
**Impact:** Privilege escalation, privilege denial
**Recommendation:** Use single permission system consistently

### Concurrency
**Problem:** DB access on EDT, no background workers
**Locations:** `PatientBillEdit.loadDataset()`, `PatientBillEdit.checkBill()`
**Impact:** UI freezes during DB operations
**Recommendation:** Use `SwingWorker` for all DB-bound operations

### Error Handling
**Problem:** Silent catches, exceptions thrown on EDT
**Locations:** `SelectPrescriptions.initManagers()`, `BillRefund` various paths
**Impact:** Crashed UI, confusing user experience
**Recommendation:** Consistent error display, EDT exception wrapping

### Resource Management
**Problem:** Static listener lists, modal dialog leaks
**Locations:** Multiple dialogs share static listeners
**Impact:** Memory leaks, stale event reception
**Recommendation:** Dialog-specific listeners, proper cleanup

## Immediate Action Items

**Critical (Fix within 24 hours):**
1. Add `removePatientBillListener()` to `BillRefund.java`
2. Add `removePrescriptionSelectedListener()` to `SelectPrescriptions.java`
3. Fix double stock decrement (`BillBrowserManager.java:246-248`)
4. Fix prescription inflation (`BillBrowserManager.java:293`)
5. Add permission checks to `BillItemGroupBrowser.java:259`

**High (Fix within 1 week):**
1. Preserve bill status on edit (`PatientBillEdit.java:2040`)
2. Pass user filter to queries (`BillBrowser.java:538`)
3. Fix archive pre-check count (`BillBrowser.java:1032`)
4. Fix refund date logic (`BillRefund.java:302`)

**Best Practices (Fix within 1 month):**
1. Replace all `double` with `BigDecimal` for money
2. Implement consistent permission checking
3. Add EDT exception handling
4. Remove static listener patterns
5. Add proper resource cleanup

## Testing Recommendations

1. **Financial Integrity Tests:** Verify stock levels, prescription quantities, and balances after various operations
2. **Permission Tests:** Validate access control consistency across tabs and dialogs
3. **Concurrency Tests:** Simulate rapid bill edits, payments, and refreshes
4. **Data Corruption Tests:** Attempt edge cases (negative prices, zero quantities, duplicate refunds)
5. **UI State Tests:** Verify dialog dismissal behavior and listener management

This review reveals fundamental architectural issues that require systematic refactoring. Prioritize critical fixes that could lead to financial discrepancies or data corruption. Focus on money handling, permission consistency, and resource management to prevent future technical debt accumulation.

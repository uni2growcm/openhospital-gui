# Travail uniquement sur GUI et Core OpenHospital

**Session ID:** ses_077432a95ffe6faeK4AHCI8zVv
**Created:** 7/22/2026, 8:31:44 AM
**Updated:** 7/22/2026, 8:37:21 AM

---

## User

TU TRAVAIL UNIQUEMENT SUR LE GUI(./) ET LE CORE(../openhospital-core)

---

## Assistant (Plan · Big Pickle · 9.6s)

---

## User

TU TRAVAIL UNIQUEMENT SUR LE GUI(./) ET LE CORE(../openhospital-core)
Je veux que lorsqu'on selectionne un medicament dans l'inventaire on n'affiche ses differents lots en retirant les lots vides

---

## Assistant (Plan · Big Pickle · 18.2s)

_Thinking:_

The user wants to modify the behavior when selecting a medication in the inventory, so that the different batches/lots are displayed while excluding empty lots (lots with zero quantity).Let me explore the codebase to understand:
1. How the inventory works with medications
2. How lots are displayed
3. Where the filtering logic should be added

I need to look at both the GUI and CORE projects.

**Tool: skill**

**Input:**
```json
{
  "name": "angular-20"
}
```

**Output:**
```
<skill_content name="angular-20">
# Skill: angular-20

# Angular Developer Guidelines

1. Always analyze the project's Angular version before providing guidance, as best practices and available features can vary significantly between versions. If creating a new project with Angular CLI, do not specify a version unless prompted by the user.

2. When generating code, follow Angular's style guide and best practices for maintainability and performance. Use the Angular CLI for scaffolding components, services, directives, pipes, and routes to ensure consistency.

3. Once you finish generating code, run `ng build` to ensure there are no build errors. If there are errors, analyze the error messages and fix them before proceeding. Do not skip this step, as it is critical for ensuring the generated code is correct and functional.

## Creating New Projects

If no guidelines are provided by the user, here are same default rules to follow when creating a new Angular project:

1. Use the latest stable version of Angular unless the user specifies otherwise.
2. Use Signals Forms for form management in new projects (available in Angular v21 and newer) [Find out more](references/signal-forms.md).

**Execution Rules for `ng new`:**
When asked to create a new Angular project, you must determine the correct execution command by following these strict steps:

**Step 1: Check for an explicit user version.**

- **IF** the user requests a specific version (e.g., Angular 15), bypass local installations and strictly use `npx`.
- **Command:** `npx @angular/cli@<requested_version> new <project-name>`

**Step 2: Check for an existing Angular installation.**

- **IF** no specific version is requested, run `ng version` in the terminal to check if the Angular CLI is already installed on the system.
- **IF** the command succeeds and returns an installed version, use the local/global installation directly.
- **Command:** `ng new <project-name>`

**Step 3: Fallback to Latest.**

- **IF** no specific version is requested AND the `ng version` command fails (indicating no Angular installation exists), you must use `npx` to fetch the latest version.
- **Command:** `npx @angular/cli@latest new <project-name>`

## Components

When working with Angular components, consult the following references based on the task:

- **Fundamentals**: Anatomy, metadata, core concepts, and template control flow (@if, @for, @switch). Read [components.md](references/components.md)
- **Inputs**: Signal-based inputs, transforms, and model inputs. Read [inputs.md](references/inputs.md)
- **Outputs**: Signal-based outputs and custom event best practices. Read [outputs.md](references/outputs.md)
- **Host Elements**: Host bindings and attribute injection. Read [host-elements.md](references/host-elements.md)

If you require deeper documentation not found in the references above, read the documentation at `https://angular.dev/guide/components`.

## Reactivity and Data Management

When managing state and data reactivity, use Angular Signals and consult the following references:

- **Signals Overview**: Core signal concepts (`signal`, `computed`), reactive contexts, and `untracked`. Read [signals-overview.md](references/signals-overview.md)
- **Dependent State (`linkedSignal`)**: Creating writable state linked to source signals. Read [linked-signal.md](references/linked-signal.md)
- **Async Reactivity (`resource`)**: Fetching asynchronous data directly into signal state. Read [resource.md](references/resource.md)
- **Side Effects (`effect`)**: Logging, third-party DOM manipulation (`afterRenderEffect`), and when NOT to use effects. Read [effects.md](references/effects.md)

## Forms

In most cases for new apps, **prefer signal forms**. When making a forms decision, analyze the project and consider the following guidelines:

- if the application is using v21 or newer and this is a new form, **prefer signal forms**.
  -For older applications or when working with existing forms, use the appropriate form type that matches the applications current form strategy.

- **Signal Forms**: Use signals for form state management. Read [signal-forms.md](references/signal-forms.md)
- **Template-driven forms**: Use for simple forms. Read [template-driven-forms.md](references/template-driven-forms.md)
- **Reactive forms**: Use for complex forms. Read [reactive-forms.md](references/reactive-forms.md)

## Dependency Injection

When implementing dependency injection in Angular, follow these guidelines:

- **Fundamentals**: Overview of Dependency Injection, services, and the `inject()` function. Read [di-fundamentals.md](references/di-fundamentals.md)
- **Creating and Using Services**: Creating services, the `providedIn: 'root'` option, and injecting into components or other services. Read [creating-services.md](references/creating-services.md)
- **Defining Dependency Providers**: Automatic vs manual provision, `InjectionToken`, `useClass`, `useValue`, `useFactory`, and scopes. Read [defining-providers.md](references/defining-providers.md)
- **Injection Context**: Where `inject()` is allowed, `runInInjectionContext`, and `assertInInjectionContext`. Read [injection-context.md](references/injection-context.md)
- **Hierarchical Injectors**: The `EnvironmentInjector` vs `ElementInjector`, resolution rules, modifiers (`optional`, `skipSelf`), and `providers` vs `viewProviders`. Read [hierarchical-injectors.md](references/hierarchical-injectors.md)

## Angular Aria

When building accessible custom components for any of the following patterns: Accordion, Listbox, Combobox, Menu, Tabs, Toolbar, Tree, Grid, consult the following reference:

- **Angular Aria Components**: Building headless, accessible components (Accordion, Listbox, Combobox, Menu, Tabs, Toolbar, Tree, Grid) and styling ARIA attributes. Read [angular-aria.md](references/angular-aria.md)

## Routing

When implementing navigation in Angular, consult the following references:

- **Define Routes**: URL paths, static vs dynamic segments, wildcards, and redirects. Read [define-routes.md](references/define-routes.md)
- **Route Loading Strategies**: Eager vs lazy loading, and context-aware loading. Read [loading-strategies.md](references/loading-strategies.md)
- **Show Routes with Outlets**: Using `<router-outlet>`, nested outlets, and named outlets. Read [show-routes-with-outlets.md](references/show-routes-with-outlets.md)
- **Navigate to Routes**: Declarative navigation with `RouterLink` and programmatic navigation with `Router`. Read [navigate-to-routes.md](references/navigate-to-routes.md)
- **Control Route Access with Guards**: Implementing `CanActivate`, `CanMatch`, and other guards for security. Read [route-guards.md](references/route-guards.md)
- **Data Resolvers**: Pre-fetching data before route activation with `ResolveFn`. Read [data-resolvers.md](references/data-resolvers.md)
- **Router Lifecycle and Events**: Chronological order of navigation events and debugging. Read [router-lifecycle.md](references/router-lifecycle.md)
- **Rendering Strategies**: CSR, SSG (Prerendering), and SSR with hydration. Read [rendering-strategies.md](references/rendering-strategies.md)
- **Route Transition Animations**: Enabling and customizing the View Transitions API. Read [route-animations.md](references/route-animations.md)

If you require deeper documentation or more context, visit the [official Angular Routing guide](https://angular.dev/guide/routing).

## Styling and Animations

When implementing styling and animations in Angular, consult the following references:

- **Using Tailwind CSS with Angular**: Integrating Tailwind CSS into Angular projects. Read [tailwind-css.md](references/tailwind-css.md)
- **Angular Animations**: Using native CSS (recommended) or the legacy DSL for dynamic effects. Read [angular-animations.md](references/angular-animations.md)
- **Styling components**: Best practices for component styles and encapsulation. Read [component-styling.md](references/component-styling.md)

## Testing

When writing or updating tests, consult the following references based on the task:

- **Fundamentals**: Best practices for unit testing (Vitest), async patterns, and `TestBed`. Read [testing-fundamentals.md](references/testing-fundamentals.md)
- **Component Harnesses**: Standard patterns for robust component interaction. Read [component-harnesses.md](references/component-harnesses.md)
- **Router Testing**: Using `RouterTestingHarness` for reliable navigation tests. Read [router-testing.md](references/router-testing.md)
- **End-to-End (E2E) Testing**: Best practices for E2E tests with Cypress. Read [e2e-testing.md](references/e2e-testing.md)

## Tooling

When working with Angular tooling, consult the following references:

- **Angular CLI**: Creating applications, generating code (components, routes, services), serving, and building. Read [cli.md](references/cli.md)
- **Code Modernization**: Automatically refactoring to modern standards using migrations. Read [migrations.md](references/migrations.md)
- **Angular MCP Server**: Available tools, configuration, and experimental features. Read [mcp.md](references/mcp.md)

Base directory for this skill: /home/uni2grow/.agents/skills/angular-20
Relative paths in this skill (e.g., scripts/, reference/) are relative to this base directory.
Note: file list is sampled.

<skill_files>

</skill_files>
</skill_content>
```

---

## Assistant (Plan · Big Pickle · 14.4s)

_Thinking:_

The user wants to modify the inventory behavior to exclude empty lots when displaying medication lots. Let me explore the codebase to understand how the inventory and medication lots are currently handled.

First, let me look at the project structure.

**Tool: task**

**Input:**
```json
{
  "description": "Explore inventory medication lots",
  "prompt": "Explore the OpenHospital GUI and CORE codebase to understand how medication inventory and lot management works. \n\nLook for:\n1. How medication inventory is displayed (likely in the GUI under `./` which is `/home/uni2grow/Documents/openhospital/openhospital-gui`)\n2. How lots (batches) of medications are loaded and displayed\n3. The data model for lots/inventory (in the CORE under `../openhospital-core` which is `/home/uni2grow/Documents/openhospital/openhospital-core`)\n4. Any existing filtering logic for lots\n5. The service/DAO layer that fetches lot data\n\nSearch for files related to: inventory, lot, medical, stock. Look at both GUI (Java Swing) and CORE (Java) projects.\n\nReturn:\n- Key file paths for the GUI components related to inventory/lot display\n- Key file paths for the CORE model, DAO, service related to lots\n- How lots are currently loaded (method signatures, SQL queries)\n- The data model for Lot/MedicalStock entities\n- Any existing filter patterns used in the codebase",
  "subagent_type": "explore"
}
```

**Output:**
```
<task id="ses_07741c393fferm7k4W23Z6gRNF" state="completed">
<task_result>

</task_result>
</task>
```

---

## Assistant (Plan · Big Pickle · 36.1s)

_Thinking:_

The task didn't return useful results. Let me explore the codebase more directly.

**Tool: read**

**Input:**
```json
{
  "filePath": "/home/uni2grow/Documents/openhospital/openhospital-gui"
}
```

**Output:**
```
<path>/home/uni2grow/Documents/openhospital/openhospital-gui</path>
<type>directory</type>
<entries>
.git/
.github/
.gitignore
.ide-settings/
.idea/
.license-header/
.springBeans
bundle/
data/
lib/
LICENSE
LOG_DEST
LOG_DEST.2026-05-25
LOG_DEST.2026-05-26
LOG_DEST.2026-05-27
LOG_DEST.2026-05-28
LOG_DEST.2026-05-29
LOG_DEST.2026-05-30
LOG_DEST.2026-05-31
LOG_DEST.2026-06-01
LOG_DEST.2026-06-02
LOG_DEST.2026-06-03
LOG_DEST.2026-06-04
LOG_DEST.2026-06-08
LOG_DEST.2026-06-09
LOG_DEST.2026-06-10
LOG_DEST.2026-06-12
LOG_DEST.2026-07-21
mvnw
mvnw.cmd
oh.bat
oh.ico
oh.ps1
oh.sh
ohmac.sh
pom.xml
README.md
rpt_base/
rpt_extra/
rpt_stat/
rsc/
src/
target/

(43 entries)
</entries>
```

**Tool: read**

**Input:**
```json
{
  "filePath": "/home/uni2grow/Documents/openhospital/openhospital-core"
}
```

**Output:**
```
<path>/home/uni2grow/Documents/openhospital/openhospital-core</path>
<type>directory</type>
<entries>
.editorconfig
.git/
.github/
.gitignore
.ide-settings/
.idea/
.license-header/
.mvn/
.springBeans
doc/
docker-compose.yml
Dockerfile-ohdb
dotenv
LICENSE
logs/
mvnw
mvnw.cmd
pom.xml
qodana.yaml
README.md
rpt_base/
rsc-test/
sql/
src/
target/

(25 entries)
</entries>
```

---

## Assistant (Plan · Big Pickle · 15.8s)

_Thinking:_

Let me search for inventory-related files in both projects.

**Tool: grep**

**Input:**
```json
{
  "pattern": "inventory|lot|stock",
  "include": "*.java",
  "path": "/home/uni2grow/Documents/openhospital/openhospital-gui/src"
}
```

**Output:**
```
Found 100 matches (more matches available)
/home/uni2grow/Documents/openhospital/openhospital-gui/src/main/java/org/isf/medstockmovtype/gui/MedicalsrMovPatList.java:
  Line 22: package org.isf.medstockmovtype.gui;

  Line 33: import org.isf.medicalstockward.manager.MovWardBrowserManager;

  Line 34: import org.isf.medicalstockward.model.MovementWard;

  Line 44:  * TODO: Move under package org.isf.medicalstockward.gui


/home/uni2grow/Documents/openhospital/openhospital-gui/src/main/java/org/isf/medstockmovtype/gui/MedicalDsrStockMovementTypeBrowser.java:
  Line 22: package org.isf.medstockmovtype.gui;

  Line 37: import org.isf.medstockmovtype.gui.MedicalDsrStockMovementTypeBrowserEdit.MedicalDsrStockMovementTypeListener;

  Line 38: import org.isf.medstockmovtype.manager.MedicalDsrStockMovementTypeBrowserManager;

  Line 39: import org.isf.medstockmovtype.model.MovementType;

  Line 59: 			MessageBundle.getMessage("angal.medstockmovtype.category.txt").toUpperCase()

  Line 88: 		this.setTitle(MessageBundle.getMessage("angal.medstockmovtype.medicalstockmovementtypebrowser.title"));

  Line 182: 					int answer = MessageDialog.yesNo(null, "angal.medstockmovtype.deletemovementtype.fmt.msg", movType.getDescription());


/home/uni2grow/Documents/openhospital/openhospital-gui/src/test/java/org/isf/accounting/gui/BillDataLoaderTest.java:
  Line 35: import org.isf.medicalstock.manager.MovStockInsertingManager;

  Line 36: import org.isf.medicalstockward.manager.MovWardBrowserManager;

  Line 37: import org.isf.medicalstockward.service.MedicalStockWardIoOperations;

  Line 43: import org.isf.medicalstock.manager.MovStockInsertingManager;

  Line 44: import org.isf.medicalstockward.manager.MovWardBrowserManager;


/home/uni2grow/Documents/openhospital/openhospital-gui/src/main/java/org/isf/medstockmovtype/gui/MedicalDsrStockMovementTypeBrowserEdit.java:
  Line 22: package org.isf.medstockmovtype.gui;

  Line 41: import org.isf.medstockmovtype.manager.MedicalDsrStockMovementTypeBrowserManager;

  Line 42: import org.isf.medstockmovtype.model.MovementType;

  Line 70: 	private void fireMedicaldsrstockmovInserted(MovementType anMedicalDsrStockMovementType) {

  Line 82: 	private void fireMedicaldsrstockmovUpdated() {

  Line 129: 			this.setTitle(MessageBundle.getMessage("angal.medstockmovtype.newmedicalstockmovementtype.title"));

  Line 131: 			this.setTitle(MessageBundle.getMessage("angal.medstockmovtype.editmedicalstockmovementtype.title"));

  Line 218: 							fireMedicaldsrstockmovInserted(medicalDsrStockMovementType);

  Line 234: 								fireMedicaldsrstockmovUpdated();

  Line 331: 			jDataPanel.add(new JLabel(MessageBundle.getMessage("angal.medstockmovtype.type") + ':'));

  Line 333: 			jDataPanel.add(new JLabel(MessageBundle.getMessage("angal.medstockmovtype.category.txt") + ':'));


/home/uni2grow/Documents/openhospital/openhospital-gui/src/main/java/org/isf/medicals/gui/MedicalBrowser.java:
  Line 58: import org.isf.medicalstock.manager.MovStockInsertingManager;

  Line 112: 			MessageBundle.getMessage("angal.medicals.stock.col").toUpperCase(),

  Line 114: 			MessageBundle.getMessage("angal.medicals.outofstock.col").toUpperCase()

  Line 356: 			int ok = MessageDialog.yesNoCancel(this, "angal.medicals.showonlycriticalstock.msg");

  Line 367: 		JButton buttonStock = new JButton(MessageBundle.getMessage("angal.medicals.stock.btn"));

  Line 368: 		buttonStock.setMnemonic(MessageBundle.getMnemonic("angal.medicals.stock.btn.key"));

  Line 379: 			List<String> lotOptions = new ArrayList<>();

  Line 380: 			lotOptions.add(MessageBundle.getMessage("angal.medicals.onlyquantity"));

  Line 381: 			lotOptions.add(MessageBundle.getMessage("angal.medicals.withlot"));

  Line 383: 			String lotOption = (String) MessageDialog.inputDialog(this,

  Line 385: 							lotOptions.toArray(),

  Line 386: 							lotOptions.get(0),

  Line 389:             if (lotOption == null ) return;

  Line 392:             if (lotOptions.indexOf(lotOption) == 0) {

  Line 393:                 int ok = MessageDialog.yesNoCancel(this, "angal.medicals.includezeroqtyinstock");

  Line 429: 			if (lotOptions.indexOf(lotOption) == i) {

  Line 432: 			if (lotOptions.indexOf(lotOption) == ++i) {

  Line 464: 		JButton buttonStockCard = new JButton(MessageBundle.getMessage("angal.common.stockcard.btn"));

  Line 465: 		buttonStockCard.setMnemonic(MessageBundle.getMnemonic("angal.common.stockcard.btn.key"));

  Line 505: 			jComboBoxWard.addItem(MessageBundle.getMessage("angal.medicalstockward.selectaward"));

  Line 512: 		jComboBoxWard.setSelectedItem(MessageBundle.getMessage("angal.medicalstockward.selectaward"));

  Line 550: 		StringBuilder filename = new StringBuilder(MessageBundle.getMessage("angal.medicals.stock.txt"));


/home/uni2grow/Documents/openhospital/openhospital-gui/src/main/java/org/isf/archive/gui/ArchiveBrowser.java:
  Line 848:                                 MessageDialog.info(ArchiveBrowser.this, "angal.medicalstock.exportsage.succes");

  Line 850:                                 MessageDialog.error(ArchiveBrowser.this, "angal.medicalstock.exportsage.error");


/home/uni2grow/Documents/openhospital/openhospital-gui/src/main/java/org/isf/accounting/gui/PatientBillEdit.java:
  Line 82: import org.isf.medicalstockward.manager.MovWardBrowserManager;

  Line 83: import org.isf.medicalstockward.model.MedicalWard;

  Line 242:         boolean stockMovementEnabled = GeneralData.STOCKMVTONBILLSAVE;

  Line 254:         if ((wardCode == null || wardCode.isEmpty()) && stockMovementEnabled) {

  Line 291:                                         + MessageBundle.getMessage("angal.newbill.stocknotavailableforitem")

  Line 726:         boolean stockMovementEnabled = GeneralData.STOCKMVTONBILLSAVE;

  Line 728:         if ("MED".equals(price.getGroup()) && stockMovementEnabled) {

  Line 733:                 MessageDialog.error(this, "angal.newbill.stocknotavailableforitem");

  Line 738:                 MessageDialog.error(this, "angal.newbill.qtynotinstock");

  Line 1228:                     MessageBundle.getMessage("angal.medicalstockwardedit.invalidquantitypleasetryagain"), //$NON-NLS-1$

  Line 1229:                     MessageBundle.getMessage("angal.medicalstockwardedit.invalidquantity"), //$NON-NLS-1$

  Line 1242:                     MessageBundle.getMessage("angal.medicalstockwardedit.invalidquantitypleasetryagain"),

  Line 1243:                     MessageBundle.getMessage("angal.medicalstockwardedit.invalidquantity"),

  Line 1250:                     MessageBundle.getMessage("angal.medicalstockwardedit.invalidquantitypleasetryagain"),

  Line 1251:                     MessageBundle.getMessage("angal.medicalstockwardedit.invalidquantity"),

  Line 1262:                         MessageBundle.getMessage("angal.medicalstockwardedit.invalidquantitypleasetryagain"),

  Line 1263:                         MessageBundle.getMessage("angal.medicalstockwardedit.invalidquantity"),

  Line 1293:                                     MessageBundle.getMessage("angal.newbill.qtynotinstock"),

  Line 1294:                                     MessageBundle.getMessage("angal.medicalstockwardedit.invalidquantity"),

  Line 1302:                                 MessageBundle.getMessage("angal.newbill.qtynotinstock"),

  Line 1303:                                 MessageBundle.getMessage("angal.medicalstockwardedit.invalidquantity"),

  Line 2714:                                 MessageBundle.getMessage("angal.newbill.stocknotavailableforitem")

  Line 3144:                                                     MessageBundle.getMessage("angal.newbill.stocknotavailableforitem")

  Line 3298:                 if (ex.getMessage().contains("qtynotinstock")) {

  Line 3300:                             MessageBundle.getMessage("angal.newbill.qtynotinstock"),

  Line 3358:                     MessageBundle.getMessage("angal.newbill.stocknotavailableforitem") + (price != null ? price.getDesc() : "")

  Line 3408:         boolean stockMove = GeneralData.STOCKMVTONBILLSAVE;

  Line 3449:         if ((wardCode == null || wardCode.isEmpty()) && stockMove) {

  Line 3542:                     MessageDialog.error(this, "angal.newbill.stocknotavailableforitem");


/home/uni2grow/Documents/openhospital/openhospital-gui/src/main/java/org/isf/accounting/gui/SelectPrescriptions.java:
  Line 481:                 MessageBundle.getMessage("angal.common.stock.txt"),


/home/uni2grow/Documents/openhospital/openhospital-gui/src/main/java/org/isf/stat/gui/report/GenericReportPharmaceuticalInventory.java:
  Line 24: import org.isf.medicalinventory.model.MedicalInventory;

  Line 43: 			MessageDialog.error(null, "angal.inventory.printing.error.msg");


/home/uni2grow/Documents/openhospital/openhospital-gui/src/main/java/org/isf/accounting/gui/BillItemGroupEdit.java:
  Line 712:                         MessageBundle.getMessage("angal.inventory.invalidquantity.msg")


/home/uni2grow/Documents/openhospital/openhospital-gui/src/main/java/org/isf/medicalstock/gui/MovStockBrowser.java:
  Line 22: package org.isf.medicalstock.gui;

  Line 81: import org.isf.medicalstock.manager.MovBrowserManager;

  Line 82: import org.isf.medicalstock.model.Lot;

  Line 83: import org.isf.medicalstock.model.Movement;

  Line 84: import org.isf.medstockmovtype.manager.MedicalDsrStockMovementTypeBrowserManager;

  Line 85: import org.isf.medstockmovtype.model.MovementType;

  Line 124: 	private static final String TEXT_ALLCHARGES = MessageBundle.getMessage("angal.medicalstock.allcharges.txt");

  Line 125: 	private static final String TEXT_ALLDISCHARGES = MessageBundle.getMessage("angal.medicalstock.alldischarges.txt");

  Line 159: 	private GoodDateChooser lotPrepFrom;

  Line 160: 	private GoodDateChooser lotPrepTo;

  Line 161: 	private GoodDateChooser lotDueFrom;

  Line 162: 	private GoodDateChooser lotDueTo;

  Line 170: 			MessageBundle.getMessage("angal.medicalstock.refno.col").toUpperCase(), // 1

  Line 172: 			MessageBundle.getMessage("angal.medicalstock.category.col").toUpperCase(), // 3

  Line 177: 			MessageBundle.getMessage("angal.medicalstock.pharmaceutical.col").toUpperCase(), // 7

  Line 178: 			MessageBundle.getMessage("angal.medicalstock.medtype.col").toUpperCase(), // 8


(Results truncated. Consider using a more specific path or pattern.)
```

**Tool: grep**

**Input:**
```json
{
  "pattern": "class.*Lot|class.*MedicalStock",
  "include": "*.java",
  "path": "/home/uni2grow/Documents/openhospital/openhospital-core/src"
}
```

**Output:**
```
Found 8 matches
/home/uni2grow/Documents/openhospital/openhospital-core/src/main/java/org/isf/medicalstockward/service/MedicalStockWardIoOperations.java:
  Line 54: public class MedicalStockWardIoOperations {


/home/uni2grow/Documents/openhospital/openhospital-core/src/main/java/org/isf/medicalstock/service/MedicalStockIoOperations.java:
  Line 62:  * Persistence class for MedicalStock module.

  Line 67: public class MedicalStockIoOperations {


/home/uni2grow/Documents/openhospital/openhospital-core/src/main/java/org/isf/medicalstockward/service/MedicalStockWardIoOperationRepositoryImpl.java:
  Line 54: public class MedicalStockWardIoOperationRepositoryImpl implements MedicalStockWardIoOperationRepositoryCustom {


/home/uni2grow/Documents/openhospital/openhospital-core/src/main/java/org/isf/medicalstock/model/Lot.java:
  Line 55: public class Lot extends Auditable<String> {


/home/uni2grow/Documents/openhospital/openhospital-core/src/main/java/org/isf/medicalstock/model/MedicalStock.java:
  Line 50: public class MedicalStock extends Auditable<String> {


/home/uni2grow/Documents/openhospital/openhospital-core/src/test/java/org/isf/medicalstock/TestLot.java:
  Line 34: public class TestLot {


/home/uni2grow/Documents/openhospital/openhospital-core/src/test/java/org/isf/medicalstock/TestMedicalStock.java:
  Line 33: public class TestMedicalStock {

```

---

## Assistant (Plan · Big Pickle · 11.7s)

_Thinking:_

Let me look at the inventory-related files more closely, especially the MedicalInventory package and the Lot model.

**Tool: glob**

**Input:**
```json
{
  "pattern": "**/medicalinventory/**/*.java",
  "path": "/home/uni2grow/Documents/openhospital/openhospital-gui/src"
}
```

**Output:**
```
/home/uni2grow/Documents/openhospital/openhospital-gui/src/main/java/org/isf/medicalinventory/gui/InventoryEdit.java
/home/uni2grow/Documents/openhospital/openhospital-gui/src/main/java/org/isf/medicalinventory/gui/InventoryWardBrowser.java
/home/uni2grow/Documents/openhospital/openhospital-gui/src/main/java/org/isf/medicalinventory/gui/InventoryWardEdit.java
/home/uni2grow/Documents/openhospital/openhospital-gui/src/main/java/org/isf/medicalinventory/gui/MedicalPicker.java
/home/uni2grow/Documents/openhospital/openhospital-gui/src/main/java/org/isf/medicalinventory/gui/StockMedModel.java
/home/uni2grow/Documents/openhospital/openhospital-gui/src/main/java/org/isf/medicalinventory/gui/InventoryBrowser.java
```

**Tool: glob**

**Input:**
```json
{
  "pattern": "**/medicalinventory/**/*.java",
  "path": "/home/uni2grow/Documents/openhospital/openhospital-core/src"
}
```

**Output:**
```
/home/uni2grow/Documents/openhospital/openhospital-core/src/main/java/org/isf/medicalinventory/service/MedicalInventoryRowIoOperationRepository.java
/home/uni2grow/Documents/openhospital/openhospital-core/src/main/java/org/isf/medicalinventory/service/MedicalInventoryRowIoOperation.java
/home/uni2grow/Documents/openhospital/openhospital-core/src/main/java/org/isf/medicalinventory/service/MedicalInventoryIoOperation.java
/home/uni2grow/Documents/openhospital/openhospital-core/src/main/java/org/isf/medicalinventory/service/MedicalInventoryIoOperationRepository.java
/home/uni2grow/Documents/openhospital/openhospital-core/src/main/java/org/isf/medicalinventory/model/MedicalInventoryRow.java
/home/uni2grow/Documents/openhospital/openhospital-core/src/main/java/org/isf/medicalinventory/model/InventoryStatus.java
/home/uni2grow/Documents/openhospital/openhospital-core/src/main/java/org/isf/medicalinventory/model/MedicalInventory.java
/home/uni2grow/Documents/openhospital/openhospital-core/src/main/java/org/isf/medicalinventory/model/InventoryType.java
/home/uni2grow/Documents/openhospital/openhospital-core/src/main/java/org/isf/medicalinventory/manager/MedicalInventoryRowManager.java
/home/uni2grow/Documents/openhospital/openhospital-core/src/main/java/org/isf/medicalinventory/manager/MedicalInventoryManager.java
```

**Tool: read**

**Input:**
```json
{
  "filePath": "/home/uni2grow/Documents/openhospital/openhospital-core/src/main/java/org/isf/medicalstock/model/Lot.java"
}
```

**Output:**
```
<path>/home/uni2grow/Documents/openhospital/openhospital-core/src/main/java/org/isf/medicalstock/model/Lot.java</path>
<type>file</type>
<content>
1: /*
2:  * Open Hospital (www.open-hospital.org)
3:  * Copyright © 2006-2025 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
4:  *
5:  * Open Hospital is a free and open source software for healthcare data management.
6:  *
7:  * This program is free software: you can redistribute it and/or modify
8:  * it under the terms of the GNU General Public License as published by
9:  * the Free Software Foundation, either version 3 of the License, or
10:  * (at your option) any later version.
11:  *
12:  * https://www.gnu.org/licenses/gpl-3.0-standalone.html
13:  *
14:  * This program is distributed in the hope that it will be useful,
15:  * but WITHOUT ANY WARRANTY; without even the implied warranty of
16:  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
17:  * GNU General Public License for more details.
18:  *
19:  * You should have received a copy of the GNU General Public License
20:  * along with this program. If not, see <https://www.gnu.org/licenses/>.
21:  */
22: package org.isf.medicalstock.model;
23: 
24: import java.math.BigDecimal;
25: import java.time.LocalDateTime;
26: 
27: import jakarta.persistence.AttributeOverride;
28: import jakarta.persistence.Column;
29: import jakarta.persistence.Entity;
30: import jakarta.persistence.EntityListeners;
31: import jakarta.persistence.Id;
32: import jakarta.persistence.JoinColumn;
33: import jakarta.persistence.ManyToOne;
34: import jakarta.persistence.Table;
35: import jakarta.persistence.Transient;
36: import jakarta.persistence.Version;
37: import jakarta.validation.constraints.NotNull;
38: 
39: import org.isf.generaldata.GeneralData;
40: import org.isf.generaldata.MessageBundle;
41: import org.isf.medicals.model.Medical;
42: import org.isf.medicalstockward.service.MedicalStockWardIoOperations;
43: import org.isf.utils.db.Auditable;
44: import org.isf.utils.time.TimeTools;
45: import org.springframework.data.jpa.domain.support.AuditingEntityListener;
46: 
47: @Entity
48: @Table(name="OH_MEDICALDSRLOT")
49: @EntityListeners(AuditingEntityListener.class)
50: @AttributeOverride(name = "createdBy", column = @Column(name = "LT_CREATED_BY", updatable = false))
51: @AttributeOverride(name = "createdDate", column = @Column(name = "LT_CREATED_DATE", updatable = false))
52: @AttributeOverride(name = "lastModifiedBy", column = @Column(name = "LT_LAST_MODIFIED_BY"))
53: @AttributeOverride(name = "active", column = @Column(name = "LT_ACTIVE"))
54: @AttributeOverride(name = "lastModifiedDate", column = @Column(name = "LT_LAST_MODIFIED_DATE"))
55: public class Lot extends Auditable<String> {
56: 
57: 	@Id
58: 	@Column(name="LT_ID_A")
59: 	private String code;
60: 
61: 	@NotNull
62: 	@ManyToOne
63: 	@JoinColumn(name="LT_MDSR_ID")
64: 	private Medical medical;
65: 
66: 	@NotNull
67: 	@Column(name="LT_PREP_DATE")		// SQL type: datetime
68: 	private LocalDateTime preparationDate;
69: 
70: 	@NotNull
71: 	@Column(name="LT_DUE_DATE")		   // SQL type: datetime
72: 	private LocalDateTime dueDate;
73: 
74: 	@Column(name="LT_COST")
75: 	private BigDecimal cost;
76: 
77: 	/**
78: 	 * Automatic calculated field for a lot's quantity stocked in the main store, 
79: 	 * taking in account only the main MedicalStock movements (charges and discharges).<br>
80: 	 * 
81: 	 * <i>
82: 	 * NB: COALESCE is needed for legacy connection to lots migrated from a version prior v1.11.0;
83: 	 * in theory, there should not exist lots without an initial charge movement
84: 	 * in the main MedicalStock, but sometimes it could happen if 
85: 	 * {@link MedicalStockWardIoOperations} are enabled (with {@link GeneralData}{@code .INTERNALPHARMACIES=true})
86: 	 * and some lots are registered directly there at the time of the first inventory.
87: 	 * 
88: 	 * @see <a href="https://github.com/informatici/openhospital-doc/blob/develop/doc_admin/AdminManual.adoc#5-1-19-internalpharmacies">Admin Manual</a>
89: 	 * @see <a href="https://github.com/informatici/openhospital-doc/blob/develop/doc_user/UserManual.adoc#63-pharmaceuticals-stock-ward-pharmaceuticals-stock-ward">User Manual</a>
90: 	 * </i>
91: 	 */
92: 	@Transient
93: 	private int mainStoreQuantity;
94: 	
95: 	/**
96: 	 * Automatic calculated field for a lot's quantity stocked in all wards, 
97: 	 * taking in account only the wards movements (inventories, discharges and transfers).<br>
98: 	 * 
99: 	 * <i>
100: 	 * @see <a href="https://github.com/informatici/openhospital-doc/blob/develop/doc_admin/AdminManual.adoc#5-1-19-internalpharmacies">Admin Manual</a>
101: 	 * @see <a href="https://github.com/informatici/openhospital-doc/blob/develop/doc_user/UserManual.adoc#63-pharmaceuticals-stock-ward-pharmaceuticals-stock-ward">User Manual</a>
102: 	 * </i>
103: 	 */
104: 	@Transient
105: 	private double wardsTotalQuantity;
106: 	
107: 	/**
108: 	 * Automatic calculated field for an overall lot's quantity (MedicalStock + MedicalStockWards).<br>
109: 	 * 
110: 	 * <i>
111: 	 * @see <a href="https://github.com/informatici/openhospital-doc/blob/develop/doc_admin/AdminManual.adoc#5-1-19-internalpharmacies">Admin Manual</a>
112: 	 * @see <a href="https://github.com/informatici/openhospital-doc/blob/develop/doc_user/UserManual.adoc#63-pharmaceuticals-stock-ward-pharmaceuticals-stock-ward">User Manual</a>
113: 	 * </i>
114: 	 */
115: 	@Transient
116: 	private double overallQuantity;
117: 
118: 	@Transient
119: 	private volatile int hashCode;
120: 	
121: 	@Version
122: 	@Column(name="LT_LOCK")
123: 	private int lock;
124: 
125: 	public Lot() {
126: 	}
127: 
128: 	public Lot(String aCode) {
129: 		code = aCode;
130: 	}
131: 
132: 	public Lot(Medical aMedical, String aCode, LocalDateTime aPreparationDate, LocalDateTime aDueDate) {
133: 		medical = aMedical;
134: 		code = aCode;
135: 		preparationDate = TimeTools.truncateToSeconds(aPreparationDate);
136: 		dueDate = TimeTools.truncateToSeconds(aDueDate);
137: 	}
138: 
139: 	public Lot(Medical aMedical, String aCode, LocalDateTime aPreparationDate, LocalDateTime aDueDate, BigDecimal aCost) {
140: 		medical = aMedical;
141: 		code = aCode;
142: 		preparationDate = TimeTools.truncateToSeconds(aPreparationDate);
143: 		dueDate = TimeTools.truncateToSeconds(aDueDate);
144: 		cost = aCost;
145: 	}
146: 
147: 	public String getCode() {
148: 		return code;
149: 	}
150: 
151: 	public Integer getMainStoreQuantity() {
152: 		return mainStoreQuantity;
153: 	}
154: 
155: 	public Double getWardsTotalQuantity() {
156: 		return wardsTotalQuantity;
157: 	}
158: 
159: 	public double getOverallQuantity() {
160: 		return mainStoreQuantity + wardsTotalQuantity;
161: 	}
162: 
163: 	public Medical getMedical() {
164: 		return medical;
165: 	}
166: 
167: 	public LocalDateTime getPreparationDate() {
168: 		return preparationDate;
169: 	}
170: 
171: 	public LocalDateTime getDueDate() {
172: 		return dueDate;
173: 	}
174: 
175: 	public BigDecimal getCost() {
176: 		return cost;
177: 	}
178: 
179: 	public void setCode(String aCode) {
180: 		code = aCode;
181: 	}
182: 
183: 	public void setMainStoreQuantity(int aQuantity) {
184: 		mainStoreQuantity = aQuantity;
185: 	}
186: 
187: 	public void setWardsTotalQuantity(double wardsTotalQuantity) {
188: 		this.wardsTotalQuantity = wardsTotalQuantity;
189: 	}
190: 
191: 	public void setPreparationDate(LocalDateTime aPreparationDate) {
192: 		preparationDate = TimeTools.truncateToSeconds(aPreparationDate);
193: 	}
194: 
195: 	public void setMedical(Medical aMedical) {
196: 		medical = aMedical;
197: 	}
198: 
199: 	public void setDueDate(LocalDateTime aDueDate) {
200: 		dueDate = TimeTools.truncateToSeconds(aDueDate);
201: 	}
202: 
203: 	public void setCost(BigDecimal cost) {
204: 		this.cost = cost;
205: 	}
206: 	
207: 	public int getLock() {
208: 		return lock;
209: 	}
210: 	
211: 	public void setLock(int lock) {
212: 		this.lock = lock;
213: 	}
214: 
215: 	@Override
216: 	public String toString() {
217: 		if (code == null) {
218: 			return MessageBundle.getMessage("angal.medicalstock.nolot.txt");
219: 		}
220: 		return getCode();
221: 	}
222: 
223: 	public boolean isValidLot() {
224: 		return getCode().length() <= 50;
225: 	}
226: 
227: 	@Override
228: 	public boolean equals(Object obj) {
229: 		if (this == obj) {
230: 			return true;
231: 		}
232: 		if (obj == null) {
233: 			return false;
234: 		}
235: 		if (getClass() != obj.getClass()) {
236: 			return false;
237: 		}
238: 		Lot other = (Lot) obj;
239: 		if (code == null) {
240: 			if (other.code != null) {
241: 				return false;
242: 			}
243: 		} else if (!code.equals(other.code)) {
244: 			return false;
245: 		}
246: 		if (cost != null) {
247: 			if (other.cost != null && cost.compareTo(other.cost) != 0) {
248: 				return false;
249: 			}
250: 		}
251: 		if (dueDate == null) {
252: 			if (other.dueDate != null) {
253: 				return false;
254: 			}
255: 		} else if (!dueDate.equals(other.dueDate)) {
256: 			return false;
257: 		}
258: 		if (preparationDate == null) {
259: 			if (other.preparationDate != null) {
260: 				return false;
261: 			}
262: 		} else if (!preparationDate.equals(other.preparationDate)) {
263: 			return false;
264: 		}
265: 		return mainStoreQuantity == other.mainStoreQuantity;
266: 	}
267: 
268: 	@Override
269: 	public int hashCode() {
270: 	    if (this.hashCode == 0) {
271: 	        final int m = 23;
272: 	        int c = 133;
273: 	        
274: 	        c = m * c + code.hashCode();
275: 	        
276: 	        this.hashCode = c;
277: 	    }
278: 	  
279: 	    return this.hashCode;
280: 	}	
281: }

(End of file - total 281 lines)
</content>
```

---

## Assistant (Plan · Big Pickle · 16.0s)

_Thinking:_

Now let me look at the InventoryEdit.java and MedicalPicker.java to understand how medications are selected and lots are displayed in the inventory.

**Tool: read**

**Input:**
```json
{
  "filePath": "/home/uni2grow/Documents/openhospital/openhospital-gui/src/main/java/org/isf/medicalinventory/gui/InventoryEdit.java"
}
```

**Output:**
```
<path>/home/uni2grow/Documents/openhospital/openhospital-gui/src/main/java/org/isf/medicalinventory/gui/InventoryEdit.java</path>
<type>file</type>
<content>
1: /*
2:  * Open Hospital (www.open-hospital.org)
3:  * Copyright © 2006-2025 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
4:  *
5:  * Open Hospital is a free and open source software for healthcare data management.
6:  *
7:  * This program is free software: you can redistribute it and/or modify
8:  * it under the terms of the GNU General Public License as published by
9:  * the Free Software Foundation, either version 3 of the License, or
10:  * (at your option) any later version.
11:  *
12:  * https://www.gnu.org/licenses/gpl-3.0-standalone.html
13:  *
14:  * This program is distributed in the hope that it will be useful,
15:  * but WITHOUT ANY WARRANTY; without even the implied warranty of
16:  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
17:  * GNU General Public License for more details.
18:  *
19:  * You should have received a copy of the GNU General Public License
20:  * along with this program. If not, see <https://www.gnu.org/licenses/>.
21:  */
22: package org.isf.medicalinventory.gui;
23: 
24: import static org.isf.utils.Constants.DATE_FORMATTER;
25: 
26: import java.awt.AWTEvent;
27: import java.awt.BorderLayout;
28: import java.awt.Color;
29: import java.awt.Component;
30: import java.awt.Dimension;
31: import java.awt.FlowLayout;
32: import java.awt.Font;
33: import java.awt.GridBagConstraints;
34: import java.awt.GridBagLayout;
35: import java.awt.GridLayout;
36: import java.awt.Insets;
37: import java.awt.event.KeyAdapter;
38: import java.awt.event.KeyEvent;
39: import java.awt.event.WindowAdapter;
40: import java.awt.event.WindowEvent;
41: import java.math.BigDecimal;
42: import java.time.LocalDate;
43: import java.time.LocalDateTime;
44: import java.time.temporal.ChronoUnit;
45: import java.util.ArrayList;
46: import java.util.Collections;
47: import java.util.Comparator;
48: import java.util.EventListener;
49: import java.util.HashMap;
50: import java.util.HashSet;
51: import java.util.List;
52: import java.util.ListIterator;
53: import java.util.Map;
54: import java.util.Objects;
55: import java.util.Set;
56: import java.util.TreeMap;
57: import java.util.stream.Collectors;
58: 
59: import javax.swing.AbstractButton;
60: import javax.swing.BorderFactory;
61: import javax.swing.ButtonGroup;
62: import javax.swing.DefaultCellEditor;
63: import javax.swing.JButton;
64: import javax.swing.JComboBox;
65: import javax.swing.JDialog;
66: import javax.swing.JFrame;
67: import javax.swing.JLabel;
68: import javax.swing.JOptionPane;
69: import javax.swing.JPanel;
70: import javax.swing.JRadioButton;
71: import javax.swing.JScrollPane;
72: import javax.swing.JTable;
73: import javax.swing.JTextField;
74: import javax.swing.SwingConstants;
75: import javax.swing.SwingUtilities;
76: import javax.swing.WindowConstants;
77: import javax.swing.border.EmptyBorder;
78: import javax.swing.event.EventListenerList;
79: import javax.swing.table.DefaultTableCellRenderer;
80: import javax.swing.table.DefaultTableModel;
81: 
82: import org.isf.generaldata.GeneralData;
83: import org.isf.generaldata.MessageBundle;
84: import org.isf.medicalinventory.manager.MedicalInventoryManager;
85: import org.isf.medicalinventory.manager.MedicalInventoryRowManager;
86: import org.isf.medicalinventory.model.InventoryStatus;
87: import org.isf.medicalinventory.model.InventoryType;
88: import org.isf.medicalinventory.model.MedicalInventory;
89: import org.isf.medicalinventory.model.MedicalInventoryRow;
90: import org.isf.medicals.manager.MedicalBrowsingManager;
91: import org.isf.medicals.model.Medical;
92: import org.isf.medicalstock.manager.MovBrowserManager;
93: import org.isf.medicalstock.manager.MovStockInsertingManager;
94: import org.isf.medicalstock.model.Lot;
95: import org.isf.medicalstock.model.Movement;
96: import org.isf.medstockmovtype.manager.MedicalDsrStockMovementTypeBrowserManager;
97: import org.isf.medstockmovtype.model.MovementType;
98: import org.isf.medtype.manager.MedicalTypeBrowserManager;
99: import org.isf.medtype.model.MedicalType;
100: import org.isf.menu.manager.Context;
101: import org.isf.menu.manager.UserBrowsingManager;
102: import org.isf.stat.gui.report.GenericReportPharmaceuticalInventory;
103: import org.isf.supplier.manager.SupplierBrowserManager;
104: import org.isf.supplier.model.Supplier;
105: import org.isf.utils.db.NormalizeString;
106: import org.isf.utils.exception.OHServiceException;
107: import org.isf.utils.exception.gui.OHServiceExceptionUtil;
108: import org.isf.utils.jobjects.GoodDateChooser;
109: import org.isf.utils.jobjects.GoodDateTimeSpinnerChooser;
110: import org.isf.utils.jobjects.GoodDateTimeToggleChooser;
111: import org.isf.utils.jobjects.MessageDialog;
112: import org.isf.utils.jobjects.ModalJFrame;
113: import org.isf.utils.jobjects.RequestFocusListener;
114: import org.isf.utils.jobjects.TextPrompt;
115: import org.isf.utils.jobjects.TextPrompt.Show;
116: import org.isf.utils.jobjects.VoLimitedTextField;
117: import org.isf.utils.time.TimeTools;
118: import org.isf.ward.manager.WardBrowserManager;
119: import org.isf.ward.model.Ward;
120: 
121: public class InventoryEdit extends ModalJFrame {
122: 
123: 	private static final long serialVersionUID = 1L;
124: 
125: 	private static EventListenerList inventoryListeners = new EventListenerList();
126: 
127: 	public interface InventoryListener extends EventListener {
128: 
129: 		void inventoryInserted(AWTEvent e);
130: 
131: 		void inventoryUpdated(AWTEvent e);
132: 
133: 		void inventoryCancelled(AWTEvent e);
134: 	}
135: 
136: 	public static void addInventoryListener(InventoryListener listener) {
137: 		inventoryListeners.add(InventoryListener.class, listener);
138: 	}
139: 
140: 	public static void removeInventoryListener(InventoryListener listener) {
141: 		inventoryListeners.remove(InventoryListener.class, listener);
142: 	}
143: 
144: 	private void fireInventoryUpdated() {
145: 		AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {
146: 
147: 			private static final long serialVersionUID = 1L;
148: 		};
149: 
150: 		EventListener[] listeners = inventoryListeners.getListeners(InventoryListener.class);
151: 		for (EventListener listener : listeners) {
152: 			((InventoryListener) listener).inventoryUpdated(event);
153: 		}
154: 		jCalendarInventoryDate.setDateTime(dateInventory);
155: 		jTableInventoryRow.updateUI();
156: 	}
157: 
158: 	private void fireInventoryInserted() {
159: 		AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {
160: 
161: 			private static final long serialVersionUID = 1L;
162: 		};
163: 
164: 		EventListener[] listeners = inventoryListeners.getListeners(InventoryListener.class);
165: 		for (EventListener listener : listeners) {
166: 			((InventoryListener) listener).inventoryInserted(event);
167: 		}
168: 		jCalendarInventoryDate.setDateTime(dateInventory);
169: 		jTableInventoryRow.updateUI();
170: 	}
171: 
172: 	private GoodDateTimeToggleChooser jCalendarInventoryDate;
173: 	private LocalDateTime dateInventory = TimeTools.getNow().truncatedTo(ChronoUnit.MINUTES);
174: 	private JPanel panelHeader;
175: 	private JPanel panelFooter;
176: 	private JPanel panelContent;
177: 	private JButton closeButton;
178: 	private JButton printButton;
179: 	private JButton deleteButton;
180: 	private JButton saveButton;
181: 	private JButton resetButton;
182: 	private JButton lotButton;
183: 	private JButton validateButton;
184: 	private JButton confirmButton;
185: 	private JScrollPane scrollPaneInventory;
186: 	private JTable jTableInventoryRow;
187: 	private DefaultTableModel model;
188: 	private List<MedicalInventoryRow> inventoryRowList = new ArrayList<>();
189: 	private List<MedicalInventoryRow> inventoryRowSearchList = new ArrayList<>();
190: 	private List<MedicalInventoryRow> inventoryRowsToDelete = new ArrayList<>();
191: 	private List<MedicalInventoryRow> inventoryRowListAdded = new ArrayList<>();
192: 	private List<Lot> lotsSaved = new ArrayList<>();
193: 	private List<Lot> lotsDeleted = new ArrayList<>();
194: 	private String[] columsNames = { MessageBundle.getMessage("angal.inventory.id.col").toUpperCase(),
195: 			MessageBundle.getMessage("angal.common.code.txt").toUpperCase(),
196: 			MessageBundle.getMessage("angal.inventory.medical.col").toUpperCase(),
197: 			MessageBundle.getMessage("angal.inventory.newlot.col").toUpperCase(),
198: 			MessageBundle.getMessage("angal.inventory.lotcode.col").toUpperCase(),
199: 			MessageBundle.getMessage("angal.medicalstock.duedate.col").toUpperCase(),
200: 			MessageBundle.getMessage("angal.inventory.theoreticalqty.col").toUpperCase(),
201: 			MessageBundle.getMessage("angal.inventory.realqty.col").toUpperCase(),
202: 			MessageBundle.getMessage("angal.inventory.difference.col").toUpperCase(),
203: 			MessageBundle.getMessage("angal.inventory.unitcost.col").toUpperCase(),
204: 			MessageBundle.getMessage("angal.inventory.totalcost.col").toUpperCase()
205: 	};
206: 	private int[] columwidth = { 50, 50, 200, 100, 100, 100, 100, 80, 80, 80, 80 };
207: 	private boolean[] columnEditable = { false, false, false, false, false, false, false, true, false, false, false };
208: 	private boolean[] columnVisible = { false, true, true, true, !GeneralData.AUTOMATICLOT_IN, true, true, true, true, GeneralData.LOTWITHCOST,
209: 			GeneralData.LOTWITHCOST };
210: 	private boolean[] columnCentered = { false, false, false, true, true, true, true, true, true, true, true };
211: 	private boolean[] columnDecimalNumber = { false, false, false, false, false, false, false, false, false, true, true };
212: 	private Class< ? >[] columnsClasses = { String.class, Integer.class, String.class, String.class, String.class, LocalDate.class, Integer.class,
213: 			Integer.class, Integer.class, BigDecimal.class, BigDecimal.class };
214: 	private MedicalInventory inventory;
215: 	private JLabel addMedicalLabel;
216: 	private JButton selectButton;
217: 	private JLabel dateInventoryLabel;
218: 	private JTextField medicalCodeTextField;
219: 	private String code;
220: 	private String mode;
221: 	private JLabel statusLabel;
222: 	private JLabel referenceLabel;
223: 	private JLabel dischargeTypeLabel;
224: 	private JLabel addMedicalsByLabel;
225: 	private JTextField referenceTextField;
226: 	private JComboBox<MovementType> dischargeCombo;
227: 	private MovementType dischargeType;
228: 	private String newReference;
229: 	private JFrame frame;
230: 	private JPanel mainPanel;
231: 	private JRadioButton radioButtonAll;
232: 	private JRadioButton radioOnlyNonZero;
233: 	private JRadioButton radioWithMovement;
234: 	private JButton jButtonCancel;
235: 	private JButton jButtonOk;
236: 	private MedicalType medicalTypeSelected;
237: 	private JComboBox<MedicalType> medicalTypeComboBox;
238: 	private List<Medical> medicals = new ArrayList<>();
239: 	private final Map<AbstractButton, Runnable> actions = new HashMap<>();
240: 	private final MedicalInventoryManager medicalInventoryManager = Context.getApplicationContext().getBean(MedicalInventoryManager.class);
241: 	private final MedicalInventoryRowManager medicalInventoryRowManager = Context.getApplicationContext().getBean(MedicalInventoryRowManager.class);
242: 	private final MedicalBrowsingManager medicalBrowsingManager = Context.getApplicationContext().getBean(MedicalBrowsingManager.class);
243: 	private final MovStockInsertingManager movStockInsertingManager = Context.getApplicationContext().getBean(MovStockInsertingManager.class);
244: 	private final MedicalDsrStockMovementTypeBrowserManager movTypeManager = Context.getApplicationContext().getBean(MedicalDsrStockMovementTypeBrowserManager.class);
245: 	private final SupplierBrowserManager supplierManager = Context.getApplicationContext().getBean(SupplierBrowserManager.class);
246: 	private final WardBrowserManager wardManager = Context.getApplicationContext().getBean(WardBrowserManager.class);
247: 	private final MedicalTypeBrowserManager medicalTypeManager = Context.getApplicationContext().getBean(MedicalTypeBrowserManager.class);
248: 	private final MovBrowserManager movBrowserManager = Context.getApplicationContext().getBean(MovBrowserManager.class);
249: 	private boolean allMedicals;
250: 	private boolean allMedicalsChosen;
251: 	private final Object[] allMedicalsOrList = {
252: 			MessageBundle.getMessage("angal.inventory.yesallmedicals.btn"),
253: 			MessageBundle.getMessage("angal.inventory.noonlytheonesinthelist.btn")
254: 	};
255: 
256: 	public InventoryEdit() {
257: 		mode = "new";
258: 		initComponents();
259: 	}
260: 
261: 	private boolean isAutomaticLotIn() {
262: 		return GeneralData.AUTOMATICLOT_IN;
263: 	}
264: 
265: 	private boolean isLotWithCost() {
266: 		return GeneralData.LOTWITHCOST;
267: 	}
268: 
269: 	public InventoryEdit(MedicalInventory inventory, String mod) {
270: 		this.inventory = inventory;
271: 		mode = mod;
272: 		initComponents();
273: 	}
274: 
275: 	private void initComponents() {
276: 		inventoryRowList = new ArrayList<>();
277: 		inventoryRowSearchList = new ArrayList<>();
278: 		try {
279: 			medicals = medicalBrowsingManager.getMedicals();
280: 		} catch (OHServiceException e) {
281: 			OHServiceExceptionUtil.showMessages(e);
282: 		}
283: 		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
284: 		setMinimumSize(new Dimension(1000, 600));
285: 		setLocationRelativeTo(null);
286: 		if (mode.equals("new")) {
287: 			setTitle(MessageBundle.getMessage("angal.inventory.newinventory.title"));
288: 		}
289: 		if (mode.equals("view")) {
290: 			setTitle(MessageBundle.getMessage("angal.inventory.viewinventory.title"));
291: 		}
292: 		if (mode.equals("update")) {
293: 			setTitle(MessageBundle.getMessage("angal.inventory.editinventory.title"));
294: 		}
295: 		getContentPane().setLayout(new BorderLayout());
296: 		panelHeader = getPanelHeader();
297: 		getContentPane().add(panelHeader, BorderLayout.NORTH);
298: 		panelContent = getPanelContent();
299: 		getContentPane().add(panelContent, BorderLayout.CENTER);
300: 		panelFooter = getPanelFooter();
301: 		getContentPane().add(panelFooter, BorderLayout.SOUTH);
302: 		addWindowListener(new WindowAdapter() {
303: 
304: 			@Override
305: 			public void windowClosing(WindowEvent e) {
306: 				closeButton.doClick();
307: 			}
308: 		});
309: 		if (mode.equals("view")) {
310: 			saveButton.setVisible(false);
311: 			validateButton.setVisible(false);
312: 			confirmButton.setVisible(false);
313: 			deleteButton.setVisible(false);
314: 			resetButton.setVisible(false);
315: 			referenceTextField.setEditable(false);
316: 			jCalendarInventoryDate.setEnabled(false);
317: 			dischargeCombo.setEnabled(false);
318: 			lotButton.setVisible(false);
319: 			printButton.setVisible(true);
320: 			selectButton.setEnabled(false);
321: 			medicalCodeTextField.setEnabled(false);
322: 
323: 		} else {
324: 			saveButton.setVisible(true);
325: 			validateButton.setVisible(true);
326: 			deleteButton.setVisible(true);
327: 			resetButton.setVisible(true);
328: 			referenceTextField.setEditable(true);
329: 			jCalendarInventoryDate.setEnabled(true);
330: 			dischargeCombo.setEnabled(true);
331: 			lotButton.setVisible(true);
332:             confirmButton.setEnabled(inventory != null && inventory.getStatus().equals(InventoryStatus.validated.toString()));
333: 			printButton.setVisible(false);
334: 		}
335: 	}
336: 
337: 	private JPanel getPanelHeader() {
338: 		if (panelHeader == null) {
339: 			panelHeader = new JPanel();
340: 			panelHeader.setBorder(new EmptyBorder(5, 0, 5, 0));
341: 			GridBagLayout gbl_panelHeader = new GridBagLayout();
342: 			gbl_panelHeader.columnWidths = new int[] { 159, 191, 192, 218, 218, 0 };
343: 			gbl_panelHeader.rowHeights = new int[] { 30, 30, 30, 30, 0 };
344: 			gbl_panelHeader.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
345: 			gbl_panelHeader.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
346: 			panelHeader.setLayout(gbl_panelHeader);
347: 			GridBagConstraints gbc_dateInventoryLabel = new GridBagConstraints();
348: 			gbc_dateInventoryLabel.anchor = GridBagConstraints.EAST;
349: 			gbc_dateInventoryLabel.insets = new Insets(0, 0, 5, 5);
350: 			gbc_dateInventoryLabel.gridx = 0;
351: 			gbc_dateInventoryLabel.gridy = 0;
352: 			panelHeader.add(getDateInventoryLabel(), gbc_dateInventoryLabel);
353: 			GridBagConstraints gbc_jCalendarInventory = new GridBagConstraints();
354: 			gbc_jCalendarInventory.fill = GridBagConstraints.HORIZONTAL;
355: 			gbc_jCalendarInventory.insets = new Insets(0, 0, 5, 5);
356: 			gbc_jCalendarInventory.gridx = 1;
357: 			gbc_jCalendarInventory.gridy = 0;
358: 			panelHeader.add(getJCalendarInventoryDate(), gbc_jCalendarInventory);
359: 			GridBagConstraints gbc_referenceLabel = new GridBagConstraints();
360: 			gbc_referenceLabel.anchor = GridBagConstraints.EAST;
361: 			gbc_referenceLabel.insets = new Insets(0, 0, 5, 5);
362: 			gbc_referenceLabel.gridx = 2;
363: 			gbc_referenceLabel.gridy = 0;
364: 			panelHeader.add(getReferenceLabel(), gbc_referenceLabel);
365: 			GridBagConstraints gbc_referenceTextField = new GridBagConstraints();
366: 			gbc_referenceTextField.fill = GridBagConstraints.HORIZONTAL;
367: 			gbc_referenceTextField.insets = new Insets(0, 0, 5, 5);
368: 			gbc_referenceTextField.gridx = 3;
369: 			gbc_referenceTextField.gridy = 0;
370: 			panelHeader.add(getReferenceTextField(), gbc_referenceTextField);
371: 			GridBagConstraints gbc_statusLabel = new GridBagConstraints();
372: 			gbc_statusLabel.anchor = GridBagConstraints.CENTER;
373: 			gbc_statusLabel.insets = new Insets(0, 0, 5, 5);
374: 			gbc_statusLabel.gridx = 4;
375: 			gbc_statusLabel.gridy = 0;
376: 			gbc_statusLabel.gridheight = 3;
377: 			panelHeader.add(getStatusLabel(), gbc_statusLabel);
378: 			GridBagConstraints gbc_dischargeLabel = new GridBagConstraints();
379: 			gbc_dischargeLabel.anchor = GridBagConstraints.EAST;
380: 			gbc_dischargeLabel.insets = new Insets(0, 0, 5, 5);
381: 			gbc_dischargeLabel.gridx = 0;
382: 			gbc_dischargeLabel.gridy = 2;
383: 			panelHeader.add(getDischargeLabel(), gbc_dischargeLabel);
384: 			GridBagConstraints gbc_dichargeCombo = new GridBagConstraints();
385: 			gbc_dichargeCombo.fill = GridBagConstraints.HORIZONTAL;
386: 			gbc_dichargeCombo.insets = new Insets(0, 0, 5, 5);
387: 			gbc_dichargeCombo.gridx = 1;
388: 			gbc_dichargeCombo.gridy = 2;
389: 			panelHeader.add(getJComboDischarge(), gbc_dichargeCombo);
390: 			GridBagConstraints gbc_addMedicalLabel = new GridBagConstraints();
391: 			gbc_addMedicalLabel.anchor = GridBagConstraints.EAST;
392: 			gbc_addMedicalLabel.insets = new Insets(0, 0, 0, 5);
393: 			gbc_addMedicalLabel.gridx = 0;
394: 			gbc_addMedicalLabel.gridy = 3;
395: 			panelHeader.add(getAddMedicalLabel(), gbc_addMedicalLabel);
396: 			GridBagConstraints gbc_medicalCodeTextField = new GridBagConstraints();
397: 			gbc_medicalCodeTextField.insets = new Insets(0, 0, 0, 5);
398: 			gbc_medicalCodeTextField.fill = GridBagConstraints.HORIZONTAL;
399: 			gbc_medicalCodeTextField.gridx = 1;
400: 			gbc_medicalCodeTextField.gridy = 3;
401: 			panelHeader.add(getMedicalCodeTextField(), gbc_medicalCodeTextField);
402: 			GridBagConstraints gbc_addMedicalsByLabel = new GridBagConstraints();
403: 			gbc_addMedicalsByLabel.anchor = GridBagConstraints.EAST;
404: 			gbc_addMedicalsByLabel.insets = new Insets(0, 0, 0, 5);
405: 			gbc_addMedicalsByLabel.gridx = 2;
406: 			gbc_addMedicalsByLabel.gridy = 3;
407: 			panelHeader.add(getAddMedicalsByLabel(), gbc_addMedicalsByLabel);
408: 			GridBagConstraints gbc_selectButton = new GridBagConstraints();
409: 			gbc_selectButton.anchor = GridBagConstraints.WEST;
410: 			gbc_selectButton.insets = new Insets(0, 0, 0, 5);
411: 			gbc_selectButton.gridx = 3;
412: 			gbc_selectButton.gridy = 3;
413: 			panelHeader.add(getSelectButton(), gbc_selectButton);
414: 		}
415: 		return panelHeader;
416: 	}
417: 
418: 	private JPanel getPanelContent() {
419: 		if (panelContent == null) {
420: 			panelContent = new JPanel();
421: 			GridBagLayout gbl_panelContent = new GridBagLayout();
422: 			gbl_panelContent.columnWidths = new int[] { 452, 0 };
423: 			gbl_panelContent.rowHeights = new int[] { 402, 0 };
424: 			gbl_panelContent.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
425: 			gbl_panelContent.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
426: 			panelContent.setLayout(gbl_panelContent);
427: 			GridBagConstraints gbc_scrollPaneInventory = new GridBagConstraints();
428: 			gbc_scrollPaneInventory.fill = GridBagConstraints.BOTH;
429: 			gbc_scrollPaneInventory.gridx = 0;
430: 			gbc_scrollPaneInventory.gridy = 0;
431: 			panelContent.add(getScrollPaneInventory(), gbc_scrollPaneInventory);
432: 		}
433: 		return panelContent;
434: 	}
435: 
436: 	private JPanel getPanelFooter() {
437: 		if (panelFooter == null) {
438: 			panelFooter = new JPanel();
439: 			panelFooter.add(getSaveButton());
440: 			panelFooter.add(getLotButton());
441: 			panelFooter.add(getDeleteButton());
442: 			panelFooter.add(getCleanTableButton());
443: 			panelFooter.add(getValidateButton());
444: 			panelFooter.add(getConfirmButton());
445: 			panelFooter.add(getPrintButton());
446: 			panelFooter.add(getCloseButton());
447: 		}
448: 		return panelFooter;
449: 	}
450: 
451: 	private GoodDateTimeToggleChooser getJCalendarInventoryDate() {
452: 		if (jCalendarInventoryDate == null) {
453: 
454: 			jCalendarInventoryDate = new GoodDateTimeToggleChooser(TimeTools.getNow());
455: 			if (inventory != null) {
456: 				jCalendarInventoryDate.setDateTime(inventory.getInventoryDate());
457: 				dateInventory = inventory.getInventoryDate();
458: 			}
459: 			jCalendarInventoryDate.addDateTimeChangeListener(event -> {
460: 				dateInventory = jCalendarInventoryDate.getLocalDateTime();
461: 			});
462: 		}
463: 		return jCalendarInventoryDate;
464: 	}
465: 
466: 	private JButton getSelectButton() {
467: 		if (selectButton == null) {
468: 			selectButton = new JButton(MessageBundle.getMessage("angal.inventory.select.btn"));
469: 			selectButton.addActionListener(actionEvent -> {
470: 				mainPanel = new JPanel();
471: 				mainPanel.setLayout(new BorderLayout(10, 10));
472: 
473: 				JPanel leftPanel = new JPanel();
474: 				leftPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
475: 				leftPanel.setLayout(new GridLayout(3, 1));
476: 				leftPanel.add(new JLabel(MessageBundle.getMessage("angal.inventory.medicaltype.txt")));
477: 				leftPanel.add(getJComboMedicalType());
478: 
479: 				JPanel rightPanel = new JPanel();
480: 				rightPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
481: 				rightPanel.setLayout(new GridLayout(3, 1));
482: 				ButtonGroup radioGroup = new ButtonGroup();
483: 				radioGroup.add(getAllRadioButton());
484: 				radioGroup.add(getMedicalWithNonZeroQuantityRadioButton());
485: 				radioGroup.add(getMedicalWithMovementRadioButton());
486: 
487: 				// Map actions to buttons
488: 				initializeActions();
489: 
490: 				// Convert HashMap to TreeMap to sort keys
491: 				Map<AbstractButton, Runnable> sortedActionMap = new TreeMap<>(Comparator.comparing(AbstractButton::getText));
492: 				sortedActionMap.putAll(actions);
493: 
494: 				// Add ActionListener to each button
495: 				sortedActionMap.forEach((key, value) -> rightPanel.add(key));
496: 
497: 				JPanel bottomPanel = new JPanel();
498: 				bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
499: 				bottomPanel.add(getOkButton());
500: 				bottomPanel.add(getCancelButton());
501: 
502: 				mainPanel.add(leftPanel, BorderLayout.CENTER);
503: 				mainPanel.add(rightPanel, BorderLayout.EAST);
504: 				mainPanel.add(bottomPanel, BorderLayout.SOUTH);
505: 
506: 				frame = new JFrame();
507: 				frame.add(mainPanel);
508: 				frame.setSize(450, 200);
509: 				frame.setTitle(MessageBundle.getMessage("angal.inventory.lotinformation.title"));
510: 				frame.setLocationRelativeTo(null);
511: 				frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
512: 				frame.addWindowListener(new WindowAdapter() {
513: 
514: 					@Override
515: 					public void windowClosing(WindowEvent e) {
516: 						int choice = MessageDialog.yesNo(frame, "angal.inventory.areyousureyouwantoclosethiswindow.msg");
517: 						if (choice == JOptionPane.YES_OPTION) {
518: 							frame.dispose();
519: 						}
520: 					}
521: 				});
522: 				frame.setVisible(true);
523: 			});
524: 		}
525: 		return selectButton;
526: 	}
527: 
528: 	private JButton getSaveButton() {
529: 		saveButton = new JButton(MessageBundle.getMessage("angal.common.save.btn"));
530: 		saveButton.setMnemonic(MessageBundle.getMnemonic("angal.common.save.btn.key"));
531: 		saveButton.addActionListener(actionEvent -> {
532: 			try {
533: 				if (inventoryRowSearchList == null || inventoryRowSearchList.isEmpty()) {
534: 					MessageDialog.error(null, "angal.inventory.cannotsaveinventorywithoutproducts.msg");
535: 					return;
536: 				}
537: 				String status = InventoryStatus.draft.toString();
538: 				String user = UserBrowsingManager.getCurrentUser();
539: 
540: 				if (mode.equals("new")) {
541: 					inventory = new MedicalInventory();
542: 					inventory.setInventoryReference(referenceTextField.getText().trim());
543: 					inventory.setInventoryDate(dateInventory);
544: 					inventory.setStatus(status);
545: 					inventory.setUser(user);
546: 					inventory.setInventoryType(InventoryType.main.toString());
547: 					inventory.setChargeType(null);
548: 					inventory.setDischargeType(dischargeType == null ? null : dischargeType.getCode());
549: 					inventory.setSupplier(null);
550: 					inventory.setDestination(null);
551: 
552: 					List<MedicalInventoryRow> newMedicalInventoryRows = new ArrayList<>();
553: 					ListIterator<MedicalInventoryRow> inventoryRowSearchListIterator = inventoryRowSearchList.listIterator();
554: 					while (inventoryRowSearchListIterator.hasNext()) {
555: 						MedicalInventoryRow medicalInventoryRow = inventoryRowSearchListIterator.next();
556: 						medicalInventoryRow.setInventory(inventory);
557: 						Lot lot = medicalInventoryRow.getLot();
558: 						Medical medical = medicalInventoryRow.getMedical();
559: 
560: 						if (lot != null) {
561: 							String lotCode = lot.getCode();
562: 							Lot lotExist = movStockInsertingManager.getLot(lotCode);
563: 							if (lotExist != null) {
564: 								Lot lotStore = movStockInsertingManager.updateLot(lot);
565: 								medicalInventoryRow.setLot(lotStore);
566: 							} else {
567: 								if (lot.getDueDate() != null) {
568: 									Lot lotStore = movStockInsertingManager.storeLot(lotCode, lot, medical);
569: 									medicalInventoryRow.setLot(lotStore);
570: 									medicalInventoryRow.setNewLot(true);
571: 								} else {
572: 									medicalInventoryRow.setLot(null);
573: 								}
574: 							}
575: 						}
576:                         medicalInventoryRow.setId(null);
577: 						newMedicalInventoryRows.add(medicalInventoryRow);
578: 					}
579: 					inventory = medicalInventoryManager.newMedicalInventory(inventory, newMedicalInventoryRows);
580: 					mode = "update";
581: 					newReference = inventory.getInventoryReference();
582: 					fireInventoryInserted();
583: 					MessageDialog.info(this, "angal.inventory.savesuccess.msg");
584: 					validateButton.setEnabled(true);
585: 					confirmButton.setEnabled(false);
586: 					resetVariables();
587: 
588: 				} else if (mode.equals("update")) {
589: 					int response = MessageDialog.yesNo(null, "angal.inventory.doyouwanttoupdatethisinventory.msg");
590: 					if (response == JOptionPane.YES_OPTION) {
591: 						String oldReference = inventory.getInventoryReference();
592: 						newReference = referenceTextField.getText().trim();
593: 						inventory.setInventoryReference(newReference);
594: 						inventory.setInventoryDate(dateInventory);
595: 						inventory.setStatus(status);
596: 						inventory.setUser(user);
597: 						inventory.setInventoryType(InventoryType.main.toString());
598: 						inventory.setChargeType(null);
599: 						inventory.setDischargeType(dischargeType == null ? null : dischargeType.getCode());
600: 						inventory.setSupplier(null);
601: 						inventory.setDestination(null);
602: 						inventory = medicalInventoryManager.updateMedicalInventory(inventory, !oldReference.equals(newReference));
603: 
604: 						ListIterator<MedicalInventoryRow> inventoryRowSearchListIterator = inventoryRowSearchList.listIterator();
605: 						while (inventoryRowSearchListIterator.hasNext()) {
606: 							MedicalInventoryRow medicalInventoryRow = inventoryRowSearchListIterator.next();
607: 							Medical medical = medicalInventoryRow.getMedical();
608: 							Lot lot = medicalInventoryRow.getLot();
609: 							if (lot != null) {
610: 								String lotCode = lot.getCode();
611: 								Lot lotExist = movStockInsertingManager.getLot(lotCode);
612: 								if (lotExist != null) {
613: 									lot.setMedical(medical);
614: 									lot = movStockInsertingManager.updateLot(lot);
615: 									medicalInventoryRow.setLot(lot);
616: 								} else {
617: 									int idInvRow = medicalInventoryRow.getId();
618: 									MedicalInventoryRow invRow = medicalInventoryRowManager.getMedicalInventoryRowById(idInvRow);
619: 									if (invRow != null && invRow.getLock() != medicalInventoryRow.getLock()) {
620: 										Lot newLot = movStockInsertingManager.storeLot(lotCode, lot, medical);
621: 										invRow.setLot(newLot);
622: 										invRow.setNewLot(true);
623: 										invRow.setRealqty(medicalInventoryRow.getRealQty());
624: 										medicalInventoryRow = invRow;
625: 									} else {
626: 										Lot newLot = movStockInsertingManager.storeLot(lotCode, lot, medical);
627: 										medicalInventoryRow.setLot(newLot);
628: 										medicalInventoryRow.setNewLot(true);
629: 									}
630: 								}
631: 							}
632: 
633: 							medicalInventoryRow.setInventory(inventory);
634: 							int id = medicalInventoryRow.getId();
635: 							if (id == 0) {
636:                                 medicalInventoryRow.setId(null);
637: 								MedicalInventoryRow savedRow = medicalInventoryRowManager.newMedicalInventoryRow(medicalInventoryRow);
638: 								inventoryRowSearchListIterator.set(savedRow);
639: 							} else {
640: 								MedicalInventoryRow updatedRow = medicalInventoryRowManager.updateMedicalInventoryRow(medicalInventoryRow);
641: 								inventoryRowSearchListIterator.set(updatedRow);
642: 							}
643: 						}
644: 
645: 						fireInventoryUpdated();
646: 						if (!lotsDeleted.isEmpty() || !inventoryRowsToDelete.isEmpty()) {
647: 							lotsDeleted.removeAll(
648: 								inventoryRowsToDelete.stream()
649: 									.map(MedicalInventoryRow::getLot)
650: 									.filter(Objects::nonNull)
651: 									.collect(Collectors.toSet()));
652: 
653: 							if (!inventoryRowsToDelete.isEmpty()) {
654: 								medicalInventoryRowManager.deleteMedicalInventoryRows(inventoryRowsToDelete);
655: 							}
656: 							for (Lot lot : lotsDeleted) {
657: 								movStockInsertingManager.deleteLot(lot);
658: 							}
659: 						}
660: 
661: 						MessageDialog.info(null, "angal.inventory.update.success.msg");
662: 
663: 						statusLabel.setText(status.toUpperCase());
664: 						statusLabel.setForeground(Color.GRAY);
665: 						resetVariables();
666: 						validateButton.setEnabled(true);
667: 						confirmButton.setEnabled(false);
668: 					}
669: 				}
670: 			} catch (OHServiceException e) {
671: 				OHServiceExceptionUtil.showMessages(e);
672: 				return;
673: 			}
674: 		});
675: 		return saveButton;
676: 	}
677: 
678: 	private JButton getValidateButton() {
679: 		validateButton = new JButton(MessageBundle.getMessage("angal.inventory.validate.btn"));
680: 		validateButton.setMnemonic(MessageBundle.getMnemonic("angal.inventory.validate.btn.key"));
681: 		validateButton.setEnabled(inventory != null);
682: 		validateButton.addActionListener(actionEvent -> {
683: 			if (inventory == null) {
684: 				MessageDialog.error(null, "angal.inventory.inventorymustbesavedbeforevalidation.msg");
685: 				return;
686: 			}
687: 			List<MedicalInventoryRow> invRowWithoutLot = inventoryRowSearchList.stream().filter(invRow -> invRow.getLot() == null).collect(Collectors.toList());
688: 			if (!invRowWithoutLot.isEmpty()) {
689: 				MessageDialog.error(null, "angal.inventory.allinventoryrowshouldhavelotbeforevalidation.msg");
690: 				return;
691: 			}
692: 			if (inventoryRowSearchList == null || inventoryRowSearchList.isEmpty()) {
693: 				MessageDialog.error(null, "angal.inventory.cannotvalidateinventorywithoutproducts.msg");
694: 				return;
695: 			}
696: 			String dischargeCode = inventory.getDischargeType();
697: 			String lastReference = inventory.getInventoryReference();
698: 			LocalDateTime lastInventoryDate = inventory.getInventoryDate();
699: 			List<MedicalInventoryRow> invRowWithoutRealQty = inventoryRowSearchList.stream().filter(invRow -> invRow.getRealQty() == 0 && invRow.isNewLot())
700: 				.toList();
701: 			if (!invRowWithoutRealQty.isEmpty()) {
702: 				MessageDialog.error(null, "angal.inventory.allinventoryrowswithnewlotshouldhaverealqtygreatterthanzero.msg");
703: 				return;
704: 			}
705: 			String errorMessage = this.checkParamsValues(dischargeCode);
706: 			if (errorMessage != null) {
707: 				MessageDialog.error(null, errorMessage);
708: 				return;
709: 			}
710: 			if (checkParametersChanges(dischargeCode,lastReference, lastInventoryDate)) {
711: 				MessageDialog.error(null, "angal.inventory.pleasesaveinventorybeforevalidateit.msg");
712: 				return;
713: 			}
714: 			// validate inventory
715: 			String status = InventoryStatus.validated.toString();
716: 			String option = askAllMedicalsOrList("angal.inventory.doyouwanttocheckforallmedicalsinthestockoronlytheonesinthelist.msg");
717: 			if (option == null) {
718: 				return;
719: 			}
720: 			allMedicals = option.equals(allMedicalsOrList[0]); // Yes (All medicals)
721: 			allMedicalsChosen = true;
722: 			try {
723: 				medicalInventoryManager.validateMedicalInventoryRow(inventory, inventoryRowSearchList, allMedicals);
724: 				inventory.setStatus(status);
725: 				inventory = medicalInventoryManager.updateMedicalInventory(inventory, true);
726: 				MessageDialog.info(null, "angal.inventory.validate.success.msg");
727: 				statusLabel.setText(status.toUpperCase());
728: 				statusLabel.setForeground(Color.BLUE);
729: 				confirmButton.setEnabled(true);
730: 				fireInventoryUpdated();
731: 			} catch (OHServiceException e) {
732: 				OHServiceExceptionUtil.showMessages(e);
733: 				int answer = MessageDialog.yesNo(null, "angal.inventory.doyouwanttoactualizetheinventory.msg");
734: 				if (answer == JOptionPane.YES_OPTION) {
735: 					try {
736: 						inventory.setStatus(status);
737: 						inventory = medicalInventoryManager.actualizeMedicalInventoryRow(inventory, allMedicals);
738: 						dateInventory = inventory.getInventoryDate();
739: 						statusLabel.setText(status.toUpperCase());
740: 						statusLabel.setForeground(Color.BLUE);
741: 						confirmButton.setEnabled(true);
742: 						jTableInventoryRow.setModel(new InventoryRowModel());
743: 						fireInventoryUpdated();
744: 					} catch (OHServiceException e1) {
745: 						OHServiceExceptionUtil.showMessages(e1);
746: 					}
747: 				} else {
748: 					try {
749: 						inventory.setStatus(InventoryStatus.draft.toString());
750: 						statusLabel.setText(InventoryStatus.draft.toString().toUpperCase());
751: 						statusLabel.setForeground(Color.GRAY);
752: 						inventory = medicalInventoryManager.updateMedicalInventory(inventory, true);
753: 						fireInventoryUpdated();
754: 					} catch (OHServiceException ex) {
755: 						OHServiceExceptionUtil.showMessages(ex);
756: 					}
757: 				}
758: 			}
759: 		});
760: 		return validateButton;
761: 	}
762: 
763: 	private String askAllMedicalsOrList(String question) {
764: 		return (String) MessageDialog.inputDialog(null, null, allMedicalsOrList, null, question);
765: 	}
766: 
767: 	private JButton getConfirmButton() {
768: 		confirmButton = new JButton(MessageBundle.getMessage("angal.inventory.confirm.btn"));
769: 		confirmButton.setMnemonic(MessageBundle.getMnemonic("angal.inventory.confirm.btn.key"));
770: 		if (inventory == null) {
771: 			confirmButton.setEnabled(false);
772: 		}
773: 		confirmButton.addActionListener(actionEvent -> {
774: 			if (inventory == null) {
775: 				MessageDialog.error(null, "angal.inventory.inventorymustbesavedbeforevalidation.msg");
776: 				return;
777: 			}
778: 			List<MedicalInventoryRow> invRowWithoutLot = inventoryRowSearchList.stream().filter(invRow -> invRow.getLot() == null).toList();
779: 			if (!invRowWithoutLot.isEmpty()) {
780: 				MessageDialog.error(null, "angal.inventory.allinventoryrowshouldhavelotbeforevalidation.msg");
781: 				return;
782: 			}
783: 			int confirm = MessageDialog.yesNo(null, "angal.inventory.doyoureallywanttoconfirmthisinventory.msg");
784: 			if (confirm == JOptionPane.YES_OPTION) {
785: 				if (inventoryRowSearchList == null || inventoryRowSearchList.isEmpty()) {
786: 					MessageDialog.error(null, "angal.inventory.cannotconfirminventorywithoutproducts.msg");
787: 					return;
788: 				}
789: 				String dischargeCode = inventory.getDischargeType();
790: 				String lastReference = inventory.getInventoryReference();
791: 				LocalDateTime lastDate = inventory.getInventoryDate();
792: 				List<MedicalInventoryRow> invRowWithoutRealQty = inventoryRowSearchList.stream().filter(invRow -> invRow.getRealQty() == 0 && invRow.isNewLot())
793: 					.toList();
794: 				if (!invRowWithoutRealQty.isEmpty()) {
795: 					MessageDialog.error(null, "angal.inventory.allinventoryrowswithnewlotshouldhaverealqtygreatterthanzero.msg");
796: 					return;
797: 				}
798: 				String errorMessage = this.checkParamsValues(dischargeCode);
799: 				if (errorMessage != null) {
800: 					MessageDialog.error(null, errorMessage);
801: 					return;
802: 				}
803: 				if (checkParametersChanges(dischargeCode, lastReference, lastDate)) {
804: 					MessageDialog.error(null, "angal.inventory.pleasesaveinventorybeforeconfirmation.msg");
805: 					return;
806: 				}
807: 				// confirm inventory
808: 				String option = null;
809: 				if (!allMedicalsChosen) { // if not asked in the same session
810: 					option = askAllMedicalsOrList("angal.inventory.doyouwanttocheckforallmedicalsinthestockoronlytheonesinthelist.msg");
811: 					if (option == null) {
812: 						return;
813: 					}
814: 					allMedicals = option.equals(allMedicalsOrList[0]); // Yes (All medicals)
815: 				}
816: 				try {
817: 					medicalInventoryManager.confirmMedicalInventoryRow(inventory, inventoryRowSearchList, allMedicals);
818: 					MessageDialog.info(null, "angal.inventory.confirm.success.msg");
819: 					fireInventoryUpdated();
820: 					closeButton.doClick();
821: 				} catch (OHServiceException e) {
822: 					OHServiceExceptionUtil.showMessages(e);
823: 					MessageDialog.info(null, "angal.inventory.pleasevalidateinventoryagainsbeforeconfirmation.msg");
824: 					confirmButton.setEnabled(false);
825: 					inventory.setStatus(InventoryStatus.draft.toString());
826: 					statusLabel.setText(InventoryStatus.draft.toString().toUpperCase());
827: 					statusLabel.setForeground(Color.GRAY);
828: 					try {
829: 						inventory = medicalInventoryManager.updateMedicalInventory(inventory, true);
830: 						fireInventoryUpdated();
831: 					} catch (OHServiceException e1) {
832: 						OHServiceExceptionUtil.showMessages(e1);
833: 					}
834: 					return;
835: 				}
836: 			}
837: 		});
838: 		return confirmButton;
839: 	}
840: 
841: 	private JButton getCleanTableButton() {
842: 		resetButton = new JButton(MessageBundle.getMessage("angal.inventory.clean.btn"));
843: 		resetButton.setMnemonic(MessageBundle.getMnemonic("angal.inventory.clean.btn.key"));
844: 		resetButton.addActionListener(actionEvent -> {
845: 			int reset = MessageDialog.yesNo(null, "angal.inventory.doyoureallywanttocleanthistable.msg");
846: 			if (reset == JOptionPane.YES_OPTION) {
847: 				if (inventory != null) {
848: 					inventoryRowsToDelete.addAll(inventoryRowSearchList);
849: 				}
850: 				inventoryRowSearchList.clear();
851: 				DefaultTableModel model = (DefaultTableModel) jTableInventoryRow.getModel();
852: 				model.setRowCount(0);
853: 				model.setColumnCount(0);
854: 				jTableInventoryRow.updateUI();
855: 			}
856: 		});
857: 		return resetButton;
858: 	}
859: 
860: 	private JButton getDeleteButton() {
861: 		deleteButton = new JButton(MessageBundle.getMessage("angal.common.delete.btn"));
862: 		deleteButton.setMnemonic(MessageBundle.getMnemonic("angal.common.delete.btn.key"));
863: 		deleteButton.addActionListener(actionEvent -> {
864: 			int[] selectedRows = jTableInventoryRow.getSelectedRows();
865: 			if (selectedRows.length == 0) {
866: 				MessageDialog.error(this, "angal.inventory.pleaseselectatleastoneinventoryrow.msg");
867: 				return;
868: 			}
869: 			int delete = MessageDialog.yesNo(null, "angal.inventory.doyoureallywanttodeletethisinventoryrow.msg");
870: 			if (delete == JOptionPane.YES_OPTION) {
871: 				if (selectedRows.length == inventoryRowSearchList.size()) {
872: 					resetButton.doClick();
873: 					return;
874: 				}
875: 				if (inventory == null) {
876: 					for (int i = selectedRows.length - 1; i >= 0; i--) {
877: 						MedicalInventoryRow selectedInventoryRow = (MedicalInventoryRow) jTableInventoryRow.getValueAt(selectedRows[i], -1);
878: 						inventoryRowSearchList.remove(selectedInventoryRow);
879: 						model.fireTableDataChanged();
880: 						jTableInventoryRow.setModel(model);
881: 					}
882: 				} else {
883: 					for (int i = selectedRows.length - 1; i >= 0; i--) {
884: 						MedicalInventoryRow inventoryRow = (MedicalInventoryRow) jTableInventoryRow.getValueAt(selectedRows[i], -1);
885: 						inventoryRowSearchList.remove(inventoryRow);
886: 						model.fireTableDataChanged();
887: 						jTableInventoryRow.setModel(model);
888: 						if (inventoryRow.getId() != 0) {
889: 							inventoryRowsToDelete.add(inventoryRow);
890: 						}
891: 					}
892: 				}
893: 				jTableInventoryRow.clearSelection();
894: 			} else {
895: 				return;
896: 			}
897: 		});
898: 		return deleteButton;
899: 	}
900: 
901: 	private JButton getLotButton() {
902: 		lotButton = new JButton(MessageBundle.getMessage("angal.inventory.lot.btn"));
903: 		lotButton.setMnemonic(MessageBundle.getMnemonic("angal.inventory.lot.btn.key"));
904: 		lotButton.addActionListener(actionEvent -> {
905: 			int selectedRow = jTableInventoryRow.getSelectedRow();
906: 			if (selectedRow == -1) {
907: 				MessageDialog.error(this, "angal.inventory.pleaseselectonlyoneinventoryrow.msg");
908: 				return;
909: 			}
910: 			MedicalInventoryRow selectedInventoryRow = (MedicalInventoryRow) jTableInventoryRow.getValueAt(selectedRow, -1);
911: 			Lot oldLot = selectedInventoryRow.getLot(); // current lot
912: 			Lot newLot;
913: 			try {
914: 				if (selectedInventoryRow.isNewLot()) {
915: 					newLot = getLot(oldLot, selectedInventoryRow.getMedical()); // create or update lot
916: 				} else {
917: 					BigDecimal cost = (oldLot != null && oldLot.getCost() != null)
918: 						? oldLot.getCost()
919: 						: BigDecimal.ZERO;
920: 
921: 					if (isLotWithCost()) {
922: 						BigDecimal newCost = askCost(2, cost);
923: 						if (newCost.compareTo(BigDecimal.ZERO) == 0) {
924: 							return;
925: 						}
926: 
927: 						oldLot.setCost(newCost);
928: 						newLot = oldLot;
929: 					} else {
930: 						newLot = oldLot;
931: 					}
932: 				}
933: 
934: 				if (newLot != null) {
935: 					String oldLotCode = (oldLot != null) ? oldLot.getCode() : null;
936: 					String newLotCode = newLot.getCode();
937: 
938: 					if (oldLotCode != null && !oldLotCode.equals(newLotCode)) {
939: 						if (!lotsDeleted.contains(oldLot)) {
940: 							lotsDeleted.add(oldLot);
941: 						}
942: 					}
943: 					if (!lotsSaved.contains(newLot)) {
944: 						lotsSaved.add(newLot);
945: 					}
946: 					selectedInventoryRow.setLot(newLot);
947: 				}
948: 			} catch (OHServiceException e) {
949: 				OHServiceExceptionUtil.showMessages(e);
950: 				return;
951: 			}
952: 			inventoryRowSearchList.set(selectedRow, selectedInventoryRow);
953: 			jTableInventoryRow.updateUI();
954: 		});
955: 		return lotButton;
956: 	}
957: 
958: 	private JButton getPrintButton() {
959: 		printButton = new JButton(MessageBundle.getMessage("angal.common.print.btn"));
960: 		printButton.setMnemonic(MessageBundle.getMnemonic("angal.common.print.btn.key"));
961: 		printButton.setEnabled(true);
962: 
963: 		printButton.addActionListener(e -> {
964: 			int printRealQty = 0;
965: 			int response = MessageDialog.yesNo(this, "angal.inventory.askforrealquantityempty.msg");
966: 			if (response == JOptionPane.YES_OPTION) {
967: 				printRealQty = 1;
968: 			}
969: 			new GenericReportPharmaceuticalInventory(inventory, "Inventory", printRealQty);
970: 		});
971: 		return printButton;
972: 	}
973: 
974: 	private JButton getCloseButton() {
975: 		closeButton = new JButton(MessageBundle.getMessage("angal.common.close.btn"));
976: 		closeButton.setMnemonic(MessageBundle.getMnemonic("angal.common.close.btn.key"));
977: 		closeButton.addActionListener(actionEvent -> {
978: 			String lastCharge = null;
979: 			String lastDischarge = null;
980: 			Integer lastSupplier = null;
981: 			String lastDestination = null;
982: 			String lastReference = null;
983: 			newReference = referenceTextField.getText().trim();
984: 			LocalDateTime lastDate = dateInventory;
985: 			if (inventory != null) {
986: 				lastCharge = inventory.getChargeType();
987: 				lastDischarge = inventory.getDischargeType();
988: 				lastSupplier = inventory.getSupplier();
989: 				lastDestination = inventory.getDestination();
990: 				lastReference = inventory.getInventoryReference();
991: 				lastDate = inventory.getInventoryDate();
992: 			}
993: 			if (checkParametersChanges(lastDischarge, lastReference, lastDate)) {
994: 				int reset = MessageDialog.yesNoCancel(null, "angal.inventory.doyouwanttosavethechanges.msg");
995: 				if (reset == JOptionPane.YES_OPTION) {
996: 					this.saveButton.doClick();
997: 					dispose();
998: 				}
999: 				if (reset == JOptionPane.NO_OPTION) {
1000: 					resetVariables();
1001: 					dispose();
1002: 				} else {
1003: 					return;
1004: 				}
1005: 			} else {
1006: 				resetVariables();
1007: 				dispose();
1008: 			}
1009: 		});
1010: 		return closeButton;
1011: 	}
1012: 
1013: 	private String checkParamsValues(String dischargeCode) {
1014: 		if (dischargeCode == null || dischargeCode.isEmpty()) {
1015: 			return "angal.inventory.choosedischargetypebeforevalidation.msg";
1016: 		}
1017: 
1018: 		return null;
1019: 	}
1020: 
1021: 	private JScrollPane getScrollPaneInventory() {
1022: 		if (scrollPaneInventory == null) {
1023: 			scrollPaneInventory = new JScrollPane();
1024: 			try {
1025: 				scrollPaneInventory.setViewportView(getJTableInventoryRow());
1026: 			} catch (OHServiceException e) {
1027: 				OHServiceExceptionUtil.showMessages(e);
1028: 			}
1029: 		}
1030: 		return scrollPaneInventory;
1031: 	}
1032: 
1033: 	private JTable getJTableInventoryRow() throws OHServiceException {
1034: 		if (jTableInventoryRow == null) {
1035: 			jTableInventoryRow = new JTable();
1036: 			jTableInventoryRow.setFillsViewportHeight(true);
1037: 			model = new InventoryRowModel();
1038: 			jTableInventoryRow.setModel(model);
1039: 			jTableInventoryRow.setAutoCreateColumnsFromModel(false);
1040: 			for (int i = 0; i < columnVisible.length; i++) {
1041: 				jTableInventoryRow.getColumnModel().getColumn(i).setPreferredWidth(columwidth[i]);
1042: 				if (!columnVisible[i]) {
1043: 					jTableInventoryRow.getColumnModel().getColumn(i).setMinWidth(0);
1044: 					jTableInventoryRow.getColumnModel().getColumn(i).setMaxWidth(0);
1045: 					jTableInventoryRow.getColumnModel().getColumn(i).setPreferredWidth(0);
1046: 				}
1047: 				if (columnCentered[i]) {
1048: 					jTableInventoryRow.getColumnModel().getColumn(i).setCellRenderer(new CenterTableCellRenderer());
1049: 				}
1050: 				if (columnDecimalNumber[i]) {
1051: 					jTableInventoryRow.getColumnModel().getColumn(i).setCellRenderer(new DecimalNumberTableCellRenderer());
1052: 				}
1053: 			}
1054: 			jTableInventoryRow.getSelectionModel().addListSelectionListener(listSelectionEvent -> {
1055: 
1056: 				if (listSelectionEvent.getValueIsAdjusting()) {
1057: 					int selectedRow = jTableInventoryRow.getSelectedRow();
1058: 					if (selectedRow == -1) {
1059: 						lotButton.setEnabled(false);
1060: 						return;
1061: 					}
1062: 					MedicalInventoryRow medInvRow = (MedicalInventoryRow) jTableInventoryRow.getValueAt(selectedRow, -1);
1063: 					Lot lot = medInvRow.getLot();
1064: 					if (lot == null) {
1065: 						lotButton.setEnabled(true);
1066: 						return;
1067: 					}
1068: 					BigDecimal cost = lot.getCost() != null ? lot.getCost() : BigDecimal.ZERO;
1069: 					lotButton.setEnabled(medInvRow.isNewLot() || (isLotWithCost() && cost.compareTo(BigDecimal.ZERO) == 0));
1070: 				}
1071: 			});
1072: 			JTextField jTextFieldEditor = new JTextField();
1073: 			DefaultCellEditor cellEditor = new DefaultCellEditor(jTextFieldEditor);
1074: 			jTableInventoryRow.setDefaultEditor(Integer.class, cellEditor);
1075: 		}
1076: 		return jTableInventoryRow;
1077: 	}
1078: 
1079: 	class InventoryRowModel extends DefaultTableModel {
1080: 
1081: 		private static final long serialVersionUID = 1L;
1082: 
1083: 		public InventoryRowModel() throws OHServiceException {
1084: 			this(null, false, false, false);
1085: 		}
1086: 
1087: 		public InventoryRowModel(boolean add) throws OHServiceException {
1088: 			this(null, false, add, false);
1089: 		}
1090: 
1091: 		public InventoryRowModel(MedicalType medType) throws OHServiceException {
1092: 			this(medType, false, false, false);
1093: 		}
1094: 
1095: 		public InventoryRowModel(boolean withNoZeroQty, MedicalType medType) throws OHServiceException {
1096: 			this(medType, withNoZeroQty, false, false);
1097: 		}
1098: 
1099: 		public InventoryRowModel(MedicalType medType, boolean withMovement) throws OHServiceException {
1100: 			this(medType, false, false, withMovement);
1101: 		}
1102: 
1103: 		public InventoryRowModel(MedicalType medType, boolean withNoZeroQty, boolean includeAll, boolean withMovement) throws OHServiceException {
1104: 			if (includeAll) {
1105: 				if (medType == null) {
1106: 					inventoryRowList = loadNewInventoryTable(null, inventory, true);
1107: 				} else {
1108: 					inventoryRowList = loadNewInventoryTableByMedicalType(medType);
1109: 				}
1110: 			} else if (withMovement) {
1111: 				inventoryRowList = loadNewInventoryTable(medType);
1112: 			} else if (withNoZeroQty) {
1113: 				if (medType == null) {
1114: 					inventoryRowList = loadNewInventoryTable(true, null);
1115: 				} else {
1116: 					inventoryRowList = loadNewInventoryTable(true, medType);
1117: 				}
1118: 			} else {
1119: 				inventoryRowSearchList.clear();
1120: 				if (inventory != null) {
1121: 					inventoryRowList = medicalInventoryRowManager.getMedicalInventoryRowByInventoryId(inventory.getId());
1122: 				}
1123: 			}
1124: 
1125: 			if (!inventoryRowList.isEmpty()) {
1126: 				for (MedicalInventoryRow invRow : inventoryRowList) {
1127: 					addMedInRowInInventorySearchList(invRow);
1128: 					if (!includeAll && invRow.getId() == 0) {
1129: 						inventoryRowListAdded.add(invRow);
1130: 					}
1131: 				}
1132: 			} else {
1133: 				if (!mode.equals("new")) {
1134: 					MessageDialog.info(null, "angal.inventory.notdataforthatfilter.msg");
1135: 				}
1136: 			}
1137: 		}
1138: 
1139: 		@Override
1140: 		public Class< ? > getColumnClass(int c) {
1141: 			return columnsClasses[c];
1142: 		}
1143: 
1144: 		@Override
1145: 		public int getRowCount() {
1146: 			if (inventoryRowSearchList == null) {
1147: 				return 0;
1148: 			}
1149: 			return inventoryRowSearchList.size();
1150: 		}
1151: 
1152: 		@Override
1153: 		public String getColumnName(int c) {
1154: 			return columsNames[c];
1155: 		}
1156: 
1157: 		@Override
1158: 		public int getColumnCount() {
1159: 			return columsNames.length;
1160: 		}
1161: 
1162: 		@Override
1163: 		public Object getValueAt(int r, int c) {
1164: 			if (r < inventoryRowSearchList.size()) {
1165: 				MedicalInventoryRow medInvtRow = inventoryRowSearchList.get(r);
1166: 				Medical medical = medInvtRow.getMedical();
1167: 				Lot lot = medInvtRow.getLot();
1168: 				if (c == -1) {
1169: 					return medInvtRow;
1170: 				} else if (c == 0) {
1171: 					return medInvtRow.getId();
1172: 				} else if (c == 1) {
1173: 					return medical.getProdCode();
1174: 				} else if (c == 2) {
1175: 					return medical.getDescription();
1176: 				} else if (c == 3) {
1177: 					return lot == null || medInvtRow.isNewLot() ? "N" : "";
1178: 				} else if (c == 4) {
1179: 					return lot == null ? "" : (lot.getCode().equals("") ? "AUTO" : lot.getCode());
1180: 				} else if (c == 5) {
1181: 					if (lot != null && lot.getDueDate() != null) {
1182: 						return lot.getDueDate().format(DATE_FORMATTER);
1183: 					}
1184: 					return "";
1185: 				} else if (c == 6) {
1186: 					return medInvtRow.getTheoreticQty();
1187: 				} else if (c == 7) {
1188: 					double dblValue = medInvtRow.getRealQty();
1189: 					return (int) dblValue;
1190: 				} else if (c == 8) {
1191: 					double difference = medInvtRow.getRealQty() - medInvtRow.getTheoreticQty();
1192: 					return difference == 0. ? "" : (int) difference;
1193: 				} else if (c == 9) {
1194: 					if (lot != null && lot.getCost() != null) {
1195: 						medInvtRow.setTotal(lot.getCost().multiply(BigDecimal.valueOf(medInvtRow.getRealQty())));
1196: 						return lot.getCost();
1197: 					}
1198: 					return BigDecimal.ZERO;
1199: 				} else if (c == 10) {
1200: 					if (lot != null && lot.getCost() != null) {
1201: 						return medInvtRow.getTotal();
1202: 					}
1203: 					return BigDecimal.ZERO;
1204: 				}
1205: 			}
1206: 			return null;
1207: 		}
1208: 
1209: 		@Override
1210: 		public void setValueAt(Object value, int r, int c) {
1211: 			if (r < inventoryRowSearchList.size()) {
1212: 				MedicalInventoryRow invRow = inventoryRowSearchList.get(r);
1213: 				if (c == 7) {
1214: 					double doubleValue = 0.0;
1215: 					if (value != null) {
1216: 						try {
1217: 							doubleValue = Double.parseDouble(value.toString());
1218: 						} catch (NumberFormatException e) {
1219: 							return;
1220: 						}
1221: 					}
1222: 					if (doubleValue < 0) {
1223: 						MessageDialog.error(null, "angal.inventory.invalidquantity.msg");
1224: 						return;
1225: 					}
1226: 					invRow.setRealqty(doubleValue);
1227: 					if (invRow.getLot() != null && invRow.getLot().getCost() != null) {
1228: 						BigDecimal total = invRow.getLot().getCost().multiply(BigDecimal.valueOf(invRow.getRealQty()));
1229: 						invRow.setTotal(total);
1230: 					}
1231: 					inventoryRowListAdded.add(invRow);
1232: 					inventoryRowSearchList.set(r, invRow);
1233: 					SwingUtilities.invokeLater(() -> jTableInventoryRow.updateUI());
1234: 				}
1235: 			}
1236: 		}
1237: 
1238: 		@Override
1239: 		public boolean isCellEditable(int rowIndex, int columnIndex) {
1240: 			return columnEditable[columnIndex];
1241: 		}
1242: 	}
1243: 
1244: 	private Lot getLot(Lot lotToUpdate, Medical medical) throws OHServiceException {
1245: 		Lot lot;
1246: 
1247: 		if (isAutomaticLotIn()) {
1248: 			LocalDateTime preparationDate = TimeTools.getNow().truncatedTo(ChronoUnit.MINUTES);
1249: 			LocalDateTime expiringDate = askExpiringDate();
1250: 			Medical assignedMedical = (lotToUpdate != null) ? lotToUpdate.getMedical() : medical;
1251: 			if (assignedMedical == null) {
1252: 				throw new IllegalArgumentException("Fatal: Medical is required for new lots."); // should not happen!
1253: 			}
1254: 			String lotCode = (lotToUpdate != null) ? lotToUpdate.getCode() : "";
1255: 			lot = new Lot(assignedMedical, lotCode, preparationDate, expiringDate);
1256: 			BigDecimal cost = (lotToUpdate != null && lotToUpdate.getCost() != null) ? lotToUpdate.getCost() : BigDecimal.ZERO;
1257: 			if (isLotWithCost()) {
1258: 				cost = askCost(2, cost);
1259: 				if (cost.compareTo(BigDecimal.ZERO) == 0) {
1260: 					return null;
1261: 				}
1262: 			}
1263: 			lot.setCost(cost);
1264: 		} else {
1265: 			lot = askLot(lotToUpdate, medical);
1266: 		}
1267: 
1268: 		return lot;
1269: 	}
1270: 
1271: 	private Lot askLot(Lot lotToUpdate, Medical medical) {
1272: 		LocalDateTime preparationDate;
1273: 		LocalDateTime expiringDate;
1274: 		Lot lot = null;
1275: 		Medical assignedMedical = medical;
1276: 
1277: 		JTextField lotCodeTextField = new JTextField(15);
1278: 		lotCodeTextField.addAncestorListener(new RequestFocusListener());
1279: 		TextPrompt suggestion = new TextPrompt(MessageBundle.getMessage("angal.medicalstock.multiplecharging.lotid"), lotCodeTextField);
1280: 		suggestion.setFont(new Font("Tahoma", Font.PLAIN, 14));
1281: 		suggestion.setForeground(Color.GRAY);
1282: 		suggestion.setHorizontalAlignment(SwingConstants.CENTER);
1283: 		suggestion.changeAlpha(0.5f);
1284: 		suggestion.changeStyle(Font.BOLD + Font.ITALIC);
1285: 		LocalDate now = LocalDate.now();
1286: 		GoodDateChooser preparationDateChooser = new GoodDateChooser(now);
1287: 		GoodDateChooser expireDateChooser = new GoodDateChooser(now);
1288: 		if (lotToUpdate != null) {
1289: 			assignedMedical = lotToUpdate.getMedical();
1290: 			lotCodeTextField.setText(lotToUpdate.getCode());
1291: 			preparationDateChooser = new GoodDateChooser(lotToUpdate.getPreparationDate().toLocalDate());
1292: 			expireDateChooser = new GoodDateChooser(lotToUpdate.getDueDate().toLocalDate());
1293: 		}

(Output capped at 50 KB. Showing lines 1-1293. Use offset=1294 to continue.)
</content>
```

**Tool: read**

**Input:**
```json
{
  "filePath": "/home/uni2grow/Documents/openhospital/openhospital-gui/src/main/java/org/isf/medicalinventory/gui/MedicalPicker.java"
}
```

**Output:**
```
<path>/home/uni2grow/Documents/openhospital/openhospital-gui/src/main/java/org/isf/medicalinventory/gui/MedicalPicker.java</path>
<type>file</type>
<content>
1: /*
2:  * Open Hospital (www.open-hospital.org)
3:  * Copyright © 2006-2025 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
4:  *
5:  * Open Hospital is a free and open source software for healthcare data management.
6:  *
7:  * This program is free software: you can redistribute it and/or modify
8:  * it under the terms of the GNU General Public License as published by
9:  * the Free Software Foundation, either version 3 of the License, or
10:  * (at your option) any later version.
11:  *
12:  * https://www.gnu.org/licenses/gpl-3.0-standalone.html
13:  *
14:  * This program is distributed in the hope that it will be useful,
15:  * but WITHOUT ANY WARRANTY; without even the implied warranty of
16:  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
17:  * GNU General Public License for more details.
18:  *
19:  * You should have received a copy of the GNU General Public License
20:  * along with this program. If not, see <https://www.gnu.org/licenses/>.
21:  */
22: package org.isf.medicalinventory.gui;
23: 
24: import java.awt.BorderLayout;
25: import java.awt.Color;
26: import java.awt.FlowLayout;
27: import java.awt.GridBagConstraints;
28: import java.awt.GridBagLayout;
29: import java.awt.Insets;
30: import java.awt.Rectangle;
31: import java.awt.event.ActionEvent;
32: import java.awt.event.KeyAdapter;
33: import java.awt.event.KeyEvent;
34: import java.awt.event.MouseAdapter;
35: import java.awt.event.MouseEvent;
36: import java.awt.event.MouseMotionListener;
37: import java.util.ArrayList;
38: import java.util.Collection;
39: import java.util.List;
40: 
41: import javax.swing.JButton;
42: import javax.swing.JDialog;
43: import javax.swing.JLabel;
44: import javax.swing.JPanel;
45: import javax.swing.JScrollPane;
46: import javax.swing.JTable;
47: import javax.swing.JTextField;
48: import javax.swing.ListSelectionModel;
49: import javax.swing.event.DocumentEvent;
50: import javax.swing.event.DocumentListener;
51: import javax.swing.table.TableColumn;
52: import javax.swing.table.TableModel;
53: 
54: import org.isf.generaldata.MessageBundle;
55: import org.isf.medicals.model.Medical;
56: import org.isf.utils.jobjects.OhDefaultCellRenderer;
57: 
58: public class MedicalPicker extends JPanel {
59: 
60: 	OhDefaultCellRenderer cellRenderer = new OhDefaultCellRenderer();
61: 	private static final long serialVersionUID = 1L;
62: 	private Collection<Medical> medicalMapValues = new ArrayList<Medical>();
63: 	private String searchCode;
64: 	private JButton jButtonSelect;
65: 	private JButton jButtonQuit;
66: 	private JLabel jLabelImage;
67: 	private JPanel jPanel1;
68: 	private JPanel jPanel2;
69: 	private JPanel jPanel3;
70: 	private JScrollPane jScrollPane1;
71: 	private JTable jTableData;
72: 	private JTextField jTextFieldFind;
73: 	private int selectedRow = -1;
74: 	private JDialog parentFrame;
75: 
76: 	private int getSelectedRow() {
77: 		return selectedRow;
78: 	}
79: 
80: 	public Medical getSelectedMedical() {
81: 		StockMedModel model = (StockMedModel) jTableData.getModel();
82: 		return model.getMedicalAtRow(this.getSelectedRow());
83: 	}
84: 
85: 	private void setSelectedRow(int selectedRow) {
86: 		this.selectedRow = selectedRow;
87: 	}
88: 
89: 	public JDialog getParentFrame() {
90: 		return parentFrame;
91: 	}
92: 
93: 	public void setParentFrame(JDialog parentFrame) {
94: 		this.parentFrame = parentFrame;
95: 	}
96: 
97: 	public MedicalPicker(StockMedModel model, List<Medical> medicals) {
98: 		initComponents(model);
99: 		this.medicalMapValues = medicals;
100: 	}
101: 
102: 	/**
103: 	 * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always
104: 	 * regenerated by the Form Editor.
105: 	 */
106: 	private void initComponents(TableModel model) {
107: 
108: 		jPanel3 = new JPanel();
109: 		jPanel1 = new JPanel();
110: 
111: 		jPanel3.setBackground(new Color(240, 240, 240));
112: 
113: 		jPanel1.setBackground(new Color(240, 240, 240));
114: 
115: 		setLayout(new BorderLayout(10, 10));
116: 		add(jPanel1, BorderLayout.CENTER);
117: 		GridBagLayout gbl_jPanel1 = new GridBagLayout();
118: 		gbl_jPanel1.columnWidths = new int[] { 575, 0 };
119: 		gbl_jPanel1.rowHeights = new int[] { 268, 0 };
120: 		gbl_jPanel1.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
121: 		gbl_jPanel1.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
122: 		jPanel1.setLayout(gbl_jPanel1);
123: 		jScrollPane1 = new JScrollPane();
124: 		jTableData = new JTable();
125: 
126: 		jTableData.setDefaultRenderer(Object.class, cellRenderer);
127: 		jTableData.setDefaultRenderer(Double.class, cellRenderer);
128: 		jTableData.addMouseMotionListener(new MouseMotionListener() {
129: 
130: 			@Override
131: 			public void mouseMoved(MouseEvent e) {
132: 				JTable aTable = (JTable) e.getSource();
133: 				int itsRow = aTable.rowAtPoint(e.getPoint());
134: 				if (itsRow >= 0) {
135: 					cellRenderer.setHoveredRow(itsRow);
136: 				} else {
137: 					cellRenderer.setHoveredRow(-1);
138: 				}
139: 				aTable.repaint();
140: 			}
141: 
142: 			@Override
143: 			public void mouseDragged(MouseEvent e) {
144: 			}
145: 		});
146: 		jTableData.addMouseListener(new MouseAdapter() {
147: 
148: 			@Override
149: 			public void mouseExited(MouseEvent e) {
150: 				cellRenderer.setHoveredRow(-1);
151: 			}
152: 		});
153: 
154: 		jTableData.setModel(model);
155: 		jTableData.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
156: 		jTableData.setShowVerticalLines(false);
157: 		jTableData.addMouseListener(new MouseAdapter() {
158: 
159: 			@Override
160: 			public void mouseClicked(MouseEvent evt) {
161: 				jTableDataMouseClicked(evt);
162: 			}
163: 
164: 			@Override
165: 			public void mousePressed(MouseEvent evt) {
166: 				if (evt.getClickCount() == 2) {
167: 					validateSelection();
168: 				}
169: 			}
170: 		});
171: 
172: 		jTableData.addKeyListener(new KeyAdapter() {
173: 
174: 			@Override
175: 			public void keyPressed(KeyEvent e) {
176: 				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
177: 					validateSelection();
178: 					e.consume();
179: 					return;
180: 				}
181: 				super.keyPressed(e);
182: 			}
183: 		});
184: 
185: 		jScrollPane1.setViewportView(jTableData);
186: 		GridBagConstraints gbc_jScrollPane1 = new GridBagConstraints();
187: 		gbc_jScrollPane1.insets = new Insets(0, 15, 0, 15);
188: 		gbc_jScrollPane1.fill = GridBagConstraints.BOTH;
189: 		gbc_jScrollPane1.gridx = 0;
190: 		gbc_jScrollPane1.gridy = 0;
191: 		jPanel1.add(jScrollPane1, gbc_jScrollPane1);
192: 		add(jPanel3, BorderLayout.NORTH);
193: 		GridBagLayout gbl_jPanel3 = new GridBagLayout();
194: 		gbl_jPanel3.columnWidths = new int[] { 90, 237, 0 };
195: 		gbl_jPanel3.rowHeights = new int[] { 50, 0 };
196: 		gbl_jPanel3.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
197: 		gbl_jPanel3.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
198: 		jPanel3.setLayout(gbl_jPanel3);
199: 
200: 		jLabelImage = new JLabel();
201: 
202: 		jLabelImage.setText(MessageBundle.getMessage("angal.common.search.txt"));
203: 		GridBagConstraints gbc_jLabelImage = new GridBagConstraints();
204: 		gbc_jLabelImage.anchor = GridBagConstraints.WEST;
205: 		gbc_jLabelImage.insets = new Insets(0, 15, 0, 5);
206: 		gbc_jLabelImage.gridx = 0;
207: 		gbc_jLabelImage.gridy = 0;
208: 		jPanel3.add(jLabelImage, gbc_jLabelImage);
209: 		jTextFieldFind = new JTextField();
210: 		jTextFieldFind.setText(searchCode);
211: 		GridBagConstraints gbc_jTextFieldFind = new GridBagConstraints();
212: 		gbc_jTextFieldFind.insets = new Insets(0, 0, 0, 15);
213: 		gbc_jTextFieldFind.fill = GridBagConstraints.HORIZONTAL;
214: 		gbc_jTextFieldFind.gridx = 1;
215: 		gbc_jTextFieldFind.gridy = 0;
216: 		jPanel3.add(jTextFieldFind, gbc_jTextFieldFind);
217: 		jTextFieldFind.getDocument().addDocumentListener(new DocumentListener() {
218: 
219: 			@Override
220: 			public void insertUpdate(DocumentEvent e) {
221: 				String text = jTextFieldFind.getText();
222: 				ArrayList<Medical> medList = new ArrayList<Medical>();
223: 				for (Medical aMed : medicalMapValues) {
224: 					medList.add(aMed);
225: 				}
226: 				StockMedModel model = new StockMedModel(medList);
227: 				jTableData.setModel(model);
228: 				TableColumn column;
229: 				for (int i = 0; i < 2; i++) {
230: 					column = jTableData.getColumnModel().getColumn(i);
231: 					if (i == 1) {
232: 						column.setPreferredWidth(375);
233: 					} else {
234: 						column.setPreferredWidth(100);
235: 					}
236: 				}
237: 				model.filter(text);
238: 				if (jTableData.getRowCount() > 0) {
239: 					jTableData.setRowSelectionInterval(0, 0);
240: 				}
241: 				jTableData.updateUI();
242: 
243: 			}
244: 
245: 			@Override
246: 			public void removeUpdate(DocumentEvent e) {
247: 				String text = jTextFieldFind.getText();
248: 				StockMedModel model = (StockMedModel) jTableData.getModel();
249: 				model.filter(text);
250: 				if (jTableData.getRowCount() > 0) {
251: 					jTableData.setRowSelectionInterval(0, 0);
252: 				}
253: 				jTableData.updateUI();
254: 			}
255: 
256: 			@Override
257: 			public void changedUpdate(DocumentEvent e) {
258: 				throw new UnsupportedOperationException("Not supported yet.");
259: 			}
260: 
261: 		});
262: 
263: 		jTextFieldFind.addKeyListener(new KeyAdapter() {
264: 
265: 			@Override
266: 			public void keyPressed(KeyEvent e) {
267: 				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
268: 					validateSelection();
269: 					e.consume();
270: 					return;
271: 				} else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
272: 					int selectedRow = jTableData.getSelectedRow();
273: 					if (jTableData.getRowCount() > 0 && selectedRow >= 0) {
274: 						selectedRow++;
275: 						if (jTableData.getRowCount() > selectedRow) {
276: 							jTableData.setRowSelectionInterval(selectedRow, selectedRow);
277: 							jTableData.scrollRectToVisible(new Rectangle(jTableData.getCellRect(selectedRow, 0, true)));
278: 						}
279: 					}
280: 				} else if (e.getKeyCode() == KeyEvent.VK_UP) {
281: 					int selectedRow = jTableData.getSelectedRow();
282: 					if (jTableData.getRowCount() > 0 && selectedRow > 0) {
283: 						selectedRow--;
284: 						jTableData.setRowSelectionInterval(selectedRow, selectedRow);
285: 						jTableData.scrollRectToVisible(new Rectangle(jTableData.getCellRect(selectedRow, 0, true)));
286: 					}
287: 				}
288: 				jTableData.updateUI();
289: 				super.keyPressed(e);
290: 			}
291: 		});
292: 
293: 		jPanel2 = new JPanel();
294: 		jButtonSelect = new JButton();
295: 		jButtonQuit = new JButton();
296: 		jPanel2.setBackground(new Color(240, 240, 240));
297: 		jButtonSelect.setText(MessageBundle.getMessage("angal.common.select.btn"));
298: 		jButtonSelect.setMnemonic(MessageBundle.getMnemonic("angal.common.select.btn.key"));
299: 		jButtonSelect.addMouseListener(new MouseAdapter() {
300: 
301: 			@Override
302: 			public void mouseClicked(MouseEvent evt) {
303: 				jButtonSelectMouseClicked(evt);
304: 			}
305: 		});
306: 		jButtonSelect.addActionListener(actionEvent -> {
307: 			jButtonSelectActionPerformed(actionEvent);
308: 		});
309: 		jButtonQuit.setText(MessageBundle.getMessage("angal.common.cancel.btn"));
310: 		jButtonQuit.setMnemonic(MessageBundle.getMnemonic("angal.common.cancel.btn.key"));
311: 		jButtonQuit.addMouseListener(new MouseAdapter() {
312: 
313: 			@Override
314: 			public void mouseClicked(MouseEvent evt) {
315: 				jButtonQuitMouseClicked(evt);
316: 			}
317: 		});
318: 		jButtonQuit.addActionListener(actionEvent -> closePicker());
319: 		add(jPanel2, BorderLayout.SOUTH);
320: 		jPanel2.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5));
321: 		jPanel2.add(jButtonSelect);
322: 		jPanel2.add(jButtonQuit);
323: 
324: 		if (jTableData.getRowCount() > 0) {
325: 			jTableData.setRowSelectionInterval(0, 0);
326: 		}
327: 
328: 	}
329: 
330: 	private void jTableDataMouseClicked(MouseEvent evt) {
331: 
332: 	}
333: 
334: 	private void validateSelection() {
335: 		int row = this.jTableData.getSelectedRow();
336: 		if (row < 0 && this.jTableData.getRowCount() > 0) {
337: 			row = 0;
338: 			this.jTableData.setRowSelectionInterval(row, row);
339: 		}
340: 		if (row < 0) {
341: 			return;
342: 		}
343: 		this.setSelectedRow(row);
344: 		closePicker();
345: 	}
346: 
347: 	private void jButtonSelectActionPerformed(ActionEvent evt) {
348: 		validateSelection();
349: 	}
350: 
351: 	private void jButtonSelectMouseClicked(MouseEvent evt) {
352: 		validateSelection();
353: 	}
354: 
355: 	private void jButtonQuitMouseClicked(MouseEvent evt) {
356: 		closePicker();
357: 	}
358: 
359: 	private void closePicker() {
360: 		this.setVisible(false);
361: 		this.getParentFrame().dispose();
362: 	}
363: }

(End of file - total 363 lines)
</content>
```

---

## Assistant (Plan · Big Pickle · 24.2s)

_Thinking:_

Let me continue reading the InventoryEdit.java file to see the rest of the code, particularly the `loadNewInventoryTable` methods and how lots are fetched.

**Tool: read**

**Input:**
```json
{
  "filePath": "/home/uni2grow/Documents/openhospital/openhospital-gui/src/main/java/org/isf/medicalinventory/gui/InventoryEdit.java",
  "offset": 1294
}
```

**Output:**
```
<path>/home/uni2grow/Documents/openhospital/openhospital-gui/src/main/java/org/isf/medicalinventory/gui/InventoryEdit.java</path>
<type>file</type>
<content>
1294: 		JPanel panel = new JPanel(new GridLayout(3, 2));
1295: 		panel.add(new JLabel(MessageBundle.getMessage("angal.medicalstock.multiplecharging.lotnumberabb")));
1296: 		panel.add(lotCodeTextField);
1297: 		panel.add(new JLabel(MessageBundle.getMessage("angal.medicalstock.multiplecharging.preparationdate")));
1298: 		panel.add(preparationDateChooser);
1299: 		panel.add(new JLabel(MessageBundle.getMessage("angal.medicalstock.multiplecharging.expiringdate")));
1300: 		panel.add(expireDateChooser);
1301: 		do {
1302: 			int ok = JOptionPane.showConfirmDialog(
1303: 				this,
1304: 				panel,
1305: 				MessageBundle.getMessage("angal.medicalstock.multiplecharging.lotinformations"),
1306: 				JOptionPane.OK_CANCEL_OPTION);
1307: 
1308: 			if (ok == JOptionPane.OK_OPTION) {
1309: 				String lotCode = lotCodeTextField.getText();
1310: 				try {
1311: 					/*
1312: 					 * check for lotCode in persisted lots and new lots in the inventoryRowSearchList avoiding null objects or lots without name yet (will be
1313: 					 * shown as "AUTO" and the lotCode will be generated)
1314: 					 */
1315: 					if (movStockInsertingManager.lotExists(lotCode) || inventoryRowSearchList.stream()
1316: 						.map(MedicalInventoryRow::getLot)
1317: 						.filter(Objects::nonNull)
1318: 						.filter(l -> !l.getCode().isEmpty())
1319: 						.anyMatch(l -> l.getCode().equals(lotCode))) {
1320: 						MessageDialog.error(this, "angal.medicalstock.multiplecharging.theinsertedlotcodealreaedyexists.msg");
1321: 						continue;
1322: 					}
1323: 					if (expireDateChooser.getDate().isBefore(preparationDateChooser.getDate())) {
1324: 						MessageDialog.error(this, "angal.medicalstock.multiplecharging.expirydatebeforepreparationdate");
1325: 					} else {
1326: 						expiringDate = expireDateChooser.getDateEndOfDay();
1327: 						preparationDate = preparationDateChooser.getDateStartOfDay();
1328: 						lot = new Lot(assignedMedical, lotCode, preparationDate, expiringDate);
1329: 						BigDecimal cost = BigDecimal.ZERO;
1330: 						if (isLotWithCost()) {
1331: 							if (lotToUpdate != null) {
1332: 								cost = askCost(2, lotToUpdate.getCost());
1333: 							} else {
1334: 								cost = askCost(2, cost);
1335: 							}
1336: 
1337: 							if (cost.compareTo(BigDecimal.ZERO) == 0) {
1338: 								return null;
1339: 							} else {
1340: 								lot.setCost(cost);
1341: 							}
1342: 						}
1343: 					}
1344: 				} catch (OHServiceException e) {
1345: 					OHServiceExceptionUtil.showMessages(e);
1346: 				}
1347: 			} else {
1348: 				return null;
1349: 			}
1350: 		} while (lot == null);
1351: 		return lot;
1352: 	}
1353: 
1354: 	private BigDecimal askCost(int qty, BigDecimal lastCost) {
1355: 		double cost = 0.;
1356: 		do {
1357: 			String input = JOptionPane.showInputDialog(this,
1358: 				MessageBundle.getMessage("angal.medicalstock.multiplecharging.unitcost"),
1359: 				lastCost);
1360: 			if (input != null) {
1361: 				try {
1362: 					cost = Double.parseDouble(input);
1363: 					if (cost < 0) {
1364: 						throw new NumberFormatException();
1365: 					} else if (cost == 0.) {
1366: 						double total = askTotalCost();
1367: 						cost = total / qty;
1368: 					}
1369: 				} catch (NumberFormatException nfe) {
1370: 					MessageDialog.error(this, "angal.medicalstock.multiplecharging.pleaseinsertavalidvalue");
1371: 				}
1372: 			} else {
1373: 				return BigDecimal.valueOf(cost);
1374: 			}
1375: 		} while (cost == 0.);
1376: 		return BigDecimal.valueOf(cost);
1377: 	}
1378: 
1379: 	protected LocalDateTime askExpiringDate() {
1380: 		LocalDateTime date = TimeTools.getNow();
1381: 		GoodDateTimeSpinnerChooser expireDateChooser = new GoodDateTimeSpinnerChooser(date);
1382: 		JPanel panel = new JPanel(new GridLayout(1, 2));
1383: 		panel.add(new JLabel(MessageBundle.getMessage("angal.medicalstock.multiplecharging.expiringdate")));
1384: 		panel.add(expireDateChooser);
1385: 
1386: 		int ok = JOptionPane.showConfirmDialog(this, panel,
1387: 			MessageBundle.getMessage("angal.medicalstock.multiplecharging.expiringdate"),
1388: 			JOptionPane.OK_CANCEL_OPTION);
1389: 
1390: 		if (ok == JOptionPane.OK_OPTION) {
1391: 			date = expireDateChooser.getLocalDateTime();
1392: 		}
1393: 		return date;
1394: 	}
1395: 
1396: 	protected double askTotalCost() {
1397: 		String input = JOptionPane.showInputDialog(this,
1398: 			MessageBundle.getMessage("angal.medicalstock.multiplecharging.totalcost"),
1399: 			0.);
1400: 		double total = 0.;
1401: 		if (input != null) {
1402: 			try {
1403: 				total = Double.parseDouble(input);
1404: 				if (total < 0) {
1405: 					throw new NumberFormatException();
1406: 				}
1407: 			} catch (NumberFormatException nfe) {
1408: 				MessageDialog.error(this, "angal.medicalstock.multiplecharging.pleaseinsertavalidvalue");
1409: 			}
1410: 		}
1411: 		return total;
1412: 	}
1413: 
1414: 	public MedicalInventory getInventory() {
1415: 		return inventory;
1416: 	}
1417: 
1418: 	public void setInventory(MedicalInventory inventory) {
1419: 		this.inventory = inventory;
1420: 	}
1421: 
1422: 	private JLabel getAddMedicalLabel() {
1423: 		if (addMedicalLabel == null) {
1424: 			addMedicalLabel = new JLabel(MessageBundle.getMessage("angal.inventory.addamedical.label"));
1425: 		}
1426: 		return addMedicalLabel;
1427: 	}
1428: 
1429: 	private JLabel getDateInventoryLabel() {
1430: 		if (dateInventoryLabel == null) {
1431: 			dateInventoryLabel = new JLabel(MessageBundle.getMessage("angal.common.date.txt"));
1432: 		}
1433: 		return dateInventoryLabel;
1434: 	}
1435: 
1436: 	private JTextField getMedicalCodeTextField() {
1437: 		if (medicalCodeTextField == null) {
1438: 			medicalCodeTextField = new JTextField();
1439: 			medicalCodeTextField.setColumns(10);
1440: 			TextPrompt suggestion = new TextPrompt(MessageBundle.getMessage("angal.common.code.txt"), medicalCodeTextField, Show.FOCUS_LOST);
1441: 			suggestion.setFont(new Font("Tahoma", Font.PLAIN, 12));
1442: 			suggestion.setForeground(Color.GRAY);
1443: 			suggestion.setHorizontalAlignment(SwingConstants.CENTER);
1444: 			suggestion.changeAlpha(0.5f);
1445: 			suggestion.changeStyle(Font.BOLD + Font.ITALIC);
1446: 			medicalCodeTextField.addKeyListener(new KeyAdapter() {
1447: 
1448: 				@Override
1449: 				public void keyPressed(KeyEvent e) {
1450: 					if (e.getKeyCode() == KeyEvent.VK_ENTER) {
1451: 						code = medicalCodeTextField.getText().trim();
1452: 						try {
1453: 							addInventoryRow(code);
1454: 						} catch (OHServiceException e1) {
1455: 							OHServiceExceptionUtil.showMessages(e1);
1456: 						}
1457: 						if (inventory != null && !inventory.getStatus().equals(InventoryStatus.draft.toString())) {
1458: 							inventory.setStatus(InventoryStatus.draft.toString());
1459: 						}
1460: 						medicalCodeTextField.setText("");
1461: 					}
1462: 				}
1463: 			});
1464: 		}
1465: 		return medicalCodeTextField;
1466: 	}
1467: 
1468: 	private List<MedicalInventoryRow> loadNewInventoryTable(String code, MedicalInventory inventory, boolean add) throws OHServiceException {
1469: 		List<MedicalInventoryRow> inventoryRowsList;
1470: 		if (inventory != null) {
1471: 			int id = inventory.getId();
1472: 			inventoryRowsList = medicalInventoryRowManager.getMedicalInventoryRowByInventoryId(id);
1473: 			if (add) {
1474: 				inventoryRowsList = getMedicalInventoryRows(code);
1475: 			}
1476: 		} else {
1477: 			inventoryRowsList = getMedicalInventoryRows(code);
1478: 		}
1479: 		return inventoryRowsList;
1480: 	}
1481: 
1482: 	private List<MedicalInventoryRow> loadNewInventoryTableByMedicalType(MedicalType medicalType) throws OHServiceException {
1483: 		return getMedicalInventoryRowsByMedicalType(medicalType);
1484: 	}
1485: 
1486: 	private List<MedicalInventoryRow> getMedicalInventoryRows(String code) throws OHServiceException {
1487: 		List<MedicalInventoryRow> inventoryRowsList = new ArrayList<>();
1488: 		List<Medical> medicalList = new ArrayList<>();
1489: 		List<Lot> lots = null;
1490: 		Medical medical = null;
1491: 		MedicalInventoryRow inventoryRowTemp = null;
1492: 		if (code != null && !code.trim().isEmpty()) {
1493: 			medical = findMedicalByCodeOrDescription(code);
1494: 			if (medical != null) {
1495: 				medicalList.add(medical);
1496: 			}
1497: 		} else {
1498: 			medicalList = medicals;
1499: 		}
1500: 		ListIterator<Medical> medicalListIterator = medicalList.listIterator();
1501: 		while (medicalListIterator.hasNext()) {
1502: 			Medical med = medicalListIterator.next();
1503: 			lots = movStockInsertingManager.getLotByMedical(med, false);
1504: 			double actualQty = med.getInqty() - med.getOutqty();
1505: 			if (lots.isEmpty()) {
1506: 				inventoryRowTemp = new MedicalInventoryRow(0, actualQty, actualQty, null, med, null);
1507: 				inventoryRowTemp.setNewLot(true); // missing parameter in the above constructor
1508: 				if (!existInInventorySearchList(inventoryRowTemp)) {
1509: 					inventoryRowsList.add(inventoryRowTemp);
1510: 				}
1511: 			} else {
1512: 				ListIterator<Lot> lotListIterator = lots.listIterator();
1513: 				while (lotListIterator.hasNext()) {
1514: 					Lot lot = lotListIterator.next();
1515: 					inventoryRowTemp = new MedicalInventoryRow(0, lot.getMainStoreQuantity(), lot.getMainStoreQuantity(), null, med, lot);
1516: 					if (!existInInventorySearchList(inventoryRowTemp)) {
1517: 						inventoryRowsList.add(inventoryRowTemp);
1518: 					}
1519: 				}
1520: 			}
1521: 		}
1522: 		return inventoryRowsList;
1523: 	}
1524: 
1525: 	private List<MedicalInventoryRow> getMedicalInventoryRowsByMedicalType(MedicalType medType) throws OHServiceException {
1526: 		List<MedicalInventoryRow> inventoryRowsList = new ArrayList<>();
1527: 		String medTypeDescription = medType.getDescription();
1528: 		List<Medical> medicalList = medicalBrowsingManager.getMedicals(medTypeDescription, false);
1529: 		List<Lot> lots = null;
1530: 		MedicalInventoryRow inventoryRowTemp = null;
1531: 		ListIterator<Medical> medicalListIterator = medicalList.listIterator();
1532: 		while (medicalListIterator.hasNext()) {
1533: 			Medical med = medicalListIterator.next();
1534: 			lots = movStockInsertingManager.getLotByMedical(med, false);
1535: 			double actualQty = med.getInqty() - med.getOutqty();
1536: 			if (lots.isEmpty()) {
1537: 				inventoryRowTemp = new MedicalInventoryRow(0, actualQty, actualQty, null, med, null);
1538: 				inventoryRowTemp.setNewLot(true); // missing parameter in the above constructor
1539: 				if (!existInInventorySearchList(inventoryRowTemp)) {
1540: 					inventoryRowsList.add(inventoryRowTemp);
1541: 				}
1542: 			} else {
1543: 				ListIterator<Lot> lotListIterator = lots.listIterator();
1544: 				while (lotListIterator.hasNext()) {
1545: 					Lot lot = lotListIterator.next();
1546: 					inventoryRowTemp = new MedicalInventoryRow(0, lot.getMainStoreQuantity(), lot.getMainStoreQuantity(), null, med, lot);
1547: 					if (!existInInventorySearchList(inventoryRowTemp)) {
1548: 						inventoryRowsList.add(inventoryRowTemp);
1549: 					}
1550: 				}
1551: 			}
1552: 		}
1553: 		return inventoryRowsList;
1554: 	}
1555: 
1556: 	private List<MedicalInventoryRow> getMedicalInventoryRowsWithMovement() throws OHServiceException {
1557: 		List<MedicalInventoryRow> inventoryRowsList = new ArrayList<>();
1558: 		List<Medical> medicalListWithMovement = new ArrayList<>();
1559: 		List<Lot> lots = null;
1560: 		MedicalInventoryRow inventoryRowTemp = null;
1561: 		List<Medical> medicalList = medicals;
1562: 		ListIterator<Medical> medicalListIterator = medicalList.listIterator();
1563: 		while (medicalListIterator.hasNext()) {
1564: 			Medical med = medicalListIterator.next();
1565: 			Integer medicalCode = med.getCode();
1566: 			List<Movement> movements = movBrowserManager.getMovements(medicalCode, null, null, null, null, null, null, null, null, null);
1567: 			if (!movements.isEmpty()) {
1568: 				medicalListWithMovement.add(med);
1569: 			}
1570: 		}
1571: 		medicalListIterator = medicalListWithMovement.listIterator();
1572: 		while (medicalListIterator.hasNext()) {
1573: 			Medical med = medicalListIterator.next();
1574: 			lots = movStockInsertingManager.getLotByMedical(med, false);
1575: 			double actualQty = med.getInqty() - med.getOutqty();
1576: 			if (lots.isEmpty()) {
1577: 				inventoryRowTemp = new MedicalInventoryRow(0, actualQty, actualQty, null, med, null);
1578: 				inventoryRowTemp.setNewLot(true); // missing parameter in the above constructor
1579: 				if (!existInInventorySearchList(inventoryRowTemp)) {
1580: 					inventoryRowsList.add(inventoryRowTemp);
1581: 				}
1582: 			} else {
1583: 				ListIterator<Lot> lotListIterator = lots.listIterator();
1584: 				while (lotListIterator.hasNext()) {
1585: 					Lot lot = lotListIterator.next();
1586: 					inventoryRowTemp = new MedicalInventoryRow(0, lot.getMainStoreQuantity(), lot.getMainStoreQuantity(), null, med, lot);
1587: 					if (!existInInventorySearchList(inventoryRowTemp)) {
1588: 						inventoryRowsList.add(inventoryRowTemp);
1589: 					}
1590: 				}
1591: 			}
1592: 		}
1593: 		return inventoryRowsList;
1594: 	}
1595: 
1596: 	private void addInventoryRow(String code) throws OHServiceException {
1597: 		List<MedicalInventoryRow> inventoryRowsList = new ArrayList<>();
1598: 		List<Medical> medicalList = new ArrayList<>();
1599: 		List<Lot> lots = null;
1600: 		Medical medical = null;
1601: 		MedicalInventoryRow inventoryRowTemp = null;
1602: 		if (code != null && !code.trim().isEmpty()) {
1603: 			medical = findMedicalByCodeOrDescription(code);
1604: 
1605: 			if (medical != null) {
1606: 				medicalList.add(medical);
1607: 			} else {
1608: 				return;
1609: 			}
1610: 		} else {
1611: 			medicalList = medicals;
1612: 		}
1613: 		int numberOfMedicalWithoutSameLotAdded = 0;
1614: 		Medical medicalWithLot = null;
1615: 		ListIterator<Medical> medicalListIterator = medicalList.listIterator();
1616: 		while (medicalListIterator.hasNext()) {
1617: 			Medical med = medicalListIterator.next();
1618: 			lots = movStockInsertingManager.getLotByMedical(med, false);
1619: 			if (lots.isEmpty()) {
1620: 				inventoryRowTemp = new MedicalInventoryRow(0, 0.0, 0.0, null, med, null);
1621: 				inventoryRowTemp.setNewLot(true); // missing parameter in the above constructor
1622: 				if (!existInInventorySearchList(inventoryRowTemp)) {
1623: 					inventoryRowsList.add(inventoryRowTemp);
1624: 				} else {
1625: 					int info = MessageDialog.yesNo(null, "angal.inventory.productalreadyexist.fmt.msg", med.getDescription());
1626: 					if (info == JOptionPane.YES_OPTION) {
1627: 						inventoryRowsList.add(inventoryRowTemp);
1628: 					}
1629: 				}
1630: 			} else {
1631: 				medicalWithLot = med;
1632: 				ListIterator<Lot> lotListIterator = lots.listIterator();
1633: 				while (lotListIterator.hasNext()) {
1634: 					Lot lot = lotListIterator.next();
1635: 					inventoryRowTemp = new MedicalInventoryRow(0, lot.getMainStoreQuantity(), lot.getMainStoreQuantity(), null, med, lot);
1636: 					if (!existInInventorySearchList(inventoryRowTemp)) {
1637: 						inventoryRowsList.add(inventoryRowTemp);
1638: 						numberOfMedicalWithoutSameLotAdded++;
1639: 					}
1640: 				}
1641: 			}
1642: 		}
1643: 		if (medicalWithLot != null && numberOfMedicalWithoutSameLotAdded == 0) {
1644: 			int info = MessageDialog.yesNo(null, "angal.inventory.productalreadyexist.fmt.msg", medicalWithLot.getDescription());
1645: 			if (info == JOptionPane.YES_OPTION) {
1646: 				inventoryRowTemp = new MedicalInventoryRow(0, 0.0, 0.0, null, medicalWithLot, null);
1647: 				inventoryRowTemp.setNewLot(true); // missing parameter in the above constructor
1648: 				inventoryRowsList.add(inventoryRowTemp);
1649: 			}
1650: 		}
1651: 		for (MedicalInventoryRow inventoryRow : inventoryRowsList) {
1652: 			addMedInRowInInventorySearchList(inventoryRow);
1653: 		}
1654: 		jTableInventoryRow.updateUI();
1655: 	}
1656: 
1657: 	private Medical findMedicalByCodeOrDescription(String text) throws OHServiceException {
1658: 		String searchText = text.trim();
1659: 		Medical medical = medicalBrowsingManager.getMedicalByMedicalCode(searchText);
1660: 		if (medical == null) {
1661: 			medical = chooseMedical(searchText);
1662: 		}
1663: 		return medical;
1664: 	}
1665: 
1666: 	private Medical chooseMedical(String text) throws OHServiceException {
1667: 		List<Medical> medList = new ArrayList<>();
1668: 
1669: 		String searchText = text.trim();
1670: 
1671: 		for (Medical med : medicals) {
1672: 			if (matchesMedicalSearch(med, searchText)) {
1673: 				medList.add(med);
1674: 			}
1675: 		}
1676: 
1677: 		medList.sort(Comparator.comparing(Medical::getDescription));
1678: 		Medical med = null;
1679: 		if (!medList.isEmpty()) {
1680: 			MedicalPicker framas = new MedicalPicker(new StockMedModel(medList), medList);
1681: 			JDialog dialog = new JDialog();
1682: 			dialog.setLocationRelativeTo(null);
1683: 			dialog.setSize(600, 350);
1684: 			dialog.setModal(true);
1685: 			dialog.setTitle(MessageBundle.getMessage("angal.medicalstock.multiplecharging.chooseamedical"));
1686: 			framas.setParentFrame(dialog);
1687: 			dialog.setContentPane(framas);
1688: 			dialog.setVisible(true);
1689: 			dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
1690: 			med = framas.getSelectedMedical();
1691: 		}
1692: 		return med;
1693: 	}
1694: 
1695: 	private boolean matchesMedicalSearch(Medical medical, String searchText) {
1696: 		String productCode = Objects.toString(medical.getProdCode(), "");
1697: 		String description = Objects.toString(medical.getDescription(), "");
1698: 		String searchableText = normalizeSearchValue(productCode + " " + description);
1699: 		String normalizedSearch = normalizeSearchValue(searchText);
1700: 
1701: 		return productCode.equalsIgnoreCase(searchText)
1702: 			|| searchableText.contains(normalizedSearch)
1703: 			|| matchesAllSearchTokens(searchableText, normalizedSearch);
1704: 	}
1705: 
1706: 	private String normalizeSearchValue(String value) {
1707: 		return NormalizeString.normalizeString(value)
1708: 			.toLowerCase()
1709: 			.replaceAll("\\s+", " ")
1710: 			.trim();
1711: 	}
1712: 
1713: 	private boolean matchesAllSearchTokens(String searchableText, String normalizedSearch) {
1714: 		if (normalizedSearch.isEmpty()) {
1715: 			return false;
1716: 		}
1717: 		for (String token : normalizedSearch.split(" ")) {
1718: 			if (!searchableText.contains(token)) {
1719: 				return false;
1720: 			}
1721: 		}
1722: 		return true;
1723: 	}
1724: 
1725: 	private JLabel getReferenceLabel() {
1726: 		if (referenceLabel == null) {
1727: 			referenceLabel = new JLabel(MessageBundle.getMessage("angal.inventory.reference.label"));
1728: 		}
1729: 		return referenceLabel;
1730: 	}
1731: 
1732: 	private JTextField getReferenceTextField() {
1733: 		if (referenceTextField == null) {
1734: 			referenceTextField = new VoLimitedTextField(40, 10); // limit to 40 because of potential suffixes
1735: 			if (inventory != null && !mode.equals("new")) {
1736: 				referenceTextField.setText(inventory.getInventoryReference());
1737: 				newReference = inventory.getInventoryReference();
1738: 			}
1739: 		}
1740: 		return referenceTextField;
1741: 	}
1742: 
1743: 	private JLabel getStatusLabel() {
1744: 		if (statusLabel == null) {
1745: 			if (inventory == null) {
1746: 				String currentStatus = InventoryStatus.draft.toString();
1747: 				String status = medicalInventoryManager.getStatusByKey(currentStatus);
1748: 				statusLabel = new JLabel(status.toUpperCase());
1749: 				statusLabel.setForeground(Color.GRAY);
1750: 			} else {
1751: 				String currentStatus = inventory.getStatus();
1752: 				String status = medicalInventoryManager.getStatusByKey(currentStatus);
1753: 				statusLabel = new JLabel(status.toUpperCase());
1754: 				if (currentStatus.equalsIgnoreCase(InventoryStatus.draft.toString())) {
1755: 					statusLabel.setForeground(Color.GRAY);
1756: 				}
1757: 				if (currentStatus.equalsIgnoreCase(InventoryStatus.validated.toString())) {
1758: 					statusLabel.setForeground(Color.BLUE);
1759: 				}
1760: 				if (currentStatus.equalsIgnoreCase(InventoryStatus.canceled.toString())) {
1761: 					statusLabel.setForeground(Color.RED);
1762: 				}
1763: 				if (currentStatus.equalsIgnoreCase(InventoryStatus.done.toString())) {
1764: 					statusLabel.setForeground(Color.GREEN);
1765: 				}
1766: 			}
1767: 			statusLabel.setFont(new Font(statusLabel.getFont().getName(), Font.BOLD, statusLabel.getFont().getSize() + 8));
1768: 		}
1769: 		return statusLabel;
1770: 	}
1771: 
1772: 	private JLabel getAddMedicalsByLabel() {
1773: 		if (addMedicalsByLabel == null) {
1774: 			addMedicalsByLabel = new JLabel(MessageBundle.getMessage("angal.inventory.addmedicalsby.label"));
1775: 		}
1776: 		return addMedicalsByLabel;
1777: 	}
1778: 
1779: 	private JLabel getDischargeLabel() {
1780: 		if (dischargeTypeLabel == null) {
1781: 			dischargeTypeLabel = new JLabel(MessageBundle.getMessage("angal.inventory.dischargetype.label"));
1782: 		}
1783: 		return dischargeTypeLabel;
1784: 	}
1785: 	private JComboBox<MovementType> getJComboDischarge() {
1786: 		MovementType movementSelected = null;
1787: 		if (dischargeCombo == null) {
1788: 			dischargeCombo = new JComboBox<>();
1789: 			try {
1790: 				List<MovementType> movementTypes = movTypeManager.getMedicalDsrStockMovementType();
1791: 				dischargeCombo.addItem(null);
1792: 				for (MovementType movType : movementTypes) {
1793: 					if (movType.getType().equals("-")) {
1794: 						dischargeCombo.addItem(movType);
1795: 						if (inventory != null && movType.getCode().equals(inventory.getDischargeType())) {
1796: 							movementSelected = movType;
1797: 						}
1798: 					}
1799: 				}
1800: 			} catch (OHServiceException e) {
1801: 				OHServiceExceptionUtil.showMessages(e);
1802: 			}
1803: 			if (inventory != null) {
1804: 				dischargeCombo.setSelectedItem(movementSelected);
1805: 				dischargeType = movementSelected;
1806: 			}
1807: 			dischargeCombo.addActionListener(actionEvent -> dischargeType = (MovementType) dischargeCombo.getSelectedItem());
1808: 		}
1809: 		return dischargeCombo;
1810: 	}
1811: 
1812: 	private int getPosition(MedicalInventoryRow inventoryRow) {
1813: 		int position = -1;
1814: 		int i = 0;
1815: 		for (MedicalInventoryRow invR : inventoryRowSearchList) {
1816: 			if (invR.getMedical().getCode().equals(inventoryRow.getMedical().getCode())) {
1817: 				position = i;
1818: 			}
1819: 			i = i + 1;
1820: 		}
1821: 		return position;
1822: 	}
1823: 
1824: 	private boolean existInInventorySearchList(MedicalInventoryRow inventoryRow) {
1825: 		boolean found = false;
1826: 		List<MedicalInventoryRow> invRows = inventoryRowSearchList.stream()
1827: 			.filter(inv -> inv.getMedical().getCode().equals(inventoryRow.getMedical().getCode())).toList();
1828: 		if (!invRows.isEmpty()) {
1829: 			for (MedicalInventoryRow invR : invRows) {
1830: 				if (inventoryRow.getLot() != null && invR.getLot() != null) {
1831: 					if (inventoryRow.getLot().getCode().equals(invR.getLot().getCode())) {
1832: 						found = true;
1833: 						break;
1834: 					}
1835: 				} else {
1836: 					if (invR.getLot() == null && inventoryRow.getLot() == null) {
1837: 						found = true;
1838: 						break;
1839: 					}
1840: 				}
1841: 			}
1842: 		}
1843: 
1844: 		return found;
1845: 	}
1846: 
1847: 	private void addMedInRowInInventorySearchList(MedicalInventoryRow inventoryRow) {
1848: 		int position = getPosition(inventoryRow);
1849: 		if (position == -1) {
1850: 			position = inventoryRowSearchList.size();
1851: 			inventoryRowSearchList.add(position, inventoryRow);
1852: 		} else {
1853: 			inventoryRowSearchList.add(position + 1, inventoryRow);
1854: 		}
1855: 		if (inventoryRow.getId() == 0) {
1856: 			inventoryRowListAdded.add(inventoryRow);
1857: 		}
1858: 	}
1859: 
1860: 	private void resetVariables() {
1861: 		inventoryRowsToDelete.clear();
1862: 		lotsDeleted.clear();
1863: 		inventoryRowListAdded.clear();
1864: 		lotsSaved.clear();
1865: 	}
1866: 
1867: 	private boolean checkParametersChanges(String dischargeCode, String reference, LocalDateTime date) {
1868: 		return !lotsSaved.isEmpty()
1869: 			|| !inventoryRowListAdded.isEmpty()
1870: 			|| !lotsDeleted.isEmpty()
1871: 			|| !inventoryRowsToDelete.isEmpty()
1872: 			|| (dischargeType != null && isMismatch(dischargeType.getCode(), dischargeCode))
1873: 			|| (reference != null && !reference.equals(newReference))
1874: 			|| !isSameDateTime(date, dateInventory);
1875: 	}
1876: 
1877: 	private boolean isMismatch(String value, String expectedValue) {
1878: 		return !value.equals(expectedValue);
1879: 	}
1880: 
1881: 	private boolean isSameDateTime(LocalDateTime date1, LocalDateTime date2) {
1882: 		return TimeTools.isSameDateTime(date1, date2);
1883: 	}
1884: 
1885: 	private JComboBox<MedicalType> getJComboMedicalType() {
1886: 		if (medicalTypeComboBox == null) {
1887: 			medicalTypeComboBox = new JComboBox<>();
1888: 			try {
1889: 				List<MedicalType> medicalTypes = medicalTypeManager.getMedicalType();
1890: 				MedicalType medicalType = new MedicalType(MessageBundle.getMessage("angal.common.all.txt"), MessageBundle.getMessage("angal.common.all.txt"));
1891: 				medicalTypeComboBox.addItem(medicalType);
1892: 				for (MedicalType medType : medicalTypes) {
1893: 					medicalTypeComboBox.addItem(medType);
1894: 				}
1895: 			} catch (OHServiceException e) {
1896: 				OHServiceExceptionUtil.showMessages(e);
1897: 			}
1898: 			medicalTypeSelected = (MedicalType) medicalTypeComboBox.getSelectedItem();
1899: 			medicalTypeComboBox.addActionListener(actionEvent -> medicalTypeSelected = (MedicalType) medicalTypeComboBox.getSelectedItem());
1900: 		}
1901: 		return medicalTypeComboBox;
1902: 	}
1903: 
1904: 	private JRadioButton getAllRadioButton() {
1905: 		if (radioButtonAll == null) {
1906: 			radioButtonAll = new JRadioButton(MessageBundle.getMessage("angal.common.all.btn"));
1907: 			radioButtonAll.setSelected(true);
1908: 		}
1909: 		return radioButtonAll;
1910: 	}
1911: 
1912: 	private JRadioButton getMedicalWithNonZeroQuantityRadioButton() {
1913: 		if (radioOnlyNonZero == null) {
1914: 			radioOnlyNonZero = new JRadioButton(MessageBundle.getMessage("angal.inventory.medicalwithonlynonzeroqty.btn"));
1915: 		}
1916: 		return radioOnlyNonZero;
1917: 	}
1918: 
1919: 	private JRadioButton getMedicalWithMovementRadioButton() {
1920: 		if (radioWithMovement == null) {
1921: 			radioWithMovement = new JRadioButton(MessageBundle.getMessage("angal.inventory.medicalwithmovementonly.btn"));
1922: 		}
1923: 		return radioWithMovement;
1924: 	}
1925: 
1926: 	private JButton getCancelButton() {
1927: 		if (jButtonCancel == null) {
1928: 			jButtonCancel = new JButton(MessageBundle.getMessage("angal.common.cancel.btn"));
1929: 			jButtonCancel.setMnemonic(MessageBundle.getMnemonic("angal.common.cancel.btn.key"));
1930: 		}
1931: 		jButtonCancel.addActionListener(actionEvent -> frame.dispose());
1932: 		return jButtonCancel;
1933: 	}
1934: 
1935: 	private List<MedicalInventoryRow> loadNewInventoryTable(boolean withNonZeroQty, MedicalType medicalTypeSelected) throws OHServiceException {
1936: 		List<MedicalInventoryRow> inventoryRowsList = getMedicalInventoryRows(null);
1937: 		if (withNonZeroQty) {
1938: 			inventoryRowsList = inventoryRowsList.stream().filter(inv -> inv.getTheoreticQty() > 0).collect(Collectors.toList());
1939: 		}
1940: 		if (medicalTypeSelected != null) {
1941: 			inventoryRowsList = inventoryRowsList.stream()
1942: 				.filter(inv -> inv.getMedical().getType().getDescription().equals(medicalTypeSelected.getDescription()))
1943: 				.collect(Collectors.toList());
1944: 		}
1945: 		return inventoryRowsList;
1946: 	}
1947: 
1948: 	private List<MedicalInventoryRow> loadNewInventoryTable(MedicalType medicalTypeSelected) throws OHServiceException {
1949: 		List<MedicalInventoryRow> inventoryRowsList = getMedicalInventoryRowsWithMovement();
1950: 		if (medicalTypeSelected != null) {
1951: 			inventoryRowsList = inventoryRowsList.stream()
1952: 				.filter(inv -> inv.getMedical().getType().getDescription().equals(medicalTypeSelected.getDescription()))
1953: 				.collect(Collectors.toList());
1954: 		}
1955: 		return inventoryRowsList;
1956: 	}
1957: 
1958: 	private JButton getOkButton() {
1959: 		if (jButtonOk == null) {
1960: 			jButtonOk = new JButton(MessageBundle.getMessage("angal.common.ok.btn"));
1961: 			jButtonOk.setMnemonic(MessageBundle.getMnemonic("angal.common.ok.btn.key"));
1962: 			jButtonOk.addActionListener(actionEvent -> {
1963: 				actions.forEach((key, value) -> {
1964: 					if (key.isSelected()) {
1965: 						actions.get(key).run();
1966: 					}
1967: 				});
1968: 				jButtonCancel.doClick();
1969: 			});
1970: 		}
1971: 		return jButtonOk;
1972: 	}
1973: 
1974: 	private boolean areAllMedicalsInInventory() throws OHServiceException {
1975: 		Set<Medical> inventorySet = new HashSet<>();
1976: 		for (MedicalInventoryRow row : inventoryRowSearchList) {
1977: 			inventorySet.add(row.getMedical());
1978: 		}
1979: 		return medicals.size() == inventorySet.size();
1980: 	}
1981: 
1982: 	private void initializeActions() {
1983: 		actions.put(radioButtonAll, () -> handleInventoryUpdate(true, false, false));
1984: 		actions.put(radioOnlyNonZero, () -> handleInventoryUpdate(false, false, true));
1985: 		actions.put(radioWithMovement, () -> handleInventoryUpdate(false, true, false));
1986: 	}
1987: 
1988: 	private void handleInventoryUpdate(boolean includeAll, boolean withMovement, boolean nonZero) {
1989: 		boolean isAllSelected = medicalTypeSelected.getDescription().equals(MessageBundle.getMessage("angal.common.all.txt"));
1990: 		MedicalType medType = isAllSelected ? null : (MedicalType) medicalTypeComboBox.getSelectedItem();
1991: 
1992: 		try {
1993: 			if (!areAllMedicalsInInventory()) {
1994: 				int userChoice = (!inventoryRowSearchList.isEmpty())
1995: 					? MessageDialog.yesNo(null, "angal.inventory.doyouwanttoaddallnotyetlistedproducts.msg")
1996: 					: JOptionPane.YES_OPTION;
1997: 
1998: 				if (userChoice == JOptionPane.YES_OPTION) {
1999: 					jTableInventoryRow.setModel(new InventoryRowModel(medType, nonZero, includeAll, withMovement));
2000: 					fireInventoryUpdated();
2001: 					showUpdateSuccessMessage();
2002: 				}
2003: 			} else {
2004: 				MessageDialog.info(null, "angal.inventory.youhavealreadyaddedallproduct.msg");
2005: 			}
2006: 		} catch (OHServiceException e) {
2007: 			OHServiceExceptionUtil.showMessages(e);
2008: 		}
2009: 	}
2010: 
2011: 	private void showUpdateSuccessMessage() {
2012: 		if (!inventoryRowList.isEmpty()) {
2013: 			MessageDialog.info(null, "angal.inventory.tablehasbeenupdated.msg");
2014: 		} else {
2015: 			MessageDialog.info(null, "angal.inventory.notdataforthatfilter.msg");
2016: 		}
2017: 	}
2018: 
2019: 	class CenterTableCellRenderer extends DefaultTableCellRenderer {
2020: 
2021: 		private static final long serialVersionUID = 1L;
2022: 
2023: 		@Override
2024: 		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
2025: 
2026: 			int differenceColumn = table.getColumnModel().getColumnIndex(MessageBundle.getMessage("angal.inventory.difference.col").toUpperCase());
2027: 			Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
2028: 			setHorizontalAlignment(CENTER);
2029: 			if (column == differenceColumn) {
2030: 				setForeground(Color.RED);
2031: 			} else {
2032: 				setForeground(Color.BLACK);
2033: 			}
2034: 			return cell;
2035: 		}
2036: 	}
2037: 
2038: 	public class DecimalNumberTableCellRenderer extends DefaultTableCellRenderer {
2039: 
2040: 		private static final long serialVersionUID = 1L;
2041: 
2042: 		@Override
2043: 		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
2044: 
2045: 			JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
2046: 			if (value instanceof BigDecimal) {
2047: 				lbl.setText(String.format("%.02f", value));
2048: 			}
2049: 			lbl.setOpaque(true);
2050: 			lbl.setBackground(Color.WHITE);
2051: 			setHorizontalAlignment(CENTER);
2052: 			return lbl;
2053: 		}
2054: 	}
2055: }

(End of file - total 2055 lines)
</content>
```

---

## Assistant (Plan · Big Pickle · 16.8s)

_Thinking:_

Now I understand the code. Let me also look at the `getLotByMedical` method in the core to understand how lots are fetched.

**Tool: grep**

**Input:**
```json
{
  "include": "*.java",
  "path": "/home/uni2grow/Documents/openhospital/openhospital-core/src",
  "pattern": "getLotByMedical"
}
```

**Output:**
```
Found 9 matches
/home/uni2grow/Documents/openhospital/openhospital-core/src/test/java/org/isf/medicalstock/Tests.java:
  Line 787: 		List<Lot> lots = movStockInsertingManager.getLotByMedical(foundMovement.getMedical(), true);

  Line 799: 		List<Lot> lots = movStockInsertingManager.getLotByMedical(foundMovement.getMedical(), false);

  Line 808: 		assertThat(movStockInsertingManager.getLotByMedical(null, true)).isEmpty();


/home/uni2grow/Documents/openhospital/openhospital-core/src/main/java/org/isf/accounting/manager/BillBrowserManager.java:
  Line 510: 			Lot lot = movStockInsertingManager.getLotByMedical(medical, false).stream().findFirst().orElse(null);


/home/uni2grow/Documents/openhospital/openhospital-core/src/main/java/org/isf/medicalstock/manager/MovBrowserManager.java:
  Line 117: 	 * {@link MovStockInsertingManager#getLotByMedical(Medical) getLotByMedical} for that.


/home/uni2grow/Documents/openhospital/openhospital-core/src/main/java/org/isf/medicalstock/manager/MovStockInsertingManager.java:
  Line 270: 	public List<Lot> getLotByMedical(Medical medical) throws OHServiceException {

  Line 283: 	public List<Lot> getLotByMedical(Medical medical, boolean removeEmpty) throws OHServiceException {


/home/uni2grow/Documents/openhospital/openhospital-core/src/main/java/org/isf/medicalinventory/manager/MedicalInventoryManager.java:
  Line 335: 			Optional<Lot> optLot = movStockInsertingManager.getLotByMedical(medical, false).stream().filter(l -> l.getCode().equals(lotCodeOfMovement))

  Line 722: 			Optional<Lot> optLot = movStockInsertingManager.getLotByMedical(medical, false).stream().filter(l -> l.getCode().equals(lotCode))

```

---

## Assistant (Plan · Big Pickle · 53.0s)

_Thinking:_

Let me look at the `MovStockInsertingManager.getLotByMedical` method to understand how lots are fetched and filtered.

**Tool: read**

**Input:**
```json
{
  "filePath": "/home/uni2grow/Documents/openhospital/openhospital-core/src/main/java/org/isf/medicalstock/manager/MovStockInsertingManager.java",
  "offset": 260,
  "limit": 80
}
```

**Output:**
```
<path>/home/uni2grow/Documents/openhospital/openhospital-core/src/main/java/org/isf/medicalstock/manager/MovStockInsertingManager.java</path>
<type>file</type>
<content>
260: 		return GeneralData.AUTOMATICLOT_OUT;
261: 	}
262: 
263: 	/**
264: 	 * Retrieves all the {@link Lot} associated to the specified {@link Medical}, expiring first on top, zero quantities will be stripped out.
265: 	 * 
266: 	 * @param medical the medical.
267: 	 * @return the list of retrieved {@link Lot}s.
268: 	 * @throws OHServiceException
269: 	 */
270: 	public List<Lot> getLotByMedical(Medical medical) throws OHServiceException {
271: 		return ioOperations.getLotsByMedical(medical, true);
272: 	}
273: 
274: 	/**
275: 	 * Retrieves all the {@link Lot} associated to the specified {@link Medical}, expiring first on top, zero quantities will be stripped out if
276: 	 * {@code removeEmpty} is set to true.
277: 	 *
278: 	 * @param medical the medical.
279: 	 * @param removeEmpty
280: 	 * @return the list of retrieved {@link Lot}s.
281: 	 * @throws OHServiceException
282: 	 */
283: 	public List<Lot> getLotByMedical(Medical medical, boolean removeEmpty) throws OHServiceException {
284: 		if (medical == null) {
285: 			return new ArrayList<>();
286: 		}
287: 		return ioOperations.getLotsByMedical(medical, removeEmpty);
288: 	}
289: 
290: 	/**
291: 	 * Batch loads the nearest expiry date for each medical in the provided list.
292: 	 *
293: 	 * @param medicalCodes list of medical codes
294: 	 * @return map of medical code to nearest expiry date
295: 	 * @throws OHServiceException
296: 	 */
297: 	public Map<Integer, LocalDate> getNearestExpiryDateByMedicals(List<Integer> medicalCodes) throws OHServiceException {
298: 		return ioOperations.getNearestExpiryDateByMedicalCodes(medicalCodes);
299: 	}
300: 
301: 	/**
302: 	 * Checks if the provided quantity is under the medical limits.
303: 	 *
304: 	 * @param medicalSelected the selected medical.
305: 	 * @param specifiedQuantity the quantity provided by the user.
306: 	 * @return {@code true} if is under the limit, {@code false} otherwise.
307: 	 * @throws OHServiceException
308: 	 */
309: 	public boolean alertCriticalQuantity(Medical medicalSelected, int specifiedQuantity) throws OHServiceException {
310: 		Medical medical = ioOperationsMedicals.getMedical(medicalSelected.getCode());
311: 		double totalQuantity = medical.getTotalQuantity();
312: 		double residual = totalQuantity - specifiedQuantity;
313: 		return residual < medical.getMinqty();
314: 	}
315: 
316: 	/**
317: 	 * Returns the date of the last movement.
318: 	 *
319: 	 * @return
320: 	 * @throws OHServiceException
321: 	 */
322: 	public LocalDateTime getLastMovementDate() throws OHServiceException {
323: 		return ioOperations.getLastMovementDate();
324: 	}
325: 
326: 	/**
327: 	 * Check if the reference number is already used.
328: 	 *
329: 	 * @return {@code true} if is already used,{@code false} otherwise.
330: 	 * @throws OHServiceException
331: 	 */
332: 	public boolean refNoExists(String refNo) throws OHServiceException {
333: 		return ioOperations.refNoExists(refNo);
334: 	}
335: 
336: 	/**
337: 	 * Insert a list of charging {@link Movement}s and related {@link Lot}s
338: 	 *
339: 	 * @param movements the list of {@link Movement}s

(Showing lines 260-339 of 541. Use offset=340 to continue.)
</content>
```

---

## Assistant (Plan · Big Pickle · 22.2s)

_Thinking:_

Now let me look at the `getLotsByMedical` method in the IO operations to understand how lots are fetched and filtered.

**Tool: grep**

**Input:**
```json
{
  "include": "*.java",
  "path": "/home/uni2grow/Documents/openhospital/openhospital-core/src",
  "pattern": "getLotsByMedical"
}
```

**Output:**
```
Found 7 matches
/home/uni2grow/Documents/openhospital/openhospital-core/src/test/java/org/isf/medicalstock/Tests.java:
  Line 235: 		List<Lot> lots = medicalStockIoOperation.getLotsByMedical(foundMovement.getMedical(), true);

  Line 251: 		List<Lot> lots = medicalStockIoOperation.getLotsByMedical(foundMovement.getMedical(), true);

  Line 329: 		List<Lot> lots = medicalStockIoOperation.getLotsByMedical(medical, true);


/home/uni2grow/Documents/openhospital/openhospital-core/src/main/java/org/isf/medicalstock/service/MedicalStockIoOperations.java:
  Line 134: 		List<Lot> lots = getLotsByMedical(movement.getMedical(), true);

  Line 677: 	public List<Lot> getLotsByMedical(Medical medical, boolean removeEmpty) throws OHServiceException {


/home/uni2grow/Documents/openhospital/openhospital-core/src/main/java/org/isf/medicalstock/manager/MovStockInsertingManager.java:
  Line 271: 		return ioOperations.getLotsByMedical(medical, true);

  Line 287: 		return ioOperations.getLotsByMedical(medical, removeEmpty);

```

---

## Assistant (Plan · Big Pickle · 12.4s)

_Thinking:_

Now let me read the `getLotsByMedical` method in `MedicalStockIoOperations.java`.

**Tool: read**

**Input:**
```json
{
  "filePath": "/home/uni2grow/Documents/openhospital/openhospital-core/src/main/java/org/isf/medicalstock/service/MedicalStockIoOperations.java",
  "limit": 80,
  "offset": 670
}
```

**Output:**
```
<path>/home/uni2grow/Documents/openhospital/openhospital-core/src/main/java/org/isf/medicalstock/service/MedicalStockIoOperations.java</path>
<type>file</type>
<content>
670: 	 * true.
671: 	 * 
672: 	 * @param medical the medical.
673: 	 * @param removeEmpty
674: 	 * @return a list of {@link Lot}.
675: 	 * @throws OHServiceException if an error occurs retrieving the lot list.
676: 	 */
677: 	public List<Lot> getLotsByMedical(Medical medical, boolean removeEmpty) throws OHServiceException {
678: 		List<Lot> lots = lotRepository.findByMedicalOrderByDueDate(medical.getCode());
679: 
680: 		if (lots.isEmpty()) {
681: 			return Collections.emptyList();
682: 		}
683: 
684: 		// Get all lot IDs
685: 		List<String> lotCodes = lots.stream().map(Lot::getCode).collect(Collectors.toList());
686: 
687: 		// Retrieve quantities in batch
688: 		List<Object[]> mainStoreQuantities = lotRepository.getMainStoreQuantities(lotCodes);
689: 		List<Object[]> wardsTotalQuantities = lotRepository.getWardsTotalQuantities(lotCodes);
690: 
691: 		// Process mainStoreQuantities and update lots
692: 		for (Object[] result : mainStoreQuantities) {
693: 			String lotCode = (String) result[0];
694: 			int mainStoreQuantity = ((Long) result[1]).intValue();
695: 
696: 			// Find the corresponding lot in the lots list
697: 			Optional<Lot> matchingLot = lots.stream().filter(lot -> lot.getCode().equals(lotCode)).findFirst();
698: 
699: 			// Update the lot if found
700: 			matchingLot.ifPresent(lot -> lot.setMainStoreQuantity(mainStoreQuantity));
701: 		}
702: 
703: 		// Process wardsTotalQuantities and update lots
704: 		for (Object[] result : wardsTotalQuantities) {
705: 			String lotCode = (String) result[0];
706: 			Double wardsTotalQuantity = (Double) result[1];
707: 
708: 			// Find the corresponding lot in the lots list
709: 			Optional<Lot> matchingLot = lots.stream().filter(lot -> lot.getCode().equals(lotCode)).findFirst();
710: 
711: 			// Update the lot if found
712: 			matchingLot.ifPresent(lot -> lot.setWardsTotalQuantity(wardsTotalQuantity));
713: 		}
714: 
715: 		// Remove empty lots
716: 		if (removeEmpty) {
717: 			lots.removeIf(lot -> lot.getMainStoreQuantity() <= 0);
718: 		}
719: 
720: 		return lots;
721: 	}
722: 
723: 	/**
724: 	 * Batch loads the nearest expiry date for each medical in the provided list.
725: 	 *
726: 	 * @param medicalCodes list of medical codes
727: 	 * @return map of medical code to nearest expiry date (only for medicals with lots in stock)
728: 	 * @throws OHServiceException if an error occurs
729: 	 */
730: 	public Map<Integer, LocalDate> getNearestExpiryDateByMedicalCodes(List<Integer> medicalCodes) throws OHServiceException {
731: 		if (medicalCodes == null || medicalCodes.isEmpty()) {
732: 			return Collections.emptyMap();
733: 		}
734: 
735: 		List<Lot> allLots = lotRepository.findByMedicalCodeInOrderByDueDate(medicalCodes);
736: 
737: 		if (allLots.isEmpty()) {
738: 			return Collections.emptyMap();
739: 		}
740: 
741: 		List<String> lotCodes = allLots.stream().map(Lot::getCode).collect(Collectors.toList());
742: 		List<Object[]> mainStoreQuantities = lotRepository.getMainStoreQuantities(lotCodes);
743: 
744: 		Map<String, Integer> qtyByLot = new HashMap<>();
745: 		for (Object[] row : mainStoreQuantities) {
746: 			String lotCode = (String) row[0];
747: 			int qty = ((Long) row[1]).intValue();
748: 			qtyByLot.put(lotCode, qty);
749: 		}

(Showing lines 670-749 of 938. Use offset=750 to continue.)
</content>
```

---

## Assistant (Plan · Big Pickle · 34.2s)

_Thinking:_

Now I understand the full picture. Let me analyze what's happening:

1. In `InventoryEdit.java`, when a medical is selected (via code or "Select" button), the code calls `getLotByMedical(med, false)` - note the `false` parameter for `removeEmpty`.

2. The `getLotsByMedical` method in `MedicalStockIoOperations.java` (line 677-721) fetches all lots for a medical, calculates their quantities, and if `removeEmpty` is `true`, removes lots with `mainStoreQuantity <= 0`.

3. In the inventory code, `getLotByMedical(med, false)` is called with `removeEmpty=false`, meaning **all lots including empty ones are returned**.

The user wants to filter out empty lots (lots with zero quantity) when displaying lots for a selected medication in the inventory.

Looking at the code, there are multiple places where lots are fetched with `removeEmpty=false`:

1. `getMedicalInventoryRows` (line 1503): `lots = movStockInsertingManager.getLotByMedical(med, false);`
2. `getMedicalInventoryRowsByMedicalType` (line 1534): `lots = movStockInsertingManager.getLotByMedical(med, false);`
3. `getMedicalInventoryRowsWithMovement` (line 1574): `lots = movStockInsertingManager.getLotByMedical(med, false);`
4. `addInventoryRow` (line 1618): `lots = movStockInsertingManager.getLotByMedical(med, false);`

All four methods pass `false` for `removeEmpty`, which means empty lots are included.

The fix is simple: change `false` to `true` in all these method calls to exclude empty lots when displaying the inventory rows.

Wait, but I need to think about this more carefully. The inventory is about counting physical stock. If a lot has zero quantity, the user might still need to see it to confirm that it's indeed empty (theoretically) and enter a real quantity. However, the user specifically asked to remove empty lots.

Let me also consider: when the inventory shows lots for a medication, what if the medication has no lots at all? In that case, the code creates a row with `null` lot. But if there are lots but all are empty, should we still show them?

The user wants: "lorsqu'on selectionne un medicament dans l'inventaire on n'affiche ses differents lots en retirant les lots vides"

Translation: "when we select a medication in the inventory, display its different lots while removing empty lots"

So the fix is to change `false` to `true` in the 4 locations where `getLotByMedical` is called.

But wait, I need to think about the edge case: if all lots for a medical are empty (quantity 0), then after filtering, the lots list would be empty. In that case, the code currently creates a row with `null` lot (see lines 1505-1509). This behavior should still work correctly because the check is `if (lots.isEmpty())`.

Actually, let me re-read the code more carefully:

```java
lots = movStockInsertingManager.getLotByMedical(med, false);
double actualQty = med.getInqty() - med.getOutqty();
if (lots.isEmpty()) {
    inventoryRowTemp = new MedicalInventoryRow(0, actualQty, actualQty, null, med, null);
    inventoryRowTemp.setNewLot(true);
    ...
} else {
    ListIterator<Lot> lotListIterator = lots.listIterator();
    while (lotListIterator.hasNext()) {
        Lot lot = lotListIterator.next();
        inventoryRowTemp = new MedicalInventoryRow(0, lot.getMainStoreQuantity(), lot.getMainStoreQuantity(), null, med, lot);
        ...
    }
}
```

So the current logic is:
- If there are no lots, create a row without a lot (new lot)
- If there are lots, create a row for each lot

If we change to `removeEmpty=true`, the behavior would be:
- If all lots are empty, `lots` would be empty after filtering, and we'd create a row without a lot (new lot)
- If some lots are non-empty, we'd create rows only for those non-empty lots

This seems correct. The user only wants to see lots that actually have stock.

Let me now plan this change. It's very straightforward - just change4 occurrences of `false` to `true`.

Actually wait, I should also check what happens in the `addInventoryRow` method (line 1596). This method is called when the user types a medical code and presses Enter. It has slightly different logic. Let me re-read it...

In `addInventoryRow` (line 1618):
```java
lots = movStockInsertingManager.getLotByMedical(med, false);
if (lots.isEmpty()) {
    inventoryRowTemp = new MedicalInventoryRow(0, 0.0, 0.0, null, med, null);
    ...
} else {
    medicalWithLot = med;
    ListIterator<Lot> lotListIterator = lots.listIterator();
    while (lotListIterator.hasNext()) {
        Lot lot = lotListIterator.next();
        inventoryRowTemp = new MedicalInventoryRow(0, lot.getMainStoreQuantity(), lot.getMainStoreQuantity(), null, med, lot);
        ...
    }
}
```

Same pattern - changing `false` to `true` here too would filter out empty lots.

This is a very simple change. Let me present the plan to the user.

Now I have a complete understanding of the codebase. Here's my analysis:

## Plan

The issue is that when a medication is selected in the inventory, all lots are displayed including those with zero quantity (empty lots).

### Root Cause

In `InventoryEdit.java`, there are **4 places** where `getLotByMedical(med, false)` is called with `removeEmpty=false`:

| Line | Method |
|------|--------|
| 1503 | `getMedicalInventoryRows()` |
| 1534 | `getMedicalInventoryRowsByMedicalType()` |
| 1574 | `getMedicalInventoryRowsWithMovement()` |
| 1618 | `addInventoryRow()` |

The `getLotByMedical` method in `MovStockInsertingManager.java:283` already supports a `removeEmpty` parameter that filters out lots with `mainStoreQuantity <= 0` when set to `true`.

### Fix

Simply change `false` → `true` in these 4 calls:

**File:** `openhospital-gui/src/main/java/org/isf/medicalinventory/gui/InventoryEdit.java`

```
Line 1503: movStockInsertingManager.getLotByMedical(med, false);  →  getLotByMedical(med, true);
Line 1534: movStockInsertingManager.getLotByMedical(med, false);  →  getLotByMedical(med, true);
Line 1574: movStockInsertingManager.getLotByMedical(med, false);  →  getLotByMedical(med, true);
Line 1618: movStockInsertingManager.getLotByMedical(med, false);  →  getLotByMedical(med, true);
```

### Impact

- When all lots for a medication are empty (qty=0), the system will show a single row with no lot (allowing the user to create a new lot), which is the existing behavior when `lots.isEmpty()`.
- When some lots have stock and others don't, only the non-empty lots will be displayed.
- No changes needed in the CORE module — the filtering logic already exists.

### Risk

Low. The existing `getLotsByMedical` method with `removeEmpty=true` is already used elsewhere (e.g., `getLotByMedical(medical)` without the boolean param defaults to `true`). This is a well-tested code path.

Do you want me to proceed with this implementation?

---


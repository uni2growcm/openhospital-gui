# Suivi de stock des vaccins

## Contexte

Le domaine vaccin (`Vaccine`, `VaccineType`, `PatientVaccine`) est aujourd'hui un pur catalogue + journal d'administration : aucune notion de quantité, de lot ou de date de péremption n'existe. Le client veut désormais suivre le stock de vaccins au dépôt central (quantités disponibles, lots, péremption), avec un décrément automatique à chaque vaccination enregistrée. Recherche exhaustive confirmée : aucun précédent de stock vaccin n'existe dans ce dépôt, ni dans mada, ni dans aucun fork sibling, ni dans l'historique git — fonctionnalité entièrement nouvelle.

Le module pharmacie existant (`org.isf.medicals`/`medicalstock`/`medicalstockward`/`medicalinventory`) est mature et couvre un besoin similaire (lots, mouvements, FEFO), mais après discussion l'utilisateur a tranché en faveur des décisions suivantes (non renégociables pour cette V1) :

1. **Sous-système dédié et indépendant** — ne pas réutiliser `Medical`/`MedicalStock`/`Movement`/`Lot`, pour ne pas complexifier un module pharmacie déjà lourdement utilisé. On s'inspire du *pattern* (mouvements signés, lots, FEFO) sans réutiliser les classes/tables.
2. **Suivi des lots et dates de péremption : essentiel.**
3. **Décrément automatique d'une dose** quand une `PatientVaccine` est enregistrée (création uniquement, pas modification), avec sélection FEFO du lot.
4. **Dépôt central uniquement** pour cette V1 — pas de suivi par service/ward.

## Approche recommandée

Nouveau package core `org.isf.vaccinestock` (`model`, `service`, `manager`), sur le modèle de `org.isf.medicalstock` mais délibérément simplifié.

### 1. Modèle de données

**`VaccineLot`** (`org.isf.vaccinestock.model.VaccineLot`, table `OH_VACCINESTOCKLOT`, préfixe `VSL_`, `extends Auditable<String>`) :
- `code` : `String` `@Id` (`VSL_ID_A`) — numéro de lot, saisi manuellement (comme `Lot.code`).
- `vaccine` : `@ManyToOne Vaccine` `@NotNull` (`VSL_VAC_ID_A`)
- `preparationDate` : `LocalDateTime` `@NotNull` (`VSL_PREP_DATE`)
- `dueDate` : `LocalDateTime` `@NotNull` (`VSL_DUE_DATE`) — sert au FEFO
- `cost` : `BigDecimal` nullable (`VSL_COST`) — optionnel
- `lock` : `int` `@Version` (`VSL_LOCK`)

**Pas de table "type de mouvement"** (contrairement à `MovementType`). Simplification volontaire : la `quantity` du mouvement est un entier **signé** (+ = charge, − = décharge), et le "motif" est une colonne texte contrainte à un enum Java `VaccineStockMovementReason` (`SUPPLY`, `MANUAL_DISCHARGE`, `CORRECTION`, `ADMINISTRATION`, `ADMINISTRATION_CANCELLED`) plutôt qu'une table de référence — évite un écran CRUD et une table supplémentaires pour un besoin qui reste fixe.

**`VaccineStockMovement`** (table `OH_VACCINESTOCKMOVEMENT`, préfixe `VSM_`, `extends Auditable<String>`) :
- `code` : `int` `@Id @GeneratedValue(IDENTITY)` (`VSM_ID`)
- `vaccine` : `@ManyToOne Vaccine` `@NotNull` (`VSM_VAC_ID_A`)
- `lot` : `@ManyToOne VaccineLot` `@NotNull` (`VSM_VSL_ID_A`) — toujours obligatoire (contrairement à `Movement.lot`, nullable), car le lot est jugé essentiel ici.
- `quantity` : `int` `@NotNull` (`VSM_QTY`) — signé
- `reason` : `String` `@NotNull` (`VSM_REASON`)
- `date` : `LocalDateTime` `@NotNull` (`VSM_DATE`)
- `patientVaccine` : `@ManyToOne PatientVaccine` nullable (`VSM_PAV_ID`) — renseigné uniquement pour `ADMINISTRATION`/`ADMINISTRATION_CANCELLED`, pour traçabilité et annulation
- `note` : `String` nullable (`VSM_NOTE`) — texte libre (bon de livraison/commentaire), pas d'entité `Supplier` en V1
- `lock` : `int` `@Version` (`VSM_LOCK`)

**Pas de table de solde agrégé** (pas de `VaccineStock` façon `MedicalStock`). Le solde est calculé à la volée par `SUM(VSM_QTY)` (par lot pour le FEFO, par vaccin pour l'affichage et l'alerte). Justification : volume de vaccins très inférieur à la pharmacie générale, et le pattern "solde dénormalisé + UPSERT" est une source de bugs connue côté pharmacie (cf. `step_a106_medicaldsrstock_control.sql`, correctif a posteriori). Index sur `VSM_VAC_ID_A` et `VSM_VSL_ID_A`.

**Seuil d'alerte stock bas** : nouvelle colonne nullable sur `Vaccine` — `minQuantity` (`Integer`, `VAC_MIN_STOCK_QTY INT DEFAULT NULL`). Nullable pour distinguer "pas de seuil" de "seuil = 0". Affichage uniquement (pas de blocage).

**Annulation à la suppression d'une `PatientVaccine`** : mouvement inverse (`+1`, `reason=ADMINISTRATION_CANCELLED`), pas de suppression physique du mouvement d'origine — préserve l'historique du lot. FK `VSM_PAV_ID → OH_PATIENTVACCINE(PAV_ID)` en `ON DELETE SET NULL` (la suppression de `PatientVaccine` est physique, pas soft-delete).

### 2. Comportement si stock insuffisant

**Avertissement non bloquant.** Si aucun lot du vaccin n'a de solde positif, le décrément échoue silencieusement côté stock (log `WARN`, aucun mouvement créé) mais **la vaccination est enregistrée quand même** — la traçabilité clinique ne doit jamais être bloquée par une lacune de stock. Point d'extension noté pour une V2 (ex. réglage `GeneralData` pour rendre ce comportement strict) mais hors scope V1.

### 3. Composants GUI (`org.isf.vaccinestock.gui`)

1. **`VaccineStockBrowser`** — tableau des soldes par vaccin (type, vaccin, solde total, péremption la plus proche, mise en évidence si solde < `minQuantity`), pagination via `GeneralData.PAGESIZE` (même pattern que `PatVacBrowser` déjà en place). Panneau secondaire : liste des mouvements, filtrable par vaccin/date/motif.
2. **`VaccineStockChargeEdit`** — formulaire d'entrée : vaccin, lot (existant ou nouveau avec `preparationDate`/`dueDate`/`cost`), quantité, date, note.
3. **`VaccineStockDischargeEdit`** — formulaire de sortie manuelle (perte, casse, péremption, correction) : vaccin, lot (suggestion FEFO ou choix explicite), quantité, motif (dropdown, valeurs hors `ADMINISTRATION*`), date, note.
4. Pas d'écran séparé "alertes péremption/stock bas" en V1 — simple mise en évidence visuelle dans `VaccineStockBrowser`.
5. Pas de suppression de mouvement dans la GUI — renforce le caractère immuable du ledger.

**Menu** : nouvel item `vaccinestock` à côté de `vaccine`/`vaccinetype`/`patientvaccine` (menu Vaccins), pas sous "pharmacy" — cohérent avec le choix d'un sous-système dédié et indépendant, regroupement par domaine métier plutôt que par fonction technique. Permissions dédiées suivant le pattern `btnpharmstockcharge`/`btnpharmstockdischarge` : `btnvaccinestockcharge`, `btnvaccinestockdischarge`.

### 4. Intégration avec `PatVacEdit`

**`PatVacEdit.getOkButton()` n'est PAS modifié** — il continue d'appeler uniquement `patVacManager.newPatientVaccine(patVac)`/`updatePatientVaccine(patVac)` comme aujourd'hui. L'intégration se fait dans **`PatVacManager`** (couche manager), qui reçoit un nouveau `VaccineStockManager` injecté en constructeur (comme `ioOperations` l'est déjà) :

- `newPatientVaccine(patVac)` : après la persistance réussie, appelle `vaccineStockManager.dispenseDose(persisted)`. Échec (stock insuffisant) → catch + `LOGGER.warn`, pas de propagation (cf. §2).
- `updatePatientVaccine(patVac)` : **inchangé**, aucun appel au stock.
- `deletePatientVaccine(patVac)` : appelle `vaccineStockManager.cancelAdministration(patVac)` **avant** `ioOperations.deletePatientVaccine(patVac)` ; ici l'exception remonte et bloque la suppression en cas d'échec (une incohérence de données réelle, contrairement au cas §2).

Sens des dépendances (pas de cycle) : `org.isf.patvac.manager` → `org.isf.vaccinestock.manager` → `org.isf.vaccinestock.model` → `org.isf.patvac.model`. `PatVacEdit` (GUI) n'appelle jamais directement un composant du package `vaccinestock`.

### 5. Migration SQL

Nouveau fichier `openhospital-core/sql/step_a122_add_vaccine_stock_tracking.sql` (suite de `step_a121_add_village_to_patientvaccine.sql`, la dernière migration de cette branche) :
- `ALTER TABLE OH_VACCINE ADD COLUMN VAC_MIN_STOCK_QTY INT DEFAULT NULL AFTER VAC_VACT_ID_A;`
- `CREATE TABLE OH_VACCINESTOCKLOT (...)` — PK `VSL_ID_A`, FK `VSL_VAC_ID_A → OH_VACCINE(VAC_ID_A)` (`RESTRICT`), colonnes d'audit standard, index sur `VSL_VAC_ID_A`/`VSL_DUE_DATE`.
- `CREATE TABLE OH_VACCINESTOCKMOVEMENT (...)` — PK auto `VSM_ID`, FK `VSM_VAC_ID_A → OH_VACCINE` (`RESTRICT`), FK `VSM_VSL_ID_A → OH_VACCINESTOCKLOT` (`RESTRICT`), FK `VSM_PAV_ID → OH_PATIENTVACCINE(PAV_ID)` (`ON DELETE SET NULL`), index sur `VSM_VAC_ID_A`/`VSM_VSL_ID_A`/`VSM_PAV_ID`.
- Inserts `MENUITEM`/`GROUPMENU` pour `vaccinestock`/`btnvaccinestockcharge`/`btnvaccinestockdischarge`, placés sous le menu Vaccins existant (pas `pharmacy`).
- Ajouter `source step_a122_add_vaccine_stock_tracking.sql;` à la fin de `openhospital-core/sql/step_04_all_following_steps.sql`.

### 6. Découpage en étapes livrables (proposé pour discussion, pas de livraison monolithique)

1. **Modèle + migration + repositories** — `VaccineLot`, `VaccineStockMovement`, colonne `VAC_MIN_STOCK_QTY`, `step_a122`, repositories Spring Data avec requêtes de solde agrégé. Aucun impact sur l'existant.
2. **`VaccineStockManager`** (`newCharge`, `newManualDischarge`, `dispenseDose` FEFO, `cancelAdministration`, calcul de solde/seuil). Testable en isolation, sans GUI ni intégration `PatVacEdit`.
3. **GUI de saisie manuelle** (`VaccineStockBrowser`, `VaccineStockChargeEdit`, `VaccineStockDischargeEdit`) + câblage menu/permissions. Utilisable seule par un administrateur pour peupler des lots réalistes.
4. **Intégration automatique avec `PatVacEdit`/`PatVacManager`** (décrément à la création, annulation à la suppression). Fait en dernier — étape la plus sensible car elle touche un écran déjà retravaillé/testé cette session, minimisant le risque de régression une fois le sous-système de stock stabilisé.
5. *(Optionnel, peut fusionner avec l'étape 3)* Polish UX : mise en évidence péremption/stock bas, traductions `MessageBundle` dans toutes les langues supportées.

### 7. Explicitement hors scope V1

Rapports dédiés, suivi par service/ward, entité `Supplier` structurée, écran d'alertes séparé, réglage de blocage strict sur rupture de stock, import/export CSV, scan de lot, notifications de réapprovisionnement.

## Fichiers critiques

- `openhospital-core/src/main/java/org/isf/vaccinestock/model/VaccineLot.java` (nouveau)
- `openhospital-core/src/main/java/org/isf/vaccinestock/model/VaccineStockMovement.java` (nouveau)
- `openhospital-core/src/main/java/org/isf/vaccinestock/manager/VaccineStockManager.java` (nouveau)
- `openhospital-core/src/main/java/org/isf/patvac/manager/PatVacManager.java` (intégration §4)
- `openhospital-core/src/main/java/org/isf/vaccine/model/Vaccine.java` (ajout `minQuantity`)
- `openhospital-core/src/main/java/org/isf/medicalstock/service/MedicalStockIoOperations.java` (référence pattern FEFO, non modifié)
- `openhospital-core/sql/step_a121_add_village_to_patientvaccine.sql` (modèle pour `step_a122`)
- `openhospital-core/sql/step_a122_add_vaccine_stock_tracking.sql` (nouveau)
- `openhospital-core/sql/step_04_all_following_steps.sql` (enregistrement de la migration)
- `openhospital-gui/src/main/java/org/isf/vaccinestock/gui/VaccineStockBrowser.java` (nouveau)
- `openhospital-gui/src/main/java/org/isf/patvac/gui/PatVacEdit.java` (référence du flux, non modifié directement)

## Vérification

Aucun affichage/DB dans ce bac à sable — vérification par étape :
1. Après chaque étape (1 à 4) : `mvn -q -o clean compile` (core, puis gui après l'étape 3) pour valider la compilation.
2. Étape 1 : tests d'intégration repository (solde agrégé correct par lot/vaccin sur un jeu de mouvements de test).
3. Étape 2 : tests manager — FEFO (le lot le plus proche de la péremption est choisi en premier), non-blocage sur stock insuffisant, mouvement inverse correct sur `cancelAdministration`.
4. Étapes 3-4 : vérification manuelle par l'utilisateur en conditions réelles (créer un lot, l'associer à une vaccination, vérifier le décrément et l'annulation à la suppression), comme pour tous les changements GUI de cette session.

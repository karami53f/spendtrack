# SpendTrack — Personal Finance Tracker

A sleek, intuitive Android personal finance tracking app built with Jetpack Compose and local Room persistence to effortlessly record daily expenses, track payment cards, and monitor spending.

### User Review & Critical Decisions

> [!IMPORTANT]
> Key preferences have been aligned through clarification questions. Review the confirmed decisions and defaults below.

- **Currency Format**: Dollar (`$`) with two decimal digits (`$12.50`), with responsive numeric keypad input.
- **Initial Seed Data**: Pre-loaded starter catalog including common items (*Bread*, *Milk*, *Gas*, *Coffee*, *Lunch*, *Groceries*) and payment cards (*Cash* marked as default, *Bank A Checking*, *Bank B Credit*).
- **Home Screen Analytics**: A dynamic summary dashboard featuring total amount spent, transaction count, and quick spending breakdown by payment card.
- **Local-First Architecture**: 100% private, on-device SQLite storage using Room Database with reactive Kotlin Flows; no cloud accounts or network connectivity required.

---

### 1. Overview & Core Concept

- **What It Does**: SpendTrack empowers users to log purchases in seconds by selecting from pre-configured item lists and payment cards, auto-filling timestamps with optional manual editing, and managing their catalogs of items and payment methods.
- **Target Audience / Persona**: Everyday budget-conscious individuals who want a friction-free, quick way to log cash, debit, and credit card transactions without linking sensitive bank credentials.
- **Key Value**: Super fast transaction logging (under 5 seconds per purchase), clear visibility into card utilization, and guaranteed offline data preservation.

---

### 2. User Experience & Visual Design

#### Key User Flows
1. **Record Purchase**:
   - Tap the prominent `+` Floating Action Button (FAB) on the Home tab to open the "Add Purchase" bottom sheet/dialog.
   - Select an Item from the dropdown or tap a quick chip; the amount field focuses with a clean decimal input.
   - Current date and time are pre-filled; user can tap to edit date/time via Material 3 Date and Time pickers.
   - Payment card defaults to the user-designated default card (e.g. *Cash* or *Bank A*), with a dropdown to select any other card.
   - Tap "Save Purchase": instant ripple animation, database insert, and immediate reactive update on the Home feed.
2. **Browse & Filter Purchases**:
   - Home tab displays a summary hero card at the top (Total Spent, Transaction Count, Top Card).
   - Below is a reverse-chronological list of purchase cards with item icon/pill, date/time formatted nicely, card tag, and amount in bold typography.
   - Swipe or tap to delete with confirmation.
3. **Manage Items**:
   - Second tab ("Items") lists all configured items with purchase counts.
   - Quick "Add Item" button opens an input dialog.
   - Items can be edited or deleted (with protection/alert if purchases reference them).
4. **Manage Cards**:
   - Third tab ("Cards") displays configured payment methods as realistic mini-card widgets.
   - Displays a star/badge for the active "Default Card".
   - One-tap "Set as Default" button, plus Add, Edit, and Delete actions.

#### Visual Identity & Theme
- **Aesthetic Direction**: Modern Fintech & Emerald Minimalist. Clean whites/dark slate surfaces paired with deep emerald green accents for financial trust and clarity.
- **Color Palette**:
  - Primary: Deep Emerald (`#0A6E51`)
  - Primary Container / Accent: Fresh Mint (`#D1F2E6` in light, `#123E32` in dark)
  - Secondary: Slate Blue (`#4A607A`)
  - Background: Crisp Warm White (`#FBFDFB` light) / Dark Charcoal (`#111413` dark)
  - Surface & Card: Pure White (`#FFFFFF`) with subtle border elevation (`#E0E5E2`)
  - Error: Coral Crimson (`#BA1A1A`)
- **Typography & Hierarchy**:
  - Display & Key Amounts: Bold tabular numbers with clear currency symbols.
  - Headers: Material 3 HeadlineSmall / TitleMedium with high legibility.
  - Subtext: LabelMedium with muted slate tone for timestamps and card badges.
- **Component Styling & Layout**:
  - Material 3 NavigationBar with rounded active indicators.
  - Floating Action Button with smooth elevation and icon morphing.
  - Card elements have 16dp rounded corners and subtle hairline borders for modern polish.
  - Edge-to-edge support with strict `WindowInsets.safeDrawing` and navigation bar padding.

---

### 3. Key Product Decisions & Trade-Offs

- **Decision 1: Persistence Mechanism (Room Database)**
  - *Chosen Approach*: Room with SQLite, structured across 3 relational entities (`PurchaseEntity`, `ItemEntity`, `CardEntity`), managed via a unified `SpendTrackRepository`.
  - *Why*: User explicitly requested local Room persistence with restart survival, fast reactive `Flow` emissions, and zero cloud sign-in friction.
  - *Alternatives Considered*: In-memory (fails persistence mandate); Firestore (overkill for strictly private local finance app requested).
- **Decision 2: Purchase Entity Card & Item Referencing**
  - *Chosen Approach*: Store item name and card name as snapshots within `PurchaseEntity` alongside optional entity IDs.
  - *Why*: If a user later renames or deletes an item/card, historical purchase records remain intact with the name they were bought under, avoiding orphaned or corrupted purchase history.
- **Decision 3: Single Default Card Invariant**
  - *Chosen Approach*: Handled in a database transaction in `CardDao`: marking a card as default automatically unmarks any previous default card.
  - *Why*: Ensures there is always exactly one unambiguous default card for the "Add Purchase" flow.
- **Decision 4: Add Purchase Interaction**
  - *Chosen Approach*: Modal Bottom Sheet with smooth slide-in rather than a disjoint screen.
  - *Why*: Keeps the user grounded on the Home screen context, allowing quick multi-logging without jarring navigation jumps.

---

### 4. Technical Architecture & Data Strategy (Technical Reference)

#### Architecture & Component Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                       UI Layer (M3)                         │
│                                                             │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────┐  │
│  │   HomeScreen    │  │   ItemsScreen   │  │ CardsScreen │  │
│  │  - SummaryCard  │  │  - ItemsList    │  │ - CardList  │  │
│  │  - PurchaseList │  │  - AddItemModal │  │ - CardBadge │  │
│  │  - AddSheet     │  │                 │  │             │  │
│  └────────┬────────┘  └────────┬────────┘  └──────┬──────┘  │
│           │                    │                  │         │
│           └────────────────────┼──────────────────┘         │
│                                ▼                            │
│                     MainViewModel / State                   │
│         - purchases: StateFlow<List<Purchase>>              │
│         - items: StateFlow<List<Item>>                      │
│         - cards: StateFlow<List<Card>>                      │
│         - defaultCard: StateFlow<Card?>                     │
└───────────────────────────────┬─────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────┐
│                 SpendTrackRepository Layer                  │
└───────────────────────────────┬─────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────┐
│                    Room Database Layer                      │
│  ┌───────────────┐   ┌────────────────┐   ┌──────────────┐  │
│  │  PurchaseDao  │   │    ItemDao     │   │   CardDao    │  │
│  └───────┬───────┘   └────────┬───────┘   └───────┬──────┘  │
│          ▼                    ▼                   ▼         │
│  ┌───────────────────────────────────────────────────────┐  │
│  │             SQLite AppDatabase (Room)                 │  │
│  │  - purchases (id, itemName, amount, timestamp, card)  │  │
│  │  - items (id, name, isDefault)                        │  │
│  │  - cards (id, name, isDefault, colorHex)              │  │
│  └───────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

#### Data Model Entities
1. `ItemEntity(id: Long = 0, name: String)`
2. `CardEntity(id: Long = 0, name: String, isDefault: Boolean = false, colorHex: Long)`
3. `PurchaseEntity(id: Long = 0, itemName: String, amount: Double, timestamp: Long, cardName: String)`

#### State Mapping & Flow
- `MainViewModel` exposes reactive `StateFlow` streams directly from Room DAOs.
- Insertion/update/deletion actions dispatch via coroutines on `Dispatchers.IO`.
- UI observes state via `collectAsStateWithLifecycle()` in Compose.
- First-launch database callback populates initial items and cards automatically.

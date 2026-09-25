# BazarFX

A buy/sell desktop marketplace built with Java, JavaFX, and FXML — now covering the full
updated lab syllabus: **Java & OOP**, **Git & version control**, **JavaFX desktop GUI**,
**multithreading & concurrency**, **relational databases with SQLite**, and **JSON/API
handling**. Users and product listings still live in local JSON files under `/data`, but
**orders now live in a real SQLite database** and the app makes a **live JSON REST API call**
to show product prices converted to USD.

---

## 1. Opening the project in IntelliJ IDEA

1. **File → Open** → select the `bazarfx` folder (the one containing `pom.xml`).
2. IntelliJ detects it as a Maven project and downloads `javafx-controls`, `javafx-fxml`,
   `gson`, and `sqlite-jdbc` automatically the first time you build (needs internet once).
3. Make sure **Project SDK** is Java 17 or newer: `File → Project Structure → Project → SDK`.
4. Run it either:
   - Right-click `MainApp.java` → **Run 'MainApp.main()'**, or
   - Terminal: `mvn javafx:run`

If IntelliJ complains about "JavaFX runtime components are missing" when running `MainApp`
directly, use the included `Launcher.java` (`Run 'Launcher.main()'`) instead — it's a plain
class that just calls `MainApp.main()`, which sidesteps that check when running straight off
a classpath.

---

## 2. Designing screens in Scene Builder

Every screen is a separate `.fxml` file under `src/main/resources/com/bazarfx/view/`:

| FXML file            | Screen                              | Controller                     |
|-----------------------|--------------------------------------|---------------------------------|
| `login.fxml`          | Log in                              | `LoginController`              |
| `signup.fxml`         | Create account                      | `SignupController`             |
| `main.fxml`           | Nav bar + content shell             | `MainController`                |
| `browse.fxml`         | Browse / search listings            | `BrowseController`             |
| `sell.fxml`           | Create a listing                    | `SellController`               |
| `product_detail.fxml` | View one listing (BDT + live USD)   | `ProductDetailController`      |
| `cart.fxml`           | Cart                                 | `CartController`               |
| `checkout.fxml`       | Simulated checkout + live status    | `CheckoutController`           |
| `my_orders.fxml`      | Buyer's own orders (SQLite-backed)  | `MyOrdersController`           |
| `dashboard.fxml`      | Seller listings + report generation | `DashboardController`          |

**To edit a screen visually:**
1. Install Scene Builder: <https://gluonhq.com/products/scene-builder/>
2. In IntelliJ, install the **JavaFX** plugin (or the "Scene Builder" integration plugin), then set
   Scene Builder's path under `Settings → Languages & Frameworks → JavaFX`.
3. Right-click any `.fxml` file → **Open in SceneBuilder**.
4. Drag controls from the Library panel; when you add a new control that the controller needs to
   reference, set its **fx:id** in the Code panel to match the `@FXML` field name in the matching
   controller class (e.g. `usernameField`, `productTable`). Wire buttons via the **On Action**
   property in the Code panel, typed exactly as the `@FXML`-annotated method name (e.g. `#onLogin`).
5. Save — Scene Builder writes straight back to the `.fxml` file, no export step needed.
6. Styling is centralized in `src/main/resources/com/bazarfx/style.css`; Scene Builder's Style
   Classes field is how you attach a class (e.g. `primary-button`) to any node.

The project already includes working FXML + controllers for every screen, so you can open any of
them in Scene Builder purely to restyle, rearrange, or extend — you don't have to build from scratch.

---

## 3. Where each syllabus week lives

| Week | Topic                                              | Where it lives in this project |
|------|-----------------------------------------------------|---------------------------------|
| 1 | C and Java Compilation; Introduction to Java Syntax | The whole `src/main/java` tree: plain classes, variables, control flow, methods, OOP syntax (see especially `model/` and `service/`). |
| 2 | Introduction to Git and Version Control | The project is a real git repository (`git init` already run) with an incremental commit history — see [section 4](#4-git--version-control-in-this-project) below for the exact log and the branching workflow to keep using. |
| 3 | Desktop GUI Development with JavaFX | Every file under `view/*.fxml` plus every class in `controller/`: stages/scenes (`SceneManager`), controls, event handling (`@FXML` `onAction` methods), layouts (`BorderPane`, `HBox`, `VBox`, `TableView`). |
| 4 | Java Multithreading and Concurrency | All of `concurrency/`: `ImageProcessor` (`ExecutorService` thread pool), `OrderStatusSimulator` (`ScheduledExecutorService`), `ReportGenerator` (`Callable`/`Future`), `NotificationService` (daemon thread + `Platform.runLater`). The async `ExchangeRateClient` (Week 7) is another concurrency example - it never blocks the UI thread on network I/O. |
| 5 | Quiz 1 | Weeks 1-4 content above is what it covers - no code changes needed here, just review. |
| 6 | Relational Database with SQLite and JavaFX | New `db/` package: `DatabaseManager` (connection + table creation) and `OrderDao` (INSERT/UPDATE/DELETE/SELECT). `OrderService` now persists every order through SQLite instead of JSON, and the new **My Orders** screen (`my_orders.fxml` / `MyOrdersController`) queries and deletes orders live. Full details in [section 5](#5-week-6---sqlite-relational-database). |
| 7 | JSON Parsing and API Response Handling with Java | New `api/` package: `ExchangeRateClient` calls a live JSON REST API and `ExchangeRateResponse` is the Java object Gson parses the response into. Wired into the product detail screen as a live BDT → USD price line. Full details in [section 6](#6-week-7---json-parsing--api-integration). |

---

## 4. Git & version control in this project

This copy of the project was handed back to you as an actual git repository (`bazarfx/.git`
is included), with each syllabus feature added as its own commit so the history itself is a
worked example of the "small, focused commits" practice Week 2 asks for:

```
f1312f4 Week 7: Add JSON/REST API integration - live BDT to USD conversion via ExchangeRateClient (Gson-parsed JSON, async HttpClient)
6f6d100 Week 6: Add 'My Orders' screen backed by live SQLite queries with cancel (DELETE) support
9b94b7c Week 6: Add SQLite relational database layer for Orders (DatabaseManager, OrderDao) and migrate OrderService to JDBC insert/update/delete/query
dfea93e Initial commit: BazarFX project skeleton (JSON-based marketplace app)
```

Run `git log --oneline` yourself to see it, or `git log -p <hash>` to see exactly what each
commit changed line by line - a good way to study what was added for Weeks 6 and 7 specifically.

**Pushing this to GitHub** (from inside the `bazarfx` folder):

```bash
git remote add origin https://github.com/<your-username>/bazarfx.git
git branch -M main
git push -u origin main
```

**Recommended branching workflow going forward**, e.g. for your own JavaFX assignment feature:

```bash
git checkout -b feature/my-new-feature
# ...make changes...
git add .
git commit -m "Add my new feature"
git push -u origin feature/my-new-feature
# open a Pull Request into main, then merge
```

A `.gitignore` is included so `target/`, `data/` (your local runtime JSON + SQLite database +
uploaded images), and IDE files are never committed — that data is generated fresh every time
the app runs.

---

## 5. Week 6 - SQLite relational database

**Why orders specifically?** Orders have a natural relational lifecycle: they're **inserted**
at checkout, their status gets **updated** repeatedly by the background order-status
simulator, they're **queried** by buyer for the My Orders screen and by the dashboard/report
screens, and they can be **deleted** on cancellation. That covers all four operations the
syllabus asks you to practice, in one coherent feature, without disturbing how Users/Products/
Reviews already work on JSON.

- `com.bazarfx.db.DatabaseManager` — opens `data/bazarfx.db` (created automatically on first
  run, no setup needed) and runs `CREATE TABLE IF NOT EXISTS orders (...)`.
- `com.bazarfx.db.OrderDao` — one method per SQL operation: `insert`, `update`, `delete`,
  `findAll`, `findByBuyer`, `findById`, all using `PreparedStatement` (never string-concatenated
  SQL, to avoid injection).
- `com.bazarfx.service.OrderService` — the same public API as before (`placeOrder`, `getById`,
  `getByBuyer`, `getAllSnapshot`, `persist`) plus a new `cancelOrder(id)`, but every method now
  goes through `OrderDao`/JDBC instead of `JsonStorage`. A `ConcurrentHashMap` cache is kept so
  reads stay instant for the UI and background threads.
- **My Orders** screen (`my_orders.fxml` + `MyOrdersController`) — new nav-bar entry that shows
  the signed-in buyer's orders straight from a live `SELECT ... WHERE buyer_username = ?`, with
  a **Cancel** button per row that issues a real `DELETE` (disabled once an order is
  `DELIVERED`). It re-queries every 2 seconds so status changes made by
  `OrderStatusSimulator` (Week 4) show up on their own.

To inspect the database directly, open `data/bazarfx.db` with any SQLite browser (e.g. "DB
Browser for SQLite") or `sqlite3 data/bazarfx.db "SELECT * FROM orders;"` from a terminal.

---

## 6. Week 7 - JSON parsing & API integration

- `com.bazarfx.api.ExchangeRateResponse` — a plain Java object whose field names match the JSON
  keys returned by the free, keyless **open.er-api.com** exchange-rate API exactly, so Gson can
  deserialize the whole HTTP response straight into it with no manual string parsing.
- `com.bazarfx.api.ExchangeRateClient` — calls `https://open.er-api.com/v6/latest/BDT` with
  `java.net.http.HttpClient`'s **async** API (so a slow/unreachable network never freezes the
  UI), reads the JSON response body, and parses it with `Gson.fromJson(...)`. Results are cached
  for 10 minutes so opening several product pages doesn't spam the API.
- **Product Detail screen** — under each listing's `BDT` price you'll now see a live
  `≈ $X.XX USD` line once the API call resolves ("USD price unavailable right now" if there's
  no internet connection, so the app never crashes on a network failure).

If you want a second, more classroom-friendly example of manual JSON parsing (as opposed to
Gson doing it for you), look at how `ExchangeRateResponse` mirrors the API's JSON shape field by
field — that mapping *is* the "conversion of structured data into Java objects" the syllabus
describes, just done declaratively instead of with `JSONObject.getString(...)` calls.

---

## 7. Running it

```bash
mvn javafx:run
```

First run creates a `data/` folder with `users.json`, `products.json`, `reviews.json`, and a new
`bazarfx.db` SQLite database (orders). Sign up two different accounts (e.g. one browser session
as a seller, log out, sign up as a buyer) to try the full sell → browse → cart → checkout →
My Orders → review flow. An internet connection is needed the first time you build (Maven fetches
`sqlite-jdbc`) and, optionally, whenever you open a product detail page (for the live USD price -
the rest of the app works fine offline).

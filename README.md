# BazarFX

**BazarFX** is a desktop buy/sell marketplace built with **Java 17+, JavaFX 21 and FXML**.
Users create accounts, list items for sale with photos, browse and filter listings, save
favourites to a wishlist, chat with sellers, add items to a cart, check out, track order
status in real time, and leave star ratings for sellers.

Data is stored in two ways side by side: **users, products and wishlists** live in local JSON
files, while **orders, reviews and chat messages** live in a local **SQLite** database. Product
prices are also shown in USD using a **live JSON REST API** call.

---

## Table of contents

1. [Features](#1-features)
2. [Tech stack](#2-tech-stack)
3. [Topics covered](#3-topics-covered)
4. [Project structure](#4-project-structure)
5. [Data storage](#5-data-storage)
6. [Concurrency in BazarFX](#6-concurrency-in-bazarfx)
7. [JSON & REST API integration](#7-json--rest-api-integration)
8. [Getting started](#8-getting-started)
9. [Editing screens in Scene Builder](#9-editing-screens-in-scene-builder)
10. [Demo data](#10-demo-data)
11. [Git & version control](#11-git--version-control)
12. [Known limitations](#12-known-limitations)

---

## 1. Features

**Accounts**
- Sign up / log in with username, email, phone, location and password.
- Passwords are hashed (SHA-256) before being stored; plain text is never written to disk.
- A session holder keeps track of the currently logged-in user.

**Selling**
- Create a listing: title, description, category (Electronics, Vehicles, Furniture, Fashion,
  Books, Other), condition (New / Used), price (BDT) and location.
- Upload multiple photos (`.png`, `.jpg`, `.jpeg`); images are copied on a background thread
  with a progress indicator so the screen never freezes.
- **Seller dashboard**: report-style cards showing your listings and total views, order-status
  distribution, your average seller rating, and marketplace-wide insights via a
  **Generate Report** button (total revenue, top category, most active seller, total orders).

**Browsing & discovery**
- Wrapping **card grid** of listings with photo (or category placeholder), price, title and meta.
- Search by keyword and category, filter by **price range** and **minimum seller rating**.
- Sort by price (low→high / high→low), newest first, most viewed, or top-rated sellers.
- **Product detail** page: photo gallery with clickable thumbnails, view counter, seller rating,
  price in BDT plus a live **≈ USD** conversion.

**Wishlist**
- Heart toggle (♡ / ♥) on every Browse card and on the detail page.
- Dedicated **Wishlist** screen showing only your saved listings.

**In-app chat**
- **Message Seller** button on a listing opens a chat scoped to that product; general
  conversations work too. Sellers can't message themselves.
- Conversation list with last message and unread counts; the open chat refreshes automatically.
- Sidebar **unread badge** stays current no matter which screen you're on.

**Cart, checkout & orders**
- Session cart, simulated checkout, and a live status list that moves each order through
  `PENDING → CONFIRMED → SHIPPED → DELIVERED` automatically.
- **My Orders** screen backed by real SQLite queries: **Cancel** (deletes the order) until it is
  delivered, then **Review** (1–5 stars + comment), then a disabled **Reviewed** state.
- Live in-app **notification feed** (e.g. "Order 3f2a… is now SHIPPED").

**Interface**
- Sidebar navigation (Browse, Sell, Cart, My Orders, Wishlist, Messages, Dashboard).
- Light / dark **theme toggle**, gradient styling, and a sidebar and notification panel that
  resize with the window. All styling lives in one stylesheet, `style.css`.

---

## 2. Tech stack

| Area | Technology |
|---|---|
| Language | Java 17+ |
| UI | JavaFX 21.0.2 (`javafx-controls`, `javafx-fxml`), FXML, Scene Builder, CSS |
| Build | Maven (`javafx-maven-plugin` 0.0.8) |
| Relational DB | SQLite via `sqlite-jdbc` 3.45.1.0 (JDBC, `PreparedStatement`) |
| JSON | Gson 2.10.1 |
| HTTP | `java.net.http.HttpClient` (async) |
| Concurrency | `java.util.concurrent` (executors, `Callable`/`Future`, scheduled tasks), `Platform.runLater` |

---

## 3. Topics covered

| Topic | Where it lives |
|---|---|
| **Java syntax & OOP** | The whole `src/main/java` tree. Encapsulation and POJOs in `model/`; an enum (`OrderStatus`); an **interface** (`Notifiable`) implemented by `NotificationService`; an **abstract class** (`BackgroundTaskRunner`) extended by `ReportGenerator`; **singletons** (`AppContext`, `NotificationService`); method overloading (`ProductService.search(...)`); generics (`JsonStorage.loadList`). |
| **Git & version control** | The project is a git repository with an incremental commit history. See [section 11](#11-git--version-control). |
| **Desktop GUI with JavaFX** | Every `view/*.fxml` file and every class in `controller/`. Stages and scenes (`SceneManager`), layouts (`BorderPane`, `HBox`, `VBox`, `TableView`), controls, `@FXML` event handlers, dialogs, and CSS theming. |
| **Multithreading & concurrency** | The whole `concurrency/` package plus the async `ExchangeRateClient`. See [section 6](#6-concurrency-in-bazarfx). |
| **Relational databases with SQLite** | The `db/` package and the services that use it. Full CRUD, a foreign key with cascade delete, and JDBC with `PreparedStatement`. See [section 5](#5-data-storage). |
| **JSON parsing & API handling** | `api/` (live REST call parsed with Gson) and `util/JsonStorage` (JSON file persistence). See [section 7](#7-json--rest-api-integration). |

---

## 4. Project structure

```
BazarFX/
├── pom.xml
├── README.md
├── .gitignore                  # target/, data/, .idea/, *.iml, out/
└── src/main
    ├── java/com/bazarfx
    │   ├── MainApp.java        # JavaFX entry point
    │   ├── Launcher.java       # plain-class launcher (avoids "JavaFX runtime missing")
    │   ├── AppContext.java     # singleton holding the shared services
    │   ├── model/              # User, Product, Order, OrderStatus, CartItem,
    │   │                       # Review, Message, WishlistItem
    │   ├── service/            # AuthService, ProductService, OrderService,
    │   │                       # ReviewService, MessageService, WishlistService
    │   ├── db/                 # DatabaseManager, OrderDao, ReviewDao, MessageDao
    │   ├── api/                # ExchangeRateClient, ExchangeRateResponse
    │   ├── concurrency/        # OrderStatusSimulator, ReportGenerator, ImageProcessor,
    │   │                       # NotificationService, ConversationPoller,
    │   │                       # UnreadMessagePoller, BackgroundTaskRunner, Notifiable
    │   ├── controller/         # one controller per screen
    │   ├── util/               # JsonStorage, SceneManager, SessionManager, PasswordUtil
    │   └── seed/               # DemoDataSeeder
    └── resources/com/bazarfx
        ├── style.css
        └── view/               # login, signup, main, browse, sell, product_detail,
                                # cart, checkout, my_orders, dashboard, wishlist, messages (.fxml)
```

**Layering:** `controller` → `service` → (`db` / `util.JsonStorage` / `api`). Controllers never
touch storage directly; every screen shares the same service instances through `AppContext`.

---

## 5. Data storage

| Data | Storage | Why |
|---|---|---|
| Users | `data/users.json` | Simple document-style data, loaded once and cached |
| Products | `data/products.json` | Same |
| Wishlist | `data/wishlist.json` | Same |
| Orders | SQLite `orders` table | Full lifecycle: insert, update, query, delete |
| Reviews | SQLite `reviews` table | Child of `orders` through a foreign key |
| Messages | SQLite `messages` table | High-frequency reads/writes, polled by background tasks |
| Photos | `data/images/{productId}/` | Copied on upload |

Everything under `data/` is created automatically on first run and is git-ignored.

### SQLite schema (`DatabaseManager`)

- **`orders`** — `id` (PK), `buyer_username`, `product_id`, `product_title`, `quantity`,
  `total_price`, `status`, `created_at`
- **`reviews`** — `id` (PK), `order_id` (**FK → `orders(id)` `ON DELETE CASCADE`**),
  `seller_username`, `buyer_username`, `rating`, `comment`, `created_at`
- **`messages`** — `id` (PK), `sender_username`, `receiver_username`, `product_id`,
  `product_title`, `content`, `created_at`, `is_read`

Notes:
- `PRAGMA foreign_keys = ON` is issued on every connection (SQLite has it off by default).
- The schema is created with `CREATE TABLE IF NOT EXISTS`, so no manual setup is needed.
- `messages.product_id` is deliberately not a foreign key, because products live in JSON.
- All SQL goes through `PreparedStatement` (never string concatenation) to prevent SQL injection.

### DAOs and services

- `OrderDao`, `ReviewDao`, `MessageDao` — one method per SQL operation (`insert`, `update`,
  `delete`, `findAll`, `findBy…`).
- `OrderService` keeps a `ConcurrentHashMap` cache in front of `OrderDao`, so the UI and
  background threads read fast and share the same `Order` instances.
- `JsonStorage` is a generic, synchronized `List<T>` reader/writer built on Gson.

To inspect the database: open `data/bazarfx.db` in "DB Browser for SQLite", or run
`sqlite3 data/bazarfx.db "SELECT * FROM orders;"`.

---

## 6. Concurrency in BazarFX

The JavaFX Application Thread must never block, and only it may touch UI nodes. Every
background task below hands its result back with `Platform.runLater(...)`.

| Class | Mechanism | What it does |
|---|---|---|
| `OrderStatusSimulator` | `ScheduledExecutorService` | Advances an order through CONFIRMED → SHIPPED → DELIVERED at 4-second steps and posts a notification each time |
| `ReportGenerator` | `ExecutorService` + `Callable` / `Future` | Runs revenue, top-category and top-seller aggregations in parallel, then combines the `Future`s on a separate thread |
| `ImageProcessor` | Fixed thread pool (3) + `AtomicInteger` | Copies uploaded photos in parallel and fires a callback once all are done |
| `NotificationService` | Daemon `ScheduledExecutorService` + `ObservableList` | App-wide notification feed, safe to push to from any thread |
| `ConversationPoller` | `ScheduledExecutorService` | Re-queries the currently open chat every 3 seconds |
| `UnreadMessagePoller` | `ScheduledExecutorService` | Always-on background task keeping the sidebar unread badge current |
| `ExchangeRateClient` | `HttpClient.sendAsync` | Network call that never blocks the UI thread |

**Thread-safety details:** `Order.status` is `volatile` with a `synchronized` setter, because
the simulator thread writes it while the UI thread reads it. `JsonStorage` and the services'
`persist()` methods are `synchronized`. Worker threads are daemon threads, and
`AppContext.shutdown()` stops every pool on exit.

**Design patterns:** `BackgroundTaskRunner` (abstract) owns pool creation and shutdown;
`Notifiable` (interface) lets the notification feed be swapped for another sink.

---

## 7. JSON & REST API integration

- **`ExchangeRateClient`** calls `https://open.er-api.com/v6/latest/BDT` (free, no API key)
  using `HttpClient`'s asynchronous API.
- **`ExchangeRateResponse`** is a plain Java class whose field names mirror the API's JSON
  keys, so `Gson.fromJson(...)` turns the whole response body into an object with no manual
  string parsing.
- Results are **cached for 10 minutes** so opening several product pages doesn't repeat the
  request.
- On any failure (no internet, bad JSON, API error) the client reports `0.0` and the product page
  shows "USD price unavailable right now" — the app never crashes on a network problem.

Persistence for users, products and the wishlist is the same idea in reverse: `JsonStorage`
serializes Java objects to pretty-printed JSON files with Gson and parses them back on startup.

---

## 8. Getting started

**Requirements:** JDK 17 or newer, Maven, internet access on first build (to download
dependencies).

```bash
mvn javafx:run
```

**IntelliJ IDEA**
1. **File → Open** → select the `BazarFX` folder (the one containing `pom.xml`).
2. Let Maven import the dependencies.
3. Confirm **Project SDK** is 17+ (`File → Project Structure → Project → SDK`).
4. Run `Launcher.main()` (recommended) or `MainApp.main()`.

If running `MainApp` directly shows "JavaFX runtime components are missing", use `Launcher`
instead — it is a plain class that just calls `MainApp.main()`.

The app works offline; only the live USD price needs a connection.

---

## 9. Editing screens in Scene Builder

Each screen is an `.fxml` file in `src/main/resources/com/bazarfx/view/` paired with a
controller:

| Screen | FXML | Controller |
|---|---|---|
| Log in | `login.fxml` | `LoginController` |
| Sign up | `signup.fxml` | `SignupController` |
| Shell (sidebar + content) | `main.fxml` | `MainController` |
| Browse / search | `browse.fxml` | `BrowseController` |
| Create listing | `sell.fxml` | `SellController` |
| Listing detail | `product_detail.fxml` | `ProductDetailController` |
| Cart | `cart.fxml` | `CartController` |
| Checkout | `checkout.fxml` | `CheckoutController` |
| My Orders | `my_orders.fxml` | `MyOrdersController` |
| Seller dashboard | `dashboard.fxml` | `DashboardController` |
| Wishlist | `wishlist.fxml` | `WishlistController` |
| Messages | `messages.fxml` | `MessagesController` |

1. Install [Scene Builder](https://gluonhq.com/products/scene-builder/) and set its path in
   IntelliJ under `Settings → Languages & Frameworks → JavaFX`.
2. Right-click an `.fxml` file → **Open in SceneBuilder**.
3. For any control the controller needs, set its **fx:id** to match the `@FXML` field name
   (e.g. `usernameField`). Wire buttons through **On Action**, typed as the handler method
   name (e.g. `#onLogin`).
4. Attach styles with the **Style Class** field (e.g. `primary-button`); classes are defined in
   `style.css`.
5. Save — Scene Builder writes straight back to the file.

---

## 10. Demo data

On first run, `DemoDataSeeder` populates an empty database so every screen has something to show:

| Account | Password | Role |
|---|---|---|
| `demo_seller` | `demo1234` | Owns most demo listings |
| `demo_buyer` | `demo1234` | Has two delivered orders, reviews, a chat, and a wishlist item |

It also creates 8 listings across all categories, two delivered orders with linked reviews, a
short demo chat, and one wishlist entry. To reset, delete the `data/` folder.

---

## 11. Git & version control

The project is a real git repository with an incremental history (`git log --oneline`).
Recommended workflow for new work:

```bash
git checkout -b feature/my-new-feature
git add .
git commit -m "Add my new feature"
git push -u origin feature/my-new-feature
# open a Pull Request into main, then merge
```

`.gitignore` excludes `target/`, `data/` (runtime JSON, SQLite database, uploaded images),
`.idea/`, `*.iml` and `out/`, since all of it is regenerated on run.

---

## 12. Known limitations

- **Password hashing** uses unsalted SHA-256, which is fine for a coursework demo but not for
  production; a real system should use bcrypt, scrypt or Argon2.
- **Checkout is simulated** — there is no real payment, and order status progression is a timer.
- **The cart is session-only** and is cleared when the app closes.
- **Users, products and wishlists are JSON files**, which don't scale or handle concurrent
  multi-user access; a production version would move them into the database as well.
- **Single machine only** — chat and orders are local to one SQLite file, so this isn't a
  networked marketplace.

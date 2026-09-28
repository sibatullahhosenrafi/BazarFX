# BazarFX

A desktop buy/sell marketplace built with Java and JavaFX. Users list products, browse and
search listings, add items to a cart, check out, message sellers, save items to a wishlist,
and track order status — all through an FXML-driven desktop GUI.

Users and product listings are stored in local JSON files under `/data`. Orders, reviews, and
chat messages are stored in a local SQLite database (`data/bazarfx.db`). Product prices also
get a live BDT → USD conversion via a public exchange-rate REST API.

---

## Running it

```bash
mvn javafx:run
```

Requires Java 17+. First run creates `data/` with `users.json`, `products.json`, `reviews.json`,
`wishlist.json`, and `bazarfx.db`. An internet connection is needed the first time you build
(Maven fetches dependencies) and optionally whenever you view a product page (for the live USD
price — the rest of the app works offline).

Sign up two accounts to try the full flow: list an item as a seller, then browse -> cart ->
checkout -> My Orders -> review as a buyer.

---

## Opening in IntelliJ IDEA

1. **File -> Open** -> select the `BazarFX` folder (contains `pom.xml`).
2. IntelliJ detects it as Maven and downloads `javafx-controls`, `javafx-fxml`, `gson`, and
   `sqlite-jdbc` on first build.
3. Set **Project SDK** to 17+: `File -> Project Structure -> Project -> SDK`.
4. Run via `Launcher.main()` (avoids the "JavaFX runtime components are missing" error you can
   get running `MainApp` directly off a classpath) or `mvn javafx:run`.

## Editing screens in Scene Builder

Each screen is a separate `.fxml` file under `src/main/resources/com/bazarfx/view/`, paired
with a controller class in `controller/`:

| Screen | FXML | Controller |
|---|---|---|
| Log in / Sign up | `login.fxml`, `signup.fxml` | `LoginController`, `SignupController` |
| Nav shell | `main.fxml` | `MainController` |
| Browse / search | `browse.fxml` | `BrowseController` |
| Create listing | `sell.fxml` | `SellController` |
| Listing detail | `product_detail.fxml` | `ProductDetailController` |
| Cart / Checkout | `cart.fxml`, `checkout.fxml` | `CartController`, `CheckoutController` |
| My Orders | `my_orders.fxml` | `MyOrdersController` |
| Seller dashboard | `dashboard.fxml` | `DashboardController` |
| Wishlist | `wishlist.fxml` | `WishlistController` |
| Messages | `messages.fxml` | `MessagesController` |

To restyle or rearrange a screen: right-click the `.fxml` -> **Open in SceneBuilder**, drag
controls in, set each new control's **fx:id** to match the `@FXML` field the controller expects,
wire actions via **On Action** (e.g. `#onLogin`), and save — Scene Builder writes straight back
to the file. Shared styling lives in `src/main/resources/com/bazarfx/style.css`.

---

## Architecture

- **`model/`** — plain data classes (`User`, `Product`, `Order`, `Review`, `Message`,
  `WishlistItem`, `CartItem`).
- **`service/`** — business logic sitting between controllers and storage (`AuthService`,
  `ProductService`, `OrderService`, `ReviewService`, `MessageService`, `WishlistService`).
- **`db/`** — SQLite persistence: `DatabaseManager` (connection + schema) and DAOs
  (`OrderDao`, `MessageDao`, `ReviewDao`) using `PreparedStatement` throughout.
- **`api/`** — live JSON REST integration: `ExchangeRateClient` (async HTTP call) and
  `ExchangeRateResponse` (Gson deserialization target).
- **`concurrency/`** — background work off the UI thread: `OrderStatusSimulator`,
  `ConversationPoller`, `UnreadMessagePoller`, `ImageProcessor`, `ReportGenerator`,
  `NotificationService`.
- **`controller/`** — one controller per FXML screen, wiring UI events to services.
- **`util/`** — `JsonStorage` (file-backed persistence for users/products/wishlist),
  `SceneManager`, `SessionManager`, `PasswordUtil`.

### Feature notes

- **SQLite-backed orders**: orders are inserted at checkout, updated repeatedly by the
  background status simulator, queried on My Orders / dashboard, and deleted on cancellation —
  all through `OrderDao`. `data/bazarfx.db` can be inspected with any SQLite browser or
  `sqlite3 data/bazarfx.db "SELECT * FROM orders;"`.
- **Live USD price**: `ExchangeRateClient` calls `open.er-api.com` asynchronously (never blocks
  the UI) and caches results for 10 minutes; product detail shows "USD price unavailable" rather
  than crashing if there's no network.
- **Wishlist**: heart toggle on Browse cards and product detail, backed by `wishlist.json`.
- **In-app chat**: buyer/seller messaging per listing or general, backed by a `messages` table;
  the open conversation polls every 3 seconds, and a separate poller keeps the unread badge
  current app-wide.
- **Search & filters**: Browse supports keyword, category, price range, minimum seller rating,
  and sort order (price, newest, most viewed, top rated).

---

## Git

This is a real git repository (`git log --oneline` to see history). Recommended workflow for
new work:

```bash
git checkout -b feature/my-new-feature
git add .
git commit -m "Add my new feature"
git push -u origin feature/my-new-feature
```

A `.gitignore` excludes `target/`, `data/`, and IDE files — all regenerated on first run.

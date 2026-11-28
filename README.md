# TiketWahana

![TiketWahana visual system](docs/screenshots/visual-system-light.png)

A portfolio-grade amusement-park ticketing desktop app built to show how far **plain Java Swing, Java2D, JDBC and MySQL** can go when the UI is treated as a product rather than a form generator.

It has a persistent operational shell, a light/dark design system, keyboard navigation, a command palette, animated Java2D analytics, a multi-attraction POS, capacity checks, real ticket redemption, shifts, RBAC, PBKDF2 authentication, audit events, CSV reporting, receipts and a ledger that balances down to the individual ticket.

## What this proves about Swing

- A coherent design system can live in `UIManager`, custom painting and a small set of reusable components — no FlatLaf, SwingX, JavaFX or icon pack.
- The dashboard chart, status pills, logo, icons, barcode and receipt preview are Java2D, not screenshots or web content.
- Long-running JDBC work is off the EDT through `SwingWorker`; the window stays responsive while the database works.
- The shell never disposes and recreates the main frame: navigation is a persistent rail plus crossfading content.
- UI state is keyboard-accessible: Ctrl+K palette, F2 new sale, F5 refresh and Ctrl+J theme.

## Feature map

| Area | Included |
|---|---|
| Dashboard | Revenue/ticket KPIs, capacity, busiest attraction, 30-day Java2D sparkline, recent transactions |
| POS | Multi-attraction cart, quantities, discount, payment method, change, capacity gate |
| Tickets | Code validation, one-time redemption, visitor log, status history |
| Attractions | CRUD, price/capacity/duration/status management |
| Reports | Visit-date filtering, correct ticket totals, CSV export |
| Shifts | Opening float, expected cash, counted cash, variance |
| Security | PBKDF2-HMAC-SHA256, forced first-login password change, RBAC, audit trail |
| Design | Light/dark theme, bundled fonts, custom icons, toasts, responsive layout, reduce-motion path |

## Run in under a minute

Requirements: JDK 17+, Maven 3.9+ (or use the included wrapper), Docker if you want a disposable MySQL instance.

```bash
# with an existing MySQL 8 / XAMPP server
./mvnw -B clean verify
./mvnw -q exec:java
```

The default connection is compatible with the original XAMPP setup:

```properties
# db.properties, or ~/.tiketwahana/db.properties
# db.url=jdbc:mysql://localhost:3306/ticket_wahana_db
# db.user=root
# db.password=
```

On first run the app creates the database tables and applies numbered migrations. The seeded account is:

- username: `admin`
- temporary password: `admin12345`

A new password of at least eight characters is required immediately. Passwords are stored as salted PBKDF2 hashes; the application does not authenticate when MySQL is unavailable.

## Disposable MySQL

```bash
docker run --name tiketwahana-mysql \
  -e MYSQL_ROOT_PASSWORD=dev \
  -e MYSQL_DATABASE=ticket_wahana_db \
  -p 3306:3306 -d mysql:8

cat > db.properties <<'EOF'
db.url=jdbc:mysql://localhost:3306/ticket_wahana_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Jakarta
db.user=root
db.password=dev
EOF

./mvnw -q exec:java
```

The committed schema is also available as `database_schema.sql`. Runtime migrations are `src/main/resources/db/migrations/001_initial.sql` and `002_portfolio.sql`; applied versions are tracked in `schema_version`.

## Engineering decisions

- **BigDecimal everywhere:** display strings are never parsed back into money. `Money` owns parsing, scaling, arithmetic and Indonesian formatting.
- **One transaction per sale:** `transaksi`, `transaksi_detail` and one `tiket` row per seat commit together, or all roll back. A returned pooled connection is rolled back and restored to auto-commit, so a failed sale cannot poison the next screen.
- **Ticket date means visit date:** reports filter `tiket.tanggal_kunjungan`; transaction timestamp is used only for revenue-by-sale-time analytics.
- **No fake success:** unfinished backup/restore/system-log controls were removed rather than pretending to work. Database failure renders an error/toast, never invented report rows.
- **No runtime network:** the desktop app uses only its configured MySQL connection. CI only runs Maven verification.

## Verification

```bash
./mvnw -B clean verify
```

The test suite covers money parsing/formatting, salted password hashing and policy, migration splitting, Code 128 checksums, and headless painting at 1440×900 and 1024×768 in both themes. The visual smoke test also writes the light/dark design-system reference images to `docs/screenshots/`.

The opt-in integration smoke uses the same wrapper against MySQL 8:

```bash
./mvnw -B -Dmysql.smoke=true \
  -Dmysql.url='jdbc:mysql://127.0.0.1:13306/ticket_wahana_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC' \
  -Dmysql.password=dev test
```

For a full operational smoke test: start MySQL 8, run the migrations through the app, log in as the seeded admin, change the temporary password, open a shift, sell tickets across two attractions, redeem one ticket, export a report and close the shift. The expected cash variance is calculated from the committed transaction ledger.

## Stack

Java 17 source/target · Swing/Java2D · JDBC · MySQL 8 · Maven · JUnit 5

No web layer. No ORM. No UI framework. That is the point.

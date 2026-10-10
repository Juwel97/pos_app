# Stockroom POS Item Manager

A small inventory-management app: a Java 21 / Spring Boot 4.1.0 REST API with a React UI served by the same Spring Boot application. Local development uses in-memory H2 in PostgreSQL compatibility mode; an optional PostgreSQL profile is included.

## Requirements

- JDK 21
- Maven 3.9+
- Node.js 20.19+ (or 22.12+) and npm

## Database configuration

By default, the application uses an in-memory H2 database with `MODE=PostgreSQL`. Data resets when the application stops. Hibernate creates/updates the `items` table on startup, and the H2 console is available at `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:pos_app`, username: `sa`, blank password).

To use PostgreSQL instead, create an empty database named `pos_app` and activate the `postgres` Spring profile. It connects to `jdbc:postgresql://localhost:5432/pos_app` as `postgres` with password `postgres` by default. Override these values using `DATABASE_URL`, `DATABASE_USERNAME`, and `DATABASE_PASSWORD` environment variables. For example, set `SPRING_PROFILES_ACTIVE=postgres` before starting the app.

## Build and run

From the repository root:

1. Build and copy the React bundle into `src/main/resources/static`:

   ```sh
   cd frontend
   npm install
   npm run build
   cd ..
   ```

2. Start the Spring Boot app:

   ```sh
   mvn spring-boot:run
   ```

3. Open `http://localhost:8080`. For UI development, run `npm run dev` from the `frontend` directory and open `http://localhost:5173`; Vite proxies `/item` requests to Spring Boot on port 8080. For production use, build the frontend first so Spring serves it from its static resources.

Package an executable jar with `mvn package` after building the frontend. The resulting app serves both the UI and REST endpoints from port 8080.

## Item API

All request and response bodies use JSON. An item has `name`, `sku`, `description`, `price`, `quantity`, and `category`. Name and SKU are required, SKU is unique, price must be non-negative, and quantity must be a non-negative integer.

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `POST` | `/item/add` | Add an item |
| `PUT` | `/item/update/{id}` | Replace an item's editable fields |
| `GET` | `/item/search?q={term}` | Search name, SKU, category, and description; omit `q` to list all items |
| `DELETE` | `/item/delete/{id}` | Delete an item |

Validation errors return HTTP 400, missing items HTTP 404, and duplicate SKUs HTTP 409.

## Cart and checkout API

`POST /orders/checkout` records an order in `order_history` and its item snapshots in `order_history_items`, deducts the requested quantities from inventory, and returns the order ID and receipt totals. The request accepts item IDs, quantities, and a discount percentage from 0 to 100; prices and totals are recalculated by the server. Insufficient stock returns HTTP 409 without saving the order or changing inventory.

```json
{
   "discountPercent": 10,
   "items": [
      { "itemId": 1, "quantity": 2 }
   ]
}
```

Example add request:

```json
{
  "name": "Ceramic coffee mug",
  "sku": "MUG-001",
  "description": "Matte ceramic, 350 ml",
  "price": 12.5,
  "quantity": 24,
  "category": "Kitchen"
}
```
package com.bazarfx.service;

import com.bazarfx.db.OrderDao;
import com.bazarfx.model.Order;
import com.bazarfx.model.OrderStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Week 6 - Relational Database with SQLite and JavaFX.
 *
 * Orders now live in a real SQLite table (see {@link OrderDao} /
 * {@link com.bazarfx.db.DatabaseManager}) instead of a JSON file. An
 * in-memory ConcurrentHashMap is still kept as a fast read cache so the UI
 * thread and background threads (OrderStatusSimulator, ReportGenerator) don't
 * hit the database on every single read - but every write goes through JDBC:
 *   - placeOrder   -> INSERT
 *   - persist      -> UPDATE (status changes made by OrderStatusSimulator)
 *   - cancelOrder  -> DELETE
 *   - getByBuyer   -> SELECT ... WHERE buyer_username = ?
 */
public class OrderService {

    private final OrderDao orderDao = new OrderDao();
    private final ConcurrentHashMap<String, Order> orders = new ConcurrentHashMap<>();

    public OrderService() {
        // SELECT * FROM orders - restores whatever was placed in previous runs.
        for (Order o : orderDao.findAll()) {
            orders.put(o.getId(), o);
        }
    }

    public Order placeOrder(String buyerUsername, String productId, String productTitle, int quantity, double totalPrice) {
        Order order = new Order(buyerUsername, productId, productTitle, quantity, totalPrice);
        orders.put(order.getId(), order);
        orderDao.insert(order);
        return order;
    }

    public Order getById(String id) {
        return orders.get(id);
    }

    /**
     * Reads from the database so orders placed in earlier sessions still show
     * up, but returns the already-cached in-memory instance whenever one
     * exists instead of the fresh row just read from SQLite.
     *
     * This matters because OrderStatusSimulator holds a direct reference to
     * the exact Order object created by placeOrder() and mutates it in place
     * every few seconds. If this method replaced that cached instance with a
     * brand-new object built from a SELECT, the simulator would keep updating
     * the orphaned old object while persist() kept re-saving the stale new
     * one - which is exactly what made orders look permanently stuck on
     * PENDING once "My Orders" had been opened once.
     */
    public List<Order> getByBuyer(String username) {
        List<Order> fromDb = orderDao.findByBuyer(username);
        List<Order> result = new ArrayList<>();
        for (Order o : fromDb) {
            Order cached = orders.get(o.getId());
            if (cached != null) {
                result.add(cached);
            } else {
                orders.put(o.getId(), o);
                result.add(o);
            }
        }
        return result;
    }

    public List<Order> getAllSnapshot() {
        return new ArrayList<>(orders.values());
    }

    /**
     * Cancels an order that hasn't been delivered yet - a genuine DELETE
     * against the orders table, triggered from the "My Orders" screen.
     */
    public boolean cancelOrder(String id) {
        Order order = orders.get(id);
        if (order == null || order.getStatus() == OrderStatus.DELIVERED) {
            return false;
        }
        orders.remove(id);
        orderDao.delete(id);
        return true;
    }

    /** Writes the current in-memory status of every cached order back to the database. */
    public synchronized void persist() {
        for (Order o : orders.values()) {
            orderDao.update(o);
        }
    }
}
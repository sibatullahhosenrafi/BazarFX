package com.bazarfx.concurrency;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A single, app-wide notification feed.
 * Any background thread can call push(...) safely; the UI thread only ever
 * sees updates through Platform.runLater, which is the standard pattern
 * for moving work from a background Thread onto the JavaFX Application Thread.
 */
public class NotificationService implements Notifiable {

    private static final NotificationService INSTANCE = new NotificationService();

    private final ObservableList<String> notifications = FXCollections.observableArrayList();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "notification-heartbeat");
        t.setDaemon(true);
        return t;
    });

    private NotificationService() {
        // Demonstrates a long-lived background Thread (via ScheduledExecutorService)
        // that is completely independent of any user action.
        scheduler.scheduleAtFixedRate(
                () -> push("Tip: prices update live - check your Wishlist for drops."),
                30, 45, TimeUnit.SECONDS);
    }

    public static NotificationService getInstance() {
        return INSTANCE;
    }

    public ObservableList<String> getNotifications() {
        return notifications;
    }

    /** Safe to call from ANY thread. */
    public void push(String message) {
        Platform.runLater(() -> {
            notifications.add(0, message);
            if (notifications.size() > 50) {
                notifications.remove(notifications.size() - 1);
            }
        });
    }

    /** Notifiable implementation - the in-app feed is one possible destination for a notification. */
    @Override
    public void notifyUser(String message) {
        push(message);
    }

    public void shutdown() {
        scheduler.shutdownNow();
    }
}

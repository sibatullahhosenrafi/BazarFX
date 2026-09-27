package com.bazarfx.concurrency;

import com.bazarfx.service.MessageService;
import com.bazarfx.util.SessionManager;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class UnreadMessagePoller {

    private final IntegerProperty unreadCount = new SimpleIntegerProperty(0);
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "unread-message-heartbeat");
        t.setDaemon(true);
        return t;
    });

    public UnreadMessagePoller(MessageService messageService) {
        scheduler.scheduleAtFixedRate(() -> {
            if (!SessionManager.isLoggedIn()) return;
            int count = messageService.countUnread(SessionManager.getCurrentUser().getUsername());
            Platform.runLater(() -> unreadCount.set(count));
        }, 2, 4, TimeUnit.SECONDS);
    }

    public IntegerProperty unreadCountProperty() {
        return unreadCount;
    }

    public void shutdown() {
        scheduler.shutdownNow();
    }
}

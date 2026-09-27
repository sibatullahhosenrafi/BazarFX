package com.bazarfx.concurrency;

import com.bazarfx.model.Message;
import com.bazarfx.service.MessageService;
import javafx.application.Platform;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;


public class ConversationPoller {

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "chat-poller");
        t.setDaemon(true);
        return t;
    });

    private ScheduledFuture<?> activeTask;

    /** Starts (or restarts) live polling of one conversation, cancelling any previously open one. */
    public synchronized void poll(MessageService messageService, String me, String otherUsername,
                                  String productId, Consumer<List<Message>> onUpdate) {
        stop();
        activeTask = scheduler.scheduleAtFixedRate(() -> {
            List<Message> messages = messageService.getConversation(me, otherUsername, productId);
            Platform.runLater(() -> onUpdate.accept(messages));
        }, 0, 3, TimeUnit.SECONDS);
    }

    /** Stops polling whichever conversation is currently open, e.g. when the user navigates away. */
    public synchronized void stop() {
        if (activeTask != null) {
            activeTask.cancel(true);
            activeTask = null;
        }
    }

    public void shutdown() {
        stop();
        scheduler.shutdownNow();
    }
}

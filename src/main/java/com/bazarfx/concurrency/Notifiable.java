package com.bazarfx.concurrency;

/**
 * Anything that can receive a user-facing notification message. Kept
 * separate from {@link NotificationService} so any future notification
 * sink (a toast popup, an email alert, a log file...) could implement this
 * same contract without depending on the concrete in-app feed.
 */
public interface Notifiable {

    /** Deliver a message to whatever this implementation shows notifications through. */
    void notifyUser(String message);
}

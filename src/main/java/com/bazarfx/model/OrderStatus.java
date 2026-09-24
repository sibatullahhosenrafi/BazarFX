package com.bazarfx.model;

/** Pipeline a simulated order moves through, advanced automatically in the background. */
public enum OrderStatus {
    PENDING,
    CONFIRMED,
    SHIPPED,
    DELIVERED
}

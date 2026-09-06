package com.bazarfx;

/**
 * Separate launcher class (does not extend Application) so the app can be run
 * with a plain classpath launch, e.g. from an IDE "Run" button, without
 * JavaFX's "runtime components are missing" module-path check tripping.
 */
public class Launcher {
    public static void main(String[] args) {
        MainApp.main(args);
    }
}

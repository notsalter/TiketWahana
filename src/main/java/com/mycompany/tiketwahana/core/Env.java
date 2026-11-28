package com.mycompany.tiketwahana.core;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.prefs.Preferences;

/**
 * Configuration resolution order: -Dtiket.config, ./db.properties, ~/.tiketwahana/db.properties,
 * then the bundled defaults. The bundled defaults are the XAMPP setup the README documents.
 */
public final class Env {

    private static final Properties DEFAULTS = new Properties();
    private static Properties resolved;

    static {
        DEFAULTS.setProperty("db.url", "jdbc:mysql://localhost:3306/ticket_wahana_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Jakarta");
        DEFAULTS.setProperty("db.user", "root");
        DEFAULTS.setProperty("db.password", "");
        DEFAULTS.setProperty("db.pool.size", "6");
        DEFAULTS.setProperty("ui.theme", "light");
        DEFAULTS.setProperty("ui.motion", "full");
    }

    private Env() {}

    public static synchronized Properties properties() {
        if (resolved != null) {
            return resolved;
        }
        Properties p = new Properties();
        p.putAll(DEFAULTS);
        for (Path candidate : candidates()) {
            if (Files.isReadable(candidate)) {
                try (InputStream in = Files.newInputStream(candidate)) {
                    p.load(in);
                } catch (IOException ignored) {
                    // a broken user config must not stop the demo from running
                }
                break;
            }
        }
        Preferences prefs = Preferences.userRoot().node("tiketwahana");
        for (String key : new String[] {"ui.theme", "ui.motion"}) {
            String value = prefs.get(key, null);
            if (value != null && !value.isBlank()) {
                p.setProperty(key, value);
            }
        }
        resolved = p;
        return p;
    }

    private static Path[] candidates() {
        String explicit = System.getProperty("tiket.config");
        Path home = Paths.get(System.getProperty("user.home", "."), ".tiketwahana", "db.properties");
        Path cwd = Paths.get("db.properties");
        if (explicit != null && !explicit.isBlank()) {
            return new Path[] {Paths.get(explicit), cwd, home};
        }
        return new Path[] {cwd, home};
    }

    public static String get(String key) {
        return properties().getProperty(key, "");
    }

    public static int getInt(String key, int fallback) {
        try {
            return Integer.parseInt(get(key).trim());
        } catch (RuntimeException e) {
            return fallback;
        }
    }

    /** Persist a UI preference without rewriting the database config file. */
    public static synchronized void set(String key, String value) {
        properties().setProperty(key, value);
        if (key.startsWith("ui.")) {
            Preferences.userRoot().node("tiketwahana").put(key, value);
        }
    }

    /** Test hook: forget any cached configuration. */
    public static synchronized void reset() {
        resolved = null;
    }
}

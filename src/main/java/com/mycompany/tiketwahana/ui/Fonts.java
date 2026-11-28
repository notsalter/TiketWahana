package com.mycompany.tiketwahana.ui;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Bundled Inter + JetBrains Mono. If a resource is missing the app still runs on
 * the platform sans, so a broken build never produces an unreadable window.
 */
public final class Fonts {

    private static final String[] INTER = {
        "/fonts/Inter-Regular.ttf", "/fonts/Inter-Medium.ttf",
        "/fonts/Inter-SemiBold.ttf", "/fonts/Inter-Bold.ttf"
    };
    private static final String[] MONO = {
        "/fonts/JetBrainsMono-Regular.ttf", "/fonts/JetBrainsMono-Medium.ttf"
    };

    private static final Map<String, Font> LOADED = new HashMap<>();
    private static final String FAMILY;
    private static final String MONO_FAMILY;
    private static float scale = 1f;

    static {
        String ui = "SansSerif";
        String mono = "Monospaced";
        for (String path : INTER) {
            Font f = load(path);
            if (f != null) {
                LOADED.put(path, f);
                ui = f.getFamily();
            }
        }
        for (String path : MONO) {
            Font f = load(path);
            if (f != null) {
                LOADED.put(path, f);
                mono = f.getFamily();
            }
        }
        String[] available = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        FAMILY = contains(available, ui) ? ui : "SansSerif";
        MONO_FAMILY = contains(available, mono) ? mono : "Monospaced";
    }

    private Fonts() {}

    private static Font load(String path) {
        try (InputStream in = Fonts.class.getResourceAsStream(path)) {
            if (in == null) {
                return null;
            }
            return Font.createFont(Font.TRUETYPE_FONT, in);
        } catch (Exception e) {
            return null;
        }
    }

    private static boolean contains(String[] names, String family) {
        for (String n : names) {
            if (n.equalsIgnoreCase(family)) {
                return true;
            }
        }
        return false;
    }

    public static void setScale(float value) {
        scale = Math.max(0.75f, Math.min(2.0f, value));
    }

    public static float scale() {
        return scale;
    }

    public static Font regular(float size) {
        return new Font(FAMILY, Font.PLAIN, Math.round(size * scale));
    }

    public static Font medium(float size) {
        return new Font(FAMILY, Font.PLAIN, Math.round(size * scale));
    }

    public static Font semibold(float size) {
        return new Font(FAMILY, Font.BOLD, Math.round(size * scale));
    }

    public static Font bold(float size) {
        return new Font(FAMILY, Font.BOLD, Math.round(size * scale));
    }

    public static Font mono(float size) {
        return new Font(MONO_FAMILY, Font.PLAIN, Math.round(size * scale));
    }

    public static Font monoBold(float size) {
        return new Font(MONO_FAMILY, Font.BOLD, Math.round(size * scale));
    }

    public static String family() {
        return FAMILY;
    }
}

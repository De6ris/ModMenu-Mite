package io.github.prospector.modmenu.util;

import net.minecraft.I18n;

public class StringUtils {
    public static String translate(String key) {
        return I18n.getString(key);
    }

    public static String translateParams(String key, Object... objects) {
        return I18n.getStringParams(key, objects);
    }

    public static String translateWithFallback(String key, String fallback) {
        String string = I18n.getString(key);
        if (string.equals(key)) return fallback;
        return string;
    }
}

package io.github.prospector.modmenu.util;

import io.github.prospector.modmenu.config.ModMenuConfigManager;

import java.text.NumberFormat;

public class SharedConstants {
    public static String showLibrariesText() {
        return StringUtils.translateParams("modmenu.showLibraries", StringUtils.translate("modmenu.showLibraries." + ModMenuConfigManager.getConfig().showLibraries()));
    }

    public static String sortingText() {
        return StringUtils.translateParams("modmenu.sorting", StringUtils.translate("modmenu.sorting.ascending"));
    }

    public static String showingModsText() {
        return "Showing " + FabricUtils.getModSizeFormatted() + "/" + FabricUtils.getModSizeFormatted() + " Mods";
    }

    public static String showingModsText(int displayedCount) {
        return "Showing " + NumberFormat.getInstance().format(displayedCount) + "/" + FabricUtils.getModSizeFormatted() + " Mods";
    }

    public static String modsLoadedText() {
        return StringUtils.translate("modmenu.title") + StringUtils.translateParams("modmenu.loaded", FabricUtils.getModSizeFormatted());
    }
}

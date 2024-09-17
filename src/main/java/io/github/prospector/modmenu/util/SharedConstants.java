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
		String x = FabricUtils.getModSizeFormatted();
		return StringUtils.translateParams("modmenu.showingMods", x + "/" + x);
	}

	public static String showingModsText(int displayedCount) {
		return StringUtils.translateParams("modmenu.showingMods", NumberFormat.getInstance().format(displayedCount) + "/" + FabricUtils.getModSizeFormatted());
	}

	public static String modsLoadedText() {
		return StringUtils.translate("modmenu.title") + StringUtils.translateParams("modmenu.loaded", FabricUtils.getModSizeFormatted());
	}
}

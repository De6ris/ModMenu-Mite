package io.github.prospector.modmenu.api;

import net.minecraft.GuiScreen;

@FunctionalInterface
public interface ConfigScreenFactory<S extends GuiScreen> {
    S create(GuiScreen menuScreen);
}
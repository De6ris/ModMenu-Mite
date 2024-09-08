package io.github.prospector.modmenu.util;

import io.github.prospector.modmenu.ModMenu;
import net.minecraft.Minecraft;
import net.minecraft.ResourceLocation;
import net.minecraft.TextureManager;

public class TextureUtils {
    public static final String VanillaBackground = "/gui/options_background.png";
    public static final String FILTERS_BUTTON_LOCATION = "/gui/filters_button.png";
    public static final String CONFIGURE_BUTTON_LOCATION = "/gui/configure_button.png";
    public static final String UNKNOWN_ICON = "/gui/unknown_pack.png";
    public static final String PARENT_MOD_TEXTURE = "/gui/parent_mod.png";

    public static void bindTexture(String string) {
        textureManager().bindTexture(new ResourceLocation("textures/" + ModMenu.MOD_ID + string));
    }

    public static void bindVanillaTexture(String string) {
        textureManager().bindTexture(new ResourceLocation("textures" + string));
    }

    public static void bindDefaultBackground() {
        bindVanillaTexture(VanillaBackground);
    }

    private static TextureManager textureManager() {
        return Minecraft.getMinecraft().getTextureManager();
    }

}

package io.github.prospector.modmenu.mixin;

import io.github.prospector.modmenu.gui.ModListScreen;
import io.github.prospector.modmenu.util.SharedConstants;
import net.minecraft.GuiButton;
import net.minecraft.GuiMainMenu;
import net.minecraft.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiMainMenu.class)
public class MixinTitleScreen extends GuiScreen {

//    @SuppressWarnings("unchecked")
//    @Inject(at = @At("RETURN"), method = "initGui")
//    public void drawMenuButton(CallbackInfo info) {
////		GuiButton texturePackButton = (GuiButton) this.buttonList.get(this.buttonList.size() - 2);
////		texturePackButton.displayString = "Texture Packs";
////		int newWidth = ((MixinGuiButton) texturePackButton).getWidth() / 2 - 5;
////		((MixinGuiButton) texturePackButton).setWidth(newWidth);
////		this.buttonList.add(new ModMenuButtonWidget(100, this.width / 2 + 6, texturePackButton.yPosition, newWidth, 20,  "Mods (" + ModMenu.getFormattedModCount() + " loaded)"));
//    }

    @SuppressWarnings("unchecked")
    @Inject(method = "addSingleplayerMultiplayerButtons", at = @At("HEAD"))
    private void addButton(int par1, int par2, CallbackInfo ci) {
        this.buttonList.add(new GuiButton(100, this.width / 2 - 100, par1 + par2 * 2, 200, 20, SharedConstants.modsLoadedText()));

    }

    @Inject(method = "actionPerformed", at = @At("HEAD"))
    private void onActionPerformed(GuiButton button, CallbackInfo ci) {
        if (button.id == 100) {
            this.mc.displayGuiScreen(new ModListScreen(this));
        }
    }

}

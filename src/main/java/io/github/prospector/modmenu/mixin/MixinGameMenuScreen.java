package io.github.prospector.modmenu.mixin;

import io.github.prospector.modmenu.gui.ModListScreen;
import io.github.prospector.modmenu.util.SharedConstants;
import net.minecraft.GuiButton;
import net.minecraft.GuiIngameMenu;
import net.minecraft.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiIngameMenu.class)
public class MixinGameMenuScreen extends GuiScreen {

    @SuppressWarnings("unchecked")
    @Inject(at = @At("RETURN"), method = "initGui")
    public void drawMenuButton(CallbackInfo info) {
        this.buttonList.add(new GuiButton(100, this.width / 2 - 100, this.height / 4 + 72 - 16, 200, 20, SharedConstants.modsLoadedText()));
    }

    @Inject(method = "actionPerformed", at = @At("HEAD"))
    private void onActionPerformed(GuiButton button, CallbackInfo ci) {
        if (button.id == 100) {
            this.mc.displayGuiScreen(new ModListScreen(this));
        }
    }
}

package io.github.prospector.modmenu.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.prospector.modmenu.gui.ModListScreen;
import io.github.prospector.modmenu.util.FabricUtils;
import io.github.prospector.modmenu.util.SharedConstants;
import io.github.prospector.modmenu.util.StringUtils;
import net.minecraft.EnumChatFormatting;
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

	@ModifyExpressionValue(method = "drawScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/Minecraft;getVersionDescriptor(Z)Ljava/lang/String;"))
	private String modifyVersionDescriptor(String original) {
		return original + EnumChatFormatting.RESET + StringUtils.translateParams("modmenu.versionDescriptor", FabricUtils.getModSizeFormatted());
	}

}

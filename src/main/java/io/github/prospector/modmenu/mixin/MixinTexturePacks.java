//package io.github.prospector.modmenu.mixin;
//
//import io.github.prospector.modmenu.ModMenu;
//import net.minecraft.TexturePackBase;
//import net.minecraft.TexturePackCustom;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//
//import java.io.InputStream;
//
//@Mixin({TexturePackBase.class, TexturePackCustom.class})
//public class MixinTexturePacks {
//
//	@Inject(method = "func_6481_a", at = @At(value = "INVOKE", target = "Ljava/lang/Class;getResourceAsStream(Ljava/lang/String;)Ljava/io/InputStream;", remap = false), cancellable = true)
//	private void onGetResource(String resource, CallbackInfoReturnable<InputStream> ci) {
//		InputStream in = ModMenu.class.getClassLoader().getResourceAsStream(resource);
//		if (in != null)
//			ci.setReturnValue(in);
//	}
//
//}

package io.github.prospector.modmenu.util;

import net.minecraft.AbstractTexture;
import net.minecraft.ResourceManager;
import net.minecraft.TextureUtil;

import java.awt.image.BufferedImage;

public class BufferedImageTexture extends AbstractTexture {

    BufferedImage bufferedImage;

    boolean textureUploaded;

    public BufferedImageTexture(BufferedImage image) {
        this.bufferedImage = image;
    }

    @Override
    public void loadTexture(ResourceManager resourceManager) {
        if (this.bufferedImage != null) {
            TextureUtil.uploadTextureImage(this.getGlTextureId(), this.bufferedImage);
        }
    }

    @Override
    public int getGlTextureId() {
        int n = super.getGlTextureId();
        if (!this.textureUploaded && this.bufferedImage != null) {
            TextureUtil.uploadTextureImage(n, this.bufferedImage);
            this.textureUploaded = true;
        }
        return n;
    }
}

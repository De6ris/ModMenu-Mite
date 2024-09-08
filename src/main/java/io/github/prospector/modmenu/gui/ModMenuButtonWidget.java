package io.github.prospector.modmenu.gui;

import net.minecraft.GuiButton;
import net.minecraft.Minecraft;

import java.util.List;

public class ModMenuButtonWidget extends GuiButton {
    private Action clickAction;
    private Action updateAction;

    public ModMenuButtonWidget(int x, int y, int width, int height, String text) {
        super(0, x, y, width, height, text);
    }

    public static Builder builder(String text, Action action) {
        return new Builder(text, action);
    }

    @Override
    public void drawButton(Minecraft par1Minecraft, int par2, int par3) {
        if (this.updateAction != null) this.updateAction.onCall(this);
        super.drawButton(par1Minecraft, par2, par3);
    }

    public ModMenuButtonWidget setOnUpdate(Action action) {
        this.updateAction = action;
        return this;
    }

    public ModMenuButtonWidget setOnClick(Action action) {
        this.clickAction = action;
        return this;
    }

    public void addToList(List<GuiButton> buttonList) {
        buttonList.add(this);
    }

    // listens here
    @Override
    public boolean mousePressed(Minecraft par1Minecraft, int par2, int par3) {
        if (!super.mousePressed(par1Minecraft, par2, par3)) {
            return false;
        }
        if (this.clickAction != null) this.clickAction.onCall(this);
        return true;
    }

    @FunctionalInterface
    public interface Action {
        void onCall(GuiButton button);
    }

    public static class Builder {
        String text;
        Action action;
        int x;
        int y;
        int width = 200;
        int height = 20;

        Builder(String text, Action action) {
            this.text = text;
            this.action = action;
        }

        public Builder position(int x, int y) {
            this.x = x;
            this.y = y;
            return this;
        }

        public Builder size(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder dimensions(int x, int y, int width, int height) {
            return this.position(x, y).size(width, height);
        }

        public ModMenuButtonWidget build() {
            return new ModMenuButtonWidget(x, y, width, height, text).setOnClick(action);
        }
    }
}

package io.github.prospector.modmenu.gui;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import io.github.prospector.modmenu.ModMenu;
import io.github.prospector.modmenu.config.ModMenuConfigManager;
import io.github.prospector.modmenu.util.*;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.api.metadata.Person;
import net.minecraft.FontRenderer;
import net.minecraft.GuiScreen;
import net.minecraft.Minecraft;
import net.minecraft.Tessellator;
import org.apache.logging.log4j.Logger;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL14;

import java.io.File;
import java.net.MalformedURLException;
import java.util.*;

public class ModListScreen extends GuiScreen {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final String textTitle;
    private TextFieldWidget searchBox;
    private DescriptionListWidget descriptionListWidget;
    private GuiScreen parent;
    private ModListWidget modList;
    private String tooltip;
    private ModListEntry selected;
    private BadgeRenderer badgeRenderer;
    private double scrollPercent = 0;
    private boolean showModCount = false;
    private boolean init = false;
    private boolean filterOptionsShown = false;
    private int paneY;
    private int paneWidth;
    private int rightPaneX;
    private int searchBoxX;
    public Set<String> showModChildren = new HashSet<>();
    private String lastSearchString = null;

    public ModListScreen(GuiScreen previousGui) {
        this.parent = previousGui;
        this.textTitle = StringUtils.translate("modmenu.title");
    }

    @Override
    public void handleMouseInput() {
        super.handleMouseInput();
        int dWheel = Mouse.getEventDWheel();
        if (dWheel != 0) {
            int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
            int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
            mouseScrolled(mouseX, mouseY, dWheel);
        }
    }

    public void mouseScrolled(double double_1, double double_2, double double_3) {
        if (modList.isMouseOver(double_1, double_2))
            this.modList.mouseScrolled(double_1, double_2, double_3);
        if (descriptionListWidget.isMouseOver(double_1, double_2))
            this.descriptionListWidget.mouseScrolled(double_1, double_2, double_3);
    }

    @Override
    public void updateScreen() {
        this.searchBox.updateCursorCounter();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        FontRenderer font = this.fontRenderer;
        paneY = 48;
        paneWidth = this.width / 2 - 8;
        rightPaneX = width - paneWidth;

        int searchBoxWidth = paneWidth - 32 - 22;
        searchBoxX = paneWidth / 2 - searchBoxWidth / 2 - 22 / 2;
        String oldText = this.searchBox == null ? "" : this.searchBox.getText();
        this.searchBox = new TextFieldWidget(this.fontRenderer, searchBoxX, 22, searchBoxWidth, 20);
        this.searchBox.setText(oldText);
        this.modList = new ModListWidget(this.mc, paneWidth, this.height, paneY + 19, this.height - 36, 36, this.searchBox.getText(), this.modList, this);
        this.modList.setLeftPos(0);
        this.descriptionListWidget = new DescriptionListWidget(this.mc, paneWidth, this.height, paneY + 60, this.height - 36, 9 + 1, this);
        this.descriptionListWidget.setLeftPos(rightPaneX);

        new ModMenuTexturedButtonWidget(width - 24, paneY, 20, 20, 0, 0, TextureUtils.CONFIGURE_BUTTON_LOCATION, 32, 64) {
            @Override
            public void drawButton(Minecraft mc, int mouseX, int mouseY) {
                if (selected != null) {
                    String modid = selected.getMetadata().getId();
                    enabled = ModMenu.hasConfigScreenFactory(modid) || ModMenu.hasLegacyConfigScreenTask(modid);
                } else {
                    enabled = false;
                }
                drawButton = enabled; // visible = enabled
                GL11.glColor4f(1f, 1f, 1f, 1f);
                if (drawButton && isHovered(mouseX, mouseY)) {
                    setTooltip(StringUtils.translate("modmenu.configure"));
                }
                super.drawButton(mc, mouseX, mouseY);
            }
        }.setOnClick(button -> {
            final String modid = Objects.requireNonNull(selected).getMetadata().getId();
            final GuiScreen screen = ModMenu.getConfigScreen(modid, this);
            if (screen != null) {
                mc.displayGuiScreen(screen);
            } else {
                ModMenu.openConfigScreen(modid);
            }
        }).addToList(this.buttonList);

        int urlButtonWidths = paneWidth / 2 - 2;
        int cappedButtonWidth = urlButtonWidths > 200 ? 200 : urlButtonWidths;

        ModMenuButtonWidget.builder(StringUtils.translate("modmenu.website"), button -> {
                    final ModMetadata metadata = Objects.requireNonNull(selected).getMetadata();
                    metadata.getContact().get("homepage").ifPresent(Sys::openURL);
                }).dimensions(rightPaneX + (urlButtonWidths / 2) - (cappedButtonWidth / 2), paneY + 36, urlButtonWidths > 200 ? 200 : urlButtonWidths, 20)
                .build()
                .setOnUpdate(button -> {
                    button.drawButton = selected != null; // visible = selected != null
                    button.enabled = button.drawButton && selected.getMetadata().getContact().get("homepage").isPresent();
                }).addToList(this.buttonList);

        ModMenuButtonWidget.builder(StringUtils.translate("modmenu.issues"), button -> {
                    final ModMetadata metadata = Objects.requireNonNull(selected).getMetadata();
                    metadata.getContact().get("issues").ifPresent(Sys::openURL);
                }).dimensions(rightPaneX + urlButtonWidths + 4 + (urlButtonWidths / 2) - (cappedButtonWidth / 2), paneY + 36, urlButtonWidths > 200 ? 200 : urlButtonWidths, 20)
                .build()
                .setOnUpdate(button -> {
                    button.drawButton = selected != null; // visible = selected != null
                    button.enabled = button.drawButton && selected.getMetadata().getContact().get("issues").isPresent();
                }).addToList(this.buttonList);

        new ModMenuTexturedButtonWidget(paneWidth / 2 + searchBoxWidth / 2 - 20 / 2 + 2, 22, 20, 20, 0, 0, TextureUtils.FILTERS_BUTTON_LOCATION, 32, 64) {
            @Override
            public void drawButton(Minecraft mc, int int_1, int int_2) {
                super.drawButton(mc, int_1, int_2);
                if (isHovered(int_1, int_2)) {
                    setTooltip(StringUtils.translate("modmenu.toggleFilterOptions"));
                }
            }
        }.setOnClick(button -> filterOptionsShown = !filterOptionsShown).addToList(this.buttonList);

        String showLibrariesText = SharedConstants.showLibrariesText();
        String sortingText = SharedConstants.sortingText();
        int showLibrariesWidth = font.getStringWidth(showLibrariesText) + 20;
        int sortingWidth = font.getStringWidth(sortingText) + 20;
        int filtersX;
        int filtersWidth = showLibrariesWidth + sortingWidth + 2;
        if ((filtersWidth + font.getStringWidth(SharedConstants.showingModsText()) + 20) >= searchBoxX + searchBoxWidth + 22) {
            filtersX = paneWidth / 2 - filtersWidth / 2;
            showModCount = false;
        } else {
            filtersX = searchBoxX + searchBoxWidth + 22 - filtersWidth + 1;
            showModCount = true;
        }

        ModMenuButtonWidget.builder(sortingText, button -> {
                    ModMenuConfigManager.getConfig().toggleSortMode();
                    modList.reloadFilters();
                }).dimensions(filtersX, 45, sortingWidth, 20)
                .build()
                .setOnUpdate(button -> {
                    button.drawButton = filterOptionsShown; // visible = filterOptionsShown
                    button.displayString = SharedConstants.sortingText();
                }).addToList(this.buttonList);

        ModMenuButtonWidget.builder(showLibrariesText, button -> {
                    ModMenuConfigManager.getConfig().toggleShowLibraries();
                    modList.reloadFilters();
                }).dimensions(filtersX + sortingWidth + 2, 45, showLibrariesWidth, 20)
                .build()
                .setOnUpdate(button -> {
                    button.drawButton = filterOptionsShown; // visible = filterOptionsShown
                    button.displayString = SharedConstants.showLibrariesText();
                }).addToList(this.buttonList);

        ModMenuButtonWidget.builder(StringUtils.translate("modmenu.modsFolder"), button -> {
            File modsFolder = new File(FabricUtils.getGameDirectory(), "mods");
            try {
                Sys.openURL(modsFolder.toURI().toURL().toString());
            } catch (MalformedURLException e) {
                LOGGER.error("Malformed mods folder URL", e);
            }
        }).dimensions(this.width / 2 - 154, this.height - 28, 150, 20).build().addToList(this.buttonList);

        ModMenuButtonWidget.builder(StringUtils.translate("gui.done"), button -> mc.displayGuiScreen(parent))
                .dimensions(this.width / 2 + 4, this.height - 28, 150, 20).build().addToList(this.buttonList);

        this.searchBox.setFocused(true);

        init = true;
    }

    public ModListWidget getModList() {
        return modList;
    }

    @Override
    public void keyTyped(char charIn, int keyCode) {
        this.searchBox.textboxKeyTyped(charIn, keyCode);
        super.keyTyped(charIn, keyCode);
        modList.keyPressed(keyCode, 0, 0);
        descriptionListWidget.keyPressed(keyCode, 0, 0);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        modList.mouseClicked(mouseX, mouseY, mouseButton);
        descriptionListWidget.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseMovedOrUp(int mouseX, int mouseY, int mouseButton) {
        super.mouseMovedOrUp(mouseX, mouseY, mouseButton);

        if (mouseButton == -1) {
            int mouseDX = Mouse.getEventDX() * this.width / this.mc.displayWidth;
            int mouseDY = this.height - Mouse.getEventDY() * this.height / this.mc.displayHeight - 1;
            for (int button = 0; button < Mouse.getButtonCount(); button++) {
                if (Mouse.isButtonDown(button)) {
                    modList.mouseDragged(mouseX, mouseY, mouseButton, mouseDX, mouseDY);
                    descriptionListWidget.mouseDragged(mouseX, mouseY, mouseButton, mouseDX, mouseDY);
                }
            }
        } else {
            modList.mouseReleased(mouseX, mouseY, mouseButton);
            descriptionListWidget.mouseReleased(mouseX, mouseY, mouseButton);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float delta) {
        FontRenderer font = this.fontRenderer;
        if (!searchBox.getText().equals(lastSearchString)) {
            lastSearchString = searchBox.getText();
            modList.filter(lastSearchString, false);
        }
        overlayBackground(paneWidth, 0, rightPaneX, height, 64, 64, 64, 255, 255);
        this.tooltip = null;
        ModListEntry selectedEntry = selected;
        if (selectedEntry != null) {
            this.descriptionListWidget.render(mouseX, mouseY, delta);
        }
        this.modList.render(mouseX, mouseY, delta);
        this.searchBox.drawTextBox();
        GL11.glDisable(GL11.GL_BLEND);
        this.drawCenteredString(font, this.textTitle, this.modList.getWidth() / 2, 8, 0xffffff);
        super.drawScreen(mouseX, mouseY, delta);
        if (showModCount || !filterOptionsShown) {
            font.drawString(SharedConstants.showingModsText(modList.getDisplayedCount()), searchBoxX, 52, 0xFFFFFF);
        }
        if (selectedEntry != null) {
            ModMetadata metadata = selectedEntry.getMetadata();
            int x = rightPaneX;
            GL11.glColor4f(1f, 1f, 1f, 1f);
            this.selected.bindIconTexture();
            GL11.glEnable(GL11.GL_BLEND);
            Tessellator tess = Tessellator.instance;
            tess.startDrawingQuads();
            tess.addVertexWithUV(x, paneY, 0, 0, 0);
            tess.addVertexWithUV(x, paneY + 32, 0, 0, 1);
            tess.addVertexWithUV(x + 32, paneY + 32, 0, 1, 1);
            tess.addVertexWithUV(x + 32, paneY, 0, 1, 0);
            tess.draw();
            GL11.glDisable(GL11.GL_BLEND);
            int lineSpacing = 9 + 1;
            int imageOffset = 36;
            String name = metadata.getName();
            name = HardcodedUtil.formatFabricModuleName(name);
            String trimmedName = name;
            int maxNameWidth = this.width - (x + imageOffset);
            if (font.getStringWidth(name) > maxNameWidth) {
                int maxWidth = maxNameWidth - font.getStringWidth("...");
                trimmedName = "";
                while (font.getStringWidth(trimmedName) < maxWidth && trimmedName.length() < name.length()) {
                    trimmedName += name.charAt(trimmedName.length());
                }
                trimmedName = trimmedName.isEmpty() ? "..." : trimmedName.substring(0, trimmedName.length() - 1) + "...";
            }
            font.drawString(trimmedName, x + imageOffset, paneY + 1, 0xFFFFFF);
            if (mouseX > x + imageOffset && mouseY > paneY + 1 && mouseY < paneY + 1 + 9 && mouseX < x + imageOffset + font.getStringWidth(trimmedName)) {
                setTooltip(StringUtils.translateParams("modmenu.modIdToolTip", metadata.getId()));
            }
            if (init || badgeRenderer == null || badgeRenderer.getMetadata() != metadata) {
                badgeRenderer = new BadgeRenderer(mc, x + imageOffset + font.getStringWidth(trimmedName) + 2, paneY, width - 28, selectedEntry.container, this);
                init = false;
            }
            badgeRenderer.draw(mouseX, mouseY);
            font.drawString("v" + metadata.getVersion().getFriendlyString(), x + imageOffset, paneY + 2 + lineSpacing, 0x808080);
            String authors;
            List<String> names = new ArrayList<>();

            metadata.getAuthors().stream()
                    .filter(Objects::nonNull)
                    .map(Person::getName)
                    .filter(Objects::nonNull)
                    .forEach(names::add);

            if (!names.isEmpty()) {
                if (names.size() > 1) {
                    authors = Joiner.on(", ").join(names);
                } else {
                    authors = names.get(0);
                }
                RenderUtils.INSTANCE.drawWrappedString(font, StringUtils.translateParams("modmenu.authorPrefix", authors), x + imageOffset, paneY + 2 + lineSpacing * 2, paneWidth - imageOffset - 4, 1, 0x808080);
            }
            if (this.tooltip != null) {
                this.renderTooltip(Lists.newArrayList(Splitter.on("\n").split(this.tooltip)), mouseX, mouseY);
            }
        }

    }

    public void overlayBackground(int x1, int y1, int x2, int y2, int red, int green, int blue, int startAlpha, int endAlpha) {
        Tessellator tessellator = Tessellator.instance;
        TextureUtils.bindDefaultBackground();
        GL11.glColor4f(1f, 1f, 1f, 1f);
        tessellator.startDrawingQuads();
        tessellator.setColorRGBA(red, green, blue, endAlpha);
        tessellator.addVertexWithUV(x1, y2, 0.0D, x1 / 32.0F, y2 / 32.0F);
        tessellator.addVertexWithUV(x2, y2, 0.0D, x2 / 32.0F, y2 / 32.0F);
        tessellator.setColorRGBA(red, green, blue, startAlpha);
        tessellator.addVertexWithUV(x2, y1, 0.0D, x2 / 32.0F, y1 / 32.0F);
        tessellator.addVertexWithUV(x1, y1, 0.0D, x1 / 32.0F, y1 / 32.0F);
        tessellator.draw();
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        this.modList.close();
    }

    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
    }

    public ModListEntry getSelectedEntry() {
        return selected;
    }

    public void updateSelectedEntry(ModListEntry entry) {
        if (entry != null) {
            this.selected = entry;
        }
    }

    public double getScrollPercent() {
        return scrollPercent;
    }

    public void updateScrollPercent(double scrollPercent) {
        this.scrollPercent = scrollPercent;
    }

    public String getSearchInput() {
        return searchBox.getText();
    }

    public boolean showingFilterOptions() {
        return filterOptionsShown;
    }

    public void renderTooltip(List<String> list, int i, int j) {
        if (!list.isEmpty()) {
            FontRenderer font = this.fontRenderer;

            GL11.glDisable(GL12.GL_RESCALE_NORMAL);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            int k = 0;

            for (String string : list) {
                int l = font.getStringWidth(string);
                if (l > k) {
                    k = l;
                }
            }

            int m = i + 12;
            int n = j - 12;
            int p = 8;
            if (list.size() > 1) {
                p += 2 + (list.size() - 1) * 10;
            }

            if (m + k > this.width) {
                m -= 28 + k;
            }

            if (n + p + 6 > this.height) {
                n = this.height - p - 6;
            }

            int q = 0xf0100010;
            this.fillGradient(m - 3, n - 4, m + k + 3, n - 3, 0xf0100010, 0xf0100010);
            this.fillGradient(m - 3, n + p + 3, m + k + 3, n + p + 4, 0xf0100010, 0xf0100010);
            this.fillGradient(m - 3, n - 3, m + k + 3, n + p + 3, 0xf0100010, 0xf0100010);
            this.fillGradient(m - 4, n - 3, m - 3, n + p + 3, 0xf0100010, 0xf0100010);
            this.fillGradient(m + k + 3, n - 3, m + k + 4, n + p + 3, 0xf0100010, 0xf0100010);
            int r = 0x505000ff;
            int s = 0x5028007f;
            this.fillGradient(m - 3, n - 3 + 1, m - 3 + 1, n + p + 3 - 1, 0x505000ff, 0x5028007f);
            this.fillGradient(m + k + 2, n - 3 + 1, m + k + 3, n + p + 3 - 1, 0x505000ff, 0x5028007f);
            this.fillGradient(m - 3, n - 3, m + k + 3, n - 3 + 1, 0x505000ff, 0x505000ff);
            this.fillGradient(m - 3, n + p + 2, m + k + 3, n + p + 3, 0x5028007f, 0x5028007f);
            GL11.glPushMatrix();
            GL11.glTranslatef(0, 0, 300);

            for (int t = 0; t < list.size(); ++t) {
                String string2 = list.get(t);
                if (string2 != null) {
                    font.drawString(string2, m, n, 0xf000f0);
                }

                if (t == 0) {
                    n += 2;
                }

                n += 10;
            }

            GL11.glPopMatrix();
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        }
    }

    protected void fillGradient(int i, int j, int k, int l, int m, int n) {
        float f = (float) (m >> 24 & 255) / 255.0F;
        float g = (float) (m >> 16 & 255) / 255.0F;
        float h = (float) (m >> 8 & 255) / 255.0F;
        float o = (float) (m & 255) / 255.0F;
        float p = (float) (n >> 24 & 255) / 255.0F;
        float q = (float) (n >> 16 & 255) / 255.0F;
        float r = (float) (n >> 8 & 255) / 255.0F;
        float s = (float) (n & 255) / 255.0F;
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.setColorRGBA_F(g, h, o, f);
        tessellator.addVertex((double) k, (double) j, 300);
        tessellator.addVertex((double) i, (double) j, 300);
        tessellator.setColorRGBA_F(q, r, s, p);
        tessellator.addVertex((double) i, (double) l, 300);
        tessellator.addVertex((double) k, (double) l, 300);
        tessellator.draw();
        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }
}

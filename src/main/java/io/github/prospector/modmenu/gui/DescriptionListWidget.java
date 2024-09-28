package io.github.prospector.modmenu.gui;

import io.github.prospector.modmenu.config.ModMenuConfig;
import io.github.prospector.modmenu.util.HardcodedUtil;
import io.github.prospector.modmenu.util.RenderUtils;
import net.minecraft.FontRenderer;
import net.minecraft.GuiConfirmOpenLink;
import net.minecraft.I18n;
import net.minecraft.Minecraft;
import org.lwjgl.Sys;

import java.util.*;

public class DescriptionListWidget extends EntryListWidget<DescriptionListWidget.DescriptionEntry> {

    private final ModListScreen parent;
    private final FontRenderer textRenderer;
    private ModListEntry lastSelected = null;

    public DescriptionListWidget(Minecraft client, int width, int height, int top, int bottom, int entryHeight, ModListScreen parent) {
        super(client, width, height, top, bottom, entryHeight);
        this.parent = parent;
        this.textRenderer = client.fontRenderer;
    }

    @Override
    public DescriptionEntry getSelected() {
        return null;
    }

    @Override
    public int getRowWidth() {
        return this.width - 10;
    }

    @Override
    protected int getScrollbarPosition() {
        return this.width - 6 + left;
    }

    @Override
    public void render(int mouseX, int mouseY, float delta) {
		ModListEntry selectedEntry = parent.getSelectedEntry();
		int rowWidth = getRowWidth();
		if (selectedEntry != lastSelected) {
			lastSelected = selectedEntry;
			clearEntries();
			setScrollAmount(-Double.MAX_VALUE);
			String description = lastSelected.getMetadata().getDescription();
			String id = lastSelected.getMetadata().getId();
			if (description.isEmpty() && HardcodedUtil.getHardcodedDescriptions().containsKey(id)) {
				description = HardcodedUtil.getHardcodedDescription(id);
			}
			if (Objects.equals(id, "mite"))
				children().add(new DescriptionEntry(I18n.getString("modmenu.mite.description")));
			if (lastSelected != null && description != null && !description.isEmpty()) {
//				List<String> lines = RenderUtils.INSTANCE.wrapStringToWidthAsList(textRenderer, description.replaceAll("\n", "\n\n"), getRowWidth());
				List<String> lines = Arrays.stream(description.split("\n")).flatMap(x -> RenderUtils.INSTANCE.wrapStringToWidthAsList(textRenderer, x, rowWidth).stream()).toList();
				for (String line : lines) {
					children().add(new DescriptionEntry(line));
				}
			}

//		Map<String, String> links = (Map<String, String>) lastSelected.getMetadata().getContact();
//		String sourceLink = ((Map<?, ?>) lastSelected.getMetadata().getContact()).get("source").toString();
//		if ((!links.isEmpty() || sourceLink != null )) {
//			addEntry( new DescriptionEntry( ""));
//			addEntry( new DescriptionEntry( I18n.getString( "modmenu.links" )));
//
//			if ( sourceLink != null ) {
//				addEntry( new LinkEntry( new LiteralText( "  " ).append( new TranslatableText( "modmenu.source" ).setStyle( new Style().setFormatting( Formatting.BLUE ).setFormatting( ( Formatting.UNDERLINE ) ) ) ).asFormattedString(), sourceLink, this ) );
//			}
//
//			links.forEach( ( key, value ) -> {
//				addEntry( new LinkEntry( new LiteralText( "  " ).append( new TranslatableText( key ).setStyle( new Style().setFormatting( Formatting.BLUE ).setFormatting( ( Formatting.UNDERLINE ) ) ) ).asFormattedString(), value, this ) );
//			} );
//		}

			String licenses = lastSelected.getMetadata().getLicense().toString();

			if (lastSelected != null && licenses != null && !licenses.isEmpty()) {
				List<String> lines = Arrays.stream(licenses.split("\n")).flatMap(x -> RenderUtils.INSTANCE.wrapStringToWidthAsList(textRenderer, x, rowWidth).stream()).toList();
				for (String line : lines) {
					if (line != null && !licenses.equals("[]")) {
						addEntry(new DescriptionEntry(""));
						addEntry(new DescriptionEntry(I18n.getString("modmenu.license")));
						children().add(new DescriptionEntry(line));
					}
				}
			}
		}

//		if ( !ModMenuConfig.HIDE_MOD_CREDITS.getValue() ) {
//			if ( "minecraft".equals( mod.getId() ) ) {
//				addEntry( new DescriptionEntry( "", this ) );
//				addEntry( new MojangCreditsEntry( new TranslatableText( "modmenu.viewCredits" ).setStyle( new Style().setFormatting( Formatting.BLUE ).setFormatting( ( Formatting.UNDERLINE ) ) ).asFormattedString(), this ) );
//			} else if ( "java".equals( mod.getId() ) ) {
//				addEntry( new DescriptionEntry( "", this ) );
//			} else {
//				List<String> authors = mod.getAuthors();
//				List<String> contributors = mod.getContributors();
//				if ( !authors.isEmpty() || !contributors.isEmpty() ) {
//					addEntry( new DescriptionEntry( "", this ) );
//					addEntry( new DescriptionEntry( I18n.translate( "modmenu.credits" ), this ) );
//					for ( String author : authors ) {
//						addEntry( new DescriptionEntry( "  " + author, this ) );
//					}
//					for ( String contributor : contributors ) {
//						addEntry( new DescriptionEntry( "  " + contributor, this ) );
//					}
//				}
//			}
//		}
        super.render(mouseX, mouseY, delta);
    }

    @Override
    protected void renderHoleBackground(int y1, int y2, int startAlpha, int endAlpha) {
        // Awful hack but it makes the background "seamless"
        parent.overlayBackground(left, y1, right, y2, 64, 64, 64, startAlpha, endAlpha);
    }

    protected class DescriptionEntry extends EntryListWidget.Entry<DescriptionEntry> {
        protected String text;

        public DescriptionEntry(String text) {
            this.text = text;
        }

        @Override
        public void render(int index, int y, int x, int itemWidth, int itemHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
            textRenderer.drawStringWithShadow(text, x, y, 0xAAAAAA);
        }
    }
}

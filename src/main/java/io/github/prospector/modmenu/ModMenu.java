package io.github.prospector.modmenu;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.LinkedListMultimap;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.prospector.modmenu.api.ConfigScreenFactory;
import io.github.prospector.modmenu.api.ModMenuApi;
import io.github.prospector.modmenu.config.ModMenuConfigManager;
import io.github.prospector.modmenu.util.FabricUtils;
import io.github.prospector.modmenu.util.HardcodedUtil;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.GuiScreen;

import java.util.*;

public class ModMenu implements ClientModInitializer {
    public static final String MOD_ID = "modmenu";
    public static final Gson GSON = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).setPrettyPrinting().create();

    private static final Map<String, Runnable> LEGACY_CONFIG_SCREEN_TASKS = new HashMap<>();
    public static final List<String> LIBRARY_MODS = new ArrayList<>();
    public static final Set<String> CLIENTSIDE_MODS = new HashSet<>();
    public static final Set<String> PATCHWORK_FORGE_MODS = new HashSet<>();
    public static final LinkedListMultimap<ModContainer, ModContainer> PARENT_MAP = LinkedListMultimap.create();
    //    private static ImmutableMap<String, Function<GuiScreen, ? extends GuiScreen>> configScreenFactories = ImmutableMap.of();
    private static ImmutableMap<String, ConfigScreenFactory<? extends GuiScreen>> configScreenFactories = ImmutableMap.of();
    private static int libraryCount = 0;

    public static boolean hasConfigScreenFactory(String modid) {
        return configScreenFactories.containsKey(modid);
    }

    public static GuiScreen getConfigScreen(String modid, GuiScreen menuScreen) {
        ConfigScreenFactory<? extends GuiScreen> factory = configScreenFactories.get(modid);
        return factory != null ? factory.create(menuScreen) : null;
    }

    public static void openConfigScreen(String modid) {
        Runnable opener = LEGACY_CONFIG_SCREEN_TASKS.get(modid);
        if (opener != null) opener.run();
    }

    public static void addLegacyConfigScreenTask(String modid, Runnable task) {
        LEGACY_CONFIG_SCREEN_TASKS.putIfAbsent(modid, task);
    }

    public static boolean hasLegacyConfigScreenTask(String modid) {
        return LEGACY_CONFIG_SCREEN_TASKS.containsKey(modid);
    }

    public static void addLibraryMod(String modid) {
        if (LIBRARY_MODS.contains(modid)) return;

        LIBRARY_MODS.add(modid);
    }

    @Override
    public void onInitializeClient() {
        ModMenuConfigManager.initializeConfig();
        ImmutableMap.Builder<String, ConfigScreenFactory<?>> factories = ImmutableMap.builder();
//        FabricUtils.getEntrypoints("modmenu", ModMenuApi.class).forEach(api -> factories.put(api.getModId(), api.getConfigScreenFactory()));
        FabricUtils.getEntrypointContainers("modmenu", ModMenuApi.class)
                .forEach(container -> factories.put(container.getProvider().getMetadata().getId(), container.getEntrypoint().getModConfigScreenFactory()));
        configScreenFactories = factories.build();
        Collection<ModContainer> mods = FabricUtils.getAllMods();
        HardcodedUtil.initializeHardcodings();
        for (ModContainer mod : mods) {
            ModMetadata metadata = mod.getMetadata();
            String id = metadata.getId();
            if (metadata.containsCustomValue("modmenu:api") && metadata.getCustomValue("modmenu:api").getAsBoolean()) {
                addLibraryMod(id);
            }
            if (metadata.containsCustomValue("modmenu:clientsideOnly") && metadata.getCustomValue("modmenu:clientsideOnly").getAsBoolean()) {
                CLIENTSIDE_MODS.add(id);
            }
            if (metadata.containsCustomValue("patchwork:source") && metadata.getCustomValue("patchwork:source").getAsObject() != null) {
                CustomValue.CvObject object = metadata.getCustomValue("patchwork:source").getAsObject();
                if ("forge".equals(object.get("loader").getAsString())) {
                    PATCHWORK_FORGE_MODS.add(id);
                }
            }
            if (metadata.containsCustomValue("modmenu:parent")) {
                String parentId = metadata.getCustomValue("modmenu:parent").getAsString();
                if (parentId != null) {
                    Optional<ModContainer> parent = FabricUtils.getModContainer(parentId);
                    parent.ifPresent(modContainer -> PARENT_MAP.put(modContainer, mod));
                }
            } else {
                HardcodedUtil.hardcodeModuleMetadata(mod, metadata, id);
            }
        }
        libraryCount = LIBRARY_MODS.size();
    }

}

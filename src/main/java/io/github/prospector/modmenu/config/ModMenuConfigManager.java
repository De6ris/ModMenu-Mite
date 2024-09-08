package io.github.prospector.modmenu.config;

import io.github.prospector.modmenu.ModMenu;
import io.github.prospector.modmenu.util.FabricUtils;

import java.io.*;

public class ModMenuConfigManager {
    private static File file;
    private static ModMenuConfig config;

    private static void prepareBiomeConfigFile() {
        if (file != null) {
            return;
        }
        file = new File(FabricUtils.getConfigDirectory() + ModMenu.MOD_ID + ".json");
    }

    public static ModMenuConfig initializeConfig() {
        if (config != null) {
            return config;
        }

        config = new ModMenuConfig();
        load();

        return config;
    }

    private static void load() {
        prepareBiomeConfigFile();

        try {
            if (!file.exists()) {
                save();
            }
            if (file.exists()) {
                BufferedReader br = new BufferedReader(new FileReader(file));

                config = ModMenu.GSON.fromJson(br, ModMenuConfig.class);
            }
        } catch (FileNotFoundException e) {
            System.err.println("Couldn't load Mod Menu configuration file; reverting to defaults");
            e.printStackTrace();
        }
    }

    public static void save() {
        prepareBiomeConfigFile();

        String jsonString = ModMenu.GSON.toJson(config);

        if (!file.exists()) {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }

            try {
                file.createNewFile();
            } catch (IOException e) {
                System.err.println("Couldn't create Mod Menu configuration file");
                e.printStackTrace();
            }
        }

        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(jsonString);
        } catch (IOException e) {
            System.err.println("Couldn't save Mod Menu configuration file");
            e.printStackTrace();
        }
    }

    public static ModMenuConfig getConfig() {
        return config;
    }
}

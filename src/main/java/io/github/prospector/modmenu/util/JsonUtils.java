package io.github.prospector.modmenu.util;

import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class JsonUtils {

    private static final Logger LOGGER = LogUtils.getLogger();

    public static boolean writeJsonToFile(String jsonString, File file) {
        File fileTmp = new File(file.getParentFile(), file.getName() + ".tmp");

        if (fileTmp.exists()) {
            fileTmp = new File(file.getParentFile(), UUID.randomUUID() + ".tmp");
        }

        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(fileTmp), StandardCharsets.UTF_8)) {
            writer.write(jsonString);
            writer.close();

            if (file.exists() && file.isFile() && file.delete() == false) {
                LOGGER.warn("Failed to delete file '{}'", file.getAbsolutePath());
            }
            return fileTmp.renameTo(file);
        } catch (Exception e) {
            LOGGER.warn("Failed to write JSON data to file '{}'", fileTmp.getAbsolutePath(), e);
        }

        return false;
    }
}

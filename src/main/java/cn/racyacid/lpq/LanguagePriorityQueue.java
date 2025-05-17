package cn.racyacid.lpq;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class LanguagePriorityQueue {
    private static final HashMap<String, String[]> QUEUES = HashMap.newHashMap(8);
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().toAbsolutePath().resolve("lpq");
    private static final File CONFIG = CONFIG_PATH.resolve("queues.json").toFile();
    public static final Logger LOGGER = LoggerFactory.getLogger("LPQ");

    @SuppressWarnings("SameReturnValue")
    public static Map<String, String[]> getQueues() {
        if (!QUEUES.isEmpty()) return QUEUES;

        Gson gson = new Gson();
        Map<String, List<String>> queues;
        try (FileReader reader = new FileReader(CONFIG)) {
            // noinspection unchecked
            queues = gson.fromJson(reader, Map.class);
        } catch (IOException e) {
            if (!(e instanceof FileNotFoundException)) throw new RuntimeException(e);

            genDefaultQueues();
            createConfig();
            return QUEUES;
        } catch (JsonSyntaxException e) {
            genDefaultQueues();
            LOGGER.warn("Failed loading queues.json, cause: {}. The game will use default queues!", e.toString());
            return QUEUES;
        }

        if (queues.isEmpty()) {
            genDefaultQueues();
            createConfig();
        } else {
            queues.forEach((k, v) -> QUEUES.put(k, v.toArray(String[]::new)));
        }

        return QUEUES;
    }

    private static void genDefaultQueues() {
        QUEUES.put("zh_cn", new String[]{"zh_hk", "zh_tw"});
        QUEUES.put("zh_hk", new String[]{"zh_cn", "zh_tw"});
        QUEUES.put("zh_tw", new String[]{"zh_cn", "zh_hk"});
        QUEUES.put("lzh", new String[]{"zh_cn", "zh_hk", "zh_tw"});
        QUEUES.put("en_nz", new String[]{"en_gb", "en_au"});
        QUEUES.put("en_au", new String[]{"en_gb", "en_nz"});
        QUEUES.put("enws", new String[]{"en_gb"});
        QUEUES.put("enp", new String[]{"en_gb"});
    }

    private static void createConfig() {
        LOGGER.info("Not found {} or it's empty, creating...", CONFIG);

        if (!CONFIG_PATH.toFile().exists() && !CONFIG_PATH.toFile().mkdirs()) {
            throw new RuntimeException("Failed creating config directory(s)");
        }

        if (!CONFIG.exists()) {
            try {
                if (!CONFIG.createNewFile()) LOGGER.warn("Failed creating config file");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter(CONFIG)) {
            gson.toJson(QUEUES, writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
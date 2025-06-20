package cn.racyacid.lpq;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
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

@Mod(value = "lpq", dist = Dist.CLIENT)
public class LanguagePriorityQueue {
    private static final HashMap<String, String[]> QUEUES = HashMap.newHashMap(8);
    private static final Path CONFIG_PATH = Minecraft.getInstance().gameDirectory.toPath().toAbsolutePath().resolve("config").resolve("lpq");
    private static final File CONFIG = CONFIG_PATH.resolve("queues.json").toFile();
    public static final Logger LOGGER = LoggerFactory.getLogger("LPQ");

    @SuppressWarnings("SameReturnValue")
    public static Map<String, String[]> getQueues() {
        if (!QUEUES.isEmpty()) return QUEUES;

        Map<String, String[]> queues = getOrCreateQueuesInConfig();

        if (queues.isEmpty()) {
            genQueuesThenCreateConfig();
        } else {
            QUEUES.putAll(queues);
        }

        return QUEUES;
    }

    private static Map<String, String[]> getOrCreateQueuesInConfig() {
        Gson gson = new Gson();
        Map<String, List<String>> queuesInJson;
        try (FileReader reader = new FileReader(CONFIG)) {
            // noinspection unchecked
            queuesInJson = gson.fromJson(reader, Map.class);
        } catch (IOException e) {
            if (!(e instanceof FileNotFoundException)) throw new RuntimeException(e);

            genQueuesThenCreateConfig();
            return QUEUES;
        } catch (JsonSyntaxException e) {
            generateDefaultQueues();
            LOGGER.warn(String.format("Failed loading queues.json, cause: %s. The game will use default queues!", e));
            return QUEUES;
        }

        Map<String, String[]> queues = HashMap.newHashMap(8);
        queuesInJson.forEach((k, v) -> queues.put(k, v.toArray(String[]::new)));
        return queues;
    }

    private static void genQueuesThenCreateConfig() {
        generateDefaultQueues();
        createConfig();
    }

    private static void generateDefaultQueues() {
        QUEUES.put("zh_cn", new String[]{"zh_hk", "zh_tw"});
        QUEUES.put("zh_hk", new String[]{"zh_cn", "zh_tw"});
        QUEUES.put("zh_tw", new String[]{"zh_cn", "zh_hk"});
        QUEUES.put("lzh", new String[]{"zh_cn", "zh_hk", "zh_tw"});
        QUEUES.put("en_nz", new String[]{"en_gb", "en_au"});
        QUEUES.put("en_au", new String[]{"en_gb", "en_nz"});
        QUEUES.put("fr_fr", new String[]{"fr_ca"});
        QUEUES.put("fr_ca", new String[]{"fr_fr"});
    }

    private static void createConfig() {
        LOGGER.info(String.format("Not found %s or it's empty, creating...", CONFIG));

        createBasicConfigFile();

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter(CONFIG)) {
            gson.toJson(QUEUES, writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void createBasicConfigFile() {
        if (CONFIG.exists()) return;

        if (!CONFIG_PATH.toFile().exists() && !CONFIG_PATH.toFile().mkdirs()) {
            throw new RuntimeException("Failed creating config dir(s)");
        }

        boolean isSuccess;
        try {
            isSuccess = CONFIG.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (!isSuccess) throw new RuntimeException("Failed creating config file");
    }
}
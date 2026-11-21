package dev.minime;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.*;
import java.util.*;

public class Config {
    public String channel = "your_channel";   // канал, чей чат читаем (без #)
    public String twitchNick = "";            // ваш логин Twitch (аккаунт, которому выдан токен)
    public String oauthToken = "";            // OAuth-токен со скоупом chat:read. НИКОМУ не показывайте!
    public String model = "self";             // self | tank | girl | имя из customModels
    public String skin = "own";               // own | имя файла из config/minime/skins (без .png)
    public List<String> customModels = new ArrayList<>();
    public int bubbleSeconds = 8;
    public int maxBubbles = 4;
    public boolean enabled = true;

    static final Path DIR = FabricLoader.getInstance().getConfigDir().resolve("minime");
    static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("minime.json");
    static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static Config load() {
        try {
            Files.createDirectories(DIR.resolve("skins"));
            if (Files.exists(FILE)) {
                Config c = GSON.fromJson(Files.readString(FILE), Config.class);
                if (c != null) return c;
            }
        } catch (Exception e) { e.printStackTrace(); }
        Config c = new Config(); c.save(); return c;
    }
    public void save() {
        try { Files.writeString(FILE, GSON.toJson(this)); } catch (Exception e) { e.printStackTrace(); }
    }
    public List<String> allModels() {
        List<String> l = new ArrayList<>(List.of("self", "tank", "girl")); l.addAll(customModels); return l;
    }
    /** own + все PNG из config/minime/skins */
    public List<String> allSkins() {
        List<String> l = new ArrayList<>(List.of("own"));
        try (var s = Files.list(DIR.resolve("skins"))) {
            s.map(p -> p.getFileName().toString()).filter(n -> n.toLowerCase().endsWith(".png"))
             .map(n -> n.substring(0, n.length() - 4)).sorted().forEach(l::add);
        } catch (Exception ignored) {}
        return l;
    }
}

package dev.minime;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/** Скины для «мини меня»: свой скин или любой 64x64 PNG из config/minime/skins. */
public class SkinManager {
    private static final Map<String, Identifier> CACHE = new HashMap<>();

    public static Identifier resolve(Minecraft mc, String skin) {
        if (skin != null && !skin.equals("own")) {
            Identifier id = CACHE.computeIfAbsent(skin, SkinManager::load);
            if (id != null) return id;
        }
        return ownSkin(mc);
    }

    /** ВНИМАНИЕ: место, которое может потребовать правки под 26.2 (API PlayerSkin). */
    private static Identifier ownSkin(Minecraft mc) {
        return mc.player.getSkin().body().texturePath();
    }

    /** Сбросить кэш (после смены/добавления файлов). */
    public static void clear() {
        var tm = Minecraft.getInstance().getTextureManager();
        CACHE.values().forEach(id -> { if (id != null) tm.release(id); });
        CACHE.clear();
    }

    private static Identifier load(String name) {
        Path f = Config.DIR.resolve("skins").resolve(name + ".png");
        if (!Files.exists(f)) return null;
        try (InputStream in = Files.newInputStream(f)) {
            NativeImage img = NativeImage.read(in);
            Identifier id = Identifier.fromNamespaceAndPath("minime", "skin/" + name.toLowerCase().replaceAll("[^a-z0-9_./-]", "_"));
            Minecraft.getInstance().getTextureManager().register(id, new DynamicTexture(() -> "minime_skin_" + name, img));
            return id;
        } catch (Exception e) { e.printStackTrace(); return null; }
    }
}

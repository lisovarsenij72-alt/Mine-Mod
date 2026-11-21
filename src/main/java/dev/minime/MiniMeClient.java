package dev.minime;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class MiniMeClient implements ClientModInitializer {
    public static Config config;
    public static TwitchChat twitch;
    public static final Identifier HUD_ID = Identifier.fromNamespaceAndPath("minime", "companion");

    @Override
    public void onInitializeClient() {
        config = Config.load();
        twitch = new TwitchChat((nick, text) -> MiniHud.addMessage(nick, text),
                st -> Minecraft.getInstance().execute(() -> msg(Minecraft.getInstance(), st)));
        twitch.connect(config.channel, config.twitchNick, config.oauthToken);

        KeyMapping.Category cat = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("minime", "main"));
        KeyMapping cycle = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.minime.cycle", GLFW.GLFW_KEY_M, cat));
        KeyMapping skinKey = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.minime.skin", GLFW.GLFW_KEY_K, cat));
        KeyMapping toggle = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.minime.toggle", GLFW.GLFW_KEY_N, cat));
        KeyMapping reconnect = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.minime.reconnect", GLFW.GLFW_KEY_J, cat));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (cycle.consumeClick()) {
                List<String> all = config.allModels();
                config.model = all.get((all.indexOf(config.model) + 1) % all.size());
                config.save();
                msg(mc, "Модель: " + config.model);
            }
            while (skinKey.consumeClick()) {
                List<String> all = config.allSkins();
                config.skin = all.get((all.indexOf(config.skin) + 1) % all.size());
                config.save();
                msg(mc, "Скин: " + config.skin);
            }
            while (toggle.consumeClick()) {
                config.enabled = !config.enabled; config.save();
                msg(mc, config.enabled ? "Мини включён" : "Мини выключен");
            }
            while (reconnect.consumeClick()) {
                config = Config.load();
                SkinManager.clear();
                twitch.connect(config.channel, config.twitchNick, config.oauthToken);
            }
        });

        HudElementRegistry.attachElementAfter(VanillaHudElements.CHAT, HUD_ID, new MiniHud());
    }

    private static void msg(Minecraft mc, String s) {
        if (mc.player != null) mc.player.displayClientMessage(Component.literal("[Mini] " + s), true);
    }
}

package dev.minime;

import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/** Чтение чата Twitch через IRC с авторизацией по OAuth-токену (скоуп chat:read). */
public class TwitchChat {
    private volatile Thread thread;
    private volatile boolean running;
    private volatile Socket socket;
    private final BiConsumer<String, String> onMessage;
    private final Consumer<String> onStatus;

    public TwitchChat(BiConsumer<String, String> onMessage, Consumer<String> onStatus) {
        this.onMessage = onMessage; this.onStatus = onStatus;
    }

    public synchronized void connect(String channel, String nick, String token) {
        disconnect();
        if (token == null || token.isBlank() || nick == null || nick.isBlank()) {
            onStatus.accept("Twitch: укажите twitchNick и oauthToken в config/minime.json");
            return;
        }
        if (channel == null || channel.isBlank() || channel.equals("your_channel")) {
            onStatus.accept("Twitch: укажите channel в config/minime.json");
            return;
        }
        String ch = channel.toLowerCase().replace("#", "").trim();
        String nk = nick.toLowerCase().trim();
        String tk = token.trim().replaceFirst("(?i)^oauth:", "");
        running = true;
        thread = new Thread(() -> loop(ch, nk, tk), "minime-twitch");
        thread.setDaemon(true);
        thread.start();
    }

    public synchronized void disconnect() {
        running = false;
        try { if (socket != null) socket.close(); } catch (IOException ignored) {}
    }

    private void loop(String channel, String nick, String token) {
        int delay = 2000;
        while (running) {
            try (Socket s = SSLSocketFactory.getDefault().createSocket("irc.chat.twitch.tv", 6697)) {
                socket = s;
                BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
                Writer out = new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8);
                send(out, "PASS oauth:" + token);
                send(out, "NICK " + nick);
                send(out, "JOIN #" + channel);
                String line;
                while (running && (line = in.readLine()) != null) {
                    if (line.startsWith("PING")) { send(out, "PONG" + line.substring(4)); continue; }
                    if (line.contains("authentication failed") || line.contains("Improperly formatted auth")) {
                        onStatus.accept("Twitch: неверный токен или логин");
                        running = false; return;   // не переподключаемся с плохим токеном
                    }
                    if (line.contains(" 001 ")) { onStatus.accept("Twitch: подключено к #" + channel); delay = 2000; }
                    int p = line.indexOf(" PRIVMSG ");
                    if (p > 0 && line.startsWith(":")) {
                        String user = line.substring(1, line.indexOf('!'));
                        int c = line.indexOf(" :", p);
                        if (c > 0) onMessage.accept(user, line.substring(c + 2));
                    }
                }
            } catch (Exception ignored) {}
            if (!running) return;
            try { Thread.sleep(delay); } catch (InterruptedException e) { return; }
            delay = Math.min(delay * 2, 30000);
        }
    }

    private static void send(Writer w, String s) throws IOException { w.write(s + "\r\n"); w.flush(); }
}

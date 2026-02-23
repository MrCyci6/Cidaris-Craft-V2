package fr.cidaris.craft.manager;

import fr.cidaris.craft.CidarisCraftPlugin;
import fr.cidaris.craft.config.files.MainConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class WebhookManager {

    private final CidarisCraftPlugin plugin;

    public WebhookManager(CidarisCraftPlugin plugin) {
        this.plugin = plugin;
    }

    public void sendUnlockLog(Player player, String craftName, List<String> executedCommands) {
        MainConfig config = plugin.getConfigManager().getConfig(MainConfig.class);

        if (!config.get().getBoolean("webhook.enabled", false)) return;

        String webhookUrl = config.get().getString("webhook.url");
        if (webhookUrl == null || webhookUrl.isEmpty() || webhookUrl.equals("WEBHOOK")) return;

        StringBuilder sb = new StringBuilder();
        sb.append("🔓 **").append(player.getName()).append("** a débloqué : **").append(craftName).append("**\\n");

        if (executedCommands != null && !executedCommands.isEmpty()) {
            sb.append("`Commandes exécutées :`\\n```bash\\n");
            for (String cmd : executedCommands) {
                String cleanCmd = cmd.replace("%player%", player.getName()).replace("%craft%", craftName).replaceAll("§[0-9a-fk-or]", "");
                String safeCmd = cleanCmd.replace("\\", "\\\\").replace("\"", "\\\"");
                sb.append(safeCmd).append("\\n");
            }
            sb.append("```");
        } else {
            sb.append("*(Aucune commande exécutée)*");
        }

        final String finalMessage = sb.toString();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                URL url = new URL(webhookUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("User-Agent", "CidarisCraft-Plugin");
                connection.setDoOutput(true);

                String jsonPayload = "{\"content\": \"" + finalMessage + "\"}";

                try (OutputStream os = connection.getOutputStream()) {
                    os.write(jsonPayload.getBytes(StandardCharsets.UTF_8));
                    os.flush();
                }

                int responseCode = connection.getResponseCode();
                if (responseCode < 200 || responseCode >= 300) {
                    plugin.getLogger().warning("Erreur Webhook Discord. Code : " + responseCode);
                }

                connection.disconnect();
            } catch (Exception e) {
                plugin.getLogger().warning("Impossible d'envoyer le log Webhook : " + e.getMessage());
            }
        });
    }
}
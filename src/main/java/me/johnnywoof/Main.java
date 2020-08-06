package me.johnnywoof;

import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Main extends Plugin implements Listener {

    private String connect_message = null, protocol_name = null;
    private final List<Integer> protocol_id = new ArrayList<>(Collections.emptyList());
    private final List<Integer> do_not_kick = new ArrayList<>(Collections.emptyList());

    @SuppressWarnings("deprecation")
    public void onEnable() {
        this.getProxy().getPluginManager().registerListener(this, this);
        this.saveDefaultConfig();
        try {
            Configuration yml = ConfigurationProvider.getProvider(YamlConfiguration.class).load(this.getConfig());
            this.protocol_name = ChatColor.stripColor(yml.getString("protocol-name", "null"));
            this.connect_message = ChatColor.translateAlternateColorCodes('&', yml.getString("kick-message",
                    "This server has been updated!.newline.Please use the latest minecraft version")
                    .replace("{protocolname}", this.protocol_name)
                    .replaceAll(".newline.", "\n"));
            if ("null".equals(this.protocol_name))
                this.protocol_name = null;
            for(int i = 0; i < yml.getList("protocol-version").size(); i++){
                this.protocol_id.add(Integer.parseInt(yml.getList("protocol-version").get(i).toString()));
            }
            for(int i = 0; i < yml.getList("do-not-kick-protocol").size(); i++){
                this.do_not_kick.add(Integer.parseInt(yml.getList("do-not-kick-protocol").get(i).toString()));
            }
            if (this.protocol_id.isEmpty()) {
                this.protocol_id.add(this.getProxy().getProtocolVersion());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPing(ProxyPingEvent event) {
        ServerPing ping = event.getResponse();
        ServerPing.Protocol protocol = ping.getVersion();

        if (this.protocol_name != null) protocol.setName(this.protocol_name);

        if (!(this.protocol_id.contains(protocol.getProtocol())))
            protocol.setProtocol(-1);

        ping.setVersion(protocol);
        event.setResponse(ping);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLoginEvent(PreLoginEvent event) {
        if (!this.protocol_id.contains(event.getConnection().getVersion())) {
            if(!this.do_not_kick.contains(event.getConnection().getVersion())) {
                event.setCancelReason(TextComponent.fromLegacyText(this.connect_message));
                event.setCancelled(true);
            }
        }
    }

    /**
     * Generates a file object for the config file
     *
     * @return The config file object
     */
    private File getConfig() {
        return new File(this.getDataFolder(), "config.yml");
    }

    /**
     * Saves the default plugin configuration file from the jar
     */
    private void saveDefaultConfig() {
        File configFile = this.getConfig();
        if (!configFile.exists()) {
            if (!this.getDataFolder().exists()) {
                if (!this.getDataFolder().mkdir()) {
                    this.getLogger().warning("Failed to create directory " + this.getDataFolder().getAbsolutePath());
                }
            }
            try {
                if (configFile.createNewFile()) {
                    try (InputStream is = this.getClass().getResourceAsStream("/config.yml");
                         OutputStream os = new FileOutputStream(configFile)) {
                        ByteStreams.copy(is, os);
                    }
                } else {
                    this.getLogger().severe("Failed to create the configuration file!");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

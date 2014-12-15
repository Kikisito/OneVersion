package me.johnnywoof;

import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.event.EventHandler;

import java.io.*;

public class OneVersion extends Plugin implements Listener {

    private String connect_message = null;
    private int protocol_id;

    public void onEnable(){

        this.getProxy().getPluginManager().registerListener(this, this);

        this.saveDefaultConfig();

        try {

            Configuration yml = ConfigurationProvider.getProvider(YamlConfiguration.class).load(this.getConfig());

            this.connect_message = yml.getString("kick-message",
                    "This server has been updated to 1.8!.newline.Please use minecraft 1.8")
                    .replaceAll(".newline.", "\n");

            this.protocol_id = yml.getInt("protocol-version", 0);

        }catch(IOException e){

            e.printStackTrace();

        }

    }

    @EventHandler
    public void onLoginEvent(PreLoginEvent event) {

        if (event.getConnection().getVersion() < this.protocol_id) {

            event.setCancelReason(this.connect_message);
            event.setCancelled(true);

        }

    }

    /**
     *
     * Generates a file object for the config file
     * @return The config file object
     *
     * */
    private File getConfig(){

        return new File(this.getDataFolder() + File.separator + "config.yml");

    }

    /**
     *
     * Saves the default plugin configuration file from the jar
     *
     * */
    private void saveDefaultConfig(){

        if (!this.getDataFolder().exists()) {
            this.getDataFolder().mkdir();
        }

        File configFile = new File(this.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            try {
                if(configFile.createNewFile()) {

                    try (InputStream is = this.getClass().getResourceAsStream("/config.yml");
                         OutputStream os = new FileOutputStream(configFile)) {
                        ByteStreams.copy(is, os);
                    }

                }else{

                    this.getLogger().severe("Failed to create the configuration file!");

                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}
package dev.jordanpg.heehomanhunt;

import dev.jordanpg.heehomanhunt.cmd.CommandHHManhunt;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class HeehoManhunt extends JavaPlugin {
    private final int configVer = 1;

    private ManhuntManager manager;
    private FileConfiguration config = getConfig();

    @Override
    public void onEnable() {
        setupConfig();

        manager = new ManhuntManager(this);

        getServer().getPluginManager().registerEvents(new ManhuntEventListener(manager), this);

        this.getCommand("hhmanhunt").setExecutor(new CommandHHManhunt(manager));
    }

    @Override
    public void onDisable() {

    }

    public Object getConfig(String path)
    {
        return config.get(path);
    }

    public void setConfig(String path, Object value)
    {
        config.set(path, value);
        saveConfig();
    }

    private void setupConfig()
    {
        config.addDefault("version", configVer);
        config.addDefault("runners.locateFortress", true);

        config.options().copyDefaults(true);
        saveConfig();
    }
}

package com.zpedroo.astralhelmet;

import com.codingforcookies.armorequip.ArmorListener;
import com.zpedroo.astralhelmet.commands.MainCmd;
import com.zpedroo.astralhelmet.listeners.PlayerGeneralListeners;
import com.zpedroo.astralhelmet.managers.HelmetManager;
import com.zpedroo.astralhelmet.tasks.HelmetTask;
import com.zpedroo.astralhelmet.utils.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;

public class AstralHelmet extends JavaPlugin {

    private static AstralHelmet instance;
    public static AstralHelmet get() { return instance; }

    public void onEnable() {
        instance = this;
        new FileUtils(this);
        new HelmetManager();
        new HelmetTask(this);

        registerCommand(getConfig().getString("Settings.command"), getConfig().getStringList("Settings.aliases"), new MainCmd());
        registerListeners();
    }

    private void registerCommand(String command, List<String> aliases, CommandExecutor executor) {
        try {
            Constructor<PluginCommand> constructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            constructor.setAccessible(true);

            PluginCommand pluginCmd = constructor.newInstance(command, this);
            pluginCmd.setAliases(aliases);
            pluginCmd.setExecutor(executor);

            Field field = Bukkit.getPluginManager().getClass().getDeclaredField("commandMap");
            field.setAccessible(true);
            CommandMap commandMap = (CommandMap) field.get(Bukkit.getPluginManager());
            commandMap.register(getName().toLowerCase(), pluginCmd);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerGeneralListeners(), this);
        getServer().getPluginManager().registerEvents(new ArmorListener(), this);
    }
}
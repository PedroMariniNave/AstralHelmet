package com.zpedroo.astralhelmet.commands;

import com.zpedroo.astralhelmet.managers.HelmetManager;
import com.zpedroo.astralhelmet.objects.Helmet;
import com.zpedroo.astralhelmet.utils.FileUtils;
import com.zpedroo.astralhelmet.utils.builder.ItemBuilder;
import de.tr7zw.nbtapi.NBTItem;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class MainCmd implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = sender instanceof Player ? (Player) sender : null;

        if (args.length >= 3) {
            CommandKeys key = getKey(args[0].toUpperCase());
            if (key != null) {
                switch (key) {
                    case OXYGEN:
                        if (!sender.hasPermission(FileUtils.get().getString(FileUtils.Files.CONFIG, "Settings.admin-permission"))) break;

                        Player target = Bukkit.getPlayer(args[1]);
                        if (target == null) {
                            sender.sendMessage(FileUtils.get().getColoredString(FileUtils.Files.CONFIG, "Messages.offline-player"));
                            return true;
                        }

                        Integer amount = 0;
                        try {
                            amount = Integer.parseInt(args[2]);
                        } catch (Exception ex) {
                            // ignore
                        }

                        if (amount <= 0) {
                            sender.sendMessage(FileUtils.get().getColoredString(FileUtils.Files.CONFIG, "Messages.invalid-amount"));
                            return true;
                        }

                        ItemStack item = ItemBuilder.build(FileUtils.get().getFile(FileUtils.Files.CONFIG).get(), "Oxygen", new String[]{
                                "{amount}"
                        }, new String[]{
                                amount.toString()
                        }).build();
                        NBTItem nbt = new NBTItem(item);

                        nbt.setInteger("OxygenAmount", amount);
                        target.getInventory().addItem(nbt.getItem());
                        return true;
                }
            }
        }

        if (player == null) return true;

        Helmet helmet = HelmetManager.getInstance().getHelmet(player.getWorld());
        if (helmet == null) {
            sender.sendMessage(FileUtils.get().getColoredString(FileUtils.Files.CONFIG, "Messages.safe-world"));
            return true;
        }

        HelmetManager.getInstance().setHelmet(player, helmet, helmet.getMaxOxygen());
        HelmetManager.getInstance().equip(player, helmet);
        return false;
    }

    private CommandKeys getKey(String str) {
        for (CommandKeys keys : CommandKeys.values()) {
            if (StringUtils.equalsIgnoreCase(keys.getKey(), str)) return keys;
        }

        return null;
    }

    enum CommandKeys {
        OXYGEN(FileUtils.get().getString(FileUtils.Files.CONFIG, "Settings.keys.oxygen"));

        private String key;

        CommandKeys(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }
    }
}
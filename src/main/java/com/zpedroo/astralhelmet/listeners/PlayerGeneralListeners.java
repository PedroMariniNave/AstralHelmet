package com.zpedroo.astralhelmet.listeners;

import com.codingforcookies.armorequip.ArmorEquipEvent;
import com.zpedroo.astralhelmet.managers.HelmetManager;
import com.zpedroo.astralhelmet.objects.Helmet;
import com.zpedroo.astralhelmet.utils.FileUtils;
import com.zpedroo.astralhelmet.utils.builder.ItemBuilder;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerGeneralListeners implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getItem() == null || event.getItem().getType().equals(Material.AIR)) return;

        NBTItem nbt = new NBTItem(event.getItem().clone());
        if (nbt.hasKey("HelmetType")) event.setCancelled(true);
        if (!nbt.hasKey("OxygenAmount")) return;

        event.setCancelled(true);

        Player player = event.getPlayer();
        ItemStack helmetItem = player.getInventory().getHelmet();
        if (helmetItem == null || helmetItem.getType().equals(Material.AIR)) {
            player.sendMessage(FileUtils.get().getColoredString(FileUtils.Files.CONFIG, "Messages.invalid-helmet"));
            return;
        }

        NBTItem helmetNBT = new NBTItem(helmetItem);
        if (!helmetNBT.hasKey("HelmetType")) {
            player.sendMessage(FileUtils.get().getColoredString(FileUtils.Files.CONFIG, "Messages.invalid-helmet"));
            return;
        }

        Helmet helmet = HelmetManager.getInstance().getDataCache().getHelmets().get(helmetNBT.getString("HelmetType").toUpperCase());
        if (helmet == null) return;

        Integer amount = helmetNBT.getInteger("HelmetOxygen");
        if (amount >= helmet.getMaxOxygen()) return;

        Integer toAdd = nbt.getInteger("OxygenAmount");
        Integer newAmount = amount + toAdd;
        Integer overLimit = 0;

        ItemStack item = event.getItem().clone();
        item.setAmount(1);
        player.getInventory().removeItem(item);

        if (newAmount > helmet.getMaxOxygen()) {
            overLimit = newAmount - helmet.getMaxOxygen();

            ItemStack oxygen = ItemBuilder.build(FileUtils.get().getFile(FileUtils.Files.CONFIG).get(), "Oxygen", new String[]{
                    "{amount}"
            }, new String[]{
                    overLimit.toString()
            }).build();
            NBTItem oxygenNBT = new NBTItem(oxygen);

            oxygenNBT.setInteger("OxygenAmount", overLimit);
            player.getInventory().addItem(oxygenNBT.getItem());
        }

        Sound sound = Sound.valueOf(FileUtils.get().getString(FileUtils.Files.CONFIG, "Sounds.fuel.sound"));
        float volume = FileUtils.get().getFloat(FileUtils.Files.CONFIG, "Sounds.fuel.volume");
        float pitch = FileUtils.get().getFloat(FileUtils.Files.CONFIG, "Sounds.fuel.pitch");

        player.getWorld().playSound(player.getLocation(), sound, volume, pitch);
        player.getInventory().setHelmet(helmet.getItem(newAmount - overLimit));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChangeWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        World from = event.getFrom();
        Helmet fromHelmet = HelmetManager.getInstance().getHelmet(from);
        if (fromHelmet != null) {
            HelmetManager.getInstance().remove(player, fromHelmet);
        }

        Helmet helmet = HelmetManager.getInstance().getHelmet(player.getWorld());
        if (helmet == null) return;

        if (!HelmetManager.getInstance().isEquipped(player, helmet)) HelmetManager.getInstance().setHelmet(player, helmet, helmet.getMaxOxygen());
        HelmetManager.getInstance().equip(player, helmet);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEquip(ArmorEquipEvent event) {
        Player player = event.getPlayer();
        NBTItem nbt = null;
        String type = null;
        Helmet helmet = null;
        if (event.getNewArmorPiece() != null && event.getNewArmorPiece().getType() != Material.AIR) {
            nbt = new NBTItem(event.getNewArmorPiece().clone());
            if (!nbt.hasKey("HelmetType")) return;

            type = nbt.getString("HelmetType");
            helmet = HelmetManager.getInstance().getDataCache().getHelmets().get(type.toUpperCase());
            if (helmet == null) return;
            if (!helmet.getWorlds().contains(player.getWorld().getName())) return;

            HelmetManager.getInstance().equip(player, helmet);
        }

        if (event.getOldArmorPiece() != null && event.getOldArmorPiece().getType() != Material.AIR) {
            nbt = new NBTItem(event.getOldArmorPiece().clone());
            if (!nbt.hasKey("HelmetType")) return;

            type = nbt.getString("HelmetType");
            helmet = HelmetManager.getInstance().getDataCache().getHelmets().get(type.toUpperCase());
            if (helmet == null) return;

            HelmetManager.getInstance().remove(player, helmet);
        }
    }
}
package com.zpedroo.astralhelmet.managers;

import com.zpedroo.astralhelmet.AstralHelmet;
import com.zpedroo.astralhelmet.managers.cache.DataCache;
import com.zpedroo.astralhelmet.objects.Helmet;
import com.zpedroo.astralhelmet.utils.FileUtils;
import com.zpedroo.astralhelmet.utils.builder.ItemBuilder;
import de.tr7zw.nbtapi.NBTItem;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class HelmetManager {

    private static HelmetManager instance;
    public static HelmetManager getInstance() { return instance; }

    private DataCache dataCache;

    public HelmetManager() {
        instance = this;
        this.dataCache = new DataCache();
        this.loadHelmets();
    }

    public Helmet getHelmet(World world) {
        for (Helmet helmet : dataCache.getHelmets().values()) {
            if (!helmet.getWorlds().contains(world.getName())) continue;

            return helmet;
        }
        return null;
    }

    public Boolean isEquipped(Player player, Helmet helmet) {
        if (player.getInventory().getHelmet() == null || player.getInventory().getHelmet().getType().equals(Material.AIR)) return false;

        NBTItem nbt = new NBTItem(player.getInventory().getHelmet());
        if (!nbt.hasKey("HelmetType")) return false;

        return StringUtils.equals(nbt.getString("HelmetType"), helmet.getName());
    }

    public void equip(Player player, Helmet helmet) {
        Sound sound = Sound.valueOf(FileUtils.get().getString(FileUtils.Files.CONFIG, "Sounds.equip.sound"));
        float volume = FileUtils.get().getFloat(FileUtils.Files.CONFIG, "Sounds.equip.volume");
        float pitch = FileUtils.get().getFloat(FileUtils.Files.CONFIG, "Sounds.equip.pitch");

        player.getWorld().playSound(player.getLocation(), sound, volume, pitch);

        for (PotionEffect effect : helmet.getEffects()) {
            if (effect == null) continue;

            player.addPotionEffect(effect);
        }
    }

    public void remove(Player player, Helmet helmet) {
        Sound sound = Sound.valueOf(FileUtils.get().getString(FileUtils.Files.CONFIG, "Sounds.unequip.sound"));
        float volume = FileUtils.get().getFloat(FileUtils.Files.CONFIG, "Sounds.unequip.volume");
        float pitch = FileUtils.get().getFloat(FileUtils.Files.CONFIG, "Sounds.unequip.pitch");

        player.getWorld().playSound(player.getLocation(), sound, volume, pitch);

        for (PotionEffect effect : helmet.getEffects()) {
            if (effect == null) continue;

            player.removePotionEffect(effect.getType());
        }
    }

    public void setHelmet(Player player, Helmet helmet, Integer oxygen) {
        if (player.getInventory().getHelmet() != null && player.getInventory().getHelmet().getType() != Material.AIR) {
            boolean given = false;
            for (ItemStack items : player.getInventory().getContents()) {
                if (items != null && items.getType() != Material.AIR) continue;

                player.getInventory().addItem(player.getInventory().getHelmet());
                given = true;
                break;
            }

            if (!given) player.getWorld().dropItem(player.getLocation(), player.getInventory().getHelmet());
        }

        player.getInventory().setHelmet(helmet.getItem(oxygen));
    }

    private void loadHelmets() {
        File folder = new File(AstralHelmet.get().getDataFolder(), "/helmets");
        File[] files = folder.listFiles((f, name) -> name.endsWith(".yml"));
        if (files == null) return;

        for (File fl : files) {
            if (fl == null) continue;

            FileConfiguration file = YamlConfiguration.loadConfiguration(fl);

            Double damage = file.getDouble("Helmet-Settings.damage");
            Integer maxOxygen = file.getInt("Helmet-Settings.oxygen.max");
            Integer oxygenAmount = file.getInt("Helmet-Settings.oxygen.amount");
            List<String> worlds = file.getStringList("Helmet-Settings.worlds");
            List<PotionEffect> effects = new ArrayList<>(file.getStringList("Helmet-Settings.effects").size());
            ItemStack item = ItemBuilder.build(file, "Helmet-Item").build();

            for (String str : file.getStringList("Helmet-Settings.effects")) {
                if (str == null) continue;

                String[] split = str.split(",");

                PotionEffectType potionEffectType = PotionEffectType.getByName(split[0].toUpperCase());
                if (potionEffectType == null) continue;

                int level = Integer.parseInt(split[1]) - 1;
                if (level < 0) continue;

                effects.add(new PotionEffect(potionEffectType, Integer.MAX_VALUE, level));
            }

            String name = file.getName().replace(".yml", "").toUpperCase();
            Helmet helmet = new Helmet(name, damage, maxOxygen, oxygenAmount, worlds, effects, item);

            getDataCache().getHelmets().put(name, helmet);
        }
    }

    public DataCache getDataCache() {
        return dataCache;
    }
}
package com.zpedroo.astralhelmet.tasks;

import com.zpedroo.astralhelmet.AstralHelmet;
import com.zpedroo.astralhelmet.managers.HelmetManager;
import com.zpedroo.astralhelmet.objects.Helmet;
import com.zpedroo.astralhelmet.utils.FileUtils;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;

public class HelmetTask extends BukkitRunnable {

    private final AstralHelmet astralHelmet;
    private final long TASK_DELAY = FileUtils.get().getLong(FileUtils.Files.CONFIG, "Settings.check-interval");

    public HelmetTask(AstralHelmet astralHelmet) {
        this.astralHelmet = astralHelmet;
        this.runTaskTimerAsynchronously(astralHelmet, TASK_DELAY, TASK_DELAY);
    }

    @Override
    public void run() {
        new HashSet<>(Bukkit.getOnlinePlayers()).forEach(player -> {
            if (player == null) return;
            if (player.getGameMode() != GameMode.SURVIVAL) return;

            Helmet helmet = HelmetManager.getInstance().getHelmet(player.getWorld());
            if (helmet == null) return;

            if (!HelmetManager.getInstance().isEquipped(player, helmet)) {
                astralHelmet.getServer().getScheduler().runTaskLater(astralHelmet, () -> player.damage(helmet.getDamage()), 0L);
                return;
            }

            NBTItem nbt = new NBTItem(player.getInventory().getHelmet().clone());
            if (!nbt.hasKey("HelmetOxygen")) return;

            Integer oxygen = nbt.getInteger("HelmetOxygen");
            Integer newOxygen = oxygen - helmet.getOxygenAmount();

            Sound sound = Sound.valueOf(FileUtils.get().getString(FileUtils.Files.CONFIG, "Sounds.breathe.sound"));
            float volume = FileUtils.get().getFloat(FileUtils.Files.CONFIG, "Sounds.breathe.volume");
            float pitch = FileUtils.get().getFloat(FileUtils.Files.CONFIG, "Sounds.breathe.pitch");

            if (newOxygen > 0) {
                player.getInventory().setHelmet(helmet.getItem(newOxygen));
                player.getWorld().playSound(player.getLocation(), sound, volume, pitch);
                return;
            }

            sound = Sound.valueOf(FileUtils.get().getString(FileUtils.Files.CONFIG, "Sounds.destroy.sound"));
            volume = FileUtils.get().getFloat(FileUtils.Files.CONFIG, "Sounds.destroy.volume");
            pitch = FileUtils.get().getFloat(FileUtils.Files.CONFIG, "Sounds.destroy.pitch");

            player.getInventory().setHelmet(new ItemStack(Material.AIR));
            player.getWorld().playSound(player.getLocation(), sound, volume, pitch);
        });
    }
}
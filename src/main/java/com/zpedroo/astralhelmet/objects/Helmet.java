package com.zpedroo.astralhelmet.objects;

import de.tr7zw.nbtapi.NBTItem;
import org.apache.commons.lang.StringUtils;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.List;

public class Helmet {

    private String name;
    private Double damage;
    private Integer maxOxygen;
    private Integer oxygenAmount;
    private List<String> worlds;
    private List<PotionEffect> effects;
    private ItemStack item;

    public Helmet(String name, Double damage, Integer maxOxygen, Integer oxygenAmount, List<String> worlds, List<PotionEffect> effects, ItemStack item) {
        this.name = name;
        this.damage = damage;
        this.maxOxygen = maxOxygen;
        this.oxygenAmount = oxygenAmount;
        this.worlds = worlds;
        this.effects = effects;
        this.item = item;
    }

    public String getName() {
        return name;
    }

    public Double getDamage() {
        return damage;
    }

    public Integer getMaxOxygen() {
        return maxOxygen;
    }

    public Integer getOxygenAmount() {
        return oxygenAmount;
    }

    public List<String> getWorlds() {
        return worlds;
    }

    public List<PotionEffect> getEffects() {
        return effects;
    }

    public ItemStack getItem(Integer oxygen) {
        NBTItem nbt = new NBTItem(item.clone());
        nbt.setInteger("HelmetOxygen", oxygen);
        nbt.setString("HelmetType", name);

        ItemStack item = nbt.getItem();

        if (item.getItemMeta() != null) {
            String displayName = item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : null;
            List<String> lore = item.getItemMeta().hasLore() ? item.getItemMeta().getLore() : null;
            ItemMeta meta = item.getItemMeta();

            if (displayName != null) meta.setDisplayName(StringUtils.replaceEach(displayName, new String[] {
                    "{oxygen}"
            }, new String[] {
                    oxygen.toString()
            }));

            if (lore != null) {
                List<String> newLore = new ArrayList<>(lore.size());

                for (String str : lore) {
                    newLore.add(StringUtils.replaceEach(str, new String[] {
                            "{oxygen}"
                    }, new String[] {
                            oxygen.toString()
                    }));
                }

                meta.setLore(newLore);
            }

            item.setItemMeta(meta);
        }

        return item;
    }
}
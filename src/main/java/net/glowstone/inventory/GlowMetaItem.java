package net.glowstone.inventory;

import com.google.common.base.Strings;
import net.glowstone.util.nbt.CompoundTag;
import net.glowstone.util.nbt.TagType;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * An implementation of {@link ItemMeta}, created through {@link GlowItemFactory}.
 */
class GlowMetaItem implements ItemMeta {

    private String displayName;
    private List<String> lore;
    private Map<Enchantment, Integer> enchants;

    /**
     * Create a GlowMetaItem, copying from another if possible.
     * @param meta The meta to copy from, or null.
     */
    public GlowMetaItem(GlowMetaItem meta) {
        if (meta == null) {
            return;
        }

        displayName = meta.displayName;

        if (meta.hasLore()) {
            this.lore = new ArrayList<>(meta.lore);
        }
        if (meta.hasEnchants()) {
            this.enchants = new HashMap<>(meta.enchants);
        }
    }

    /**
     * Check whether this ItemMeta can be applied to the given material.
     * @param material The Material.
     * @return True if this ItemMeta is applicable.
     */
    public boolean isApplicable(Material material) {
        return material != Material.AIR;
    }

    public ItemMeta clone() {
        try {
            GlowMetaItem clone = (GlowMetaItem) super.clone();
            if (this.lore != null) {
                clone.lore = new ArrayList<>(this.lore);
            }
            if (this.enchants != null) {
                clone.enchants = new HashMap<>(this.enchants);
            }
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new Error(e);
        }
    }

    public Map<String, Object> serialize() {
        Map<String, Object> result = new HashMap<>();
        result.put("meta-type", "UNSPECIFIC");

        if (hasDisplayName()) {
            result.put("display-name", getDisplayName());
        }
        if (hasLore()) {
            result.put("lore", getLore());
        }
        // todo: enchantments

        return result;
    }

    void writeNbt(CompoundTag tag) {
        CompoundTag displayTags = new CompoundTag();
        if (hasDisplayName()) {
            displayTags.putString("Name", getDisplayName());
        }
        if (hasLore()) {
            displayTags.putList("Lore", TagType.STRING, getLore());
        }

        if (!displayTags.isEmpty()) {
            tag.putCompound("display", displayTags);
        }

        // todo: enchantments
    }

    void readNbt(CompoundTag tag) {
        if (tag.isCompound("display")) {
            CompoundTag display = tag.getCompound("display");
            if (display.isString("Name")) {
                setDisplayName(display.getString("Name"));
            }
            if (display.isList("Lore", TagType.STRING)) {
                setLore(display.<String>getList("Lore", TagType.STRING));
            }
        }

        // todo: enchantments
    }

    @Override
    public String toString() {
        Map<String, Object> map = serialize();
        return map.get("meta-type") + "_META:" + map;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Basic properties

    public boolean hasDisplayName() {
        return !Strings.isNullOrEmpty(displayName);
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String name) {
        displayName = name;
    }

    public boolean hasLore() {
        return lore != null && !lore.isEmpty();
    }

    public List<String> getLore() {
        return lore;
    }

    public void setLore(List<String> lore) {
        // todo: fancy validation things
        this.lore = lore;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Enchants

    public boolean hasEnchants() {
        return enchants != null && !enchants.isEmpty();
    }

    public boolean hasEnchant(Enchantment ench) {
        return hasEnchants() && enchants.containsKey(ench);
    }

    public int getEnchantLevel(Enchantment ench) {
        return hasEnchant(ench) ? enchants.get(ench) : 0;
    }

    public Map<Enchantment, Integer> getEnchants() {
        return hasEnchants() ? Collections.unmodifiableMap(enchants) : Collections.<Enchantment, Integer>emptyMap();
    }

    public boolean addEnchant(Enchantment ench, int level, boolean ignoreLevelRestriction) {
        if (enchants == null) {
            enchants = new HashMap<>(4);
        }

        if (ignoreLevelRestriction || level >= ench.getStartLevel() && level <= ench.getMaxLevel()) {
            Integer old = enchants.put(ench, level);
            return old == null || old != level;
        }
        return false;
    }

    public boolean removeEnchant(Enchantment ench) {
        return hasEnchants() && enchants.remove(ench) != null;
    }

    public boolean hasConflictingEnchant(Enchantment ench) {
        return false;
    }
}

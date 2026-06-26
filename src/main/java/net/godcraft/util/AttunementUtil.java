package net.godcraft.util;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import java.util.List;
import java.util.Optional;

public class AttunementUtil {
    public static final List<String> VANILLA_ENCHANTMENT_IDS = List.of(
        "minecraft:protection",
        "minecraft:fire_protection",
        "minecraft:feather_falling",
        "minecraft:blast_protection",
        "minecraft:projectile_protection",
        "minecraft:respiration",
        "minecraft:aqua_affinity",
        "minecraft:thorns",
        "minecraft:depth_strider",
        "minecraft:frost_walker",
        "minecraft:soul_speed",
        "minecraft:swift_sneak",
        "minecraft:binding_curse",
        "minecraft:sharpness",
        "minecraft:smite",
        "minecraft:bane_of_arthropods",
        "minecraft:knockback",
        "minecraft:fire_aspect",
        "minecraft:looting",
        "minecraft:sweeping_edge",
        "minecraft:efficiency",
        "minecraft:silk_touch",
        "minecraft:unbreaking",
        "minecraft:fortune",
        "minecraft:power",
        "minecraft:punch",
        "minecraft:flame",
        "minecraft:infinity",
        "minecraft:luck_of_the_sea",
        "minecraft:lure",
        "minecraft:loyalty",
        "minecraft:impaling",
        "minecraft:riptide",
        "minecraft:channeling",
        "minecraft:multishot",
        "minecraft:quick_charge",
        "minecraft:piercing",
        "minecraft:mending",
        "minecraft:vanishing_curse",
        "minecraft:density",
        "minecraft:breach",
        "minecraft:wind_burst",
        "godcraft:conduction"
    );

    @SuppressWarnings("unchecked")
    public static Holder<Enchantment> getEnchantment(Level level, String id) {
        return level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                .get(ResourceKey.create(Registries.ENCHANTMENT, Identifier.parse(id)))
                .map(ref -> (Holder<Enchantment>) ref)
                .orElse(null);
    }
}

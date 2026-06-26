package net.godcraft.scratch;

import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.core.Holder;

public class TestClass {
    public static String getEnchantmentId(Holder<Enchantment> holder) {
        return holder.unwrapKey().map(key -> key.identifier().toString()).orElse("");
    }
}

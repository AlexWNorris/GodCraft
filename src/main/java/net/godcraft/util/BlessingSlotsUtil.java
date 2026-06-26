package net.godcraft.util;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public class BlessingSlotsUtil {
    public static boolean isDiamondOrNetheriteArmor(ItemStack stack) {
        if (stack.isEmpty()) return false;
        Item item = stack.getItem();
        return item == Items.DIAMOND_HELMET || item == Items.DIAMOND_CHESTPLATE ||
               item == Items.DIAMOND_LEGGINGS || item == Items.DIAMOND_BOOTS ||
               item == Items.NETHERITE_HELMET || item == Items.NETHERITE_CHESTPLATE ||
               item == Items.NETHERITE_LEGGINGS || item == Items.NETHERITE_BOOTS;
    }

    public static boolean isDiamondOrNetheriteToolOrWeapon(ItemStack stack) {
        if (stack.isEmpty()) return false;
        Item item = stack.getItem();
        return item == Items.DIAMOND_SWORD || item == Items.DIAMOND_PICKAXE ||
               item == Items.DIAMOND_AXE || item == Items.DIAMOND_SHOVEL ||
               item == Items.DIAMOND_HOE ||
               item == Items.NETHERITE_SWORD || item == Items.NETHERITE_PICKAXE ||
               item == Items.NETHERITE_AXE || item == Items.NETHERITE_SHOVEL ||
               item == Items.NETHERITE_HOE;
    }

    public static int getMaxMajorSlots(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        Item item = stack.getItem();
        if (item == Items.DIAMOND_CHESTPLATE || item == Items.NETHERITE_CHESTPLATE) {
            return 1;
        }
        return 0;
    }

    public static int getMaxMinorSlots(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        if (isDiamondOrNetheriteArmor(stack) || isDiamondOrNetheriteToolOrWeapon(stack)) {
            return 1;
        }
        return 0;
    }

    public static boolean canHaveBlessings(ItemStack stack) {
        return getMaxMajorSlots(stack) > 0 || getMaxMinorSlots(stack) > 0;
    }
}

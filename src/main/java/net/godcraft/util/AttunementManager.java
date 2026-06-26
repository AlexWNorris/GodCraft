package net.godcraft.util;

import net.godcraft.ModDataComponents;
import net.godcraft.attachment.ModAttachments;
import net.godcraft.attachment.PlayerAttunements;
import net.godcraft.component.AppliedAttunements;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import java.util.*;

public class AttunementManager {
    private static final EquipmentSlot[] ACTIVE_SLOTS = {
        EquipmentSlot.MAINHAND,
        EquipmentSlot.OFFHAND,
        EquipmentSlot.HEAD,
        EquipmentSlot.CHEST,
        EquipmentSlot.LEGS,
        EquipmentSlot.FEET
    };

    /** Called every player tick on the server to dynamically apply/remove attunement enchantments. */
    public static void tickPlayer(Player player) {
        if (player.level().isClientSide()) return;

        PlayerAttunements attunementData = player.getData(ModAttachments.ATTUNEMENTS.get());
        Set<String> equipped = attunementData.equipped();

        // 1. Gather all equipped attunements as Holders
        List<Holder<Enchantment>> equippedHolders = new ArrayList<>();
        for (String id : equipped) {
            Holder<Enchantment> holder = AttunementUtil.getEnchantment(player.level(), id);
            if (holder != null) {
                equippedHolders.add(holder);
            }
        }

        // Keep track of which ItemStack instances in active slots we processed
        Set<ItemStack> activeStacks = new HashSet<>();

        // 2. Scan active slots and apply attunements
        for (EquipmentSlot slot : ACTIVE_SLOTS) {
            ItemStack stack = player.getItemBySlot(slot);
            if (stack.isEmpty()) continue;

            activeStacks.add(stack);
            applyToStack(stack, player.level(), equippedHolders);
        }

        // 3. Scan the rest of the player's inventory and clean up any stale attunements
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEmpty() || activeStacks.contains(stack)) continue;

            if (stack.has(ModDataComponents.APPLIED_ATTUNEMENTS.get())) {
                cleanStack(stack);
            }
        }
    }

    /**
     * Applies the given equipped attunement holders to an ItemStack.
     * Only compatible, item-applicable enchantments are added.
     * Previously applied attunements are tracked in the APPLIED_ATTUNEMENTS component
     * so permanent enchantments are preserved.
     */
    public static void applyToStack(ItemStack stack, Level level, List<Holder<Enchantment>> equippedHolders) {
        ItemEnchantments currentEnchants = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        AppliedAttunements applied = stack.getOrDefault(ModDataComponents.APPLIED_ATTUNEMENTS.get(), AppliedAttunements.EMPTY);

        // Determine the set of permanent enchantments (current minus previously applied attunements)
        Map<Holder<Enchantment>, Integer> permanentEnchants = new HashMap<>();
        for (var entry : currentEnchants.entrySet()) {
            String name = entry.getKey().unwrapKey()
                    .map(k -> k.identifier().toString())
                    .orElse("");
            if (!applied.ids().contains(name)) {
                permanentEnchants.put(entry.getKey(), entry.getValue());
            }
        }

        // Determine which equipped attunements are compatible with this stack
        List<Holder<Enchantment>> selectedHolders = new ArrayList<>();
        List<String> selectedIds = new ArrayList<>();

        for (Holder<Enchantment> attunement : equippedHolders) {
            // Skip if item doesn't support this enchantment type
            if (!attunement.value().canEnchant(stack)) {
                continue;
            }

            // Skip if conflicts with any permanent enchantment
            boolean conflicts = false;
            for (Holder<Enchantment> perm : permanentEnchants.keySet()) {
                if (!Enchantment.areCompatible(attunement, perm)) {
                    conflicts = true;
                    break;
                }
            }
            if (conflicts) continue;

            // Skip if conflicts with an already-selected attunement
            for (Holder<Enchantment> sel : selectedHolders) {
                if (!Enchantment.areCompatible(attunement, sel)) {
                    conflicts = true;
                    break;
                }
            }
            if (conflicts) continue;

            selectedHolders.add(attunement);
            selectedIds.add(attunement.unwrapKey()
                    .map(k -> k.identifier().toString())
                    .orElse(""));
        }

        // If applied IDs match what we'd apply now, skip the update
        if (new HashSet<>(selectedIds).equals(new HashSet<>(applied.ids()))) {
            return;
        }

        // Rebuild the enchantments component: permanent first, then selected attunements
        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        for (var entry : permanentEnchants.entrySet()) {
            mutable.set(entry.getKey(), entry.getValue());
        }
        for (Holder<Enchantment> attunement : selectedHolders) {
            int permLevel = mutable.getLevel(attunement);
            int attLevel = attunement.value().getMaxLevel();
            mutable.set(attunement, Math.max(permLevel, attLevel));
        }

        stack.set(DataComponents.ENCHANTMENTS, mutable.toImmutable());
        if (selectedIds.isEmpty()) {
            stack.remove(ModDataComponents.APPLIED_ATTUNEMENTS.get());
        } else {
            stack.set(ModDataComponents.APPLIED_ATTUNEMENTS.get(), new AppliedAttunements(selectedIds));
        }
    }

    /**
     * Removes all temporarily applied attunement enchantments from a stack,
     * preserving only permanent enchantments.
     */
    public static void cleanStack(ItemStack stack) {
        ItemEnchantments currentEnchants = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        AppliedAttunements applied = stack.getOrDefault(ModDataComponents.APPLIED_ATTUNEMENTS.get(), AppliedAttunements.EMPTY);

        if (applied.isEmpty()) {
            stack.remove(ModDataComponents.APPLIED_ATTUNEMENTS.get());
            return;
        }

        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        for (var entry : currentEnchants.entrySet()) {
            String name = entry.getKey().unwrapKey()
                    .map(k -> k.identifier().toString())
                    .orElse("");
            if (!applied.ids().contains(name)) {
                mutable.set(entry.getKey(), entry.getValue());
            }
        }

        stack.set(DataComponents.ENCHANTMENTS, mutable.toImmutable());
        stack.remove(ModDataComponents.APPLIED_ATTUNEMENTS.get());
    }

    /** Cleans active slot items before player data is saved to disk on logout. */
    public static void onPlayerLoggedOut(Player player) {
        if (player.level().isClientSide()) return;
        for (EquipmentSlot slot : ACTIVE_SLOTS) {
            ItemStack stack = player.getItemBySlot(slot);
            if (!stack.isEmpty() && stack.has(ModDataComponents.APPLIED_ATTUNEMENTS.get())) {
                cleanStack(stack);
            }
        }
    }

    /** Cleans dropped item entities that may carry temporary attunement enchantments. */
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof ItemEntity itemEntity) {
            ItemStack stack = itemEntity.getItem();
            if (stack.has(ModDataComponents.APPLIED_ATTUNEMENTS.get())) {
                ItemStack cleanCopy = stack.copy();
                cleanStack(cleanCopy);
                itemEntity.setItem(cleanCopy);
            }
        }
    }
}

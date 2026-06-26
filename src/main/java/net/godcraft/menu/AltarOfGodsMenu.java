package net.godcraft.menu;

import net.godcraft.ModDataComponents;
import net.godcraft.ModMenuTypes;
import net.godcraft.component.BlessingSlots;
import net.godcraft.item.BlessingItem;
import net.godcraft.item.BlessingType;
import net.godcraft.util.BlessingSlotsUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import net.minecraft.core.Holder;
import net.minecraft.world.item.enchantment.Enchantment;
import net.godcraft.util.AttunementUtil;
import net.godcraft.attachment.PlayerAttunements;

public class AltarOfGodsMenu extends AbstractContainerMenu {
    private final ContainerLevelAccess access;
    private final Player player;
    private final DataSlot activeTab = DataSlot.standalone();
    private final DataSlot unlocked0 = DataSlot.standalone();
    private final DataSlot unlocked1 = DataSlot.standalone();
    private final DataSlot equipped0 = DataSlot.standalone();
    private final DataSlot equipped1 = DataSlot.standalone();

    private final Container container = new SimpleContainer(3) {
        @Override
        public void setChanged() {
            super.setChanged();
            AltarOfGodsMenu.this.slotsChanged(this);
        }
    };
    
    private boolean isUpdating = false;
    private ItemStack lastGear = ItemStack.EMPTY;

    public AltarOfGodsMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, ContainerLevelAccess.NULL);
    }

    public AltarOfGodsMenu(int containerId, Inventory playerInventory, ContainerLevelAccess access) {
        super(ModMenuTypes.ALTAR_OF_GODS.get(), containerId);
        this.access = access;
        this.player = playerInventory.player;

        // Register data slots for syncing
        this.activeTab.set(0);
        this.addDataSlot(activeTab);
        this.addDataSlot(unlocked0);
        this.addDataSlot(unlocked1);
        this.addDataSlot(equipped0);
        this.addDataSlot(equipped1);

        // Slot 0: Gear Input (Blessings tab)
        this.addSlot(new Slot(this.container, 0, 80, 20) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return BlessingSlotsUtil.canHaveBlessings(stack);
            }
            
            @Override
            public int getMaxStackSize() {
                return 1;
            }

            @Override
            public boolean isActive() {
                return AltarOfGodsMenu.this.getActiveTab() == 0;
            }
        });

        // Slot 1: Major Blessing
        this.addSlot(new Slot(this.container, 1, 56, 20) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                ItemStack gear = AltarOfGodsMenu.this.container.getItem(0);
                if (BlessingSlotsUtil.getMaxMajorSlots(gear) <= 0) return false;
                return stack.getItem() instanceof BlessingItem blessing && blessing.getBlessingType() == BlessingType.MAJOR;
            }

            @Override
            public boolean isActive() {
                if (AltarOfGodsMenu.this.getActiveTab() != 0) return false;
                ItemStack gear = AltarOfGodsMenu.this.container.getItem(0);
                return BlessingSlotsUtil.getMaxMajorSlots(gear) > 0;
            }
            
            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        // Slot 2: Minor Blessing / Attunement
        this.addSlot(new Slot(this.container, 2, 104, 20) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                ItemStack gear = AltarOfGodsMenu.this.container.getItem(0);
                if (BlessingSlotsUtil.getMaxMinorSlots(gear) <= 0) return false;
                if (!(stack.getItem() instanceof BlessingItem blessing)) return false;
                return blessing.getBlessingType() == BlessingType.MINOR || blessing.getBlessingType() == BlessingType.ATTUNEMENT;
            }

            @Override
            public boolean isActive() {
                if (AltarOfGodsMenu.this.getActiveTab() != 0) return false;
                ItemStack gear = AltarOfGodsMenu.this.container.getItem(0);
                return BlessingSlotsUtil.getMaxMinorSlots(gear) > 0;
            }
            
            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        // Player Inventory Slots (rows 0-2)
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18) {
                    @Override
                    public boolean isActive() {
                        return AltarOfGodsMenu.this.getActiveTab() == 0;
                    }
                });
            }
        }

        // Player Hotbar
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142) {
                @Override
                public boolean isActive() {
                    return AltarOfGodsMenu.this.getActiveTab() == 0;
                }
            });
        }
    }

    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
        if (this.isUpdating) return;

        this.isUpdating = true;
        try {
            if (container == this.container) {
                ItemStack currentGear = this.container.getItem(0);
                if (!ItemStack.matches(currentGear, lastGear)) {
                    // Gear in Slot 0 changed
                    this.lastGear = currentGear.copy();
                    if (currentGear.isEmpty() || !BlessingSlotsUtil.canHaveBlessings(currentGear)) {
                        this.container.setItem(1, ItemStack.EMPTY);
                        this.container.setItem(2, ItemStack.EMPTY);
                    } else {
                        BlessingSlots blessingSlots = currentGear.getOrDefault(ModDataComponents.BLESSING_SLOTS.get(), BlessingSlots.EMPTY);
                        
                        // Sync Major Blessing
                        if (BlessingSlotsUtil.getMaxMajorSlots(currentGear) > 0 && !blessingSlots.majorBlessing().isEmpty()) {
                            Item item = BuiltInRegistries.ITEM.get(Identifier.parse(blessingSlots.majorBlessing())).map(ref -> ref.value()).orElse(null);
                            this.container.setItem(1, item != null ? new ItemStack(item) : ItemStack.EMPTY);
                        } else {
                            this.container.setItem(1, ItemStack.EMPTY);
                        }

                        // Sync Minor Blessing
                        if (BlessingSlotsUtil.getMaxMinorSlots(currentGear) > 0 && !blessingSlots.minorBlessings().isEmpty()) {
                            String blessingId = blessingSlots.minorBlessings().get(0);
                            Item item = BuiltInRegistries.ITEM.get(Identifier.parse(blessingId)).map(ref -> ref.value()).orElse(null);
                            this.container.setItem(2, item != null ? new ItemStack(item) : ItemStack.EMPTY);
                        } else {
                            this.container.setItem(2, ItemStack.EMPTY);
                        }
                    }
                } else {
                    // Slot 1 or 2 changed
                    if (!currentGear.isEmpty() && BlessingSlotsUtil.canHaveBlessings(currentGear)) {
                        ItemStack majorStack = this.container.getItem(1);
                        ItemStack minorStack = this.container.getItem(2);

                        String majorBlessing = "";
                        if (!majorStack.isEmpty() && majorStack.getItem() instanceof BlessingItem majorItem) {
                            majorBlessing = majorItem.getBlessingId();
                        }

                        List<String> minorBlessings = new ArrayList<>();
                        if (!minorStack.isEmpty() && minorStack.getItem() instanceof BlessingItem minorItem) {
                            minorBlessings.add(minorItem.getBlessingId());
                        }

                        BlessingSlots newSlots = new BlessingSlots(majorBlessing, minorBlessings);
                        if (newSlots.isEmpty()) {
                            currentGear.remove(ModDataComponents.BLESSING_SLOTS.get());
                        } else {
                            currentGear.set(ModDataComponents.BLESSING_SLOTS.get(), newSlots);
                        }
                        this.lastGear = currentGear.copy();
                    }
                }
            }
        } finally {
            this.isUpdating = false;
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, net.godcraft.GodCraft.ALTAR_OF_GODS.get());
    }

    @Override
    public void broadcastChanges() {
        if (this.player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            PlayerAttunements current = serverPlayer.getData(net.godcraft.attachment.ModAttachments.ATTUNEMENTS.get());
            int u0 = 0, u1 = 0, e0 = 0, e1 = 0;
            for (int i = 0; i < AttunementUtil.VANILLA_ENCHANTMENT_IDS.size(); i++) {
                String id = AttunementUtil.VANILLA_ENCHANTMENT_IDS.get(i);
                boolean isUnlocked = current.unlocked().contains(id);
                boolean isEquipped = current.equipped().contains(id);
                if (i < 32) {
                    if (isUnlocked) u0 |= (1 << i);
                    if (isEquipped) e0 |= (1 << i);
                } else {
                    int bit = i - 32;
                    if (isUnlocked) u1 |= (1 << bit);
                    if (isEquipped) e1 |= (1 << bit);
                }
            }
            this.unlocked0.set(u0);
            this.unlocked1.set(u1);
            this.equipped0.set(e0);
            this.equipped1.set(e1);
        }
        super.broadcastChanges();
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        if (buttonId == 0) {
            this.activeTab.set(0);
            return true;
        } else if (buttonId == 1) {
            this.activeTab.set(1);
            return true;
        } else if (buttonId >= 100) {
            int index = buttonId - 100;
            if (index >= 0 && index < AttunementUtil.VANILLA_ENCHANTMENT_IDS.size()) {
                String enchantmentId = AttunementUtil.VANILLA_ENCHANTMENT_IDS.get(index);
                if (!player.level().isClientSide() && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                    PlayerAttunements current = serverPlayer.getData(net.godcraft.attachment.ModAttachments.ATTUNEMENTS.get());
                    Set<String> unlocked = new HashSet<>(current.unlocked());
                    Set<String> equipped = new HashSet<>(current.equipped());

                    if (unlocked.contains(enchantmentId)) {
                        if (equipped.contains(enchantmentId)) {
                            equipped.remove(enchantmentId);
                        } else {
                            // Check conflicts with currently equipped attunements
                            Holder<Enchantment> newEnch = AttunementUtil.getEnchantment(player.level(), enchantmentId);
                            if (newEnch != null) {
                                List<String> toRemove = new ArrayList<>();
                                for (String activeId : equipped) {
                                    Holder<Enchantment> activeEnch = AttunementUtil.getEnchantment(player.level(), activeId);
                                    if (activeEnch != null && !Enchantment.areCompatible(newEnch, activeEnch)) {
                                        toRemove.add(activeId);
                                    }
                                }
                                equipped.removeAll(toRemove);
                                equipped.add(enchantmentId);
                            }
                        }
                        serverPlayer.setData(net.godcraft.attachment.ModAttachments.ATTUNEMENTS.get(), new PlayerAttunements(unlocked, equipped));
                    }
                }
            }
            return true;
        }
        return false;
    }

    public int getActiveTab() {
        return this.activeTab.get();
    }

    public boolean isAttunementUnlocked(int index) {
        if (index < 0 || index >= AttunementUtil.VANILLA_ENCHANTMENT_IDS.size()) return false;
        if (index < 32) {
            return (this.unlocked0.get() & (1 << index)) != 0;
        } else {
            return (this.unlocked1.get() & (1 << (index - 32))) != 0;
        }
    }

    public boolean isAttunementEquipped(int index) {
        if (index < 0 || index >= AttunementUtil.VANILLA_ENCHANTMENT_IDS.size()) return false;
        if (index < 32) {
            return (this.equipped0.get() & (1 << index)) != 0;
        } else {
            return (this.equipped1.get() & (1 << (index - 32))) != 0;
        }
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.access.execute((level, pos) -> {
            this.clearContainer(player, this.container);
        });
    }

    @Override
    protected void clearContainer(Player player, Container container) {
        // Only return the item in Slot 0
        if (!player.isAlive() || player instanceof net.minecraft.server.level.ServerPlayer && ((net.minecraft.server.level.ServerPlayer)player).hasDisconnected()) {
            player.drop(container.removeItemNoUpdate(0), false);
        } else {
            player.getInventory().placeItemBackInInventory(container.removeItemNoUpdate(0));
        }
        // Discard virtual display stacks in 1 and 2
        container.removeItemNoUpdate(1);
        container.removeItemNoUpdate(2);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index < 3) {
                // Move from Altar slots to Player Inventory
                if (!this.moveItemStackTo(itemstack1, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Move from Player Inventory to Altar slots
                if (BlessingSlotsUtil.canHaveBlessings(itemstack1)) {
                    if (!this.moveItemStackTo(itemstack1, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (itemstack1.getItem() instanceof BlessingItem blessing) {
                    if (blessing.getBlessingType() == BlessingType.MAJOR) {
                        if (!this.moveItemStackTo(itemstack1, 1, 2, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else {
                        if (!this.moveItemStackTo(itemstack1, 2, 3, false)) {
                            return ItemStack.EMPTY;
                        }
                    }
                } else {
                    return ItemStack.EMPTY;
                }
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, itemstack1);
        }

        return itemstack;
    }
}

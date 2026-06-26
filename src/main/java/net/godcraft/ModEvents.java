package net.godcraft;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.npc.villager.VillagerTrades;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.util.RandomSource;
import net.minecraft.util.TriState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.godcraft.ModDataComponents;
import net.godcraft.component.BlessingSlots;
import net.godcraft.util.BlessingSlotsUtil;
import net.godcraft.util.AttunementManager;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import java.util.List;

@EventBusSubscriber(modid = GodCraft.MODID)
public class ModEvents {

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        AttunementManager.tickPlayer(event.getEntity());
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        AttunementManager.onEntityJoinLevel(event);
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        AttunementManager.onPlayerLoggedOut(event.getEntity());
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().getBlockState(event.getPos()).is(Blocks.ENCHANTING_TABLE)) {
            event.setUseBlock(TriState.FALSE);
            event.setUseItem(TriState.FALSE);
            if (!event.getLevel().isClientSide() && event.getEntity() != null) {
                event.getEntity().displayClientMessage(Component.literal("The gods have forbidden the use of Enchanting Tables."), false);
            }
        }
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getPlacedBlock().is(Blocks.ENCHANTING_TABLE)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onVillagerTrades(VillagerTradesEvent event) {
        if (event.getType() == VillagerProfession.LIBRARIAN) {
            var trades = event.getTrades();
            trades.forEach((level, list) -> {
                list.removeIf(trade -> {
                    if (trade instanceof VillagerTrades.EnchantBookForEmeralds) {
                        return true;
                    }
                    try {
                        MerchantOffer offer = trade.getOffer(null, null, RandomSource.create(42L));
                        if (offer != null && offer.getResult().is(Items.ENCHANTED_BOOK)) {
                            return true;
                        }
                    } catch (Exception e) {
                        String className = trade.getClass().getName();
                        if (className.contains("EnchantBook") || className.contains("EnchantedBook") || className.contains("BookForEmeralds")) {
                            return true;
                        }
                    }
                    return false;
                });
            });
        }
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (BlessingSlotsUtil.canHaveBlessings(stack)) {
            BlessingSlots blessings = stack.getOrDefault(ModDataComponents.BLESSING_SLOTS.get(), BlessingSlots.EMPTY);
            List<Component> tooltips = event.getToolTip();
            tooltips.add(Component.empty());
            tooltips.add(Component.translatable("tooltip.godcraft.blessing_slots_title").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
            
            int maxMajor = BlessingSlotsUtil.getMaxMajorSlots(stack);
            int maxMinor = BlessingSlotsUtil.getMaxMinorSlots(stack);
            
            if (maxMajor > 0) {
                if (!blessings.majorBlessing().isEmpty()) {
                    Item item = BuiltInRegistries.ITEM.get(Identifier.parse(blessings.majorBlessing())).map(ref -> ref.value()).orElse(null);
                    Component blessingName = item != null ? item.getName(new ItemStack(item)) : Component.literal(blessings.majorBlessing());
                    tooltips.add(Component.translatable("tooltip.godcraft.major_slot_equipped", blessingName).withStyle(ChatFormatting.YELLOW));
                } else {
                    tooltips.add(Component.translatable("tooltip.godcraft.major_slot_empty").withStyle(ChatFormatting.GRAY));
                }
            }
            
            if (maxMinor > 0) {
                if (!blessings.minorBlessings().isEmpty()) {
                    for (String id : blessings.minorBlessings()) {
                        Item item = BuiltInRegistries.ITEM.get(Identifier.parse(id)).map(ref -> ref.value()).orElse(null);
                        Component blessingName = item != null ? item.getName(new ItemStack(item)) : Component.literal(id);
                        tooltips.add(Component.translatable("tooltip.godcraft.minor_slot_equipped", blessingName).withStyle(ChatFormatting.AQUA));
                    }
                } else {
                    tooltips.add(Component.translatable("tooltip.godcraft.minor_slot_empty").withStyle(ChatFormatting.GRAY));
                }
            }
        }
    }
}

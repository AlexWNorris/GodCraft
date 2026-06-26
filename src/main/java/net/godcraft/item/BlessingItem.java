package net.godcraft.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import java.util.function.Consumer;

public class BlessingItem extends Item {
    private final BlessingType blessingType;
    private final String blessingId;

    public BlessingItem(Properties properties, BlessingType blessingType, String blessingId) {
        super(properties);
        this.blessingType = blessingType;
        this.blessingId = blessingId;
    }

    public BlessingType getBlessingType() {
        return blessingType;
    }

    public String getBlessingId() {
        return blessingId;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        ChatFormatting color = ChatFormatting.GRAY;
        String tierName = "";
        
        switch (blessingType) {
            case MAJOR -> {
                color = ChatFormatting.GOLD;
                tierName = "Major Blessing";
            }
            case MINOR -> {
                color = ChatFormatting.AQUA;
                tierName = "Minor Blessing";
            }
            case ATTUNEMENT -> {
                color = ChatFormatting.DARK_GREEN;
                tierName = "Attunement";
            }
        }
        
        tooltipComponents.accept(Component.literal(tierName).withStyle(color, ChatFormatting.ITALIC));
        tooltipComponents.accept(Component.translatable("tooltip.godcraft.blessing_id", blessingId).withStyle(ChatFormatting.DARK_GRAY));
        
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
    }
}

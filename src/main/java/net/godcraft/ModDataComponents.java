package net.godcraft;

import net.godcraft.component.AppliedAttunements;
import net.godcraft.component.BlessingSlots;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, GodCraft.MODID);

    public static final Supplier<DataComponentType<BlessingSlots>> BLESSING_SLOTS =
            DATA_COMPONENT_TYPES.register("blessing_slots", () ->
                    DataComponentType.<BlessingSlots>builder()
                            .persistent(BlessingSlots.CODEC)
                            .networkSynchronized(BlessingSlots.STREAM_CODEC)
                            .build()
            );

    public static final Supplier<DataComponentType<AppliedAttunements>> APPLIED_ATTUNEMENTS =
            DATA_COMPONENT_TYPES.register("applied_attunements", () ->
                    DataComponentType.<AppliedAttunements>builder()
                            .persistent(AppliedAttunements.CODEC)
                            .networkSynchronized(AppliedAttunements.STREAM_CODEC)
                            .build()
            );

    public static void register(IEventBus eventBus) {
        DATA_COMPONENT_TYPES.register(eventBus);
    }
}

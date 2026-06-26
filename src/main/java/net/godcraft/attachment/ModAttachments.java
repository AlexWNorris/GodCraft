package net.godcraft.attachment;

import net.godcraft.GodCraft;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import java.util.function.Supplier;

public class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, GodCraft.MODID);

    public static final Supplier<AttachmentType<PlayerAttunements>> ATTUNEMENTS = ATTACHMENT_TYPES.register(
            "player_attunements",
            () -> AttachmentType.builder(() -> PlayerAttunements.EMPTY)
                    .serialize(PlayerAttunements.CODEC)
                    .copyOnDeath()
                    .build()
    );

    public static void register(IEventBus eventBus) {
        ATTACHMENT_TYPES.register(eventBus);
    }
}

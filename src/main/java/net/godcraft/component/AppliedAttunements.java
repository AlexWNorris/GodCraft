package net.godcraft.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import java.util.List;

public record AppliedAttunements(List<String> ids) {
    public static final AppliedAttunements EMPTY = new AppliedAttunements(List.of());

    public static final Codec<AppliedAttunements> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.STRING.listOf().fieldOf("ids").forGetter(AppliedAttunements::ids)
        ).apply(instance, AppliedAttunements::new)
    );

    public static final StreamCodec<io.netty.buffer.ByteBuf, AppliedAttunements> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    public boolean isEmpty() {
        return ids == null || ids.isEmpty();
    }
}

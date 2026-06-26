package net.godcraft.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import java.util.List;

public record BlessingSlots(String majorBlessing, List<String> minorBlessings) {
    public static final BlessingSlots EMPTY = new BlessingSlots("", List.of());

    public static final Codec<BlessingSlots> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.optionalFieldOf("majorBlessing", "").forGetter(BlessingSlots::majorBlessing),
        Codec.STRING.listOf().optionalFieldOf("minorBlessings", List.of()).forGetter(BlessingSlots::minorBlessings)
    ).apply(instance, BlessingSlots::new));

    public static final StreamCodec<io.netty.buffer.ByteBuf, BlessingSlots> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    public boolean isEmpty() {
        return (majorBlessing == null || majorBlessing.isEmpty()) && (minorBlessings == null || minorBlessings.isEmpty());
    }
}

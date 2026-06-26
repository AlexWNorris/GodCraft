package net.godcraft.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public record PlayerAttunements(Set<String> unlocked, Set<String> equipped) {
    public static final PlayerAttunements EMPTY = new PlayerAttunements(new HashSet<>(), new HashSet<>());

    public static final MapCodec<PlayerAttunements> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            Codec.STRING.listOf().xmap(list -> (Set<String>) new HashSet<>(list), List::copyOf).fieldOf("unlocked").forGetter(PlayerAttunements::unlocked),
            Codec.STRING.listOf().xmap(list -> (Set<String>) new HashSet<>(list), List::copyOf).fieldOf("equipped").forGetter(PlayerAttunements::equipped)
        ).apply(instance, PlayerAttunements::new)
    );
    
    public PlayerAttunements copy() {
        return new PlayerAttunements(new HashSet<>(unlocked), new HashSet<>(equipped));
    }
}

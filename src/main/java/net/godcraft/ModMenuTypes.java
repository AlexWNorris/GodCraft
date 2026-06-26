package net.godcraft;

import net.godcraft.menu.AltarOfGodsMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(Registries.MENU, GodCraft.MODID);

    public static final Supplier<MenuType<AltarOfGodsMenu>> ALTAR_OF_GODS =
            MENU_TYPES.register("altar_of_the_gods", () ->
                    new MenuType<>(AltarOfGodsMenu::new, FeatureFlags.DEFAULT_FLAGS)
            );

    public static void register(IEventBus eventBus) {
        MENU_TYPES.register(eventBus);
    }
}

package elucent.eidolon.registries;

import elucent.eidolon.api.ritual.IncenseRitual;
import elucent.eidolon.common.incense.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static elucent.eidolon.Eidolon.prefix;

public class IncenseRegistry {

    static final Map<ResourceLocation, Supplier<IncenseRitual>> incenses = new HashMap<>();
    static final Map<Item, ResourceLocation> itemToIncense = new HashMap<>();
    public static final ResourceLocation PRAYER_INCENSE = prefix("prayer_incense");
    public static final ResourceLocation TOUGH_INCENSE = prefix("tough_incense");
    public static final ResourceLocation GLOOM_INCENSE = prefix("gloom_incense");
    ;
    public static final ResourceLocation RESTORATION_INCENSE = prefix("restoration_incense");
    public static final ResourceLocation DEATH_BANE_INCENSE = prefix("death_bane_incense");

    public static void register(Item incense, ResourceLocation name, Supplier<IncenseRitual> incenseRitualSupplier) {
        assert name != null;
        itemToIncense.put(incense, name);
        incenses.put(name, incenseRitualSupplier);
    }

    public static IncenseRitual getIncenseRitual(Item item) {
        if (!itemToIncense.containsKey(item)) return null;
        return getIncenseRitual(itemToIncense.get(item));
    }

    public static IncenseRitual getIncenseRitual(ResourceLocation name) {
        return incenses.get(name).get();
    }

    public static void init() {
        register(Registry.OFFERING_INCENSE.get(), PRAYER_INCENSE, () -> new PrayerIncense(PRAYER_INCENSE));
        register(Registry.ENDER_CALX.get(), TOUGH_INCENSE, () -> new ToughIncense(TOUGH_INCENSE));
        register(Registry.DEATH_ESSENCE.get(), GLOOM_INCENSE, () -> new GloomIncense(GLOOM_INCENSE));
        register(Items.GOLDEN_APPLE, RESTORATION_INCENSE, () -> new RestorationIncense(RESTORATION_INCENSE));
        register(Registry.SILVER_NUGGET.get(), DEATH_BANE_INCENSE, () -> new DeathBaneIncense(DEATH_BANE_INCENSE));
    }

}

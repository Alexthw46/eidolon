package elucent.eidolon.compat.apotheosis;

import elucent.eidolon.Eidolon;
import elucent.eidolon.common.item.WandItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import shadows.apotheosis.adventure.affix.AffixManager;
import shadows.apotheosis.adventure.loot.LootCategory;
import shadows.apotheosis.adventure.loot.LootRarity;
import shadows.apotheosis.ench.asm.EnchHooks;

public class Apotheosis {
    public static final LootCategory WAND = LootCategory.register(LootCategory.SWORD, "wand", itemStack -> itemStack.getItem() instanceof WandItem, toArray(EquipmentSlot.MAINHAND));

    public static boolean isTreasureOnly(final Enchantment enchantment) {
        return EnchHooks.isTreasureOnly(enchantment);
    }

    public static int getMaxLevel(final Enchantment enchantment) {
        return EnchHooks.getMaxLevel(enchantment);
    }

    public static int rarityToNumber(final LootRarity rarity) {
        return LootRarity.LIST.indexOf(rarity);
    }

    public static int affixToAmount(final LootRarity affixRarity, final LootRarity minRarity) {
        // This way the amount starts at the minimum rarity with 1
        return 1 + Apotheosis.rarityToNumber(affixRarity) - Apotheosis.rarityToNumber(minRarity);
    }

    private static EquipmentSlot[] toArray(final EquipmentSlot... equipmentSlots) {
        return equipmentSlots;
    }

    public static void initialize() {
        AffixManager.INSTANCE.registerSerializer(new ResourceLocation(Eidolon.MODID, "tracking"), TrackingAffix.SERIALIZER);
        AffixManager.INSTANCE.registerSerializer(new ResourceLocation(Eidolon.MODID, "hailing"), HailingAffix.SERIALIZER);
    }
}
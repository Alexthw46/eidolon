package elucent.eidolon.registries;

import elucent.eidolon.Eidolon;
import elucent.eidolon.capability.*;
import elucent.eidolon.client.particle.*;
import elucent.eidolon.common.block.CandleBlock;
import elucent.eidolon.common.block.*;
import elucent.eidolon.common.item.Tiers;
import elucent.eidolon.common.item.*;
import elucent.eidolon.common.item.curio.*;
import elucent.eidolon.common.tile.*;
import elucent.eidolon.common.world.EidolonAbstractTreeFeature;
import elucent.eidolon.gui.ResearchTableContainer;
import elucent.eidolon.gui.SoulEnchanterContainer;
import elucent.eidolon.gui.WoodenBrewingStandContainer;
import elucent.eidolon.gui.WorktableContainer;
import elucent.eidolon.util.DamageTypeData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageEffects;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.item.*;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

import static net.minecraft.world.level.block.state.properties.WoodType.register;

@SuppressWarnings({"unused", "DataFlowIssue"})
public class Registry {

    public static final TagKey<Item> ILLWOOD_LOGS = ItemTags.create(new ResourceLocation(Eidolon.MODID, "illwood_logs"));
    public static TagKey<Item>
            INGOTS_LEAD = ItemTags.create(new ResourceLocation("forge", "ingots/lead"));
    public static TagKey<Item> INGOTS_PEWTER = ItemTags.create(new ResourceLocation("forge", "ingots/pewter"));
    public static TagKey<Item> INGOTS_ARCANE_GOLD = ItemTags.create(new ResourceLocation("forge", "ingots/arcane_gold"));
    public static final TagKey<Item> INGOTS_SILVER = ItemTags.create(new ResourceLocation("forge", "ingots/silver"));
    public static TagKey<Item> GEMS_SHADOW = ItemTags.create(new ResourceLocation("forge", "gems/shadow_gem"));
    public static final TagKey<Item> ZOMBIE_FOOD_TAG = ItemTags.create(new ResourceLocation(Eidolon.MODID, "zombie_food"));

    public static TagKey<Block> CRUCIBLE_HOT_BLOCKS = BlockTags.create(new ResourceLocation(Eidolon.MODID, "crucible_hot_blocks"));
    public static TagKey<Block> PLANTER_PLANTS = BlockTags.create(new ResourceLocation(Eidolon.MODID, "planter_plants"));

    public static TagKey<DamageType> FORGE_MAGIC = TagKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("forge:is_magic"));
    public static TagKey<DamageType> FORGE_WITHER = TagKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("forge:is_wither"));


    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Eidolon.MODID);

    static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Eidolon.MODID);
    static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Eidolon.MODID);
    static final DeferredRegister<BlockEntityType<?>> TILE_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Eidolon.MODID);
    static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, Eidolon.MODID);

    static final DeferredRegister<ArgumentTypeInfo<?, ?>> ARG_TYPES = DeferredRegister.create(ForgeRegistries.COMMAND_ARGUMENT_TYPES, Eidolon.MODID);

    static Item.Properties itemProps() {
        return new Item.Properties();
    }

    public static final RegistryObject<CreativeModeTab> TAB = TABS.register("general", () -> CreativeModeTab.builder().icon(Registry.SHADOW_GEM.get()::getDefaultInstance).title(Component.translatable("itemGroup.eidolon"))
            .displayItems((params, output) -> {
                for (var entry : ITEMS.getEntries()) {
                    output.accept(entry.get().getDefaultInstance());
                }
            }).build());

    static BlockBehaviour.Properties blockProps(Block mat, DyeColor color) {
        return BlockBehaviour.Properties.copy(mat).mapColor(color);
    }

    static BlockBehaviour.Properties blockProps(Block mat) {
        return BlockBehaviour.Properties.copy(mat);
    }

    private static Boolean allowsSpawnOnLeaves(BlockState state, BlockGetter reader, BlockPos pos, EntityType<?> entity) {
        return entity == EntityType.OCELOT || entity == EntityType.PARROT;
    }

    private static boolean isntSolid(BlockState state, BlockGetter reader, BlockPos pos) {
        return false;
    }

    static RegistryObject<Item> addItem(String name) {
        return addItem(name, itemProps());
    }

    static RegistryObject<Item> addItem(String name, Item.Properties props) {
        return addItem(name, () -> new Item(props));
    }

    static RegistryObject<Item> addItem(String name, Supplier<Item> item) {
        return ITEMS.register(name, item);
    }

    static RegistryObject<Block> addBlock(String name, Block.Properties props) {
        Supplier<Block> b = () -> new Block(props);
        var block = BLOCKS.register(name, b);
        ITEMS.register(name, () -> new BlockItem(block.get(), itemProps()));
        return block;
    }

    static <T extends Block> RegistryObject<T> addBlock(String name, Supplier<T> b) {
        var block = BLOCKS.register(name, b);
        ITEMS.register(name, () -> new BlockItem(block.get(), itemProps()));
        return block;
    }

    public static final WoodType ILLWOOD = register(new WoodType("eidolon:illwood", BlockSetType.DARK_OAK));
    public static final WoodType POLISHED = register(new WoodType("eidolon:polished", BlockSetType.DARK_OAK));

    static <T extends AbstractContainerMenu> RegistryObject<MenuType<T>> addContainer(String name, MenuType.MenuSupplier<T> factory) {
        return CONTAINERS.register(name, () -> new MenuType<>(factory, FeatureFlags.VANILLA_SET));
    }

    public static final RegistryObject<Item>
            LEAD_INGOT = addItem("lead_ingot");
    public static final RegistryObject<Item> RAW_LEAD = addItem("raw_lead");
    public static final RegistryObject<Item> LEAD_NUGGET = addItem("lead_nugget");
    public static final RegistryObject<Item> SILVER_INGOT = addItem("silver_ingot");
    public static final RegistryObject<Item> RAW_SILVER = addItem("raw_silver");
    public static final RegistryObject<Item> SILVER_NUGGET = addItem("silver_nugget");
    public static final RegistryObject<Item> PEWTER_BLEND = addItem("pewter_blend");
    public static final RegistryObject<Item> PEWTER_INGOT = addItem("pewter_ingot");
    public static final RegistryObject<Item> PEWTER_NUGGET = addItem("pewter_nugget");
    public static final RegistryObject<Item> PEWTER_INLAY = addItem("pewter_inlay");
    public static final RegistryObject<Item> ARCANE_GOLD_INGOT = addItem("arcane_gold_ingot");
    public static final RegistryObject<Item> ARCANE_GOLD_NUGGET = addItem("arcane_gold_nugget");
    public static final RegistryObject<Item> ELDER_BRICK = addItem("elder_brick");
    public static final RegistryObject<Item> OFFERING_INCENSE = addItem("offering_incense");
    public static final RegistryObject<Item> SULFUR = addItem("sulfur");
    public static final RegistryObject<Item> GOLD_INLAY = addItem("gold_inlay");
    public static final RegistryObject<Item> ZOMBIE_HEART = addItem("zombie_heart", () -> new ItemBase(itemProps().rarity(Rarity.UNCOMMON).food(
            new FoodProperties.Builder()
                    .nutrition(2).saturationMod(1.5f)
                    .effect(() -> new MobEffectInstance(MobEffects.HUNGER, 1800), 0.875f)
                    .effect(() -> new MobEffectInstance(MobEffects.POISON, 900, 1), 1.0f)
                    .build())).setLore("lore.eidolon.zombie_heart"));
    public static final RegistryObject<Item> TATTERED_CLOTH = addItem("tattered_cloth");
    public static final RegistryObject<Item> WRAITH_HEART = addItem("wraith_heart", () -> new ItemBase(itemProps()
            .rarity(Rarity.UNCOMMON)).setLore("lore.eidolon.wraith_heart"));
    public static final RegistryObject<Item> TOP_HAT = addItem("top_hat", () -> new TopHatItem(itemProps().stacksTo(1).rarity(Rarity.EPIC)).setLore("lore.eidolon.top_hat"));
    public static final RegistryObject<Item> BASIC_RING = addItem("basic_ring", () -> new BasicRingItem(itemProps().stacksTo(1)));
    public static final RegistryObject<Item> BASIC_AMULET = addItem("basic_amulet", () -> new BasicAmuletItem(itemProps().stacksTo(1)));
    public static final RegistryObject<Item> BASIC_BELT = addItem("basic_belt", () -> new BasicBeltItem(itemProps().stacksTo(1)));
    public static final RegistryObject<Item> CODEX = addItem("codex", () -> new CodexItem(itemProps().stacksTo(1).rarity(Rarity.UNCOMMON)).setLore("lore.eidolon.codex"));
    public static final RegistryObject<Item> SOUL_SHARD = addItem("soul_shard");
    public static final RegistryObject<Item> DEATH_ESSENCE = addItem("death_essence");
    public static final RegistryObject<Item> CRIMSON_ESSENCE = addItem("crimson_essence");
    public static final RegistryObject<Item> CRIMSON_GEM = addItem("crimson_gem");

    public static final RegistryObject<Item> FUNGUS_SPROUTS = addItem("fungus_sprouts", itemProps().food(new FoodProperties.Builder().nutrition(2).saturationMod(0.1f).build()));
    public static final RegistryObject<Item> WARPED_SPROUTS = addItem("warped_sprouts", itemProps().food(new FoodProperties.Builder().nutrition(4).saturationMod(0.6f).effect(() -> new MobEffectInstance(EidolonPotions.ANCHORED_EFFECT.get(), 900), 1).build()));
    public static final RegistryObject<Item> ENDER_CALX = addItem("ender_calx");
    public static final RegistryObject<Item> TALLOW = addItem("tallow");
    public static final RegistryObject<Item> LESSER_SOUL_GEM = addItem("lesser_soul_gem");
    public static final RegistryObject<Item> UNHOLY_SYMBOL = addItem("unholy_symbol", () -> new TheurgySymbolItem(itemProps().rarity(Rarity.UNCOMMON).stacksTo(1)).setLore("lore.eidolon.unholy_symbol"));
    public static final RegistryObject<Item> HOLY_SYMBOL = addItem("holy_symbol", () -> new TheurgySymbolItem(itemProps().rarity(Rarity.UNCOMMON).stacksTo(1)).setLore("lore.eidolon.holy_symbol"));

    public static final RegistryObject<Item> REAPER_SCYTHE = addItem("reaper_scythe", () -> new ReaperScytheItem(itemProps().rarity(Rarity.UNCOMMON))
            .setLore("lore.eidolon.reaper_scythe"));
    public static final RegistryObject<Item> CLEAVING_AXE = addItem("cleaving_axe", () -> new CleavingAxeItem(itemProps().rarity(Rarity.UNCOMMON))
            .setLore("lore.eidolon.cleaving_axe"));
    public static final RegistryObject<Item> SHADOW_GEM = addItem("shadow_gem");
    public static final RegistryObject<Item> WICKED_WEAVE = addItem("wicked_weave");
    public static final RegistryObject<Item> WARLOCK_HAT = addItem("warlock_hat", () -> new WarlockRobesItem(Type.HELMET, itemProps()));
    public static final RegistryObject<Item> WARLOCK_CLOAK = addItem("warlock_cloak", () -> new WarlockRobesItem(Type.CHESTPLATE, itemProps()));
    public static final RegistryObject<Item> WARLOCK_BOOTS = addItem("warlock_boots", () -> new WarlockRobesItem(Type.BOOTS, itemProps()));
    public static final RegistryObject<Item> SILVER_HELMET = addItem("silver_helmet", () -> new SilverArmorItem(Type.HELMET, itemProps()));
    public static final RegistryObject<Item> SILVER_CHESTPLATE = addItem("silver_chestplate", () -> new SilverArmorItem(Type.CHESTPLATE, itemProps()));
    public static final RegistryObject<Item> SILVER_LEGGINGS = addItem("silver_leggings", () -> new SilverArmorItem(Type.LEGGINGS, itemProps()));
    public static final RegistryObject<Item> SILVER_BOOTS = addItem("silver_boots", () -> new SilverArmorItem(Type.BOOTS, itemProps()));
    public static final RegistryObject<Item> SILVER_SWORD = addItem("silver_sword", () -> new SwordItem(Tiers.SilverTier.INSTANCE, 3, -2.4f, itemProps()) {

                @Override
                public boolean hurtEnemy(@NotNull ItemStack pStack, @NotNull LivingEntity pTarget, @NotNull LivingEntity pAttacker) {
                    if (pTarget.getMobType() == MobType.UNDEAD) {
                        pTarget.setSecondsOnFire(5);
                    }
                    return super.hurtEnemy(pStack, pTarget, pAttacker);
                }
            }
    );
    public static final RegistryObject<Item> SILVER_PICKAXE = addItem("silver_pickaxe", () -> new PickaxeItem(Tiers.SilverTier.INSTANCE, 1, -2.4f, itemProps()));
    public static final RegistryObject<Item> SILVER_AXE = addItem("silver_axe", () -> new AxeItem(Tiers.SilverTier.INSTANCE, 6, -2.4f, itemProps()));
    public static final RegistryObject<Item> SILVER_SHOVEL = addItem("silver_shovel", () -> new ShovelItem(Tiers.SilverTier.INSTANCE, 1.5f, -2.4f, itemProps()));
    public static final RegistryObject<Item> SILVER_HOE = addItem("silver_hoe", () -> new HoeItem(Tiers.SilverTier.INSTANCE, 0, -2.4f, itemProps()));
    public static final RegistryObject<Item> ATHAME = addItem("athame", () -> new AthameItem(itemProps().stacksTo(1)));
    public static final RegistryObject<Item> REVERSAL_PICK = addItem("reversal_pick", () -> new ReversalPickItem(itemProps()
            .rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> VOID_AMULET = addItem("void_amulet", () -> new VoidAmuletItem(itemProps()
            .rarity(Rarity.UNCOMMON).stacksTo(1)).setLore("lore.eidolon.void_amulet"));
    public static final RegistryObject<Item> WARDED_MAIL = addItem("warded_mail", () -> new WardedMailItem(itemProps()
            .rarity(Rarity.UNCOMMON).stacksTo(1)).setLore("lore.eidolon.warded_mail"));
    public static final RegistryObject<Item> SAPPING_SWORD = addItem("sapping_sword", () -> new SappingSwordItem(itemProps()
            .rarity(Rarity.UNCOMMON).stacksTo(1)).setLore("lore.eidolon.sapping_sword"));
    public static final RegistryObject<Item> SANGUINE_AMULET = addItem("sanguine_amulet", () -> new SanguineAmuletItem(itemProps()
            .rarity(Rarity.UNCOMMON).stacksTo(1)).setLore("lore.eidolon.sanguine_amulet"));
    public static final RegistryObject<Item> ENERVATING_RING = addItem("enervating_ring", () -> new EnervatingRingItem(itemProps()
            .rarity(Rarity.UNCOMMON).stacksTo(1)).setLore("lore.eidolon.enervating_ring"));
    public static final RegistryObject<Item> SOULFIRE_WAND = addItem("soulfire_wand", () -> new SoulfireWandItem(itemProps()
            .rarity(Rarity.UNCOMMON).stacksTo(1).durability(253).setNoRepair())
            .setLore("lore.eidolon.soulfire_wand"));
    public static final RegistryObject<Item> BONECHILL_WAND = addItem("bonechill_wand", () -> new BonechillWandItem(itemProps()
            .rarity(Rarity.UNCOMMON).stacksTo(1).durability(253).setNoRepair())
            .setLore("lore.eidolon.bonechill_wand"));
    public static final RegistryObject<Item> GRAVITY_BELT = addItem("gravity_belt", () -> new GravityBeltItem(itemProps()
            .rarity(Rarity.UNCOMMON).stacksTo(1)).setLore("lore.eidolon.gravity_belt"));
    public static final RegistryObject<Item> RESOLUTE_BELT = addItem("resolute_belt", () -> new ResoluteBeltItem(itemProps()
            .rarity(Rarity.UNCOMMON).stacksTo(1)).setLore("lore.eidolon.resolute_belt"));
    public static final RegistryObject<Item> PRESTIGIOUS_PALM = addItem("prestigious_palm", () -> new PrestigiousPalmItem(itemProps()
            .rarity(Rarity.UNCOMMON).stacksTo(1)).setLore("lore.eidolon.prestigious_palm"));
    public static final RegistryObject<Item> MIND_SHIELDING_PLATE = addItem("mind_shielding_plate", () -> new MindShieldingPlateItem(itemProps()
            .rarity(Rarity.UNCOMMON).stacksTo(1)).setLore("lore.eidolon.mind_shielding_plate"));
    public static final RegistryObject<Item> GLASS_HAND = addItem("glass_hand", () -> new GlassHandItem(itemProps()
            .rarity(Rarity.RARE).stacksTo(1)).setLore("lore.eidolon.glass_hand"));
    public static final RegistryObject<Item> TERMINUS_MIRROR = addItem("terminus_mirror", () -> new TerminusMirrorItem(itemProps()
            .rarity(Rarity.RARE).stacksTo(1)).setLore("lore.eidolon.terminus_mirror"));
    public static final RegistryObject<Item> ANGELS_SIGHT = addItem("angels_sight", () -> new AngelSightItem(itemProps()
            .rarity(Rarity.RARE).stacksTo(1)).setLore("lore.eidolon.angels_sight"));
    public static final RegistryObject<Item> WITHERED_HEART = addItem("withered_heart", () -> new ItemBase(itemProps().rarity(Rarity.RARE).food(
            new FoodProperties.Builder()
                    .nutrition(2).saturationMod(1.5f)
                    .effect(() -> new MobEffectInstance(MobEffects.HUNGER, 1800), 0.875f)
                    .effect(() -> new MobEffectInstance(MobEffects.WITHER, 900, 1), 1.0f)
                    .build())).setLore("lore.eidolon.withered_heart"));
    public static final RegistryObject<Item> IMBUED_BONES = addItem("imbued_bones", itemProps().rarity(Rarity.UNCOMMON));
    public static final RegistryObject<Item> SUMMONING_STAFF = addItem("summoning_staff", () -> new SummoningStaffItem(itemProps().rarity(Rarity.RARE).stacksTo(1)));
    public static final RegistryObject<Item> DEATHBRINGER_SCYTHE = addItem("deathbringer_scythe", () -> new DeathbringerScytheItem(itemProps().rarity(Rarity.RARE).stacksTo(1))
            .setLore("lore.eidolon.deathbringer_scythe"));
    public static final RegistryObject<Item> SOULBONE_AMULET = addItem("soulbone_amulet", () -> new SoulboneAmuletItem(itemProps()
            .rarity(Rarity.RARE).stacksTo(1)).setLore("lore.eidolon.soulbone_amulet"));
    public static final RegistryObject<Item> BONELORD_HELM = addItem("bonelord_helm", () -> new BonelordArmorItem(Type.HELMET, itemProps().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> BONELORD_CHESTPLATE = addItem("bonelord_chestplate", () -> new BonelordArmorItem(Type.CHESTPLATE, itemProps().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> BONELORD_GREAVES = addItem("bonelord_greaves", () -> new BonelordArmorItem(Type.LEGGINGS, itemProps().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> PAROUSIA_DISC = addItem("music_disc_parousia", () -> new RecordItem(9, EidolonSounds.PAROUSIA,
            itemProps().stacksTo(1).rarity(Rarity.RARE), 3680));
    public static final RegistryObject<Item> RAVEN_FEATHER = addItem("raven_feather");
    public static final RegistryObject<Item> RAVEN_CLOAK = addItem("raven_cloak", () -> new RavenCloakItem(itemProps().rarity(Rarity.RARE).stacksTo(1)));
    public static final RegistryObject<Item> MERAMMER_RESIN = addItem("merammer_resin");
    public static final RegistryObject<Item> MAGIC_INK = addItem("magic_ink");
    public static final RegistryObject<Item> MAGICIANS_WAX = addItem("magicians_wax");
    public static final RegistryObject<Item> ARCANE_SEAL = addItem("arcane_seal");
    public static final RegistryObject<Item> PARCHMENT = addItem("parchment");
    public static final RegistryObject<Item> NOTETAKING_TOOLS = addItem("notetaking_tools", () -> new NotetakingToolsItem(itemProps().stacksTo(16)));
    public static final RegistryObject<Item> RESEARCH_NOTES = addItem("research_notes", () -> new ResearchNotesItem(itemProps().rarity(Rarity.UNCOMMON).stacksTo(1)));
    public static final RegistryObject<Item> COMPLETED_RESEARCH = addItem("completed_research", () -> new CompletedResearchItem(itemProps().rarity(Rarity.UNCOMMON).stacksTo(1)));
    public static final RegistryObject<Item> RED_CANDY = addItem("red_candy", () -> new ItemBase(itemProps().rarity(Rarity.COMMON).food(
            new FoodProperties.Builder()
                    .nutrition(2).saturationMod(2)
                    .build())).setLore(ChatFormatting.RED, "lore.eidolon.red_candy"));
    public static final RegistryObject<Item> GRAPE_CANDY;

    static {
        GRAPE_CANDY = addItem("grape_candy", () -> new ItemBase(itemProps().rarity(Rarity.COMMON).food(
                new FoodProperties.Builder()
                        .nutrition(2).saturationMod(2)
                        .build())).setLore(ChatFormatting.LIGHT_PURPLE, "lore.eidolon.grape_candy"));
    }

    public static final RegistryObject<Block>
            LEAD_ORE = addBlock("lead_ore", blockProps(Blocks.STONE)
            .sound(SoundType.STONE).strength(2.8f, 3.0f).requiresCorrectToolForDrops());
    public static final RegistryObject<Block> DEEP_LEAD_ORE = addBlock("deep_lead_ore", blockProps(Blocks.STONE)
            .sound(SoundType.DEEPSLATE).strength(3.2f, 3.0f).requiresCorrectToolForDrops());
    public static final RegistryObject<Block> LEAD_BLOCK = addBlock("lead_block", blockProps(Blocks.STONE, DyeColor.PURPLE)
            .sound(SoundType.METAL).strength(3.0f, 3.0f).requiresCorrectToolForDrops());
    public static final RegistryObject<Block> RAW_LEAD_BLOCK = addBlock("raw_lead_block", blockProps(Blocks.STONE, DyeColor.PURPLE)
            .sound(SoundType.DEEPSLATE).strength(2.4f, 3.0f).requiresCorrectToolForDrops());
    public static final RegistryObject<Block> SILVER_ORE = addBlock("silver_ore", blockProps(Blocks.STONE)
            .sound(SoundType.STONE).strength(3.2f, 3.0f).requiresCorrectToolForDrops());
    public static final RegistryObject<Block> DEEP_SILVER_ORE = addBlock("deep_silver_ore", blockProps(Blocks.STONE)
            .sound(SoundType.DEEPSLATE).strength(3.6f, 3.0f).requiresCorrectToolForDrops());
    public static final RegistryObject<Block> SILVER_BLOCK = addBlock("silver_block", blockProps(Blocks.STONE, DyeColor.LIGHT_BLUE)
            .sound(SoundType.METAL).strength(3.0f, 3.0f).requiresCorrectToolForDrops());
    public static final RegistryObject<Block> RAW_SILVER_BLOCK = addBlock("raw_silver_block", blockProps(Blocks.STONE, DyeColor.LIGHT_BLUE)
            .sound(SoundType.STONE).strength(2.4f, 3.0f).requiresCorrectToolForDrops());
    public static final RegistryObject<Block> PEWTER_BLOCK = addBlock("pewter_block", blockProps(Blocks.STONE, DyeColor.LIGHT_GRAY)
            .sound(SoundType.METAL).strength(4.0f, 4.0f).requiresCorrectToolForDrops());
    public static final RegistryObject<Block> ARCANE_GOLD_BLOCK = addBlock("arcane_gold_block", blockProps(Blocks.STONE, DyeColor.YELLOW)
            .sound(SoundType.METAL).strength(3.0f, 4.0f).requiresCorrectToolForDrops());
    public static final RegistryObject<Block> SHADOW_GEM_BLOCK = addBlock("shadow_gem_block", blockProps(Blocks.STONE, DyeColor.PURPLE)
            .sound(SoundType.METAL).strength(3.0f, 4.0f).requiresCorrectToolForDrops());
    public static final RegistryObject<Block> WOODEN_ALTAR = addBlock("wooden_altar", () -> new TableBlockBase(blockProps(Blocks.OAK_WOOD)
            .sound(SoundType.WOOD).strength(1.6f, 3.0f)));
    public static final RegistryObject<Block> STONE_ALTAR = addBlock("stone_altar", () -> new TableBlockBase(blockProps(Blocks.STONE)
            .sound(SoundType.STONE).strength(2.8f, 3.0f)
            .requiresCorrectToolForDrops().noOcclusion())
            .setMainShape(Shapes.or(
                    Shapes.box(0, 0.375, 0, 1, 1, 1),
                    Shapes.box(0.0625, 0.125, 0.0625, 0.9375, 0.375, 0.9375)
            )));
    public static final RegistryObject<Block> CANDLE = addBlock("candle", () -> new CandleBlock(blockProps(Blocks.CANDLE, DyeColor.WHITE)
            .sound(SoundType.STONE).lightLevel(state -> 15).strength(0.6f, 0.8f).noOcclusion()));
    public static final RegistryObject<Block> CANDLESTICK = addBlock("candlestick", () -> new CandlestickBlock(blockProps(Blocks.CANDLE, DyeColor.YELLOW)
            .sound(SoundType.STONE).lightLevel(state -> 15).strength(1.2f, 2.0f).noOcclusion()));
    public static final RegistryObject<Block> MAGIC_CANDLE = addBlock("magic_candle", () -> new CandleBlock(blockProps(Blocks.CANDLE, DyeColor.RED)
            .sound(SoundType.STONE).lightLevel(state -> 15).strength(0.6f, 0.8f).noOcclusion()));
    public static final RegistryObject<Block> MAGIC_CANDLESTICK = addBlock("magic_candlestick", () -> new CandlestickBlock(blockProps(Blocks.CANDLE, DyeColor.YELLOW)
            .sound(SoundType.STONE).lightLevel(state -> 15).strength(1.2f, 2.0f).noOcclusion()));
    public static final RegistryObject<Block> STRAW_EFFIGY = addBlock("straw_effigy", () -> new EffigyBlock(blockProps(Blocks.HAY_BLOCK, DyeColor.YELLOW)
            .sound(SoundType.WOOD).strength(1.4f, 2.0f)
            .noOcclusion()).setShape(
            Shapes.box(0.28125, 0, 0.28125, 0.71875, 1, 0.71875)
    ));

    public static final Supplier<BlockBehaviour.Properties> GOLD_DUMMY = () -> BlockBehaviour.Properties.of().mapColor(MapColor.GOLD).instrument(NoteBlockInstrument.BELL).strength(3.0F, 6.0F).sound(SoundType.METAL);
    public static final RegistryObject<Block> CENSER = addBlock("censer", () -> new IncenseBurnerBlock(GOLD_DUMMY.get()
            .strength(1.4f, 2.0f)
            .noOcclusion()).setShape(Shapes.box(0.3125, 0, 0.3125, 0.6875, 0.5, 0.6875)));
    public static final RegistryObject<Block> GOBLET = addBlock("goblet", () -> new GobletBlock(GOLD_DUMMY.get()
            .strength(1.4f, 2.0f)
            .noOcclusion()).setShape(Shapes.box(0.3125, 0, 0.3125, 0.6875, 0.5, 0.6875)));
    public static final RegistryObject<Block> ELDER_EFFIGY = addBlock("unholy_effigy", () -> new EffigyBlock(blockProps(Blocks.STONE)
            .sound(SoundType.STONE).strength(2.8f, 3.0f)
            .requiresCorrectToolForDrops()
            .noOcclusion()).setShape(
            Shapes.box(0.25, 0, 0.25, 0.75, 1, 0.75)
    ));
    public static final RegistryObject<Block> WORKTABLE = addBlock("worktable", () -> new WorktableBlock(blockProps(Blocks.OAK_WOOD)
            .sound(SoundType.WOOD).strength(1.6f, 3.0f)
            .noOcclusion()).setShape(Shapes.or(
            Shapes.box(0, 0, 0, 1, 0.25, 1),
            Shapes.box(0.125, 0.25, 0.125, 0.875, 0.625, 0.875),
            Shapes.box(0, 0.625, 0, 1, 1, 1)
    )));
    public static final RegistryObject<Block> RESEARCH_TABLE = addBlock("research_table", () -> new ResearchTableBlock(blockProps(Blocks.OAK_WOOD)
            .sound(SoundType.WOOD).strength(1.6f, 3.0f)
            .noOcclusion()).setShape(Shapes.or(
            Shapes.box(0, 0, 0, 1, 0.25, 1),
            Shapes.box(0.125, 0.25, 0.125, 0.875, 0.625, 0.875),
            Shapes.box(0, 0.625, 0, 1, 1, 1)
    )));
    public static final RegistryObject<Block> PLINTH = addBlock("plinth", () -> new PillarBlockBase(blockProps(Blocks.STONE)
            .sound(SoundType.STONE).strength(2.0f, 3.0f)
            .requiresCorrectToolForDrops().noOcclusion())
            .setShape(Shapes.box(0.25, 0, 0.25, 0.75, 1, 0.75)));
    public static final RegistryObject<Block> OBELISK = addBlock("obelisk", () -> new PillarBlockBase(blockProps(Blocks.STONE)
            .sound(SoundType.STONE).strength(2.0f, 3.0f)
            .requiresCorrectToolForDrops().noOcclusion())
            .setShape(Shapes.box(0.125, 0, 0.125, 0.875, 1, 0.875)));
    public static final RegistryObject<Block> BRAZIER = addBlock("brazier", () -> new BrazierBlock(blockProps(Blocks.OAK_WOOD, DyeColor.GRAY)
            .sound(SoundType.METAL).strength(2.5f, 3.0f)
            .noOcclusion())
            .setShape(Shapes.box(0.1875, 0, 0.1875, 0.8125, 0.75, 0.8125)));
    public static final RegistryObject<Block> CRUCIBLE = addBlock("crucible", () -> new CrucibleBlock(blockProps(Blocks.IRON_BLOCK, DyeColor.GRAY)
            .sound(SoundType.METAL).strength(4.0f, 3.0f)
            .requiresCorrectToolForDrops().noOcclusion())
            .setShape(Shapes.or(
                    Shapes.box(0.0625, 0.875, 0.0625, 0.1875, 1, 0.9375),
                    Shapes.box(0.8125, 0.875, 0.0625, 0.9375, 1, 0.9375),
                    Shapes.box(0.0625, 0.875, 0.0625, 0.9375, 1, 0.1875),
                    Shapes.box(0.0625, 0.875, 0.8125, 0.9375, 1, 0.9375),
                    Shapes.box(0, 0.125, 0, 0.125, 0.875, 1),
                    Shapes.box(0.875, 0.125, 0, 1, 0.875, 1),
                    Shapes.box(0, 0.125, 0, 1, 0.875, 0.125),
                    Shapes.box(0, 0.125, 0.875, 1, 0.875, 1),
                    Shapes.box(0.0625, 0, 0.0625, 0.9375, 0.125, 0.9375)
            )));
    public static final RegistryObject<Block> STONE_HAND = addBlock("stone_hand", () -> new HandBlock(blockProps(Blocks.STONE)
            .sound(SoundType.STONE).strength(2.0f, 3.0f)
            .requiresCorrectToolForDrops().noOcclusion())
            .setShape(Shapes.box(0.25, 0, 0.25, 0.75, 0.75, 0.75)));
    public static final RegistryObject<Block> ENCHANTED_ASH = addBlock("enchanted_ash", () -> new EnchantedAshBlock(blockProps(Blocks.REDSTONE_WIRE, DyeColor.WHITE)
            .sound(SoundType.STONE).strength(0.0f, 0.75f).noOcclusion())
            .setShape(Shapes.empty()));
    public static final RegistryObject<Block> NECROTIC_FOCUS = addBlock("necrotic_focus", () -> new NecroticFocusBlock(blockProps(Blocks.STONE)
            .sound(SoundType.STONE).strength(2.8f, 3.0f)
            .requiresCorrectToolForDrops().noOcclusion())
            .setShape(Shapes.box(0.25, 0, 0.25, 0.75, 0.75, 0.75)));
    public static final RegistryObject<Block> PLANTER = addBlock("planter", () -> new BlockBase(blockProps(Blocks.OAK_WOOD)
            .sound(SoundType.WOOD).strength(2.0f, 3.0f)
            .noOcclusion())
            .setShape(Shapes.or(
                    Shapes.box(0, 0.25, 0, 1, 1, 1),
                    Shapes.box(0.25, 0, 0.25, 0.75, 0.25, 0.75))));
    public static final RegistryObject<Block> MERAMMER_ROOT = addBlock("merammer_root", () -> new HerbBlockBase(blockProps(Blocks.WHEAT)
            .sound(SoundType.GRASS).noOcclusion()));
    public static final RegistryObject<Block> AVENNIAN_SPRIG = addBlock("avennian_sprig", () -> new HerbBlockBase(blockProps(Blocks.WHEAT)
            .sound(SoundType.GRASS).noOcclusion()));
    public static final RegistryObject<Block> OANNA_BLOOM = addBlock("oanna_bloom", () -> new HerbBlockBase(blockProps(Blocks.WHEAT)
            .sound(SoundType.GRASS).noOcclusion()));
    public static final RegistryObject<Block> SILDRIAN_SEED = addBlock("sildrian_seed", () -> new HerbBlockBase(blockProps(Blocks.WHEAT)
            .sound(SoundType.GRASS).noOcclusion()));
    public static final RegistryObject<Block> ILLWOOD_SAPLING = addBlock("illwood_sapling", () -> new SaplingBlock(new EidolonAbstractTreeFeature.TreeGrower(), blockProps(Blocks.OAK_SAPLING)
            .sound(SoundType.GRASS).noOcclusion().noCollission()));
    public static final RegistryObject<Block> ILLWOOD_LEAVES = addBlock("illwood_leaves", () -> new LeavesBlock(blockProps(Blocks.MANGROVE_LEAVES)
            .randomTicks().sound(SoundType.GRASS).noOcclusion().isValidSpawn(Registry::allowsSpawnOnLeaves)
            .isSuffocating(Registry::isntSolid).isViewBlocking(Registry::isntSolid)));
    public static final RegistryObject<Block> STRIPPED_ILLWOOD_LOG = addBlock("stripped_illwood_log", () -> new RotatedPillarBlock(blockProps(Blocks.OAK_WOOD)
            .sound(SoundType.WOOD).strength(1.4f, 3.0f)));
    public static final RegistryObject<Block> STRIPPED_ILLWOOD_BARK = addBlock("stripped_illwood_bark", () -> new RotatedPillarBlock(blockProps(Blocks.OAK_WOOD)
            .sound(SoundType.WOOD).strength(1.4f, 3.0f)));
    public static final RegistryObject<Block> ILLWOOD_LOG = addBlock("illwood_log", () -> new StrippableLog(blockProps(Blocks.OAK_WOOD)
            .sound(SoundType.WOOD).strength(1.6f, 3.0f), STRIPPED_ILLWOOD_LOG));
    public static final RegistryObject<Block> ILLWOOD_BARK = addBlock("illwood_bark", () -> new StrippableLog(blockProps(Blocks.OAK_WOOD)
            .sound(SoundType.WOOD).strength(1.6f, 3.0f), STRIPPED_ILLWOOD_BARK));

    public static final RegistryObject<Block> SOUL_ENCHANTER = addBlock("soul_enchanter", () -> new SoulEnchanterBlock(blockProps(Blocks.ENCHANTING_TABLE)
            .sound(SoundType.STONE).strength(5.0f, 1200.0f)
            .requiresCorrectToolForDrops().noOcclusion())
            .setShape(Shapes.box(0, 0, 0, 1, 0.75, 1)));
    public static final RegistryObject<Block> WOODEN_STAND = addBlock("wooden_brewing_stand", () -> new WoodenStandBlock(blockProps(Blocks.BREWING_STAND)
            .sound(SoundType.STONE).strength(2.0f, 3.0f)
            .noOcclusion()));
    public static final RegistryObject<Block> GHOST_LIGHT = BLOCKS.register("ghost_light", () -> new GhostLight(BlockBehaviour.Properties.of().noCollission().noOcclusion().noLootTable().noParticlesOnBreak().dynamicShape().strength(0f, 0f)
            .sound(SoundType.FROGLIGHT).lightLevel(p -> p.getValue(GhostLight.DEITY) ? 12 : 8)));

    public static DecoBlockPack
            SMOOTH_STONE_BRICK = new DecoBlockPack(BLOCKS, "smooth_stone_bricks", blockProps(Blocks.STONE)
            .sound(SoundType.STONE).requiresCorrectToolForDrops().strength(2.0f, 3.0f))
            .addWall(),
            SMOOTH_STONE_TILES = new DecoBlockPack(BLOCKS, "smooth_stone_tiles", blockProps(Blocks.STONE)
                    .sound(SoundType.STONE).requiresCorrectToolForDrops().strength(2.0f, 3.0f)),
            SMOOTH_STONE_MASONRY = new DecoBlockPack(BLOCKS, "smooth_stone_masonry", blockProps(Blocks.STONE)
                    .sound(SoundType.STONE).requiresCorrectToolForDrops().strength(1.6f, 3.0f)),
            ELDER_BRICKS = new DecoBlockPack(BLOCKS, "elder_bricks", blockProps(Blocks.STONE, DyeColor.ORANGE)
                    .sound(SoundType.STONE).requiresCorrectToolForDrops().strength(3.0f, 3.0f))
                    .addWall(),
            ELDER_MASONRY = new DecoBlockPack(BLOCKS, "elder_masonry", blockProps(Blocks.STONE, DyeColor.ORANGE)
                    .sound(SoundType.STONE).requiresCorrectToolForDrops().strength(2.4f, 3.0f)),
            BONE_PILE = new DecoBlockPack(BLOCKS, "bone_pile", blockProps(Blocks.BONE_BLOCK)
                    .sound(SoundType.DEEPSLATE).requiresCorrectToolForDrops().strength(1.6f, 3.0f));

    public static DecoBlockPack.WoodDecoBlock
            POLISHED_PLANKS = new DecoBlockPack.WoodDecoBlock(BLOCKS, "polished_planks", POLISHED, blockProps(Blocks.OAK_WOOD)
            .sound(SoundType.WOOD).strength(1.6f, 3.0f))
            .addFence().addButton().addSign().addPressurePlate(),
            ILLWOOD_PLANKS = new DecoBlockPack.WoodDecoBlock(BLOCKS, "illwood_planks", ILLWOOD, blockProps(Blocks.OAK_WOOD)
                    .sound(SoundType.WOOD).strength(1.6f, 3.0f))
                    .addFence().addButton().addSign().addPressurePlate();

    public static final RegistryObject<Block>
            POLISHED_WOOD_PILLAR = addBlock("polished_wood_pillar", () -> new RotatedPillarBlock(blockProps(Blocks.OAK_WOOD)
            .strength(1.6f, 3.0f))),
            SMOOTH_STONE_ARCH = addBlock("smooth_stone_arch", () -> new PillarBlockBase(blockProps(Blocks.STONE)
                    .sound(SoundType.STONE).strength(2.0f, 3.0f).requiresCorrectToolForDrops())),
            MOSSY_SMOOTH_STONE_BRICKS = addBlock("mossy_smooth_stone_bricks", blockProps(Blocks.STONE)
                    .sound(SoundType.STONE).strength(2.0f, 3.0f).requiresCorrectToolForDrops()),
            ELDER_BRICKS_EYE = addBlock("elder_bricks_eye", blockProps(Blocks.STONE, DyeColor.ORANGE)
                    .sound(SoundType.STONE).strength(3.0f, 3.0f).requiresCorrectToolForDrops()),
            ELDER_PILLAR = addBlock("elder_pillar", () -> new PillarBlockBase(blockProps(Blocks.STONE, DyeColor.ORANGE)
                    .sound(SoundType.STONE).strength(3.0f, 3.0f)
                    .requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> SCRIPTORIUM = addBlock("scriptorium", () -> new Scriptorium(blockProps(Blocks.CRAFTING_TABLE)));


    public static final RegistryObject<MenuType<WorktableContainer>>
            WORKTABLE_CONTAINER = addContainer("worktable", WorktableContainer::new);
    public static final RegistryObject<MenuType<SoulEnchanterContainer>>
            SOUL_ENCHANTER_CONTAINER = addContainer("soul_enchanter", SoulEnchanterContainer::new);
    public static final RegistryObject<MenuType<WoodenBrewingStandContainer>>
            WOODEN_STAND_CONTAINER = addContainer("wooden_brewing_stand", WoodenBrewingStandContainer::new);
    public static final RegistryObject<MenuType<ResearchTableContainer>>
            RESEARCH_TABLE_CONTAINER = addContainer("research_table", ResearchTableContainer::new);


    public static void init() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        EidolonAttributes.ATTRIBUTES.register(modEventBus);
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        EidolonEntities.ENTITIES.register(modEventBus);
        EidolonPotions.POTIONS.register(modEventBus);
        EidolonPotions.POTION_TYPES.register(modEventBus);
        TILE_ENTITIES.register(modEventBus);
        EidolonParticles.PARTICLES.register(modEventBus);
        EidolonSounds.SOUND_EVENTS.register(modEventBus);
        Worldgen.FEATURES.register(modEventBus);
        CONTAINERS.register(modEventBus);
        EidolonRecipes.RECIPE_TYPES.register(modEventBus);
        EidolonRecipes.RECIPE_SERIALIZERS.register(modEventBus);
        ARG_TYPES.register(modEventBus);
        TABS.register(modEventBus);
        AdvancementTriggers.init();
    }

    @OnlyIn(Dist.CLIENT)
    public static void clientInit() {
    }

    public static RegistryObject<BlockEntityType<HandTileEntity>> HAND_TILE_ENTITY;
    public static RegistryObject<BlockEntityType<BrazierTileEntity>> BRAZIER_TILE_ENTITY;
    public static RegistryObject<BlockEntityType<NecroticFocusTileEntity>> NECROTIC_FOCUS_TILE_ENTITY;
    public static RegistryObject<BlockEntityType<CrucibleTileEntity>> CRUCIBLE_TILE_ENTITY;
    public static RegistryObject<BlockEntityType<EffigyTileEntity>> EFFIGY_TILE_ENTITY;
    public static RegistryObject<BlockEntityType<SoulEnchanterTileEntity>> SOUL_ENCHANTER_TILE_ENTITY;
    public static RegistryObject<BlockEntityType<WoodenStandTileEntity>> WOODEN_STAND_TILE_ENTITY;
    public static RegistryObject<BlockEntityType<GobletTileEntity>> GOBLET_TILE_ENTITY;
    public static RegistryObject<BlockEntityType<CenserTileEntity>> CENSER_TILE_ENTITY;
    public static RegistryObject<BlockEntityType<ResearchTableTileEntity>> RESEARCH_TABLE_TILE_ENTITY;

    public static RegistryObject<BlockEntityType<ScriptoriumTile>> SCRIPTORIUM_TILE;



    static {
        HAND_TILE_ENTITY = TILE_ENTITIES.register("hand_tile", () -> BlockEntityType.Builder.of(HandTileEntity::new, STONE_HAND.get()).build(null));
        BRAZIER_TILE_ENTITY = TILE_ENTITIES.register("brazier_tile", () -> BlockEntityType.Builder.of(BrazierTileEntity::new, BRAZIER.get()).build(null));
        NECROTIC_FOCUS_TILE_ENTITY = TILE_ENTITIES.register("necrotic_focus", () -> BlockEntityType.Builder.of(NecroticFocusTileEntity::new, NECROTIC_FOCUS.get()).build(null));
        CRUCIBLE_TILE_ENTITY = TILE_ENTITIES.register("crucible", () -> BlockEntityType.Builder.of(CrucibleTileEntity::new, CRUCIBLE.get()).build(null));
        EFFIGY_TILE_ENTITY = TILE_ENTITIES.register("effigy", () -> BlockEntityType.Builder.of(EffigyTileEntity::new, STRAW_EFFIGY.get(), ELDER_EFFIGY.get()).build(null));
        SOUL_ENCHANTER_TILE_ENTITY = TILE_ENTITIES.register("soul_enchanter", () -> BlockEntityType.Builder.of(SoulEnchanterTileEntity::new, SOUL_ENCHANTER.get()).build(null));
        WOODEN_STAND_TILE_ENTITY = TILE_ENTITIES.register("wooden_brewing_stand", () -> BlockEntityType.Builder.of(WoodenStandTileEntity::new, WOODEN_STAND.get()).build(null));
        GOBLET_TILE_ENTITY = TILE_ENTITIES.register("goblet", () -> BlockEntityType.Builder.of(GobletTileEntity::new, GOBLET.get()).build(null));
        CENSER_TILE_ENTITY = TILE_ENTITIES.register("censer", () -> BlockEntityType.Builder.of(CenserTileEntity::new, CENSER.get()).build(null));
        RESEARCH_TABLE_TILE_ENTITY = TILE_ENTITIES.register("research_table", () -> BlockEntityType.Builder.of(ResearchTableTileEntity::new, RESEARCH_TABLE.get()).build(null));
        SCRIPTORIUM_TILE = TILE_ENTITIES.register("scriptorium", () -> BlockEntityType.Builder.of(ScriptoriumTile::new, SCRIPTORIUM.get()).build(null));
    }


    public static final DamageTypeData SAPPING = DamageTypeData.builder()
            .simpleId("sap")
            .scaling(DamageScaling.ALWAYS)
            .tag(FORGE_WITHER)
            .build();

    public static final DamageTypeData RITUAL_DAMAGE = DamageTypeData.builder()
            .simpleId("ritual")
            .scaling(DamageScaling.ALWAYS)
            .tag(DamageTypeTags.BYPASSES_ARMOR)
            .tag(DamageTypeTags.BYPASSES_ENCHANTMENTS)
            .build();
    public static final DamageTypeData FROST_DAMAGE = DamageTypeData.builder()
            .simpleId("frost")
            .effects(DamageEffects.FREEZING)
            .tag(FORGE_MAGIC)
            .build();

    public void registerCaps(RegisterCapabilitiesEvent event) {
        event.register(IReputation.class);
        event.register(IKnowledge.class);
        event.register(ISoul.class);
        event.register(IPlayerData.class);
    }

    @SuppressWarnings("deprecation")
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void registerFactories(RegisterParticleProvidersEvent evt) {
        Minecraft.getInstance().particleEngine.register(EidolonParticles.FEATHER_PARTICLE.get(), FeatherParticleType.Factory::new);
        Minecraft.getInstance().particleEngine.register(EidolonParticles.FLAME_PARTICLE.get(), FlameParticleType.Factory::new);
        Minecraft.getInstance().particleEngine.register(EidolonParticles.SMOKE_PARTICLE.get(), SmokeParticleType.Factory::new);
        Minecraft.getInstance().particleEngine.register(EidolonParticles.SPARKLE_PARTICLE.get(), SparkleParticleType.Factory::new);
        Minecraft.getInstance().particleEngine.register(EidolonParticles.WISP_PARTICLE.get(), WispParticleType.Factory::new);
        Minecraft.getInstance().particleEngine.register(EidolonParticles.BUBBLE_PARTICLE.get(), BubbleParticleType.Factory::new);
        Minecraft.getInstance().particleEngine.register(EidolonParticles.STEAM_PARTICLE.get(), SteamParticleType.Factory::new);
        Minecraft.getInstance().particleEngine.register(EidolonParticles.LINE_WISP_PARTICLE.get(), LineWispParticleType.Factory::new);
        Minecraft.getInstance().particleEngine.register(EidolonParticles.SIGN_PARTICLE.get(), sprite -> new SignParticleType.Factory());
        Minecraft.getInstance().particleEngine.register(EidolonParticles.SLASH_PARTICLE.get(), SlashParticleType.Factory::new);
        Minecraft.getInstance().particleEngine.register(EidolonParticles.GLOWING_SLASH_PARTICLE.get(), GlowingSlashParticleType.Factory::new);
        Minecraft.getInstance().particleEngine.register(EidolonParticles.RUNE_PARTICLE.get(), sprite -> new RuneParticleType.Factory());
    }

    public static final RegistryObject<ArgumentTypeInfo<?, ?>> SIGN_ARG = ARG_TYPES.register("sign", () -> ArgumentTypeInfos.registerByClass(KnowledgeCommand.SignArgument.class, SingletonArgumentInfo.contextFree(KnowledgeCommand.SignArgument::signs)));
    public static final RegistryObject<ArgumentTypeInfo<?, ?>> RUNE_ARG = ARG_TYPES.register("rune", () -> ArgumentTypeInfos.registerByClass(KnowledgeCommand.RuneArgument.class, SingletonArgumentInfo.contextFree(KnowledgeCommand.RuneArgument::runes)));
    public static final RegistryObject<ArgumentTypeInfo<?, ?>> RESEARCH_ARG = ARG_TYPES.register("research", () -> ArgumentTypeInfos.registerByClass(KnowledgeCommand.ResearchArgument.class, SingletonArgumentInfo.contextFree(KnowledgeCommand.ResearchArgument::researches)));
    public static final RegistryObject<ArgumentTypeInfo<?, ?>> DEITY_ARG = ARG_TYPES.register("deity", () -> ArgumentTypeInfos.registerByClass(ReputationCommand.DeityArgument.class, SingletonArgumentInfo.contextFree(ReputationCommand.DeityArgument::deities)));

}

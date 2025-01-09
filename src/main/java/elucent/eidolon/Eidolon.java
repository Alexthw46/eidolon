package elucent.eidolon;

import com.google.common.collect.ImmutableSet;
import elucent.eidolon.client.ClientConfig;
import elucent.eidolon.client.ClientRegistry;
import elucent.eidolon.codex.CodexChapters;
import elucent.eidolon.common.item.AthameItem;
import elucent.eidolon.common.tile.*;
import elucent.eidolon.compat.CompatHandler;
import elucent.eidolon.event.Events;
import elucent.eidolon.gui.ResearchTableScreen;
import elucent.eidolon.gui.SoulEnchanterScreen;
import elucent.eidolon.gui.WoodenBrewingStandScreen;
import elucent.eidolon.gui.WorktableScreen;
import elucent.eidolon.mixin.BlockEntityTypeAccessor;
import elucent.eidolon.network.Networking;
import elucent.eidolon.proxy.ClientProxy;
import elucent.eidolon.proxy.ISidedProxy;
import elucent.eidolon.proxy.ServerProxy;
import elucent.eidolon.registries.*;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static net.minecraft.world.entity.Mob.checkMobSpawnRules;

@Mod(Eidolon.MODID)
public class Eidolon {
    public static final ISidedProxy proxy = DistExecutor.unsafeRunForDist(() -> ClientProxy::new, () -> ServerProxy::new);

    public static final String MODID = "eidolon";
    public static final Logger LOG = LogManager.getLogger("Eidolon Repraised");

    public static ResourceLocation prefix(String path) {
        return new ResourceLocation("eidolon", path);
    }

    public static boolean trueMobType = false;

    public static MobType getTrueMobType(LivingEntity e) {
        trueMobType = true;
        MobType type = e.getMobType();
        trueMobType = false;
        return type;
    }

    public Eidolon() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::setup);
        modEventBus.addListener(this::sendImc);
        modEventBus.addListener(this::spawnPlacements);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        modEventBus.register(new Registry());
        Registry.init();
        proxy.init();
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new Events());

        CompatHandler.initialize();
    }

    public void setup(final FMLCommonSetupEvent event) {
        Networking.init();
        event.enqueueWork(() -> {
            Spells.init();
            RitualRegistry.init();
            IncenseRegistry.init();
            EidolonRecipes.ritualRecipeTypes.addAll(List.of(EidolonRecipes.CRAFTING_RITUAL_TYPE.get(), EidolonRecipes.SUMMON_RITUAL_TYPE.get(), EidolonRecipes.COMMAND_RITUAL_TYPE.get(), EidolonRecipes.LOCATION_RITUAL_TYPE.get(), EidolonRecipes.RITUAL_TYPE.get()));
            EidolonPotions.addBrewingRecipes();
            AltarEntries.init();
            Researches.init();
            Runes.init();
            AthameItem.initHarvestables();
            Raid.RaiderType.create("eidolon:necromancer", EidolonEntities.NECROMANCER.get(), new int[]{0, 0, 0, 0, 0, 1, 0, 1});
            addBlocksToTile(BlockEntityType.SIGN, Registry.ILLWOOD_PLANKS.getStandingSign(), Registry.ILLWOOD_PLANKS.getWallSign(), Registry.POLISHED_PLANKS.getStandingSign(), Registry.POLISHED_PLANKS.getWallSign());
            addBlocksToTile(BlockEntityType.HANGING_SIGN, Registry.ILLWOOD_PLANKS.getHangingSign(), Registry.ILLWOOD_PLANKS.getHangingWallSign(), Registry.POLISHED_PLANKS.getHangingSign(), Registry.POLISHED_PLANKS.getHangingWallSign());
        });
    }

    public static void addBlocksToTile(BlockEntityType<?> bet, Block... blocksToAdd) {
        Set<Block> oldSet = ((BlockEntityTypeAccessor) bet).getValidBlocks();
        if (oldSet instanceof ImmutableSet<Block>) {
            Set<Block> newSet = new HashSet<>();
            Collections.addAll(newSet, blocksToAdd);
            newSet.addAll(oldSet);
            ((BlockEntityTypeAccessor) bet).setValidBlocks(newSet);
        } else {
            Collections.addAll(oldSet, blocksToAdd);
        }
    }


    public void spawnPlacements(final SpawnPlacementRegisterEvent event) {
        event.register(EidolonEntities.ZOMBIE_BRUTE.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules, SpawnPlacementRegisterEvent.Operation.AND);
        event.register(EidolonEntities.WRAITH.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules, SpawnPlacementRegisterEvent.Operation.AND);
        event.register(EidolonEntities.GIANT_SKEL.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (pType, pLevel, pSpawnType, pPos, pRandom) -> (pLevel.getDifficulty() != Difficulty.PEACEFUL && checkMobSpawnRules(pType, pLevel, pSpawnType, pPos, pRandom)), SpawnPlacementRegisterEvent.Operation.AND);
        event.register(EidolonEntities.RAVEN.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Animal::checkAnimalSpawnRules, SpawnPlacementRegisterEvent.Operation.AND);
        event.register(EidolonEntities.SLIMY_SLUG.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (e, w, t, pos, rand) -> true, SpawnPlacementRegisterEvent.Operation.AND);
    }

    @OnlyIn(Dist.CLIENT)
    public static void clientSetup(final FMLClientSetupEvent event) {
        BlockEntityRenderers.register(Registry.HAND_TILE_ENTITY.get(), (trd) -> new HandTileRenderer());
        BlockEntityRenderers.register(Registry.BRAZIER_TILE_ENTITY.get(), (trd) -> new BrazierTileRenderer());
        BlockEntityRenderers.register(Registry.NECROTIC_FOCUS_TILE_ENTITY.get(), (trd) -> new NecroticFocusTileRenderer());
        BlockEntityRenderers.register(Registry.CRUCIBLE_TILE_ENTITY.get(), (trd) -> new CrucibleTileRenderer());
        BlockEntityRenderers.register(Registry.SOUL_ENCHANTER_TILE_ENTITY.get(), (trd) -> new SoulEnchanterTileRenderer());
        BlockEntityRenderers.register(Registry.GOBLET_TILE_ENTITY.get(), (trd) -> new GobletTileRenderer());
        BlockEntityRenderers.register(Registry.CENSER_TILE_ENTITY.get(), (trd) -> new CenserRenderer());

        event.enqueueWork(() -> {
            CodexChapters.init();

            MenuScreens.register(Registry.WORKTABLE_CONTAINER.get(), WorktableScreen::new);
            MenuScreens.register(Registry.SOUL_ENCHANTER_CONTAINER.get(), SoulEnchanterScreen::new);
            MenuScreens.register(Registry.WOODEN_STAND_CONTAINER.get(), WoodenBrewingStandScreen::new);
            MenuScreens.register(Registry.RESEARCH_TABLE_CONTAINER.get(), ResearchTableScreen::new);

            ClientRegistry.initCurios();

            Sheets.addWoodType(Registry.ILLWOOD);
            Sheets.addWoodType(Registry.POLISHED);
        });
        MinecraftForge.EVENT_BUS.addListener((PlayerInteractEvent.RightClickBlock e) -> {
            InteractionResult result = Events.rightClickLectern(e.getEntity(), e.getLevel(), e.getHitVec());
            if (result.consumesAction()) {
                e.setCanceled(true);
                e.setCancellationResult(result);
            }
        });
    }

    @OnlyIn(Dist.CLIENT)
    public static void registerOverlays(RegisterGuiOverlaysEvent evt) {
        evt.registerAbove(VanillaGuiOverlay.PLAYER_HEALTH.id(), "hearts", new ClientRegistry.EidolonHearts());
        evt.registerBelow(VanillaGuiOverlay.CHAT_PANEL.id(), "mana_bar", new ClientRegistry.EidolonManaBar());
        evt.registerAbove(VanillaGuiOverlay.EXPERIENCE_BAR.id(), "raven_charge", new ClientRegistry.EidolonRavenCharge());
    }

    public void sendImc(InterModEnqueueEvent evt) {
        InterModComms.sendTo("consecration", "holy_material", () -> "silver");
    }
}

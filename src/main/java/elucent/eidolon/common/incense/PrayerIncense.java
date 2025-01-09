package elucent.eidolon.common.incense;

import elucent.eidolon.api.altar.AltarInfo;
import elucent.eidolon.api.deity.Deity;
import elucent.eidolon.api.ritual.IncenseRitual;
import elucent.eidolon.api.ritual.Ritual;
import elucent.eidolon.capability.IReputation;
import elucent.eidolon.common.deity.Deities;
import elucent.eidolon.common.deity.DeityLocks;
import elucent.eidolon.common.spell.PrayerSpell;
import elucent.eidolon.common.tile.CenserTileEntity;
import elucent.eidolon.common.tile.EffigyTileEntity;
import elucent.eidolon.registries.Spells;
import elucent.eidolon.util.KnowledgeUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;

public class PrayerIncense extends IncenseRitual {
    public PrayerIncense(ResourceLocation registryName) {
        super(800, registryName);
    }

    @Override
    public boolean start(@Nullable Player player, CenserTileEntity censer) {
        super.start(player, censer);
        Level world = censer.getLevel();
        BlockPos pos = censer.getBlockPos();
        if (world == null || player == null) return false;
        LazyOptional<IReputation> reputationLazyOptional = world.getCapability(IReputation.INSTANCE);
        if (!reputationLazyOptional.isPresent() || reputationLazyOptional.resolve().isEmpty()) return false;
        if (!reputationLazyOptional.resolve().get().canPray(player, Spells.CENSER, world.getGameTime())) {
            player.displayClientMessage(Component.translatable("eidolon.message.prayer_cooldown"), true);
            return false;
        }
        List<EffigyTileEntity> effigies = Ritual.getTilesWithinAABB(EffigyTileEntity.class, world, new AABB(pos.offset(-4, -4, -4), pos.offset(5, 5, 5)));
        if (effigies.isEmpty()) {
            player.displayClientMessage(Component.translatable("eidolon.message.no_effigy"), true);
            return false;
        }
        EffigyTileEntity effigy = effigies.stream().min(Comparator.comparingDouble((e) -> e.getBlockPos().distSqr(pos))).get();
        if (effigy.ready()) {
            Deity deity = Deities.LIGHT_DEITY;
            AltarInfo info = AltarInfo.getAltarInfo(world, effigy.getBlockPos());
            world.getCapability(IReputation.INSTANCE, null).ifPresent((rep) -> {
                if (rep.getReputation(player, deity.getId()) < 3) {
                    player.displayClientMessage(Component.translatable("eidolon.message.not_enough_reputation"), true);
                    return;
                }
                KnowledgeUtil.grantResearchNoToast(player, DeityLocks.BASIC_INCENSE_PRAYER);
                rep.pray(player, Spells.CENSER, world.getGameTime());
                rep.addReputation(player, deity.getId(), 2.0 + 0.5 * info.getPower());
                PrayerSpell.updateMagic(info, player, world, rep.getReputation(player, deity.getId()));
            });
            return true;
        }
        return false;
    }

}

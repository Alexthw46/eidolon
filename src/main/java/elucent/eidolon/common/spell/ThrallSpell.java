package elucent.eidolon.common.spell;

import elucent.eidolon.Eidolon;
import elucent.eidolon.api.spells.Sign;
import elucent.eidolon.capability.ISoul;
import elucent.eidolon.common.deity.DeityLocks;
import elucent.eidolon.util.EntityUtil;
import elucent.eidolon.util.KnowledgeUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class ThrallSpell extends StaticSpell {

    public ThrallSpell(ResourceLocation name, Sign... signs) {
        super(name, 50, signs);
    }

    @Override
    public boolean canCast(Level world, BlockPos pos, Player player) {
        HitResult ray = rayTrace(player, player.getReachDistance(), 0, true);
        if (ray instanceof EntityHitResult result && result.getEntity() instanceof LivingEntity living) {
            var type = Eidolon.getTrueMobType(living);
            return type == MobType.UNDEAD;
        }
        return false;
    }

    @Override
    public void cast(Level world, BlockPos pos, Player player) {
        if (world instanceof ServerLevel && player instanceof ServerPlayer sp) {
            HitResult ray = rayTrace(player, player.getReachDistance(), 0, true);
            if (ray instanceof EntityHitResult result && result.getEntity() instanceof LivingEntity living) {
                float actualCost = 2 * getCost() * living.getHealth() / living.getMaxHealth();
                player.getCapability(ISoul.INSTANCE).ifPresent(soul -> {
                    if (soul.getMagic() >= actualCost) {
                        soul.takeMagic(actualCost);
                        EntityUtil.enthrall(player, living);
                        KnowledgeUtil.grantResearchNoToast(player, DeityLocks.ENTHRALL_UNDEAD);
                    } else
                        sp.connection.send(new ClientboundSetActionBarTextPacket(Component.translatable("eidolon.title.no_mana")));
                });
            }
        }
    }
}

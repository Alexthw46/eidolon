package elucent.eidolon.common.deity;

import elucent.eidolon.api.deity.Deity;
import elucent.eidolon.capability.Facts;
import elucent.eidolon.capability.IReputation;
import elucent.eidolon.registries.Signs;
import elucent.eidolon.util.KnowledgeUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class LightDeity extends Deity {
    public LightDeity(ResourceLocation id, int red, int green, int blue) {
        super(id, red, green, blue);
        progression.add(new Stage(DeityLocks.BASIC_INCENSE_PRAYER, 3, true)
                .requirement(new ResearchRequirement(DeityLocks.BASIC_INCENSE_PRAYER))
        ).add(new Stage(DeityLocks.HEAL_VILLAGER, 15, true)
                .requirement(new ResearchRequirement(DeityLocks.HEAL_VILLAGER))
        ).add(new Stage(DeityLocks.CURE_ZOMBIE, 35, true)
                .requirement(new ResearchRequirement(DeityLocks.CURE_ZOMBIE))
        ).add(new Stage(DeityLocks.SMITE_UNDEAD, 50, true)
                .requirement(new ResearchRequirement(DeityLocks.SMITE_UNDEAD))
        ).setMax(100);
    }

    @Override
    public void onReputationUnlock(Player player, IReputation rep, ResourceLocation lock) {
        if (lock.equals(DeityLocks.BASIC_INCENSE_PRAYER)) {
            KnowledgeUtil.grantSign(player, Signs.SOUL_SIGN);
        } else if (lock.equals(DeityLocks.HEAL_VILLAGER)) {
            KnowledgeUtil.grantSign(player, Signs.MIND_SIGN);
        } else if (lock.equals(DeityLocks.CURE_ZOMBIE)) {
            KnowledgeUtil.grantSign(player, Signs.DEATH_SIGN);
        } else if (lock.equals(DeityLocks.SMITE_UNDEAD)) {
            KnowledgeUtil.grantSign(player, Signs.WARDING_SIGN);
        }
    }

    @Override
    public void onReputationLock(Player player, IReputation rep, ResourceLocation lock) {
        if (lock.equals(DeityLocks.BASIC_INCENSE_PRAYER)) {
            KnowledgeUtil.grantSign(player, Signs.FLAME_SIGN);
        } else if (lock.equals(DeityLocks.HEAL_VILLAGER)) {
            KnowledgeUtil.grantFact(player, Facts.VILLAGER_HEALING);
        } else if (lock.equals(DeityLocks.CURE_ZOMBIE)) {
            KnowledgeUtil.grantSign(player, Signs.HARMONY_SIGN);
            KnowledgeUtil.grantFact(player, Facts.ZOMBIE_CURE);
        } else if (lock.equals(DeityLocks.SMITE_UNDEAD)) {
            KnowledgeUtil.grantFact(player, Facts.SMITE);
            KnowledgeUtil.grantSign(player, Signs.MAGIC_SIGN);
        }
    }
}

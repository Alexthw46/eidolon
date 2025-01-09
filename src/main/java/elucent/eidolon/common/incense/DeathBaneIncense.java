package elucent.eidolon.common.incense;

import elucent.eidolon.api.ritual.IncenseRitual;
import elucent.eidolon.registries.EidolonPotions;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public class DeathBaneIncense extends IncenseRitual {
    public DeathBaneIncense(ResourceLocation registryName) {
        super(800, registryName);
    }

    @Override
    public void tickEffect(int age) {
        // every 20 ticks, apply a wither effect to the living entities around the censer and heal the undead
        if (age % 20 == 0) {
            Level level = censer.getLevel();
            BlockPos pos = censer.getBlockPos();
            assert level != null;
            for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, new AABB(pos).inflate(10))) {
                if (entity.getMobType() != MobType.UNDEAD) {
                    entity.addEffect(new MobEffectInstance(EidolonPotions.LIGHT_BLESSED.get(), 20 * 60 * 10));
                } else {
                    entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 20 * 60 * 2));
                }
            }
        }
    }

}
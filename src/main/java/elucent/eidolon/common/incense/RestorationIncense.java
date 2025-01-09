package elucent.eidolon.common.incense;

import elucent.eidolon.api.ritual.IncenseRitual;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public class RestorationIncense extends IncenseRitual {

    public RestorationIncense(ResourceLocation registryName) {
        super(800, registryName);
    }

    @Override
    public float getGreen() {
        return .75f;
    }

    @Override
    public void tickEffect(int age) {
        // every 20 ticks, apply a wither effect to the undead around the censer and heal the living entities
        if (age % 20 == 0) {
            Level level = censer.getLevel();
            BlockPos pos = censer.getBlockPos();
            assert level != null;
            for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, new AABB(pos).inflate(10))) {
                if (entity.getMobType() == MobType.UNDEAD) {
                    entity.addEffect(new MobEffectInstance(MobEffects.WITHER, 200, 1));
                } else {
                    entity.heal(1);
                }
            }
        }
    }

}

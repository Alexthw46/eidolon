package elucent.eidolon.common.incense;

import elucent.eidolon.api.ritual.IncenseRitual;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public class ToughIncense extends IncenseRitual {
    public ToughIncense(ResourceLocation registryName) {
        super(800, registryName);
    }

    @Override
    public void tickEffect(int age) {
        if (age % 20 == 0) {
            // fetch all creatures around the censer and give them resistance
            Level level = censer.getLevel();
            BlockPos pos = censer.getBlockPos();
            assert level != null;
            for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, new AABB(pos).inflate(15))) {
                entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 1));
            }
        }
    }
}
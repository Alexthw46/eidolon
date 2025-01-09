package elucent.eidolon.common.incense;

import elucent.eidolon.api.ritual.IncenseRitual;
import elucent.eidolon.client.particle.Particles;
import elucent.eidolon.common.tile.CenserTileEntity;
import elucent.eidolon.registries.EidolonParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public class GloomIncense extends IncenseRitual {
    public GloomIncense(ResourceLocation registryName) {
        super(800, registryName);
    }

    @Override
    public float getRed() {
        return 0.5f;
    }

    @Override
    public float getBlue() {
        return 1.0f;
    }

    @Override
    public void animateParticles(CenserTileEntity censer, int burnCounter) {
        super.animateParticles(censer, burnCounter);
        Level level = censer.getLevel();
        BlockPos pos = censer.getBlockPos();
        assert level != null;
        double x = pos.getX();
        double y = pos.getY();
        double z = pos.getZ();
        if (level.random.nextInt(2) == 0) Particles.create(EidolonParticles.SMOKE_PARTICLE)
                .setAlpha(0.35f, 0).setScale(0.375f, 0.125f).setLifetime(280)
                .randomOffset(10, 0.5).randomVelocity(0.025f, 0.025f)
                .addVelocity(0, -0.0125f, 0)
                .setColor(0.125f, 0.125f, 0.125f, 0.005f, 0.005f, 0.005f)
                .repeat(level, x, y + 0.125, z, 5);
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
                    entity.addEffect(new MobEffectInstance(MobEffects.WITHER, 200, 1));
                } else {
                    entity.heal(1);
                }
            }
        }
    }
}

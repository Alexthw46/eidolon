package elucent.eidolon.api.ritual;

import elucent.eidolon.client.particle.Particles;
import elucent.eidolon.common.tile.CenserTileEntity;
import elucent.eidolon.registries.EidolonParticles;
import elucent.eidolon.registries.IncenseRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public abstract class IncenseRitual {

    protected CenserTileEntity censer;
    protected Player player;
    protected ResourceLocation registryName;
    int maxDuration;

    public IncenseRitual(int maxDuration, ResourceLocation registryName) {
        this.maxDuration = maxDuration;
        this.registryName = registryName;
    }

    public boolean start(@Nullable Player player, CenserTileEntity censer) {
        this.censer = censer;
        this.player = player;
        return true;
    }

    public void tick(int age) {
        if (censer != null)
            if (age < this.maxDuration) {
                tickEffect(age);
            } else {
                censer.extinguish();
            }
    }

    public void tickEffect(int age) {
    }

    public void write(CompoundTag tag) {
        CompoundTag contextTag = new CompoundTag();
        if (registryName != null) // failsafe
            contextTag.putString("incenseRegistryName", registryName.toString());
        tag.put("incenseContext", contextTag);
    }

    public static IncenseRitual read(CompoundTag tag) {
        CompoundTag contextTag = tag.getCompound("incenseContext");
        // read the incense registry name from the tag
        ResourceLocation incenseRegistryName = ResourceLocation.tryParse(contextTag.getString("incenseRegistryName"));
        // get the incense from the registry
        return IncenseRegistry.getIncenseRitual(incenseRegistryName);
    }

    public float getRed() {
        return 1.0f;
    }

    public float getGreen() {
        return .5f;
    }

    public float getBlue() {
        return .3f;
    }

    public void animateParticles(CenserTileEntity censer, int burnCounter) {
        BlockPos pos = censer.getBlockPos();
        Level level = censer.getLevel();
        float x = pos.getX() + 0.5f, y = pos.getY() + 0.45f, z = pos.getZ() + 0.5f;
        float r = getRed();
        float g = getGreen();
        float b = getBlue();

        if (burnCounter < 160) Particles.create(EidolonParticles.FLAME_PARTICLE)
                .setAlpha(0.5f, 0).setScale(0.25f, 0.125f).setLifetime(20)
                .randomOffset(0.125, 0.125).randomVelocity(0.00625f, 0.01875f)
                .addVelocity(0, 0.00625f, 0)
                .setColor(r, g, b, r, g * 0.5f, b * 1.5f)
                .spawn(level, x, y, z);

        assert level != null;
        if (level.random.nextInt(20) == 0) Particles.create(EidolonParticles.SPARKLE_PARTICLE)
                .setAlpha(1, 0).setScale(0.0625f, 0).setLifetime(40)
                .randomOffset(0.0625, 0).randomVelocity(0.125f, 0)
                .addVelocity(0, 0.125f, 0)
                .setColor(r, g * 1.5f, b * 2, r, g, b)
                .enableGravity().setSpin(0.4f)
                .spawn(level, x, y, z);

        if (level.random.nextInt(5) == 0) Particles.create(EidolonParticles.SMOKE_PARTICLE)
                .setAlpha(0.25f, 0).setScale(0.375f, 0.125f).setLifetime(160)
                .randomOffset(0.25, 0.125).randomVelocity(0.025f, 0.025f)
                .addVelocity(0, 0.0125f, 0)
                .setColor(0.5f, 0.5f, 0.5f, 0.25f, 0.25f, 0.25f)
                .spawn(level, x, y + 0.125, z);

    }

}

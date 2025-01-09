package elucent.eidolon.common.spell;

import elucent.eidolon.api.spells.Sign;
import elucent.eidolon.api.spells.SignSequence;
import elucent.eidolon.api.spells.Spell;
import elucent.eidolon.capability.ISoul;
import elucent.eidolon.util.MathUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class StaticSpell extends Spell {
    public SignSequence signs;
    private int cost;
    private int delay = 10;
    public @Nullable ForgeConfigSpec.ConfigValue<Integer> COST;


    public StaticSpell(ResourceLocation name, Sign... signs) {
        super(name);
        // this.signs = new SignSequence(signs);
    }

    @Override
    public void setSigns(SignSequence signs) {
        this.signs = signs;
    }

    public StaticSpell(ResourceLocation name, int cost, Sign... signs) {
        this(name, signs);
        this.cost = cost;
    }

    public StaticSpell(ResourceLocation name, int cost, int delay, Sign... signs) {
        this(name, signs);
        this.cost = cost;
        this.delay = delay;
    }

    public int getCost() {
        return COST == null ? cost : COST.get();
    }

    @NotNull
    protected static Vec3 getVector(Level world, Player player) {
        HitResult ray = world.clip(new ClipContext(player.getEyePosition(0), player.getEyePosition(0).add(player.getLookAngle().scale(4)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
        return ray.getType() == HitResult.Type.BLOCK ? ray.getLocation() : player.getEyePosition(0).add(player.getLookAngle().scale(4));
    }

    public static HitResult rayTrace(Entity entity, double length, float lookOffset, boolean hitLiquids) {
        HitResult result = entity.pick(length, lookOffset, hitLiquids);
        EntityHitResult entityLookedAt = MathUtil.getLookedAtEntity(entity, 25);
        return entityLookedAt == null ? result : entityLookedAt;
    }

    @Override
    public boolean matches(SignSequence signs) {
        return this.signs.equals(signs);
    }

    public abstract boolean canCast(Level world, BlockPos pos, Player player);

    @Override
    public boolean canCast(Level world, BlockPos pos, Player player, SignSequence signs) {
        if (getCost() > 0 && !player.isCreative()) {
            LazyOptional<ISoul> capability = player.getCapability(ISoul.INSTANCE);
            if (capability.isPresent() && capability.resolve().isPresent()) {
                ISoul soul = capability.resolve().get();
                if (soul.getMagic() < getCost()) {
                    if (player instanceof ServerPlayer serverPlayer)
                        serverPlayer.connection.send(new ClientboundSetActionBarTextPacket(Component.translatable("eidolon.title.no_mana")));
                    return false;
                }
            }
        }
        return canCast(world, pos, player);
    }

    public abstract void cast(Level world, BlockPos pos, Player player);

    @Override
    public void cast(Level world, BlockPos pos, Player player, SignSequence signs) {
        cast(world, pos, player);
    }

    @Override
    public void buildConfig(ForgeConfigSpec.Builder spellBuilder) {
        DELAY = spellBuilder.comment("The delay in ticks before the spell is cast").defineInRange("delay", delay, 0, Integer.MAX_VALUE);
        COST = spellBuilder.comment("The cost of casting this spell").defineInRange("cost", cost, 0, Integer.MAX_VALUE);
    }
}

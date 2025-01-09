package elucent.eidolon.common.tile;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public interface IBurner {

    void startBurning(Player player, @NotNull Level world, BlockPos pos);

    boolean canStartBurning();

    void extinguish();

}


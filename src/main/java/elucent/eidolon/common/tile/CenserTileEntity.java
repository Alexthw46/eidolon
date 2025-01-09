package elucent.eidolon.common.tile;

import elucent.eidolon.api.ritual.IncenseRitual;
import elucent.eidolon.registries.IncenseRegistry;
import elucent.eidolon.registries.Registry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FlintAndSteelItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT;

public class CenserTileEntity extends TileEntityBase implements IBurner {
    public CenserTileEntity(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
        super(tileEntityTypeIn, pos, state);
    }

    ItemStack incense = ItemStack.EMPTY;

    boolean isBurning;
    int burnCounter;
    IncenseRitual incenseRitual;

    public CenserTileEntity(BlockPos pos, BlockState state) {
        this(Registry.CENSER_TILE_ENTITY.get(), pos, state);
    }

    public boolean canStartBurning() {
        return !isBurning && !incense.isEmpty();
    }

    @Override
    public void onDestroyed(BlockState state, BlockPos pos) {
        super.onDestroyed(state, pos);
        if (!isBurning && !incense.isEmpty() && level != null) {
            // drop the incense item if the censer is destroyed
            level.addFreshEntity(new ItemEntity(level, pos.getX(), pos.getY(), pos.getZ(), incense));
        }
    }

    public void tick() {
        if (level == null) return;
        if (!level.isClientSide && isBurning && incense() != null) {
            burnCounter++;
            this.incense().tick(burnCounter);
            sync();
        }
        if (burnCounter == 80) {
            incense = ItemStack.EMPTY;
            sync();
        }
        if (level.isClientSide && isBurning && incense() != null) {
            incenseRitual.animateParticles(this, burnCounter);
        }
    }

    @Override
    public InteractionResult onActivated(BlockState state, BlockPos pos, Player player, InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND && level instanceof ServerLevel && !isBurning) {
            ItemStack itemInHand = player.getItemInHand(hand);
            if (itemInHand.isEmpty() && !incense.isEmpty()) {
                ItemHandlerHelper.giveItemToPlayer(player, incense);
                incense = ItemStack.EMPTY;
                if (!level.isClientSide) sync();
                return InteractionResult.SUCCESS;
            } else if (!itemInHand.isEmpty() && incense.isEmpty()) {
                if (IncenseRegistry.getIncenseRitual(itemInHand.getItem()) != null) {
                    incense = itemInHand.split(1);
                    if (!level.isClientSide) sync();
                    return InteractionResult.SUCCESS;
                }
            } else if (!itemInHand.isEmpty() && !incense.isEmpty()) {
                if (itemInHand.getItem() instanceof FlintAndSteelItem) {
                    if (!level.isClientSide && canStartBurning()) this.startBurning(player, level, pos);
                }
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    public void startBurning(Player player, @NotNull Level world, BlockPos pos) {
        // call the incense method to init/check and then the start method
        if (incense() != null && incense().start(player, this)) {
            isBurning = true;
            world.setBlockAndUpdate(getBlockPos(), getBlockState().setValue(LIT, isBurning));
            burnCounter = 0;
            sync();
        }
    }

    @Override
    public void load(@NotNull CompoundTag pTag) {
        super.load(pTag);
        if (pTag.contains("incense")) {
            incense = ItemStack.of(pTag.getCompound("incense"));
        } else incense = ItemStack.EMPTY;
        if (pTag.contains("incenseContext") && incense.isEmpty()) {
            incenseRitual = IncenseRitual.read(pTag);
            incenseRitual.start(null, this);
        }
        burnCounter = pTag.getInt("burnCounter");
        isBurning = pTag.getBoolean("isBurning");
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag pTag) {
        super.saveAdditional(pTag);
        if (!incense.isEmpty()) {
            pTag.put("incense", incense.save(new CompoundTag()));
        }
        if (incenseRitual != null) {
            incenseRitual.write(pTag);
        }
        pTag.putInt("burnCounter", burnCounter);
        pTag.putBoolean("isBurning", isBurning);
    }

    public void extinguish() {
        if (level instanceof ServerLevel) {
            isBurning = false;
            level.setBlockAndUpdate(getBlockPos(), getBlockState().setValue(LIT, isBurning));
            burnCounter = 0;
            incenseRitual = null;
            sync();
        }
    }

    public IncenseRitual incense() {
        // if there is an incense item in the censer, get the ritual associated with it
        if (incenseRitual == null && !incense.isEmpty()) {
            incenseRitual = IncenseRegistry.getIncenseRitual(incense.getItem());
        }
        return incenseRitual;
    }
}

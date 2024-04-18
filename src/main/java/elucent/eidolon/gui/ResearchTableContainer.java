package elucent.eidolon.gui;

import elucent.eidolon.api.research.Research;
import elucent.eidolon.api.research.ResearchTask;
import elucent.eidolon.common.tile.ResearchTableTileEntity;
import elucent.eidolon.mixin.AbstractContainerMenuMixin;
import elucent.eidolon.registries.Registry;
import elucent.eidolon.registries.Researches;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ResearchTableContainer extends AbstractContainerMenu implements ContainerListener {
    private final Container tile;
    private final ContainerData intArray;
    protected final List<ResearchTask> tasks;

    public ResearchTableContainer(int id, Inventory playerInventory) {
        this(id, playerInventory, new SimpleContainer(2), new SimpleContainerData(2));
    }

    public ResearchTableContainer(int id, Inventory playerInventory, Container inventory, ContainerData data) {
        super(Registry.RESEARCH_TABLE_CONTAINER.get(), id);
        this.tile = inventory;
        this.intArray = data;
        this.addSlot(new NotesSlot(inventory, 0, 58, 68));
        this.addSlot(new SealSlot(inventory, 1, 58, 32));
        this.addDataSlots(data);
        this.tasks = new ArrayList<>();
        
        for(int k = 0; k < 3; ++k) {
            for(int i1 = 0; i1 < 9; ++i1) {
                this.addSlot(new Slot(playerInventory, i1 + k * 9 + 9, 16 + i1 * 18, 142 + k * 18));
            }
        }

        for(int l = 0; l < 9; ++l) {
            this.addSlot(new Slot(playerInventory, l, 16 + l * 18, 200));
        }
        
        if (inventory instanceof ResearchTableTileEntity t) {
            t.addListener(this);
        }
        
        if (tile instanceof ResearchTableTileEntity) updateSlots();
    }

    protected void popSlot() {
        slots.remove(slots.size() - 1);
        List<ItemStack> lastSlots = ((AbstractContainerMenuMixin) this).getLastSlots();
        List<ItemStack> remoteSlots = ((AbstractContainerMenuMixin) this).getRemoteSlots();
        lastSlots.remove(lastSlots.size() - 1);
        remoteSlots.remove(remoteSlots.size() - 1);
    }

    @Override
    public void clicked(int pSlotId, int pButton, @NotNull ClickType pClickType, @NotNull Player pPlayer) {
        if (pSlotId >= slots.size()) return;
        super.clicked(pSlotId, pButton, pClickType, pPlayer);
    }
    
    public void updateSlots() {
        if (tile instanceof ResearchTableTileEntity t) {
            for (int i = 38; i < slots.size(); i ++) if (!slots.get(i).getItem().isEmpty()) {
                double d0 = t.getBlockPos().getY() + 1.3F;
                ItemEntity itementity = new ItemEntity(t.getLevel(), t.getBlockPos().getX() + 0.5, d0, t.getBlockPos().getZ() + 0.5, slots.get(i).getItem());
                itementity.setPickUpDelay(40);
                t.getLevel().addFreshEntity(itementity);
            }
        }
        while (slots.size() > 38) popSlot(); // Pare down to just the base 2 slots + player inventory.
        checkTask();
        if (tile instanceof ResearchTableTileEntity) this.broadcastFullState();
    }

    private void checkTask() {
        if (!slots.get(0).getItem().is(Registry.RESEARCH_NOTES.get()) || getProgress() > 0) return;
        // Slots don't appear when research is in progress.
        ItemStack stack = slots.get(0).getItem();
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("research")) return;
        Research r = Researches.find(new ResourceLocation(tag.getString("research")));
        if (r == null || tag.getInt("stepsDone") >= r.getStars()) return;
        List<ResearchTask> tasks = r.getTasks(getSeed(stack), tag.getInt("stepsDone"));
        for (int i = 0; i < tasks.size(); i++) {
            int x = 189, y = 17 + 36 * i;
            tasks.get(i).modifyContainer(this, x, y);
        }
    }

    @Override
    public void removed(@NotNull Player player) {
        super.removed(player);
        if (player instanceof ServerPlayer) {
            for (int i = 38; i < slots.size(); i++)
                if (!slots.get(i).getItem().isEmpty()) {
                    player.drop(slots.get(i).getItem(), false);
                }
        }
        if (this.tile instanceof ResearchTableTileEntity t) {
            t.removeListener(this);
        }
    }

    public boolean stillValid(@NotNull Player playerIn) {
        return this.tile.stillValid(playerIn);
    }

    public @NotNull ItemStack quickMoveStack(@NotNull Player playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            // we are in the player inventory
            if ((index < 0 || (index > 1 && index < 38))) {

                boolean placeInExtraSlots = false;
                for (int extra = 38; extra < slots.size(); extra++) {
                    placeInExtraSlots = placeInExtraSlots || this.slots.get(extra).mayPlace(itemstack1);
                }
                if (placeInExtraSlots) {
                    if (!this.moveItemStackTo(itemstack1, 38, slots.size(), false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (this.slots.get(0).mayPlace(itemstack1)) {
                    if (!this.moveItemStackTo(itemstack1, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (this.slots.get(1).mayPlace(itemstack1)) {
                    if (!this.moveItemStackTo(itemstack1, 1, 2, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index >= 2 && index < 29) {
                    if (!this.moveItemStackTo(itemstack1, 29, 38, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index >= 29) {
                    if (!this.moveItemStackTo(itemstack1, 2, 29, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.moveItemStackTo(itemstack1, 2, 38, false)) {
                    return ItemStack.EMPTY;
                }
            } else { // we are in the table slots
                if (!this.moveItemStackTo(itemstack1, 2, 38, false)) {
                    return ItemStack.EMPTY;
                }

                slot.onQuickCraft(itemstack1, itemstack);
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(playerIn, itemstack1);
        }

        return itemstack;
    }

    public int getProgress() {
        return this.intArray.get(0);
    }

    public int getSeed(ItemStack stack) {
        if (!stack.hasTag() || !stack.getTag().contains("seed")) return 0;
        return stack.getTag().getInt("seed");
    }

    @Override
    public void initializeContents(int id, List<ItemStack> items, @NotNull ItemStack carried) {
        this.slots.get(0).set(items.get(0).copy());
        updateSlots();

        try {
            super.initializeContents(id, items, carried);
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

    class NotesSlot extends Slot {
        public NotesSlot(Container iInventoryIn, int index, int xPosition, int yPosition) {
            super(iInventoryIn, index, xPosition, yPosition);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.is(Registry.RESEARCH_NOTES.get());
        }

        @Override
        public boolean mayPickup(@NotNull Player pPlayer) {
            if (getProgress() > 0) return false;
            return super.mayPickup(pPlayer);
        }

        @Override
        public void setChanged() {
            super.setChanged();
            if (tile instanceof ResearchTableTileEntity) updateSlots();
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }

    static class SealSlot extends Slot {
        public SealSlot(Container iInventoryIn, int index, int xPosition, int yPosition) {
            super(iInventoryIn, index, xPosition, yPosition);
        }

        public boolean mayPlace(ItemStack stack) {
            return stack.is(Registry.ARCANE_SEAL.get());
        }

        public int getMaxStackSize() {
            return 64;
        }
    }

    public void trySubmitGoal(Player player, int index) {
        if (slots.get(0).getItem().is(Registry.RESEARCH_NOTES.get())) {
            ItemStack stack = slots.get(0).getItem();
            if (!stack.hasTag() || !stack.getTag().contains("research")) return;
            Research r = Researches.find(new ResourceLocation(stack.getTag().getString("research")));
            if (r == null) return;
            List<ResearchTask> tasks = r.getTasks(getSeed(stack), stack.getTag().getInt("stepsDone"));
            if (tasks.size() < index) return;
            ResearchTask toComplete = tasks.get(index);
            int startingSlot = 38;
            for (int i = 0; i < index; i ++) startingSlot += tasks.get(i).getSlotCount();
            if (!toComplete.isComplete(this, player, startingSlot).complete()) return;
            toComplete.onComplete(this, player, startingSlot);
            this.setData(0, 200); // start progress countdown.
            this.broadcastChanges();
            this.updateSlots();
        }
    }

    public void tryStamp(Player player) {
        if (slots.get(0).getItem().is(Registry.RESEARCH_NOTES.get()) && slots.get(1).getItem().is(Registry.ARCANE_SEAL.get())) {
            ItemStack notes = slots.get(0).getItem();
            if (!notes.hasTag() || !notes.getTag().contains("research")) return;
            Research r = Researches.find(new ResourceLocation(notes.getTag().getString("research")));
            if (r == null) return;
            if (notes.getTag().getInt("stepsDone") < r.getStars()) return;

            slots.get(1).remove(1);
            ItemStack completed = new ItemStack(Registry.COMPLETED_RESEARCH.get());
            completed.getOrCreateTag().putString("research", r.getRegistryName().toString());
            slots.get(0).set(completed);
            this.updateSlots();
        }
    }

    @Override
    public void slotChanged(@NotNull AbstractContainerMenu menu, int slot, @NotNull ItemStack stack) {
        if (slot == 0) updateSlots();
    }

    @Override
    public void dataChanged(@NotNull AbstractContainerMenu menu, int slot, int value) {
        if (slot == 0 && (value == 0 || value == 200)) updateSlots();
    }
}

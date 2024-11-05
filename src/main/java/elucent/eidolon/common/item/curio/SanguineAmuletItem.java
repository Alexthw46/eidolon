package elucent.eidolon.common.item.curio;

import com.mojang.datafixers.util.Either;
import elucent.eidolon.registries.Registry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.SlotContext;

public class SanguineAmuletItem extends EidolonCurio {
    public SanguineAmuletItem(Properties properties) {
        super(properties);
        DistExecutor.unsafeCallWhenOn(Dist.CLIENT, () -> () -> {
            MinecraftForge.EVENT_BUS.addListener(SanguineAmuletItem::renderTooltip);
            return null;
        });
    }

    static int getCharge(ItemStack stack) {
        if (stack.hasTag()) {
            var tag = stack.getTag();
            if (tag != null && tag.contains("charge")) {
                return tag.getInt("charge");
            }
        }
        return 0;
    }

    static void addCharge(ItemStack stack, int diff) {
        int newCharge = Mth.clamp(getCharge(stack) + diff, 0, 40);
        stack.getOrCreateTag().putInt("charge", newCharge);
    }

    static void setCharge(ItemStack stack, int charge) {
        int newCharge = Mth.clamp(charge, 0, 40);
        stack.getOrCreateTag().putInt("charge", newCharge);
    }


    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        if (!entity.level().isClientSide) {
            if (entity.tickCount % 80 == 0 &&
                entity.getHealth() >= entity.getMaxHealth() - 0.0001 &&
                entity instanceof Player player && player.getFoodData().getFoodLevel() >= 18 &&
                getCharge(stack) < 40) {
                float f = player.getFoodData().getSaturationLevel() > 0 ?
                        Math.min(4 * player.getFoodData().getSaturationLevel(), 16.0F) : 4.0f;
                player.causeFoodExhaustion(f);
                addCharge(stack, 1);
            }
            if (entity.tickCount % 10 == 0 &&
                getCharge(stack) > 0 && entity.getHealth() < entity.getMaxHealth()) {
                int taken = (int) Math.min(1, entity.getMaxHealth() - entity.getHealth());
                addCharge(stack, -taken);
                entity.heal(taken);
            }
        }
    }

    @Override
    public boolean canSync(SlotContext slotContext, ItemStack stack) {
        return true;
    }


    @NotNull
    @Override
    public CompoundTag writeSyncData(SlotContext slotContext, ItemStack stack) {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("charge", getCharge(stack));
        return nbt;
    }

    @Override
    public void readSyncData(SlotContext slotContext, CompoundTag compound, ItemStack stack) {
        setCharge(stack, compound.getInt("charge"));
    }


    public static class SanguineAmuletTooltipInfo implements TooltipComponent {
        final ItemStack stack;
        final int maxWidth;

        public SanguineAmuletTooltipInfo(ItemStack stack, int maxWidth) {
            this.stack = stack;
            this.maxWidth = maxWidth;
        }
    }

    public static class SanguineAmuletTooltipComponent implements ClientTooltipComponent {
        final ItemStack stack;
        final int maxWidth;

        public SanguineAmuletTooltipComponent(SanguineAmuletTooltipInfo info) {
            this.stack = info.stack;
            this.maxWidth = info.maxWidth;
        }

        @Override
        public int getHeight() {
            int charge = getCharge(stack);
            int rows = (charge + 19) / 20;
            return 8 + 12 * rows;
        }

        @Override
        public int getWidth(@NotNull Font font) {
            return maxWidth;
        }

        @Override
        public void renderImage(@NotNull Font font, int x, int y, @NotNull GuiGraphics pGuiGraphics) {
            Minecraft mc = Minecraft.getInstance();
            int charge = getCharge(stack);
            int rows = (charge + 19) / 20;
            for (int i = 0; i < charge; i += 20) {
                for (int j = 0; j < Mth.clamp(charge - i, 0, 20); j += 2) {
                    if (charge - (i + j) == 1) {
                        pGuiGraphics.blit(new ResourceLocation("minecraft", "textures/gui/icons.png"), x - 1 + j / 2 * 8, y + (i / 20) * 9 + 2, 61, 0, 9, 9, 256, 256);
                    } else
                        pGuiGraphics.blit(new ResourceLocation("minecraft", "textures/gui/icons.png"), x - 1 + j / 2 * 8, y + (i / 20) * 9 + 2, 52, 0, 9, 9, 256, 256);
                }
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void renderTooltip(RenderTooltipEvent.GatherComponents event) {
        ItemStack stack = event.getItemStack();
        if (stack.getItem() == Registry.SANGUINE_AMULET.get()) {
            event.getTooltipElements().add(Either.right(new SanguineAmuletTooltipInfo(stack, event.getMaxWidth())));
        }
    }
}

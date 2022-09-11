package gregtech.api.util;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SlotDelegate extends Slot {

    private final Slot delegate;

    public SlotDelegate(Slot delegate) {
        super(delegate.inventory, delegate.slotNumber, delegate.xPos, delegate.yPos);
        this.delegate = delegate;
    }

    @Override
    public void onSlotChange(@NotNull ItemStack p_75220_1_, @NotNull ItemStack p_75220_2_) {
        this.delegate.onSlotChange(p_75220_1_, p_75220_2_);
    }

    @NotNull
    @Override
    public ItemStack onTake(@NotNull EntityPlayer thePlayer, @NotNull ItemStack stack) {
        return this.delegate.onTake(thePlayer, stack);
    }

    @Override
    public boolean isItemValid(@NotNull ItemStack stack) {
        return this.delegate.isItemValid(stack);
    }

    @NotNull
    @Override
    public ItemStack getStack() {
        return this.delegate.getStack();
    }

    @Override
    public boolean getHasStack() {
        return this.delegate.getHasStack();
    }

    @Override
    public void putStack(@NotNull ItemStack stack) {
        this.delegate.putStack(stack);
    }

    @Override
    public void onSlotChanged() {
        this.delegate.onSlotChanged();
    }

    @Override
    public int getSlotStackLimit() {
        return this.delegate.getSlotStackLimit();
    }

    @Override
    public int getItemStackLimit(@NotNull ItemStack stack) {
        return this.delegate.getItemStackLimit(stack);
    }

    @NotNull
    @Override
    public ItemStack decrStackSize(int amount) {
        return this.delegate.decrStackSize(amount);
    }

    @Override
    public boolean canTakeStack(@NotNull EntityPlayer playerIn) {
        return this.delegate.canTakeStack(playerIn);
    }

    @Override
    public boolean isEnabled() {
        return this.delegate.isEnabled();
    }

    @Nullable
    @Override
    public TextureAtlasSprite getBackgroundSprite() {
        return this.delegate.getBackgroundSprite();
    }

    @NotNull
    @Override
    public ResourceLocation getBackgroundLocation() {
        return this.delegate.getBackgroundLocation();
    }

    @Nullable
    @Override
    public String getSlotTexture() {
        return this.delegate.getSlotTexture();
    }

    @Override
    public void setBackgroundLocation(@NotNull ResourceLocation texture) {
        this.delegate.setBackgroundLocation(texture);
    }

    @Override
    public void setBackgroundName(@Nullable String name) {
        this.delegate.setBackgroundName(name);
    }
}

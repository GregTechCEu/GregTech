package gregtech.common.covers;

import gregtech.api.GregTechAPI;
import gregtech.api.cover.*;
import gregtech.api.util.GTTransferUtils;
import gregtech.api.util.GTUtility;
import gregtech.common.inventory.handlers.ToolItemStackHandler;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * @deprecated Will be removed
 */
@ApiStatus.ScheduledForRemoval(inVersion = "2.9")
@Deprecated
public class CoverCraftingTable extends CoverBase implements ITickable {

    private static final ResourceLocation STORAGE_COVER_LOCATION = GTUtility.gregtechId("storage");
    private static @Nullable CoverDefinition storageCoverDefinition = null;
    private static boolean attemptedStorageCoverLookup = false;

    private final ItemStackHandler internalInventory = new ItemStackHandler(18);
    private final ItemStackHandler toolInventory = new ToolItemStackHandler(9);

    public CoverCraftingTable(@NotNull CoverDefinition definition, @NotNull CoverableView coverableView,
                              @NotNull EnumFacing attachedSide) {
        super(definition, coverableView, attachedSide);
    }

    @Override
    public boolean canAttach(@NotNull CoverableView coverable, @NotNull EnumFacing side) {
        return true;
    }

    @Override
    public boolean shouldAutoConnectToPipes() {
        return false;
    }

    @Override
    public void renderCover(@NotNull CCRenderState renderState, @NotNull Matrix4 translation,
                            IVertexOperation[] pipeline, @NotNull Cuboid6 plateBox, @NotNull BlockRenderLayer layer) {}

    @Override
    public void update() {
        if (getWorld().isRemote) {
            return;
        }
        CoverableView coverableView = getCoverableView();
        if (coverableView instanceof CoverHolder holder) {
            EnumFacing coverSide = getAttachedSide();
            holder.removeCover(coverSide);

            if (!attemptedStorageCoverLookup) {
                storageCoverDefinition = GregTechAPI.COVER_REGISTRY.getObject(STORAGE_COVER_LOCATION);
                attemptedStorageCoverLookup = true;
            }

            if (storageCoverDefinition == null) {
                // Could not find a storage cover to convert to
                // so drop contents on the ground instead
                dropContents();
                return;
            }

            Cover cover = storageCoverDefinition.createCover(holder, coverSide);
            if (!holder.canPlaceCoverOnSide(coverSide) || !cover.canAttach(holder, coverSide)) {
                // could not attach for some reason
                // so drop contents on the ground instead
                dropContents();
                return;
            }

            holder.addCover(coverSide, cover);
            cover.onAttachment(holder, coverSide, null, storageCoverDefinition.getDropItemStack());

            IItemHandler itemHandler;
            if (cover instanceof CoverStorage coverStorage) {
                itemHandler = coverStorage.getStorageHandler();
            } else {
                itemHandler = null;
            }

            if (itemHandler == null) {
                // could not retrieve item handler from the cover
                // so drop contents on the ground instead
                dropContents();
                return;
            }

            // transfer what can fit into the storage cover
            GTTransferUtils.moveInventoryItems(internalInventory, itemHandler);
            GTTransferUtils.moveInventoryItems(toolInventory, itemHandler);

            // drop everything else on the ground
            dropContents();
        }
    }

    private void dropContents() {
        dropInventoryContents(internalInventory);
        dropInventoryContents(toolInventory);
    }

    @Override
    public @NotNull List<ItemStack> getDrops() {
        return Collections.emptyList();
    }

    @Override
    public void writeToNBT(@NotNull NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setTag("ToolInventory", toolInventory.serializeNBT());
        tagCompound.setTag("InternalInventory", internalInventory.serializeNBT());
    }

    @Override
    public void readFromNBT(@NotNull NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        this.toolInventory.deserializeNBT(tagCompound.getCompoundTag("ToolInventory"));
        this.internalInventory.deserializeNBT(tagCompound.getCompoundTag("InternalInventory"));
    }
}

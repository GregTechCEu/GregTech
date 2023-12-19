package gregtech.api.cover;

import gregtech.api.GregTechAPI;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class CoverDefinition {

    private final ResourceLocation resourceLocation;
    private final CoverCreator coverCreator;
    private final ItemStack dropItemStack;

    public CoverDefinition(@NotNull ResourceLocation resourceLocation, @NotNull CoverCreator coverCreator,
                           @NotNull ItemStack dropItemStack) {
        this.resourceLocation = resourceLocation;
        this.coverCreator = coverCreator;
        this.dropItemStack = dropItemStack.copy();
    }

    /**
     * @param id the cover's id
     * @return the cover associated with the id
     */
    public static @Nullable CoverDefinition getCoverById(@NotNull ResourceLocation id) {
        return GregTechAPI.COVER_REGISTRY.getObject(id);
    }

    /**
     * @param networkId the cover's network id
     * @return the cover definition associated with the id
     * @see CoverSaveHandler for an existing implementation
     */
    public static @Nullable CoverDefinition getCoverByNetworkId(int networkId) {
        return GregTechAPI.COVER_REGISTRY.getObjectById(networkId);
    }

    /**
     * @param definition the cover's definition
     * @return the network id associated with the cover
     * @see CoverSaveHandler for an existing implementation
     */
    public static int getNetworkIdForCover(@NotNull CoverDefinition definition) {
        return GregTechAPI.COVER_REGISTRY.getIDForObject(definition);
    }

    public @NotNull ResourceLocation getResourceLocation() {
        return resourceLocation;
    }

    public @NotNull ItemStack getDropItemStack() {
        return dropItemStack.copy();
    }

    public @NotNull Cover createCover(@NotNull CoverableView coverableView, @NotNull EnumFacing side) {
        return coverCreator.create(this, coverableView, side);
    }

    @FunctionalInterface
    public interface CoverCreator {

        @NotNull
        Cover create(@NotNull CoverDefinition definition, @NotNull CoverableView coverableView,
                     @NotNull EnumFacing attachedSide);
    }
}

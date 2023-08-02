package gregtech.api.cover2;

import gregtech.api.GregTechAPI;
import gregtech.api.cover.CoverIO;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class CoverDefinition2 {

    private final ResourceLocation coverId;
    private final CoverCreator coverCreator;
    private final ItemStack dropItemStack;

    public CoverDefinition2(@NotNull ResourceLocation coverId, @NotNull CoverCreator coverCreator, @NotNull ItemStack dropItemStack) {
        this.coverId = coverId;
        this.coverCreator = coverCreator;
        this.dropItemStack = dropItemStack.copy();
    }

    /**
     * @param id the cover's id
     * @return the cover associated with the id
     */
    public static @Nullable CoverDefinition2 getCoverById(@NotNull ResourceLocation id) {
        return GregTechAPI.COVER_REGISTRY_2.getObject(id);
    }

    /**
     * @param networkId the cover's network id
     * @return the cover definition associated with the id
     * @see CoverIO for an existing implementation
     */
    public static @Nullable CoverDefinition2 getCoverByNetworkId(int networkId) {
        return GregTechAPI.COVER_REGISTRY_2.getObjectById(networkId);
    }

    /**
     * @param definition the cover's definition
     * @return the network id associated with the cover
     * @see CoverIO for an existing implementation
     */
    public static int getNetworkIdForCover(@NotNull CoverDefinition2 definition) {
        return GregTechAPI.COVER_REGISTRY_2.getIDForObject(definition);
    }

    public @NotNull ResourceLocation getCoverId() {
        return coverId;
    }

    public @NotNull ItemStack getDropItemStack() {
        return dropItemStack.copy();
    }

    public @NotNull Cover createCover(@NotNull CoverableView coverableView, @NotNull EnumFacing side) {
        return coverCreator.create(this, coverableView, side);
    }

    @FunctionalInterface
    public interface CoverCreator {

        @NotNull Cover create(@NotNull CoverDefinition2 definition, @NotNull CoverableView coverableView,
                              @NotNull EnumFacing attachedSide);
    }
}

package gregtech.api.cover;

import gregtech.api.GregTechAPI;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.BiFunction;

public final class CoverDefinition {

    private final ResourceLocation coverId;
    private final BiFunction<ICoverable, EnumFacing, CoverBehavior> behaviorCreator;
    private final ItemStack dropItemStack;

    public CoverDefinition(ResourceLocation coverId, BiFunction<ICoverable, EnumFacing, CoverBehavior> behaviorCreator, @Nonnull ItemStack dropItemStack) {
        this.coverId = coverId;
        this.behaviorCreator = behaviorCreator;
        this.dropItemStack = dropItemStack.copy();
    }

    /**
     * @param id the cover's id
     * @return the cover associated with the id
     */
    public static CoverDefinition getCoverById(@Nonnull ResourceLocation id) {
        return GregTechAPI.COVER_REGISTRY.getObject(id);
    }

    /**
     * @param networkId the cover's network id
     * @return the cover definition associated with the id
     * @see CoverIO for an existing implementation
     */
    @Nullable
    public static CoverDefinition getCoverByNetworkId(int networkId) {
        return GregTechAPI.COVER_REGISTRY.getObjectById(networkId);
    }

    /**
     * @param definition the cover's definition
     * @return the network id associated with the cover
     * @see CoverIO for an existing implementation
     */
    public static int getNetworkIdForCover(@Nonnull CoverDefinition definition) {
        return GregTechAPI.COVER_REGISTRY.getIDForObject(definition);
    }

    public ResourceLocation getCoverId() {
        return coverId;
    }

    @Nonnull
    public ItemStack getDropItemStack() {
        return dropItemStack.copy();
    }

    @Nonnull
    public CoverBehavior createCoverBehavior(ICoverable metaTileEntity, EnumFacing side) {
        CoverBehavior coverBehavior = behaviorCreator.apply(metaTileEntity, side);
        coverBehavior.setCoverDefinition(this);
        return coverBehavior;
    }
}

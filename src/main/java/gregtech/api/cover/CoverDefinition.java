package gregtech.api.cover;

import gregtech.api.GregTechAPI;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.function.BiFunction;

public final class CoverDefinition {

    private final ResourceLocation coverId;
    private final BiFunction<ICoverable, EnumFacing, CoverBehavior> behaviorCreator;
    private final ItemStack dropItemStack;

    public CoverDefinition(ResourceLocation coverId, BiFunction<ICoverable, EnumFacing, CoverBehavior> behaviorCreator, ItemStack dropItemStack) {
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
     * @see CoverIO
     * @deprecated Use {@link CoverDefinition#getCoverById(ResourceLocation)}
     */
    @Deprecated
    public static CoverDefinition getCoverByNetworkId(int networkId) {
        return null;
    }

    /**
     * @see CoverIO
     * @deprecated Use {@link CoverDefinition#getCoverId()} with {@link ResourceLocation#toString()}
     */
    @Deprecated
    public static int getNetworkIdForCover(CoverDefinition definition) {
        return Integer.MAX_VALUE;
    }

    public ResourceLocation getCoverId() {
        return coverId;
    }

    public ItemStack getDropItemStack() {
        return dropItemStack.copy();
    }

    public CoverBehavior createCoverBehavior(ICoverable metaTileEntity, EnumFacing side) {
        CoverBehavior coverBehavior = behaviorCreator.apply(metaTileEntity, side);
        coverBehavior.setCoverDefinition(this);
        return coverBehavior;
    }
}

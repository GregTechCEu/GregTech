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

    public CoverDefinition(ResourceLocation coverId, BiFunction<ICoverable, EnumFacing, CoverBehavior> behaviorCreator, @Nonnull ItemStack dropItemStack) {
        this.coverId = coverId;
        this.behaviorCreator = behaviorCreator;
        this.dropItemStack = dropItemStack.copy();
    }

    /**
     * @deprecated use {@link GregTechAPI#COVER_REGISTRY} and {@link gregtech.api.registry.GTSimpleRegistry#getObject(Object)}
     */
    @Deprecated
    public static CoverDefinition getCoverById(@Nonnull ResourceLocation id) {
        return GregTechAPI.COVER_REGISTRY.getObject(id);
    }

    /**
     * @see CoverIO
     * @deprecated use {@link GregTechAPI#COVER_REGISTRY} and {@link gregtech.api.registry.GTSimpleRegistry#getObject(Object)}
     */
    @Deprecated
    public static CoverDefinition getCoverByNetworkId(@SuppressWarnings("unused") int networkId) {
        return null;
    }

    /**
     * @see CoverIO
     * @deprecated Use {@link CoverDefinition#getCoverId()} with {@link ResourceLocation#toString()}
     */
    @Deprecated
    public static int getNetworkIdForCover(@SuppressWarnings("unused") CoverDefinition definition) {
        return Integer.MAX_VALUE;
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

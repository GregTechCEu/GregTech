package gregtech.common.pipelike.itempipe.net;

import gregtech.api.pipenet.IRoutePath;
import gregtech.api.unification.material.properties.ItemPipeProperties;
import gregtech.api.util.FacingPos;
import gregtech.common.pipelike.itempipe.tile.TileEntityItemPipe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Predicate;

public class ItemRoutePath implements IRoutePath<TileEntityItemPipe> {

    private final TileEntityItemPipe targetPipe;
    private final EnumFacing faceToHandler;
    private final int distance;
    private final ItemPipeProperties properties;
    private final Predicate<ItemStack> filters;

    public ItemRoutePath(TileEntityItemPipe targetPipe, EnumFacing facing, int distance, ItemPipeProperties properties, List<Predicate<ItemStack>> filters) {
        this.targetPipe = targetPipe;
        this.faceToHandler = facing;
        this.distance = distance;
        this.properties = properties;
        this.filters = stack -> {
            for (Predicate<ItemStack> filter : filters)
                if (!filter.test(stack)) return false;
            return true;
        };
    }

    @Nonnull
    @Override
    public TileEntityItemPipe getTargetPipe() {
        return targetPipe;
    }

    @Nonnull
    @Override
    public EnumFacing getTargetFacing() {
        return faceToHandler;
    }

    @Override
    public int getDistance() {
        return distance;
    }

    public ItemPipeProperties getProperties() {
        return properties;
    }

    public boolean matchesFilters(ItemStack stack) {
        return filters.test(stack);
    }

    public IItemHandler getHandler() {
        return getTargetCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
    }

    public FacingPos toFacingPos() {
        return new FacingPos(getTargetPipePos(), faceToHandler);
    }
}

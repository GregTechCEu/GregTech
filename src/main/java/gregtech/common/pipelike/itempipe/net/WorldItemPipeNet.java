package gregtech.common.pipelike.itempipe.net;

import gregtech.api.cover.Cover;
import gregtech.api.pipenet.AbstractEdgePredicate;
import gregtech.api.pipenet.WorldPipeNetG;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.unification.material.properties.ItemPipeProperties;
import gregtech.common.covers.CoverConveyor;
import gregtech.common.covers.CoverItemFilter;
import gregtech.common.covers.ItemFilterMode;
import gregtech.common.covers.ManualImportExportMode;
import gregtech.common.pipelike.itempipe.ItemPipeType;
import gregtech.common.pipelike.itempipe.tile.TileEntityItemPipe;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class WorldItemPipeNet extends WorldPipeNetG<ItemPipeProperties, ItemPipeType> {

    private static final String DATA_ID = "gregtech.item_pipe_net";

    public static WorldItemPipeNet getWorldPipeNet(World world) {
        WorldItemPipeNet netWorldData = (WorldItemPipeNet) world.loadData(WorldItemPipeNet.class, DATA_ID);
        if (netWorldData == null) {
            netWorldData = new WorldItemPipeNet(DATA_ID);
            world.setData(DATA_ID, netWorldData);
        }
        netWorldData.setWorldAndInit(world);
        return netWorldData;
    }

    public WorldItemPipeNet(String name) {
        super(name, true, false);
    }

    @Override
    protected Class<? extends IPipeTile<ItemPipeType, ItemPipeProperties>> getBasePipeClass() {
        return TileEntityItemPipe.class;
    }

    @Override
    protected AbstractEdgePredicate<?> getPredicate(Cover thisCover, Cover neighbourCover) {
        ItemEdgePredicate predicate = new ItemEdgePredicate();
        if (thisCover instanceof CoverItemFilter filter &&
                filter.getFilterMode() != ItemFilterMode.FILTER_INSERT) {
            predicate.setSourceFilter(filter.getFilterContainer());
        }
        if (neighbourCover instanceof CoverItemFilter filter &&
                filter.getFilterMode() != ItemFilterMode.FILTER_EXTRACT) {
            predicate.setTargetFilter(filter.getFilterContainer());
        }
        if (thisCover instanceof CoverConveyor conveyor) {
            if (conveyor.getManualImportExportMode() == ManualImportExportMode.DISABLED) {
                predicate.setShutteredSource(true);
            } else if (conveyor.getManualImportExportMode() == ManualImportExportMode.FILTERED) {
                predicate.setSourceFilter(conveyor.getItemFilterContainer());
            }
        }
        if (neighbourCover instanceof CoverConveyor conveyor) {
            if (conveyor.getManualImportExportMode() == ManualImportExportMode.DISABLED) {
                predicate.setShutteredTarget(true);
            } else if (conveyor.getManualImportExportMode() == ManualImportExportMode.FILTERED) {
                predicate.setTargetFilter(conveyor.getItemFilterContainer());
            }
        }
        // TODO should robot arms apply rate limits to edge predicates?
        return shutterify(predicate, thisCover, neighbourCover);
    }

    @Override
    protected void writeNodeData(ItemPipeProperties nodeData, NBTTagCompound tagCompound) {
        tagCompound.setInteger("Priority", nodeData.getPriority());
        tagCompound.setFloat("Rate", nodeData.getTransferRate());
    }

    @Override
    protected ItemPipeProperties readNodeData(NBTTagCompound tagCompound) {
        return new ItemPipeProperties(tagCompound.getInteger("Priority"), tagCompound.getFloat("Rate"));
    }
}

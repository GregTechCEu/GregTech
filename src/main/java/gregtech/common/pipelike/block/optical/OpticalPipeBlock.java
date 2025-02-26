package gregtech.common.pipelike.block.optical;

import gregtech.api.graphnet.pipenet.IPipeNetNodeHandler;
import gregtech.api.graphnet.pipenet.physical.block.PipeActivableBlock;
import gregtech.api.graphnet.pipenet.physical.tile.PipeTileEntity;
import gregtech.api.items.toolitem.ToolClasses;
import gregtech.common.creativetab.GTCreativeTabs;
import gregtech.common.pipelike.handlers.OpticalNetHandler;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

public class OpticalPipeBlock extends PipeActivableBlock {

    public OpticalPipeBlock(OpticalStructure structure) {
        super(structure);
        setCreativeTab(GTCreativeTabs.TAB_GREGTECH_PIPES);
    }

    @Override
    public String getToolClass() {
        return ToolClasses.WIRE_CUTTER;
    }

    @Override
    protected String getConnectLangKey() {
        return "gregtech.tool_action.wire_cutter.connect";
    }

    @Override
    protected boolean allowsBlocking() {
        return false;
    }

    @Override
    @NotNull
    public IPipeNetNodeHandler getHandler(PipeTileEntity tileContext) {
        return OpticalNetHandler.INSTANCE;
    }

    @Override
    protected @NotNull IPipeNetNodeHandler getHandler(@NotNull ItemStack stack) {
        return OpticalNetHandler.INSTANCE;
    }
}

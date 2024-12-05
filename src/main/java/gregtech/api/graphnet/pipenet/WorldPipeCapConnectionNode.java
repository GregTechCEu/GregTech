package gregtech.api.graphnet.pipenet;

import gregtech.api.GTValues;
import gregtech.api.graphnet.GraphClassType;
import gregtech.api.graphnet.net.IGraphNet;
import gregtech.api.graphnet.net.NetNode;
import gregtech.api.util.FacingPos;
import gregtech.api.util.GTLog;
import gregtech.api.util.GTUtility;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WorldPipeCapConnectionNode extends NetNode implements NodeWithFacingToOthers, NodeExposingCapabilities {

    public static final int SORTING_KEY = 432;

    public static final GraphClassType<WorldPipeCapConnectionNode> TYPE = new GraphClassType<>(GTValues.MODID,
            "WorldPipeCapConnectionNode",
            WorldPipeCapConnectionNode::resolve);

    private FacingPos posAndFacing;

    public WorldPipeCapConnectionNode(WorldPipeNet net) {
        super(net);
        sortingKey = SORTING_KEY;
    }

    private static WorldPipeCapConnectionNode resolve(IGraphNet net) {
        if (net instanceof WorldPipeNet w) return new WorldPipeCapConnectionNode(w);
        GTLog.logger.fatal(
                "Attempted to initialize a WorldPipeCapConnectionNode to a non-WorldPipeNet. If relevant NPEs occur later, this is most likely the cause.");
        return null;
    }

    public WorldPipeNode getParent() {
        return getNet().getNode(getEquivalencyData().getPos());
    }

    public WorldPipeCapConnectionNode setPosAndFacing(FacingPos posAndFacing) {
        this.posAndFacing = posAndFacing;
        return this;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = super.serializeNBT();
        tag.setLong("Pos", posAndFacing.getPos().toLong());
        tag.setByte("Facing", (byte) posAndFacing.getFacing().getIndex());
        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        super.deserializeNBT(nbt);
        this.setPosAndFacing(new FacingPos(BlockPos.fromLong(nbt.getLong("Pos")),
                EnumFacing.byIndex(nbt.getByte("Facing"))));
    }

    @Override
    public @NotNull WorldPipeNet getNet() {
        return (WorldPipeNet) super.getNet();
    }

    @Override
    public @NotNull FacingPos getEquivalencyData() {
        return posAndFacing;
    }

    @Override
    public @NotNull GraphClassType<? extends WorldPipeCapConnectionNode> getType() {
        return TYPE;
    }

    @Override
    public @Nullable EnumFacing getFacingToOther(@NotNull NetNode other) {
        if (other instanceof WorldPipeNode n && GTUtility.arePosEqual(n.getEquivalencyData(), posAndFacing.getPos()))
            return posAndFacing.getFacing().getOpposite();
        else return null;
    }

    @Override
    public ICapabilityProvider getProvider() {
        WorldPipeNode parent = getParent();
        return parent.getTileEntity().getTargetWithCapabilities(parent, posAndFacing.getFacing());
    }

    @Override
    public EnumFacing exposedFacing() {
        return posAndFacing.getFacing().getOpposite();
    }
}

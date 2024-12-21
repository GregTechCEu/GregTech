package gregtech.api.graphnet.pipenet.physical.tile;

import gregtech.api.graphnet.net.NetNode;
import gregtech.api.graphnet.pipenet.WorldPipeCapConnectionNode;
import gregtech.api.graphnet.pipenet.WorldPipeNode;
import gregtech.api.graphnet.pipenet.physical.IPipeCapabilityObject;
import gregtech.api.util.FacingPos;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;

public class NodeManagingPCW extends PipeCapabilityWrapper {

    private final EnumMap<EnumFacing, WorldPipeCapConnectionNode> managed = new EnumMap<>(EnumFacing.class);

    public NodeManagingPCW(@NotNull PipeTileEntity owner, @NotNull WorldPipeNode node,
                           Object2ObjectMap<Capability<?>, IPipeCapabilityObject> capabilities, int inactiveKey,
                           int activeKey) {
        super(owner, node, capabilities, inactiveKey, activeKey);
    }

    @Override
    public void invalidate() {
        for (WorldPipeCapConnectionNode n : managed.values()) {
            n.getNet().removeNode(n);
        }
    }

    @Override
    protected void setActiveInternal(@NotNull EnumFacing facing) {
        super.setActiveInternal(facing);
        FacingPos pos = new FacingPos(node.getEquivalencyData(), facing);
        NetNode existing = node.getNet().getNode(pos);
        WorldPipeCapConnectionNode connectionNode;
        if (existing instanceof WorldPipeCapConnectionNode c) {
            connectionNode = c;
        } else {
            connectionNode = new WorldPipeCapConnectionNode(node.getNet());
            connectionNode.setPosAndFacing(pos);
            connectionNode.getNet().addNode(connectionNode);
        }
        managed.put(facing, connectionNode);
        node.getNet().addEdge(node, connectionNode, true);
    }

    @Override
    protected void setIdleInternal(@NotNull EnumFacing facing) {
        super.setIdleInternal(facing);
        WorldPipeCapConnectionNode n = managed.remove(facing);
        if (n != null) node.getNet().removeNode(n);
    }

    public @Nullable WorldPipeCapConnectionNode getNodeForFacing(EnumFacing facing) {
        return managed.get(facing);
    }
}

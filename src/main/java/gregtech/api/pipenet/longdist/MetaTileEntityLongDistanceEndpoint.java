package gregtech.api.pipenet.longdist;

import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.IDataInfoProvider;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public abstract class MetaTileEntityLongDistanceEndpoint extends MetaTileEntity implements IDataInfoProvider {

    public static MetaTileEntityLongDistanceEndpoint tryGet(World world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof IGregTechTileEntity) {
            MetaTileEntity mte = ((IGregTechTileEntity) te).getMetaTileEntity();
            if (mte instanceof MetaTileEntityLongDistanceEndpoint) {
                return (MetaTileEntityLongDistanceEndpoint) mte;
            }
        }
        return null;
    }

    private final LongDistancePipeType pipeType;
    private MetaTileEntityLongDistanceEndpoint link;

    public MetaTileEntityLongDistanceEndpoint(ResourceLocation metaTileEntityId, LongDistancePipeType pipeType) {
        super(metaTileEntityId);
        this.pipeType = pipeType;
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return null;
    }

    @Override
    public void onAttached(Object... data) {
        if (getWorld() == null || getWorld().isRemote) return;
        List<LongDistanceNetwork> networks = new ArrayList<>();
        BlockPos.PooledMutableBlockPos offsetPos = BlockPos.PooledMutableBlockPos.retain();
        for (EnumFacing facing : EnumFacing.VALUES) {
            offsetPos.setPos(getPos()).move(facing);
            LongDistanceNetwork network = LongDistanceNetwork.get(getWorld(), offsetPos);
            if (network != null && pipeType == network.getPipeType()) {
                networks.add(network);
            }
        }
        offsetPos.release();

        if (networks.isEmpty()) {
            LongDistanceNetwork network = this.pipeType.createNetwork(getWorld());
            network.onPlaceEndpoint(this);
        } else if (networks.size() == 1) {
            networks.get(0).onPlaceEndpoint(this);
        }
    }

    @Override
    public void onRemoval() {
        if (link != null) {
            link.invalidateLink();
            invalidateLink();
        }
        LongDistanceNetwork.get(getWorld(), getPos()).onRemoveEndpoint(this);
    }

    @Override
    public void onNeighborChanged() {
        List<LongDistanceNetwork> networks = new ArrayList<>();
        BlockPos.PooledMutableBlockPos offsetPos = BlockPos.PooledMutableBlockPos.retain();
        for (EnumFacing facing : EnumFacing.VALUES) {
            offsetPos.setPos(getPos()).move(facing);
            LongDistanceNetwork network = LongDistanceNetwork.get(getWorld(), offsetPos);
            if (network != null && network.getPipeType() == this.pipeType) {
                networks.add(network);
            }
        }
        LongDistanceNetwork network = LongDistanceNetwork.get(getWorld(), getPos());
        if (network == null) {
            if (networks.isEmpty()) {
                network = this.pipeType.createNetwork(getWorld());
                network.onPlaceEndpoint(this);
            } else if (networks.size() == 1) {
                networks.get(0).onPlaceEndpoint(this);
            }
        } else {
            if (networks.size() > 1) {
                network.onRemoveEndpoint(this);
            }
        }
    }

    public MetaTileEntityLongDistanceEndpoint getLink() {
        if (link == null) {
            LongDistanceNetwork network = LongDistanceNetwork.get(getWorld(), getPos());
            if (network != null && !network.isCalculating() && network.isValid()) {
                this.link = network.getOtherEndpoint(this);
            }
        }
        return this.link;
    }

    public void invalidateLink() {
        this.link = null;
    }

    public EnumFacing getOutputFacing() {
        return getFrontFacing().getOpposite();
    }

    public LongDistancePipeType getPipeType() {
        return pipeType;
    }

    @Nonnull
    @Override
    public List<ITextComponent> getDataInfo() {
        List<ITextComponent> textComponents = new ArrayList<>();
        LongDistanceNetwork network = LongDistanceNetwork.get(getWorld(), getPos());
        if (network == null) {
            textComponents.add(new TextComponentString("No network found"));
        } else {
            textComponents.add(new TextComponentString("Network:"));
            textComponents.add(new TextComponentString(" - " + network.longDistancePipeBlocks.size() + " pipes"));
            MetaTileEntityLongDistanceEndpoint ep1 = network.getFirstEndpoint(), ep2 = network.getSecondEndpoint();
            textComponents.add(new TextComponentString(" - endpoint 1: " + (ep1 == null ? "none" : ep1.getPos())));
            textComponents.add(new TextComponentString(" - endpoint 2: " + (ep2 == null ? "none" : ep2.getPos())));
        }
        return textComponents;
    }
}

package gregtech.common.metatileentities.storage;

import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.IDataInfoProvider;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.pipenet.longdist.ILDEndpoint;
import gregtech.api.pipenet.longdist.LongDistanceNetwork;
import gregtech.api.pipenet.longdist.LongDistancePipeType;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public abstract class MetaTileEntityLongDistanceEndpoint extends MetaTileEntity implements ILDEndpoint, IDataInfoProvider {

    private final LongDistancePipeType pipeType;
    private Type type = Type.NONE;
    private ILDEndpoint link;
    private boolean placed = false;

    public MetaTileEntityLongDistanceEndpoint(ResourceLocation metaTileEntityId, LongDistancePipeType pipeType) {
        super(metaTileEntityId);
        this.pipeType = pipeType;
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return null;
    }

    public void updateNetwork() {
        LongDistanceNetwork network = LongDistanceNetwork.get(getWorld(), getPos());
        if (network != null) {
            if (network.getTotalSize() == 1) {
                return;
            }
            network.onRemoveEndpoint(this);
        }

        List<LongDistanceNetwork> networks = findNetworks();
        if (networks.isEmpty()) {
            network = this.pipeType.createNetwork(getWorld());
            network.onPlaceEndpoint(this);
            setType(Type.NONE);
        } else if (networks.size() == 1) {
            networks.get(0).onPlaceEndpoint(this);
        } else {
            setType(Type.NONE);
        }
    }

    @Override
    public void setFrontFacing(EnumFacing frontFacing) {
        this.placed = true;
        super.setFrontFacing(frontFacing);
        if (getWorld() != null && !getWorld().isRemote) {
            updateNetwork();
        }
    }

    @Override
    public void onRemoval() {
        if (link != null) {
            link.invalidateLink();
            invalidateLink();
        }
        setType(Type.NONE);
        LongDistanceNetwork.get(getWorld(), getPos()).onRemoveEndpoint(this);
    }

    @Override
    public void onNeighborChanged() {
        if (!placed || getWorld() == null || getWorld().isRemote) return;

        List<LongDistanceNetwork> networks = findNetworks();
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
        if (networks.size() != 1) {
            setType(Type.NONE);
        }
    }

    private List<LongDistanceNetwork> findNetworks() {
        List<LongDistanceNetwork> networks = new ArrayList<>();
        LongDistanceNetwork network;
        // only check input and output side
        network = LongDistanceNetwork.get(getWorld(), getPos().offset(getFrontFacing()));
        if (network != null && pipeType == network.getPipeType()) {
            networks.add(network);
            setType(Type.OUTPUT);
        }
        network = LongDistanceNetwork.get(getWorld(), getPos().offset(getOutputFacing()));
        if (network != null && pipeType == network.getPipeType()) {
            networks.add(network);
            setType(Type.INPUT);
        }
        return networks;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        if (pipeType.getMinLength() > 0) {
            tooltip.add(I18n.format("gregtech.machine.endpoint.tooltip.min_length", pipeType.getMinLength()));
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        NBTTagCompound nbt = super.writeToNBT(data);
        data.setByte("Type", (byte) type.ordinal());
        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.type = Type.values()[data.getByte("Type")];
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public void setType(Type type) {
        this.type = type;
    }

    @Override
    public ILDEndpoint getLink() {
        if (link == null) {
            LongDistanceNetwork network = LongDistanceNetwork.get(getWorld(), getPos());
            if (network != null && network.isValid()) {
                this.link = network.getOtherEndpoint(this);
            }
        }
        return this.link;
    }

    @Override
    public void invalidateLink() {
        this.link = null;
    }

    @Override
    public EnumFacing getOutputFacing() {
        return getFrontFacing().getOpposite();
    }

    @Override
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
            ILDEndpoint in = network.getActiveInputIndex(), out = network.getActiveOutputIndex();
            textComponents.add(new TextComponentString(" - input: " + (in == null ? "none" : in.getPos())));
            textComponents.add(new TextComponentString(" - output: " + (out == null ? "none" : out.getPos())));
        }
        if (isInput()) {
            textComponents.add(new TextComponentString("Input endpoint"));
        }
        if (isOutput()) {
            textComponents.add(new TextComponentString("Output endpoint"));
        }
        return textComponents;
    }
}

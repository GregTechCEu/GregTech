package gregtech.common.metatileentities.storage;

import gregtech.api.GTValues;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.IDataInfoProvider;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.pipenet.longdist.ILDEndpoint;
import gregtech.api.pipenet.longdist.LongDistanceNetwork;
import gregtech.api.pipenet.longdist.LongDistancePipeType;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public abstract class MetaTileEntityLongDistanceEndpoint extends MetaTileEntity implements ILDEndpoint, IDataInfoProvider {

    private final LongDistancePipeType pipeType;
    private ILDEndpoint link;
    private boolean placed = false;
    private byte type = 0;

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

        // only check input and output side
        List<LongDistanceNetwork> networks = new ArrayList<>();
        network = LongDistanceNetwork.get(getWorld(), getPos().offset(getFrontFacing()));
        if (network != null && pipeType == network.getPipeType()) {
            networks.add(network);
            setOutput();
        }
        network = LongDistanceNetwork.get(getWorld(), getPos().offset(getOutputFacing()));
        if (network != null && pipeType == network.getPipeType()) {
            networks.add(network);
            setInput();
        }

        if (networks.isEmpty()) {
            network = this.pipeType.createNetwork(getWorld());
            network.onPlaceEndpoint(this);
            setUnknown();
        } else if (networks.size() == 1) {
            networks.get(0).onPlaceEndpoint(this);
        } else {
            setUnknown();
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
        setUnknown();
        LongDistanceNetwork.get(getWorld(), getPos()).onRemoveEndpoint(this);
    }

    @Override
    public void onNeighborChanged() {
        if (!placed || getWorld() == null || getWorld().isRemote) return;
        List<LongDistanceNetwork> networks = new ArrayList<>();
        LongDistanceNetwork network;
        // only check input and output side
        network = LongDistanceNetwork.get(getWorld(), getPos().offset(getFrontFacing()));
        if (network != null && pipeType == network.getPipeType()) {
            networks.add(network);
            setOutput();
        }
        network = LongDistanceNetwork.get(getWorld(), getPos().offset(getOutputFacing()));
        if (network != null && pipeType == network.getPipeType()) {
            networks.add(network);
            setInput();
        }

        network = LongDistanceNetwork.get(getWorld(), getPos());
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
            setUnknown();
        }
    }

    @Override
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(Textures.VOLTAGE_CASINGS[GTValues.LV].getParticleSprite(), 0xFFFFFF);
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
        data.setByte("Type", type);
        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.type = data.getByte("Type");
    }

    @Override
    public void setInput() {
        this.type = 1;
    }

    @Override
    public void setOutput() {
        this.type = 2;
    }

    @Override
    public void setUnknown() {
        this.type = 0;
    }

    @Override
    public boolean isInput() {
        return this.type == 1;
    }

    @Override
    public boolean isOutput() {
        return this.type == 2;
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

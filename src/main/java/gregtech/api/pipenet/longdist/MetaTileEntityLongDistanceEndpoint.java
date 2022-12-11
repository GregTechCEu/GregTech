package gregtech.api.pipenet.longdist;

import gregtech.api.GTValues;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.IDataInfoProvider;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
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
    private boolean placed = false;

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
        this.placed = true;
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
        }
        network = LongDistanceNetwork.get(getWorld(), getPos().offset(getOutputFacing()));
        if (network != null && pipeType == network.getPipeType()) {
            networks.add(network);
        }

        if (networks.isEmpty()) {
            network = this.pipeType.createNetwork(getWorld());
            network.onPlaceEndpoint(this);
        } else if (networks.size() == 1) {
            networks.get(0).onPlaceEndpoint(this);
        }
    }

    @Override
    public void setFrontFacing(EnumFacing frontFacing) {
        boolean changed = getFrontFacing() != frontFacing;
        super.setFrontFacing(frontFacing);
        if ((changed || placed) && getWorld() != null && !getWorld().isRemote) {
            updateNetwork();
        }
        this.placed = false;
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
        LongDistanceNetwork network;
        // only check input and output side
        network = LongDistanceNetwork.get(getWorld(), getPos().offset(getFrontFacing()));
        if (network != null && pipeType == network.getPipeType()) {
            networks.add(network);
        }
        network = LongDistanceNetwork.get(getWorld(), getPos().offset(getOutputFacing()));
        if (network != null && pipeType == network.getPipeType()) {
            networks.add(network);
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
        if (pipeType.allowOnlyStraight()) {
            tooltip.add(I18n.format("gregtech.machine.endpoint.tooltip.straight"));
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

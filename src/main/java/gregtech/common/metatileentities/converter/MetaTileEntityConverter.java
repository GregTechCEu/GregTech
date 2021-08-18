package gregtech.common.metatileentities.converter;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.GTValues;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.tool.ISoftHammerItem;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.metatileentity.ITieredMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.render.Textures;
import gregtech.api.util.PipelineUtil;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class MetaTileEntityConverter extends MetaTileEntity implements ITieredMetaTileEntity {

    private final ConverterTrait converterTrait;

    private final int tier;
    private final int slots;

    public MetaTileEntityConverter(ResourceLocation metaTileEntityId, int tier, int amps) {
        super(metaTileEntityId);
        this.tier = tier;
        this.slots = amps;
        converterTrait = new ConverterTrait(this, tier, amps, true);
        initializeInventory();
    }

    public MetaTileEntityConverter(ResourceLocation metaTileEntityId, int tier, ConverterTrait trait) {
        super(metaTileEntityId);
        this.tier = tier;
        converterTrait = trait.copyNew(this);
        this.slots = converterTrait.getBaseAmps();
        initializeInventory();
    }

    @Override
    public boolean onRightClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
        ItemStack stack = playerIn.getHeldItem(hand);
        if(!stack.isEmpty()) {
            ISoftHammerItem softHammer = stack.getCapability(GregtechCapabilities.CAPABILITY_MALLET, null);
            if(softHammer != null && softHammer.damageItem(1, false)) {
                if(!getWorld().isRemote) {
                    converterTrait.invertMode();
                    writeCustomData(-9, buf -> buf.writeBoolean(converterTrait.isFeToEu()));
                    notifyBlockUpdate();
                    markDirty();
                }
                return true;
            }
        }

        return super.onRightClick(playerIn, hand, facing, hitResult);
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        if(dataId == -9) {
            converterTrait.setMode(buf.readBoolean());
            getHolder().scheduleChunkForRenderUpdate();
        }
        super.receiveCustomData(dataId, buf);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityConverter(metaTileEntityId, tier, converterTrait);
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        buf.writeBoolean(converterTrait.isFeToEu());
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        converterTrait.setMode(buf.readBoolean());
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        Textures.VOLTAGE_CASINGS[getTier()].render(renderState, translation, pipeline);
        if (converterTrait.isFeToEu()) {
            for(EnumFacing facing : EnumFacing.values()) {
                if(facing == frontFacing)
                    Textures.ENERGY_OUT.renderSided(facing, renderState, translation, PipelineUtil.color(pipeline, GTValues.VC[getTier()]));
                else
                    Textures.CONVERTER_FE_IN.renderSided(facing, renderState, translation, pipeline);
            }
        } else {
            for(EnumFacing facing : EnumFacing.values()) {
                if(facing == frontFacing)
                    Textures.CONVERTER_FE_IN.renderSided(facing, renderState, translation, pipeline);
                else
                    Textures.ENERGY_IN.renderSided(facing, renderState, translation, PipelineUtil.color(pipeline, GTValues.VC[getTier()]));
            }
        }
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        int rowSize = (int) Math.sqrt(converterTrait.getBaseAmps());
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 176,
                18 + 18 * rowSize + 94)
                .label(10, 5, getMetaFullName());

        for (int y = 0; y < rowSize; y++) {
            for (int x = 0; x < rowSize; x++) {
                int index = y * rowSize + x;
                builder.widget(new SlotWidget(importItems, index, 89 - rowSize * 9 + x * 18, 18 + y * 18, true, true)
                        .setBackgroundTexture(GuiTextures.SLOT, GuiTextures.BATTERY_OVERLAY));
            }
        }
        builder.bindPlayerInventory(entityPlayer.inventory, GuiTextures.SLOT, 7, 18 + 18 * rowSize + 12);
        return builder.build(getHolder(), entityPlayer);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        boolean feToEu = converterTrait.isFeToEu();
        if (capability == GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER) {
            if (!feToEu && (side != frontFacing || side == null))
                return GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER.cast(converterTrait.getEnergyEUContainer());
            else if (feToEu && side == frontFacing)
                return GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER.cast(converterTrait.getEnergyEUContainer());
            return null;
        }
        if (capability == CapabilityEnergy.ENERGY) {
            if (feToEu && (side != frontFacing || side == null))
                return CapabilityEnergy.ENERGY.cast(converterTrait.getEnergyFEContainer());
            else if (!feToEu && side == frontFacing)
                return CapabilityEnergy.ENERGY.cast(converterTrait.getEnergyFEContainer());
            return null;
        }
        return super.getCapability(capability, side);
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return new ItemStackHandler(slots) {
            @Nonnull
            @Override
            public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
                if (converterTrait.getBatteryContainer(stack) == null)
                    return stack; //do not allow to insert non-battery items
                return super.insertItem(slot, stack, simulate);
            }

            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }
        };
    }

    @Override
    public int getTier() {
        return tier;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        long voltage = converterTrait.getVoltage();
        long amps = converterTrait.getBaseAmps();
        tooltip.add(I18n.format("gregtech.machine.energy_converter.tooltip"));
        if(converterTrait.isFeToEu()) {
            tooltip.add(I18n.format("gregtech.universal.tooltip.voltage_out", voltage, GTValues.VN[tier]));
            tooltip.add(I18n.format("gregtech.universal.tooltip.amperage_out", amps));
        } else {
            tooltip.add(I18n.format("gregtech.universal.tooltip.voltage_in", voltage, GTValues.VN[tier]));
            tooltip.add(I18n.format("gregtech.universal.tooltip.amperage_in", amps));
        }
        tooltip.add(I18n.format("gregtech.universal.tooltip.energy_storage_capacity", converterTrait.getEnergyEUContainer().getEnergyCapacity()));
    }


}

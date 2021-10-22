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
import gregtech.common.ConfigHolder;
import gregtech.common.tools.DamageValues;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static gregtech.api.capability.GregtechDataCodes.SYNC_TILE_MODE;

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
        ItemStack itemStack = playerIn.getHeldItem(hand);
        if (!itemStack.isEmpty() && itemStack.hasCapability(GregtechCapabilities.CAPABILITY_MALLET, null)) {
            ISoftHammerItem softHammerItem = itemStack.getCapability(GregtechCapabilities.CAPABILITY_MALLET, null);

            if (getWorld().isRemote) {
                scheduleRenderUpdate();
                return true;
            }

            if (!softHammerItem.damageItem(DamageValues.DAMAGE_FOR_SOFT_HAMMER, false))
                return false;

            if (converterTrait.isFeToEu()) {
                setConversionMode(false);
                playerIn.sendMessage(new TextComponentTranslation("gregtech.machine.energy_converter.message_conversion_eu",
                        converterTrait.getBaseAmps(), converterTrait.getVoltage(), (long) (ConfigHolder.U.energyOptions.euToFeRatio * converterTrait.getVoltage() * converterTrait.getBaseAmps())));
            } else {
                setConversionMode(true);
                playerIn.sendMessage(new TextComponentTranslation("gregtech.machine.energy_converter.message_conversion_fe",
                        (long) (ConfigHolder.U.energyOptions.feToEuRatio * converterTrait.getVoltage() * converterTrait.getBaseAmps()), converterTrait.getBaseAmps(), converterTrait.getVoltage()));
            }
            return true;
        }

        return super.onRightClick(playerIn, hand, facing, hitResult);
    }

    public void setConversionMode(boolean inverted) {
        converterTrait.setMode(inverted);
        if (!getWorld().isRemote) {
            writeCustomData(SYNC_TILE_MODE, b -> b.writeBoolean(converterTrait.isFeToEu()));
            getHolder().notifyBlockUpdate();
            markDirty();
        }
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        if(dataId == SYNC_TILE_MODE) {
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
        super.writeInitialSyncData(buf);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        converterTrait.setMode(buf.readBoolean());
        super.receiveInitialSyncData(buf);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        Textures.VOLTAGE_CASINGS[getTier()].render(renderState, translation, pipeline);
        if (converterTrait.isFeToEu()) {
            for (EnumFacing facing : EnumFacing.values()) {
                if (facing == frontFacing)
                    Textures.CONVERTER_FE_IN.renderSided(facing, renderState, translation, pipeline);
                else
                    Textures.ENERGY_OUT.renderSided(facing, renderState, translation, PipelineUtil.color(pipeline, GTValues.VC[getTier()]));
            }
        } else {
            for (EnumFacing facing : EnumFacing.values()) {
                if (facing == frontFacing)
                    Textures.CONVERTER_FE_OUT.renderSided(facing, renderState, translation, pipeline);
                else
                    Textures.ENERGY_IN.renderSided(facing, renderState, translation, PipelineUtil.color(pipeline, GTValues.VC[getTier()]));
            }
        }
    }

    @Override
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(Textures.VOLTAGE_CASINGS[getTier()].getParticleSprite(), getPaintingColor());
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
        tooltip.add(I18n.format("gregtech.machine.energy_converter.tooltip_tool_usage"));
        tooltip.add(I18n.format("gregtech.universal.tooltip.energy_storage_capacity", converterTrait.getEnergyEUContainer().getEnergyCapacity()));
        tooltip.add(I18n.format("gregtech.machine.energy_converter.tooltip_conversion_fe", (long) (ConfigHolder.U.energyOptions.feToEuRatio * voltage * amps), amps, voltage, GTValues.VN[tier]));
        tooltip.add(I18n.format("gregtech.machine.energy_converter.tooltip_conversion_eu", amps, voltage, GTValues.VN[tier], (long) (ConfigHolder.U.energyOptions.euToFeRatio * voltage * amps)));
    }


}

package gregtech.common.metatileentities.converter;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.GTValues;
import gregtech.api.capability.FeCompat;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.tool.ISoftHammerItem;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.metatileentity.ITieredMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.utils.PipelineUtil;
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
                setFeToEu(false);
                playerIn.sendMessage(new TextComponentTranslation("gregtech.machine.energy_converter.message_conversion_eu",
                        converterTrait.getBaseAmps(), converterTrait.getVoltage(), FeCompat.toFe(converterTrait.getVoltage() * converterTrait.getBaseAmps(), false)));
            } else {
                setFeToEu(true);
                playerIn.sendMessage(new TextComponentTranslation("gregtech.machine.energy_converter.message_conversion_fe",
                        FeCompat.toFe(converterTrait.getVoltage() * converterTrait.getBaseAmps(), true), converterTrait.getBaseAmps(), converterTrait.getVoltage()));
            }
            return true;
        }

        return super.onRightClick(playerIn, hand, facing, hitResult);
    }

    public void setFeToEu(boolean feToEu) {
        converterTrait.setFeToEu(feToEu);
        if (!getWorld().isRemote) {
            writeCustomData(SYNC_TILE_MODE, b -> b.writeBoolean(converterTrait.isFeToEu()));
            getHolder().notifyBlockUpdate();
            markDirty();
        }
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        if (dataId == SYNC_TILE_MODE) {
            converterTrait.setFeToEu(buf.readBoolean());
            getHolder().scheduleChunkForRenderUpdate();
        }
        super.receiveCustomData(dataId, buf);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityConverter(metaTileEntityId, tier, slots);
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        buf.writeBoolean(converterTrait.isFeToEu());
        super.writeInitialSyncData(buf);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        converterTrait.setFeToEu(buf.readBoolean());
        super.receiveInitialSyncData(buf);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        Textures.VOLTAGE_CASINGS[getTier()].render(renderState, translation, pipeline);
        if (converterTrait.isFeToEu()) {
            for (EnumFacing facing : EnumFacing.values()) {
                if (facing == frontFacing)
                    Textures.ENERGY_OUT.renderSided(facing, renderState, translation, PipelineUtil.color(pipeline, GTValues.VC[getTier()]));
                else
                    Textures.CONVERTER_FE_IN.renderSided(facing, renderState, translation, pipeline);
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
        int slotWidth, slotHeight;
        if (slots == 8) {
            slotWidth = 4;
            slotHeight = 2;
        } else {
            slotWidth = slotHeight = (int) Math.sqrt(converterTrait.getBaseAmps());
        }
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 176,
                        18 + 18 * slotHeight + 94)
                .label(10, 5, getMetaFullName());

        for (int y = 0; y < slotHeight; y++) {
            for (int x = 0; x < slotWidth; x++) {
                builder.widget(new SlotWidget(importItems, y * slotWidth + x, 89 - slotWidth * 9 + x * 18, 18 + y * 18, true, true)
                        .setBackgroundTexture(GuiTextures.SLOT, GuiTextures.BATTERY_OVERLAY));
            }
        }
        builder.bindPlayerInventory(entityPlayer.inventory, GuiTextures.SLOT, 7, 18 + 18 * slotHeight + 12);
        return builder.build(getHolder(), entityPlayer);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER) {
            return converterTrait.isFeToEu() == (side == frontFacing) ?
                    GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER.cast(converterTrait.getEnergyEUContainer()) : null;
        }
        if (capability == CapabilityEnergy.ENERGY) {
            return side != (converterTrait.isFeToEu() ? frontFacing : null) ?
                    CapabilityEnergy.ENERGY.cast(converterTrait.getEnergyFEContainer()) : null;
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
    public boolean isValidFrontFacing(EnumFacing facing) {
        return true;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        long voltage = converterTrait.getVoltage();
        long amps = converterTrait.getBaseAmps();
        tooltip.add(I18n.format("gregtech.machine.energy_converter.tooltip_tool_usage"));
        tooltip.add(I18n.format("gregtech.universal.tooltip.item_storage_capacity", slots));
        tooltip.add(I18n.format("gregtech.universal.tooltip.energy_storage_capacity", converterTrait.getEnergyEUContainer().getEnergyCapacity()));
        tooltip.add(I18n.format("gregtech.machine.energy_converter.tooltip_conversion_fe", FeCompat.toFe(voltage * amps, true), amps, voltage, GTValues.VNF[tier]));
        tooltip.add(I18n.format("gregtech.machine.energy_converter.tooltip_conversion_eu", amps, voltage, GTValues.VNF[tier], FeCompat.toFe(voltage * amps, false)));
    }
}

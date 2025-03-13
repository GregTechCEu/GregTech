package gregtech.common.metatileentities.storage;

import gregtech.api.capability.impl.FilteredFluidHandler;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.items.itemhandlers.GTItemStackHandler;
import gregtech.api.metatileentity.ITieredMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuis;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.utils.RenderUtil;
import gregtech.common.mui.widget.GTFluidSlot;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.BoolValue;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandlers;
import com.cleanroommc.modularui.widgets.ItemSlot;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.ToggleButton;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.layout.Grid;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static gregtech.api.capability.GregtechDataCodes.*;

public class MetaTileEntityBuffer extends MetaTileEntity implements ITieredMetaTileEntity {

    private final int tier;
    private FluidTankList fluidTankList;
    private ItemStackHandler itemStackHandler;
    private boolean autoOutputItems;
    private boolean autoOutputFluids;
    private EnumFacing outputFacingItems;
    private EnumFacing outputFacingFluids;

    public MetaTileEntityBuffer(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId);
        this.tier = tier;
        initializeInventory();
    }

    @Override
    protected void initializeInventory() {
        super.initializeInventory();
        FilteredFluidHandler[] fluidHandlers = new FilteredFluidHandler[tier + 2];
        for (int i = 0; i < tier + 2; i++) {
            fluidHandlers[i] = new FilteredFluidHandler((int) (2000 * Math.pow(2, getTier())));
        }
        fluidInventory = fluidTankList = new FluidTankList(false, fluidHandlers);
        itemInventory = itemStackHandler = new GTItemStackHandler(this, ((int) Math.pow(tier + 2, 2)));
    }

    @Override
    public boolean needsSneakToRotate() {
        return true;
    }

    @Override
    public IItemHandlerModifiable getExportItems() {
        return itemStackHandler;
    }

    @Override
    public FluidTankList getExportFluids() {
        return fluidTankList;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityBuffer(metaTileEntityId, tier);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(Textures.VOLTAGE_CASINGS[tier].getParticleSprite(), this.getPaintingColorForRendering());
    }

    @Override
    public boolean usesMui2() {
        return true;
    }

    @Override
    public ModularPanel buildUI(PosGuiData guiData, PanelSyncManager guiSyncManager) {
        int invTier = tier + 2;
        guiSyncManager.registerSlotGroup("item_inv", invTier);

        List<List<IWidget>> slotWidgets = new ArrayList<>();
        for (int y = 0; y < invTier; y++) {
            slotWidgets.add(new ArrayList<>());
            for (int x = 0; x < invTier; x++) {
                int index = y * invTier + x;
                slotWidgets.get(y)
                        .add(new ItemSlot().slot(SyncHandlers.itemSlot(itemStackHandler, index).slotGroup("item_inv")));
            }
        }

        BooleanSyncValue workingStateValueItems = new BooleanSyncValue(this::isAutoOutputItems,
                this::setAutoOutputItems);
        guiSyncManager.syncValue("working_state_items", workingStateValueItems);
        BooleanSyncValue workingStateValueFluids = new BooleanSyncValue(this::isAutoOutputFluids,
                this::setAutoOutputFluids);
        guiSyncManager.syncValue("working_state_fluids", workingStateValueFluids);

        List<List<IWidget>> tankWidgets = new ArrayList<>();
        for (int i = 0; i < this.fluidTankList.getTanks(); i++) {
            tankWidgets.add(new ArrayList<>());
            tankWidgets.get(i).add(new GTFluidSlot().syncHandler(this.fluidTankList.getTankAt(i)));
        }

        // TODO: Change the position of the name when it's standardized.
        return GTGuis.createPanel(this, 176, 18 + Math.max(166, 112 + 18 * invTier))
                .child(IKey.lang(getMetaFullName()).asWidget().pos(5, 5))
                .child(SlotGroupWidget.playerInventory().left(7).bottom(7))
                .child(new Grid()
                        .top(18).height(18 * invTier)
                        .left(7)
                        .minElementMargin(0, 0)
                        .minColWidth(18).minRowHeight(18)
                        .matrix(slotWidgets))
                .child(new Grid()
                        .top(18).height(18 * invTier)
                        .left(144 + 7)
                        .minElementMargin(0, 0)
                        .minColWidth(18).minRowHeight(18)
                        .matrix(tankWidgets))
                .child(Flow.row().pos(7, 18 * invTier + 23).width(36).height(18)
                        .child(new ToggleButton()
                                .value(new BoolValue.Dynamic(workingStateValueItems::getBoolValue,
                                        workingStateValueItems::setBoolValue))
                                .overlay(GTGuiTextures.BUTTON_ITEM_OUTPUT))
                        .child(new ToggleButton()
                                .value(new BoolValue.Dynamic(workingStateValueFluids::getBoolValue,
                                        workingStateValueFluids::setBoolValue))
                                .overlay(GTGuiTextures.BUTTON_FLUID_OUTPUT)));
    }

    @Override
    public int getTier() {
        return tier;
    }

    public boolean isAutoOutputItems() {
        return autoOutputItems;
    }

    public boolean isAutoOutputFluids() {
        return autoOutputFluids;
    }

    public void setAutoOutputItems(boolean autoOutputItems) {
        this.autoOutputItems = autoOutputItems;
        if (!getWorld().isRemote) {
            writeCustomData(UPDATE_AUTO_OUTPUT_ITEMS, buf -> buf.writeBoolean(autoOutputItems));
            markDirty();
        }
    }

    public void setAutoOutputFluids(boolean autoOutputFluids) {
        this.autoOutputFluids = autoOutputFluids;
        if (!getWorld().isRemote) {
            writeCustomData(UPDATE_AUTO_OUTPUT_FLUIDS, buf -> buf.writeBoolean(autoOutputFluids));
            markDirty();
        }
    }

    @Override
    public boolean onWrenchClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing,
                                 CuboidRayTraceResult hitResult) {
        if (!playerIn.isSneaking()) {
            if (getOutputFacingItems() == facing) return false;
            if (hasFrontFacing() && facing == getFrontFacing()) return false;
            if (!getWorld().isRemote) {
                setOutputFacing(facing);
            }
            return true;
        }
        return super.onWrenchClick(playerIn, hand, facing, hitResult);
    }

    public void setOutputFacing(EnumFacing outputFacing) {
        this.outputFacingItems = outputFacing;
        this.outputFacingFluids = outputFacing;
        if (!getWorld().isRemote) {
            notifyBlockUpdate();
            writeCustomData(UPDATE_OUTPUT_FACING, buf -> {
                buf.writeByte(outputFacingItems.getIndex());
                buf.writeByte(outputFacingFluids.getIndex());
            });
            markDirty();
        }
    }

    @Override
    public void setFrontFacing(EnumFacing frontFacing) {
        super.setFrontFacing(frontFacing);
        if (this.outputFacingItems == null || this.outputFacingFluids == null) {
            setOutputFacing(frontFacing.getOpposite());
        }
    }

    public EnumFacing getOutputFacingItems() {
        return outputFacingItems == null ? getFrontFacing().getOpposite() : outputFacingItems;
    }

    public EnumFacing getOutputFacingFluids() {
        return outputFacingFluids == null ? getFrontFacing().getOpposite() : outputFacingFluids;
    }

    @Override
    public void update() {
        super.update();
        if (!getWorld().isRemote) {
            if (getOffsetTimer() % 5 == 0) {
                if (isAutoOutputFluids()) {
                    pushFluidsIntoNearbyHandlers(getOutputFacingFluids());
                }
                if (isAutoOutputItems()) {
                    pushItemsIntoNearbyHandlers(getOutputFacingItems());
                }
            }
        }
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        Textures.VOLTAGE_CASINGS[tier].render(renderState, translation, ArrayUtils.add(pipeline,
                new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering()))));
        for (EnumFacing facing : EnumFacing.VALUES) {
            if (facing == getOutputFacingItems()) {
                Textures.PIPE_OUT_OVERLAY.renderSided(facing, renderState, translation, pipeline);
            } else {
                Textures.BUFFER_OVERLAY.renderSided(facing, renderState, translation, pipeline);
            }
        }
        if (isAutoOutputItems() && outputFacingItems != null) {
            Textures.ITEM_OUTPUT_OVERLAY.renderSided(outputFacingItems, renderState,
                    RenderUtil.adjustTrans(translation, outputFacingItems, 2), pipeline);
        }
        if (isAutoOutputFluids() && outputFacingFluids != null) {
            Textures.FLUID_OUTPUT_OVERLAY.renderSided(outputFacingFluids, renderState,
                    RenderUtil.adjustTrans(translation, outputFacingFluids, 2), pipeline);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setTag("Inventory", itemStackHandler.serializeNBT());
        tag.setTag("FluidInventory", fluidTankList.serializeNBT());
        tag.setInteger("OutputFacing", getOutputFacingItems().getIndex());
        tag.setInteger("OutputFacingF", getOutputFacingFluids().getIndex());
        tag.setBoolean("AutoOutputItems", autoOutputItems);
        tag.setBoolean("AutoOutputFluids", autoOutputFluids);
        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        this.itemStackHandler.deserializeNBT(tag.getCompoundTag("Inventory"));
        this.fluidTankList.deserializeNBT(tag.getCompoundTag("FluidInventory"));
        this.outputFacingItems = EnumFacing.VALUES[tag.getInteger("OutputFacing")];
        this.outputFacingFluids = EnumFacing.VALUES[tag.getInteger("OutputFacingF")];
        this.autoOutputItems = tag.getBoolean("AutoOutputItems");
        this.autoOutputFluids = tag.getBoolean("AutoOutputFluids");
    }

    @Override
    public void writeInitialSyncData(@NotNull PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(autoOutputFluids);
        buf.writeBoolean(autoOutputItems);
        buf.writeByte(getOutputFacingFluids().getIndex());
        buf.writeByte(getOutputFacingItems().getIndex());
    }

    @Override
    public void receiveInitialSyncData(@NotNull PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.autoOutputFluids = buf.readBoolean();
        this.autoOutputItems = buf.readBoolean();
        this.outputFacingFluids = EnumFacing.VALUES[buf.readByte()];
        this.outputFacingItems = EnumFacing.VALUES[buf.readByte()];
    }

    @Override
    public void receiveCustomData(int dataId, @NotNull PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == UPDATE_OUTPUT_FACING) {
            this.outputFacingItems = EnumFacing.VALUES[buf.readByte()];
            this.outputFacingFluids = EnumFacing.VALUES[buf.readByte()];
            scheduleRenderUpdate();
        } else if (dataId == UPDATE_AUTO_OUTPUT_ITEMS) {
            this.autoOutputItems = buf.readBoolean();
            scheduleRenderUpdate();
        } else if (dataId == UPDATE_AUTO_OUTPUT_FLUIDS) {
            this.autoOutputFluids = buf.readBoolean();
            scheduleRenderUpdate();
        }
    }

    @Override
    protected boolean shouldSerializeInventories() {
        return true;
    }

    @Override
    public boolean hasFrontFacing() {
        return true;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, @NotNull List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.machine.buffer.tooltip"));
        tooltip.add(I18n.format("gregtech.universal.tooltip.item_storage_capacity", (int) Math.pow(tier + 2, 2)));
        tooltip.add(I18n.format("gregtech.universal.tooltip.fluid_storage_capacity_mult", tier + 2,
                (int) (1000 * Math.pow(2, tier))));
    }

    @Override
    public void addToolUsages(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.tool_action.screwdriver.auto_output_covers"));
        tooltip.add(I18n.format("gregtech.tool_action.wrench.set_facing"));
        super.addToolUsages(stack, world, tooltip, advanced);
    }

    @Override
    public void clearMachineInventory(@NotNull List<@NotNull ItemStack> itemBuffer) {
        clearInventory(itemBuffer, itemStackHandler);
    }
}
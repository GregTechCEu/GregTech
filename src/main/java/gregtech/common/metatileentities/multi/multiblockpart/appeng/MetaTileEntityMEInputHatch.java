package gregtech.common.metatileentities.multi.multiblockpart.appeng;

import gregtech.api.capability.IDataStickIntractable;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.AbilityInstances;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.sync.appeng.AEFluidSyncHandler;
import gregtech.api.mui.sync.appeng.AESyncHandler;
import gregtech.api.mui.widget.appeng.fluid.AEFluidConfigSlot;
import gregtech.api.mui.widget.appeng.fluid.AEFluidDisplaySlot;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.slot.ExportOnlyAEFluidList;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.slot.ExportOnlyAEFluidSlot;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.stack.WrappedFluidStack;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.IFluidTank;

import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.layout.Grid;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.Arrays;
import java.util.List;

public class MetaTileEntityMEInputHatch extends MetaTileEntityMEInputBase<IAEFluidStack>
                                        implements IMultiblockAbilityPart<IFluidTank>, IDataStickIntractable {

    public static final String FLUID_BUFFER_TAG = "FluidTanks";

    public MetaTileEntityMEInputHatch(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier, false, IFluidStorageChannel.class);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityMEInputHatch(this.metaTileEntityId, getTier());
    }

    @Override
    protected void initializeInventory() {
        super.initializeInventory();
        this.importFluids = new FluidTankList(false, getAEHandler().getInventory());
    }

    @Override
    protected @NotNull ExportOnlyAEFluidList initializeAEHandler() {
        return new ExportOnlyAEFluidList(this, CONFIG_SIZE, this.getController());
    }

    @Override
    public @NotNull ExportOnlyAEFluidList getAEHandler() {
        return (ExportOnlyAEFluidList) aeHandler;
    }

    @Override
    protected @NotNull AESyncHandler<IAEFluidStack> createAESyncHandler() {
        return new AEFluidSyncHandler(getAEHandler(), this::markDirty, circuitInventory::setCircuitValue);
    }

    @Override
    protected @NotNull Widget<?> createMainColumnWidget(@Range(from = 0, to = 3) int index, @NotNull PosGuiData guiData,
                                                        @NotNull PanelSyncManager panelSyncManager) {
        return switch (index) {
            case 2 -> new Widget<>()
                    .size(18);
            case 3 -> createGhostCircuitWidget();
            default -> super.createMainColumnWidget(index, guiData, panelSyncManager);
        };
    }

    @Override
    protected @NotNull Widget<?> createConfigGrid(@NotNull PosGuiData guiData,
                                                  @NotNull PanelSyncManager panelSyncManager) {
        Grid grid = new Grid()
                .pos(7, 25)
                .size(18 * 4)
                .minElementMargin(0, 0)
                .minColWidth(18)
                .minRowHeight(18)
                .matrix(Grid.mapToMatrix((int) Math.sqrt(CONFIG_SIZE), CONFIG_SIZE,
                        index -> new AEFluidConfigSlot(isStocking(), index, this::isAutoPull)
                                .syncHandler(SYNC_HANDLER_NAME, 0)
                                .debugName("Index " + index)));

        for (IWidget slotUpper : grid.getChildren()) {
            ((AEFluidConfigSlot) slotUpper).onSelect(() -> {
                for (IWidget slotLower : grid.getChildren()) {
                    ((AEFluidConfigSlot) slotLower).deselect();
                }
            });
        }

        return grid;
    }

    @Override
    protected @NotNull Widget<?> createDisplayGrid(@NotNull PosGuiData guiData,
                                                   @NotNull PanelSyncManager panelSyncManager) {
        return new Grid()
                .pos(7 + 18 * 5, 25)
                .size(18 * 4)
                .minElementMargin(0, 0)
                .minColWidth(18)
                .minRowHeight(18)
                .matrix(Grid.mapToMatrix((int) Math.sqrt(CONFIG_SIZE), CONFIG_SIZE,
                        index -> new AEFluidDisplaySlot(index)
                                .background(GTGuiTextures.SLOT_DARK)
                                .syncHandler(SYNC_HANDLER_NAME, 0)
                                .debugName("Index " + index)));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);

        NBTTagList tanks = new NBTTagList();
        for (int i = 0; i < CONFIG_SIZE; i++) {
            ExportOnlyAEFluidSlot tank = this.getAEHandler().getInventory()[i];
            NBTTagCompound tankTag = new NBTTagCompound();
            tankTag.setInteger("slot", i);
            tankTag.setTag("tank", tank.serializeNBT());
            tanks.appendTag(tankTag);
        }
        data.setTag(FLUID_BUFFER_TAG, tanks);

        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);

        if (data.hasKey(FLUID_BUFFER_TAG, 9)) {
            NBTTagList tanks = (NBTTagList) data.getTag(FLUID_BUFFER_TAG);
            for (NBTBase nbtBase : tanks) {
                NBTTagCompound tankTag = (NBTTagCompound) nbtBase;
                ExportOnlyAEFluidSlot tank = this.getAEHandler().getInventory()[tankTag.getInteger("slot")];
                tank.deserializeNBT(tankTag.getCompoundTag("tank"));
            }
        }
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (this.shouldRenderOverlay()) {
            if (isOnline()) {
                Textures.ME_INPUT_HATCH_ACTIVE.renderSided(getFrontFacing(), renderState, translation, pipeline);
            } else {
                Textures.ME_INPUT_HATCH.renderSided(getFrontFacing(), renderState, translation, pipeline);
            }
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, @NotNull List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.machine.fluid_hatch.import.tooltip"));
        tooltip.add(I18n.format("gregtech.machine.me.fluid_import.tooltip"));
        tooltip.add(I18n.format("gregtech.machine.me_import_fluid_hatch.configs.tooltip"));
        tooltip.add(I18n.format("gregtech.machine.me.copy_paste.tooltip"));
        tooltip.add(I18n.format("gregtech.machine.me.extra_connections.tooltip"));
        tooltip.add(I18n.format("gregtech.universal.enabled"));
    }

    @Override
    public @NotNull List<MultiblockAbility<?>> getAbilities() {
        return Arrays.asList(MultiblockAbility.IMPORT_FLUIDS, MultiblockAbility.IMPORT_ITEMS);
    }

    @Override
    public void registerAbilities(@NotNull AbilityInstances abilityInstances) {
        if (abilityInstances.isKey(MultiblockAbility.IMPORT_FLUIDS)) {
            abilityInstances.add(Arrays.asList(getAEHandler().getInventory()));
        } else if (abilityInstances.isKey(MultiblockAbility.IMPORT_ITEMS)) {
            abilityInstances.add(circuitInventory);
        }
    }

    @Override
    public final void onDataStickLeftClick(EntityPlayer player, ItemStack dataStick) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setTag("MEInputHatch", writeConfigToTag());
        dataStick.setTagCompound(tag);
        dataStick.setTranslatableName("gregtech.machine.me.fluid_import.data_stick.name");
        player.sendStatusMessage(new TextComponentTranslation("gregtech.machine.me.import_copy_settings"), true);
    }

    protected NBTTagCompound writeConfigToTag() {
        NBTTagCompound tag = new NBTTagCompound();
        NBTTagCompound configStacks = new NBTTagCompound();
        tag.setTag("ConfigStacks", configStacks);
        for (int i = 0; i < CONFIG_SIZE; i++) {
            var slot = this.aeHandler.getInventory()[i];
            IAEFluidStack config = slot.getConfig();
            if (config == null) {
                continue;
            }
            NBTTagCompound stackNbt = new NBTTagCompound();
            config.writeToNBT(stackNbt);
            configStacks.setTag(Integer.toString(i), stackNbt);
        }

        tag.setInteger(REFRESH_RATE_TAG, this.refreshRate);

        return tag;
    }

    @Override
    public final boolean onDataStickRightClick(EntityPlayer player, ItemStack dataStick) {
        NBTTagCompound tag = dataStick.getTagCompound();
        if (tag == null || !tag.hasKey("MEInputHatch")) {
            return false;
        }
        readConfigFromTag(tag.getCompoundTag("MEInputHatch"));
        syncME();
        player.sendStatusMessage(new TextComponentTranslation("gregtech.machine.me.import_paste_settings"), true);
        return true;
    }

    protected void readConfigFromTag(NBTTagCompound tag) {
        if (tag.hasKey("ConfigStacks")) {
            NBTTagCompound configStacks = tag.getCompoundTag("ConfigStacks");
            for (int i = 0; i < CONFIG_SIZE; i++) {
                String key = Integer.toString(i);
                if (configStacks.hasKey(key)) {
                    NBTTagCompound configTag = configStacks.getCompoundTag(key);
                    this.aeHandler.getInventory()[i].setConfig(WrappedFluidStack.fromNBT(configTag));
                } else {
                    this.aeHandler.getInventory()[i].setConfig(null);
                }
            }
        }

        if (tag.hasKey(REFRESH_RATE_TAG)) {
            this.refreshRate = tag.getInteger(REFRESH_RATE_TAG);
        }
    }
}

package gregtech.common.metatileentities.electric.multiblockpart;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.IMaintenanceHatch;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.ClickButtonWidget;
import gregtech.api.items.toolitem.ToolMetaItem;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.multiblock.IMaintenance;
import gregtech.api.render.SimpleOverlayRenderer;
import gregtech.api.render.Textures;
import gregtech.api.util.GTToolTypes;
import gregtech.common.items.MetaItems;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.*;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.List;

import static gregtech.api.capability.MultiblockDataCodes.IS_TAPED;
import static gregtech.api.capability.MultiblockDataCodes.STORE_MAINTENANCE;

public class MetaTileEntityMaintenanceHatch extends MetaTileEntityMultiblockPart implements IMultiblockAbilityPart<IMaintenanceHatch>, IMaintenanceHatch {

    private ItemStackHandler inventory;
    private boolean isTaped;

    // Used to store state temporarily if the Controller is broken
    private byte maintenanceProblems = -1;
    private int timeActive = -1;

    public MetaTileEntityMaintenanceHatch(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, 1);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder metaTileEntityHolder) {
        return new MetaTileEntityMaintenanceHatch(metaTileEntityId);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);

        if (shouldRenderOverlay()) {
            (isTaped ? Textures.MAINTENANCE_OVERLAY_TAPED : Textures.MAINTENANCE_OVERLAY)
                    .renderSided(getFrontFacing(), renderState, translation, pipeline);
        }
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return new ItemStackHandler(1);
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        return new ItemStackHandler(1);
    }

    @Override
    protected void initializeInventory() {
        super.initializeInventory();
        this.inventory = new ItemStackHandler(1);
        this.itemInventory = this.inventory;
    }

    @Override
    public void clearMachineInventory(NonNullList<ItemStack> itemBuffer) {
        clearInventory(itemBuffer, this.inventory);
    }

    /**
     * Sets this Maintenance Hatch as being duct taped
     * @param isTaped is the state of the hatch being taped or not
     */
    @Override
    public void setTaped(boolean isTaped) {
        this.isTaped = isTaped;
        if (!getWorld().isRemote) {
            writeCustomData(IS_TAPED, buf -> buf.writeBoolean(isTaped));
            markDirty();
        }
    }

    /**
     * Stores maintenance data to this MetaTileEntity
     * @param maintenanceProblems is the byte value representing the problems
     * @param timeActive is the int value representing the total time the parent multiblock has been active
     */
    @Override
    public void storeMaintenanceData(byte maintenanceProblems, int timeActive) {
        this.maintenanceProblems = maintenanceProblems;
        this.timeActive = timeActive;
        if (!getWorld().isRemote) {
            writeCustomData(STORE_MAINTENANCE, buf -> {
                buf.writeByte(maintenanceProblems);
                buf.writeInt(timeActive);
            });
        }
    }

    /**
     *
     * @return whether this maintenance hatch has maintenance data
     */
    @Override
    public boolean hasMaintenanceData() {
        return this.maintenanceProblems != -1;
    }

    /**
     * reads this MetaTileEntity's maintenance data
     * @return Tuple of Byte, Integer corresponding to the maintenance problems, and total time active
     */
    @Override
    public Tuple<Byte, Integer> readMaintenanceData() {
        Tuple<Byte, Integer> data = new Tuple<>(this.maintenanceProblems, this.timeActive);
        storeMaintenanceData((byte) -1, -1);
        return data;
    }

    /**
     * Fixes the maintenance problems of this hatch's Multiblock Controller
     * @param entityPlayer the player performing the fixing
     */
    private void fixMaintenanceProblems(@Nullable EntityPlayer entityPlayer) {
        if (!(this.getController() instanceof IMaintenance))
            return;

        if (!((IMaintenance) this.getController()).hasMaintenanceProblems())
            return;

        byte problems = ((IMaintenance) this.getController()).getMaintenanceProblems();

        if (entityPlayer != null) {
            if (entityPlayer.capabilities.isCreativeMode) {
                fixAllMaintenanceProblems();
                return;
            }
            // For every slot in the player's main inventory, try to duct tape fix
            for (int i = 0; i < entityPlayer.inventory.mainInventory.size(); i++) {
                if (consumeDuctTape(new ItemStackHandler(entityPlayer.inventory.mainInventory), i)) {
                    fixAllMaintenanceProblems();
                    setTaped(true);
                    return;
                }
            }
            // For each problem the multi has, try to fix with tools
            for (byte i = 0; i < 6; i++) {
                if (((problems >> i) & 1) == 0)
                    fixProblemWithTool(i, entityPlayer);
            }
        }
    }

    /**
     *
     * Handles duct taping for manual and auto-taping use
     *
     * @param handler is the handler to get duct tape from
     * @param slot is the inventory slot to check for tape
     * @return true if tape was consumed, else false
     */
    private boolean consumeDuctTape(@Nullable ItemStackHandler handler, int slot) {
        if (handler == null)
            return false;

        ItemStack itemStack = handler.getStackInSlot(slot);
        if (!itemStack.isEmpty() && itemStack.isItemEqual(MetaItems.DUCT_TAPE.getStackForm()))
            if (itemStack.getCount() - 1 >= 0) {
                itemStack.shrink(1);
                return true;
            }
        return false;
    }

    /**
     *
     * Fixes all maintenance problems that a player's inventory has tools for
     *
     * @param problemIndex is the index of the maintenance problem
     * @param entityPlayer is the target player whose inventory is used to scan for tools
     */
    private void fixProblemWithTool(int problemIndex, EntityPlayer entityPlayer) {
        List<ToolMetaItem.MetaToolValueItem> tools = null;

        switch (problemIndex) {
            case 0:
                tools = GTToolTypes.wrenches;
                break;
            case 1:
                tools = GTToolTypes.screwdrivers;
                break;
            case 2:
                tools = GTToolTypes.softHammers;
                break;
            case 3:
                tools = GTToolTypes.hardHammers;
                break;
            case 4:
                tools = GTToolTypes.wireCutters;
                break;
            case 5:
                tools = GTToolTypes.crowbars;
                break;
        }

        if (tools == null)
            return;

        for (ToolMetaItem.MetaToolValueItem tool : tools) {
            for (ItemStack itemStack : entityPlayer.inventory.mainInventory) {
                if (itemStack.isItemEqualIgnoreDurability(tool.getStackForm())) {
                    ((IMaintenance) this.getController()).setMaintenanceFixed(problemIndex);
                    damageTool(itemStack);
                    this.setTaped(false);
                }
            }
        }
    }

    /**
     * Applies damage to toon upon its use for maintenance
     *
     * @param itemStack item to apply damage to
     */
    private void damageTool(ItemStack itemStack) {
        if (itemStack.getItem() instanceof ToolMetaItem) {
            ToolMetaItem<?> toolMetaItem = (ToolMetaItem<?>) itemStack.getItem();
            toolMetaItem.damageItem(itemStack, 1, true, false);
        }
    }

    /**
     * Fixes every maintenance problem of the controller
     */
    private void fixAllMaintenanceProblems() {
        if (this.getController() instanceof IMaintenance)
            for (int i = 0; i < 6; i++) ((IMaintenance) this.getController()).setMaintenanceFixed(i);
    }

    @Override
    public boolean isFullAuto() {
        return false;
    }

    @Override
    public void onRemoval() {
        if (getController() instanceof IMaintenance) {
            IMaintenance controller = (IMaintenance) getController();
            if (!getWorld().isRemote && controller != null)
                controller.storeTaped(isTaped);
        }
        super.onRemoval();
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return ModularUI.builder(GuiTextures.BACKGROUND, 176, 18 + 18 + 94)
                .label(10, 5, this.getMetaFullName())
                .widget(new ClickButtonWidget(89 - 9 - 1, 18 - 1, 20, 20, "", data -> fixMaintenanceProblems(entityPlayer))
                .setButtonTexture(GuiTextures.MAINTENANCE_ICON))
                .bindPlayerInventory(entityPlayer.inventory, GuiTextures.SLOT, 7, 18 + 18 + 12)
                .build(this.getHolder(), entityPlayer);
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == STORE_MAINTENANCE) {
            this.maintenanceProblems = buf.readByte();
            this.timeActive = buf.readInt();
            markDirty();
        } else if (dataId == IS_TAPED) {
            this.isTaped = buf.readBoolean();
            scheduleRenderUpdate();
            markDirty();
        }
    }

    @Override
    public MultiblockAbility<IMaintenanceHatch> getAbility() {
        return MultiblockAbility.MAINTENANCE_HATCH;
    }

    @Override
    public void registerAbilities(List<IMaintenanceHatch> abilityList) {
        abilityList.add(this);
    }
}

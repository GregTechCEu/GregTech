package gregtech.common.metatileentities.electric.multiblockpart;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.IMaintenanceHatch;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.ClickButtonWidget;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.items.toolitem.ToolMetaItem;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.multiblock.IMaintenance;
import gregtech.api.render.ICubeRenderer;
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
    private final byte type; // Type 0 is regular, 1 is auto taping, 2 is full auto
    private boolean isTaped;

    // Used to store state temporarily if the Controller is broken
    private byte maintenanceProblems = -1;
    private int timeActive = -1;

    public MetaTileEntityMaintenanceHatch(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier);
        this.initializeInventory();

        if (tier == 6) type = 2;
        else if (tier == 3) type = 1;
        else type = 0;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder metaTileEntityHolder) {
        return new MetaTileEntityMaintenanceHatch(metaTileEntityId, getTier());
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);

        if (shouldRenderOverlay()) {

            SimpleOverlayRenderer renderer;
            if (type == 2) renderer = Textures.MAINTENANCE_OVERLAY_AUTO_TAPING;
            else if (isTaped) renderer = Textures.MAINTENANCE_OVERLAY_TAPED;
            else renderer = Textures.MAINTENANCE_OVERLAY;

            renderer.renderSided(getFrontFacing(), renderState, translation, pipeline);
        }
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        if (this.type != 1)
            return super.createImportItemHandler();
        return new ItemStackHandler(1);
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        if (this.type != 1)
            return super.createExportItemHandler();
        return new ItemStackHandler(1);
    }

    //todo prevent hatches not of type 1 from accepting items
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
    public boolean hasMaintenanceData() {
        return this.maintenanceProblems != -1;
    }

    /**
     * reads this MetaTileEntity's maintenance data
     * @return Tuple of Byte, Integer corresponding to the maintenance problems, and total time active
     */
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

        switch (this.type) {
            case 0: { // Manual
                if (entityPlayer == null)
                    break;

            // For every slot in the player's main inventory, try to duct tape fix
            for (int i = 0; i < entityPlayer.inventory.mainInventory.size(); i++) {
                if (consumeDuctTape(new ItemStackHandler(entityPlayer.inventory.mainInventory), i)) {
                    fixAllMaintenanceProblems();
                    setTaped(true);
                    break;
                }
            }

                if (isTaped)
                    break;

                // For each problem the multi has, try to fix with tools
                for (byte i = 0; i < 6; i++) {
                    if (((problems >> i) & 1) == 0)
                        fixProblemWithTool(i, entityPlayer);
                }
                break;
            }
            case 1: { // Consume Duct Tape for auto taping repair, then fix everything
                if (consumeDuctTape(this.inventory, 0)) //todo make this emit redstone if it is out of tape
                    fixAllMaintenanceProblems();
                break;
            }
            // Fully automatic hatch never lets maintenance change elsewhere
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

    /**
     * @return the maintenance hatch type, bounded [0, 3)
     */
    public int getType() {
        return this.type;
    }

    //todo make this emit redstone if it is out of tape
    @Override
    protected boolean canMachineConnectRedstone(EnumFacing side) {
        return super.canMachineConnectRedstone(side);
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
    public void update() {
        super.update();
        if (this.type != 0) { // if not a manual hatch, check every second for problems to fix
            if (this.getController() instanceof IMaintenance) {
                if (getOffsetTimer() % 20 == 0 && ((IMaintenance) this.getController()).hasMaintenanceProblems() && this.getController().isStructureFormed()) {
                    fixMaintenanceProblems(null);
                }
            }
        }
    }

    @Override
    public ICubeRenderer getBaseTexture() {
        MultiblockControllerBase controller = getController();
        if (controller == null) {
            return Textures.VOLTAGE_CASINGS[getTier()];
        }
        return controller.getBaseTexture(this);
    }

    /**
     * Do not open the gui if the hatch is a full auto hatch
     */
    @Override
    public boolean onRightClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
        if (type == 2) {
            return false;
        }
        return super.onRightClick(playerIn, hand, facing, hitResult);
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {

        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 176, 18 + 18 + 94).label(10, 5, this.getMetaFullName());
        if (type == 1) {
            builder.widget(new SlotWidget(this.getItemInventory(), 0, 89 - 9, 18, true, true)
                    .setBackgroundTexture(GuiTextures.SLOT));
        } else if (type == 0) {
            builder.widget(new ClickButtonWidget(89 - 9 - 1, 18 - 1, 20, 20, "", data -> fixMaintenanceProblems(entityPlayer))
                    .setButtonTexture(GuiTextures.MAINTENANCE_ICON));
        }

        builder.bindPlayerInventory(entityPlayer.inventory, GuiTextures.SLOT, 7, 18 + 18 + 12);
        return builder.build(this.getHolder(), entityPlayer);
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
        abilityList.add((IMaintenanceHatch) this);
    }
}

package gregtech.api.metatileentity.multiblock;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget.ClickData;
import gregtech.api.gui.widgets.AdvancedTextWidget;
import gregtech.api.multiblock.IMaintenance;
import gregtech.api.multiblock.PatternMatchContext;
import gregtech.api.util.XSTR;
import gregtech.common.ConfigHolder;
import gregtech.common.metatileentities.electric.multiblockpart.maintenance.MetaTileEntityMaintenanceHatch;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.util.text.event.HoverEvent.Action;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static gregtech.api.capability.MultiblockDataCodes.STORE_TAPED;

public abstract class MultiblockWithDisplayBase extends MultiblockControllerBase implements IMaintenance {

    public static final XSTR XSTR_RAND = new XSTR();

    private int timeActive;
    private static final int minimumMaintenanceTime = 5184000; // 72 real-life hours = 5184000 ticks

    /**
     * This value stores whether each of the 5 maintenance problems have been fixed.
     * A value of 0 means the problem is not fixed, else it is fixed
     * Value positions correspond to the following from left to right: 0=Wrench, 1=Screwdriver, 2=Soft Hammer, 3=Hard Hammer, 4=Wire Cutter, 5=Crowbar
     */
    protected byte maintenance_problems;

    // Used for data preservation with Maintenance Hatch
    private boolean storedTaped = false;

    public MultiblockWithDisplayBase(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
        this.maintenance_problems = 0b000000;
    }

    /**
     * Sets the maintenance problem corresponding to index to fixed
     *
     * @param index of the maintenance problem
     */
    public void setMaintenanceFixed(int index) {
        this.maintenance_problems |= 1 << index;
    }

    /**
     * Used to cause a single random maintenance problem
     */
    protected void causeMaintenanceProblems() {
        this.maintenance_problems &= ~(1 << ((int) (XSTR_RAND.nextFloat()*5)));
    }

    /**
     *
     * @return the byte value representing the maintenance problems
     */
    public byte getMaintenanceProblems() {
        return maintenance_problems;
    }

    /**
     *
     * @return the amount of maintenance problems the multiblock has
     */
    public int getNumMaintenanceProblems() {
        return 6 - Integer.bitCount(maintenance_problems);
    }

    /**
     *
     * @return whether the multiblock has any maintenance problems
     */
    public boolean hasMaintenanceProblems() {
        return this.maintenance_problems < 63;
    }

    /**
     *
     * @return whether this multiblock has maintenance mechanics
     */
    public boolean hasMaintenance() {
        return false;
    }

    /**
     * Used to calculate whether a maintenance problem should happen based on machine time active
     * @param duration in ticks to add to the counter of active time
     */
    public void calculateMaintenance(int duration) {
        if (!ConfigHolder.U.GT5u.enableMaintenance || !hasMaintenance())
            return;

        MetaTileEntityMaintenanceHatch maintenanceHatch = getAbilities(GregtechCapabilities.MAINTENANCE_HATCH).get(0);
        if (maintenanceHatch.getType() == 2) {
            return;
        }

        timeActive += duration;
        if (minimumMaintenanceTime - timeActive <= 0)
            if (XSTR_RAND.nextFloat() - 0.75f >= 0) {
                causeMaintenanceProblems();
                maintenanceHatch.setTaped(false);
                timeActive = timeActive - minimumMaintenanceTime;
            }
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        if (this.hasMaintenance() && ConfigHolder.U.GT5u.enableMaintenance) { // nothing extra if no maintenance
            if (getAbilities(GregtechCapabilities.MAINTENANCE_HATCH).isEmpty())
                return;
            MetaTileEntityMaintenanceHatch maintenanceHatch = getAbilities(GregtechCapabilities.MAINTENANCE_HATCH).get(0);
            if (maintenanceHatch.getType() == 2) { // set problems fixed with full auto hatches
                this.maintenance_problems = 0b111111;
            } else {
                readMaintenanceData(maintenanceHatch);
                if (maintenanceHatch.getType() == 0 && storedTaped) {
                    maintenanceHatch.setTaped(true);
                    storeTaped(false);
                }
            }
        }
    }

    /**
     * Stores the taped state of the maintenance hatch
     * @param isTaped is whether the maintenance hatch is taped or not
     */
    public void storeTaped(boolean isTaped) {
        this.storedTaped = isTaped;
        writeCustomData(STORE_TAPED, buf -> {
            buf.writeBoolean(isTaped);
        });
    }

    /**
     * reads maintenance data from a maintenance hatch
     * @param hatch is the hatch to read the data from
     */
    private void readMaintenanceData(MetaTileEntityMaintenanceHatch hatch) {
        if (hatch.hasMaintenanceData()) {
            Tuple<Byte, Integer> data = hatch.readMaintenanceData();
            this.maintenance_problems = data.getFirst();
            this.timeActive = data.getSecond();
        }
    }

    @Override
    public void invalidateStructure() {
        if (hasMaintenance() && ConfigHolder.U.GT5u.enableMaintenance) { // nothing extra if no maintenance
            if (getAbilities(GregtechCapabilities.MAINTENANCE_HATCH).isEmpty())
                return;
            MetaTileEntityMaintenanceHatch maintenance = getAbilities(GregtechCapabilities.MAINTENANCE_HATCH).get(0);
            if (maintenance.getType() != 2) // store maintenance data for non full auto hatches
                maintenance.storeMaintenanceData(maintenance_problems, timeActive);
        }
        super.invalidateStructure();
    }

    @Override
    protected boolean checkStructureComponents(List<IMultiblockPart> parts, Map<MultiblockAbility<Object>, List<Object>> abilities) {
        boolean canForm = super.checkStructureComponents(parts, abilities);
        if (!canForm)
            return false;

        if (!hasMaintenance())
            return true;

//        int mufflerCount = abilities.getOrDefault(GregtechCapabilities.MUFFLER_HATCH, Collections.emptyList()).size();
        int maintenanceCount = abilities.getOrDefault(GregtechCapabilities.MAINTENANCE_HATCH, Collections.emptyList()).size();

//        if (hasMuffler) {
//            if (mufflerCount != 1)
//                return false;
//        } else {
//            if (mufflerCount != 0)
//                return false;
//        }
        return maintenanceCount == 1;
    }

    /**
     * Called serverside to obtain text displayed in GUI
     * each element of list is displayed on new line
     * to use translation, use TextComponentTranslation
     */
    protected void addDisplayText(List<ITextComponent> textList) {
        if (!isStructureFormed()) {
            ITextComponent tooltip = new TextComponentTranslation("gregtech.multiblock.invalid_structure.tooltip");
            tooltip.setStyle(new Style().setColor(TextFormatting.GRAY));
            textList.add(new TextComponentTranslation("gregtech.multiblock.invalid_structure")
                    .setStyle(new Style().setColor(TextFormatting.RED)
                            .setHoverEvent(new HoverEvent(Action.SHOW_TEXT, tooltip))));
        } else if (!hasMaintenance()) {
            return;
        }

        if (!hasMaintenanceProblems()) {
            textList.add(new TextComponentTranslation("gregtech.multiblock.universal.no_problems")
                .setStyle(new Style().setColor(TextFormatting.AQUA))
            );
            return;
        }

        ITextComponent hoverEventTranslation = new TextComponentTranslation("gregtech.multiblock.universal.has_problems_header")
                .setStyle(new Style().setColor(TextFormatting.GRAY));

        if (((this.maintenance_problems >> 0) & 1) == 0)
            hoverEventTranslation.appendSibling(new TextComponentTranslation("gregtech.multiblock.universal.problem.wrench", "\n"));

        if (((this.maintenance_problems >> 1) & 1) == 0)
            hoverEventTranslation.appendSibling(new TextComponentTranslation("gregtech.multiblock.universal.problem.screwdriver", "\n"));

        if (((this.maintenance_problems >> 2) & 1) == 0)
            hoverEventTranslation.appendSibling(new TextComponentTranslation("gregtech.multiblock.universal.problem.soft_mallet", "\n"));

        if (((this.maintenance_problems >> 3) & 1) == 0)
            hoverEventTranslation.appendSibling(new TextComponentTranslation("gregtech.multiblock.universal.problem.hard_hammer", "\n"));

        if (((this.maintenance_problems >> 4) & 1) == 0)
            hoverEventTranslation.appendSibling(new TextComponentTranslation("gregtech.multiblock.universal.problem.wire_cutter", "\n"));

        if (((this.maintenance_problems >> 5) & 1) == 0)
            hoverEventTranslation.appendSibling(new TextComponentTranslation("gregtech.multiblock.universal.problem.crowbar", "\n"));

        TextComponentTranslation textTranslation = new TextComponentTranslation("gregtech.multiblock.universal.has_problems");

        textList.add(textTranslation.setStyle(new Style().setColor(TextFormatting.RED)
                .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverEventTranslation))));
    }

    /**
     * Called on serverside when client is clicked on the specific text component
     * with special click event handler
     * Data is the data specified in the component
     */
    protected void handleDisplayClick(String componentData, ClickData clickData) {
    }

    protected ModularUI.Builder createUITemplate(EntityPlayer entityPlayer) {
        ModularUI.Builder builder = ModularUI.extendedBuilder();
        builder.image(7, 4, 162, 121, GuiTextures.DISPLAY);
        builder.label(11, 9, getMetaFullName(), 0xFFFFFF);
        builder.widget(new AdvancedTextWidget(11, 19, this::addDisplayText, 0xFFFFFF)
                .setMaxWidthLimit(156)
                .setClickHandler(this::handleDisplayClick));
        builder.bindPlayerInventory(entityPlayer.inventory, 134);
        return builder;
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return createUITemplate(entityPlayer).build(getHolder(), entityPlayer);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setByte("Maintenance", maintenance_problems);
        data.setInteger("ActiveTimer", timeActive);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        maintenance_problems = data.getByte("Maintenance");
        timeActive = data.getInteger("ActiveTimer");
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeByte(maintenance_problems);
        buf.writeInt(timeActive);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        maintenance_problems = buf.readByte();
        timeActive = buf.readInt();
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == STORE_TAPED) {
            storedTaped = buf.readBoolean();
        }
    }

}

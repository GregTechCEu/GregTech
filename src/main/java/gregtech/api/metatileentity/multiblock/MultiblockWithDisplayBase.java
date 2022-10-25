package gregtech.api.metatileentity.multiblock;

import gregtech.api.GTValues;
import gregtech.api.block.VariantActiveBlock;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IMaintenanceHatch;
import gregtech.api.capability.IMufflerHatch;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget.ClickData;
import gregtech.api.gui.widgets.AdvancedTextWidget;
import gregtech.api.gui.widgets.ImageCycleButtonWidget;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.GTUtility;
import gregtech.common.ConfigHolder;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.util.text.event.HoverEvent.Action;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;

import static gregtech.api.capability.GregtechDataCodes.IS_WORKING;
import static gregtech.api.capability.GregtechDataCodes.STORE_TAPED;

public abstract class MultiblockWithDisplayBase extends MultiblockControllerBase implements IMaintenance {


    private boolean voidingItems = false;
    private boolean voidingFluids = false;
    private VoidingMode voidingMode;

    /**
     * Items to recover in a muffler hatch
     */
    protected final List<ItemStack> recoveryItems = new ArrayList<ItemStack>() {{
        add(OreDictUnifier.get(OrePrefix.dustTiny, Materials.Ash));
    }};

    private int timeActive;
    private static final int minimumMaintenanceTime = 3456000; // 48 real-life hours = 3456000 ticks

    /**
     * This value stores whether each of the 5 maintenance problems have been fixed.
     * A value of 0 means the problem is not fixed, else it is fixed
     * Value positions correspond to the following from left to right: 0=Wrench, 1=Screwdriver, 2=Soft Hammer, 3=Hard Hammer, 4=Wire Cutter, 5=Crowbar
     */
    protected byte maintenance_problems;

    // Used for data preservation with Maintenance Hatch
    private boolean storedTaped = false;

    private final String NBT_VOIDING_MODE = "VoidingMode";
    private final String NBT_VOIDING_ITEMS = "VoidingItems";
    private final String NBT_VOIDING_FLUIDS = "VoidingFluids";

    protected List<BlockPos> variantActiveBlocks;
    protected boolean lastActive;

    public MultiblockWithDisplayBase(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
        this.maintenance_problems = 0b000000;
        this.voidingMode = VoidingMode.VOID_NONE;
    }

    /**
     * Sets the maintenance problem corresponding to index to fixed
     *
     * @param index of the maintenance problem
     */
    @Override
    public void setMaintenanceFixed(int index) {
        this.maintenance_problems |= 1 << index;
    }

    /**
     * Used to cause a single random maintenance problem
     */
    @Override
    public void causeMaintenanceProblems() {
        this.maintenance_problems &= ~(1 << ((int) (GTValues.RNG.nextFloat() * 5)));
    }

    /**
     * @return the byte value representing the maintenance problems
     */
    @Override
    public byte getMaintenanceProblems() {
        return ConfigHolder.machines.enableMaintenance ? maintenance_problems : 0b111111;
    }

    /**
     * @return the amount of maintenance problems the multiblock has
     */
    @Override
    public int getNumMaintenanceProblems() {
        return ConfigHolder.machines.enableMaintenance ? 6 - Integer.bitCount(maintenance_problems) : 0;
    }

    /**
     * @return whether the multiblock has any maintenance problems
     */
    @Override
    public boolean hasMaintenanceProblems() {
        return ConfigHolder.machines.enableMaintenance && this.maintenance_problems < 63;
    }

    /**
     * @return whether this multiblock has maintenance mechanics
     */
    @Override
    public boolean hasMaintenanceMechanics() {
        return true;
    }

    public boolean hasMufflerMechanics() {
        return false;
    }

    /**
     * Used to calculate whether a maintenance problem should happen based on machine time active
     *
     * @param duration in ticks to add to the counter of active time
     */
    public void calculateMaintenance(int duration) {
        if (!ConfigHolder.machines.enableMaintenance || !hasMaintenanceMechanics())
            return;

        IMaintenanceHatch maintenanceHatch = getAbilities(MultiblockAbility.MAINTENANCE_HATCH).get(0);
        if (maintenanceHatch.isFullAuto()) {
            return;
        }

        timeActive += duration * maintenanceHatch.getTimeMultiplier();
        if (minimumMaintenanceTime - timeActive <= 0)
            if (GTValues.RNG.nextFloat() - 0.75f >= 0) {
                causeMaintenanceProblems();
                maintenanceHatch.setTaped(false);
                timeActive = timeActive - minimumMaintenanceTime;
            }
    }

    @Override
    public boolean isStructureObstructed() {
        return hasMufflerMechanics() && !isMufflerFaceFree();
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        if (this.hasMaintenanceMechanics() && ConfigHolder.machines.enableMaintenance) { // nothing extra if no maintenance
            if (getAbilities(MultiblockAbility.MAINTENANCE_HATCH).isEmpty())
                return;
            IMaintenanceHatch maintenanceHatch = getAbilities(MultiblockAbility.MAINTENANCE_HATCH).get(0);
            if (maintenanceHatch.startWithoutProblems()) {
                this.maintenance_problems = (byte) 0b111111;
                this.timeActive = 0;
            }
            readMaintenanceData(maintenanceHatch);
            if (storedTaped) {
                maintenanceHatch.setTaped(true);
                storeTaped(false);
            }
        }
        this.variantActiveBlocks = context.getOrDefault("VABlock", new LinkedList<>());
        VariantActiveBlock.ACTIVE_BLOCKS.putIfAbsent(getWorld().provider.getDimension(), new ObjectOpenHashSet<>());
        replaceVariantBlocksActive(false);
    }

    @Override
    public void update() {
        super.update();
        if (!getWorld().isRemote) {
            boolean state = isActive();
            if (lastActive != state) {
                this.setLastActive(state);
                this.markDirty();
                this.replaceVariantBlocksActive(lastActive);
            }
        }
    }

    public void setLastActive(boolean lastActive) {
        this.lastActive = lastActive;
        this.writeCustomData(IS_WORKING, buf -> buf.writeBoolean(lastActive));
    }

    /**
     * Stores the taped state of the maintenance hatch
     *
     * @param isTaped is whether the maintenance hatch is taped or not
     */
    @Override
    public void storeTaped(boolean isTaped) {
        this.storedTaped = isTaped;
        writeCustomData(STORE_TAPED, buf -> buf.writeBoolean(isTaped));
    }

    /**
     * reads maintenance data from a maintenance hatch
     *
     * @param hatch is the hatch to read the data from
     */
    private void readMaintenanceData(IMaintenanceHatch hatch) {
        if (hatch.hasMaintenanceData()) {
            Tuple<Byte, Integer> data = hatch.readMaintenanceData();
            this.maintenance_problems = data.getFirst();
            this.timeActive = data.getSecond();
        }
    }

    /**
     * Outputs the recovery items into the muffler hatch
     */
    public void outputRecoveryItems() {
        IMufflerHatch muffler = getAbilities(MultiblockAbility.MUFFLER_HATCH).get(0);
        muffler.recoverItemsTable(GTUtility.copyStackList(recoveryItems));
    }

    public void outputRecoveryItems(int parallel) {
        IMufflerHatch muffler = getAbilities(MultiblockAbility.MUFFLER_HATCH).get(0);
        ArrayList<ItemStack> parallelRecover = new ArrayList<>();
        IntStream.range(0, parallel).forEach(value -> parallelRecover.addAll(recoveryItems));
        muffler.recoverItemsTable(GTUtility.copyStackList(parallelRecover));
    }

    /**
     * @return whether the muffler hatch's front face is free
     */
    public boolean isMufflerFaceFree() {
        if (hasMufflerMechanics() && getAbilities(MultiblockAbility.MUFFLER_HATCH).size() == 0)
            return false;

        return isStructureFormed() && hasMufflerMechanics() && getAbilities(MultiblockAbility.MUFFLER_HATCH).get(0).isFrontFaceFree();
    }

    /**
     * Produces the muffler particles
     */
    @SideOnly(Side.CLIENT)
    public void runMufflerEffect(float xPos, float yPos, float zPos, float xSpd, float ySpd, float zSpd) {
        getWorld().spawnParticle(EnumParticleTypes.SMOKE_LARGE, xPos, yPos, zPos, xSpd, ySpd, zSpd);
    }

    /**
     * Sets the recovery items of this multiblock
     *
     * @param recoveryItems is the items to set
     */
    protected void setRecoveryItems(ItemStack... recoveryItems) {
        this.recoveryItems.clear();
        this.recoveryItems.addAll(Arrays.asList(recoveryItems));
    }

    /**
     * @return whether the current multiblock is active or not
     */
    public boolean isActive() {
        return isStructureFormed();
    }

    @Override
    public void invalidateStructure() {
        if (hasMaintenanceMechanics() && ConfigHolder.machines.enableMaintenance) { // nothing extra if no maintenance
            if (!getAbilities(MultiblockAbility.MAINTENANCE_HATCH).isEmpty())
                getAbilities(MultiblockAbility.MAINTENANCE_HATCH).get(0)
                        .storeMaintenanceData(maintenance_problems, timeActive);
        }
        this.lastActive = false;
        this.replaceVariantBlocksActive(false);
        super.invalidateStructure();
    }

    protected void replaceVariantBlocksActive(boolean isActive) {
        if (variantActiveBlocks != null && !variantActiveBlocks.isEmpty()) {
            int id = getWorld().provider.getDimension();

            writeCustomData(GregtechDataCodes.VARIANT_RENDER_UPDATE, buf -> {
                buf.writeInt(id);
                buf.writeBoolean(isActive);
                buf.writeInt(variantActiveBlocks.size());
                for (BlockPos blockPos : variantActiveBlocks) {
                    if (isActive) {
                        VariantActiveBlock.ACTIVE_BLOCKS.get(id).add(blockPos);
                    } else {
                        VariantActiveBlock.ACTIVE_BLOCKS.get(id).remove(blockPos);
                    }
                    buf.writeBlockPos(blockPos);
                }
            });
        }
    }

    public TraceabilityPredicate autoAbilities() {
        return autoAbilities(true, true);
    }

    public TraceabilityPredicate autoAbilities(boolean checkMaintenance, boolean checkMuffler) {
        TraceabilityPredicate predicate = new TraceabilityPredicate();
        if (checkMaintenance && hasMaintenanceMechanics()) {
            predicate = predicate.or(abilities(MultiblockAbility.MAINTENANCE_HATCH)
                    .setMinGlobalLimited(ConfigHolder.machines.enableMaintenance ? 1 : 0).setMaxGlobalLimited(1));
        }
        if (checkMuffler && hasMufflerMechanics()) {
            predicate =  predicate.or(abilities(MultiblockAbility.MUFFLER_HATCH).setMinGlobalLimited(1).setMaxGlobalLimited(1));
        }
        return predicate;
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
        } else {
            if (hasMaintenanceMechanics() && ConfigHolder.machines.enableMaintenance) {
                addMaintenanceText(textList);
            }
            if (hasMufflerMechanics() && !isMufflerFaceFree())
                textList.add(new TextComponentTranslation("gregtech.multiblock.universal.muffler_obstructed")
                        .setStyle(new Style().setColor(TextFormatting.RED)
                                .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                        new TextComponentTranslation("gregtech.multiblock.universal.muffler_obstructed.tooltip")))));
        }
    }

    protected void addMaintenanceText(List<ITextComponent> textList) {
        if (!hasMaintenanceProblems()) {
            textList.add(new TextComponentTranslation("gregtech.multiblock.universal.no_problems")
                    .setStyle(new Style().setColor(TextFormatting.GREEN))
            );
        } else {

            ITextComponent hoverEventTranslation = new TextComponentTranslation("gregtech.multiblock.universal.has_problems_header")
                    .setStyle(new Style().setColor(TextFormatting.GRAY));

            if (((this.maintenance_problems) & 1) == 0)
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
        if(shouldShowVoidingModeButton()) {
            builder.widget(new ImageCycleButtonWidget(149, 121 - 17, 18, 18, GuiTextures.BUTTON_VOID_MULTIBLOCK,
                    4, this::getVoidingMode, this::setVoidingMode)
                    .setTooltipHoverString(this::getVoidingModeTooltip));
        }
        builder.bindPlayerInventory(entityPlayer.inventory, 134);
        return builder;
    }

    protected boolean shouldShowVoidingModeButton() {
        return true;
    }

    protected int getVoidingMode() {
        return voidingMode.ordinal();
    }

    private void setVoidingMode(int mode) {
        this.voidingMode = VoidingMode.VALUES[mode];

        this.voidingFluids = mode >= 2;

        this.voidingItems = mode == 1 || mode == 3;

        // After changing the voiding mode, reset the notified buses in case a recipe can run now that voiding mode has been changed
        for(IFluidTank tank : this.getAbilities(MultiblockAbility.IMPORT_FLUIDS)) {
            this.getNotifiedFluidInputList().add((IFluidHandler) tank);
        }
        this.getNotifiedItemInputList().addAll(this.getAbilities(MultiblockAbility.IMPORT_ITEMS));

        markDirty();
    }

    private String getVoidingModeTooltip(int mode) {
        return VoidingMode.VALUES[mode].getName();
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
        data.setBoolean(NBT_VOIDING_ITEMS, voidingItems);
        data.setBoolean(NBT_VOIDING_FLUIDS, voidingFluids);
        data.setInteger(NBT_VOIDING_MODE, voidingMode.ordinal());
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        maintenance_problems = data.getByte("Maintenance");
        timeActive = data.getInteger("ActiveTimer");
        if(data.hasKey(NBT_VOIDING_ITEMS)) {
            voidingItems = data.getBoolean(NBT_VOIDING_ITEMS);
        }

        if(data.hasKey(NBT_VOIDING_FLUIDS)) {
            voidingFluids = data.getBoolean(NBT_VOIDING_FLUIDS);
        }

        if(data.hasKey(NBT_VOIDING_MODE)) {
            voidingMode = VoidingMode.values()[data.getInteger(NBT_VOIDING_MODE)];
        }
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeByte(maintenance_problems);
        buf.writeInt(timeActive);
        buf.writeBoolean(voidingFluids);
        buf.writeBoolean(voidingItems);
        buf.writeInt(voidingMode.ordinal());
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        maintenance_problems = buf.readByte();
        timeActive = buf.readInt();
        voidingFluids = buf.readBoolean();
        voidingItems = buf.readBoolean();
        voidingMode = VoidingMode.values()[buf.readInt()];
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == STORE_TAPED) {
            storedTaped = buf.readBoolean();
        }
        if (dataId == GregtechDataCodes.VARIANT_RENDER_UPDATE) {
            int minX;
            int minY;
            int minZ;
            minX = minY = minZ = Integer.MAX_VALUE;
            int maxX;
            int maxY;
            int maxZ;
            maxX = maxY = maxZ = Integer.MIN_VALUE;

            int id = buf.readInt();
            boolean isActive = buf.readBoolean();
            int size = buf.readInt();
            for (int i = 0; i < size; i++) {
                BlockPos blockPos = buf.readBlockPos();
                if (isActive) {
                    VariantActiveBlock.ACTIVE_BLOCKS.get(id).add(blockPos);
                } else {
                    VariantActiveBlock.ACTIVE_BLOCKS.get(id).remove(blockPos);
                }
                minX = Math.min(minX, blockPos.getX());
                minY = Math.min(minY, blockPos.getY());
                minZ = Math.min(minZ, blockPos.getZ());
                maxX = Math.max(maxX, blockPos.getX());
                maxY = Math.max(maxY, blockPos.getY());
                maxZ = Math.max(maxZ, blockPos.getZ());
            }

            if (getWorld().provider.getDimension() == id) {
                getWorld().markBlockRangeForRenderUpdate(new BlockPos(minX, minY, minZ), new BlockPos(maxX, maxY, maxZ));
            }
        }
        if (dataId == IS_WORKING) {
            lastActive = buf.readBoolean();
        }
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        T capabilityResult = super.getCapability(capability, side);
        if (capabilityResult != null) return capabilityResult;
        if (capability == GregtechTileCapabilities.CAPABILITY_MAINTENANCE) {
            if (this.hasMaintenanceMechanics() && ConfigHolder.machines.enableMaintenance) {
                return GregtechTileCapabilities.CAPABILITY_MAINTENANCE.cast(this);
            }
        }
        return null;
    }

    @Override
    public boolean canVoidRecipeFluidOutputs() {
        return voidingFluids;
    }

    @Override
    public boolean canVoidRecipeItemOutputs() {
        return voidingItems;
    }
}

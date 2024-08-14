package gregtech.common.metatileentities.multi.electric;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.block.ICleanroomFilter;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.IMufflerHatch;
import gregtech.api.capability.IWorkable;
import gregtech.api.capability.impl.CleanroomLogic;
import gregtech.api.capability.impl.EnergyContainerList;
import gregtech.api.gui.Widget;
import gregtech.api.metatileentity.IDataInfoProvider;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.SimpleGeneratorMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.CleanroomType;
import gregtech.api.metatileentity.multiblock.FuelMultiblockController;
import gregtech.api.metatileentity.multiblock.ICleanroomProvider;
import gregtech.api.metatileentity.multiblock.ICleanroomReceiver;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockDisplayText;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.pattern.GreggyBlockPos;
import gregtech.api.pattern.MultiblockShapeInfo;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.pattern.pattern.FactoryExpandablePattern;
import gregtech.api.pattern.pattern.IBlockPattern;
import gregtech.api.util.BlockInfo;
import gregtech.api.util.GTUtility;
import gregtech.api.util.Mods;
import gregtech.api.util.RelativeDirection;
import gregtech.api.util.TextComponentUtil;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.handler.AABBHighlightRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.utils.TooltipHelper;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.BlockCleanroomCasing;
import gregtech.common.blocks.BlockGlassCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.common.metatileentities.multi.MetaTileEntityCokeOven;
import gregtech.common.metatileentities.multi.MetaTileEntityPrimitiveBlastFurnace;
import gregtech.common.metatileentities.multi.MetaTileEntityPrimitiveWaterPump;
import gregtech.common.metatileentities.multi.electric.centralmonitor.MetaTileEntityCentralMonitor;
import gregtech.core.sound.GTSoundEvents;

import net.minecraft.block.BlockDoor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.core.AEConfig;
import appeng.core.features.AEFeature;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static gregtech.api.gui.widgets.AdvancedTextWidget.withButton;

public class MetaTileEntityCleanroom extends MultiblockWithDisplayBase
                                     implements ICleanroomProvider, IWorkable, IDataInfoProvider {

    public static final int CLEAN_AMOUNT_THRESHOLD = 90;
    public static final int MIN_CLEAN_AMOUNT = 0;

    public static final int MIN_RADIUS = 2;
    public static final int MIN_DEPTH = 4;
    public static final int MAX_RADIUS = 7;
    public static final int MAX_DEPTH = 14;
    private static final GreggyBlockPos offset = new GreggyBlockPos(1, 1, 1);
    private final int[] bounds = { 0, MIN_DEPTH, MIN_RADIUS, MIN_RADIUS, MIN_RADIUS, MIN_RADIUS };
    private CleanroomType cleanroomType = null;
    private int cleanAmount;

    private IEnergyContainer energyContainer;

    private ICleanroomFilter cleanroomFilter;
    private boolean renderingAABB;
    private final CleanroomLogic cleanroomLogic;
    private final Collection<ICleanroomReceiver> cleanroomReceivers = new HashSet<>();
    private AABBHighlightRenderer.AABBRender aabb;

    /**
     * Reverse map from enum facing -> relative direction, refreshed on every setFrontFacing(...) call
     */
    private final Map<EnumFacing, RelativeDirection> facingMap = new HashMap<>();

    public MetaTileEntityCleanroom(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
        this.cleanroomLogic = new CleanroomLogic(this, GTValues.LV);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityCleanroom(metaTileEntityId);
    }

    private void initializeAbilities() {
        this.energyContainer = new EnergyContainerList(getAbilities(MultiblockAbility.INPUT_ENERGY));
    }

    private void resetTileAbilities() {
        this.energyContainer = new EnergyContainerList(new ArrayList<>());
    }

    @Override
    protected void formStructure(String name) {
        super.formStructure(name);
        initializeAbilities();

        renderingAABB = false;
        writeCustomData(GregtechDataCodes.RENDER_UPDATE, buf -> buf.writeBoolean(false));

        ICleanroomFilter type = allSameType(GregTechAPI.CLEANROOM_FILTERS, getSubstructure(name).getCache());
        if (type == null) {
            invalidateStructure(name);
            return;
        }

        this.cleanroomFilter = type;
        this.cleanroomType = type.getCleanroomType();

        forEachFormed(name, info -> {
            TileEntity te = info.getTileEntity();
            if (!(te instanceof IGregTechTileEntity gtte)) return;

            MetaTileEntity mte = gtte.getMetaTileEntity();

            if (!(mte instanceof ICleanroomReceiver receiver)) return;

            if (receiver.getCleanroom() != this) {
                receiver.setCleanroom(this);
                cleanroomReceivers.add(receiver);
            }
        });

        // max progress is based on the dimensions of the structure: (x^3)-(x^2)
        // taller cleanrooms take longer than wider ones
        // minimum of 100 is a 5x5x5 cleanroom: 125-25=100 ticks
        // this.cleanroomLogic.setMaxProgress(Math.max(100,
        // ((lDist + rDist + 1) * (bDist + fDist + 1) * hDist) - ((lDist + rDist + 1) * (bDist + fDist + 1))));
        this.cleanroomLogic.setMaxProgress(100);
        this.cleanroomLogic.setMinEnergyTier(cleanroomFilter.getMinTier());
    }

    @Override
    public void invalidateStructure(String name) {
        super.invalidateStructure(name);
        resetTileAbilities();
        this.cleanroomLogic.invalidate();
        this.cleanAmount = MIN_CLEAN_AMOUNT;
        cleanroomReceivers.forEach(receiver -> receiver.setCleanroom(null));
        cleanroomReceivers.clear();
    }

    @Override
    protected void updateFormedValid() {
        if (!getWorld().isRemote) {
            this.cleanroomLogic.updateLogic();
            if (this.cleanroomLogic.wasActiveAndNeedsUpdate()) {
                this.cleanroomLogic.setWasActiveAndNeedsUpdate(false);
                this.cleanroomLogic.setActive(false);
            }
        }
    }

    @Override
    public void checkStructurePattern() {
        super.checkStructurePattern();
    }

    @Override
    public boolean allowsExtendedFacing() {
        return false;
    }

    @Override
    public boolean allowsFlip() {
        return false;
    }

    @NotNull
    @Override
    protected IBlockPattern createStructurePattern() {
        TraceabilityPredicate wallPredicate = states(getCasingState(), getGlassState());
        TraceabilityPredicate energyPredicate = autoAbilities().or(abilities(MultiblockAbility.INPUT_ENERGY)
                .setMinGlobalLimited(1).setMaxGlobalLimited(3));

        TraceabilityPredicate edgePredicate = states(getCasingState())
                .or(energyPredicate);
        TraceabilityPredicate facePredicate = wallPredicate
                .or(energyPredicate)
                .or(doorPredicate().setMaxGlobalLimited(8))
                .or(abilities(MultiblockAbility.PASSTHROUGH_HATCH).setMaxGlobalLimited(30));
        TraceabilityPredicate filterPredicate = filterPredicate();
        TraceabilityPredicate innerPredicate = innerPredicate();
        TraceabilityPredicate verticalEdgePredicate = edgePredicate
                .or(states(getGlassState()));

        return FactoryExpandablePattern.start(RelativeDirection.UP, RelativeDirection.RIGHT, RelativeDirection.FRONT)
                .boundsFunction((w, c, f, u) -> bounds)
                .predicateFunction((c, b) -> {
                    // controller always at origin
                    if (c.origin()) return selfPredicate();

                    int intersects = 0;

                    // aisle dir is up, so its bounds[0] and bounds[1]
                    boolean topAisle = c.x() == b[0];
                    boolean botAisle = c.x() == -b[1];

                    if (topAisle || botAisle) intersects++;
                    // negative signs for the LEFT and BACK ordinals
                    // string dir is right, so its bounds[2] and bounds[3]
                    if (c.y() == -b[2] || c.y() == b[3]) intersects++;
                    // char dir is front, so its bounds[4] and bounds[5]
                    if (c.z() == b[4] || c.z() == -b[5]) intersects++;

                    // GTLog.logger.info(intersects + " intersects at " + c);

                    // more than or equal to 2 intersects means it is an edge
                    if (intersects >= 2) {
                        if (topAisle || botAisle) return edgePredicate;
                        return verticalEdgePredicate;
                    }

                    // 1 intersect means it is a face
                    if (intersects == 1) {
                        if (topAisle) return filterPredicate;
                        return facePredicate;
                    }

                    // intersects == 0, so its not a face
                    return innerPredicate;
                })
                .build();
    }

    @NotNull
    protected TraceabilityPredicate filterPredicate() {
        return new TraceabilityPredicate(
                (worldState, patternState) -> GregTechAPI.CLEANROOM_FILTERS.containsKey(worldState.getBlockState()),
                () -> GregTechAPI.CLEANROOM_FILTERS.entrySet().stream()
                        .filter(entry -> entry.getValue().getCleanroomType() != null)
                        .sorted(Comparator.comparingInt(entry -> entry.getValue().getTier()))
                        .map(entry -> new BlockInfo(entry.getKey(), null))
                        .toArray(BlockInfo[]::new))
                                .addTooltips("gregtech.multiblock.pattern.error.filters");
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.PLASCRETE;
    }

    // protected to allow easy addition of addon "cleanrooms"
    @NotNull
    protected IBlockState getCasingState() {
        return MetaBlocks.CLEANROOM_CASING.getState(BlockCleanroomCasing.CasingType.PLASCRETE);
    }

    @NotNull
    protected IBlockState getGlassState() {
        return MetaBlocks.TRANSPARENT_CASING.getState(BlockGlassCasing.CasingType.CLEANROOM_GLASS);
    }

    @NotNull
    protected static TraceabilityPredicate doorPredicate() {
        return new TraceabilityPredicate(
                (worldState, patternState) -> worldState.getBlockState().getBlock() instanceof BlockDoor);
    }

    @NotNull
    protected TraceabilityPredicate innerPredicate() {
        return new TraceabilityPredicate((worldState, patternState) -> {
            // all non-MetaTileEntities are allowed inside by default
            TileEntity tileEntity = worldState.getTileEntity();
            if (!(tileEntity instanceof IGregTechTileEntity)) return true;

            MetaTileEntity metaTileEntity = ((IGregTechTileEntity) tileEntity).getMetaTileEntity();

            // always ban other cleanrooms, can cause problems otherwise
            if (metaTileEntity instanceof ICleanroomProvider) return false;

            return !isMachineBanned(metaTileEntity);
        });
    }

    @Override
    public void setFrontFacing(EnumFacing facing) {
        super.setFrontFacing(facing);
        updateFacingMap();
    }

    protected void updateFacingMap() {
        // cache relative front, back, left, right
        for (int i = 2; i < 6; i++) {
            EnumFacing abs = RelativeDirection.VALUES[i].getRelativeFacing(frontFacing, upwardsFacing, false);
            facingMap.put(abs, RelativeDirection.VALUES[i]);
        }
    }

    @Override
    public SoundEvent getBreakdownSound() {
        return GTSoundEvents.BREAKDOWN_MECHANICAL;
    }

    protected boolean isMachineBanned(MetaTileEntity metaTileEntity) {
        // blacklisted machines: mufflers and all generators, miners/drills, primitives
        if (metaTileEntity instanceof IMufflerHatch) return true;
        if (metaTileEntity instanceof SimpleGeneratorMetaTileEntity) return true;
        if (metaTileEntity instanceof FuelMultiblockController) return true;
        if (metaTileEntity instanceof MetaTileEntityLargeMiner) return true;
        if (metaTileEntity instanceof MetaTileEntityFluidDrill) return true;
        if (metaTileEntity instanceof MetaTileEntityCentralMonitor) return true;
        if (metaTileEntity instanceof MetaTileEntityCleanroom) return true;
        if (metaTileEntity instanceof MetaTileEntityCokeOven) return true;
        if (metaTileEntity instanceof MetaTileEntityPrimitiveBlastFurnace) return true;
        return metaTileEntity instanceof MetaTileEntityPrimitiveWaterPump;
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        MultiblockDisplayText.builder(textList, isStructureFormed("MAIN"))
                .setWorkingStatus(cleanroomLogic.isWorkingEnabled(), cleanroomLogic.isActive())
                .addEnergyUsageLine(energyContainer)
                .addCustom(tl -> {
                    // Cleanliness status line
                    if (isStructureFormed("MAIN")) {
                        ITextComponent cleanState;
                        if (isClean()) {
                            cleanState = TextComponentUtil.translationWithColor(
                                    TextFormatting.GREEN,
                                    "gregtech.multiblock.cleanroom.clean_state",
                                    this.cleanAmount);
                        } else {
                            cleanState = TextComponentUtil.translationWithColor(
                                    TextFormatting.DARK_RED,
                                    "gregtech.multiblock.cleanroom.dirty_state",
                                    this.cleanAmount);
                        }

                        tl.add(TextComponentUtil.translationWithColor(
                                TextFormatting.GRAY,
                                "gregtech.multiblock.cleanroom.clean_status",
                                cleanState));
                    }
                })
                .addCustom(tl -> {
                    if (!cleanroomLogic.isVoltageHighEnough()) {
                        ITextComponent energyNeeded = new TextComponentString(
                                GTValues.VNF[cleanroomFilter.getMinTier()]);
                        tl.add(TextComponentUtil.translationWithColor(TextFormatting.YELLOW,
                                "gregtech.multiblock.cleanroom.low_tier", energyNeeded));
                    }
                })
                .addCustom(tl -> {
                    if (isStructureFormed("MAIN")) return;

                    // todo lang translations
                    tl.add(getWithButton("North: ", EnumFacing.NORTH));
                    tl.add(getWithButton("West: ", EnumFacing.WEST));
                    tl.add(getWithButton("South: ", EnumFacing.SOUTH));
                    tl.add(getWithButton("East: ", EnumFacing.EAST));
                    tl.add(getWithButton("Height: ", EnumFacing.DOWN));

                    tl.add(withButton(new TextComponentString(renderingAABB ? "[Disable Outline]" : "[Enable Outline]"), "render:" +
                            renderingAABB));
                })
                .addEnergyUsageExactLine(isClean() ? 4 : GTValues.VA[getEnergyTier()])
                .addWorkingStatusLine()
                .addProgressLine(getProgressPercent() / 100.0);
    }

    protected ITextComponent getWithButton(String text, EnumFacing facing) {
        RelativeDirection relative = facing == EnumFacing.DOWN ? RelativeDirection.DOWN : facingMap.get(facing);
        if (relative == null)
            return new TextComponentString("null value at facingMap.get(EnumFacing." + facing.getName() + ")");

        String name = relative.name();

        ITextComponent button = new TextComponentString(text + bounds[relative.ordinal()]);
        button.appendText(" ");
        button.appendSibling(withButton(new TextComponentString("[-]"), name + ":-"));
        button.appendText(" ");
        button.appendSibling(withButton(new TextComponentString("[+]"), name + ":+"));
        return button;
    }

    @Override
    protected void handleDisplayClick(String componentData, Widget.ClickData clickData) {
        super.handleDisplayClick(componentData, clickData);

        String[] data = componentData.split(":");

        if ("render".equals(data[0])) {
            boolean render = !Boolean.parseBoolean(data[1]);
            renderingAABB = render;
            writeCustomData(GregtechDataCodes.RENDER_UPDATE, buf -> buf.writeBoolean(render));
        }

        switch (data[0]) {
            case "LEFT" -> bounds[2] = MathHelper.clamp(bounds[2] + getFactor(data[1]), MIN_RADIUS, MAX_RADIUS);
            case "RIGHT" -> bounds[3] = MathHelper.clamp(bounds[3] + getFactor(data[1]), MIN_RADIUS, MAX_RADIUS);
            case "FRONT" -> bounds[4] = MathHelper.clamp(bounds[4] + getFactor(data[1]), MIN_RADIUS, MAX_RADIUS);
            case "BACK" -> bounds[5] = MathHelper.clamp(bounds[5] + getFactor(data[1]), MIN_RADIUS, MAX_RADIUS);
            case "DOWN" -> bounds[1] = MathHelper.clamp(bounds[1] + getFactor(data[1]), MIN_DEPTH, MAX_DEPTH);
            default -> {
                return;
            }
        }

        writeCustomData(GregtechDataCodes.UPDATE_STRUCTURE_SIZE, buf -> buf.writeVarIntArray(bounds));

        getSubstructure("MAIN").clearCache();
    }

    protected static int getFactor(String str) {
        return "+".equals(str) ? 1 : -1;
    }

    @Override
    protected void addWarningText(List<ITextComponent> textList) {
        MultiblockDisplayText.builder(textList, isStructureFormed("MAIN"), false)
                .addLowPowerLine(!drainEnergy(true))
                .addCustom(tl -> {
                    if (isStructureFormed("MAIN") && !isClean()) {
                        tl.add(TextComponentUtil.translationWithColor(
                                TextFormatting.YELLOW,
                                "gregtech.multiblock.cleanroom.warning_contaminated"));
                    }
                })
                .addMaintenanceProblemLines(getMaintenanceProblems());
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.machine.cleanroom.tooltip.1"));
        tooltip.add(I18n.format("gregtech.machine.cleanroom.tooltip.2"));
        tooltip.add(I18n.format("gregtech.machine.cleanroom.tooltip.3"));
        tooltip.add(I18n.format("gregtech.machine.cleanroom.tooltip.4"));

        if (TooltipHelper.isCtrlDown()) {
            tooltip.add("");
            tooltip.add(I18n.format("gregtech.machine.cleanroom.tooltip.5"));
            tooltip.add(I18n.format("gregtech.machine.cleanroom.tooltip.6"));
            tooltip.add(I18n.format("gregtech.machine.cleanroom.tooltip.7"));
            tooltip.add(I18n.format("gregtech.machine.cleanroom.tooltip.8"));
            tooltip.add(I18n.format("gregtech.machine.cleanroom.tooltip.9"));
            if (Mods.AppliedEnergistics2.isModLoaded()) {
                tooltip.add(I18n.format(AEConfig.instance().isFeatureEnabled(AEFeature.CHANNELS) ?
                        "gregtech.machine.cleanroom.tooltip.ae2.channels" :
                        "gregtech.machine.cleanroom.tooltip.ae2.no_channels"));
            }
            tooltip.add("");
        } else {
            tooltip.add(I18n.format("gregtech.machine.cleanroom.tooltip.hold_ctrl"));
        }
    }

    @SideOnly(Side.CLIENT)
    protected void renderAABB(boolean render) {
        if (render) {
            if (aabb == null) aabb = new AABBHighlightRenderer.AABBRender(new GreggyBlockPos(getPos()), new GreggyBlockPos(getPos()), 1, 1, 1, Long.MAX_VALUE);

            // reset coords
            aabb.from().from(getPos());
            aabb.to().from(getPos());

            // ordinal 0 is UP, which is always 0
            for (int i = 1; i < 6; i++) {
                EnumFacing facing = RelativeDirection.VALUES[i].getRelativeFacing(getFrontFacing(), getUpwardsFacing(), false);
                if (facing.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE) {
                    // from is always absolutely positive
                    aabb.from().offset(facing, bounds[i]);
                } else {
                    // to is always absolutely negative
                    aabb.to().offset(facing, bounds[i]);
                }
            }

            // offset by 1 since the renderer doesn't do it
            aabb.from().add(offset);

            // this is so scuffed im sorry for going back to kila level code :sob:
            // surely this won't cause the gc to blow up
            AABBHighlightRenderer.addAABB(aabb, () -> isValid() && getWorld().isBlockLoaded(getPos(), false) && getWorld().getTileEntity(getPos()) == getHolder());
        } else {
            AABBHighlightRenderer.removeAABB(aabb);
        }
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        this.getFrontOverlay().renderOrientedState(renderState, translation, pipeline, getFrontFacing(), isActive(),
                isWorkingEnabled());
    }

    @SideOnly(Side.CLIENT)
    @NotNull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return Textures.CLEANROOM_OVERLAY;
    }

    @Override
    public boolean checkCleanroomType(@NotNull CleanroomType type) {
        return type == this.cleanroomType;
    }

    @Override
    public void setCleanAmount(int amount) {
        this.cleanAmount = amount;
    }

    @Override
    public void adjustCleanAmount(int amount) {
        // do not allow negative cleanliness nor cleanliness above 100
        this.cleanAmount = MathHelper.clamp(this.cleanAmount + amount, 0, 100);
    }

    @Override
    public boolean isClean() {
        return this.cleanAmount >= CLEAN_AMOUNT_THRESHOLD;
    }

    @NotNull
    @Override
    public List<ITextComponent> getDataInfo() {
        return Collections.singletonList(new TextComponentTranslation(
                isClean() ? "gregtech.multiblock.cleanroom.clean_state" : "gregtech.multiblock.cleanroom.dirty_state",
                this.cleanAmount));
    }

    @Override
    public boolean isActive() {
        return super.isActive() && this.cleanroomLogic.isActive();
    }

    @Override
    public boolean isWorkingEnabled() {
        return this.cleanroomLogic.isWorkingEnabled();
    }

    @Override
    public void setWorkingEnabled(boolean isActivationAllowed) {
        if (!isActivationAllowed) // pausing sets not clean
            setCleanAmount(MIN_CLEAN_AMOUNT);
        this.cleanroomLogic.setWorkingEnabled(isActivationAllowed);
    }

    @Override
    public int getProgress() {
        return cleanroomLogic.getProgressTime();
    }

    @Override
    public int getMaxProgress() {
        return cleanroomLogic.getMaxProgress();
    }

    public int getProgressPercent() {
        return cleanroomLogic.getProgressPercent();
    }

    @Override
    public int getEnergyTier() {
        if (energyContainer == null) return GTValues.LV;
        return Math.min(GTValues.MAX,
                Math.max(GTValues.LV, GTUtility.getFloorTierByVoltage(energyContainer.getInputVoltage())));
    }

    @Override
    public long getEnergyInputPerSecond() {
        return energyContainer.getInputPerSec();
    }

    public boolean drainEnergy(boolean simulate) {
        if (energyContainer == null) return false;

        long energyToDrain = isClean() ? 4 :
                GTValues.VA[getEnergyTier()];
        long resultEnergy = energyContainer.getEnergyStored() - energyToDrain;
        if (resultEnergy >= 0L && resultEnergy <= energyContainer.getEnergyCapacity()) {
            if (!simulate)
                energyContainer.changeEnergy(-energyToDrain);
            return true;
        }
        return false;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == GregtechTileCapabilities.CAPABILITY_WORKABLE)
            return GregtechTileCapabilities.CAPABILITY_WORKABLE.cast(this);
        if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE)
            return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this);
        return super.getCapability(capability, side);
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == GregtechDataCodes.UPDATE_STRUCTURE_SIZE) {
            System.arraycopy(buf.readVarIntArray(), 0, bounds, 0, 6);
            renderAABB(renderingAABB);
        } else if (dataId == GregtechDataCodes.WORKABLE_ACTIVE) {
            this.cleanroomLogic.setActive(buf.readBoolean());
            scheduleRenderUpdate();
        } else if (dataId == GregtechDataCodes.WORKING_ENABLED) {
            this.cleanroomLogic.setWorkingEnabled(buf.readBoolean());
            scheduleRenderUpdate();
        } else if (dataId == GregtechDataCodes.RENDER_UPDATE) {
            this.renderingAABB = buf.readBoolean();
            renderAABB(this.renderingAABB);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(@NotNull NBTTagCompound data) {
        super.writeToNBT(data);
        data.setInteger("cleanAmount", this.cleanAmount);
        return this.cleanroomLogic.writeToNBT(data);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.cleanAmount = data.getInteger("cleanAmount");
        this.cleanroomLogic.readFromNBT(data);
        updateFacingMap();
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeInt(this.cleanAmount);
        this.cleanroomLogic.writeInitialSyncData(buf);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.cleanAmount = buf.readInt();
        this.cleanroomLogic.receiveInitialSyncData(buf);
    }

    @Override
    public void getSubItems(CreativeTabs creativeTab, NonNullList<ItemStack> subItems) {
        if (ConfigHolder.machines.enableCleanroom) {
            super.getSubItems(creativeTab, subItems);
        }
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        ArrayList<MultiblockShapeInfo> shapeInfo = new ArrayList<>();
        MultiblockShapeInfo.Builder builder = MultiblockShapeInfo.builder()
                .aisle("XXXXX", "XIHLX", "XXDXX", "XXXXX", "XXXXX")
                .aisle("XXXXX", "X   X", "G   G", "X   X", "XFFFX")
                .aisle("XXXXX", "X   X", "G   G", "X   X", "XFSFX")
                .aisle("XXXXX", "X   X", "G   G", "X   X", "XFFFX")
                .aisle("XMXEX", "XXOXX", "XXRXX", "XXXXX", "XXXXX")
                .where('X', MetaBlocks.CLEANROOM_CASING.getState(BlockCleanroomCasing.CasingType.PLASCRETE))
                .where('G', MetaBlocks.TRANSPARENT_CASING.getState(BlockGlassCasing.CasingType.CLEANROOM_GLASS))
                .where('S', MetaTileEntities.CLEANROOM, EnumFacing.SOUTH)
                .where(' ', Blocks.AIR.getDefaultState())
                .where('E', MetaTileEntities.ENERGY_INPUT_HATCH[GTValues.LV], EnumFacing.SOUTH)
                .where('I', MetaTileEntities.PASSTHROUGH_HATCH_ITEM, EnumFacing.NORTH)
                .where('L', MetaTileEntities.PASSTHROUGH_HATCH_FLUID, EnumFacing.NORTH)
                .where('H', MetaTileEntities.HULL[GTValues.HV], EnumFacing.NORTH)
                .where('D', MetaTileEntities.DIODES[GTValues.HV], EnumFacing.NORTH)
                .where('M',
                        () -> ConfigHolder.machines.enableMaintenance ? MetaTileEntities.MAINTENANCE_HATCH :
                                MetaBlocks.CLEANROOM_CASING.getState(BlockCleanroomCasing.CasingType.PLASCRETE),
                        EnumFacing.SOUTH)
                .where('O',
                        Blocks.IRON_DOOR.getDefaultState().withProperty(BlockDoor.FACING, EnumFacing.NORTH)
                                .withProperty(BlockDoor.HALF, BlockDoor.EnumDoorHalf.LOWER))
                .where('R', Blocks.IRON_DOOR.getDefaultState().withProperty(BlockDoor.FACING, EnumFacing.NORTH)
                        .withProperty(BlockDoor.HALF, BlockDoor.EnumDoorHalf.UPPER));

        GregTechAPI.CLEANROOM_FILTERS.entrySet().stream()
                .filter(entry -> entry.getValue().getCleanroomType() != null)
                .sorted(Comparator.comparingInt(entry -> entry.getValue().getTier()))
                .forEach(entry -> shapeInfo.add(builder.where('F', entry.getKey()).build()));

        return shapeInfo;
    }

    @Override
    protected boolean shouldShowVoidingModeButton() {
        return false;
    }
}

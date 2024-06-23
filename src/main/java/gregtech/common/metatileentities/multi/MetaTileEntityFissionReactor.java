package gregtech.common.metatileentities.multi;

import gregtech.api.GregTechAPI;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.ICoolantHandler;
import gregtech.api.capability.IFuelRodHandler;
import gregtech.api.capability.ILockableHandler;
import gregtech.api.capability.IMaintenanceHatch;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.AdvancedTextWidget;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.ProgressWidget;
import gregtech.api.gui.widgets.SliderWidget;
import gregtech.api.gui.widgets.ToggleButtonWidget;
import gregtech.api.gui.widgets.UpdatedSliderWidget;
import gregtech.api.metatileentity.IDataInfoProvider;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IControlRodPort;
import gregtech.api.metatileentity.multiblock.IFissionReactorHatch;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.IProgressBarMultiblock;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.nuclear.fission.FissionReactor;
import gregtech.api.nuclear.fission.components.ControlRod;
import gregtech.api.nuclear.fission.components.CoolantChannel;
import gregtech.api.nuclear.fission.components.FuelRod;
import gregtech.api.nuclear.fission.components.ReactorComponent;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.MultiblockShapeInfo;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.pattern.PatternStringError;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.properties.FissionFuelProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.api.util.BlockInfo;
import gregtech.api.util.GTStringUtils;
import gregtech.api.util.GTUtility;
import gregtech.api.util.RelativeDirection;
import gregtech.api.util.TextComponentUtil;
import gregtech.api.util.TextFormattingUtil;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.BlockFissionCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityCoolantExportHatch;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityFuelRodImportHatch;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class MetaTileEntityFissionReactor extends MultiblockWithDisplayBase
                                          implements IDataInfoProvider, IProgressBarMultiblock {

    private FissionReactor fissionReactor;
    private int diameter;
    private int heightTop;
    private int heightBottom;
    private int height;
    private int flowRate = 1;
    // Used for maintenance mechanics
    private boolean isFlowingCorrectly = true;
    private double controlRodInsertionValue;
    private LockingState lockingState = LockingState.UNLOCKED;

    private double temperature;
    private double maxTemperature;
    private double pressure;
    private double maxPressure;
    private double power;
    private double maxPower;
    private double kEff;
    private double fuelDepletionPercent;

    private NBTTagCompound transientData;

    public MetaTileEntityFissionReactor(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityFissionReactor(metaTileEntityId);
    }

    protected ModularUI.Builder createUITemplate(EntityPlayer entityPlayer) {
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 240, 208);

        // Display
        builder.image(4, 4, 232, 109, GuiTextures.DISPLAY);

        // triple bar
        ProgressWidget progressBar = new ProgressWidget(
                () -> this.getFillPercentage(0),
                4, 115, 76, 7,
                GuiTextures.PROGRESS_BAR_FISSION_HEAT, ProgressWidget.MoveType.HORIZONTAL)
                        .setHoverTextConsumer(list -> this.addBarHoverText(list, 0));
        builder.widget(progressBar);

        progressBar = new ProgressWidget(
                () -> this.getFillPercentage(1),
                82, 115, 76, 7,
                GuiTextures.PROGRESS_BAR_FISSION_PRESSURE, ProgressWidget.MoveType.HORIZONTAL)
                        .setHoverTextConsumer(list -> this.addBarHoverText(list, 1));
        builder.widget(progressBar);

        progressBar = new ProgressWidget(
                () -> this.getFillPercentage(2),
                160, 115, 76, 7,
                GuiTextures.PROGRESS_BAR_FISSION_ENERGY, ProgressWidget.MoveType.HORIZONTAL)
                        .setHoverTextConsumer(list -> this.addBarHoverText(list, 2));
        builder.widget(progressBar);

        builder.label(9, 9, getMetaFullName(), 0xFFFFFF);

        builder.widget(new UpdatedSliderWidget("gregtech.gui.fission.control_rod_insertion", 10, 60, 220,
                18, 0.0f, 1.0f,
                (float) controlRodInsertionValue, this::setControlRodInsertionValue,
                () -> (float) this.controlRodInsertionValue) {

            @Override
            protected String getDisplayString() {
                return I18n.format("gregtech.gui.fission.control_rod_insertion",
                        String.format("%.2f%%", this.getSliderValue() * 100));
            }
        }.setBackground(GuiTextures.DARK_SLIDER_BACKGROUND).setSliderIcon(GuiTextures.DARK_SLIDER_ICON));
        builder.widget(new SliderWidget("gregtech.gui.fission.coolant_flow", 10, 80, 220, 18, 0.0f, 16000.f, flowRate,
                this::setFlowRate).setBackground(GuiTextures.DARK_SLIDER_BACKGROUND)
                        .setSliderIcon(GuiTextures.DARK_SLIDER_ICON));

        builder.widget(new AdvancedTextWidget(9, 20, this::addDisplayText, 0xFFFFFF)
                .setMaxWidthLimit(220)
                .setClickHandler(this::handleDisplayClick));

        // Power Button

        builder.widget(new ToggleButtonWidget(215, 183, 18, 18, GuiTextures.BUTTON_LOCK,
                this::isLocked, this::tryLocking).shouldUseBaseBackground()
                        .setTooltipText("gregtech.gui.fission.lock"));
        builder.widget(new ImageWidget(215, 201, 18, 6, GuiTextures.BUTTON_POWER_DETAIL));

        // Voiding Mode Button
        builder.widget(new ImageWidget(215, 161, 18, 18, GuiTextures.BUTTON_VOID_NONE)
                .setTooltip("gregtech.gui.multiblock_voiding_not_supported"));

        builder.widget(new ImageWidget(215, 143, 18, 18, GuiTextures.BUTTON_NO_DISTINCT_BUSES)
                .setTooltip("gregtech.multiblock.universal.distinct_not_supported"));

        // Flex Button
        builder.widget(getFlexButton(215, 125, 18, 18));

        builder.bindPlayerInventory(entityPlayer.inventory, 125);
        return builder;
    }

    @Override
    public double getFillPercentage(int index) {
        if (index == 0) {
            return fissionReactor.temperature / fissionReactor.maxTemperature;
        } else if (index == 1) {
            return fissionReactor.pressure / fissionReactor.maxPressure;
        } else {
            return fissionReactor.power / fissionReactor.maxPower;
        }
    }

    @Override
    protected @NotNull Widget getFlexButton(int x, int y, int width, int height) {
        return new ToggleButtonWidget(x, y, width, height, this::areControlRodsRegulated,
                this::toggleControlRodRegulation).setButtonTexture(GuiTextures.BUTTON_CONTROL_ROD_HELPER)
                        .setTooltipText("gregtech.gui.fission.helper");
    }

    private void toggleControlRodRegulation(boolean b) {
        this.fissionReactor.controlRodRegulationOn = b;
    }

    private boolean areControlRodsRegulated() {
        return this.fissionReactor.controlRodRegulationOn;
    }

    @Override
    public void addBarHoverText(List<ITextComponent> list, int index) {
        if (index == 0) {
            list.add(new TextComponentTranslation("gregtech.gui.fission.temperature",
                    String.format("%.1f", this.temperature) + " / " + String.format("%.1f", this.maxTemperature)));
        } else if (index == 1) {
            list.add(new TextComponentTranslation("gregtech.gui.fission.pressure",
                    String.format("%.0f", this.pressure) + " / " + String.format("%.0f", this.maxPressure)));
        } else {
            list.add(new TextComponentTranslation("gregtech.gui.fission.power", String.format("%.1f", this.power),
                    String.format("%.1f", this.maxPower)));
        }
    }

    private void setFlowRate(float flowrate) {
        this.flowRate = (int) flowrate;
        if (flowRate < 1) flowRate = 1;
    }

    private void setControlRodInsertionValue(float value) {
        this.controlRodInsertionValue = value;
        if (fissionReactor != null)
            fissionReactor.updateControlRodInsertion(controlRodInsertionValue);
    }

    private boolean isLocked() {
        return lockingState == LockingState.LOCKED;
    }

    private TextFormatting getLockedTextColor() {
        if (lockingState == LockingState.LOCKED)
            return TextFormatting.GREEN;
        if (lockingState == LockingState.INVALID_COMPONENT)
            return TextFormatting.RED;
        if (lockingState == LockingState.UNLOCKED)
            return TextFormatting.DARK_AQUA;
        if (lockingState == LockingState.SHOULD_LOCK)
            return TextFormatting.BLACK;
        return getWorld().getWorldTime() % 4 >= 2 ? TextFormatting.RED : TextFormatting.YELLOW;
    }

    private void tryLocking(boolean lock) {
        if (!isStructureFormed())
            return;

        if (lock)
            lockAndPrepareReactor();
        else
            unlockAll();
    }

    @Override
    protected void addErrorText(List<ITextComponent> list) {
        if (lockingState != LockingState.LOCKED && lockingState != LockingState.UNLOCKED) {
            list.add(
                    new TextComponentTranslation("gregtech.gui.fission.lock." + lockingState.toString().toLowerCase()));
        }
    }

    @Override
    protected void addDisplayText(List<ITextComponent> list) {
        super.addDisplayText(list);
        list.add(
                TextComponentUtil.setColor(new TextComponentTranslation(
                        "gregtech.gui.fission.lock." + lockingState.toString().toLowerCase()), getLockedTextColor()));
        list.add(new TextComponentTranslation("gregtech.gui.fission.k_eff", String.format("%.4f", this.kEff)));
        list.add(new TextComponentTranslation("gregtech.gui.fission.depletion",
                String.format("%.2f", this.fuelDepletionPercent * 100)));
    }

    private Consumer<List<ITextComponent>> getStatsText() {
        return (list) -> {
            list.add(new TextComponentTranslation("gregtech.gui.fission.temperature",
                    String.format("%.1f", this.temperature) + " / " + String.format("%.1f", this.maxTemperature)));
            list.add(new TextComponentTranslation("gregtech.gui.fission.pressure",
                    String.format("%.0f", this.pressure) + " / " + String.format("%.0f", this.maxPressure)));
            list.add(new TextComponentTranslation("gregtech.gui.fission.power", String.format("%.1f", this.power),
                    String.format("%.1f", this.maxPower)));
            list.add(new TextComponentTranslation("gregtech.gui.fission.k_eff", String.format("%.4f", this.kEff)));
            list.add(new TextComponentTranslation("gregtech.gui.fission.depletion",
                    String.format("%.2f", Math.min(100, this.fuelDepletionPercent * 100))));
            if (this.getMaintenanceProblems() > 0) {
                byte maintenanceProblems = this.getMaintenanceProblems();
                if ((getMaintenanceProblems() & 1) == 0) {
                    list.add(TextComponentUtil.translationWithColor(
                            TextFormatting.DARK_RED,
                            "gregtech.multiblock.universal.problem.wrench"));
                } else if (((maintenanceProblems >> 1) & 1) == 0) {
                    list.add(TextComponentUtil.translationWithColor(
                            TextFormatting.DARK_RED,
                            "gregtech.multiblock.universal.problem.screwdriver"));
                } else if (((maintenanceProblems >> 2) & 1) == 0) {
                    list.add(TextComponentUtil.translationWithColor(
                            TextFormatting.DARK_RED,
                            "gregtech.multiblock.universal.problem.soft_mallet"));
                } else if (((maintenanceProblems >> 3) & 1) == 0) {
                    list.add(TextComponentUtil.translationWithColor(
                            TextFormatting.DARK_RED,
                            "gregtech.multiblock.universal.problem.hard_hammer"));
                } else if (((maintenanceProblems >> 4) & 1) == 0) {
                    list.add(TextComponentUtil.translationWithColor(
                            TextFormatting.DARK_RED,
                            "gregtech.multiblock.universal.problem.wire_cutter"));
                } else if (((maintenanceProblems >> 5) & 1) == 0) {
                    list.add(TextComponentUtil.translationWithColor(
                            TextFormatting.DARK_RED,
                            "gregtech.multiblock.universal.problem.crowbar"));
                }
            }
        };
    }

    public boolean isBlockEdge(@NotNull World world, @NotNull BlockPos pos, @NotNull EnumFacing direction, int steps) {
        BlockPos test = pos.offset(direction, steps);

        if (world.getBlockState(test).getBlock() == MetaBlocks.FISSION_CASING) {
            return false;
        }

        MetaTileEntity potentialTile = GTUtility.getMetaTileEntity(world, test);
        if (potentialTile == null) {
            return true;
        }

        return !(potentialTile instanceof IFissionReactorHatch || potentialTile instanceof IMaintenanceHatch);
    }

    /**
     * Uses the upper layer to determine the diameter of the structure
     */
    public int findDiameter(int heightAbove) {
        int i = 1;
        while (i <= 15) {
            if (this.isBlockEdge(this.getWorld(), this.getPos().up(heightAbove), this.getFrontFacing().getOpposite(),
                    i))
                break;
            i++;
        }
        return i;
    }

    /**
     * Checks for casings on top or bottom of the controller to determine the height of the reactor
     */
    public int findHeight(boolean top) {
        int i = 1;
        while (i <= 15) {
            if (this.isBlockEdge(this.getWorld(), this.getPos(), top ? EnumFacing.UP : EnumFacing.DOWN, i))
                break;
            i++;
        }
        return i - 1;
    }

    @Override
    public void updateFormedValid() {
        // Take in coolant, take in fuel, update reactor, output steam

        if (!this.getWorld().isRemote && this.getOffsetTimer() % 20 == 0) {
            if (this.lockingState == LockingState.LOCKED) {
                // Coolant handling
                if (this.getOffsetTimer() % 100 == 0) {
                    if (isFlowingCorrectly) {
                        if (getWorld().rand.nextDouble() > (1 - 0.01 * this.getNumMaintenanceProblems())) {
                            isFlowingCorrectly = false;
                        }
                    } else {
                        if (getWorld().rand.nextDouble() > 0.12 * this.getNumMaintenanceProblems()) {
                            isFlowingCorrectly = true;
                        }
                    }
                }
                this.fissionReactor.makeCoolantFlow(isFlowingCorrectly ? flowRate : 0);

                // Fuel handling
                if (this.fissionReactor.isDepleted()) {
                    boolean canWork = true;
                    for (IFuelRodHandler fuelImport : this.getAbilities(MultiblockAbility.IMPORT_FUEL_ROD)) {
                        if (fuelImport.getStackHandler().extractItem(0, 1, true).isEmpty()) {
                            canWork = false;
                            this.lockingState = LockingState.MISSING_FUEL;
                            break;
                        } else if (!((MetaTileEntityFuelRodImportHatch) fuelImport).getExportHatch(this.height - 1)
                                .getExportItems().insertItem(0,
                                        OreDictUnifier.get(OrePrefix.fuelRodHotDepleted, fuelImport.getFuel()), true)
                                .isEmpty()) {
                                    // We still need to know if the output is blocked, even if the recipe doesn't start
                                    // yet
                                    canWork = false;
                                    this.lockingState = LockingState.FUEL_CLOGGED;
                                    break;
                                }
                    }

                    for (IFuelRodHandler fuelImport : this.getAbilities(MultiblockAbility.IMPORT_FUEL_ROD)) {
                        if (fissionReactor.needsOutput) {
                            ((MetaTileEntityFuelRodImportHatch) fuelImport).getExportHatch(this.height - 1)
                                    .getExportItems().insertItem(0,
                                            OreDictUnifier.get(OrePrefix.fuelRodHotDepleted, fuelImport.getFuel()),
                                            false);
                            this.fissionReactor.fuelMass -= 60;
                        }
                        if (canWork) {
                            fuelImport.getStackHandler().extractItem(0, 1, false);
                            this.fissionReactor.fuelMass += 60;
                        }
                    }
                    if (canWork) {
                        fissionReactor.needsOutput = true;
                    } else {
                        this.unlockAll();
                    }

                    this.fissionReactor.fuelDepletion = 0.;
                }
            }
            this.updateReactorState();

            this.syncReactorStats();

            boolean melts = this.fissionReactor.checkForMeltdown();
            boolean explodes = this.fissionReactor.checkForExplosion();
            double hydrogen = this.fissionReactor.accumulatedHydrogen;
            if (melts) {
                this.performMeltdownEffects();
            }
            if (explodes) {
                this.performPrimaryExplosion();
                if (hydrogen > 1) {
                    this.performSecondaryExplosion(hydrogen);
                }
            }
        }
    }

    protected void performMeltdownEffects() {
        this.unlockAll();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(this.getPos());
        pos = pos.move(this.getFrontFacing().getOpposite(), diameter / 2);
        for (int i = 0; i <= this.heightBottom; i++) {
            this.getWorld().setBlockState(pos.add(0, -i, 0), Materials.Corium.getFluid().getBlock().getDefaultState());
            this.getWorld().setBlockState(pos.add(1, -i, 0), Materials.Corium.getFluid().getBlock().getDefaultState());
            this.getWorld().setBlockState(pos.add(-1, -i, 0), Materials.Corium.getFluid().getBlock().getDefaultState());
            this.getWorld().setBlockState(pos.add(0, -i, 1), Materials.Corium.getFluid().getBlock().getDefaultState());
            this.getWorld().setBlockState(pos.add(0, -i, -1), Materials.Corium.getFluid().getBlock().getDefaultState());
        }
        this.getWorld().setBlockState(pos.add(0, 1, 0), Materials.Corium.getFluid().getBlock().getDefaultState());
    }

    protected void performPrimaryExplosion() {
        this.unlockAll();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(this.getPos());
        pos = pos.move(this.getFrontFacing().getOpposite(), diameter / 2);
        this.getWorld().createExplosion(null, pos.getX(), pos.getY() + heightTop, pos.getZ(), 4.f, true);
    }

    protected void performSecondaryExplosion(double accumulatedHydrogen) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(this.getPos());
        pos = pos.move(this.getFrontFacing().getOpposite(), diameter / 2);
        this.getWorld().newExplosion(null, pos.getX(), pos.getY() + heightTop + 3, pos.getZ(),
                4.f + (float) Math.log(accumulatedHydrogen), true, true);
    }

    @NotNull
    @Override
    protected BlockPattern createStructurePattern() {
        this.heightTop = Math.max(Math.min(this.getWorld() != null ? this.findHeight(true) : 1, 7), 1);
        this.heightBottom = Math.max(Math.min(this.getWorld() != null ? this.findHeight(false) : 1, 7), 1);

        this.height = heightTop + heightBottom + 1;

        this.diameter = this.getWorld() != null ? Math.max(Math.min(this.findDiameter(heightTop), 15), 5) : 5;

        int radius = this.diameter % 2 == 0 ? (int) Math.floor(this.diameter / 2.f) :
                Math.round((this.diameter - 1) / 2.f);

        StringBuilder interiorBuilder = new StringBuilder();

        String[] interiorSlice = new String[this.diameter];
        String[] controllerSlice;
        String[] topSlice;
        String[] bottomSlice;

        // First loop over the matrix
        for (int i = 0; i < this.diameter; i++) {
            for (int j = 0; j < this.diameter; j++) {

                if (Math.pow(i - Math.floor(this.diameter / 2.), 2) + Math.pow(j - Math.floor(this.diameter / 2.), 2) <
                        Math.pow(radius + 0.5f, 2)) {
                    interiorBuilder.append('A');
                } else {
                    interiorBuilder.append(' ');
                }
            }

            interiorSlice[i] = interiorBuilder.toString();
            interiorBuilder.setLength(0);
        }

        // Second loop is to detect where to put walls, the controller and I/O, two fewer iterations are needed because
        // two strings always represent two walls on opposite sides
        interiorSlice[this.diameter - 1] = interiorSlice[0] = interiorSlice[0].replace('A', 'B');
        for (int i = 1; i < this.diameter - 1; i++) {
            for (int j = 0; j < this.diameter; j++) {
                if (interiorSlice[i].charAt(j) != 'A') {
                    continue;
                }

                // The integer division is fine here, since we want an odd diameter (say, 5) to go to the middle value
                // (2 in this case)
                int outerI = i + (int) Math.signum(i - (diameter / 2));

                if (Math.pow(outerI - Math.floor(this.diameter / 2.), 2) +
                        Math.pow(j - Math.floor(this.diameter / 2.), 2) >
                        Math.pow(radius + 0.5f, 2)) {
                    interiorSlice[i] = GTStringUtils.replace(interiorSlice[i], j, 'B');
                }

                int outerJ = j + (int) Math.signum(j - (diameter / 2));
                if (Math.pow(i - Math.floor(this.diameter / 2.), 2) +
                        Math.pow(outerJ - Math.floor(this.diameter / 2.), 2) >
                        Math.pow(radius + 0.5f, 2)) {
                    interiorSlice[i] = GTStringUtils.replace(interiorSlice[i], j, 'B');
                }
            }
        }

        controllerSlice = interiorSlice.clone();
        topSlice = interiorSlice.clone();
        bottomSlice = interiorSlice.clone();
        controllerSlice[0] = controllerSlice[0].substring(0, (int) Math.floor(this.diameter / 2.)) + 'S' +
                controllerSlice[0].substring((int) Math.floor(this.diameter / 2.) + 1);
        for (int i = 0; i < this.diameter; i++) {
            topSlice[i] = topSlice[i].replace('A', 'I');
            bottomSlice[i] = bottomSlice[i].replace('A', 'O');
        }

        return FactoryBlockPattern.start(RelativeDirection.RIGHT, RelativeDirection.FRONT, RelativeDirection.UP)
                .aisle(bottomSlice)
                .aisle(interiorSlice).setRepeatable(heightBottom - 1)
                .aisle(controllerSlice)
                .aisle(interiorSlice).setRepeatable(heightTop - 1)
                .aisle(topSlice)
                .where('S', selfPredicate())
                // A for interior components
                .where('A',
                        states(getFuelChannelState(), getControlRodChannelState(), getCoolantChannelState()).or(air()))
                // I for the inputs on the top
                .where('I',
                        states(getVesselState()).or(getImportPredicate()))
                // O for the outputs on the bottom
                .where('O',
                        states(getVesselState())
                                .or(abilities(MultiblockAbility.EXPORT_COOLANT, MultiblockAbility.EXPORT_FUEL_ROD)))
                // B for the vessel blocks on the walls
                .where('B',
                        states(getVesselState())
                                .or(abilities(MultiblockAbility.MAINTENANCE_HATCH).setMinGlobalLimited(1)
                                        .setMaxGlobalLimited(1)))
                .where(' ', any())
                .build();
    }

    public TraceabilityPredicate getImportPredicate() {
        MultiblockAbility<?>[] allowedAbilities = { MultiblockAbility.IMPORT_COOLANT, MultiblockAbility.IMPORT_FUEL_ROD,
                MultiblockAbility.CONTROL_ROD_PORT };
        return tilePredicate((state, tile) -> {
            if (!(tile instanceof IMultiblockAbilityPart<?> &&
                    ArrayUtils.contains(allowedAbilities, ((IMultiblockAbilityPart<?>) tile).getAbility()))) {
                return false;
            }
            if (tile instanceof IFissionReactorHatch hatchPart) {
                if (!hatchPart.checkValidity(height - 1)) {
                    state.setError(new PatternStringError("gregtech.multiblock.pattern.error.hatch_invalid"));
                    return false;
                }
                return true;
            }
            return false;
        },
                () -> Arrays.stream(allowedAbilities)
                        .flatMap(ability -> MultiblockAbility.REGISTRY.get(ability).stream())
                        .filter(Objects::nonNull).map(tile -> {
                            MetaTileEntityHolder holder = new MetaTileEntityHolder();
                            holder.setMetaTileEntity(tile);
                            holder.getMetaTileEntity().onPlacement();
                            holder.getMetaTileEntity().setFrontFacing(EnumFacing.SOUTH);
                            return new BlockInfo(MetaBlocks.MACHINE.getDefaultState(), holder);
                        }).toArray(BlockInfo[]::new));
    }

    @NotNull
    @Override
    public List<ITextComponent> getDataInfo() {
        List<ITextComponent> list = new ArrayList<>();
        list.add(new TextComponentTranslation("gregtech.multiblock.fission_reactor.diameter",
                new TextComponentTranslation(TextFormattingUtil.formatNumbers(this.diameter) + "m")
                        .setStyle(new Style().setColor(TextFormatting.YELLOW))));
        list.add(new TextComponentTranslation("gregtech.multiblock.fission_reactor.height",
                new TextComponentTranslation(TextFormattingUtil.formatNumbers(this.height) + "m")
                        .setStyle(new Style().setColor(TextFormatting.YELLOW))));
        return list;
    }

    @NotNull
    protected IBlockState getVesselState() {
        return MetaBlocks.FISSION_CASING.getState(BlockFissionCasing.FissionCasingType.REACTOR_VESSEL);
    }

    @NotNull
    protected IBlockState getFuelChannelState() {
        return MetaBlocks.FISSION_CASING.getState(BlockFissionCasing.FissionCasingType.FUEL_CHANNEL);
    }

    @NotNull
    protected IBlockState getControlRodChannelState() {
        return MetaBlocks.FISSION_CASING.getState(BlockFissionCasing.FissionCasingType.CONTROL_ROD_CHANNEL);
    }

    @NotNull
    IBlockState getCoolantChannelState() {
        return MetaBlocks.FISSION_CASING.getState(BlockFissionCasing.FissionCasingType.COOLANT_CHANNEL);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.FISSION_REACTOR_TEXTURE;
    }

    @SideOnly(Side.CLIENT)
    @NotNull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return Textures.FISSION_REACTOR_OVERLAY;
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);

        this.getFrontOverlay().renderOrientedState(renderState, translation, pipeline, getFrontFacing(), isActive(),
                true);
    }

    @Override
    public boolean isActive() {
        return isStructureFormed() && lockingState == LockingState.LOCKED;
    }

    @Override
    public void checkStructurePattern() {
        if (!this.isStructureFormed()) {
            reinitializeStructurePattern();
        }
        super.checkStructurePattern();
    }

    @Override
    public void invalidateStructure() {
        this.unlockAll();
        this.fissionReactor = null;
        this.temperature = 273;
        this.maxTemperature = 273;
        this.power = 0;
        this.kEff = 0;
        this.pressure = 0;
        this.maxPressure = 0;
        this.maxPower = 0;
        super.invalidateStructure();
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        if (fissionReactor == null) {
            fissionReactor = new FissionReactor(this.diameter - 2, this.height - 2, controlRodInsertionValue);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        data.setInteger("diameter", this.diameter);
        data.setInteger("heightTop", this.heightTop);
        data.setInteger("heightBottom", this.heightBottom);
        data.setInteger("flowRate", this.flowRate);
        data.setDouble("controlRodInsertion", this.controlRodInsertionValue);
        data.setBoolean("locked", this.lockingState == LockingState.LOCKED);
        if (fissionReactor != null) {
            data.setTag("transientData", this.fissionReactor.serializeNBT());
        }

        return super.writeToNBT(data);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.diameter = data.getInteger("diameter");
        this.heightTop = data.getInteger("heightTop");
        this.heightBottom = data.getInteger("heightBottom");
        this.flowRate = data.getInteger("flowRate");
        this.controlRodInsertionValue = data.getDouble("controlRodInsertion");
        this.height = this.heightTop + this.heightBottom + 1;
        if (data.getBoolean("locked")) {
            this.lockingState = LockingState.SHOULD_LOCK;
        }
        if (data.hasKey("transientData")) {
            transientData = data.getCompoundTag("transientData");
        }
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeInt(this.diameter);
        buf.writeInt(this.heightTop);
        buf.writeInt(this.heightBottom);
        buf.writeInt(this.flowRate);
        buf.writeDouble(this.controlRodInsertionValue);
        if (this.lockingState == LockingState.SHOULD_LOCK) {
            if (fissionReactor == null) {
                this.fissionReactor = new FissionReactor(this.diameter - 2, this.height - 2, controlRodInsertionValue);
            }
            this.lockAndPrepareReactor();
            this.fissionReactor.deserializeNBT(transientData);
        }
        buf.writeBoolean(this.lockingState == LockingState.LOCKED);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.diameter = buf.readInt();
        this.heightTop = buf.readInt();
        this.heightBottom = buf.readInt();
        this.flowRate = buf.readInt();
        this.controlRodInsertionValue = buf.readDouble();
        if (buf.readBoolean()) {
            this.lockingState = LockingState.LOCKED;
        }
    }

    public void syncReactorStats() {
        this.temperature = this.fissionReactor.temperature;
        this.maxTemperature = this.fissionReactor.maxTemperature;
        this.pressure = this.fissionReactor.pressure;
        this.maxPressure = this.fissionReactor.maxPressure;
        this.power = this.fissionReactor.power;
        this.maxPower = this.fissionReactor.maxPower;
        this.kEff = this.fissionReactor.kEff;
        this.controlRodInsertionValue = this.fissionReactor.controlRodInsertion;
        this.fuelDepletionPercent = this.fissionReactor.fuelDepletion / this.fissionReactor.maxFuelDepletion;
        writeCustomData(GregtechDataCodes.SYNC_REACTOR_STATS, (packetBuffer -> {
            packetBuffer.writeDouble(this.fissionReactor.temperature);
            packetBuffer.writeDouble(this.fissionReactor.maxTemperature);
            packetBuffer.writeDouble(this.fissionReactor.pressure);
            packetBuffer.writeDouble(this.fissionReactor.maxPressure);
            packetBuffer.writeDouble(this.fissionReactor.power);
            packetBuffer.writeDouble(this.fissionReactor.maxPower);
            packetBuffer.writeDouble(this.fissionReactor.kEff);
            packetBuffer.writeDouble(this.fissionReactor.controlRodInsertion);
            packetBuffer.writeDouble(this.fuelDepletionPercent);
        }));
        this.markDirty();
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);

        if (dataId == GregtechDataCodes.SYNC_REACTOR_STATS) {
            this.temperature = buf.readDouble();
            this.maxTemperature = buf.readDouble();
            this.pressure = buf.readDouble();
            this.maxPressure = buf.readDouble();
            this.power = buf.readDouble();
            this.maxPower = buf.readDouble();
            this.kEff = buf.readDouble();
            this.controlRodInsertionValue = buf.readDouble();
            this.fuelDepletionPercent = buf.readDouble();
        } else if (dataId == GregtechDataCodes.SYNC_LOCKING_STATE) {
            this.lockingState = buf.readEnumValue(LockingState.class);
        }
    }

    protected void lockAll() {
        for (ILockableHandler handler : this.getAbilities(MultiblockAbility.IMPORT_COOLANT)) {
            handler.setLock(true);
        }
        for (ILockableHandler handler : this.getAbilities(MultiblockAbility.IMPORT_FUEL_ROD)) {
            handler.setLock(true);
        }
    }

    protected void unlockAll() {
        // Deal with any unused fuel rods
        if (fissionReactor.needsOutput) {
            for (IFuelRodHandler fuelImport : this.getAbilities(MultiblockAbility.IMPORT_FUEL_ROD)) {
                ((MetaTileEntityFuelRodImportHatch) fuelImport).getExportHatch(this.height - 1)
                        .getExportItems().insertItem(0,
                                OreDictUnifier.get(OrePrefix.fuelRodHotDepleted, fuelImport.getFuel()), false);
            }
        }
        fissionReactor.needsOutput = false;
        for (ILockableHandler handler : this.getAbilities(MultiblockAbility.IMPORT_COOLANT)) {
            handler.setLock(false);
        }
        for (ILockableHandler handler : this.getAbilities(MultiblockAbility.IMPORT_FUEL_ROD)) {
            handler.setLock(false);
        }
        this.fissionReactor.turnOff();
        setLockingState(LockingState.UNLOCKED);
    }

    private void lockAndPrepareReactor() {
        this.lockAll();
        int radius = this.diameter / 2;     // This is the floor of the radius, the actual radius is 0.5 blocks
        // larger
        BlockPos reactorOrigin = this.getPos().offset(this.frontFacing.getOpposite(), radius);
        radius--;
        boolean foundFuel = false;
        for (int i = -radius; i <= radius; i++) {
            for (int j = -radius; j <= radius; j++) {
                if (Math.pow(i, 2) + Math.pow(j, 2) > Math.pow(radius, 2) + radius)         // (radius + .5)^2 =
                    // radius^2 + radius + .25
                    continue;
                BlockPos currentPos = reactorOrigin.offset(this.frontFacing.rotateYCCW(), i)
                        .offset(this.frontFacing.getOpposite(), j).offset(EnumFacing.UP, heightTop);
                if (getWorld().getTileEntity(currentPos) instanceof IGregTechTileEntity gtTe) {
                    MetaTileEntity mte = gtTe.getMetaTileEntity();
                    ReactorComponent component;

                    if (mte instanceof ICoolantHandler coolantIn) {
                        Fluid lockedFluid = coolantIn.getLockedObject();
                        if (lockedFluid != null) {
                            Material mat = GregTechAPI.materialManager.getMaterial(
                                    lockedFluid.getName());
                            if (mat != null && mat.hasProperty(PropertyKey.COOLANT)) {
                                coolantIn.setCoolant(mat);
                                BlockPos exportHatchPos = currentPos.offset(EnumFacing.DOWN, height - 1);
                                if (getWorld().getTileEntity(
                                        exportHatchPos) instanceof IGregTechTileEntity coolantOutCandidate) {
                                    MetaTileEntity coolantOutMTE = coolantOutCandidate.getMetaTileEntity();
                                    if (coolantOutMTE instanceof MetaTileEntityCoolantExportHatch coolantOut) {
                                        coolantOut.setCoolant(mat);
                                        component = new CoolantChannel(100050, 0, mat, 1000, coolantIn, coolantOut);
                                        fissionReactor.addComponent(component, i + radius, j + radius);
                                        continue;
                                    }
                                }
                            }
                        }
                        this.unlockAll();
                        setLockingState(LockingState.MISSING_COOLANT);
                        return;
                    } else if (mte instanceof IFuelRodHandler fuelIn) {
                        ItemStack lockedFuel = fuelIn.getStackHandler().getStackInSlot(0);
                        if (!lockedFuel.isEmpty()) {
                            MaterialStack mat = OreDictUnifier.getMaterial(lockedFuel);
                            if (mat != null && OreDictUnifier.getPrefix(lockedFuel) == OrePrefix.fuelRod) {
                                FissionFuelProperty property = mat.material.getProperty(PropertyKey.FISSION_FUEL);
                                if (property != null) {
                                    component = new FuelRod(property.getMaxTemperature(), 1, property, 650, 3);
                                    fuelIn.setFuel(mat.material);
                                    foundFuel = true;
                                    fissionReactor.addComponent(component, i + radius, j + radius);
                                    continue;
                                }
                            }
                        }
                        this.unlockAll();
                        setLockingState(LockingState.MISSING_FUEL);
                        return;
                    } else if (mte instanceof IControlRodPort controlIn) {
                        component = new ControlRod(100000, controlIn.hasModeratorTip(), 1, 800);
                        fissionReactor.addComponent(component, i + radius, j + radius);
                    }
                }
            }
        }
        if (!foundFuel) {
            this.unlockAll();
            setLockingState(LockingState.NO_FUEL_CHANNELS);
            return;
        }
        fissionReactor.prepareThermalProperties();
        fissionReactor.computeGeometry();
        setLockingState(LockingState.LOCKED);
    }

    private void updateReactorState() {
        this.fissionReactor.updatePower();
        this.fissionReactor.updateTemperature();
        this.fissionReactor.updatePressure();
        this.fissionReactor.updateNeutronPoisoning();
        this.fissionReactor.regulateControlRods();
    }

    protected void setLockingState(LockingState lockingState) {
        if (this.lockingState != lockingState) {
            writeCustomData(GregtechDataCodes.SYNC_LOCKING_STATE, (buf) -> buf.writeEnumValue(lockingState));
        }
        this.lockingState = lockingState;
    }

    private enum LockingState {
        // The reactor is locked
        LOCKED,
        // The reactor is unlocked
        UNLOCKED,
        // The reactor is supposed to be locked, but the locking logic is yet to run
        SHOULD_LOCK,
        // The reactor can't lock because it is missing fuel in a fuel channel
        MISSING_FUEL,
        // The reactor can't lock because it is missing coolant in a coolant channel
        MISSING_COOLANT,
        // The reactor can't lock because a fuel output is clogged
        FUEL_CLOGGED,
        // There are no fuel channels at all!
        NO_FUEL_CHANNELS,
        // The reactor can't lock because components are flagged as invalid
        INVALID_COMPONENT
    }

    public List<MultiblockShapeInfo> getMatchingShapes() {
        List<MultiblockShapeInfo> shapes = new ArrayList<>();

        for (int diameter = 5; diameter <= 15; diameter += 2) {
            int radius = diameter % 2 == 0 ? (int) Math.floor(diameter / 2.f) :
                    Math.round((diameter - 1) / 2.f);
            StringBuilder interiorBuilder = new StringBuilder();

            String[] interiorSlice = new String[diameter];
            String[] controllerSlice;
            String[] topSlice;
            String[] bottomSlice;

            // First loop over the matrix
            for (int i = 0; i < diameter; i++) {
                for (int j = 0; j < diameter; j++) {
                    if (Math.pow(i - Math.floor(diameter / 2.), 2) +
                            Math.pow(j - Math.floor(diameter / 2.), 2) <
                            Math.pow(radius + 0.5f, 2)) {
                        interiorBuilder.append('A');
                    } else {
                        interiorBuilder.append(' ');
                    }
                }

                interiorSlice[i] = interiorBuilder.toString();
                interiorBuilder.setLength(0);
            }

            // Second loop is to detect where to put walls, the controller and I/O
            for (int i = 0; i < diameter; i++) {
                for (int j = 0; j < diameter; j++) {
                    if (interiorSlice[i].charAt(j) != 'A') {
                        continue;
                    }

                    int outerI = i + (int) Math.signum(i - (diameter / 2));

                    if (Math.pow(outerI - Math.floor(diameter / 2.), 2) +
                            Math.pow(j - Math.floor(diameter / 2.), 2) >
                            Math.pow(radius + 0.5f, 2)) {
                        interiorSlice[i] = GTStringUtils.replace(interiorSlice[i], j, 'V');
                    }

                    int outerJ = j + (int) Math.signum(j - (diameter / 2));
                    if (Math.pow(i - Math.floor(diameter / 2.), 2) +
                            Math.pow(outerJ - Math.floor(diameter / 2.), 2) >
                            Math.pow(radius + 0.5f, 2)) {
                        interiorSlice[i] = GTStringUtils.replace(interiorSlice[i], j, 'V');
                    }
                }
            }

            controllerSlice = interiorSlice.clone();
            topSlice = interiorSlice.clone();
            bottomSlice = interiorSlice.clone();
            controllerSlice[0] = controllerSlice[0].substring(0, (int) Math.floor(diameter / 2.)) + "SM" +
                    controllerSlice[0].substring((int) Math.floor(diameter / 2.) + 2);

            // Example hatches
            controllerSlice[1] = controllerSlice[1].substring(0, (int) Math.floor(diameter / 2.) - 1) + "frc" +
                    controllerSlice[1].substring((int) Math.floor(diameter / 2.) + 2);
            controllerSlice[3] = controllerSlice[3].substring(0, (int) Math.floor(diameter / 2.)) + "r" +
                    controllerSlice[3].substring((int) Math.floor(diameter / 2.) + 1);

            topSlice[1] = topSlice[1].substring(0, (int) Math.floor(diameter / 2.) - 1) + "eqb" +
                    topSlice[1].substring((int) Math.floor(diameter / 2.) + 2);
            topSlice[3] = topSlice[3].substring(0, (int) Math.floor(diameter / 2.)) + "m" +
                    topSlice[3].substring((int) Math.floor(diameter / 2.) + 1);

            bottomSlice[1] = bottomSlice[1].substring(0, (int) Math.floor(diameter / 2.) - 1) + "gVd" +
                    bottomSlice[1].substring((int) Math.floor(diameter / 2.) + 2);

            for (int i = 0; i < diameter; i++) {
                topSlice[i] = topSlice[i].replace('A', 'V');
                bottomSlice[i] = bottomSlice[i].replace('A', 'V');
            }
            MultiblockShapeInfo.Builder builder = MultiblockShapeInfo.builder(RelativeDirection.RIGHT,
                    RelativeDirection.FRONT, RelativeDirection.UP);
            builder.aisle(topSlice);
            for (int i = 0; i < heightBottom - 1; i++) {
                builder.aisle(interiorSlice);
            }
            builder.aisle(controllerSlice);
            for (int i = 0; i < heightTop - 1; i++) {
                builder.aisle(interiorSlice);
            }
            builder.aisle(bottomSlice);
            shapes.add(builder.where('S', MetaTileEntities.FISSION_REACTOR, EnumFacing.NORTH)
                    // A for interior components, which are air here
                    .where('A', Blocks.AIR.getDefaultState())
                    // Technically a duplicate, but this just makes things easier
                    .where(' ', Blocks.AIR.getDefaultState())
                    // I for the inputs on the top
                    .where('V', this.getVesselState())
                    .where('f', this.getFuelChannelState())
                    .where('c', this.getCoolantChannelState())
                    .where('r', this.getControlRodChannelState())
                    .where('e', MetaTileEntities.FUEL_ROD_INPUT, EnumFacing.UP)
                    .where('g', MetaTileEntities.FUEL_ROD_OUTPUT, EnumFacing.DOWN)
                    .where('b', MetaTileEntities.COOLANT_INPUT, EnumFacing.DOWN)
                    .where('d', MetaTileEntities.COOLANT_OUTPUT, EnumFacing.UP)
                    .where('q', MetaTileEntities.CONTROL_ROD, EnumFacing.UP)
                    .where('m', MetaTileEntities.CONTROL_ROD_MODERATED, EnumFacing.DOWN)

                    // B for the vessel blocks on the walls
                    .where('M', () -> ConfigHolder.machines.enableMaintenance ? MetaTileEntities.MAINTENANCE_HATCH :
                            this.getVesselState(), EnumFacing.NORTH)
                    .build());
        }
        return shapes;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.machine.fission_reactor.tooltip.1"));
        tooltip.add(I18n.format("gregtech.machine.fission_reactor.tooltip.2"));
        tooltip.add(I18n.format("gregtech.machine.fission_reactor.tooltip.3"));
    }
}

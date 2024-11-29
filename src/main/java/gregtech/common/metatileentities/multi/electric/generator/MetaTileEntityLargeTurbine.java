package gregtech.common.metatileentities.multi.electric.generator;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.IRotorHolder;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.MultiblockFuelRecipeLogic;
import gregtech.api.metatileentity.ITieredMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.*;
import gregtech.api.metatileentity.multiblock.ui.MultiblockUIFactory;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.sync.FixedIntArraySyncValue;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.util.KeyUtil;
import gregtech.api.util.TextComponentUtil;
import gregtech.api.util.TextFormattingUtil;
import gregtech.client.renderer.ICubeRenderer;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.GenericSyncValue;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.StringSyncValue;
import com.cleanroommc.modularui.widgets.ProgressWidget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MetaTileEntityLargeTurbine extends FuelMultiblockController
                                        implements ITieredMetaTileEntity, ProgressBarMultiblock {

    private static final int SYNC_ROTOR = GregtechDataCodes.assignId();
    public final int tier;

    public final IBlockState casingState;
    public final IBlockState gearboxState;
    public final ICubeRenderer casingRenderer;
    public final boolean hasMufflerHatch;
    public final ICubeRenderer frontOverlay;

    private static final int MIN_DURABILITY_TO_WARN = 10;

    public IFluidHandler exportFluidHandler;

    public MetaTileEntityLargeTurbine(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap, int tier,
                                      IBlockState casingState, IBlockState gearboxState, ICubeRenderer casingRenderer,
                                      boolean hasMufflerHatch, ICubeRenderer frontOverlay) {
        super(metaTileEntityId, recipeMap, tier);
        this.casingState = casingState;
        this.gearboxState = gearboxState;
        this.casingRenderer = casingRenderer;
        this.hasMufflerHatch = hasMufflerHatch;
        this.frontOverlay = frontOverlay;
        this.tier = tier;
        this.recipeMapWorkable = new LargeTurbineWorkableHandler(this, tier);
        this.recipeMapWorkable.setMaximumOverclockVoltage(GTValues.V[tier]);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityLargeTurbine(metaTileEntityId, recipeMap, tier, casingState, gearboxState,
                casingRenderer, hasMufflerHatch, frontOverlay);
    }

    public IRotorHolder getRotorHolder() {
        List<IRotorHolder> abilities = getAbilities(MultiblockAbility.ROTOR_HOLDER);
        if (abilities.isEmpty())
            return null;
        return abilities.get(0);
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.exportFluidHandler = null;
    }

    /**
     * @return true if turbine is formed and it's face is free and contains
     *         only air blocks in front of rotor holder
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isRotorFaceFree() {
        IRotorHolder rotorHolder = getRotorHolder();
        if (rotorHolder != null)
            return isStructureFormed() && getRotorHolder().isFrontFaceFree();
        return false;
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        this.exportFluidHandler = new FluidTankList(true, getAbilities(MultiblockAbility.EXPORT_FLUIDS));
        ((LargeTurbineWorkableHandler) this.recipeMapWorkable).updateTanks();
    }

    @Override
    protected long getMaxVoltage() {
        long maxProduction = recipeMapWorkable.getMaxVoltage();
        long currentProduction = ((LargeTurbineWorkableHandler) recipeMapWorkable).boostProduction((int) maxProduction);
        if (isActive() && currentProduction <= maxProduction) {
            return recipeMapWorkable.getMaxVoltage();
        } else {
            return 0L;
        }
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        MultiblockFuelRecipeLogic recipeLogic = (MultiblockFuelRecipeLogic) recipeMapWorkable;

        MultiblockDisplayText.builder(textList, isStructureFormed())
                .setWorkingStatus(recipeLogic.isWorkingEnabled(), recipeLogic.isActive())
                .addEnergyProductionLine(getMaxVoltage(), recipeLogic.getRecipeEUt())
                .addCustom(tl -> {
                    if (isStructureFormed()) {
                        IRotorHolder rotorHolder = getRotorHolder();
                        if (rotorHolder.getRotorEfficiency() > 0) {
                            ITextComponent efficiencyInfo = TextComponentUtil.stringWithColor(
                                    TextFormatting.AQUA,
                                    TextFormattingUtil.formatNumbers(rotorHolder.getTotalEfficiency()) + "%");
                            tl.add(TextComponentUtil.translationWithColor(
                                    TextFormatting.GRAY,
                                    "gregtech.multiblock.turbine.efficiency",
                                    efficiencyInfo));
                        }
                    }
                })
                .addFuelNeededLine(recipeLogic.getRecipeFluidInputInfo(), recipeLogic.getPreviousRecipeDuration())
                .addWorkingStatusLine();
    }

    @Override
    protected void addWarningText(List<ITextComponent> textList) {
        MultiblockDisplayText.builder(textList, isStructureFormed(), false)
                .addCustom(tl -> {
                    if (isStructureFormed()) {
                        IRotorHolder rotorHolder = getRotorHolder();
                        if (rotorHolder.getRotorEfficiency() > 0) {
                            if (rotorHolder.getRotorDurabilityPercent() <= MIN_DURABILITY_TO_WARN) {
                                tl.add(TextComponentUtil.translationWithColor(
                                        TextFormatting.YELLOW,
                                        "gregtech.multiblock.turbine.rotor_durability_low"));
                            }
                        }
                    }
                })
                .addLowDynamoTierLine(isDynamoTierTooLow())
                .addMaintenanceProblemLines(getMaintenanceProblems());
    }

    @Override
    protected void addErrorText(List<ITextComponent> textList) {
        super.addErrorText(textList);
        if (isStructureFormed()) {
            if (!isRotorFaceFree()) {
                textList.add(TextComponentUtil.translationWithColor(TextFormatting.RED,
                        "gregtech.multiblock.turbine.obstructed"));
                textList.add(TextComponentUtil.translationWithColor(TextFormatting.GRAY,
                        "gregtech.multiblock.turbine.obstructed.desc"));
            }

            IRotorHolder rotorHolder = getRotorHolder();
            if (rotorHolder.getRotorEfficiency() <= 0) {
                textList.add(TextComponentUtil.translationWithColor(TextFormatting.RED,
                        "gregtech.multiblock.turbine.no_rotor"));
            }
        }
    }

    @Override
    protected MultiblockUIFactory createUIFactory() {
        MultiblockFuelRecipeLogic recipeLogic = (MultiblockFuelRecipeLogic) recipeMapWorkable;
        IntSyncValue efficiency = new IntSyncValue(
                () -> 0, null,
                () -> getRotorHolder().getRotorEfficiency(), null);
        IntSyncValue total = new IntSyncValue(
                () -> 0, null,
                () -> getRotorHolder().getTotalEfficiency(), null);
        IntSyncValue durability = new IntSyncValue(
                () -> 0, null,
                () -> getRotorHolder().getRotorDurabilityPercent(), null);
        BooleanSyncValue rotorFree = new BooleanSyncValue(
                this::isRotorFaceFree, null);
         StringSyncValue fuelAmount = new StringSyncValue(recipeLogic::getRecipeFluidInputInfo, null);
         IntSyncValue prevDuration = new IntSyncValue(recipeLogic::getPreviousRecipeDuration, null);

        return new MultiblockUIFactory(this)
                .syncValue("eff", efficiency)
                .syncValue("total", total)
                .syncValue("free", durability)
                .syncValue("dura", rotorFree)
                .syncValue("fuel_amount", fuelAmount)
                .syncValue("prev_duration", prevDuration)
                .configureDisplayText(builder -> builder
                        .setWorkingStatus(recipeLogic::isWorkingEnabled, recipeLogic::isActive)
                        .addEnergyProductionLine(getMaxVoltage(), recipeLogic.getRecipeEUt())
                        .addCustom(tl -> {
                            if (isStructureFormed()) {
                                if (efficiency.getIntValue() > 0) {
                                    IKey efficiencyInfo = KeyUtil.number(TextFormatting.AQUA,
                                            total.getIntValue(), "%");
                                    tl.add(KeyUtil.lang(TextFormatting.GRAY,
                                            "gregtech.multiblock.turbine.efficiency",
                                            efficiencyInfo));
                                }
                            }
                        })
                        .addFuelNeededLine(fuelAmount.getValue(), prevDuration::getIntValue)
                        .addWorkingStatusLine())
                .configureWarningText(false, builder -> builder
                        .addCustom(tl -> {
                            if (isStructureFormed()) {
                                if (efficiency.getIntValue() > 0) {
                                    if (durability.getIntValue() <= MIN_DURABILITY_TO_WARN) {
                                        tl.add(KeyUtil.lang(TextFormatting.YELLOW,
                                                "gregtech.multiblock.turbine.rotor_durability_low"));
                                    }
                                }
                            }
                        })
                        .addLowDynamoTierLine(isDynamoTierTooLow())
                        .addMaintenanceProblemLines(getMaintenanceProblems()))
                .configureErrorText(builder -> builder
                        .addCustom(keyList -> {
                            if (isStructureFormed()) {
                                if (!rotorFree.getBoolValue()) {
                                    keyList.add(KeyUtil.lang(TextFormatting.RED,
                                            "gregtech.multiblock.turbine.obstructed"));
                                    keyList.add(KeyUtil.lang(TextFormatting.GRAY,
                                            "gregtech.multiblock.turbine.obstructed.desc"));
                                }

                                if (efficiency.getIntValue() <= 0) {
                                    keyList.add(KeyUtil.lang(TextFormatting.RED,
                                            "gregtech.multiblock.turbine.no_rotor"));
                                }
                            }
                        }));
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.universal.tooltip.base_production_eut", GTValues.V[tier] * 2));
        tooltip.add(I18n.format("gregtech.multiblock.turbine.efficiency_tooltip", GTValues.VNF[tier]));
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("CCCC", "CHHC", "CCCC")
                .aisle("CHHC", "RGGR", "CHHC")
                .aisle("CCCC", "CSHC", "CCCC")
                .where('S', selfPredicate())
                .where('G', states(getGearBoxState()))
                .where('C', states(getCasingState()))
                .where('R', metaTileEntities(MultiblockAbility.REGISTRY.get(MultiblockAbility.ROTOR_HOLDER).stream()
                        .filter(mte -> (mte instanceof ITieredMetaTileEntity) &&
                                (((ITieredMetaTileEntity) mte).getTier() >= tier))
                        .toArray(MetaTileEntity[]::new))
                                .addTooltips("gregtech.multiblock.pattern.clear_amount_3")
                                .addTooltip("gregtech.multiblock.pattern.error.limited.1", GTValues.VN[tier])
                                .setExactLimit(1)
                                .or(abilities(MultiblockAbility.OUTPUT_ENERGY)).setExactLimit(1))
                .where('H', states(getCasingState()).or(autoAbilities(false, true, false, false, true, true, true)))
                .build();
    }

    @Override
    public String[] getDescription() {
        return new String[] { I18n.format("gregtech.multiblock.large_turbine.description") };
    }

    public IBlockState getCasingState() {
        return casingState;
    }

    public IBlockState getGearBoxState() {
        return gearboxState;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return casingRenderer;
    }

    @SideOnly(Side.CLIENT)
    @NotNull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return frontOverlay;
    }

    @Override
    public boolean hasMufflerMechanics() {
        return hasMufflerHatch;
    }

    @Override
    public boolean isStructureObstructed() {
        return super.isStructureObstructed() || !isRotorFaceFree();
    }

    @Override
    public int getTier() {
        return tier;
    }

    @Override
    public boolean canVoidRecipeItemOutputs() {
        return true;
    }

    @Override
    public boolean canVoidRecipeFluidOutputs() {
        return true;
    }

    @Override
    public boolean shouldShowVoidingModeButton() {
        return false;
    }

    @Override
    public int getProgressBarCount() {
        return 3;
    }

    @Override
    public @NotNull ProgressWidget createProgressBar(PanelSyncManager panelSyncManager, int index) {
        return switch (index) {
            case 0 -> {
                FixedIntArraySyncValue fuelValue = new FixedIntArraySyncValue(this::getFuelAmount, null);
                StringSyncValue fuelNameValue = new StringSyncValue(() -> {
                    FluidStack stack = ((MultiblockFuelRecipeLogic) recipeMapWorkable).getInputFluidStack();
                    if (stack == null) {
                        return null;
                    }
                    Fluid fluid = stack.getFluid();
                    if (fluid == null) {
                        return null;
                    }
                    return fluid.getName();
                }, null);
                panelSyncManager.syncValue("fuel_amount", fuelValue);
                panelSyncManager.syncValue("fuel_name", fuelNameValue);

                yield new ProgressWidget()
                        .progress(() -> fuelValue.getValue()[1] == 0 ? 0 :
                                1.0 * fuelValue.getValue()[0] / fuelValue.getValue()[1])
                        .texture(GTGuiTextures.PROGRESS_BAR_LCE_FUEL, MultiblockUIFactory.Bars.THIRD_WIDTH)
                        .tooltipBuilder(t -> createFuelTooltip(t, fuelValue, fuelNameValue));
            }
            case 1 -> {
                IntSyncValue rotorSpeedValue = new IntSyncValue(() -> {
                    IRotorHolder rotorHolder = getRotorHolder();
                    if (rotorHolder == null) {
                        return 0;
                    }
                    return rotorHolder.getRotorSpeed();
                }, null);

                IntSyncValue rotorMaxSpeedValue = new IntSyncValue(() -> {
                    IRotorHolder rotorHolder = getRotorHolder();
                    if (rotorHolder == null) {
                        return 0;
                    }
                    return rotorHolder.getMaxRotorHolderSpeed();
                }, null);

                panelSyncManager.syncValue("rotor_speed", rotorSpeedValue);
                panelSyncManager.syncValue("rotor_max_speed", rotorMaxSpeedValue);

                yield new ProgressWidget()
                        .progress(() -> rotorMaxSpeedValue.getIntValue() == 0 ? 0 :
                                1.0 * rotorSpeedValue.getIntValue() / rotorMaxSpeedValue.getIntValue())
                        .texture(GTGuiTextures.PROGRESS_BAR_TURBINE_ROTOR_SPEED, MultiblockUIFactory.Bars.THIRD_WIDTH)
                        .tooltip(tooltip -> tooltip.setAutoUpdate(true))
                        .tooltipBuilder(t -> {
                            if (isStructureFormed()) {
                                int speed = rotorSpeedValue.getIntValue();
                                int maxSpeed = rotorMaxSpeedValue.getIntValue();
                                float percent = maxSpeed == 0 ? 0 : 1.0f * speed / maxSpeed;

                                // TODO working dynamic color substitutions into IKey.lang
                                if (percent < 0.4) {
                                    t.addLine(
                                            IKey.lang("gregtech.multiblock.turbine.rotor_speed.low", speed, maxSpeed));
                                } else if (percent < 0.8) {
                                    t.addLine(IKey.lang("gregtech.multiblock.turbine.rotor_speed.medium", speed,
                                            maxSpeed));
                                } else {
                                    t.addLine(
                                            IKey.lang("gregtech.multiblock.turbine.rotor_speed.high", speed, maxSpeed));
                                }
                            } else {
                                t.addLine(IKey.lang("gregtech.multiblock.invalid_structure"));
                            }
                        });
            }
            case 2 -> {
                IntSyncValue durabilityValue = new IntSyncValue(() -> {
                    IRotorHolder rotorHolder = getRotorHolder();
                    if (rotorHolder == null) {
                        return 0;
                    }
                    return rotorHolder.getRotorDurabilityPercent();
                }, null);
                IntSyncValue efficiencyValue = new IntSyncValue(() -> {
                    IRotorHolder rotorHolder = getRotorHolder();
                    if (rotorHolder == null) {
                        return 0;
                    }
                    return rotorHolder.getRotorEfficiency();
                }, null);

                panelSyncManager.syncValue("rotor_durability", durabilityValue);
                panelSyncManager.syncValue("rotor_efficiency", efficiencyValue);

                yield new ProgressWidget()
                        .progress(() -> durabilityValue.getIntValue() / 100.0)
                        .texture(GTGuiTextures.PROGRESS_BAR_TURBINE_ROTOR_DURABILITY,
                                MultiblockUIFactory.Bars.THIRD_WIDTH)
                        .tooltip(tooltip -> tooltip.setAutoUpdate(true))
                        .tooltipBuilder(t -> {
                            if (isStructureFormed()) {
                                if (efficiencyValue.getIntValue() <= 0) {
                                    t.addLine(IKey.lang("gregtech.multiblock.turbine.no_rotor"));
                                } else {
                                    int durability = durabilityValue.getIntValue();
                                    // TODO working dynamic color substitutions into IKey.lang
                                    if (durability > 40) {
                                        t.addLine(IKey.lang("gregtech.multiblock.turbine.rotor_durability.high",
                                                durability));
                                    } else if (durability > MIN_DURABILITY_TO_WARN) {
                                        t.addLine(IKey.lang("gregtech.multiblock.turbine.rotor_durability.medium",
                                                durability));
                                    } else {
                                        t.addLine(IKey.lang("gregtech.multiblock.turbine.rotor_durability.low",
                                                durability));
                                    }
                                }
                            } else {
                                t.addLine(IKey.lang("gregtech.multiblock.invalid_structure"));
                            }
                        });
            }
            default -> throw new IllegalStateException("Invalid index received " + index);
        };
    }

    /**
     * @return an array of [fuel stored, fuel capacity]
     */
    private int[] getFuelAmount() {
        if (getInputFluidInventory() != null) {
            MultiblockFuelRecipeLogic recipeLogic = (MultiblockFuelRecipeLogic) recipeMapWorkable;
            if (recipeLogic.getInputFluidStack() != null) {
                FluidStack testStack = recipeLogic.getInputFluidStack().copy();
                testStack.amount = Integer.MAX_VALUE;
                return getTotalFluidAmount(testStack, getInputFluidInventory());
            }
        }
        return new int[2];
    }
}

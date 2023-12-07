package gregtech.common.metatileentities.multi.electric.generator;

import gregtech.api.GTValues;
import gregtech.api.capability.IRotorHolder;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.MultiblockFuelRecipeLogic;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.metatileentity.ITieredMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.*;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.recipes.RecipeMap;
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
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MetaTileEntityLargeTurbine extends FuelMultiblockController
                                        implements ITieredMetaTileEntity, IProgressBarMultiblock {

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
        if (isActive() && currentProduction < maxProduction) {
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
    protected boolean shouldShowVoidingModeButton() {
        return false;
    }

    @Override
    public int getNumProgressBars() {
        return 3;
    }

    @Override
    public double getFillPercentage(int index) {
        if (index == 0) {
            int[] fuelAmount = new int[2];
            if (getInputFluidInventory() != null) {
                MultiblockFuelRecipeLogic recipeLogic = (MultiblockFuelRecipeLogic) recipeMapWorkable;
                if (recipeLogic.getInputFluidStack() != null) {
                    FluidStack testStack = recipeLogic.getInputFluidStack().copy();
                    testStack.amount = Integer.MAX_VALUE;
                    fuelAmount = getTotalFluidAmount(testStack, getInputFluidInventory());
                }
            }
            return fuelAmount[1] != 0 ? 1.0 * fuelAmount[0] / fuelAmount[1] : 0;
        } else if (index == 1) {
            IRotorHolder rotorHolder = getRotorHolder();
            return rotorHolder != null ? 1.0 * rotorHolder.getRotorSpeed() / rotorHolder.getMaxRotorHolderSpeed() : 0;
        } else {
            IRotorHolder rotorHolder = getRotorHolder();
            return rotorHolder != null ? 1.0 * rotorHolder.getRotorDurabilityPercent() / 100 : 0;
        }
    }

    @Override
    public TextureArea getProgressBarTexture(int index) {
        if (index == 0) {
            return GuiTextures.PROGRESS_BAR_LCE_FUEL;
        } else if (index == 1) {
            return GuiTextures.PROGRESS_BAR_TURBINE_ROTOR_SPEED;
        } else {
            return GuiTextures.PROGRESS_BAR_TURBINE_ROTOR_DURABILITY;
        }
    }

    @Override
    public void addBarHoverText(List<ITextComponent> hoverList, int index) {
        if (index == 0) {
            // Fuel
            addFuelText(hoverList);
        } else if (index == 1) {
            // Rotor speed
            IRotorHolder rotorHolder = getRotorHolder();
            if (rotorHolder == null || rotorHolder.getRotorEfficiency() <= 0) {
                hoverList.add(TextComponentUtil.translationWithColor(TextFormatting.YELLOW,
                        "gregtech.multiblock.turbine.no_rotor"));
            } else {
                int rotorSpeed = rotorHolder.getRotorSpeed();
                int rotorMaxSpeed = rotorHolder.getMaxRotorHolderSpeed();
                ITextComponent rpmTranslated = TextComponentUtil.translationWithColor(
                        getRotorSpeedColor(rotorSpeed, rotorMaxSpeed),
                        "gregtech.multiblock.turbine.rotor_rpm_unit_name");
                ITextComponent rotorInfo = TextComponentUtil.translationWithColor(
                        getRotorSpeedColor(rotorSpeed, rotorMaxSpeed),
                        "%s / %s %s",
                        TextFormattingUtil.formatNumbers(rotorSpeed),
                        TextFormattingUtil.formatNumbers(rotorMaxSpeed),
                        rpmTranslated);
                hoverList.add(TextComponentUtil.translationWithColor(
                        TextFormatting.GRAY,
                        "gregtech.multiblock.turbine.rotor_speed",
                        rotorInfo));
            }
        } else {
            // Rotor durability
            IRotorHolder rotorHolder = getRotorHolder();
            if (rotorHolder == null || rotorHolder.getRotorEfficiency() <= 0) {
                // No rotor found
                hoverList.add(TextComponentUtil.translationWithColor(TextFormatting.YELLOW,
                        "gregtech.multiblock.turbine.no_rotor"));
            } else {
                int rotorDurability = rotorHolder.getRotorDurabilityPercent();
                ITextComponent rotorInfo = TextComponentUtil.stringWithColor(
                        getRotorDurabilityColor(rotorDurability),
                        rotorDurability + "%");
                hoverList.add(TextComponentUtil.translationWithColor(
                        TextFormatting.GRAY,
                        "gregtech.multiblock.turbine.rotor_durability",
                        rotorInfo));
            }
        }
    }

    private TextFormatting getRotorDurabilityColor(int durability) {
        if (durability > 40) {
            return TextFormatting.GREEN;
        } else if (durability > MIN_DURABILITY_TO_WARN) {
            return TextFormatting.YELLOW;
        } else {
            return TextFormatting.RED;
        }
    }

    private TextFormatting getRotorSpeedColor(int rotorSpeed, int maxRotorSpeed) {
        double speedRatio = 1.0 * rotorSpeed / maxRotorSpeed;
        if (speedRatio < 0.4) {
            return TextFormatting.RED;
        } else if (speedRatio < 0.8) {
            return TextFormatting.YELLOW;
        } else {
            return TextFormatting.GREEN;
        }
    }
}

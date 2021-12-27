package gregtech.common.metatileentities.multi;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IFuelInfo;
import gregtech.api.capability.IFuelable;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.ItemFuelInfo;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.capability.tool.ISoftHammerItem;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.*;
import gregtech.api.gui.Widget.ClickData;
import gregtech.api.metatileentity.MTETrait;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.metatileentity.sound.ISoundCreator;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.recipes.ModHandler;
import gregtech.api.sound.GTSounds;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockBoilerCasing.BoilerCasingType;
import gregtech.api.render.ICubeRenderer;
import gregtech.api.render.OrientedOverlayRenderer;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.api.sound.GTSounds;
import gregtech.common.blocks.BlockFireboxCasing;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import java.util.*;

import static gregtech.api.gui.widgets.AdvancedTextWidget.withButton;
import static gregtech.api.gui.widgets.AdvancedTextWidget.withHoverTextTranslate;

public class MetaTileEntityLargeBoiler extends MultiblockWithDisplayBase implements ISoundCreator {

    public final BoilerType boilerType;
    protected BoilerRecipeLogic recipeLogic;

    private FluidTankList fluidImportInventory;
    private ItemHandlerList itemImportInventory;
    private FluidTankList steamOutputTank;

    private int throttlePercentage = 100;

    public MetaTileEntityLargeBoiler(ResourceLocation metaTileEntityId, BoilerType boilerType) {
        super(metaTileEntityId);
        this.boilerType = boilerType;
        this.recipeLogic = new BoilerRecipeLogic(this);
        resetTileAbilities();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityLargeBoiler(metaTileEntityId, boilerType);
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        initializeAbilities();
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        resetTileAbilities();
        this.throttlePercentage = 100; // todo sync
        this.recipeLogic.invalidate();
        replaceFireboxAsActive(false);
    }

    private void initializeAbilities() {
        this.fluidImportInventory = new FluidTankList(true, getAbilities(MultiblockAbility.IMPORT_FLUIDS));
        this.itemImportInventory = new ItemHandlerList(getAbilities(MultiblockAbility.IMPORT_ITEMS));
        this.steamOutputTank = new FluidTankList(true, getAbilities(MultiblockAbility.EXPORT_FLUIDS));
    }

    private void resetTileAbilities() {
        this.fluidImportInventory = new FluidTankList(true);
        this.itemImportInventory = new ItemHandlerList(Collections.emptyList());
        this.steamOutputTank = new FluidTankList(true);
    }

    @Override
    public void onRemoval() {
        super.onRemoval();
        if (!getWorld().isRemote && isStructureFormed()) {
            replaceFireboxAsActive(false);
        }
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        super.addDisplayText(textList);
        if (isStructureFormed()) { // todo
            textList.add(new TextComponentTranslation("gregtech.multiblock.large_boiler.temperature", recipeLogic.getHeatScaled()));
            textList.add(new TextComponentTranslation("gregtech.multiblock.large_boiler.steam_output", recipeLogic.getLastTickSteam()));

            ITextComponent throttleText = new TextComponentTranslation("gregtech.multiblock.large_boiler.throttle", getThrottle());
            withHoverTextTranslate(throttleText, "gregtech.multiblock.large_boiler.throttle.tooltip");
            textList.add(throttleText);

            ITextComponent buttonText = new TextComponentTranslation("gregtech.multiblock.large_boiler.throttle_modify");
            buttonText.appendText(" ");
            buttonText.appendSibling(withButton(new TextComponentString("[-]"), "sub"));
            buttonText.appendText(" ");
            buttonText.appendSibling(withButton(new TextComponentString("[+]"), "add"));
            textList.add(buttonText);
        }
    }

    @Override
    protected void handleDisplayClick(String componentData, ClickData clickData) {
        super.handleDisplayClick(componentData, clickData);
        int modifier = componentData.equals("add") ? 1 : -1;
        int result = (clickData.isShiftClick ? 1 : 5) * modifier;
        this.throttlePercentage = MathHelper.clamp(throttlePercentage + result, 20, 100);
    }

    private int setupRecipeAndConsumeInputs() {
//        for (IFluidTank fluidTank : fluidImportInventory.getFluidTanks()) {
//            FluidStack fuelStack = fluidTank.drain(Integer.MAX_VALUE, false);
//            if (fuelStack == null || ModHandler.isWater(fuelStack))
//                continue; //ignore empty tanks and water
//            FuelRecipe dieselRecipe = RecipeMaps.COMBUSTION_GENERATOR_FUELS.findRecipe(GTValues.V[9], fuelStack);
//            if (dieselRecipe != null) {
//                int fuelAmountToConsume = (int) Math.ceil(dieselRecipe.getRecipeFluid().amount * CONSUMPTION_MULTIPLIER * boilerType.fuelConsumptionMultiplier * getThrottleMultiplier());
//                if (fuelStack.amount >= fuelAmountToConsume) {
//                    fluidTank.drain(fuelAmountToConsume, true);
//                    long recipeVoltage = FuelRecipeLogic.getTieredVoltage(dieselRecipe.getMinVoltage());
//                    int voltageMultiplier = (int) Math.max(1L, recipeVoltage / GTValues.V[GTValues.LV]);
//                    return (int) Math.ceil(dieselRecipe.getDuration() * CONSUMPTION_MULTIPLIER / 2.0 * voltageMultiplier * getThrottleMultiplier());
//                } else continue;
//            }
//            FuelRecipe denseFuelRecipe = RecipeMaps.SEMI_FLUID_GENERATOR_FUELS.findRecipe(GTValues.V[9], fuelStack);
//            if (denseFuelRecipe != null) {
//                int fuelAmountToConsume = (int) Math.ceil(denseFuelRecipe.getRecipeFluid().amount * CONSUMPTION_MULTIPLIER * boilerType.fuelConsumptionMultiplier * getThrottleMultiplier());
//                if (fuelStack.amount >= fuelAmountToConsume) {
//                    fluidTank.drain(fuelAmountToConsume, true);
//                    long recipeVoltage = FuelRecipeLogic.getTieredVoltage(denseFuelRecipe.getMinVoltage());
//                    int voltageMultiplier = (int) Math.max(1L, recipeVoltage / GTValues.V[GTValues.LV]);
//                    return (int) Math.ceil(denseFuelRecipe.getDuration() * CONSUMPTION_MULTIPLIER * 2 * voltageMultiplier * getThrottleMultiplier());
//                }
//            }
//        }
        for (int slotIndex = 0; slotIndex < itemImportInventory.getSlots(); slotIndex++) {
            ItemStack itemStack = itemImportInventory.getStackInSlot(slotIndex);
            int fuelBurnValue = (int) Math.ceil(TileEntityFurnace.getItemBurnTime(itemStack) / (50.0 * boilerType.fuelConsumptionMultiplier * getThrottleMultiplier()));
            if (fuelBurnValue > 0) {
                if (itemStack.getCount() == 1) {
                    ItemStack containerItem = itemStack.getItem().getContainerItem(itemStack);
                    itemImportInventory.setStackInSlot(slotIndex, containerItem);
                } else {
                    itemStack.shrink(1);
                    itemImportInventory.setStackInSlot(slotIndex, itemStack);
                }
                return fuelBurnValue;
            }
        }
        return 0;
    }

    private double getThrottleMultiplier() {
        return throttlePercentage / 100.0;
    }

    private double getThrottleEfficiency() {
        return MathHelper.clamp(1.0 + 0.3 * Math.log(getThrottleMultiplier()), 0.4, 1.0);
    }
*/
    @Override
    public boolean isActive() {
        return super.isActive() && recipeLogic.isActive() && recipeLogic.isWorkingEnabled();
    }

    private void replaceFireboxAsActive(boolean isActive) {
        BlockPos centerPos = getPos().offset(getFrontFacing().getOpposite()).down();
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos blockPos = centerPos.add(x, 0, z);
                IBlockState blockState = getWorld().getBlockState(blockPos);
                if (blockState.getBlock() instanceof BlockFireboxCasing) {
                    blockState = blockState.withProperty(BlockFireboxCasing.ACTIVE, isActive);
                    getWorld().setBlockState(blockPos, blockState);
                }
            }
        }
    }

    @Override
    public int getLightValueForPart(IMultiblockPart sourcePart) {
        return sourcePart == null ? 0 : (isActive() && recipeLogic.isWorkingEnabled() ? 15 : 0);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return boilerType == null ? null : FactoryBlockPattern.start()
                .aisle("XXX", "CCC", "CCC", "CCC")
                .aisle("XXX", "CPC", "CPC", "CCC")
                .aisle("XXX", "CSC", "CCC", "CCC")
                .where('S', selfPredicate())
                .where('P', states(boilerType.pipeState))
                .where('X', states(boilerType.fireboxState).setMinGlobalLimited(4)
                        .or(abilities(MultiblockAbility.IMPORT_FLUIDS).setMinGlobalLimited(1))
                        .or(abilities(MultiblockAbility.IMPORT_ITEMS).setMinGlobalLimited(1))
                        .or(autoAbilities())) // muffler, maintenance
                .where('C', states(boilerType.casingState).setMinGlobalLimited(20)
                        .or(abilities(MultiblockAbility.EXPORT_FLUIDS).setMinGlobalLimited(1)))
                .build();
    }

    @Override
    public String[] getDescription() {
        return new String[]{I18n.format("gregtech.multiblock.large_boiler.description")};
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        this.getFrontOverlay().renderOrientedState(renderState, translation, pipeline, getFrontFacing(), isActive(), recipeLogic.isWorkingEnabled());
    }

    @Nonnull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return boilerType.frontOverlay;
    }

    private boolean isFireboxPart(IMultiblockPart sourcePart) {
        return isStructureFormed() && (((MetaTileEntity) sourcePart).getPos().getY() < getPos().getY());
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        if (sourcePart != null && isFireboxPart(sourcePart)) {
            return isActive() && recipeLogic.isWorkingEnabled() ? boilerType.fireboxActiveRenderer : boilerType.fireboxIdleRenderer;
        }
        return boilerType.casingRenderer;
    }

    @Override
    public boolean shouldRenderOverlay(IMultiblockPart sourcePart) {
        return sourcePart == null || !isFireboxPart(sourcePart);
    }

    @Override
    public boolean hasMufflerMechanics() {
        return true;
    }

    @Override
    public void onAttached(Object... data) {
        super.onAttached(data);
        if (getWorld() != null && getWorld().isRemote) {
            this.setupSound(GTSounds.BOILER, this.getPos());
        }
    }

    @Override
    protected void updateFormedValid() {
        if (isMufflerFaceFree()) {
            this.recipeLogic.update();
        }
//        for (IFluidTank fluidTank : fluidImportInventory.getFluidTanks()) {
//            FluidStack fuelStack = fluidTank.drain(Integer.MAX_VALUE, false);
//            if (fuelStack == null || ModHandler.isWater(fuelStack))
//                continue;
//            FuelRecipe dieselRecipe = RecipeMaps.COMBUSTION_GENERATOR_FUELS.findRecipe(GTValues.V[9], fuelStack);
//            if (dieselRecipe != null) {
//                long recipeVoltage = FuelRecipeLogic.getTieredVoltage(dieselRecipe.getMinVoltage());
//                int voltageMultiplier = (int) Math.max(1L, recipeVoltage / GTValues.V[GTValues.LV]);
//                int burnTime = (int) Math.ceil(dieselRecipe.getDuration() * CONSUMPTION_MULTIPLIER / 2.0 * voltageMultiplier * getThrottleMultiplier());
//                int fuelAmountToConsume = (int) Math.ceil(dieselRecipe.getRecipeFluid().amount * CONSUMPTION_MULTIPLIER * boilerType.fuelConsumptionMultiplier * getThrottleMultiplier());
//                final long fuelBurnTime = (fuelStack.amount * burnTime) / fuelAmountToConsume;
//                FluidFuelInfo fluidFuelInfo = (FluidFuelInfo) fuels.get(fuelStack.getUnlocalizedName());
//                if (fluidFuelInfo == null) {
//                    fluidFuelInfo = new FluidFuelInfo(fuelStack, fuelStack.amount, fluidCapacity, fuelAmountToConsume, fuelBurnTime);
//                    fuels.put(fuelStack.getUnlocalizedName(), fluidFuelInfo);
//                } else {
//                    fluidFuelInfo.addFuelRemaining(fuelStack.amount);
//                    fluidFuelInfo.addFuelBurnTime(fuelBurnTime);
//                }
//            }
//            FuelRecipe denseFuelRecipe = RecipeMaps.SEMI_FLUID_GENERATOR_FUELS.findRecipe(GTValues.V[9], fuelStack);
//            if (denseFuelRecipe != null) {
//                long recipeVoltage = FuelRecipeLogic.getTieredVoltage(denseFuelRecipe.getMinVoltage());
//                int voltageMultiplier = (int) Math.max(1L, recipeVoltage / GTValues.V[GTValues.LV]);
//                int burnTime = (int) Math.ceil(denseFuelRecipe.getDuration() * CONSUMPTION_MULTIPLIER * 2 * voltageMultiplier * getThrottleMultiplier());
//                int fuelAmountToConsume = (int) Math.ceil(denseFuelRecipe.getRecipeFluid().amount * CONSUMPTION_MULTIPLIER * boilerType.fuelConsumptionMultiplier * getThrottleMultiplier());
//                final long fuelBurnTime = (fuelStack.amount * burnTime) / fuelAmountToConsume;
//                FluidFuelInfo fluidFuelInfo = (FluidFuelInfo) fuels.get(fuelStack.getUnlocalizedName());
//                if (fluidFuelInfo == null) {
//                    fluidFuelInfo = new FluidFuelInfo(fuelStack, fuelStack.amount, fluidCapacity, fuelAmountToConsume, fuelBurnTime);
//                    fuels.put(fuelStack.getUnlocalizedName(), fluidFuelInfo);
//                } else {
//                    fluidFuelInfo.addFuelRemaining(fuelStack.amount);
//                    fluidFuelInfo.addFuelBurnTime(fuelBurnTime);
//                }
//            }
//        }
        int itemCapacity = 0; // item capacity is all slots
        for (int slotIndex = 0; slotIndex < itemImportInventory.getSlots(); slotIndex++) {
            itemCapacity += itemImportInventory.getSlotLimit(slotIndex);
        }
        for (int slotIndex = 0; slotIndex < itemImportInventory.getSlots(); slotIndex++) {
            ItemStack itemStack = itemImportInventory.getStackInSlot(slotIndex);
            final long burnTime = (int) Math.ceil(TileEntityFurnace.getItemBurnTime(itemStack) / (50.0 * this.boilerType.fuelConsumptionMultiplier * getThrottleMultiplier()));
            if (burnTime > 0) {
                ItemFuelInfo itemFuelInfo = (ItemFuelInfo) fuels.get(itemStack.getTranslationKey());
                if (itemFuelInfo == null) {
                    itemFuelInfo = new ItemFuelInfo(itemStack, itemStack.getCount(), itemCapacity, 1, itemStack.getCount() * burnTime);
                    fuels.put(itemStack.getTranslationKey(), itemFuelInfo);
                } else {
                    itemFuelInfo.addFuelRemaining(itemStack.getCount());
                    itemFuelInfo.addFuelBurnTime(itemStack.getCount() * burnTime);
                }
            } else {
                this.hasNoWater = true;
            }
        } else {
            this.hasNoWater = false;
        }

        if (fuelBurnTicksLeft == 0) {
            double heatEfficiency = getHeatEfficiencyMultiplier();
            int fuelMaxBurnTime = (int) Math.round(setupRecipeAndConsumeInputs() * heatEfficiency);
            if (fuelMaxBurnTime > 0) {
                this.fuelBurnTicksLeft = fuelMaxBurnTime;
                if (wasActiveAndNeedsUpdate) {
                    this.wasActiveAndNeedsUpdate = false;
                } else setActive(true);
                markDirty();
            }
        }
        }*/
    }

    public boolean canCreateSound() {
        return isActive();
    }

    public int getThrottle() {
        return throttlePercentage;
    }

    public void setThrottle() {

    }

    @Override
    public IItemHandlerModifiable getImportItems() {
        return itemImportInventory;
    }

    @Override
    public FluidTankList getImportFluids() {
        return fluidImportInventory;
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        return super.createExportItemHandler();
    }

    @Override
    public FluidTankList getExportFluids() {
        return steamOutputTank;
    }

    @Override
    protected boolean shouldUpdate(MTETrait trait) {
        return !(trait instanceof BoilerRecipeLogic);
    }
}

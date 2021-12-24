package gregtech.common.metatileentities.multi;

import codechicken.lib.raytracer.CuboidRayTraceResult;
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
import gregtech.api.gui.Widget.ClickData;
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
import gregtech.common.blocks.BlockFireboxCasing;
import gregtech.common.blocks.BlockFireboxCasing.FireboxCasingType;
import gregtech.common.blocks.BlockMetalCasing.MetalCasingType;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.tools.DamageValues;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import static gregtech.api.capability.GregtechDataCodes.IS_WORKING;
import static gregtech.api.gui.widgets.AdvancedTextWidget.withButton;
import static gregtech.api.gui.widgets.AdvancedTextWidget.withHoverTextTranslate;

public class MetaTileEntityLargeBoiler extends MultiblockWithDisplayBase implements IFuelable, ISoundCreator {

    private static final int CONSUMPTION_MULTIPLIER = 100;
    private static final int BOILING_TEMPERATURE = 100;

    public enum BoilerType {
        BRONZE(900, 1.0f, 28, 500,
                MetaBlocks.METAL_CASING.getState(MetalCasingType.BRONZE_BRICKS),
                MetaBlocks.BOILER_FIREBOX_CASING.getState(FireboxCasingType.BRONZE_FIREBOX),
                MetaBlocks.BOILER_CASING.getState(BoilerCasingType.BRONZE_PIPE),
                Textures.BRONZE_PLATED_BRICKS,
                Textures.BRONZE_FIREBOX, Textures.BRONZE_FIREBOX_ACTIVE, Textures.LARGE_BRONZE_BOILER),

        STEEL(1600, 1.6f, 30, 800,
                MetaBlocks.METAL_CASING.getState(MetalCasingType.STEEL_SOLID),
                MetaBlocks.BOILER_FIREBOX_CASING.getState(FireboxCasingType.STEEL_FIREBOX),
                MetaBlocks.BOILER_CASING.getState(BoilerCasingType.STEEL_PIPE),
                Textures.SOLID_STEEL_CASING,
                Textures.STEEL_FIREBOX, Textures.STEEL_FIREBOX_ACTIVE, Textures.LARGE_STEEL_BOILER),

        TITANIUM(3700, 3.0f, 31, 2000,
                MetaBlocks.METAL_CASING.getState(MetalCasingType.TITANIUM_STABLE),
                MetaBlocks.BOILER_FIREBOX_CASING.getState(FireboxCasingType.TITANIUM_FIREBOX),
                MetaBlocks.BOILER_CASING.getState(BoilerCasingType.TITANIUM_PIPE),
                Textures.STABLE_TITANIUM_CASING,
                Textures.TITANIUM_FIREBOX, Textures.TITANIUM_FIREBOX_ACTIVE, Textures.LARGE_TITANIUM_BOILER),

        TUNGSTENSTEEL(7800, 5.4f, 32, 4000,
                MetaBlocks.METAL_CASING.getState(MetalCasingType.TUNGSTENSTEEL_ROBUST),
                MetaBlocks.BOILER_FIREBOX_CASING.getState(FireboxCasingType.TUNGSTENSTEEL_FIREBOX),
                MetaBlocks.BOILER_CASING.getState(BoilerCasingType.TUNGSTENSTEEL_PIPE),
                Textures.ROBUST_TUNGSTENSTEEL_CASING,
                Textures.TUNGSTENSTEEL_FIREBOX, Textures.TUNGSTENSTEEL_FIREBOX_ACTIVE, Textures.LARGE_TUNGSTENSTEEL_BOILER);

        public final int baseSteamOutput;
        public final float fuelConsumptionMultiplier;
        public final int temperatureEffBuff;
        public final int maxTemperature;
        public final IBlockState casingState;
        public final IBlockState fireboxState;
        public final IBlockState pipeState;
        public final ICubeRenderer solidCasingRenderer;
        public final ICubeRenderer fireboxIdleRenderer;
        public final ICubeRenderer firefoxActiveRenderer;
        public final ICubeRenderer frontOverlay;

        BoilerType(int baseSteamOutput, float fuelConsumptionMultiplier, int temperatureEffBuff, int maxTemperature, IBlockState casingState, IBlockState fireboxState, IBlockState pipeState, ICubeRenderer solidCasingRenderer, ICubeRenderer fireboxIdleRenderer, ICubeRenderer firefoxActiveRenderer, ICubeRenderer frontOverlay) {
            this.baseSteamOutput = baseSteamOutput;
            this.fuelConsumptionMultiplier = fuelConsumptionMultiplier;
            this.temperatureEffBuff = temperatureEffBuff;
            this.maxTemperature = maxTemperature;
            this.casingState = casingState;
            this.fireboxState = fireboxState;
            this.pipeState = pipeState;
            this.solidCasingRenderer = solidCasingRenderer;
            this.fireboxIdleRenderer = fireboxIdleRenderer;
            this.firefoxActiveRenderer = firefoxActiveRenderer;
            this.frontOverlay = frontOverlay;
        }
    }

    public final BoilerType boilerType;

    private int currentTemperature;
    private int fuelBurnTicksLeft;
    private int throttlePercentage = 100;
    private boolean isActive;
    private boolean wasActiveAndNeedsUpdate;
    private boolean hasNoWater;
    private int lastTickSteamOutput;

    private FluidTankList fluidImportInventory;
    private ItemHandlerList itemImportInventory;
    private FluidTankList steamOutputTank;

    public MetaTileEntityLargeBoiler(ResourceLocation metaTileEntityId, BoilerType boilerType) {
        super(metaTileEntityId);
        this.boilerType = boilerType;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityLargeBoiler(metaTileEntityId, boilerType);
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        this.fluidImportInventory = new FluidTankList(true, getAbilities(MultiblockAbility.IMPORT_FLUIDS));
        this.itemImportInventory = new ItemHandlerList(getAbilities(MultiblockAbility.IMPORT_ITEMS));
        this.steamOutputTank = new FluidTankList(true, getAbilities(MultiblockAbility.EXPORT_FLUIDS));
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.fluidImportInventory = new FluidTankList(true);
        this.itemImportInventory = new ItemHandlerList(Collections.emptyList());
        this.steamOutputTank = new FluidTankList(true);
        this.currentTemperature = 0; //reset temperature
        this.fuelBurnTicksLeft = 0;
        this.hasNoWater = false;
        this.throttlePercentage = 100;
        setActive(false);
        replaceFireboxAsActive(false);
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
        if (isStructureFormed()) {
            textList.add(new TextComponentTranslation("gregtech.multiblock.large_boiler.temperature", currentTemperature, boilerType.maxTemperature));
            textList.add(new TextComponentTranslation("gregtech.multiblock.large_boiler.steam_output", lastTickSteamOutput, boilerType.baseSteamOutput));

            ITextComponent heatEffText = new TextComponentTranslation("gregtech.multiblock.large_boiler.heat_efficiency", (int) (getHeatEfficiencyMultiplier() * 100));
            withHoverTextTranslate(heatEffText, "gregtech.multiblock.large_boiler.heat_efficiency.tooltip");
            textList.add(heatEffText);

            ITextComponent throttleText = new TextComponentTranslation("gregtech.multiblock.large_boiler.throttle", throttlePercentage, (int) (getThrottleEfficiency() * 100));
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

    private double getHeatEfficiencyMultiplier() {
        double temperature = currentTemperature / (boilerType.maxTemperature * 1.0);
        return 1.0 + Math.round(boilerType.temperatureEffBuff * temperature) / 100.0;
    }

    @Override
    protected void updateFormedValid() {
        if (fuelBurnTicksLeft > 0 && currentTemperature < boilerType.maxTemperature) {
            --this.fuelBurnTicksLeft;
            if (getOffsetTimer() % 20 == 0) {
                this.currentTemperature++;
            }
            if (fuelBurnTicksLeft == 0) {
                this.wasActiveAndNeedsUpdate = true;
            }
        } else if (currentTemperature > 0 && getOffsetTimer() % 20 == 0) {
            --this.currentTemperature;
        }

        this.lastTickSteamOutput = 0;
        if (currentTemperature >= BOILING_TEMPERATURE) {
            boolean doWaterDrain = getOffsetTimer() % 20 == 0;
            FluidStack drainedWater = ModHandler.getWaterFromContainer(fluidImportInventory, doWaterDrain);
            if (drainedWater != null && drainedWater.amount > 0) {
                if (currentTemperature > BOILING_TEMPERATURE && hasNoWater) {
                    float explosionPower = currentTemperature / (float) BOILING_TEMPERATURE * 2.0f;
                    getWorld().setBlockToAir(getPos());
                    getWorld().createExplosion(null, getPos().getX() + 0.5, getPos().getY() + 0.5, getPos().getZ() + 0.5,
                            explosionPower, true);
                }
                this.hasNoWater = false;
                if (currentTemperature >= BOILING_TEMPERATURE) {
                    double outputMultiplier = currentTemperature / (boilerType.maxTemperature * 1.0) * getThrottleMultiplier() * getThrottleEfficiency();
                    int steamOutput = (int) (boilerType.baseSteamOutput * outputMultiplier);
                    FluidStack steamStack = ModHandler.getSteam(steamOutput);
                    steamOutputTank.fill(steamStack, true);
                    this.lastTickSteamOutput = steamOutput;
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

        if (wasActiveAndNeedsUpdate) {
            this.wasActiveAndNeedsUpdate = false;
            setActive(false);
        }
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

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setInteger("CurrentTemperature", currentTemperature);
        data.setInteger("FuelBurnTicksLeft", fuelBurnTicksLeft);
        data.setBoolean("HasNoWater", hasNoWater);
        data.setInteger("ThrottlePercentage", throttlePercentage);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.currentTemperature = data.getInteger("CurrentTemperature");
        this.fuelBurnTicksLeft = data.getInteger("FuelBurnTicksLeft");
        this.hasNoWater = data.getBoolean("HasNoWater");
        if (data.hasKey("ThrottlePercentage")) {
            this.throttlePercentage = data.getInteger("ThrottlePercentage");
        }
        this.isActive = fuelBurnTicksLeft > 0;
    }

    private void setActive(boolean active) {
        this.isActive = active;
        if (!getWorld().isRemote) {
            if (isStructureFormed()) {
                replaceFireboxAsActive(active);
            }
            writeCustomData(IS_WORKING, buf -> buf.writeBoolean(isActive));
            markDirty();
        }
    }

    @Override
    public boolean isActive() {
        return super.isActive() && isActive;
    }

    private double getThrottleMultiplier() {
        return throttlePercentage / 100.0;
    }

    private double getThrottleEfficiency() {
        return MathHelper.clamp(1.0 + 0.3 * Math.log(getThrottleMultiplier()), 0.4, 1.0);
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
        return sourcePart == null ? 0 : (isActive ? 15 : 0);
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(isActive);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.isActive = buf.readBoolean();
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == IS_WORKING) {
            this.isActive = buf.readBoolean();
        }
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
                        .or(abilities(MultiblockAbility.IMPORT_ITEMS).setMinGlobalLimited(1)))
                .where('C', states(boilerType.casingState).setMinGlobalLimited(20)
                        .or(abilities(MultiblockAbility.EXPORT_FLUIDS).setMinGlobalLimited(1))
                        .or(autoAbilities())) // maintainer
                .build();
    }

    @Override
    public String[] getDescription() {
        return new String[]{I18n.format("gregtech.multiblock.large_boiler.description")};
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        this.getFrontOverlay().renderOrientedState(renderState, translation, pipeline, getFrontFacing(), isActive, true);
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
            return isActive ? boilerType.firefoxActiveRenderer : boilerType.fireboxIdleRenderer;
        }
        return boilerType.solidCasingRenderer;
    }

    @Override
    public boolean shouldRenderOverlay(IMultiblockPart sourcePart) {
        return sourcePart == null || !isFireboxPart(sourcePart);
    }

    @Override
    public boolean onRightClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
        ItemStack itemStack = playerIn.getHeldItem(hand);
        if (!itemStack.isEmpty() && itemStack.hasCapability(GregtechCapabilities.CAPABILITY_MALLET, null)) {
            ISoftHammerItem softHammerItem = itemStack.getCapability(GregtechCapabilities.CAPABILITY_MALLET, null);

            if (getWorld().isRemote) {
                return true;
            }
            if (!softHammerItem.damageItem(DamageValues.DAMAGE_FOR_SOFT_HAMMER, false)) {
                return false;
            }
        }
        return super.onRightClick(playerIn, hand, facing, hitResult);
    }

    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        T result = super.getCapability(capability, side);
        if (result != null)
            return result;
        if (capability == GregtechCapabilities.CAPABILITY_FUELABLE) {
            return GregtechCapabilities.CAPABILITY_FUELABLE.cast(this);
        }
        return null;
    }

    @Override
    public Collection<IFuelInfo> getFuels() {
        if (!isStructureFormed())
            return Collections.emptySet();
        final LinkedHashMap<Object, IFuelInfo> fuels = new LinkedHashMap<>();
        int fluidCapacity = 0; // fluid capacity is all non water tanks
        for (IFluidTank fluidTank : fluidImportInventory.getFluidTanks()) {
            FluidStack fuelStack = fluidTank.drain(Integer.MAX_VALUE, false);
            if (!ModHandler.isWater(fuelStack))
                fluidCapacity += fluidTank.getCapacity();
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
            }
        }
        return fuels.values();
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

    public boolean canCreateSound() {
        return isActive;
    }
}

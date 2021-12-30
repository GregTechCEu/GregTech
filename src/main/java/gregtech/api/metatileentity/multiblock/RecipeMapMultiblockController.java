package gregtech.api.metatileentity.multiblock;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.google.common.collect.Lists;
import gregtech.api.GTValues;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.EnergyContainerList;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.AdvancedTextWidget;
import gregtech.api.metatileentity.sound.ISoundCreator;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.util.GTUtility;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.BlockFireboxCasing;
import gregtech.common.blocks.VariantActiveBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

public abstract class RecipeMapMultiblockController extends MultiblockWithDisplayBase implements ISoundCreator, IMultipleRecipeMaps {

    public final RecipeMap<?> recipeMap;
    protected MultiblockRecipeLogic recipeMapWorkable;
    protected List<BlockPos> variantActiveBlocks;
    protected boolean lastActive;

    protected IItemHandlerModifiable inputInventory;
    protected IItemHandlerModifiable outputInventory;
    protected IMultipleTankHandler inputFluidInventory;
    protected IMultipleTankHandler outputFluidInventory;
    protected IEnergyContainer energyContainer;

    private boolean isDistinct = false;

    // array of possible recipes, specific to each multi - used when the multi has multiple RecipeMaps
    protected RecipeMap<?>[] recipeMaps;

    // index of the current selected recipe - used when the multi has multiple RecipeMaps
    private int recipeMapIndex;

    public RecipeMapMultiblockController(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap) {
        super(metaTileEntityId);
        this.recipeMap = recipeMap;
        this.recipeMapWorkable = new MultiblockRecipeLogic(this);
        this.recipeMapIndex = 0;
        this.recipeMaps = null;
        resetTileAbilities();
    }

    public IEnergyContainer getEnergyContainer() {
        return energyContainer;
    }

    public IItemHandlerModifiable getInputInventory() {
        return inputInventory;
    }

    public IItemHandlerModifiable getOutputInventory() {
        return outputInventory;
    }

    public IMultipleTankHandler getInputFluidInventory() {
        return inputFluidInventory;
    }

    public IMultipleTankHandler getOutputFluidInventory() {
        return outputFluidInventory;
    }

    public MultiblockRecipeLogic getRecipeMapWorkable() {
        return recipeMapWorkable;
    }

    /**
     * Performs extra checks for validity of given recipe before multiblock
     * will start it's processing.
     */
    public boolean checkRecipe(Recipe recipe, boolean consumeIfSuccess) {
        return true;
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        initializeAbilities();
        variantActiveBlocks = context.getOrDefault("VABlock", new LinkedList<>());
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        resetTileAbilities();
        this.recipeMapWorkable.invalidate();
        this.replaceVariantBlocksActive(false);
        variantActiveBlocks.clear();
        lastActive = false;
    }

    protected void replaceVariantBlocksActive(boolean isActive) {
        if (variantActiveBlocks != null) {
            for (BlockPos blockPos : variantActiveBlocks) {
                IBlockState blockState = getWorld().getBlockState(blockPos);
                if (blockState.getBlock() instanceof VariantActiveBlock) {
                    getWorld().setBlockState(blockPos, blockState.withProperty(BlockFireboxCasing.ACTIVE, isActive));
                }
            }
        }
    }

    @Override
    public void onRemoval() {
        super.onRemoval();
        if (!getWorld().isRemote && isStructureFormed()) {
            replaceVariantBlocksActive(false);
            variantActiveBlocks.clear();
            lastActive = false;
        }
    }

    @Override
    protected void updateFormedValid() {
        if (!hasMufflerMechanics() || isMufflerFaceFree())
            this.recipeMapWorkable.updateWorkable();
        boolean state = this.recipeMapWorkable.isWorking() && ConfigHolder.client.casingsActiveEmissiveTextures;
        if (lastActive != state) {
            lastActive = state;
            replaceVariantBlocksActive(lastActive);
        }
    }

    @Override
    public boolean isActive() {
        return isStructureFormed() && recipeMapWorkable.isActive() && recipeMapWorkable.isWorkingEnabled();
    }

    protected void initializeAbilities() {
        this.inputInventory = new ItemHandlerList(getAbilities(MultiblockAbility.IMPORT_ITEMS));
        this.inputFluidInventory = new FluidTankList(allowSameFluidFillForOutputs(), getAbilities(MultiblockAbility.IMPORT_FLUIDS));
        this.outputInventory = new ItemHandlerList(getAbilities(MultiblockAbility.EXPORT_ITEMS));
        this.outputFluidInventory = new FluidTankList(allowSameFluidFillForOutputs(), getAbilities(MultiblockAbility.EXPORT_FLUIDS));
        this.energyContainer = new EnergyContainerList(getAbilities(MultiblockAbility.INPUT_ENERGY));
    }

    private void resetTileAbilities() {
        this.inputInventory = new ItemStackHandler(0);
        this.inputFluidInventory = new FluidTankList(true);
        this.outputInventory = new ItemStackHandler(0);
        this.outputFluidInventory = new FluidTankList(true);
        this.energyContainer = new EnergyContainerList(Lists.newArrayList());
    }

    protected boolean allowSameFluidFillForOutputs() {
        return true;
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        super.addDisplayText(textList);
        if (isStructureFormed()) {
            IEnergyContainer energyContainer = recipeMapWorkable.getEnergyContainer();
            if (energyContainer != null && energyContainer.getEnergyCapacity() > 0) {
                long maxVoltage = Math.max(energyContainer.getInputVoltage(), energyContainer.getOutputVoltage());
                String voltageName = GTValues.VNF[GTUtility.getTierByVoltage(maxVoltage)];
                textList.add(new TextComponentTranslation("gregtech.multiblock.max_energy_per_tick", maxVoltage, voltageName));
            }

            if (canBeDistinct()) {
                ITextComponent buttonText = new TextComponentTranslation("gregtech.multiblock.universal.distinct");
                buttonText.appendText(" ");
                ITextComponent button = AdvancedTextWidget.withButton(isDistinct() ?
                        new TextComponentTranslation("gregtech.multiblock.universal.distinct.yes").setStyle(new Style().setColor(TextFormatting.GREEN)) :
                        new TextComponentTranslation("gregtech.multiblock.universal.distinct.no").setStyle(new Style().setColor(TextFormatting.RED)), "distinct");
                AdvancedTextWidget.withHoverTextTranslate(button, "gregtech.multiblock.universal.distinct.info");
                buttonText.appendSibling(button);
                textList.add(buttonText);
            }

            if (hasMultipleRecipeMaps()) {
                textList.add(new TextComponentTranslation("gregtech.multiblock.multiple_recipemaps.header")
                        .setStyle(new Style().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                new TextComponentTranslation("gregtech.multiblock.multiple_recipemaps.tooltip")))));

                textList.add(new TextComponentTranslation("recipemap." + this.recipeMaps[this.recipeMapIndex].getUnlocalizedName() + ".name")
                        .setStyle(new Style().setColor(TextFormatting.AQUA)
                                .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                new TextComponentTranslation("gregtech.multiblock.multiple_recipemaps.tooltip")))));
            }

            if (!recipeMapWorkable.isWorkingEnabled()) {
                textList.add(new TextComponentTranslation("gregtech.multiblock.work_paused"));

            } else if (recipeMapWorkable.isActive()) {
                textList.add(new TextComponentTranslation("gregtech.multiblock.running"));
                int currentProgress = (int) (recipeMapWorkable.getProgressPercent() * 100);
                if (this.recipeMapWorkable.getParallelLimit() != 1) {
                    textList.add(new TextComponentTranslation("gregtech.multiblock.parallel", this.recipeMapWorkable.getParallelLimit()));
                }
                textList.add(new TextComponentTranslation("gregtech.multiblock.progress", currentProgress));
            } else {
                textList.add(new TextComponentTranslation("gregtech.multiblock.idling"));
            }

            if (recipeMapWorkable.isHasNotEnoughEnergy()) {
                textList.add(new TextComponentTranslation("gregtech.multiblock.not_enough_energy").setStyle(new Style().setColor(TextFormatting.RED)));
            }
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        if (this.hasMultipleRecipeMaps())
            tooltip.add(I18n.format("gregtech.multiblock.multiple_recipemaps_recipes.tooltip", this.recipeMapsToString()));
    }

    @Override
    protected void handleDisplayClick(String componentData, Widget.ClickData clickData) {
        super.handleDisplayClick(componentData, clickData);
        toggleDistinct();
    }

    @Override
    public TraceabilityPredicate autoAbilities() {
        return autoAbilities(true, true, true, true, true, true, true);
    }

    public TraceabilityPredicate autoAbilities(boolean checkEnergyIn,
                                               boolean checkMaintainer,
                                               boolean checkItemIn,
                                               boolean checkItemOut,
                                               boolean checkFluidIn,
                                               boolean checkFluidOut,
                                               boolean checkMuffler) {
        TraceabilityPredicate predicate = super.autoAbilities(checkMaintainer, checkMuffler)
                .or(checkEnergyIn ? abilities(MultiblockAbility.INPUT_ENERGY).setMinGlobalLimited(1).setMaxGlobalLimited(3).setPreviewCount(1) : new TraceabilityPredicate());

        boolean checkedItemsIn = false;
        boolean checkedItemsOut = false;
        boolean checkedFluidsIn = false;
        boolean checkedFluidsOut = false;

        for (RecipeMap<?> recipeMap : getAvailableRecipeMaps()) {
            if (!checkedItemsIn && checkItemIn) {
                if (recipeMap.getMinInputs() > 0) {
                    checkedItemsIn = true;
                    predicate = predicate.or(abilities(MultiblockAbility.IMPORT_ITEMS).setMinGlobalLimited(1).setPreviewCount(1));
                } else if (recipeMap.getMaxInputs() > 0) {
                    checkedItemsIn = true;
                    predicate = predicate.or(abilities(MultiblockAbility.IMPORT_ITEMS).setPreviewCount(1));
                }
            }
            if (!checkedItemsOut && checkItemOut) {
                if (recipeMap.getMinOutputs() > 0) {
                    checkedItemsOut = true;
                    predicate = predicate.or(abilities(MultiblockAbility.EXPORT_ITEMS).setMinGlobalLimited(1).setPreviewCount(1));
                } else if (recipeMap.getMaxOutputs() > 0) {
                    checkedItemsOut = true;
                    predicate = predicate.or(abilities(MultiblockAbility.EXPORT_ITEMS).setPreviewCount(1));
                }
            }
            if (!checkedFluidsIn && checkFluidIn) {
                if (recipeMap.getMinFluidInputs() > 0) {
                    checkedFluidsIn = true;
                    predicate = predicate.or(abilities(MultiblockAbility.IMPORT_FLUIDS).setMinGlobalLimited(1).setPreviewCount(recipeMap.getMinFluidInputs()));
                } else if (recipeMap.getMaxFluidInputs() > 0) {
                    checkedFluidsIn = true;
                    predicate = predicate.or(abilities(MultiblockAbility.IMPORT_FLUIDS).setPreviewCount(1));
                }
            }
            if (!checkedFluidsOut && checkFluidOut) {
                if (recipeMap.getMinFluidOutputs() > 0) {
                    checkedFluidsOut = true;
                    predicate = predicate.or(abilities(MultiblockAbility.EXPORT_FLUIDS).setMinGlobalLimited(1).setPreviewCount(recipeMap.getMinFluidOutputs()));
                } else if (recipeMap.getMaxFluidOutputs() > 0) {
                    checkedFluidsOut = true;
                    predicate = predicate.or(abilities(MultiblockAbility.EXPORT_FLUIDS).setPreviewCount(1));
                }
            }
        }
        return predicate;
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        this.getFrontOverlay().renderOrientedState(renderState, translation, pipeline, getFrontFacing(), recipeMapWorkable.isActive(), recipeMapWorkable.isWorkingEnabled());
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean("isDistinct", isDistinct);
        data.setTag("RecipeMapIndex", new NBTTagInt(recipeMapIndex));
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        isDistinct = data.getBoolean("isDistinct");
        recipeMapIndex = data.getInteger("RecipeMapIndex");
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(isDistinct);
        buf.writeByte(recipeMapIndex);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        isDistinct = buf.readBoolean();
        recipeMapIndex = buf.readByte();
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == GregtechDataCodes.RECIPE_MAP_INDEX) {
            recipeMapIndex = buf.readInt();
            scheduleRenderUpdate();
        }
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        T capabilityResult = super.getCapability(capability, side);
        if (capabilityResult == null && capability == GregtechTileCapabilities.MULTIPLE_RECIPEMAPS) {
            return (T) this;
        }
        return capabilityResult;
    }

    public boolean canBeDistinct() {
        return false;
    }

    public boolean isDistinct() {
        return isDistinct;
    }

    protected void toggleDistinct() {
        isDistinct = !isDistinct;
        recipeMapWorkable.onDistinctChanged();
        //mark buses as changed on distinct toggle
        if (isDistinct) {
            this.notifiedItemInputList.addAll(this.getAbilities(MultiblockAbility.IMPORT_ITEMS));
        } else {
            this.notifiedItemInputList.add(this.inputInventory);
        }
    }


    @Override
    public void onAttached(Object... data) {
        super.onAttached(data);
        if (getWorld() != null && getWorld().isRemote) {
            this.setupSound(recipeMap.getSound(), this.getPos());
        }
    }

    public boolean canCreateSound() {
        return recipeMapWorkable.isActive();
    }


    @Override
    public RecipeMap<?>[] getAvailableRecipeMaps() {
        return hasMultipleRecipeMaps() ? this.recipeMaps : new RecipeMap<?>[]{this.recipeMap};
    }

    @Override
    public int getRecipeMapIndex() {
        return recipeMapIndex;
    }

    @Override
    public void addRecipeMaps(RecipeMap<?>... recipeMaps) {
        if (hasMultipleRecipeMaps())
            this.recipeMaps = Stream.concat(Arrays.stream(this.recipeMaps), Arrays.stream(recipeMaps)).toArray(RecipeMap<?>[]::new);
    }

    @Override
    public void setRecipeMapIndex(int index) {
        this.recipeMapIndex = index;
        if (!getWorld().isRemote) {
            writeCustomData(GregtechDataCodes.RECIPE_MAP_INDEX, buf -> buf.writeInt(index));
            markDirty();
        }
    }

    @Override
    public RecipeMap<?> getCurrentRecipeMap() {
        return recipeMaps[recipeMapIndex];
    }

    @SideOnly(Side.CLIENT)
    public String recipeMapsToString() {
        StringBuilder recipeMapsString = new StringBuilder();
        for(int i = 0; i < recipeMaps.length; i++) {
            recipeMapsString.append(recipeMaps[i].getLocalizedName());
            if(recipeMaps.length - 1 != i)
                recipeMapsString.append(", "); // For delimiting
        }
        return recipeMapsString.toString();
    }

    @Override
    public boolean onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
        if (!getWorld().isRemote && this.hasMultipleRecipeMaps()) {
            if (!this.recipeMapWorkable.isActive()) {
                int index;
                if (playerIn.isSneaking()) // cycle recipemaps backwards
                    index = (recipeMapIndex - 1 < 0 ? recipeMaps.length - 1 : recipeMapIndex - 1) % recipeMaps.length;
                else // cycle recipemaps forwards
                    index = (recipeMapIndex + 1) % recipeMaps.length;

                setRecipeMapIndex(index);
                this.recipeMapWorkable.forceRecipeRecheck();
            } else {
                playerIn.sendMessage(new TextComponentTranslation("gregtech.multiblock.multiple_recipemaps.switch_message"));
            }
        }

        return true; // return true here on the client to keep the GUI closed
    }
}

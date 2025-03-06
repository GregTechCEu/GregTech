package gregtech.common.metatileentities.multi.electric;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.impl.EnergyContainerHandler;
import gregtech.api.capability.impl.EnergyContainerList;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.gui.widgets.ImageCycleButtonWidget;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.IndicatorImageWidget;
import gregtech.api.gui.widgets.ProgressWidget;
import gregtech.api.metatileentity.IFastRenderMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockDisplayText;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.MultiblockShapeInfo;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.logic.PrimitiveRecipeRun;
import gregtech.api.recipes.logic.RecipeLogicConstants;
import gregtech.api.recipes.logic.RecipeView;
import gregtech.api.recipes.logic.SingleRecipeRun;
import gregtech.api.recipes.logic.statemachine.RecipeMaintenanceOperator;
import gregtech.api.recipes.logic.statemachine.RecipeRunCheckOperator;
import gregtech.api.recipes.logic.statemachine.builder.RecipeStandardStateMachineBuilder;
import gregtech.api.recipes.logic.statemachine.overclock.RecipeStandardOverclockingOperator;
import gregtech.api.recipes.logic.statemachine.running.RecipeCleanupSaveOperation;
import gregtech.api.recipes.logic.statemachine.running.RecipeFinalizingOperator;
import gregtech.api.recipes.lookup.property.EUToStartProperty;
import gregtech.api.recipes.lookup.property.PowerCapacityProperty;
import gregtech.api.recipes.lookup.property.PowerSupplyProperty;
import gregtech.api.recipes.lookup.property.PropertySet;
import gregtech.api.recipes.properties.impl.FusionEUToStartProperty;
import gregtech.api.util.GTUtility;
import gregtech.api.util.RelativeDirection;
import gregtech.api.util.TextComponentUtil;
import gregtech.api.util.TextFormattingUtil;
import gregtech.api.util.interpolate.Eases;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.IRenderSetup;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.shader.postprocessing.BloomEffect;
import gregtech.client.shader.postprocessing.BloomType;
import gregtech.client.utils.BloomEffectUtil;
import gregtech.client.utils.EffectRenderContext;
import gregtech.client.utils.IBloomEffect;
import gregtech.client.utils.RenderBufferHelper;
import gregtech.client.utils.RenderUtil;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.BlockFusionCasing;
import gregtech.common.blocks.BlockGlassCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AtomicDouble;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.DoubleSupplier;

import static gregtech.api.util.RelativeDirection.*;

public class MetaTileEntityFusionReactor extends RecipeMapMultiblockController
                                         implements IFastRenderMetaTileEntity, IBloomEffect {

    protected static final int NO_COLOR = 0;

    private final int tier;
    private EnergyContainerList inputEnergyContainers;
    private long heat = 0;
    private int fusionRingColor = NO_COLOR;
    private final FusionProgressSupplier progressBarSupplier;

    @SideOnly(Side.CLIENT)
    private boolean registeredBloomRenderTicket;

    public MetaTileEntityFusionReactor(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, RecipeMaps.FUSION_RECIPES);
        this.tier = tier;
        this.energyContainer = new EnergyContainerHandler(this, 0, 0, 0, 0, 0) {

            @NotNull
            @Override
            public String getName() {
                return GregtechDataCodes.FUSION_REACTOR_ENERGY_CONTAINER_TRAIT;
            }
        };
        this.progressBarSupplier = new FusionProgressSupplier();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityFusionReactor(metaTileEntityId, tier);
    }

    @Override
    protected void modifyRecipeLogicStandardBuilder(RecipeStandardStateMachineBuilder builder) {
        super.modifyRecipeLogicStandardBuilder(builder);
        builder.setFinalCheck(run -> {
            long heatDiff = run.getRecipeView().getRecipe().getProperty(FusionEUToStartProperty.getInstance(), 0L) -
                    heat;
            // if the stored heat is >= required energy, recipe is okay to run
            if (heatDiff <= 0)
                return RecipeRunCheckOperator.standardConsumptionCheck(run, getInputInventory(),
                        getInputFluidInventory());

            // if the remaining energy needed is more than stored, do not run
            if (energyContainer.getEnergyStored() < heatDiff)
                return false;

            // check consumptions
            if (!RecipeRunCheckOperator.standardConsumptionCheck(run, getInputInventory(), getInputFluidInventory()))
                return false;

            // remove the energy needed
            energyContainer.removeEnergy(heatDiff);
            // increase the stored heat
            heat += heatDiff;
            return true;
        }).setOverclockFactory(FusionOverclockOperator::new)
                .setCleanupOperator(RecipeCleanupSaveOperation.STANDARD_INSTANCE)
                .setOverclockSpeedFactor(RecipeLogicConstants.FUSION_OVERCLOCK_SPEED_FACTOR)
                .setOverclockCostFactor(RecipeLogicConstants.FUSION_OVERCLOCK_VOLTAGE_FACTOR);
    }

    @Override
    protected @NotNull PropertySet computePropertySet() {
        PropertySet set = super.computePropertySet();
        set.add(new EUToStartProperty(Math.min(energyContainer.getEnergyCapacity(),
                energyContainer.getEnergyStored() + heat)));
        return set;
    }

    @NotNull
    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("###############", "######OGO######", "###############")
                .aisle("######ICI######", "####GGAAAGG####", "######ICI######")
                .aisle("####CC###CC####", "###EAAOGOAAE###", "####CC###CC####")
                .aisle("###C#######C###", "##EKEG###GEKE##", "###C#######C###")
                .aisle("##C#########C##", "#GAE#######EAG#", "##C#########C##")
                .aisle("##C#########C##", "#GAG#######GAG#", "##C#########C##")
                .aisle("#I###########I#", "OAO#########OAO", "#I###########I#")
                .aisle("#C###########C#", "GAG#########GAG", "#C###########C#")
                .aisle("#I###########I#", "OAO#########OAO", "#I###########I#")
                .aisle("##C#########C##", "#GAG#######GAG#", "##C#########C##")
                .aisle("##C#########C##", "#GAE#######EAG#", "##C#########C##")
                .aisle("###C#######C###", "##EKEG###GEKE##", "###C#######C###")
                .aisle("####CC###CC####", "###EAAOGOAAE###", "####CC###CC####")
                .aisle("######ICI######", "####GGAAAGG####", "######ICI######")
                .aisle("###############", "######OSO######", "###############")
                .where('S', selfPredicate())
                .where('G', states(getCasingState(), getGlassState()))
                .where('E',
                        states(getCasingState(), getGlassState()).or(metaTileEntities(Arrays
                                .stream(MetaTileEntities.ENERGY_INPUT_HATCH)
                                .filter(mte -> mte != null && tier <= mte.getTier() && mte.getTier() <= GTValues.UV)
                                .toArray(MetaTileEntity[]::new))
                                        .setMinGlobalLimited(1).setPreviewCount(16)))
                .where('C', states(getCasingState()))
                .where('K', states(getCoilState()))
                .where('O', states(getCasingState(), getGlassState()).or(abilities(MultiblockAbility.EXPORT_FLUIDS)))
                .where('A', air())
                .where('I',
                        states(getCasingState()).or(abilities(MultiblockAbility.IMPORT_FLUIDS).setMinGlobalLimited(2)))
                .where('#', any())
                .build();
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        List<MultiblockShapeInfo> shapeInfos = new ArrayList<>();

        MultiblockShapeInfo.Builder baseBuilder = MultiblockShapeInfo.builder(RIGHT, DOWN, FRONT)
                .aisle("###############", "######WGW######", "###############")
                .aisle("######DCD######", "####GG###GG####", "######UCU######")
                .aisle("####CC###CC####", "###w##EGE##s###", "####CC###CC####")
                .aisle("###C#######C###", "##nKeG###GeKn##", "###C#######C###")
                .aisle("##C#########C##", "#G#s#######w#G#", "##C#########C##")
                .aisle("##C#########C##", "#G#G#######G#G#", "##C#########C##")
                .aisle("#D###########D#", "N#S#########N#S", "#U###########U#")
                .aisle("#C###########C#", "G#G#########G#G", "#C###########C#")
                .aisle("#D###########D#", "N#S#########N#S", "#U###########U#")
                .aisle("##C#########C##", "#G#G#######G#G#", "##C#########C##")
                .aisle("##C#########C##", "#G#s#######w#G#", "##C#########C##")
                .aisle("###C#######C###", "##eKnG###GnKe##", "###C#######C###")
                .aisle("####CC###CC####", "###w##WGW##s###", "####CC###CC####")
                .aisle("######DCD######", "####GG###GG####", "######UCU######")
                .aisle("###############", "######EME######", "###############")
                .where('M', MetaTileEntities.FUSION_REACTOR[tier - GTValues.LuV], EnumFacing.SOUTH)
                .where('C', getCasingState())
                .where('G', MetaBlocks.TRANSPARENT_CASING.getState(
                        BlockGlassCasing.CasingType.FUSION_GLASS))
                .where('K', getCoilState())
                .where('W', MetaTileEntities.FLUID_EXPORT_HATCH[tier], EnumFacing.NORTH)
                .where('E', MetaTileEntities.FLUID_EXPORT_HATCH[tier], EnumFacing.SOUTH)
                .where('S', MetaTileEntities.FLUID_EXPORT_HATCH[tier], EnumFacing.EAST)
                .where('N', MetaTileEntities.FLUID_EXPORT_HATCH[tier], EnumFacing.WEST)
                .where('w', MetaTileEntities.ENERGY_INPUT_HATCH[tier], EnumFacing.WEST)
                .where('e', MetaTileEntities.ENERGY_INPUT_HATCH[tier], EnumFacing.SOUTH)
                .where('s', MetaTileEntities.ENERGY_INPUT_HATCH[tier], EnumFacing.EAST)
                .where('n', MetaTileEntities.ENERGY_INPUT_HATCH[tier], EnumFacing.NORTH)
                .where('U', MetaTileEntities.FLUID_IMPORT_HATCH[tier], EnumFacing.UP)
                .where('D', MetaTileEntities.FLUID_IMPORT_HATCH[tier], EnumFacing.DOWN)
                .where('#', Blocks.AIR.getDefaultState());

        shapeInfos.add(baseBuilder.shallowCopy()
                .where('G', getCasingState())
                .build());
        shapeInfos.add(baseBuilder.build());
        return shapeInfos;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        if (isActive()) {
            return Textures.ACTIVE_FUSION_TEXTURE;
        } else {
            return Textures.FUSION_TEXTURE;
        }
    }

    private IBlockState getGlassState() {
        return MetaBlocks.TRANSPARENT_CASING.getState(BlockGlassCasing.CasingType.FUSION_GLASS);
    }

    private IBlockState getCasingState() {
        if (tier == GTValues.LuV)
            return MetaBlocks.FUSION_CASING.getState(BlockFusionCasing.CasingType.FUSION_CASING);
        if (tier == GTValues.ZPM)
            return MetaBlocks.FUSION_CASING.getState(BlockFusionCasing.CasingType.FUSION_CASING_MK2);

        return MetaBlocks.FUSION_CASING.getState(BlockFusionCasing.CasingType.FUSION_CASING_MK3);
    }

    private IBlockState getCoilState() {
        if (tier == GTValues.LuV)
            return MetaBlocks.FUSION_CASING.getState(BlockFusionCasing.CasingType.SUPERCONDUCTOR_COIL);

        return MetaBlocks.FUSION_CASING.getState(BlockFusionCasing.CasingType.FUSION_COIL);
    }

    protected int getFusionRingColor() {
        return this.fusionRingColor;
    }

    protected boolean hasFusionRingColor() {
        return this.fusionRingColor != NO_COLOR;
    }

    protected void setFusionRingColor(int fusionRingColor) {
        if (this.fusionRingColor != fusionRingColor) {
            this.fusionRingColor = fusionRingColor;
            writeCustomData(GregtechDataCodes.UPDATE_COLOR, buf -> buf.writeVarInt(fusionRingColor));
        }
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        long energyStored = this.energyContainer.getEnergyStored();
        super.formStructure(context);
        this.initializeAbilities();
        ((EnergyContainerHandler) this.energyContainer).setEnergyStored(energyStored);
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.energyContainer = new EnergyContainerHandler(this, 0, 0, 0, 0, 0) {

            @NotNull
            @Override
            public String getName() {
                return GregtechDataCodes.FUSION_REACTOR_ENERGY_CONTAINER_TRAIT;
            }
        };
        this.inputEnergyContainers = new EnergyContainerList(Lists.newArrayList());
        this.heat = 0;
        this.setFusionRingColor(NO_COLOR);
    }

    @Override
    protected void initializeAbilities() {
        this.inputInventory = new ItemHandlerList(getAbilities(MultiblockAbility.IMPORT_ITEMS));
        this.inputFluidInventory = new FluidTankList(true, getAbilities(MultiblockAbility.IMPORT_FLUIDS));
        this.outputInventory = new ItemHandlerList(getAbilities(MultiblockAbility.EXPORT_ITEMS));
        this.outputFluidInventory = new FluidTankList(true, getAbilities(MultiblockAbility.EXPORT_FLUIDS));
        List<IEnergyContainer> energyInputs = getAbilities(MultiblockAbility.INPUT_ENERGY);
        this.inputEnergyContainers = new EnergyContainerList(energyInputs);
        long euCapacity = calculateEnergyStorageFactor(energyInputs.size());
        this.energyContainer = new EnergyContainerHandler(this, euCapacity, GTValues.V[tier], 0, 0, 0) {

            @NotNull
            @Override
            public String getName() {
                return GregtechDataCodes.FUSION_REACTOR_ENERGY_CONTAINER_TRAIT;
            }
        };
    }

    private long calculateEnergyStorageFactor(int energyInputAmount) {
        return energyInputAmount * (long) Math.pow(2, tier - 6) * 10000000L;
    }

    @Override
    protected void updateFormedValid() {
        if (this.heat > 0) {
            if (!isActive() || insufficientEnergy()) {
                heat = heat <= 10000 ? 0 : (heat - 10000);
            }
        }
        if (this.inputEnergyContainers.getEnergyStored() > 0) {
            long energyAdded = this.energyContainer.addEnergy(this.inputEnergyContainers.getEnergyStored());
            if (energyAdded > 0) this.inputEnergyContainers.removeEnergy(energyAdded);
        }
        super.updateFormedValid();
        // TODO multiple recipe display
        // if (isRecipeSelected() && activeWorker.isLogicEnabled()) {
        // if (fusionRingColor == NO_COLOR) {
        // FluidStack out = getOutputFluidStack();
        // if (out != null) {
        // setFusionRingColor(0xFF000000 | out.getFluid().getColor());
        // }
        // }
        // } else if (isStructureFormed()) {
        // setFusionRingColor(NO_COLOR);
        // }
    }

    /**
     * See {@link RecipeFinalizingOperator} for encoding pattern.
     */
    protected @Nullable NBTTagCompound getMostUpToDateRecipe() {
        // TODO multiple recipe display
        // NBTTagCompound worker = activeWorker.logicData();
        // if (worker.hasKey(RecipeFinalizingOperator.STANDARD_RESULTS_KEY)) {
        // return worker.getCompoundTag(RecipeFinalizingOperator.STANDARD_RESULTS_KEY);
        // } else if (worker.hasKey(RecipeCleanupSaveOperation.STANDARD_PREVIOUS_RECIPE_KEY)) {
        // return worker.getCompoundTag(RecipeCleanupSaveOperation.STANDARD_PREVIOUS_RECIPE_KEY);
        // }
        return null;
    }

    protected @Nullable FluidStack getOutputFluidStack() {
        NBTTagCompound recipe = getMostUpToDateRecipe();
        if (recipe == null) return null;

        NBTTagList list = recipe.getTagList("FluidsOut", Constants.NBT.TAG_COMPOUND);
        if (list.isEmpty()) return null;

        return FluidStack.loadFluidStackFromNBT(list.getCompoundTagAt(0));
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeVarInt(this.fusionRingColor);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.fusionRingColor = buf.readVarInt();
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        if (dataId == GregtechDataCodes.UPDATE_COLOR) {
            this.fusionRingColor = buf.readVarInt();
        } else {
            boolean active = this.isActive();
            super.receiveCustomData(dataId, buf);
            if (active != this.isActive()) progressBarSupplier.resetCountdown();
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, @NotNull List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(
                I18n.format("gregtech.machine.fusion_reactor.capacity", calculateEnergyStorageFactor(16) / 1000000L));
        tooltip.add(I18n.format("gregtech.machine.fusion_reactor.overclocking"));
    }

    @SideOnly(Side.CLIENT)
    @NotNull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return Textures.FUSION_REACTOR_OVERLAY;
    }

    @Override
    public boolean hasMaintenanceMechanics() {
        return false;
    }

    public long getHeat() {
        return heat;
    }

    @Override
    protected ModularUI.Builder createUITemplate(EntityPlayer entityPlayer) {
        // Background
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 198, 236);

        // Display
        builder.image(4, 4, 190, 138, GuiTextures.DISPLAY);

        // Energy Bar
        builder.widget(new ProgressWidget(
                () -> energyContainer.getEnergyCapacity() > 0 ?
                        1.0 * energyContainer.getEnergyStored() / energyContainer.getEnergyCapacity() : 0,
                4, 144, 94, 7,
                GuiTextures.PROGRESS_BAR_FUSION_ENERGY, ProgressWidget.MoveType.HORIZONTAL)
                        .setHoverTextConsumer(this::addEnergyBarHoverText));

        // Heat Bar
        builder.widget(new ProgressWidget(
                () -> energyContainer.getEnergyCapacity() > 0 ? 1.0 * heat / energyContainer.getEnergyCapacity() : 0,
                100, 144, 94, 7,
                GuiTextures.PROGRESS_BAR_FUSION_HEAT, ProgressWidget.MoveType.HORIZONTAL)
                        .setHoverTextConsumer(this::addHeatBarHoverText));

        // Indicator Widget
        builder.widget(new IndicatorImageWidget(174, 122, 17, 17, getLogo())
                .setWarningStatus(getWarningLogo(), this::addWarningText)
                .setErrorStatus(getErrorLogo(), this::addErrorText));

        // Title
        if (tier == GTValues.LuV) {
            // MK1
            builder.widget(new ImageWidget(66, 9, 67, 12, GuiTextures.FUSION_REACTOR_MK1_TITLE).setIgnoreColor(true));
        } else if (tier == GTValues.ZPM) {
            // MK2
            builder.widget(new ImageWidget(65, 9, 69, 12, GuiTextures.FUSION_REACTOR_MK2_TITLE).setIgnoreColor(true));
        } else {
            // MK3
            builder.widget(new ImageWidget(64, 9, 71, 12, GuiTextures.FUSION_REACTOR_MK3_TITLE).setIgnoreColor(true));
        }

        // Fusion Diagram + Progress Bar
        builder.widget(new ImageWidget(55, 24, 89, 101, GuiTextures.FUSION_REACTOR_DIAGRAM).setIgnoreColor(true));
        builder.widget(FusionProgressSupplier.Type.BOTTOM_LEFT.getWidget(this));
        builder.widget(FusionProgressSupplier.Type.TOP_LEFT.getWidget(this));
        builder.widget(FusionProgressSupplier.Type.TOP_RIGHT.getWidget(this));
        builder.widget(FusionProgressSupplier.Type.BOTTOM_RIGHT.getWidget(this));

        // Fusion Legend
        builder.widget(new ImageWidget(7, 98, 108, 41, GuiTextures.FUSION_REACTOR_LEGEND).setIgnoreColor(true));

        // Power Button + Detail
        builder.widget(new ImageCycleButtonWidget(173, 211, 18, 18, GuiTextures.BUTTON_POWER,
                this::isWorkingEnabled, this::setWorkingEnabled));
        builder.widget(new ImageWidget(173, 229, 18, 6, GuiTextures.BUTTON_POWER_DETAIL));

        // Voiding Mode Button
        builder.widget(new ImageCycleButtonWidget(173, 189, 18, 18, GuiTextures.BUTTON_VOID_MULTIBLOCK,
                4, this::getVoidingMode, this::setVoidingMode)
                        .setTooltipHoverString(MultiblockWithDisplayBase::getVoidingModeTooltip));

        // Distinct Buses Unavailable Image
        builder.widget(new ImageWidget(173, 171, 18, 18, GuiTextures.BUTTON_NO_DISTINCT_BUSES)
                .setTooltip("gregtech.multiblock.universal.distinct_not_supported"));

        // Flex Unavailable Image
        builder.widget(getFlexButton(173, 153, 18, 18));

        // Player Inventory
        builder.bindPlayerInventory(entityPlayer.inventory, 153);
        return builder;
    }

    private void addEnergyBarHoverText(List<ITextComponent> hoverList) {
        ITextComponent energyInfo = TextComponentUtil.stringWithColor(
                TextFormatting.AQUA,
                TextFormattingUtil.formatNumbers(energyContainer.getEnergyStored()) + " / " +
                        TextFormattingUtil.formatNumbers(energyContainer.getEnergyCapacity()) + " EU");
        hoverList.add(TextComponentUtil.translationWithColor(
                TextFormatting.GRAY,
                "gregtech.multiblock.energy_stored",
                energyInfo));
    }

    private void addHeatBarHoverText(List<ITextComponent> hoverList) {
        ITextComponent heatInfo = TextComponentUtil.stringWithColor(
                TextFormatting.RED,
                TextFormattingUtil.formatNumbers(heat) + " / " +
                        TextFormattingUtil.formatNumbers(energyContainer.getEnergyCapacity()));
        hoverList.add(TextComponentUtil.translationWithColor(
                TextFormatting.GRAY,
                "gregtech.multiblock.fusion_reactor.heat",
                heatInfo));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setLong("Heat", heat);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        heat = data.getLong("Heat");
    }

    private static class FusionProgressSupplier {

        private final AtomicDouble tracker = new AtomicDouble(0.0);
        private final ProgressWidget.TimedProgressSupplier bottomLeft;
        private final DoubleSupplier topLeft;
        private final DoubleSupplier topRight;
        private final DoubleSupplier bottomRight;

        public FusionProgressSupplier() {
            // Bottom Left, fill on [0, 0.25)
            bottomLeft = new ProgressWidget.TimedProgressSupplier(200, 164, false) {

                @Override
                public double getAsDouble() {
                    double val = super.getAsDouble();
                    tracker.set(val);
                    if (val >= 0.25) {
                        return 1;
                    }
                    return 4 * val;
                }

                @Override
                public void resetCountdown() {
                    super.resetCountdown();
                    tracker.set(0);
                }
            };

            // Top Left, fill on [0.25, 0.5)
            topLeft = () -> {
                double val = tracker.get();
                if (val < 0.25) {
                    return 0;
                } else if (val >= 0.5) {
                    return 1;
                }
                return 4 * (val - 0.25);
            };

            // Top Right, fill on [0.5, 0.75)
            topRight = () -> {
                double val = tracker.get();
                if (val < 0.5) {
                    return 0;
                } else if (val >= 0.75) {
                    return 1;
                }
                return 4 * (val - 0.5);
            };

            // Bottom Right, fill on [0.75, 1.0]
            bottomRight = () -> {
                double val = tracker.get();
                if (val < 0.75) {
                    return 0;
                } else if (val >= 1) {
                    return 1;
                }
                return 4 * (val - 0.75);
            };
        }

        public void resetCountdown() {
            bottomLeft.resetCountdown();
        }

        public DoubleSupplier getSupplier(Type type) {
            return switch (type) {
                case BOTTOM_LEFT -> bottomLeft;
                case TOP_LEFT -> topLeft;
                case TOP_RIGHT -> topRight;
                case BOTTOM_RIGHT -> bottomRight;
            };
        }

        private enum Type {

            BOTTOM_LEFT(
                    61, 66, 35, 41,
                    GuiTextures.PROGRESS_BAR_FUSION_REACTOR_DIAGRAM_BL, ProgressWidget.MoveType.VERTICAL),
            TOP_LEFT(
                    61, 30, 41, 35,
                    GuiTextures.PROGRESS_BAR_FUSION_REACTOR_DIAGRAM_TL, ProgressWidget.MoveType.HORIZONTAL),
            TOP_RIGHT(
                    103, 30, 35, 41,
                    GuiTextures.PROGRESS_BAR_FUSION_REACTOR_DIAGRAM_TR, ProgressWidget.MoveType.VERTICAL_DOWNWARDS),
            BOTTOM_RIGHT(
                    97, 72, 41, 35,
                    GuiTextures.PROGRESS_BAR_FUSION_REACTOR_DIAGRAM_BR, ProgressWidget.MoveType.HORIZONTAL_BACKWARDS);

            private final int x;
            private final int y;
            private final int width;
            private final int height;
            private final TextureArea texture;
            private final ProgressWidget.MoveType moveType;

            Type(int x, int y, int width, int height, TextureArea texture, ProgressWidget.MoveType moveType) {
                this.x = x;
                this.y = y;
                this.width = width;
                this.height = height;
                this.texture = texture;
                this.moveType = moveType;
            }

            public ProgressWidget getWidget(MetaTileEntityFusionReactor instance) {
                return new ProgressWidget(
                        () -> instance.isActive() ?
                                instance.progressBarSupplier.getSupplier(this).getAsDouble() : 0,
                        x, y, width, height, texture, moveType)
                                .setIgnoreColor(true)
                                .setHoverTextConsumer(
                                        tl -> MultiblockDisplayText.builder(tl, instance.isStructureFormed())
                                                .setWorkingStatus(instance.isWorkingEnabled(),
                                                        instance.isActive())
                                                .addWorkingStatusLine());
            }
        }
    }

    protected class FusionOverclockOperator extends RecipeStandardOverclockingOperator {

        public FusionOverclockOperator(double costFactor, double speedFactor, boolean canUpTransform,
                                       @Nullable DoubleSupplier durationDiscount) {
            super(costFactor, speedFactor, canUpTransform, durationDiscount);
        }

        @Override
        public void operate(NBTTagCompound data, Map<String, Object> transientData) {
            RecipeView view = (RecipeView) transientData.get(keyView);
            PropertySet properties = (PropertySet) transientData.get(keyProperties);
            RecipeMaintenanceOperator.MaintenanceValues maintenance = (RecipeMaintenanceOperator.MaintenanceValues) transientData
                    .get(RecipeMaintenanceOperator.STANDARD_KEY);
            if (view == null || properties == null) throw new IllegalStateException();

            double discount = durationDiscount != null ? durationDiscount.getAsDouble() : 1;
            if (maintenance != null) {
                discount *= maintenance.durationBonus();
                if (view.getRecipe().isGenerating()) {
                    discount *= 1 - RecipeLogicConstants.MAINTENANCE_PROBLEM_DURATION_FACTOR * maintenance.count();
                } else {
                    discount *= 1 + RecipeLogicConstants.MAINTENANCE_PROBLEM_DURATION_FACTOR * maintenance.count();
                }
            }
            if (view.getActualEUt() == 0) {
                transientData.put(keyRun,
                        new PrimitiveRecipeRun(view, properties, view.getActualDuration() * discount));
                return;
            }
            int recipeVoltageTier = GTUtility.getTierByVoltage(view.getRecipe().getVoltage());
            long machineVoltage;
            long amperage;
            if (view.getRecipe().isGenerating()) {
                PowerCapacityProperty property = properties.getDefaultable(PowerCapacityProperty.EMPTY);
                machineVoltage = property.voltage();
                amperage = property.amperage();
            } else {
                PowerSupplyProperty property = properties.getDefaultable(PowerSupplyProperty.EMPTY);
                machineVoltage = property.voltage();
                amperage = property.amperage();
            }
            int machineVoltageTier = GTUtility.getFloorTierByVoltage(machineVoltage);
            int overclocks;
            if (canUpTransform) {
                // log base cost factor of the ratio of available EUt to required EUt
                overclocks = (int) (Math.log((double) (machineVoltage * amperage) / view.getActualEUt()) /
                        Math.log(costFactor));
            } else {
                overclocks = machineVoltageTier - recipeVoltageTier;
                while (view.getActualEUt() * Math.pow(costFactor, overclocks) > machineVoltage * amperage) overclocks--;
            }
            overclocks = Math.max(0, overclocks);

            long euToStart = view.getRecipe().getProperty(FusionEUToStartProperty.getInstance(), 0L);
            int fusionTier = FusionEUToStartProperty.getFusionTier(euToStart);
            if (fusionTier != 0) overclocks = Math.min(overclocks, MetaTileEntityFusionReactor.this.tier - fusionTier);

            transientData.put(keyRun, new SingleRecipeRun(view, recipeVoltageTier, machineVoltageTier, properties,
                    Math.pow(costFactor, overclocks),
                    view.getActualDuration() * discount / Math.pow(speedFactor, overclocks)));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(double x, double y, double z, float partialTicks) {
        if (this.hasFusionRingColor() && !this.registeredBloomRenderTicket) {
            this.registeredBloomRenderTicket = true;
            BloomEffectUtil.registerBloomRender(FusionBloomSetup.INSTANCE, getBloomType(), this, this);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderBloomEffect(@NotNull BufferBuilder buffer, @NotNull EffectRenderContext context) {
        if (!this.hasFusionRingColor()) return;
        int color = RenderUtil.interpolateColor(this.getFusionRingColor(), -1, Eases.QUAD_IN.getInterpolation(
                Math.abs((Math.abs(getOffsetTimer() % 50) + context.partialTicks()) - 25) / 25));
        float a = (float) (color >> 24 & 255) / 255.0F;
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;
        EnumFacing relativeBack = RelativeDirection.BACK.getRelativeFacing(getFrontFacing(), getUpwardsFacing(),
                isFlipped());
        EnumFacing.Axis axis = RelativeDirection.UP.getRelativeFacing(getFrontFacing(), getUpwardsFacing(), isFlipped())
                .getAxis();

        buffer.begin(GL11.GL_QUAD_STRIP, DefaultVertexFormats.POSITION_COLOR);
        RenderBufferHelper.renderRing(buffer,
                getPos().getX() - context.cameraX() + relativeBack.getXOffset() * 7 + 0.5,
                getPos().getY() - context.cameraY() + relativeBack.getYOffset() * 7 + 0.5,
                getPos().getZ() - context.cameraZ() + relativeBack.getZOffset() * 7 + 0.5,
                6, 0.2, 10, 20,
                r, g, b, a, axis);
        Tessellator.getInstance().draw();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean shouldRenderBloomEffect(@NotNull EffectRenderContext context) {
        return this.hasFusionRingColor() && context.camera().isBoundingBoxInFrustum(getRenderBoundingBox());
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        EnumFacing relativeRight = RelativeDirection.RIGHT.getRelativeFacing(getFrontFacing(), getUpwardsFacing(),
                isFlipped());
        EnumFacing relativeBack = RelativeDirection.BACK.getRelativeFacing(getFrontFacing(), getUpwardsFacing(),
                isFlipped());

        return new AxisAlignedBB(
                this.getPos().offset(relativeBack).offset(relativeRight, 6),
                this.getPos().offset(relativeBack, 13).offset(relativeRight.getOpposite(), 6));
    }

    @Override
    public boolean shouldRenderInPass(int pass) {
        return pass == 0;
    }

    @Override
    public boolean isGlobalRenderer() {
        return true;
    }

    private static BloomType getBloomType() {
        ConfigHolder.FusionBloom fusionBloom = ConfigHolder.client.shader.fusionBloom;
        return BloomType.fromValue(fusionBloom.useShader ? fusionBloom.bloomStyle : -1);
    }

    @SideOnly(Side.CLIENT)
    private static final class FusionBloomSetup implements IRenderSetup {

        private static final FusionBloomSetup INSTANCE = new FusionBloomSetup();

        float lastBrightnessX;
        float lastBrightnessY;

        @Override
        public void preDraw(@NotNull BufferBuilder buffer) {
            BloomEffect.strength = (float) ConfigHolder.client.shader.fusionBloom.strength;
            BloomEffect.baseBrightness = (float) ConfigHolder.client.shader.fusionBloom.baseBrightness;
            BloomEffect.highBrightnessThreshold = (float) ConfigHolder.client.shader.fusionBloom.highBrightnessThreshold;
            BloomEffect.lowBrightnessThreshold = (float) ConfigHolder.client.shader.fusionBloom.lowBrightnessThreshold;
            BloomEffect.step = 1;

            lastBrightnessX = OpenGlHelper.lastBrightnessX;
            lastBrightnessY = OpenGlHelper.lastBrightnessY;
            GlStateManager.color(1, 1, 1, 1);
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
            GlStateManager.disableTexture2D();
        }

        @Override
        public void postDraw(@NotNull BufferBuilder buffer) {
            GlStateManager.enableTexture2D();
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lastBrightnessX, lastBrightnessY);
        }
    }
}

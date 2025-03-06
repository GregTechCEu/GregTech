package gregtech.common.metatileentities.multi.electric;

import gregtech.api.GTValues;
import gregtech.api.capability.IHasRecipeMap;
import gregtech.api.metatileentity.IMachineHatchMultiblock;
import gregtech.api.metatileentity.ITieredMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.DistinctRecipeMapMultiblockController;
import gregtech.api.metatileentity.multiblock.DummyCleanroom;
import gregtech.api.metatileentity.multiblock.ICleanroomProvider;
import gregtech.api.metatileentity.multiblock.ICleanroomReceiver;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockDisplayText;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.logic.statemachine.builder.RecipeStandardStateMachineBuilder;
import gregtech.api.recipes.lookup.property.PropertySet;
import gregtech.api.util.GTUtility;
import gregtech.api.util.TextComponentUtil;
import gregtech.api.util.TextFormattingUtil;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.OrientedOverlayRenderer;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.core.sound.GTSoundEvents;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MetaTileEntityProcessingArray extends DistinctRecipeMapMultiblockController
                                           implements IMachineHatchMultiblock {

    private static final ICleanroomProvider DUMMY_CLEANROOM = DummyCleanroom.createForAllTypes();

    protected @NotNull ItemStack currentMachineStack = ItemStack.EMPTY;
    protected MetaTileEntity mte = null;
    // The Voltage Tier of the machines the PA is operating upon, from GTValues.V
    protected int machineTier;
    // The maximum Voltage of the machines the PA is operating upon
    protected long machineVoltage;
    // The Recipe Map of the machines the PA is operating upon
    protected RecipeMap<?> activeRecipeMap;

    private final int tier;
    private boolean machineChanged;

    public MetaTileEntityProcessingArray(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, null);
        this.tier = tier;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityProcessingArray(metaTileEntityId, tier);
    }

    @Override
    protected void modifyRecipeLogicStandardBuilder(RecipeStandardStateMachineBuilder builder) {
        super.modifyRecipeLogicStandardBuilder(builder);
        builder.setLookup(() -> getRecipeMap().getLookup());
    }

    @Override
    protected int getBaseParallelLimit() {
        return Math.min(getMachineLimit(), currentMachineStack.getCount());
    }

    @Override
    protected @NotNull PropertySet computePropertySet() {
        PropertySet set = super.computePropertySet();
        set.comprehensive(machineVoltage,
                getEnergyContainer().getInputAmperage(), machineVoltage,
                getEnergyContainer().getOutputAmperage());
        return set;
    }

    @Override
    public boolean shouldRecipeWorkableUpdate() {
        if (machineChanged) findMachineStack();
        if (activeRecipeMap == null) return false;
        if (ArrayUtils.contains(this.getBlacklist(), activeRecipeMap.getUnlocalizedName())) {
            return false;
        }
        if (!GTUtility.isMachineValidForMachineHatch(currentMachineStack, getBlacklist())) {
            return false;
        }
        return true;
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();

        // invalidate mte's cleanroom reference
        if (mte != null && mte instanceof ICleanroomReceiver) {
            ((ICleanroomReceiver) mte).setCleanroom(null);
        }

        // Reset locally cached variables upon invalidation
        currentMachineStack = ItemStack.EMPTY;
        mte = null;
        machineChanged = true;
        machineTier = 0;
        machineVoltage = 0L;
        activeRecipeMap = null;
    }

    protected void findMachineStack() {
        // The Processing Array is limited to 1 Machine Interface per multiblock, and only has 1 slot
        ItemStack machine = this.getAbilities(MultiblockAbility.MACHINE_HATCH).get(0).getStackInSlot(0);

        mte = GTUtility.getMetaTileEntity(machine);

        if (mte instanceof IHasRecipeMap has) {
            this.activeRecipeMap = has.getRecipeMap();
            // Set the world for MTEs, as some need it for checking their recipes
            MetaTileEntityHolder holder = new MetaTileEntityHolder();
            mte = holder.setMetaTileEntity(mte);
            holder.setWorld(this.getWorld());

            updateCleanroom();
        } else {
            this.activeRecipeMap = null;
        }

        // Find the voltage tier of the machine.
        this.machineTier = mte instanceof ITieredMetaTileEntity ? ((ITieredMetaTileEntity) mte).getTier() : 0;

        this.machineVoltage = GTValues.V[this.machineTier];

        this.currentMachineStack = machine;
    }

    private void updateCleanroom() {
        // Set the cleanroom of the MTEs to the PA's cleanroom reference
        if (mte instanceof ICleanroomReceiver receiver) {
            if (ConfigHolder.machines.cleanMultiblocks) {
                receiver.setCleanroom(DUMMY_CLEANROOM);
            } else {
                ICleanroomProvider provider = getCleanroom();
                if (provider != null) receiver.setCleanroom(provider);
            }
        }
    }

    @Override
    public int getMachineLimit() {
        return tier == 0 ? 16 : 64;
    }

    @NotNull
    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("XXX", "XXX", "XXX")
                .aisle("XXX", "X#X", "XXX")
                .aisle("XXX", "XSX", "XXX")
                .where('L', states(getCasingState()))
                .where('S', selfPredicate())
                .where('X', states(getCasingState())
                        .setMinGlobalLimited(tier == 0 ? 11 : 4)
                        .or(autoAbilities(false, true, true, true, true, true, true))
                        .or(abilities(MultiblockAbility.INPUT_ENERGY).setMinGlobalLimited(1).setMaxGlobalLimited(4))
                        .or(abilities(MultiblockAbility.MACHINE_HATCH).setExactLimit(1)))
                .where('#', air())
                .build();
    }

    public IBlockState getCasingState() {
        return tier == 0 ? MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.TUNGSTENSTEEL_ROBUST) :
                MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.HSSE_STURDY);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return tier == 0 ? Textures.ROBUST_TUNGSTENSTEEL_CASING : Textures.STURDY_HSSE_CASING;
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        MultiblockDisplayText.builder(textList, isStructureFormed())
                .setWorkingStatus(isWorkingEnabled(), isActive())
                .addEnergyUsageLine(getEnergyContainer())
                .addEnergyTierLine(currentMachineStack == ItemStack.EMPTY ? -1 : machineTier)
                .addCustom(tl -> {
                    if (isStructureFormed()) {

                        // Machine mode text
                        // Shared text components for both states
                        ITextComponent maxMachinesText = TextComponentUtil.stringWithColor(TextFormatting.DARK_PURPLE,
                                Integer.toString(getMachineLimit()));
                        maxMachinesText = TextComponentUtil.translationWithColor(TextFormatting.GRAY,
                                "gregtech.machine.machine_hatch.machines_max", maxMachinesText);

                        if (activeRecipeMap == null) {
                            // No machines in hatch
                            ITextComponent noneText = TextComponentUtil.translationWithColor(TextFormatting.YELLOW,
                                    "gregtech.machine.machine_hatch.machines_none");
                            ITextComponent bodyText = TextComponentUtil.translationWithColor(TextFormatting.GRAY,
                                    "gregtech.machine.machine_hatch.machines", noneText);
                            ITextComponent hoverText1 = TextComponentUtil.translationWithColor(TextFormatting.GRAY,
                                    "gregtech.machine.machine_hatch.machines_none_hover");
                            tl.add(TextComponentUtil.setHover(bodyText, hoverText1, maxMachinesText));
                        } else {
                            // Some amount of machines in hatch
                            String key = currentMachineStack.getTranslationKey();
                            ITextComponent mapText = TextComponentUtil.translationWithColor(TextFormatting.DARK_PURPLE,
                                    key + ".name");
                            mapText = TextComponentUtil.translationWithColor(
                                    TextFormatting.DARK_PURPLE,
                                    "%sx %s",
                                    getBaseParallelLimit(), mapText);
                            ITextComponent bodyText = TextComponentUtil.translationWithColor(TextFormatting.GRAY,
                                    "gregtech.machine.machine_hatch.machines", mapText);
                            ITextComponent voltageName = new TextComponentString(GTValues.VNF[machineTier]);
                            int amps = currentMachineStack.getCount();
                            String energyFormatted = TextFormattingUtil
                                    .formatNumbers(GTValues.V[machineTier] * amps);
                            ITextComponent hoverText = TextComponentUtil.translationWithColor(
                                    TextFormatting.GRAY,
                                    "gregtech.machine.machine_hatch.machines_max_eut",
                                    energyFormatted, amps, voltageName);
                            tl.add(TextComponentUtil.setHover(bodyText, hoverText, maxMachinesText));
                        }

                        // Hatch locked status
                        if (isActive()) {
                            tl.add(TextComponentUtil.translationWithColor(TextFormatting.DARK_RED,
                                    "gregtech.machine.machine_hatch.locked"));
                        }
                    }
                })
                .addWorkingStatusLine();
        // .addProgressLine(recipeProgressPercent());
        // TODO multiple recipe display
    }

    @SideOnly(Side.CLIENT)
    @NotNull
    @Override
    protected OrientedOverlayRenderer getFrontOverlay() {
        return tier == 0 ? Textures.PROCESSING_ARRAY_OVERLAY : Textures.ADVANCED_PROCESSING_ARRAY_OVERLAY;
    }

    @Override
    public void notifyMachineChanged() {
        machineChanged = true;
    }

    @Override
    public String[] getBlacklist() {
        return ConfigHolder.machines.processingArrayBlacklist;
    }

    @Override
    public SoundEvent getBreakdownSound() {
        return GTSoundEvents.BREAKDOWN_MECHANICAL;
    }

    @Override
    public SoundEvent getSound() {
        return GTSoundEvents.ARC;
    }

    @Override
    public TraceabilityPredicate autoAbilities(boolean checkEnergyIn, boolean checkMaintenance, boolean checkItemIn,
                                               boolean checkItemOut, boolean checkFluidIn, boolean checkFluidOut,
                                               boolean checkMuffler) {
        TraceabilityPredicate predicate = super.autoAbilities(checkMaintenance, checkMuffler)
                .or(checkEnergyIn ? abilities(MultiblockAbility.INPUT_ENERGY).setMinGlobalLimited(1)
                        .setMaxGlobalLimited(4).setPreviewCount(1) : new TraceabilityPredicate());

        predicate = predicate.or(abilities(MultiblockAbility.IMPORT_ITEMS).setPreviewCount(1));

        predicate = predicate.or(abilities(MultiblockAbility.EXPORT_ITEMS).setPreviewCount(1));

        predicate = predicate.or(abilities(MultiblockAbility.IMPORT_FLUIDS).setPreviewCount(1));

        predicate = predicate.or(abilities(MultiblockAbility.EXPORT_FLUIDS).setPreviewCount(1));

        return predicate;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.universal.tooltip.parallel", getMachineLimit()));
    }

    @Override
    public int getItemOutputLimit() {
        return mte == null ? 0 : mte.getItemOutputLimit();
    }

    @Override
    public void setCleanroom(ICleanroomProvider provider) {
        super.setCleanroom(provider);
        updateCleanroom();
    }
}

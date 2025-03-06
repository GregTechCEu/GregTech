package gregtech.common.metatileentities.multi.electric;

import gregtech.api.block.IHeatingCoilBlockStats;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.DistinctRecipeMapMultiblockController;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockDisplayText;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.logic.statemachine.builder.RecipeStandardStateMachineBuilder;
import gregtech.api.util.GTUtility;
import gregtech.api.util.TextComponentUtil;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockMachineCasing.MachineCasingType;
import gregtech.common.blocks.MetaBlocks;
import gregtech.core.sound.GTSoundEvents;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MetaTileEntityPyrolyseOven extends DistinctRecipeMapMultiblockController {

    private int coilTier;

    public MetaTileEntityPyrolyseOven(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, RecipeMaps.PYROLYSE_RECIPES);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityPyrolyseOven(metaTileEntityId);
    }

    @Override
    protected void modifyRecipeLogicStandardBuilder(RecipeStandardStateMachineBuilder builder) {
        super.modifyRecipeLogicStandardBuilder(builder);
        builder.setDurationDiscount(() -> {
            int coilTier = getCoilTier();
            if (coilTier < 0) return 1;
            // 75% speed with cupronickel (coilTier = 0)
            if (coilTier == 0) return 4d / 3;
            // each coil above kanthal (coilTier = 1) is 50% faster
            return 2d / (1 + coilTier);
        });
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("XXX", "XXX", "XXX")
                .aisle("CCC", "C#C", "CCC")
                .aisle("CCC", "C#C", "CCC")
                .aisle("XXX", "XSX", "XXX")
                .where('S', selfPredicate())
                .where('X', states(getCasingState()).setMinGlobalLimited(6).or(autoAbilities()))
                .where('C', heatingCoils())
                .where('#', air())
                .build();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.VOLTAGE_CASINGS[0];
    }

    protected IBlockState getCasingState() {
        return MetaBlocks.MACHINE_CASING.getState(MachineCasingType.ULV);
    }

    @Override
    public SoundEvent getBreakdownSound() {
        return GTSoundEvents.BREAKDOWN_ELECTRICAL;
    }

    @SideOnly(Side.CLIENT)
    @NotNull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return Textures.PYROLYSE_OVEN_OVERLAY;
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        Object type = context.get("CoilType");
        if (type instanceof IHeatingCoilBlockStats)
            this.coilTier = ((IHeatingCoilBlockStats) type).getTier();
        else
            this.coilTier = 0;
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        MultiblockDisplayText.builder(textList, isStructureFormed())
                .setWorkingStatus(isWorkingEnabled(), isActive())
                .addEnergyUsageLine(getEnergyContainer())
                .addEnergyTierLine(GTUtility.getTierByVoltage(getEnergyContainer().getInputVoltage()))
                .addCustom(tl -> {
                    if (isStructureFormed()) {
                        int processingSpeed = coilTier == 0 ? 75 : 50 * (coilTier + 1);
                        ITextComponent speedIncrease = TextComponentUtil.stringWithColor(
                                getSpeedColor(processingSpeed),
                                processingSpeed + "%");

                        ITextComponent base = TextComponentUtil.translationWithColor(
                                TextFormatting.GRAY,
                                "gregtech.multiblock.pyrolyse_oven.speed",
                                speedIncrease);

                        ITextComponent hover = TextComponentUtil.translationWithColor(
                                TextFormatting.GRAY,
                                "gregtech.multiblock.pyrolyse_oven.speed_hover");

                        tl.add(TextComponentUtil.setHover(base, hover));
                    }
                })
                .addParallelsLine(getBaseParallelLimit())
                .addWorkingStatusLine();
        // .addProgressLine(recipeProgressPercent());
        // TODO multiple recipe display
    }

    private TextFormatting getSpeedColor(int speed) {
        if (speed < 100) {
            return TextFormatting.RED;
        } else if (speed == 100) {
            return TextFormatting.GRAY;
        } else if (speed < 250) {
            return TextFormatting.GREEN;
        } else {
            return TextFormatting.LIGHT_PURPLE;
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.machine.pyrolyse_oven.tooltip.1"));
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.coilTier = -1;
    }

    protected int getCoilTier() {
        return this.coilTier;
    }

    @Override
    public boolean hasMufflerMechanics() {
        return true;
    }
}

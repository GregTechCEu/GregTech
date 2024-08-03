package gregtech.common.metatileentities.multi.electric;

import gregtech.api.gui.Widget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.pattern.BlockPattern;
import gregtech.api.pattern.pattern.FactoryBlockPattern;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.util.RelativeDirection;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockMetalCasing.MetalCasingType;
import gregtech.common.blocks.MetaBlocks;
import gregtech.core.sound.GTSoundEvents;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import static gregtech.api.gui.widgets.AdvancedTextWidget.withButton;

// todo fix this stupid multiblock back to its original form
public class MetaTileEntityVacuumFreezer extends RecipeMapMultiblockController {

    public MetaTileEntityVacuumFreezer(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, RecipeMaps.VACUUM_RECIPES);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityVacuumFreezer(metaTileEntityId);
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("XXX", "XXX", "XXX")
                .aisle("XXX", "X#X", "XXX")
                .aisle("XXX", "XSX", "XXX")
                .where('S', selfPredicate())
                .where('X', states(getCasingState()).setMinGlobalLimited(14).or(autoAbilities()))
                .where('#', air())
                .build();
    }

    @Override
    protected void createStructurePatterns() {
        super.createStructurePatterns();
        structures.put("SECOND", FactoryBlockPattern.start()
                .aisle("X")
                .where('X', states(getCasingState()))
                .startOffset(RelativeDirection.FRONT, 5)
                .build());
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        super.addDisplayText(textList);

        ITextComponent button = new TextComponentString("Second structure offset: " +
                ((BlockPattern) getSubstructure("SECOND")).getStartOffset(RelativeDirection.FRONT));
        button.appendText(" ");
        button.appendSibling(withButton(new TextComponentString("[-]"), "sub"));
        button.appendText(" ");
        button.appendSibling(withButton(new TextComponentString("[+]"), "add"));
        textList.add(button);

        textList.add(
                new TextComponentString("Second structure: " + (isStructureFormed("SECOND") ? "FORMED" : "UNFORMED")));
    }

    @Override
    protected void handleDisplayClick(String componentData, Widget.ClickData clickData) {
        super.handleDisplayClick(componentData, clickData);
        int mod = componentData.equals("add") ? 1 : -1;
        // ((BlockPattern) getSubstructure("SECOND").moveStartOffset(RelativeDirection.FRONT, mod);
        // getSubstructure("SECOND").clearCache();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.FROST_PROOF_CASING;
    }

    protected IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(MetalCasingType.ALUMINIUM_FROSTPROOF);
    }

    @Override
    public SoundEvent getBreakdownSound() {
        return GTSoundEvents.BREAKDOWN_ELECTRICAL;
    }

    @SideOnly(Side.CLIENT)
    @NotNull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return Textures.VACUUM_FREEZER_OVERLAY;
    }
}

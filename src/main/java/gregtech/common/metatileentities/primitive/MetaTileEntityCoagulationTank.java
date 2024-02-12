package gregtech.common.metatileentities.primitive;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.ProgressWidget;
import gregtech.api.gui.widgets.RecipeProgressWidget;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.gui.widgets.TankWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapPrimitiveMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.util.GTTransferUtils;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockPrimitiveCasing;
import gregtech.common.blocks.MetaBlocks;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;

public class MetaTileEntityCoagulationTank extends RecipeMapPrimitiveMultiblockController {
    public MetaTileEntityCoagulationTank(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, RecipeMaps.COAGULATION_RECIPES);
    }

    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityCoagulationTank(this.metaTileEntityId);
    }

    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("XXX", "XXX", "XXX")
                .aisle("XXX", "X#X", "X#X")
                .aisle("XXX", "XSX", "XXX")
                .where('X',
                        states(MetaBlocks.PRIMITIVE_CASING
                                .getState(BlockPrimitiveCasing.MultiblockCasingType.COAGULATION_TANK_WALL))
                                .or(abilities(MultiblockAbility.EXPORT_ITEMS)
                                        .setMaxGlobalLimited(1))
                                .or(abilities(MultiblockAbility.IMPORT_FLUIDS)
                                        .setMaxGlobalLimited(1)))
                .where('#', air())
                .where('S', this.selfPredicate()).build();
    }

    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.WOODEN_COAGULATION_TANK_WALL;
    }

    protected ModularUI.Builder createUITemplate(EntityPlayer entityPlayer) {
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.PRIMITIVE_BACKGROUND, 176,166);
        builder.label(6, 6, this.getMetaFullName());
        builder.widget(new RecipeProgressWidget(this.recipeMapWorkable::getProgressPercent, 76, 41, 20, 15,
                GuiTextures.PRIMITIVE_BLAST_FURNACE_PROGRESS_BAR, ProgressWidget.MoveType.HORIZONTAL, RecipeMaps.COAGULATION_RECIPES));

        builder.widget((new SlotWidget(this.importItems, 0, 30, 30, true, true)
                .setBackgroundTexture(GuiTextures.PRIMITIVE_SLOT)));
        builder.widget((new SlotWidget(this.importItems, 1, 48, 30, true, true)
                .setBackgroundTexture(GuiTextures.PRIMITIVE_SLOT)));
        builder.widget((new TankWidget(this.importFluids.getTankAt(1), 30, 48, 18, 18))
                .setAlwaysShowFull(true)
                .setBackgroundTexture(GuiTextures.FLUID_SLOT)
                .setContainerClicking(true, true));
        builder.widget((new TankWidget(this.importFluids.getTankAt(0), 48, 48, 18, 18))
                .setAlwaysShowFull(true).setBackgroundTexture(GuiTextures.FLUID_SLOT)
                .setContainerClicking(true, true));
        builder.widget((new SlotWidget(this.exportItems, 0, 106, 39, true, false)
                .setBackgroundTexture(GuiTextures.PRIMITIVE_SLOT)));


        return builder.bindPlayerInventory(entityPlayer.inventory, GuiTextures.PRIMITIVE_SLOT, 0);
    }

    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        this.getFrontOverlay().renderOrientedState(renderState, translation, pipeline, this.getFrontFacing(),
                this.recipeMapWorkable.isActive(), this.recipeMapWorkable.isWorkingEnabled());
    }

    public void update() {
        super.update();
        if (this.getOffsetTimer() % 5 == 0 && this.isStructureFormed()){
            for (IFluidTank tank : getAbilities(MultiblockAbility.IMPORT_FLUIDS)) {
                if(tank.getFluid() != null){
                    NonNullList<FluidStack> fluidStacks = NonNullList.create();
                    int toFill = (this.importFluids.getTankAt(0).getCapacity() - this.importFluids.getTankAt(0).getFluidAmount());
                    int amount = Math.min(tank.getFluidAmount(), toFill);
                    fluidStacks.add(new FluidStack(tank.getFluid().getFluid(),amount));
                    if(GTTransferUtils.addFluidsToFluidHandler(this.importFluids,true, fluidStacks)) {
                        GTTransferUtils.addFluidsToFluidHandler(this.importFluids,false, fluidStacks);
                        tank.drain(amount, true);
                    }
                }
            }
            for (int i = 0; i < this.exportItems.getSlots(); i++) {
                ItemStack stack = this.exportItems.getStackInSlot(i);
                this.exportItems.setStackInSlot(i,GTTransferUtils.insertItem(new ItemHandlerList(this.getAbilities(MultiblockAbility.EXPORT_ITEMS)), stack,false));
            }
            this.fillInternalTankFromFluidContainer();
        }
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
    }

    @Override
    protected void initializeAbilities() {
        super.initializeAbilities();
    }

    protected ICubeRenderer getFrontOverlay() {
        return Textures.PRIMITIVE_PUMP_OVERLAY;
    }

    @Override

    public boolean hasMaintenanceMechanics() {
        return false;
    }
}

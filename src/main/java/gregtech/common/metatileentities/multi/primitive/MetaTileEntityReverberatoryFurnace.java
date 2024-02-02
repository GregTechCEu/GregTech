package gregtech.common.metatileentities.multi.primitive;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.*;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityUIFactory;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.RecipeMapPrimitiveMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.MultiblockShapeInfo;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.recipes.RecipeMaps;
import gregtech.client.particle.VanillaParticleEffects;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;

import net.minecraft.block.BlockDoor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MetaTileEntityReverberatoryFurnace extends RecipeMapPrimitiveMultiblockController {

    public MetaTileEntityReverberatoryFurnace(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, RecipeMaps.COKE_OVEN_RECIPES);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityReverberatoryFurnace(metaTileEntityId);
    }

    @NotNull
    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("#XXX#", "#XXX#", "#XXX#", "##X##", "#####", "#####", "#####")
                .aisle("XXXXX", "XAAAX", "XAAAX", "#XXX#", "##X##", "##X##", "##X##")
                .aisle("XXXXX", "XAAAX", "XAAAX", "#XAX#", "#XAX#", "#XAX#", "#XAX#")
                .aisle("XXXXX", "XAAAX", "XAAAX", "#XXX#", "##X##", "##X##", "##X##")
                .aisle("#XXX#", "#XXX#", "#XXX#", "##S##", "#####", "#####", "#####")
                .where('X',
                        refractoryBricks()
                                .or(metaTileEntities(MetaTileEntities.ITEM_IMPORT_BUS).setMinGlobalLimited(1).setMaxGlobalLimited(2))
                                .or(metaTileEntities(MetaTileEntities.ITEM_EXPORT_BUS).setMinGlobalLimited(1).setMaxGlobalLimited(1))
                )
                .where('D', doorPredicate())
                .where('A', air())
                .where('S', selfPredicate())
                .where('#', any())
                .build();
    }

    protected IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.COKE_BRICKS);
    }

    protected static TraceabilityPredicate doorPredicate() {
        return new TraceabilityPredicate(
                blockWorldState -> blockWorldState.getBlockState().getBlock() instanceof BlockDoor);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.COKE_BRICKS;
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        getFrontOverlay().renderOrientedState(renderState, translation, pipeline, getFrontFacing(),
                recipeMapWorkable.isActive(), recipeMapWorkable.isWorkingEnabled());
    }

    @SideOnly(Side.CLIENT)
    @NotNull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return Textures.COKE_OVEN_OVERLAY;
    }

    @Override
    public boolean hasMaintenanceMechanics() {
        return false;
    }

    @Override
    protected ModularUI.Builder createUITemplate(EntityPlayer entityPlayer) {
        return ModularUI.builder(GuiTextures.PRIMITIVE_BACKGROUND, 176, 166)
                .shouldColor(false)
                .widget(new LabelWidget(5, 5, getMetaFullName()))
                .widget(new SlotWidget(importItems, 0, 52, 30, true, true)
                        .setBackgroundTexture(GuiTextures.PRIMITIVE_SLOT, GuiTextures.PRIMITIVE_FURNACE_OVERLAY))
                .widget(new RecipeProgressWidget(recipeMapWorkable::getProgressPercent, 76, 32, 20, 15,
                        GuiTextures.PRIMITIVE_BLAST_FURNACE_PROGRESS_BAR, ProgressWidget.MoveType.HORIZONTAL,
                        RecipeMaps.COKE_OVEN_RECIPES))
                .widget(new SlotWidget(exportItems, 0, 103, 30, true, false)
                        .setBackgroundTexture(GuiTextures.PRIMITIVE_SLOT, GuiTextures.PRIMITIVE_FURNACE_OVERLAY))
                .widget(new TankWidget(exportFluids.getTankAt(0), 134, 13, 20, 58)
                        .setBackgroundTexture(GuiTextures.PRIMITIVE_LARGE_FLUID_TANK)
                        .setOverlayTexture(GuiTextures.PRIMITIVE_LARGE_FLUID_TANK_OVERLAY)
                        .setContainerClicking(true, false))
                .bindPlayerInventory(entityPlayer.inventory, GuiTextures.PRIMITIVE_SLOT, 0);
    }

    @Override
    public void randomDisplayTick() {
        if (this.isActive()) {
            VanillaParticleEffects.defaultFrontEffect(this, 0.3F, EnumParticleTypes.SMOKE_LARGE,
                    EnumParticleTypes.FLAME);
            if (ConfigHolder.machines.machineSounds && GTValues.RNG.nextDouble() < 0.1) {
                BlockPos pos = getPos();
                getWorld().playSound(pos.getX(), pos.getY(), pos.getZ(),
                        SoundEvents.BLOCK_FURNACE_FIRE_CRACKLE, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
            }
        }
    }

    @Override
    public boolean onRightClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing,
                                CuboidRayTraceResult hitResult) {
        // try to fill a bucket (or similar) with creosote on right click (if not sneaking)
        if (playerIn.getHeldItem(hand).hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
            if (!playerIn.isSneaking()) {
                return getWorld().isRemote || FluidUtil.interactWithFluidHandler(playerIn, hand, getFluidInventory());
            } else {
                // allow opening UI on shift-right-click with fluid container item
                if (getWorld() != null && !getWorld().isRemote) {
                    MetaTileEntityUIFactory.INSTANCE.openUI(getHolder(), (EntityPlayerMP) playerIn);
                }
                return true;
            }
        }
        return super.onRightClick(playerIn, hand, facing, hitResult);
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        ArrayList<MultiblockShapeInfo> shapeInfo = new ArrayList<>();
        MultiblockShapeInfo.Builder builder = MultiblockShapeInfo.builder()
                .aisle("#XXX#", "#XXX#", "#XXX#", "##X##", "#####", "#####", "#####")
                .aisle("XXXXX", "X###X", "X###X", "#XXX#", "##X##", "##X##", "##X##")
                .aisle("IXXXE", "X###X", "X###X", "#X#X#", "#X#X#", "#X#X#", "#X#X#")
                .aisle("XXXXX", "X###X", "X###X", "#XXX#", "##X##", "##X##", "##X##")
                .aisle("#XXX#", "#XXX#", "#XXX#", "##S##", "#####", "#####", "#####")
                 /*.where('X',
                        refractoryBricks()
                                .or(metaTileEntities(MetaTileEntities.ITEM_IMPORT_BUS).setMinGlobalLimited(1).setMaxGlobalLimited(1))
                                .or(metaTileEntities(MetaTileEntities.ITEM_EXPORT_BUS).setMinGlobalLimited(1).setMaxGlobalLimited(1))
                )*/
                .where('I', MetaTileEntities.ITEM_IMPORT_BUS[GTValues.LV], EnumFacing.DOWN)
                .where('E', MetaTileEntities.ITEM_EXPORT_BUS[GTValues.LV], EnumFacing.DOWN)
                .where('#', Blocks.AIR.getDefaultState())
                .where('S', MetaTileEntities.REVERBERATORY_FURNACE, EnumFacing.SOUTH);
        GregTechAPI.REFRACTORY_BRICKS.entrySet().stream()
                .sorted(Comparator.comparingInt(entry -> entry.getValue().getTier()))
                .forEach(entry -> shapeInfo.add(builder.where('X', entry.getKey()).build()));
        return shapeInfo;
    }

}

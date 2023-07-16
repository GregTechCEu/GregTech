package gregtech.common.metatileentities.multi;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.sync.GuiSyncHandler;
import com.cleanroommc.modularui.sync.SyncHandlers;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.widgets.ProgressWidget;
import com.cleanroommc.modularui.widgets.TextWidget;
import com.cleanroommc.modularui.widgets.layout.Row;
import gregtech.api.GTValues;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.LabelWidget;
import gregtech.api.gui.widgets.RecipeProgressWidget;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.gui.widgets.TankWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.RecipeMapPrimitiveMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.ui.SlotUtils;
import gregtech.api.ui.UITextures;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static gregtech.api.capability.GregtechDataCodes.*;

public class MetaTileEntityCokeOven extends RecipeMapPrimitiveMultiblockController {

    public MetaTileEntityCokeOven(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, RecipeMaps.COKE_OVEN_RECIPES);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityCokeOven(metaTileEntityId);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("XXX", "XXX", "XXX")
                .aisle("XXX", "X#X", "XXX")
                .aisle("XXX", "XYX", "XXX")
                .where('X', states(getCasingState()).or(metaTileEntities(MetaTileEntities.COKE_OVEN_HATCH).setMaxGlobalLimited(5)))
                .where('#', air())
                .where('Y', selfPredicate())
                .build();
    }

    protected IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.COKE_BRICKS);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.COKE_BRICKS;
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        getFrontOverlay().renderOrientedState(renderState, translation, pipeline, getFrontFacing(), recipeMapWorkable.isActive(), recipeMapWorkable.isWorkingEnabled());
    }

    @Nonnull
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
                .widget(new RecipeProgressWidget(recipeMapWorkable::getProgressPercent, 76, 32, 20, 15, GuiTextures.PRIMITIVE_BLAST_FURNACE_PROGRESS_BAR, gregtech.api.gui.widgets.ProgressWidget.MoveType.HORIZONTAL, RecipeMaps.COKE_OVEN_RECIPES))
                .widget(new SlotWidget(exportItems, 0, 103, 30, true, false)
                        .setBackgroundTexture(GuiTextures.PRIMITIVE_SLOT, GuiTextures.PRIMITIVE_FURNACE_OVERLAY))
                .widget(new TankWidget(exportFluids.getTankAt(0), 134, 13, 20, 58)
                        .setBackgroundTexture(GuiTextures.PRIMITIVE_LARGE_FLUID_TANK)
                        .setOverlayTexture(GuiTextures.PRIMITIVE_LARGE_FLUID_TANK_OVERLAY)
                        .setContainerClicking(true, false))
                .bindPlayerInventory(entityPlayer.inventory, GuiTextures.PRIMITIVE_SLOT, 0);
    }

    @Override
    public void buildSyncHandler(GuiSyncHandler guiSyncHandler, EntityPlayer player) {
        super.buildSyncHandler(guiSyncHandler, player);
        for (int i = 0; i < importItems.getSlots(); i++) {
            guiSyncHandler.syncValue(IMPORT_ITEMS_SYNC, i, SyncHandlers.itemSlot(importItems, i)
                    .slotGroup(IMPORT_ITEMS_SYNC)
            );
        }
        for (int i = 0; i < exportItems.getSlots(); i++) {
            guiSyncHandler.syncValue(EXPORT_ITEMS_SYNC, i, SyncHandlers.itemSlot(exportItems, i)
                    .slotGroup(EXPORT_ITEMS_SYNC)
            );
        }
        for (int i = 0; i < exportFluids.getTanks(); i++) {
            guiSyncHandler.syncValue(EXPORT_FLUIDS_SYNC, i, SyncHandlers.fluidSlot(exportFluids.getTankAt(i))
                    .canFillSlot(false).canDrainSlot(true)
            );
        }
        guiSyncHandler.registerSlotGroup(IMPORT_ITEMS_SYNC, 1);
        guiSyncHandler.registerSlotGroup(EXPORT_ITEMS_SYNC, 1);
        guiSyncHandler.syncValue(PROGRESS_BAR_UI_SYNC, SyncHandlers.doubleNumber(recipeMapWorkable::getProgressPercent, d -> {}));
    }

    @SideOnly(Side.CLIENT)
    @Nullable
    @Override
    protected ModularPanel createClientGui(@Nonnull GuiContext context) {
        ModularPanel panel = new ModularPanel(context).name(getGuiName());
        panel.flex()
                .size(176, 166)
                .align(Alignment.Center);

        panel.bindPlayerInventory()
                .child(new Row().child(new TextWidget(IKey.lang(getMetaFullName()))
                        .padding(7, 3))
                )
                .child(new Row().child(SlotUtils.itemGroup(importItems.getSlots(), 1)
                                .synced(IMPORT_ITEMS_SYNC).build())
                        .padding(52, 30)
                )
                .child(new Row().child(SlotUtils.itemGroup(exportItems.getSlots(), 1)
                                .synced(EXPORT_ITEMS_SYNC).build())
                        .padding(103, 30)
                )
                .child(new Row().child(SlotUtils.fluidGroup(exportFluids.getTanks(), 1)
                                .synced(EXPORT_FLUIDS_SYNC).build())
                        .padding(134, 13)
                )
                .child(new Row().child(new ProgressWidget()
                                .texture(UITextures.PROGRESS_BAR_ARROW, 20)
                                .size(20, 20)
                                .progress(recipeMapWorkable::getProgressPercent)
                                .direction(ProgressWidget.Direction.RIGHT))
                        .padding(76, 29)
                );

        return panel;
    }

    @Override
    public boolean hasNewUi() {
        return true;
    }

    @Override
    public void randomDisplayTick() {
        if (this.isActive()) {
            final BlockPos pos = getPos();
            float x = pos.getX() + 0.5F;
            float z = pos.getZ() + 0.5F;

            final EnumFacing facing = getFrontFacing();
            final float horizontalOffset = GTValues.RNG.nextFloat() * 0.6F - 0.3F;
            final float y = pos.getY() + GTValues.RNG.nextFloat() * 0.375F + 0.3F;

            if (facing.getAxis() == EnumFacing.Axis.X) {
                if (facing.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE) x += 0.52F;
                else x -= 0.52F;
                z += horizontalOffset;
            } else if (facing.getAxis() == EnumFacing.Axis.Z) {
                if (facing.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE) z += 0.52F;
                else z -= 0.52F;
                x += horizontalOffset;
            }
            if (ConfigHolder.machines.machineSounds && GTValues.RNG.nextDouble() < 0.1) {
                getWorld().playSound(x, y, z, SoundEvents.BLOCK_FURNACE_FIRE_CRACKLE, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
            }
            getWorld().spawnParticle(EnumParticleTypes.SMOKE_LARGE, x, y, z, 0, 0, 0);
            getWorld().spawnParticle(EnumParticleTypes.FLAME, x, y, z, 0, 0, 0);
        }
    }
}

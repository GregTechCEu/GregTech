package gregtech.common.metatileentities.multi;

import gregtech.api.GTValues;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.RecipeMapPrimitiveMultiblockController;
import gregtech.api.metatileentity.multiblock.ui.MultiblockUIFactory;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuiTheme;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.util.GTUtility;
import gregtech.client.particle.VanillaParticleEffects;
import gregtech.client.renderer.CubeRendererState;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.cclop.ColourOperation;
import gregtech.client.renderer.cclop.LightMapOperation;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.utils.BloomEffectUtil;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.texture.TextureUtils;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.UITexture;
import com.cleanroommc.modularui.value.sync.DoubleSyncValue;
import com.cleanroommc.modularui.widgets.ItemSlot;
import com.cleanroommc.modularui.widgets.ProgressWidget;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;

public class MetaTileEntityPrimitiveBlastFurnace extends RecipeMapPrimitiveMultiblockController {

    private static final TraceabilityPredicate SNOW_PREDICATE = new TraceabilityPredicate(
            bws -> GTUtility.isBlockSnow(bws.getBlockState()));

    public MetaTileEntityPrimitiveBlastFurnace(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, RecipeMaps.PRIMITIVE_BLAST_FURNACE_RECIPES);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityPrimitiveBlastFurnace(metaTileEntityId);
    }

    @NotNull
    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("XXX", "XXX", "XXX", "XXX")
                .aisle("XXX", "X&X", "X#X", "X#X")
                .aisle("XXX", "XYX", "XXX", "XXX")
                .where('X', states(MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.PRIMITIVE_BRICKS)))
                .where('#', air())
                .where('&', air().or(SNOW_PREDICATE)) // this won't stay in the structure, and will be broken while
                // running
                .where('Y', selfPredicate())
                .build();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.PRIMITIVE_BRICKS;
    }

    @Override
    protected MultiblockUIFactory createUIFactory() {
        return new MultiblockUIFactory(this)
                .setSize(176, 166)
                .disableDisplay()
                .disableButtons()
                .addScreenChildren((parent, syncManager) -> {
                    UITexture[] importOverlays = {
                            GTGuiTextures.PRIMITIVE_INGOT_OVERLAY,
                            GTGuiTextures.PRIMITIVE_DUST_OVERLAY,
                            GTGuiTextures.PRIMITIVE_FURNACE_OVERLAY
                    };

                    UITexture[] exportOverlays = {
                            GTGuiTextures.PRIMITIVE_INGOT_OVERLAY,
                            GTGuiTextures.PRIMITIVE_DUST_OVERLAY,
                            GTGuiTextures.PRIMITIVE_DUST_OVERLAY
                    };

                    SlotGroup importGroup = new SlotGroup("import", 1, true);

                    // 标题
                    parent.child(IKey.lang(getMetaFullName()).asWidget().pos(5, 5));

                    // 输入槽位（3个单独创建的槽）
                    parent.child(new ItemSlot()
                            .background(GTGuiTextures.SLOT_PRIMITIVE, importOverlays[0])
                            .slot(new ModularSlot(importItems, 0)
                                    .slotGroup(importGroup)
                                    .accessibility(true, true))
                            .pos(40, 12));

                    parent.child(new ItemSlot()
                            .background(GTGuiTextures.SLOT_PRIMITIVE, importOverlays[1])
                            .slot(new ModularSlot(importItems, 1)
                                    .slotGroup(importGroup)
                                    .accessibility(true, true))
                            .pos(40, 30)); // 水平偏移18像素

                    parent.child(new ItemSlot()
                            .background(GTGuiTextures.SLOT_PRIMITIVE, importOverlays[2])
                            .slot(new ModularSlot(importItems, 2)
                                    .slotGroup(importGroup)
                                    .accessibility(true, true))
                            .pos(40, 48)); // 水平偏移18像素

                    // 进度条（保持原始位置）
                    parent.child(new ProgressWidget()
                            .texture(GTGuiTextures.PRIMITIVE_BLAST_FURNACE_PROGRESS_BAR, -1)
                            .size(20, 15)
                            .pos(62, 32) // 调整到输入槽右侧
                            .value(new DoubleSyncValue(recipeMapWorkable::getProgressPercent)));

                    // 输出槽位（3个单独创建的槽）
                    parent.child(new ItemSlot()
                            .background(GTGuiTextures.SLOT_PRIMITIVE, exportOverlays[0])
                            .slot(new ModularSlot(exportItems, 0)
                                    .accessibility(false, true))
                            .pos(86, 30)); // 进度条右侧

                    parent.child(new ItemSlot()
                            .background(GTGuiTextures.SLOT_PRIMITIVE, exportOverlays[1])
                            .slot(new ModularSlot(exportItems, 1)
                                    .accessibility(false, true))
                            .pos(104, 30)); // 水平偏移18像素

                    parent.child(new ItemSlot()
                            .background(GTGuiTextures.SLOT_PRIMITIVE, exportOverlays[2])
                            .slot(new ModularSlot(exportItems, 2)
                                    .accessibility(false, true))
                            .pos(122, 30)); // 水平偏移18像素
                });
    }

    @Override
    public GTGuiTheme getUITheme() {
        return GTGuiTheme.PRIMITIVE;
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        getFrontOverlay().renderOrientedState(renderState, translation, pipeline, getFrontFacing(),
                recipeMapWorkable.isActive(), recipeMapWorkable.isWorkingEnabled());
        if (recipeMapWorkable.isActive() && isStructureFormed()) {
            EnumFacing back = getFrontFacing().getOpposite();
            Matrix4 offset = translation.copy().translate(back.getXOffset(), -0.3, back.getZOffset());
            CubeRendererState op = Textures.RENDER_STATE.get();
            Textures.RENDER_STATE.set(new CubeRendererState(op.layer, CubeRendererState.PASS_MASK, op.world));
            Textures.renderFace(renderState, offset,
                    ArrayUtils.addAll(pipeline, new LightMapOperation(240, 240), new ColourOperation(0xFFFFFFFF)),
                    EnumFacing.UP, Cuboid6.full, TextureUtils.getBlockTexture("lava_still"),
                    BloomEffectUtil.getEffectiveBloomLayer());
            Textures.RENDER_STATE.set(op);
        }
    }

    @SideOnly(Side.CLIENT)
    @NotNull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return Textures.PRIMITIVE_BLAST_FURNACE_OVERLAY;
    }

    @Override
    public boolean hasMaintenanceMechanics() {
        return false;
    }

    @Override
    public void update() {
        super.update();

        if (this.isActive()) {
            if (getWorld().isRemote) {
                VanillaParticleEffects.PBF_SMOKE.runEffect(this);
            } else {
                damageEntitiesAndBreakSnow();
            }
        }
    }

    private void damageEntitiesAndBreakSnow() {
        BlockPos middlePos = this.getPos();
        middlePos = middlePos.offset(getFrontFacing().getOpposite());
        this.getWorld().getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(middlePos))
                .forEach(entity -> entity.attackEntityFrom(DamageSource.LAVA, 3.0f));

        if (getOffsetTimer() % 10 == 0) {
            IBlockState state = getWorld().getBlockState(middlePos);
            GTUtility.tryBreakSnow(getWorld(), middlePos, state, true);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void randomDisplayTick() {
        if (this.isActive()) {
            VanillaParticleEffects.defaultFrontEffect(this, 0.3F, EnumParticleTypes.SMOKE_LARGE,
                    EnumParticleTypes.FLAME);
            if (ConfigHolder.machines.machineSounds && GTValues.RNG.nextDouble() < 0.1) {
                BlockPos pos = getPos();
                getWorld().playSound(pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F,
                        SoundEvents.BLOCK_FURNACE_FIRE_CRACKLE, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
            }
        }
    }
}

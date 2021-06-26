package gregtech.common.render;

import codechicken.lib.render.BlockRenderer;
import codechicken.lib.render.CCModel;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.block.BlockRenderingRegistry;
import codechicken.lib.render.block.ICCBlockRenderer;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.texture.TextureUtils;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import codechicken.lib.vec.TransformationList;
import codechicken.lib.vec.Vector3;
import codechicken.lib.vec.uv.IconTransformation;
import gregtech.api.unification.material.type.Material;
import gregtech.api.util.GTUtility;
import gregtech.common.blocks.surfacerock.BlockSurfaceRock;
import gregtech.common.blocks.surfacerock.TileEntitySurfaceRock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import org.lwjgl.opengl.GL11;

import java.util.Random;

public class StoneRenderer implements ICCBlockRenderer {

    private static final StoneRenderer INSTANCE = new StoneRenderer();
    public static EnumBlockRenderType BLOCK_RENDER_TYPE;
    private static CCModel[] placeholderModels = new CCModel[1];

    public static void preInit() {
        BLOCK_RENDER_TYPE = BlockRenderingRegistry.createRenderType("gt_stone");
        BlockRenderingRegistry.registerRenderer(BLOCK_RENDER_TYPE, INSTANCE);
        Random random = new Random();
        placeholderModels[0] = generateModel(random);
    }

    private static CCModel generateModel(Random random) {
        return StonePileModelGenerator.generatePebblePileModel(random);
    }

    private static CCModel getActualModel(IBlockAccess world, BlockPos pos) {
        TileEntitySurfaceRock tileEntity = BlockSurfaceRock.getTileEntity(world, pos);
        if (tileEntity != null) {
            if (tileEntity.cachedModel == null) {
                Random random = new Random(MathHelper.getPositionRandom(pos));
                tileEntity.cachedModel = generateModel(random);
            }
            return (CCModel) tileEntity.cachedModel;
        }
        return placeholderModels[0];
    }

    @Override
    public boolean renderBlock(IBlockAccess world, BlockPos pos, IBlockState state, BufferBuilder buffer) {
        //otherwise, we are in solid rendering layer and render primary stone
        CCRenderState renderState = CCRenderState.instance();
        renderState.reset();
        renderState.bind(buffer);
        Matrix4 translation = new Matrix4();
        translation.translate(pos.getX(), pos.getY(), pos.getZ());
        TextureAtlasSprite stoneSprite = TextureUtils.getBlockTexture("stone");
        Material material = ((BlockSurfaceRock) state.getBlock()).getStoneMaterial(world, pos, state);
        int renderingColor = GTUtility.convertRGBtoOpaqueRGBA_CL(material.materialRGB);
        IVertexOperation[] operations = new IVertexOperation[] {
            new IconTransformation(stoneSprite),
            new ColourMultiplier(renderingColor),
            new TransformationList(translation)};
        if (world != null) {
            renderState.setBrightness(world, pos);
        }
        renderState.setPipeline(operations);
        CCModel actualModel = getActualModel(world, pos);
        renderState.setModel(actualModel);
        renderState.render();
        return true;
    }


    @Override
    public void handleRenderBlockDamage(IBlockAccess world, BlockPos pos, IBlockState state, TextureAtlasSprite sprite, BufferBuilder buffer) {
        CCRenderState renderState = CCRenderState.instance();
        renderState.reset();
        renderState.bind(buffer);
        renderState.setPipeline(new Vector3(new Vec3d(pos)).translation(), new IconTransformation(sprite));
        Cuboid6 baseBox = new Cuboid6(state.getBoundingBox(world, pos));
        BlockRenderer.renderCuboid(renderState, baseBox, 0);
    }

    @Override
    public void renderBrightness(IBlockState state, float brightness) {
        Tessellator tessellator = Tessellator.getInstance();
        tessellator.getBuffer().begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
        renderBlock(null, BlockPos.ORIGIN, state, tessellator.getBuffer());
        tessellator.draw();
    }

    @Override
    public void registerTextures(TextureMap map) {
    }
}

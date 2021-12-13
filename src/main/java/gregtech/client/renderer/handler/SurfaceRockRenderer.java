package gregtech.client.renderer.handler;

import codechicken.lib.raytracer.IndexedCuboid6;
import codechicken.lib.render.BlockRenderer;
import codechicken.lib.render.CCModel;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.block.BlockRenderingRegistry;
import codechicken.lib.render.block.ICCBlockRenderer;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.render.pipeline.attribute.ColourAttribute;
import codechicken.lib.texture.TextureUtils;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import codechicken.lib.vec.TransformationList;
import codechicken.lib.vec.Vector3;
import codechicken.lib.vec.uv.IconTransformation;
import gregtech.api.unification.material.Material;
import gregtech.api.util.GTUtility;
import gregtech.api.util.Position;
import gregtech.api.util.PositionedRect;
import gregtech.api.util.Size;
import gregtech.common.blocks.BlockSurfaceRock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@SideOnly(Side.CLIENT)
public class SurfaceRockRenderer implements ICCBlockRenderer {

    private static final SurfaceRockRenderer INSTANCE = new SurfaceRockRenderer();
    public static EnumBlockRenderType BLOCK_RENDER_TYPE;
    private static final CCModel[] placeholderModels = new CCModel[1];

    public static void preInit() {
        BLOCK_RENDER_TYPE = BlockRenderingRegistry.createRenderType("gt_stone");
        BlockRenderingRegistry.registerRenderer(BLOCK_RENDER_TYPE, INSTANCE);
        Random random = new Random();
        placeholderModels[0] = generateModel(random);
    }

    public static CCModel generateModel(Random random) {
        List<IndexedCuboid6> cuboid6s = generateCuboidList(random);
        CCModel ccModel = CCModel.quadModel(cuboid6s.size() * 24);
        for (int i = 0; i < cuboid6s.size(); i++) {
            IndexedCuboid6 cuboid6 = cuboid6s.get(i);
            ccModel.generateBlock(i * 24, cuboid6);
            int b = (int) cuboid6.data;
            int[] colours = ccModel.getOrAllocate(ColourAttribute.attributeKey);
            int color = (b & 0xFF) << 24 | (b & 0xFF) << 16 | (b & 0xFF) << 8 | (0xFF);
            Arrays.fill(colours, i * 24, i * 24 + 24, color);
        }
        return ccModel.computeNormals();
    }

    private static List<IndexedCuboid6> generateCuboidList(Random random) {
        ArrayList<IndexedCuboid6> result = new ArrayList<>();
        List<PositionedRect> occupiedAreas = new ArrayList<>();
        int stonePlaceAttempts = 64;
        int maxStones = 8;
        int stonesPlaced = 0;
        for (int i = 0; i < stonePlaceAttempts && stonesPlaced < maxStones; i++) {
            int sizeX = 2 + random.nextInt(3);
            int sizeZ = 2 + random.nextInt(3);
            int stoneHeight = 4 + random.nextInt(4);
            int posX = random.nextInt(16 - sizeX);
            int posZ = random.nextInt(16 - sizeZ);
            PositionedRect rect = new PositionedRect(new Position(posX, posZ), new Size(sizeX, sizeZ));
            if (occupiedAreas.stream().noneMatch(rect::intersects)) {
                Vector3 minVector = new Vector3(posX / 16.0, 0 / 16.0, posZ / 16.0);
                Cuboid6 bounds = new Cuboid6(minVector, minVector.copy());
                bounds.max.add(sizeX / 16.0, stoneHeight / 16.0, sizeZ / 16.0);
                int brightness = 100 + random.nextInt(130);
                result.add(new IndexedCuboid6(brightness, bounds));
                occupiedAreas.add(rect);
                stonesPlaced++;
            }
        }
        return result;
    }

    private static CCModel getActualModel(IBlockAccess world, BlockPos pos) {
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
        int renderingColor = GTUtility.convertRGBtoOpaqueRGBA_CL(material.getMaterialRGB());
        IVertexOperation[] operations = new IVertexOperation[]{
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

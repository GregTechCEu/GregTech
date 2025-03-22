package gregtech.client.model.block;

import gregtech.client.renderer.GTRendererState;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.ChunkRenderContainer;
import net.minecraft.client.renderer.EnumFaceDirection;
import net.minecraft.client.renderer.RegionRenderCacheBuilder;
import net.minecraft.client.renderer.block.model.BlockPartRotation;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.block.statemap.BlockStateMapper;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.client.renderer.chunk.ChunkCompileTaskGenerator;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.model.BlockStateLoader;
import net.minecraftforge.client.model.ForgeBlockStateV1;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.ITransformation;

import com.google.gson.Gson;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import java.io.Reader;
import java.util.function.Function;

/**
 * some notes on the mc rendering pipeline
 * <br />
 * <br />
 * Block model jsons are loaded in {@link ModelBakery#loadBlock(BlockStateMapper, Block, ResourceLocation)}}.
 * <br />
 * {@link ModelBakery} is the vanilla impl, override by forge's {@link ModelLoader}
 * <br />
 * the jsons are deserialized by {@link BlockStateLoader#load(Reader, ResourceLocation, Gson)}.
 * <br />
 * if {@code forge_marker = 1}, then a {@link ForgeBlockStateV1} is created
 * <br />
 * Model locations are filled out by {@link IStateMapper StateMappers},
 * which are registered in {@link ModelLoader#setCustomStateMapper(Block, IStateMapper)}
 * <br />
 * There exists {@link ICustomModelLoader}, which could be used to implement custom loaders.
 * it has a method to return an IModel from a ResourceLocation
 * <br />
 * {@link IModel} is what creates {@link IBakedModel BakedModels} in
 * {@link IModel#bake(IModelState, VertexFormat, Function)}
 * <br />
 * <br />
 * that data is then put into a buffer builder and then "rendered" by
 * {@link BlockRendererDispatcher#renderBlock(IBlockState, BlockPos, IBlockAccess, BufferBuilder)}
 * <br />
 * that method is called by {@link RenderChunk#rebuildChunk(float, float, float, ChunkCompileTaskGenerator)}
 * <br />
 * {@link RegionRenderCacheBuilder} stores a {@link BufferBuilder} for each render layer
 * <br />
 * the buffer begins accepting data at {@link RenderChunk#preRenderBlocks(BufferBuilder, BlockPos)},
 * and finishes at
 * {@link RenderChunk#postRenderBlocks(BlockRenderLayer, float, float, float, BufferBuilder, CompiledChunk)}
 * <br />
 * <br />
 * the expected vertex format for blocks is {@link DefaultVertexFormats#BLOCK}
 * <br />
 * each vertex has a Position(3F), Color(4UB), UV(2F and 2S?)
 * <br />
 * i don't know what the 2S UV is used for, maybe lighting/AO information?
 * <br />
 * <br />
 * the actual drawing of vertex data is done in
 * {@link ChunkRenderDispatcher#uploadDisplayList(BufferBuilder, int, RenderChunk)}
 * or if VBO is enabled: {@link ChunkRenderDispatcher#uploadVertexBuffer(BufferBuilder, VertexBuffer)}
 * <br />
 * there's also {@link ChunkRenderContainer}, which has different implementations if VBO is enabled or not
 * <br />
 * <br />
 * A custom block renderer would need to add vertex data to the buffer in the expected format
 * I would need to add methods to {@link gregtech.client.renderer.ICubeRenderer} to move away from CCL
 */
public class GregtechBlockRenderer {

    public static void fillVertexData(int[] vData, int vIndex, EnumFacing side,
                                      GTRendererState.UV uvs, float[] bounds, TextureAtlasSprite sprite,
                                      ITransformation transformation, BlockPartRotation rotation, boolean shade) {
        EnumFacing enumfacing = transformation.rotate(side);
        int i = shade ? getFaceShadeColor(enumfacing) : -1;
        EnumFaceDirection.VertexInformation info = EnumFaceDirection.getFacing(side).getVertexInformation(vIndex);
        Vector3f position = new Vector3f(bounds[info.xIndex], bounds[info.yIndex], bounds[info.zIndex]);
        rotatePart(position, rotation);
        int rIndex = rotateVertex(position, side, vIndex, transformation);
        storeVertexData(vData, rIndex, vIndex, position, i, sprite, uvs);
    }

    private static int getFaceShadeColor(EnumFacing facing) {
        float f = getFaceBrightness(facing);
        int i = (int) (f * 255.0F);
        return -16777216 | i << 16 | i << 8 | i;
    }

    private static float getFaceBrightness(EnumFacing facing) {
        return switch (facing) {
            case DOWN -> 0.5F;
            case UP -> 1.0F;
            case NORTH, SOUTH -> 0.8F;
            case WEST, EAST -> 0.6F;
        };
    }

    private static final float SCALE_ROTATION_22_5 = 1.0F / (float) Math.cos(0.39269909262657166D) - 1.0F;
    private static final float SCALE_ROTATION_GENERAL = 1.0F / (float) Math.cos((Math.PI / 4D)) - 1.0F;

    private static void rotatePart(Vector3f position, @Nullable BlockPartRotation partRotation) {
        if (partRotation != null) {
            Matrix4f matrix4f = getMatrixIdentity();
            Vector3f rotationVector = new Vector3f(0.0F, 0.0F, 0.0F);

            switch (partRotation.axis) {
                case X -> {
                    Matrix4f.rotate(partRotation.angle * 0.017453292F, new Vector3f(1.0F, 0.0F, 0.0F),
                            matrix4f, matrix4f);
                    rotationVector.set(0.0F, 1.0F, 1.0F);
                }
                case Y -> {
                    Matrix4f.rotate(partRotation.angle * 0.017453292F, new Vector3f(0.0F, 1.0F, 0.0F),
                            matrix4f, matrix4f);
                    rotationVector.set(1.0F, 0.0F, 1.0F);
                }
                case Z -> {
                    Matrix4f.rotate(partRotation.angle * 0.017453292F, new Vector3f(0.0F, 0.0F, 1.0F),
                            matrix4f, matrix4f);
                    rotationVector.set(1.0F, 1.0F, 0.0F);
                }
            }

            if (partRotation.rescale) {
                if (Math.abs(partRotation.angle) == 22.5F) {
                    rotationVector.scale(SCALE_ROTATION_22_5);
                } else {
                    rotationVector.scale(SCALE_ROTATION_GENERAL);
                }

                Vector3f.add(rotationVector, new Vector3f(1.0F, 1.0F, 1.0F), rotationVector);
            } else {
                rotationVector.set(1.0F, 1.0F, 1.0F);
            }

            rotateScale(position, new Vector3f(partRotation.origin), matrix4f, rotationVector);
        }
    }

    private static Matrix4f getMatrixIdentity() {
        Matrix4f matrix4f = new Matrix4f();
        matrix4f.setIdentity();
        return matrix4f;
    }

    private static void rotateScale(Vector3f position, Vector3f rotationOrigin, Matrix4f rotationMatrix,
                                    Vector3f scale) {
        Vector4f vector4f = new Vector4f(position.x - rotationOrigin.x, position.y - rotationOrigin.y,
                position.z - rotationOrigin.z, 1.0F);
        Matrix4f.transform(rotationMatrix, vector4f, vector4f);
        vector4f.x *= scale.x;
        vector4f.y *= scale.y;
        vector4f.z *= scale.z;
        position.set(vector4f.x + rotationOrigin.x, vector4f.y + rotationOrigin.y, vector4f.z + rotationOrigin.z);
    }

    private static int rotateVertex(Vector3f p_188011_1_, EnumFacing p_188011_2_, int p_188011_3_,
                                    net.minecraftforge.common.model.ITransformation p_188011_4_) {
        if (p_188011_4_ == ModelRotation.X0_Y0) {
            return p_188011_3_;
        } else {
            net.minecraftforge.client.ForgeHooksClient.transform(p_188011_1_, p_188011_4_.getMatrix());
            return p_188011_4_.rotate(p_188011_2_, p_188011_3_);
        }
    }

    private static void storeVertexData(int[] faceData, int storeIndex, int vertexIndex, Vector3f position,
                                        int shadeColor, TextureAtlasSprite sprite, GTRendererState.UV faceUV) {
        int i = storeIndex * 7;
        faceData[i] = Float.floatToRawIntBits(position.x);
        faceData[i + 1] = Float.floatToRawIntBits(position.y);
        faceData[i + 2] = Float.floatToRawIntBits(position.z);
        faceData[i + 3] = shadeColor;
        faceData[i + 4] = Float.floatToRawIntBits(sprite.getInterpolatedU(faceUV.getVertexU(vertexIndex)));
        faceData[i + 4 + 1] = Float.floatToRawIntBits(sprite.getInterpolatedV(faceUV.getVertexV(vertexIndex)));
    }
}

package gregtech.client.model.block;

import gregtech.api.GTValues;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.ChunkRenderContainer;
import net.minecraft.client.renderer.RegionRenderCacheBuilder;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.client.renderer.chunk.ChunkCompileTaskGenerator;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.model.IModelState;

import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * some notes on the mc rendering pipeline
 * <br />
 * <br />
 * Resources (.jsons) are loaded in {@link ModelLoader#loadBlocks()} and {@link ModelLoader#loadVariantModels()}.
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
 * that data is then put into a buffer builder and then rendered by
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
 */
public class GregtechBlockRenderer implements ICustomModelLoader {

    @Override
    public void onResourceManagerReload(@NotNull IResourceManager resourceManager) {}

    @Override
    public boolean accepts(ResourceLocation modelLocation) {
        return modelLocation.getPath().equals(GTValues.MODID);
    }

    @Override
    public @NotNull IModel loadModel(@NotNull ResourceLocation modelLocation) throws Exception {
        return null;
    }
}

package gregtech.client.renderer;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.texture.TextureUtils.IIconRegister;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.io.IOException;

public interface ICubeRenderer extends IIconRegister {

    String EMISSIVE = "emissive";

    @SideOnly(Side.CLIENT)
    default void render(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        render(renderState, translation, pipeline, Cuboid6.full);
    }

    @SideOnly(Side.CLIENT)
    TextureAtlasSprite getParticleSprite();

    @SideOnly(Side.CLIENT)
    default void render(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, Cuboid6 bounds) {
        for (EnumFacing side : EnumFacing.values()) {
            renderSided(side, bounds, renderState, pipeline, translation);
        }
    }

    @SideOnly(Side.CLIENT)
    default void renderSided(EnumFacing side, CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        renderSided(side, Cuboid6.full, renderState, pipeline, translation);
    }

    @SideOnly(Side.CLIENT)
    default void renderSided(EnumFacing side, Cuboid6 bounds, CCRenderState renderState, IVertexOperation[] pipeline, Matrix4 translation) {
        renderOrientedState(renderState, translation, pipeline, bounds, side, false, false);
    }

    @SideOnly(Side.CLIENT)
    default void renderOriented(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, Cuboid6 bounds, EnumFacing frontFacing) {
        renderOrientedState(renderState, translation, pipeline, bounds, frontFacing, false, false);
    }

    @SideOnly(Side.CLIENT)
    default void renderOriented(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, EnumFacing frontFacing) {
        renderOriented(renderState, translation, pipeline, Cuboid6.full, frontFacing);
    }

    @SideOnly(Side.CLIENT)
    void renderOrientedState(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, Cuboid6 bounds, EnumFacing frontFacing, boolean isActive, boolean isWorkingEnabled);

    @SideOnly(Side.CLIENT)
    default void renderOrientedState(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, EnumFacing frontFacing, boolean isActive, boolean isWorkingEnabled) {
        renderOrientedState(renderState, translation, pipeline, Cuboid6.full, frontFacing, isActive, isWorkingEnabled);
    }

    /**
     * @param manager  the ResourceManager to retrieve resources from
     * @param location the location of the resource
     * @return if the resource exists
     */
    @SideOnly(Side.CLIENT)
    static boolean resourceExists(@Nonnull IResourceManager manager, @Nonnull ResourceLocation location) {
        try (IResource ignored1 = manager.getResource(location)) {
            return true;
        } catch (IOException ignored) {
            return false;
        }
    }
}

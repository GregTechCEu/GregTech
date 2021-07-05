package gregtech.api.metatileentity;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.GTValues;
import gregtech.api.render.SimpleOverlayRenderer;
import gregtech.api.render.SimpleSidedCubeRenderer;
import gregtech.api.render.Textures;
import gregtech.common.metatileentities.traits.TraitInfiniteEnergy;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public abstract class InfiniteEnergyTileEntityBase<TRAIT extends TraitInfiniteEnergy> extends MetaTileEntity {
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");

    protected final TRAIT trait;

    public InfiniteEnergyTileEntityBase(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
        trait = createTrait();
    }

    protected abstract TRAIT createTrait();

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        Textures.VOLTAGE_CASINGS[GTValues.MAX].render(renderState, translation, pipeline);
        for (EnumFacing facing : EnumFacing.VALUES)
            getOverlay().renderSided(facing, renderState, translation, pipeline);
    }

    @Override
    public boolean onWrenchClick(EntityPlayer player, EnumHand hand, EnumFacing wrenchSide, CuboidRayTraceResult hitResult) {
        if (!player.isCreative() || player.isSneaking())
            return super.onWrenchClick(player, hand, wrenchSide, hitResult);
        return true;
    }

    @Override
    public void update() {
        super.update();
    }


    @SideOnly(Side.CLIENT)
    protected abstract SimpleOverlayRenderer getOverlay();

    @Override
    @SideOnly(Side.CLIENT)
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(Textures.VOLTAGE_CASINGS[GTValues.MAX].getSpriteOnSide(SimpleSidedCubeRenderer.RenderSide.TOP), 0xFFFFFF);
    }
}

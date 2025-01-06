package gregtech.client.renderer.pipe;

import gregtech.api.graphnet.pipenet.physical.block.PipeBlock;
import gregtech.api.graphnet.pipenet.physical.block.PipeMaterialBlock;
import gregtech.api.unification.material.Material;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.pipe.cache.BlockableSQC;
import gregtech.client.renderer.pipe.cache.RestrictiveSQC;
import gregtech.client.renderer.pipe.cache.StructureQuadCache;
import gregtech.client.renderer.pipe.quad.ColorData;
import gregtech.client.renderer.pipe.quad.PipeQuadHelper;
import gregtech.client.renderer.pipe.util.CacheKey;
import gregtech.client.renderer.pipe.util.SpriteInformation;
import gregtech.client.renderer.texture.Textures;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

@SideOnly(Side.CLIENT)
public class PipeModel extends AbstractPipeModel<CacheKey> {

    protected final @NotNull Supplier<SpriteInformation> inTex;
    protected final @NotNull Supplier<SpriteInformation> sideTex;
    protected final @Nullable Supplier<SpriteInformation> restrictiveTex;
    protected final @NotNull Supplier<SpriteInformation> blockedTex;

    public PipeModel(@NotNull Supplier<SpriteInformation> inTex, @NotNull Supplier<SpriteInformation> sideTex,
                     @Nullable Supplier<SpriteInformation> restrictiveTex,
                     @NotNull Supplier<SpriteInformation> blockedTex) {
        this.inTex = inTex;
        this.sideTex = sideTex;
        this.restrictiveTex = restrictiveTex;
        this.blockedTex = blockedTex;
    }

    public PipeModel(@NotNull Supplier<SpriteInformation> inTex, @NotNull Supplier<SpriteInformation> sideTex,
                     boolean restrictive) {
        this(inTex, sideTex, restrictive ? Textures.RESTRICTIVE_OVERLAY : null, Textures.PIPE_BLOCKED_OVERLAY);
    }

    @Override
    public SpriteInformation getParticleSprite(@Nullable Material material) {
        return sideTex.get();
    }

    @Override
    protected @NotNull CacheKey toKey(@NotNull IExtendedBlockState state) {
        return defaultKey(state);
    }

    @Override
    protected StructureQuadCache constructForKey(CacheKey key) {
        if (restrictiveTex != null) {
            return RestrictiveSQC.create(PipeQuadHelper.create(key.getThickness()), inTex.get(), sideTex.get(),
                    blockedTex.get(), restrictiveTex.get());
        } else {
            return BlockableSQC.create(PipeQuadHelper.create(key.getThickness()), inTex.get(), sideTex.get(),
                    blockedTex.get());
        }
    }

    @Override
    @Nullable
    protected PipeItemModel<CacheKey> getItemModel(PipeModelRedirector redirector, @NotNull ItemStack stack,
                                                   World world, EntityLivingBase entity) {
        PipeBlock block = PipeBlock.getBlockFromItem(stack);
        if (block == null) return null;
        Material mater = block instanceof PipeMaterialBlock mat ? mat.getMaterialForStack(stack) : null;
        return new PipeItemModel<>(redirector, this, new CacheKey(block.getStructure().getRenderThickness()),
                mater != null ? new ColorData(GTUtility.convertRGBtoARGB(mater.getMaterialRGB())) :
                        ColorData.PLAIN);
    }
}

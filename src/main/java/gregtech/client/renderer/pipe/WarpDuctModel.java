package gregtech.client.renderer.pipe;

import gregtech.api.graphnet.pipenet.physical.block.PipeBlock;
import gregtech.client.renderer.pipe.cache.ExtraBlockableCoreRestrictiveSQC;
import gregtech.client.renderer.pipe.cache.ExtraBlockableSQC;
import gregtech.client.renderer.pipe.cache.StructureQuadCache;
import gregtech.client.renderer.pipe.quad.ColorData;
import gregtech.client.renderer.pipe.quad.PipeQuadHelper;
import gregtech.client.renderer.pipe.util.CacheKey;
import gregtech.client.renderer.pipe.util.SpriteInformation;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class WarpDuctModel extends PipeModel {

    protected final @NotNull Supplier<SpriteInformation> blockedInTex;

    public WarpDuctModel(@NotNull Supplier<SpriteInformation> inTex, @NotNull Supplier<SpriteInformation> sideTex,
                         @Nullable Supplier<SpriteInformation> restrictiveTex,
                         @NotNull Supplier<SpriteInformation> blockedInTex,
                         @NotNull Supplier<SpriteInformation> blockedSideTex) {
        super(inTex, sideTex, restrictiveTex, blockedSideTex);
        this.blockedInTex = blockedInTex;
    }

    @Override
    protected StructureQuadCache constructForKey(CacheKey key) {
        if (restrictiveTex != null) {
            return ExtraBlockableCoreRestrictiveSQC.create(PipeQuadHelper.create(key.getThickness()), inTex.get(),
                    sideTex.get(),
                    blockedTex.get(), blockedInTex.get(), restrictiveTex.get());
        } else {
            return ExtraBlockableSQC.create(PipeQuadHelper.create(key.getThickness()), inTex.get(), sideTex.get(),
                    blockedTex.get(), blockedInTex.get());
        }
    }

    @Override
    protected @Nullable PipeItemModel<CacheKey> getItemModel(PipeModelRedirector redirector,
                                                             @NotNull ItemStack stack, World world,
                                                             EntityLivingBase entity) {
        PipeBlock block = PipeBlock.getBlockFromItem(stack);
        if (block == null) return null;
        return new PipeItemModel<>(redirector, this,
                new CacheKey(block.getStructure().getRenderThickness()),
                ColorData.PLAIN);
    }
}

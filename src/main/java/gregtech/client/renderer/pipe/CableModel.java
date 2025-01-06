package gregtech.client.renderer.pipe;

import gregtech.api.graphnet.pipenet.physical.block.PipeBlock;
import gregtech.api.graphnet.pipenet.physical.block.PipeMaterialBlock;
import gregtech.api.graphnet.pipenet.physical.tile.PipeTileEntity;
import gregtech.api.unification.material.Material;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.pipe.cache.ExtraCappedSQC;
import gregtech.client.renderer.pipe.cache.StandardSQC;
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
public class CableModel extends AbstractPipeModel<CacheKey> {

    public static final int DEFAULT_INSULATION_COLOR = 0xFF404040;

    public static final CableModel INSTANCE = new CableModel();
    public static final CableModel[] INSULATED_INSTANCES = new CableModel[Textures.INSULATION.length];

    static {
        for (int i = 0; i < INSULATED_INSTANCES.length; i++) {
            INSULATED_INSTANCES[i] = new CableModel(Textures.INSULATION[i], Textures.INSULATION_FULL);
        }
    }

    private final Supplier<SpriteInformation> wireTex;
    private final Supplier<SpriteInformation> insulationTex;
    private final Supplier<SpriteInformation> fullInsulationTex;

    public CableModel(@NotNull Supplier<SpriteInformation> wireTex, @Nullable Supplier<SpriteInformation> insulationTex,
                      @Nullable Supplier<SpriteInformation> fullInsulationTex) {
        this.wireTex = wireTex;
        this.insulationTex = insulationTex;
        this.fullInsulationTex = fullInsulationTex;
    }

    public CableModel(@Nullable Supplier<SpriteInformation> insulationTex,
                      @Nullable Supplier<SpriteInformation> fullInsulationTex) {
        this(Textures.WIRE, insulationTex, fullInsulationTex);
    }

    public CableModel() {
        this(null, null);
    }

    @Override
    protected ColorData computeColorData(@NotNull IExtendedBlockState ext) {
        if (insulationTex == null) return super.computeColorData(ext);
        Material material = ext.getValue(AbstractPipeModel.MATERIAL_PROPERTY);
        int insulationColor = safeInt(ext.getValue(COLOR_PROPERTY));
        if (material != null) {
            int matColor = GTUtility.convertRGBtoARGB(material.getMaterialRGB());
            if (insulationColor == 0 || insulationColor == matColor) {
                // unpainted
                insulationColor = DEFAULT_INSULATION_COLOR;
            }
            return new ColorData(matColor, insulationColor);
        }
        return new ColorData(0, 0);
    }

    @Override
    public SpriteInformation getParticleSprite(@Nullable Material material) {
        return wireTex.get();
    }

    @Override
    protected @NotNull CacheKey toKey(@NotNull IExtendedBlockState state) {
        return defaultKey(state);
    }

    @Override
    protected StructureQuadCache constructForKey(CacheKey key) {
        SpriteInformation sideTex = fullInsulationTex != null ? fullInsulationTex.get() : wireTex.get();
        if (insulationTex == null) {
            return StandardSQC.create(PipeQuadHelper.create(key.getThickness()), wireTex.get(), sideTex);
        } else {
            return ExtraCappedSQC.create(PipeQuadHelper.create(key.getThickness()), wireTex.get(), sideTex,
                    insulationTex.get());
        }
    }

    @Override
    protected @Nullable PipeItemModel<CacheKey> getItemModel(PipeModelRedirector redirector, @NotNull ItemStack stack,
                                                             World world, EntityLivingBase entity) {
        PipeBlock block = PipeBlock.getBlockFromItem(stack);
        if (block == null) return null;
        Material mater = block instanceof PipeMaterialBlock mat ? mat.getMaterialForStack(stack) : null;
        return new PipeItemModel<>(redirector, this, new CacheKey(block.getStructure().getRenderThickness()),
                new ColorData(mater != null ? GTUtility.convertRGBtoARGB(mater.getMaterialRGB()) :
                        PipeTileEntity.DEFAULT_COLOR, DEFAULT_INSULATION_COLOR));
    }
}

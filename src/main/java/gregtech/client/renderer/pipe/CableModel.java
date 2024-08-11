package gregtech.client.renderer.pipe;

import gregtech.api.graphnet.pipenet.physical.block.PipeBlock;
import gregtech.api.graphnet.pipenet.physical.block.PipeMaterialBlock;
import gregtech.api.graphnet.pipenet.physical.tile.PipeTileEntity;
import gregtech.api.unification.material.Material;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.pipe.cache.ExtraCappedSQC;
import gregtech.client.renderer.pipe.cache.StructureQuadCache;
import gregtech.client.renderer.pipe.quad.ColorData;
import gregtech.client.renderer.pipe.quad.PipeQuadHelper;
import gregtech.client.renderer.pipe.util.CacheKey;
import gregtech.client.renderer.pipe.util.SpriteInformation;
import gregtech.client.renderer.texture.Textures;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.IRegistry;
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

    private static final ResourceLocation loc = GTUtility.gregtechId("block/cable");

    public static final CableModel INSTANCE = new CableModel("wire");
    public static final CableModel[] INSULATED_INSTANCES = new CableModel[Textures.INSULATION.length];

    static {
        for (int i = 0; i < INSULATED_INSTANCES.length; i++) {
            INSULATED_INSTANCES[i] = new CableModel(Textures.INSULATION[i], Textures.INSULATION_FULL, "insulated_" + i);
        }
    }

    private final Supplier<SpriteInformation> wireTex;
    private final Supplier<SpriteInformation> insulationTex;
    private final Supplier<SpriteInformation> fullInsulationTex;

    public CableModel(@NotNull Supplier<SpriteInformation> wireTex, @Nullable Supplier<SpriteInformation> insulationTex,
                      @Nullable Supplier<SpriteInformation> fullInsulationTex, String variant) {
        super(new ModelResourceLocation(loc, variant));
        this.wireTex = wireTex;
        this.insulationTex = insulationTex;
        this.fullInsulationTex = fullInsulationTex;
    }

    public CableModel(@Nullable Supplier<SpriteInformation> insulationTex,
                      @Nullable Supplier<SpriteInformation> fullInsulationTex, String variant) {
        this(Textures.WIRE, insulationTex, fullInsulationTex, variant);
    }

    public CableModel(String variant) {
        this(null, null, variant);
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
    public @NotNull TextureAtlasSprite getParticleTexture() {
        return getParticleSprite(null).sprite();
    }

    @Override
    protected @NotNull CacheKey toKey(@NotNull IExtendedBlockState state) {
        return defaultKey(state);
    }

    @Override
    protected StructureQuadCache constructForKey(CacheKey key) {
        SpriteInformation sideTex = fullInsulationTex != null ? fullInsulationTex.get() : wireTex.get();
        if (insulationTex == null) {
            return StructureQuadCache.create(PipeQuadHelper.create(key.getThickness()), wireTex.get(), sideTex);
        } else {
            return ExtraCappedSQC.create(PipeQuadHelper.create(key.getThickness()), wireTex.get(), sideTex,
                    insulationTex.get());
        }
    }

    @Override
    protected @Nullable PipeItemModel<CacheKey> getItemModel(@NotNull ItemStack stack, World world,
                                                             EntityLivingBase entity) {
        PipeBlock block = PipeBlock.getBlockFromItem(stack);
        if (block == null) return null;
        Material mater = block instanceof PipeMaterialBlock mat ? mat.getMaterialForStack(stack) : null;
        return new PipeItemModel<>(this, new CacheKey(block.getStructure().getRenderThickness()),
                new ColorData(mater != null ? GTUtility.convertRGBtoARGB(mater.getMaterialRGB()) :
                        PipeTileEntity.DEFAULT_COLOR, DEFAULT_INSULATION_COLOR));
    }

    public static void registerModels(IRegistry<ModelResourceLocation, IBakedModel> registry) {
        registry.putObject(INSTANCE.getLoc(), INSTANCE);
        for (CableModel model : INSULATED_INSTANCES) {
            registry.putObject(model.getLoc(), model);
        }
    }
}

package gregtech.client.model;

import gregtech.api.GTValues;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.info.MaterialIconSet;
import gregtech.api.unification.material.info.MaterialIconType;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.StoneType;
import gregtech.api.util.GTUtility;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Mod.EventBusSubscriber(modid = GTValues.MODID, value = Side.CLIENT)
public class OreBakedModel implements IBakedModel {

    private static final Map<Entry, ModelResourceLocation> ENTRIES = new Object2ObjectOpenHashMap<>();

    public static ModelResourceLocation registerOreEntry(@NotNull StoneType stoneType, @NotNull Material material,
                                                         boolean isSmallOre) {
        return ENTRIES.computeIfAbsent(
                new Entry(stoneType, material.getMaterialIconSet(), material.getProperty(PropertyKey.ORE).isEmissive(), isSmallOre),
                Entry::getModelId);
    }

    protected final StoneType stoneType;
    protected final IBakedModel overlay;

    private IBakedModel baseModel;

    public OreBakedModel(StoneType stoneType, IBakedModel overlay) {
        this.stoneType = stoneType;
        this.overlay = overlay;
    }

    protected IBakedModel getBaseModel() {
        if (this.baseModel == null) {
            return this.baseModel = Minecraft.getMinecraft().blockRenderDispatcher
                    .getModelForState(this.stoneType.stone.get());
        }
        return this.baseModel;
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        // a way to guarantee one variant on random models with arbitrary entries.
        // this essentially prevents z-fighting issues as long as the first model defined in weighted baked model
        // does not have any rotation applied.
        List<BakedQuad> quads = new ArrayList<>(getBaseModel().getQuads(null, side, 0));
        quads.addAll(this.overlay.getQuads(null, side, rand));
        return quads;
    }

    @Override
    public boolean isAmbientOcclusion() {
        return getBaseModel().isAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return getBaseModel().isGui3d();
    }

    @Override
    public boolean isBuiltInRenderer() {
        return false;
    }

    @Override
    public @NotNull TextureAtlasSprite getParticleTexture() {
        return getBaseModel().getParticleTexture();
    }

    @Override
    public @NotNull ItemOverrideList getOverrides() {
        return ItemOverrideList.NONE;
    }

    @SuppressWarnings("deprecation")
    @Override
    public @NotNull ItemCameraTransforms getItemCameraTransforms() {
        return getBaseModel().getItemCameraTransforms();
    }

    @Override
    public boolean isAmbientOcclusion(@NotNull IBlockState state) {
        return getBaseModel().isAmbientOcclusion(state);
    }

    @SubscribeEvent
    public static void onTextureStitch(TextureStitchEvent.Pre event) {
        for (Map.Entry<Entry, ModelResourceLocation> e : ENTRIES.entrySet()) {
            Entry entry = e.getKey();
            MaterialIconType iconType = entry.isSmall ? MaterialIconType.oreSmall : MaterialIconType.ore;
            event.getMap().registerSprite(iconType.getBlockTexturePath(entry.iconSet));
        }
    }

    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent event) {
        Map<ResourceLocation, IBakedModel> overlayCache = new Object2ObjectOpenHashMap<>();

        for (Map.Entry<Entry, ModelResourceLocation> e : ENTRIES.entrySet()) {
            Entry entry = e.getKey();
            MaterialIconType iconType = entry.isSmall ? MaterialIconType.oreSmall : MaterialIconType.ore;
            IBakedModel overlay = overlayCache.computeIfAbsent(iconType.getBlockTexturePath(entry.iconSet),
                    tex -> new ModelFactory(ModelFactory.ModelTemplate.ORE_OVERLAY)
                            .addSprite("texture", tex)
                            .bake());
            event.getModelRegistry().putObject(e.getValue(), entry.emissive ?
                    new EmissiveOreBakedModel(entry.stoneType, overlay) :
                    new OreBakedModel(entry.stoneType, overlay));
        }
    }

    private static final class Entry {

        final StoneType stoneType;
        final MaterialIconSet iconSet;
        final boolean emissive;
        final boolean isSmall;

        private final int hash;

        private Entry(StoneType stoneType, MaterialIconSet iconSet, boolean emissive, boolean isSmall) {
            this.stoneType = stoneType;
            this.iconSet = iconSet;
            this.emissive = emissive;
            this.isSmall = isSmall;

            this.hash = Objects.hash(stoneType.name, iconSet.name, emissive, isSmall);
        }

        public ModelResourceLocation getModelId() {
            return new ModelResourceLocation(GTUtility.gregtechId((this.isSmall ? "ore_small_" : "ore_") +
                    this.stoneType.name + "_" + this.iconSet.name + (this.emissive ? "_emissive" : "")), "");
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Entry entry = (Entry) o;
            return this.stoneType.name.equals(entry.stoneType.name) &&
                    this.iconSet.name.equals(entry.iconSet.name) &&
                    this.emissive == entry.emissive &&
                    this.isSmall == entry.isSmall;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public String toString() {
            return "stoneType=" + stoneType.name + ", iconSet=" + iconSet.name + ", emissive=" + emissive;
        }
    }
}

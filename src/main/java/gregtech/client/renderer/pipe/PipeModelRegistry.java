package gregtech.client.renderer.pipe;

import gregtech.api.graphnet.pipenet.physical.block.PipeMaterialBlock;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.pipe.util.MaterialModelOverride;
import gregtech.client.renderer.pipe.util.MaterialModelSupplier;
import gregtech.client.renderer.texture.Textures;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.IRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

@SideOnly(Side.CLIENT)
public final class PipeModelRegistry {

    public static final int PIPE_MODEL_COUNT = 7;
    private static final Object2ObjectOpenHashMap<Material, PipeModel[]> PIPE = new Object2ObjectOpenHashMap<>();
    private static final PipeModelRedirector[] PIPE_MODELS = new PipeModelRedirector[PIPE_MODEL_COUNT];
    private static final ObjectLinkedOpenHashSet<MaterialModelOverride<PipeModel>> PIPE_OVERRIDES = new ObjectLinkedOpenHashSet<>();
    private static final Object2ObjectOpenHashMap<Material, PipeModel[]> PIPE_RESTRICTIVE = new Object2ObjectOpenHashMap<>();
    private static final PipeModelRedirector[] PIPE_RESTRICTIVE_MODELS = new PipeModelRedirector[PIPE_MODEL_COUNT];
    private static final ObjectLinkedOpenHashSet<MaterialModelOverride<PipeModel>> PIPE_RESTRICTIVE_OVERRIDES = new ObjectLinkedOpenHashSet<>();

    public static final int CABLE_MODEL_COUNT = 6;
    private static final Object2ObjectOpenHashMap<Material, CableModel[]> CABLE = new Object2ObjectOpenHashMap<>();
    private static final PipeModelRedirector[] CABLE_MODELS = new PipeModelRedirector[CABLE_MODEL_COUNT];
    private static final ObjectLinkedOpenHashSet<MaterialModelOverride<CableModel>> CABLE_OVERRIDES = new ObjectLinkedOpenHashSet<>();

    private static final ActivablePipeModel OPTICAL;
    private static final PipeModelRedirector OPTICAL_MODEL;

    private static final ActivablePipeModel LASER;
    private static final PipeModelRedirector LASER_MODEL;

    static {
        initPipes();
        initCables();
        ResourceLocation loc = GTUtility.gregtechId("block/pipe_activable");
        OPTICAL = new ActivablePipeModel(Textures.OPTICAL_PIPE_IN, Textures.OPTICAL_PIPE_SIDE,
                Textures.OPTICAL_PIPE_SIDE_OVERLAY, Textures.OPTICAL_PIPE_SIDE_OVERLAY_ACTIVE, false);
        OPTICAL_MODEL = new PipeModelRedirector(new ModelResourceLocation(loc, "optical"), m -> OPTICAL, s -> null);
        LASER = new ActivablePipeModel(Textures.LASER_PIPE_IN, Textures.LASER_PIPE_SIDE, Textures.LASER_PIPE_OVERLAY,
                Textures.LASER_PIPE_OVERLAY_EMISSIVE, true);
        LASER_MODEL = new PipeModelRedirector(new ModelResourceLocation(loc, "laser"), m -> LASER, s -> null);
    }

    public static void registerPipeOverride(@NotNull MaterialModelOverride<PipeModel> override) {
        PIPE_OVERRIDES.addAndMoveToFirst(override);
        PIPE.clear();
        PIPE.trim(16);
    }

    public static void registerPipeRestrictiveOverride(@NotNull MaterialModelOverride<PipeModel> override) {
        PIPE_RESTRICTIVE_OVERRIDES.addAndMoveToFirst(override);
        PIPE_RESTRICTIVE.clear();
        PIPE_RESTRICTIVE.trim(16);
    }

    public static void registerCableOverride(@NotNull MaterialModelOverride<CableModel> override) {
        CABLE_OVERRIDES.addAndMoveToFirst(override);
        CABLE.clear();
        CABLE.trim(16);
    }

    public static PipeModelRedirector getPipeModel(@Range(from = 0, to = PIPE_MODEL_COUNT - 1) int i) {
        return PIPE_MODELS[i];
    }

    public static PipeModelRedirector getPipeRestrictiveModel(@Range(from = 0, to = PIPE_MODEL_COUNT - 1) int i) {
        return PIPE_RESTRICTIVE_MODELS[i];
    }

    public static PipeModelRedirector getCableModel(@Range(from = 0, to = CABLE_MODEL_COUNT - 1) int i) {
        return CABLE_MODELS[i];
    }

    public static PipeModelRedirector getOpticalModel() {
        return OPTICAL_MODEL;
    }

    public static PipeModelRedirector getLaserModel() {
        return LASER_MODEL;
    }

    public static void registerModels(@NotNull IRegistry<ModelResourceLocation, IBakedModel> registry) {
        for (PipeModelRedirector redirector : PIPE_MODELS) {
            registry.putObject(redirector.getLoc(), redirector);
        }
        for (PipeModelRedirector redirector : PIPE_RESTRICTIVE_MODELS) {
            registry.putObject(redirector.getLoc(), redirector);
        }
        for (PipeModelRedirector redirector : CABLE_MODELS) {
            registry.putObject(redirector.getLoc(), redirector);
        }
        registry.putObject(OPTICAL_MODEL.getLoc(), OPTICAL_MODEL);
        registry.putObject(LASER_MODEL.getLoc(), LASER_MODEL);
    }

    public static PipeModelRedirector materialModel(@NotNull ResourceLocation loc, MaterialModelSupplier supplier,
                                                    @NotNull String variant,
                                                    PipeModelRedirector.@NotNull Supplier redirectorSupplier) {
        return redirectorSupplier.create(new ModelResourceLocation(loc, variant), supplier,
                stack -> {
                    PipeMaterialBlock pipe = PipeMaterialBlock.getBlockFromItem(stack);
                    if (pipe == null) return null;
                    else return pipe.getMaterialForStack(stack);
                });
    }

    public static PipeModelRedirector materialModel(@NotNull ResourceLocation loc, MaterialModelSupplier supplier,
                                                    @NotNull String variant) {
        return new PipeModelRedirector(new ModelResourceLocation(loc, variant), supplier,
                stack -> {
                    PipeMaterialBlock pipe = PipeMaterialBlock.getBlockFromItem(stack);
                    if (pipe == null) return null;
                    else return pipe.getMaterialForStack(stack);
                });
    }

    private static void initPipes() {
        PipeModel[] array = new PipeModel[PIPE_MODEL_COUNT];
        // standard
        array[0] = new PipeModel(Textures.PIPE_TINY, Textures.PIPE_SIDE, false);
        array[1] = new PipeModel(Textures.PIPE_SMALL, Textures.PIPE_SIDE, false);
        array[2] = new PipeModel(Textures.PIPE_NORMAL, Textures.PIPE_SIDE, false);
        array[3] = new PipeModel(Textures.PIPE_LARGE, Textures.PIPE_SIDE, false);
        array[4] = new PipeModel(Textures.PIPE_HUGE, Textures.PIPE_SIDE, false);
        array[5] = new PipeModel(Textures.PIPE_QUADRUPLE, Textures.PIPE_SIDE, false);
        array[6] = new PipeModel(Textures.PIPE_NONUPLE, Textures.PIPE_SIDE, false);
        PIPE_OVERRIDES.add(new MaterialModelOverride.StandardOverride<>(array, m -> true));

        array = new PipeModel[PIPE_MODEL_COUNT];
        array[1] = new PipeModel(Textures.PIPE_SMALL_WOOD, Textures.PIPE_SIDE_WOOD, false);
        array[2] = new PipeModel(Textures.PIPE_NORMAL_WOOD, Textures.PIPE_SIDE_WOOD, false);
        array[3] = new PipeModel(Textures.PIPE_LARGE_WOOD, Textures.PIPE_SIDE_WOOD, false);
        registerPipeOverride(new MaterialModelOverride.StandardOverride<>(array, m -> m.hasProperty(PropertyKey.WOOD)));

        array = new PipeModel[PIPE_MODEL_COUNT];
        array[0] = new PipeModel(Textures.PIPE_TINY, Textures.PIPE_SIDE, true);
        array[1] = new PipeModel(Textures.PIPE_SMALL, Textures.PIPE_SIDE, true);
        array[2] = new PipeModel(Textures.PIPE_NORMAL, Textures.PIPE_SIDE, true);
        array[3] = new PipeModel(Textures.PIPE_LARGE, Textures.PIPE_SIDE, true);
        array[4] = new PipeModel(Textures.PIPE_HUGE, Textures.PIPE_SIDE, true);
        array[5] = new PipeModel(Textures.PIPE_QUADRUPLE, Textures.PIPE_SIDE, true);
        array[6] = new PipeModel(Textures.PIPE_NONUPLE, Textures.PIPE_SIDE, true);
        PIPE_RESTRICTIVE_OVERRIDES.add(new MaterialModelOverride.StandardOverride<>(array, m -> true));

        ResourceLocation loc = GTUtility.gregtechId("block/pipe_material");
        for (int i = 0; i < PIPE_MODEL_COUNT; i++) {
            int finalI = i;
            PIPE_MODELS[i] = materialModel(loc, m -> getOrCachePipeModel(m, finalI), String.valueOf(i));
            PIPE_RESTRICTIVE_MODELS[i] = materialModel(loc, m -> getOrCachePipeRestrictiveModel(m, finalI),
                    "restrictive_" + i);
        }
    }

    private static PipeModel getOrCachePipeModel(Material m, int i) {
        if (m == null) return PIPE_OVERRIDES.last().getModel(null, i);
        PipeModel[] cached = PIPE.computeIfAbsent(m, k -> new PipeModel[PIPE_MODEL_COUNT]);
        PipeModel selected = cached[i];
        if (selected == null) {
            for (MaterialModelOverride<PipeModel> override : PIPE_OVERRIDES) {
                selected = override.getModel(m, i);
                if (selected != null) break;
            }
            cached[i] = selected;
        }
        return selected;
    }

    private static PipeModel getOrCachePipeRestrictiveModel(Material m, int i) {
        if (m == null) return PIPE_RESTRICTIVE_OVERRIDES.last().getModel(null, i);
        PipeModel[] cached = PIPE_RESTRICTIVE.computeIfAbsent(m, k -> new PipeModel[PIPE_MODEL_COUNT]);
        PipeModel selected = cached[i];
        if (selected == null) {
            for (MaterialModelOverride<PipeModel> override : PIPE_RESTRICTIVE_OVERRIDES) {
                selected = override.getModel(m, i);
                if (selected != null) break;
            }
            cached[i] = selected;
        }
        return selected;
    }

    private static void initCables() {
        CableModel[] array = new CableModel[CABLE_MODEL_COUNT];
        for (int i = 0; i < CABLE_MODEL_COUNT; i++) {
            if (i == 0) {
                array[i] = new CableModel();
                continue;
            }
            array[i] = new CableModel(Textures.INSULATION[i - 1], Textures.INSULATION_FULL);
        }
        CABLE_OVERRIDES.add(new MaterialModelOverride.StandardOverride<>(array, m -> true));

        ResourceLocation loc = GTUtility.gregtechId("block/cable");
        for (int i = 0; i < CABLE_MODEL_COUNT; i++) {
            int finalI = i;
            CABLE_MODELS[i] = materialModel(loc, m -> getOrCacheCableModel(m, finalI), String.valueOf(i));
        }
    }

    private static CableModel getOrCacheCableModel(Material m, int i) {
        if (m == null) return CABLE_OVERRIDES.last().getModel(null, i);
        CableModel[] cached = CABLE.computeIfAbsent(m, k -> new CableModel[CABLE_MODEL_COUNT]);
        CableModel selected = cached[i];
        if (selected == null) {
            for (MaterialModelOverride<CableModel> override : CABLE_OVERRIDES) {
                selected = override.getModel(m, i);
                if (selected != null) break;
            }
            cached[i] = selected;
        }
        return selected;
    }
}

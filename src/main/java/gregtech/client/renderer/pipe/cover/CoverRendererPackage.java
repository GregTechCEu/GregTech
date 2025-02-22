package gregtech.client.renderer.pipe.cover;

import gregtech.client.renderer.pipe.quad.ColorData;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.property.IUnlistedProperty;

import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;

public final class CoverRendererPackage {

    public static final CoverRendererPackage EMPTY = new CoverRendererPackage(false);
    public static final UnlistedCRPProperty CRP_PROPERTY = new UnlistedCRPProperty("CRP");

    private final EnumMap<EnumFacing, CoverRenderer> renderers = new EnumMap<>(EnumFacing.class);
    private final EnumSet<EnumFacing> plates = EnumSet.allOf(EnumFacing.class);

    private final boolean renderBackside;

    public CoverRendererPackage(boolean renderBackside) {
        this.renderBackside = renderBackside;
    }

    public void addRenderer(CoverRenderer renderer, @NotNull EnumFacing facing) {
        renderers.put(facing, renderer);
        plates.remove(facing);
    }

    public void addQuads(List<BakedQuad> quads, BlockRenderLayer renderLayer, ColorData data) {
        for (var renderer : renderers.entrySet()) {
            EnumSet<EnumFacing> plates = EnumSet.copyOf(this.plates);
            // force front and back plates to render
            plates.add(renderer.getKey());
            plates.add(renderer.getKey().getOpposite());
            renderer.getValue().addQuads(quads, renderer.getKey(), plates, renderBackside, renderLayer, data);
        }
    }

    public byte getMask() {
        byte mask = 0;
        for (EnumFacing facing : renderers.keySet()) {
            mask |= 1 << facing.ordinal();
        }
        return mask;
    }

    public static class UnlistedCRPProperty implements IUnlistedProperty<CoverRendererPackage> {

        private final String name;

        public UnlistedCRPProperty(@NotNull String name) {
            this.name = name;
        }

        @NotNull
        @Override
        public String getName() {
            return name;
        }

        @Override
        public boolean isValid(CoverRendererPackage value) {
            return true;
        }

        @Override
        public Class<CoverRendererPackage> getType() {
            return CoverRendererPackage.class;
        }

        @Override
        public String valueToString(CoverRendererPackage value) {
            return value.toString();
        }
    }
}

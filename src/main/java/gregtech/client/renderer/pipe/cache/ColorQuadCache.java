package gregtech.client.renderer.pipe.cache;

import gregtech.api.util.reference.RegeneratingSoftReference;
import gregtech.client.renderer.pipe.quad.ColorData;
import gregtech.client.renderer.pipe.quad.RecolorableBakedQuad;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.List;
import java.util.Map;

@SideOnly(Side.CLIENT)
public final class ColorQuadCache {

    private final List<RecolorableBakedQuad> prototypes;

    private final Object2ObjectLinkedOpenHashMap<ColorData, List<BakedQuad>> cache;


    public ColorQuadCache(List<RecolorableBakedQuad> prototypes) {
        this.prototypes = prototypes;
        this.cache = new Object2ObjectLinkedOpenHashMap<>();
    }

    public List<BakedQuad> getQuads(ColorData data) {
        List<BakedQuad> existing = cache.getAndMoveToFirst(data);
        if (existing == null) {
            existing = new ObjectArrayList<>();
            for (RecolorableBakedQuad quad : prototypes) {
                existing.add(quad.withColor(data));
            }
            cache.put(data, existing);
            if (cache.size() > 20) cache.removeLast();
        }
        return existing;
    }
}

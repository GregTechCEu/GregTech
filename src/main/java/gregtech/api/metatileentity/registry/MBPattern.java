package gregtech.api.metatileentity.registry;

import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.client.renderer.scene.VBOWorldSceneRenderer;
import gregtech.client.renderer.scene.WorldSceneRenderer;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.Map;

public class MBPattern {

    final VBOWorldSceneRenderer sceneRenderer;
    final List<ItemStack> parts;
    final Map<BlockPos, TraceabilityPredicate> predicateMap;

    public MBPattern(final VBOWorldSceneRenderer sceneRenderer, final List<ItemStack> parts,
                     Map<BlockPos, TraceabilityPredicate> predicateMap) {
        this.sceneRenderer = sceneRenderer;
        this.parts = parts;
        this.predicateMap = predicateMap;
    }

    public List<ItemStack> getParts() {
        return parts;
    }
    public VBOWorldSceneRenderer getSceneRenderer() {
        return sceneRenderer;
    }
    public Map<BlockPos, TraceabilityPredicate> getPredicateMap() {
        return predicateMap;
    }
}

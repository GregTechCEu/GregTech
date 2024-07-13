package gregtech.common.pipelike.block.cable;

import gregtech.api.graphnet.pipenet.physical.IBurnable;
import gregtech.api.graphnet.pipenet.physical.PipeMaterialBlock;
import gregtech.api.unification.material.Material;

import gregtech.api.unification.material.registry.MaterialRegistry;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;
import java.util.Map;

public class CableBlock extends PipeMaterialBlock implements IBurnable {

    private static final Map<MaterialRegistry, Map<CableStructure, CableBlock>> CACHE = new Object2ObjectOpenHashMap<>();

    public CableBlock(CableStructure structure, MaterialRegistry registry, Collection<? extends Material> materials) {
        super(structure, registry, materials);
        CACHE.compute(registry, (k, v) -> {
            if (v == null) v = new Object2ObjectOpenHashMap<>();
            v.put(structure, this);
            return v;
        });
    }

    @Override
    public void partialBurn(IBlockState state, World world, BlockPos pos) {
        CableStructure structure = (CableStructure) getStructure();
        if (structure.partialBurnStructure() != null) {
            CableBlock newBlock = CACHE.get(registry).get(structure.partialBurnStructure());
            world.setBlockState(pos, newBlock.getStateFromMeta(this.getMetaFromState(state)));
        }
    }

    // TODO
    @Override
    protected Pair<TextureAtlasSprite, Integer> getParticleTexture(World world, BlockPos blockPos) {
        return null;
    }
}

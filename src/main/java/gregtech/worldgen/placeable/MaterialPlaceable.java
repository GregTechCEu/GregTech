package gregtech.worldgen.placeable;

import gregtech.api.GregTechAPI;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.ore.StoneType;
import gregtech.worldgen.WorldgenPlaceable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class MaterialPlaceable implements WorldgenPlaceable {

    private final Material material;

    /**
     * @param material the material to place
     */
    public MaterialPlaceable(@NotNull Material material) {
        this.material = material;
    }

    @Override
    public void place(@NotNull World world, @NotNull BlockPos pos, @NotNull IBlockState existing) {
        var map = GregTechAPI.oreBlockTable.get(material);
        if (map == null) return;

        StoneType stoneType = StoneType.computeStoneType(existing, world, pos);
        if (stoneType == null) return;

        var oreBlock = map.get(stoneType);
        if (oreBlock == null) return;

        IBlockState state = oreBlock.getOreBlock(stoneType);
        world.setBlockState(pos, state, 0);
    }

    @Override
    public void placeSmall(@NotNull World world, @NotNull BlockPos pos, @NotNull IBlockState existing) {
        //TODO Small Ores
    }
}

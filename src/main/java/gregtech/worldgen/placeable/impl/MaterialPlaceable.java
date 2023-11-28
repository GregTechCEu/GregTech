package gregtech.worldgen.placeable.impl;

import gregtech.api.GregTechAPI;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.ore.StoneType;
import gregtech.api.util.IBlockOre;
import gregtech.worldgen.WorldgenModule;
import gregtech.worldgen.placeable.WorldgenPlaceable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

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
        place(world, pos, existing, GregTechAPI.oreBlockTable.get(material));
    }

    @Override
    public boolean hasRegular() {
        return true;
    }

    @Override
    public void placeSmall(@NotNull World world, @NotNull BlockPos pos, @NotNull IBlockState existing) {
        place(world, pos, existing, GregTechAPI.smallOreBlockTable.get(material));
    }

    @Override
    public boolean hasSmall() {
        return WorldgenModule.isSmallOresEnabled();
    }

    private static void place(@NotNull World world, @NotNull BlockPos pos, @NotNull IBlockState existing,
                              @Nullable Map<StoneType, IBlockOre> map) {
        if (map == null) return;

        StoneType stoneType = StoneType.computeStoneType(existing, world, pos);
        if (stoneType == null) return;

        var oreBlock = map.get(stoneType);
        if (oreBlock == null) return;

        IBlockState state = oreBlock.getOreBlock(stoneType);
        world.setBlockState(pos, state, 16); // prevent observer updates with flag 16
    }
}

package gregtech.common.blocks.foam;

import gregtech.api.items.toolitem.ToolClasses;

import net.minecraft.block.BlockColored;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving.SpawnPlacementType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import org.jetbrains.annotations.NotNull;

public class BlockPetrifiedFoam extends BlockColored {

    public BlockPetrifiedFoam(boolean isReinforced) {
        super(Material.ROCK);
        setTranslationKey(isReinforced ? "gt.reinforced_stone" : "gt.petrified_foam");
        setSoundType(isReinforced ? SoundType.STONE : SoundType.SNOW);
        setResistance(isReinforced ? 16.0f : 4.0f);
        setHardness(isReinforced ? 4.0f : 1.0f);
        setHarvestLevel(ToolClasses.PICKAXE, isReinforced ? 2 : 1);
    }

    @Override
    public boolean canCreatureSpawn(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos,
                                    @NotNull SpawnPlacementType type) {
        return false;
    }
}

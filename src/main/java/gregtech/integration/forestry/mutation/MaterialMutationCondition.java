package gregtech.integration.forestry.mutation;

import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.api.util.LocalizationUtils;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import forestry.api.apiculture.IBeeHousing;
import forestry.api.climate.IClimateProvider;
import forestry.api.genetics.IAllele;
import forestry.api.genetics.IGenome;
import forestry.api.genetics.IMutationCondition;
import forestry.core.tiles.TileUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class MaterialMutationCondition implements IMutationCondition {

    private final Set<IBlockState> acceptedBlocks = new HashSet<>();
    private final String displayName;

    public MaterialMutationCondition(@NotNull Material material) {
        this.displayName = LocalizationUtils.format("gregtech.mutation.block_of", material.getLocalizedName());
        String oreDictName = new UnificationEntry(OrePrefix.block, material).toString();

        for (ItemStack ore : OreDictUnifier.getAllWithOreDictionaryName(oreDictName)) {
            if (!ore.isEmpty()) {
                Item oreItem = ore.getItem();
                Block oreBlock = Block.getBlockFromItem(oreItem);
                if (oreBlock != Blocks.AIR) {
                    this.acceptedBlocks.addAll(oreBlock.getBlockState().getValidStates());
                }
            }
        }
    }

    @Override
    public float getChance(@NotNull World world, @NotNull BlockPos pos, @NotNull IAllele allele0,
                           @NotNull IAllele allele1, @NotNull IGenome genome0,
                           @NotNull IGenome genome1, @NotNull IClimateProvider climate) {
        TileEntity tile;
        do {
            pos = pos.down();
            tile = TileUtil.getTile(world, pos);
        } while (tile instanceof IBeeHousing);

        IBlockState blockState = world.getBlockState(pos);
        return this.acceptedBlocks.contains(blockState) ? 1.0F : 0.0F;
    }

    @Override
    public @NotNull String getDescription() {
        return LocalizationUtils.format("for.mutation.condition.resource", this.displayName);
    }
}

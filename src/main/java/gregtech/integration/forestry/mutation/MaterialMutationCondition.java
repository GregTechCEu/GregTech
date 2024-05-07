package gregtech.integration.forestry.mutation;

import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
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

import java.util.HashSet;
import java.util.Set;

import static org.apache.commons.lang3.StringUtils.capitalize;

public class MaterialMutationCondition implements IMutationCondition {

    private final Set<IBlockState> acceptedBlocks = new HashSet();
    private final String displayName;

    public MaterialMutationCondition(Material material) {
        this.displayName = LocalizationUtils.format("gregtech.mutation.block_of", material.getLocalizedName());
        String oredictName = "block" + capitalize(material.getName());

        for (ItemStack ore : OreDictUnifier.getAllWithOreDictionaryName(oredictName)) {
            if (!ore.isEmpty()) {
                Item oreItem = ore.getItem();
                Block oreBlock = Block.getBlockFromItem(oreItem);
                if (oreBlock != Blocks.AIR) {
                    this.acceptedBlocks.addAll(oreBlock.getBlockState().getValidStates());
                }
            }
        }
    }

    public float getChance(World world, BlockPos pos, IAllele allele0, IAllele allele1, IGenome genome0,
                           IGenome genome1, IClimateProvider climate) {
        TileEntity tile;
        do {
            pos = pos.down();
            tile = TileUtil.getTile(world, pos);
        } while (tile instanceof IBeeHousing);

        IBlockState blockState = world.getBlockState(pos);
        return this.acceptedBlocks.contains(blockState) ? 1.0F : 0.0F;
    }

    public String getDescription() {
        return LocalizationUtils.format("for.mutation.condition.resource", this.displayName);
    }
}

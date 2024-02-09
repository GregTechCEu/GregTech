package gregtech.api.recipes.recipeproperties;

import net.minecraft.block.state.IBlockState;
import java.util.ArrayList;
import java.util.Arrays;

public class PseudoMultiPropertyValues {
    public ArrayList<IBlockState> validBlockStates;

    public final String blockGroupName;

    public PseudoMultiPropertyValues(String blockGroupName, IBlockState... validBlockStates) {
        this.validBlockStates = new ArrayList<>(Arrays.asList(validBlockStates));
        this.blockGroupName = blockGroupName;
    }

    public PseudoMultiPropertyValues(String blockGroupName, ArrayList<IBlockState> validBlocks) {
        this.validBlockStates = validBlocks;
        this.blockGroupName = blockGroupName;
    }

    public ArrayList<IBlockState> getValidBlockStates() {
        return validBlockStates;
    }

    public String getBlockGroupName() {
        return blockGroupName;
    }
}

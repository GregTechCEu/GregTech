package gregtech.api.worldgen.filler;

import com.google.gson.JsonObject;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.minecraft.CraftTweakerMC;
import gregtech.api.GTValues;
import gregtech.api.worldgen.config.FillerConfigUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.common.Optional.Method;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenGetter;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@ZenClass("mods.gregtech.ore.filler.BlacklistedBlockFiller")
@ZenRegister
public class BlacklistedBlockFiller extends BlockFiller {

    private FillerEntry blockStateFiller;
    private final List<IBlockState> blacklist;

    public BlacklistedBlockFiller(List<IBlockState> blacklist) {
        this.blacklist = blacklist;
    }

    public BlacklistedBlockFiller(List<IBlockState> blacklist, FillerEntry blockStateFiller) {
        this.blacklist = blacklist;
        this.blockStateFiller = blockStateFiller;
    }

    public List<IBlockState> getBlacklist() {
        return blacklist;
    }

    @ZenGetter("blacklist")
    @Method(modid = GTValues.MODID_CT)
    public List<crafttweaker.api.block.IBlockState> ctGetBlacklist() {
        return blacklist.stream()
                .map(CraftTweakerMC::getBlockState)
                .collect(Collectors.toList());
    }

    @Override
    public void loadFromConfig(JsonObject object) {
        this.blockStateFiller = FillerConfigUtils.createBlockStateFiller(object.get("value"));
    }

    @Override
    public IBlockState apply(IBlockState currentState, IBlockAccess blockAccess, BlockPos blockPos, int relativeX, int relativeY, int relativeZ) {
        for (IBlockState blockState : blacklist) {
            if (blockState == currentState) {
                return currentState;
            }
        }
        return blockStateFiller.apply(currentState, blockAccess, blockPos);
    }

    @Override
    public List<FillerEntry> getAllPossibleStates() {
        return Collections.singletonList(blockStateFiller);
    }
}

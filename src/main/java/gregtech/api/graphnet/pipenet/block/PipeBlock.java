package gregtech.api.graphnet.pipenet.block;

import gregtech.api.block.BuiltInRenderBlock;

import gregtech.api.graphnet.pipenet.IPipeNetNodeHandler;
import gregtech.api.util.GTUtility;
import gregtech.common.ConfigHolder;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLiving;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class PipeBlock extends BuiltInRenderBlock {

    public final IPipeStructure structure;

    public PipeBlock(IPipeStructure structure) {
        super(net.minecraft.block.material.Material.IRON);
        this.structure = structure;
        this.setDefaultState(constructDefaultState());

        setTranslationKey("pipe" + getStructureName());
        setSoundType(SoundType.METAL);
        setHardness(2.0f);
        setResistance(3.0f);
        setLightOpacity(0);
        disableStats();
    }

    protected IBlockState constructDefaultState() {
        return this.blockState.getBaseState();
    }

    @Override
    public void onBlockAdded(@NotNull World worldIn, @NotNull BlockPos pos, @NotNull IBlockState state) {
        super.onBlockAdded(worldIn, pos, state);
        getHandler(state).addToNets(worldIn, pos, structure);
    }

    @Override
    public void breakBlock(@NotNull World worldIn, @NotNull BlockPos pos, @NotNull IBlockState state) {
        super.breakBlock(worldIn, pos, state);
        getHandler(state).removeFromNets(worldIn, pos, structure);
    }

    @NotNull
    protected abstract IPipeNetNodeHandler getHandler(IBlockState state);

    @Override
    public boolean canCreatureSpawn(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos,
                                    @NotNull EntityLiving.SpawnPlacementType type) {
        return false;
    }

    public String getStructureName() {
        return GTUtility.lowerUnderscoreToUpperCamel(structure.getName());
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               @NotNull ITooltipFlag flag) {
        if (ConfigHolder.misc.debug) {
            tooltip.add("MetaItem Id: pipe" + getStructureName());
        }
    }

    @Nullable
    public static PipeBlock getPipeBlockFromItem(@NotNull ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof ItemBlock) {
            Block block = ((ItemBlock) item).getBlock();
            if (block instanceof PipeBlock) return (PipeBlock) block;
        }
        return null;
    }

    @Override
    protected Pair<TextureAtlasSprite, Integer> getParticleTexture(World world, BlockPos blockPos) {
        return null;
    }
}

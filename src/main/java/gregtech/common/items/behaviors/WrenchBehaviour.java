package gregtech.common.items.behaviors;

import com.google.common.collect.UnmodifiableIterator;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.util.GTUtility;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockPistonExtension;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.List;

public class WrenchBehaviour implements IItemBehaviour {

    private final int cost;

    public WrenchBehaviour(int cost) {
        this.cost = cost;
    }

    @Override
    @SuppressWarnings("rawtypes, unchecked")
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
        if (!world.isRemote && !world.isAirBlock(pos)) {
            ItemStack stack = player.getHeldItem(hand);

            TileEntity tileEntity = world.getTileEntity(pos);

            if (tileEntity instanceof IGregTechTileEntity) {
                //machines handle wrench click manually
                return EnumActionResult.PASS;
            }

            // Adapted mostly from Block#rotateBlock()
            IBlockState state = world.getBlockState(pos);
            UnmodifiableIterator<IProperty<?>> propertyItr = state.getProperties().keySet().iterator();
            IProperty prop;
            while (propertyItr.hasNext()) {
                prop = propertyItr.next();
                if (prop.getName().equals("facing") || prop.getName().equals("rotation")) {
                    if (prop.getValueClass() == EnumFacing.class) {
                        Block block = state.getBlock();
                        if (!(block instanceof BlockBed) && !(block instanceof BlockPistonExtension)) {
                            Collection<EnumFacing> facings = (Collection<EnumFacing>) prop.getAllowedValues();
                            if (facings.contains(side) && state.getValue(prop) != side) {
                                world.setBlockState(pos, state.withProperty(prop, side));
                                GTUtility.doDamageItem(stack, this.cost, false);
                                return EnumActionResult.SUCCESS;
                            }
                        }
                    }
                }
            }
        }
        return EnumActionResult.PASS;
    }

    @Override
    public void addInformation(ItemStack itemStack, List<String> lines) {
        lines.add(I18n.format("behaviour.wrench"));
    }
}

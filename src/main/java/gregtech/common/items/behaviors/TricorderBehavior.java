package gregtech.common.items.behaviors;

import gregtech.api.GregTechAPI;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.util.LocalizationUtils;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.fluids.IFluidTank;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class TricorderBehavior implements IItemBehaviour {

    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance();

    private final int cost;

    public TricorderBehavior(int cost) {
        this.cost = cost;
    }

    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
        if (!world.isRemote && !world.isAirBlock(pos)) {
            ItemStack stack = player.getHeldItem(hand);

            List<String> list = getScannerInfo(player, world, pos, 0, side, hitX, hitY, hitZ);
            for (String line : list) {
                player.sendMessage(new TextComponentString(line));
            }
        }
        return EnumActionResult.PASS;
    }

    public List<String> getScannerInfo(EntityPlayer player, World world, BlockPos pos, int scanLevel, EnumFacing side, float hitX, float hitY, float hitZ) {
        int energyCost = 0;

        List<String> list = new ArrayList<>();

        TileEntity tileEntity = world.getTileEntity(pos);

        Block block = world.getBlockState(pos).getBlock();

        // coordinates of the block
        //todo TextComponentTranslations do not show colors

        // list.add(new TextComponentTranslation("behavior.tricorder.position", formatNumbers(pos.getX()), formatNumbers(pos.getY()), formatNumbers(pos.getZ()), world.provider.getDimension()));
        list.add(
                "----- X: " + TextFormatting.AQUA + formatNumbers(pos.getX()) + TextFormatting.RESET +
                " Y: " + TextFormatting.AQUA + formatNumbers(pos.getY()) + TextFormatting.RESET +
                " Z: " + TextFormatting.AQUA + formatNumbers(pos.getZ()) + TextFormatting.RESET +
                " D: " + TextFormatting.AQUA + world.provider.getDimension() + TextFormatting.RESET + " -----");


        // hardness and blast resistance
        list.add("Hardness: " + TextFormatting.YELLOW + block.blockHardness + TextFormatting.RESET +
                " Blast Resistance: " + TextFormatting.YELLOW + block.getExplosionResistance(null) + TextFormatting.RESET);

        if (tileEntity instanceof MetaTileEntityHolder) {
            // todo does this need a null check? probably
            MetaTileEntity metaTileEntity = ((MetaTileEntityHolder) tileEntity).getMetaTileEntity();

            // name of the machine
            //  todo this wont work for pipes/cables, will need to check ```if (tileEntity instanceof TileEntityPipeBase)```
//        list.add(new TextComponentTranslation("behavior.tricorder.block_name", I18n.format(block.getLocalizedName()), world.getBlockState(pos).getBlock().getMetaFromState(world.getBlockState(pos))));
            list.add(1, "Name: " + TextFormatting.BLUE + LocalizationUtils.format(metaTileEntity.getMetaFullName()) + TextFormatting.RESET +
                    " MetaData: " + TextFormatting.AQUA + GregTechAPI.MTE_REGISTRY.getIdByObjectName(metaTileEntity.metaTileEntityId) + TextFormatting.RESET);


            // fluid tanks
            FluidTankList tanks = metaTileEntity.getImportFluids();
            if (tanks != null) {
                if (!tanks.getFluidTanks().isEmpty()) {
                    energyCost += 500;
                    for (int i = 0; i < tanks.getFluidTanks().size(); i++) {
                        IFluidTank tank = tanks.getTankAt(i);
                        list.add("Tank " + i + ": " +
                                TextFormatting.GREEN + formatNumbers((tank.getFluid() == null ? 0 : tank.getFluid().amount)) + TextFormatting.RESET + " L / " +
                                TextFormatting.YELLOW + formatNumbers(tank.getCapacity()) + TextFormatting.RESET + " L " + TextFormatting.GOLD + (tank.getFluid() == null ? "" : tank.getFluid().getLocalizedName()) + TextFormatting.RESET);
                    }
                }
            }
        }

        return list;
    }

    public static String formatNumbers(long number) {
        return NUMBER_FORMAT.format(number);
    }

    public static String formatNumbers(double number) {
        return NUMBER_FORMAT.format(number);
    }

    @Override
    public void addInformation(ItemStack itemStack, List<String> lines) {
    }

}

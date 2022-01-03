package gregtech.common.items.behaviors;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.IWorkable;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.metatileentity.IDataInfoProvider;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.sound.ISoundCreator;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.pipenet.tile.TileEntityPipeBase;
import gregtech.api.util.GTUtility;
import gregtech.api.util.LocalizationUtils;
import gregtech.api.worldgen.bedrockFluids.BedrockFluidVeinHandler;
import gregtech.common.pipelike.fluidpipe.tile.TileEntityFluidPipe;
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
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;

import java.util.ArrayList;
import java.util.List;

public class TricorderBehavior implements IItemBehaviour {

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

        // list.add(new TextComponentTranslation("behavior.tricorder.position", GTUtility.formatNumbers(pos.getX()), GTUtility.formatNumbers(pos.getY()), GTUtility.formatNumbers(pos.getZ()), world.provider.getDimension()));
        list.add(
                "----- X: " + TextFormatting.AQUA + GTUtility.formatNumbers(pos.getX()) + TextFormatting.RESET +
                " Y: " + TextFormatting.AQUA + GTUtility.formatNumbers(pos.getY()) + TextFormatting.RESET +
                " Z: " + TextFormatting.AQUA + GTUtility.formatNumbers(pos.getZ()) + TextFormatting.RESET +
                " D: " + TextFormatting.AQUA + world.provider.getDimension() + TextFormatting.RESET + " -----");


        // hardness and blast resistance
        list.add("Hardness: " + TextFormatting.YELLOW + block.blockHardness + TextFormatting.RESET +
                " Blast Resistance: " + TextFormatting.YELLOW + block.getExplosionResistance(null) + TextFormatting.RESET);

        if (tileEntity instanceof MetaTileEntityHolder) {
            MetaTileEntity metaTileEntity = ((MetaTileEntityHolder) tileEntity).getMetaTileEntity();
            if (metaTileEntity == null)
                return list;

            // name of the machine
            //  todo this wont work for pipes/cables, will need to check ```if (tileEntity instanceof TileEntityPipeBase)```
//        list.add(new TextComponentTranslation("behavior.tricorder.block_name", I18n.format(block.getLocalizedName()), world.getBlockState(pos).getBlock().getMetaFromState(world.getBlockState(pos))));
            list.add(1, "Name: " + TextFormatting.BLUE + LocalizationUtils.format(metaTileEntity.getMetaFullName()) + TextFormatting.RESET +
                    " MetaData: " + TextFormatting.AQUA + GregTechAPI.MTE_REGISTRY.getIdByObjectName(metaTileEntity.metaTileEntityId) + TextFormatting.RESET);

            list.add("=========================");

            // fluid tanks
            FluidTankList tanks = metaTileEntity.getImportFluids();
            if (tanks != null) {
                if (!tanks.getFluidTanks().isEmpty()) {
                    energyCost += 500;
                    for (int i = 0; i < tanks.getFluidTanks().size(); i++) {
                        IFluidTank tank = tanks.getTankAt(i);
                        list.add("Tank " + i + ": " +
                                TextFormatting.GREEN + GTUtility.formatNumbers((tank.getFluid() == null ? 0 : tank.getFluid().amount)) + TextFormatting.RESET + " L / " +
                                TextFormatting.YELLOW + GTUtility.formatNumbers(tank.getCapacity()) + TextFormatting.RESET + " L " + TextFormatting.GOLD + (tank.getFluid() == null ? "" : tank.getFluid().getLocalizedName()) + TextFormatting.RESET);
                    }
                }
            }
            // sound muffling
            if (metaTileEntity instanceof ISoundCreator) {
                energyCost += 500;
                if (metaTileEntity.isMuffled()) list.add(TextFormatting.GREEN + "Is Muffled" + TextFormatting.RESET);
            }

            // workable progress info
            IWorkable workable = metaTileEntity.getCapability(GregtechTileCapabilities.CAPABILITY_WORKABLE, null);
            if (workable != null) {
                if (!workable.isWorkingEnabled()) {
                    list.add("Disabled." + TextFormatting.RED + TextFormatting.RESET);
                }
//                if (workable.wasShutdown()) { todo
//                    list.add("Shut down due to power loss." + TextFormatting.RED + TextFormatting.RESET);
//                }
                energyCost += 400;
                if (workable.getMaxProgress() > 0)
                    list.add("Progress/Load: " + TextFormatting.GREEN + GTUtility.formatNumbers(workable.getProgress())  + TextFormatting.RESET + " / " +
                            TextFormatting.YELLOW + GTUtility.formatNumbers(workable.getMaxProgress()) + TextFormatting.RESET);
            }

            list.add("=========================");

            // energy container
            IEnergyContainer container = metaTileEntity.getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, null);
            if (container != null && container.getEnergyCapacity() > 0) {
                list.add(
                        "Max IN: " + TextFormatting.RED + GTUtility.formatNumbers(container.getInputVoltage()) + " (" + GTValues.VN[GTUtility.getTierByVoltage(container.getInputVoltage())] + ") " + TextFormatting.RESET +
                                " EU at " + TextFormatting.RED + GTUtility.formatNumbers(container.getInputAmperage()) + TextFormatting.RESET + " A");
                list.add(
                        "Max OUT: " + TextFormatting.RED + GTUtility.formatNumbers(container.getOutputVoltage()) + " (" + GTValues.VN[GTUtility.getTierByVoltage(container.getOutputVoltage())] + ") " + TextFormatting.RESET +
                                " EU at " + TextFormatting.RED + GTUtility.formatNumbers(container.getOutputAmperage()) + TextFormatting.RESET + " A");
                list.add(
                        "Energy: " + TextFormatting.GREEN + GTUtility.formatNumbers(container.getEnergyStored()) + TextFormatting.RESET + " EU / " +
                                TextFormatting.YELLOW + GTUtility.formatNumbers(container.getEnergyCapacity()) + TextFormatting.RESET + " EU");
            }

            // machine-specific info
            IDataInfoProvider provider = null;
            if (tileEntity instanceof IDataInfoProvider)
                provider = (IDataInfoProvider) tileEntity;
            else if (metaTileEntity instanceof IDataInfoProvider)
                provider = (IDataInfoProvider) metaTileEntity;

            if (provider != null) {
                list.add("=========================");

                list.addAll(provider.getDataInfo());
            }

            // crops (adds 1000EU)
            list.add("=========================");

            // bedrock fluids
            if (player.isCreative()) {
                Fluid fluid = BedrockFluidVeinHandler.getFluid(world, pos.getX() / 16, pos.getZ() / 16);//-# to only read
                if (fluid != null) {
                    FluidStack stack = new FluidStack(fluid, BedrockFluidVeinHandler.getFluidRateInChunk(world, pos.getX() / 16, pos.getZ() / 16));
                    list.add(TextFormatting.GOLD + fluid.getLocalizedName(stack) + TextFormatting.RESET + ": " + TextFormatting.YELLOW + GTUtility.formatNumbers(stack.amount) + TextFormatting.RESET + " L");
                } else {
                    list.add(TextFormatting.GOLD + "Nothing" + TextFormatting.RESET + ": " + TextFormatting.YELLOW + '0' + TextFormatting.RESET + " L");
                }
            }

            // pollution
//            if (GT_Pollution.hasPollution(currentChunk)) {
//                list.add("Pollution in Chunk: " + TextFormatting.RED + GTUtility.formatNumbers(GT_Pollution.getPollution(currentChunk)) + TextFormatting.RESET + " gibbl");
//            } else {
//                list.add(TextFormatting.GREEN + "No Pollution in Chunk! HAYO!" + TextFormatting.RESET);
//            }

        } else {
            if (tileEntity instanceof TileEntityPipeBase) {
                IPipeTile<?, ?> pipeTile = (IPipeTile<?, ?>) tileEntity;

                if (pipeTile.getPipeBlock().getRegistryName() != null) {
                    list.add("Name: " + TextFormatting.BLUE + LocalizationUtils.format(pipeTile.getPipeBlock().getRegistryName().toString()) + TextFormatting.RESET +
                            " MetaData: " + TextFormatting.AQUA + block.getMetaFromState(world.getBlockState(pos)) + TextFormatting.RESET);
                }

                if (pipeTile.getPipeBlock().getPipeTypeClass() == TileEntityFluidPipe.class) {

                }
            }
        }

        return list;
    }

    @Override
    public void addInformation(ItemStack itemStack, List<String> lines) {
    }

}

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
import gregtech.api.util.GTUtility;
import gregtech.api.util.LocalizationUtils;
import gregtech.api.worldgen.bedrockFluids.BedrockFluidVeinHandler;
import gregtech.common.pipelike.fluidpipe.tile.TileEntityFluidPipe;
import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
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

            for (ITextComponent line : getScannerInfo(player, world, pos, 0, side, hitX, hitY, hitZ)) {
                player.sendMessage(line);
            }
        }
        return EnumActionResult.SUCCESS;
    }

    public List<ITextComponent> getScannerInfo(EntityPlayer player, World world, BlockPos pos, int scanLevel, EnumFacing side, float hitX, float hitY, float hitZ) {
        int energyCost = 0;

        List<ITextComponent> list = new ArrayList<>();

        TileEntity tileEntity = world.getTileEntity(pos);

        Block block = world.getBlockState(pos).getBlock();

        // coordinates of the block
        //todo TextComponentTranslations do not show colors

        list.add(new TextComponentTranslation(I18n.format("behavior.tricorder.position",
                GTUtility.formatNumbers(pos.getX()), GTUtility.formatNumbers(pos.getY()),
                GTUtility.formatNumbers(pos.getZ()), world.provider.getDimension())));

        // hardness and blast resistance
        list.add(new TextComponentTranslation(I18n.format("behavior.tricorder.block_hardness", block.blockHardness, block.getExplosionResistance(null))));

        if (tileEntity instanceof MetaTileEntityHolder) {
            MetaTileEntity metaTileEntity = ((MetaTileEntityHolder) tileEntity).getMetaTileEntity();
            if (metaTileEntity == null)
                return list;

            // name of the machine
            list.add(new TextComponentTranslation(I18n.format("behavior.tricorder.block_name",
                    LocalizationUtils.format(metaTileEntity.getMetaFullName()), GregTechAPI.MTE_REGISTRY.getIdByObjectName(metaTileEntity.metaTileEntityId))));

            list.add(new TextComponentTranslation("behavior.tricorder.divider"));

            // fluid tanks
            FluidTankList tanks = metaTileEntity.getImportFluids();
            if (tanks != null) {
                if (!tanks.getFluidTanks().isEmpty()) {
                    energyCost += 500;
                    for (int i = 0; i < tanks.getFluidTanks().size(); i++) {
                        IFluidTank tank = tanks.getTankAt(i);
                        list.add(new TextComponentTranslation(I18n.format("behavior.tricorder.tank", i,
                                tank.getFluid() == null ? 0 : tank.getFluid().amount, tank.getCapacity(),
                                tank.getFluid() == null ? "" : tank.getFluid().getLocalizedName())));
                    }
                }
            }

            // sound muffling
            if (metaTileEntity instanceof ISoundCreator) {
                energyCost += 500;
                if (metaTileEntity.isMuffled())
                    list.add(new TextComponentTranslation(I18n.format("behavior.tricorder.muffled")));
            }

            // workable progress info
            IWorkable workable = metaTileEntity.getCapability(GregtechTileCapabilities.CAPABILITY_WORKABLE, null);
            if (workable != null) {
                if (!workable.isWorkingEnabled()) {
                    list.add(new TextComponentTranslation(I18n.format("behavior.tricorder.machine_disabled")));
                }
                //            if (workable.wasShutdown()) { //todo
                //                list.add(new TextComponentTranslation(I18n.format("behavior.tricorder.machine_power_loss")));
                //            }
                energyCost += 400;
                if (workable.getMaxProgress() > 0) {
                    list.add(new TextComponentTranslation(I18n.format("behavior.tricorder.machine_progress",
                            GTUtility.formatNumbers(workable.getProgress()), GTUtility.formatNumbers(workable.getMaxProgress()))));
                }
            }

            // energy container
            IEnergyContainer container = metaTileEntity.getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, null);
            if (container != null && container.getEnergyCapacity() > 0) {
                list.add(new TextComponentTranslation("behavior.tricorder.divider"));
                if (container.getInputVoltage() > 0) {
                    list.add(new TextComponentTranslation(I18n.format("behavior.tricorder.energy_container_in", GTUtility.formatNumbers(container.getInputVoltage()),
                            GTValues.VN[GTUtility.getTierByVoltage(container.getInputVoltage())], GTUtility.formatNumbers(container.getInputAmperage()))));
                }
                if (container.getOutputVoltage() > 0) {
                    list.add(new TextComponentTranslation(I18n.format("behavior.tricorder.energy_container_out", GTUtility.formatNumbers(container.getOutputVoltage()),
                            GTValues.VN[GTUtility.getTierByVoltage(container.getOutputVoltage())], GTUtility.formatNumbers(container.getOutputAmperage()))));
                }
                list.add(new TextComponentTranslation(I18n.format("behavior.tricorder.energy_container_storage", GTUtility.formatNumbers(container.getEnergyStored()),
                        GTUtility.formatNumbers(container.getEnergyCapacity()))));
            }

            // machine-specific info
            IDataInfoProvider provider = null;
            if (tileEntity instanceof IDataInfoProvider)
                provider = (IDataInfoProvider) tileEntity;
            else if (metaTileEntity instanceof IDataInfoProvider)
                provider = (IDataInfoProvider) metaTileEntity;

            if (provider != null) {
                list.add(new TextComponentTranslation("behavior.tricorder.divider"));

                list.addAll(provider.getDataInfo());
            }

        } else if (tileEntity instanceof IPipeTile) {
            // pipes need special name handling
            IPipeTile<?, ?> pipeTile = (IPipeTile<?, ?>) tileEntity;

            if (pipeTile.getPipeBlock().getRegistryName() != null) {
                list.add(new TextComponentTranslation(I18n.format("behavior.tricorder.block_name",
                        LocalizationUtils.format(pipeTile.getPipeBlock().getTranslationKey()), block.getMetaFromState(world.getBlockState(pos)))));
            }

            // pipe-specific info
            if (tileEntity instanceof IDataInfoProvider) {
                IDataInfoProvider provider = (IDataInfoProvider) tileEntity;

                list.add(new TextComponentTranslation("behavior.tricorder.divider"));

                list.addAll(provider.getDataInfo());
            }

            if (tileEntity instanceof TileEntityFluidPipe) {
                // getting fluid info always costs 500
                energyCost += 500;
            }
        } else if (tileEntity instanceof IDataInfoProvider) {
            IDataInfoProvider provider = (IDataInfoProvider) tileEntity;

            list.add(new TextComponentTranslation("behavior.tricorder.divider"));

            list.addAll(provider.getDataInfo());
        } else {
            list.add(new TextComponentTranslation(I18n.format("behavior.tricorder.block_name",
                    I18n.format(block.getTranslationKey()), block.getMetaFromState(world.getBlockState(pos)))));
        }


        // crops (adds 1000EU)

        // bedrock fluids
        if (player.isCreative()) {
            list.add(new TextComponentTranslation("behavior.tricorder.divider"));
            Fluid fluid = BedrockFluidVeinHandler.getFluid(world, pos.getX() / 16, pos.getZ() / 16);//-# to only read
            if (fluid != null) {
                FluidStack stack = new FluidStack(fluid, BedrockFluidVeinHandler.getFluidRateInChunk(world, pos.getX() / 16, pos.getZ() / 16));
                list.add(new TextComponentTranslation(I18n.format("behavior.tricorder.bedrock_fluid.amount", fluid.getLocalizedName(stack),
                        GTUtility.formatNumbers(stack.amount))));
            } else {
                list.add(new TextComponentTranslation(I18n.format("behavior.tricorder.bedrock_fluid.amount")));
            }
        }

        // pollution
//            if (GT_Pollution.hasPollution(currentChunk)) {
//                list.add("Pollution in Chunk: " + TextFormatting.RED + GTUtility.formatNumbers(GT_Pollution.getPollution(currentChunk)) + TextFormatting.RESET + " gibbl");
//            } else {
//                list.add(TextFormatting.GREEN + "No Pollution in Chunk! HAYO!" + TextFormatting.RESET);
//            }

        return list;
    }

    @Override
    public void addInformation(ItemStack itemStack, List<String> lines) {
    }

}

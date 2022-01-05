package gregtech.common.items.behaviors;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.capability.*;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.metatileentity.IDataInfoProvider;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.sound.ISoundCreator;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.sound.GTSounds;
import gregtech.api.util.GTUtility;
import gregtech.api.util.LocalizationUtils;
import gregtech.api.worldgen.bedrockFluids.BedrockFluidVeinHandler;
import gregtech.common.ConfigHolder;
import gregtech.common.pipelike.fluidpipe.tile.TileEntityFluidPipe;
import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class TricorderBehavior implements IItemBehaviour {

    private final int debugLevel;
    private int energyCost;

    public TricorderBehavior(int debugLevel) {
        this.debugLevel = debugLevel;
    }

    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
        if (!world.isRemote && !world.isAirBlock(pos)) {

            List<ITextComponent> info = getScannerInfo(player, world, pos);
            if (drainEnergy(player.getHeldItem(hand), energyCost, true)) {
                drainEnergy(player.getHeldItem(hand), energyCost, false);
                for (ITextComponent line : info) {
                    player.sendMessage(line);
                }
                if (ConfigHolder.client.toolUseSounds)
                    world.playSound(null, pos, GTSounds.TRICORDER_TOOL, SoundCategory.PLAYERS, 1, 1);
            } else {
                player.sendMessage(new TextComponentTranslation("behavior.prospector.not_enough_energy"));
            }
        }
        return EnumActionResult.SUCCESS;
    }

    public List<ITextComponent> getScannerInfo(EntityPlayer player, World world, BlockPos pos) {
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

        MetaTileEntity metaTileEntity;
        if (tileEntity instanceof MetaTileEntityHolder) {
            metaTileEntity = ((MetaTileEntityHolder) tileEntity).getMetaTileEntity();
            if (metaTileEntity == null)
                return list;

            // name of the machine
            list.add(new TextComponentTranslation(I18n.format("behavior.tricorder.block_name",
                    LocalizationUtils.format(metaTileEntity.getMetaFullName()), GregTechAPI.MTE_REGISTRY.getIdByObjectName(metaTileEntity.metaTileEntityId))));

            list.add(new TextComponentTranslation("behavior.tricorder.divider"));

            // fluid tanks
            FluidTankList tanks = metaTileEntity.getImportFluids();
            int tankIndex = 0;
            boolean allTanksEmpty = true;
            if (tanks != null && !tanks.getFluidTanks().isEmpty()) {
                energyCost += 500;
                for (int i = tankIndex; i < tanks.getFluidTanks().size(); i++) {
                    IFluidTank tank = tanks.getTankAt(i);
                    if (tank.getFluid() == null)
                        continue;

                    allTanksEmpty = false;
                    list.add(new TextComponentTranslation(I18n.format("behavior.tricorder.tank", i,
                            tank.getFluid().amount, tank.getCapacity(), tank.getFluid().getLocalizedName())));
                }
                tankIndex += tanks.getFluidTanks().size();
            }
            tanks = metaTileEntity.getExportFluids();
            if (tanks != null && !tanks.getFluidTanks().isEmpty()) {
                energyCost += 500;
                for (int i = 0; i < tanks.getFluidTanks().size(); i++) {
                    IFluidTank tank = tanks.getTankAt(i);
                    if (tank.getFluid() == null)
                        continue;

                    allTanksEmpty = false;
                    list.add(new TextComponentTranslation(I18n.format("behavior.tricorder.tank", i + tankIndex,
                            tank.getFluid().amount, tank.getCapacity(), tank.getFluid().getLocalizedName())));
                }
            }

            if (allTanksEmpty && (metaTileEntity.getImportFluids() != null || metaTileEntity.getExportFluids() != null))
                list.add(new TextComponentTranslation("behavior.tricorder.tanks_empty"));

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
                    I18n.format(block.getLocalizedName()), block.getMetaFromState(world.getBlockState(pos)))));
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

        // debug
        if (tileEntity instanceof MetaTileEntityHolder) {
            list.addAll(((MetaTileEntityHolder) tileEntity).getDebugInfo(player, debugLevel));
        }

        this.energyCost = energyCost;
        return list;
    }

    private boolean drainEnergy(@Nonnull ItemStack stack, long amount, boolean simulate) {
        if (debugLevel > 2)
            return true;

        IElectricItem electricItem = stack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        if (electricItem == null)
            return false;

        return electricItem.discharge(amount, Integer.MAX_VALUE, true, false, simulate) >= amount;
    }
}

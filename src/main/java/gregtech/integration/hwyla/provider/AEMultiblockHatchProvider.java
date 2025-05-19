package gregtech.integration.hwyla.provider;

import gregtech.api.GTValues;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.MetaTileEntityAEHostableChannelPart;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.api.IWailaRegistrar;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AEMultiblockHatchProvider implements IWailaDataProvider {

    private static final String NBT_KEY = "ae_part_online";

    public static final AEMultiblockHatchProvider INSTANCE = new AEMultiblockHatchProvider();

    public void register(@NotNull IWailaRegistrar registrar) {
        registrar.registerBodyProvider(this, IGregTechTileEntity.class);
        registrar.registerNBTProvider(this, IGregTechTileEntity.class);
        registrar.addConfig(GTValues.MOD_NAME, "gregtech.ae_multiblock_hatch_provider");
    }

    @Override
    public @NotNull NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world,
                                              BlockPos pos) {
        if (te instanceof IGregTechTileEntity gtte &&
                gtte.getMetaTileEntity() instanceof MetaTileEntityAEHostableChannelPart<?>aeHostablePart) {
            tag.setBoolean(NBT_KEY, aeHostablePart.isOnline());
        }

        return tag;
    }

    @Override
    public @NotNull List<String> getWailaBody(ItemStack itemStack, List<String> tooltip, IWailaDataAccessor accessor,
                                              IWailaConfigHandler config) {
        if (accessor.getNBTData().hasKey(NBT_KEY)) {
            if (accessor.getNBTData().getBoolean(NBT_KEY)) {
                tooltip.add(I18n.format("gregtech.gui.me_network.online"));
            } else {
                tooltip.add(I18n.format("gregtech.gui.me_network.offline"));
            }
        }

        return tooltip;
    }
}

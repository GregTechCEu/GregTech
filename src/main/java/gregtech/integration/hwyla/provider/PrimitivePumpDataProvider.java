package gregtech.integration.hwyla.provider;

import gregtech.api.GTValues;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IPrimitivePump;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.api.IWailaRegistrar;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PrimitivePumpDataProvider implements IWailaDataProvider {

    public static final PrimitivePumpDataProvider INSTANCE = new PrimitivePumpDataProvider();

    public void register(@NotNull IWailaRegistrar registrar) {
        registrar.registerBodyProvider(this, IGregTechTileEntity.class);
        registrar.registerNBTProvider(this, IGregTechTileEntity.class);
        registrar.addConfig(GTValues.MODID, "gregtech.primitive_pump");
    }

    @NotNull
    @Override
    public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world,
                                     BlockPos pos) {
        if (te instanceof IGregTechTileEntity gtte) {
            if (gtte.getMetaTileEntity() instanceof IPrimitivePump pump) {
                NBTTagCompound subTag = new NBTTagCompound();
                subTag.setInteger("Production", pump.getFluidProduction());
                tag.setTag("gregtech.IPrimitivePump", subTag);
            }
        }
        return tag;
    }

    @NotNull
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> tooltip, IWailaDataAccessor accessor,
                                     IWailaConfigHandler config) {
        if (!config.getConfig("gregtech.primitive_pump") ||
                !(accessor.getTileEntity() instanceof IGregTechTileEntity gtte) ||
                !(gtte.getMetaTileEntity() instanceof IPrimitivePump)) {
            return tooltip;
        }

        if (accessor.getNBTData().hasKey("gregtech.IPrimitivePump")) {
            NBTTagCompound tag = accessor.getNBTData().getCompoundTag("gregtech.IPrimitivePump");
            int production = tag.getInteger("Production");
            tooltip.add(I18n.format("gregtech.top.primitive_pump_production") + " " + TextFormatting.AQUA + production +
                    TextFormatting.RESET + " L/s");
        }
        return tooltip;
    }
}

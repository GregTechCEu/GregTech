package gregtech.integration.opencomputers.values;

import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.ICoverable;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.integration.opencomputers.InputValidator;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.prefab.AbstractValue;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.Objects;

public class ValueCoverBehavior extends AbstractValue {

    private BlockPos pos;
    private EnumFacing side;
    private int dim;
    private String coverName;

    public final Object[] NULL_COVER = new Object[]{null, "Found no cover, this is an invalid object."};

    protected ValueCoverBehavior(CoverBehavior coverBehavior, EnumFacing side, String coverName) {
        pos = coverBehavior.coverHolder.getPos();
        dim = coverBehavior.coverHolder.getWorld().provider.getDimension();
        this.side = side;
        this.coverName = coverName;
    }

    public ValueCoverBehavior(CoverBehavior coverBehavior, EnumFacing side) {
        this(coverBehavior, side, "gt_coverBehavior");
    }

    protected CoverBehavior getCoverBehavior() {
        World world = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(dim);
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof IGregTechTileEntity) {
            ICoverable coverable = te.getCapability(GregtechTileCapabilities.CAPABILITY_COVERABLE, null);
            if (coverable != null) {
                return coverable.getCoverAtSide(side);
            }
        }
        return null;
    }

    @Callback(doc = "function():number --  Returns the side of the cover.")
    public Object[] getSide(final Context context, final Arguments args) {
        return new Object[]{side.ordinal()};
    }

    @Callback(doc = "function():string --  Returns the type name of the cover.")
    public Object[] getTypeName(final Context context, final Arguments args) {
        return new Object[]{coverName};
    }

    @Callback(doc = "function(signal:number) --  Sets redstone signal output.")
    public Object[] setRedstoneSignalOutput(final Context context, final Arguments args) {
        CoverBehavior cover = getCoverBehavior();
        if (cover == null) {
            return NULL_COVER;
        }

        int signal = InputValidator.getInteger(args, 0, 0, 15);
        cover.setRedstoneSignalOutput(signal);
        return new Object[]{};
    }

    @Callback(doc = "function():number --  Gets redstone signal output.")
    public final Object[] getRedstoneSignalOutput(final Context context, final Arguments args) {
        CoverBehavior cover = getCoverBehavior();
        if (cover == null) {
            return NULL_COVER;
        }

        return new Object[]{cover.getRedstoneSignalOutput()};
    }

    @Callback(doc = "function():number --  Gets redstone signal input.")
    public final Object[] getRedstoneSignalInput(final Context context, final Arguments args) {
        CoverBehavior cover = getCoverBehavior();
        if (cover == null) {
            return NULL_COVER;
        }

        return new Object[]{cover.getRedstoneSignalInput()};
    }

    @Override
    public int hashCode() {
        return Objects.hash(pos, dim);
    }

    @Override
    public Object[] call(Context context, Arguments arguments) {
        return new Object[0];
    }

    @Override
    public void load(NBTTagCompound nbt) {
        dim = nbt.getInteger("dim");
        pos = NBTUtil.getPosFromTag(nbt.getCompoundTag("pos"));
        side = EnumFacing.values()[nbt.getInteger("side")];
        coverName = nbt.getString("name");
    }

    @Override
    public void save(NBTTagCompound nbt) {
        nbt.setTag("pos", NBTUtil.createPosTag(pos));
        nbt.setInteger("dim", dim);
        nbt.setInteger("side", side.ordinal());
        nbt.setString("name", coverName);
    }
}

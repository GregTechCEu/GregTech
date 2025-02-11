package gregtech.common.pipelike.net.fluid;

import gregtech.api.GTValues;
import gregtech.api.fluids.FluidState;
import gregtech.api.fluids.attribute.FluidAttribute;
import gregtech.api.graphnet.logic.NetLogicEntry;
import gregtech.api.graphnet.logic.NetLogicType;
import gregtech.api.graphnet.predicate.test.FluidTestObject;
import gregtech.api.util.GTUtility;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.BitSet;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

public final class FluidContainmentLogic extends NetLogicEntry<FluidContainmentLogic, NBTTagCompound> {

    public static final FluidContainmentLogicType TYPE = new FluidContainmentLogicType();

    private int maximumTemperature;

    private final Set<ResourceLocation> containableAttributes = new ObjectOpenHashSet<>();
    private @NotNull EnumSet<FluidState> containableStates = EnumSet.noneOf(FluidState.class);

    @Override
    public @NotNull FluidContainmentLogicType getType() {
        return TYPE;
    }

    @Contract("_ -> this")
    public FluidContainmentLogic contain(FluidState state) {
        this.containableStates.add(state);
        return this;
    }

    @Contract("_ -> this")
    public FluidContainmentLogic contain(@NotNull FluidAttribute attribute) {
        this.containableAttributes.add(attribute.getResourceLocation());
        return this;
    }

    @Contract("_ -> this")
    public FluidContainmentLogic notContain(FluidState state) {
        this.containableStates.remove(state);
        return this;
    }

    @Contract("_ -> this")
    public FluidContainmentLogic notContain(@NotNull FluidAttribute attribute) {
        this.containableAttributes.remove(attribute.getResourceLocation());
        return this;
    }

    public boolean contains(FluidState state) {
        return this.containableStates.contains(state);
    }

    public boolean contains(@NotNull FluidAttribute attribute) {
        return this.containableAttributes.contains(attribute.getResourceLocation());
    }

    public boolean handles(FluidTestObject testObject) {
        return handles(testObject.recombine());
    }

    public boolean handles(FluidStack stack) {
        if (!contains(FluidState.inferState(stack))) return false;
        for (FluidAttribute attribute : FluidAttribute.inferAttributes(stack)) {
            if (!contains(attribute)) return false;
        }
        return true;
    }

    public void setMaximumTemperature(int maximumTemperature) {
        this.maximumTemperature = maximumTemperature;
    }

    public int getMaximumTemperature() {
        return maximumTemperature;
    }

    @Override
    public @Nullable FluidContainmentLogic union(NetLogicEntry<?, ?> other) {
        if (other instanceof FluidContainmentLogic logic) {
            if (this.containableAttributes.equals(logic.containableAttributes) &&
                    this.containableStates.equals(logic.containableStates)) {
                return this;
            } else {
                FluidContainmentLogic returnable = TYPE.getNew();
                returnable.containableStates = EnumSet.copyOf(this.containableStates);
                returnable.containableStates.retainAll(logic.containableStates);
                returnable.containableAttributes.addAll(this.containableAttributes);
                returnable.containableAttributes.retainAll(logic.containableAttributes);
                return returnable;
            }
        }
        return this;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        NBTTagList list = new NBTTagList();
        for (ResourceLocation loc : containableAttributes) {
            list.appendTag(new NBTTagString(loc.toString()));
        }
        tag.setTag("Attributes", list);
        tag.setByteArray("States", GTUtility.setToMask(containableStates).toByteArray());
        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        NBTTagList list = nbt.getTagList("Attributes", 8);
        for (int i = 0; i < list.tagCount(); i++) {
            containableAttributes.add(new ResourceLocation(list.getStringTagAt(i)));
        }
        containableStates = GTUtility.maskToSet(FluidState.class, BitSet.valueOf(nbt.getByteArray("States")));
    }

    @Override
    public void encode(PacketBuffer buf, boolean fullChange) {
        buf.writeVarInt(containableAttributes.size());
        for (ResourceLocation loc : containableAttributes) {
            buf.writeString(loc.toString());
        }
        buf.writeByteArray(GTUtility.setToMask(containableStates).toByteArray());
    }

    @Override
    public void decode(PacketBuffer buf, boolean fullChange) {
        int attributes = buf.readVarInt();
        for (int i = 0; i < attributes; i++) {
            containableAttributes.add(new ResourceLocation(buf.readString(255)));
        }
        containableStates = GTUtility.maskToSet(FluidState.class, BitSet.valueOf(buf.readByteArray(255)));
    }

    public static final class FluidContainmentLogicType extends NetLogicType<FluidContainmentLogic> {

        public FluidContainmentLogicType() {
            super(GTValues.MODID, "FluidContainment", FluidContainmentLogic::new,
                    new FluidContainmentLogic().contain(FluidState.LIQUID));
        }

        public @NotNull FluidContainmentLogic getWith(Collection<FluidState> states,
                                                      @NotNull Collection<FluidAttribute> attributes,
                                                      int maximumTemperature) {
            FluidContainmentLogic logic = getNew();
            logic.containableStates.addAll(states);
            for (FluidAttribute attribute : attributes) {
                logic.contain(attribute);
            }
            logic.maximumTemperature = maximumTemperature;
            return logic;
        }
    }
}

package gregtech.api.worldgen.generator;

import gregtech.api.GTValues;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.Callable;

@EventBusSubscriber
public class GTWorldGenCapability {

    private int maxHeight;
    private int maxBottomHeight;

    public void readFromNBT(NBTTagCompound tagCompound) {
        this.maxHeight = tagCompound.getInteger("H");
        if (tagCompound.hasKey("BH")) {
            this.maxBottomHeight = maxHeight;
        } else this.maxBottomHeight = maxHeight;
    }

    public NBTTagCompound writeToNBT() {
        NBTTagCompound tagCompound = new NBTTagCompound();
        if (maxHeight != 0) tagCompound.setInteger("H", maxHeight);
        if (maxBottomHeight != maxHeight) tagCompound.setInteger("BH", maxBottomHeight);
        return tagCompound;
    }

    public void setFrom(GTWorldGenCapability other) {
        this.maxHeight = other.maxHeight;
        this.maxBottomHeight = other.maxBottomHeight;
    }

    public int getMaxHeight() {
        return maxHeight;
    }

    public int getMaxBottomHeight() {
        return maxBottomHeight;
    }

    public void setMaxHeight(int maxHeight, int maxLiquidHeight) {
        this.maxHeight = maxHeight;
        this.maxBottomHeight = maxLiquidHeight;
    }

    @CapabilityInject(GTWorldGenCapability.class)
    public static Capability<GTWorldGenCapability> CAPABILITY;

    private static final ResourceLocation CAPABILITY_ID = new ResourceLocation(GTValues.MODID, "worldgen");

    public static final Callable<GTWorldGenCapability> FACTORY = GTWorldGenCapability::new;

    public static final IStorage<GTWorldGenCapability> STORAGE = new IStorage<GTWorldGenCapability>() {
        @Override
        public NBTBase writeNBT(Capability<GTWorldGenCapability> capability, GTWorldGenCapability instance, EnumFacing side) {
            return instance.writeToNBT();
        }

        @Override
        public void readNBT(Capability<GTWorldGenCapability> capability, GTWorldGenCapability instance, EnumFacing side, NBTBase nbt) {
            instance.readFromNBT((NBTTagCompound) nbt);
        }
    };

    private static class WorldGenCapabilityProvider implements ICapabilityProvider, INBTSerializable<NBTTagCompound> {

        private final GTWorldGenCapability capabilityInstance = new GTWorldGenCapability();

        @Override
        public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
            return capability == CAPABILITY;
        }

        @Nullable
        @Override
        public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
            if (capability == CAPABILITY) {
                return CAPABILITY.cast(capabilityInstance);
            }
            return null;
        }

        @Override
        public NBTTagCompound serializeNBT() {
            return capabilityInstance.writeToNBT();
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt) {
            capabilityInstance.readFromNBT(nbt);
        }
    }

    @SubscribeEvent
    public static void attachChunkCapabilities(AttachCapabilitiesEvent<Chunk> event) {
        event.addCapability(CAPABILITY_ID, new WorldGenCapabilityProvider());
    }
}

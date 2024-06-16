package gregtech.api.metatileentity.registry;

import gregtech.api.GTValues;
import gregtech.api.block.machines.BlockMachine;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.util.GTControlledRegistry;
import gregtech.integration.groovy.GroovyScriptModule;

import net.minecraft.util.ResourceLocation;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public final class MTERegistry extends GTControlledRegistry<ResourceLocation, MetaTileEntity> {

    private final String modid;
    private final int networkId;

    private BlockMachine block;

    public MTERegistry(@NotNull String modid, int networkId) {
        super(Short.MAX_VALUE);
        this.modid = modid;
        this.networkId = networkId;
    }

    @Override
    public void register(int id, @NotNull ResourceLocation key, @NotNull MetaTileEntity value) {
        if (!checkModid(key.getNamespace())) {
            throw new IllegalArgumentException("Cannot register MTE to another mod's registry");
        }
        super.register(id, key, value);
    }

    /**
     * @param modid the modid to test
     * @return if the modid is allowed to register to this registry
     */
    private boolean checkModid(@NotNull String modid) {
        if (!this.modid.equals(modid)) {
            // if we are the GT registry, allow GroovyScript to register to it anyway
            // otherwise allow no one to register to it
            return this.modid.equals(GTValues.MODID) && modid.equals(GroovyScriptModule.getPackId());
        }
        return true;
    }

    /**
     * @return the modid of this registry
     */
    public @NotNull String getModid() {
        return this.modid;
    }

    /**
     * @return the id used to represent this MTE Registry over the network
     */
    public int getNetworkId() {
        return this.networkId;
    }

    /**
     * @return the Block associated with this registry
     */
    public @NotNull BlockMachine getBlock() {
        return this.block;
    }

    /**
     * Initialize the registry's Block
     *
     * @param block the block to set
     */
    @ApiStatus.Internal
    public void setBlock(@NotNull BlockMachine block) {
        this.block = block;
    }

    @Override
    public String toString() {
        return "MTERegistry{" +
                "modid='" + modid + '\'' +
                ", networkId=" + networkId +
                ", block=" + block +
                '}';
    }
}

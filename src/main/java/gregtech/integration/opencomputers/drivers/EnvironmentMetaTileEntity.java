package gregtech.integration.opencomputers.drivers;

import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;

import li.cil.oc.api.driver.NamedBlock;
import li.cil.oc.integration.ManagedTileEntityEnvironment;

public abstract class EnvironmentMetaTileEntity<T> extends ManagedTileEntityEnvironment<T> implements NamedBlock {

    private final String preferredName;

    public EnvironmentMetaTileEntity(IGregTechTileEntity holder, T capability, String name) {
        super(capability, name);
        preferredName = holder.getMetaTileEntity().metaTileEntityId.getPath();
    }

    @Override
    public String preferredName() {
        return preferredName;
    }

    @Override
    public int priority() {
        return 0;
    }
}

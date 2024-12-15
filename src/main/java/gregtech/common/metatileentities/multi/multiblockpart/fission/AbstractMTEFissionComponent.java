package gregtech.common.metatileentities.multi.multiblockpart.fission;

import gregtech.api.GTValues;
import gregtech.api.fission.component.FissionComponent;
import gregtech.api.fission.component.FissionComponentData;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockPart;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class AbstractMTEFissionComponent<T extends FissionComponentData> extends MetaTileEntityMultiblockPart implements FissionComponent, IMultiblockAbilityPart<FissionComponent> {

    protected @Nullable T componentData;
    protected boolean locked;

    public AbstractMTEFissionComponent(@NotNull ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, GTValues.EV);
    }

    @Override
    public final void lock() {
        this.locked = true;
    }

    @Override
    public final void unlock() {
        this.locked = false;
    }

    @Override
    public final MultiblockAbility<FissionComponent> getAbility() {
        return MultiblockAbility.FISSION_COMPONENT;
    }

    @Override
    public final void registerAbilities(@NotNull List<FissionComponent> abilityList) {
        abilityList.add(this);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean("locked", locked);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.locked = data.getBoolean("locked");
    }
}

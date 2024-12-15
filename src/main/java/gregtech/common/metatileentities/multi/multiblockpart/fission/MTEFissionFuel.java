package gregtech.common.metatileentities.multi.multiblockpart.fission;

import gregtech.api.capability.impl.NotifiableItemStackHandler;
import gregtech.api.fission.component.ComponentDirection;
import gregtech.api.fission.component.FissionComponent;
import gregtech.api.fission.component.NeutronEmitter;
import gregtech.api.fission.component.ReactiveComponent;
import gregtech.api.fission.component.impl.data.FuelData;
import gregtech.api.fission.reactor.ReactorPathWalker;
import gregtech.api.fission.reactor.pathdata.NeutronPathData;
import gregtech.api.fission.reactor.pathdata.ReactivityPathData;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.TankWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;

import gregtech.api.util.GTTransferUtils;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.items.IItemHandlerModifiable;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MTEFissionFuel extends MTEFissionItemComponent<FuelData> implements NeutronEmitter,
                                                                                 ReactiveComponent {

    private boolean isOutputFull;

    public MTEFissionFuel(@NotNull ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MTEFissionFuel(metaTileEntityId);
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        // TODO lockable, filter
        return new NotifiableItemStackHandler(this, 1, this, false);
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        return new NotifiableItemStackHandler(this, 1, this, true);
    }

    @Override
    public void update() {
        super.update();
        if (getWorld().isRemote) {
            return;
        }

        if (isOutputFull) {
            if (componentData == null) {
                isOutputFull = false;
            } else if (!getNotifiedItemOutputList().isEmpty()) {
                if (!componentData.result.isEmpty()) {
                    ItemStack stack = componentData.result.copy();
                    for (IItemHandlerModifiable handler : getNotifiedItemOutputList()) {
                        if (GTTransferUtils.insertItem(handler, stack, true).isEmpty()) {
                            isOutputFull = false;
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    protected boolean acceptsComponentData() {
        assert componentData != null;
        if (componentData.result.isEmpty()) {
            return true;
        }

        if (GTTransferUtils.insertItem(exportItems, componentData.result, true).isEmpty()) {
            this.isOutputFull = false;
            return true;
        }
        return false;
    }

    @Override
    protected ModularUI createUI(@NotNull EntityPlayer entityPlayer) {
        return ModularUI.defaultBuilder()
                .label(5, 5, getMetaFullName())
                .slot(importItems, 0, 176 / 2 - 9 - 36, 40, GuiTextures.SLOT)
                .slot(exportItems, 0, 176 / 2 + 9 + 18, 40, GuiTextures.SLOT)
                .bindPlayerInventory(entityPlayer.inventory)
                .build(getHolder(), entityPlayer);
    }

    @Override
    protected @NotNull Class<FuelData> getDataClass() {
        return FuelData.class;
    }

    @Override
    protected void onDataSet(@Nullable NBTTagCompound tag) {
        if (tag != null && tag.hasKey("durability")) {
            this.durability = tag.getInteger("durability");
        } else {
            assert componentData != null;
            this.durability = componentData.durability;
        }
    }

    @Override
    protected void onDurabilityDepleted(@NotNull ItemStack stack) {
        assert componentData != null;
        if (componentData.result.isEmpty()) {
            return;
        }

        ItemStack produced = componentData.result.copy();
        if (GTTransferUtils.insertItem(exportItems, produced, true).isEmpty()) {
            GTTransferUtils.insertItem(exportItems, produced, false);
            this.isOutputFull = false;
        } else {
            this.isOutputFull = true;
        }
    }

    @Override
    protected boolean extractExtra() {
        assert componentData != null;
        if (componentData.result.isEmpty()) {
            return super.extractExtra();
        }

        ItemStack produced = componentData.result.copy();
        if (GTTransferUtils.insertItem(exportItems, produced, true).isEmpty()) {
            return super.extractExtra();
        }
        this.isOutputFull = true;
        return false;
    }

    @Override
    protected int dataDurability() {
        assert componentData != null;
        return componentData.durability;
    }

    @Override
    public void processNeutronPath(@NotNull ReactorPathWalker walker, @NotNull List<NeutronPathData> neutronData,
                                   @NotNull List<ReactivityPathData> reactivityData, @NotNull FissionComponent source,
                                   @NotNull ComponentDirection direction, int r, int c, float neutrons) {
        // neutron hits a reactive fuel
        neutronData.add(NeutronPathData.of(this, direction, () -> durability > 0 ? neutrons : 0));
    }

    @Override
    public float generateNeutrons() {
        assert componentData != null;
        return componentData.emission;
    }

    @Override
    public boolean canReact() {
        return durability > 0 && !isOutputFull;
    }

    @Override
    public float react(float amount) {
        assert componentData != null;
        return Math.min(amount, durability);
    }

    @Override
    public float heatPerFission() {
        assert componentData != null;
        return componentData.heatPerFission;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean("isOutputFull", isOutputFull);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.isOutputFull = data.getBoolean("isOutputFull");
    }
}

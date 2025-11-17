package gregtech.common.metatileentities.electric;

import gregtech.api.GTValues;
import gregtech.api.items.itemhandlers.GTItemStackHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.TieredMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuis;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.util.GTTransferUtils;
import gregtech.client.renderer.texture.Textures;

import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.items.IItemHandlerModifiable;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandlers;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.layout.Grid;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MetaTileEntityFisher extends TieredMetaTileEntity {

    private static final int WATER_CHECK_SIZE = 25;

    private final int inventorySize;
    private final long fishingTicks;
    private final long energyAmountPerFish;

    public MetaTileEntityFisher(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier);
        this.inventorySize = (tier + 1) * (tier + 1);
        this.fishingTicks = 1000 - tier * 200L;
        this.energyAmountPerFish = GTValues.V[tier];
        initializeInventory();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityFisher(metaTileEntityId, getTier());
    }

    @Override
    public boolean usesMui2() {
        return true;
    }

    @Override
    public ModularPanel buildUI(PosGuiData guiData, PanelSyncManager guiSyncManager, UISettings settings) {
        int rowSize = (int) Math.sqrt(inventorySize);
        guiSyncManager.registerSlotGroup("item_in", 1);
        guiSyncManager.registerSlotGroup("item_out", rowSize);

        List<List<IWidget>> widgets = new ArrayList<>();
        for (int i = 0; i < rowSize; i++) {
            widgets.add(new ArrayList<>());
            for (int j = 0; j < rowSize; j++) {
                int index = i * rowSize + j;
                widgets.get(i).add(new ItemSlot()
                        .slot(SyncHandlers.itemSlot(exportItems, index)
                                .slotGroup("item_out")
                                .accessibility(false, true)));
            }
        }

        return GTGuis.createPanel(this, 176, 18 + 18 * rowSize + 94)
                .child(IKey.lang(getMetaFullName()).asWidget().pos(5, 5))
                .child(SlotGroupWidget.playerInventory(false).left(7).bottom(7))
                .child(new ItemSlot().slot(SyncHandlers.itemSlot(importItems, 0)
                        .slotGroup("item_in"))
                        .background(GTGuiTextures.SLOT, GTGuiTextures.STRING_SLOT_OVERLAY)
                        .pos(7 + 9, 9 * (rowSize + 1)))
                .child(new Grid()
                        .top(18).alignX(0.5f)
                        .height(rowSize * 18)
                        .minElementMargin(0, 0)
                        .minColWidth(18).minRowHeight(18)
                        .matrix(widgets));
    }

    @Override
    public void update() {
        super.update();
        ItemStack baitStack = importItems.getStackInSlot(0);
        if (!getWorld().isRemote && energyContainer.getEnergyStored() >= energyAmountPerFish &&
                getOffsetTimer() % fishingTicks == 0L && !baitStack.isEmpty()) {
            WorldServer world = (WorldServer) this.getWorld();
            int waterCount = 0;
            int edgeSize = (int) Math.sqrt(WATER_CHECK_SIZE);
            for (int x = 0; x < edgeSize; x++) {
                for (int z = 0; z < edgeSize; z++) {
                    BlockPos waterCheckPos = getPos().down().add(x - edgeSize / 2, 0, z - edgeSize / 2);
                    if (world.getBlockState(waterCheckPos).getBlock() instanceof BlockLiquid &&
                            world.getBlockState(waterCheckPos).getMaterial() == Material.WATER) {
                        waterCount++;
                    }
                }
            }
            if (waterCount == WATER_CHECK_SIZE) {
                LootTable table = world.getLootTableManager().getLootTableFromLocation(LootTableList.GAMEPLAY_FISHING);
                List<ItemStack> itemStacks = table.generateLootForPools(world.rand,
                        new LootContext.Builder(world).build());
                if (GTTransferUtils.addItemsToItemHandler(exportItems, true, itemStacks)) {
                    GTTransferUtils.addItemsToItemHandler(exportItems, false, itemStacks);
                    energyContainer.removeEnergy(energyAmountPerFish);
                    baitStack.shrink(1);
                }
            }
        }
        if (!getWorld().isRemote && getOffsetTimer() % 5 == 0) {
            pushItemsIntoNearbyHandlers(getFrontFacing());
        }
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return new GTItemStackHandler(this, 1) {

            @NotNull
            @Override
            public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
                if (OreDictUnifier.hasOreDictionary(stack, "string")) {
                    return super.insertItem(slot, stack, simulate);
                }
                return stack;
            }
        };
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        return new GTItemStackHandler(this, inventorySize);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        Textures.SCREEN.renderSided(EnumFacing.UP, renderState, translation, pipeline);
        Textures.PIPE_OUT_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
        Textures.PIPE_IN_OVERLAY.renderSided(EnumFacing.DOWN, renderState, translation, pipeline);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.machine.fisher.tooltip"));
        tooltip.add(I18n.format("gregtech.machine.fisher.speed", fishingTicks));
        tooltip.add(I18n.format("gregtech.machine.fisher.requirement", (int) Math.sqrt(WATER_CHECK_SIZE),
                (int) Math.sqrt(WATER_CHECK_SIZE)));
        tooltip.add(I18n.format("gregtech.universal.tooltip.voltage_in", energyContainer.getInputVoltage(),
                GTValues.VNF[getTier()]));
        tooltip.add(
                I18n.format("gregtech.universal.tooltip.energy_storage_capacity", energyContainer.getEnergyCapacity()));
    }

    @Override
    public boolean getIsWeatherOrTerrainResistant() {
        return true;
    }

    @Override
    public void addToolUsages(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.tool_action.screwdriver.access_covers"));
        tooltip.add(I18n.format("gregtech.tool_action.wrench.set_facing"));
        super.addToolUsages(stack, world, tooltip, advanced);
    }
}

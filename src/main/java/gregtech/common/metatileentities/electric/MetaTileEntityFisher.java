package gregtech.common.metatileentities.electric;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.GTValues;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.TieredMetaTileEntity;
import gregtech.api.util.GTTransferUtils;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.client.renderer.texture.Textures;
import gregtech.api.unification.OreDictUnifier;
import gregtech.common.ConfigHolder;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        int rowSize = (int) Math.sqrt(inventorySize);

        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 176,
                18 + 18 * rowSize + 94)
                .label(10, 5, getMetaFullName())
                .widget(new SlotWidget(importItems, 0, 18, 18, true, true)
                        .setBackgroundTexture(GuiTextures.SLOT, GuiTextures.STRING_SLOT_OVERLAY));

        for (int y = 0; y < rowSize; y++) {
            for (int x = 0; x < rowSize; x++) {
                int index = y * rowSize + x;
                builder.widget(new SlotWidget(exportItems, index, 89 - rowSize * 9 + x * 18, 18 + y * 18, true, false)
                        .setBackgroundTexture(GuiTextures.SLOT));
            }
        }

        builder.bindPlayerInventory(entityPlayer.inventory, GuiTextures.SLOT, 7, 18 + 18 * rowSize + 12);
        return builder.build(getHolder(), entityPlayer);
    }

    @Override
    public void update() {
        super.update();
        ItemStack baitStack = importItems.getStackInSlot(0);
        if (!getWorld().isRemote && energyContainer.getEnergyStored() >= energyAmountPerFish && getOffsetTimer() % fishingTicks == 0L && !baitStack.isEmpty()) {
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
                NonNullList<ItemStack> itemStacks = NonNullList.create();
                itemStacks.addAll(table.generateLootForPools(world.rand, new LootContext.Builder(world).build()));
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
        return new ItemStackHandler(1) {
            @Nonnull
            @Override
            public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
                if (OreDictUnifier.getOreDictionaryNames(stack).contains("string")) {
                    return super.insertItem(slot, stack, simulate);
                }
                return stack;
            }
        };
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        return new ItemStackHandler(inventorySize);
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
        tooltip.add(I18n.format("gregtech.machine.fisher.tooltip"));
        if (ConfigHolder.machines.doTerrainExplosion)
            tooltip.add(I18n.format("gregtech.universal.tooltip.terrain_resist"));
        tooltip.add(I18n.format("gregtech.machine.fisher.speed", fishingTicks));
        tooltip.add(I18n.format("gregtech.machine.fisher.requirement", (int) Math.sqrt(WATER_CHECK_SIZE), (int) Math.sqrt(WATER_CHECK_SIZE)));
        tooltip.add(I18n.format("gregtech.universal.tooltip.voltage_in", energyContainer.getInputVoltage(), GTValues.VNF[getTier()]));
        tooltip.add(I18n.format("gregtech.universal.tooltip.energy_storage_capacity", energyContainer.getEnergyCapacity()));
    }

    @Override
    public boolean getIsWeatherOrTerrainResistant() {
        return true;
    }
}

package gregtech.common.metatileentities.storage;

import gregtech.api.capability.impl.FilteredFluidHandler;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.items.itemhandlers.GTItemStackHandler;
import gregtech.api.metatileentity.ITieredMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.mui.GTGuis;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.mui.widget.GTFluidSlot;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemStackHandler;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
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
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MetaTileEntityBuffer extends MetaTileEntity implements ITieredMetaTileEntity {

    private static final int TANK_SIZE = 64000;
    private final int tier;

    private FluidTankList fluidTankList;
    private ItemStackHandler itemStackHandler;

    public MetaTileEntityBuffer(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId);
        this.tier = tier;
        initializeInventory();
    }

    @Override
    protected void initializeInventory() {
        super.initializeInventory();
        FilteredFluidHandler[] fluidHandlers = new FilteredFluidHandler[tier + 2];
        for (int i = 0; i < tier + 2; i++) {
            fluidHandlers[i] = new FilteredFluidHandler(TANK_SIZE);
        }
        fluidInventory = fluidTankList = new FluidTankList(false, fluidHandlers);
        itemInventory = itemStackHandler = new GTItemStackHandler(this, ((int) Math.pow(tier + 2, 2)));
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityBuffer(metaTileEntityId, tier);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(Textures.VOLTAGE_CASINGS[tier].getParticleSprite(), this.getPaintingColorForRendering());
    }

    @Override
    public boolean usesMui2() {
        return true;
    }

    @Override
    public ModularPanel buildUI(PosGuiData guiData, PanelSyncManager guiSyncManager, UISettings settings) {
        int invTier = tier + 2;
        guiSyncManager.registerSlotGroup("item_inv", invTier);

        List<List<IWidget>> slotWidgets = new ArrayList<>();
        for (int y = 0; y < invTier; y++) {
            slotWidgets.add(new ArrayList<>());
            for (int x = 0; x < invTier; x++) {
                int index = y * invTier + x;
                slotWidgets.get(y)
                        .add(new ItemSlot().slot(SyncHandlers.itemSlot(itemStackHandler, index).slotGroup("item_inv")));
            }
        }

        List<List<IWidget>> tankWidgets = new ArrayList<>();
        for (int i = 0; i < this.fluidTankList.getTanks(); i++) {
            tankWidgets.add(new ArrayList<>());
            tankWidgets.get(i).add(new GTFluidSlot().syncHandler(this.fluidTankList.getTankAt(i)));
        }

        // TODO: Change the position of the name when it's standardized.
        return GTGuis.createPanel(this, 176, Math.max(166, 18 + 18 * invTier + 94))
                .child(IKey.lang(getMetaFullName()).asWidget().pos(5, 5))
                .child(SlotGroupWidget.playerInventory(false).left(7).bottom(7))
                .child(new Grid()
                        .top(18).height(18 * invTier)
                        .left(7)
                        .minElementMargin(0, 0)
                        .minColWidth(18).minRowHeight(18)
                        .matrix(slotWidgets))
                .child(new Grid()
                        .top(18).height(18 * invTier)
                        .left(144 + 7)
                        .minElementMargin(0, 0)
                        .minColWidth(18).minRowHeight(18)
                        .matrix(tankWidgets));
    }

    @Override
    public int getTier() {
        return tier;
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        Textures.VOLTAGE_CASINGS[tier].render(renderState, translation, ArrayUtils.add(pipeline,
                new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering()))));
        for (EnumFacing facing : EnumFacing.VALUES) {
            Textures.BUFFER_OVERLAY.renderSided(facing, renderState, translation, pipeline);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setTag("Inventory", itemStackHandler.serializeNBT());
        tag.setTag("FluidInventory", fluidTankList.serializeNBT());
        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        this.itemStackHandler.deserializeNBT(tag.getCompoundTag("Inventory"));
        this.fluidTankList.deserializeNBT(tag.getCompoundTag("FluidInventory"));
    }

    @Override
    protected boolean shouldSerializeInventories() {
        return false;
    }

    @Override
    public boolean hasFrontFacing() {
        return false;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.machine.buffer.tooltip"));
        tooltip.add(I18n.format("gregtech.universal.tooltip.item_storage_capacity", (int) Math.pow(tier + 2, 2)));
        tooltip.add(I18n.format("gregtech.universal.tooltip.fluid_storage_capacity_mult", tier + 2, TANK_SIZE));
    }

    @Override
    public void addToolUsages(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.tool_action.screwdriver.access_covers"));
        // TODO Add this when the Buffer gets an auto-output side, and change the above to
        // "gregtech.tool_action.screwdriver.auto_output_covers"
        // tooltip.add(I18n.format("gregtech.tool_action.wrench.set_facing"));
        super.addToolUsages(stack, world, tooltip, advanced);
    }

    @Override
    public void clearMachineInventory(@NotNull List<@NotNull ItemStack> itemBuffer) {
        clearInventory(itemBuffer, itemStackHandler);
    }
}

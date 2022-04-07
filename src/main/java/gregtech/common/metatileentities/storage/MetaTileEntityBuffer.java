package gregtech.common.metatileentities.storage;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.impl.FilteredFluidHandler;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.TankWidget;
import gregtech.api.metatileentity.ITieredMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.client.renderer.texture.Textures;
import gregtech.api.util.GTUtility;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemStackHandler;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.List;

public class MetaTileEntityBuffer extends MetaTileEntity implements ITieredMetaTileEntity {

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
            fluidHandlers[i] = new FilteredFluidHandler(64000);
        }
        fluidInventory = fluidTankList = new FluidTankList(false, fluidHandlers);
        itemInventory = itemStackHandler = new ItemStackHandler((int)Math.pow(tier + 2, 2));
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityBuffer(metaTileEntityId, tier);
    }

    @Override
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(Textures.VOLTAGE_CASINGS[tier].getParticleSprite(), this.getPaintingColorForRendering());
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        int invTier = tier + 2;
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND,
                176, Math.max(166, 18 + 18 * invTier + 94));//176, 166
        for (int i = 0; i < this.fluidTankList.getTanks(); i++) {
            builder.widget(new TankWidget(this.fluidTankList.getTankAt(i), 176 - 8 - 18, 18 + 18 * i, 18, 18)
                    .setAlwaysShowFull(true)
                    .setBackgroundTexture(GuiTextures.FLUID_SLOT)
                    .setContainerClicking(true, true));
        }
        for (int y = 0; y < invTier; y++) {
            for (int x = 0; x < invTier; x++) {
                int index = y * invTier + x;
                builder.slot(itemStackHandler, index, 8 + x * 18, 18 + y * 18, GuiTextures.SLOT);
            }
        }
        return builder.label(6, 6, getMetaFullName())
                .bindPlayerInventory(entityPlayer.inventory, GuiTextures.SLOT, 8, 18 + 18 * invTier + 12)
                .build(getHolder(), entityPlayer);
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
        tooltip.add(I18n.format("gregtech.machine.buffer.inventory", (int)Math.pow(tier + 2, 2)));
        tooltip.add(I18n.format("gregtech.machine.buffer.tanks", tier + 2));
    }

    @Override
    public void clearMachineInventory(NonNullList<ItemStack> itemBuffer) {
        clearInventory(itemBuffer, itemStackHandler);
    }
}

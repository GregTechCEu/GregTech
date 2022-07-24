package gregtech.common.covers.detector;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.CoverWithUI;
import gregtech.api.cover.ICoverable;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.*;
import gregtech.api.util.GTLog;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.*;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.Mod;
import scala.Int;

import javax.xml.soap.Text;

public class CoverDetectorEnergyLatch extends CoverBehavior implements CoverWithUI, ITickable{
    public int minPercent, maxPercent;
    private int outputAmount = 0;

    public CoverDetectorEnergyLatch(ICoverable coverHolder, EnumFacing attachedSide) {
        super(coverHolder, attachedSide);
        this.minPercent = 10;
        this.maxPercent = 90;
    }

    @Override
    public boolean canAttach() {
        return coverHolder.getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, null) != null;
    }

    @Override
    public void renderCover(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, Cuboid6 plateBox, BlockRenderLayer layer) {
        Textures.DETECTOR_ENERGY.renderSided(attachedSide, plateBox, renderState, pipeline, translation);
    }

    @Override
    public EnumActionResult onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, CuboidRayTraceResult hitResult){
        if (!this.coverHolder.getWorld().isRemote) {
            openUI((EntityPlayerMP) playerIn);
        }
        return EnumActionResult.SUCCESS;
    }

    @Override
    public void update() {
        if (coverHolder.getOffsetTimer() % 20 != 0)
            return;

        IEnergyContainer energyContainer = coverHolder.getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, null);
        if (energyContainer != null && energyContainer.getEnergyCapacity() > 0){
            float currentStorageRatio = (float) energyContainer.getEnergyStored() / (float) energyContainer.getEnergyCapacity();

            if (currentStorageRatio >= maxPercent){
                outputAmount = 15;
            }
            if (currentStorageRatio <= minPercent){
                outputAmount = 0;
            }
            setRedstoneSignalOutput(outputAmount);
            GTLog.logger.warn(
                    "Storage Ratio: " + currentStorageRatio +
                    "\nMin Percent: " + minPercent +
                    "\nMax Percent: " + maxPercent
            );
        }
    }

    protected ModularUI buildUI(ModularUI.Builder builder, EntityPlayer player){
        return builder.build(this, player);
    }

    @Override
    public ModularUI createUI(EntityPlayer player) {
        WidgetGroup primaryGroup = new WidgetGroup();
        primaryGroup.addWidget(new ImageWidget(42, 22, 92, 20, GuiTextures.DISPLAY));
        primaryGroup.addWidget(new TextFieldWidget2(42, 26, 92, 20, () -> String.valueOf((minPercent)), val ->{
                    if (val != null && !val.isEmpty()) {
                        minPercent = Math.min(maxPercent - 1, Integer.parseInt(val));
                    }
                })
            .setNumbersOnly(0, 100)
            .setMaxLength(3)
        );

        primaryGroup.addWidget(new ImageWidget(42, 42 + 5, 92, 20, GuiTextures.DISPLAY));
        primaryGroup.addWidget(new TextFieldWidget2(42, 46 + 5, 92, 20, () -> String.valueOf((maxPercent)), val ->{
            if (val != null && !val.isEmpty())
                maxPercent = Math.max(minPercent + 1, Integer.parseInt(val));
        })
                .setNumbersOnly(0, 100)
                .setMaxLength(3)
        );

        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 176, 105 + 82)
                .widget(primaryGroup)
                .bindPlayerInventory(player.inventory, GuiTextures.SLOT, 7, 190);

        return buildUI(builder, player);
    }

    @Override
    public boolean canConnectRedstone() {
        return true;
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("minPercent", this.minPercent);
        tagCompound.setInteger("maxPercent", this.maxPercent);
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        this.minPercent = tagCompound.getInteger("minPercent");
        this.maxPercent = tagCompound.getInteger("maxPercent");
    }

    @Override
    public void writeInitialSyncData(PacketBuffer packetBuffer) {
        packetBuffer.writeInt(this.minPercent);
        packetBuffer.writeInt(this.maxPercent);
    }

    @Override
    public void readInitialSyncData(PacketBuffer packetBuffer) {
        this.minPercent = packetBuffer.readInt();
        this.maxPercent = packetBuffer.readInt();
    }
}

package gregtech.common.covers.detector;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.widget.Widget;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.CoverWithUI;
import gregtech.api.cover.ICoverable;
import gregtech.api.gui.GregTechUI;
import gregtech.api.gui.GuiTextures;
import com.cleanroommc.modularui.api.drawable.Text;
import com.cleanroommc.modularui.api.math.Alignment;
import com.cleanroommc.modularui.api.screen.ModularWindow;
import com.cleanroommc.modularui.api.screen.UIBuildContext;
import com.cleanroommc.modularui.common.widget.*;
import com.cleanroommc.modularui.common.widget.textfield.TextFieldWidget;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.*;


public class CoverDetectorEnergyAdvanced extends CoverBehavior implements CoverWithUI, ITickable{
    public int minPercent, maxPercent;
    private int outputAmount;
    private boolean inverted;

    public CoverDetectorEnergyAdvanced (ICoverable coverHolder, EnumFacing attachedSide) {
        super(coverHolder, attachedSide);
        this.minPercent = 10;
        this.maxPercent = 90;
        this.outputAmount = 0;
        this.inverted = false;
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
            // openUI((EntityPlayerMP) playerIn);
            GregTechUI.getCoverUi(attachedSide).open(playerIn, coverHolder.getWorld(), coverHolder.getPos());
        }
        return EnumActionResult.SUCCESS;
    }

    @Override
    public void update() {
        if (coverHolder.getOffsetTimer() % 20 != 0)
            return;

        IEnergyContainer energyContainer = coverHolder.getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, null);
        if (energyContainer != null && energyContainer.getEnergyCapacity() > 0){
            float currentStorageRatio = 100f * energyContainer.getEnergyStored() / energyContainer.getEnergyCapacity();

            if (currentStorageRatio >= maxPercent){
                if (inverted)
                    outputAmount = 15;
                else
                    outputAmount = 0;
            }
            if (currentStorageRatio <= minPercent){
                if (inverted)
                    outputAmount = 0;
                else
                    outputAmount = 15;
            }
            setRedstoneSignalOutput(outputAmount);
        }
    }

    @Override
    public ModularWindow createWindow(UIBuildContext buildContext) {
        ModularWindow.Builder builder = ModularWindow.builder(176, 105);
        builder.setBackground(GuiTextures.VANILLA_BACKGROUND)
                .widget(new Column()
                        .widget(new Row()
                                .widget(new TextWidget(new Text("cover.advanced_energy_detector.min_percent").localise())
                                        .setTextAlignment(Alignment.CenterLeft)
                                        .setSize(100, 10)
                                )
                                .widget(new TextFieldWidget()
                                        .setGetterInt(() -> minPercent)
                                        .setSetterInt(this::setMinPercent)
                                        .setMaxLength(3)
                                        .setNumbers(0, 100)
                                        .setTextAlignment(Alignment.CenterLeft)
                                        .setSize(20, 10)
                                        .setPos(10, 0)
                                        .setBackground(GuiTextures.DISPLAY_SMALL)
                                )

                        )
                        .widget(new Row()
                                .widget(new TextWidget(new Text("cover.advanced_energy_detector.max_percent").localise())
                                        .setTextAlignment(Alignment.CenterLeft)
                                        .setSize(100, 10)
                                )
                                .widget(new TextFieldWidget()
                                        .setGetterInt(() -> maxPercent)
                                        .setSetterInt(this::setMaxPercent)
                                        .setMaxLength(3)
                                        .setNumbers(0, 100)
                                        .setTextAlignment(Alignment.CenterLeft)
                                        .setSize(20, 10)
                                        .setPos(10, 0)
                                        .setBackground(GuiTextures.DISPLAY_SMALL)
                                )
                                .setPos(0, 10)
                        )
                        .widget(new Row()
                                .widget(new ButtonWidget()
                                        .setOnClick(this::toggleInvert)
                                        .addTooltip(Text.localised("cover.advanced_energy_detector.invert_tooltip"))
                                        .setSize(120, 10)
                                        .setBackground(GuiTextures.BUTTON, new Text("cover.advanced_energy_detector.invert_label").localise())
                                )
                                .setPos(0, 10)
                        )
                        .setPos(16, 16)
                );
        return builder.build();
    }

    private void setMinPercent(int val){
        minPercent = Math.min(maxPercent - 1, val);
    }
    private void setMaxPercent(int val){
        maxPercent = Math.max(minPercent + 1, val);
    }
    private void toggleInvert(Widget.ClickData data, Widget widget){
        inverted = !inverted;
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
        tagCompound.setInteger("outputAmount", outputAmount);
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        this.minPercent = tagCompound.getInteger("minPercent");
        this.maxPercent = tagCompound.getInteger("maxPercent");
        this.outputAmount = tagCompound.getInteger("outputAmount");
    }

    @Override
    public void writeInitialSyncData(PacketBuffer packetBuffer) {
        packetBuffer.writeInt(this.minPercent);
        packetBuffer.writeInt(this.maxPercent);
        packetBuffer.writeInt(this.outputAmount);
    }

    @Override
    public void readInitialSyncData(PacketBuffer packetBuffer) {
        this.minPercent = packetBuffer.readInt();
        this.maxPercent = packetBuffer.readInt();
        this.outputAmount = packetBuffer.readInt();
    }
}

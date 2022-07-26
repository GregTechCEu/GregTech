package gregtech.common.covers.detector;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.widget.ISyncedWidget;
import com.cleanroommc.modularui.api.widget.IWidgetParent;
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
import gregtech.api.util.GTLog;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.*;


public class CoverDetectorEnergyAdvanced extends CoverBehavior implements CoverWithUI, ITickable{
    public int minPercent, maxPercent;
    public int minEU, maxEU;
    private int outputAmount;
    private boolean inverted;
    private boolean useEU;
    private int maxLength,  maxEnterable;

    public CoverDetectorEnergyAdvanced (ICoverable coverHolder, EnumFacing attachedSide) {
        super(coverHolder, attachedSide);
        this.minPercent = 10;
        this.maxPercent = 90;
        this.minEU = 512;
        this.maxEU = 2048;
        this.outputAmount = 0;
        this.inverted = false;
        this.useEU = false;
        this.maxLength = 3;
        this.maxEnterable = 100;
    }

    @Override
    public boolean canAttach() {
        return coverHolder.getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, null) != null;
    }

    // TODO: use unique texture for advanced energy detector
    @Override
    public void renderCover(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, Cuboid6 plateBox, BlockRenderLayer layer) {
        Textures.DETECTOR_ENERGY.renderSided(attachedSide, plateBox, renderState, pipeline, translation);
    }

    @Override
    public EnumActionResult onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, CuboidRayTraceResult hitResult){
        if (!this.coverHolder.getWorld().isRemote) {
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

            if (useEU) {
                compareValue((float) energyContainer.getEnergyStored(), maxEU, minEU);
            } else {
                compareValue(currentStorageRatio, maxPercent, minPercent);
            }
            setRedstoneSignalOutput(outputAmount);
        }
    }

    private void compareValue (float value, int maxValue, int minValue) {
        if (value >= maxValue){
            outputAmount = inverted ? 15 : 0;
        }

        if (value <= minValue) {
            outputAmount = inverted ? 0 : 15;
        }
    }

    @Override
    public ModularWindow createWindow(UIBuildContext buildContext) {
        ModularWindow.Builder builder = ModularWindow.builder(188, 90);

        builder.setBackground(GuiTextures.VANILLA_BACKGROUND)
                .widget(new Column()
                        .widget(new Row()
                                .widget(new TextWidget(Text.localised("cover.advanced_energy_detector.min"))
                                        .setTextAlignment(Alignment.CenterLeft)
                                        .setSize(100, 16)
                                )
                                .widget(new TextFieldWidget()
                                        .setGetterInt(this::getMinValue)
                                        .setSetterInt(this::setMinValue)
                                        .setMaxLength(maxLength)
                                        .setNumbers(0, maxEnterable)
                                        .setTextAlignment(Alignment.CenterLeft)
                                        .setSize(80, 16)
                                        .setBackground(GuiTextures.DISPLAY_SMALL)
                                )
                        )
                        .widget(new Row()
                                .widget(new TextWidget(Text.localised("cover.advanced_energy_detector.max"))
                                        .setTextAlignment(Alignment.CenterLeft)
                                        .setSize(100, 16)
                                )
                                .widget(new TextFieldWidget()
                                        .setGetterInt(this::getMaxValue)
                                        .setSetterInt(this::setMaxValue)
                                        .setMaxLength(maxLength)
                                        .setNumbers(0, maxEnterable)
                                        .setTextAlignment(Alignment.CenterLeft)
                                        .setSize(80, 16)
                                        .setBackground(GuiTextures.DISPLAY_SMALL)
                                )
                        )
                        .widget(new Row()
                                .widget(new ButtonWidget()
                                        .setOnClick(this::toggleInvert)
                                        .addTooltip(Text.localised("cover.advanced_energy_detector.invert_tooltip"))
                                        .setBackground(GuiTextures.BASE_BUTTON, Text.localised("cover.advanced_energy_detector.invert_label", this.inverted))
                                        .setSize(180, 16)
                                )
                        )
                        .widget(new Row()
                                .widget(new ButtonWidget()
                                        .setOnClick(this::toggleEU)
                                        .addTooltip(Text.localised("cover.advanced_energy_detector.toggle_EU_tooltip"))
                                        .setBackground(GuiTextures.BASE_BUTTON, Text.localised("cover.advanced_energy_detector.toggle_EU_label", this.useEU))
                                        .setSize(180, 16)
                                )
                        )
                        .setPos(4, 4)
                );

        return builder.build();
    }

    private int getMinValue() {
        return useEU ? minEU : minPercent;
    }

    private void setMinValue(int val){
        if (useEU)
            minEU = Math.min(maxEU - 1, val);
        else
            minPercent = Math.min(maxPercent - 1, val);
    }

    private int getMaxValue() {
        return useEU ? maxEU : maxPercent;
    }

    private void setMaxValue(int val){
        if (useEU)
            maxEU = Math.max(minEU + 1, val);
        else
            maxPercent = Math.max(minPercent + 1, val);
    }

    private void toggleInvert(Widget.ClickData data, Widget widget){
        inverted = !inverted;
        widget.setBackground(GuiTextures.BASE_BUTTON, Text.localised("cover.advanced_energy_detector.invert_label", this.inverted));
    }

    private void updateFields(int val, ISyncedWidget syncedWidget) {
        if (syncedWidget instanceof TextFieldWidget) {
            ((TextFieldWidget) syncedWidget)
                    .setMaxLength(maxLength)
                    .setNumbers(0, maxEnterable);
        }
    }

    private void toggleEU(Widget.ClickData data, Widget widget){
        // i have to invert useEU for some godforsaken reason
        if (!useEU) {
            maxLength = 10;
            maxEnterable = Integer.MAX_VALUE;
        } else {
            maxLength = 3;
            maxEnterable = 100;
        }
        widget.getWindow().syncedWidgets.forEach(this::updateFields);

        useEU = !useEU;
        widget.setBackground(GuiTextures.BASE_BUTTON, Text.localised("cover.advanced_energy_detector.toggle_EU_label", this.useEU));
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
        tagCompound.setInteger("maxEU", this.maxEU);
        tagCompound.setInteger("minEU", this.minEU);
        tagCompound.setInteger("outputAmount", outputAmount);
        tagCompound.setBoolean("inverted", this.inverted);
        tagCompound.setBoolean("useEU", this.useEU);
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        this.minPercent = tagCompound.getInteger("minPercent");
        this.maxPercent = tagCompound.getInteger("maxPercent");
        this.minEU = tagCompound.getInteger("minEU");
        this.maxEU = tagCompound.getInteger("maxEU");
        this.outputAmount = tagCompound.getInteger("outputAmount");
        this.inverted = tagCompound.getBoolean("inverted");
        this.useEU = tagCompound.getBoolean("useEU");
    }

    @Override
    public void writeInitialSyncData(PacketBuffer packetBuffer) {
        packetBuffer.writeInt(this.minPercent);
        packetBuffer.writeInt(this.maxPercent);
        packetBuffer.writeInt(this.minEU);
        packetBuffer.writeInt(this.maxEU);
        packetBuffer.writeInt(this.outputAmount);
        packetBuffer.writeBoolean(this.inverted);
        packetBuffer.writeBoolean(this.useEU);
    }

    @Override
    public void readInitialSyncData(PacketBuffer packetBuffer) {
        this.minPercent = packetBuffer.readInt();
        this.maxPercent = packetBuffer.readInt();
        this.minEU = packetBuffer.readInt();
        this.maxEU = packetBuffer.readInt();
        this.outputAmount = packetBuffer.readInt();
        this.inverted = packetBuffer.readBoolean();
        this.useEU = packetBuffer.readBoolean();
    }
}

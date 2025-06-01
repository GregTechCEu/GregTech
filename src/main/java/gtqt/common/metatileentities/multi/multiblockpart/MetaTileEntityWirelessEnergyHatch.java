package gtqt.common.metatileentities.multi.multiblockpart;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;

import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;

import com.cleanroommc.modularui.value.sync.StringSyncValue;

import gregtech.api.GTValues;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.AdvancedTextWidget;
import gregtech.api.gui.widgets.IncrementButtonWidget;
import gregtech.api.gui.widgets.LabelWidget;
import gregtech.api.gui.widgets.ScrollableListWidget;
import gregtech.api.gui.widgets.TextFieldWidget2;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.AbilityInstances;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.mui.GTGuis;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.SimpleOverlayRenderer;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockPart;

import gtqt.api.util.wireless.EnergyContainerWireless;

import gtqt.api.util.wireless.NetworkDatabase;
import gtqt.api.util.wireless.NetworkNode;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MetaTileEntityWirelessEnergyHatch extends MetaTileEntityMultiblockPart
        implements IMultiblockAbilityPart<IEnergyContainer> {
    private final int amperage;
    private final boolean isExport;
    private final EnergyContainerWireless energyContainer;
    public int WirelessId=-1;
    public MetaTileEntityWirelessEnergyHatch(ResourceLocation metaTileEntityId, int tier, int amperage, boolean isExport) {
        super(metaTileEntityId, tier);
        this.isExport = isExport;
        this.amperage = amperage;
        energyContainer = new EnergyContainerWireless(this,isExport, GTValues.V[tier],this.amperage);

    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityWirelessEnergyHatch(this.metaTileEntityId,this.getTier(),this.amperage,this.isExport);
    }
    public void setCurrentWirelessID(int parallelAmount) {
        this.WirelessId = Math.min(Math.max(this.WirelessId + parallelAmount,1),(int) Math.pow((getTier()+1),2));
    }
    public String getWirelessidToString() {
        return Integer.toString(this.WirelessId);
    }
    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        ModularUI.Builder builder;
        builder = ModularUI.builder(GuiTextures.BACKGROUND, 198, 208);
        builder.widget((new AdvancedTextWidget(9, 8, this::addDisplayText, 167427215)));
        builder.widget(new LabelWidget(8,9,"==========无线网络============"));
        builder.widget(new LabelWidget(8,18,"当前频道:"));
        builder.widget(new IncrementButtonWidget(8, 27, 9, 9, 1, 4, 16, 64, this::setCurrentWirelessID)
                .setDefaultTooltip()
                .setShouldClientCallback(false));
        builder.widget(new IncrementButtonWidget(88, 27, 9, 9, -1, -4, -16, -64, this::setCurrentWirelessID)
                .setDefaultTooltip()
                .setShouldClientCallback(false));
        builder.widget(new TextFieldWidget2(40, 27, 51, 20, this::getWirelessidToString, val -> {
            if (val != null && !val.isEmpty()) {
                getWirelessidToString();
            }
        }));

        var scroll = new ScrollableListWidget(0,36,198,172);
        scroll.addWidget((new AdvancedTextWidget(8, 9, this::addNetworksDisplayText, 16777215)));
        builder.widget(scroll);
        return builder.build(this.getHolder(), entityPlayer);
    }
    protected void addDisplayText(List<ITextComponent> textList) {
        if(this.getOwnerGT()!=null)
        {
            NetworkDatabase db = NetworkDatabase.get(getWorld());
            NetworkNode node = db.getNetwork(WirelessId);
            if(node!=null)
            {
                textList.add(new TextComponentString(this.getOwnerGT().toString()));
                textList.add(new TextComponentString("网络ID:"+node.getNetworkID()));
                textList.add(new TextComponentString("网络名称:"+node.getNetworkName()));
                textList.add(new TextComponentString("网络存储能量:"+node.getEnergy().toString()));
            }

        }
    }
    protected void addNetworksDisplayText(List<ITextComponent> textList) {
        if(this.getOwnerGT()!=null)
        {
            NetworkDatabase db = NetworkDatabase.get(getWorld());
            db.getNetworks().keySet().forEach(x->{
                var node = db.getNetwork(x);
                if(node!=null)
                {
                    textList.add(new TextComponentString("--------------------"));
                    textList.add(new TextComponentString(this.getOwnerGT().toString()));
                    textList.add(new TextComponentString("网络ID:"+node.getNetworkID()));
                    textList.add(new TextComponentString("网络名称:"+node.getNetworkName()));
                    textList.add(new TextComponentString("网络存储能量:"+node.getEnergy().toString()));
                    textList.add(new TextComponentString("--------------------"));
                }
            });


        }
    }
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (this.shouldRenderOverlay()) {
            getOverlay().renderSided(getFrontFacing(),renderState,translation,pipeline);
        }

    }
    @SideOnly(Side.CLIENT)
    private SimpleOverlayRenderer getOverlay() {
        if (amperage == 2) {
            return Textures.MULTIPART_WIRELESS_ENERGY;
        } else if (amperage == 4) {
            return Textures.MULTIPART_WIRELESS_ENERGY_4x;
        } else if (amperage == 16) {
            return Textures.MULTIPART_WIRELESS_ENERGY_16x;
        } else if (amperage == 64) {
            return Textures.MULTIPART_WIRELESS_ENERGY_64x;
        } else if (amperage == 256) {
            return Textures.MULTIPART_WIRELESS_ENERGY_256x;
        } else if (amperage == 1024) {
            return Textures.MULTIPART_WIRELESS_ENERGY_1024x;
        } else if (amperage == 4096) {
            return Textures.MULTIPART_WIRELESS_ENERGY_4096x;
        } else if (amperage == 16384) {
            return Textures.MULTIPART_WIRELESS_ENERGY_16384x;
        } else if (amperage == 65536) {
            return Textures.MULTIPART_WIRELESS_ENERGY_65536x;
        } else if (amperage == 262144) {
            return Textures.MULTIPART_WIRELESS_ENERGY_262144x;
        } else if (amperage == 1048576) {
            return Textures.MULTIPART_WIRELESS_ENERGY_1048576x;
        }
        else return Textures.MULTIPART_WIRELESS_ENERGY;

    }

    @Override
    public MultiblockAbility<IEnergyContainer> getAbility() {
        return isExport ? MultiblockAbility.OUTPUT_ENERGY : MultiblockAbility.INPUT_ENERGY;
    }

    @Override
    public void registerAbilities(@NotNull AbilityInstances abilityInstances) {
        abilityInstances.add(energyContainer);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setInteger("wirelessid",this.WirelessId);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.WirelessId = data.getInteger("wirelessid");
    }

    public void addInformation(ItemStack stack, World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format(this.isExport ? "gregtech.machine.wireless.export.tooltip" : "gregtech.machine.wireless.import.tooltip", new Object[0]));
    }

}

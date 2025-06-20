package gtqt.common.metatileentities.multi.multiblockpart;

import gregtech.api.GTValues;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.AdvancedTextWidget;
import gregtech.api.gui.widgets.IncrementButtonWidget;
import gregtech.api.gui.widgets.ScrollableListWidget;
import gregtech.api.gui.widgets.TextFieldWidget2;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.AbilityInstances;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.SimpleOverlayRenderer;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockPart;

import gtqt.api.util.wireless.NetworkManager;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gtqt.api.util.wireless.EnergyContainerWireless;
import gtqt.api.util.wireless.NetworkDatabase;
import gtqt.api.util.wireless.NetworkNode;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.input.Keyboard;

import java.math.BigInteger;
import java.util.List;

public class MetaTileEntityWirelessEnergyHatch extends MetaTileEntityMultiblockPart
        implements IMultiblockAbilityPart<IEnergyContainer> {

    private final int amperage;
    private final boolean isExport;
    private final EnergyContainerWireless energyContainer;
    public int WirelessId = -1;
    // 在类中添加两个字段
    private BigInteger lastEnergy = BigInteger.ZERO;
    private final long lastUpdateTime = 0;

    public MetaTileEntityWirelessEnergyHatch(ResourceLocation metaTileEntityId, int tier, int amperage,
                                             boolean isExport) {
        super(metaTileEntityId, tier);
        this.isExport = isExport;
        this.amperage = amperage;
        energyContainer = new EnergyContainerWireless(this, isExport, GTValues.V[tier], this.amperage);

    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityWirelessEnergyHatch(this.metaTileEntityId, this.getTier(), this.amperage,
                this.isExport);
    }

    public void setCurrentWirelessID(int i) {
        this.WirelessId = Math.min(Math.max(this.WirelessId + i, 1), (int) Math.pow((getTier() + 1), 2));
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 360, 240);

        builder.image(3, 4, 186, 214, GuiTextures.DISPLAY);

        var scroll = new ScrollableListWidget(0, 6, 188, 202);
        scroll.addWidget((new AdvancedTextWidget(6, 2, this::addNetworksDisplayText, 16777215)));
        builder.widget(scroll);

        builder.bindPlayerInventory(entityPlayer.inventory, GuiTextures.SLOT, 192, 160);

        builder.widget(new IncrementButtonWidget(4, 219, 80, 18, 1, 4, 16, 64, this::setCurrentWirelessID)
                .setDefaultTooltip()
                .setShouldClientCallback(false));

        builder.widget(new IncrementButtonWidget(84, 219, 80, 18, -1, -4, -16, -64, this::setCurrentWirelessID)
                .setDefaultTooltip()
                .setShouldClientCallback(false));

        builder.image(164, 220, 24, 16, GuiTextures.DISPLAY);
        builder.widget(new TextFieldWidget2(170, 224, 20, 18, this::getValue, this::setValue).setMaxLength(3)
                .setAllowedChars(TextFieldWidget2.WHOLE_NUMS));

        builder.image(190, 4, 164, 154, GuiTextures.DISPLAY);
        builder.dynamicLabel(194, 10, () -> "无线能源频道管理系统", 0xFFFFFF);
        builder.widget((new AdvancedTextWidget(194, 22, this::addDisplayText, 16777215)).setMaxWidthLimit(162));

        return builder.build(this.getHolder(), entityPlayer);
    }

    private String getValue() {
        return String.valueOf(WirelessId);
    }

    private void setValue(String val) {
        try {
            this.WirelessId = Integer.parseInt(val);
        } catch (NumberFormatException e) {
            this.WirelessId = 0;
        }

    }

    protected void addDisplayText(List<ITextComponent> textList) {
        textList.add(new TextComponentString("正在访问>ID：" + WirelessId + "的网络"));
        if (this.getOwnerGT() != null) {
            NetworkDatabase db = NetworkDatabase.get(getWorld());
            NetworkNode node = db.getNetwork(WirelessId);
            if (node != null) {
                textList.add(new TextComponentString("网络名称:" + node.getNetworkName()));
                textList.add(new TextComponentString("存储能量: " +
                        formatScientificNotation(node.getEnergy()) + " EU (" +
                        formatEnergyValue(node.getEnergy()) + ")"));


                BigInteger energyDiff = node.getEnergy().subtract(lastEnergy);
                if (!energyDiff.equals(BigInteger.ZERO)) {
                    String change = energyDiff.compareTo(BigInteger.ZERO) > 0 ?
                            "+" + formatEnergyValue(energyDiff) :
                            formatEnergyValue(energyDiff);
                    textList.add(new TextComponentString("能量变化: " + change));
                }
//                for (var machine:node.machines)
//                {
//                    var mw = NetworkManager.getWorldByDimension(machine.getDimension());
//                    if(mw!=null && !mw.isRemote && mw.isBlockLoaded(machine.getPos()) && GTUtility.getMetaTileEntity(mw,machine.getPos())!=null)
//                    {
//                        var mte = GTUtility.getMetaTileEntity(mw,machine.getPos());
//                        textList.add(new TextComponentString("维度: " + machine.getDimension()+"机器："+new TextComponentTranslation(mte.getMetaFullName())));
//                    }
//                }
                lastEnergy = node.getEnergy();
            }

        }
    }

    private String formatScientificNotation(BigInteger energy) {
        double value = energy.doubleValue();
        return String.format("%.3E", value);
    }

    private String formatEnergyValue(BigInteger energy) {
        if (energy.compareTo(BigInteger.valueOf(1_000_000_000L)) >= 0) {
            return energy.divide(BigInteger.valueOf(1_000_000_000L)) + " GE";
        } else if (energy.compareTo(BigInteger.valueOf(1_000_000L)) >= 0) {
            return energy.divide(BigInteger.valueOf(1_000_000L)) + " ME";
        } else if (energy.compareTo(BigInteger.valueOf(1_000L)) >= 0) {
            return energy.divide(BigInteger.valueOf(1_000L)) + " KE";
        } else {
            return energy + " EU";
        }
    }

    protected void addNetworksDisplayText(List<ITextComponent> textList) {
        if (this.getOwnerGT() != null) {
            NetworkDatabase db = NetworkDatabase.get(getWorld());
            db.getNetworks().keySet().forEach(x -> {
                var node = db.getNetwork(x);
                if (node != null) {
                    textList.add(new TextComponentString("--------------------"));
                    textList.add(new TextComponentString(">>ID:" + node.getNetworkID()));
                    textList.add(new TextComponentString(this.getOwnerGT().toString()));
                    textList.add(new TextComponentString("网络名称:" + node.getNetworkName()));
                    textList.add(new TextComponentString("存储能量:" + node.getEnergy().toString()));
                }
            });

        }
    }

    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (this.shouldRenderOverlay()) {
            getOverlay().renderSided(getFrontFacing(), renderState, translation, pipeline);
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
        } else return Textures.MULTIPART_WIRELESS_ENERGY;

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
        data.setInteger("wirelessid", this.WirelessId);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.WirelessId = data.getInteger("wirelessid");
    }

    @Override
    public void addInformation(ItemStack stack,
                               World player,
                               @NotNull List<String> tooltip,
                               boolean advanced) {
        String tierName = GTValues.VNF[this.getTier()];
        tooltip.add(I18n.format("gregtech.machine.wireless_energy_hatch.tooltip.1"));
        tooltip.add(I18n.format("gregtech.machine.wireless_energy_hatch.tooltip.2"));

        if (this.isExport) {
            tooltip.add(I18n.format("gregtech.universal.tooltip.voltage_out", this.energyContainer.getOutputVoltage(), tierName));
            tooltip.add(I18n.format("gregtech.universal.tooltip.amperage_out_till", this.energyContainer.getOutputAmperage()));
        } else {
            tooltip.add(I18n.format("gregtech.universal.tooltip.voltage_in", this.energyContainer.getInputVoltage(), tierName));
            tooltip.add(I18n.format("gregtech.universal.tooltip.amperage_in_till", this.energyContainer.getInputAmperage()));
        }

        tooltip.add(I18n.format("gregtech.universal.tooltip.energy_storage_capacity", this.energyContainer.getEnergyCapacity()));
        tooltip.add(I18n.format("gregtech.universal.enabled"));

        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
            tooltip.add(I18n.format("gregtech.machine.wireless_energy_hatch.tooltip.shift"));
        } else {
            tooltip.add(I18n.format("gregtech.tooltip.hold_shift"));
        }
    }
}

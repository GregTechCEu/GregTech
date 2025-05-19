package gtqt.common.metatileentities.multi.multiblockpart;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;

import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IThreadHatch;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.IncrementButtonWidget;
import gregtech.api.gui.widgets.LabelWidget;
import gregtech.api.gui.widgets.ServerWidgetGroup;
import gregtech.api.gui.widgets.TextFieldWidget2;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.AbilityInstances;
import gregtech.api.metatileentity.multiblock.AdvanceRecipeMapMultiblockController;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.OrientedOverlayRenderer;

import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockPart;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;
import java.util.function.IntSupplier;

public class MetaTileEntityThreadHatch extends MetaTileEntityMultiblockPart
        implements IMultiblockAbilityPart<IThreadHatch>, IThreadHatch {

    private static final int MIN_THREAD = 1;

    private final int maxThread;

    private int currentThread;

    public MetaTileEntityThreadHatch(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier);
        this.maxThread = (int) Math.pow(2,tier);
        this.currentThread = this.maxThread;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity metaTileEntityHolder) {
        return new MetaTileEntityThreadHatch(this.metaTileEntityId, this.getTier());
    }

    @Override
    public int getCurrentThread() {
        return currentThread;
    }

    public void setCurrentThread(int ThreadAmount) {
        this.currentThread = MathHelper.clamp(this.currentThread + ThreadAmount, 1, this.maxThread);
        if(this.getController() instanceof AdvanceRecipeMapMultiblockController mte)
        {
            mte.refreshThread(currentThread);
        }
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        ServerWidgetGroup ThreadAmountGroup = new ServerWidgetGroup(() -> true);
        ThreadAmountGroup.addWidget(new ImageWidget(62, 36, 53, 20, GuiTextures.DISPLAY)
                .setTooltip("gregtech.machine.thread_hatch.display"));

        ThreadAmountGroup.addWidget(new IncrementButtonWidget(118, 36, 30, 20, maxThread>64?maxThread/64:1,  maxThread>32?maxThread/32:1, maxThread>16?maxThread/16:1, maxThread/4, this::setCurrentThread)
                .setDefaultTooltip()
                .setShouldClientCallback(false));
        ThreadAmountGroup.addWidget(new IncrementButtonWidget(29, 36, 30, 20,  maxThread>64?-maxThread/64:-1, maxThread>32?-maxThread/32:-1,  maxThread>16?-maxThread/16:-1, -maxThread/4, this::setCurrentThread)
                .setDefaultTooltip()
                .setShouldClientCallback(false));

        ThreadAmountGroup.addWidget(new TextFieldWidget2(63, 42, 51, 20, this::getThreadAmountToString, val -> {
            if (val != null && !val.isEmpty()) {
                setCurrentThread(Integer.parseInt(val));
            }
        })
                .setCentered(true)
                .setNumbersOnly(1, this.maxThread)
                .setMaxLength(6)
                .setValidator(getTextFieldValidator(() -> this.maxThread)));

        return ModularUI.defaultBuilder()
                .widget(new LabelWidget(5, 5, getMetaFullName()))
                .widget(ThreadAmountGroup)
                .bindPlayerInventory(entityPlayer.inventory, GuiTextures.SLOT, 0)
                .build(getHolder(), entityPlayer);
    }

    public String getThreadAmountToString() {
        return Integer.toString(this.currentThread);
    }

    public static @NotNull Function<String, String> getTextFieldValidator(IntSupplier maxSupplier) {
        return val -> {
            if (val.isEmpty())
                return String.valueOf(MIN_THREAD);
            int max = maxSupplier.getAsInt();
            int num;
            try {
                num = Integer.parseInt(val);
            } catch (NumberFormatException ignored) {
                return String.valueOf(max);
            }
            if (num < MIN_THREAD)
                return String.valueOf(MIN_THREAD);
            if (num > max)
                return String.valueOf(max);
            return val;
        };
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, @NotNull List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.machine.thread_hatch.tooltip", this.maxThread));
        tooltip.add(I18n.format("gregtech.universal.disabled"));
    }

    @Override
    public MultiblockAbility<IThreadHatch> getAbility() {
        return MultiblockAbility.THREAD_HATCH;
    }

    @Override
    public void registerAbilities(@NotNull AbilityInstances abilityInstances) {
        abilityInstances.add(this);
    }


    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (shouldRenderOverlay()) {
            OrientedOverlayRenderer overlayRenderer;
            /*
            if (getTier() == GTValues.IV)
                overlayRenderer = GCYMTextures.PARALLEL_HATCH_MK1_OVERLAY;
            else if (getTier() == GTValues.LuV)
                overlayRenderer = GCYMTextures.PARALLEL_HATCH_MK2_OVERLAY;
            else if (getTier() == GTValues.ZPM)
                overlayRenderer = GCYMTextures.PARALLEL_HATCH_MK3_OVERLAY;
            else
                overlayRenderer = GCYMTextures.PARALLEL_HATCH_MK4_OVERLAY;

             */

            overlayRenderer = Textures.FUSION_REACTOR_OVERLAY;

            if (getController() != null && getController() instanceof RecipeMapMultiblockController) {
                overlayRenderer.renderOrientedState(renderState, translation, pipeline, getFrontFacing(),
                        getController().isActive(),
                        getController().getCapability(GregtechTileCapabilities.CAPABILITY_CONTROLLABLE, null)
                                .isWorkingEnabled());
            } else {
                overlayRenderer.renderOrientedState(renderState, translation, pipeline, getFrontFacing(), false, false);
            }
        }
    }

    @Override
    public boolean canPartShare() {
        return false;
    }

    @Override
    public NBTTagCompound writeToNBT(@NotNull NBTTagCompound data) {
        data.setInteger("currentThread", this.currentThread);
        return super.writeToNBT(data);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.currentThread = data.getInteger("currentThread");
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeInt(this.currentThread);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.currentThread = buf.readInt();
    }
}

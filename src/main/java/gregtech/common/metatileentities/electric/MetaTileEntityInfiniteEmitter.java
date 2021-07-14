package gregtech.common.metatileentities.electric;

import gregtech.api.GTValues;
import gregtech.api.gui.IUIHolder;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.EmptyTextureArea;
import gregtech.api.gui.widgets.ClickButtonWidget;
import gregtech.api.gui.widgets.CycleButtonWidget;
import gregtech.api.gui.widgets.LabelWidget;
import gregtech.api.gui.widgets.TextFieldWidgetInfiniteEnergy;
import gregtech.api.metatileentity.InfiniteEnergyTileEntityBase;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.render.SimpleOverlayRenderer;
import gregtech.api.render.Textures;
import gregtech.api.util.function.BooleanConsumer;
import gregtech.common.metatileentities.traits.TraitInfiniteEmitter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.config.GuiUtils;

import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class MetaTileEntityInfiniteEmitter extends InfiniteEnergyTileEntityBase<TraitInfiniteEmitter> {
    public MetaTileEntityInfiniteEmitter(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    protected TraitInfiniteEmitter createTrait() {
        return new TraitInfiniteEmitter(this);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityInfiniteEmitter(metaTileEntityId);
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        InfiniteEnergyUIData d = new InfiniteEnergyUIData();
        d.setEnergy(trait.getEnergy());
        d.setInfinite(trait.isInfinite());
        d.setTier(trait.getTier());
        return d.guiBuilder()
                .buttonInfinite(trait::setInfinite)
                .energyInput("EU", trait::setEnergy)
                .buttonTier(trait::setTier)
                .buttonAcceptDecline()
                .createUI(getHolder(), entityPlayer);
    }

    @Override
    protected SimpleOverlayRenderer getOverlay() {
        return Textures.INFINITE_EMITTER_FACE;
    }

    public static class InfiniteEnergyUIData {
        private static final Pattern NUMBER_PATTERN = Pattern.compile("[0-9]*");
        private static final Predicate<String> NUMBER_PATTERN_VALIDATOR = s -> NUMBER_PATTERN.matcher(s).matches();
        private static final String[] TRANSLATABLE_VOLTAGE_NAMES;

        static {
            TRANSLATABLE_VOLTAGE_NAMES = new String[GTValues.V.length];
            for (int i = 0; i < GTValues.V.length; i++)
                TRANSLATABLE_VOLTAGE_NAMES[i] = "info.infinite_energy." + GTValues.VN[i].toLowerCase();
        }

        private boolean isDirty;

        private String energyText;
        private int tier;
        private boolean isInfinite;

        public InfiniteEnergyGuiBuilder guiBuilder() {
            return new InfiniteEnergyGuiBuilder();
        }

        public void markDirty(boolean isDirty) {
            this.isDirty = isDirty;
        }

        public boolean isDirty() {
            return this.isDirty;
        }

        public String getEnergyText() {
            return energyText;
        }

        public void setEnergyText(String energyText) {
            markDirty(true);
            this.energyText = energyText;
        }

        public BigInteger parseEnergyFromText() {
            return energyText.isEmpty() ? BigInteger.ZERO : new BigInteger(energyText);
        }

        public void setEnergy(BigInteger energy) {
            setEnergyText(energy.toString());
        }

        public int getTier() {
            return tier;
        }

        public void setTier(int tier) {
            markDirty(true);
            this.tier = tier;
        }

        public boolean isInfinite() {
            return isInfinite;
        }

        public void setInfinite(boolean infinite) {
            markDirty(true);
            isInfinite = infinite;
        }

        public final class InfiniteEnergyGuiBuilder {
            private final ArrayDeque<Widget> widgets = new ArrayDeque<>();

            private int y;
            private boolean infinite;
            private boolean energy;
            private boolean tier;

            private BooleanConsumer applyInfiniteFlagChange;
            private Consumer<BigInteger> applyEnergyChange;
            private IntConsumer applyTierChange;

            public InfiniteEnergyGuiBuilder buttonInfinite(BooleanConsumer applyInfiniteFlagChange) {
                this.applyInfiniteFlagChange = applyInfiniteFlagChange;
                if (!widgets.isEmpty()) y += 4;
                widgets.add(new CycleButtonWidget(
                        0,
                        y,
                        200,
                        20,
                        new String[]{"info.infinite_energy.finite", "info.infinite_energy.infinite"},
                        () -> isInfinite() ? 1 : 0,
                        i -> setInfinite(i != 0)
                ));
                y += 20;
                infinite = true;
                return this;
            }

            public InfiniteEnergyGuiBuilder energyInput(String labelText, Consumer<BigInteger> applyEnergyChange) {
                this.applyEnergyChange = applyEnergyChange;
                if (!widgets.isEmpty()) y += 4;
                widgets.add(new TextFieldWidgetInfiniteEnergy(
                        2,
                        y,
                        180,
                        12,
                        true,
                        InfiniteEnergyUIData.this
                ).setValidator(NUMBER_PATTERN_VALIDATOR));
                widgets.add(new LabelWidget(188, y + 2, labelText, -1));
                y += 12;
                energy = true;
                return this;
            }

            public InfiniteEnergyGuiBuilder buttonTier(IntConsumer applyTierChange) {
                this.applyTierChange = applyTierChange;
                if (!widgets.isEmpty()) y += 4;
                widgets.add(new CycleButtonWidget(
                        0,
                        y,
                        200,
                        20,
                        TRANSLATABLE_VOLTAGE_NAMES,
                        InfiniteEnergyUIData.this::getTier,
                        InfiniteEnergyUIData.this::setTier
                ));
                y += 20;
                tier = true;
                return this;
            }

            public InfiniteEnergyGuiBuilder buttonAcceptDecline() {
                if (!widgets.isEmpty()) y += 16;
                buttonClose(true);
                buttonClose(false);
                y += 20;
                return this;
            }

            private void buttonClose(boolean setDirty) {
                widgets.add(new ClickButtonWidget(
                        setDirty ? 0 : 200 - 98, y,
                        98,
                        20,
                        setDirty ? "info.infinite_energy.accept" : "info.infinite_energy.decline",
                        c -> markDirty(setDirty)) {
                    @Override
                    public void handleClientAction(int id, PacketBuffer buffer) {
                        super.handleClientAction(id, buffer);
                        this.gui.entityPlayer.closeScreen();
                    }

                    @Override
                    public void drawInForeground(int mouseX, int mouseY) {
                        if (isMouseOver(getPosition().x, getPosition().y, getSize().width, getSize().height, mouseX, mouseY)) {
                            GuiUtils.drawHoveringText(
                                    Collections.singletonList(I18n.format(setDirty ? "info.button.accept" : "info.button.decline")),
                                    mouseX,
                                    mouseY,
                                    this.gui.getScreenWidth(),
                                    this.gui.getScreenHeight(),
                                    -1,
                                    Minecraft.getMinecraft().fontRenderer);
                        }
                    }
                });
            }

            public ModularUI createUI(IUIHolder holder, EntityPlayer player) {
                ModularUI.Builder b = new ModularUI.Builder(new EmptyTextureArea(200, y), 200, y);
                for (Widget w : widgets) b.widget(w);
                b.bindCloseListener(() -> {
                    if (!player.world.isRemote && isDirty()) {
                        if (infinite) applyInfiniteFlagChange.apply(isInfinite());
                        if (energy) applyEnergyChange.accept(parseEnergyFromText());
                        if (tier) applyTierChange.accept(getTier());
                    }
                });
                return b.build(holder, player);
            }
        }
    }

}

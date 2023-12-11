package gregtech.api.capability.impl;

import gregtech.api.capability.IEnergyContainer;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import org.hamcrest.MatcherAssert;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static gregtech.api.GTValues.*;
import static org.hamcrest.CoreMatchers.is;

public class EnergyContainerListTest {

    @NotNull
    private static MetaTileEntity createDummyMTE() {
        return new MetaTileEntity(new ResourceLocation("")) {

            @Override
            public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
                return null;
            }

            @Override
            protected ModularUI createUI(EntityPlayer entityPlayer) {
                return null;
            }
        };
    }

    @NotNull
    private static IEnergyContainer createContainer(int amps) {
        return createContainer(amps, LV);
    }

    @NotNull
    private static IEnergyContainer createContainer(int amps, int tier) {
        return EnergyContainerHandler.receiverContainer(createDummyMTE(),
                V[tier] * 64L * amps, V[tier], amps);
    }

    @NotNull
    private static EnergyContainerList createList(int size, int ampsPerHatch) {
        List<IEnergyContainer> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(createContainer(ampsPerHatch));
        }
        return new EnergyContainerList(list);
    }

    @Test
    public void test2A() {
        // 1x 2A of LV should become 2A of 32
        check(createList(1, 2), 32, 2);

        // 2x 2A of LV should become 1A of 128
        check(createList(2, 2), 128, 1);

        // 3x 2A of LV should become 1A of 192
        check(createList(3, 2), 192, 1);

        // 4x 2A of LV should become 2A of 128
        check(createList(4, 2), 128, 2);

        // 5x 2A of LV should become 1A of 320
        check(createList(5, 2), 320, 1);
    }

    @Test
    public void test4A() {
        // 1x 4A of LV should become 1A of 128
        check(createList(1, 4), 128, 1);

        // 2x 4A of LV should become 2A of 128
        check(createList(2, 4), 128, 2);

        // 3x 4A of LV should become 1A of 384
        check(createList(3, 4), 384, 1);

        // 4x 4A of LV should become 1A of 512
        check(createList(4, 4), 512, 1);

        // 5x 4A of LV should become 1A of 640
        check(createList(5, 4), 640, 1);
    }

    @Test
    public void test16A() {
        // 1x 16A of LV should become 1A of 512
        check(createList(1, 16), 512, 1);

        // 2x 16A of LV should become 2A of 512
        check(createList(2, 16), 512, 2);

        // 3x 16A of LV should become 1A of 1536
        check(createList(3, 16), 1536, 1);

        // 4x 16A of LV should become 1A of 2048
        check(createList(4, 16), 2048, 1);

        // 5x 16A of LV should become 1A of 2560
        check(createList(5, 16), 2560, 1);
    }

    @Test
    public void testMixed() {
        List<IEnergyContainer> list = new ArrayList<>();
        list.add(createContainer(2));
        list.add(createContainer(4));

        // 6A of LV should become 1A of 192
        check(new EnergyContainerList(list), 192, 1);

        list = new ArrayList<>();
        list.add(createContainer(2));
        list.add(createContainer(4));
        list.add(createContainer(16));

        // 22A of LV should become 1A of 704
        check(new EnergyContainerList(list), 704, 1);

        list = new ArrayList<>();
        list.add(createContainer(4));
        list.add(createContainer(4));
        list.add(createContainer(16));

        // 24A of LV should become 1A of 768
        check(new EnergyContainerList(list), 768, 1);

        list = new ArrayList<>();
        list.add(createContainer(4));
        list.add(createContainer(4));
        list.add(createContainer(4));
        list.add(createContainer(4));
        list.add(createContainer(16));

        // 32A of LV should become 2A of 512
        check(new EnergyContainerList(list), 512, 2);

        list = new ArrayList<>();
        list.add(createContainer(2));
        list.add(createContainer(2));
        list.add(createContainer(2));
        list.add(createContainer(2));
        list.add(createContainer(4));
        list.add(createContainer(4));
        list.add(createContainer(16));

        // 32A of LV should become 2A of 512
        check(new EnergyContainerList(list), 512, 2);

        list = new ArrayList<>();
        list.add(createContainer(2));
        list.add(createContainer(2, MV));

        // 2.5A of MV should become 1A of 320
        check(new EnergyContainerList(list), 320, 1);
    }

    private static void check(@NotNull EnergyContainerList list, long inputVoltage, long inputAmperage) {
        MatcherAssert.assertThat(list.getInputVoltage(), is(inputVoltage));
        MatcherAssert.assertThat(list.getInputAmperage(), is(inputAmperage));
    }
}

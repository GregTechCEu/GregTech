package gregtech.common.covers;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import gregtech.api.GTValues;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.ICoverable;
import gregtech.api.metatileentity.TieredMetaTileEntity;
import gregtech.client.renderer.texture.Textures;
import gregtech.integration.cc.IPeripheralWrapper;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class CoverScreen extends CoverBehavior implements IPeripheralWrapper {

    public CoverScreen(ICoverable coverHolder, EnumFacing attachedSide) {
        super(coverHolder, attachedSide);
    }

    @Override
    public boolean canAttach() {
        return true;
    }

    @Override
    public void renderCover(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, Cuboid6 plateBox, BlockRenderLayer layer) {
        Textures.DISPLAY.renderSided(attachedSide, plateBox, renderState, pipeline, translation);
    }

    @Override
    public boolean shouldAutoConnect() {
        return false;
    }

    @Optional.Method(modid = GTValues.MODID_COMPUTERCRAFT)
    public IPeripheral getPeripheral() {
        return new CoverScreenPeripheral(this);
    }

    public class CoverScreenPeripheral implements IPeripheral {
        public CoverScreen coverScreen;

        public CoverScreenPeripheral(CoverScreen coverScreen) {
            this.coverScreen = coverScreen;
        }

        @Nonnull
        public String getType() {
            return "gregtech_cover_screen";
        }

        @Nonnull
        public String[] getMethodNames() {
            return new String[]{"getEnergy", "getTier"};
        }

        @Nullable
        public Object[] callMethod(@Nonnull IComputerAccess iComputerAccess, @Nonnull ILuaContext iLuaContext, int i, @Nonnull Object[] objects) throws LuaException, InterruptedException {
            if (i == 0 && coverScreen.coverHolder instanceof TieredMetaTileEntity) {
                TieredMetaTileEntity tieredMetaTileEntity = (TieredMetaTileEntity) coverScreen.coverHolder;
                Map<String, Object> data = new HashMap<>();
                data.put("stored", tieredMetaTileEntity.getEnergyStored());
                data.put("capacity", tieredMetaTileEntity.getEnergyCapacity());
                data.put("average_input", tieredMetaTileEntity.getAverageInput());
                data.put("average_output", tieredMetaTileEntity.getAverageOutput());
                data.put("input_voltage", tieredMetaTileEntity.getInputVoltage());
                data.put("output_voltage", tieredMetaTileEntity.getOutputVoltage());
                data.put("input_amperage", tieredMetaTileEntity.getInputAmperage());
                data.put("output_amperage", tieredMetaTileEntity.getOutputAmperage());
                return new Object[]{data};
            } else if (i == 1 && coverScreen.coverHolder instanceof TieredMetaTileEntity) {
                TieredMetaTileEntity tieredMetaTileEntity = (TieredMetaTileEntity) coverScreen.coverHolder;
                return new Object[]{tieredMetaTileEntity.getTier()};
            }
            return null;
        }

        public boolean equals(@Nullable IPeripheral iPeripheral) {
            return iPeripheral == this;
        }
    }
}

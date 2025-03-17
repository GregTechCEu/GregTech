package gregtech.common.metatileentities.multi;

import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;

import net.minecraft.block.state.IBlockState;

import static gregtech.common.blocks.BlockBoilerCasing.BoilerCasingType.*;
import static gregtech.common.blocks.BlockFireboxCasing.FireboxCasingType.*;
import static gregtech.common.blocks.BlockMetalCasing.MetalCasingType.*;
import static gregtech.common.blocks.MetaBlocks.*;

public class BoilerType {

    // Bronze melting point 1357K, maximum chassis temperature 1257K; 884 degrees above boiling point of water
    // target steam per tick 800, water per tick 50
    public static final BoilerType BRONZE = new BoilerType(1257, 50,
            METAL_CASING.getState(BRONZE_BRICKS),
            BOILER_FIREBOX_CASING.getState(BRONZE_FIREBOX),
            BOILER_CASING.getState(BRONZE_PIPE),
            Textures.BRONZE_PLATED_BRICKS,
            Textures.BRONZE_FIREBOX,
            Textures.BRONZE_FIREBOX_ACTIVE,
            Textures.LARGE_BRONZE_BOILER);

    // Steel melting point 2046K, maximum chassis temperature 1946K; 1573 degrees above boiling point of water
    // target steam per tick 1600, water per tick 100
    public static final BoilerType STEEL = new BoilerType(1946, 100,
            METAL_CASING.getState(STEEL_SOLID),
            BOILER_FIREBOX_CASING.getState(STEEL_FIREBOX),
            BOILER_CASING.getState(STEEL_PIPE),
            Textures.SOLID_STEEL_CASING,
            Textures.STEEL_FIREBOX,
            Textures.STEEL_FIREBOX_ACTIVE,
            Textures.LARGE_STEEL_BOILER);

    // Titanium melting point 2426K, maximum chassis temperature 2326K; 1953 degrees above boiling point of water
    // target steam per tick 3200, water per tick 200
    public static final BoilerType TITANIUM = new BoilerType(2326, 200,
            METAL_CASING.getState(TITANIUM_STABLE),
            BOILER_FIREBOX_CASING.getState(TITANIUM_FIREBOX),
            BOILER_CASING.getState(TITANIUM_PIPE),
            Textures.STABLE_TITANIUM_CASING,
            Textures.TITANIUM_FIREBOX,
            Textures.TITANIUM_FIREBOX_ACTIVE,
            Textures.LARGE_TITANIUM_BOILER);

    // Tungstensteel melting point 3587K, maximum chassis temperature 3487K; 3114 degrees above boiling point of water
    // target steam per tick 6400, water per tick 400
    public static final BoilerType TUNGSTENSTEEL = new BoilerType(3487, 400,
            METAL_CASING.getState(TUNGSTENSTEEL_ROBUST),
            BOILER_FIREBOX_CASING.getState(TUNGSTENSTEEL_FIREBOX),
            BOILER_CASING.getState(TUNGSTENSTEEL_PIPE),
            Textures.ROBUST_TUNGSTENSTEEL_CASING,
            Textures.TUNGSTENSTEEL_FIREBOX,
            Textures.TUNGSTENSTEEL_FIREBOX_ACTIVE,
            Textures.LARGE_TUNGSTENSTEEL_BOILER);

    // Workable Data
    private final int maximumChassisTemperature; // determines heating time
    private final int targetWaterBoilRate; // determines water boil amount at maximum temperature
    // efficiency is increased by higher boiling rate, and decreased by a higher maximum temperature.

    // Structure Data
    public final IBlockState casingState;
    public final IBlockState fireboxState;
    public final IBlockState pipeState;

    // Rendering Data
    public final ICubeRenderer casingRenderer;
    public final ICubeRenderer fireboxIdleRenderer;
    public final ICubeRenderer fireboxActiveRenderer;
    public final ICubeRenderer frontOverlay;

    BoilerType(int maximumChassisTemperature, int targetWaterBoilRate,
               IBlockState casingState,
               IBlockState fireboxState,
               IBlockState pipeState,
               ICubeRenderer casingRenderer,
               ICubeRenderer fireboxIdleRenderer,
               ICubeRenderer fireboxActiveRenderer,
               ICubeRenderer frontOverlay) {
        this.maximumChassisTemperature = maximumChassisTemperature;
        this.targetWaterBoilRate = targetWaterBoilRate;

        this.casingState = casingState;
        this.fireboxState = fireboxState;
        this.pipeState = pipeState;

        this.casingRenderer = casingRenderer;
        this.fireboxIdleRenderer = fireboxIdleRenderer;
        this.fireboxActiveRenderer = fireboxActiveRenderer;
        this.frontOverlay = frontOverlay;
    }

    public int maximumChassisTemperature() {
        return maximumChassisTemperature;
    }

    public int targetWaterBoilRate() {
        return targetWaterBoilRate;
    }
}

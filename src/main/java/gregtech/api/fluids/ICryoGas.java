package gregtech.api.fluids;

import gregtech.api.GregTechAPI;
import gregtech.api.unification.material.Material;

public class ICryoGas {

    private Material normal_gas;
    private Material liquid_gas;
    //public static ArrayList<ICryoGas> cryo_gases = new ArrayList<ICryoGas>();

    private int EUt = 128;
    private int duration = 200;

    public static void main(String[] args){
        //this is needed for some reason, otherwise it will error
    }

    public ICryoGas() {
        //this is needed for some reason, otherwise it will error
    }

    public ICryoGas(Material gas_normal, Material gas_liquid) {
        normal_gas = gas_normal;
        liquid_gas = gas_liquid;
    }

    public void setEUt(int power) {
        EUt = power;
    }

    public void setDuration(int time) {
        duration = time;
    }

    public Material getGas() {
        return normal_gas;
    }

    public Material getLiquidGas() {
        return liquid_gas;
    }

    public int getEUt() {
        return EUt;
    }

    public int getDuration() {
        return duration;
    }
}

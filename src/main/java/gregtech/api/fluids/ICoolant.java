package gregtech.api.fluids;

import gregtech.api.GregTechAPI;
import gregtech.api.unification.material.Material;

public class ICoolant{
    private Material coolant;
    private int amount_to_use;
    private int time_factor;

    public static void main(String[] args){
        //this is needed for some reason, otherwise it will error
    }

    public ICoolant() {
        //this is needed for some reason, otherwise it will error
    }

    public ICoolant(Material coolant){
        this.coolant = coolant;
    }

    public void setAmountToUse(int amount) {
        this.amount_to_use = amount;
    }

    public void setTimeFactor(int time) {
        this.time_factor = time;
    }

    public int getTimeFactor() {
        return time_factor;
    }

    public Material getCoolant() {
        return coolant;
    }

    public int getAmount_to_use() {
        return amount_to_use;
    }
}

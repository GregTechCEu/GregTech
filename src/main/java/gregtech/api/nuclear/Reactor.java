package gregtech.api.nuclear;

import gregtech.api.nuclear.components.ControlRod;
import gregtech.api.nuclear.components.CoolantChannel;
import gregtech.api.nuclear.components.FuelRod;

import java.util.ArrayList;

public class Reactor {

    private ReactorComponent[][] reactorLayout;
    private ArrayList<FuelRod> fuelRods;
    private ArrayList<ControlRod> controlRods;
    private ArrayList<CoolantChannel> coolantChannels;
    private int reactorDepth;
    private double reactorMaxTemperature;
    private double averageThermalConductivity;
    private int numberOfComponents;

    public void buildReactorLayout(){
        //TODO: Filling the reactor layout matrix with the components from the GUI
    }


    /**
     * Calculates the thermal properties of the reactor and fills the component arrays used later
     */
    public void prepareThermalProperties(){

        int idRod = 0, idControl = 0, idChannel = 0;

        reactorMaxTemperature = Integer.MAX_VALUE;

        for(int i = 0; i < reactorLayout.length; i++){
            for(int j = 0; j < reactorLayout[i].length; j++){
                if(reactorLayout[i][j].isInside()){
                    reactorLayout[i][j].setPos(i, j);
                    numberOfComponents++;
                    averageThermalConductivity += reactorLayout[i][j].getThermalConductivity();
                    reactorMaxTemperature = Double.min(reactorMaxTemperature, reactorLayout[i][j].getMaxTemperature());

                    if(reactorLayout[i][j] instanceof FuelRod){
                        reactorLayout[i][j].setID(idRod);
                        fuelRods.add((FuelRod) reactorLayout[i][j]);
                        idRod++;
                    }

                    if(reactorLayout[i][j] instanceof ControlRod){
                        reactorLayout[i][j].setID(idControl);
                        controlRods.add((ControlRod) reactorLayout[i][j]);
                        idControl++;
                    }

                    if(reactorLayout[i][j] instanceof CoolantChannel){
                        reactorLayout[i][j].setID(idChannel);
                        coolantChannels.add((CoolantChannel) reactorLayout[i][j]);
                        idChannel++;
                    }
                }
            }
        }

        averageThermalConductivity /= numberOfComponents;

    }

    /**
     * Calculates the geometry-dependent factors necessary for the simulation
     */
    public void computeGeometry(){

    }

}

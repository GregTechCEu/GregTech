package gregtech.api.nuclear;

import gregtech.api.nuclear.components.ControlRod;
import gregtech.api.nuclear.components.CoolantChannel;
import gregtech.api.nuclear.components.FuelRod;

import javax.naming.ldap.Control;
import java.util.ArrayList;

public class Reactor {

    private int geometricIntegrationSteps;

    private ReactorComponent[][] reactorLayout;
    private ArrayList<FuelRod> fuelRods;
    private ArrayList<ControlRod> controlRods;
    private ArrayList<CoolantChannel> coolantChannels;
    private int reactorDepth;
    private double reactorMaxTemperature;
    private double averageThermalConductivity;
    private int numberOfComponents;

    public Reactor(int reactorDepth){
        reactorLayout = new ReactorComponent[15][15];
        this.reactorDepth = reactorDepth;
        reactorMaxTemperature = Double.MAX_VALUE;
        averageThermalConductivity = 0.;
        numberOfComponents = 1;
    }

    public void buildReactorLayout(){
        //TODO: Filling the reactor layout matrix with the components from the GUI
    }


    /**
     * Calculates the thermal properties of the reactor and fills the component arrays used later
     */
    public void prepareThermalProperties(){

        int idRod = 0, idControl = 0, idChannel = 0;

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

        double[][] geometricMatrixSlowNeutrons = new double[fuelRods.size()][fuelRods.size()];
        double[][] geometricMatrixFastNeutrons = new double[fuelRods.size()][fuelRods.size()];

        /*
           We calculate geometric factor matrices to determine how many neutrons go from the i-th to the j-th fuel rod
           This factor is different for slow and fast neutrons because they interact differently with the materials and fuel
        */
        for(int i = 0; i < fuelRods.size(); i++){
            for(int j = 0; j < i; j++){
                double mij = 0;
                boolean pathIsClear = true;
                ArrayList<ControlRod> controlRodsHit = new ArrayList<ControlRod>();
                ArrayList<CoolantChannel> coolantChannelsHit = new ArrayList<CoolantChannel>();

                /*
                Geometric factor calculation is done by (rough) numerical integration along a straight path between the two cells
                 */
                for(int t = 0; t < geometricIntegrationSteps; t++){
                    double[] pos = new double[]{.5, .5};
                    pos[0] += (fuelRods.get(j).getPos()[0] - fuelRods.get(i).getPos()[0])*((float)t/geometricIntegrationSteps) + fuelRods.get(i).getPos()[0];
                    pos[1] += (fuelRods.get(j).getPos()[0] - fuelRods.get(i).getPos()[1])*((float)t/geometricIntegrationSteps) + fuelRods.get(i).getPos()[1];
                    ReactorComponent component = reactorLayout[(int)Math.floor(pos[0])][(int)Math.floor(pos[1])];

                    mij += component.getModeratorFactor();

                    /*
                    For simplicity we pretend that fuel rods are completely opaque to neutrons, paths that hit fuel rods are ignored as obstructed
                     */
                    if(component instanceof FuelRod && component.samePositionAs(fuelRods.get(i)) && component.samePositionAs(fuelRods.get(j))){
                        pathIsClear = false;
                        break;
                    }

                    /*
                    We keep track of which active elements we hit, so we can determined how important they are relative to the others later
                     */
                    if(component instanceof ControlRod){
                        if(!controlRodsHit.contains(component)){
                            controlRodsHit.add((ControlRod)component);
                        }
                    } else if(component instanceof CoolantChannel){
                        if(!coolantChannelsHit.contains(component)){
                            coolantChannelsHit.add((CoolantChannel)component);
                        }
                    }
                }

                /*
                The actual calculation of the geometric factors, fast neutrons are randomly converted into slow neutrons along the path, we pretend that fuel rods are infinitely long and thin for simplicity
                This means the fraction of slow neutrons will go as (1-exp(-m * x))/x where x is the distance between the cells
                The fraction of fast neutrons is simply one minus the fraction of slow neutrons
                 */
                if(pathIsClear){
                    mij /= geometricIntegrationSteps;
                    geometricMatrixSlowNeutrons[i][j] = geometricMatrixFastNeutrons[j][i] = (1. - Math.exp(-mij*fuelRods.get(i).getDistance(fuelRods.get(j))))/fuelRods.get(i).getDistance(fuelRods.get(j));
                    geometricMatrixFastNeutrons[i][j] = geometricMatrixFastNeutrons[j][i] = 1./fuelRods.get(i).getDistance(fuelRods.get(j)) - geometricMatrixSlowNeutrons[i][j];

                    for(ControlRod rod : controlRodsHit){
                        rod.addFuelRodPairToMap(fuelRods.get(i), fuelRods.get(j));
                    }

                    for(CoolantChannel channel : coolantChannelsHit){
                        channel.addFuelRodPairToMap(fuelRods.get(i), fuelRods.get(j));
                    }
                }
            }
        }

        /*
        We now use the data we have on the geometry to calculate the reactor's stats
         */
    }

}

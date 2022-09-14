package gregtech.api.nuclear;

import gregtech.api.nuclear.components.ControlRod;
import gregtech.api.nuclear.components.CoolantChannel;
import gregtech.api.nuclear.components.FuelRod;

import java.util.ArrayList;

public class Reactor {

    private static final double iodineYield = 0.02;
    private static final double iodineDecayConstant = 0.0006;
    private static final double xenonDecayConstant = 0.0004;

    public static double timeStep = 0.01;

    private static int meltdownLimit = 0;

    private static double maxTemperature = 900.;
    private static double lowPressureLimit = 100.;

    private int geometricIntegrationSteps;

    private ReactorComponent[][] reactorLayout;
    private ArrayList<FuelRod> fuelRods;
    private ArrayList<ControlRod> controlRods;
    private ArrayList<CoolantChannel> coolantChannels;
    private ArrayList<ControlRod> effectiveControlRods;
    private ArrayList<CoolantChannel> effectiveCoolantChannels;
    private int reactorDepth;
    private double reactorMaxTemperature;
    private double averageThermalConductivity;
    private int numberOfComponents;

    private double avgCoolantTemperature;
    private double controlRodFactor;

    private double beta;
    private double lSlow;
    private double lFast;
    private double kSlow;
    private double kFast;
    private double k;
    private double l;

    private double n;
    private double T;
    private double dn;
    private double dT;
    private ArrayList<Float> C;
    private ArrayList<Float> dC;

    private double iodineAmount;
    private double xenonAmount;

    private double avgBoilingPoint;
    private double avgAbsorption;
    private double avgModeration;

    private double coolantFactor;

    private double kEff;

    private double groupSum;
    private double totNeutronSources;

    private double[][] avgDelayedNeutronGroups;

    private double avgGeometricFactorSlowNeutrons;
    private double avgGeometricFactorFastNeutrons;

    private double avgTemperatureCoefficient;

    private double avgPressure;

    private int meltdownCounter;

    private boolean updateControlRods;

    public Reactor(int reactorDepth) {
        reactorLayout = new ReactorComponent[15][15];
        this.reactorDepth = reactorDepth;
        reactorMaxTemperature = Double.MAX_VALUE;
        averageThermalConductivity = 0.;
        numberOfComponents = 1;

        avgCoolantTemperature = 0.;

        beta = 0.;
        lSlow = 0.;
        lFast = 0.;
        kSlow = 0.;
        kFast = 0.;
        k = 0.;
        l = 0.;

        n = 0.;
        T = 0.;
        dn = 0.;
        dT = 0.;
        //C = 0.;
        //dC = 0.;

        iodineAmount = 0.;
        xenonAmount = 0.;

        avgBoilingPoint = 0.;
        avgAbsorption = 0.;
        avgModeration = 0.;

        coolantFactor = 0.;

        groupSum = 0.;
        totNeutronSources = 0.;

        avgDelayedNeutronGroups  = new double[6][2];

        avgGeometricFactorSlowNeutrons = 0.;
        avgGeometricFactorFastNeutrons = 0.;

        avgTemperatureCoefficient = 0.;

        avgPressure = 0.;

        meltdownCounter = 0;

        updateControlRods = false;
    }

    public void buildReactorLayout() {
        //TODO: Filling the reactor layout matrix with the components from the GUI
    }


    /**
     * Calculates the thermal properties of the reactor and fills the component arrays used later
     */
    public void prepareThermalProperties() {

        int idRod = 0, idControl = 0, idChannel = 0;

        for(int i = 0; i < reactorLayout.length; i++) {
            for(int j = 0; j < reactorLayout[i].length; j++) {
                if(reactorLayout[i][j].isInside()){
                    reactorLayout[i][j].setPos(i, j);
                    numberOfComponents++;
                    averageThermalConductivity += reactorLayout[i][j].getThermalConductivity();
                    reactorMaxTemperature = Double.min(reactorMaxTemperature, reactorLayout[i][j].getMaxTemperature());

                    if(reactorLayout[i][j] instanceof FuelRod) {
                        reactorLayout[i][j].setID(idRod);
                        fuelRods.add((FuelRod) reactorLayout[i][j]);
                        idRod++;
                    }

                    if(reactorLayout[i][j] instanceof ControlRod) {
                        reactorLayout[i][j].setID(idControl);
                        controlRods.add((ControlRod) reactorLayout[i][j]);
                        idControl++;
                    }

                    if(reactorLayout[i][j] instanceof CoolantChannel) {
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
        for(int i = 0; i < fuelRods.size(); i++) {
            for(int j = 0; j < i; j++) {
                double mij = 0;
                boolean pathIsClear = true;
                ArrayList<ControlRod> controlRodsHit = new ArrayList<ControlRod>();
                ArrayList<CoolantChannel> coolantChannelsHit = new ArrayList<CoolantChannel>();

                /*
                Geometric factor calculation is done by (rough) numerical integration along a straight path between the two cells
                 */
                for(int t = 0; t < geometricIntegrationSteps; t++) {
                    double[] pos = new double[]{.5, .5};
                    pos[0] += (fuelRods.get(j).getPos()[0] - fuelRods.get(i).getPos()[0])*((float)t/geometricIntegrationSteps) + fuelRods.get(i).getPos()[0];
                    pos[1] += (fuelRods.get(j).getPos()[0] - fuelRods.get(i).getPos()[1])*((float)t/geometricIntegrationSteps) + fuelRods.get(i).getPos()[1];
                    ReactorComponent component = reactorLayout[(int)Math.floor(pos[0])][(int)Math.floor(pos[1])];

                    mij += component.getModeratorFactor();

                    /*
                    For simplicity we pretend that fuel rods are completely opaque to neutrons, paths that hit fuel rods are ignored as obstructed
                     */
                    if(component instanceof FuelRod && component.samePositionAs(fuelRods.get(i)) && component.samePositionAs(fuelRods.get(j))) {
                        pathIsClear = false;
                        break;
                    }

                    /*
                    We keep track of which active elements we hit, so we can determined how important they are relative to the others later
                     */
                    if(component instanceof ControlRod) {
                        if(!controlRodsHit.contains(component)) {
                            controlRodsHit.add((ControlRod)component);
                        }
                    } else if(component instanceof CoolantChannel) {
                        if(!coolantChannelsHit.contains(component)) {
                            coolantChannelsHit.add((CoolantChannel)component);
                        }
                    }
                }

                /*
                The actual calculation of the geometric factors, fast neutrons are randomly converted into slow neutrons along the path, we pretend that fuel rods are infinitely tall and thin for simplicity
                This means the fraction of slow neutrons will go as (1-exp(-m * x))/x where x is the distance between the cells
                The fraction of fast neutrons is simply one minus the fraction of slow neutrons
                 */
                if(pathIsClear){
                    mij /= geometricIntegrationSteps;
                    geometricMatrixSlowNeutrons[i][j] = geometricMatrixFastNeutrons[j][i] = (1. - Math.exp(-mij*fuelRods.get(i).getDistance(fuelRods.get(j))))/fuelRods.get(i).getDistance(fuelRods.get(j));
                    geometricMatrixFastNeutrons[i][j] = geometricMatrixFastNeutrons[j][i] = 1./fuelRods.get(i).getDistance(fuelRods.get(j)) - geometricMatrixSlowNeutrons[i][j];

                    for(ControlRod rod : controlRodsHit) {
                        rod.addFuelRodPairToMap(fuelRods.get(i), fuelRods.get(j));
                    }

                    for(CoolantChannel channel : coolantChannelsHit) {
                        channel.addFuelRodPairToMap(fuelRods.get(i), fuelRods.get(j));
                    }
                }
            }
        }

        /*
        We now use the data we have on the geometry to calculate the reactor's stats
         */
        double avgGeometricFactorSlowNeutrons = 0.;
        double avgGeometricFactorFastNeutrons = 0.;

        double avgHighEnergyFissionFactor = 0.;
        double avgLowEnergyFissionFactor = 0.;
        double avgHighEnergyCaptureFactor = 0.;
        double avgLowEnergyCaptureFactor = 0.;

        double totFuelStart = 0.;

        double avgFuelRodDistance = 0.;

        for(FuelRod i : fuelRods) {
            for(FuelRod j : fuelRods) {
                avgGeometricFactorSlowNeutrons += geometricMatrixSlowNeutrons[i.getId()][j.getId()];
                avgGeometricFactorFastNeutrons += geometricMatrixFastNeutrons[i.getId()][j.getId()];

                avgFuelRodDistance += i.getDistance(j);
            }
            totNeutronSources += i.getNeutronSourceIntensity();
            avgTemperatureCoefficient += i.getFuel().getTemperatureCoefficient();

            avgHighEnergyFissionFactor += i.getHEFissionFactor();
            avgLowEnergyFissionFactor += i.getLEFissionFactor();
            avgHighEnergyCaptureFactor += i.getHECaptureFactor();
            avgLowEnergyCaptureFactor += i.getLECaptureFactor();

            totFuelStart += i.getFuel().getDuration();

            for(int c = 0; c < avgDelayedNeutronGroups.length; c++){
                avgDelayedNeutronGroups[c][0] += i.getFuel().getDelayedNeutronsGroups()[c][0];
                avgDelayedNeutronGroups[c][1] += i.getFuel().getDelayedNeutronsGroups()[c][1];
            }
        }

        avgGeometricFactorSlowNeutrons *= 0.125/fuelRods.size();
        avgGeometricFactorFastNeutrons *= 0.125/fuelRods.size();

        avgTemperatureCoefficient /= fuelRods.size();

        avgHighEnergyFissionFactor /= fuelRods.size();
        avgLowEnergyFissionFactor /= fuelRods.size();
        avgHighEnergyCaptureFactor /= fuelRods.size();
        avgLowEnergyCaptureFactor /= fuelRods.size();

        avgFuelRodDistance /= 2.*fuelRods.size();

        for(int i = 0; i < avgDelayedNeutronGroups.length; i++) {
            for(int j = 0; j < avgDelayedNeutronGroups.length; j++) {
                avgDelayedNeutronGroups[i][j] /= fuelRods.size();
            }
            beta += avgDelayedNeutronGroups[i][0];
        }

        lSlow = avgFuelRodDistance/(2200. * avgLowEnergyCaptureFactor);
        lFast = avgFuelRodDistance/(15000000. * avgHighEnergyCaptureFactor);

        kSlow = avgLowEnergyFissionFactor/avgLowEnergyCaptureFactor * avgGeometricFactorSlowNeutrons;
        kFast = avgHighEnergyFissionFactor/avgHighEnergyCaptureFactor * avgGeometricFactorFastNeutrons;

        k = (kSlow + kFast) * reactorDepth/(1. + reactorDepth);
        l = (lSlow + lFast)/2.;

        /*
        We give each control rod and coolant channel a weight depending on how many fuel rods they affect
         */
        this.computeControlRodWeights();
        this.computeCoolantChannelWeights();

        controlRodFactor = ControlRod.ControlRodFactor(effectiveControlRods);
        avgCoolantTemperature /= coolantChannels.size();

        this.prepareInitialConditions();

        //TODO: Implement coolant class, extending material?
        for(CoolantChannel channel : effectiveCoolantChannels) {
            //T += channel.getCoolant().getTemperature() * channel.getWeight();
            //avgBoilingPoint += channel.getCoolant().getBoilingPoint() * channel.getWeight();
            //avgAbsorption += channel.getCoolant().getAbsorption() * channel.getWeight();
            //avgModeration += channel.getCoolant().getModerationFactor() * channel.getWeight();
            //avgPressure += channel.getCoolant().getPressure() * channel.getWeight();
        }

        //coolantFactor = Coolant.CoolantTemperatureFactor(T, avgBoilingPoint, avgAbsorption, avgModeration, avgPressure);

        kEff = 1. * k;
    }

    /**
     * Loops over all the control rods, determines which ones actually affect reactivity, and gives them a weight depending on how many fuel rods they affect
     */
    public void computeControlRodWeights() {
        for(ControlRod rod : controlRods){
            rod.computeWeightFromFuelRodMap();
            if(rod.getWeight() > 0){
                effectiveControlRods.add(rod);
            }
        }
        ControlRod.NormalizeWeights(effectiveControlRods);
    }

    /**
     * Loops over all the coolant channels, determines which ones actually affect reactivity, and gives them a weight depending on how many fuel rods they affect
     */
    public void computeCoolantChannelWeights() {
        for(CoolantChannel channel : coolantChannels) {
            channel.computeWeightFromFuelRodMap();
            if(channel.getWeight() > 0) {
                effectiveCoolantChannels.add(channel);
            }
        }
        CoolantChannel.NormalizeWeights(effectiveCoolantChannels);
    }

    public void prepareInitialConditions() {
        for(CoolantChannel channel : effectiveCoolantChannels) {
            T += channel.getCoolant().getFluid().getTemperature() * channel.getWeight();
            //TODO: Add boiling point to liquids
            avgBoilingPoint += channel.getCoolant().getFluid().getTemperature() * channel.getWeight();
            //TODO: Add neutron absorption coefficients to materials
            //avgAbsorption += channel.getCoolant().getAbsorption() * channel.getWeight();
            //TODO: Add neutron moderation coefficients to materials
            //avgModeration += channel.getCoolant().getModeration() * channel.getWeight();
            //TODO: Add pressure to coolants
            //avgPressure += channel.getCoolant().getPressure() * channel.getWeight();
        }
    }

    /**
     * The actual simulation, this method runs a single step
     */
    public void simulationStep() {
        dn = (kEff*(1-beta)-1)*(n/l) + groupSum + totNeutronSources;
        n += dn * timeStep;

        for(int i = 0; i < avgDelayedNeutronGroups.length; i++) {
            groupSum = 0;
            groupSum += avgDelayedNeutronGroups[i][1] * C.get(i);
            dC.set(i, (float)(kEff * avgDelayedNeutronGroups[i][0] * (n/l) - avgDelayedNeutronGroups[i][1] * C.get(i)));
            C.set(i, (float)(C.get(i) * timeStep));
        }

        dT = 2.5 * n - averageThermalConductivity * (T - avgCoolantTemperature);
        T += dT * timeStep;

        iodineAmount += (n * iodineYield - iodineAmount * iodineDecayConstant) * timeStep;
        xenonAmount += (iodineAmount * iodineDecayConstant - xenonAmount * (xenonDecayConstant + n * lSlow  * avgGeometricFactorSlowNeutrons)) * timeStep;

        if(updateControlRods) {
            controlRodFactor = ControlRod.ControlRodFactor(effectiveControlRods);
            updateControlRods = false;
        }

        kEff = k * (1 + avgTemperatureCoefficient * T);
        kEff *= 1 + (float)effectiveControlRods.size()/fuelRods.size() * controlRodFactor;
        //kEff *= 1 + 1 + Coolant.CoolantTemperatureFactor(T, avgBoilingPoint, avgAbsorption, avgModeration, avgPressure)
        kEff *= 1 - 0.5 * xenonAmount * lSlow * avgGeometricFactorSlowNeutrons;
        kEff = Math.max(0., kEff);

        if(T > maxTemperature) {
            meltdownCounter++;
            if(T > 10 * maxTemperature) {
                this.explode();
                if(T >= 900 && avgPressure < 100) {
                    this.hydrogenExplosion();
                }
            }
            if(meltdownCounter >= meltdownLimit) {
                this.meltdown();
            }
        }
    }

    protected void explode() {
        //TODO explosion
    }

    protected void hydrogenExplosion() {
        //TODO secondary explosion
    }

    protected void meltdown() {
        //TODO meltdown
    }

}

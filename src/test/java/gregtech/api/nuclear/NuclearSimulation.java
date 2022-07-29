package gregtech.api.nuclear;

import gregtech.Bootstrap;
import gregtech.api.nuclear.components.ControlRod;
import gregtech.api.nuclear.components.CoolantChannel;
import gregtech.api.nuclear.components.FuelRod;
import gregtech.api.nuclear.fuels.NuclearFuels;
import gregtech.api.unification.material.properties.CoolingProperty;
import gregtech.api.util.GTLog;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;

import static gregtech.api.nuclear.ReactorComponents.*;

public class NuclearSimulation {
    @BeforeClass
    public static void bootstrap() {
        Bootstrap.perform();
        NuclearFuels.register();
        ReactorComponents.init();
    }

    @Test
    public void init() {
        int reactor_depth = 10;

        int time_max = 36000;

        ReactorComponent[][] reactor_layout = new ReactorComponent[][]{
                new ReactorComponent[]{A, A, A, A, A, V, V, V, V, V, A, A, A, A, A},
                new ReactorComponent[]{A, A, A, V, V, T, T, T, T, T, V, V, A, A, A},
                new ReactorComponent[]{A, A, V, T, T, T, T, T, T, T, T, T, V, A, A},
                new ReactorComponent[]{A, V, T, T, T, C, M, F, M, C, T, T, T, V, A},
                new ReactorComponent[]{A, V, T, T, M, F, M, R, M, F, M, T, T, V, A},
                new ReactorComponent[]{V, T, T, C, F, R, M, F, M, R, F, C, T, T, V},
                new ReactorComponent[]{V, T, T, M, M, M, C, R, C, M, M, M, T, T, V},
                new ReactorComponent[]{V, T, T, F, R, F, R, P, R, F, R, F, T, T, V},
                new ReactorComponent[]{V, T, T, M, M, M, C, R, C, M, M, M, T, T, V},
                new ReactorComponent[]{V, T, T, C, F, R, M, F, M, R, F, C, T, T, V},
                new ReactorComponent[]{A, V, T, T, M, F, M, R, M, F, M, T, T, V, A},
                new ReactorComponent[]{A, V, T, T, T, C, M, F, M, C, T, T, T, V, A},
                new ReactorComponent[]{A, A, V, T, T, T, T, T, T, T, T, T, V, A, A},
                new ReactorComponent[]{A, A, A, V, V, T, T, T, T, T, V, V, A, A, A},
                new ReactorComponent[]{A, A, A, A, A, V, V, V, V, V, A, A, A, A, A}
        };

        int nsteps = 100;


        //##########REACTOR GEOMETRY ANALYSIS, IF YOU ARE USING THIS AS A PLANNER DO NOT EDIT BEYOND THIS POINT##########

        ArrayList<FuelRod> fuel_rods = new ArrayList<>();
        ArrayList<ControlRod> control_rods = new ArrayList<>();
        ArrayList<CoolantChannel> coolant_channels = new ArrayList<>();

        int N = 0;
        int ID_rod = 0;
        int ID_control = 0;
        int ID_channel = 0;

        for (int i = 0, reactor_layoutLength = reactor_layout.length; i < reactor_layoutLength; i++) {
            ReactorComponent[] row = reactor_layout[i];
            for (int j = 0, rowLength = row.length; j < rowLength; j++) {
                reactor_layout[i][j] = row[j].copy();
            }
        }

        double average_thermal_conductivity = 0;
        double reactor_max_temp = 1e9;

        for (int i = 0, reactor_layoutLength = reactor_layout.length; i < reactor_layoutLength; i++) {
            ReactorComponent[] row = reactor_layout[i];
            for (int j = 0, rowLength = row.length; j < rowLength; j++) {
                ReactorComponent component = reactor_layout[i][j];
                if (component.isInside()) {
                    component.setPos(i, j);
                    N += 1;
                    average_thermal_conductivity += component.getThermalConductivity();
                    reactor_max_temp = Math.min(reactor_max_temp, component.getMaxTemperature());
                    if (component instanceof FuelRod) {
                        component.setID(ID_rod);
                        fuel_rods.add((FuelRod) component);
                        ID_rod += 1;
                    } else if (component instanceof ControlRod) {
                        component.setID(ID_control);
                        control_rods.add((ControlRod) component);
                        ID_control += 1;
                    } else if (component instanceof CoolantChannel) {
                        component.setID(ID_channel);
                        coolant_channels.add((CoolantChannel) component);
                        ID_channel += 1;
                    }
                }
            }
        }

        average_thermal_conductivity /= N;

        GTLog.logger.info("Built reactor with: ");
        GTLog.logger.info("> " + N + "total components, of which: ");
        GTLog.logger.info(">  " + fuel_rods.size() + " are fuel rods");
        GTLog.logger.info(">  " + control_rods.size() + " are control rods");
        GTLog.logger.info(">  " + coolant_channels.size() + " are coolant channels");

        ArrayList<ControlRod> effective_control_rods = new ArrayList<>();
        ArrayList<CoolantChannel> effective_coolant_channels = new ArrayList<>();

        GTLog.logger.info("Analyzing geometry...");


        double[][] geometric_matrix_sn = new double[fuel_rods.size()][fuel_rods.size()];
        double[][] geometric_matrix_fn = new double[fuel_rods.size()][fuel_rods.size()];

        for (int i = 0; i<fuel_rods.size();i++) {
            for (int j = 0; j < i; j++) {
                double m_ij = 0;
                boolean channel_is_unobstructed = true;
                ArrayList<ControlRod> control_rods_hit = new ArrayList<>();
                ArrayList<CoolantChannel> coolant_channels_hit = new ArrayList<>();

                for (int k = 0; k < nsteps; k++) {
                    double[] pos = new double[2];
                    double factor = (double) k / nsteps;
                    for (int l = 0; l < pos.length; l++) {
                        pos[l] = 0.5D + (fuel_rods.get(j).getPos()[l] - fuel_rods.get(i).getPos()[l]) * factor + fuel_rods.get(i).getPos()[l];
                    }

                    ReactorComponent component = reactor_layout[(int) Math.floor(pos[0])][(int) Math.floor(pos[1])];

                    m_ij += component.getModeratorFactor();

                    if (component instanceof FuelRod && component != fuel_rods.get(i) && component != fuel_rods.get(j)) {
                        channel_is_unobstructed = false;
                        break;
                    }
                    if (component instanceof ControlRod && !(control_rods_hit.contains((ControlRod) component))) {
                        control_rods_hit.add((ControlRod) component);
                    } else if (component instanceof CoolantChannel && !(coolant_channels_hit.contains((CoolantChannel) component))) {
                        coolant_channels_hit.add((CoolantChannel) component);
                    }
                }

                if (channel_is_unobstructed) {
                    m_ij /= nsteps;
                    double normalized = MathUtil.l2norm(MathUtil.intArraySub(fuel_rods.get(j).getPos(), fuel_rods.get(i).getPos()));
                    geometric_matrix_sn[i][j] = (1 - Math.exp(-m_ij * normalized)) / normalized;
                    geometric_matrix_fn[i][j] = 1 / normalized - geometric_matrix_sn[i][j];
                    geometric_matrix_sn[j][i] = geometric_matrix_sn[i][j];
                    geometric_matrix_fn[j][i] = geometric_matrix_fn[i][j];

                    for (ControlRod c : control_rods_hit) {
                        c.addFuelRodPairToMap(fuel_rods.get(i), fuel_rods.get(j));
                    }
                    for (CoolantChannel c : coolant_channels_hit) {
                        c.addFuelRodPairToMap(fuel_rods.get(i), fuel_rods.get(j));
                    }
                }
            }
        }


        GTLog.logger.info("Done");
        GTLog.logger.info("Calculating stats from geometry...");

        double average_geometric_factor_sn = 0;
        double average_geometric_factor_fn = 0;

        double average_HE_fission_factor = 0;
        double average_LE_fission_factor = 0;
        double average_HE_capture_factor = 0;
        double average_LE_capture_factor = 0;

        double average_temperature_coefficient = 0;

        double total_fuel_start = 0;

        double neutron_sources_total = 0;

        Double[][] average_delayed_neutrons_groups = new Double[6][];
        for (int i = 0; i < 6; i++) {
            average_delayed_neutrons_groups[i] = new Double[]{0D, 0D};
        }

        double average_fuel_rod_distance = 0;

        for (FuelRod f1 : fuel_rods) {
            for (FuelRod f2 : fuel_rods) {
                average_geometric_factor_sn += geometric_matrix_sn[f1.getId()][f2.getId()];
                average_geometric_factor_fn += geometric_matrix_fn[f1.getId()][f2.getId()];

                average_fuel_rod_distance += MathUtil.l2norm(MathUtil.intArraySub(f1.getPos(), f2.getPos()));
            }


            neutron_sources_total += f1.getNeutronSourceIntensity();

            average_temperature_coefficient += f1.getFuel().getTemperatureCoefficient();

            average_HE_fission_factor += f1.getHEFissionFactor();
            average_LE_fission_factor += f1.getLEFissionFactor();
            average_HE_capture_factor += f1.getHECaptureFactor();
            average_LE_capture_factor += f1.getLECaptureFactor();

            total_fuel_start += f1.getFuel().getDuration();

            for (int c = 0; c < average_delayed_neutrons_groups.length; c++) {
                average_delayed_neutrons_groups[c][0] += f1.getFuel().getDelayedNeutronsGroups()[c][0];
                average_delayed_neutrons_groups[c][1] += f1.getFuel().getDelayedNeutronsGroups()[c][1];
            }
        }

        average_geometric_factor_sn *= 0.25 / (2 * fuel_rods.size());
        average_geometric_factor_fn *= 0.25 / (2 * fuel_rods.size());

        average_temperature_coefficient /= fuel_rods.size();

        average_HE_fission_factor /= fuel_rods.size();
        average_LE_fission_factor /= fuel_rods.size();
        average_HE_capture_factor /= fuel_rods.size();
        average_LE_capture_factor /= fuel_rods.size();

        average_fuel_rod_distance /= (2 * fuel_rods.size());

        double beta = 0;

        for (int i = 0; i < average_delayed_neutrons_groups.length; i++) {
            for (int j = 0; j < average_delayed_neutrons_groups[i].length; j++) {
                average_delayed_neutrons_groups[i][j] /= fuel_rods.size();
            }
            beta += average_delayed_neutrons_groups[i][0];
        }

        double l_slow = average_fuel_rod_distance / (2200 * average_LE_capture_factor);
        double l_fast = average_fuel_rod_distance / (15000000 * average_HE_capture_factor);

        double k_slow = 2 * average_LE_fission_factor / average_LE_capture_factor * average_geometric_factor_sn;
        double k_fast = 2 * average_HE_fission_factor / average_HE_capture_factor * average_geometric_factor_fn;

        double k = (k_slow + k_fast) / 2 * (2D * reactor_depth / (1 + reactor_depth));
        double l = (l_slow + l_fast) / 2;

        double average_coolant_temperature = 0;

        for (ControlRod rod : control_rods) {
            rod.computeWeightFromFuelRodMap();
            if (rod.getWeight() > 0) {
                effective_control_rods.add(rod);
            }
        }

        for (CoolantChannel channel : coolant_channels) {
            channel.computeWeightFromFuelRodMap();
            average_coolant_temperature += channel.getCoolant().getCoolantProperties().getTemperature();
            if (channel.getWeight() > 0) {
                effective_coolant_channels.add(channel);
            }
        }

        ControlRod.NormalizeWeights(effective_control_rods);

        CoolantChannel.NormalizeWeights(effective_coolant_channels);

        double control_rod_factor = ControlRod.ControlRodFactor(effective_control_rods);

        average_coolant_temperature /= coolant_channels.size();

        double average_temperature = 0;
        double average_boiling_point = 0;
        double average_absorption = 0;
        double average_moderation = 0;
        double average_pressure = 0;

        for (CoolantChannel channel : effective_coolant_channels) {
            average_temperature += channel.getCoolant().getCoolantProperties().getTemperature() * channel.getWeight();
            average_boiling_point += channel.getCoolant().getCoolantProperties().getBoilingPoint() * channel.getWeight();
            average_absorption += channel.getCoolant().getCoolantProperties().getAbsorptionFactor() * channel.getWeight();
            average_moderation += channel.getCoolant().getCoolantProperties().getModeratorFactor() * channel.getWeight();
            average_pressure += channel.getCoolant().getCoolantProperties().getPressure() * channel.getWeight();
        }

        double coolant_factor = CoolingProperty.coolantTemperatureFactor(average_temperature, average_boiling_point, average_absorption, average_moderation, average_pressure);

        GTLog.logger.info("Done");

        GTLog.logger.info("====================Geometric stats====================");

        GTLog.logger.info("> Slow neutron geometric factor:{}", average_geometric_factor_sn);
        GTLog.logger.info("> Fast neutron geometric factor:{}", average_geometric_factor_fn);

        GTLog.logger.info("====================Fuel stats====================");

        GTLog.logger.info("> Total neutron source intensity:{}", neutron_sources_total);
        GTLog.logger.info("> Delayed neutrons yields and decay constants:");
        GTLog.logger.info(">  " + "#" + "  Yield " + "    Decay Constant ");
        for (int i = 0; i < average_delayed_neutrons_groups.length; i++) {
            GTLog.logger.info(">  " + (i + 1) + "|" + average_delayed_neutrons_groups[i][0] + "|" + average_delayed_neutrons_groups[i][1]);
        }
        GTLog.logger.info("====================Control rod stats====================");

        GTLog.logger.info("> Starting control rod factor:" + control_rod_factor);
        GTLog.logger.info("> List of control rod weights:");
        GTLog.logger.info(">  " + "ID" + " Weight ");
        for (ControlRod rod : effective_control_rods) {
            GTLog.logger.info(">  " + rod.getId() + "|" + rod.getWeight());
        }
        GTLog.logger.info("> Ineffective rods are ignored");

        GTLog.logger.info("====================Coolant channel stats====================");

        GTLog.logger.info("> List of coolant channel weights:");
        GTLog.logger.info(">  " + "ID" + " Weight ");
        for (CoolantChannel channel : effective_coolant_channels) {
            GTLog.logger.info(">  " + channel.getId() + "|" + channel.getWeight());
        }
        GTLog.logger.info("> Channels not affecting reactivity are not listed");
        GTLog.logger.info("====================Thermal stats====================");
        GTLog.logger.info(">" + "Average thermal conductivity:" + average_thermal_conductivity);
        GTLog.logger.info(">" + "Average coolant temperature:" + average_coolant_temperature + "K");
        GTLog.logger.info(">" + "Max temperature:" + reactor_max_temp + "K");

        GTLog.logger.info("====================Coolant stats====================");
        GTLog.logger.info("> Starting coolant factor:" + coolant_factor);
        GTLog.logger.info("> List of coolant properties:");
        GTLog.logger.info(">  " + "Average temperature:" + average_temperature + "K");
        GTLog.logger.info(">  " + "Average boiling point:" + average_boiling_point + "K");
        GTLog.logger.info(">  " + "Average absorption factor:" + average_absorption);
        GTLog.logger.info(">  " + "Average moderation factor:" + average_moderation);
        GTLog.logger.info(">  " + "Average pressure:" + average_pressure + "bar");

        GTLog.logger.info("====================Kinetic parameters====================");
        GTLog.logger.info("> Beta:" + beta);
        GTLog.logger.info("> Neutron lifetime:" + l);
        GTLog.logger.info("> Reactivity:" + k);
        GTLog.logger.info("> Average fuel temperature coefficient:" + average_temperature_coefficient);

//##########REACTOR SIMULATION##########

        int n = 0;

        double reactorTemp = average_temperature;

        double p = average_pressure;

        double dn = 0;

        double deltaT = 0;

        double k_eff = 1. * k;


        double[] rC = new double[average_delayed_neutrons_groups.length];
        double[] deltaCooling = new double[average_delayed_neutrons_groups.length];

        double group_sum = 0;

        double iodine_amount = 0;
        double iodine_yield = 0.02;
        double iodine_decay_constant = 0.0006;
        double xenon_amount = 0;
        double xenon_decay_constant = 0.0004;

        boolean update_control_rods = false;

        //neutrons_final = []
        //temperature_final = []
        //reactivity_final = []
        //total_fuel_final = []
        //iodine_amount_final = []
        //poison_amount_final = []

        double timestep = 1e-2;

        int meltdown_counter = 0;

        int timestart = 0;
        int timeend = time_max;
        //#time_max = 600000
        long currentTime = System.nanoTime();
        GTLog.logger.info("Simulating...");

        for (int time = timestart; time < timeend; time++) {

            //#Actions simulating external intervention here

            if (reactorTemp >= 400 && time < 2000) {
                for (ControlRod rod : effective_control_rods) {
                    rod.setInsertion(0.22F);
                }
                update_control_rods = true;
            }

            //#Actions simulating external intervention above here

            dn = (k_eff * (1 - beta) - 1) * (n / l) + group_sum + neutron_sources_total;
            n += dn * timestep;

            for (int i = 0; i < average_delayed_neutrons_groups.length; i++) {
                group_sum = 0;
                group_sum += average_delayed_neutrons_groups[i][1] * rC[i];
                deltaCooling[i] = (k_eff * average_delayed_neutrons_groups[i][0] * (n / l) - average_delayed_neutrons_groups[i][1] * rC[i]);
                rC[i] += deltaCooling[i] * timestep;
            }

            deltaT = 2.5 * n - average_thermal_conductivity * (reactorTemp - average_coolant_temperature);
            reactorTemp += deltaT * timestep;

            iodine_amount += (n * iodine_yield - iodine_amount * iodine_decay_constant) * timestep;
            xenon_amount += (iodine_amount * iodine_decay_constant - xenon_amount * (xenon_decay_constant + n * l_slow * average_geometric_factor_sn)) * timestep;

            if (update_control_rods) {
                control_rod_factor = ControlRod.ControlRodFactor(effective_control_rods);
                update_control_rods = false;
            }

            k_eff = k * (1 + average_temperature_coefficient * reactorTemp);
            k_eff *= 1 + effective_control_rods.size() * 1F / fuel_rods.size() * control_rod_factor;
            k_eff *= 1 + CoolingProperty.coolantTemperatureFactor(reactorTemp, average_boiling_point, average_absorption, average_moderation, p);
            k_eff *= 1 - 0.5 * xenon_amount * l_slow * average_geometric_factor_sn;
            k_eff = Math.max(0, k_eff);

            //#Checking for fail conditions

            if (reactorTemp > reactor_max_temp) {
                meltdown_counter += 1;
            }
            if (reactorTemp > 10 * reactor_max_temp) {
                GTLog.logger.info("Reactor has exploded");
            }
            if ((reactorTemp >= 900 && p < 100)) {
                GTLog.logger.info("Secondary hydrogen explosion happened");
                break;
            }
            if (meltdown_counter >= 60) {
                GTLog.logger.info("Reactor has melted down");
                break;
            }
        }
        GTLog.logger.info("Simulation ran for " + (System.nanoTime() - currentTime) + "ns" );
    }
}

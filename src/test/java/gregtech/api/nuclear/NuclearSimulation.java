package gregtech.api.nuclear;

import gregtech.Bootstrap;
import gregtech.api.nuclear.components.ControlRod;
import gregtech.api.nuclear.components.CoolantChannel;
import gregtech.api.nuclear.components.FuelRod;
import gregtech.api.util.GTLog;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;

import static gregtech.api.nuclear.ReactorComponents.*;

public class NuclearSimulation {
    @BeforeClass
    public static void bootstrap() {
        Bootstrap.perform();
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

        int i = 0;
        int j = 0;
        while (i < fuel_rods.size()) {
            while (j < i) {
                int m_ij = 0;
                boolean channel_is_unobstructed = true;
                ArrayList<ControlRod> control_rods_hit = new ArrayList<>();
                ArrayList<CoolantChannel> coolant_channels_hit = new ArrayList<>();

                for (int k = 0; k < nsteps; k++) {
                    double[] pos = new double[]{.5, .5};
                    for (int l = 0; l < pos.length; l++) {
                        double d = (fuel_rods.get(j).getPos()[l] - fuel_rods.get(i).getPos()[l]) * (k * 1D / nsteps) + fuel_rods.get(i).getPos()[l] + .5;
                        pos[l] = d;
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
                    double normalized = MathUtil.frobeniusNorm(MathUtil.intArraySub(fuel_rods.get(j).getPos(), fuel_rods.get(i).getPos()));
                    geometric_matrix_sn[i][j] = (1 - Math.exp(-m_ij * normalized) / normalized);
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
                j += 1;
            }
            j = 0;
            i += 1;
        }


        GTLog.logger.info("Done");
        GTLog.logger.info("Calculating stats from geometry...");

        average_geometric_factor_sn = 0;
        average_geometric_factor_fn = 0;

        average_HE_fission_factor = 0;
        average_LE_fission_factor = 0;
        average_HE_capture_factor = 0;
        average_LE_capture_factor = 0;

        average_temperature_coefficient = 0;

        total_fuel_start = 0;

        neutron_sources_total = 0;

        average_delayed_neutrons_groups = [
            [0, 0],
            [0, 0],
            [0, 0],
            [0, 0],
            [0, 0],
            [0, 0]
            ]

        average_fuel_rod_distance = 0;

        for i in fuel_rods:
        for j in fuel_rods:
        average_geometric_factor_sn += geometric_matrix_sn[i.getID()][j.getID()]
        average_geometric_factor_fn += geometric_matrix_fn[i.getID()][j.getID()]

        average_fuel_rod_distance += np.linalg.norm(i.getPos() - j.getPos(), 2)

        neutron_sources_total += i.getNeutronSourceIntensity()

        average_temperature_coefficient += i.getFuel().getTemperatureCoefficient()

        average_HE_fission_factor += i.getHEFissionFactor()
        average_LE_fission_factor += i.getLEFissionFactor()
        average_HE_capture_factor += i.getHECaptureFactor()
        average_LE_capture_factor += i.getLECaptureFactor()

        total_fuel_start += i.getFuel().getDuration()

        for c in range(0, len(average_delayed_neutrons_groups)):
        average_delayed_neutrons_groups[c][0] += i.getFuel().getDelayedNeutronsGroups()[c][0]
        average_delayed_neutrons_groups[c][1] += i.getFuel().getDelayedNeutronsGroups()[c][1]

        average_geometric_factor_sn *= 0.25 / (2 * len(fuel_rods))
        average_geometric_factor_fn *= 0.25 / (2 * len(fuel_rods))

        average_temperature_coefficient /= len(fuel_rods)

        average_HE_fission_factor /= len(fuel_rods)
        average_LE_fission_factor /= len(fuel_rods)
        average_HE_capture_factor /= len(fuel_rods)
        average_LE_capture_factor /= len(fuel_rods)

        average_fuel_rod_distance /= (2 * len(fuel_rods))

        beta = 0
    }
}

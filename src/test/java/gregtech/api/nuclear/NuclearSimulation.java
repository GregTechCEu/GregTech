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
        GTLog.logger.info(">  " +  fuel_rods.size() + " are fuel rods");
        GTLog.logger.info(">  " + control_rods.size() + " are control rods");
        GTLog.logger.info(">  " + coolant_channels.size() + " are coolant channels");
    }
}

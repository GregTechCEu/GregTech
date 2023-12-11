package gregtech.integration.xaero;

import java.awt.*;

// ****************************************************************************************
/**
 * Utilities for converting between various colour spaces. Note that the default scaling
 * for RGB is 0-1 and hue is expressed as degrees (0-360). For other values, see
 * documentation for each conversion routine.
 * 
 * @author Jo Wood,giCentre, City University London. Includes modified code from Duane
 *         Schwartzwald and Harry Parker.
 * @version 3.4, 5th February, 2016.
 */
// *****************************************************************************************

/*
 * This file is part of giCentre utilities library. gicentre.utils is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * gicentre.utils is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * source code (see COPYING.LESSER included with this source code). If not, see
 * http://www.gnu.org/licenses/.
 */

public class ColorUtility {

    public enum WhitePoint {
        // Tristimulus values taken from Schanda, J. (19xx) Colorimetry, p.74

        /**
         * The D50 illuminant with 2 degree observer position.
         */
        D50(96.4, 100.0, 82.5),
        /**
         * The D55 illuminant with 2 degree observer position.
         */
        D55(95.68, 100.0, 92.14),
        /**
         * The D65 illuminant with 2 degree observer position.
         */
        D65(95.04, 100.0, 108.88),
        /**
         * The D75 illuminant with 2 degree observer position.
         */
        D75(94.97, 100.0, 122.61),
        /**
         * The standard illuminant C.
         */
        C(98.07, 100.0, 118.22);

        private double[] params;

        private WhitePoint(double X, double Y, double Z) {
            params = new double[] { X, Y, Z };
        }

        /**
         * Reports the tristimulus reference coordinates of the whitepoint.
         *
         * @return Triplet of tristimulus coordinates of the whitepoint.
         */
        double[] getTristimulus() {
            return params;
        }
    }

    /**
     * sRGB to XYZ conversion matrix (3x3)
     */
    static final double[][] M = { { 0.4124, 0.3576, 0.1805 },       // RX, GX, BX
            { 0.2126, 0.7152, 0.0722 },       // RY, GY, BY
            { 0.0193, 0.1192, 0.9505 } };      // RZ, GZ, BZ

    /**
     * Finds the CIELab triplet representing the given colour. CIELab L value scaled between 0-100,
     * and a and b values scaled between -100 and 100. Based on the conversion code by Duane
     * Schwartzwald, 12th March, 2006 and Harry Parker, Feb 27th, 2007.
     * See <a href="http://rsbweb.nih.gov/ij/plugins/download/Color_Space_Converter.java" target="_blank">
     * rsbweb.nih.gov/ij/plugins/download/Color_Space_Converter.java</a>.
     *
     * @param colour Colour to convert.
     * @param wp     Whitepoint colour calibration value.
     * @return Triplet of CIELab values in the order <i>L</i>, <i>a</i> and <i>b</i>.
     */
    public static double[] getLab(Color colour, WhitePoint wp) {
        return XYZtoLAB(RGBtoXYZ(colour), wp);
    }

    public static double[] getLab(Color colour) {
        return XYZtoLAB(RGBtoXYZ(colour), WhitePoint.C);
    }

    static double[] RGBtoXYZ(Color colour) {
        double r = colour.getRed() / 255.0;
        double g = colour.getGreen() / 255.0;
        double b = colour.getBlue() / 255.0;
        return RGBtoXYZ(new double[] { r, g, b });
    }

    /**
     * Converts the given rgb triplet into XYZ colour space.
     *
     * @param rgb RGB triplet to convert. Values should be scaled between 0-1.
     * @return Colour in XYZ space.
     */
    static double[] RGBtoXYZ(double[] rgb) {
        double r = rgb[0];
        double g = rgb[1];
        double b = rgb[2];

        // assume sRGB
        if (r <= 0.04045) {
            r = r / 12.92;
        } else {
            r = Math.pow(((r + 0.055) / 1.055), 2.4);
        }
        if (g <= 0.04045) {
            g = g / 12.92;
        } else {
            g = Math.pow(((g + 0.055) / 1.055), 2.4);
        }
        if (b <= 0.04045) {
            b = b / 12.92;
        } else {
            b = Math.pow(((b + 0.055) / 1.055), 2.4);
        }

        r *= 100.0;
        g *= 100.0;
        b *= 100.0;

        // [X Y Z] = [r g b][M]
        double[] result = new double[3];
        result[0] = (r * M[0][0]) + (g * M[0][1]) + (b * M[0][2]);
        result[1] = (r * M[1][0]) + (g * M[1][1]) + (b * M[1][2]);
        result[2] = (r * M[2][0]) + (g * M[2][1]) + (b * M[2][2]);

        return result;
    }

    static double[] XYZtoLAB(double[] XYZ, WhitePoint wp) {
        double x = XYZ[0] / wp.getTristimulus()[0];
        double y = XYZ[1] / wp.getTristimulus()[1];
        double z = XYZ[2] / wp.getTristimulus()[2];

        if (x > 0.008856) {
            x = Math.pow(x, 1.0 / 3.0);
        } else {
            x = (7.787 * x) + (16.0 / 116.0);
        }
        if (y > 0.008856) {
            y = Math.pow(y, 1.0 / 3.0);
        } else {
            y = (7.787 * y) + (16.0 / 116.0);
        }
        if (z > 0.008856) {
            z = Math.pow(z, 1.0 / 3.0);
        } else {
            z = (7.787 * z) + (16.0 / 116.0);
        }

        return new double[] { 116 * y - 16, 500 * (x - y), 200 * (y - z) };
    }
}

package com.theshopatvsp.levelandroidsdk.ble.helper;

/**
 * Created by andrco on 6/13/16.
 */
public class LeastSquaresExponentialFit {
    //returns a, b
    public static double[] fit(double x[], double y[]) {
        double a = 0.0, b = 0.0, part1 = 0.0, part2 = 0.0, part3 = 0.0, part4 = 0.0, part5 = 0.0, part6 = 0.0, part7 = 0.0;

        for (int i = 0; i < x.length; i++) {
            part1 += Math.pow(x[i], 2) * y[i];
            part2 += Math.log(y[i]) * y[i];
            part3 += x[i] * y[i];
            part4 += x[i] * y[i] * Math.log(y[i]);
            part5 += y[i];
            part6 += Math.pow(x[i], 2) * y[i];
        }

        a = ((part1 * part2) - (part3 * part4)) / ((part5 * part6) - Math.pow(part3, 2));
        b = ((part5 * part4) - (part3 * part2)) / ((part5 * part1) - Math.pow(part3, 2));

        return new double[]{Math.exp(a), b};
    }
}

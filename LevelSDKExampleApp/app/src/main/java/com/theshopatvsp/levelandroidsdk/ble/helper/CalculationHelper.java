package com.theshopatvsp.levelandroidsdk.ble.helper;

import  com.theshopatvsp.levelandroidsdk.ble.model.Step;
import  com.theshopatvsp.levelandroidsdk.model.LevelUser;
import  com.theshopatvsp.levelandroidsdk.model.constants.Gender;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by andrco on 6/13/16.
 */
public class CalculationHelper {
    //CONSTANTS HERE
    private static final float MALE_MULTIPLIER = 0.415f;
    private static final float FEMALE_MULTIPLIER = 0.413f;
    //BMR
    private static final float BMR_MALE_ADDITOR = 66.473f;
    private static final float BMR_FEMALE_ADDITOR = 655.0955f;
    private static final float BMR_HEIGHT_MULTIPLIER = 2.54f;
    private static final float BMR_MALE_HEIGHT_MULTIPLIER = 5.0033f;
    private static final float BMR_FEMALE_HEIGHT_MULTIPLIER = 1.8496f;
    private static final float BMR_MALE_WEIGHT_MULTIPLIER = 13.7516f;
    private static final float BMR_FEMALE_WEIGHT_MULTIPLIER = 9.5634f;
    private static final float BMR_MALE_AGE_MULTIPLIER = 6.755f;
    private static final float BMR_FEMALE_AGE_MULTIPLIER = 4.6756f;
    //METS
    private static final float METS_SLOW_MULTIPLIER = 1.1898f;
    private static final float METS_SLOW_STEPRATE_MULTIPLIER = 0.3794f;
    private static final float METS_FAST_MULTIPLIER = 1.1674f;
    private static final float METS_FAST_ADDITOR = 2.6979f;
    private static final float SPEED_MALE_MULTIPLIER = 0.6915f;
    private static final float SPEED_MALE_STEPRATE_MULTIPLIER = 0.824f;
    private static final float SPEED_FEMALE_MULTIPLIER = 0.6552f;
    private static final float SPEED_FEMALE_STEPRATE_MULTIPLIER = 0.8028f;
    private static final float STRIDE_LENGTH_MULTIPLIER = 0.8091f;
    private static final float STRIDE_LENGTH_STEPRATE_MULTIPLIER = 0.5356f;
    private static final float ACTIVE_BURN_MULTIPLIER = 0.454f;
    private static final float ACTIVE_BURN_WEIGHT_MULTIPLIER = 0.0167f;
    private static final double activeMinuteMetsThreshold = 1.38;

    //DON'T CHANGE THESE UNLESS YOU CONTROL THE PHYSICS OF THE UNIVERSE!
    private static final int inchesPerFoot = 12;
    private static final int feetPerMile = 5280;
    private static final int daysInYear = 365;
    private static final int daysInLeapYear = 366;
    private static final int secsPerMinute = 60;
    private static final int minutesPerDay = 1440;
    private static final int hoursPerDay = 24;

    private LevelUser user;
    private String timezone;
    private boolean useAllOfIt = false;
    private float userMetsCorrectionFactor = 0.0f;

    public CalculationHelper(LevelUser user, String timezone) {
        this.user = user;
        this.timezone = timezone;
        format.setTimeZone(TimeZone.getTimeZone(timezone));
        hourFormat.setTimeZone(TimeZone.getTimeZone(timezone));
        userMetsCorrectionFactor = (float) (3.5f * (user.getWeightLbs() * ACTIVE_BURN_MULTIPLIER * minutesPerDay * 5) / (calculateBMR() * 1000));
    }

    public CalculationHelper(LevelUser user, long startInterval, long stopInterval, String timezone) {
        this(user, timezone);
        this.minTimestamp = startInterval;
        this.maxTimestamp = stopInterval;
        this.intervalSet = true;
    }

    public CalculationHelper(LevelUser user, long startInterval, long stopInterval, String timezone, boolean allOfIt) {
        this(user, startInterval, stopInterval, timezone);
        this.useAllOfIt = allOfIt;
    }

    // User Calculations ****************

    public float calculateStepLength() {
        float totalFeet = ((float) user.getHeightFeet() + ((float) user.getHeightInches() / (float) 12));

        if (user.getGender() != null) {

            if (user.getGender() == Gender.MALE) {
                return (MALE_MULTIPLIER * totalFeet);
            } else {
                return (FEMALE_MULTIPLIER * totalFeet);
            }

        } else {
            return 0;
        }

    }

    public float calculateDistanceInMilesFromStepTotal(int stepTotal) {
        float distanceInFeet = calculateStepLength() * stepTotal;

        return distanceInFeet / feetPerMile;

    }


    public float calculateTotalHeightInInches() {

        float totalHeight = 0;

        if ((user.getHeightFeet() > 0) || (user.getHeightInches() > 0)) {

            totalHeight = (user.getHeightFeet() * inchesPerFoot) + user.getHeightInches();

        }

        return totalHeight;

    }

    public float calculateBMR() {

        float BMR = 0;

        if ((user.getGender() != null) && (user.getWeightLbs() > 0) && ((user.getHeightFeet() > 0) || (user.getHeightInches() > 0))) {
            if (user.getGender() == Gender.MALE) {
                BMR = BMR_MALE_ADDITOR + (BMR_MALE_WEIGHT_MULTIPLIER * ACTIVE_BURN_MULTIPLIER * (float) user.getWeightLbs()) +
                        (BMR_MALE_HEIGHT_MULTIPLIER * BMR_HEIGHT_MULTIPLIER * calculateTotalHeightInInches()) -
                        (BMR_MALE_AGE_MULTIPLIER * user.getAge());
            } else {
                BMR = BMR_FEMALE_ADDITOR + (BMR_FEMALE_WEIGHT_MULTIPLIER * ACTIVE_BURN_MULTIPLIER * (float) user.getWeightLbs()) +
                        (BMR_FEMALE_HEIGHT_MULTIPLIER * BMR_HEIGHT_MULTIPLIER * calculateTotalHeightInInches()) -
                        (BMR_FEMALE_AGE_MULTIPLIER * user.getAge());
            }

        }

        return BMR;

    }


    // Other Calculations **************

    public double calculateDistanceInMiles(int stepCount) {
        return (stepCount * calculateStrideLength(stepCount)) / feetPerMile;
    }


    /* don't know if we need this...
    public float calculateCalorieBurnGoal() {

        float caloriesBurnedGoal = 2500; // Default

        float[] goalLevelValues = {1.2f, 1.375f, 1.55f, 1.725f, 1.9f};

        float goalLevelValue = goalLevelValues[user.getActivityLevel().getId()];
        caloriesBurnedGoal = goalLevelValue * calculateBMR();

        return caloriesBurnedGoal;

    }*/

    public float calculateHourOfTheDay(Date date) {

        Calendar now = Calendar.getInstance();
        now.setTimeZone(TimeZone.getTimeZone(timezone));

        now.setTime(date);
        float hour = now.get(Calendar.HOUR_OF_DAY);
        float minute = now.get(Calendar.MINUTE);

        return hour + (minute / secsPerMinute);
    }

    public static float calculateHourOfTheDay(int hour, int minute) {
        return hour + (minute / secsPerMinute);
    }

    public float calculateRestingBurn() {
        Calendar now = Calendar.getInstance();
        now.setTimeZone(TimeZone.getTimeZone(timezone));

        return calculateBMR() * (calculateHourOfTheDay(now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE)) / hoursPerDay);
    }

    public float calculateRestingBurn(Date date) {

        return calculateBMR() * (calculateHourOfTheDay(date) / hoursPerDay);
    }

    public float calculateActiveBurn(int stepCount) {
        return calculateActiveBurnWithWeight(stepCount, user.getWeightLbs());
    }

    public float calculateActiveBurnWithWeight(int stepCount, double weight) {
        return calculateActiveBurnWithWeight(stepCount, weight, 0.0f, false);
    }

    public float calculateActiveBurnWithWeight(int stepCount, double weight, float metsOverride, boolean overrideMets) {
        float activeBurn = 0;

        if (overrideMets) {
            activeBurn = (float) (metsOverride * userMetsCorrectionFactor * ACTIVE_BURN_MULTIPLIER * weight * ACTIVE_BURN_WEIGHT_MULTIPLIER);
        } else {
            float mets = calculateMETs(stepCount);

            if (mets > 0) {
                activeBurn = (float) (mets * userMetsCorrectionFactor * ACTIVE_BURN_MULTIPLIER * weight * ACTIVE_BURN_WEIGHT_MULTIPLIER);
            }
        }

        return activeBurn;
    }

    public float calculateManualActivityBurn() {

        float manualActivityBurn = 0;
        float selfReportedActivityHours = 0.0f;

        manualActivityBurn = 10 * userMetsCorrectionFactor * ACTIVE_BURN_MULTIPLIER * (float) user.getWeightLbs() * selfReportedActivityHours;

        return manualActivityBurn;

    }

    // Calculations based on Steps Over Time ************

    private static float calculateStepRate(int stepCount) {

        return (float) stepCount / (float) secsPerMinute;

    }

    public double calculateStrideLength(int stepCount) {

        double stepRate = calculateStepRate(stepCount);
        double height = calculateTotalHeightInInches() / 12;
        double fit[] = null, x[] = new double[2], y[] = new double[2];

        if (user.getGender() == Gender.MALE) {
            x[0] = 1.6;
            x[1] = 3.2;
            y[0] = 0.3736 * height;
            y[1] = 0.7433 * height;

            fit = LeastSquaresExponentialFit.fit(x, y);

            if (stepRate < 1.4) {
                stepRate = 1.8;
            }
        } else {
            x[0] = 1.6;
            x[1] = 3.2;
            y[0] = 0.3778 * height;
            y[1] = 0.7426 * height;

            fit = LeastSquaresExponentialFit.fit(x, y);

            if (stepRate < 1.4) {
                stepRate = 1.9;
            }
        }

        a = fit[0];
        b = fit[1];

        return (fit[0] * Math.exp(fit[1] * stepRate));

    }

    public float calculateSpeedMPH(int stepCount) {

        float stepRate = calculateStepRate(stepCount);
        float speedMPH = 0;

        if (stepRate > 0) {
            if (user.getGender() == Gender.MALE)
                speedMPH = (float) (SPEED_MALE_MULTIPLIER * Math.exp(SPEED_MALE_STEPRATE_MULTIPLIER * stepRate));
            else
                speedMPH = (float) (SPEED_FEMALE_MULTIPLIER * Math.exp(SPEED_FEMALE_STEPRATE_MULTIPLIER * stepRate));
        }

        return speedMPH;

    }

    public float calculateMETs(int stepCount) {
        float mets = 0;
        float speedMPH = calculateSpeedMPH(stepCount);

        if (speedMPH > 1.51 && speedMPH <= 5)
            mets = (float) (METS_SLOW_MULTIPLIER * Math.exp(speedMPH * METS_SLOW_STEPRATE_MULTIPLIER));
        else if (speedMPH > 5)
            mets = METS_FAST_MULTIPLIER * speedMPH + METS_FAST_ADDITOR;

        return mets;
    }

    public static final String DELIMITER = ",";

    public String status() {
        return totalSteps + DELIMITER + totalActiveBurn + DELIMITER + getTotalRestingBurn() + DELIMITER + totalDistance + DELIMITER + totalActiveTime + DELIMITER + realTotalActiveTime +
                DELIMITER + longestActive + DELIMITER + longestIdle + DELIMITER + longestStretch + DELIMITER + getAverageSpeed() + DELIMITER + a + DELIMITER + b;
    }

    private double a, b;
    //experimental stuff goes here
    private Date prevTimestamp;
    private static final int MINUTE = 60 * 1000;
    private int idleCounter = 0;
    private int idleCounterDist = 0;
    private int totalActiveTime = 0;
    //used for the avg speed calculation
    private int realTotalActiveTime = 0;
    private int totalSteps = 0;
    private double totalDistance = 0;
    private double totalActiveBurn = 0;
    private int longestActive = 0;
    private int longestIdle = 0;
    private double longestStretch = 0;
    private double tempLongestStretch = 0;
    private int stepCount;
    private double averageSpeed = 0.0;
    private int tempLongestActive = 0;
    private int tempLongestIdle = 0;
    private boolean sawIt = false;
    private boolean countedLast = false;
    private DateFormat format = new SimpleDateFormat("MM/dd/yyyy");
    private DateFormat hourFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    private long minTimestamp = Long.MAX_VALUE;
    private long maxTimestamp = -1;
    private boolean intervalSet = false;

    private Date getMidnight() {
        Calendar start = Calendar.getInstance();
        start.setTimeZone(TimeZone.getTimeZone(timezone));
// hardReset hour, minutes, seconds and millis
        start.set(Calendar.HOUR_OF_DAY, 23);
        start.set(Calendar.MINUTE, 59);
        start.set(Calendar.SECOND, 59);
        start.set(Calendar.MILLISECOND, 0);

        return start.getTime();
    }

    public static boolean isActiveMinute(double metsValue) {

        return (metsValue >= activeMinuteMetsThreshold);

    }

    public void calculateStuff(Step step) {
        stepCount++;
        Calendar current = Calendar.getInstance();
        current.setTimeZone(TimeZone.getTimeZone(timezone));
        current.setTimeInMillis(step.getTimestamp());
        Date currentTimestamp = current.getTime();
        int minutes = 0;
        double dist = calculateDistanceInMiles(step.getSteps());

        //used for resting burn because it is time based
        if (!intervalSet) {
            if (step.getTimestamp() > maxTimestamp) {
                maxTimestamp = step.getTimestamp();
            }

            if (step.getTimestamp() < minTimestamp) {
                minTimestamp = step.getTimestamp();
            }
        }

        //see if there is a gap in step records
        if (prevTimestamp != null) {
            minutes = (int) ((currentTimestamp.getTime() / MINUTE) - (prevTimestamp.getTime() / MINUTE));
        }

        // find a gap if one exists
        if (minutes > 1) {
            if (minutes + idleCounter <= 3) {
                idleCounter += minutes;
            } else {
                if (tempLongestActive > 0) {
                    if (tempLongestActive > longestActive) {
                        longestActive = tempLongestActive;
                    }

                    if (tempLongestStretch > longestStretch) {
                        longestStretch = tempLongestStretch;
                    }

                    tempLongestActive = 0;
                    tempLongestStretch = 0;
                }

                tempLongestIdle += minutes;

                if (idleCounter > 0 && !sawIt) {
                    tempLongestIdle += idleCounter;
                    sawIt = true;
                }
            }
        }

        //if it's an active minutes
        if (step.getMets() >= activeMinuteMetsThreshold) {
            //increment a whole bunch of stuff
            totalActiveTime += 1;
            tempLongestActive += 1;
            tempLongestStretch += dist;
            averageSpeed += calculateSpeedMPH(step.getSteps());
            realTotalActiveTime += 1;

            //if there was an idle period backfill
            if (idleCounter > 0) {
                totalActiveTime += idleCounter;
                tempLongestActive += idleCounter;
                tempLongestStretch += idleCounterDist;
            }

            //hardReset the idle counter things
            idleCounter = 0;
            idleCounterDist = 0;
            sawIt = false;
            countedLast = false;

            //if we had a tempLongestIdle see if it was our longest idle
            if (tempLongestIdle > 0) {
                if (tempLongestIdle > longestIdle) {
                    longestIdle = tempLongestIdle;
                }

                tempLongestIdle = 0;
            }
        } else { //it's an idle minute
            //is it really an idle minute? we give the user a buffer of 2 minutes in case they stopped at a red light or something
            if ((tempLongestActive > 0 && idleCounter >= 2) || tempLongestActive == 0) {
                tempLongestIdle += 1;

                //if the user really was idle count it as idle
                if (idleCounter > 0 && !sawIt) {
                    tempLongestIdle += idleCounter;
                    sawIt = true;
                }

                //hardReset idle stuff
                idleCounter = 0;
                idleCounterDist = 0;

                //if we have longest active/strech stuff, see if it's the longest
                if (tempLongestActive > 0) {
                    if (tempLongestActive > longestActive) {
                        longestActive = tempLongestActive;
                    }

                    if (tempLongestStretch > longestStretch) {
                        longestStretch = tempLongestStretch;
                    }

                    tempLongestStretch = 0;
                    tempLongestActive = 0;
                }
            } else { // we're in the idle buffer
                //if the user was active, count the last minute as active because why not
                if (tempLongestActive > 1 && idleCounter == 0 && step.getMets() > 0 && !countedLast) {
                    totalActiveTime += 1;
                    tempLongestActive += 1;
                    tempLongestStretch += dist;
                    idleCounter = 0;
                    idleCounterDist = 0;
                    sawIt = false;
                    countedLast = true;
                } else { // the user is really really idle and in the buffer
                    idleCounter++;
                    idleCounterDist += dist;
                }
            }
        }

        totalSteps += step.getSteps();
        totalDistance += dist;
        totalActiveBurn += step.getActiveBurn();


        prevTimestamp = current.getTime();
    }

    public int getLongestIdle() {
        return longestIdle > tempLongestIdle ? longestIdle : tempLongestIdle;
    }

    public int getLongestActive() {
        return longestActive > tempLongestActive ? longestActive : tempLongestActive;
    }

    private Calendar getCalendarWithTimezone(long timestamp) {
        Calendar start = Calendar.getInstance();
        start.setTimeZone(TimeZone.getTimeZone(timezone));
        start.setTimeInMillis(timestamp);

        return start;
    }

    private Date getDateWithTimezone(long timestamp) {
        return getCalendarWithTimezone(timestamp).getTime();
    }


    public double getTotalRestingBurn() {

        double totalRestingBurn = 0.0;
        Date minTimestampDate = getDateWithTimezone(minTimestamp);
        Date maxTimestampDate = getDateWithTimezone(maxTimestamp);
        String minDate = format.format(minTimestampDate);
        String maxDate = format.format(maxTimestampDate);

        Calendar start = getCalendarWithTimezone(minTimestamp);
        Calendar end = getCalendarWithTimezone(maxTimestamp);
        boolean endCalculated = false;

        while (!start.after(end)) {
            String target = format.format(start.getTime());
            double burn = calculateRestingBurn(getMidnight());

            if (target.equalsIgnoreCase(minDate) && target.equalsIgnoreCase(maxDate)) {
                totalRestingBurn = calculateRestingBurn(maxTimestampDate) - calculateRestingBurn(minTimestampDate);
                endCalculated = true;
                break;
            } else if (target.equalsIgnoreCase(minDate)) {
                totalRestingBurn += burn - calculateRestingBurn(minTimestampDate);
            } else if (target.equalsIgnoreCase(maxDate)) {
                totalRestingBurn += calculateRestingBurn(maxTimestampDate);
                endCalculated = true;
            } else {
                totalRestingBurn += burn;
            }

            start.add(Calendar.DATE, 1);
        }

        if (!endCalculated) {

            totalRestingBurn += calculateRestingBurn(maxTimestampDate);
        }

        return totalRestingBurn;
    }

    public double getTotalActiveBurn() {
        return totalActiveBurn;
    }

    public double getTotalDistance() {
        return totalDistance;
    }

    public int getTotalSteps() {
        return totalSteps;
    }

    public int getTotalActiveTime() {
        return totalActiveTime;
    }

    public double getLongestStretch() {
        return longestStretch > tempLongestStretch ? longestStretch : tempLongestStretch;
    }

    public double getAverageSpeed() {
        return averageSpeed / Double.valueOf(realTotalActiveTime);
    }

    public int getRealTotalActiveTime() {
        return realTotalActiveTime;
    }
}

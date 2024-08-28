/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2011 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.ftpmanager.common.constants;

import java.util.Calendar;

/**
 * @author kamkaka
 * 
 */
public final class TimeConstants {

    /*
     * I M P O R T A N T
     * 
     * Please note all millisecond-related constants are of type long to prevent
     * overflow problems when calculating
     * 
     * 
     * I M P O R T A N T
     */

    private TimeConstants() {
        //PMD Silence !!!
    }

    public final static long ONE_SECOND_IN_MILLISECONDS = 1000;

    public final static long ONE_MINUTE_IN_MILLISECONDS = 60 * ONE_SECOND_IN_MILLISECONDS;

    public final static long ONE_HOUR_IN_MILLISECONDS = 60 * ONE_MINUTE_IN_MILLISECONDS;

    public final static long ONE_DAY_IN_MILLISECONDS = 24L * ONE_HOUR_IN_MILLISECONDS;

    public final static Calendar CALENDER_OBJECT = Calendar.getInstance();

}

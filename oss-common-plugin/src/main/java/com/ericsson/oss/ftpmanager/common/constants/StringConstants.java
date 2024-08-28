/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2009 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.ftpmanager.common.constants;

/**
 * @author eeitsik
 * 
 */
public final class StringConstants {

    private StringConstants() {
        //PMD silence !!
    }

    public static final String EMPTY_STRING = "";

    public final static String DELIMITER_EQUALTO = "=";

    public final static String[] EMPTY_STRING_ARRAY = new String[] {};

    public final static String NEW_LINE = System.getProperty("line.separator"); // "\n"

    public final static String SPACE = " ";

    public static final String OPEN_BRACKET = " (";

    public final static String CLOSE_BRACKET = ")";

    public final static String OPEN_BRACKET_WITH_SPACE_AT_END = "( ";

    public final static String CLOSE_BRACKET_WITH_SPACE_BEFORE_AND_AFTER = " ) "; // space at end

    public static final String CELL = "CELL";

    public static final String NMS_SCANNER = "NMS_SCANNER";

    public static final String NETWORK_ELEMENTS = "NETWORK_ELEMENTS";

    public static final String THREADNAME_DELIMITER = "*";

    public final static String FILE_SEPARATOR = System
            .getProperty("file.separator");

    public final static String COMMA = ",";

    public final static String PIPE = "|";

    public final static String SEMI_COLON = ":";

    public static final String CELL_TRACE_POSTFIX_OSS = "CellTraceFilesLocation";

    public static final String UE_TRACE_POSTFIX_OSS = "UeTraceFilesLocation";

}

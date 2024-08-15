package org.processmining.algorithms.statechart.l2l;

public class HandleActivityUtil {

    private static final String HANDLE_ACT_SUFFIX = "+handle";
    
    public static boolean isHandleActivity(String activity) {
        return activity.endsWith(HANDLE_ACT_SUFFIX);
    }
    
    public static String createHandleActivity(String baseLabel) {
        return baseLabel + HANDLE_ACT_SUFFIX;
    }

}

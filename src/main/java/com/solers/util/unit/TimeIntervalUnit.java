/****************************************************************
 *
 * Solers, Inc. as the author of Enterprise File Delivery 2.1 (EFD 2.1)
 * source code submitted herewith to the Government under contract
 * retains those intellectual property rights as set forth by the Federal 
 * Acquisition Regulations agreement (FAR). The Government has 
 * unlimited rights to redistribute copies of the EFD 2.1 in 
 * executable or source format to support operational installation 
 * and software maintenance. Additionally, the executable or 
 * source may be used or modified for by third parties as 
 * directed by the government.
 *
 * (c) 2009 Solers, Inc.
 ***********************************************************/
package com.solers.util.unit;

/**
 * User: DMartin Date: Mar 5, 2007 Time: 2:59:45 PM Enumeration of time units.
 */
public enum TimeIntervalUnit {
    
    SECONDS("S", "Seconds"),
    MINUTES("M", "Minutes"),
    HOURS("H", "Hours"),
    DAYS("D", "Days"),
    MILLISECONDS("MS", "Milliseconds");
    
    public static final int MILLIS_PER_SECOND = 1000;
    public static final int MINUTES_PER_HOUR = 60;
    public static final int HOURS_PER_DAY = 24;

    private final String value;
    private final String displayName;

    private TimeIntervalUnit(String value, String display) {
        this.value = value;
        this.displayName = display;
    }
    
    public String getName() {
        return name();
    }

    /**
     * Return a unique String value to represent this enumeration in a web form.
     * 
     * @return A unique String value that may be used in a call to toValue() to map back to this enum.
     */
    public String toString() {
        return value;
    }


    /**
     * Useful for converting string value from web to the enumerated type.
     * 
     * @param value
     *            A String representing an enumerated value
     * @return enumerated value corresponding to the string value.
     */

    public static TimeIntervalUnit toValue(String value) {
        if (value == null) {
            return null;
        }
        if (value.equals(SECONDS.toString())) {
            return SECONDS;
        } else if (value.equals(MINUTES.toString())) {
            return MINUTES;
        } else if (value.equals(HOURS.toString())) {
            return HOURS;
        } else if (value.equals(DAYS.toString())) {
            return DAYS;
        } else {
            return null;
        }
    }

    /**
     * Returns factor that will convert a number of type unit to milliseconds
     * 
     * @return mulitplication factor to modify time in unit given
     */
    public long getMillisFactor() {
        switch (this) {
            case DAYS:
                return HOURS.getMillisFactor() * HOURS_PER_DAY;
            case HOURS:
                return MINUTES.getMillisFactor() * MINUTES_PER_HOUR;
            case MINUTES:
                return SECONDS.getMillisFactor() * MINUTES_PER_HOUR;
            case SECONDS:
                return MILLIS_PER_SECOND;
            default:
                return 1;
        }
    }
    
    /**
     * Convert a value to milliseconds
     * @param value
     * @return
     */
    public long toMillis(long value) {
        return getMillisFactor() * value;
    }

    /**
     * Return a displayable String value to suitable for a user interface. The value must be configured with a prior call to toValue(value, displayName).
     * 
     * @return A displayable String value
     */
    public String getDisplayName() {
        return displayName;
    }

    public long convert(long value, TimeIntervalUnit unit) {
        long millis = unit.toMillis(value);
        return millis / this.getMillisFactor();
    }
    
    public static String format(long value) {
        return format(value, MILLISECONDS, SECONDS);
    }
    
    public static String format(long value, TimeIntervalUnit unitValue, TimeIntervalUnit base) {
        if (value == 0 || value < base.getMillisFactor()) {
            return String.format("0%s ", unitValue.toString().toLowerCase());
        }
        
        long millis = MILLISECONDS.convert(value, unitValue);
        TimeIntervalUnit prev = DAYS;
        StringBuilder result = new StringBuilder();
        boolean more = true;
        
        do {
            long current = prev.convert(millis, MILLISECONDS);
            
            if (current != 0) {
                result.append(String.format("%d%s ", current, prev.toString().toLowerCase()));
            }
            
            millis -= MILLISECONDS.convert(current, prev);
            
            if (prev == base) {
                more = false;
            }
            prev = prev.prevUnit();
        } while (more);
        return result.toString().trim();
    }
    
    private TimeIntervalUnit prevUnit() {
        switch (this) {
            case DAYS: return HOURS;
            case HOURS: return MINUTES;
            case MINUTES: return SECONDS;
            case SECONDS: 
            default: return MILLISECONDS;
        }
    }
}

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
 * User: DMartin Date: Mar 5, 2007 Time: 2:59:45 PM Enumeration of file size units.
 */
public enum FileSizeUnit {
    BYTES("B", 1, "Bytes (B)"), 
    KILOBYTES("K", 1024, "Kilobytes (KB)"), 
    MEGABYTES("M", 1048576, "Megabytes (MB)"),
    GIGABYTES("G", 1073741824, "Gigabytes (GB)");

    private final String value;
    private final long bytevalue;
    private final String displayName;

    private FileSizeUnit(String value, long bytevalue, String defaultDisplayName) {
        this.value = value;
        this.bytevalue = bytevalue;
        this.displayName = defaultDisplayName;
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

    public static FileSizeUnit toValue(String value) {
        if (value.equals(FileSizeUnit.BYTES.toString())) {
            return FileSizeUnit.BYTES;
        } else if (value.equals(FileSizeUnit.KILOBYTES.toString())) {
            return FileSizeUnit.KILOBYTES;
        } else if (value.equals(FileSizeUnit.MEGABYTES.toString())) {
            return FileSizeUnit.MEGABYTES;
        } else if (value.equals(FileSizeUnit.GIGABYTES.toString())) {
            return FileSizeUnit.GIGABYTES;
        } else {
            throw new RuntimeException("Invalid FileSizeUnit:" + value);
        }
    }

    /**
     * Return a displayable String value to suitable for a user interface. The value must be configured with a prior call to toValue(value, displayName).
     * 
     * @return A displayable String value
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Convert a unit of my type to another unit type, e.g. FileSizeUnit.KILOBYTES.convert(1024, FileSizeUnit.BYTES); // 1
     */
    public double convert(double size, FileSizeUnit units) {
        double bytes = toBytes(size, units);
        double answer = bytes / this.bytevalue;

        // we do not support bits
        if (this.equals(BYTES)) {
            return Math.ceil(answer);
        } else {
            return answer;
        }
    }

    private double toBytes(double size, FileSizeUnit unit) {
        return size * unit.bytevalue;
    }

    /**
     * Automatically calculate an appropriate unit for a given file size.
     * 
     * @param size
     *            the length value
     * @param units
     *            unit of length
     * @return a FileSizeUnit that will make reading the size easiest
     */
    public FileSizeUnit recommendUnit(double size) {
        //try a different unit when size is a fraction or when the size
        //is greater than 1024, indicating that we may be able to subdivide
        //the unit into a larger one.
        if (size > 0 && size < 1) {
            if (BYTES.equals(this))
                return BYTES;
            double newSize = prevUnit().convert(size, this);
            return prevUnit().recommendUnit(newSize);
        } else if (size >= KILOBYTES.bytevalue) {
            if (GIGABYTES.equals(this))
                return GIGABYTES;
            double newSize = nextUnit().convert(size, this);
            return nextUnit().recommendUnit(newSize);
        }
        
        //return the same unit when size is 0 or between 1 and 1023.9 bar.
        return this;
    }

    private FileSizeUnit prevUnit() {
        switch (this) {
            case GIGABYTES:
                return MEGABYTES;
            case MEGABYTES:
                return KILOBYTES;
            case KILOBYTES:
            default:
                return BYTES;
        }
    }

    private FileSizeUnit nextUnit() {
        switch (this) {
            case BYTES:
                return KILOBYTES;
            case KILOBYTES:
                return MEGABYTES;
            case MEGABYTES:
            default:
                return GIGABYTES;
        }
    }
    
    public static String format(double size) {
        FileSizeUnit unit = FileSizeUnit.BYTES.recommendUnit(size);
        String format = (unit.equals(FileSizeUnit.BYTES)) ? "%.0f %s" : "%.2f %s";
        double converted = unit.convert(size, FileSizeUnit.BYTES);
        return String.format(format, converted, unit.getDisplayName());
    }
}

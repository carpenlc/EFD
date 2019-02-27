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
package mil.nga.efd.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import javax.validation.constraints.NotEmpty;

import mil.nga.efd.validations.ValidFilter;

/**
 * User: DMartin Date: Feb 26, 2007 Time: 5:21:40 PM A POJO class that is
 * persisted to a database in the table 'file_filter'. FileFilter holds the
 * information to filter information from inclusion in a inventory. There may be
 * zero or more file filters associated with a content set, hence the Hibernate
 * many-to-one relationship in the annotations below on the content_set
 * property.
 * 
 * The named query GET_FILE_FILTERS returns a list of file filters for a given
 * content set.
 */

//@Configurable("filterPrototype") //no longer use this spring bean
@Entity
@Table(name = "file_filter")
@ValidFilter(message="{contentset.filter.invalid}")
public class FileFilter implements Serializable {

    /**
	 * Eclipse-generated serialVersionUID
	 */
	private static final long serialVersionUID = 4070519099840013507L;

	private static final int PATTERN_COLUMN_SIZE = 255;

    private Long id;
    private String pattern;
    private Pattern patternType;
    private Boolean inclusive;
    
    public FileFilter() {
        
    }
    
    public FileFilter(String pattern, Pattern patternType) {
        setPattern(pattern);
        setPatternType(patternType);
    }

    /**
     * The primary key of the file filter. Its value will be automatically set
     * by hibernate the first time that the file filter is persisted and will
     * not be changed.
     * 
     * @return The id property which is the primary key for a file filter
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * The pattern that this filter should match. This pattern may include a
     * relative file system path, file name extensions, or patterns from the
     * middle of file names or paths. This property must be used in conjuntion
     * with the patternType property to construct an actual filter pattern
     * usable by Java. This value is configurable by the end user.
     * 
     * @return pattern property
     */
    @Column(name = "pattern", nullable = false, length = PATTERN_COLUMN_SIZE)
    @NotEmpty(message="{contentset.filter.pattern}")
    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }
    
    /**
     * Whether this filter is used to include or exclude files.
     */
    @Column(name = "inclusive", nullable = true)
    public Boolean isInclusive() {
        return inclusive == null ? Boolean.FALSE : inclusive;
    }

    @Transient
    public boolean getInclusive() {
        return isInclusive().booleanValue();
    }
    
    public void setInclusive(Boolean inclusive) {
        this.inclusive = inclusive;
    }
    
    public void setInclusive(boolean inclusive) {
        setInclusive(Boolean.valueOf(inclusive));
    }

    /**
     * The type of file filter pattern that this filter represents. This
     * property is used in conjunction with the pattern property to construct an
     * actual filter pattern usable by Java. This value is configurable by the
     * end user.
     * 
     * @return patternType property
     */
    @Column(name = "pattern_type", nullable = false)
    public Pattern getPatternType() {
        return patternType;
    }

    public void setPatternType(Pattern patternType) {
        this.patternType = patternType;
    }
    
    public boolean equals(Object o) {
        if (o instanceof FileFilter) {
            FileFilter f = (FileFilter) o;
            return equals(pattern, f.pattern) && equals(patternType, f.patternType);
        }
        return false;
    }
    
    private boolean equals(Object first, Object second) {
        if (first == null && second == null) {
            return true;
        }
        if (first == null ^ second == null) {
            return false;
        }
        return first.equals(second);
    }
    
    public int hashCode() {
        return 
            (pattern == null ? 0 : pattern.hashCode()) | 
            (patternType == null ? 0 : patternType.hashCode());
    }
    
    public static enum Pattern {
        BEGINS("B", "Begins with"), 
        ENDS("E", "Ends with"), 
        CONTAINS("C", "Contains"),
        REGEX("R", "Regular expression");

        private final String value;
        private final String displayName;

        private Pattern(String value, String displayName) {
            this.value = value;
            this.displayName = displayName;
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

        public static Pattern toValue(String value) {
            for (Pattern p : Pattern.values()) {
                if (p.value.equals(value)) return p;
            }
            throw new IllegalArgumentException("No such Pattern.");
        }

        /**
         * Return a displayable String value to suitable for a user interface. The value must be configured with a prior call to toValue(value, displayName).
         * 
         * @return A displayable String value
         */
        public String getDisplayName() {
            return displayName;
        }

    }
}

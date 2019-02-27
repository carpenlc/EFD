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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang3.StringUtils;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import org.springframework.beans.factory.annotation.Configurable;

import mil.nga.efd.controllers.ContentSetDAO;

import mil.nga.efd.validations.ExistingDirectory;
import mil.nga.efd.validations.GbsIsEnabled;
import mil.nga.efd.validations.Readable;
import mil.nga.efd.validations.RestrictedPath;
import mil.nga.efd.validations.ValidFileName;
import mil.nga.efd.validations.ValidFtpConnection;
import com.solers.delivery.util.FileSystemUtil;
import com.solers.util.unit.TimeIntervalUnit;

/**
 * Created by IntelliJ IDEA. User: DMartin Date: Feb 21, 2007 Time: 10:49:40 AM
 * 
 * A pojo class that is persisted to a database in the table 'content_set'. The
 * ContentSet class holds the information that is common to all content sets
 * whether they are suppliers or consumers. When a row in the content_set table
 * represents consumer, there will be a one-to-one relationship to the
 * consumer_content_set table where the information specific to a consumer is
 * stored.
 * 
 * The primary key on this table is referenced as a foreign key from the primary
 * key on the consumer_set_info table.
 * 
 * The named query GET_SUPPLIER_BY_NAME may be used to query a single supplier
 * content set if its logical name is known. This query is most likely useful to
 * the transport when processing requests.
 * 
 * The named quary GET_SUPPLIER_SETS returns a complete list of supplier content
 * sets.
 */

@Configurable("supplierPrototype")
@Entity
@Table(name = "content_set")
@Inheritance(strategy = InheritanceType.JOINED)
@NamedQueries({ 
    @NamedQuery(
    		name = ContentSetDAO.GET_SUPPLIER_BY_NAME, 
    		query = "SELECT c FROM ContentSet c WHERE c.name = :name"),
    @NamedQuery(
    		name = ContentSetDAO.GET_SUPPLIER_SETS, 
    		query = "SELECT c FROM ContentSet c WHERE c.supplier = 1")
    })
@ValidFtpConnection
public class ContentSet implements Serializable {
    
    /**
	 * Eclipse-generated serialVersionUID
	 */
	private static final long serialVersionUID = 5538141587676485440L;
	
	private static final int NAME_LENGTH = 80;
    private static final int PATH_LENGTH = 255;

    private Long id;
    private String name;
    private String path;
    private boolean enabled;
    private boolean supplier = true;
    private int inventoryInterval;
    private TimeIntervalUnit inventoryIntervalUnit;
    private long creationTime;
    private String description;
    private boolean supportsGbsTransport;
    private FtpConnection ftpConnection;
    private List<FileFilter> fileFilters = Collections.emptyList();
    private Set<ResourceParameter> resourceParameters = Collections.emptySet();
    private Set<AllowedHost> aliases;
    private Set<ScheduleExpression> expressions = new HashSet<ScheduleExpression>();
    private String displayPath;
    private long updateTime;
    
    public ContentSet() {
        setCreationTime(System.currentTimeMillis());
    }

    /**
     * The primary key of the content set. Its value will be automatically set
     * by hibernate the first time that the content set is persiste and will not
     * be changed.
     * 
     * @return The id property which is the primary key for a content set.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @OneToMany(targetEntity = FileFilter.class, cascade = CascadeType.ALL, fetch=FetchType.EAGER)
    @JoinColumn(name = "content_set")
    @Valid
    public List<FileFilter> getFileFilters() {
        return fileFilters;
    }

    public void setFileFilters(List<FileFilter> fileFilters) {
        this.fileFilters = fileFilters;
    }
    
    public void setFilters(List<FileFilter> filters) {
        setFileFilters(filters);
    }
    
    @OneToMany(targetEntity = ResourceParameter.class, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    public Set<ResourceParameter> getResourceParameters() {
        return resourceParameters;
    }
    
    @Transient
    public Set<ResourceParameter> getParameters() {
        return new HashSet<ResourceParameter>(resourceParameters == null ? 
            new HashSet<ResourceParameter>(0) : resourceParameters);
    }
    
    @Transient
    public Map<String, Object> getResourceParametersAsMap() {
        Map<String, Object> params = new HashMap<String, Object>(resourceParameters.size());
        for (ResourceParameter p : resourceParameters) {
            params.put(p.getName(), p.getValue());
        }
        return params;
    }
    
    public void setResourceParameters(Set<ResourceParameter> resourceParameters) {
        this.resourceParameters = resourceParameters;
    }
    
    /**
     * It is useful to get an actual List<FileFilter> instead of whatever
     * lazy collection Hibernate might pass back in getFileFilters().
     * 
     * XStream for one, cannot handle hibernates lazy collections.
     * @return
     */
    @Transient
    public List<FileFilter> getFilters() {
        List<FileFilter> result = new ArrayList<FileFilter>();
        
        List<FileFilter> filters = getFileFilters();
        if (filters != null) {
            for (FileFilter f : getFileFilters()) {
                result.add(f);
            }
        }
        
        return result;
    }


    /**
     * A logical name assigned to the content set by the end user. It must be
     * unique and may be used by a named query to fetch a content set from the
     * persistence mechanism.
     * 
     * @return the name property
     */
    @Column(nullable = false, length = NAME_LENGTH, unique=true)
    @NotEmpty(message="{contentset.name}")
    @Pattern(regexp = "[\\w-]*", message="{contentset.name.invalid}")
    @Size(max=NAME_LENGTH, message="{contentset.name.too.long}")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * A file system path pointing to the base folder of a content set. For a
     * supplier, it is the starting point for constructing an inventory and for
     * satisfying requests from a consumer. For a consumer, it is the starting
     * point for storing content requested from a supplier. This value is set by
     * the end user.
     * 
     * @return the path property
     */
    @Column(nullable = false, length = PATH_LENGTH)
    @NotEmpty(message="{contentset.path}")
    @ValidFileName
    @RestrictedPath
    @ExistingDirectory
    @Readable
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = FileSystemUtil.correctPath(path);
    }

    /**
     * A flag that indicates whether the consumer or supplier should be actively
     * running. The value is used during system startup to activate the
     * processing of content sets. It may be updated during normal operations by
     * an end user.
     * 
     * @return the enabled property
     */
    @Column(name = "is_enabled", nullable = false)
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * A flag that specifies whether this content set is a supplier or consumer.
     * It is set once when a content set is created. It is used by named queries
     * to filter supplier or consumer content sets. The flag is set to true for
     * a supplier, false for a consumer.
     * 
     * @return the supplier property
     */
    @Column(name = "is_supplier", nullable = false)
    public boolean isSupplier() {
        return supplier;
    }

    public void setSupplier(boolean supplier) {
        this.supplier = supplier;
    }

    /**
     * A quantity of time that represents the minimum amount of time to wait
     * before processing an inventory. For a supplier, it is the time to wait
     * before generating a new inventory. For a consumer, it is the amount of
     * time to wait before requesting a new inventory from the supplier. This
     * value is set by the end user.
     * 
     * This quantity must be used in conjunction with the inventoryIntervalUnit
     * property to translate the quantity into a real amount of time.
     * 
     * @return inventoryInterval property
     */
    @Column(name = "inventory_interval", nullable = false)
    @Min(value=1, message="{contentset.inventoryinterval}")
    public int getInventoryInterval() {
        return inventoryInterval;
    }

    public void setInventoryInterval(int inventoryInterval) {
        this.inventoryInterval = inventoryInterval;
    }

    /**
     * A unit of time used to translate the inventoryInterval property from a
     * number to a real amount of time. This value is set by the end user.
     * 
     * @return inventoryIntervalUnit property
     */
    @Column(name = "inventory_interval_unit", nullable = false)
    public TimeIntervalUnit getInventoryIntervalUnit() {
        return inventoryIntervalUnit;
    }

    public void setInventoryIntervalUnit(TimeIntervalUnit inventoryIntervalUnit) {
        this.inventoryIntervalUnit = inventoryIntervalUnit;
    }

    /**
     * Timestamp when the prototype for a new content set is created. It is set
     * only once.
     * 
     * @return creationTime property
     */
    @Column(name = "creation_time", nullable = false, updatable=false)
    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    /**
     * A human readable description of a content set that is specified by an end
     * user. It is not used to perform any processing.
     * 
     * @return description property
     */
    @Column(name = "description", nullable = true)
    @Pattern(regexp = "^[a-zA-Z0-9\\s?!_.-]{0,255}+$", 
            message="{contentset.description.invalid}")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Flag that specifies whether this content set has the ability to use GBS
     * as a transport. A supplier would use GBS to send requested data to a
     * consumer. A consumer would use GBS to receive requested data. This value
     * is configurable by the end user.
     * 
     * @return supportsGbsTransport property
     */
    @Column(name = "supports_gbs_transport", nullable = false)
    @GbsIsEnabled
    public boolean isSupportsGbsTransport() {
        return supportsGbsTransport;
    }

    public void setSupportsGbsTransport(boolean supportsGbsTransport) {
        this.supportsGbsTransport = supportsGbsTransport;
    }
    
    @OneToOne(targetEntity = FtpConnection.class, cascade = CascadeType.ALL)
    @JoinColumn(name = "connectionId", insertable = true, updatable = true, nullable = true)
    @Valid
    public FtpConnection getFtpConnection() {
        return ftpConnection;
    }

    public void setFtpConnection(FtpConnection ftpConnection) {
        this.ftpConnection = ftpConnection;
    }
    
    //@Transient
    @Override
    public String toString() {
        return String.format("Id: %s, Name: %s", this.id, this.getName());
    }
    
    @OneToMany(targetEntity = ScheduleExpression.class, cascade = CascadeType.ALL, fetch=FetchType.EAGER)    
    @JoinColumn(name = "content_set")
    @Valid
    public Set<ScheduleExpression> getExpressions() {      
        return expressions;     
    }
        
    public void setExpressions(Set<ScheduleExpression> expressions) {
        this.expressions = expressions;
    }    
    
    public void addScheduleExpression(ScheduleExpression expression) {
        getExpressions().add(expression);
    }
    
    @Transient
    public Set<ScheduleExpression> getScheduleExpressions() {
       Set<ScheduleExpression> result = new HashSet<ScheduleExpression>();
        
        Set<ScheduleExpression> exprs = getExpressions();
        if (exprs != null) {
            for (ScheduleExpression expr : exprs) {
                result.add(expr);
            }
        }
        
        return result;
    }
    
    public void setScheduleExpressions(Set<ScheduleExpression> expressions) {
        setExpressions(expressions);
    }
    
    @ManyToMany(fetch = FetchType.EAGER)
    @Valid
    public Set<AllowedHost> getAliases() {
        return aliases;
    }

    public void setAliases(Set<AllowedHost> aliases) {
        this.aliases = aliases;
    }
    
    @Transient
    public Set<AllowedHost> getAllowedHosts() {
        Set<AllowedHost> result = new HashSet<AllowedHost>();
        
        Set<AllowedHost> aliases = getAliases();
        if (aliases != null) {
            for (AllowedHost host : aliases) {
                result.add(host);
            }
        }
        
        return result;
    }
    
    public void setAllowedHosts(Set<AllowedHost> hosts) {
        setAliases(hosts);
    }
    
    /**
     * @param commonName
     * @return True if {@code commonName} is allowed by this content set
     */
    public boolean allows(String commonName) {
        if (StringUtils.isEmpty(commonName) || aliases == null) {
            return false;
        }
        
        for (AllowedHost host : getAliases()) {
            if (commonName.equals(host.getCommonName())) {
                return true;
            }
        }
        return false;
    }
    
    public void addAllowedHost(AllowedHost host) {
        if (aliases == null) {
            aliases = new HashSet<AllowedHost>();
        }
        aliases.add(host);
    }
    
    public boolean removeAllowedHost(AllowedHost host) {
        if (aliases != null) {
            return aliases.remove(host);
        }
        return false;
    }
    
    @Column(name = "displayPath", nullable = true)
    public String getDisplayPath() {
        return displayPath;
    }

    public void setDisplayPath(String displayPath) {
        this.displayPath = displayPath;
    }

    @Column(name = "update_time")
    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }
}

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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Min;

import org.springframework.beans.factory.annotation.Configurable;

import mil.nga.efd.controllers.ConsumerContentSetDAO;
import mil.nga.efd.validations.Writable;

import com.solers.util.unit.FileSizeUnit;
import com.solers.util.unit.TimeIntervalUnit;


/**
 * User: DMartin Date: Feb 26, 2007 Time: 4:08:51 PM A pojo class that is persisted to a database in the table 'consumer_set_info'. ConsumerContentSet extends
 * the ContentSet class to hold the information that is specific to a consumer content set. The information in the subclass is stored in the database table
 * consumer_set_info and has a one-to-one relationship to the content_set table.
 * 
 * The primary key on this table is also a foreign key to the content_set table.
 * 
 * The named query GET_CONSUMER_BY_NAME may be used to query a single consumer content set if its logical name is known.
 * 
 * The named query GET_CONSUMER_SETS returns a complete list of consumer content sets.
 */

@Configurable("consumerPrototype")
@Entity
@Table(name = "content_set_consumer")
@PrimaryKeyJoinColumn(name = "id")
@NamedQueries({ 
    @NamedQuery(
            name = ConsumerContentSetDAO.GET_CONSUMER_BY_NAME, 
            query = "SELECT c FROM ConsumerContentSet c WHERE c.name LIKE :name"), 
    @NamedQuery(
            name = ConsumerContentSetDAO.GET_CONSUMER_SETS, 
            query = "SELECT c FROM ConsumerContentSet c WHERE c.supplier = 0") 
    })
public class ConsumerContentSet extends ContentSet {

    /**
	 * Eclipse-generated serialVersionUID
	 */
	private static final long serialVersionUID = -6157042150360635294L;
	
	private String supplierName;
    private String supplierAddress;
    private int supplierPort;
    private int fileDeleteDelay;
    private TimeIntervalUnit fileDeleteDelayUnit;
    private long maxFileSize;
    private FileSizeUnit maxFileSizeUnit;
    private String virtualManifest;
    private Integer concurrentTransfers;

    public ConsumerContentSet() {
        super();
        this.setSupplier(false);
    }

    /**
     * The supplierName property specifies the name of a supplier that exists 
     * on a remote node. The name will be used by the transport to match the 
     * consumer request with its corresponding supplier. The transport will 
     * include this information in its http request.
     * 
     * @return supplierName property
     */
    @Column(name = "supplier_name", nullable = false)
    @NotEmpty(message="{contentset.supplier.name}")
    @Pattern(regexp = "^[a-zA-Z0-9\\s_.-]+$", 
            message="{contentset.supplier.name.invalid}")
    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    @Transient
    @Writable
    public String getPath() {
        return super.getPath();
    }
    
    /**
     * The ip address of the node containing the supplier. The transport will 
     * need this information to construct its http request.
     * 
     * @return supplierAddress property
     */
    @Column(name = "supplier_address", nullable = false)
    @NotEmpty(message="{contentset.supplier.address}")
    @Pattern(regexp = "^[a-zA-Z0-9\\s_.-]+$", 
            message="{contentset.supplier.address.invalid}")
    public String getSupplierAddress() {
        return supplierAddress;
    }

    public void setSupplierAddress(String supplierAddress) {
        this.supplierAddress = supplierAddress;
    }

    /**
     * The port used by the transport on the supplier node. This information is used by the transport to construct its http request.
     * 
     * @return supplierPort property
     */
    @Column(name = "supplier_port", nullable = false)
    @Min(value=1, message="{contentset.supplier.port}")
    public int getSupplierPort() {
        return supplierPort;
    }

    public void setSupplierPort(int supplierPort) {
        this.supplierPort = supplierPort;
    }
       
    /**
     * A quantity of time to wait before deleting a file or folder that is no longer in the inventory. this quantity when combined with the fileDeleteDelayUnit
     * property can be translated into an actual amount of time.
     * 
     * @return fileDeleteDelayUnit property
     */
    @Column(name = "file_delete_delay", nullable = false)
    @Min(value=0, message="{contentset.deletedelay}")
    public int getFileDeleteDelay() {
        return fileDeleteDelay;
    }

    public void setFileDeleteDelay(int fileDeleteDelay) {
        this.fileDeleteDelay = fileDeleteDelay;
    }

    /**
     * A time unit corresponding to the value stored in the fileDeleteDelay property that describes how to translate the value into an actual time.
     * 
     * @return fileDeleteDelayUnit property
     */
    @Column(name = "file_delete_delay_unit", nullable = false)
    public TimeIntervalUnit getFileDeleteDelayUnit() {
        return fileDeleteDelayUnit;
    }

    public void setFileDeleteDelayUnit(TimeIntervalUnit fileDeleteDelayUnit) {
        this.fileDeleteDelayUnit = fileDeleteDelayUnit;
    }

    /**
     * The maximum size of a file that will be accepted by this consumer content set. The quantity specified by this property must be combined with the units
     * specified by property maxFileSizeUnit to translated this limit into an actual file size.
     * 
     * @return maxFileSize property
     */
    @Column(name = "max_file_size", nullable = true)
    @Min(value=0, message="{contentset.maxfilesize}")
    public long getMaxFileSize() {
        return maxFileSize;
    }

    public void setMaxFileSize(long maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    /**
     * A file size unit corresponding to the value stored in the maxFileSize property that describes how to translate the value into an actual file size.
     * 
     * @return maxFileSizeUnit property
     */
    @Column(name = "max_file_size_unit", nullable = true)
    public FileSizeUnit getMaxFileSizeUnit() {
        return maxFileSizeUnit;
    }

    public void setMaxFileSizeUnit(FileSizeUnit maxFileSizeUnit) {
        this.maxFileSizeUnit = maxFileSizeUnit;
    }
    
    /**
     * A resource URI that indicates an alternate source for comparing the 
     * supplier manifest.
     */
    @Column(name = "virtual_manifest", nullable = true)
    @Pattern(regexp = "^[a-zA-Z0-9\\s?!_.-]{0,255}+$", 
            message="{contentset.virtualmanifest.invalid}")
    public String getVirtualManifest() {
        return virtualManifest;
    }
    
    public void setVirtualManifest(String virtualManifest) {
        this.virtualManifest = virtualManifest;
    }

    @Column
    @Min(value=1, message="{contentset.concurrentTransfers.min}")
    public Integer getConcurrentTransfers() {
        return concurrentTransfers;
    }

    public void setConcurrentTransfers(Integer concurrentTransfers) {
        this.concurrentTransfers = concurrentTransfers;
    }
}

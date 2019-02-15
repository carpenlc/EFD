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

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
@Entity
@Table(name="alerts")
public class Alert {
    
    public static enum AlertType {
        ADMIN, USER, ALL
    }
    
    private Long id;
    private Date timestamp;
    private String message;
    private AlertType type;
    private boolean unread; // Derby couldn't handle a column named "read"
    
    public Alert() {
        
    }
    
    public Alert(String message, AlertType type) {
        setMessage(message);
        setType(type);
        setTimestamp(new Date());
        setUnread(true);
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    @Column
    public Date getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
    
    @Column
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }

    @Column
    public AlertType getType() {
        return type;
    }

    public void setType(AlertType type) {
        this.type = type;
    }

    @Column
    public boolean isUnread() {
        return unread;
    }

    public void setUnread(boolean unread) {
        this.unread = unread;
    }
    
}

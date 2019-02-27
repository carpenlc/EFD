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
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import mil.nga.efd.controllers.AllowedHostDAO;

//import com.solers.delivery.domain.validations.NotBlank;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
@Entity
@Table(name="allowed_hosts")
@NamedQuery(name = AllowedHostDAO.GET_BY_ALIAS, 
        query = "SELECT a FROM AllowedHost a WHERE a.alias = :alias")
public class AllowedHost implements Serializable {
    
    /**
	 * Eclipse-generated serialVersionUID
	 */
	private static final long serialVersionUID = -1810446287782648885L;
	
	private Long id;
    private String alias;
    private String commonName;
    
    public AllowedHost() {
        
    }
    
    public AllowedHost(String alias, String commonName) {
        setAlias(alias);
        setCommonName(commonName);
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
    @NotBlank(message="{allowedhost.alias.required}")
    @Pattern(regexp = "^[a-zA-Z0-9\\s_.-]+$", 
            message="{allowedhost.alias.invalid}")
    public String getAlias() {
        return alias;
    }
    
    public void setAlias(String alias) {
        this.alias = alias;
    }
    
    @Column(nullable=false, updatable=true)
    @NotBlank(message="{allowedhost.commonname.required}")
    @Pattern(regexp = "^[a-zA-Z0-9\\s_.-]+$", 
            message="{allowedhost.commonname.invalid}")
    public String getCommonName() {
        return commonName;
    }
    
    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AllowedHost) {
            return ((AllowedHost) obj).alias.equals(alias);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return alias == null ? 0 : alias.hashCode();
    }
    
    @Override
    public String toString() {
        return alias;
    }
}

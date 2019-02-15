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
package com.solers.delivery.domain;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import com.solers.delivery.daos.PasswordDAO;


@Entity
@Table(name="password")
@NamedQueries({ 
    @NamedQuery(name = PasswordDAO.GET_PASSWORDS, query = "SELECT p FROM Password p WHERE p.user.id = :id"),
    @NamedQuery(name = PasswordDAO.DELETE_OLDEST, query = "DELETE FROM Password p WHERE p.id = (select pp.id from Password pp where pp.user.id = :id and createdDate = (select min(ppp.createdDate) from Password ppp where ppp.user.id = :id))"),
    @NamedQuery(name = PasswordDAO.DELETE_ALL, query = "DELETE FROM Password p WHERE p.user.id = :id")
    })
public class Password {

    private long id;
    private Date createdDate;
    private String password;
    private User user;
    private Boolean adminCreated;

    public Password() {}

    public Password(String password, User user) {
        this.createdDate = new Date();
        this.password = password;
        this.user = user;
    }

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Column(nullable=false, updatable=false)
    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    @Column(nullable=false)
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @ManyToOne(cascade= {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name="users",
                referencedColumnName="id",
                updatable=false
    )
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
    
    @Column
    public Boolean isAdminCreated() {
    	return adminCreated == null ? false : adminCreated;
    }
   
   public void setAdminCreated(Boolean adminCreated) {
	   this.adminCreated = adminCreated;
	   
   }
   
    
    
    
}

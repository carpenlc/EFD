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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.Pattern;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;

import com.solers.delivery.daos.UserDAO;

@Configurable("userPrototype")
@Entity
@Table(name = "users")
@NamedQuery(name = UserDAO.GET_USER_BY_USERNAME, 
        query = "SELECT u FROM User u WHERE u.username=:username")
public class User {

    private Long id;
    private String username;
    private boolean enabled;
    private boolean adminRole = false;
    private boolean initialPassword;
    private String firstName, lastName;
    private Date lastLogin;
    private Date currLogin; 
    private int failedLogins = 0;
    private Integer lastFailedLogins = 0;
    private String location;
    private String lastLocation;

    @Transient
    public String getName() {
        return String.format("%s %s", getFirstName(), getLastName());
    }
    
    @Transient
    @Override
    public String toString() {
        return String.format("Id: %s, Name: %s", this.id, this.getUsername());
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Column(nullable = false, unique = true)
    @NotEmpty(message="{user.username.required}")
    @Pattern(regex = "[\\w-]*", message="{user.username.invalid}")
    public String getUsername() {
        return username;
    }

    @Column
    public Date getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }

    @Column
    public Date getCurrLogin() {
        return currLogin;
    }

    public void setCurrLogin(Date currLogin) {
        this.currLogin = currLogin;
    }

    @Column(nullable = false)
    public int getFailedLogins() {
        return failedLogins;
    }

    public void setFailedLogins(int count) {
        this.failedLogins = count;
    }

    @Column
    public Integer getLastFailedLogins() {
        return lastFailedLogins;
    }

    public void setLastFailedLogins(Integer count) {
        this.lastFailedLogins = count;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Column(nullable = false)
    public boolean isEnabled() {
        return this.enabled;
    }

    @Column(nullable = false)
    public boolean isAdminRole() {
        return this.adminRole;
    }

    public void setAdminRole(boolean adminRole) {
        this.adminRole = adminRole;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @Column(nullable = false)
    @NotEmpty(message="{user.first.required}")
    @Pattern(regex = "^[a-zA-Z\\s'.-]+$", message="{user.name.invalid}")
    public String getFirstName() {
        return firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Column(nullable = false)
    @NotEmpty(message="{user.last.required}")
    @Pattern(regex = "^[a-zA-Z\\s'.-]+$", message="{user.name.invalid}")
    public String getLastName() {
        return lastName;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Column
    public String getLocation() {
        return location;
    }
    
    public void setLastLocation(String lastLocation) {
        this.lastLocation = lastLocation;
    }

    @Column
    public String getLastLocation() {
        return lastLocation;
    }

    @Column(nullable = false)
    public boolean isInitialPassword() {
        return initialPassword;
    }

    public void setInitialPassword(boolean initialPassword) {
        this.initialPassword = initialPassword;
    }

    @Transient()
    public GrantedAuthority[] getAuthorities() {
        if (adminRole) {
            return new GrantedAuthority[] { new GrantedAuthorityImpl("ROLE_ADMIN") };
        } else {
            return new GrantedAuthority[] { new GrantedAuthorityImpl("ROLE_USER") };
        }
    }

    @Transient()
    public void updateLastLogin(String remoteIPAddr) {        
        this.lastFailedLogins = this.failedLogins;
        this.failedLogins = 0;
        
        // The first time login, there is no last login history, use the current location. Same for lastLogin
        this.lastLocation = (this.location == null) ? remoteIPAddr : this.location;
        this.location = remoteIPAddr;
        
        this.lastLogin = (this.currLogin == null) ? new Date() : this.currLogin;
        this.currLogin = new Date();
    }

    @Transient()
    public void updateFailedLogin() {
        this.failedLogins++;
    }
  
     
}

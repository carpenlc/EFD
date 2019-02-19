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
package com.solers.delivery.transport.gbs.ftp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import org.apache.ftpserver.ftplet.Authentication;
import org.apache.ftpserver.ftplet.AuthenticationFailedException;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.usermanager.AnonymousAuthentication;
import org.apache.ftpserver.usermanager.UsernamePasswordAuthentication;
import org.apache.ftpserver.usermanager.impl.AbstractUserManager;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.ConcurrentLoginPermission;
import org.apache.ftpserver.usermanager.impl.ConcurrentLoginRequest;
import org.apache.ftpserver.usermanager.impl.TransferRatePermission;
import org.apache.ftpserver.usermanager.impl.TransferRateRequest;
import org.apache.ftpserver.usermanager.impl.WritePermission;
import org.apache.ftpserver.util.BaseProperties;
import org.apache.log4j.Logger;

/**
 * User Manager implementation
 */
public class FTPUserManager implements UserManager {
    
    private static final Logger log = Logger.getLogger(FTPUserManager.class);

    private BaseUser user = new BaseUser();  
    private static final int MAX_IDLE    = 30; 
    private final static String PREFIX  = "ftpserver.user.";
    private BaseProperties userDataProp = new BaseProperties();
    
    @Override
    public String getAdminName() throws FtpException {
        return user.getName();
    }

    @Override
    public boolean isAdmin(String arg0) throws FtpException {
        return arg0.equals(user.getName());
    }

    public void setName(String name) {
        user.setName(name);
    }
    
    public void setPassword(String password) {
        user.setPassword(password);
    }
    
    public void setDir(String dir) {    
    }
    
    public void setArchive(String archive) {
        user.setHomeDirectory(archive);
        userDataProp.setProperty(AbstractUserManager.ATTR_HOME, archive);
    }
    
    /**
     * Configure user manager.
     */
    public void configure() {
        user.setEnabled(true);
        user.setMaxIdleTime(MAX_IDLE);
        
        try {
           save(user);
        } catch (FtpException e) {
            e.printStackTrace();
            log.error("Cannot save user data" + e.getMessage());
        }        
    }

    
    /**
     * Save user data. Store the properties.
     */
    public synchronized void save(User usr) throws FtpException {      
       // null value check
       if(usr.getName() == null) {
           throw new NullPointerException("User name is null.");
       }      
       
       String thisPrefix = PREFIX + usr.getName() + '.';
       
       // set other properties
       userDataProp.setProperty(thisPrefix + AbstractUserManager.ATTR_PASSWORD, user.getPassword());
       
       String home = usr.getHomeDirectory();
       if (home == null) {
           home = "/";
       }
       
       userDataProp.setProperty(thisPrefix + AbstractUserManager.ATTR_HOME, home);
       userDataProp.setProperty(thisPrefix + AbstractUserManager.ATTR_ENABLE,        usr.getEnabled());
       userDataProp.setProperty(thisPrefix + AbstractUserManager.ATTR_WRITE_PERM, true);
       userDataProp.setProperty(thisPrefix + AbstractUserManager.ATTR_MAX_IDLE_TIME, usr.getMaxIdleTime());
       
       TransferRateRequest transferRateRequest = new TransferRateRequest();
       transferRateRequest = (TransferRateRequest) usr.authorize(transferRateRequest);
       
       if(transferRateRequest != null) {
           userDataProp.setProperty(thisPrefix + AbstractUserManager.ATTR_MAX_UPLOAD_RATE,   
                   transferRateRequest.getMaxUploadRate());
           userDataProp.setProperty(thisPrefix + AbstractUserManager.ATTR_MAX_DOWNLOAD_RATE, 
                   transferRateRequest.getMaxDownloadRate());
       } else {
           userDataProp.remove(thisPrefix + AbstractUserManager.ATTR_MAX_UPLOAD_RATE);
           userDataProp.remove(thisPrefix + AbstractUserManager.ATTR_MAX_DOWNLOAD_RATE);       
       }
       
       // request that always will succeed
       ConcurrentLoginRequest concurrentLoginRequest = new ConcurrentLoginRequest(0, 0);
       concurrentLoginRequest = (ConcurrentLoginRequest) usr.authorize(concurrentLoginRequest);
       
       if(concurrentLoginRequest != null) {
           userDataProp.setProperty(thisPrefix + AbstractUserManager.ATTR_MAX_LOGIN_NUMBER, 
                   concurrentLoginRequest.getMaxConcurrentLogins());
           userDataProp.setProperty(thisPrefix + AbstractUserManager.ATTR_MAX_LOGIN_PER_IP, 
                   concurrentLoginRequest.getMaxConcurrentLoginsPerIP());
       } else {
           userDataProp.remove(thisPrefix + AbstractUserManager.ATTR_MAX_LOGIN_NUMBER);
           userDataProp.remove(thisPrefix + AbstractUserManager.ATTR_MAX_LOGIN_PER_IP);   
       }
    }
     
    /**
     * Delete an user. Removes all this user entries from the properties.
     * After removing the corresponding from the properties, save the data.
     */
    public synchronized void delete(String usrName) throws FtpException {
        if (usrName != null && usrName.length() > 0 && usrName.equals(user.getName())) {
           log.error("Cannot delete admin user:" + usrName);
           throw new FtpException("Cannot delete admin user:" + usrName);
        }
        
        // remove entries from properties
        String thisPrefix = PREFIX + usrName + '.';
        Enumeration<?> propNames = userDataProp.propertyNames();
        ArrayList<String> remKeys = new ArrayList<String>();
        while(propNames.hasMoreElements()) {
            String thisKey = propNames.nextElement().toString();
            if(thisKey.startsWith(thisPrefix)) {
                remKeys.add(thisKey);
            }
        }
        Iterator<String> remKeysIt = remKeys.iterator();
        while (remKeysIt.hasNext()) {
            userDataProp.remove(remKeysIt.next());
        }
    }
    
    /**
     * Get all user names.
     */
    public synchronized String[] getAllUserNames() {
        // get all user names
        String suffix = '.' + AbstractUserManager.ATTR_HOME;
        ArrayList<String> ulst = new ArrayList<String>();
        Enumeration<?> allKeys = userDataProp.propertyNames();
        int prefixlen = PREFIX.length();
        int suffixlen = suffix.length();
        while(allKeys.hasMoreElements()) {
            String key = (String)allKeys.nextElement();
            if(key.endsWith(suffix)) {
                String name = key.substring(prefixlen);
                int endIndex = name.length() - suffixlen;
                name = name.substring(0, endIndex);
                ulst.add(name);
            }
        }
        
        Collections.sort(ulst);
        return ulst.toArray(new String[0]);
    }

    /**
     * Load user data.
     */
    public synchronized User getUserByName(String userName) {      
        if (!doesExist(userName)) {
            return null;
        }
        
        String baseKey = PREFIX + userName + '.';
        BaseUser user = new BaseUser();
        user.setName(userName);
        user.setEnabled(userDataProp.getBoolean(baseKey + AbstractUserManager.ATTR_ENABLE, true));
        user.setHomeDirectory( userDataProp.getProperty(baseKey + AbstractUserManager.ATTR_HOME, "/") );
        
        List<Authority> authorities = new ArrayList<Authority>();
        
        if(userDataProp.getBoolean(baseKey + AbstractUserManager.ATTR_WRITE_PERM, false)) {
            authorities.add(new WritePermission());
        }
        
        int maxLogin = userDataProp.getInteger(baseKey + AbstractUserManager.ATTR_MAX_LOGIN_NUMBER, 0);
        int maxLoginPerIP = userDataProp.getInteger(baseKey + AbstractUserManager.ATTR_MAX_LOGIN_PER_IP, 0);
        
        authorities.add(new ConcurrentLoginPermission(maxLogin, maxLoginPerIP));

        int uploadRate = userDataProp.getInteger(baseKey + AbstractUserManager.ATTR_MAX_UPLOAD_RATE, 0);
        int downloadRate = userDataProp.getInteger(baseKey + AbstractUserManager.ATTR_MAX_DOWNLOAD_RATE, 0);        
        authorities.add(new TransferRatePermission(downloadRate, uploadRate));       
        user.setAuthorities(authorities);       
        user.setMaxIdleTime(userDataProp.getInteger(baseKey + AbstractUserManager.ATTR_MAX_IDLE_TIME, 0));

        return user;
    }
    
    /**
     * User existence check
     */
    public synchronized boolean doesExist(String name) {
        String key = PREFIX + name + '.' + AbstractUserManager.ATTR_HOME; 
        return userDataProp.containsKey(key);
    }
    
    /**
     * User authenticate method
     */
    public synchronized User authenticate(Authentication authentication) throws AuthenticationFailedException {       
        if(authentication instanceof UsernamePasswordAuthentication) {
            UsernamePasswordAuthentication upauth = (UsernamePasswordAuthentication) authentication;
            
            String user = upauth.getUsername(); 
            String password = upauth.getPassword(); 
        
            if(user == null) {
                throw new AuthenticationFailedException("Authentication failed");
            }
            
            if(password == null) {
                password = "";
            }
            
            String passVal = userDataProp.getProperty(PREFIX + user + '.' + AbstractUserManager.ATTR_PASSWORD);

            if(password.equals(passVal)) {
                return getUserByName(user);
            } else {
                throw new AuthenticationFailedException("Authentication failed");
            }
            
        } else if(authentication instanceof AnonymousAuthentication) {
            if(doesExist("anonymous")) {
                return getUserByName("anonymous");
            } else {
                throw new AuthenticationFailedException("Authentication failed");
            }
        } else {
            throw new IllegalArgumentException("Authentication not supported by this user manager");
        }
    }
        
    /**
     * Close the user manager - remove existing entries.
     */
    public synchronized void dispose() {
        if (userDataProp != null) {
            userDataProp.clear();
            userDataProp = null;
        }
    }
}
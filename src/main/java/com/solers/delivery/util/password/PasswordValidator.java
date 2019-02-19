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
package com.solers.delivery.util.password;

import java.util.regex.Pattern;

/**
 * Password validation functions
 * 
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public final class PasswordValidator {
     
    private static final int PASS_LENGTH = 15;    
    private static final String[] ILLEGAL_WORDS = {"master", "root", "delivery", "admin", "administrator", "efd"};
    
    private PasswordValidator() {
        
    }
    
    /**
     * Validate that {@code password} does not contain any of the strings in {@code strings}
     * 
     * @param password Password to check
     * @param strings Strings to check (may be be null)
     * @throws InvalidPasswordException If {@code password} contains any of the strings in {@code strings}
     */
    public static void validatePasswordDoesNotContain(String password, String...strings) throws InvalidPasswordException {
        if (strings != null) {
            for (String s : strings) {
                if (s.length() > 0) {
                    if (password.toUpperCase().contains(s.toUpperCase())) {
                        throw new InvalidPasswordException("Password cannot contain: " + s);
                    }
                }
            }
        }
    }
    
    /**
     * <p>Validate a system password.  A system password is used
     * to access EFD resources</p>
     * 
     * <p>System passwords must contain at least fifteen characters, at least two 
     * numerical characters, two upper case letters, two lower case letters, 
     * and two non-alphanumeric characters</p>
     * 
     * @param password Password to check
     * @param message Invalid password message
     * @throws InvalidPasswordException If {@code password} does not meet the defined criteria
     */
    public static void validateSystemPassword(String password, String message) throws InvalidPasswordException {
        validateSystemPassword(password, message, PASS_LENGTH);
    }
    
    /**
     * <p>Validate a system password.  A system password is used
     * to access EFD resources</p>
     * 
     * <p>System passwords must contain at least {@code length} characters, at least two 
     * numerical characters, two upper case letters, two lower case letters, 
     * and two non-alphanumeric characters</p>
     * 
     * @param password Password to check
     * @param message Invalid Password Message
     * @param length Minimum length of the password
     * @throws InvalidPasswordException If {@code password} does not meet the defined criteria
     */
    public static void validateSystemPassword(String password, String message, int length) throws InvalidPasswordException {
        
        validatePasswordDoesNotContain(password, ILLEGAL_WORDS);
        
        String formattedMessage = String.format(message, length);        
        
        ensureAtLeast(1, "\\p{Lower}", password, formattedMessage);
        ensureAtLeast(1, "\\p{Upper}", password, formattedMessage);
        ensureAtLeast(1, "\\p{Punct}", password, formattedMessage);
        
        if (password.length() < length) {
            throw new InvalidPasswordException(formattedMessage);
        }
    }
    
    /**
     * <p>Validate a non-system password</p>
     * 
     * <p>A non-system password is used to access resources outside of EFD like an
     * ftp server or database.</p>
     * 
     * <p>Validates that {@code password} doesn't contain
     * non-printable characters.</p>
     * 
     * @param password
     * @throws InvalidPasswordException If {@code password} and {@code confirmation} don't meet the defined criteria
     */
    public static void validateNonSystemPassword(char [] password) throws InvalidPasswordException {
        check("\\p{Print}+", String.valueOf(password), "Passwords need to be valid characters");
    }
    
    /**
     * Ensures that at least {@code number} occurrences of {@code pattern} appear in {@code input}
     * @param number
     * @param pattern
     * @param input
     * @param message
     * @throws InvalidPasswordException
     */
    private static void ensureAtLeast(int number, String pattern, String input, String message) throws InvalidPasswordException {
        StringBuilder p = new StringBuilder(".*");
        for (int i=0; i < number; i++) {
            p.append(pattern);
            p.append(".*");
        }
        if (!Pattern.compile(p.toString()).matcher(input).matches()) {
            throw new InvalidPasswordException(message);
        }
    }
    
    private static void check(String pattern, String input, String message) throws InvalidPasswordException {
        if (!Pattern.compile(pattern).matcher(input).matches()) {
            throw new InvalidPasswordException(message);
        }
    }
}

package utils;

import org.apache.commons.lang3.StringUtils;

import com.github.ddth.commons.utils.HashUtils;

import bo.user.UserBo;

/**
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public class UserUtils {
    /**
     * Encrypts a password.
     * 
     * @param salt
     * @param rawPassword
     * @return
     */
    public static String encryptPassword(String salt, String rawPassword) {
        return HashUtils.sha256(salt + "." + rawPassword);
    }

    /**
     * Encrypts a user's password.
     * 
     * @param user
     * @param rawPassword
     * @return
     */
    public static String encryptPassword(UserBo user, String rawPassword) {
        return encryptPassword(user.getId(), rawPassword);
    }

    /**
     * Authenticates user against a password.
     * 
     * @param user
     * @param inputPassword
     * @return
     */
    public static boolean authenticate(UserBo user, String inputPassword) {
        String encryptedPassword = encryptPassword(user, inputPassword);
        return StringUtils.equalsIgnoreCase(encryptedPassword, user.getPassword());
    }
}

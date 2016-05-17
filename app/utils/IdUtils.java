package utils;

import com.github.ddth.commons.utils.IdGenerator;

/**
 * ID related utility class.
 * 
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public class IdUtils {
    public final static IdGenerator ID_GEN = IdGenerator.getInstance(IdGenerator.getMacAddr());
}

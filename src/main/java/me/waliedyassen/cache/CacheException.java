package me.waliedyassen.cache;

/**
 * This will be thrown whenever any exception or a critical problem happen within the cache library.
 *
 * @author Walied K. Yassen
 */
public class CacheException extends RuntimeException {

    /**
     * Constructs a new {@link CacheException} type object instance.
     *
     * @param message the error message of the exception.
     */
    public CacheException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@link CacheException} type object instance.
     *
     * @param message the error message of the exception.
     * @param cause   the error cause of the exception.
     */
    public CacheException(String message, Throwable cause) {
        super(message, cause);
    }
}

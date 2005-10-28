package net.sourceforge.cruisecontrol.util;

import net.sourceforge.cruisecontrol.CruiseControlException;
import java.io.File;

/**
 * Reusable assertion like facility for handling configuration mistakes (e.g. unsupported/required attributes).
 *
 * @author <a href="mailto:jerome@coffeebreaks.org">Jerome Lacoste</a>
 */
public final class ValidationHelper {
    private ValidationHelper() {
    }

    /**
     * Handle required plugin attributes.
     *
     * @param attribute
     * @param attributeName
     * @param plugin
     * @throws CruiseControlException
     */
    public static void assertIsSet(final Object attribute, final String attributeName, final Class plugin)
            throws CruiseControlException {
        assertIsSet(attribute, attributeName, getShortClassName(plugin));
    }

    /**
     * Handle required plugin attributes.
     *
     * @param attribute
     * @param attributeName
     * @param pluginName
     * @throws CruiseControlException
     */
    public static void assertIsSet(final Object attribute, final String attributeName, final String pluginName)
            throws CruiseControlException {
        if (attribute == null) {
            fail("'" + attributeName + "' is required for " + pluginName);
        }
    }

    /**
     * Handle required plugin attributes.
     *
     * @param attribute
     * @param plugin
     * @throws CruiseControlException if empty (null OK)
     */
    public static void assertNotEmpty(final String attribute, final String attributeName, final Class plugin)
            throws CruiseControlException {
        assertTrue(attribute == null || !"".equals(attribute), attributeName
                + " must be meaningful or not provided on " + getShortClassName(plugin));
    }

    public static void assertTrue(boolean condition, String message) throws CruiseControlException {
        if (!condition) {
            fail(message);
        }
    }

    public static void fail(String message) throws CruiseControlException {
        throw new CruiseControlException(message);
    }

    public static void fail(String message, Exception e) throws CruiseControlException {
        throw new CruiseControlException(message, e);
    }

    public static void assertFalse(boolean condition, String message) throws CruiseControlException {
        if (condition) {
            fail(message);
        }
    }

    /**
     * The short class name of an object.
     *
     * @param plugin
     * @return The short class name
     * @throws NullPointerException if object is null
     */
    private static String getShortClassName(final Class plugin) {
        final String fullClassName = plugin.getName();
        return fullClassName.substring(fullClassName.lastIndexOf('.') + 1);
    }

    public static void assertExists(File file, String attributeName, Class plugin) throws CruiseControlException {
        if (file == null || attributeName == null || plugin == null) {
            throw new IllegalArgumentException("All parameters are required.");
        }

        if (!file.exists()) {
            fail("File specified [" + file.getAbsolutePath() + "] for attribute [" + attributeName + "] on plugin ["
                    + plugin.getName() + "] doesn't exist.");
        }
    }

    public static void assertIsNotDirectory(File file, String attributeName, Class plugin)
            throws CruiseControlException {
        if (file == null || attributeName == null || plugin == null) {
            throw new IllegalArgumentException("All parameters are required.");
        }

        if (file.isDirectory()) {
            fail("File specified [" + file.getAbsolutePath() + "] for attribute [" + attributeName + "] on plugin ["
                    + plugin.getName() + "] is really a directory where a file was expected.");
        }
    }

    public static void assertIsReadable(File file, String attributeName, Class plugin) throws CruiseControlException {
        if (file == null || attributeName == null || plugin == null) {
            throw new IllegalArgumentException("All parameters are required.");
        }

        if (!file.canRead()) {
            fail("File specified [" + file.getAbsolutePath() + "] for attribute [" + attributeName + "] on plugin ["
                    + plugin.getName() + "] is not readable.");
        }
    }
}

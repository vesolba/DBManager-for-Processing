package dbmanager;

/**
 * JDBCTableModelListener.java
 */

/**
 * The listener interface the JDBCTableModel uses to notify interested
 * parties that a row has been retrieved from the database.
 * <p>
 * This is particularly useful to implement progress indicators.
 * </p>
 * @see JDBCTableModel
 *
 * @author gm310509
 */
public interface JDBCTableModelListener { 
    /**
     * Called once for each row retrieved from the database.
     */
    public void rowRetrieved ();
}
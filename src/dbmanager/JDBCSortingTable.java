package dbmanager;


/*
 * JDBCSortingTable.java
 */

import java.awt.event.MouseEvent;
import javax.swing.*;

import java.awt.*;
import java.awt.event.MouseAdapter;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;


/**
 * A JTable with sorting capability. The JTable has sorting capability which
 * a user can activate by clicking a column header.
 * @author Glenn
 */
public class JDBCSortingTable extends JTable {


    /**
     * Creates a new JTable using the supplied tableModel.
     * <p>
     * Unlike a standard JTable, this table will sort it's data if the user
     * clicks on a column header.
     * </p>
     * @param tableModel a table model containing result set data.
     */
    public JDBCSortingTable(JDBCTableModel tableModel) {
        super (tableModel);
        getTableHeader().addMouseListener(new MouseHeaderEvent ());

        tableModel.initRenderer(this, null, new Color (255, 224, 176));

        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        adjustWidths();
    }

    
    public static final int MAX_WIDTH = 600;

    /**
     * Adjust the column widths for a good display based upon the data
     * contained in the table model.
     */
    public void adjustWidths () {
        /* strategy:
         * Scan each column. For each row in a column, determine the width
         * of the data.
         * Then look at the  column header.
         * The width of the column is the largest of all these values
         * such that the maximum (MAX_WIDTH) is not exceeded.
         */

        for (int col = 0; col < getColumnCount (); col++) {
            int maxWidth = 0;
            for (int row = 0; row < getRowCount (); row++) {
                Object value = getValueAt (row, col);
                TableCellRenderer rend = getCellRenderer (row, col);
                if (rend != null) {
                    Component comp = rend.getTableCellRendererComponent (this, value, false, false, row, col);
                    maxWidth = Math.max (maxWidth, comp.getPreferredSize ().width);
                }
                else {
                    // If there is no rednderer, use a default value.
                    maxWidth = Math.max (maxWidth, 20);
                }
            }

            TableColumn column = getColumnModel ().getColumn (col);
            TableCellRenderer headerRenderer = column.getHeaderRenderer ();
            if (headerRenderer == null) {
                headerRenderer = getTableHeader ().getDefaultRenderer ();
            }
            Object headerValue = column.getHeaderValue ();
            Component headerComp = headerRenderer.getTableCellRendererComponent (this, headerValue, false, false, 0, col);
            maxWidth = Math.max (maxWidth, headerComp.getPreferredSize ().width);
                            // Put a cap on the absolute maximum.
            maxWidth = Math.min(maxWidth, MAX_WIDTH);

            column.setPreferredWidth (maxWidth + 8);
        }
    }


    /**
     * Helper class to deal with table header clicks.
     */
    private class MouseHeaderEvent extends MouseAdapter {
        /**
         * Handle a mouse click in the table header.
         * @param e the event details.
         */
        @Override
        public void mouseClicked(MouseEvent e) {
            int column = getTableHeader().columnAtPoint(e.getPoint());
            ((JDBCTableModel) getModel()).setSortColumn(column);
        }
    }

}
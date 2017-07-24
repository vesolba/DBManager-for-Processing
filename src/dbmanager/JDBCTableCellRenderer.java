package dbmanager;

import java.awt.Color;
import java.awt.Component;

/*
 * JDBCTableCellRenderer.java
 */
import java.math.BigDecimal;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Renders a cell in the JTable
 * @author gm310509
 */
public class JDBCTableCellRenderer extends DefaultTableCellRenderer {
    
    /** Creates a new instance of JDBCTableCellRenderer */
    public JDBCTableCellRenderer () {
    }

    
    private Color nullBackground = new Color (240, 240, 240);
    private Color nullBackgroundSelected = new Color (192, 192, 255);

    /**
     * sets the colour scheme for null values
     * @param nullColour the new Null colour.
     */
    public void setNullColourScheme (Color nullColour) {
        nullBackground = nullColour;
        nullBackgroundSelected = nullColour.darker(); 
    }

    /**
     * Returns the default table cell renderer.
     * <p>
     * During a printing operation, this method will be called with
     * <code>isSelected</code> and <code>hasFocus</code> values of
     * <code>false</code> to prevent selection and focus from appearing
     * in the printed output. To do other customization based on whether
     * or not the table is being printed, check the return value from
     * {@link javax.swing.JComponent#isPaintingForPrint()}.
     * 
     * 
     * @param table  the <code>JTable</code>
     * @param value  the value to assign to the cell at
     * 			<code>[row, column]</code>
     * @param isSelected true if cell is selected
     * @param hasFocus true if cell has focus
     * @param row  the row of the cell to render
     * @param column the column of the cell to render
     * @return the default table cell renderer
     * @see javax.swing.JComponent#isPaintingForPrint()
     */
    @Override
    public Component getTableCellRendererComponent (JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component retValue;

        retValue = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        JLabel retLabel = null;
        if (retValue instanceof JLabel) {
            retLabel = (JLabel) retValue;
            retLabel.setIcon (null);
            if (value instanceof Long ||
                    value instanceof Float ||
                    value instanceof Double ||
                    value instanceof BigDecimal) {
                retLabel.setHorizontalAlignment (SwingConstants.RIGHT);
            }
            else if (value instanceof JDBCTableLob) {
                JDBCTableLob lobValue = (JDBCTableLob) value;
//                System.out.println("lobValue " + lobValue.getType ());
                if (lobValue.getType () == JDBCTableLob.LobType.BLOB) {
                    retLabel.setIcon (new javax.swing.ImageIcon(getClass().getResource("/com/teradata/util/jtable/resources/blob.png")));
                    retLabel.setText (null);
                    retLabel.setHorizontalAlignment (SwingConstants.CENTER);
                }
                else {
                    retLabel.setIcon(new javax.swing.ImageIcon (getClass().getResource ("/com/teradata/util/jtable//resources/clob.png")));
                    retLabel.setText (lobValue.getText());
                    retLabel.setHorizontalAlignment (SwingConstants.LEFT);
                }
            }
            else {
                retLabel.setHorizontalAlignment (SwingConstants.LEFT);
            }
        }
    
        if (value == null) {
            if (isSelected) {
                retValue.setBackground (nullBackgroundSelected);
                
            }
            else {
                retValue.setBackground (nullBackground);
            }
            if (nullText != null && retLabel != null) {
                retLabel.setText (nullText);
            }
        }
        else {
            if (isSelected) {
                retValue.setBackground (table.getSelectionBackground ());
            }
            else {
                retValue.setBackground (table.getBackground ());
            }
        }
        return retValue;
    }

    /**
     * Holds value of property nullText.
     */
    private String nullText = "??null??";

    /**
     * Retrieve the null text value.
     * @return the null text.
     */
    public String getNullText () {
        return this.nullText;
    }

    /**
     * Set the nullText value.
     * @param nullText the new null text value.
     */
    public void setNullText (String nullText) {
        this.nullText = nullText;
    }

}
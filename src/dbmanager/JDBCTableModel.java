package dbmanager;

/*
 * JDBCTableModel.java
 */

import java.awt.Color;
import java.io.File;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;

/**
 * A JTable table model. Contains the content of a ResultSet retrieved from a
 * database.
 * 
 * @author gm310509
 */
public class JDBCTableModel extends AbstractTableModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/** The content (resultset) retrieved from the database */
	ArrayList<Object[]> contents = new ArrayList<Object[]>();
	/**
	 * A set of pointers to rows based upon the sort sequence. This is a convenience
	 * structure to speed up the sorting by simply sorting row numbers rather than
	 * the entire contents array list.
	 */
	ArrayList<SortElement> sorted = new ArrayList<SortElement>();

	/** A list of the column names */
	String[] columnNames;
	/** The class associated with each column */
	Class[] columnClasses;
	/** Holds value of property lobPath. */
	private String lobPath = System.getProperty("java.io.tmpdir");
	/** A list of JDBCTableModelListeners. */
	private LinkedList<JDBCTableModelListener> jdbcModelListenerList = new LinkedList<JDBCTableModelListener>();
	/** Text to be displayed for null values */
	private String nullText = "??null??";
	/** Holds value of property nullColour. */
	private Color nullColour = new Color(240, 240, 240);

	/**
	 * The column number that was last used to sort the data. or -1 if the model
	 * hasn't been sorted (by this class).
	 */
	private int sortColumn = -1;

	/**
	 * Create an empty JDBCTableModel.
	 */
	public JDBCTableModel() {
		super();
	}

	/**
	 * Creates a JDBCTableModel and retrieves the supplied ResultSet from the
	 * database.
	 * 
	 * @param resultSet
	 *            the result set to retrieve.
	 *
	 * @throws java.sql.SQLException
	 */
	public JDBCTableModel(Connection conn, String query, String mode) throws SQLException {
		super();
		
		Statement statement;
		ResultSetMetaData metadata;
		ResultSet resultSet = null;
		
		if (conn != null) {

				statement = conn.createStatement();
				resultSet = statement.executeQuery(query);
		}
		getTableContents(resultSet);
	}

	/**
	 * Creates a JDBCTableModel and retrives the supplied ResultSet from the
	 * database. Row retrieval notifications are sent to the supplied listener.
	 * 
	 * @param resultSet
	 *            the result set to retrieve.
	 * @param listener
	 *            the listener to notify of rows retrieved.
	 * @throws java.sql.SQLException
	 */
	public JDBCTableModel(ResultSet resultSet, JDBCTableModelListener listener) throws SQLException {
		super();
		this.jdbcModelListenerList.add(listener);
		getTableContents(resultSet);
	}

	JDBCTableCellRenderer renderer = null;

	/**
	 * Initialise the table cell renderer.
	 * <p>
	 * The table cell renderer draws the individual cells within the JTable.
	 * </p>
	 *
	 * @param table
	 *            the JTable to which the renderer applies
	 * @param nullString
	 *            a string to be displayed in cells containing a null value.
	 * @return the table renderer
	 */
	public JDBCTableCellRenderer initRenderer(JTable table, String nullString) {
		return initRenderer(table, nullString, nullColour);
	}

	/**
	 * Initialise the table cell renderer.
	 * <p>
	 * The table cell renderer renders the cells in the JTable.
	 * </p>
	 * 
	 * @param table
	 *            the table to which the render applies
	 * @param nullString
	 *            the string to be displayed for null values.
	 * @param nullColour
	 *            the background colour to display in cells containing the nulls
	 * @return the table cell renderer.
	 */
	public JDBCTableCellRenderer initRenderer(JTable table, String nullString, Color nullColour) {
		renderer = new JDBCTableCellRenderer();
		renderer.setNullText(nullString);
		renderer.setNullColourScheme(nullColour);

		table.setDefaultRenderer(Object.class, renderer);
		table.setDefaultRenderer(String.class, renderer);
		table.setDefaultRenderer(Long.class, renderer);
		table.setDefaultRenderer(Float.class, renderer);
		table.setDefaultRenderer(Double.class, renderer);
		table.setDefaultRenderer(java.math.BigDecimal.class, renderer);
		table.setDefaultRenderer(java.sql.Date.class, renderer);
		table.setDefaultRenderer(java.sql.Time.class, renderer);
		table.setDefaultRenderer(java.sql.Timestamp.class, renderer);
		table.setDefaultRenderer(java.sql.Clob.class, renderer);
		table.setDefaultRenderer(java.sql.Blob.class, renderer);
		return renderer;

	}

	/**
	 * Set the table meta data
	 *
	 * @param resultSet
	 *            the result set from which to extract the meta data.
	 *
	 * @throws java.sql.SQLException
	 */
	public void setMetaData(ResultSet resultSet) throws SQLException {
		ResultSetMetaData meta = resultSet.getMetaData();

		ArrayList<String> colNamesList = new ArrayList<String>();
		ArrayList<Object> colClassesList = new ArrayList<Object>();
		for (int i = 1; i <= meta.getColumnCount(); i++) {
			colNamesList.add(meta.getColumnName(i));
			// System.out.println("name: " + meta.getColumnName (i));
			int dbType = meta.getColumnType(i);
			switch (dbType) {
			case Types.SMALLINT:
			case Types.INTEGER:
			case Types.BIGINT:
				colClassesList.add(Long.class);
				break;
			case Types.FLOAT:
				colClassesList.add(Float.class);
				break;
			case Types.DOUBLE:
			case Types.REAL:
				colClassesList.add(Double.class);
				break;
			case Types.DECIMAL:
			case Types.NUMERIC:
				colClassesList.add(BigDecimal.class);
				break;
			case Types.DATE:
				colClassesList.add(java.sql.Date.class);
				break;
			case Types.TIME:
				colClassesList.add(java.sql.Time.class);
				break;
			case Types.TIMESTAMP:
				colClassesList.add(java.sql.Timestamp.class);
				break;
			case Types.CLOB:
				colClassesList.add(java.sql.Clob.class);
				break;
			case Types.BLOB:
				colClassesList.add(java.sql.Blob.class);
				break;
			case Types.BINARY:
			case Types.VARBINARY:
				colClassesList.add(Byte.class);
				break;
			case Types.CHAR:
			case Types.VARCHAR:
				colClassesList.add(String.class);
				break;
			default:
				System.out.println("col: " + i + " type = " + meta.getColumnTypeName(i) + " (" + dbType + ")");
				colClassesList.add(String.class);
				break;
			}
			// System.out.println ("Type: " + meta.getColumnType (i) + ": " +
			// meta.getColumnTypeName (i));;
		}

		columnNames = new String[colNamesList.size()];
		colNamesList.toArray(columnNames);
		columnClasses = new Class[colClassesList.size()];
		colClassesList.toArray(columnClasses);
	}

	/** Counts the row number. Used for generating the initial sorted sequence. */
	private int rowNum = 0;

	/**
	 * Retrieve a single row from the result set.
	 * 
	 * @param resultSet
	 *            the result set to retrieve the row from.
	 * @throws java.sql.SQLException
	 */
	public void getRow(ResultSet resultSet) throws SQLException {

		ArrayList<Object> cellList = new ArrayList<Object>();
		for (int i = 0; i < columnClasses.length; i++) {
			Object cellValue = null;
			if (columnClasses[i] == String.class) {
				cellValue = resultSet.getString(i + 1);
			} else if (columnClasses[i] == Long.class) {
				cellValue = new Long(resultSet.getLong(i + 1));
			} else if (columnClasses[i] == Float.class) {
				cellValue = new Float(resultSet.getFloat(i + 1));
			} else if (columnClasses[i] == Double.class) {
				cellValue = new Double(resultSet.getDouble(i + 1));
			} else if (columnClasses[i] == BigDecimal.class) {
				cellValue = resultSet.getBigDecimal(i + 1);
			} else if (columnClasses[i] == java.sql.Date.class) {
				cellValue = resultSet.getDate(i + 1);
			} else if (columnClasses[i] == java.sql.Time.class) {
				cellValue = resultSet.getTime(i + 1);
			} else if (columnClasses[i] == java.sql.Timestamp.class) {
				cellValue = resultSet.getTimestamp(i + 1);
			}
			// Clobs and Blobs should be written to temporary storage.
			else if (columnClasses[i] == Clob.class || columnClasses[i] == Blob.class) {

				try {
					if (columnClasses[i] == Clob.class) {
						Clob clobValue = resultSet.getClob(i + 1);
						if (clobValue != null) {
							JDBCTableLob lobValue = new JDBCTableLob(clobValue, lobPath);
							cellValue = lobValue;
						}
					} else {
						Blob blobValue = resultSet.getBlob(i + 1);
						if (blobValue != null) {
							JDBCTableLob lobValue = new JDBCTableLob(blobValue, lobPath);
							cellValue = lobValue;
						}
					}
				} catch (java.io.IOException e) {
					e.printStackTrace();
					cellValue = null;
				} catch (SQLException e) {
					e.printStackTrace();
					cellValue = null;
				}
			} else if (columnClasses[i] == Byte.class) {
				cellValue = resultSet.getObject(i + 1);
				if (cellValue instanceof byte[]) {
					byte[] bytes = (byte[]) cellValue;
					StringBuffer sb = new StringBuffer();
					for (int j = 0; j < bytes.length; j++) {
						sb.append(toHexString(bytes[j]));
					}
					cellValue = sb.toString();
				}
			} else {
				cellValue = resultSet.getObject(i + 1);
			}

			if (resultSet.wasNull()) {
				cellValue = null;
			}

			cellList.add(cellValue);
		}
		Object[] cells = cellList.toArray();
		contents.add(cells);
		sorted.add(new SortElement(null, rowNum++));
	}

	private static final String hexDigitsUpper = "0123456789ABCDEF";

	/**
	 * Converts a byte into a hexadecimal string.
	 * 
	 * @param b
	 *            the byte to convert
	 * @return the byte as a hexadecimal strinq.
	 */
	private String toHexString(byte b) {
		String digits = hexDigitsUpper;
		int high = (b >>> 4) & 0x0f; // Unsigned shift right 4 bits + bitwise and.
		int low = b & 0x0f; // bitwise and.

		String result = digits.substring(high, high + 1) + digits.substring(low, low + 1);
		return result.toLowerCase();
	}

	/**
	 * Retrieves an entire result set from the database.
	 * 
	 * @param resultSet
	 *            the resultset to which data should be retrieved.
	 * @throws java.sql.SQLException
	 */
	protected void getTableContents(ResultSet resultSet) throws SQLException {

		setMetaData(resultSet);

		while (resultSet.next()) {
			getRow(resultSet);

			for (JDBCTableModelListener listener : jdbcModelListenerList) {
				listener.rowRetrieved();
			}
		}

	}

	/**
	 * Retrieve the row count.
	 * 
	 * @return the row count
	 */
	@Override
	public int getRowCount() {
		return contents.size();
	}

	/**
	 * Retrieve the number of columns in the result set.
	 * 
	 * @return the number of columns.
	 */
	@Override
	public int getColumnCount() {
		if (contents.size() == 0) {
			return 0;
		} else {
			return contents.get(0).length;
		}
	}

	/**
	 * Retrieves the value at the specified row and column.
	 *
	 * <p>
	 * Note that the row and column are JTable row and column numbers, not result
	 * set numbers. That is, they start at 0, not 1.
	 * </p>
	 *
	 * @param row
	 *            the row number
	 * @param column
	 *            the column number.
	 * @return the value at that cell.
	 */
	@Override
	public Object getValueAt(int row, int column) {
		row = sorted.get(row).getRowNum();
		return contents.get(row)[column];
	}

	/**
	 * Retrieve the class for the specified column.
	 *
	 * <p>
	 * Note that the row and column are JTable row and column numbers, not result
	 * set numbers. That is, they start at 0, not 1.
	 * </p>
	 *
	 * @param col
	 *            the column to retrieve.
	 * @return the class.
	 */
	@Override
	public Class getColumnClass(int col) {
		return columnClasses[col];
	}

	/**
	 * Retrieve the name of the specified column.\
	 * <p>
	 * Note that the row and column are JTable row and column numbers, not result
	 * set numbers. That is, they start at 0, not 1.
	 * </p>
	 *
	 * @param col
	 *            the column number
	 * @return the column name.
	 */
	@Override
	public String getColumnName(int col) {
		return columnNames[col];
	}

	/**
	 * Retrieves the list of registered listeners.
	 * 
	 * @return the list of listeners.
	 */
	public LinkedList<JDBCTableModelListener> getListenerList() {
		return this.jdbcModelListenerList;
	}

	/**
	 * Sets the listener list.
	 * 
	 * @param jdbcModelListenerList
	 *            the new list of listeners.
	 */
	public void setListenerList(LinkedList<JDBCTableModelListener> listenerList) {
		this.jdbcModelListenerList = listenerList;
	}

	/**
	 * Add a listener to the list of listeners.
	 * 
	 * @param listener
	 *            the new listener.
	 */
	public void addListener(JDBCTableModelListener listener) {
		this.jdbcModelListenerList.add(listener);
	}

	/**
	 * Remove a listener from the registered listener list
	 * 
	 * @param listener
	 *            the listener to be removed.
	 * @return the listener removed or null if the listener wasn't found.
	 */
	public boolean removeListener(JDBCTableModelListener listener) {
		return this.jdbcModelListenerList.remove(listener);
	}

	/**
	 * Retrieve the null text value.
	 * 
	 * @return the nullText.
	 */
	public String getNullText() {
		return this.nullText;
	}

	/**
	 * Sets the null text.
	 * 
	 * @param nullText
	 *            the new null text value.
	 */
	public void setNullText(String nullText) {
		this.nullText = nullText;
	}

	/**
	 * Retrieve the null colour.
	 * 
	 * @return the null colour.
	 */
	public java.awt.Color getNullColour() {
		return nullColour;
	}

	/**
	 * Sets the null colour.
	 * 
	 * @param nullColour
	 *            the new null colour.
	 */
	public void setNullColour(java.awt.Color nullColour) {
		this.nullColour = nullColour;
		if (renderer != null) {
			renderer.setNullColourScheme(nullColour);
		}
	}

	/**
	 * Cleanup any LOB files that have been created as a result of the query.
	 * <p>
	 * Note: Failure to call this method when the table is finished with will result
	 * in LOB's being left in place when the table model is destroyed.
	 * </p>
	 */
	public void cleanupLob() {
		for (int col = 0; col < getColumnCount(); col++) {
			if (getColumnClass(col) == java.sql.Clob.class || getColumnClass(col) == java.sql.Blob.class) {
				int rowCount = getRowCount();
				for (int row = 0; row < rowCount; row++) {
					JDBCTableLob lob = (JDBCTableLob) getValueAt(row, col);
					if (lob != null) {
						lob.cleanup();
					}
				}
			}
		}
	}

	/**
	 * Retrieve the LOB path.
	 * <p>
	 * Any large objects retrieved from the database are stored in this location.
	 * </p>
	 *
	 * @return the lob path.
	 */
	public String getLobPath() {
		return this.lobPath;
	}

	/**
	 * Sets the lobPath.
	 * <p>
	 * Any large objects retrieved from the database are stored in this location.
	 * </p>
	 * <p>
	 * If the directory doesn't exist, it is created. If a problem occurs creating
	 * the directoy, the path will be set to null resulting in the system temporary
	 * directory being used.
	 * </p>
	 * 
	 * @param lobPath
	 *            New value of property lobPath.
	 */
	public void setLobPath(String lobPath) {
		this.lobPath = lobPath;
		if (lobPath == null || "".equals(lobPath)) {
			return;
		}
		File lobFile = new File(lobPath);
		if (lobFile.isDirectory()) {
			return;
		}
		if (lobFile.mkdirs()) {
			return;
		}
		lobFile = null; // This will cause the default system directory to be used.
	}

	/**
	 * Retrieve the current sort column number.
	 * <p>
	 * If the model hasn't been sorted, this method returns -1
	 * </p>
	 * 
	 * @return the current sort column number or -1
	 */
	public int getSortColumn() {
		return sortColumn;
	}

	/**
	 * Set the sort column number.
	 * <p>
	 * Upon setting this, the data will be resorted.
	 * </p>
	 * 
	 * @param sortColumn
	 */
	public void setSortColumn(int sortColumn) {
		this.sortColumn = sortColumn;
		resortData();
	}

	/**
	 * Sorts the data based upon the column selected.
	 */
	public void resortData() {

		/*
		 * Determine whether or not the data type of the column is suitable for sorting.
		 * A suitable column is one that implements the Comparable interface. Most of
		 * the Java types implement the comparable interface so most columns can be
		 * sorted. Note that this attempt to cast must be used (as opposed to simply
		 * determining if the content of the table is an instance of Comparable). This
		 * is because the content of the table could contain null values. Null values
		 * don't implement anything - so instanceof won't always work.
		 */
		try {
			/*
			 * Attempt to cast the sort column class to a Comparable class. If this works,
			 * we are good to sort. If an exception is thrown, display a
			 * "sorry, we can't sort on this column" message.
			 */
			Class testClass = getColumnClass(sortColumn).asSubclass(Comparable.class);
		} catch (ClassCastException e) {
			JOptionPane.showMessageDialog(null,
					"The selected column is not able to be used to sort the data.\nPlease choose a different column.",
					"Invalid sort column", JOptionPane.ERROR_MESSAGE);
			return;
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null,
					"The selected column is not able to be used to sort the data.\nPlease choose a different column.\n"
							+ e.getMessage(),
					"Invalid sort column", JOptionPane.ERROR_MESSAGE);
			return;
		}

		sorted = new ArrayList<SortElement>();
		/* Build up a list of the elements to be sorted */
		int rowCount = getRowCount();
		for (int i = 0; i < rowCount; i++) {
			sorted.add(new SortElement((Comparable) contents.get(i)[sortColumn], i));
		}

		Collections.sort(sorted);

		TableModelEvent e = new TableModelEvent(this);
		fireTableChanged(e);
	}

	/**
	 * A private class to support sorting. The class contains the key value and the
	 * index into the array list where this row can be found.
	 */
	private class SortElement implements Comparable {
		private int rowNum;
		private Comparable key;

		/**
		 * Creates the sort element with the supplied key and rowNumber.
		 * 
		 * @param key
		 *            the sort key value.
		 * @param rowNum
		 *            the row Number.
		 */
		public SortElement(Comparable key, int rowNum) {
			this.key = key;
			this.rowNum = rowNum;
		}

		/**
		 * Retrieve the sort key value.
		 * 
		 * @return the sort key value.
		 */
		public Comparable getKey() {
			return key;
		}

		/**
		 * Set the sort key value.
		 * 
		 * @param key
		 *            the new sort key value.
		 */
		public void setKey(Comparable key) {
			this.key = key;
		}

		/**
		 * Retrieves the row number in the contents array for this sort key value.
		 * 
		 * @return the row number.
		 */
		public int getRowNum() {
			return rowNum;
		}

		/**
		 * Set the row number in the content array for this sort key value.
		 * 
		 * @param rowNum
		 *            the new row number.
		 */
		public void setRowNum(int rowNum) {
			this.rowNum = rowNum;
		}

		/**
		 * Compare this SortElement to another.
		 * 
		 * @param o
		 *            the other SortElement.
		 * @return -1, 0, 1 depending on whether this SortKey comes before or after the
		 *         other SortKey
		 */
		public int compareTo(Object o) {

			if (key == null && o == null) {
				System.out.println("key = " + key + ", other = " + o + ", result = 0");
				return 0;
			} else if (key == null) {
				System.out.println("key = " + key + ", other = " + o + ", result = -1");
				return -1;
			} else if (o == null) {
				System.out.println("key = " + key + ", other = " + o + ", result = 1");
				return 1;
			}

			if (o instanceof SortElement) {
				SortElement other = (SortElement) o;
				if (other.getKey() == null) {
					return 1;
				}
				int result = key.compareTo(other.getKey());

				return result;
			} else {
				return -1;
			}
		}
	}
}
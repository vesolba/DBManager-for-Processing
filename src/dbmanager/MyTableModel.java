package dbmanager;

import java.awt.Color;
import java.io.IOException;
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
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.table.AbstractTableModel;

@SuppressWarnings("serial")
public class MyTableModel extends AbstractTableModel {

	/** The content (resultset) retrieved from the database */
	// ArrayList<Object[]> contents = new ArrayList<Object[]>();

	/** Holds value of property lobPath. */
	private String lobPath = System.getProperty("java.io.tmpdir");
	/** Text to be displayed for null values */
	private String nullText = "??null??";
	/** Holds value of property nullColour. */
	private Color nullColour = new Color(240, 240, 240);

	ArrayList<Boolean> colsAutoincrement = new ArrayList<>();
	ArrayList<Boolean> colsSearchable = new ArrayList<>();
	ArrayList<String> colsNames = new ArrayList<>(); // Column names
	ArrayList<Integer> colsSizes = new ArrayList<>();
	ArrayList<ArrayList<Object>> data = new ArrayList<>();
	ArrayList<ArrayList<Object>> data2Add = new ArrayList<>();
	ArrayList<String> colsTypes = new ArrayList<>();
	ArrayList<Class> colsClasses = new ArrayList<>();

	Statement statement;
	ResultSetMetaData metadata;
	ResultSet resultSet;

	Connection conn = null;
	String query = "";
	String mode = "MODE_FILL";
	String sourceTable = "";

	public MyTableModel(Connection conn, String query, String mode) {
		super();

		this.conn = conn;
		this.query = query;
		this.mode = mode;

		if (conn != null) {

			try {
				statement = conn.createStatement();
				resultSet = statement.executeQuery(query);

				// Metadata several values.
				sourceTable = resultSet.getMetaData().getTableName(1);
				int c = resultSet.getMetaData().getColumnCount();
				for (int i = 1; i <= c; i++) {
					colsNames.add(resultSet.getMetaData().getColumnName(i));
					colsSizes.add(resultSet.getMetaData().getPrecision(i));
					colsAutoincrement.add(resultSet.getMetaData().isAutoIncrement(i));
					colsSearchable.add(resultSet.getMetaData().isSearchable(i));
					String columnTypeName = resultSet.getMetaData().getColumnTypeName(i);
					colsTypes.add(columnTypeName);
					int columnType = resultSet.getMetaData().getColumnType(i);
					colsClasses.add(getColClass(columnType, columnTypeName));
				}

				if (mode.equals("MODE_FILL") && resultSet.next()) {

					do {
						ArrayList<Object> row = new ArrayList<>();
						for (int i = 0; i < c; i++) {
							Object redObj = null;
							if (colsClasses.get(i) == Clob.class) {

								Clob clobValue = resultSet.getClob(i + 1);

								if (clobValue != null) {
									JDBCTableLob lobValue = new JDBCTableLob(clobValue, lobPath);
									redObj = lobValue;
								}

							} else {
								if (colsClasses.get(i) == Blob.class) {
									Blob blobValue = resultSet.getBlob(i + 1);
									if (blobValue != null) {
										JDBCTableLob lobValue = new JDBCTableLob(blobValue, lobPath);
										redObj = lobValue;
									}
								} else {
									if (colsClasses.get(i) == Byte.class) {
										redObj = resultSet.getObject(i + 1);
										if (redObj instanceof byte[]) {
											byte[] bytes = (byte[]) redObj;
											StringBuffer sb = new StringBuffer();
											for (int j = 0; j < bytes.length; j++) {
												sb.append(toHexString(bytes[j]));
											}
											redObj = sb.toString();
										}
									} else {
										if (colsClasses.get(i) == java.sql.SQLXML.class) {
											java.sql.SQLXML sqlXMLValue = resultSet.getSQLXML(i + 1);
											redObj = sqlXMLValue.toString();
										} else {
											redObj = resultSet.getObject(i + 1);

										}
									}
								}
							}
							row.add(redObj);

						}
						data.add(row);

					} while (resultSet.next());

				} // (mode.equals("MODE_FILL") && resultSet.next())

				// else { //There is not data or we doesn't want it.
				// ArrayList<Object> row = new ArrayList<>();
				// for (int i = 0; i < c; i++) {
				//
				// if (colsClasses.get(i) == Clob.class) {
				// Clob defValue = conn.createClob();
				// defValue.setString(1, "");
				// row.add(defValue);
				// } else
				//
				// if (colsClasses.get(i) == Blob.class) {
				// Blob defValue = conn.createBlob();
				// byte[] zeroByte = { 00 };
				// defValue.setBytes(1, zeroByte);
				// row.add(defValue);
				// } else
				//
				// if (colsClasses.get(i) == Byte.class) {
				// byte[] defValue = { 00 };
				// row.add(defValue);
				// } else
				//
				// if (colsClasses.get(i) == java.sql.Date.class || colsClasses.get(i) ==
				// java.sql.Time.class
				// || colsClasses.get(i) == java.sql.Timestamp.class || colsClasses.get(i) ==
				// String.class
				// || colsClasses.get(i) == java.sql.SQLXML.class) {
				// Object defValue = "";
				// row.add(defValue);
				// } else {
				// Object defValue = 0;
				// row.add(defValue);
				// }
				// }
				// data.add(row);
				// }
			} catch (SQLException | IOException ex) {
				System.out.println("Could not connect to database");
				ex.printStackTrace();
			} finally {
				try {

					if (statement != null) {
						statement.close();
					}
				} catch (SQLException ex) {
					Logger.getLogger(MyTableModel.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		}

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

	@Override
	public int getRowCount() {
		if (mode.equals("MODE_NEW_ROW")) {
			return data2Add.size();
		} else {
			return data.size();
		}
	}

	@Override
	public int getColumnCount() {
		return colsNames.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {

		if (rowIndex >= getRowCount() || columnIndex >= getColumnCount())
			return null;

		ArrayList<Object> row;

		if (mode.equals("MODE_NEW_ROW")) {
			row = data2Add.get(rowIndex);
		} else {
			row = data.get(rowIndex);
		}

		Object value = row.get(columnIndex);

		// if (colsClasses.get(columnIndex) == Clob.class) {
		// try {
		// return ((Clob) value).getSubString(1, (int) ((Clob) value).length());
		// } catch (SQLException e) {
		// e.printStackTrace();
		// }
		// } else if (colsClasses.get(columnIndex) == Blob.class) {
		// try {
		// return ((Blob) value).getBytes(1, (int) ((Blob) value).length());
		// } catch (SQLException e) {
		// e.printStackTrace();
		// }
		// } else
		return value;
	}

	@Override
	public String getColumnName(int column) {
		return colsNames.get(column);
	}

	public int getColumnSize(int column) {
		return colsSizes.get(column);

	}

	@Override
	public boolean isCellEditable(int row, int col) {

		return true;
	}

	@Override
	public void setValueAt(Object value, int rowIndex, int columnIndex) {

		ArrayList<Object> row;

		if (mode.equals("MODE_NEW_ROW")) {
			row = data2Add.get(rowIndex);
		} else {
			row = data.get(rowIndex);
		}

		row.set(columnIndex, value);

	}

	public ArrayList<Object> addEmptyRow(Connection conn) {

		int numCols = getColumnCount();

		ArrayList<Object> row = new ArrayList<>();

		try {

			for (int i = 0; i < numCols; i++) {

				System.out.println(i + " class " + colsClasses.get(i));

				if (colsClasses.get(i) == Clob.class) {
					Clob defValue;
					defValue = conn.createClob();

					defValue.setString(1, "");
					row.add(defValue);
				} else

				if (colsClasses.get(i) == Blob.class) {
					Blob defValue = conn.createBlob();
					byte[] zeroByte = { 00 };
					defValue.setBytes(1, zeroByte);
					row.add(defValue);
				} else

				if (colsClasses.get(i) == Byte.class) {
					byte[] defValue = { 00 };
					row.add(defValue);
				} else

				if (colsClasses.get(i) == java.sql.Date.class || colsClasses.get(i) == java.sql.Time.class
						|| colsClasses.get(i) == java.sql.Timestamp.class || colsClasses.get(i) == String.class
						|| colsClasses.get(i) == java.sql.SQLXML.class) {
					Object defValue = "";
					row.add(defValue);
				} else {
					Object defValue = 0;
					row.add(defValue);
				}
			}

			System.out.println("addrow OK");
			return row;

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;

	}

	public boolean deleteRow(int numRow) {
		try {
			System.out.println("Intro deleteRow");
			resultSet.absolute(numRow);
			resultSet.deleteRow();
			resultSet.beforeFirst();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		return true;

	}

	@Override
	public Class<? extends Object> getColumnClass(int column) {
		Class classToReturn;

		if ((column >= 0) && column < getColumnCount() && getRowCount() > 0) {
			classToReturn = getValueAt(0, column).getClass();
		} else {
			classToReturn = Object.class;
		}
		return classToReturn;
	}

	/**
	 * @return the colsSearchable
	 */
	public ArrayList<Boolean> getColsSearchable() {
		return colsSearchable;
	}

	/**
	 * Obtains the column class statically.
	 *
	 * @param columnType,
	 *            SQL type.
	 *
	 * @param columnTypeName,
	 *            Name of the column type.
	 */
	public Class<?> getColClass(int columnType, String columnTypeName) {

		switch (columnType) {
		case Types.SMALLINT:
		case Types.INTEGER:
			return Integer.class;

		case Types.BIGINT:
			return Long.class;

		case Types.FLOAT:
		case Types.DOUBLE:
			return Double.class;

		case Types.REAL:
			return Double.class;

		case Types.DECIMAL:
		case Types.NUMERIC:
			return BigDecimal.class;

		case Types.DATE:
			return java.sql.Date.class;

		case Types.TIME:
			return java.sql.Time.class;

		case Types.TIMESTAMP:
			return java.sql.Timestamp.class;

		case Types.CLOB:
			return java.sql.Clob.class;

		case Types.BLOB:
			return java.sql.Blob.class;

		case Types.BINARY:
		case Types.VARBINARY:
			return Byte.class;

		case Types.CHAR:
		case Types.VARCHAR:
			return String.class;

		case Types.SQLXML:
			return java.sql.SQLXML.class;

		default:
			System.out.println("Error in columnType: " + columnType + " " + columnTypeName);
			return String.class;
		}
	}

	public void insertNewData() {

		int numRows = data2Add.size();
		int numCols = getColumnCount();
		String tableName = sourceTable;
		int numRowsAdded = 0;
		String quotePrefix = "";
		String quoteSuffix = "";

		for (int i = 0; i < numRows; i++) {
			String order = "INSERT INTO " + tableName + "(";
			String order2 = " VALUES (";
			
			ArrayList<Object> row = data2Add.get(i);
			boolean colsAdded = false;

			for (int j = 0; j < numCols; j++) {
				if (!colsAutoincrement.get(j)) {

					if (!colsAdded) {
						colsAdded = true;
					} else {
						order += ", ";
						order2 += ", ";
					}
					order += colsNames.get(j);
					
					
					quotePrefix = (String) DBManager.dataTypeInfo(colsTypes.get(j), "LITERAL_PREFIX");
					quoteSuffix = (String) DBManager.dataTypeInfo(colsTypes.get(j), "LITERAL_SUFFIX");
				
					if (quotePrefix != null) {
						order2 += quotePrefix;
					}
					order2 += row.get(j);
					if (quoteSuffix != null) {
						order2 += quoteSuffix;
					}
				}
			}

			if (colsAdded) {
				order += ")" + order2 + ")";

				System.out.println(order);
				
				try {					
					statement = conn.createStatement();
					int result = statement.executeUpdate(order);
					numRowsAdded++;
					System.out.println("Rows added = " + numRowsAdded);					
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
			}
		}
	}
}

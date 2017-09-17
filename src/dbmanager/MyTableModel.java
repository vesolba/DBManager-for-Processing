package dbmanager;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Writer;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
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
	ArrayList<String> typeNames = new ArrayList<>();
	ArrayList<Class> colsClasses = new ArrayList<>();

	Statement statement;
	ResultSetMetaData metadata;
	ResultSet resultSet;

	Connection conn = null;
	String query = "";
	String mode = "MODE_FILL";
	String sourceTable = "";

	public MyTableModel(Connection conn, String query, String tableToUse, String mode) {
		super();

		sourceTable = tableToUse;
		this.conn = conn;
		this.query = query;
		this.mode = mode;

		if (conn != null) {
			String typeName = "";
			int columnType = 0;

			try {
				if (query.contains("*")) {
					DatabaseMetaData dmd = conn.getMetaData();
					ResultSet columns = dmd.getColumns(null, null, tableToUse, null);
					String colName = "";
					String columnsToUse = "";
					String finalQuery = "";

					while (columns.next()) {
						colName = columns.getString("COLUMN_NAME");
						typeName = columns.getString("TYPE_NAME");
//						columnType = columns.getInt("COLUMN_TYPE");
						colsNames.add(colName);
						typeNames.add(typeName);
						System.out.println(colName + " " + typeName);
//						ResultSet procColumns = dmd.getProcedureColumns(null, null, null, colName);
//						colsSizes.add(procColumns.getInt("PRECISION"));
//						colsAutoincrement.add(columns.getBoolean("AUTO_INCREMENT"));
//						colsSearchable.add(columns.getBoolean("SEARCHABLE"));
//						colsClasses.add(DBManager.getColClass(columnType, typeName));
					}

					for (int i = 0; i < colsNames.size(); i++) {

						if (typeNames.get(i).equals("XML")) {
							columnsToUse += " xmlserialize (" + colsNames.get(i) + " as clob) ";
						} else {
							columnsToUse += colsNames.get(i);
						}
						columnsToUse += (i < colsNames.size() - 1) ? ", " : "";
					}

					int asterixPosition = query.indexOf("*");
					finalQuery = query.substring(0, asterixPosition) + columnsToUse
							+ query.substring(asterixPosition + 1);

					System.out.println(finalQuery);

					query = finalQuery;
				}

				statement = conn.createStatement();

				resultSet = statement.executeQuery(query);

				System.out.println(" resultset ");
				
				// Metadata for several values.
				int c = resultSet.getMetaData().getColumnCount();
				for (int i = 1; i <= c; i++) {
//					colsNames.add(resultSet.getMetaData().getColumnName(i));
					colsSizes.add(resultSet.getMetaData().getPrecision(i));
					colsAutoincrement.add(resultSet.getMetaData().isAutoIncrement(i));
					colsSearchable.add(resultSet.getMetaData().isSearchable(i));
//					String columnTypeName = resultSet.getMetaData().getColumnTypeName(i);
//					typeNames.add(columnTypeName);
					columnType = resultSet.getMetaData().getColumnType(i);
					colsClasses.add(DBManager.getColClass(columnType, typeName));
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

		if (colsClasses.get(columnIndex) == Clob.class) {
			value = ((JDBCTableLob) value).getText();
		} else if (colsClasses.get(columnIndex) == Blob.class) {
			try {
				return ((Blob) value).getBytes(1, (int) ((Blob) value).length());
			} catch (SQLException e) {
				e.printStackTrace();
			}
			if (colsClasses.get(columnIndex) == java.sql.SQLXML.class) {
				try {
					value = ((java.sql.SQLXML) value).getString();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

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
			}
		} // else
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

			return row;

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;

	}

	public boolean deleteRow(int numRow) {
		try {
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
	public Class<?> getColumnClass(int column) {
		return colsClasses.get(column);
	}

	/**
	 * @return the colsSearchable
	 */
	public ArrayList<Boolean> getColsSearchable() {
		return colsSearchable;
	}

	// public void insertNewData() {
	//
	// int numRows = data2Add.size();
	// int numCols = getColumnCount();
	// String tableName = sourceTable;
	// int numRowsAdded = 0;
	// String quotePrefix = "";
	// String quoteSuffix = "";
	//
	// for (int i = 0; i < numRows; i++) {
	// String order = "INSERT INTO " + tableName + "(";
	// String order2 = " VALUES (";
	//
	// ArrayList<Object> row = data2Add.get(i);
	//
	// for (int j = 0; j < numCols; j++) {
	// if (!colsAutoincrement.get(j)) {
	//
	// order += colsNames.get(j) + ((j < numCols - 1) ? "," : "");
	//
	// quotePrefix = (String) DBManager.dataTypeInfo(typeNames.get(j),
	// "LITERAL_PREFIX");
	// quoteSuffix = (String) DBManager.dataTypeInfo(typeNames.get(j),
	// "LITERAL_SUFFIX");
	//
	// if (quotePrefix != null)
	// order2 += quotePrefix;
	// order2 += row.get(j);
	// if (quoteSuffix != null)
	// order2 += quoteSuffix;
	// order2 += "?" + ((j < numCols - 1) ? "," : "");
	// }
	// }
	//
	// order += ")" + order2 + ")";
	//
	// try {
	// statement = conn.createStatement();
	// int result = statement.executeUpdate(order);
	// numRowsAdded++;
	// } catch (SQLException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }
	// }

	public void insertNewRow(ArrayList<Object> row2Add) {

		int numCols = getColumnCount();
		String tableName = sourceTable;
		String quotePrefix = "";
		String quoteSuffix = "";

		String order = "INSERT INTO " + tableName + " (";
		String order2 = " VALUES (";

		for (int j = 0; j < numCols; j++) {
			if (!colsAutoincrement.get(j)) {

				order += colsNames.get(j) + ((j < numCols - 1) ? "," : "");

				quotePrefix = (String) DBManager.dataTypeInfo(typeNames.get(j), "LITERAL_PREFIX");
				quoteSuffix = (String) DBManager.dataTypeInfo(typeNames.get(j), "LITERAL_SUFFIX");

				// if (quotePrefix != null) {
				// order2 += quotePrefix;
				// }

				order2 += "?";

				// if (quoteSuffix != null) {
				// order2 += quoteSuffix;
				// }

				order2 += ((j < numCols - 1) ? "," : "");
			}
		}

		order += ")";
		order2 += ")";

		System.out.println(order + order2);

		PreparedStatement ps = null;
		SQLXML rssData = null;

		try {
			ps = conn.prepareStatement(order + order2);

		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		for (int j = 0; j < numCols; j++) {
			if (!colsAutoincrement.get(j)) {
				if (colsClasses.get(j) == java.sql.Clob.class) {
					String file2Load = row2Add.get(j).toString();
					try {
						// conn.setAutoCommit(false);
						Clob myClob = conn.createClob();
						Writer clobWriter = myClob.setCharacterStream(1);
						String str = readFile(file2Load, clobWriter);
						System.out.println("Wrote the following: " + clobWriter.toString());
						System.out.println("Length of Clob: " + myClob.length());
						ps.setClob(j + 1, myClob);

					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					if (colsClasses.get(j) == java.sql.Blob.class) {
						try {
							// conn.setAutoCommit(false);
							File file2Load = new File(row2Add.get(j).toString());
							Blob blob = conn.createBlob();
							ObjectOutputStream oos;
							oos = new ObjectOutputStream(blob.setBinaryStream(1));
							oos.writeObject(file2Load);
							ps.setBlob(j + 1, blob);
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else {
						if (colsClasses.get(j) == java.sql.SQLXML.class) {
							try {
								// conn.setAutoCommit(false);
								File file2Load = new File(row2Add.get(j).toString());
								rssData = conn.createSQLXML();
								ps.setSQLXML(j + 1, rssData);

							} catch (SQLException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						} else {

							try {
								ps.setObject(j + 1, row2Add.get(j));
							} catch (SQLException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}
			}
		}

		try {
			ps.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String readFile(String fileName, Writer writerArg) throws FileNotFoundException, IOException {

		BufferedReader br = new BufferedReader(new FileReader(fileName));
		String nextLine = "";
		StringBuffer sb = new StringBuffer();
		while ((nextLine = br.readLine()) != null) {
			System.out.println("Writing: " + nextLine);
			writerArg.write(nextLine);
			sb.append(nextLine);
		}
		// Convert the content into to a string
		String clobData = sb.toString();

		// Return the data.
		return clobData;
	}

}

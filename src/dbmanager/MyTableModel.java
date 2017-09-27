package dbmanager;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.StringReader;
import java.io.Writer;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.table.AbstractTableModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class MyTableModel extends AbstractTableModel {

	/** The content (resultset) retrieved from the database */
	// ArrayList<Object[]> contents = new ArrayList<Object[]>();

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/** Holds value of property lobPath. */
	private String lobPath = System.getProperty("java.io.tmpdir");
	/** Text to be displayed for null values */
	private String nullText = "??null??";
	/** Holds value of property nullColour. */
	private Color nullColour = new Color(240, 240, 240);

	ArrayList<Boolean> colsAutoincrement = new ArrayList<>();
	ArrayList<Boolean> colsSearchable = new ArrayList<>();
	ArrayList<Boolean> isXMLConvertedToClob = new ArrayList<>();
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
	private String sourceTable = "";

	public MyTableModel(Connection conn, String query, String tableToUse, String mode) {
		super();

		sourceTable = tableToUse;
		this.conn = conn;
		this.query = query;
		this.mode = mode;

		if (conn != null) {
			String typeName = "";
			int columnType = 0;
			boolean fromAsterisk = false;

			try {
				if (query.contains("*")) { // May be it has hidden XML reads
					fromAsterisk = true;
					DatabaseMetaData dmd = conn.getMetaData();
					ResultSet typeInfo = dmd.getTypeInfo();
					ResultSet columns = dmd.getColumns(null, null, tableToUse, null);
					String colName = "";
					String columnsToUse = "";
					String finalQuery = "";

					while (columns.next()) {
						colName = columns.getString("COLUMN_NAME");
						typeName = columns.getString("TYPE_NAME");
						colsNames.add(colName);
						typeNames.add(typeName);
					}

					for (int i = 0; i < colsNames.size(); i++) {

						if (typeNames.get(i).equals("XML")) {
							columnsToUse += " xmlserialize (" + colsNames.get(i) + " as clob) ";
							isXMLConvertedToClob.add(true);
						} else {
							columnsToUse += colsNames.get(i);
							isXMLConvertedToClob.add(false);
						}
						columnsToUse += (i < (colsNames.size() - 1)) ? ", " : "";
					}

					int asterixPosition = query.indexOf("*");
					finalQuery = query.substring(0, asterixPosition) + columnsToUse
							+ query.substring(asterixPosition + 1);
					// colsNames.clear();
					typeNames.clear();
					System.out.println(finalQuery);
					query = finalQuery;
				}

				statement = conn.createStatement();
				resultSet = statement.executeQuery(query);
				
				
				// Metadata for several values.
				int c = resultSet.getMetaData().getColumnCount();
				for (int i = 1; i <= c; i++) {

					System.out.println(resultSet.getMetaData().getColumnName(i));

					if (!fromAsterisk)
						colsNames.add(resultSet.getMetaData().getColumnName(i));
					colsSizes.add(resultSet.getMetaData().getPrecision(i));
					colsAutoincrement.add(resultSet.getMetaData().isAutoIncrement(i));
				//	colsSearchable.add(resultSet.getMetaData().isSearchable(i));
					String columnTypeName = resultSet.getMetaData().getColumnTypeName(i);
					colsSearchable.add((short) DBManager.dataTypeInfo(columnTypeName, "SEARCHABLE") == 2
									|| (short) DBManager.dataTypeInfo(columnTypeName, "SEARCHABLE") == 3);
						
					// if(!fromAsterisk)
					typeNames.add(columnTypeName);
					columnType = resultSet.getMetaData().getColumnType(i);
					colsClasses.add(DBManager.getColClass(columnType, typeName));
				}

				if (mode.equals("MODE_FILL") && resultSet.next()) {

					do {
						ArrayList<Object> row = new ArrayList<>();
						for (int i = 0; i < c; i++) {
							Object redObj = null;

							System.out.println("typeNames" + i + ": " + typeNames.get(i));
							System.out.println("colsclasses" + i + ": " + colsClasses.get(i));

							if (typeNames.get(i).equals("CLOB")) {
								// if (colsClasses.get(i) == java.sql.Clob.class) {

								Clob clobValue = resultSet.getClob(i + 1);

								if (clobValue != null) {
									JDBCTableLob lobValue = new JDBCTableLob(clobValue, lobPath);
									redObj = lobValue;
								}

							} else {
								if (typeNames.get(i).equals("BLOB")) {
									// if (colsClasses.get(i) == java.sql.Blob.class) {

									Blob blobValue = resultSet.getBlob(i + 1);
									if (blobValue != null) {
										JDBCTableLob lobValue = new JDBCTableLob(blobValue, lobPath);
										redObj = lobValue;
									}
								} else {
									if (colsClasses.get(i) == java.lang.Byte.class) {
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
										if (typeNames.get(i).equals("XML")) {
											// if (colsClasses.get(i) == java.sql.SQLXML.class) {
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

		if (colsClasses.get(columnIndex) == java.sql.Clob.class) {
			value = ((JDBCTableLob) value).getText();
		} else if (colsClasses.get(columnIndex) == java.sql.Blob.class) {
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
		
		return Math.max(colsSizes.get(column), 30);
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

	public void insertNewRow(ArrayList<Object> row2Add) {

		int numCols = getColumnCount();
		String tableName = sourceTable;
		String quotePrefix = "";
		String quoteSuffix = "";

		String order = "INSERT INTO " + tableName + " (";
		String order2 = " VALUES (";

		System.out.println("Entra 1");

		for (int j = 0; j < numCols; j++) {
			System.out.println("Entra 2");

			if (!colsAutoincrement.get(j)) {

				System.out.println("Entra 3");
				order += colsNames.get(j) + (((j < numCols - 1) 
						&& !colsAutoincrement.get(j + 1)) ? ", " : " ");

				System.out.println("Entra 4: " + typeNames.get(j));
				System.out.println("colsClasses.get(j): " + colsClasses.get(j));
				System.out.println("typeNames.get(j): " + typeNames.get(j));

				if (typeNames.get(j).equals("XML") || typeNames.get(j).equals("CLOB") && isXMLConvertedToClob.get(j)) {
					System.out.println("Entra 5");
					order2 += " xmlparse (document cast (? as clob) preserve whitespace)";

					// XMLPARSE(DOCUMENT ' <name> Derby </name>' PRESERVE
					// WHITESPACE));
				} else {
					order2 += "?";
				}
				System.out.println("Entra 6");

				order2 += (((j < numCols - 1) && !colsAutoincrement.get(j + 1)) ? ", " : " ");
			}
		}

		order += ")";
		order2 += ")";

		PreparedStatement ps = null;
		System.out.println("Entra 7");

		try {
			System.out.println("Entra 8:" + order + order2);
			ps = conn.prepareStatement(order + order2);
			System.out.println("Entra 9");

		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println("Entra 10");

		int numUsedParameters = 0;

		for (int j = 0; j < numCols; j++) {

			System.out.println("colsClasses.get(j): " + colsClasses.get(j));
			System.out.println("typeName.get(j): " + typeNames.get(j));

			// + " " + typeNames.get(j).equals("XML"));

			if (colsClasses.get(j) == java.sql.Clob.class && !typeNames.get(j).equals("XML")) {

				System.out.println("Entra 11");
				String file2Load = row2Add.get(j).toString();

				try {
					// conn.setAutoCommit(false);
					Clob myClob = conn.createClob();
					Writer clobWriter = myClob.setCharacterStream(1);
					String str = readFile(file2Load, clobWriter);
					System.out.println("Wrote the following: " + clobWriter.toString());
					System.out.println("Length of Clob: " + myClob.length());
					if (!colsAutoincrement.get(j)) {
						ps.setClob(++numUsedParameters, myClob);
					}
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
						if (!colsAutoincrement.get(j)) {
							ps.setBlob(++numUsedParameters, blob);
						}
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {

					if (typeNames.get(j).equals("XML")) {

						try {
							// conn.setAutoCommit(false);

							// Reads the XML file

							String fileName = row2Add.get(j).toString();
							javax.xml.parsers.DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory
									.newInstance();

							factory.setNamespaceAware(true);
							DocumentBuilder builder = factory.newDocumentBuilder();
							StringReader strReader = new StringReader(fileName);
							InputSource iSrc = new InputSource(strReader);
							String XString = iSrc.toString();
							XString = XString.replaceAll("[^\\x20-\\x7e\\x0A]", ""); // To replace illegal characters
							Document doc = builder.parse(fileName);
							String convertedDoc = JDBCUtilities.convertDocumentToString(doc);
							if (!colsAutoincrement.get(j)) {
								ps.setClob(++numUsedParameters, new StringReader(convertedDoc));
							}
							// conn.commit();

						} catch (ParserConfigurationException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} catch (SAXException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} catch (TransformerConfigurationException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} catch (TransformerException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							// e.printStackTrace();
							JDBCUtilities.printSQLException(e);
						}

					} else {
						try {
//							quotePrefix = (String) DBManager.dataTypeInfo(typeNames.get(j), "LITERAL_PREFIX");
//							quoteSuffix = (String) DBManager.dataTypeInfo(typeNames.get(j), "LITERAL_SUFFIX");

//							if (quotePrefix != null) {
//								String toAdd = quotePrefix + row2Add.get(j) + quoteSuffix;
//								if (!colsAutoincrement.get(j)) {
//									ps.setObject(++numUsedParameters, toAdd);
//								}
//							}
							String toAdd = (String) row2Add.get(j);
							if (!colsAutoincrement.get(j)) {
								ps.setObject(++numUsedParameters, toAdd);
							}
							
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		}

		try

		{
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
			writerArg.write(nextLine);
			sb.append(nextLine);
		}
		// Convert the content into to a string
		String clobData = sb.toString();

		// Return the data.
		return clobData;
	}

	/**
	 * @return the mode
	 */
	public String getMode() {
		return mode;
	}

	/**
	 * @param mode
	 *            the mode to set
	 */
	public void setMode(String mode) {
		this.mode = mode;
	}

	/**
	 * @return the sourceTable
	 */
	public String getSourceTable() {
		return sourceTable;
	}

}

package dbmanager;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.derby.drda.NetworkServerControl;

public class DBConnect {

	private static NetworkServerControl server;
	public static boolean serverIsOn;
	public static final String DBMSYSTABLE = "DBM4PROC";

	public static boolean inicServer() {
		try {
			server = new NetworkServerControl();
			server.start(null);
			server.ping(); // if ping fails throws an exception
			serverIsOn = true;
			DBGUIFrame.checkServerMenu();
			return true;
		} catch (Exception f) {
			serverIsOn = false;
			DBGUIFrame.checkServerMenu();
			return false;
		}

		// String mySysInfo = server.getSysinfo();
		// System.out.println("\bSysInfo\n" + mySysInfo);
		//
		// String myRuntimeInfo = server.getRuntimeInfo();
		// System.out.println("\bRunTimeInfo\n" + myRuntimeInfo);

		// System.out.println("\bProperties\n");
		// Properties p = server.getCurrentProperties();
		// p.list(System.out);
		// System.out.println(" derby.drda.host = " +
		// System.getProperty("derby.drda.host"));
	}

	public static void stopServer() {
		try {
			server = new NetworkServerControl();
			server.shutdown();
			serverIsOn = false;
		} catch (Exception f) {
			DBFactory.errorPrint(f);
		}

		DBGUIFrame.checkServerMenu();
	}

	// Can connect in embedded mode or as client mode
	public static Connection connect(boolean embed, String dBPath, String user, char[] paswd, boolean create)
			throws Exception {

		String userLit = "; ";
		String paswLit = "; ";
		String myUser = user;
		char[] myPaswd = paswd;
		String dbURL = "";

		if (embed) {
			try {
				Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
				dbURL = "jdbc:derby:" + dBPath + "; create=" + create;
			} catch (Exception e) {
				System.out.println(dbURL);
				throw e;
			}
		} else {
			if (myUser == null || myUser.equals("")) {
				userLit = paswLit = "; ";
			} else {
				userLit = "; user=";
				paswLit = " password= ";
			}
			try {
				Class.forName("org.apache.derby.jdbc.ClientDriver");
				dbURL = "jdbc:derby://localhost:1527/" + dBPath + userLit + myUser + paswLit + myPaswd + "; create="
						+ create;
			} catch (Exception e) {
				System.out.println(dbURL);
				throw e;
			}
		}

		try {
			if (DBManager.conn != null) {
				DBManager.conn.close();
			}
			DBManager.conn = DriverManager.getConnection(dbURL);
		} catch (SQLException ex) {
			throw ex;
		}

		return DBManager.conn;
	}

	// Here we read the rest of data for the tree but only those from the opened
	// node.
	public static void loadTables(DefaultMutableTreeNode node, DBTreeNodeK nodeInfo) {

		// Connection conn = null;
		String pathLocation = nodeInfo.getPathLocation();
		String nodeText = nodeInfo.getText();
		String childTableName = "";
		DefaultMutableTreeNode partialCO = null;

		try {
			DBManager.conn = connect(true, pathLocation + "/" + nodeText, "", null, false);
		} catch (Exception ex) {

			if (ex instanceof SQLException) {

				String sQLState = ((SQLException) ex).getSQLState();

				if (sQLState.equals("08004")) { // Authentication error
					JOptionPane.showMessageDialog(
							null, "Error : " + ((SQLException) ex).getSQLState() + "  "
									+ ((SQLException) ex).getErrorCode() + " " + (ex).getMessage(),
							"Authentication Error", JOptionPane.WARNING_MESSAGE);
					return;
				}

				if (sQLState.equals("XJ004")) {
					JOptionPane.showMessageDialog(null,
							"Error : " + sQLState + "  " + ((SQLException) ex).getErrorCode() + " " + (ex).getMessage(),
							"The folder is not a valid Java DB.", JOptionPane.ERROR_MESSAGE);
				} else {
					JOptionPane.showMessageDialog(
							null, "Error : " + ((SQLException) ex).getSQLState() + "  "
									+ ((SQLException) ex).getErrorCode() + " " + (ex).getMessage(),
							"SQL Error", JOptionPane.ERROR_MESSAGE);
				}
			} else {
				JOptionPane.showMessageDialog(null,
						"It is locked by another process or it is not a valid folder. Try again later.",
						"Folder not available.", JOptionPane.ERROR_MESSAGE);
			}

			DBFactory.errorPrint(ex);

		}

		try {

			DatabaseMetaData dbmd = DBManager.conn.getMetaData();

			String[] types = { "TABLE" };
			ResultSet rs = dbmd.getTables(null, null, "%", types);
			node.removeAllChildren();
			DBTreeNodeK partialTablesHead = new DBTreeNodeK("HEAD", "Tables", pathLocation, "TABLE", nodeText,
					childTableName, "");
			DefaultMutableTreeNode partialTH = new DefaultMutableTreeNode(partialTablesHead);
			node.add(partialTH);

			// Load tables
			while (rs.next()) {
				childTableName = rs.getString("TABLE_NAME");

				DBTreeNodeK childNodeInfo = new DBTreeNodeK("TABLE", childTableName, pathLocation, "TABLE", nodeText,
						childTableName, "");

				DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(childNodeInfo, true);
				partialTH.add(childNode);

				DBTreeNodeK partialColumnsHead = new DBTreeNodeK("HEAD", "Columns", pathLocation, "TABLE", nodeText,
						childTableName, "");
				partialCO = new DefaultMutableTreeNode(partialColumnsHead);
				childNode.add(partialCO);

				// Load columns

				ResultSet rsColumns = dbmd.getColumns(null, null, childTableName, null);

				while (rsColumns.next()) {
					String childColName = rsColumns.getString("COLUMN_NAME");
					String columnTypeName = rsColumns.getString("TYPE_NAME");
					int dataType = rsColumns.getInt("DATA_TYPE");

					Object createParams = DBManager.dataTypeInfo(columnTypeName, "CREATE_PARAMS");
					String typeDescr = columnTypeName;

					if (createParams != null && ((createParams.toString()).contains("length")
							|| (createParams.toString()).contains("precision"))) {

						int columnSize = rsColumns.getInt("COLUMN_SIZE");
						if (dataType == -3 || dataType == -4) {
							typeDescr.replaceFirst("()", "(" + columnSize + ")");
							if (createParams.toString().contains("scale")) {
								int decimalDigits = rsColumns.getInt("DECIMAL_DIGITS");
								typeDescr.replaceFirst(")", ", " + decimalDigits // Scale
										+ ")");
							}
						} else {
							typeDescr += "(" + columnSize;
							if (createParams.toString().contains("scale")) {
								int decimalDigits = rsColumns.getInt("DECIMAL_DIGITS");
								typeDescr += ", " + decimalDigits; // Scale
							}
							typeDescr += ")";
						}
					}

					DBTreeNodeK gChildNodeInfo = new DBTreeNodeK("COLUMN", childColName, pathLocation, "TABLE",
							nodeText, childTableName, typeDescr);

					DefaultMutableTreeNode gChildNode = new DefaultMutableTreeNode(gChildNodeInfo, true);
					partialCO.add(gChildNode);

				}

				rsColumns.close();

				if (partialCO.getChildCount() == 0) {
					partialCO.add(new DefaultMutableTreeNode(new DBTreeNodeK("DUMMY", "W/O Columns", pathLocation,
							"TABLE", nodeText, childTableName, "")));
				} else {

					// Load indices

					DBTreeNodeK partialIndicesHead = new DBTreeNodeK("HEAD", "Indices", pathLocation, "TABLE", nodeText,
							childTableName, "");
					DefaultMutableTreeNode partialIH = new DefaultMutableTreeNode(partialIndicesHead);
					partialCO.add(partialIH);

					ResultSet rsIndices = dbmd.getIndexInfo(null, null, childTableName, false, true);

					// **
					// ResultSet getIndexInfo(String catalog,
					// String schema,
					// String table,
					// boolean unique,
					// boolean approximate)
					// throws SQLException
					//
					// Retrieves a description of the given table's indices and statistics. They are
					// ordered by NON_UNIQUE, TYPE, INDEX_NAME, and ORDINAL_POSITION.
					// Each index column description has the following columns:
					// 1.TABLE_CAT String => table catalog (may be null)
					// 2.TABLE_SCHEM String => table schema (may be null)
					// 3.TABLE_NAME String => table name
					// 4.NON_UNIQUE boolean => Can index values be non-unique. false when TYPE is
					// tableIndexStatistic
					// 5.INDEX_QUALIFIER String => index catalog (may be null); null when TYPE is
					// tableIndexStatistic
					// 6.INDEX_NAME String => index name; null when TYPE is tableIndexStatistic
					// 7.TYPE short => index type:
					// * tableIndexStatistic - this identifies table statistics that are returned in
					// conjuction with a table's index descriptions.
					// * tableIndexClustered - this is a clustered index
					// * tableIndexHashed - this is a hashed index
					// * tableIndexOther - this is some other style of index
					//
					// 8.ORDINAL_POSITION short => column sequence number within index; zero when
					// TYPE is tableIndexStatistic
					// 9.COLUMN_NAME String => column name; null when TYPE is tableIndexStatistic
					// 10.ASC_OR_DESC String => column sort sequence, "A" => ascending, "D" =>
					// descending, may be null if sort sequence is not supported; null when TYPE is
					// tableIndexStatistic
					// 11.CARDINALITY long => When TYPE is tableIndexStatistic, then this is the
					// number of rows in the table; otherwise, it is the number of unique values in
					// the index.
					// 12.PAGES long => When TYPE is tableIndexStatisic then this is the number of
					// pages used for the table, otherwise it is the number of pages used for the
					// current index.
					// 13.FILTER_CONDITION String => Filter condition, if any. (may be null)
					// Parameters:catalog - a catalog name; must match the catalog name as it is
					// stored in this database; "" retrieves those without a catalog; null means
					// that the catalog name should not be used to narrow the searchschema - a
					// schema name; must match the schema name as it is stored in this database; ""
					// retrieves those without a schema; null means that the schema name should not
					// be used to narrow the searchtable - a table name; must match the table name
					// as it is stored in this databaseunique - when true, return only indices for
					// unique values; when false, return indices regardless of whether unique or
					// notapproximate - when true, result is allowed to reflect approximate or out
					// of data values; when false, results are requested to be
					// accurateReturns:ResultSet - each row is an index column
					// descriptionThrows:SQLException - if a database access error occurs
					// **************************

					String prevIndexName = "";
					DBTreeNodeK indexNodeInfo = null;
					DefaultMutableTreeNode indexNode = null;

					while (rsIndices.next()) {
						String indexName = rsIndices.getString("INDEX_NAME");

						if (!prevIndexName.equals(indexName)) {
							prevIndexName = indexName;

							indexNodeInfo = new DBTreeNodeK("INDEX", indexName, pathLocation, "TABLE", nodeText,
									childTableName, "Index");
							indexNode = new DefaultMutableTreeNode(indexNodeInfo, true);
							partialIH.add(indexNode);
						}

						String indexedColumn = rsIndices.getString("COLUMN_NAME");
						String asc_or_desc = rsIndices.getString("ASC_OR_DESC"); // To be denoted by icon
						String idxColStr = indexedColumn + (asc_or_desc.equals("D") ? " DESC" : " ASC");
						DBTreeNodeK idxColNodeInfo = new DBTreeNodeK("INDEXEDCOLUMN", idxColStr, pathLocation, "TABLE",
								nodeText, childTableName, "COLUMN");
						DefaultMutableTreeNode idxColNode = new DefaultMutableTreeNode(idxColNodeInfo);
						indexNode.add(idxColNode);
					}

					if (partialIH.getChildCount() == 0) {
						partialIH.add(new DefaultMutableTreeNode(new DBTreeNodeK("DUMMY", "W/O Indices", pathLocation,
								"TABLE", nodeText, childTableName, "")));
						partialIndicesHead.setCategory("DUMMY");
					}
				}
			}
			if (partialTH.getChildCount() == 0) {
				partialTablesHead.setCategory("DUMMY");
			}

			// Views and procedures not implemented yet
			// node.add(new DefaultMutableTreeNode(new DBTreeNodeK("DUMMY", "Views. Not
			// implemented yet.")));
			// node.add(new DefaultMutableTreeNode(new DBTreeNodeK("DUMMY", "Procedures. Not
			// implemented yet.")));
			rs.close();
			DBManager.conn.close();

		} catch (

		SQLException e) {
			e.printStackTrace();
		}
	}

	public static void createSysDB(Connection connex, String pathToDBManager) {

		System.out.println("Entering createSysTable.");

		try {
			if (connex == null) {
				System.out.println("Connex is null.");
				return;
			}

			System.out.println("SCHEMA: " + connex.getSchema());

			// Recreate initial system table
			DBManager.stmt = connex.createStatement();

			// This creates a System DB master table containing a list of
			// the databases managed by DBManager.
			System.out.println("SYS Tables creation.");

			//
			String query = "create table APP.DBLIST (DBMS VARCHAR(30) default 'Java DB' not null,"
					+ " DBNAME VARCHAR(30) not null, " + " DESCRIPTION VARCHAR(255), FILEPATH VARCHAR(255) not null,"
					+ " primary key (DBMS, DBNAME))";

			int result = DBManager.stmt.executeUpdate(query);
			System.out.println("Query Create Table: " + query + " Resultado: " + result);

			query = "INSERT INTO APP.DBLIST (DBMS, DBNAME, DESCRIPTION, FILEPATH)" + " VALUES ('Java DB', '"
					+ DBMSYSTABLE + "', 'Contains the list of databases.'," + "'" + DBManager.pathToDBSettings + "')";

			result = DBManager.stmt.executeUpdate(query);
			System.out.println("Query INSERT DATA: " + query + " Resultado: " + result);

		} catch (Exception ey) {
			System.out.println("Error in table creation.");
			ey.printStackTrace();
		}

	}

	/**
	 * @return the server
	 */
	public static NetworkServerControl getServer() {
		return server;
	}

}

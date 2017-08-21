/**
 * Manages Derby server.
 *
 * ##copyright##
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA  02111-1307  USA
 *
 * @author   ##author##
 * @modified ##date##
 * @version  ##tool.prettyVersion##
 */

package dbmanager;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import processing.app.Base;
import processing.app.Sketch;
import processing.app.tools.Tool;
import processing.app.ui.Editor;

// when creating a tool, the name of the main class which implements Tool must
// be the same as the value defined for project.name in your build.properties

public class DBManager<propsDBM> implements Tool {
	Base base;
	public DBGUIFrame frame;
	public static String javaDBInstall; // Path to Derby installation
	public static String pathToDBSettings;
	public static DBManageProps propsDBM;
	public static Statement stmt;
	public static String pathToSketchBook;
	public static TreeModel treeDataModel;
	public static Connection conn;
	public static JTree dBtree;
	public static String derbySystemHome;
	public TreePath treePath;
	public static String pathToDBManager;
	public static DBFactory dBfactory;
	private static JPopupMenu popup;
	public static MyTreeCellRenderer renderer;
	public static String renderProp = "";
	public TreeExpansionUtil xpan;
	private static Object[][] hiddenTypesTable = new Object[50][20];
	private static Map<String, Integer> rowSearch = new HashMap<String, Integer>();
	private static Map<String, Integer> colSearch = new HashMap<String, Integer>();
	public static Map<String, String> typeConvert = new HashMap<String, String>();

	@Override
	public String getMenuTitle() {
		return "##tool.name##";
	}

	@Override
	public void init(Base base) {

		// Store a reference to the Processing application itself
		this.base = base;
	}

	@Override
	public void run() {
		// Get the currently active Editor to run the Tool on it
		Editor editor = base.getActiveEditor();

		Sketch sketch = editor.getSketch();

		// Fill in author.name, author.url, tool.prettyVersion and
		// project.prettyName in build.properties for them to be auto-replaced
		// here.
		System.out.println("Tool ##tool.name## ##tool.prettyVersion## by ##author##");

		if (frame == null) { // We are beginning

			initTypesTitles();

			// Obtains Processing System parameters
			Base.locateSketchbookFolder(); // Directory for sketches
			pathToSketchBook = Base.getSketchbookFolder().getAbsolutePath();

			// Path to the dir. with application settings.
			// There we have our DBM System database.
			pathToDBSettings = Base.getSettingsFolder().getAbsolutePath() + "DBM";

			// We check if the directory exist. If not, this may be the first startup and
			// we need to install our system of directories.
			File dirToTest = new File(pathToDBSettings);
			boolean dirWasCreated = false;
			boolean firstTime = false;

			if (!dirToTest.exists()) {
				JOptionPane.showMessageDialog(null, "The settings folder " + dirToTest + " will be created.",
						"Settings folder missing.", JOptionPane.WARNING_MESSAGE);
				firstTime = true;

				if (!(dirWasCreated = dirToTest.mkdirs())) {
					JOptionPane.showConfirmDialog(null, "Fatal Error. The settings folder could not be created.");
					System.exit(-1);
				}
			}

			propsDBM = new DBManageProps(pathToDBSettings);

			if (dirWasCreated && firstTime) {
				propsDBM.saveInitialProperties();
			}

			propsDBM.readProperties();

			// Derby System Home. Default dir. for server and new databases.
			// Derby properties
			String processingPath = System.getProperty("user.dir");
			derbySystemHome = propsDBM.getDBMProp("derby.system.home");

			if (derbySystemHome == null || derbySystemHome.trim().equals("")) {
				derbySystemHome = processingPath + "\\data";
				propsDBM.setDBMProp("derby.system.home", derbySystemHome);
				propsDBM.saveProperties();
				System.setProperty("derby.system.home", derbySystemHome);
			}

			propsDBM.readProperties();

			dBtree = new JTree();
			dBtree.setRowHeight(Integer.parseInt(propsDBM.getDBMProp("treerowsheight")));
			dBtree.setDragEnabled(true);

			// Enable tool tips.
			ToolTipManager.sharedInstance().registerComponent(dBtree);

			// We want to change the Jtree node icon
			renderer = new MyTreeCellRenderer();
			renderProp = propsDBM.getDBMProp("treeiconsflavour");

			if (renderProp.equals("Processing")) {
				dBtree.setCellRenderer(renderer);
			} else {
				dBtree.setCellRenderer(null);
			}

			// Fills Services JTree model
			treeDataModel = new DefaultTreeModel(getTreeModel());
			dBtree.setModel(treeDataModel);
			dBtree.setShowsRootHandles(true);
			dBtree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

			dBtree.addTreeSelectionListener(new TreeSelectionListener() {

				@Override
				public void valueChanged(TreeSelectionEvent event) {
					if (event != null) {
						JTree auxTree = (JTree) event.getSource();
						DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) auxTree
								.getLastSelectedPathComponent();
						if (selectedNode != null) {
							DBTreeNodeK nodeInfo = (DBTreeNodeK) selectedNode.getUserObject();
							ExecSQLPanel.getTxtSelected()
									.setText(nodeInfo.getPathLocation() + '/' + nodeInfo.getdBaseName());
							if (nodeInfo.getCategory().equals("TABLE")) {

								String table2Manage = nodeInfo.getText();
								frame.getExecSQLPanel().getTextEditingElement().setText(table2Manage);
								frame.getExecSQLPanel().executeSQL("SELECT * FROM " + table2Manage, "MODE_FILL");
							}
						}
					}
				}
			});

			dBtree.addTreeWillExpandListener(new TreeWillExpandListener() {

				@Override
				public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
					treePath = event.getPath();
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) treePath.getLastPathComponent();
					DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) node.getFirstChild();
					DBTreeNodeK nodeInfo = (DBTreeNodeK) node.getUserObject();
					DBTreeNodeK childNodeInfo = (DBTreeNodeK) childNode.getUserObject();

					if (nodeInfo.getCategory().equals("Java DB") && childNodeInfo.getCategory().equals("DUMMY")) {
						DBConnect.loadTables(node, nodeInfo);
					}
				}

				@Override
				public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {

				}
			});

			dBtree.addMouseMotionListener(new MouseMotionAdapter() {
				@Override
				public void mouseMoved(MouseEvent e) {
					int x = (int) e.getPoint().getX();
					int y = (int) e.getPoint().getY();
					treePath = dBtree.getPathForLocation(x, y);
					if (treePath == null) {
						dBtree.setCursor(Cursor.getDefaultCursor());
					} else {
						dBtree.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
					}
				}
			});

			dBtree.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {

					DefaultMutableTreeNode node = null;

					if (treePath == null) {
						return;
					} else {
						node = (DefaultMutableTreeNode) treePath.getLastPathComponent();
						int row = dBtree.getClosestRowForLocation(e.getX(), e.getY());

						if (row == -1) {
							return;
						} else {
							dBtree.setSelectionRow(row);
						}
					}

					if (SwingUtilities.isRightMouseButton(e)) {
						popup = new JPopupMenu();
						dBtree.add(popup);
						ActionListener menuListener = new ActionListener() {
							public void actionPerformed(ActionEvent event) {
								dBfactory = new DBFactory(event, dBtree.getSelectionPath(), frame);

								final TreeExpansionUtil expander = new TreeExpansionUtil(dBtree);

								final String state = expander.getExpansionState();

								// System.out.println(state);

								treeDataModel = new DefaultTreeModel(getTreeModel());
								dBtree.setModel(treeDataModel);
								dBtree.updateUI();

								// Recover the expansion state
								expander.setExpansionState(state);
							}
						};

						if (node == null)
							return;

						DBTreeNodeK nodeInfo = (DBTreeNodeK) node.getUserObject();
						JMenuItem menuItem;

						popup.add(menuItem = new JMenuItem("Copy name"));
						menuItem.addActionListener(menuListener);
						popup.addSeparator();

						switch (nodeInfo.getCategory()) { // Action callbacks are in DBFactory
						case "root":
							popup.add(menuItem = new JMenuItem("Create Database..."));
							menuItem.addActionListener(menuListener);
							popup.add(menuItem = new JMenuItem("Register Database..."));
							menuItem.addActionListener(menuListener);
							popup.add(menuItem = new JMenuItem("Refresh"));
							menuItem.addActionListener(menuListener);
							popup.add(menuItem = new JMenuItem("Properties"));
							menuItem.addActionListener(menuListener);
							break;

						case "Java DB":
							if (!nodeInfo.getText().equals(DBConnect.DBMSYSTABLE)) { // Take care not delete sys
																						// database contents
								popup.add(menuItem = new JMenuItem("Delete Database..."));
								menuItem.addActionListener(menuListener);
								popup.add(menuItem = new JMenuItem("Unregister Database..."));
								menuItem.addActionListener(menuListener);
								popup.addSeparator();
								popup.add(menuItem = new JMenuItem("Create Table..."));
								menuItem.addActionListener(menuListener);
								popup.add(menuItem = new JMenuItem("Recreate Table..."));
								menuItem.addActionListener(menuListener);
							}
							popup.add(menuItem = new JMenuItem("Refresh"));
							menuItem.addActionListener(menuListener);

							break;

						case "HEAD":

							if (!nodeInfo.getdBaseName().equals(DBConnect.DBMSYSTABLE)) { // Take care not delete sys
								if (nodeInfo.getText().equals("Tables") || nodeInfo.getText().equals("Columns")
										|| nodeInfo.getText().equals("Indices")) {
									// menuItem.addActionListener(menuListener);
									dBtree.setToolTipText(nodeInfo.getDataType() + " ");
								}

								if (nodeInfo.getText().equals("Tables")) {
									popup.add(menuItem = new JMenuItem("Create Table..."));
									menuItem.addActionListener(menuListener);
									popup.add(menuItem = new JMenuItem("Recreate Table..."));
									menuItem.addActionListener(menuListener);
								}
							}

							popup.add(menuItem = new JMenuItem("Refresh"));
							menuItem.addActionListener(menuListener);
							break;

						case "TABLE":
							popup.add(menuItem = new JMenuItem("Manage data..."));
							menuItem.addActionListener(menuListener);

							if (!nodeInfo.getdBaseName().equals(DBConnect.DBMSYSTABLE)) { // Take care not delete sys
								popup.add(menuItem = new JMenuItem("Delete Table"));
								menuItem.addActionListener(menuListener);
								popup.add(menuItem = new JMenuItem("Add column..."));
								menuItem.addActionListener(menuListener);

								popup.add(menuItem = new JMenuItem("Execute Command..."));
								menuItem.addActionListener(menuListener);
								popup.addSeparator();
							}
							popup.add(menuItem = new JMenuItem("Generate DDL..."));
							menuItem.addActionListener(menuListener);

							popup.addSeparator();

							popup.add(menuItem = new JMenuItem("Refresh"));
							menuItem.addActionListener(menuListener);
							popup.add(menuItem = new JMenuItem("Properties"));
							menuItem.addActionListener(menuListener);

							break;

						case "DUMMY":
							if (nodeInfo.getText().equals("Tables")) {
								popup.add(menuItem = new JMenuItem("Create Table..."));
								menuItem.addActionListener(menuListener);
								popup.add(menuItem = new JMenuItem("Recreate Table..."));
								menuItem.addActionListener(menuListener);

							}
							popup.add(menuItem = new JMenuItem("Refresh"));
							menuItem.addActionListener(menuListener);
							break;
						default:
							popup.add(menuItem = new JMenuItem("Refresh"));
							menuItem.addActionListener(menuListener);
						}

						popup.show(e.getComponent(), e.getX(), e.getY());
					}

				}
			});

			frame = new DBGUIFrame();
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			frame.pack();
			frame.setSize(screenSize.width * 2 / 3, screenSize.height * 2 / 3);
			frame.setLocationRelativeTo(null);
		}

		String servInicSt = propsDBM.getDBMProp("Server_initial_state");
		if (servInicSt.equals("on")) {
			DBConnect.inicServer();
		}

		DBGUIFrame.checkServerMenu();
		frame.setVisible(true);
		frame.toFront();
	}

	// Reads from DBMSYSTABLE and begins to build the JTree model
	public static DefaultMutableTreeNode getTreeModel() {

		DBTreeNodeK nodeInfo = new DBTreeNodeK("root", "Databases", pathToDBSettings, "DBASE", DBConnect.DBMSYSTABLE,
				DBConnect.DBMSYSTABLE);
		DefaultMutableTreeNode root = new DefaultMutableTreeNode(nodeInfo);

		WelcomeDlg dialog = null;
		pathToDBManager = pathToDBSettings + "/" + DBConnect.DBMSYSTABLE;

		// We attempt to connect DBMSYSTABLE and determine if exists
		try {
			conn = DBConnect.connect(!DBConnect.serverIsOn, pathToDBManager, "", null, false);
		} catch (Exception ex) {
			try {
				DBConnect.inicServer();
				conn = DBConnect.connect(!DBConnect.serverIsOn, pathToDBManager, "", null, false);
			} catch (Exception ez) {
				System.out.println("The DBM system database doesn't exists.");
				conn = null;
			}
		}

		if (conn == null) { // DBMSYSTABLE is missing. May be this is the first
							// run of the tool.
			// A welcome dialog
			try {
				dialog = new WelcomeDlg();
				dialog.getTxtJavaExec().setText(Paths.get(System.getenv("java_home")).toRealPath().toString());
				dialog.getTxtJavaDBExec().setText(Paths.get(System.getenv("derby_home")).toRealPath().toString());
				dialog.getTxtDerbySysHome()
						.setText(Paths.get(System.getProperty("derby.system.home")).toRealPath().toString());
				dialog.getTxtJdbcDerby().setText(dialog.getTxtDerbySysHome().getText());
				dialog.getTxtProcessing().setText(Paths.get(System.getProperty("user.dir")).toRealPath().toString());
				dialog.getTxtSketches().setText(DBManager.pathToSketchBook);

				dialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
				dialog.setLocationRelativeTo(null);
				dialog.setVisible(true);
			} catch (Exception f) {
				f.printStackTrace();
			}

			if (dialog.result == 0) {
				System.setProperty("derby.system.home", dialog.getTxtDerbySysHome().getText());
			}

			dialog.dispose();

			// We attempt to connect again to DBMSYSTABLE but now with "create =
			// true".
			try {
				conn = DBConnect.connect(!DBConnect.serverIsOn, pathToDBManager, "", null, true);
			} catch (Exception ex) {
				System.out.println("The DBM system database can't be created or is not accesible.");
				conn = null;
				ex.printStackTrace();
			}

			try {
				DBConnect.createSysDB(conn, pathToDBManager);
			} catch (Exception ey) {
				ey.printStackTrace();
				return null;
			}

		}

		try {

			// Fills a local table with data types
			loadHiddenDataTypes(conn);

			// Check that DBLIST table exists
			DatabaseMetaData dmd = conn.getMetaData();
			ResultSet rs = dmd.getTables(null, "APP", "DBLIST", null);

			if (!rs.next()) {
				DBConnect.createSysDB(conn, pathToDBManager);
			} else {

//				System.out.println("SysTable exists detected." + rs.toString());

			}

		} catch (SQLException ex) {
			System.out.println("SysTable detection failed.");
			ex.printStackTrace();
			return null;
		}

		try {
			// Statement stmt =
			// conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
			// ResultSet.CONCUR_UPDATABLE);
			stmt = conn.createStatement();
			String sql = "SELECT * from DBLIST";
			ResultSet rs = stmt.executeQuery(sql);
			DatabaseMetaData dmd = conn.getMetaData();

			DefaultMutableTreeNode partialHead = root;

			// Partial lazy reading. First, we complete the tree until the list of
			// databases, and
			// we read each database in the moment of open it to add its data to the tree.
			while (rs.next()) {
				String redDBMS = rs.getString("DBMS");
				String dBName = rs.getString("DBNAME");
				String pathToDBase = rs.getString("FILEPATH");
				String dataType = "DBASE";
				String dBaseName = dBName;
				nodeInfo = new DBTreeNodeK(redDBMS, dBName, pathToDBase, dataType, dBaseName, dBName);

				DefaultMutableTreeNode auxNode = new DefaultMutableTreeNode(nodeInfo);
				partialHead.add(auxNode);

				// We create dummy data to manage lazy reading
				DBTreeNodeK dummyTablesHead = new DBTreeNodeK("DUMMY", "Tables", "", "", "", dBaseName);
				// DBTreeNodeK dummyViewsHead = new DBTreeNodeK("DUMMY", "Views", "", "", "",
				// dBaseName);
				// DBTreeNodeK dummyProcsHead = new DBTreeNodeK("DUMMY", "Procedures", "", "",
				// "",
				// dBaseName);
				DefaultMutableTreeNode dummyNodeT = new DefaultMutableTreeNode(dummyTablesHead);
				// DefaultMutableTreeNode dummyNodeV = new
				// DefaultMutableTreeNode(dummyViewsHead);
				// DefaultMutableTreeNode dummyNodeP = new
				// DefaultMutableTreeNode(dummyProcsHead);
				auxNode.add(dummyNodeT);
				// auxNode.add(dummyNodeV);
				// auxNode.add(dummyNodeP);

			}

			// Next, we read drivers
			/*
			 * newHeadNode = new DBTreeNodeK(); newHeadNode.setCategory("DRIVERHEAD");
			 * newHeadNode.setText("Drivers"); partialHead = new
			 * DefaultMutableTreeNode(newHeadNode); root.add(partialHead);
			 * 
			 * sql = "SELECT * from DRIVERLIST"; rs = stmt.executeQuery(sql); while
			 * (rs.next()) { String redDriver = rs.getString("DRVNAME"); nodeInfo = new
			 * DBTreeNodeK(); nodeInfo.setCategory("DRIVER"); nodeInfo.setText(redDriver);
			 * partialHead.add(new DefaultMutableTreeNode(nodeInfo)); }
			 * 
			 * // Next, we read connections newHeadNode = new DBTreeNodeK();
			 * newHeadNode.setCategory("CONNHEAD"); newHeadNode.setText("Connections");
			 * partialHead = new DefaultMutableTreeNode(newHeadNode); root.add(partialHead);
			 * 
			 * sql = "SELECT * from CONNLIST"; rs = stmt.executeQuery(sql); while
			 * (rs.next()) { String redConn = rs.getString("DISPLAYNAME"); nodeInfo = new
			 * DBTreeNodeK(); nodeInfo.setCategory("CONN"); nodeInfo.setText(redConn);
			 * partialHead.add(new DefaultMutableTreeNode(nodeInfo)); }
			 */

		} catch (SQLException ex) {
			System.out.println("SQL Exception " + ex);
		} // end try
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			stmt.close();
			conn.close();
		} catch (Exception e) {
		}
		return (root);
	}

	public static boolean isNumeric(String str) {
		return (str.matches("[+-]?\\d*(\\.\\d+)?") && str.equals("") == false);
	}

	public static void loadHiddenDataTypes(Connection conn) {
		try {

			// private Object[][] hiddenTypesTable = {};
			// private Map rowSearch = new HashMap();

			DatabaseMetaData dbmd = conn.getMetaData();
			ResultSet rset;
			rset = dbmd.getTypeInfo();
			int counter = 0;

			while (rset.next()) {
				Object[] tipo = new Object[18];
				tipo[0] = rset.getString("TYPE_NAME");
				rowSearch.put(tipo[0].toString(), counter);

				tipo[1] = rset.getInt("DATA_TYPE");
				tipo[2] = rset.getInt("PRECISION");
				tipo[3] = rset.getString("LITERAL_PREFIX");
				tipo[4] = rset.getString("LITERAL_SUFFIX");
				tipo[5] = rset.getString("CREATE_PARAMS");
				tipo[6] = rset.getShort("NULLABLE");
				tipo[7] = rset.getBoolean("CASE_SENSITIVE");
				tipo[8] = rset.getShort("SEARCHABLE");
				tipo[9] = rset.getBoolean("UNSIGNED_ATTRIBUTE");
				tipo[10] = rset.getBoolean("FIXED_PREC_SCALE");
				tipo[11] = rset.getBoolean("AUTO_INCREMENT");
				tipo[12] = rset.getString("LOCAL_TYPE_NAME");
				tipo[13] = rset.getShort("MINIMUM_SCALE");
				tipo[14] = rset.getShort("MAXIMUM_SCALE");
				tipo[15] = rset.getInt("SQL_DATA_TYPE");
				tipo[16] = rset.getInt("SQL_DATETIME_SUB");
				tipo[17] = rset.getInt("NUM_PREC_RADIX");
				hiddenTypesTable[counter] = tipo;
				counter++;
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void initTypesTitles() {

		colSearch.put("TYPE_NAME", 0);
		colSearch.put("DATA_TYPE", 1);
		colSearch.put("PRECISION", 2);
		colSearch.put("LITERAL_PREFIX", 3);
		colSearch.put("LITERAL_SUFFIX", 4);
		colSearch.put("CREATE_PARAMS", 5);
		colSearch.put("NULLABLE", 6);
		colSearch.put("CASE_SENSITIVE", 7);
		colSearch.put("SEARCHABLE", 8);
		colSearch.put("UNSIGNED_ATTRIBUTE", 9);
		colSearch.put("FIXED_PREC_SCALE", 10);
		colSearch.put("AUTO_INCREMENT", 11);
		colSearch.put("LOCAL_TYPE_NAME", 12);
		colSearch.put("MINIMUM_SCALE", 13);
		colSearch.put("MAXIMUM_SCALE", 14);
		colSearch.put("SQL_DATA_TYPE", 15);
		colSearch.put("SQL_DATETIME_SUB", 16);
		colSearch.put("NUM_PREC_RADIX", 17);

		typeConvert.put("BIGINT", "java.lang.Long");
		typeConvert.put("LONG VARCHAR FOR BIT DATA", "java.lang.Byte[]");
		typeConvert.put("VARCHAR () FOR BIT DATA", "java.lang.Byte[]");
		typeConvert.put("CHAR () FOR BIT DATA", "java.lang.Byte[]");
		typeConvert.put("LONG VARCHAR", "java.lang.String");
		typeConvert.put("CHAR", "java.lang.String");
		typeConvert.put("NUMERIC", "java.math.BigDecimal");
		typeConvert.put("DECIMAL", "java.math.BigDecimal");
		typeConvert.put("INTEGER", "java.lang.Integer");
		typeConvert.put("SMALLINT", "java.lang.Integer");
		typeConvert.put("FLOAT", "java.lang.Double");
		typeConvert.put("REAL", "java.lang.Float");
		typeConvert.put("DOUBLE", "java.lang.Double");
		typeConvert.put("VARCHAR", "java.lang.String");
		typeConvert.put("BOOLEAN", "java.lang.Boolean");
		typeConvert.put("DATE", "java.sql.Date");
		typeConvert.put("TIME", "java.sql.Time");
		typeConvert.put("TIMESTAMP", "java.sql.Timestamp");
		typeConvert.put("BLOB", "java.sql.Blob");
		typeConvert.put("CLOB", "java.sql.Clob");
		typeConvert.put("XML", "java.sql.SQLXML");
	}

	public static Object dataTypeInfo(String colType, String colInfo) {

//		System.out.println(" row " + colType + " column " + colInfo + "  " + rowSearch.get(colInfo) + "  "
//				+ colSearch.get(colInfo));
		return hiddenTypesTable[rowSearch.get(colType)][colSearch.get(colInfo)];
	}

	/**
	 * Obtains the column class statically.
	 *
	 * @param columnType,
	 *            SQL type.
	 *
	 * @param columnTypeName,
	 *            Name of the column type.
	 * @ @return Class<?>.
	 * 
	 */
	public static Class<?> getColClass(int columnType, String columnTypeName) {

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

		case Types.BOOLEAN:
			return Boolean.class;

		case Types.SQLXML:
			return java.sql.SQLXML.class;

		default:
			System.out.println("Error in columnType: " + columnType + " " + columnTypeName);
			return Object.class;
		}
	}
}
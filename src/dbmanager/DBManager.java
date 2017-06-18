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
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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

public class DBManager implements Tool {

	Base base;
	DBGUIFrame frame;
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
	public static String renderProp;

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
				int optionButtons = JOptionPane.YES_NO_OPTION;
				int dialogResult = JOptionPane.showConfirmDialog(null, "Is this your first time with DBManager?",
						"Warning", optionButtons);
				if (dialogResult == JOptionPane.YES_OPTION) {
					firstTime = true;
					dirWasCreated = dirToTest.mkdirs();
				} else {
					JOptionPane.showConfirmDialog(null, "Fatal Error");
					System.exit(0);
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

			if (derbySystemHome == null || derbySystemHome.equals("")) {
				derbySystemHome = processingPath + "/data";
				propsDBM.setDBMProp("derby.system.home", derbySystemHome);
				propsDBM.saveProperties();
				System.setProperty("derby.system.home", derbySystemHome);
			}

			dBtree = new JTree();
			dBtree.setRowHeight(Integer.parseInt(propsDBM.getDBMProp("treerowsheight")));

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
						JTree tree = (JTree) event.getSource();
						DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree
								.getLastSelectedPathComponent();
						if (selectedNode != null) {
							DBTreeNodeK nodeInfo = (DBTreeNodeK) selectedNode.getUserObject();
							frame.getTxtSelected().setText(nodeInfo.getdBaseName());
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

					int row = dBtree.getClosestRowForLocation(e.getX(), e.getY());
					dBtree.setSelectionRow(row);

					if (SwingUtilities.isRightMouseButton(e)) {
						popup = new JPopupMenu();
						dBtree.add(popup);
						ActionListener menuListener = new ActionListener() {
							public void actionPerformed(ActionEvent event) {
								dBfactory = new DBFactory(event, dBtree.getSelectionPath());
								// To refresh the tree, rebuilds the tree
								// model.
								treeDataModel = new DefaultTreeModel(getTreeModel());
								dBtree.setModel(treeDataModel);
								dBtree.repaint();
							}
						};

						DefaultMutableTreeNode node = (DefaultMutableTreeNode) treePath.getLastPathComponent();

						if (node == null)
							return;

						DBTreeNodeK nodeInfo = (DBTreeNodeK) node.getUserObject();
						JMenuItem menuItem;
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
							popup.add(menuItem = new JMenuItem("Delete Database..."));
							if (nodeInfo.getText().equals(DBConnect.DBMSYSTABLE)) { // Take care not delete sys database
								menuItem.setEnabled(false);
							} else {
								menuItem.addActionListener(menuListener);
							}
							popup.add(menuItem = new JMenuItem("Unregister Database..."));
							if (nodeInfo.getText().equals(DBConnect.DBMSYSTABLE)) {
								menuItem.setEnabled(false);
							} else {
								menuItem.addActionListener(menuListener);
								popup.addSeparator();
								popup.add(menuItem = new JMenuItem("Create Table..."));
								menuItem.addActionListener(menuListener);
							}

							break;

						case "HEAD":
							if (nodeInfo.getText().equals("Tables")) {
								popup.add(menuItem = new JMenuItem("Create Table..."));
							} else {
								if (nodeInfo.getText().equals("Columns")) {
									popup.add(menuItem = new JMenuItem("Add column..."));

								} else {
									if (nodeInfo.getText().equals("Indices")) {
										popup.add(menuItem = new JMenuItem());

									} else {
										popup.add(menuItem = new JMenuItem("Error Jtree"));

									}

								}
							}

							menuItem.addActionListener(menuListener);
							break;

						case "TABLE":
							popup.add(menuItem = new JMenuItem("Manage Data..."));
							menuItem.addActionListener(menuListener);

							popup.add(menuItem = new JMenuItem("Add Column..."));
							menuItem.addActionListener(menuListener);

							popup.add(menuItem = new JMenuItem("Execute Command..."));
							menuItem.addActionListener(menuListener);
							popup.addSeparator();
							popup.add(menuItem = new JMenuItem("Delete"));
							menuItem.addActionListener(menuListener);

							popup.add(menuItem = new JMenuItem("Grab Structure..."));
							menuItem.addActionListener(menuListener);

							popup.add(menuItem = new JMenuItem("Recreate Table..."));
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
							}
							break;
						default:
							break;
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

		DBTreeNodeK nodeInfo = new DBTreeNodeK("root", "Databases");
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
			// Check that DBLIST table exists
			DatabaseMetaData dmd = conn.getMetaData();
			ResultSet rs = dmd.getTables(null, "APP", "DBLIST", null);
			if (!rs.next()) {
				System.out.println("SysTable creation when reswasnull");
				DBConnect.createSysDB(conn, pathToDBManager);
			} else {

				System.out.println("SysTable exists detected." + rs.toString());
			}

		} catch (SQLException ex) {
			System.out.println("SysTable detection failed.");
			ex.printStackTrace();
			return null;
		}

		try {
			stmt = conn.createStatement();

			String sql = "SELECT * from DBLIST";
			ResultSet rs = stmt.executeQuery(sql);
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

}
package dbmanager;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

public class DBFactory {

	public DBFactory() {

	}

	public DBFactory(ActionEvent event, TreePath treePath) {

		switch (event.getActionCommand().toString()) {
		case "Create Database...":
			callDBCreationDialog(treePath);
			// DBManager.dBtree.setModel(DBManager.treeDataModel);
			break;

		case "Register Database...":

			break;

		case "Delete Database...":
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) treePath.getLastPathComponent();
			DBTreeNodeK nodeInfo = (DBTreeNodeK) node.getUserObject();

			// In Java DB, a database is just a directory with particular contents
			String sDirectory = nodeInfo.getPathLocation() + "/" + nodeInfo.getText();
			JDialog.setDefaultLookAndFeelDecorated(true);

			int response = JOptionPane.showConfirmDialog(null,
					"You are going to delete the database " + sDirectory
							+ ". \n All the data stored there will be lost.\n" + "Do you want to continue?",
					"Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

			if (response == JOptionPane.YES_OPTION) {
				try {
					deleteDBDir(new File(sDirectory));
					System.out.println("DB " + sDirectory + " has been deleted.");
					unregisterDB((DefaultMutableTreeNode) DBManager.dBtree.getLastSelectedPathComponent(), true);
				} catch (Exception h) {
					System.out.println("It was not possible to delete " + sDirectory + " DB.");
					h.printStackTrace();
				}
			}
			break;

		case "Unregister Database...":
			unregisterDB((DefaultMutableTreeNode) DBManager.dBtree.getLastSelectedPathComponent(), false);
			break;

		case "Create Table...":

			try {
				TableCreaDialog dialog = new TableCreaDialog();
				dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
				dialog.setLocationRelativeTo(null);
				dialog.setVisible(true);

			} catch (Exception h) {
				h.printStackTrace();
			}

			break;
		case "Delete Table...":
			break;
		case "Add column...":
			break;
		case "Add index...":
			break;
		case "Error Jtree":
			break;
		case "Refresh":
			// ((DefaultTreeModel) DBManager.dBtree.getModel()).reload();
			DBManager.dBtree.repaint();
			break;
		case "Properties":
			break;
		default:
			DBManager.dBtree.repaint();
			break;

		}
	}

	public static void callDBCreationDialog(TreePath treePath) {
		try {
			DBCreationDialog dialog = new DBCreationDialog("", "", "", null, DBManager.derbySystemHome, treePath);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.getTxtDBLocation().setText(DBManager.derbySystemHome);
			dialog.setLocationRelativeTo(null);
			dialog.setVisible(true);
		} catch (Exception f) {
			f.printStackTrace();
		}
	}

	/**
	 * Deletes the given path and, if it is a directory, deletes all its children.
	 */

	public static void deleteDBDir(File element) {
		if (element.isDirectory()) {
			for (File sub : element.listFiles()) {
				deleteDBDir(sub);
			}
		}
		element.delete();
	}

	// public boolean deleteWithChildren(String path) {
	// File file = new File(path);
	// if (!file.exists()) {
	// return true;
	// }
	// if (!file.isDirectory()) {
	// return file.delete();
	// }
	// return this.deleteChildren(file) && file.delete();
	// }
	//
	// private boolean deleteChildren(File dir) {
	// File[] children = dir.listFiles();
	// boolean childrenDeleted = true;
	// for (int i = 0; children != null && i < children.length; i++) {
	// File child = children[i];
	// if (child.isDirectory()) {
	// childrenDeleted = this.deleteChildren(child) && childrenDeleted;
	// }
	// if (child.exists()) {
	// childrenDeleted = child.delete() && childrenDeleted;
	// }
	// }
	// return childrenDeleted;
	// }

	public static void unregisterDB(DefaultMutableTreeNode selectedElement, boolean callFromDel) {

		DBTreeNodeK nodeInfo = (DBTreeNodeK) selectedElement.getUserObject();
		String name2Del = nodeInfo.getText();
		String category2Del = nodeInfo.getCategory();
		String path2Del = nodeInfo.getPathLocation();
		int dialogResult = JOptionPane.NO_OPTION;
		Connection sysConn = null;

		if (!callFromDel) {
			int optionButtons = JOptionPane.YES_NO_OPTION;
			dialogResult = JOptionPane.showConfirmDialog(null, "Are you sure to unregister that element?", "Warning",
					optionButtons);
		}
		if (callFromDel || dialogResult == JOptionPane.YES_OPTION) {

			try {
				sysConn = DBConnect.connect(!DBConnect.serverIsOn, DBManager.pathToDBManager, "", null, false);
			} catch (Exception ey) {

				// If we can not connect and the server is off, lets try with server on.
				if (!DBConnect.serverIsOn) {
					try {
						DBConnect.inicServer();
						DBGUIFrame.getMnServer().setForeground(Color.GREEN);
						sysConn = DBConnect.connect(!DBConnect.serverIsOn, DBManager.pathToDBManager, "", null, false);

					} catch (Exception ex) {
						JOptionPane.showMessageDialog(null, "Database not available.", "Error",
								JOptionPane.ERROR_MESSAGE);
						ex.printStackTrace();
					}
				} else {
					System.out.println("The database " + DBManager.pathToDBManager + " is not available.");
					ey.printStackTrace();
				}

			}

			try {
				DBManager.stmt = sysConn.createStatement();
				String Deletion = "";

				switch (category2Del) {
				case "Java DB":
					Deletion = "DELETE FROM DBLIST WHERE " + "FILEPATH = \'" + path2Del + "\' AND DBNAME = \'"
							+ name2Del + "\'";

					break;

				// case "Drivers":
				// Deletion = "DELETE FROM DRIVERLIST WHERE "
				// + "DRVFILE = \'" + path2Del + "\' AND DRVNAME = \'" + name2Del + "\'";
				//
				// break;
				//
				// default:
				// Deletion = "DELETE FROM CONNLIST WHERE "
				// + "DISPLAYNAME = \'" + name2Del + "\'";
				//
				// break;
				}

				DBManager.stmt.executeUpdate(Deletion);

				DefaultMutableTreeNode node = selectedElement;
				DefaultTreeModel model = (DefaultTreeModel) (DBManager.dBtree.getModel());
				model.removeNodeFromParent(node);
				((DefaultTreeModel) DBManager.dBtree.getModel()).reload();

			} catch (Exception ey) {
				System.out.println("Error when element deletion.");
				ey.printStackTrace();
			}

		}

	}

	public static void loadComboCols(ResultSet rset, MyColumnTypes tipo) throws SQLException {

		while (rset.next()) {
			tipo = new MyColumnTypes();
			tipo.TYPE_NAME = rset.getString("TYPE_NAME");
			tipo.DATA_TYPE = rset.getInt("DATA_TYPE");
			tipo.PRECISION = rset.getInt("PRECISION");
			tipo.LITERAL_PREFIX = rset.getString("LITERAL_PREFIX");
			tipo.LITERAL_SUFFIX = rset.getString("LITERAL_SUFFIX");
			tipo.CREATE_PARAMS = rset.getString("CREATE_PARAMS");
			tipo.NULLABLE = rset.getShort("NULLABLE");
			tipo.CASE_SENSITIVE = rset.getBoolean("CASE_SENSITIVE");
			tipo.SEARCHABLE = rset.getShort("SEARCHABLE");
			tipo.UNSIGNED_ATTRIBUTE = rset.getBoolean("UNSIGNED_ATTRIBUTE");
			tipo.FIXED_PREC_SCALE = rset.getBoolean("FIXED_PREC_SCALE");
			tipo.AUTO_INCREMENT = rset.getBoolean("AUTO_INCREMENT");
			tipo.LOCAL_TYPE_NAME = rset.getString("LOCAL_TYPE_NAME");
			tipo.MINIMUM_SCALE = rset.getShort("MINIMUM_SCALE");
			tipo.MAXIMUM_SCALE = rset.getShort("MAXIMUM_SCALE");
			tipo.SQL_DATA_TYPE = rset.getInt("SQL_DATA_TYPE");
			tipo.SQL_DATETIME_SUB = rset.getInt("SQL_DATETIME_SUB");
			tipo.NUM_PREC_RADIX = rset.getInt("NUM_PREC_RADIX");
			TableCreaDialog.myComboModel.insertItem(tipo);

		}
	}

}

package dbmanager;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.apache.derby.tools.dblook;

public class DBFactory {

	public DBFactory(ActionEvent event, TreePath treePath, DBGUIFrame frame) {

		String actCommand = event.getActionCommand().toString();
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) treePath.getLastPathComponent();
		DBTreeNodeK nodeInfo = (DBTreeNodeK) node.getUserObject();

		switch (actCommand) {
		case "Copy name": // Copy the node name in the clipboard

			StringSelection stringSelection = new StringSelection(nodeInfo.getText());
			Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
			clpbrd.setContents(stringSelection, null);
			break;

		case "Create Database...":
		case "Register Database...":

			try {
				DBCreationDialog dialog = new DBCreationDialog("", "", "", null, DBManager.derbySystemHome, actCommand);
				// DB Name, User, Pwd, Description, DB Location (File Location), Action command
				dialog.getTxtDBLocation().setText(DBManager.derbySystemHome);
				dialog.setLocationRelativeTo(null);

				if (actCommand.equals("Register Database...")) {
					dialog.button.doClick();
					dialog.setTitle("Java DB Database Registration");
				} else {
					dialog.setTitle("Java DB Database Creation");
				}
				dialog.setVisible(true);

			} catch (Exception f) {
				errorPrint(f);
			}

			break;

		case "Delete Database...":
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
				Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
				dialog.pack();
				dialog.setSize(screenSize.width * 2 / 3, screenSize.height * 2 / 3);
				dialog.setLocationRelativeTo(null);
				dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
				dialog.setVisible(true);

			} catch (Exception h) {
				h.printStackTrace();
			}

			break;

		case "Delete Table":
			String table2Manage = nodeInfo.getText();

			JDialog.setDefaultLookAndFeelDecorated(true);

			response = JOptionPane.showConfirmDialog(null,
					"You are going to delete the table " + table2Manage
							+ ". \n All the data stored there will be lost.\n" + "Do you want to continue?",
					"Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

			if (response == JOptionPane.YES_OPTION) {

				Connection conn = null;

				try {
					conn = DBConnect.connect(!DBConnect.serverIsOn,
							nodeInfo.getPathLocation() + "/" + nodeInfo.getdBaseName(), "", null, false);
				} catch (Exception ex) {
					// If we can not connect and the server is off, we repeat the try with server
					// on.
					if (!DBConnect.serverIsOn) {
						try {
							DBConnect.inicServer();
							DBGUIFrame.getMnServer().setForeground(Color.GREEN);
							conn = DBConnect.connect(!DBConnect.serverIsOn,
									nodeInfo.getPathLocation() + "/" + nodeInfo.getdBaseName(), "", null, false);
						} catch (Exception ey) {
							JOptionPane.showConfirmDialog(null, "The database " + nodeInfo.getPathLocation() + "/"
									+ nodeInfo.getdBaseName() + " is not available.");
							ex.printStackTrace();
						}
					} else {
						JOptionPane.showMessageDialog(null, "The database " + nodeInfo.getPathLocation() + "/"
								+ nodeInfo.getdBaseName() + " is not available.");
						ex.printStackTrace();
					}
				}

				if (conn != null) {
					try {
						String sql = "DROP TABLE " + table2Manage;
						Statement statement = conn.createStatement();
						int result = statement.executeUpdate(sql);
						JOptionPane.showMessageDialog(null, "Table " + table2Manage
								+ " has been droped from the database " + nodeInfo.getdBaseName());
					} catch (Exception h) {
						JOptionPane.showMessageDialog(null,
								"It was not possible to delete " + table2Manage + " table.");
						h.printStackTrace();
					} finally {
						try {
							if (conn != null && !conn.isClosed()) {
								conn.close();
							}
						} catch (SQLException e) {
							e.printStackTrace();
						}
					}

				}
			}

			break;

		case "Generate DDL...":
			JFileChooser chooser1 = new JFileChooser();
			chooser1.setCurrentDirectory(null);
			int returnVal1 = chooser1.showSaveDialog(chooser1);
			if (returnVal1 == JFileChooser.APPROVE_OPTION) {

				int reply = 0;

				File filePath = chooser1.getSelectedFile();
				if (filePath.exists()) {
					reply = JOptionPane.showConfirmDialog(frame,
							"There is a file with the same path\n" + "The contents of the file will be lost.\n"
									+ "Do you want to overwrite that file?",
							"File will be overwritten", JOptionPane.YES_NO_OPTION);
				}

				if (!filePath.exists() || reply == JOptionPane.YES_OPTION) {
					String table2Grab = nodeInfo.getText();
					String[] args = { "-d", "jdbc:derby:" + nodeInfo.getPathLocation() + "/" + nodeInfo.getdBaseName(),
							"-t", table2Grab, "-o", filePath.toString(), "-verbose", "-noview" };

					////////////////////////////////////////////////////////////////////////
					// DBLook options
					// -z schemaName Only objects with the specified schema are included in the DDL.
					// -t tableOne tableTwo ... Tables to which the DDL should be restricted.
					// -td Statement delimiter for SQL statements generated by dblook. Default
					// semmicolon (;).
					// -o filename File where the generated DDL is written. If not specified,
					// console (System.out).
					// -append Prevents overwriting the DDL output.
					// -verbose All errors and warnings (both SQL and internal) should be echoed
					// (System.err).
					// -noview Specifies that CREATE VIEW statements should not be generated
					////////////////////////////////////////////////////////////////////////

					try {
						new dblook(args);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

			break;

		case "Recreate Table...":
			JFileChooser chooser2 = new JFileChooser();
			// chooser2.setCurrentDirectory(null);
			int returnVal2 = chooser2.showOpenDialog(chooser2);
			if (returnVal2 == JFileChooser.APPROVE_OPTION) {

				File filePath = chooser2.getSelectedFile();
//				System.out.println("File: " + filePath.getName());

				try {
					BufferedReader in = new BufferedReader(new FileReader(filePath));
					frame.getExecSQLPanel().getTextPaneInSQL().read(in, filePath);
					in.close();
//					System.out.println(frame.getExecSQLPanel().getTextPaneInSQL().getText());
				} catch (IOException ex) {
					System.err.println("Open plaintext error: " + ex);
				}
			}

			break;
		case "Add column...":
			break;
		case "Manage data...":
			table2Manage = nodeInfo.getText();
			frame.getExecSQLPanel().getTextEditingElement().setText(table2Manage);
			frame.getExecSQLPanel().executeSQL("SELECT * FROM " + table2Manage, "MODE_FILL");

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
		String nameSelected = nodeInfo.getText();
		String categorySelected = nodeInfo.getCategory();
		String pathSelected = nodeInfo.getPathLocation();
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
				// if (!DBConnect.serverIsOn) {
				// try {
				// DBConnect.inicServer();
				// DBGUIFrame.getMnServer().setForeground(Color.GREEN);
				// sysConn = DBConnect.connect(!DBConnect.serverIsOn, DBManager.pathToDBManager,
				// "", null, false);
				//
				// } catch (Exception ex) {
				// JOptionPane.showMessageDialog(null, "Database not available.", "Error",
				// JOptionPane.ERROR_MESSAGE);
				// ex.printStackTrace();
				// }
				// } else {
				// System.out.println("The database " + DBManager.pathToDBManager + " is not
				// available.");
				// ey.printStackTrace();
				// }
				errorPrint(ey);
			}

			try {
				Statement sysStmt = sysConn.createStatement();
				String Deletion = "";

				switch (categorySelected) {
				case "Java DB":
					Deletion = "DELETE FROM DBLIST WHERE " + "FILEPATH = \'" + pathSelected + "\' AND DBNAME = \'"
							+ nameSelected + "\'";

					break;

				// case "Drivers":
				// Deletion = "DELETE FROM DRIVERLIST WHERE "
				// + "DRVFILE = \'" + pathSelected + "\' AND DRVNAME = \'" + nameSelected +
				// "\'";
				//
				// break;
				//
				// default:
				// Deletion = "DELETE FROM CONNLIST WHERE "
				// + "DISPLAYNAME = \'" + nameSelected + "\'";
				//
				// break;
				}

				sysStmt.executeUpdate(Deletion);

				DefaultMutableTreeNode node = selectedElement;
				DefaultTreeModel model = (DefaultTreeModel) (DBManager.dBtree.getModel());
				model.removeNodeFromParent(node);
				((DefaultTreeModel) DBManager.dBtree.getModel()).reload();

			} catch (Exception ey) {
				errorPrint(ey);
			}
		}
	}

	static void errorPrint(Throwable e) {
		if (e instanceof SQLException)
			SQLExceptionPrint((SQLException) e);
		else
			System.out.println("A non-SQL error: " + e.toString());
	}

	static void SQLExceptionPrint(SQLException sqle) {
		while (sqle != null) {
			System.out.println("\n---SQLException Caught---\n");
			System.out.println("SQLState:   " + (sqle).getSQLState());
			System.out.println("Severity: " + (sqle).getErrorCode());
			System.out.println("Message:  " + (sqle).getMessage());
			sqle.printStackTrace();
			sqle = sqle.getNextException();
		}
	}

}

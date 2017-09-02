package dbmanager;

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
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.apache.derby.tools.dblook;

public class DBFactory {

	public DBFactory(ActionEvent event, TreePath treePath, DBGUIFrame frame) {

		String actCommand = event.getActionCommand().toString();
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) treePath.getLastPathComponent();
		DBTreeNodeK nodeInfo = (DBTreeNodeK) node.getUserObject();
		Connection conn = null;
		DatabaseMetaData dbmd = null;
		Statement stmt = null;

		switch (actCommand) {
		case "Expand all":
			DBGUIFrame.expandAll();
			// DBManager.dBtree.updateUI();
			break;

		case "Collapse all":
			System.out.println("Entra 2");
			DBGUIFrame.collapseAll(DBManager.dBtree);
			DBManager.dBtree.updateUI();
			break;

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

				conn = null;

				try {
					conn = DBConnect.connect(true, nodeInfo.getPathLocation() + "/" + nodeInfo.getdBaseName(), "", null,
							false);
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(null, "The database " + nodeInfo.getPathLocation() + "/"
							+ nodeInfo.getdBaseName() + " is not available.");
					ex.printStackTrace();

				}

				if (conn != null) {
					try {
						String sql = "DROP TABLE " + table2Manage;
						Statement statement = conn.createStatement();
						int result = statement.executeUpdate(sql);
						JOptionPane.showMessageDialog(null, "Table " + table2Manage
								+ " has been droped from the database " + nodeInfo.getdBaseName());

						final TreeExpansionUtil expander = new TreeExpansionUtil(DBManager.dBtree);
						final String state = expander.getExpansionState();

						System.out.println(state);
						DBManager.dBtree.setModel(new DefaultTreeModel(DBManager.getTreeModel()));
						// Recover the expansion state
						expander.setExpansionState(state);
						DBManager.dBtree.updateUI();

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
				// System.out.println("File: " + filePath.getName());

				try {
					BufferedReader in = new BufferedReader(new FileReader(filePath));
					frame.getExecSQLPanel().getTextPaneInSQL().read(in, filePath);
					in.close();
					// System.out.println(frame.getExecSQLPanel().getTextPaneInSQL().getText());
				} catch (IOException ex) {
					System.err.println("Open plaintext error: " + ex);
				}
			}

			break;

		case "Add column...":
			AddColumnDlg colDialog = new AddColumnDlg();
			MyComboModel myComboModel = null;
			node = (DefaultMutableTreeNode) DBManager.dBtree.getLastSelectedPathComponent();
			nodeInfo = ((DBTreeNodeK) node.getUserObject());
			String tableName = nodeInfo.getText();
			String currDBPath = nodeInfo.getPathLocation();
			String currDBName = nodeInfo.getdBaseName();
			ArrayList<String> indices = new ArrayList<String>();
			String columnType = "";
			String columnName = "";

			try {
				conn = DBConnect.connect(true, currDBPath + "/" + currDBName, "", null, false);

				// Data for the data type combo
				dbmd = conn.getMetaData();

				ResultSet rset = dbmd.getTypeInfo();

				myComboModel = loadComboCols(rset);
				colDialog.getComboType().setModel(myComboModel);
				rset.close();
				colDialog.pack();
				Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
				colDialog.setSize(screenSize.width * 2 / 4, screenSize.height * 2 / 3);
				colDialog.setLocationRelativeTo(null);
				colDialog.getComboType().setSelectedIndex(0);
				colDialog.getComboType().setEnabled(true);
				colDialog.setVisible(true);
			} catch (Exception ex) {
				colDialog.setEnabled(false);
				ex.printStackTrace();
			}

			// Return from the column dialog
			if (colDialog.result == 0) {
				String autoIncText = "";
				String foreignKeyText = "";

				// We create the DDL sentence from the Add Column dialog.
				String sql = "ALTER TABLE APP." + tableName + " ADD COLUMN " + colDialog.getTextName().getText() + " ";

				columnType = colDialog.getComboType().getSelectedItem().toString();

				int colSize = (int) colDialog.getTextSize().getValue(); // Size
				int colScale = (int) colDialog.getTextScale().getValue(); // Scale
				int dataType = (int) DBManager.dataTypeInfo(columnType, "DATA_TYPE");
				Object objParams = DBManager.dataTypeInfo(columnType, "CREATE_PARAMS");

				if (objParams != null) {
					String params = objParams.toString();
					if (params.contains("length") || params.contains("precision")) {
						if (dataType == -3 || dataType == -4) {
							columnType.replaceFirst("()", "(" + colSize + ")");
							if (params.contains("scale")) {
								columnType.replaceFirst(")", ", " + colScale // Scale
										+ ")");
							}

						} else {
							columnType += "(" + colSize;
							if (params.contains("scale")) {
								columnType += ", " + colScale; // Scale
							}
							columnType += ")";
						}
					}
				}

				String defValue = colDialog.getTxtDefValue().getText(); // Default value

				if (colDialog.getChkbxIndex().isSelected())
					indices.add(columnName);

				// Autoincrement
				if (colDialog.getChckbxAutoinc().isSelected() && autoIncText.equals("")) {
					autoIncText = " GENERATED " + colDialog.getComboGenerated().getSelectedItem(); // by default/always
					autoIncText += " AS IDENTITY ";

					int inicVal = Integer.parseInt(colDialog.getTxtInitValue().getText());
					int incrVal = Integer.parseInt(colDialog.getTextIncrement().getText());

					if (incrVal != 0) {
						autoIncText += "(START WITH " + inicVal + ", INCREMENT BY " + incrVal + ")";
					}
				}

				// Column level Foreign keys
				if (colDialog.getChkbxForeign().isSelected()) { // Foreign keys
					foreignKeyText = " CONSTRAINT " + colDialog.getTxtConstName().getText() + " REFERENCES "
							+ colDialog.getTxtRefTable().getText() + " (" + colDialog.getTxtColNames().getText() + ")"
							+ ((colDialog.getChkbxOnDelete().isSelected())
									? (" ON DELETE " + colDialog.getComboOnDelete().getSelectedItem().toString())
									: "")
							+ ((colDialog.getChkbxOnUpdate().isSelected())
									? (" ON UPDATE " + colDialog.getComboOnUpdate().getSelectedItem().toString())
									: "");

				}

				sql += columnType + ((defValue.equals("")) ? "" : (" DEFAULT " + defValue)) // Default
						+ ((colDialog.getChkbxNull().isSelected()) ? "" : " not NULL")
						+ ((colDialog.getChkbxPrimKey().isSelected()) ? " PRIMARY KEY "
								: (colDialog.getChkbxUnique().isSelected()) ? " UNIQUE " : "")
						+ ((colDialog.getChckbxAutoinc().isSelected()) // Autoincrement
								? autoIncText
								: "")
						+ ((colDialog.getChkbxForeign().isSelected()) // Foreign key
								? foreignKeyText
								: "");

				// sql += ")";
				System.out.println(sql);
				try {
					stmt = conn.createStatement();
					stmt.executeUpdate(sql);
					JOptionPane.showMessageDialog(null,
							"Table " + tableName + " has been created in the database " + currDBName);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			break;
		case "Delete column":
			String columName = nodeInfo.getNodeText();
			String colTableName = nodeInfo.getdTypeName();
			String colDBName = nodeInfo.getdBaseName();

			System.out.println("getText: " + nodeInfo.getText() + "\n Category: " + nodeInfo.getCategory()
					+ "\n NodeText: " + nodeInfo.getNodeText() + "\n DataType: " + nodeInfo.getDataType()
					+ "\n dBaseName: " + nodeInfo.getdBaseName() + "\n dTypeName: " + nodeInfo.getdTypeName()
					+ "\n FullTypeDesc: " + nodeInfo.getFullTypeDesc());

			// etText: ALTERINSERT Columna
			// Category: COLUMN
			// NodeText: ALTERINSERT Columna
			// DataType: TABLE
			// dBaseName: Things Base de datos
			// dTypeName: TESTABLE Tabla
			// FullTypeDesc: LONG VARCHAR tipo columna

			JDialog.setDefaultLookAndFeelDecorated(true);
			response = JOptionPane.showConfirmDialog(null,
					"You are going to delete the column " + columName + "\n from the table " + colTableName
							+ " in the database " + colDBName + "\n Do you want to continue?",
					"Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

			if (response == JOptionPane.YES_OPTION) {

				conn = null;

				try {
					conn = DBConnect.connect(true, nodeInfo.getPathLocation() + "/" + colDBName, "", null, false);
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(null,
							"The database " + nodeInfo.getPathLocation() + "/" + colDBName + " is not available.");
					ex.printStackTrace();
				}

				if (conn != null) {
					try {
						String sql = "ALTER TABLE " + colTableName + " DROP " + columName;
						Statement statement = conn.createStatement();
						int result = statement.executeUpdate(sql);
//						JOptionPane.showMessageDialog(null, "Table " + colTableName
//								+ " has been droped from the database " + nodeInfo.getdBaseName());

						final TreeExpansionUtil expander = new TreeExpansionUtil(DBManager.dBtree);
						final String state = expander.getExpansionState();

						System.out.println(state);
						DBManager.dBtree.setModel(new DefaultTreeModel(DBManager.getTreeModel()));
						// Recover the expansion state
						expander.setExpansionState(state);
						DBManager.dBtree.updateUI();

					} catch (Exception h) {
						JOptionPane.showMessageDialog(null,
								"It was not possible to delete the column.");
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
		case "Manage data...":
			table2Manage = nodeInfo.getText();
			frame.getExecSQLPanel().getTextEditingElement().setText(table2Manage);
			frame.getExecSQLPanel().executeSQL("SELECT * FROM " + table2Manage, "MODE_FILL");

			break;
		case "Error Jtree":
			break;
		case "Refresh":
			// ((DefaultTreeModel) DBManager.dBtree.getModel()).reload();
			// DBManager.dBtree.repaint();
			break;
		case "Properties":
			break;
		default:

			// DBManager.dBtree.repaint();

		}

		DBManager.dBtree.updateUI();
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

	public static MyComboModel loadComboCols(ResultSet rset) throws SQLException {

		MyComboModel myComboModel = new MyComboModel();
		MyColumnTypes tipo;

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
			myComboModel.insertItem(tipo);
		}

		return myComboModel;
	}

	public static void expandAll(JTree tree, TreePath path) {
		int actRow = tree.getRowForPath(path);
		for (int i = 0; i < tree.getRowCount(); i++) {
			tree.expandRow(i);
		}
	}

	public static void collapseAll(JTree tree) {
		int row = tree.getRowCount() - 1;
		while (row >= 0) {
			tree.collapseRow(row);
			row--;
		}
	}
}

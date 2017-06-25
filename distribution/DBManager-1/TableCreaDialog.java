package dbmanager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.Cursor;

public class TableCreaDialog extends JDialog {

	private JTextField txtTablename;
	private JTable table;
	private Connection conn = null;
	private DatabaseMetaData dbmd = null;
	private Statement stmt = null;
	private MyColumnTypes tipo;
	public static MyComboModel myComboModel;
	public AddColumnDlg dialog;
	public DefaultMutableTreeNode currentNode;
	public DBTreeNodeK currentNodeInfo;

	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmssSSS");
	private JButton btnEdit;

	public TableCreaDialog() {
		jbInit();
	}

	private void jbInit() {
		currentNode = (DefaultMutableTreeNode) DBManager.dBtree.getLastSelectedPathComponent();
		currentNodeInfo = ((DBTreeNodeK) currentNode.getUserObject());

		String currDBPath;
		String currDBName;
		String currCategory;

		// if (currentNodeInfo.getCategory().equals("Java DB")) {
		// We are in the database node. We can obtain data here
		currDBPath = currentNodeInfo.getPathLocation();
		currDBName = currentNodeInfo.getdBaseName();
		currCategory = currentNodeInfo.getCategory();
		// } else {

		// We are in the "Tables" head node. We need data from parent
		// DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode)
		// currentNode.getParent();
		// DBTreeNodeK parentNodeInfo = ((DBTreeNodeK) parentNode.getUserObject());
		// currDBPath = parentNodeInfo.getPathLocation();
		// currDBName = parentNodeInfo.getText();
		// currCategory = currentNodeInfo.getCategory();
		// }
		setTitle("Database " + currDBName + " Table Creation");

		try {
			conn = DBConnect.connect(!DBConnect.serverIsOn, currDBPath + "/" + currDBName, "", null, false);
		} catch (Exception ex) {

			// If we can not connect and the server is off, we repeat the try with server
			// on.
			if (!DBConnect.serverIsOn) {
				try {
					DBConnect.inicServer();
					DBGUIFrame.getMnServer().setForeground(Color.GREEN);
					conn = DBConnect.connect(!DBConnect.serverIsOn, currDBPath + "/" + currDBName, "", null, false);

				} catch (Exception ey) {
					JOptionPane.showMessageDialog(this, "Database not available.", "Error", JOptionPane.ERROR_MESSAGE);
					ex.printStackTrace();
				}
			} else {
				System.out.println("The database " + currDBPath + "/" + currDBName + " is not available.");
				ex.printStackTrace();
			}
		}

		setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/data/DBM4P3-32.png")));
		setMinimumSize(new Dimension(600, 400));
		setMaximumSize(new Dimension(3000, 3000));
		setModal(true);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		BorderLayout borderLayout = (BorderLayout) getContentPane().getLayout();

		JToolBar toolBar = new JToolBar();
		getContentPane().add(toolBar, BorderLayout.NORTH);

		JLabel lblNewLabel = new JLabel("Table name: ");
		toolBar.add(lblNewLabel);

		txtTablename = new JTextField();
		txtTablename.setText("Untitled");
		toolBar.add(txtTablename);
		txtTablename.setColumns(10);

		table = new JTable();
		table.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		table.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (table.getSelectedRow() > -1) {
					btnEdit.setEnabled(true);
				} else {
					btnEdit.setEnabled(false);
				}
			}
		});
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if (table.getSelectedRow() > -1) {
					btnEdit.setEnabled(true);
				} else {
					btnEdit.setEnabled(false);
				}
			}
		});
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setModel(new DefaultTableModel(new Object[][] {}, new String[] { "Key", "Unique", "Null", "Index",
				"Column name", "Data type", "Size", "Scale", "Default value", "Check", "Check constrains" }) {
			Class[] columnTypes = new Class[] { Boolean.class, Boolean.class, Boolean.class, Boolean.class,
					String.class, Object.class, Integer.class, Integer.class, String.class, Boolean.class,
					String.class };

			public Class getColumnClass(int columnIndex) {
				return columnTypes[columnIndex];
			}
		});

		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setColumnSelectionAllowed(true);
		table.setCellSelectionEnabled(true);

		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setAutoscrolls(true);
		scrollPane.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		getContentPane().add(scrollPane, BorderLayout.CENTER);

		JPanel buttonsPanel = new JPanel();
		getContentPane().add(buttonsPanel, BorderLayout.EAST);
		GridBagLayout gbl_buttonsPanel = new GridBagLayout();
		gbl_buttonsPanel.columnWidths = new int[] { 90, 0 };
		gbl_buttonsPanel.rowHeights = new int[] { 35, 27, 27, 27, 27, 0, 0 };
		gbl_buttonsPanel.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
		gbl_buttonsPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		buttonsPanel.setLayout(gbl_buttonsPanel);

		JButton btnAddColumn = new JButton("Add Column");
		btnAddColumn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {

					// Data for the data type combo
					dbmd = conn.getMetaData();
					ResultSet rset = dbmd.getTypeInfo();
					dialog = new AddColumnDlg();
					dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					myComboModel = new MyComboModel();
					DBFactory.loadComboCols(rset, tipo);
					dialog.getComboType().setModel(myComboModel);
					rset.close();

					dialog.setLocationRelativeTo(null);
					dialog.getComboType().setSelectedIndex(0);
					dialog.getComboType().setEnabled(true);
					dialog.setVisible(true);

				} catch (Exception e) {
					dialog.setEnabled(false);
					e.printStackTrace();
				}

				if (dialog.result == 0) {
					int numCols = table.getModel().getColumnCount();

					Object[] fila = new Object[numCols];
					fila[0] = dialog.getChkbxPrimKey().isSelected();
					fila[1] = dialog.getChkbxUnique().isSelected();
					fila[2] = dialog.getChkbxNull().isSelected();
					fila[3] = dialog.getChkbxIndex().isSelected();
					fila[4] = dialog.getTextName().getText();
					fila[5] = dialog.getComboType().getSelectedItem();
					fila[6] = dialog.getTextSize().getValue();
					fila[7] = dialog.getTextScale().getValue();
					fila[8] = dialog.getTxtDefValue().getText();
					fila[9] = dialog.getChkbxCheck().isSelected();
					fila[10] = dialog.getTextCheck().getText();
					((DefaultTableModel) table.getModel()).addRow(fila);
				}

			}
		});
		btnAddColumn.setPreferredSize(new Dimension(90, 35));
		btnAddColumn.setMinimumSize(new Dimension(90, 35));
		btnAddColumn.setMaximumSize(new Dimension(125, 35));
		btnAddColumn.setAlignmentX(Component.CENTER_ALIGNMENT);

		GridBagConstraints gbc_btnAddColumn = new GridBagConstraints();
		gbc_btnAddColumn.anchor = GridBagConstraints.NORTHWEST;
		gbc_btnAddColumn.insets = new Insets(0, 0, 5, 0);
		gbc_btnAddColumn.gridx = 0;
		gbc_btnAddColumn.gridy = 0;
		buttonsPanel.add(btnAddColumn, gbc_btnAddColumn);

		btnEdit = new JButton("Edit");
		btnEdit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				int selectedRow = table.getSelectedRow();
				System.out.println(selectedRow);
				if (selectedRow > -1) {
					try {
						// Data for the data type combo
						dbmd = conn.getMetaData();
						ResultSet rset = dbmd.getTypeInfo();
						dialog = new AddColumnDlg();
						dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
						myComboModel = new MyComboModel();
						DBFactory.loadComboCols(rset, tipo);
						dialog.getComboType().setModel(myComboModel);
						rset.close();
						dialog.setLocationRelativeTo(null);

						dialog.getChkbxPrimKey().setSelected((boolean) table.getModel().getValueAt(selectedRow, 0));
						dialog.getChkbxUnique().setSelected((boolean) table.getModel().getValueAt(selectedRow, 1));
						dialog.getChkbxNull().setSelected((boolean) table.getModel().getValueAt(selectedRow, 2));
						dialog.getChkbxIndex().setSelected((boolean) table.getModel().getValueAt(selectedRow, 3));
						dialog.getTextName().setText((String) table.getModel().getValueAt(selectedRow, 4));
						dialog.getComboType().getModel().setSelectedItem(table.getModel().getValueAt(selectedRow, 5));
						dialog.getTextSize().setValue((int) table.getModel().getValueAt(selectedRow, 6));
						dialog.getTextScale().setValue((int) table.getModel().getValueAt(selectedRow, 7));
						dialog.getTxtDefValue().setText((String) table.getModel().getValueAt(selectedRow, 8));
						dialog.getChkbxCheck().setSelected((boolean) table.getModel().getValueAt(selectedRow, 9));
						dialog.getTextCheck().setText((String) table.getModel().getValueAt(selectedRow, 10));

						dialog.getComboType().setEnabled(true);
						dialog.setVisible(true);

					} catch (Exception e2) {
						dialog.setEnabled(false);
						e2.printStackTrace();
					}

					if (dialog.result == 0) {

						int numCols = table.getModel().getColumnCount();

						table.getModel().setValueAt(dialog.getChkbxPrimKey().isSelected(), selectedRow, 0);
						table.getModel().setValueAt(dialog.getChkbxUnique().isSelected(), selectedRow, 1);
						table.getModel().setValueAt(dialog.getChkbxNull().isSelected(), selectedRow, 2);
						table.getModel().setValueAt(dialog.getChkbxIndex().isSelected(), selectedRow, 3);
						table.getModel().setValueAt(dialog.getTextName().getText(), selectedRow, 4);
						table.getModel().setValueAt(dialog.getComboType().getModel().getSelectedItem(), selectedRow, 5);
						table.getModel().setValueAt(dialog.getTextSize().getValue(), selectedRow, 6);
						table.getModel().setValueAt(dialog.getTextScale().getValue(), selectedRow, 7);
						table.getModel().setValueAt(dialog.getTxtDefValue().getText(), selectedRow, 8);
						table.getModel().setValueAt(dialog.getChkbxCheck().isSelected(), selectedRow, 9);
						table.getModel().setValueAt(dialog.getTextCheck().getText(), selectedRow, 10);
					}
				}
			}
		});
		btnEdit.setPreferredSize(new Dimension(90, 35));
		btnEdit.setMinimumSize(new Dimension(90, 35));
		btnEdit.setMaximumSize(new Dimension(125, 35));
		btnEdit.setHorizontalTextPosition(SwingConstants.CENTER);
		btnEdit.setAlignmentX(Component.CENTER_ALIGNMENT);

		GridBagConstraints gbc_btnEdit = new GridBagConstraints();
		gbc_btnEdit.anchor = GridBagConstraints.WEST;
		gbc_btnEdit.fill = GridBagConstraints.VERTICAL;
		gbc_btnEdit.insets = new Insets(0, 0, 5, 0);
		gbc_btnEdit.gridx = 0;
		gbc_btnEdit.gridy = 1;
		buttonsPanel.add(btnEdit, gbc_btnEdit);

		JButton btnRemove = new JButton("Remove");
		btnRemove.setPreferredSize(new Dimension(90, 35));
		btnRemove.setMinimumSize(new Dimension(90, 35));
		btnRemove.setMaximumSize(new Dimension(125, 35));
		btnRemove.setAlignmentX(Component.CENTER_ALIGNMENT);
		GridBagConstraints gbc_btnRemove = new GridBagConstraints();
		gbc_btnRemove.anchor = GridBagConstraints.WEST;
		gbc_btnRemove.fill = GridBagConstraints.VERTICAL;
		gbc_btnRemove.insets = new Insets(0, 0, 5, 0);
		gbc_btnRemove.gridx = 0;
		gbc_btnRemove.gridy = 2;
		buttonsPanel.add(btnRemove, gbc_btnRemove);

		JButton btnMoveUp = new JButton("Move Up");
		btnMoveUp.setPreferredSize(new Dimension(90, 35));
		btnMoveUp.setMinimumSize(new Dimension(90, 35));
		btnMoveUp.setMaximumSize(new Dimension(125, 35));
		btnMoveUp.setAlignmentX(Component.CENTER_ALIGNMENT);
		GridBagConstraints gbc_btnMoveUp = new GridBagConstraints();
		gbc_btnMoveUp.anchor = GridBagConstraints.WEST;
		gbc_btnMoveUp.fill = GridBagConstraints.VERTICAL;
		gbc_btnMoveUp.insets = new Insets(0, 0, 5, 0);
		gbc_btnMoveUp.gridx = 0;
		gbc_btnMoveUp.gridy = 3;
		buttonsPanel.add(btnMoveUp, gbc_btnMoveUp);

		JButton btnMoveDown = new JButton("Move Down");
		btnMoveDown.setPreferredSize(new Dimension(90, 35));
		btnMoveDown.setMinimumSize(new Dimension(90, 35));
		btnMoveDown.setMaximumSize(new Dimension(125, 35));
		btnMoveDown.setAlignmentX(Component.CENTER_ALIGNMENT);
		GridBagConstraints gbc_btnMoveDown = new GridBagConstraints();
		gbc_btnMoveDown.insets = new Insets(0, 0, 5, 0);
		gbc_btnMoveDown.anchor = GridBagConstraints.WEST;
		gbc_btnMoveDown.fill = GridBagConstraints.VERTICAL;
		gbc_btnMoveDown.gridx = 0;
		gbc_btnMoveDown.gridy = 4;
		buttonsPanel.add(btnMoveDown, gbc_btnMoveDown);

		JPanel panel = new JPanel();

		getContentPane().add(panel, BorderLayout.SOUTH);
		panel.setLayout(new GridLayout(2, 1, 0, 0));

		JLabel lblNewLabel_1 = new JLabel("  New label");
		panel.add(lblNewLabel_1);

		JPanel panelOKCancel = new JPanel();
		panel.add(panelOKCancel);
		GridBagLayout gbl_panelOKCancel = new GridBagLayout();
		gbl_panelOKCancel.columnWidths = new int[] { 287, 90, 90, 95, 0 };
		gbl_panelOKCancel.rowHeights = new int[] { 29, 5 };
		gbl_panelOKCancel.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		gbl_panelOKCancel.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		panelOKCancel.setLayout(gbl_panelOKCancel);

		JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				dispose();
			}
		});

		JButton btnOK = new JButton("OK");
		btnOK.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// We are going to compound the create ddl sentence.
				// First we Test if the table already exists
				String tableName = txtTablename.getText();
				ResultSet rs;
				try {
					rs = dbmd.getTables(null, null, tableName, null);
					if (rs.next()) {
						System.out.println("the table already exists.");
					} else {
						stmt = conn.createStatement();

						// We create the DDL sentence from the Add Column dialog table.
						String sql = "CREATE TABLE " + tableName + " (";

						ArrayList<String> priKeys = new ArrayList<String>();
						ArrayList<String> uniques = new ArrayList<String>();
						ArrayList<String> indices = new ArrayList<String>();
						String columnName = "";
						String columnType = "";

						int tableNumRows = table.getModel().getRowCount();
						for (int i = 0; i < tableNumRows; i++) {
							columnName = (String) table.getModel().getValueAt(i, 4);
							columnType = (String) table.getModel().getValueAt(i, 5).toString();
							int elem6 = (int) table.getModel().getValueAt(i, 6); // Size

							if (columnType.indexOf('(') > -1) {
								columnType.replaceFirst("()", "(" + elem6 + ")");
							}

							int elem7 = (int) table.getModel().getValueAt(i, 7); // Scale
							String elem8 = (String) table.getModel().getValueAt(i, 8); // Default value

							if ((boolean) table.getModel().getValueAt(i, 0))
								priKeys.add(columnName);
							else if ((boolean) table.getModel().getValueAt(i, 1))
								uniques.add(columnName);
							else if ((boolean) table.getModel().getValueAt(i, 3))
								indices.add(columnName);

							sql += columnName + " " + columnType
									+ ((elem6 > 0 && columnType.indexOf('(') == -1) ? "(" + elem6 : "") // Size
									+ ((elem7 > 0) ? ("," + elem7 + ")")
											: ((elem6 > 0 && columnType.indexOf('(') == -1) ? ")" : "")) // Scale
									+ ((elem8.equals("")) ? "" : (" DEFAULT " + elem8)) // Default
									+ (((boolean) table.getModel().getValueAt(i, 2)) ? "" : " not NULL")
									+ (((boolean) table.getModel().getValueAt(i, 9)) // Constrains checkbox
											? (table.getModel().getValueAt(i, 10)) // Constrains area
											: "");

							if (i < tableNumRows - 1 || !priKeys.isEmpty()) {
								sql += ", ";
							} else {
								sql += ") ";
							}
						}

						if (!priKeys.isEmpty()) {

							sql += " PRIMARY KEY (" + priKeys.get(0);
							for (int i = 1; i < priKeys.size(); i++) {
								sql += ",\n" + priKeys.get(i);
							}

							sql += "))\n";
						}
						;

						System.out.println(sql);
						stmt.executeUpdate(sql);
						JOptionPane.showMessageDialog(btnOK,
								"Table " + tableName + " has been created in the database " + currDBName);

						if (!uniques.isEmpty()) {

							Timestamp timestamp = new Timestamp(System.currentTimeMillis());
							sql = "CREATE UNIQUE INDEX SQL" + sdf.format(timestamp) + " ON " + tableName + " ("
									+ uniques.get(0);

							for (int i = 1; i < uniques.size(); i++) {
								sql += ", " + uniques.get(i);
							}
							sql += ") ";
							System.out.println(sql);
							stmt.executeUpdate(sql);
							JOptionPane.showMessageDialog(btnOK,
									"UNIQUE INDEX has been created on the table " + tableName);
						}

						if (!indices.isEmpty()) {
							Timestamp timestamp = new Timestamp(System.currentTimeMillis());
							sql = "CREATE INDEX SQL" + sdf.format(timestamp) + " ON " + tableName + " ("
									+ indices.get(0);

							for (int i = 1; i < indices.size(); i++) {
								sql += ", " + indices.get(i);
							}

							sql += ") ";
							System.out.println(sql);
							stmt.executeUpdate(sql);
							JOptionPane.showMessageDialog(btnOK, "INDEX has been created on the table " + tableName);
						}
						setVisible(false);
					}
					rs.close();

				} catch (java.sql.SQLSyntaxErrorException synt) {
					JOptionPane.showMessageDialog(null, "Syntax error, check your inputs.", "Error",
							JOptionPane.ERROR_MESSAGE, null);

				} catch (SQLException se) {
					// Handle errors for JDBC
					se.printStackTrace();
				} catch (Exception e) {
					// Handle errors for Class.forName
					e.printStackTrace();

				} // end try
			}

		});

		btnOK.setMinimumSize(new Dimension(79, 29));
		btnOK.setMaximumSize(new Dimension(79, 29));
		btnOK.setPreferredSize(new Dimension(79, 29));

		GridBagConstraints gbc_btnOK = new GridBagConstraints();
		gbc_btnOK.anchor = GridBagConstraints.NORTH;
		gbc_btnOK.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnOK.insets = new Insets(0, 0, 0, 5);
		gbc_btnOK.gridx = 2;
		gbc_btnOK.gridy = 0;
		panelOKCancel.add(btnOK, gbc_btnOK);
		btnCancel.setMinimumSize(new Dimension(79, 29));
		btnCancel.setMaximumSize(new Dimension(79, 29));
		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.fill = GridBagConstraints.BOTH;
		gbc_btnCancel.gridx = 3;
		gbc_btnCancel.gridy = 0;
		panelOKCancel.add(btnCancel, gbc_btnCancel);
	}

	public static boolean isNumeric(String inputData) {
		Scanner sc = new Scanner(inputData);
		return sc.hasNextInt();

		// if (table.getSelectedRow() > -1) {
		// btnEdit.setEnabled(true);
		// } else {
		// btnEdit.setEnabled(false);
		// }
	}
}

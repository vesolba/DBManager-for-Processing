package dbmanager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
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

import javax.swing.BoxLayout;
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

public class TableCreaDialog extends JDialog {

	private JTextField txtTablename;
	private JTable table;

	private Connection conn = null;
	private DatabaseMetaData dbmd = null;
	private Statement stmt = null;
	private MyColumnTypes tipo;
	public static MyComboModel myComboModel;
	public AddColumnDlg colDialog;
	public DefaultMutableTreeNode currentNode;
	public DBTreeNodeK currentNodeInfo;

	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmssSSS");
	private JButton btnEdit;

	public TableCreaDialog() {

		jbInit();
	}

	@SuppressWarnings("serial")
	private void jbInit() {
		BorderLayout borderLayout = (BorderLayout) getContentPane().getLayout();
		borderLayout.setVgap(5);
		borderLayout.setHgap(5);
		currentNode = (DefaultMutableTreeNode) DBManager.dBtree.getLastSelectedPathComponent();
		currentNodeInfo = ((DBTreeNodeK) currentNode.getUserObject());

		String currDBPath = currentNodeInfo.getPathLocation();
		String currDBName = currentNodeInfo.getdBaseName();
		String currCategory = currentNodeInfo.getCategory();

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
		setModal(true);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		getContentPane().getLayout();

		JToolBar toolBar = new JToolBar();
		getContentPane().add(toolBar, BorderLayout.NORTH);

		JLabel lblNewLabel = new JLabel("Table name: ");
		toolBar.add(lblNewLabel);

		txtTablename = new JTextField();
		txtTablename.setText("Untitled");
		toolBar.add(txtTablename);
		txtTablename.setColumns(10);

		JPanel panel_1 = new JPanel();
		getContentPane().add(panel_1, BorderLayout.CENTER);

		table = new JTable();
		table.setPreferredScrollableViewportSize(new Dimension(0, 0));
		table.setIntercellSpacing(new Dimension(10, 5));

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
		panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.X_AXIS));
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		table.setModel(new DefaultTableModel(new Object[][] {}, new String[] { "Key", "Unique", "Null", "Index",
				"Column name", "Data type", "Size", "Scale", "Default value", "Check", "Check constrains" }) {
			Class[] columnTypes = new Class[] { Boolean.class, Boolean.class, Boolean.class, Boolean.class,
					String.class, Object.class, Integer.class, Integer.class, String.class, Boolean.class,
					String.class };

		});
		table.getColumnModel().getColumn(0).setPreferredWidth(100);
		table.getColumnModel().getColumn(0).setMinWidth(50);
		table.getColumnModel().getColumn(0).setMaxWidth(1000);
		table.getColumnModel().getColumn(1).setPreferredWidth(100);
		table.getColumnModel().getColumn(1).setMinWidth(50);
		table.getColumnModel().getColumn(1).setMaxWidth(1000);
		table.getColumnModel().getColumn(2).setPreferredWidth(100);
		table.getColumnModel().getColumn(2).setMinWidth(50);
		table.getColumnModel().getColumn(2).setMaxWidth(1000);
		table.getColumnModel().getColumn(3).setPreferredWidth(100);
		table.getColumnModel().getColumn(3).setMinWidth(50);
		table.getColumnModel().getColumn(3).setMaxWidth(1000);
		table.getColumnModel().getColumn(4).setPreferredWidth(150);
		table.getColumnModel().getColumn(4).setMinWidth(100);
		table.getColumnModel().getColumn(4).setMaxWidth(1000);
		table.getColumnModel().getColumn(5).setPreferredWidth(150);
		table.getColumnModel().getColumn(5).setMinWidth(100);
		table.getColumnModel().getColumn(5).setMaxWidth(1000);
		table.getColumnModel().getColumn(6).setPreferredWidth(100);
		table.getColumnModel().getColumn(6).setMinWidth(50);
		table.getColumnModel().getColumn(6).setMaxWidth(1000);
		table.getColumnModel().getColumn(7).setPreferredWidth(50);
		table.getColumnModel().getColumn(7).setMinWidth(20);
		table.getColumnModel().getColumn(7).setMaxWidth(1000);
		table.getColumnModel().getColumn(8).setPreferredWidth(100);
		table.getColumnModel().getColumn(8).setMinWidth(50);
		table.getColumnModel().getColumn(8).setMaxWidth(1000);
		table.getColumnModel().getColumn(9).setPreferredWidth(50);
		table.getColumnModel().getColumn(9).setMinWidth(20);
		table.getColumnModel().getColumn(9).setMaxWidth(1000);
		table.getColumnModel().getColumn(10).setPreferredWidth(300);
		table.getColumnModel().getColumn(10).setMinWidth(200);
		table.getColumnModel().getColumn(10).setMaxWidth(1000);

		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setColumnSelectionAllowed(true);

		JScrollPane scrollPane = new JScrollPane(table);
		panel_1.add(scrollPane);
		scrollPane.setAutoscrolls(true);
		scrollPane.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));

		JPanel buttonsPanel = new JPanel();
		buttonsPanel.setAlignmentY(Component.TOP_ALIGNMENT);
		panel_1.add(buttonsPanel);
		buttonsPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));

		JButton btnAddColumn = new JButton("Add Column");
		btnAddColumn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {

					// Data for the data type combo
					dbmd = conn.getMetaData();
					ResultSet rset = dbmd.getTypeInfo();

					colDialog = new AddColumnDlg();
					colDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					myComboModel = new MyComboModel();
					loadComboCols(rset, tipo);
					colDialog.getComboType().setModel(myComboModel);
					rset.close();

					colDialog.setLocationRelativeTo(null);
					colDialog.getComboType().setSelectedIndex(0);
					colDialog.getComboType().setEnabled(true);
					colDialog.setVisible(true);

				} catch (Exception e) {
					colDialog.setEnabled(false);
					e.printStackTrace();
				}

				// Return from the column dialog
				if (colDialog.result == 0) {
					// int numCols = table.getModel().getColumnCount();

					Object[] fila = new Object[11];
					fila[0] = colDialog.getChkbxPrimKey().isSelected();
					fila[1] = colDialog.getChkbxUnique().isSelected();
					fila[2] = colDialog.getChkbxNull().isSelected();
					fila[3] = colDialog.getChkbxIndex().isSelected();
					fila[4] = colDialog.getTextName().getText();
					fila[5] = colDialog.getComboType().getSelectedItem();
					fila[6] = colDialog.getTextSize().getValue();
					fila[7] = colDialog.getTextScale().getValue();
					fila[8] = colDialog.getTxtDefValue().getText();
					fila[9] = colDialog.getChkbxCheck().isSelected();
					fila[10] = colDialog.getTextCheck().getText();
					((DefaultTableModel) table.getModel()).addRow(fila);
				}

			}
		});
		buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.PAGE_AXIS));
		btnAddColumn.setPreferredSize(new Dimension(90, 35));
		btnAddColumn.setMinimumSize(new Dimension(90, 35));
		btnAddColumn.setMaximumSize(new Dimension(125, 35));
		btnAddColumn.setAlignmentX(Component.CENTER_ALIGNMENT);
		buttonsPanel.add(btnAddColumn);

		btnEdit = new JButton("Edit"); // Edit column
		btnEdit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				int selectedRow = table.getSelectedRow();
				System.out.println(selectedRow);
				if (selectedRow > -1) {
					try {
						// Data for the data type combo
						dbmd = conn.getMetaData();
						ResultSet rset = dbmd.getTypeInfo();
						colDialog = new AddColumnDlg();
						colDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
						myComboModel = new MyComboModel();
						loadComboCols(rset, tipo);
						colDialog.getComboType().setModel(myComboModel);
						rset.close();

						colDialog.setLocationRelativeTo(null);

						colDialog.getChkbxPrimKey().setSelected((boolean) table.getModel().getValueAt(selectedRow, 0));
						colDialog.getChkbxUnique().setSelected((boolean) table.getModel().getValueAt(selectedRow, 1));
						colDialog.getChkbxNull().setSelected((boolean) table.getModel().getValueAt(selectedRow, 2));
						colDialog.getChkbxIndex().setSelected((boolean) table.getModel().getValueAt(selectedRow, 3));
						colDialog.getTextName().setText((String) table.getModel().getValueAt(selectedRow, 4));
						colDialog.getComboType().getModel()
								.setSelectedItem(table.getModel().getValueAt(selectedRow, 5));
						colDialog.getTextSize().setValue((int) table.getModel().getValueAt(selectedRow, 6));
						colDialog.getTextScale().setValue((int) table.getModel().getValueAt(selectedRow, 7));
						colDialog.getTxtDefValue().setText((String) table.getModel().getValueAt(selectedRow, 8));
						colDialog.getChkbxCheck().setSelected((boolean) table.getModel().getValueAt(selectedRow, 9));
						colDialog.getTextCheck().setText((String) table.getModel().getValueAt(selectedRow, 10));

						colDialog.getComboType().setEnabled(true);
						colDialog.setVisible(true);

					} catch (Exception e2) {
						colDialog.setEnabled(false);
						e2.printStackTrace();
					}

					if (colDialog.result == 0) {

						table.getModel().setValueAt(colDialog.getChkbxPrimKey().isSelected(), selectedRow, 0);
						table.getModel().setValueAt(colDialog.getChkbxUnique().isSelected(), selectedRow, 1);
						table.getModel().setValueAt(colDialog.getChkbxNull().isSelected(), selectedRow, 2);
						table.getModel().setValueAt(colDialog.getChkbxIndex().isSelected(), selectedRow, 3);
						table.getModel().setValueAt(colDialog.getTextName().getText(), selectedRow, 4);
						table.getModel().setValueAt(colDialog.getComboType().getModel().getSelectedItem(), selectedRow,
								5);
						table.getModel().setValueAt(colDialog.getTextSize().getValue(), selectedRow, 6);
						table.getModel().setValueAt(colDialog.getTextScale().getValue(), selectedRow, 7);
						table.getModel().setValueAt(colDialog.getTxtDefValue().getText(), selectedRow, 8);
						table.getModel().setValueAt(colDialog.getChkbxCheck().isSelected(), selectedRow, 9);
						table.getModel().setValueAt(colDialog.getTextCheck().getText(), selectedRow, 10);
					}
				}
			}
		});
		btnEdit.setPreferredSize(new Dimension(90, 35));
		btnEdit.setMinimumSize(new Dimension(90, 35));
		btnEdit.setMaximumSize(new Dimension(125, 35));
		btnEdit.setHorizontalTextPosition(SwingConstants.CENTER);
		btnEdit.setAlignmentX(Component.CENTER_ALIGNMENT);
		buttonsPanel.add(btnEdit);

		JButton btnRemove = new JButton("Remove");
		btnRemove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				((DefaultTableModel) table.getModel()).removeRow(table.getSelectedRow());
			}
		});
		btnRemove.setPreferredSize(new Dimension(90, 35));
		btnRemove.setMinimumSize(new Dimension(90, 35));
		btnRemove.setMaximumSize(new Dimension(125, 35));
		btnRemove.setAlignmentX(Component.CENTER_ALIGNMENT);
		buttonsPanel.add(btnRemove);

		JButton btnMoveUp = new JButton("Move Up");
		btnMoveUp.setPreferredSize(new Dimension(90, 35));
		btnMoveUp.setMinimumSize(new Dimension(90, 35));
		btnMoveUp.setMaximumSize(new Dimension(125, 35));
		btnMoveUp.setAlignmentX(Component.CENTER_ALIGNMENT);
		buttonsPanel.add(btnMoveUp);

		JButton btnMoveDown = new JButton("Move Down");
		btnMoveDown.setPreferredSize(new Dimension(90, 35));
		btnMoveDown.setMinimumSize(new Dimension(90, 35));
		btnMoveDown.setMaximumSize(new Dimension(125, 35));
		btnMoveDown.setAlignmentX(Component.CENTER_ALIGNMENT);
		buttonsPanel.add(btnMoveDown);

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

		// btnOK.setMinimumSize(new Dimension(79, 29));
		// btnOK.setMaximumSize(new Dimension(79, 29));
		// btnOK.setPreferredSize(new Dimension(79, 29));

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

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		pack();
		setSize(screenSize.width * 2 / 5, screenSize.height * 2 / 5);
		setLocationRelativeTo(null);

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

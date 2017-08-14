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
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
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
	private JSpinner spinTabCrea = null;

	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmssSSS");
	public String autoIncText = "";
	public String foreignKeyText = "";

	public TableCreaDialog() {

		jbInit();
	}

	@SuppressWarnings("serial")
	private void jbInit() {
		getContentPane().setPreferredSize(new Dimension(500, 200));
		setPreferredSize(new Dimension(700, 400));
		setMinimumSize(new Dimension(500, 200));
		setMaximumSize(new Dimension(20000, 20000));
		getContentPane().setMaximumSize(new Dimension(20000, 20000));
		BorderLayout borderLayout = (BorderLayout) getContentPane().getLayout();
		borderLayout.setVgap(5);
		borderLayout.setHgap(5);
		currentNode = (DefaultMutableTreeNode) DBManager.dBtree.getLastSelectedPathComponent();
		currentNodeInfo = ((DBTreeNodeK) currentNode.getUserObject());

		String currDBPath = currentNodeInfo.getPathLocation();
		String currDBName = currentNodeInfo.getdBaseName();

		setTitle("Database " + currDBName + " Table Creation");

		try {
			conn = DBConnect.connect(!DBConnect.serverIsOn, currDBPath + "/" + currDBName, "", null, false);
		} catch (Exception ex) {

			System.out.println("The database " + currDBPath + "/" + currDBName + " is not available.");
			ex.printStackTrace();

		}

		setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/data/DBM4P3-32.png")));
		setModal(true);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		getContentPane().getLayout();

		// if (DBManager.propsDBM != null) {
		String propTable = DBManager.propsDBM.getDBMProp("spinTabCrea");
		if (propTable != null && !propTable.isEmpty() && table != null) {
			spinTabCrea.setValue(Integer.parseInt(propTable));
			table.setFont(table.getFont().deriveFont(Float.parseFloat(spinTabCrea.getValue().toString())));
			table.setRowHeight((int) Float.parseFloat(spinTabCrea.getValue().toString()) + 5);
		}

		// }
		table = new JTable() {
			public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
				Component c = super.prepareRenderer(renderer, row, column);

				// Alternate row color
				if (!isRowSelected(row))
					c.setBackground(row % 2 == 0 ? getBackground() : Color.LIGHT_GRAY);

				return c;
			}
		};

		table.setMinimumSize(new Dimension(100, 100));
		table.setMaximumSize(new Dimension(2000, 2000));
		table.setPreferredScrollableViewportSize(new Dimension(300, 200));
		table.setIntercellSpacing(new Dimension(10, 5));

		table.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		table.setModel(new DefaultTableModel(new Object[][] {},
				new String[] { "Key", "Unique", "Null", "Index", "Autoincrement", "Foreign Key", "Check", "Column name",
						"Data type", "Size", "Scale", "Default value", "Generated", "Start with", "Incr. by", "Cycle",
						"Const. name", "Ref. table", "Column names", "On del.", "On delete", "On upd.", "On update",
						"Cons.Check name ", "Check conditions" }) {
			Class[] colsClasses = new Class[] { Boolean.class, Boolean.class, Boolean.class, Boolean.class,
					Boolean.class, Boolean.class, Boolean.class, String.class, Object.class, Integer.class,
					Integer.class, String.class, String.class, Integer.class, Integer.class, Boolean.class,
					String.class, String.class, String.class, Boolean.class, String.class, Boolean.class, String.class,
					String.class, String.class };

			public Class getColumnClass(int columnIndex) {
				return colsClasses[columnIndex];
			}
		});

		int[] colsSizes = { 7, 7, 7, 7, 7, 7, 7, 30, 20, 10, 10, 10, 20, 10, 10, 7, 30, 20, 30, 7, 20, 7, 20, 20, 100 };

		for (int i = 0; i < 25; i++) {
			table.getColumnModel().getColumn(i).setMaxWidth(1000);
			table.getColumnModel().getColumn(i).setMinWidth(10);

			int colWidth = Math.max(colsSizes[i], table.getModel().getColumnName(i).length());
			table.getColumnModel().getColumn(i).setPreferredWidth(colWidth * 10);
		}

		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setPreferredSize(new Dimension(400, 100));
		scrollPane.setMaximumSize(new Dimension(20000, 20000));
		getContentPane().add(scrollPane, BorderLayout.CENTER);
		scrollPane.setAutoscrolls(true);
		scrollPane.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));

		JPanel panel = new JPanel();
		panel.setMaximumSize(new Dimension(10000, 1000));

		getContentPane().add(panel, BorderLayout.SOUTH);
		panel.setLayout(new GridLayout(2, 1, 0, 0));

		JLabel lblNewLabel_1 = new JLabel("  New label");
		panel.add(lblNewLabel_1);

		JPanel panelSaveCancel = new JPanel();
		panel.add(panelSaveCancel);
		GridBagLayout gbl_panelSaveCancel = new GridBagLayout();
		gbl_panelSaveCancel.columnWidths = new int[] { 90, 0 };
		gbl_panelSaveCancel.rowHeights = new int[] { 29, 5 };
		gbl_panelSaveCancel.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panelSaveCancel.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		panelSaveCancel.setLayout(gbl_panelSaveCancel);

		JPanel panel_1 = new JPanel();
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.insets = new Insets(0, 0, 10, 50);
		gbc_panel_1.anchor = GridBagConstraints.EAST;
		gbc_panel_1.gridx = 0;
		gbc_panel_1.gridy = 0;
		panelSaveCancel.add(panel_1, gbc_panel_1);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[] { 90, 95, 0 };
		gbl_panel_1.rowHeights = new int[] { 29, 0 };
		gbl_panel_1.columnWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		gbl_panel_1.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		panel_1.setLayout(gbl_panel_1);

		JButton btnSave = new JButton("Save");
		GridBagConstraints gbc_btnSave = new GridBagConstraints();
		gbc_btnSave.weightx = 1.0;
		gbc_btnSave.anchor = GridBagConstraints.EAST;
		gbc_btnSave.fill = GridBagConstraints.BOTH;
		gbc_btnSave.insets = new Insets(0, 0, 0, 5);
		gbc_btnSave.gridx = 0;
		gbc_btnSave.gridy = 0;
		panel_1.add(btnSave, gbc_btnSave);

		JButton btnCancel = new JButton("Cancel");
		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.fill = GridBagConstraints.BOTH;
		gbc_btnCancel.gridx = 1;
		gbc_btnCancel.gridy = 0;
		panel_1.add(btnCancel, gbc_btnCancel);
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				dispose();
			}
		});
		btnCancel.setMinimumSize(new Dimension(79, 29));
		btnCancel.setMaximumSize(new Dimension(79, 29));

		btnSave.addActionListener(new ActionListener() {
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

						// ArrayList<String> priKeys = new ArrayList<String>();
						ArrayList<String> indices = new ArrayList<String>();
						String columnName = "";
						String columnType = "";

						int tableNumRows = table.getModel().getRowCount();
						for (int i = 0; i < tableNumRows; i++) {
							columnName = (String) table.getModel().getValueAt(i, 7);
							columnType = (String) table.getModel().getValueAt(i, 8).toString();
							int colSize = (int) table.getModel().getValueAt(i, 9); // Size
							int colScale = (int) table.getModel().getValueAt(i, 10);
							int dataType = (int) DBManager.dataTypeInfo(columnType, "DATA_TYPE");
							String params = DBManager.dataTypeInfo(columnType, "CREATE_PARAMS").toString();

							if (params.contains("length") || params.contains("precision")) {
								if (dataType == -3 || dataType == -4) {
									columnType.replaceFirst("()", "(" + colSize + ")");
									if (params.contains("scale")) {
										columnType.replaceFirst(")", ", " + (int) table.getModel().getValueAt(i, 10) // Scale
												+ ")");
									}

								} else {
									columnType += "(" + colSize;
									if (params.contains("scale")) {
										columnType += ", " + (int) table.getModel().getValueAt(i, 10); // Scale
									}
									columnType += ")";
								}
							}

							String defValue = (String) table.getModel().getValueAt(i, 11); // Default value

							if ((boolean) table.getModel().getValueAt(i, 3))
								indices.add(columnName);

							// Autoincrement
							if ((boolean) table.getModel().getValueAt(i, 4) && autoIncText.equals("")) {
								autoIncText = " GENERATED " + table.getModel().getValueAt(i, 12); // by default/always
								autoIncText += " AS IDENTITY ";

								int inicVal = Integer.parseInt(table.getModel().getValueAt(i, 13).toString());
								int incrVal = Integer.parseInt(table.getModel().getValueAt(i, 14).toString());

								if (incrVal != 0) {
									autoIncText += "(START WITH " + inicVal + " INCREMENT BY " + incrVal
											+ (((boolean) table.getModel().getValueAt(i, 15)) ? ", cycle)" : ")");
								}
							}

							// Foreign keys
							if ((boolean) table.getModel().getValueAt(i, 5)) { // Foreign keys
								foreignKeyText = " CONSTRAINT " + table.getModel().getValueAt(i, 16) + " REFERENCES "
										+ table.getModel().getValueAt(i, 17) + " (" + table.getModel().getValueAt(i, 18)
										+ ")" + " ON DELETE " + table.getModel().getValueAt(i, 19) + " ON UPDATE "
										+ table.getModel().getValueAt(i, 20);
							}

							sql += columnName + " " + columnType
									+ ((defValue.equals("")) ? "" : (" DEFAULT " + defValue)) // Default
									+ (((boolean) table.getModel().getValueAt(i, 2)) ? "" : " not NULL")
									+ (((boolean) table.getModel().getValueAt(i, 0)) ? " PRIMARY KEY "
											: ((boolean) table.getModel().getValueAt(i, 1)) ? " UNIQUE " : "")
									+ (((boolean) table.getModel().getValueAt(i, 4)) // Autoincrement
											? sql += autoIncText
											: "")
									+ (((boolean) table.getModel().getValueAt(i, 5)) // Foreign key
											? sql += foreignKeyText
											: ";");

							if (i < tableNumRows - 1) {
								sql += ", ";
							} else {
								sql += ") ";
							}
						}

						// if (!priKeys.isEmpty()) {
						//
						// sql += " PRIMARY KEY (" + priKeys.get(0);
						// for (int i = 1; i < priKeys.size(); i++) {
						// sql += ",\n" + priKeys.get(i);
						// }
						//
						// sql += "))\n";
						// }
						// ;

						System.out.println(sql);
						stmt.executeUpdate(sql);
						JOptionPane.showMessageDialog(btnSave,
								"Table " + tableName + " has been created in the database " + currDBName);

						// if (!uniques.isEmpty()) {
						//
						// Timestamp timestamp = new Timestamp(System.currentTimeMillis());
						// sql = "CREATE UNIQUE INDEX SQL" + sdf.format(timestamp) + " ON " + tableName
						// + " ("
						// + uniques.get(0);
						//
						// for (int i = 1; i < uniques.size(); i++) {
						// sql += ", " + uniques.get(i);
						// }
						// sql += ") ";
						// System.out.println(sql);
						// stmt.executeUpdate(sql);
						// JOptionPane.showMessageDialog(btnSave,
						// "UNIQUE INDEX has been created on the table " + tableName);
						// }

						if (!indices.isEmpty()) {
							// Timestamp timestamp = new Timestamp(System.currentTimeMillis());
							sql = "CREATE INDEX SQL"
									// + sdf.format(timestamp)
									+ " ON " + tableName + " (" + indices.get(0) + ")";

							System.out.println(sql);
							stmt.executeUpdate(sql);
							JOptionPane.showMessageDialog(btnSave, "INDEX has been created on the table " + tableName);

							for (int i = 1; i < indices.size(); i++) {
								sql = ", CREATE INDEX SQL"
										// + sdf.format(timestamp)
										+ " ON " + tableName + " (" + indices.get(i) + ")";
								System.out.println(sql);
								stmt.executeUpdate(sql);
								JOptionPane.showMessageDialog(btnSave,
										"INDEX has been created on the table " + tableName);
							}
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

		pack();
		setSize(791, 400);
		setLocationRelativeTo(null);

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JLabel lblNewLabel = new JLabel("   Table name: ");
		menuBar.add(lblNewLabel);

		txtTablename = new JTextField();
		txtTablename.setPreferredSize(new Dimension(200, 25));
		txtTablename.setMinimumSize(new Dimension(100, 20));
		txtTablename.setMaximumSize(new Dimension(400, 100));
		menuBar.add(txtTablename);
		txtTablename.setText("Untitled");
		txtTablename.setColumns(10);

		JMenu mnTable = new JMenu(" Table ");
		mnTable.setHorizontalAlignment(SwingConstants.CENTER);
		mnTable.setPreferredSize(new Dimension(60, 25));
		mnTable.setMinimumSize(new Dimension(20, 20));
		mnTable.setMaximumSize(new Dimension(100, 40));
		menuBar.add(mnTable);

		JMenuItem mntmNewMenuItem_2 = new JMenuItem("Table level constraints");
		mnTable.add(mntmNewMenuItem_2);

		JMenu mnNewMenu = new JMenu("Column");
		mnNewMenu.setPreferredSize(new Dimension(60, 25));
		mnNewMenu.setMinimumSize(new Dimension(20, 20));
		mnNewMenu.setMaximumSize(new Dimension(100, 40));
		mnNewMenu.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		menuBar.add(mnNewMenu);

		JMenuItem mntmAddColumn = new JMenuItem("Add column");
		mntmAddColumn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
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

					colDialog.pack();
					Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
					colDialog.setSize(screenSize.width * 2 / 4, screenSize.height * 2 / 3);
					colDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
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

					Object[] fila = new Object[25];
					fila[0] = colDialog.getChkbxPrimKey().isSelected();
					fila[1] = colDialog.getChkbxUnique().isSelected();
					fila[2] = colDialog.getChkbxNull().isSelected();
					fila[3] = colDialog.getChkbxIndex().isSelected();
					fila[4] = colDialog.getChckbxAutoinc().isSelected();
					fila[5] = colDialog.getChkbxForeign().isSelected();
					fila[6] = colDialog.getChkbxCheck().isSelected();

					fila[7] = colDialog.getTextName().getText();
					fila[8] = colDialog.getComboType().getSelectedItem();
					fila[9] = colDialog.getTextSize().getValue();
					fila[10] = colDialog.getTextScale().getValue();
					fila[11] = colDialog.getTxtDefValue().getText();

					fila[12] = colDialog.getComboGenerated().getSelectedItem();
					fila[13] = colDialog.getTxtInitValue().getText();
					fila[14] = colDialog.getTextIncrement().getText();
					fila[15] = colDialog.getChckbxCycle().isSelected();

					fila[16] = colDialog.getTxtConstName().getText();
					fila[17] = colDialog.getTxtRefTable().getText();
					fila[18] = colDialog.getTxtColNames().getText();
					fila[19] = colDialog.getChkbxOnDelete().isSelected();
					fila[20] = colDialog.getComboOnDelete().getSelectedItem();
					fila[21] = colDialog.getChkbxOnUpdate().isSelected();
					fila[22] = colDialog.getComboOnUpdate().getSelectedItem();

					fila[23] = colDialog.getTxtConstCheck().getText();
					fila[24] = colDialog.getEditorPaneConditions().getText();

					((DefaultTableModel) table.getModel()).addRow(fila);
					table.updateUI();

				}

			}

		});
		mnNewMenu.add(mntmAddColumn);

		JMenuItem mntmDelColumn = new JMenuItem("Remove column");
		mntmDelColumn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				((DefaultTableModel) table.getModel()).removeRow(table.getSelectedRow());
			}
		});
		mnNewMenu.add(mntmDelColumn);

		JMenu mnNewMenu_1 = new JMenu("Move rows");
		mnNewMenu_1.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		menuBar.add(mnNewMenu_1);

		JMenuItem mntmNewMenuItem = new JMenuItem("Move UP");
		mnNewMenu_1.add(mntmNewMenuItem);

		JMenuItem mntmNewMenuItem_1 = new JMenuItem("Move DOWN");
		mnNewMenu_1.add(mntmNewMenuItem_1);

		Component horizontalGlue = Box.createHorizontalGlue();
		menuBar.add(horizontalGlue);

		spinTabCrea = new JSpinner();
		spinTabCrea.setPreferredSize(new Dimension(40, 25));
		spinTabCrea.setMinimumSize(new Dimension(20, 20));
		spinTabCrea.setMaximumSize(new Dimension(40, 40));
		menuBar.add(spinTabCrea);
		spinTabCrea.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				if (table != null) {
					table.setFont(table.getFont().deriveFont(Float.parseFloat(spinTabCrea.getValue().toString())));
					table.setRowHeight((int) Float.parseFloat(spinTabCrea.getValue().toString()) + 5);
					DBManager.propsDBM.setDBMProp("spinTabCrea", spinTabCrea.getValue().toString());
					DBManager.propsDBM.saveProperties();
				}
			}
		});
		spinTabCrea.setModel(new SpinnerNumberModel(14, 1, 100, 1));
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

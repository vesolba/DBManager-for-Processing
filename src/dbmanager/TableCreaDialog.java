package dbmanager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.BoxLayout;
import javax.swing.JSeparator;
import javax.swing.border.LineBorder;
import javax.swing.UIManager;

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

	// private static final SimpleDateFormat sdf = new
	// SimpleDateFormat("yyMMddHHmmssSSS");
	public String autoIncText = "";
	public String foreignKeyText = "";
	private JTextField textCheckConstrainName;
	private JTextField textPrimKey;
	private JTextField textConditions;
	private JTextField textPrimKeyConstName;
	private JTextField textUniqueConstName;
	private JTextField textForeignConstName;
	private JLabel labelCheck;
	private JLabel chkbxPrimKey;
	private JLabel chkbkUnique;
	private JLabel chkbxForeign;
	private JFormattedTextField fTextUnique;
	private JTextField fTextForeign;
	private JFormattedTextField fTextReferColumns;
	private JFormattedTextField fTextReferTabName;

	public TableCreaDialog() {

		jbInit();
	}

	@SuppressWarnings("serial")
	private void jbInit() {
		getContentPane().setPreferredSize(new Dimension(500, 200));
		setPreferredSize(new Dimension(700, 600));
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
			conn = DBConnect.connect(true, currDBPath + "/" + currDBName, "", null, false);
		} catch (Exception ex) {

			System.out.println("The database " + currDBPath + "/" + currDBName + " is not available.");
			ex.printStackTrace();

		}

		setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/data/DBM4P3-32.png")));
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

		table.setMinimumSize(new Dimension(100, 50));
		table.setMaximumSize(new Dimension(2000, 2000));
		table.setPreferredScrollableViewportSize(new Dimension(300, 100));
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

		JMenuBar menuBar = new JMenuBar();
		menuBar.setBorder(UIManager.getBorder("MenuBar.border"));
		getContentPane().add(menuBar, BorderLayout.NORTH);

		JLabel lblNewLabel = new JLabel("   Table name: ");
		menuBar.add(lblNewLabel);

		txtTablename = new JTextField();
		txtTablename.setPreferredSize(new Dimension(200, 25));
		txtTablename.setMinimumSize(new Dimension(100, 20));
		txtTablename.setMaximumSize(new Dimension(400, 100));
		menuBar.add(txtTablename);
		txtTablename.setColumns(10);
		TextPrompt tptxtTablename = new TextPrompt("Write a name", txtTablename);
		tptxtTablename.changeAlpha(0.5f);

		JMenu mnTable = new JMenu(" Table      ");
		mnTable.setBorder(UIManager.getBorder("Menu.border"));
		mnTable.setHorizontalAlignment(SwingConstants.CENTER);
		mnTable.setPreferredSize(new Dimension(60, 25));
		mnTable.setMinimumSize(new Dimension(20, 20));
		mnTable.setMaximumSize(new Dimension(100, 40));
		menuBar.add(mnTable);

		JMenuItem mntmSave = new JMenuItem(" Save table  ");
		mntmSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// We are going to compound the create ddl sentence.
				// First we Test if the table already exists
				String tableName = txtTablename.getText();
				ResultSet rs;
				try {

					String currDBPath = currentNodeInfo.getPathLocation();
					String currDBName = currentNodeInfo.getdBaseName();

					conn = DBConnect.connect(true, currDBPath + "/" + currDBName, "", null, false);

					// Data for the data type combo
					dbmd = conn.getMetaData();

					rs = dbmd.getTables(null, null, tableName, null);

					if (rs.next()) {
						System.out.println("the table already exists.");
					} else {

						stmt = conn.createStatement();

						// We create the DDL sentence from the Add Column dialog table.
						String sql = "CREATE TABLE " + tableName + " (";

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
							Object objParams = DBManager.dataTypeInfo(columnType, "CREATE_PARAMS");

							if (objParams != null) {
								String params = objParams.toString();
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
							}

							System.out.println("llama 4: " + columnType);

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
									autoIncText += "(START WITH " + inicVal + ", INCREMENT BY " + incrVal
											+ (((boolean) table.getModel().getValueAt(i, 15)) ? ", cycle)" : ")");
								}
							}

							// Column level Foreign keys
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
											? autoIncText
											: "")
									+ (((boolean) table.getModel().getValueAt(i, 5)) // Foreign key
											? foreignKeyText
											: "");

							if (i < tableNumRows - 1) {
								sql += ", ";
							}
						}

						// Table level constraints

						// Check
						if (!textConditions.getText().equals("")) {

							sql += ",\n";

							if (!textCheckConstrainName.getText().equals("")) {
								sql += " CONSTRAINT " + textCheckConstrainName.getText();
							}
							sql += " CHECK (" + textConditions.getText() + ")";
						}

						// Primary keys
						if (!textPrimKey.getText().equals("")) {

							sql += ",\n";

							if (!textPrimKeyConstName.getText().equals("")) {
								sql += " CONSTRAINT " + textPrimKeyConstName.getText();
							}
							sql += " PRIMARY KEY (" + textPrimKey.getText() + ")";
						}

						// Unique
						if (!fTextUnique.getText().equals("")) {

							sql += ",\n";

							if (!textUniqueConstName.getText().equals("")) {
								sql += " CONSTRAINT " + textUniqueConstName.getText();
							}
							sql += " PRIMARY KEY (" + fTextUnique.getText() + ")";
						}

						// Foreign keys
						if (!fTextForeign.getText().equals("")) {

							sql += ",\n";

							if (!textForeignConstName.getText().equals("")) {
								sql += " CONSTRAINT " + textForeignConstName.getText();
							}
							sql += " FOREIGN KEY (" + fTextForeign.getText() + ")" + " REFERENCES "
									+ fTextReferTabName.getText() + "(" + fTextReferColumns.getText() + ")";
						}

						sql += ");";
						System.out.println(sql);
						stmt.executeUpdate(sql);
						JOptionPane.showMessageDialog(null,
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
							JOptionPane.showMessageDialog(null, "INDEX has been created on the table " + tableName);

							for (int i = 1; i < indices.size(); i++) {
								sql = ", CREATE INDEX SQL"
										// + sdf.format(timestamp)
										+ " ON " + tableName + " (" + indices.get(i) + ")";
								System.out.println(sql);
								stmt.executeUpdate(sql);
								JOptionPane.showMessageDialog(null, "INDEX has been created on the table " + tableName);
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
		mntmSave.setActionCommand("Save");
		mnTable.add(mntmSave);

		JMenuItem mntmNewMenuItem_2 = new JMenuItem(" Exit ");
		mntmNewMenuItem_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				dispose();
			}
		});
		mnTable.add(mntmNewMenuItem_2);

		Component horizontalStrut_1 = Box.createHorizontalStrut(20);
		menuBar.add(horizontalStrut_1);

		JMenu mnNewMenu = new JMenu(" Column      ");
		mnNewMenu.setPreferredSize(new Dimension(60, 25));
		mnNewMenu.setMinimumSize(new Dimension(20, 20));
		mnNewMenu.setMaximumSize(new Dimension(100, 40));
		mnNewMenu.setBorder(UIManager.getBorder("Menu.border"));
		menuBar.add(mnNewMenu);

		JMenuItem mntmAddColumn = new JMenuItem("Add column");
		mntmAddColumn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {

					String currDBPath = currentNodeInfo.getPathLocation();
					String currDBName = currentNodeInfo.getdBaseName();

					conn = DBConnect.connect(true, currDBPath + "/" + currDBName, "", null, false);

					// Data for the data type combo
					dbmd = conn.getMetaData();

					ResultSet rset = dbmd.getTypeInfo();

					colDialog = new AddColumnDlg();

					myComboModel = new MyComboModel();
					loadComboCols(rset, tipo);
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
				table.updateUI();
			}
		});
		mnNewMenu.add(mntmDelColumn);

		Component horizontalStrut = Box.createHorizontalStrut(20);
		menuBar.add(horizontalStrut);

		JMenu mnNewMenu_1 = new JMenu("Move rows");
		mnNewMenu_1.setBorder(UIManager.getBorder("Menu.border"));
		menuBar.add(mnNewMenu_1);

		JMenuItem mntmNewMenuItem = new JMenuItem("Move UP");
		mntmNewMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int currentRow = table.getSelectedRow();
				if (currentRow > 0) {
					((DefaultTableModel) table.getModel()).moveRow(currentRow, currentRow, currentRow - 1);
				}
			}
		});
		mnNewMenu_1.add(mntmNewMenuItem);

		JMenuItem mntmNewMenuItem_1 = new JMenuItem("Move DOWN");
		mntmNewMenuItem_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				int currentRow = table.getSelectedRow();
				if (currentRow < table.getModel().getRowCount()) {
					((DefaultTableModel) table.getModel()).moveRow(currentRow, currentRow, currentRow + 1);
				}
			}
		});
		mnNewMenu_1.add(mntmNewMenuItem_1);

		Component horizontalGlue = Box.createHorizontalGlue();
		menuBar.add(horizontalGlue);

		if (DBManager.propsDBM != null) {
			String propTable1 = DBManager.propsDBM.getDBMProp("spinTabCrea");

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
			propTable1 = DBManager.propsDBM.getDBMProp("spinTabCrea");

			if (propTable1 != null && !propTable1.isEmpty()) {
				spinTabCrea.setValue(Integer.parseInt(propTable1));
				table.setFont(table.getFont().deriveFont(Float.parseFloat(spinTabCrea.getValue().toString())));
				table.setRowHeight((int) Float.parseFloat(spinTabCrea.getValue().toString()) + 5);
			}

		}
		JSplitPane splitPane = new JSplitPane();
		splitPane.setMaximumSize(new Dimension(2000, 2000));
		splitPane.setResizeWeight(0.5);
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		getContentPane().add(splitPane, BorderLayout.CENTER);

		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setBorder(UIManager.getBorder("MenuBar.border"));
		scrollPane.setMinimumSize(new Dimension(100, 40));
		scrollPane.setMaximumSize(new Dimension(2000, 2000));
		scrollPane.setPreferredSize(new Dimension(400, 200));
		// getContentPane().add(scrollPane, BorderLayout.NORTH);
		scrollPane.setAutoscrolls(true);
		scrollPane.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));

		JPanel panel = new JPanel();
		panel.setBorder(
				new TitledBorder(null, "Table level constrains ", TitledBorder.LEFT, TitledBorder.TOP, null, null));
		panel.setMinimumSize(new Dimension(100, 10));
		panel.setPreferredSize(new Dimension(400, 200));
		panel.setMaximumSize(new Dimension(10000, 1000));

		splitPane.setLeftComponent(scrollPane);
		splitPane.setRightComponent(panel);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		JPanel panel_5 = new JPanel();
		panel.add(panel_5);

		textCheckConstrainName = new JTextField();
		panel_5.add(textCheckConstrainName);
		textCheckConstrainName.setPreferredSize(new Dimension(100, 20));
		TextPrompt tpCheckConstraintName = new TextPrompt("Constraint Name (optional)", textCheckConstrainName);
		tpCheckConstraintName.changeAlpha(0.5f);
		textCheckConstrainName.setMinimumSize(new Dimension(100, 20));
		textCheckConstrainName.setMaximumSize(new Dimension(2000, 2000));
		textCheckConstrainName.setColumns(15);

		labelCheck = new JLabel("CHECK  Conditions: (");
		panel_5.add(labelCheck);
		labelCheck.setAlignmentX(0.5f);
		textConditions = new JTextField();
		panel_5.add(textConditions);
		textConditions.setColumns(30);
		TextPrompt tpConditions = new TextPrompt("Ex.: IdUsu > 0, Role in (Chief, Employee, Colaborator)",
				textConditions);
		tpConditions.changeAlpha(0.5f);

		JLabel label_2 = new JLabel(")");
		panel_5.add(label_2);

		JSeparator separator = new JSeparator();
		separator.setPreferredSize(new Dimension(0, 1));
		separator.setMaximumSize(new Dimension(2000, 2000));
		panel.add(separator);

		JPanel panel_2 = new JPanel();
		panel.add(panel_2);

		textPrimKeyConstName = new JTextField();
		textPrimKeyConstName.setPreferredSize(new Dimension(100, 20));
		textPrimKeyConstName.setMinimumSize(new Dimension(100, 20));
		textPrimKeyConstName.setMaximumSize(new Dimension(2000, 2000));
		textPrimKeyConstName.setColumns(15);
		panel_2.add(textPrimKeyConstName);
		TextPrompt tpPrimKeyConstName = new TextPrompt("Constraint Name (optional)", textPrimKeyConstName);
		tpPrimKeyConstName.changeAlpha(0.5f);

		chkbxPrimKey = new JLabel("PRIMARY KEY (");
		panel_2.add(chkbxPrimKey);

		textPrimKey = new JTextField();
		panel_2.add(textPrimKey);
		textPrimKey.setColumns(30);
		TextPrompt tpPrimKey = new TextPrompt("Comma separated  columns", textPrimKey);
		tpPrimKey.changeAlpha(0.5f);

		JLabel lblNewLabel_3 = new JLabel(")");
		panel_2.add(lblNewLabel_3);

		JSeparator separator_1 = new JSeparator();
		separator_1.setMaximumSize(new Dimension(2000, 2000));
		separator_1.setPreferredSize(new Dimension(0, 1));
		panel.add(separator_1);

		JPanel panel_4 = new JPanel();
		panel.add(panel_4);

		textUniqueConstName = new JTextField();
		textUniqueConstName.setPreferredSize(new Dimension(100, 20));
		textUniqueConstName.setMinimumSize(new Dimension(100, 20));
		textUniqueConstName.setMaximumSize(new Dimension(2000, 2000));
		textUniqueConstName.setColumns(15);
		panel_4.add(textUniqueConstName);
		TextPrompt tpUniqueConstName = new TextPrompt("Constraint Name (optional)", textUniqueConstName);
		tpUniqueConstName.changeAlpha(0.5f);

		chkbkUnique = new JLabel("UNIQUE (");
		panel_4.add(chkbkUnique);

		fTextUnique = new JFormattedTextField();
		panel_4.add(fTextUnique);
		fTextUnique.setColumns(30);
		TextPrompt tpUnique = new TextPrompt("Comma separated  columns", fTextUnique);
		tpUnique.changeAlpha(0.5f);

		JLabel label = new JLabel(")");
		panel_4.add(label);

		JSeparator separator_2 = new JSeparator();
		separator_2.setPreferredSize(new Dimension(0, 1));
		separator_2.setMaximumSize(new Dimension(2000, 2000));
		panel.add(separator_2);

		JPanel panel_6 = new JPanel();
		panel.add(panel_6);

		textForeignConstName = new JTextField();
		textForeignConstName.setPreferredSize(new Dimension(100, 20));
		textForeignConstName.setMinimumSize(new Dimension(100, 20));
		textForeignConstName.setMaximumSize(new Dimension(2000, 2000));
		textForeignConstName.setColumns(15);
		panel_6.add(textForeignConstName);
		TextPrompt tpForeignConstName = new TextPrompt("Constraint Name (optional)", textForeignConstName);
		tpForeignConstName.changeAlpha(0.5f);

		chkbxForeign = new JLabel("FOREIGN KEY (");
		panel_6.add(chkbxForeign);

		fTextForeign = new JTextField();
		panel_6.add(fTextForeign);
		fTextForeign.setColumns(30);
		TextPrompt tpForeign = new TextPrompt("Comma separated columns", fTextForeign);
		tpForeign.changeAlpha(0.5f);

		JLabel label_1 = new JLabel(")");
		panel_6.add(label_1);

		JPanel panel_1 = new JPanel();
		panel_6.add(panel_1);

		JLabel lblNewLabel_2 = new JLabel("REFERENCES ");
		panel_1.add(lblNewLabel_2);

		fTextReferTabName = new JFormattedTextField();
		panel_1.add(fTextReferTabName);
		fTextReferTabName.setColumns(15);
		TextPrompt tpTextReferTabName = new TextPrompt("Referenced table name", fTextReferTabName);

		JLabel lblNewLabel_4 = new JLabel("(");
		panel_1.add(lblNewLabel_4);

		fTextReferColumns = new JFormattedTextField();
		panel_1.add(fTextReferColumns);
		fTextReferColumns.setColumns(30);
		TextPrompt tpReferColumns = new TextPrompt("Comma separated columns", fTextReferColumns);

		JLabel lblNewLabel_5 = new JLabel(")");
		panel_1.add(lblNewLabel_5);
		tpTextReferTabName.changeAlpha(0.5f);
		tpReferColumns.changeAlpha(0.5f);

		pack();
		setSize(591, 500);
		setLocationRelativeTo(null);

		JPanel panel_3 = new JPanel();
		panel_3.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		getContentPane().add(panel_3, BorderLayout.SOUTH);
		panel_3.setPreferredSize(new Dimension(100, 35));
		panel_3.setMaximumSize(new Dimension(2000, 2000));
		FlowLayout flowLayout_1 = (FlowLayout) panel_3.getLayout();
		flowLayout_1.setAlignment(FlowLayout.LEFT);

		JLabel lblNewLabel_1 = new JLabel();
		panel_3.add(lblNewLabel_1);
		lblNewLabel_1.setAlignmentY(Component.TOP_ALIGNMENT);
		lblNewLabel_1.setHorizontalTextPosition(SwingConstants.LEFT);
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

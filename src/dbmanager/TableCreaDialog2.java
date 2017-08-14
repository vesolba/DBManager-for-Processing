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

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultMutableTreeNode;

public class TableCreaDialog2 extends JDialog {

	private JTextField txtTablename;
	private JTable table;
	private JSpinner spinTabCrea;

	private Connection conn = null;
	private DatabaseMetaData dbmd = null;
	private Statement stmt = null;
	private MyColumnTypes tipo;
	public static MyComboModel myComboModel;
	public AddColumnDlg colDialog;
	public DefaultMutableTreeNode currentNode;
	public DBTreeNodeK currentNodeInfo;

	public String autoIncText = "";

	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmssSSS");
	private JButton btnEdit;
	private TableCreaModel tableModel;
	private JPanel buttonsPanel;
	private JPanel panel_2;

	public TableCreaDialog2() {

		jbInit();
	}

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

			System.out.println("The database " + currDBPath + "/" + currDBName + " is not available.");
			ex.printStackTrace();
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

		tableModel = new TableCreaModel(conn, "", "");
		table = new JTable(tableModel) {
			public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
				Component c = super.prepareRenderer(renderer, row, column);

				int rendererWidth = c.getPreferredSize().width;
				TableColumn tableColumn = getColumnModel().getColumn(column);

				int renderWidth2 = Math.max(rendererWidth + getIntercellSpacing().width,
						tableColumn.getPreferredWidth());
				int renderWidth3 = Math.max(renderWidth2, tableModel.getColumnSize(column));

				tableColumn.setPreferredWidth(renderWidth3);

				// Alternate row color
				if (!isRowSelected(row))
					c.setBackground(row % 2 == 0 ? getBackground() : Color.LIGHT_GRAY);

				return c;
			}
		};

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
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setColumnSelectionAllowed(true);

		JScrollPane scrollPane = new JScrollPane(table);
		getContentPane().add(scrollPane, BorderLayout.CENTER);
		scrollPane.setAutoscrolls(true);
		scrollPane.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));

		// JPanel panel_1 = new JPanel();
		// getContentPane().add(panel_1, BorderLayout.SOUTH);

		// panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.X_AXIS));

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

					Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
					colDialog.pack();
					colDialog.setSize(screenSize.width * 2 / 4, screenSize.height * 2 / 4);
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

					ArrayList<Object> row = new ArrayList<>();

					row.add(colDialog.getChkbxPrimKey().isSelected());
					row.add(colDialog.getChkbxUnique().isSelected());
					row.add(colDialog.getChkbxNull().isSelected());
					row.add(colDialog.getChkbxIndex().isSelected());
					row.add(colDialog.getChckbxAutoinc().isSelected());
					row.add(colDialog.getChkbxForeign().isSelected());
					row.add(colDialog.getChkbxCheck().isSelected());

					row.add(colDialog.getTextName().getText());
					row.add(colDialog.getComboType().getSelectedItem());
					row.add(colDialog.getTextSize().getValue());
					row.add(colDialog.getTextScale().getValue());
					row.add(colDialog.getTxtDefValue().getText());

					row.add(colDialog.getComboGenerated().getSelectedItem());
					row.add(colDialog.getTxtInitValue().getText());
					row.add(colDialog.getTextIncrement().getText());
					row.add(colDialog.getChckbxCycle().isSelected());

					row.add(colDialog.getTxtConstName().getText());
					row.add(colDialog.getTxtRefTable().getText());
					row.add(colDialog.getTxtColNames().getText());
					row.add(colDialog.getChkbxOnDelete().isSelected());
					row.add(colDialog.getComboOnDelete().getSelectedItem());
					row.add(colDialog.getChkbxOnUpdate().isSelected());
					row.add(colDialog.getComboOnUpdate().getSelectedItem());

					row.add(colDialog.getTxtConstCheck().getText());
					row.add(colDialog.getEditorPaneConditions().getText());
					tableModel.data.add(row);

					// Autoincrement
					if (colDialog.getChckbxAutoinc().isSelected() && autoIncText.equals("")) {
						autoIncText = " GENERATED " + (colDialog.getComboGenerated().getSelectedItem().toString());
						autoIncText += " AS IDENTITY (START WITH "
								+ Integer.parseInt(colDialog.getTxtInitValue().getText());
						autoIncText += ",  INCREMENT BY " + Integer.parseInt(colDialog.getTextIncrement().getText());
						autoIncText += (colDialog.getChckbxCycle().isSelected()) ? ", cycle)" : ")";
					}
					table.updateUI();

				}
			}
		});

		if (DBManager.propsDBM != null) {
			String propTable = DBManager.propsDBM.getDBMProp("spinTabCrea");

			spinTabCrea = new JSpinner();
			spinTabCrea.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent arg0) {
					table.setFont(table.getFont().deriveFont(Float.parseFloat(spinTabCrea.getValue().toString())));
					table.setRowHeight((int) Float.parseFloat(spinTabCrea.getValue().toString()) + 5);
					DBManager.propsDBM.setDBMProp("spinTabCrea", spinTabCrea.getValue().toString());
					DBManager.propsDBM.saveProperties();
				}
			});
			panel_2.add(spinTabCrea);
			spinTabCrea.setPreferredSize(new Dimension(40, 20));
			spinTabCrea.setMinimumSize(new Dimension(1000, 20));
			spinTabCrea.setMaximumSize(new Dimension(1000, 200));
			spinTabCrea.setModel(new SpinnerNumberModel(12, 12, 100, 1));
			propTable = DBManager.propsDBM.getDBMProp("spinTabCrea");

			if (propTable != null && !propTable.isEmpty()) {
				spinTabCrea.setValue(Integer.parseInt(propTable));
				table.setFont(table.getFont().deriveFont(Float.parseFloat(spinTabCrea.getValue().toString())));
				table.setRowHeight((int) Float.parseFloat(spinTabCrea.getValue().toString()) + 5);
			}

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

							colDialog.getChkbxPrimKey().setSelected((boolean) tableModel.getValueAt(selectedRow, 0));
							colDialog.getChkbxUnique().setSelected((boolean) tableModel.getValueAt(selectedRow, 1));
							colDialog.getChkbxNull().setSelected((boolean) tableModel.getValueAt(selectedRow, 2));
							colDialog.getChkbxIndex().setSelected((boolean) tableModel.getValueAt(selectedRow, 3));
							colDialog.getChckbxAutoinc().setSelected((boolean) tableModel.getValueAt(selectedRow, 4));
							colDialog.getChkbxForeign().setSelected((boolean) tableModel.getValueAt(selectedRow, 5));
							colDialog.getChkbxCheck().setSelected((boolean) tableModel.getValueAt(selectedRow, 6));

							colDialog.getTextName().setText((String) tableModel.getValueAt(selectedRow, 7));
							colDialog.getComboType().getModel().setSelectedItem(tableModel.getValueAt(selectedRow, 8));
							colDialog.getTextSize().setValue((int) tableModel.getValueAt(selectedRow, 9));
							colDialog.getTextScale().setValue((int) tableModel.getValueAt(selectedRow, 10));
							colDialog.getTxtDefValue().setText((String) tableModel.getValueAt(selectedRow, 11));

							colDialog.getComboGenerated().getModel()
									.setSelectedItem(tableModel.getValueAt(selectedRow, 12));
							colDialog.getTxtInitValue().setText((String) tableModel.getValueAt(selectedRow, 13));
							colDialog.getTextIncrement().setText((String) tableModel.getValueAt(selectedRow, 14));
							colDialog.getChckbxCycle().setSelected((boolean) tableModel.getValueAt(selectedRow, 15));

							colDialog.getTxtConstName().setText((String) tableModel.getValueAt(selectedRow, 16));
							colDialog.getTxtRefTable().setText((String) tableModel.getValueAt(selectedRow, 17));
							colDialog.getTxtColNames().setText((String) tableModel.getValueAt(selectedRow, 18));
							colDialog.getChkbxOnDelete().setSelected((boolean) tableModel.getValueAt(selectedRow, 19));
							colDialog.getComboOnDelete().getModel()
									.setSelectedItem(tableModel.getValueAt(selectedRow, 20));
							colDialog.getChkbxOnUpdate().setSelected((boolean) tableModel.getValueAt(selectedRow, 21));
							colDialog.getComboOnUpdate().getModel()
									.setSelectedItem(tableModel.getValueAt(selectedRow, 22));

							colDialog.getTxtConstCheck().setText((String) tableModel.getValueAt(selectedRow, 23));
							colDialog.getEditorPaneConditions()
									.setText((String) tableModel.getValueAt(selectedRow, 24));

							colDialog.getComboType().setEnabled(true);
							colDialog.setVisible(true);

						} catch (Exception e2) {
							colDialog.setEnabled(false);
							e2.printStackTrace();
						}

						if (colDialog.result == 0) {

							tableModel.setValueAt(colDialog.getChkbxPrimKey().isSelected(), selectedRow, 0);
							tableModel.setValueAt(colDialog.getChkbxUnique().isSelected(), selectedRow, 1);
							tableModel.setValueAt(colDialog.getChkbxNull().isSelected(), selectedRow, 2);
							tableModel.setValueAt(colDialog.getChkbxIndex().isSelected(), selectedRow, 3);
							tableModel.setValueAt(colDialog.getChckbxAutoinc().isSelected(), selectedRow, 4);
							tableModel.setValueAt(colDialog.getChkbxForeign().isSelected(), selectedRow, 5);
							tableModel.setValueAt(colDialog.getChkbxCheck().isSelected(), selectedRow, 6);

							tableModel.setValueAt(colDialog.getTextName().getText(), selectedRow, 7);
							tableModel.setValueAt(colDialog.getComboType().getModel().getSelectedItem(), selectedRow,
									8);
							tableModel.setValueAt(colDialog.getTextSize().getValue(), selectedRow, 9);
							tableModel.setValueAt(colDialog.getTextScale().getValue(), selectedRow, 10);
							tableModel.setValueAt(colDialog.getTxtDefValue().getText(), selectedRow, 11);

							tableModel.setValueAt(colDialog.getComboGenerated().getModel().getSelectedItem(),
									selectedRow, 12);
							tableModel.setValueAt(colDialog.getTxtInitValue().getText(), selectedRow, 13);
							tableModel.setValueAt(colDialog.getTextIncrement().getText(), selectedRow, 14);
							tableModel.setValueAt(colDialog.getChckbxCycle().isSelected(), selectedRow, 15);
							tableModel.setValueAt(colDialog.getTxtConstName().getText(), selectedRow, 16);
							tableModel.setValueAt(colDialog.getTxtRefTable().getText(), selectedRow, 17);
							tableModel.setValueAt(colDialog.getTxtColNames().getText(), selectedRow, 18);
							;
							tableModel.setValueAt(colDialog.getChkbxOnDelete().isSelected(), selectedRow, 19);
							tableModel.setValueAt(colDialog.getComboOnDelete().getModel().getSelectedItem(),
									selectedRow, 20);
							tableModel.setValueAt(colDialog.getChkbxOnUpdate().isSelected(), selectedRow, 21);
							tableModel.setValueAt(colDialog.getComboOnUpdate().getModel().getSelectedItem(),
									selectedRow, 22);
							tableModel.setValueAt(colDialog.getTxtConstCheck().getText(), selectedRow, 23);
							tableModel.setValueAt(colDialog.getEditorPaneConditions().getText(), selectedRow, 24);

							// Autoincrement
							if (colDialog.getChckbxAutoinc().isSelected() && autoIncText.equals("")) {
								autoIncText = " GENERATED "
										+ (colDialog.getComboGenerated().getSelectedItem().toString());
								autoIncText += " AS IDENTITY (START WITH "
										+ Integer.parseInt(colDialog.getTxtInitValue().getText());
								autoIncText += ",  INCREMENT BY "
										+ Integer.parseInt(colDialog.getTextIncrement().getText());
								autoIncText += (colDialog.getChckbxCycle().isSelected()) ? ", cycle)" : ")";
							}

							table.updateUI();
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

					int rowSel = table.getSelectedRow();
					tableModel.data.remove(rowSel);
					table.updateUI();
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

							int tableNumRows = tableModel.getRowCount();
							for (int i = 0; i < tableNumRows; i++) {
								columnName = (String) tableModel.getValueAt(i, 5);
								columnType = (String) tableModel.getValueAt(i, 6).toString();
								int elem7 = (int) tableModel.getValueAt(i, 7); // Size
								int dataType = (int) DBManager.dataTypeInfo(columnType, "DATA_TYPE");

								String params = DBManager.dataTypeInfo(columnType, "CREATE_PARAMS").toString();
								if (params.contains("length") || params.contains("precision")) {
									if (dataType == -3 || dataType == -4) {
										columnType.replaceFirst("()", "(" + elem7);
									} else {
										columnType += "(" + elem7;
									}

									if (params.contains("scale")) {
										columnType += ", " + (int) tableModel.getValueAt(i, 8); // Scale
									}

									columnType += ")";
								}

								String defValue = (String) tableModel.getValueAt(i, 9); // Default value

								if ((boolean) tableModel.getValueAt(i, 0))
									priKeys.add(columnName);
								else if ((boolean) tableModel.getValueAt(i, 1))
									uniques.add(columnName);
								else if ((boolean) tableModel.getValueAt(i, 3))
									indices.add(columnName);

								sql += columnName + " " + columnType
										+ ((defValue.equals("")) ? "" : (" DEFAULT " + defValue)) // Default
										+ (((boolean) tableModel.getValueAt(i, 2)) ? "" : " not NULL")
										+ (((boolean) tableModel.getValueAt(i, 4)) // Autoincrement
												? sql += autoIncText
												: "")
										+ (((boolean) tableModel.getValueAt(i, 10)) // Constrains checkbox
												? (tableModel.getValueAt(i, 11)) // Constrains area
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
								JOptionPane.showMessageDialog(btnOK,
										"INDEX has been created on the table " + tableName);
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

		buttonsPanel = new JPanel();
		getContentPane().add(buttonsPanel, BorderLayout.EAST);
		buttonsPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.PAGE_AXIS));

		panel_2 = new JPanel();
		buttonsPanel.add(panel_2);
		panel_2.setMaximumSize(new Dimension(1000, 100));
		panel_2.setLayout(new BoxLayout(panel_2, BoxLayout.X_AXIS));

		Component horizontalGlue = Box.createHorizontalGlue();
		panel_2.add(horizontalGlue);
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
			myComboModel.insertItem(tipo);
		}
	}

}

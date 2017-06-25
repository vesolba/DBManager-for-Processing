package dbmanager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.DefaultMutableTreeNode;

@SuppressWarnings("serial")
public class DBGUIFrame extends JFrame {

	/**
	 * 
	 */
	private JPanel contentPane;
	private static JMenu mnServer = null;
	private static JMenuItem mntmStartServer = null;
	private static JMenuItem mntmStopServer = null;
	private JSpinner spinRowHeight;
	private JSpinner spinFontSize;
	private JSpinner spinTreeFontSize;
	private JSpinner spinTable;
	private JTextField txtLastMessage;
	private JTable tableSQLResult;
	private JTextPane txtSelected;
	private JTextPane textPaneInSQL;

	/**
	 * Create the frame.
	 */
	public DBGUIFrame() {

		jbInit();
	}

	private void jbInit() {
		setPreferredSize(new Dimension(600, 400));
		setMinimumSize(new Dimension(300, 200));
		setMaximumSize(new Dimension(3000, 3000));
		setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/data/DBM4P3-32.png")));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 661, 431);
		setTitle("DB Manager for Processing ");

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		mnServer = new JMenu("Server is off");
		mnServer.setForeground(Color.RED);
		mnServer.setIcon(null);
		menuBar.add(mnServer);

		mntmStartServer = new JMenuItem("Start Server");

		mnServer.add(mntmStartServer);
		mntmStartServer.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {

				try {
					DBConnect.inicServer();
					checkServerMenu();
				} catch (Exception f) {

					checkServerMenu();
				}

			}

		});

		mntmStopServer = new JMenuItem("Stop Server");
		mnServer.add(mntmStopServer);
		mntmStopServer.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					DBConnect.stopServer();
					checkServerMenu();
				} catch (Exception f) {
					checkServerMenu();
				}

			}

		});

		JMenuItem mntmMenuRefresh = new JMenuItem("Refresh");
		mntmMenuRefresh.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				checkServerMenu();
			}
		});
		mnServer.add(mntmMenuRefresh);

		JMenuItem mntmSettings = new JMenuItem("Settings...");
		mnServer.add(mntmSettings);
		mntmSettings.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					DBSettingsDialog dialog = new DBSettingsDialog();

					// Parameters window that user must agree
					DBManager.javaDBInstall = System.getenv("DERBY_INSTALL");
					dialog.getTxtInstallation().setText(DBManager.javaDBInstall);

					dialog.getTxtLocation().setText(DBManager.derbySystemHome);

					dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					dialog.setLocationRelativeTo(null);
					dialog.setVisible(true);

					// if (dialog != null) {

					// TxtInstallation will be ok if the connection works.

					// }

				} catch (Exception f) {
					f.printStackTrace();
				}
			}

		});

		JMenuItem mntmExit = new JMenuItem("Exit");
		mntmExit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				setVisible(false);
				dispose();
			}

		});

		Component horizontalStrut = Box.createHorizontalStrut(10);
		menuBar.add(horizontalStrut);

		JMenu mnDBTree = new JMenu("DB Tree");
		menuBar.add(mnDBTree);

		JMenu mnIcons = new JMenu("Icons");
		mnIcons.setHorizontalAlignment(SwingConstants.LEFT);
		mnDBTree.add(mnIcons);

		JRadioButtonMenuItem mntmProcessing = new JRadioButtonMenuItem("Processing");
		JRadioButtonMenuItem mntmDefault = new JRadioButtonMenuItem("Default");
		mntmProcessing.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				DBManager.dBtree.setCellRenderer(DBManager.renderer);
				mntmProcessing.setSelected(true);
				mntmDefault.setSelected(false);
				DBManager.propsDBM.setDBMProp("treeiconsflavour", "Processing");
				DBManager.propsDBM.saveProperties();
			}
		});
		mntmProcessing.setSelected(DBManager.renderProp.equals("Processing"));
		mnIcons.add(mntmProcessing);

		mntmDefault.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DBManager.dBtree.setCellRenderer(null);
				mntmProcessing.setSelected(false);
				mntmDefault.setSelected(true);
				DBManager.propsDBM.setDBMProp("treeiconsflavour", "Default");
				DBManager.propsDBM.saveProperties();
			}
		});
		mntmDefault.setSelected(DBManager.renderProp.equals("Default"));
		mnIcons.add(mntmDefault);

		JMenu mnRowsHeight = new JMenu("Rows height");
		mnDBTree.add(mnRowsHeight);

		// To change dbtree rows height
		spinRowHeight = new JSpinner();
		spinRowHeight.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				DBManager.dBtree.setRowHeight((int) spinRowHeight.getValue());
				DBManager.propsDBM.setDBMProp("treerowsheight", (spinRowHeight.getValue().toString()));
				DBManager.propsDBM.saveProperties();
			}
		});

		mnRowsHeight.add(spinRowHeight);
		spinRowHeight.setToolTipText("Rows height");
		spinRowHeight.setModel(new SpinnerNumberModel(new Integer(35), null, null, new Integer(1)));

		// Initialize and Change DBTree Font Size
		JMenu mnTreeFontSize = new JMenu("Font Size");
		mnDBTree.add(mnTreeFontSize);

		String propFont = DBManager.propsDBM.getDBMProp("treeFontSize");

		spinTreeFontSize = new JSpinner();
		spinTreeFontSize.setModel(new SpinnerNumberModel(new Integer(12), null, null, new Integer(1)));
		spinTreeFontSize.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				DBManager.dBtree.setFont(DBManager.dBtree.getFont()
						.deriveFont(Float.parseFloat(spinTreeFontSize.getValue().toString())));
				DBManager.propsDBM.setDBMProp("treeFontSize", spinTreeFontSize.getValue().toString());
				DBManager.propsDBM.saveProperties();
			}
		});
		mnTreeFontSize.add(spinTreeFontSize);
		if (propFont != null && !propFont.isEmpty()) {
			spinTreeFontSize.setValue(Integer.parseInt(propFont));
			DBManager.dBtree.setFont(
					DBManager.dBtree.getFont().deriveFont(Float.parseFloat(spinTreeFontSize.getValue().toString())));
		}

		JMenuItem mntmExpandAll = new JMenuItem("Expand All");
		mnDBTree.add(mntmExpandAll);

		JMenuItem mntmCollapseAll = new JMenuItem("Collapse All");
		mnDBTree.add(mntmCollapseAll);

		Component horizontalStrut_2 = Box.createHorizontalStrut(10);
		menuBar.add(horizontalStrut_2);

		mntmExit.setHorizontalAlignment(SwingConstants.LEFT);
		menuBar.add(mntmExit);

		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));

		JSplitPane splitPane = new JSplitPane();
		splitPane.setResizeWeight(0.3);
		contentPane.add(splitPane, BorderLayout.CENTER);

		JPanel leftPanel = new JPanel();
		splitPane.setLeftComponent(leftPanel);

		leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.X_AXIS));

		JScrollPane treeView = new JScrollPane(DBManager.dBtree);
		leftPanel.add(treeView);

		JPanel rightPanel = new JPanel();
		splitPane.setRightComponent(rightPanel);
		rightPanel.setLayout(new BorderLayout(0, 0));

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		rightPanel.add(tabbedPane, BorderLayout.CENTER);

		JPanel execSQLTab = new JPanel();
		tabbedPane.addTab("Execute SQL", null, execSQLTab, null);
		execSQLTab.setLayout(new BorderLayout(0, 0));

		JSplitPane splPanExecSQL = new JSplitPane();
		splPanExecSQL.setResizeWeight(0.4);
		splPanExecSQL.setOrientation(JSplitPane.VERTICAL_SPLIT);
		execSQLTab.add(splPanExecSQL);

		JPanel upperPanel = new JPanel();
		upperPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);
		splPanExecSQL.setLeftComponent(upperPanel);
		upperPanel.setLayout(new BorderLayout(0, 0));

		JMenuBar menuBar_1 = new JMenuBar();
		menuBar_1.setAlignmentX(Component.RIGHT_ALIGNMENT);
		menuBar_1.setAlignmentY(Component.CENTER_ALIGNMENT);
		upperPanel.add(menuBar_1, BorderLayout.NORTH);

		JLabel lblWriteSQL = new JLabel(" Data From: ");
		menuBar_1.add(lblWriteSQL);

		txtSelected = new JTextPane();
		menuBar_1.add(txtSelected);

		Component horizontalGlue = Box.createHorizontalGlue();
		menuBar_1.add(horizontalGlue);

		JSeparator separator_3 = new JSeparator();
		separator_3.setOrientation(SwingConstants.VERTICAL);
		menuBar_1.add(separator_3);

		JMenu mnQuery = new JMenu("Query");
		mnQuery.setHorizontalAlignment(SwingConstants.RIGHT);
		menuBar_1.add(mnQuery);

		JMenuItem mntmSELECTgral = new JMenuItem("SELECT * FROM tablename");
		mntmSELECTgral.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				textPaneInSQL.setText(mntmSELECTgral.getText());
			}
		});
		mnQuery.add(mntmSELECTgral);

		JMenuItem mntmFullSelect = new JMenuItem("Full Select");
		mntmFullSelect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
			}
		});
		mnQuery.add(mntmFullSelect);

		JSeparator separator = new JSeparator();
		separator.setMaximumSize(new Dimension(0, 1000));
		separator.setOrientation(SwingConstants.VERTICAL);
		menuBar_1.add(separator);

		JMenu mnUpdate = new JMenu("Update");
		mnUpdate.setHorizontalAlignment(SwingConstants.RIGHT);
		menuBar_1.add(mnUpdate);

		JMenuItem mntmFullUpdate = new JMenuItem("Full Update");
		mntmFullUpdate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textPaneInSQL.setText(" UPDATE tableName [ [ AS ] correlationName ] ] \n"
						+ "SET columnName = value       \n" + " [ , columnName = value ]* \n" + "	[ WHERE clause ] \n"
						+ "  |  \n" + "UPDATE tableName \n" + "SET columnName = value \n"
						+ "[ , columnName = value ]*  \n" + "WHERE CURRENT OF ");
			}
		});
		mnUpdate.add(mntmFullUpdate);

		JMenuItem mntmFullInsert = new JMenuItem("Full Insert");
		mntmFullInsert.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textPaneInSQL.setText("");
			}
		});
		mnUpdate.add(mntmFullInsert);

		JSeparator separator_1 = new JSeparator();
		separator_1.setMaximumSize(new Dimension(0, 32767));
		separator_1.setOrientation(SwingConstants.VERTICAL);
		menuBar_1.add(separator_1);

		JMenu mnStructure = new JMenu("Structure");
		mnStructure.setHorizontalAlignment(SwingConstants.RIGHT);
		menuBar_1.add(mnStructure);

		JSeparator separator_4 = new JSeparator();
		separator_4.setOrientation(SwingConstants.VERTICAL);
		separator_4.setMaximumSize(new Dimension(0, 32767));
		menuBar_1.add(separator_4);

		JMenu mnOthers = new JMenu("Others");
		mnOthers.setHorizontalAlignment(SwingConstants.RIGHT);
		menuBar_1.add(mnOthers);

		JSeparator separator_2 = new JSeparator();
		separator_2.setSize(new Dimension(100, 0));
		separator_2.setMinimumSize(new Dimension(100, 0));
		separator_2.setMaximumSize(new Dimension(100, 32767));
		separator_2.setOrientation(SwingConstants.VERTICAL);
		menuBar_1.add(separator_2);

		spinFontSize = new JSpinner();
		spinFontSize.setModel(new SpinnerNumberModel(new Integer(12), null, null, new Integer(1)));
		menuBar_1.add(spinFontSize);

		spinFontSize.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				textPaneInSQL.setFont(
						textPaneInSQL.getFont().deriveFont(Float.parseFloat(spinFontSize.getValue().toString())));

				DBManager.propsDBM.setDBMProp("fontsize01", spinFontSize.getValue().toString());
				DBManager.propsDBM.saveProperties();
			}
		});
		textPaneInSQL = new JTextPane();
		textPaneInSQL.setDropMode(DropMode.INSERT);
		propFont = DBManager.propsDBM.getDBMProp("fontsize01");
		if (propFont != null && !propFont.isEmpty()) {
			spinFontSize.setValue(Integer.parseInt(propFont));
			textPaneInSQL
					.setFont(textPaneInSQL.getFont().deriveFont(Float.parseFloat(spinFontSize.getValue().toString())));
		}

		JScrollPane scrollPane_1 = new JScrollPane(textPaneInSQL);

		JPopupMenu popupInSQL = new JPopupMenu();
		addPopup(textPaneInSQL, popupInSQL);

		JMenuItem mntmPaste = new JMenuItem("Paste");
		mntmPaste.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textPaneInSQL.paste();
			}
		});
		popupInSQL.add(mntmPaste);
		upperPanel.add(scrollPane_1, BorderLayout.CENTER);

		Box horizontalBox = Box.createHorizontalBox();
		upperPanel.add(horizontalBox, BorderLayout.SOUTH);

		JButton btnExecSQL = new JButton("Execute");
		btnExecSQL.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {

				Connection conn = null;

				DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) DBManager.dBtree
						.getLastSelectedPathComponent();
				DBTreeNodeK nodeInfo = (DBTreeNodeK) selectedNode.getUserObject();

				System.out.println(
						"Table model connection " + nodeInfo.getPathLocation() + "/" + nodeInfo.getdBaseName());

				try {
					conn = DBConnect.connect(!DBConnect.serverIsOn,
							nodeInfo.getPathLocation() + "/" + nodeInfo.getdBaseName(), "", null, false);
				} catch (Exception ex) {

					System.out.println("Table model connection online " + nodeInfo.getPathLocation() + "/"
							+ nodeInfo.getdBaseName());

					try {
						DBConnect.inicServer();
						conn = DBConnect.connect(!DBConnect.serverIsOn,
								nodeInfo.getPathLocation() + "/" + nodeInfo.getdBaseName(), "", null, false);
					} catch (Exception ey) {
						JOptionPane.showMessageDialog(null, "Database not available.", "Error",
								JOptionPane.ERROR_MESSAGE);
						ey.printStackTrace();
					}

				}

				String query = textPaneInSQL.getText();
				System.out.println(query);

				String trimmedQuery = query.trim();

				if ("select ".equalsIgnoreCase(trimmedQuery.substring(0, 7))) {

					System.out.println("query");
					MyTableModel tModel = new MyTableModel(conn, trimmedQuery);

					tableSQLResult.setModel(tModel);
					tableSQLResult.updateUI();

				} else {
					System.out.println("update");
					try {
						Statement statement = conn.createStatement();
						int c = statement.executeUpdate(query);
						txtLastMessage.setText(c + "registers updated");
					} catch (SQLException ex) {
						Logger.getLogger(DBGUIFrame.class.getName()).log(Level.SEVERE, null, ex);
					}
				}
			}
		});
		horizontalBox.add(btnExecSQL);

		Component horizontalGlue_1 = Box.createHorizontalGlue();
		horizontalBox.add(horizontalGlue_1);

		JLabel lblLastMessage = new JLabel("   Last Message: ");
		horizontalBox.add(lblLastMessage);

		txtLastMessage = new JTextField();
		horizontalBox.add(txtLastMessage);
		txtLastMessage.setColumns(10);

		JPanel lowPanel = new JPanel();
		splPanExecSQL.setRightComponent(lowPanel);
		lowPanel.setLayout(new BorderLayout(0, 0));

		tableSQLResult = new JTable() {
			public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
				Component c = super.prepareRenderer(renderer, row, column);

				int rendererWidth = c.getPreferredSize().width;
				TableColumn tableColumn = getColumnModel().getColumn(column);
				tableColumn.setPreferredWidth(
						Math.max(rendererWidth + getIntercellSpacing().width, tableColumn.getPreferredWidth()));

				// Alternate row color
				if (!isRowSelected(row))
					c.setBackground(row % 2 == 0 ? getBackground() : Color.LIGHT_GRAY);

				return c;
			}
		};
		tableSQLResult.setAutoscrolls(false);
		tableSQLResult.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		tableSQLResult.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		tableSQLResult.setCellSelectionEnabled(true);
		tableSQLResult.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tableSQLResult.setBackground(SystemColor.info);

		String propTable = DBManager.propsDBM.getDBMProp("spinTable");

		spinTable = new JSpinner();
		spinTable.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				tableSQLResult.setFont(
						tableSQLResult.getFont().deriveFont(Float.parseFloat(spinTable.getValue().toString())));
				tableSQLResult.setRowHeight((int) Float.parseFloat(spinTable.getValue().toString()) + 2);
				DBManager.propsDBM.setDBMProp("spinTable", spinTable.getValue().toString());
				DBManager.propsDBM.saveProperties();
			}
		});
		spinTable.setToolTipText("Table Font Size");
		spinTable.setModel(new SpinnerNumberModel(new Integer(12), null, null, new Integer(1)));
		horizontalBox.add(spinTable);
		propTable = DBManager.propsDBM.getDBMProp("spinTable");
		if (propTable != null && !propTable.isEmpty()) {
			spinTable.setValue(Integer.parseInt(propTable));
			tableSQLResult
					.setFont(tableSQLResult.getFont().deriveFont(Float.parseFloat(spinTable.getValue().toString())));
			tableSQLResult.setRowHeight((int) Float.parseFloat(spinTable.getValue().toString()) + 2);
		}

		JScrollPane scrollPane = new JScrollPane(tableSQLResult);
		scrollPane.setAutoscrolls(true);
		scrollPane.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		lowPanel.add(scrollPane, BorderLayout.CENTER);

		JPanel visualMngrTab = new JPanel();
		tabbedPane.addTab("Visual Manager", null, visualMngrTab, null);
		visualMngrTab.setLayout(new BorderLayout(0, 0));

		JTextPane textPane = new JTextPane();
		visualMngrTab.add(textPane);

		JPanel panel = new JPanel();
		tabbedPane.addTab("Code Gen.", null, panel, null);

		JPanel panelBatch = new JPanel();
		tabbedPane.addTab("Batch", null, panelBatch, null);
	}

	public static boolean checkServerMenu() {
		if (DBConnect.serverIsOn) {
			mnServer.setForeground(Color.GREEN);
			mnServer.setText("Server is on");
			mntmStartServer.setEnabled(false);
			mntmStopServer.setEnabled(true);
			return true;
		} else {
			mnServer.setForeground(Color.RED);
			mnServer.setText("Server is off");
			mntmStartServer.setEnabled(true);
			mntmStopServer.setEnabled(false);
			return false;
		}
	}

	/**
	 * @return the mnServer
	 */
	public static JMenu getMnServer() {
		return mnServer;
	}

	/**
	 * @return the txtSelected
	 */
	public JTextPane getTxtSelected() {
		return txtSelected;
	}

	// To shutting down an unique database from your application
	// DriverManager.getConnection( "jdbc:derby:sample;shutdown=true");

	private static void addPopup(Component component, final JPopupMenu popup) {
		component.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}

			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}

			private void showMenu(MouseEvent e) {
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		});
	}
}

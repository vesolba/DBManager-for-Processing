package dbmanager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

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
	private JSpinner spinTreeFontSize;
	private JTextPane textPane;
	private ExecSQLPanel execSQLPanel;
	private MyTypeInfoPanel testPanel;

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
				final TreeExpansionUtil expander = new TreeExpansionUtil(DBManager.dBtree);
				final String state = expander.getExpansionState();

				System.out.println(state);
				DBManager.dBtree.setModel(new DefaultTreeModel(DBManager.getTreeModel()));
				// Recover the expansion state
				expander.setExpansionState(state);
				DBManager.dBtree.updateUI();

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
				DBManager.dBtree.setRowHeight((int) spinRowHeight.getValue() + 5);
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
		mntmExpandAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				expandAll(DBManager.dBtree, true);
				DBManager.dBtree.updateUI();
				// int numRows = DBManager.dBtree.getRowCount();
				// for (int i = 0; i < numRows; i++) {
				// DBManager.dBtree.expandRow(i);
				// }
			}
		});
		mnDBTree.add(mntmExpandAll);

		JMenuItem mntmCollapseAll = new JMenuItem("Collapse All");
		mntmCollapseAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				expandAll(DBManager.dBtree, false);
				DBManager.dBtree.updateUI();
				// int numRows = DBManager.dBtree.getRowCount();
				// for (int i = 0; i < numRows; i++) {
				// DBManager.dBtree.collapseRow(i);
				// }
			}
		});
		mnDBTree.add(mntmCollapseAll);

		Component horizontalStrut_2 = Box.createHorizontalStrut(10);
		menuBar.add(horizontalStrut_2);

		JMenu mnCreate = new JMenu(" Create ");
		menuBar.add(mnCreate);

		JMenuItem mntmDBCreate = new JMenuItem("Create Database"); // Create DB
		mntmDBCreate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					DBCreationDialog dialog = new DBCreationDialog("", "", "", null, DBManager.derbySystemHome,
							"Create Database...");
					// DB Name, User, Pwd, Description, DB Location (File Location), Path in dBtree,
					// Action command
					dialog.getTxtDBLocation().setText(DBManager.derbySystemHome);
					dialog.setLocationRelativeTo(null);

					dialog.setTitle("Java DB Database Creation");
					dialog.setVisible(true);

				} catch (Exception f) {
					DBFactory.errorPrint(f);
				}
			}
		});
		mnCreate.add(mntmDBCreate);

		JMenuItem mntmNewMenuItem = new JMenuItem("Register Database...");
		mntmNewMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					DBCreationDialog dialog = new DBCreationDialog("", "", "", null, DBManager.derbySystemHome,
							"Register Database...");
					// DB Name, User, Pwd, Description, DB Location (File Location), Action command
					dialog.getTxtDBLocation().setText(DBManager.derbySystemHome);
					dialog.setLocationRelativeTo(null);

					dialog.setTitle("Java DB Database Registration");
					dialog.button.doClick();

					dialog.setVisible(true);

				} catch (Exception f) {
					DBFactory.errorPrint(f);
				}

			}
		});
		mnCreate.add(mntmNewMenuItem);

		JMenuItem mntmTableCrea = new JMenuItem("Create Table"); // Create table
		mntmTableCrea.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					TableCreaDialog dialog = new TableCreaDialog();
					Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
					dialog.pack();
					dialog.setSize(screenSize.width * 2 / 3, screenSize.height * 2 / 3);
					dialog.setLocationRelativeTo(null);
					dialog.setVisible(true);
					DBManager.dBtree.updateUI();
				} catch (Exception h) {
					h.printStackTrace();
				}

			}
		});
		mnCreate.add(mntmTableCrea);

		Component horizontalStrut_1 = Box.createHorizontalStrut(10);
		menuBar.add(horizontalStrut_1);

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
		treeView.setInheritsPopupMenu(true);
		leftPanel.add(treeView);

		JPanel rightPanel = new JPanel();
		splitPane.setRightComponent(rightPanel);
		rightPanel.setLayout(new BorderLayout(0, 0));

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		rightPanel.add(tabbedPane, BorderLayout.CENTER);

		execSQLPanel = new ExecSQLPanel();
		tabbedPane.addTab("Exec SQL", null, execSQLPanel, null);

		testPanel = new MyTypeInfoPanel();
		tabbedPane.addTab("Info Panel", null, testPanel, null);

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

	public JTextPane getTextPane() {
		return textPane;
	}

	/**
	 * @return the execSQLPanel
	 */
	public ExecSQLPanel getExecSQLPanel() {
		return execSQLPanel;
	}

	/**
	 * @return the testPanel
	 */
	public MyTypeInfoPanel getTestPanel() {
		return testPanel;
	}

	public static void expandAll() {

		expandAll(DBManager.dBtree, true);
	}

	public static void collapseAll(JTree tree) {
		int row = tree.getRowCount() - 1;
		while (row >= 0) {
			tree.collapseRow(row);
			row--;
		}
	}

	// If expand is true, expands all nodes in the tree.
	// Otherwise, collapses all nodes in the tree.
	public static void expandAll(JTree tree, boolean expand) {
		TreeNode root = (TreeNode) tree.getModel().getRoot();

		// Traverse tree from root
		expandAll(tree, new TreePath(root), expand);
	}

	private static void expandAll(JTree tree, TreePath parent, boolean expand) {
		// Traverse children
		TreeNode node = (TreeNode) parent.getLastPathComponent();
		if (node.getChildCount() >= 0) {
			for (Enumeration e = node.children(); e.hasMoreElements();) {
				TreeNode n = (TreeNode) e.nextElement();
				TreePath path = parent.pathByAddingChild(n);
				expandAll(tree, path, expand);
			}
		}

		// Expansion or collapse must be done bottom-up
		if (expand) {
			tree.expandPath(parent);
		} else {
			tree.collapsePath(parent);
		}
	}
}

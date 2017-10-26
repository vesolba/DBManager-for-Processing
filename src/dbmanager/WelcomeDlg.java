	package dbmanager;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

@SuppressWarnings("serial")
public class WelcomeDlg extends JDialog {
	private JTextField txtJavaExec;
	private JTextField txtJavaDBExec;
	private JTextField txtJdbcDerby;
	private JTextField txtDerbySysHome;
	private JTextField txtSketches;
	private JTextField txtProcessing;

	public int result = -1;

	public WelcomeDlg() {
		jbInit();
	}

	private void jbInit() {
		setPreferredSize(new Dimension(640, 400));
		setMaximumSize(new Dimension(10000, 10000));
		setMinimumSize(new Dimension(640, 400));
		setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/data/DBM4P3-32.png")));
		setModal(true);
		BorderLayout borderLayout = (BorderLayout) getContentPane().getLayout();
		borderLayout.setVgap(5);
		borderLayout.setHgap(5);
		setTitle("Welcome to DBManager for Processing");

		JPanel panelSup = new JPanel();
		getContentPane().add(panelSup, BorderLayout.NORTH);
		panelSup.setLayout(new CardLayout(20, 10));

		JTextArea txtAreaLegend = new JTextArea();
		txtAreaLegend.setEditable(false);
		txtAreaLegend.setWrapStyleWord(true);
		txtAreaLegend.setText(
				"Following, there are several directories defining where have been installed " +
				"the applications that we are using and its working environments. " +
				"Please, take a look and choose whether you agree or want to change any of the " +
				"indicated parameters that allow it. Then, press OK to continue or Cancel to exit.");
		txtAreaLegend.setLineWrap(true);
		txtAreaLegend.setFont(new Font("Verdana", Font.PLAIN, 12));
		panelSup.add(txtAreaLegend, "name_231839158424816");

		JPanel panCentral = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panCentral.getLayout();
		flowLayout.setHgap(60);
		getContentPane().add(panCentral, BorderLayout.CENTER);

		JPanel panContents = new JPanel();
		panCentral.add(panContents);
		GridBagLayout gbl_panContents = new GridBagLayout();
		gbl_panContents.columnWidths = new int[] { 30, 300, 30, 5 };
		gbl_panContents.rowHeights = new int[] { 35, 33, 33, 33, 33, 33 };
		gbl_panContents.columnWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		gbl_panContents.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
		panContents.setLayout(gbl_panContents);

		JLabel label = new JLabel("Java executables (java_home): ");
		label.setFont(new Font("Verdana", Font.PLAIN, 12));
		GridBagConstraints gbc_label = new GridBagConstraints();
		gbc_label.anchor = GridBagConstraints.EAST;
		gbc_label.fill = GridBagConstraints.VERTICAL;
		gbc_label.insets = new Insets(0, 0, 5, 5);
		gbc_label.gridx = 0;
		gbc_label.gridy = 0;
		panContents.add(label, gbc_label);

		txtJavaExec = new JTextField("");
		txtJavaExec.setEditable(false);
		txtJavaExec.setColumns(10);
		GridBagConstraints gbc_txtJavaExec = new GridBagConstraints();
		gbc_txtJavaExec.fill = GridBagConstraints.BOTH;
		gbc_txtJavaExec.insets = new Insets(0, 0, 5, 5);
		gbc_txtJavaExec.gridx = 1;
		gbc_txtJavaExec.gridy = 0;
		panContents.add(txtJavaExec, gbc_txtJavaExec);

		JLabel lblJavaDbExecs = new JLabel("Java DB execs. (derby_home): ");
		lblJavaDbExecs.setHorizontalAlignment(SwingConstants.RIGHT);
		lblJavaDbExecs.setFont(new Font("Verdana", Font.PLAIN, 12));
		GridBagConstraints gbc_lblJavaDbExecs = new GridBagConstraints();
		gbc_lblJavaDbExecs.fill = GridBagConstraints.BOTH;
		gbc_lblJavaDbExecs.insets = new Insets(0, 0, 5, 5);
		gbc_lblJavaDbExecs.gridx = 0;
		gbc_lblJavaDbExecs.gridy = 1;
		panContents.add(lblJavaDbExecs, gbc_lblJavaDbExecs);

		txtJavaDBExec = new JTextField("");
		txtJavaDBExec.setEditable(false);
		txtJavaDBExec.setColumns(10);
		GridBagConstraints gbc_txtJavaDBExec = new GridBagConstraints();
		gbc_txtJavaDBExec.fill = GridBagConstraints.BOTH;
		gbc_txtJavaDBExec.insets = new Insets(0, 0, 5, 5);
		gbc_txtJavaDBExec.gridx = 1;
		gbc_txtJavaDBExec.gridy = 1;
		panContents.add(txtJavaDBExec, gbc_txtJavaDBExec);

		JLabel lblDefaultForDbs = new JLabel("Default DBs (derby.system.home): ");
		lblDefaultForDbs.setHorizontalAlignment(SwingConstants.TRAILING);
		lblDefaultForDbs.setFont(new Font("Verdana", Font.PLAIN, 12));
		GridBagConstraints gbc_lblDefaultForDbs = new GridBagConstraints();
		gbc_lblDefaultForDbs.fill = GridBagConstraints.BOTH;
		gbc_lblDefaultForDbs.insets = new Insets(0, 0, 5, 5);
		gbc_lblDefaultForDbs.gridx = 0;
		gbc_lblDefaultForDbs.gridy = 2;
		panContents.add(lblDefaultForDbs, gbc_lblDefaultForDbs);

		txtDerbySysHome = new JTextField("");
		txtDerbySysHome.setColumns(10);
		GridBagConstraints gbc_txtDerbySysHome = new GridBagConstraints();
		gbc_txtDerbySysHome.fill = GridBagConstraints.BOTH;
		gbc_txtDerbySysHome.insets = new Insets(0, 0, 5, 5);
		gbc_txtDerbySysHome.gridx = 1;
		gbc_txtDerbySysHome.gridy = 2;
		panContents.add(txtDerbySysHome, gbc_txtDerbySysHome);

		JButton button = new JButton("Browse...");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				chooser.setCurrentDirectory(new java.io.File("."));
				int returnVal = chooser.showOpenDialog(WelcomeDlg.this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					// System.out.println("You chose to open this file:
					// " + chooser.getSelectedFile().getName());
					txtDerbySysHome.setText(chooser.getSelectedFile().getAbsolutePath());
				}
			}
		});
		GridBagConstraints gbc_button = new GridBagConstraints();
		gbc_button.insets = new Insets(0, 0, 5, 5);
		gbc_button.gridx = 2;
		gbc_button.gridy = 2;
		panContents.add(button, gbc_button);

		JLabel lblAlsoJdbcderby = new JLabel("Also jdbc:derby: & host:1527/: ");
		lblAlsoJdbcderby.setHorizontalAlignment(SwingConstants.TRAILING);
		lblAlsoJdbcderby.setFont(new Font("Verdana", Font.PLAIN, 12));
		GridBagConstraints gbc_lblAlsoJdbcderby = new GridBagConstraints();
		gbc_lblAlsoJdbcderby.fill = GridBagConstraints.BOTH;
		gbc_lblAlsoJdbcderby.insets = new Insets(0, 0, 5, 5);
		gbc_lblAlsoJdbcderby.gridx = 0;
		gbc_lblAlsoJdbcderby.gridy = 3;
		panContents.add(lblAlsoJdbcderby, gbc_lblAlsoJdbcderby);

		txtJdbcDerby = new JTextField("");
		txtJdbcDerby.setEditable(false);
		txtJdbcDerby.setColumns(10);
		GridBagConstraints gbc_txtJdbcDerby = new GridBagConstraints();
		gbc_txtJdbcDerby.fill = GridBagConstraints.BOTH;
		gbc_txtJdbcDerby.insets = new Insets(0, 0, 5, 5);
		gbc_txtJdbcDerby.gridx = 1;
		gbc_txtJdbcDerby.gridy = 3;
		panContents.add(txtJdbcDerby, gbc_txtJdbcDerby);

		JLabel lblProcessingHomeuserdir = new JLabel("Processing home (user.dir): ");
		lblProcessingHomeuserdir.setHorizontalAlignment(SwingConstants.TRAILING);
		lblProcessingHomeuserdir.setFont(new Font("Verdana", Font.PLAIN, 12));
		GridBagConstraints gbc_lblProcessingHomeuserdir = new GridBagConstraints();
		gbc_lblProcessingHomeuserdir.fill = GridBagConstraints.BOTH;
		gbc_lblProcessingHomeuserdir.insets = new Insets(0, 0, 5, 5);
		gbc_lblProcessingHomeuserdir.gridx = 0;
		gbc_lblProcessingHomeuserdir.gridy = 4;
		panContents.add(lblProcessingHomeuserdir, gbc_lblProcessingHomeuserdir);

		txtProcessing = new JTextField();
		txtProcessing.setEditable(false);
		txtProcessing.setColumns(10);
		GridBagConstraints gbc_txtProcessing = new GridBagConstraints();
		gbc_txtProcessing.fill = GridBagConstraints.BOTH;
		gbc_txtProcessing.insets = new Insets(0, 0, 5, 5);
		gbc_txtProcessing.gridx = 1;
		gbc_txtProcessing.gridy = 4;
		panContents.add(txtProcessing, gbc_txtProcessing);

		JLabel lblSketchesBook = new JLabel("Sketches Book: ");
		lblSketchesBook.setHorizontalAlignment(SwingConstants.TRAILING);
		lblSketchesBook.setFont(new Font("Verdana", Font.PLAIN, 12));
		GridBagConstraints gbc_lblSketchesBook = new GridBagConstraints();
		gbc_lblSketchesBook.fill = GridBagConstraints.BOTH;
		gbc_lblSketchesBook.insets = new Insets(0, 0, 0, 5);
		gbc_lblSketchesBook.gridx = 0;
		gbc_lblSketchesBook.gridy = 5;
		panContents.add(lblSketchesBook, gbc_lblSketchesBook);

		txtSketches = new JTextField();
		txtSketches.setEditable(false);
		txtSketches.setColumns(10);
		GridBagConstraints gbc_txtSketches = new GridBagConstraints();
		gbc_txtSketches.insets = new Insets(0, 0, 0, 5);
		gbc_txtSketches.fill = GridBagConstraints.BOTH;
		gbc_txtSketches.gridx = 1;
		gbc_txtSketches.gridy = 5;
		panContents.add(txtSketches, gbc_txtSketches);

		JPanel panelSur = new JPanel();
		getContentPane().add(panelSur, BorderLayout.SOUTH);
		panelSur.setLayout(new FlowLayout(FlowLayout.RIGHT, 40, 10));

		JPanel panel = new JPanel();
		panelSur.add(panel);
		panel.setLayout(new GridLayout(0, 2, 5, 0));

		JButton btnOK = new JButton("OK");
		btnOK.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				result = 0;
				setVisible(false);
				return;
			}
		});
		panel.add(btnOK);

		JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				result = -1;
				setVisible(false);
				return;
			}
		});
		panel.add(btnCancel);
		btnCancel.setHorizontalAlignment(SwingConstants.RIGHT);
		
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		pack();
		setSize(screenSize.width * 2 / 4, screenSize.height * 2 / 4);
		setLocationRelativeTo(null);
	}

	/**
	 * @return the txtJavaExec
	 */
	public JTextField getTxtJavaExec() {
		return txtJavaExec;
	}

	/**
	 * @return the txtJavaDBExec
	 */
	public JTextField getTxtJavaDBExec() {
		return txtJavaDBExec;
	}

	/**
	 * @return the txtJdbcDerby
	 */
	public JTextField getTxtJdbcDerby() {
		return txtJdbcDerby;
	}

	/**
	 * @return the txtDerbySysHome
	 */
	public JTextField getTxtDerbySysHome() {
		return txtDerbySysHome;
	}

	/**
	 * @return the txtSketches
	 */
	public JTextField getTxtSketches() {
		return txtSketches;
	}

	/**
	 * @return the txtProcessing
	 */
	public JTextField getTxtProcessing() {
		return txtProcessing;
	}

}

package dbmanager;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
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
import javax.swing.border.EmptyBorder;
import java.awt.Rectangle;
import javax.swing.BoxLayout;
import java.awt.GridLayout;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;
import net.miginfocom.swing.MigLayout;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import java.awt.ComponentOrientation;
import java.awt.Cursor;
import javax.swing.DropMode;

public class DBSettingsDialog extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private JTextField txtInstallation;
	private JTextField txtLocation;
	private JLabel lblDbInstallation;
	private JButton btnBrowseInst;
	private JLabel lblDBLocation;
	private JButton btnBrowseLocate;
	private JButton okButton;
	private JButton cancelButton;

	/**
	 * Create the dialog.
	 */
	public DBSettingsDialog() {
		jbInit();
	}

	private void jbInit() {
		getContentPane().setBounds(new Rectangle(20, 20, 20, 20));
		setModal(true);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setIconImage(Toolkit.getDefaultToolkit()
				.getImage(DBSettingsDialog.class.getResource("/javax/swing/plaf/basic/icons/image-delayed.png")));
		setTitle("Java DB Settings");
		setBounds(100, 100, 537, 264);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBounds(new Rectangle(50, 50, 50, 50));
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.NORTH);
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.X_AXIS));
		{
			JTextArea txtSpecify = new JTextArea();
			txtSpecify.setEnabled(false);
			txtSpecify.setEditable(false);
			txtSpecify.setDropMode(DropMode.INSERT);
			txtSpecify.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			txtSpecify.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
			txtSpecify.setColumns(1);
			txtSpecify.setWrapStyleWord(true);
			txtSpecify.setRows(1);
			txtSpecify.setFont(new Font("Monospaced", Font.PLAIN, 11));
			txtSpecify.setLineWrap(true);
			txtSpecify.setText("Specify the folder where Java DB is installed and the folder "
					+ "where you will keep your databases. The database location folder will be used "
					+ "as the value of the derby.system.home property.");
			contentPanel.add(txtSpecify);
		}
		{
			JPanel panel = new JPanel();
			getContentPane().add(panel, BorderLayout.WEST);
			{
				lblDbInstallation = new JLabel("Java DB Installation: ");
				lblDbInstallation.setHorizontalAlignment(SwingConstants.RIGHT);
			}
			{
				txtInstallation = new JTextField();
				txtInstallation.setEnabled(false);
				txtInstallation.setEditable(false);
				txtInstallation.setHorizontalAlignment(SwingConstants.LEFT);
				txtInstallation.setColumns(10);
			}
			{
				btnBrowseInst = new JButton("Browse...");
				btnBrowseInst.setEnabled(false);
				btnBrowseInst.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						JFileChooser chooser = new JFileChooser();
						chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
						chooser.setCurrentDirectory(new java.io.File("."));
						int returnVal = chooser.showOpenDialog(DBSettingsDialog.this);
						if (returnVal == JFileChooser.APPROVE_OPTION) {
							// System.out.println("You chose to open this file:
							// " + chooser.getSelectedFile().getName());
							txtInstallation.setText(chooser.getSelectedFile().getName());
						}

					}
				});
			}
			{
				lblDBLocation = new JLabel("Database Location:\r\n ");
				lblDBLocation.setHorizontalAlignment(SwingConstants.RIGHT);
			}
			{
				txtLocation = new JTextField();
				txtLocation.setColumns(10);
			}
			{
				btnBrowseLocate = new JButton("Browse...");
				btnBrowseLocate.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						JFileChooser chooser = new JFileChooser();
						chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
						chooser.setCurrentDirectory(new java.io.File("."));
						int returnVal = chooser.showOpenDialog(DBSettingsDialog.this);
						if (returnVal == JFileChooser.APPROVE_OPTION) {
							// System.out.println("You chose to open this file:
							// " + chooser.getSelectedFile().getName());
							txtLocation.setText(chooser.getSelectedFile().getName());
						}
					}
				});
			}
			{
				okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						setVisible(false);

					}
				});
				okButton.setActionCommand("OK");
				getRootPane().setDefaultButton(okButton);
			}
			{
				cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						setVisible(false);
						dispose();
					}
				});
				cancelButton.setActionCommand("Cancel");
			}
			panel.setLayout(new FormLayout(
					new ColumnSpec[] { ColumnSpec.decode("25px"), ColumnSpec.decode("105px"),
							FormSpecs.UNRELATED_GAP_COLSPEC, ColumnSpec.decode("273px"), FormSpecs.RELATED_GAP_COLSPEC,
							ColumnSpec.decode("79px"), ColumnSpec.decode("25px"), },
					new RowSpec[] { RowSpec.decode("21px"), RowSpec.decode("23px"), FormSpecs.RELATED_GAP_ROWSPEC,
							RowSpec.decode("23px"), RowSpec.decode("48px"), RowSpec.decode("23px"), }));
			panel.add(lblDbInstallation, "2, 2, fill, center");
			panel.add(txtInstallation, "4, 2, fill, center");
			panel.add(lblDBLocation, "2, 4, fill, center");
			panel.add(txtLocation, "4, 4, fill, center");
			panel.add(okButton, "4, 6, right, top");
			panel.add(btnBrowseLocate, "6, 4, left, top");
			panel.add(btnBrowseInst, "6, 2, left, top");
			panel.add(cancelButton, "6, 6, fill, top");
		}
	}

	/**
	 * @return the txtInstallation
	 */
	public JTextField getTxtInstallation() {
		return txtInstallation;
	}

	/**
	 * @param txtInstallation
	 *            the txtInstallation to set
	 */
	public void setTxtInstallation(JTextField txtInstallation) {
		this.txtInstallation = txtInstallation;
	}

	/**
	 * @return the txtLocation
	 */
	public JTextField getTxtLocation() {
		return txtLocation;
	}

	/**
	 * @param txtLocation
	 *            the txtLocation to set
	 */
	public void setTxtLocation(JTextField txtLocation) {
		this.txtLocation = txtLocation;
	}

}

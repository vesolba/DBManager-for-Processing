package dbmanager;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

public class AddColumnDlg extends JDialog {

	private JFormattedTextField textName;
	private JFormattedTextField textSize;
	private JTextField txtDefValue;
	private JFormattedTextField textScale;
	private JComboBox<MyColumnTypes> comboType;
	private JButton btnOK;
	public int result = -1;
	private JCheckBox chkbxPrimKey;
	private JCheckBox chkbxUnique;
	private JCheckBox chkbxNull;
	private JCheckBox chkbxIndex;
	private JCheckBox chkbxCheck;
	private JTextArea textCheck;

	public AddColumnDlg() {
		jbInit();
	}

	private void jbInit() {
		setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/data/DBM4P3-32.png")));
		setBounds(new Rectangle(0, 0, 600, 400));
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setTitle("Add column");
		setModal(true);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 55, 15, 157, 55, 71, 97, 0 };
		gridBagLayout.rowHeights = new int[] { 10, 28, 28, 28, 28, 57, 52, 31, 23, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		getContentPane().setLayout(gridBagLayout);

		JLabel lblName = new JLabel("Name: ");
		lblName.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_lblName = new GridBagConstraints();
		gbc_lblName.fill = GridBagConstraints.BOTH;
		gbc_lblName.insets = new Insets(0, 0, 5, 5);
		gbc_lblName.gridwidth = 2;
		gbc_lblName.gridx = 0;
		gbc_lblName.gridy = 1;
		getContentPane().add(lblName, gbc_lblName);

		textName = new JFormattedTextField();
		textName.setColumns(10);
		GridBagConstraints gbc_textName = new GridBagConstraints();
		gbc_textName.fill = GridBagConstraints.HORIZONTAL;
		gbc_textName.insets = new Insets(0, 0, 5, 0);
		gbc_textName.gridwidth = 4;
		gbc_textName.gridx = 2;
		gbc_textName.gridy = 1;
		getContentPane().add(textName, gbc_textName);

		JLabel lblType = new JLabel("Type: ");
		lblType.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_lblType = new GridBagConstraints();
		gbc_lblType.fill = GridBagConstraints.BOTH;
		gbc_lblType.insets = new Insets(0, 0, 5, 5);
		gbc_lblType.gridwidth = 2;
		gbc_lblType.gridx = 0;
		gbc_lblType.gridy = 2;
		getContentPane().add(lblType, gbc_lblType);

		comboType = new JComboBox<MyColumnTypes>();
		comboType.setAutoscrolls(true);
		comboType.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String parameters = ((MyColumnTypes) comboType.getSelectedItem()).CREATE_PARAMS;

				if (parameters == null || parameters.equals("")) {
					textSize.setEnabled(false);
					textScale.setEnabled(false);
				} else if (parameters.equals("length") || parameters.equals("precision")) {
					textSize.setEnabled(true);
					textScale.setEnabled(false);
				} else if (parameters.equals("precision,scale")) {
					textSize.setEnabled(true);
					textScale.setEnabled(true);
				}

				if (!textName.getText().equals("")) {
					btnOK.setEnabled(true);
				}

			}
		});
		comboType.setEditable(false);

		GridBagConstraints gbc_comboType = new GridBagConstraints();
		gbc_comboType.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboType.insets = new Insets(0, 0, 5, 0);
		gbc_comboType.gridwidth = 4;
		gbc_comboType.gridx = 2;
		gbc_comboType.gridy = 2;
		getContentPane().add(comboType, gbc_comboType);

		JLabel lblSize = new JLabel("Size: ");
		lblSize.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_lblSize = new GridBagConstraints();
		gbc_lblSize.fill = GridBagConstraints.BOTH;
		gbc_lblSize.insets = new Insets(0, 0, 5, 5);
		gbc_lblSize.gridwidth = 2;
		gbc_lblSize.gridx = 0;
		gbc_lblSize.gridy = 3;
		getContentPane().add(lblSize, gbc_lblSize);

		textSize = new JFormattedTextField(new Integer(0));
		textSize.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!textName.getText().equals("")) {
					btnOK.setEnabled(true);
				}
			}
		});
		textSize.setText("");
		textSize.setColumns(10);
		GridBagConstraints gbc_textSize = new GridBagConstraints();
		gbc_textSize.fill = GridBagConstraints.HORIZONTAL;
		gbc_textSize.insets = new Insets(0, 0, 5, 5);
		gbc_textSize.gridx = 2;
		gbc_textSize.gridy = 3;
		getContentPane().add(textSize, gbc_textSize);

		JLabel label = new JLabel("Scale: ");
		label.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_label = new GridBagConstraints();
		gbc_label.fill = GridBagConstraints.BOTH;
		gbc_label.insets = new Insets(0, 0, 5, 5);
		gbc_label.gridx = 3;
		gbc_label.gridy = 3;
		getContentPane().add(label, gbc_label);

		textScale = new JFormattedTextField(0);
		textScale.setColumns(10);
		GridBagConstraints gbc_textScale = new GridBagConstraints();
		gbc_textScale.fill = GridBagConstraints.HORIZONTAL;
		gbc_textScale.insets = new Insets(0, 0, 5, 0);
		gbc_textScale.gridwidth = 2;
		gbc_textScale.gridx = 4;
		gbc_textScale.gridy = 3;
		getContentPane().add(textScale, gbc_textScale);

		JLabel lblDefault = new JLabel("Default: ");
		lblDefault.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_lblDefault = new GridBagConstraints();
		gbc_lblDefault.fill = GridBagConstraints.BOTH;
		gbc_lblDefault.insets = new Insets(0, 0, 5, 5);
		gbc_lblDefault.gridwidth = 2;
		gbc_lblDefault.gridx = 0;
		gbc_lblDefault.gridy = 4;
		getContentPane().add(lblDefault, gbc_lblDefault);

		txtDefValue = new JTextField();
		txtDefValue.setColumns(10);
		GridBagConstraints gbc_txtDefValue = new GridBagConstraints();
		gbc_txtDefValue.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtDefValue.insets = new Insets(0, 0, 5, 0);
		gbc_txtDefValue.gridwidth = 4;
		gbc_txtDefValue.gridx = 2;
		gbc_txtDefValue.gridy = 4;
		getContentPane().add(txtDefValue, gbc_txtDefValue);

		Box horizontalBox = Box.createHorizontalBox();
		horizontalBox.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "Constraints",
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		horizontalBox.setToolTipText("ttrt");

		chkbxPrimKey = new JCheckBox("Primary key");
		chkbxPrimKey.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (chkbxPrimKey.isSelected()) {
					chkbxUnique.setSelected(true);
					chkbxUnique.setEnabled(true);

					chkbxNull.setSelected(false);
					chkbxNull.setEnabled(true);

					chkbxIndex.setSelected(true);
					chkbxIndex.setEnabled(false);
				}

				if (!textName.getText().equals("")) {
					btnOK.setEnabled(true);
				}

			}
		});
		chkbxPrimKey.setIconTextGap(10);
		chkbxPrimKey.setMargin(new Insets(0, 50, 0, 0));
		horizontalBox.add(chkbxPrimKey);

		chkbxUnique = new JCheckBox("Unique");
		chkbxUnique.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (chkbxUnique.isSelected()) {
					chkbxIndex.setSelected(true);
					chkbxIndex.setEnabled(false);
				} else {
					chkbxIndex.setEnabled(true);
				}
				if (!textName.getText().equals("")) {
					btnOK.setEnabled(true);
				}

			}
		});
		chkbxUnique.setIconTextGap(10);
		chkbxUnique.setLocation(new Point(20, 0));
		chkbxUnique.setMargin(new Insets(0, 40, 0, 0));
		horizontalBox.add(chkbxUnique);

		chkbxNull = new JCheckBox("Null");
		chkbxNull.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (chkbxNull.isSelected()) {
					chkbxPrimKey.setSelected(false);
				}
			}
		});
		chkbxNull.setIconTextGap(10);
		chkbxNull.setLocation(new Point(20, 0));
		chkbxNull.setMargin(new Insets(0, 40, 0, 0));
		chkbxNull.setSelected(true);
		horizontalBox.add(chkbxNull);

		chkbxIndex = new JCheckBox("Index");
		chkbxIndex.setIconTextGap(10);
		chkbxIndex.setLocation(new Point(20, 0));
		chkbxIndex.setMargin(new Insets(0, 40, 0, 0));
		horizontalBox.add(chkbxIndex);
		GridBagConstraints gbc_horizontalBox = new GridBagConstraints();
		gbc_horizontalBox.fill = GridBagConstraints.BOTH;
		gbc_horizontalBox.insets = new Insets(0, 0, 5, 0);
		gbc_horizontalBox.gridwidth = 6;
		gbc_horizontalBox.gridx = 0;
		gbc_horizontalBox.gridy = 5;
		getContentPane().add(horizontalBox, gbc_horizontalBox);

		chkbxCheck = new JCheckBox("Check");
		GridBagConstraints gbc_chkbxCheck = new GridBagConstraints();
		gbc_chkbxCheck.anchor = GridBagConstraints.NORTHWEST;
		gbc_chkbxCheck.insets = new Insets(0, 0, 5, 5);
		gbc_chkbxCheck.gridx = 0;
		gbc_chkbxCheck.gridy = 6;
		getContentPane().add(chkbxCheck, gbc_chkbxCheck);

		textCheck = new JTextArea();
		textCheck.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		textCheck.setWrapStyleWord(true);
		GridBagConstraints gbc_textCheck = new GridBagConstraints();
		gbc_textCheck.fill = GridBagConstraints.BOTH;
		gbc_textCheck.insets = new Insets(0, 0, 5, 0);
		gbc_textCheck.gridwidth = 5;
		gbc_textCheck.gridx = 1;
		gbc_textCheck.gridy = 6;
		getContentPane().add(textCheck, gbc_textCheck);

		btnOK = new JButton("OK");
		btnOK.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				result = 0;
				setVisible(false);
				return;
			}
		});
		btnOK.setEnabled(false);
		GridBagConstraints gbc_btnOK = new GridBagConstraints();
		gbc_btnOK.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnOK.anchor = GridBagConstraints.NORTH;
		gbc_btnOK.insets = new Insets(0, 0, 5, 5);
		gbc_btnOK.gridwidth = 2;
		gbc_btnOK.gridx = 3;
		gbc_btnOK.gridy = 8;
		getContentPane().add(btnOK, gbc_btnOK);

		JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				result = -1;
				setVisible(false);
				return;
			}
		});
		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.insets = new Insets(0, 0, 5, 0);
		gbc_btnCancel.anchor = GridBagConstraints.NORTH;
		gbc_btnCancel.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnCancel.gridx = 5;
		gbc_btnCancel.gridy = 8;
		getContentPane().add(btnCancel, gbc_btnCancel);
	}

	/**
	 * @return the comboType
	 */
	public JComboBox<MyColumnTypes> getComboType() {
		return comboType;
	}

	class DBNumberVerifier extends InputVerifier {
		public boolean verify(JComponent input) {
			JTextField tf = (JTextField) input;
			return "pass".equals(tf.getText());
		}
	}

	public JTextField getTextName() {
		return textName;
	}

	public JFormattedTextField getTextSize() {
		return textSize;
	}

	public JFormattedTextField getTextScale() {
		return textScale;
	}

	public JCheckBox getChkbxPrimKey() {
		return chkbxPrimKey;
	}

	public JCheckBox getChkbxUnique() {
		return chkbxUnique;
	}

	public JCheckBox getChkbxNull() {
		return chkbxNull;
	}

	public JCheckBox getChkbxIndex() {
		return chkbxIndex;
	}

	public JCheckBox getChkbxCheck() {
		return chkbxCheck;
	}

	public JTextArea getTextCheck() {
		return textCheck;
	}

	public JTextField getTxtDefValue() {
		return txtDefValue;
	}
}

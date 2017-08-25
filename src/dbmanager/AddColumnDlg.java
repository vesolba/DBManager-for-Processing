package dbmanager;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

public class AddColumnDlg extends JDialog {

	private JFormattedTextField textName;
	private JFormattedTextField textSize;
	private JTextField textDefault;
	private JFormattedTextField textScale;
	private JComboBox<MyColumnTypes> comboType;
	private JButton btnOK;
	public int result = -1;
	private JCheckBox chkbxPrimKey;
	private JCheckBox chkbxUnique;
	private JCheckBox chkbxNotNull;
	private JCheckBox chkbxIndex;
	private final ButtonGroup buttonGroup = new ButtonGroup();
	private JLabel label;
	private JPanel panelUpper;
	private JPanel panelAuto;
	private JPanel panelButtons;
	private JPanel panel_3;
	private JPanel panel_4;
	private JPanel panel_5;
	private JPanel panel_6;
	private JPanel panel_7;
	private JPanel panel_8;
	private JPanel panelForeign;
	private JPanel panelCheck;
	private Component horizontalStrut;
	private JCheckBox chkbxAutoinc;
	private JPanel panel_9;
	private JLabel label_1;
	private JLabel label_2;
	private JPanel panel_12;
	private JLabel label_3;
	private JFormattedTextField txtInitValue;
	private JPanel panel_13;
	private JLabel label_4;
	private JFormattedTextField txtIncrement;
	private JPanel panel_14;
	private JCheckBox chkbxCycle;
	private JLabel label_5;
	private JCheckBox chkbxForeign;
	private JPanel panel_15;
	private JLabel label_6;
	private JFormattedTextField txtConstName;
	private JPanel panel_16;
	private JLabel label_7;
	private JTextField txtRefTable;
	private JPanel panel_17;
	private JLabel label_8;
	private JTextField txtColNames;
	private JPanel panel_18;
	private JCheckBox chkbxOnDelete;
	private JComboBox<MyColumnTypes> comboOnDelete;
	private JLabel label_9;
	private JPanel panel_19;
	private JCheckBox chkbxOnUpdate;
	private JComboBox<MyColumnTypes> comboOnUpdate;
	private JCheckBox chkbxCheck;
	private JPanel panel_20;
	private JLabel lblConstraintName;
	private JTextField txtConstCheck;
	private JPanel panel_21;
	private JLabel label_11;
	private JEditorPane editorPaneConditions;
	private JLabel label_12;
	private JComboBox comboGenerated;
	private JSeparator separator;
	private JSeparator separator_1;
	private JSeparator separator_2;
	private JSeparator separator_3;
	private JPanel panel;
	private JPanel panel_1;
	private JPanel panel_2;

	public AddColumnDlg() {
		jbInit();
	}

	private void jbInit() {

		setFont(new Font("Tahoma", Font.PLAIN, 14));

		setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/data/DBM4P3-32.png")));
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setTitle("Add column");
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

		panelUpper = new JPanel();
		panelUpper.setPreferredSize(new Dimension(1000, 50));
		panelUpper.setMinimumSize(new Dimension(1000, 50));
		panelUpper.setMaximumSize(new Dimension(20000, 20000));
		getContentPane().add(panelUpper);

		panel_3 = new JPanel();
		panelUpper.add(panel_3);

		JLabel lblName = new JLabel("Name: ");
		panel_3.add(lblName);
		lblName.setHorizontalAlignment(SwingConstants.RIGHT);

		textName = new JFormattedTextField();
		textName.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent arg0) {
				if (textName.getText().isEmpty()) {
					comboType.setEnabled(false);
					btnOK.setEnabled(false);
				} else {
					comboType.setEnabled(true);
					btnOK.setEnabled(true);
				}
			}
		});
		TextPrompt tptextName = new TextPrompt("Write a name", textName);
		tptextName.changeAlpha(0.5f);
		textName.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent arg0) {
				if (textName.getText().isEmpty()) {
					comboType.setEnabled(false);

				} else {
					comboType.setEnabled(true);
				}
			}
		});
		panel_3.add(textName);
		textName.setColumns(20);

		panel_4 = new JPanel();
		panelUpper.add(panel_4);

		JLabel lblType = new JLabel("Type: ");
		panel_4.add(lblType);
		lblType.setHorizontalAlignment(SwingConstants.RIGHT);

		comboType = new JComboBox<MyColumnTypes>();
		comboType.setEnabled(false);
		comboType.setPreferredSize(new Dimension(200, 20));
		comboType.setMaximumSize(new Dimension(1000, 1000));
		panel_4.add(comboType);
		comboType.setAutoscrolls(true);

		comboType.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String parameters = ((MyColumnTypes) comboType.getSelectedItem()).CREATE_PARAMS;

				textSize.setEnabled(false);
				textScale.setEnabled(false);
				if (parameters != null && !parameters.equals("")) {
					if (parameters.contains("length") || parameters.contains("precision")) {
						textSize.setEnabled(true);
					} else {
						textSize.setEnabled(false);
					}
					if (parameters.contains("scale")) {
						textScale.setEnabled(true);
					} else {
						textScale.setEnabled(false);
					}
				}

				// if (comboType.getSelectedItem().toString().equals("CLOB")) {
				// getTextDefault().setText("EMPTY_CLOB()");

				// }

			}
		});
		comboType.setEditable(true);

		panel = new JPanel();
		panelUpper.add(panel);

		panel_5 = new JPanel();
		panel.add(panel_5);

		JLabel lblSize = new JLabel("Size: ");
		panel_5.add(lblSize);
		lblSize.setHorizontalAlignment(SwingConstants.RIGHT);

		textSize = new JFormattedTextField(new Integer(0));
		textSize.setEnabled(false);
		textSize.setHorizontalAlignment(SwingConstants.LEFT);
		panel_5.add(textSize);
		textSize.setText("");
		textSize.setColumns(10);

		panel_6 = new JPanel();
		panel.add(panel_6);

		label = new JLabel("Scale: ");
		panel_6.add(label);
		label.setSize(new Dimension(1000, 0));
		label.setHorizontalAlignment(SwingConstants.CENTER);

		textScale = new JFormattedTextField(0);
		textScale.setEnabled(false);
		textScale.setText("");
		panel_6.add(textScale);
		textScale.setColumns(10);

		panel_7 = new JPanel();
		panel.add(panel_7);

		JLabel lblDefault = new JLabel("Default: ");
		panel_7.add(lblDefault);
		lblDefault.setHorizontalAlignment(SwingConstants.RIGHT);

		textDefault = new JTextField();
		panel_7.add(textDefault);
		textDefault.setColumns(10);

		panel_8 = new JPanel();
		panelUpper.add(panel_8);

		chkbxPrimKey = new JCheckBox("Primary key");
		chkbxPrimKey.setEnabled(false);
		panel_8.add(chkbxPrimKey);
		chkbxPrimKey.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (chkbxPrimKey.isSelected()) {
					chkbxUnique.setSelected(true);
					chkbxUnique.setEnabled(true);

					chkbxNotNull.setSelected(false);
					chkbxNotNull.setEnabled(true);

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

		chkbxUnique = new JCheckBox("Unique");
		chkbxUnique.setEnabled(false);
		panel_8.add(chkbxUnique);
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

		chkbxNotNull = new JCheckBox("Null");
		chkbxNotNull.setEnabled(false);
		panel_8.add(chkbxNotNull);
		chkbxNotNull.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (chkbxNotNull.isSelected()) {
					chkbxPrimKey.setSelected(false);
				}
			}
		});
		chkbxNotNull.setIconTextGap(10);
		chkbxNotNull.setLocation(new Point(20, 0));
		chkbxNotNull.setMargin(new Insets(0, 40, 0, 0));
		chkbxNotNull.setSelected(true);

		chkbxIndex = new JCheckBox("Index");
		chkbxIndex.setEnabled(false);
		panel_8.add(chkbxIndex);
		chkbxIndex.setIconTextGap(10);
		chkbxIndex.setLocation(new Point(20, 0));
		chkbxIndex.setMargin(new Insets(0, 40, 0, 0));

		separator = new JSeparator();
		separator.setMaximumSize(new Dimension(32767, 0));
		separator.setAlignmentY(Component.TOP_ALIGNMENT);
		getContentPane().add(separator);

		panelAuto = new JPanel();
		panelAuto.setPreferredSize(new Dimension(1000, 15));
		panelAuto.setMinimumSize(new Dimension(1000, 5));
		panelAuto.setMaximumSize(new Dimension(20000, 20000));
		getContentPane().add(panelAuto);

		chkbxAutoinc = new JCheckBox("Identity Generation");
		chkbxAutoinc.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				if (comboGenerated != null) {
					if (chkbxAutoinc.isSelected()) {
						comboGenerated.setEnabled(true);
						txtInitValue.setEnabled(true);
						txtIncrement.setEnabled(true);
						chkbxCycle.setEnabled(true);
					} else {
						comboGenerated.setEnabled(false);
						txtInitValue.setEnabled(false);
						txtIncrement.setEnabled(false);
						chkbxCycle.setEnabled(false);
					}
				}
			}
		});
		panelAuto.add(chkbxAutoinc);

		panel_9 = new JPanel();
		panelAuto.add(panel_9);

		label_1 = new JLabel("    ... GENERATED ");
		panel_9.add(label_1);

		comboGenerated = new JComboBox();
		comboGenerated.setEnabled(false);
		comboGenerated.setModel(new DefaultComboBoxModel(new String[] { "BY DEFAULT", "ALWAYS" }));
		comboGenerated.setMaximumRowCount(20);

		panel_9.add(comboGenerated);

		label_2 = new JLabel(" AS IDENTITY ");
		panel_9.add(label_2);

		panel_1 = new JPanel();
		panel_1.setBorder(new TitledBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null), "Autoincrement",
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panelAuto.add(panel_1);

		panel_12 = new JPanel();
		panel_1.add(panel_12);

		label_3 = new JLabel("(START WITH \r\n");
		panel_12.add(label_3);

		txtInitValue = new JFormattedTextField();
		txtInitValue.setEnabled(false);
		txtInitValue.setText("0");
		txtInitValue.setColumns(10);
		panel_12.add(txtInitValue);

		panel_13 = new JPanel();
		panel_1.add(panel_13);

		label_4 = new JLabel(", INCREMENT BY ");
		panel_13.add(label_4);

		txtIncrement = new JFormattedTextField();
		txtIncrement.setText("0");
		txtIncrement.setEnabled(false);
		txtIncrement.setColumns(10);
		panel_13.add(txtIncrement);

		panel_14 = new JPanel();
		panel_1.add(panel_14);

		chkbxCycle = new JCheckBox(", CYCLE");
		chkbxCycle.setEnabled(false);
		panel_14.add(chkbxCycle);

		label_5 = new JLabel(")");
		panel_14.add(label_5);

		separator_1 = new JSeparator();
		separator_1.setMaximumSize(new Dimension(32767, 0));
		getContentPane().add(separator_1);

		panelForeign = new JPanel();
		panelForeign.setPreferredSize(new Dimension(1000, 50));
		panelForeign.setMinimumSize(new Dimension(1000, 50));
		panelForeign.setMaximumSize(new Dimension(20000, 20000));
		getContentPane().add(panelForeign);

		chkbxForeign = new JCheckBox("Foreign Key");
		chkbxForeign.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				if (txtConstName != null) {
					if (chkbxForeign.isSelected()) {
						txtConstName.setEnabled(true);
						txtRefTable.setEnabled(true);
						txtColNames.setEnabled(true);
						chkbxOnDelete.setEnabled(true);
						chkbxOnUpdate.setEnabled(true);
					} else {
						txtConstName.setEnabled(false);
						txtRefTable.setEnabled(false);
						txtColNames.setEnabled(false);
						chkbxOnDelete.setEnabled(false);
						chkbxOnUpdate.setEnabled(false);
						comboOnDelete.setEnabled(false);
						comboOnUpdate.setEnabled(false);
					}
				}
			}
		});
		chkbxForeign.setVerticalAlignment(SwingConstants.TOP);
		chkbxForeign.setHorizontalAlignment(SwingConstants.LEFT);
		panelForeign.add(chkbxForeign);

		panel_15 = new JPanel();
		panelForeign.add(panel_15);

		label_6 = new JLabel("... CONSTRAINT ");
		panel_15.add(label_6);

		txtConstName = new JFormattedTextField();
		txtConstName.setEnabled(false);
		txtConstName.setEditable(false);
		txtConstName.setColumns(10);
		TextPrompt tptxtConstName = new TextPrompt("Constraint Name (optional)", txtConstName);
		tptxtConstName.setEnabled(false);
		tptxtConstName.changeAlpha(0.5f);
		panel_15.add(txtConstName);

		panel_16 = new JPanel();
		panelForeign.add(panel_16);

		label_7 = new JLabel(" REFERENCES ");
		panel_16.add(label_7);

		txtRefTable = new JTextField();
		txtRefTable.setEnabled(false);
		txtRefTable.setEditable(false);
		txtRefTable.setColumns(10);
		TextPrompt tptxtRefTable = new TextPrompt("Referenced table name", txtRefTable);
		tptxtRefTable.setEnabled(false);
		tptxtRefTable.changeAlpha(0.5f);
		panel_16.add(txtRefTable);

		panel_17 = new JPanel();
		panelForeign.add(panel_17);

		label_8 = new JLabel("(");
		panel_17.add(label_8);

		txtColNames = new JTextField();
		txtColNames.setEditable(false);
		txtColNames.setColumns(30);
		TextPrompt tptxtColNames = new TextPrompt("Comma separated column names", txtColNames);
		tptxtColNames.changeAlpha(0.5f);
		panel_17.add(txtColNames);

		label_9 = new JLabel(")");
		panel_17.add(label_9);

		panel_2 = new JPanel();
		panelForeign.add(panel_2);

		panel_18 = new JPanel();
		panel_2.add(panel_18);

		chkbxOnDelete = new JCheckBox("ON DELETE  ");

		chkbxOnDelete.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				if (comboOnDelete != null) {
					if (chkbxOnDelete.isSelected()) {
						comboOnDelete.setEnabled(true);
					} else {
						comboOnDelete.setEnabled(false);
					}
				}
			}
		});
		chkbxOnDelete.setEnabled(false);
		panel_18.add(chkbxOnDelete);

		comboOnDelete = new JComboBox();
		comboOnDelete
				.setModel(new DefaultComboBoxModel(new String[] { "NO ACTION", "RESTRICT", "CASCADE", "SET NULL" }));
		panel_18.add(comboOnDelete);

		panel_19 = new JPanel();
		panel_2.add(panel_19);

		chkbxOnUpdate = new JCheckBox("ON UPDATE ");
		chkbxOnUpdate.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				if (comboOnUpdate != null) {
					if (chkbxOnUpdate.isSelected()) {
						comboOnUpdate.setEnabled(true);
					} else {
						comboOnUpdate.setEnabled(false);
					}
				}
			}
		});
		chkbxOnUpdate.setEnabled(false);
		panel_19.add(chkbxOnUpdate);

		comboOnUpdate = new JComboBox();
		comboOnUpdate.setEnabled(false);
		comboOnUpdate.setModel(new DefaultComboBoxModel(new String[] { "NO ACTION", "RESTRICT" }));
		panel_19.add(comboOnUpdate);

		separator_2 = new JSeparator();
		separator_2.setMaximumSize(new Dimension(32767, 0));
		getContentPane().add(separator_2);

		panelCheck = new JPanel();
		panelCheck.setPreferredSize(new Dimension(1000, 50));
		panelCheck.setMinimumSize(new Dimension(1000, 50));
		panelCheck.setMaximumSize(new Dimension(20000, 20000));
		getContentPane().add(panelCheck);

		chkbxCheck = new JCheckBox("Check");
		chkbxCheck.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				if (txtConstCheck != null) {
					if (chkbxCheck.isSelected()) {
						txtConstCheck.setEnabled(true);
						editorPaneConditions.setEnabled(true);
					} else {
						txtConstCheck.setEnabled(false);
						editorPaneConditions.setEnabled(false);
					}
				}
			}
		});
		chkbxCheck.setVerticalAlignment(SwingConstants.TOP);
		chkbxCheck.setHorizontalAlignment(SwingConstants.LEFT);
		panelCheck.add(chkbxCheck);

		panel_20 = new JPanel();
		panelCheck.add(panel_20);

		lblConstraintName = new JLabel("... CONSTRAINT   Name: ");
		panel_20.add(lblConstraintName);

		txtConstCheck = new JTextField();
		txtConstCheck.setEnabled(false);
		txtConstCheck.setColumns(10);
		panel_20.add(txtConstCheck);

		panel_21 = new JPanel();
		panelCheck.add(panel_21);

		label_11 = new JLabel("Conditions: ( ");
		panel_21.add(label_11);

		editorPaneConditions = new JEditorPane();
		editorPaneConditions
				.setToolTipText("Examples: (salary >= 10000), (BONUS > TAX), (MEAL IN ('B', 'L', 'D', 'S'))");
		editorPaneConditions.setPreferredSize(new Dimension(500, 50));
		editorPaneConditions.setMinimumSize(new Dimension(1000, 40));
		editorPaneConditions.setMaximumSize(new Dimension(20000, 20000));
		editorPaneConditions.setEnabled(false);
		editorPaneConditions.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
		panel_21.add(editorPaneConditions);

		label_12 = new JLabel(" ) ");
		panel_21.add(label_12);

		separator_3 = new JSeparator();
		separator_3.setMaximumSize(new Dimension(32767, 0));
		getContentPane().add(separator_3);

		panelButtons = new JPanel();
		panelButtons.setPreferredSize(new Dimension(1000, 10));
		panelButtons.setMinimumSize(new Dimension(1000, 10));
		panelButtons.setMaximumSize(new Dimension(20000, 10000));
		getContentPane().add(panelButtons);
		panelButtons.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5));

		btnOK = new JButton("OK");
		panelButtons.add(btnOK);
		btnOK.setPreferredSize(new Dimension(90, 30));
		btnOK.setMinimumSize(new Dimension(10, 10));
		btnOK.setMaximumSize(new Dimension(100, 100));
		btnOK.setAlignmentX(0.5f);
		btnOK.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				result = 0;
				setVisible(false);
				// return;
			}
		});
		btnOK.setEnabled(false);

		JButton btnCancel = new JButton("Cancel");
		btnCancel.setPreferredSize(new Dimension(90, 30));
		btnCancel.setMinimumSize(new Dimension(10, 10));
		btnCancel.setMaximumSize(new Dimension(100, 100));
		panelButtons.add(btnCancel);

		horizontalStrut = Box.createHorizontalStrut(20);
		horizontalStrut.setPreferredSize(new Dimension(80, 0));
		horizontalStrut.setMinimumSize(new Dimension(40, 0));
		horizontalStrut.setMaximumSize(new Dimension(100, 0));
		panelButtons.add(horizontalStrut);
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				result = -1;
				setVisible(false);
				// return;
			}
		});
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
		return chkbxNotNull;
	}

	public JCheckBox getChkbxIndex() {
		return chkbxIndex;
	}

	public JCheckBox getChkbxCheck() {
		return chkbxCheck;
	}

	public JTextField getTxtDefValue() {
		return textDefault;
	}

	/**
	 * @return the chckbxAutoinc
	 */
	public JCheckBox getChckbxAutoinc() {
		return chkbxAutoinc;
	}

	/**
	 * @return the chckbxCycle
	 */
	public JCheckBox getChckbxCycle() {
		return chkbxCycle;
	}

	/**
	 * @return the textIncrement
	 */
	public JTextField getTextIncrement() {
		return txtIncrement;
	}

	/**
	 * @return the buttonGroup
	 */
	public ButtonGroup getButtonGroup() {
		return buttonGroup;
	}

	/**
	 * @return the comboGenerated
	 */
	public JComboBox getComboGenerated() {
		return comboGenerated;
	}

	/**
	 * @return the textDefault
	 */
	public JTextField getTextDefault() {
		return textDefault;
	}

	/**
	 * @return the chkbxNotNull
	 */
	public JCheckBox getChkbxNotNull() {
		return chkbxNotNull;
	}

	/**
	 * @return the chkbxAutoinc
	 */
	public JCheckBox getChkbxAutoinc() {
		return chkbxAutoinc;
	}

	/**
	 * @return the txtInitValue
	 */
	public JTextField getTxtInitValue() {
		return txtInitValue;
	}

	/**
	 * @return the txtIncrement
	 */
	public JTextField getTxtIncrement() {
		return txtIncrement;
	}

	/**
	 * @return the chkbxCycle
	 */
	public JCheckBox getChkbxCycle() {
		return chkbxCycle;
	}

	/**
	 * @return the chkbxForeign
	 */
	public JCheckBox getChkbxForeign() {
		return chkbxForeign;
	}

	/**
	 * @return the txtConstName
	 */
	public JTextField getTxtConstName() {
		return txtConstName;
	}

	/**
	 * @return the txtRefTable
	 */
	public JTextField getTxtRefTable() {
		return txtRefTable;
	}

	/**
	 * @return the txtColNames
	 */
	public JTextField getTxtColNames() {
		return txtColNames;
	}

	/**
	 * @return the chkbxOnDelete
	 */
	public JCheckBox getChkbxOnDelete() {
		return chkbxOnDelete;
	}

	/**
	 * @return the comboOnDelete
	 */
	public JComboBox<MyColumnTypes> getComboOnDelete() {
		return comboOnDelete;
	}

	/**
	 * @return the chkbxOnUpdate
	 */
	public JCheckBox getChkbxOnUpdate() {
		return chkbxOnUpdate;
	}

	/**
	 * @return the comboOnUpdate
	 */
	public JComboBox<MyColumnTypes> getComboOnUpdate() {
		return comboOnUpdate;
	}

	/**
	 * @return the txtConstCheck
	 */
	public JTextField getTxtConstCheck() {
		return txtConstCheck;
	}

	/**
	 * @return the editorPaneConditions
	 */
	public JEditorPane getEditorPaneConditions() {
		return editorPaneConditions;
	}

}

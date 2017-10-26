package dbmanager;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.border.BevelBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

public class InsertRowDlg extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String propFont = "";
	public String mode = "MODE_NEW_ROW";
	private JLabel label;
	private JPanel dynamicPanel;
	private JTextField[] colValue;
	private JButton[] btnBrowse;
	private JCheckBox[] checkBox;
	private ArrayList<Object> row2Add = new ArrayList<>();

	public InsertRowDlg(MyTableModel tModel) {
		jbInit(tModel);
	}

	private void jbInit(MyTableModel tModel) {

		setModal(true);
		setModalityType(ModalityType.APPLICATION_MODAL);
		getContentPane().setLayout(new BorderLayout(0, 0));

		JPanel panel = new JPanel();
		panel.setPreferredSize(new Dimension(300, 150));
		panel.setMinimumSize(new Dimension(40, 30));
		panel.setMaximumSize(new Dimension(20000, 20000));

		JScrollPane scrollPane = new JScrollPane(panel);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		getContentPane().add(scrollPane);

		int numColumns = tModel.getColumnCount();
		colValue = new JTextField[numColumns];
		btnBrowse = new JButton[numColumns];
		checkBox = new JCheckBox[numColumns];

		for (int i = 0; i < numColumns; i++) {

			label = new JLabel(tModel.getColumnName(i) + ":");
			label.setAlignmentX(Component.RIGHT_ALIGNMENT);
			label.setSize(getPreferredSize());

			switch (tModel.typeNames.get(i)) {
			case "SMALLINT":
			case "BIGINT":
				// Long.class;
			case "INTEGER":
				// Integer.class;
				colValue[i] = new JFormattedTextField(NumberFormat.getIntegerInstance());
				colValue[i].setToolTipText(tModel.typeNames.get(i));
				colValue[i].setColumns(20);

				dynamicPanel = new JPanel();
				dynamicPanel.add(label);
				dynamicPanel.add(colValue[i]);
				panel.add(dynamicPanel);

				break;

			case "FLOAT":
			case "DOUBLE":
				// Double.class;
			case "REAL":
				// Double.clas
			case "DECIMAL":
			case "NUMERIC":
				// BigDecimal.class;
				colValue[i] = new JFormattedTextField(NumberFormat.getNumberInstance());
				colValue[i].setToolTipText(tModel.typeNames.get(i));
				colValue[i].setColumns(20);

				dynamicPanel = new JPanel();
				dynamicPanel.add(label);
				dynamicPanel.add(colValue[i]);
				panel.add(dynamicPanel);
				break;

			case "DATE":
				// java.sql.Date.class;
				colValue[i] = new JFormattedTextField(DateFormat.getDateInstance());
				colValue[i].setToolTipText(tModel.typeNames.get(i));
				colValue[i].setColumns(20);

				dynamicPanel = new JPanel();
				dynamicPanel.add(label);
				dynamicPanel.add(colValue[i]);
				panel.add(dynamicPanel);
				break;

			case "TIME":
				// java.sql.Time.class;
				colValue[i] = new JFormattedTextField(DateFormat.getTimeInstance());
				colValue[i].setToolTipText(tModel.typeNames.get(i));
				colValue[i].setColumns(20);

				dynamicPanel = new JPanel();
				dynamicPanel.add(label);
				dynamicPanel.add(colValue[i]);
				panel.add(dynamicPanel);
				break;
			case "TIMESTAMP":
				// java.sql.Timestamp.class;
				colValue[i] = new JFormattedTextField(DateFormat.getDateTimeInstance());
				colValue[i].setToolTipText(tModel.typeNames.get(i));
				colValue[i].setColumns(20);

				dynamicPanel = new JPanel();
				dynamicPanel.add(label);
				dynamicPanel.add(colValue[i]);
				panel.add(dynamicPanel);
				break;
			case "CLOB":
			case "BLOB":
				// java.sql.Clob.class;
				// java.sql.Blob.class;

				colValue[i] = new JTextField();
				colValue[i].setToolTipText(tModel.typeNames.get(i));
				colValue[i].setColumns(20);

				btnBrowse[i] = new JButton("Browse...");
				dynamicPanel = new JPanel();
				dynamicPanel.add(label);
				dynamicPanel.add(colValue[i]);
				dynamicPanel.add(btnBrowse[i]);
				JTextField auxField = colValue[i];
				btnBrowse[i].addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						JFileChooser chooser = new JFileChooser();
						chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
						chooser.setCurrentDirectory(new java.io.File("."));
						int returnVal = chooser.showOpenDialog(null);
						if (returnVal == JFileChooser.APPROVE_OPTION) {
							// System.out.println("You chose to open this file:
							// " + chooser.getSelectedFile().getName());
							auxField.setText(chooser.getSelectedFile().getAbsolutePath());
						}
					}
				});

				break;

			case "CHAR":
			case "VARCHAR":
				// String.class;
				colValue[i] = new JTextField();
				colValue[i].setToolTipText(tModel.typeNames.get(i));
				colValue[i].setColumns(20);

				dynamicPanel = new JPanel();
				dynamicPanel.add(label);
				dynamicPanel.add(colValue[i]);
				panel.add(dynamicPanel);
				break;

			case "BOOLEAN":
				// Boolean.class;
				checkBox[i] = new JCheckBox(tModel.getColumnName(i));
				checkBox[i].setToolTipText(tModel.typeNames.get(i));
				dynamicPanel = new JPanel();
				dynamicPanel.add(checkBox[i]);

				break;

			case "XML":
				// java.sql.XML.class;
				colValue[i] = new JTextField();
				colValue[i].setToolTipText(tModel.typeNames.get(i));
				colValue[i].setColumns(30);

				btnBrowse[i] = new JButton("Browse...");
				dynamicPanel = new JPanel();
				dynamicPanel.add(label);
				dynamicPanel.add(colValue[i]);
				dynamicPanel.add(btnBrowse[i]);
				JTextField auxField2 = colValue[i];
				btnBrowse[i].addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						JFileChooser chooser = new JFileChooser();
						chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
						chooser.setCurrentDirectory(new java.io.File("."));
						chooser.setFileFilter(new FileNameExtensionFilter("xml files (*.xml)", "xml"));
						int returnVal = chooser.showOpenDialog(null);
						if (returnVal == JFileChooser.APPROVE_OPTION) {
							System.out.println(
									"You chose to open this file: " + chooser.getSelectedFile().getAbsolutePath());
							auxField2.setText(chooser.getSelectedFile().getAbsolutePath());

						}
					}
				});
				break;

			default:
				colValue[i] = new JTextField("", 30);
				colValue[i].setToolTipText(tModel.typeNames.get(i));
				dynamicPanel = new JPanel();
				dynamicPanel.add(label);
				dynamicPanel.add(colValue[i]);
			}

			panel.add(dynamicPanel);

			if (i < numColumns - 1) {
				JSeparator separator = new JSeparator();
				separator.setPreferredSize(new Dimension(0, 1));
				separator.setMaximumSize(new Dimension(2000, 2000));
				panel.add(separator);
			}
		}

		this.pack();

		JToolBar toolBar = new JToolBar();
		toolBar.setMargin(new Insets(4, 20, 4, 20));
		toolBar.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		toolBar.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		toolBar.setBounds(new Rectangle(4, 4, 4, 4));
		toolBar.setAlignmentX(Component.RIGHT_ALIGNMENT);
		toolBar.setAlignmentY(Component.BOTTOM_ALIGNMENT);
		toolBar.setFloatable(false);

		getContentPane().add(toolBar, BorderLayout.SOUTH);

		JButton btnSave = new JButton("   Save   ");
		btnSave.setAlignmentX(Component.RIGHT_ALIGNMENT);
		btnSave.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {

				for (int i = 0; i < tModel.getColumnCount(); i++) {
					switch (tModel.typeNames.get(i)) {

					case "SMALLINT":
					case "BIGINT":
						// Long.class;
					case "INTEGER":
						// Integer.class;
						row2Add.add(((JFormattedTextField) colValue[i]).getValue());
						break;

					case "FLOAT":
					case "DOUBLE":
						// Double.class;
					case "REAL":
						// Double.clas
					case "DECIMAL":
					case "NUMERIC":
						// BigDecimal.class;
						row2Add.add(((JFormattedTextField) colValue[i]).getValue());
						break;

					case "DATE":
						// java.sql.Date.class;
						row2Add.add(((JFormattedTextField) colValue[i]).getValue());
						break;

					case "TIME":
						// java.sql.Time.class;
						row2Add.add(((JFormattedTextField) colValue[i]).getValue());
						break;
					case "TIMESTAMP":
						// java.sql.Timestamp.class;
						row2Add.add(((JFormattedTextField) colValue[i]).getValue());
						break;
					case "CLOB":
					case "BLOB":
						// java.sql.Clob.class;
						// java.sql.Blob.class;

						row2Add.add(colValue[i].getText());
						break;

					case "CHAR":
					case "VARCHAR":
						// String.class;
						row2Add.add(colValue[i].getText());
						break;

					case "BOOLEAN":
						// Boolean.class;
						row2Add.add(checkBox[i].isSelected());
						break;

					case "XML":
						// java.sql.XML.class;
						row2Add.add(colValue[i].getText());
						break;

					default:
						row2Add.add(colValue[i].getText());
						break;
					}

				}

				tModel.insertNewRow(row2Add);
				setVisible(false);
			}
		});

		btnSave.setMinimumSize(new Dimension(100, 20));
		btnSave.setPreferredSize(new Dimension(100, 30));
		btnSave.setMaximumSize(new Dimension(150, 50));
		btnSave.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnSave.setFont(new Font("Dialog", Font.PLAIN, 15));
		toolBar.add(btnSave);

		JButton btnCancel = new JButton("  Cancel  ");
		btnCancel.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnCancel.setFont(new Font("Dialog", Font.PLAIN, 15));
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int confirm = JOptionPane.showConfirmDialog(null,
						"The changes will be lost.\n Do you want to Cancel anyway?", "WARNING",
						JOptionPane.YES_NO_OPTION);

				if (confirm == JOptionPane.YES_OPTION) {
					setVisible(false);
					dispose();
				}
			}
		});

		btnCancel.setMinimumSize(new Dimension(100, 20));
		btnCancel.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnCancel.setMaximumSize(new Dimension(150, 50));
		btnCancel.setPreferredSize(new Dimension(100, 30));
		toolBar.add(btnCancel);
	}
}
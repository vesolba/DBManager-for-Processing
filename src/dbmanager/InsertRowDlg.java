<<<<<<< HEAD
package dbmanager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.Dialog.ModalityType;

public class InsertRowDlg extends JDialog {

	private JTable table = null;
	private String propFont = "";
	public String mode = "MODE_NEW_ROW";

	public InsertRowDlg(MyTableModel tModel, Connection conn) {
		jbInit(tModel, conn);
	}

	private void jbInit(MyTableModel tModel, Connection conn) {

		setModal(true);
		setModalityType(ModalityType.APPLICATION_MODAL);
		setTitle("Insert rows");
		getContentPane().setLayout(new BorderLayout(0, 0));

		table = new JTable(tModel) {
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

		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setColumnSelectionAllowed(true);
		table.setCellSelectionEnabled(true);
		JScrollPane scrollPane = new JScrollPane(table);
		getContentPane().add(scrollPane);

		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		getContentPane().add(toolBar, BorderLayout.SOUTH);

		JButton btnAddRow = new JButton("Add row");
		btnAddRow.setMinimumSize(new Dimension(110, 25));
		btnAddRow.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				tModel.data2Add.add(tModel.addEmptyRow(conn));
				table.updateUI();

			}
		});
		btnAddRow.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnAddRow.setMaximumSize(new Dimension(120, 30));
		toolBar.add(btnAddRow);

		JButton btnDeleteRow = new JButton("Delete row");
		btnDeleteRow.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				int rowSel = table.getSelectedRow();
				tModel.data2Add.remove(rowSel);
				table.updateUI();

			}
		});
		btnDeleteRow.setMinimumSize(new Dimension(110, 25));
		btnDeleteRow.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnDeleteRow.setMaximumSize(new Dimension(120, 30));
		toolBar.add(btnDeleteRow);

		JButton btnSave = new JButton("Save additions");
		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tModel.insertNewData();
			}
		});
		btnSave.setMinimumSize(new Dimension(110, 25));
		btnSave.setPreferredSize(new Dimension(70, 23));
		btnSave.setMaximumSize(new Dimension(120, 30));
		btnSave.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnSave.setFont(new Font("Tahoma", Font.PLAIN, 12));
		toolBar.add(btnSave);

		JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int confirm = JOptionPane.showConfirmDialog(null,
						"The rows not saved will be lost.\n Do you want to Cancel anyway?", "WARNING",
						JOptionPane.YES_NO_OPTION);

				if (confirm == JOptionPane.YES_OPTION) {
					setVisible(false);
					dispose();
				}
			}
		});
		btnCancel.setMinimumSize(new Dimension(110, 25));
		btnCancel.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnCancel.setMaximumSize(new Dimension(120, 30));
		btnCancel.setPreferredSize(new Dimension(70, 23));
		toolBar.add(btnCancel);

		Component horizontalGlue = Box.createHorizontalGlue();
		horizontalGlue.setMaximumSize(new Dimension(300, 0));
		toolBar.add(horizontalGlue);

		JSpinner spinInserTable = new JSpinner();
		spinInserTable.setToolTipText("Table Font Size");
		spinInserTable.setModel(new SpinnerNumberModel(new Integer(12), null, null, new Integer(1)));
		toolBar.add(spinInserTable);

		if (DBManager.propsDBM != null) {
			String propTable = DBManager.propsDBM.getDBMProp("spinInserTable");

			spinInserTable.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent arg0) {
					table.setFont(table.getFont().deriveFont(Float.parseFloat(spinInserTable.getValue().toString())));
					table.setRowHeight((int) Float.parseFloat(spinInserTable.getValue().toString()) + 5);
					DBManager.propsDBM.setDBMProp("spinInserTable", spinInserTable.getValue().toString());
					DBManager.propsDBM.saveProperties();
				}
			});
			propTable = DBManager.propsDBM.getDBMProp("spinInserTable");

			if (propTable != null && !propTable.isEmpty()) {
				spinInserTable.setValue(Integer.parseInt(propTable));
				table.setFont(table.getFont().deriveFont(Float.parseFloat(spinInserTable.getValue().toString())));
				table.setRowHeight((int) Float.parseFloat(spinInserTable.getValue().toString()) + 5);
			}
		}
	}

	// @Override
	// public int getColumnCount() {
	// return colsNames.size();
	// }
	//
	// @Override
	// public int getRowCount() {
	// return data.size();
	// }
	//
	// @Override
	// public String getColumnName(int column) {
	// return colsNames.get(column);
	// }
	//
	// @Override
	// public Object getValueAt(int rowIndex, int columnIndex) {
	// ArrayList<Object> row = data.get(rowIndex);
	// Object value = row.get(columnIndex);
	// return value;
	// }
	//
	// @Override
	// public void setValueAt(Object value, int rowIndex, int columnIndex) {
	//
	// ArrayList<Object> row = data.get(rowIndex);
	// row.set(columnIndex, value);

	// }

	// Doesn't adds nothing. Only to format columns.
	// public boolean addRow(Connection conn) {
	//
	// int numCols = tModel.getColumnCount();
	//
	// ArrayList<Object> row = new ArrayList<>();
	//
	// try {
	//
	// for (int i = 0; i < numCols; i++) {
	//
	// if (tModel.colsClasses.get(i) == Clob.class) {
	// Clob defValue;
	// defValue = conn.createClob();
	//
	// defValue.setString(1, "");
	// row.add(defValue);
	// } else
	//
	// if (tModel.colsClasses.get(i) == Blob.class) {
	// Blob defValue = conn.createBlob();
	// byte[] zeroByte = { 00 };
	// defValue.setBytes(1, zeroByte);
	// row.add(defValue);
	// } else
	//
	// if (tModel.colsClasses.get(i) == Byte.class) {
	// byte[] defValue = { 00 };
	// row.add(defValue);
	// } else
	//
	// if (tModel.colsClasses.get(i) == java.sql.Date.class ||
	// tModel.colsClasses.get(i) == java.sql.Time.class
	// || tModel.colsClasses.get(i) == java.sql.Timestamp.class
	// || tModel.colsClasses.get(i) == String.class
	// || tModel.colsClasses.get(i) == java.sql.SQLXML.class) {
	// Object defValue = "";
	// row.add(defValue);
	// } else {
	// Object defValue = 0;
	// row.add(defValue);
	// }
	// }
	//
	// tModel.data.add(row);
	//
	// } catch (SQLException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// return false;
	//
	// };
=======
package dbmanager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.Dialog.ModalityType;

public class InsertRowDlg extends JDialog {

	private JTable table = null;
	private String propFont = "";
	public String mode = "MODE_NEW_ROW";

	public InsertRowDlg(MyTableModel tModel, Connection conn) {
		jbInit(tModel, conn);
	}

	private void jbInit(MyTableModel tModel, Connection conn) {

		setModal(true);
		setModalityType(ModalityType.APPLICATION_MODAL);
		setTitle("Insert rows");
		getContentPane().setLayout(new BorderLayout(0, 0));

		table = new JTable(tModel) {
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

		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setColumnSelectionAllowed(true);
		table.setCellSelectionEnabled(true);
		JScrollPane scrollPane = new JScrollPane(table);
		getContentPane().add(scrollPane);

		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		getContentPane().add(toolBar, BorderLayout.SOUTH);

		JButton btnAddRow = new JButton("Add row");
		btnAddRow.setMinimumSize(new Dimension(110, 25));
		btnAddRow.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				tModel.data2Add.add(tModel.addEmptyRow(conn));
				table.updateUI();

			}
		});
		btnAddRow.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnAddRow.setMaximumSize(new Dimension(120, 30));
		toolBar.add(btnAddRow);

		JButton btnDeleteRow = new JButton("Delete row");
		btnDeleteRow.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				int rowSel = table.getSelectedRow();
				tModel.data2Add.remove(rowSel);
				table.updateUI();

			}
		});
		btnDeleteRow.setMinimumSize(new Dimension(110, 25));
		btnDeleteRow.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnDeleteRow.setMaximumSize(new Dimension(120, 30));
		toolBar.add(btnDeleteRow);

		JButton btnSave = new JButton("Save additions");
		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tModel.insertNewData();
			}
		});
		btnSave.setMinimumSize(new Dimension(110, 25));
		btnSave.setPreferredSize(new Dimension(70, 23));
		btnSave.setMaximumSize(new Dimension(120, 30));
		btnSave.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnSave.setFont(new Font("Tahoma", Font.PLAIN, 12));
		toolBar.add(btnSave);

		JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int confirm = JOptionPane.showConfirmDialog(null,
						"The rows not saved will be lost.\n Do you want to Cancel anyway?", "WARNING",
						JOptionPane.YES_NO_OPTION);

				if (confirm == JOptionPane.YES_OPTION) {
					setVisible(false);
					dispose();
				}
			}
		});
		btnCancel.setMinimumSize(new Dimension(110, 25));
		btnCancel.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		btnCancel.setMaximumSize(new Dimension(120, 30));
		btnCancel.setPreferredSize(new Dimension(70, 23));
		toolBar.add(btnCancel);

		Component horizontalGlue = Box.createHorizontalGlue();
		horizontalGlue.setMaximumSize(new Dimension(300, 0));
		toolBar.add(horizontalGlue);

		if (DBManager.propsDBM != null) {
			String propTable = DBManager.propsDBM.getDBMProp("spinInserTable");

			JSpinner spinInserTable = new JSpinner();
			spinInserTable.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent arg0) {
					table.setFont(table.getFont().deriveFont(Float.parseFloat(spinInserTable.getValue().toString())));
					table.setRowHeight((int) Float.parseFloat(spinInserTable.getValue().toString()) + 5);
					DBManager.propsDBM.setDBMProp("spinInserTable", spinInserTable.getValue().toString());
					DBManager.propsDBM.saveProperties();
				}
			});
			spinInserTable.setToolTipText("Table Font Size");
			spinInserTable.setModel(new SpinnerNumberModel(new Integer(12), null, null, new Integer(1)));
			toolBar.add(spinInserTable);
			propTable = DBManager.propsDBM.getDBMProp("spinInserTable");

			if (propTable != null && !propTable.isEmpty()) {
				spinInserTable.setValue(Integer.parseInt(propTable));
				table.setFont(table.getFont().deriveFont(Float.parseFloat(spinInserTable.getValue().toString())));
				table.setRowHeight((int) Float.parseFloat(spinInserTable.getValue().toString()) + 5);
			}
		}
	}

	// @Override
	// public int getColumnCount() {
	// return colsNames.size();
	// }
	//
	// @Override
	// public int getRowCount() {
	// return data.size();
	// }
	//
	// @Override
	// public String getColumnName(int column) {
	// return colsNames.get(column);
	// }
	//
	// @Override
	// public Object getValueAt(int rowIndex, int columnIndex) {
	// ArrayList<Object> row = data.get(rowIndex);
	// Object value = row.get(columnIndex);
	// return value;
	// }
	//
	// @Override
	// public void setValueAt(Object value, int rowIndex, int columnIndex) {
	//
	// ArrayList<Object> row = data.get(rowIndex);
	// row.set(columnIndex, value);

	// }

	// Doesn't adds nothing. Only to format columns.
	// public boolean addRow(Connection conn) {
	//
	// int numCols = tModel.getColumnCount();
	//
	// ArrayList<Object> row = new ArrayList<>();
	//
	// try {
	//
	// for (int i = 0; i < numCols; i++) {
	//
	// if (tModel.colsClasses.get(i) == Clob.class) {
	// Clob defValue;
	// defValue = conn.createClob();
	//
	// defValue.setString(1, "");
	// row.add(defValue);
	// } else
	//
	// if (tModel.colsClasses.get(i) == Blob.class) {
	// Blob defValue = conn.createBlob();
	// byte[] zeroByte = { 00 };
	// defValue.setBytes(1, zeroByte);
	// row.add(defValue);
	// } else
	//
	// if (tModel.colsClasses.get(i) == Byte.class) {
	// byte[] defValue = { 00 };
	// row.add(defValue);
	// } else
	//
	// if (tModel.colsClasses.get(i) == java.sql.Date.class ||
	// tModel.colsClasses.get(i) == java.sql.Time.class
	// || tModel.colsClasses.get(i) == java.sql.Timestamp.class
	// || tModel.colsClasses.get(i) == String.class
	// || tModel.colsClasses.get(i) == java.sql.SQLXML.class) {
	// Object defValue = "";
	// row.add(defValue);
	// } else {
	// Object defValue = 0;
	// row.add(defValue);
	// }
	// }
	//
	// tModel.data.add(row);
	//
	// } catch (SQLException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// return false;
	//
	// };
>>>>>>> 52852d7381e2f8b2cd6e07db2bfe7789238d6c5f
}
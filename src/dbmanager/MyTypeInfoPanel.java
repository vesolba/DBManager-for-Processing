package dbmanager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import java.awt.Dimension;
import javax.swing.JTextArea;
import javax.swing.BoxLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MyTypeInfoPanel extends JPanel {
	private JTable table;
	private JTextArea textAreaMessage1 = null;
	private JTextArea textAreaMessage2;

	public MyTypeInfoPanel() {
		jbInit();
	}

	private void jbInit() {

		setLayout(new BorderLayout(0, 0));

		JMenuBar menuBar = new JMenuBar();
		add(menuBar, BorderLayout.NORTH);

		Component horizontalGlue = Box.createHorizontalGlue();
		menuBar.add(horizontalGlue);

		JLabel lblNewLabel = new JLabel("  Java DB available column data types  ");
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		menuBar.add(lblNewLabel);

		Component horizontalGlue_1 = Box.createHorizontalGlue();
		menuBar.add(horizontalGlue_1);
		JSpinner spinTestPane = new JSpinner();
		spinTestPane.setMinimumSize(new Dimension(50, 20));
		spinTestPane.setMaximumSize(new Dimension(1000, 1000));
		spinTestPane.setPreferredSize(new Dimension(40, 20));
		spinTestPane.setModel(new SpinnerNumberModel(0, 0, 100, 1));
		menuBar.add(spinTestPane);

		table = new JTable() {
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
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
			}
		});
		table.setToolTipText("Available Data Types ");
		table.setMaximumSize(new Dimension(1000, 0));
		table.setEnabled(false);

		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setRowSelectionAllowed(false);
		table.setModel(newTableModel());

		if (DBManager.propsDBM != null) {
			String propTable = DBManager.propsDBM.getDBMProp("spinTestPane");
			spinTestPane.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent arg0) {

					if (textAreaMessage1 != null) {
						textAreaMessage1.setFont(
								table.getFont().deriveFont(Float.parseFloat(spinTestPane.getValue().toString())));
					}
					if (textAreaMessage2 != null) {
						textAreaMessage2.setFont(
								table.getFont().deriveFont(Float.parseFloat(spinTestPane.getValue().toString())));
					}
					table.setFont(table.getFont().deriveFont(Float.parseFloat(spinTestPane.getValue().toString())));
					table.setRowHeight((int) Float.parseFloat(spinTestPane.getValue().toString()) + 5);
					DBManager.propsDBM.setDBMProp("spinTestPane", spinTestPane.getValue().toString());
					DBManager.propsDBM.saveProperties();
				}
			});
			spinTestPane.setModel(new SpinnerNumberModel(20, 0, 100, 1));

			propTable = DBManager.propsDBM.getDBMProp("spinTestPane");

			if (propTable != null && !propTable.isEmpty()) {
				spinTestPane.setValue(Integer.parseInt(propTable));
				table.setFont(table.getFont().deriveFont(Float.parseFloat(spinTestPane.getValue().toString())));
				table.setRowHeight((int) Float.parseFloat(spinTestPane.getValue().toString()) + 5);
			}
		}

		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setPreferredSize(new Dimension(1000, 419));
		add(scrollPane, BorderLayout.CENTER);

		Connection conn = null;
		try {

			conn = DBConnect.connect(DBManager.prefInicConn, DBManager.pathToDBManager, false);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		table.setModel(newTableModel());
		loadData(conn);
		table.updateUI();

		JPanel mesagesPane = new JPanel();
		add(mesagesPane, BorderLayout.SOUTH);
		mesagesPane.setLayout(new BoxLayout(mesagesPane, BoxLayout.X_AXIS));

		textAreaMessage1 = new JTextArea();
		textAreaMessage1.setFont(new Font("Monospaced", Font.PLAIN, 13));
		textAreaMessage1.setEnabled(false);
		textAreaMessage1.setEditable(false);
		textAreaMessage1.setText(
				"Values for the Nullable column:\r\n0 - typeNoNulls - NULL values not allowed\r\n1 - typeNullable - NULL values allowed\r\n2 - Unknown - nullability unknown");
		textAreaMessage1.setColumns(20);
		mesagesPane.add(textAreaMessage1);

		textAreaMessage2 = new JTextArea();
		textAreaMessage2.setEnabled(false);
		textAreaMessage2.setEditable(false);
		textAreaMessage2.setFont(new Font("Monospaced", Font.PLAIN, 13));
		textAreaMessage2.setText(
				"Values for the Searchable column:\r\n0 - typePredNone - No support\r\n1 - typePredChar - Only with WHERE .. LIKE\r\n2 - typePredBasic - Except for WHERE .. LIKE\r\n3 - typeSearchable - Supported for all WHERE ..");
		mesagesPane.add(textAreaMessage2);
	}

	public void loadData(Connection conn) {
		try {

			DatabaseMetaData dbmd = conn.getMetaData();
			ResultSet rset;
			rset = dbmd.getTypeInfo();

			while (rset.next()) {
				Object[] tipo = new Object[18];
				tipo[0] = rset.getString("TYPE_NAME");
				tipo[1] = rset.getInt("DATA_TYPE");
				tipo[2] = rset.getInt("PRECISION");
				tipo[3] = rset.getString("LITERAL_PREFIX");
				tipo[4] = rset.getString("LITERAL_SUFFIX");
				tipo[5] = rset.getString("CREATE_PARAMS");
				tipo[6] = rset.getShort("NULLABLE");
				tipo[7] = rset.getBoolean("CASE_SENSITIVE");
				tipo[8] = rset.getShort("SEARCHABLE");
				tipo[9] = rset.getBoolean("UNSIGNED_ATTRIBUTE");
				tipo[10] = rset.getBoolean("FIXED_PREC_SCALE");
				tipo[11] = rset.getBoolean("AUTO_INCREMENT");
				tipo[12] = rset.getString("LOCAL_TYPE_NAME");
				tipo[13] = rset.getShort("MINIMUM_SCALE");
				tipo[14] = rset.getShort("MAXIMUM_SCALE");
				tipo[15] = rset.getInt("SQL_DATA_TYPE");
				tipo[16] = rset.getInt("SQL_DATETIME_SUB");
				tipo[17] = rset.getInt("NUM_PREC_RADIX");
				((DefaultTableModel) table.getModel()).addRow(tipo);
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private DefaultTableModel newTableModel() {
		return new DefaultTableModel(new Object[][] {},
				new String[] { "TYPE_NAME", "DATA_TYPE", "PRECISION", "LITERAL_PREFIX", "LITERAL_SUFFIX",
						"CREATE_PARAMS", "NULLABLE", "CASE_SENSITIVE", "SEARCHABLE", "UNSIGNED_ATTRIBUTE",
						"FIXED_PREC_SCALE", "AUTO_INCREMENT", "LOCAL_TYPE_NAME", "MINIMUM_SCALE", "MAXIMUM_SCALE",
						"SQL_DATA_TYPE", "SQL_DATETIME_SUB", "NUM_PREC_RADIX" }) {
			Class[] columnTypes = new Class[] { String.class, Integer.class, Integer.class, String.class, String.class,
					String.class, Short.class, Boolean.class, Short.class, Boolean.class, Boolean.class, Boolean.class,
					String.class, Short.class, Short.class, Integer.class, Integer.class, Integer.class };

			public Class getColumnClass(int columnIndex) {
				return columnTypes[columnIndex];
			}
		};
	}

	/**
	 * @return the table
	 */
	public JTable getTable() {
		return table;
	}

}

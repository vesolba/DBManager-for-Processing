package dbmanager;

import java.util.List;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.table.AbstractTableModel;

@SuppressWarnings("serial")
public class TableCreaModel extends AbstractTableModel {

	ArrayList<String> colsNames = new ArrayList<>(Arrays.asList("Key", "Unique", "Null", "Index", "Autoincrement",
			"Foreign Key", "Check", "Column name", "Data type", "Size", "Scale", "Default value", "Generated",
			"Start with", "Incr. by", "Cycle", "Const. name", "Ref. table", "Column names", "On del.", "On delete",
			"On upd.", "On update", "Cons.Check name ", "Check conditions")); // Column names

	ArrayList<Integer> colsSizes = new ArrayList<>(
			Arrays.asList(3, 3, 3, 3, 3, 3, 3, 30, 20, 10, 10, 10, 20, 10, 10, 3, 30, 20, 30, 3, 20, 3, 20, 20, 100));
	ArrayList<ArrayList<Object>> data = new ArrayList<>();
	ArrayList<String> colsTypes = new ArrayList<>(Arrays.asList("java.lang.Boolean", "java.lang.Boolean",
			"java.lang.Boolean", "java.lang.Boolean", "java.lang.Boolean", "java.lang.Boolean", "java.lang.Boolean",
			"java.lang.String", "java.lang.Object", "java.lang.Integer", "java.lang.Integer", "java.lang.String",
			"java.lang.String", "java.lang.Integer", "java.lang.Integer", "java.lang.Boolean", "java.lang.String",
			"java.lang.String", "java.lang.String", "java.lang.String", "java.lang.String", "java.lang.String",
			"java.lang.String"));
	ArrayList<Class> colsClasses = new ArrayList<>(
			Arrays.asList(Boolean.class, Boolean.class, Boolean.class, Boolean.class, Boolean.class, Boolean.class,
					Boolean.class, String.class, Object.class, Integer.class, Integer.class, String.class, String.class,
					Integer.class, Integer.class, Boolean.class, String.class, String.class, String.class,
					Boolean.class, String.class, Boolean.class, String.class, String.class, String.class));

	Statement statement;
	ResultSetMetaData metadata;
	ResultSet resultSet;

	Connection conn = null;
	String query = "";
	String mode = "MODE_FILL";
	String sourceTable = "";

	public TableCreaModel(Connection conn, String query, String mode) {
		super();

		this.conn = conn;
		this.query = query;
		this.mode = mode;

	}

	@Override
	public int getColumnCount() {
		return colsNames.size();
	}

	@Override
	public int getRowCount() {
		return data.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {

		if (rowIndex >= getRowCount() || columnIndex >= getColumnCount())
			return null;
		ArrayList<Object> row;
		row = data.get(rowIndex);
		Object value = row.get(columnIndex);

		return value;
	}

	@Override
	public String getColumnName(int column) {
		return colsNames.get(column);
	}

	public int getColumnSize(int column) {
		return colsSizes.get(column);
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		return true;
	}

	@Override
	public Class<?> getColumnClass(int column) {
		return colsClasses.get(column);
	}
}

package dbmanager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.table.AbstractTableModel;

@SuppressWarnings("serial")
public class MyTableModel extends AbstractTableModel {

	String dbUrl;
	Statement statement;
	ResultSetMetaData metadata;
	ResultSet resultSet;
	ArrayList<String> cols = new ArrayList<>();
	ArrayList<Integer> colsSize = new ArrayList<>();
	ArrayList<ArrayList<String>> data = new ArrayList<>();

	public MyTableModel(Connection conn, String query) {

		if (conn != null) {

			try {

				statement = conn.createStatement();
				resultSet = statement.executeQuery(query);

				int c = resultSet.getMetaData().getColumnCount();
				for (int i = 1; i <= c; i++) {
					cols.add(resultSet.getMetaData().getColumnName(i));
					colsSize.add(resultSet.getMetaData().getPrecision(i));
				}

				while (resultSet.next()) {
					ArrayList<String> row = new ArrayList<>();
					for (int i = 1; i <= c; i++) {
						row.add(resultSet.getString(i));
					}
					data.add(row);
				}
			} catch (SQLException ex) {
				System.out.println("Could not connect to database");
			} finally {
				try {
					if (statement != null) {
						statement.close();
					}
					if (conn != null) {
						conn.close();
					}
				} catch (SQLException ex) {
					Logger.getLogger(MyTableModel.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		}
	}

	@Override
	public int getRowCount() {
		return data.size();
	}

	@Override
	public int getColumnCount() {
		return cols.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		ArrayList<String> row = data.get(rowIndex);
		return row.get(columnIndex);
	}

	@Override
	public String getColumnName(int column) {
		return cols.get(column);
	}
	
	public int getColumnSize(int column) {
		return colsSize.get(column);
	}

	
}

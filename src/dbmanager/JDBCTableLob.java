package dbmanager;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;

import sun.net.www.URLConnection;

/**
 * A class to manage large objects retrieved from the database.
 * 
 * @author gm310509
 */
public class JDBCTableLob {
	/**
	 * The maximum number of CLOB characters to display in a result set JTable
	 * before showing ellipses (...)
	 */
	private static final int CLOB_SIZE = 100;

	/**
	 * The type of Large Object
	 */
	public enum LobType {
		/** A Character Large Object */
		CLOB,
		/** A Binary Large Object */
		BLOB
	};

	/**
	 * Creates a new CLOB in the specified directory.
	 * 
	 * @param clob
	 *            the clob.
	 * @param tempDirectory
	 *            The directory in which to save the clob.
	 * @throws java.io.IOException
	 * @throws java.sql.SQLException
	 */
	public JDBCTableLob(Clob clob, String tempDirectory) throws IOException, SQLException {
		this(clob, new File(tempDirectory));
	}

	/**
	 * Creates a new CLOB in the specified directory.
	 * 
	 * @param clob
	 *            the clob
	 * @param tempDirectory
	 *            the directory in which to save the clob.
	 * @throws java.io.IOException
	 * @throws java.sql.SQLException
	 */
	public JDBCTableLob(Clob clob, File tempDirectory) throws IOException, SQLException {

		file = File.createTempFile("sql", ".lob", tempDirectory);
		copyFile(new FileOutputStream(file), clob.getAsciiStream());
		type = LobType.CLOB;

		long len = Math.min(CLOB_SIZE, clob.length());
		String moreInd = "";
		if (clob.length() > CLOB_SIZE) {
			moreInd = "...";
		}
		try {
			text = clob.getSubString(1, (int) len) + moreInd;
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Creates a new blob in the specified directory.
	 * 
	 * @param blob
	 *            the blob
	 * @param tempDirectory
	 *            the diretory in which to create it.
	 * @throws java.io.IOException
	 * @throws java.sql.SQLException
	 */
	public JDBCTableLob(Blob blob, String tempDirectory) throws IOException, SQLException {
		this(blob, new File(tempDirectory));
	}

	/**
	 * Creates a new blob in the specified directory.
	 * 
	 * @param blob
	 *            the new blob
	 * @param tempDirectory
	 *            the directory in which to create it.
	 * @throws java.io.IOException
	 * @throws java.sql.SQLException
	 */
	public JDBCTableLob(Blob blob, File tempDirectory) throws IOException, SQLException {

		String newFileExt = getFileExt(blob, tempDirectory);

		file = File.createTempFile("sql", "." + newFileExt, tempDirectory);
		copyFile(new FileOutputStream(file), blob.getBinaryStream());
		type = LobType.BLOB;

		text = "BLOB";
	}

	public String getFileExt(Blob blob, File tempDirectory) {
		InputStream inStream = null;
		BufferedInputStream bis = null;
		String guessedType, guessedExt = "";

		try {

			// open input stream for reading purpose.In order to use
			// guessContentTypeFromStream the stream has to support
			// mark and reset methods. So ...
			inStream = blob.getBinaryStream();

			// ...input stream is converted to buffered input stream
			// that supports them.
			bis = new BufferedInputStream(inStream);

			guessedType = URLConnection.guessContentTypeFromStream(bis); // Obtains mime type 
			if (!guessedType.equals("")) {				// for example  "image/png". We...
											// ...only need the extension.
				guessedExt = guessedType.substring(guessedType.indexOf("/") + 1);
			}
		} catch (Exception e) {
			// if any I/O error occurs
			e.printStackTrace();
		} finally {
			// releases any system resources associated with the stream
			if (inStream != null)
				try {
					inStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			if (bis != null)
				try {
					bis.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		return guessedExt;

	}

	/**
	 * Copies a file to a new location.
	 * <p>
	 * This method is primarily used to copy the LOB "file" in the ResultSet to a
	 * file on disk.
	 * </p>
	 * 
	 * @param os
	 *            the output stream (copy to)
	 * @param is
	 *            the input stream (copy from)
	 * @throws java.io.IOException
	 */
	private void copyFile(OutputStream os, InputStream is) throws IOException {
		BufferedOutputStream bos = new BufferedOutputStream(os);
		BufferedInputStream bis = new BufferedInputStream(is);
		int aByte;
		while ((aByte = is.read()) != -1) {
			bos.write(aByte);
		}
		bis.close();
		bos.close();
	}

	/**
	 * Cleanup the temporary file containing the LOB data.
	 */
	public void cleanup() {
		file.delete();
	}

	/**
	 * Holds value of property file.
	 */
	private File file;

	/**
	 * Retrieve the file corresponding to the LOB data
	 * 
	 * @return the lob File.
	 */
	public File getFile() {
		return this.file;
	}

	/**
	 * Sets the file corresponding to the LOB data.
	 * 
	 * @param file
	 *            the new File.
	 */
	public void setFile(File file) {
		this.file = file;
	}

	/**
	 * Retrieve the Input Stream for this LOB
	 * 
	 * @return the Input Stream to read the LOB data from temporary storage.
	 * @throws java.io.IOException
	 */
	public InputStream getInputStream() throws IOException {
		return new FileInputStream(file);
	}

	/**
	 * The type of LOB
	 */
	private LobType type;

	/**
	 * The type of the LOB.
	 * 
	 * @return The type of LOB.
	 */
	public LobType getType() {
		return this.type;
	}

	/**
	 * A text string describing the LOB.
	 */
	private String text = "";

	/**
	 * A text string describing the LOB.
	 * 
	 * @return the lob test.
	 */
	public String getText() {
		return this.text;
	}

	/**
	 * Sets the text describing this lob.
	 * 
	 * @param text
	 *            the new LOB text.
	 */
	public void setText(String text) {
		this.text = text;
	}

	@Override
	public String toString() {
		return file.getAbsolutePath();
	}
}
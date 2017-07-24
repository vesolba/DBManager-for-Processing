package dbmanager;


import java.io.*;
import java.sql.*;

/**
 * A class to manage large objects retrieved from the database.
 * @author gm310509 
 */
public class JDBCTableLob {
    /**
     * The maximum number of CLOB characters to display in a result set JTable
     * before showing elipses (...)
     */
    private static final int CLOB_SIZE = 100;

    /**
     * The type of Large Object
     */
    public enum LobType {
        /** A Character Large Object */
        CLOB,
        /** A Binary Large Object */
        BLOB };

    /**
     * Creates a new CLOB in the specified directory.
     * @param clob the clob.
     * @param tempDirectory The directory in which to save the clob.
     * @throws java.io.IOException
     * @throws java.sql.SQLException
     */
    public JDBCTableLob (Clob clob, String tempDirectory)
            throws IOException, SQLException {
        this (clob, new File (tempDirectory));
    }

    /**
     * Creates a new CLOB in the specified directory.
     * @param clob the clob
     * @param tempDirectory the directory in which to save the clob.
     * @throws java.io.IOException
     * @throws java.sql.SQLException
     */
    public JDBCTableLob (Clob clob, File tempDirectory)
            throws IOException, SQLException {
        
        file = File.createTempFile ("sql", ".lob", tempDirectory);
        copyFile (new FileOutputStream (file), clob.getAsciiStream ());
        type = LobType.CLOB;
        
        long len = Math.min (CLOB_SIZE, clob.length());
        String moreInd = "";
        if (clob.length () > CLOB_SIZE) {
            moreInd = "...";
        }
        try {
            text = clob.getSubString (1, (int) len) + moreInd;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Creates a new  blob in the specified directory.
     * @param blob the blob
     * @param tempDirectory the diretory in which to create it.
     * @throws java.io.IOException
     * @throws java.sql.SQLException
     */
    public JDBCTableLob (Blob blob, String tempDirectory)
            throws IOException, SQLException {
        this (blob, new File (tempDirectory));
    }
    
    /**
     * Creates a new blob in the specified directory.
     * @param blob the new blob
     * @param tempDirectory the directory in which to create it.
     * @throws java.io.IOException
     * @throws java.sql.SQLException
     */
    public JDBCTableLob (Blob blob, File tempDirectory)
            throws IOException, SQLException {
        
        file = File.createTempFile ("sql", ".lob", tempDirectory);
        copyFile (new FileOutputStream (file), blob.getBinaryStream ());
        type = LobType.BLOB;

        text = "BLOB";
    }


    /**
     * Copies a file to a new location.
     * <p>
     * This method is primarily used to copy the LOB "file" in the ResultSet
     * to a file on disk.
     * </p>
     * @param os the output stream (copy to)
     * @param is the input stream (copy from)
     * @throws java.io.IOException
     */
    private void copyFile (OutputStream os, InputStream is)
            throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream (os);
        BufferedInputStream bis = new BufferedInputStream (is);
        int aByte;
        while ((aByte = is.read ()) != -1) {
            bos.write (aByte);
        }
        bis.close ();
        bos.close ();
    }

    /**
     * Cleanup the temporary file containing the LOB data.
     */
    public void cleanup () {
        file.delete ();
    }

    /**
     * Holds value of property file.
     */
    private File file;

    /**
     * Retrieve the file corresponding to the LOB data
     * @return the lob File.
     */
    public File getFile () {
        return this.file;
    }

    /**
     * Sets the file corresponding to the LOB data.
     * @param file the new File.
     */
    public void setFile (File file) {
        this.file = file;
    }

    /**
     * Retrieve the Input Stream for this LOB
     * @return the Input Stream to read the LOB data from temporary storage.
     * @throws java.io.IOException
     */
    public InputStream getInputStream ()
            throws IOException {
        return new FileInputStream (file);
    }

    /**
     * The type of LOB
     */
    private LobType type;

    /**
     * The type of the LOB.
     * @return The type of LOB.
     */
    public LobType getType () {
        return this.type;
    }

    /**
     * A text string describing the LOB.
     */
    private String text = "";

    /**
     * A text string describing the LOB.
     * @return the lob test.
     */
    public String getText () {
        return this.text;
    }

    /**
     * Sets the text describing this lob.
     * @param text the new LOB text.
     */
    public void setText (String text) {
        this.text = text;
    }

    @Override
    public String toString () {
        return file.getAbsolutePath();
    }
}
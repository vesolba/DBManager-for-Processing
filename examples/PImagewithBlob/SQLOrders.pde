void createBlobsTable() { //<>// //<>//
  // The previous table is dropped
  String order0 = "DROP TABLE TUTTIBLOBS ";
  // BLOB table creation
  String order1 = "CREATE TABLE TUTTIBLOBS " +
    "(IDKEY INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY " +
    "(START WITH 1, INCREMENT BY 1)," +  //Autoincrement field
    "MODE VARCHAR(10), " +     //Mode can be Stream or Blob
    "FILENAME VARCHAR(50), " + //Save the file name in other field if needed
    "IMAGE BLOB)";
  //-- PRIMARY/UNIQUE
  String order2 = "ALTER TABLE TUTTIBLOBS ADD CONSTRAINT ANYNAME PRIMARY KEY (IDKEY)";

  try {
    stmt.execute(order0);
    stmt.execute(order1);
    stmt.execute(order2);
  } 
  catch (SQLSyntaxErrorException s) {
  }  
  catch (Exception e ) {
    e.printStackTrace();
  }
}


void loadImageIntoDB(String fileName, String mode) {

  try {
    PreparedStatement ps = 
      conn.prepareStatement("INSERT INTO TUTTIBLOBS(MODE, FILENAME, IMAGE) " +
      "VALUES (?, ?, ?)");
    ps.setString(1, mode);
    ps.setString(2, fileName);

    File file2Load = new File(dirResources + "/" + fileName);

    /// Mode Stream: Raw data from file into blob column
    if (mode.equals("Stream")) {
      InputStream fInStream = new FileInputStream(file2Load);  
      ps.setBinaryStream(3, fInStream);
    } else
      /// Mode Blob: Raw data in a blob object into a blob column
      if (mode.equals("Blob")) {
        Blob blob = conn.createBlob();
        copyFile(blob.setBinaryStream(1), new FileInputStream(file2Load));
        ps.setBlob(3, blob);
      }
    ps.executeUpdate();
  } 
  catch (Exception e ) {
    e.printStackTrace();
  }
}
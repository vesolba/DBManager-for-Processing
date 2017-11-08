//

void  startServer() {
  try {
    server = new NetworkServerControl();
    server.start (null);
    println("Server started");
  } 
  catch (Exception e ) {
    e.printStackTrace();
  }
}

void  stopServer() {
  try {
    if (server!=null) server.shutdown();
    println("Server stopped");
  } 
  catch (Exception e ) {
    e.printStackTrace();
  }
}

void openDBbyServer(String db2Open) { //<>//
  try { //<>//
    conn = DriverManager.getConnection("jdbc:derby://localhost:1527/" //<>//
      + db2Open + ";create=true"); //<>//
    stmt = conn.createStatement(); //<>//
    println("DB is open"); //<>//
  }  //<>//
  catch (Exception e ) { //<>//
    e.printStackTrace(); //<>//
  } //<>//
}

void closeDBbyServer() {
  try {
    if (conn != null)
    {
      // Retrieve an url from a conn
      String myURL = conn.getMetaData().getURL();

      // To unlock the database is necesary to close it first.
      DriverManager.getConnection(myURL + ";shutdown=true");
      conn.close();
    }
  } 
  catch (Exception e ) {
 //   e.printStackTrace();
  }
}

void openEmbeddedDB(String db2Open) {
  try {
    conn = DriverManager.getConnection("jdbc:derby:" + db2Open
      + ";create=true");
    // conn.setAutoCommit(false);
    stmt = conn.createStatement();
    print("DB is open");
  } 
  catch (Exception e ) {
    e.printStackTrace();
  }
}

void closeEmbeddedDB(String db2Close) {
  try {
    conn = DriverManager.getConnection("jdbc:derby:" + db2Close
      + ";shutdown=true");
    // conn.setAutoCommit(false);
    stmt = conn.createStatement();
    print("DB is open");
  } 
  catch (Exception e ) {
    e.printStackTrace();
  }
}



void deleteTable(String tableName) {
  try {
    stmt.execute("drop table " + tableName);
  } 
  catch (Exception e ) {
  }
}
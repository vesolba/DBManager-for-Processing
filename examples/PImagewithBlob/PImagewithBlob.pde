import java.io.BufferedOutputStream; //<>//
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringReader;
import java.io.Writer;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.SQLSyntaxErrorException;  
import java.sql.ResultSet;
import java.sql.DriverManager;
import java.sql.Blob;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;
import java.util.Enumeration;

//If this import throws an error, create under your sketch
//directory a subdirectory named "code" and copy there the
//derby jars of your javadb installation.
import org.apache.derby.drda.NetworkServerControl;

static NetworkServerControl server;
Connection conn;
Statement stmt;
ResultSet rset;

String dirResources;
PImage photo;

void setup() {
  size(800, 600);

  // Databases will be allocated under "./sketchPath/databases".
  // Derby allocates each database in a separate directory.
  System.setProperty("derby.system.home", sketchPath("databases"));
  println("sketchPath: " + sketchPath("databases"));

  // Now, lets stablish where the program can take resources
  System.setProperty("user.home", sketchPath("data"));
  dirResources = System.getProperty("user.home");

  try {
    // We initialize the DB to access online mode.
    // To acces in embedded mode use openEmbeddedDB in ConnectToDB.
    startServer();
    openDBbyServer("MyImagesDB"); // If no exist the DB, it is created.
    createBlobsTable();          // Here is created the blobs table.

    // Lets load several images into the database.
    // Some of them as byte streams, and others as blobs.
    // The insertion is always in a DB blob column.
    loadImageIntoDB("backimage.jpg", "Stream");
    loadImageIntoDB("bloggerbutton1.gif", "Stream");
    loadImageIntoDB("icon.jpg", "Stream");
    loadImageIntoDB("mearth.jpg", "Stream");
    loadImageIntoDB("image.png", "Stream");
    loadImageIntoDB("spad.jpg", "Stream");

    loadImageIntoDB("backimage.jpg", "Blob");
    loadImageIntoDB("bloggerbutton1.gif", "Blob");
    loadImageIntoDB("icon.jpg", "Blob");
    loadImageIntoDB("mearth.jpg", "Blob");
    loadImageIntoDB("image.png", "Blob");
    loadImageIntoDB("spad.jpg", "Blob");
  } 
  catch (Exception e ) {
    e.printStackTrace();
  }

  noLoop();
}

void draw() {
  // This is a system temporary path where we can write auxiliary files.  
  String tempPath = System.getProperty("java.io.tmpdir");

  // Here we extract the images from the database 
  // and display them into the sketch window.
  try {
    stmt = conn.createStatement();
    String sql = "SELECT * from TUTTIBLOBS";
    ResultSet rs = stmt.executeQuery(sql);
    File tempFile = null;

    int counter = 0, counter2 = 0;
    while (rs.next()) {
      String mode = rs.getString("MODE");
      String filename = rs.getString("FILENAME");

      // The image is extracted to a temporary file 
      // and then loaded in the PImage

      tempFile = File.createTempFile("dbp", filename, new File(tempPath));
      PImage photo = null;

      switch (mode) {
      case "Stream":
        copyFile(new FileOutputStream(tempFile), rs.getBinaryStream("IMAGE"));
        photo = loadImage(tempFile.toString());
        textSize(24);
        fill(0, 102, 153, 51);
        text("Saved as Stream in a database blob column", width / 3, 30); 
        break;

      case "Blob":
        Blob blob = rs.getBlob("IMAGE");
        copyFile(new FileOutputStream(tempFile), blob.getBinaryStream());
        photo = loadImage(tempFile.toString());
        text("Saved as Blob in a database blob column", width / 3, height/2); 
        break;

      default:
      }

      image(photo, counter * width/10, 20 + counter++ * height/30 + counter2 * height/2, 150, 75);
      if (counter > 5) {
        counter = 0;
        counter2++;
      }
    }
  } 
  catch (Exception e ) {
    e.printStackTrace();
  }
}
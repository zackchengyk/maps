package edu.brown.cs.jwu175zcheng12.csvdataset;

import edu.brown.cs.jwu175zcheng12.repl.GeneralREPL;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * An abstract class which represents a CSV parser. Children of this
 * class will typically be datasets which will inherit the ability to
 * read from CSV files.
 *
 * @param <T> the class type which will represent a row of the
 *            input CSV files
 */
public abstract class GeneralCSVDataset<T> {

  private List<T> allData = null;
  private final String entryName;
  private final String[] headers;

  /**
   * The constructor for this abstract class. Note that a copy must be made of
   * the headers argument, since if not the caller might be able to change its
   * cells after the thing that extends GeneralCSVDataset has been created.
   *
   * @param entryName the name of the thing each row represents,
   *                  e.g. "stars" or "mock people"
   * @param headers   a String[] which gives the correct field headers
   *                  to expect of any CSV files parsed later
   */
  GeneralCSVDataset(String entryName, String[] headers) {
    this.entryName = entryName;
    this.headers = headers.clone();
  }

  /**
   * A getter function for this class' allData field.
   *
   * @param information the thing of type T to be added to allData
   */
  protected final void addToAllData(T information) {
    this.allData.add(information);
  }

  /**
   * A getter function for this class' allData field.
   *
   * @return an unmodifiable Collection of T, the allData field of this class,
   * or null if allData is null.
   */
  public final List<T> getAllData() {
    if (this.allData == null) {
      return null;
    }
    return Collections.unmodifiableList(this.allData);
  }

  /**
   * A setter function for this class' allData field.
   *
   * @param newData the List to replace allData
   */
  protected final void setAllData(List<T> newData) {
    this.allData = newData;
  }

  /**
   * A function which attempts to load T data, from a csv file
   * whose file name is provided, into allData. In the event of any error,
   * allData should be set to null.
   *
   * @param filename              the file name of the csv file from which
   *                              star data should be read
   * @param checkFirstLineHeaders a boolean indicating whether the first line
   *                              of the CSV file should be checked against
   *                              this.headers
   */
  public void loadData(String filename, boolean checkFirstLineHeaders) {
    BufferedReader csvReader = null;
    try {
      // Create a new allData collection
      this.allData = new ArrayList<>();
      // Open the reader
      String filePath = new File("").getAbsolutePath();
      csvReader = new BufferedReader(
          new FileReader(filePath + "/" + filename,
              StandardCharsets.UTF_8));
      // Check the header line of the CSV file
      String rowString;
      int counter = 0;
      if (checkFirstLineHeaders && this.headers != null) {
        rowString = csvReader.readLine();
        if (rowString == null) {
          GeneralREPL.printError("File '"
              + filename + "' is completely empty.");
          throw new IOException("File '" + filename + "' is completely empty.");
        }
        String[] rowStringArray = rowString.split(",");
        if (!Arrays.equals(this.headers, rowStringArray)) {
          GeneralREPL.printError("Header mismatch. File '"
              + filename + "' not parsed.");
          throw new IOException("Header mismatch. File '" + filename
              + "' not parsed.");
        }
      } else {
        // Skip the headers line
        csvReader.readLine();
      }
      rowString = csvReader.readLine();
      while (rowString != null) {
        String[] rowStringArray = rowString.split(",");
        handleOneCSVRow(rowString, rowStringArray, counter);
        counter++;
        rowString = csvReader.readLine();
      }
      // Print message
      GeneralREPL.print("Read " + counter + " " + this.entryName + " from " + filename);
    } catch (FileNotFoundException error) {
      // Catch a FileNotFoundException
      GeneralREPL.printError("File does not exist.");
      // In the event of any error, allData should be set to null
      allData = null;
    } catch (Exception error) {
      // Catch any Exception
      GeneralREPL.printError(error.getClass().getCanonicalName()
          + " returned when trying to read file '" + filename + "' as CSV.");
      // In the event of any error, allData should be set to null
      allData = null;
    } finally {
      // Close the reader
      if (csvReader != null) {
        try {
          csvReader.close();
        } catch (IOException error) {
          // Catch an IOException
          GeneralREPL.printError("IOException returned when trying to close "
              + "csvReader.");
        }
      }
    }
  }

  /**
   * A function which handles a single row of the CSV file.
   * Usually creates an object of type T and puts it in the collection.
   *
   * @param rowString      a String, the row as read by BufferedReader
   * @param rowStringArray a String[], the result of splitting rowString
   *                       on commas
   * @param counter        an int, just a counter for debugging printouts which
   *                       represents the number of rows read so far, not
   *                       including the header row
   *
   * @throws IOException throws an Exception if a malformed CSV row is found
   */
  protected abstract void handleOneCSVRow(String rowString,
                                          String[] rowStringArray, int counter)
      throws IOException;
}

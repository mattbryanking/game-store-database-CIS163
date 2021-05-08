
import javax.swing.table.AbstractTableModel;
import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

/*************************************************************************
 * Class to create, organize, load, and save lists of Rental objects for
 * different screens in the GUI
 *
 * @author Matthew King, Priscila Ontiveros-Chucatiny, Benjamin Stenglein
 * @version 3/7/2020
 ************************************************************************/

 public class ListModel extends AbstractTableModel {

    /** a universal version identifier for a Serializable class */
    private static final long serialVersionUID = 1L;

    /** holds all the rentals */
    private ArrayList<Rental> listOfRentals;

    /** holds only the rentals that are to be displayed */
    private ArrayList<Rental> filteredListRentals;

    /** current screen being displayed */
    private ScreenDisplay display = ScreenDisplay.CurrentRentalStatus;

    /** 
     * list of collumns needed for Current Rental Screen, both Within 7 Days Screens,
     * and 14 Days Late Screen.
     */
    private String[] columnNamesCurrentRentals = {"Renter\'s Name", "Est. Cost",
            "Rented On", "Due Date ", "Console", "Name of the Game"};
    
    /** list of collumns needed for Returned Screen */
    private String[] columnNamesReturned = {"Renter\'s Name", "Rented On Date",
            "Due Date", "Actual date returned ", "Est. Cost", " Real Cost"};

    /** list of collumns needed for Everything Screen */
    private String[] columnEverything = {"Renter\'s Name", "Rented On Date",
            "Due Date", "Actual date returned ", "Est. Cost", " Real Cost",
            "Console", "Name of the Game"};
    
    /** list of collumns needed for Late Rentals Screen */
    private String[] columnLate = {"Renter\'s Name", "Est. Cost",
            "Rented On", "Due Date ", "No. of Days Late", "Console", "Name of the Game"};

    /** standard formatter for all dates displayed in GUI */
    private DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");

    
    
    /******************************************************************
	 * Constructor prepares lists and default screen for GUI.
	 *****************************************************************/
    public ListModel() {
        // sets starting screen to CurrentRentalStatus
        display = ScreenDisplay.CurrentRentalStatus;
        listOfRentals = new ArrayList<>();
        filteredListRentals = new ArrayList<>();
        updateScreen();
        createList();
    }

    /******************************************************************
	 * Makes display variable hold the desired screen that is 
     * selected in GUI.
	 *
	 * @param selected screen to change to
	 *****************************************************************/
    public void setDisplay(ScreenDisplay selected) {
        display = selected;
        updateScreen();
    }

    /******************************************************************
	 * Filters, sorts, and modifies lists to match display set in
     * setDisplay method, then pushes information to GUI class. 
     * 
     * @throws RuntimeException when display does not match any 
     * expected cases
	 *****************************************************************/
    private void updateScreen() {
        switch (display) {
            case CurrentRentalStatus:
                filteredListRentals = (ArrayList<Rental>) listOfRentals.stream()
                    // removes rentals that have been returned
                    .filter(n -> n.actualDateReturned == null)
                    // sets all rentals to proper capitalization
                    .filter(n -> {
                            n.setNameOfRenter(n.getNameOfRenter().substring(0, 1) + n.getNameOfRenter().substring(1).toLowerCase());
                            return true;
                    })
                    .collect(Collectors.toList());
                
                // sorts rentals by name of renter
                Collections.sort(filteredListRentals, (n1, n2) -> n1.nameOfRenter.compareTo(n2.nameOfRenter));
                break;

            case ReturnedItems:
                filteredListRentals = (ArrayList<Rental>) listOfRentals.stream()
                    // removes rentals that have been returned
                    .filter(n -> n.actualDateReturned != null)
                    // sets all rentals to proper capitalization
                    .filter(n -> {                       
                            n.setNameOfRenter(n.getNameOfRenter().substring(0, 1) + n.getNameOfRenter().substring(1).toLowerCase());
                            return true;
                    })
                    .collect(Collectors.toList());

                // sorts rentals by name of renter
                Collections.sort(filteredListRentals, new Comparator<Rental>() {
                    @Override
                    public int compare(Rental n1, Rental n2) {
                        return n1.nameOfRenter.compareTo(n2.nameOfRenter);
                    }
                });
                break;

            case DueWithInWeek:
                filteredListRentals = (ArrayList<Rental>) listOfRentals.stream()
                    // removes rentals that have been returned
                    .filter(n -> n.actualDateReturned == null)
                    // removes rentals that have more than 7 days between rented and due date
                    .filter(n -> daysBetween(n.rentedOn , n.dueBack) <= 7)
                    // sets all rentals to proper capitalization
                    .filter(n -> {
                            n.setNameOfRenter(n.getNameOfRenter().substring(0, 1) + n.getNameOfRenter().substring(1).toLowerCase());
                            return true;
                    })
                    .collect(Collectors.toList());

                // sorts rentals by name of renter
                Collections.sort(filteredListRentals, (n1, n2) -> n1.nameOfRenter.compareTo(n2.nameOfRenter));
                break;

            case DueWithinWeekGamesFirst:
                filteredListRentals = (ArrayList<Rental>) listOfRentals.stream()
                    // removes rentals that have been returned
                    .filter(n -> n.actualDateReturned == null)
                    // removes rentals that have more than 7 days between rented and due date
                    .filter(n -> daysBetween(n.rentedOn , n.dueBack) <= 7)
                    // sets all rentals to proper capitalization
                    .filter(n -> {
                            n.setNameOfRenter(n.getNameOfRenter().substring(0, 1) + n.getNameOfRenter().substring(1).toLowerCase());
                            return true;
                    })
                    .collect(Collectors.toList());
                
                // creates new arraylist of only games to be sorted seperatly 
                ArrayList<Rental> tempGame = (ArrayList<Rental>) filteredListRentals.stream()
                    .filter(n -> n instanceof Game)
                    .collect(Collectors.toList());
                // sorts rentals by name of renter
                Collections.sort(tempGame, (n1, n2) -> n1.nameOfRenter.compareTo(n2.nameOfRenter));
                
                // creates new arraylist of only consoles to be sorted seperatly 
                ArrayList<Rental> tempConsole = (ArrayList<Rental>) filteredListRentals.stream()
                    .filter(n -> n instanceof Console)
                    .collect(Collectors.toList());
                // sorts rentals by name of renter
                Collections.sort(tempConsole, (n1, n2) -> n1.nameOfRenter.compareTo(n2.nameOfRenter));

                // combines lists in order of games -> consoles
                tempGame.addAll(tempConsole);
                filteredListRentals = tempGame;
                break;

            case Cap14DaysOverdue:
                filteredListRentals = (ArrayList<Rental>) listOfRentals.stream()
                    // removes rentals that have been returned
                    .filter(n -> n.actualDateReturned == null)
                    // removes rentals that have less than 8 days between rented and due date
                    .filter(n -> daysBetween(n.rentedOn , n.dueBack) > 7)
                    .collect(Collectors.toList());
                
                // creates new arraylist of only consoles to be capitalized and sorted seperatly 
                ArrayList<Rental> tempCapitalConsole = (ArrayList<Rental>) filteredListRentals.stream()
                    .filter(n -> n instanceof Console)
                    // capitalizes names if more than 14 days overdue, removes otherwise
                    .filter(n -> {
                    if (daysBetween(n.rentedOn , n.dueBack) >= 14) {
                        n.setNameOfRenter(n.getNameOfRenter().toUpperCase());
                        return true;
                    }
                    return false;
                    })
                    .collect(Collectors.toList());
                // sorts rentals by name of renter
                Collections.sort(tempCapitalConsole, (n1, n2) -> n1.nameOfRenter.compareTo(n2.nameOfRenter));

                // creates new arraylist of only consoles to be sorted seperatly
                ArrayList<Rental> tempLowerConsole = (ArrayList<Rental>) filteredListRentals.stream()
                    .filter(n -> n instanceof Console)
                    // removes rentals that are more than 14 days overdue
                    .filter(n -> {
                        if (daysBetween(n.rentedOn , n.dueBack) >= 14) {
                            return false;
                        }   
                        return true;
                    })
                    .collect(Collectors.toList());
                // sorts rentals by name of renter
                Collections.sort(tempLowerConsole, (n1, n2) -> n1.nameOfRenter.compareTo(n2.nameOfRenter));
 
                // creates new arraylist of only games to be capitalized and sorted seperatly
                ArrayList<Rental> tempCapitalGame = (ArrayList<Rental>) filteredListRentals.stream()
                    .filter(n -> n instanceof Game)
                    // capitalizes names if more than 14 days overdue, removes otherwise
                    .filter(n -> {
                        if (daysBetween(n.rentedOn , n.dueBack) >= 14) {
                            n.setNameOfRenter(n.getNameOfRenter().toUpperCase());
                            return true;
                        }
                    return false;
                    })
                    .collect(Collectors.toList());
                // sorts rentals by name of renter
                Collections.sort(tempCapitalGame, (n1, n2) -> n1.nameOfRenter.compareTo(n2.nameOfRenter));

                // creates new arraylist of only games to be sorted seperatly
                ArrayList<Rental> tempLowerGame = (ArrayList<Rental>) filteredListRentals.stream()
                    .filter(n -> n instanceof Game)
                    // removes rentals that are more than 14 days overdue
                    .filter(n -> {
                        if (daysBetween(n.rentedOn , n.dueBack) >= 14) {
                            return false;
                        }
                        return true;
                    })
                    .collect(Collectors.toList());
                // sorts rentals by name of renter
                Collections.sort(tempLowerGame, (n1, n2) -> n1.nameOfRenter.compareTo(n2.nameOfRenter));
                
                // combines capital lists in order of consoles -> games
                tempCapitalConsole.addAll(tempCapitalGame);
                ArrayList<Rental> tempCapitalList = tempCapitalConsole;

                // combines non-capital lists in order of consoles -> games
                tempLowerConsole.addAll(tempLowerGame);
                ArrayList<Rental> tempLowerList = tempLowerConsole;

                // combines lists in order of capital -> non-capital
                tempCapitalList.addAll(tempLowerList);
                filteredListRentals = tempCapitalList;
                break;
            
            case EveryThingScreen:
                filteredListRentals = (ArrayList<Rental>) listOfRentals.stream()
                    // sets all rentals to proper capitalization
                    .filter(n -> {
                            n.setNameOfRenter(n.getNameOfRenter().substring(0, 1) + n.getNameOfRenter().substring(1).toLowerCase());
                            return true;

                    })
                    .collect(Collectors.toList());

                // sorts rentals by name of renter
                Collections.sort(filteredListRentals, (n1, n2) -> n1.nameOfRenter.compareTo(n2.nameOfRenter));
                break;

            case LateRentalScreen:
                filteredListRentals = (ArrayList<Rental>) listOfRentals.stream()
                    // removes rentals that have been returned
                    .filter(n -> n.actualDateReturned == null)
                    // sets all rentals to proper capitalization
                    .filter(n -> {
                            n.setNameOfRenter(n.getNameOfRenter().substring(0, 1) + n.getNameOfRenter().substring(1).toLowerCase());
                            return true;
                    })
                    // removes rentals that are not late
                    .filter(n -> daysLate(n.dueBack) > 0)
                    .collect(Collectors.toList());

                // creates new arraylist of only games to be sorted seperatly
                ArrayList<Rental> tempGameLate = (ArrayList<Rental>) filteredListRentals.stream()
                    .filter(n -> n instanceof Game)
                    .collect(Collectors.toList());
                Collections.sort(tempGameLate, new Comparator<Rental>() {
                    @Override
                    public int compare(Rental n1, Rental n2) {
                        // sorts rentals by how many days late they are
                        int result = ((Integer)daysLate(n1.dueBack)).compareTo(daysLate(n2.dueBack));
                        // if two rentals are equally late, they're sorted by name
                        if (result == 0)
                            result = n1.nameOfRenter.compareTo(n2.nameOfRenter);
                        return result;
                    }
                });

                // creates new arraylist of only consoles to be sorted seperatly
                ArrayList<Rental> tempConsoleLate = (ArrayList<Rental>) filteredListRentals.stream()
                    .filter(n -> n instanceof Console)
                    .collect(Collectors.toList());
                Collections.sort(tempConsoleLate, new Comparator<Rental>() {
                    @Override
                    public int compare(Rental n1, Rental n2) {  
                        // sorts rentals by how many days late they are
                        int result = ((Integer)daysLate(n1.dueBack)).compareTo(daysLate(n2.dueBack));
                        // if two rentals are equally late, they're sorted by name
                        if (result == 0)
                            result = n1.nameOfRenter.compareTo(n2.nameOfRenter);
                        return result;
                    }
                });

                // combines lists in order of games -> consoles
                tempGameLate.addAll(tempConsoleLate);
                filteredListRentals = tempGameLate;
                break;

            default:
                throw new RuntimeException("upDate is in undefined state: " + display);
        }
        // pushes information to GUI
        fireTableStructureChanged();
    }
    
    /******************************************************************
     * Compares two GregorianCalendar dates. 
     * Order of parameters matter: if the first date is later than 
     * the second date, a negative integer will be returned.
     * 
     * @param startDate Gregorian Calendar object representing the 
     * earlier date to be compared
     * @param endDate Gregorian Calendar object representing the later 
     * date to be compared
     * @return an int representing how many days are between both 
     * dates 
     *****************************************************************/
    private int daysBetween(GregorianCalendar startDate, GregorianCalendar endDate) {
		// Determine how many days the Game was rented out
		GregorianCalendar gTemp = new GregorianCalendar();
		gTemp = (GregorianCalendar) endDate.clone();
		int daysBetween = 0;
		while (gTemp.compareTo(startDate) > 0) {
            // this subtracts one day from gTemp
			gTemp.add(Calendar.DATE, -1);  
			daysBetween++;
		}
		return daysBetween;
	}

    /******************************************************************
     * Compares a GregorianCalendar date to the current date.
     * The inputted date should be earlier than the current date in
     * most cases; if the inputted date is later, a negative integer 
     * will be returned.
     * 
     * @param pastDate Gregorian Calendar object representing date
     * before current day to be compared
     * @return an int representing how many days are between entered
     * date and the current day
     *****************************************************************/
	private int daysLate(GregorianCalendar pastDate) {
        // default GregorianCalendar is current day pulled from system information
        GregorianCalendar currentDay = new GregorianCalendar();
        return daysBetween(pastDate, currentDay); 
    }

    /******************************************************************
     * Retrieves the name of a desired column in the GUI using a set
     * list of names for the current display.
     * 
     * @param col int representing the column to fetch the name of
     * @return a String of the column's name
     * @throws RuntimeException when display does not match any 
     * expected cases
     *****************************************************************/
       @Override
    public String getColumnName(int col) {
        // returns specified index of arraylist of column names
        switch (display) {
            case CurrentRentalStatus:
                return columnNamesCurrentRentals[col];
            case ReturnedItems:
                return columnNamesReturned[col];
            case DueWithInWeek:
                return columnNamesCurrentRentals[col];
            case Cap14DaysOverdue:
                return columnNamesCurrentRentals[col];
            case DueWithinWeekGamesFirst:
                return columnNamesCurrentRentals[col];
            case EveryThingScreen:
                return columnEverything[col];
            case LateRentalScreen:
                return columnLate[col];
        }
        throw new RuntimeException("Undefined state for Col Names: " + display);
    }

    /******************************************************************
     * Retrieves the amount of columns needed to correctly show the
     * current display.
     * 
     * @return an int of the total columns needed
     * @throws IllegalArgumentException when display does not match any 
     * expected cases
     *****************************************************************/
    @Override
    public int getColumnCount() {
        // returns number of items in arraylist of column names
        switch (display) {
            case CurrentRentalStatus:
                return columnNamesCurrentRentals.length;
            case ReturnedItems:
                return columnNamesReturned.length;
            case DueWithInWeek:
                return columnNamesCurrentRentals.length;
            case Cap14DaysOverdue:
                return columnNamesCurrentRentals.length;
            case DueWithinWeekGamesFirst:
                return columnNamesCurrentRentals.length;
            case EveryThingScreen:
                return columnEverything.length;
            case LateRentalScreen:
                return columnLate.length;
        }
        throw new IllegalArgumentException();
    }

    /******************************************************************
     * Retrieves the amount of rows needed to correctly show the
     * current display.
     *****************************************************************/
    @Override
    public int getRowCount() {
        // returns number of items in the arraylist
        return filteredListRentals.size();     
    }

    /******************************************************************
     * Retrieves information to be displayed at row/column location 
     * based on current display.
     * 
     * @param row integer for desired row in GUI
     * @param col integer for desired column in GUI
     * @return relevant object to be displayed
     * @throws IllegalArgumentException when display does not match any 
     * expected cases
     *****************************************************************/
    @Override
    public Object getValueAt(int row, int col) {
        switch (display) {
            case CurrentRentalStatus:
                return currentRentScreen(row, col);
            case ReturnedItems:
                return rentedOutScreen(row, col);
            case DueWithInWeek:
                return currentRentScreen(row, col);
            case Cap14DaysOverdue:
                return currentRentScreen(row, col);
            case DueWithinWeekGamesFirst:
                return currentRentScreen(row, col);
            case EveryThingScreen:
                return EverythingScreen(row, col);
            case LateRentalScreen:
                return LateRentalScreen(row,col);
        }
        throw new IllegalArgumentException();
    }

    /******************************************************************
     * Retrieves information to be displayed at specified location on
     * Current Rental Screen, Within 7 Days Screen, Within 7 Days 
     * Games First Screen and 14 Days Late Screen.
     * 
     * @param row integer for desired row in GUI
     * @param col integer for desired column in GUI
     * @return relevant object to be displayed
     * @throws RuntimeException row or column is out of range
     *****************************************************************/
    private Object currentRentScreen(int row, int col) {
        switch (col) {
            case 0:
                // this returns the name of the renter
                return (filteredListRentals.get(row).nameOfRenter);

            case 1:
                // this returns estimated cost of rental
                return (filteredListRentals.get(row).getCost(filteredListRentals.
                        get(row).dueBack));

            case 2:
                // this returns the date the item was rented on
                return (formatter.format(filteredListRentals.get(row).rentedOn.getTime()));

            case 3:
                // checks if item has been returned
                if (filteredListRentals.get(row).dueBack == null)
                    return "-";

                // this returns the date the item is due
                return (formatter.format(filteredListRentals.get(row).dueBack.getTime()));

            case 4:
                // checks if item is a game or console
                if (filteredListRentals.get(row) instanceof Console)
                    // this returns the name of the console
                    return (((Console) filteredListRentals.get(row)).getConsoleType());
                else {
                    // checks if item is a game or console
                    if (filteredListRentals.get(row) instanceof Game)
                        // checks if console was rented along with game
                        if (((Game) filteredListRentals.get(row)).getConsole() != null)
                            // this returns the name of the console if above is true
                            return ((Game) filteredListRentals.get(row)).getConsole();
                        else
                            return "";
                }

            case 5:
                // checks if item is a game
                if (filteredListRentals.get(row) instanceof Game)
                    // this returns the name of the game
                    return (((Game) filteredListRentals.get(row)).getNameGame());
                else
                    return "";
            
            default:
                throw new RuntimeException("Row,col out of range: " + row + " " + col);
        }
    }

     /******************************************************************
     * Retrieves information to be displayed at specified location on
     * Late Rental Screen.
     * 
     * @param row integer for desired row in GUI
     * @param col integer for desired column in GUI
     * @return relevant object to be displayed
     * @throws RuntimeException row or column is out of range
     *****************************************************************/
    private Object LateRentalScreen(int row, int col){
        switch (col) {
            case 0:
                // this returns the name of the renter
                return (filteredListRentals.get(row).nameOfRenter);

            case 1:
                // this returns estimated cost of rental
                return (filteredListRentals.get(row).getCost(filteredListRentals.
                        get(row).dueBack));

            case 2:
                // this returns the date the item was rented on
                return (formatter.format(filteredListRentals.get(row).rentedOn.getTime()));

            case 3:
                // checks if item has been returned
                if (filteredListRentals.get(row).dueBack == null)
                    return "-";
                // this returns the date the item is due
                return (formatter.format(filteredListRentals.get(row).dueBack.getTime()));

            case 4:
                // checks if item has been returned
                if (filteredListRentals.get(row).dueBack == null)
                    return "-";
                // this returns the amount of days past due the item has been rented
                return daysLate(filteredListRentals.get(row).dueBack);
                
            case 5:
                // checks if item is a game or console
                if (filteredListRentals.get(row) instanceof Console)
                    // this returns the name of the console
                    return (((Console) filteredListRentals.get(row)).getConsoleType());
                else {
                    // checks if item is a game or console
                    if (filteredListRentals.get(row) instanceof Game)
                        // checks if console was rented along with game
                        if (((Game) filteredListRentals.get(row)).getConsole() != null)
                            // this returns the name of the console if above is true
                            return ((Game) filteredListRentals.get(row)).getConsole();
                    else
                        return "";
                }   

            case 6:
                // checks if item is a game
                if (filteredListRentals.get(row) instanceof Game)
                    // this returns the name of the game
                    return (((Game) filteredListRentals.get(row)).getNameGame());
                else
                    return "";
                      
            default:
                throw new RuntimeException("Row,col out of range: " + row + " " + col);
        }
    }

     /******************************************************************
     * Retrieves information to be displayed at specified location on
     * Everything Screen.
     * 
     * @param row integer for desired row in GUI
     * @param col integer for desired column in GUI
     * @return relevant object to be displayed
     * @throws RuntimeException row or column is out of range
     *****************************************************************/
    private Object EverythingScreen(int row, int col) {
        switch (col) {
            case 0: 
                // this returns the name of the renter
                return (filteredListRentals.get(row).nameOfRenter);
            
            case 1: 
                // this returns the date the item was rented on
                return (formatter.format(filteredListRentals.get(row).rentedOn.getTime()));
            
            case 2: 
                // checks if item has been returned 
                if (filteredListRentals.get(row).dueBack == null)
                    return "-";
                // this returns the date the item is due
                return (formatter.format(filteredListRentals.get(row).dueBack.getTime()));          

            case 3: 
                // this returns "Not Returned" if item has not been returned
                if (filteredListRentals.get(row).getActualDateReturned() == null)
                    return "Not Returned";
                // this returns the date the item was returned
                return (formatter.format(filteredListRentals.get(row).actualDateReturned.getTime()));

            case 4: 
                // this returns estimated cost of rental
                if(filteredListRentals.get(row).getCost(filteredListRentals.
                    get(row).dueBack) == 0)
                        return "Not Returned";
                return (filteredListRentals.get(row).getCost(filteredListRentals.
                    get(row).dueBack)); 

            case 5: 
                // this returns actual cost of rental
                if (filteredListRentals.get(row).getActualDateReturned() == null)
                        return "Not Returned";
                return (filteredListRentals.get(row).getCost(filteredListRentals.
                    get(row).dueBack)); 

            case 6: 
                // checks if item is a game or console
                if (filteredListRentals.get(row) instanceof Console)
                    // this returns the name of the console
                    return (((Console) filteredListRentals.get(row)).getConsoleType());
                else {
                    // checks if item is a game or console
                    if (filteredListRentals.get(row) instanceof Game)
                        // checks if console was rented along with game
                        if (((Game) filteredListRentals.get(row)).getConsole() != null)
                            // this returns the name of the console if above is true
                            return ((Game) filteredListRentals.get(row)).getConsole();
                        else
                            return "";
                }
    
            case 7: 
                // checks if item is a game
                if (filteredListRentals.get(row) instanceof Game)
                    // this returns the name of the game
                    return (((Game) filteredListRentals.get(row)).getNameGame());
                else
                    return "";
            
            default:
                throw new RuntimeException("Row,col out of range: " + row + " " + col);
        }
    }

     /******************************************************************
     * Retrieves information to be displayed at specified location on
     * Returned Screen.
     * 
     * @param row integer for desired row in GUI
     * @param col integer for desired column in GUI
     * @return relevant object to be displayed
     * @throws RuntimeException row or column is out of range
     *****************************************************************/
    private Object rentedOutScreen(int row, int col) {
        switch (col) {
            case 0:
                // this returns the name of the renter
                return (filteredListRentals.get(row).nameOfRenter);

            case 1:
                // this returns the date the item was rented on
                return (formatter.format(filteredListRentals.get(row).rentedOn.
                    getTime()));

            case 2:
                // this returns the date the item is due
                return (formatter.format(filteredListRentals.get(row).dueBack.
                    getTime()));

            case 3:
                // this returns the date the item was returned
                return (formatter.format(filteredListRentals.get(row).
                    actualDateReturned.getTime()));

            case 4:
                // this returns estimated cost of rental
                return (filteredListRentals.
                    get(row).getCost(filteredListRentals.get(row).dueBack));

            case 5:
                // this returns actual cost of rental
                return (filteredListRentals.
                    get(row).getCost(filteredListRentals.get(row).
                    actualDateReturned));

            default:
                throw new RuntimeException("Row,col out of range: " + row + " " + col);
        }
    }

    /****************************************************************
     * A method to add one Rental object to the overall list of
     * objects.
     * 
     * @param a Rental object to add to list
     ****************************************************************/
    public void add(Rental a) {
        listOfRentals.add(a);
        updateScreen();
    }

    /****************************************************************
     * A method to access one element from the filtered list rentals
     * This method is used when filtering streams depending on
     * different parameters
     * 
     * @param i the element desired to access
     * @return a rental object
     ****************************************************************/
    public Rental get(int i) {
        return filteredListRentals.get(i);
    }

    /******************************************************************
     * A method to upadate the current screen by calling the main
     * updateScreen method
     * 
     * @param index where to start on the list
     * @param unit  the unit desired to show when updating the screen
     *****************************************************************/
    public void update(int index, Rental unit) {
        updateScreen();
    }

    /******************************************************************
     * A method to save a list of all rentals in the 
     * format of a database
     * 
     * @param filename the name of the file to save by
     * @throws RuntimeException for various errors saving file
     *****************************************************************/
    public void saveDatabase(String filename) {
        try {
            FileOutputStream fos = new FileOutputStream(filename);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            System.out.println(listOfRentals.toString());
            os.writeObject(listOfRentals);
            os.close();
        } catch (IOException ex) {
            throw new RuntimeException("Saving problem! " + display);
        }
    }

    /******************************************************************
     * A method to load a list of rentals from a file
     * 
     * @param the desired filename
     * @throws RuntimeException for various errors loading file
     *****************************************************************/
    public void loadDatabase(String filename) {
        listOfRentals.clear();
        
        try {
            FileInputStream fis = new FileInputStream(filename);
            ObjectInputStream is = new ObjectInputStream(fis);

            
            listOfRentals = (ArrayList<Rental>) is.readObject();
            updateScreen();
            is.close();
        } catch (Exception ex) {
            throw new RuntimeException("Loading problem: " + display);
        }
    }

    /******************************************************************
     * A method to save a list of rentals as a text file
     * 
     * @param filename the desired file name when saving the list
     * @return true if process was successful
     *****************************************************************/
    public boolean saveAsText(String filename) {
        // if filename is empty thow error
        if (filename.equals("")) {
            throw new IllegalArgumentException();
        }
        
        // start writing on text file
        try {
            PrintWriter out = new PrintWriter(new BufferedWriter(
                    new FileWriter(filename)));
            out.println(listOfRentals.size());
            for (int i = 0; i < listOfRentals.size(); i++) {
                Rental unit = listOfRentals.get(i);
                out.println(unit.getClass().getName());
                out.println("Name is " + unit.getNameOfRenter());
                out.println("Rented on " + formatter.format(unit.rentedOn.getTime()));
                out.println("DueDate " + formatter.format(unit.dueBack.getTime()));

                if (unit.getActualDateReturned() == null) {
                    out.println("Not returned!");
                }
                else {
                    out.println(formatter.format(unit.actualDateReturned.getTime()));
                }
                if (unit instanceof Game) {
                    out.println(((Game) unit).getNameGame());
                    if (((Game) unit).getConsole() != null)
                        out.println(((Game) unit).getConsole());
                    else
                        out.println("No Console");
                }

                if (unit instanceof Console) {
                    out.println(((Console) unit).getConsoleType());
                }
            }
            out.close();
            return true;

        } catch (IOException ex) {
            return false;
        }
    }

    /******************************************************************
     * Method that loads rentals from a text file
     * It will iterate through every single line
     * 
     * @param filename the name desired for the file
     * @throws IllegalArgumentException if the filename is null
     * @throws RuntimeException for various errors in creation
     *****************************************************************/
    public void loadFromText(String filename) {
        listOfRentals.clear();
        // local variables to keep track of number of games and
        // consoles scanned
        int games = 0;
        int consoles = 0;
        
        // make sure filename is correct
        if (filename == null) {
			throw new IllegalArgumentException("Null filename");
        }

        // start scanning file
        try
        {
            // Scanner object to scan text file
                Scanner inFS = new Scanner(new File(filename));
            // Date formatter object used to create gregorial calendar dates
                SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");

           // while there is a line to scann, loop through text
           while(inFS.hasNext())
           {
            // local string to read number of rentals
            String lineOfText = inFS.nextLine();
            // if the line scanned is a game then assume next lines are 
            // the attributes of a game object
            if (lineOfText.contains(".Game")) { 
                // increase number of games scanned
                games++;

                try
                {
                    // First line read after class line is the name
                    String Name = inFS.nextLine();
                    // Discard "Name is String"
                    Name = Name.substring(8);

                    // Read the date as string first
                    String d1 = inFS.nextLine();
                    // create a new date object and parse it from string scanned before
                    Date date1 = df.parse(d1.substring(10));
                    // create a greg. calendar object and assign its date
                    GregorianCalendar rentDate = new GregorianCalendar();
                    // assign rent date value
                    rentDate.setTime(date1);

                    // Read the date as string first
                    String d2 = inFS.nextLine();
                    // create a new date object and parse it from string scanned before
                    Date date2 = df.parse(d2.substring(8));
                    // create a greg. calendar object and assign its date
                    GregorianCalendar dueDate = new GregorianCalendar();
                    // assign rent date value
                    dueDate.setTime(date2);

                    // Read the return date as string first
                    String d3 = inFS.nextLine();
                    // create a date object and initialize it to a starting value
                    Date date3 = df.parse("00/00/0000");
                    // if the rental has not been returned yet
                    if(!d3.equals("Not returned!")) 
                        date3 = df.parse(d3);
                    // create a greg. calendar object and assign its date
                    GregorianCalendar returnedDate = new GregorianCalendar();
                    // assign rent date value
                    returnedDate.setTime(date3);

                    // Scan and assign title value from string
                    String title = inFS.nextLine();

                    // read console type
                    String consoleType = inFS.nextLine();
                    // Consoletype initially set to null before assigning values
                    ConsoleTypes myConsole = null;
                    
                    // Assign consoletype to myConsole depending on the string scanned
                    switch(consoleType){
                        case "NoSelection":
                            myConsole = ConsoleTypes.NoSelection;
                            break;
                        case "PlayStation4": 
                            myConsole = ConsoleTypes.PlayStation4;
                            break;
                        case "XBoxOneS": 
                            myConsole = ConsoleTypes.XBoxOneS;
                            break;
                        case "PlayStation4Pro": 
                            myConsole = ConsoleTypes.PlayStation4Pro;
                            break;
                        case "NintendoSwich": 
                            myConsole = ConsoleTypes.NintendoSwich;
                            break;
                        case "SegaGenesisMini": 
                            myConsole = ConsoleTypes.SegaGenesisMini;
                            break;
                    }

                    // If there is no date returned parameter should be null
                    Game game1 =  new Game(Name, rentDate, dueDate, null, title, myConsole);
                    
                    // If rental is returned then assign valid returnedDate
                    if(!d3.equals("Not returned!")) 
                        game1 = new Game(Name, rentDate, dueDate, returnedDate, title, myConsole);
                    
                    // add element to list of rentals
                    listOfRentals.add(game1);
                    
                }catch(ParseException e) {
                    throw new RuntimeException("Error in testing, creation of list");
                }     
            }
            
            if (lineOfText.contains(".Console")){
                consoles++;
                try
                {
                    // First line read after class line is the name
                    String Name = inFS.nextLine();
                    // Discard "Name is String"
                    Name = Name.substring(8);

                    // Read the date as string first
                    String d1 = inFS.nextLine();
                    // create a new date object and parse it from string scanned before
                    Date date1 = df.parse(d1.substring(10));
                    // create a greg. calendar object and assign its date
                    GregorianCalendar rentDate = new GregorianCalendar();
                    // assign rent date value
                    rentDate.setTime(date1);

                    // Read the date as string first
                    String d2 = inFS.nextLine();
                    // create a date object and initialize it to a starting value
                    Date date2 = df.parse(d2.substring(8));
                    // create a greg. calendar object and assign its date
                    GregorianCalendar dueDate = new GregorianCalendar();
                    // assign rent date value
                    dueDate.setTime(date2);

                     // Read the return date as string first
                    String d3 = inFS.nextLine();
                    // create a date object and initialize it to a starting value
                    Date date3 = df.parse("00/00/0000");
                    // if the rental has not been returned yet
                    if(!d3.equals("Not returned!"))
                        date3 = df.parse(d3);
                    // create a greg. calendar object and assign its date
                    GregorianCalendar returnedDate = new GregorianCalendar();
                    // assign return date value
                    returnedDate.setTime(date3);

                    // read console type
                    String consoleType = inFS.nextLine();
                    // Consoletype initially set to null before assigning values
                    ConsoleTypes myConsole = null;
                     
                    // Assign consoletype to myConsole depending on the string scanned
                    switch(consoleType){
                        case "NoSelection": 
                            myConsole = ConsoleTypes.NoSelection;
                            break;
                        case "PlayStation4": 
                            myConsole = ConsoleTypes.PlayStation4;
                            break;
                        case "XBoxOneS": 
                            myConsole = ConsoleTypes.XBoxOneS;
                            break;
                        case "PlayStation4Pro": 
                            myConsole = ConsoleTypes.PlayStation4Pro;
                            break;
                        case "NintendoSwich": 
                            myConsole = ConsoleTypes.NintendoSwich;
                            break;
                        case "SegaGenesisMini": 
                            myConsole = ConsoleTypes.SegaGenesisMini;
                            break;
                    }
                    // If there is no date returned parameter should be null
                    Console console1 = new Console(Name, rentDate, dueDate, null, myConsole);
                    // If rental is returned then assign valid returnedDate
                    if(!d3.equals("Not returned!"))
                        console1 = new Console(Name, rentDate, dueDate, returnedDate, myConsole);
                    // add element to list of rentals
                    listOfRentals.add(console1);

                }catch(ParseException e) {
                    throw new RuntimeException("Error in testing, creation of list");
                }
            }
        }
        // close file
        inFS.close();
            
        }catch(IOException error2 ) {
            throw new RuntimeException("Oops! An error occured related to: " + filename);
        }
        // Make sure files have been read correctly
        System.out.print("Amount of games: "+ games + " Amount of consoles: " + consoles);
        updateScreen();
        
    }

    /******************************************************************
     *  DO NOT MODIFY THIS METHOD!!!!!!
     *****************************************************************/
    public void createList() {
        SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
        GregorianCalendar g1 = new GregorianCalendar();
        GregorianCalendar g2 = new GregorianCalendar();
        GregorianCalendar g3 = new GregorianCalendar();
        GregorianCalendar g4 = new GregorianCalendar();
        GregorianCalendar g5 = new GregorianCalendar();
        GregorianCalendar g6 = new GregorianCalendar();
        GregorianCalendar g7 = new GregorianCalendar();
        GregorianCalendar g8 = new GregorianCalendar();

        try {
            Date d1 = df.parse("1/20/2020");
            g1.setTime(d1);
            Date d2 = df.parse("12/22/2020");
            g2.setTime(d2);
            Date d3 = df.parse("12/20/2019");
            g3.setTime(d3);
            Date d4 = df.parse("7/02/2020");
            g4.setTime(d4);
            Date d5 = df.parse("1/20/2010");
            g5.setTime(d5);
            Date d6 = df.parse("9/29/2020");
            g6.setTime(d6);
            Date d7 = df.parse("7/25/2020");
            g7.setTime(d7);
            Date d8 = df.parse("7/29/2020");
            g8.setTime(d8);

            Console console1 = new Console("Person1", g4, g6, null, ConsoleTypes.PlayStation4);
            Console console2 = new Console("Person2", g5, g3, null, ConsoleTypes.PlayStation4);
            Console console3 = new Console("Person5", g4, g8, null, ConsoleTypes.SegaGenesisMini);
            Console console4 = new Console("Person6", g4, g7, null, ConsoleTypes.SegaGenesisMini);
            Console console5 = new Console("Person1", g5, g4, g3, ConsoleTypes.XBoxOneS);

            Game game1 = new Game("Person1", g3, g2, null, "title1", ConsoleTypes.PlayStation4);
            Game game2 = new Game("Person1", g3, g1, null, "title2", ConsoleTypes.PlayStation4);
            Game game3 = new Game("Person1", g5, g3, null, "title2", ConsoleTypes.SegaGenesisMini);
            Game game4 = new Game("Person7", g4, g8, null, "title2", null);
            Game game5 = new Game("Person3", g3, g1, g1, "title2", ConsoleTypes.XBoxOneS);
            Game game6 = new Game("Person6", g4, g7, null, "title1", ConsoleTypes.NintendoSwich);
            Game game7 = new Game("Person5", g4, g8, null, "title1", ConsoleTypes.NintendoSwich);

            add(game1);
            add(game4);
            add(game5);
            add(game2);
            add(game3);
            add(game6);
            add(game7);

            add(console1);
            add(console2);
            add(console5);
            add(console3);
            add(console4);

            // create a bunch of them.
            int count = 0;
            Random rand = new Random(13);
            String guest = null;

            while (count < 0) {  // change this number to 300 for a complete test of your code
                Date date = df.parse("7/" + (rand.nextInt(10) + 2) + "/2020");
                GregorianCalendar g = new GregorianCalendar();
                g.setTime(date);
                
                if (rand.nextBoolean()) {
                    guest = "Game" + rand.nextInt(5);
                    Game game;
                    if (count % 2 == 0)
                        game = new Game(guest, g4, g, null, "title2", ConsoleTypes.NintendoSwich);
                    else
                        game = new Game(guest, g4, g, null, "title2", null);
                    add(game);
                } 
                else {
                    guest = "Console" + rand.nextInt(5);
                    date = df.parse("7/" + (rand.nextInt(20) + 2) + "/2020");
                    g.setTime(date);
                    Console console = new Console(guest, g4, g, null, getOneRandom(rand));
                    add(console);
                }

                count++;
            }
        } catch (ParseException e) {
            throw new RuntimeException("Error in testing, creation of list");
        }
    }
    
    /******************************************************************
     * Method to generate different console types
     * 
     * @param rand a random number
     * @return a random console
     *****************************************************************/
    public ConsoleTypes getOneRandom(Random rand) {
        int number = rand.nextInt(ConsoleTypes.values().length - 1);
        
        switch (number) {
            case 0:
                return ConsoleTypes.PlayStation4;
            case 1:
                return ConsoleTypes.XBoxOneS;
            case 2:
                return ConsoleTypes.PlayStation4Pro;
            case 3:
                return ConsoleTypes.NintendoSwich;
            default:
                return ConsoleTypes.SegaGenesisMini;
        }
    }
}
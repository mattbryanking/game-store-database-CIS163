package project2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.GregorianCalendar;

public class GUIRentalStore extends JFrame implements ActionListener {

    /** a universal version identifier for a Serializable class */
    private static final long serialVersionUID = 1L;

    private JMenuBar menus;

    private JMenu fileMenu;
    private JMenu actionMenu;

    private JMenuItem openSerItem;
    private JMenuItem exitItem;
    private JMenuItem saveSerItem;
    private JMenuItem openTextItem;
    private JMenuItem saveTextItem;
    private JMenuItem rentConsoleItem;
    private JMenuItem rentGameItem;
    private JMenuItem returnItem;

    private JMenuItem currentRentedItemScn;
    private JMenuItem currentReturnedItemScn;
    private JMenuItem withIn7ItemScn;
    private JMenuItem sort30DaysItemScn;
    private JMenuItem sortGameItemScn;
    private JMenuItem EveryThingScreen;
    private JMenuItem LateRentalScreen;

    private JPanel panel;

    private JLabel screenStatus;

    private ListModel dList;

    private JTable jTable;

    private JScrollPane scrollList;

    public GUIRentalStore(){

        screenStatus = new JLabel("Current Rentals Screen");


        menus = new JMenuBar();
        fileMenu = new JMenu("File");
        actionMenu = new JMenu("Action");
        openSerItem = new JMenuItem("Open File");
        exitItem = new JMenuItem("Exit");
        saveSerItem = new JMenuItem("Save File");
        openTextItem = new JMenuItem("Open Text");
        saveTextItem = new JMenuItem("Save Text");
        rentConsoleItem = new JMenuItem("Rent a Console");
        rentGameItem = new JMenuItem("Rent a Game");
        returnItem = new JMenuItem("Return of Game or Console");

        currentRentedItemScn = new JMenuItem("Current Rental Screen");
        currentReturnedItemScn = new JMenuItem("Returned screen");
        withIn7ItemScn = new JMenuItem("Within 7 Days Screen");
        sortGameItemScn = new JMenuItem("Within 7 Days Games First Screen");
        sort30DaysItemScn = new JMenuItem("Cap all Rentals 14 days late Screen");
        EveryThingScreen = new JMenuItem("EveryThing Screen");
        LateRentalScreen = new JMenuItem("Late Rentals Screen");

        fileMenu.add(openSerItem);
        fileMenu.add(saveSerItem);
        fileMenu.addSeparator();
        fileMenu.add(openTextItem);
        fileMenu.add(saveTextItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        fileMenu.addSeparator();
        fileMenu.add(currentRentedItemScn);
        fileMenu.add(currentReturnedItemScn);
        fileMenu.add(withIn7ItemScn);
        fileMenu.add (sortGameItemScn);
        fileMenu.add(sort30DaysItemScn);
        fileMenu.add(EveryThingScreen);
        fileMenu.add(LateRentalScreen);
        fileMenu.addSeparator();

        actionMenu.add(rentConsoleItem);
        actionMenu.add(rentGameItem);
        actionMenu.addSeparator();
        actionMenu.add(returnItem);

        menus.add(fileMenu);
        menus.add(actionMenu);

        openSerItem.addActionListener(this);
        saveSerItem.addActionListener(this);
        openTextItem.addActionListener(this);
        saveTextItem.addActionListener(this);
        exitItem.addActionListener(this);
        rentConsoleItem.addActionListener(this);
        rentGameItem.addActionListener(this);
        returnItem.addActionListener(this);

        currentRentedItemScn.addActionListener(this);
        currentReturnedItemScn.addActionListener(this);
        withIn7ItemScn.addActionListener(this);
        sortGameItemScn.addActionListener(this);
        sort30DaysItemScn.addActionListener(this);
        EveryThingScreen.addActionListener(this);
        LateRentalScreen.addActionListener(this);

        setJMenuBar(menus);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        panel = new JPanel();
        dList = new ListModel();
        jTable = new JTable(dList);
        scrollList = new JScrollPane(jTable);
        panel.add(screenStatus);
        panel.add(scrollList);
        add(panel);
        scrollList.setPreferredSize(new Dimension(1000,800));

        setVisible(true);
        setSize(1070,950);
    }

    public void actionPerformed(ActionEvent e) {
        Object comp = e.getSource();

        if (currentRentedItemScn == comp){
            screenStatus.setText("Current Rentals Screen");
            dList.setDisplay(ScreenDisplay.CurrentRentalStatus);
        }

        if (currentReturnedItemScn == comp){
            dList.setDisplay(ScreenDisplay.ReturnedItems);
            screenStatus.setText("Returned Rentals Screen");
        }

        if (withIn7ItemScn == comp) {
            screenStatus.setText("Due Within a Week Screen");
            GregorianCalendar dat = new GregorianCalendar();
            dList.setDisplay(ScreenDisplay.DueWithInWeek);
        }

        if (sortGameItemScn == comp){
            screenStatus.setText("Due Within a Week Games First Screen");
            dList.setDisplay(ScreenDisplay.DueWithinWeekGamesFirst);
        }

        if (sort30DaysItemScn == comp){
            screenStatus.setText("Capitalize 14 Days Overdue Screen");
            dList.setDisplay(ScreenDisplay.Cap14DaysOverdue);
        }

        if (EveryThingScreen == comp){
            screenStatus.setText("Everything Screen");
            dList.setDisplay(ScreenDisplay.EveryThingScreen);
        }
        if (LateRentalScreen == comp){
            screenStatus.setText("Late Rentals Screen");
            dList.setDisplay(ScreenDisplay.LateRentalScreen);
        }

        if (openSerItem == comp || openTextItem == comp) {
            JFileChooser chooser = new JFileChooser();
            int status = chooser.showOpenDialog(null);
            if (status == JFileChooser.APPROVE_OPTION) {
                String filename = chooser.getSelectedFile().getAbsolutePath();
                if (openSerItem == comp)
                    dList.loadDatabase(filename);
                else if (openTextItem == comp)
                    dList.loadFromText((filename));
            }
        }

        if (saveSerItem == comp || saveTextItem == comp) {
            JFileChooser chooser = new JFileChooser();
            int status = chooser.showSaveDialog(null);
            if (status == JFileChooser.APPROVE_OPTION) {
                String filename = chooser.getSelectedFile().getAbsolutePath();
                if (saveSerItem == e.getSource())
                    dList.saveDatabase(filename);
                else if (saveTextItem == comp)
                    dList.saveAsText(filename);
            }
        }

        if(e.getSource() == exitItem){
            System.exit(1);
        }
        if(e.getSource() == rentConsoleItem){
            Console Console = new Console();
            RentConsoleDialog dialog = new RentConsoleDialog(this, Console);
            if(dialog.getCloseStatus() == RentConsoleDialog.OK){
                dList.add(Console);
            }
        }
        if(e.getSource() == rentGameItem){
            Game gameOnly = new Game();
            RentGameDialog dialog = new RentGameDialog(this, gameOnly);
            if(dialog.getCloseStatus() == RentGameDialog.OK){
                dList.add(gameOnly);
            }
        }

        if (returnItem == e.getSource()) {
            int index = jTable.getSelectedRow();
            if (index != -1) {
                GregorianCalendar dat = new GregorianCalendar();

                Rental unit = dList.get(index);
                ReturnedOnDialog dialog = new ReturnedOnDialog(this, unit);

                if(unit.actualDateReturned != null){
                JOptionPane.showMessageDialog(null,
                        "  Be sure to thank " + unit.nameOfRenter +
                                "\n for renting with us. The price is:  " +
                                unit.getCost(unit.actualDateReturned) +
                                " dollars");
                dList.update(index, unit);
                }
            }
        }
    }

    public static void main(String[] args) {
        new GUIRentalStore();
    }
}
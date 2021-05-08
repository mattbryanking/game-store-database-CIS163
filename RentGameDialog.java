package project2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class RentGameDialog extends JDialog implements ActionListener {

    /** a universal version identifier for a Serializable class */
    private static final long serialVersionUID = 1L;
    
    private JTextField txtRentedName;
    private JTextField txtDateRentedOn;
    private JTextField txtDateDueDate;
    private JTextField txtNameOfGame;
    private JComboBox<ConsoleTypes> comBoxConsoleType;

    private JButton okButton;
    private JButton cancelButton;
    private int closeStatus;
    private Game game;
    public static final int OK = 0;
    public static final int CANCEL = 1;

    /*********************************************************
     Instantiate a Custom Dialog as 'modal' and wait for the
     user to provide data and click on a button.

     @param parent reference to the JFrame application
     @param game an instantiated object to be filled with data
     *********************************************************/

    public RentGameDialog(JFrame parent, Game game) {
        // call parent and create a 'modal' dialog
        super(parent, true);
        this.game = game;

        setTitle("Game dialog box");
        closeStatus = CANCEL;
        setSize(400,200);

        // prevent user from closing window
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        txtRentedName = new JTextField("Judy",30);
        txtDateRentedOn = new JTextField(15);
        txtDateDueDate = new JTextField(15);
        txtNameOfGame = new JTextField("Game1", 15);
        comBoxConsoleType = new JComboBox<>(ConsoleTypes.values());

        Calendar currentDate = Calendar.getInstance();
        SimpleDateFormat formatter= new SimpleDateFormat("MM/dd/yyyy"); //format it as per your requirement
        String dateNow = formatter.format(currentDate.getTime());
        currentDate.add(Calendar.DATE, 1);
        String dateTomorrow = formatter.format(currentDate.getTime());

        txtDateRentedOn.setText(dateNow);
        txtDateDueDate.setText(dateTomorrow);

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new GridLayout(6,2));

        textPanel.add(new JLabel(""));
        textPanel.add(new JLabel(""));

        textPanel.add(new JLabel("Name of Renter: "));
        textPanel.add(txtRentedName);
        textPanel.add(new JLabel("Date rented on: "));
        textPanel.add(txtDateRentedOn);
        textPanel.add(new JLabel("Due date (est.): "));
        textPanel.add(txtDateDueDate);
        textPanel.add(new JLabel("Name of the Gamed"));
        textPanel.add(txtNameOfGame);
        textPanel.add(new JLabel("ConsoleType"));
        textPanel.add(comBoxConsoleType);

        getContentPane().add(textPanel, BorderLayout.CENTER);

        okButton = new JButton("OK");
        cancelButton = new JButton("Cancel");
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        okButton.addActionListener(this);
        cancelButton.addActionListener(this);

        setVisible (true);
    }

    /**************************************************************
     Respond to either button clicks
     @param e the action event that was just fired
     **************************************************************/
    public void actionPerformed(ActionEvent e) {

        JButton button = (JButton) e.getSource();

        // if OK clicked the fill the object
        if (button == okButton) {
            // save the information in the object
            closeStatus = OK;
            SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");

            Date d1 = null;
            Date d2 = null;

            int pos = 0;

            int rentDay = 0;
            int rentMonth = 0;
            int rentYear = 0;

            int backDay = 0;
            int backMonth = 0;
            int backYear = 0;

            String checkDate = "";
            String checkBack = "";

            for(String retval : txtDateRentedOn.getText().split("/")){
                checkDate+= retval;
            }
            for(String retval : txtDateDueDate.getText().split("/")){
                checkBack+= retval;
            }
            
            boolean isNumeric1 = checkDate.chars().allMatch( Character::isDigit );
            boolean isNumeric2 = checkBack.chars().allMatch( Character::isDigit );

            if(!isNumeric1 || !isNumeric2) {
                JOptionPane.showMessageDialog(null,"Invalid date!");
                closeStatus = CANCEL;
            }

            if(isNumeric1 && isNumeric2){
                try {
                
                    pos = 0;
                    for( String retval: txtDateRentedOn.getText().split("/")){
                        if(pos == 0)
                            rentDay = Integer.parseInt(retval);
                        if(pos == 1)
                            rentMonth = Integer.parseInt(retval);
                        if(pos == 2)
                            rentYear = Integer.parseInt(retval);
                        pos++;
                    }
                    //System.out.println("dueDay: "+ rentDay + " dueMonth: " + rentMonth);
                    pos = 0;
                    for( String retval: txtDateDueDate.getText().split("/")){
                        if(pos == 0)
                            backDay = Integer.parseInt(retval);
                        if(pos == 1)
                            backMonth = Integer.parseInt(retval);
                        if(pos == 2)
                            backYear = Integer.parseInt(retval);
                        pos++;
                    }
                    //System.out.println("backDay: "+ backDay + " backMonth: " + backMonth);
    
                    if(backDay< rentDay || backMonth < rentMonth || backYear < rentYear) {
                        JOptionPane.showMessageDialog(null,"Invalid return date!");
                        closeStatus = CANCEL;
                    }
    
                    GregorianCalendar rentOnTemp = new GregorianCalendar();
                    d1 = df.parse(txtDateRentedOn.getText());
                    rentOnTemp.setTime(d1);
                    game.setRentedOn(rentOnTemp);
    
                    GregorianCalendar dueDateTemp = new GregorianCalendar();
                    d2 = df.parse(txtDateDueDate.getText());
                    dueDateTemp.setTime(d2);
                    game.setDueBack(dueDateTemp);
    
                    game.setNameOfRenter(txtRentedName.getText());
                    game.setNameGame(txtNameOfGame.getText());
    
                    ConsoleTypes type = ((ConsoleTypes) comBoxConsoleType.getSelectedItem());
    
                    game.setConsole(type);
    
                } catch (ParseException e1) {
    //                  Do some thing good, what that is, I am not sure.
                }
            }
            
        }

        // make the dialog disappear
        dispose();
    }

    /**************************************************************
     Return a String to let the caller know which button
     was clicked

     @return an int representing the option OK or CANCEL
     **************************************************************/
    public int getCloseStatus(){
        return closeStatus;
    }
}
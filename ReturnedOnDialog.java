package project2;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

public class ReturnedOnDialog extends JDialog implements ActionListener {

    /** a universal version identifier for a Serializable class */
    private static final long serialVersionUID = 1L;

	private JTextField txtDate;

	private JButton okButton;
	private JButton cancelButton;
	private int closeStatus;
	private Rental unit;

	static final int OK = 0;
	static final int CANCEL = 1;

	/*********************************************************
	 Instantiate a Custom Dialog as 'modal' and wait for the
	 user to provide data and click on a button.

	 @param parent reference to the JFrame application
	 @param unit an instantiated object to be filled with data
	 *********************************************************/
	public ReturnedOnDialog(JFrame parent, Rental unit) {
		// call parent and create a 'modal' dialog
		super(parent, true);

		this.unit = unit;
		setTitle("Returned dialog box");
		closeStatus = CANCEL;
		setSize(300,100);

		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");

        txtDate = new JTextField(dateFormat.format(unit.
                getDueBack().getTime()),30);

		JPanel textPanel = new JPanel();
		textPanel.setLayout(new GridLayout(1,2));
		textPanel.add(new JLabel("Returned Date: "));
		textPanel.add(txtDate);

		getContentPane().add(textPanel, BorderLayout.CENTER);

		// Instantiate and display two buttons
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

			if (unit.actualDateReturned != null) {
				JOptionPane.showMessageDialog(null,"Item has already been returned!");
				closeStatus = CANCEL;
			}
			
			// save the information in the object
			closeStatus = OK;
			SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
			GregorianCalendar gTemp = new GregorianCalendar();

			String checkDate = "";

            for(String retval : txtDate.getText().split("/")){
                checkDate+= retval;
            }
            boolean isNumeric = checkDate.chars().allMatch( Character::isDigit );

            if(!isNumeric) {
                JOptionPane.showMessageDialog(null,"Invalid date!");
                closeStatus = CANCEL;
            }

			Date d = null;
			if (isNumeric) {
			try {
				d = df.parse(txtDate.getText());
				gTemp.setTime(d);
				// System.out.println("Gtemp compared to: "+ gTemp.compareTo(unit.getRentedOn()));
				// System.out.println("Unit compared to " + unit.getRentedOn().compareTo(gTemp));

				if(unit.getRentedOn().compareTo(gTemp) == 1){
					//unit.setActualDateReturned(null);
					JOptionPane.showMessageDialog(null,"Invalid return date.");
                	closeStatus = CANCEL;
				}
				else
					unit.setActualDateReturned(gTemp);
				

				

			} catch (ParseException e1) {
			}

			}
		// make the dialog disappear
		dispose();
		}
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


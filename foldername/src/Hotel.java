import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.ArrayList;
import java.util.Calendar;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Hotel{
    private JFrame mainFrame;

    // Mock data for available rooms
    private String[] roomTypes = {"Standard", "Deluxe", "Suite"};
    private double[] roomPrices = {100.0, 150.0, 250.0};
    
    // Data structures to store bookings and history
    private Map<String, Booking> bookings = new HashMap<>();
    private List<Booking> bookingHistory = new ArrayList<>();

    public Hotel() {
        prepareGUI();
    }

    private void prepareGUI() {
        mainFrame = new JFrame("Smart Hotel Management System");
        mainFrame.setSize(500, 350);
        mainFrame.setLayout(new GridLayout(5, 1));
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JButton bookRoomBtn = new JButton("Book a Room");
        JButton checkInBtn = new JButton("Check-in Guest");
        JButton checkOutBtn = new JButton("Check-out Guest");
        JButton historyBtn = new JButton("Booking History");
        JButton exitBtn = new JButton("Exit");

        bookRoomBtn.addActionListener(e -> bookRoom());
        checkInBtn.addActionListener(e -> checkInGuest());
        checkOutBtn.addActionListener(e -> checkOutGuest());
        historyBtn.addActionListener(e -> showBookingHistory());
        exitBtn.addActionListener(e -> System.exit(0));

        mainFrame.add(bookRoomBtn);
        mainFrame.add(checkInBtn);
        mainFrame.add(checkOutBtn);
        mainFrame.add(historyBtn);
        mainFrame.add(exitBtn);

        mainFrame.setVisible(true);
    }

    private void bookRoom() {
        // Step 1: Show available room types with occupancy limitations
        JDialog roomSelectionDialog = new JDialog(mainFrame, "Select Room Type", true);
        roomSelectionDialog.setLayout(new GridLayout(roomTypes.length + 1, 1));

        for (int i = 0; i < roomTypes.length; i++) {
            String occupancyInfo = "";
            
            if (i == 0) { // Standard
                occupancyInfo = " (Max: 2 adults or 1 adult + 2 kids)";
            } else if (i == 1) { // Deluxe
                occupancyInfo = " (Max: 3 adults or 2 adults + 3 kids)";
            } else if (i == 2) { // Suite
                occupancyInfo = " (Max: 7 people - adults & kids)";
            }
            
            JButton roomButton = new JButton(roomTypes[i] + occupancyInfo + " - $" + roomPrices[i] + " per night");
            int finalI = i;
            roomButton.addActionListener(e -> {
                roomSelectionDialog.dispose();
                askForGuestDetails(roomTypes[finalI], roomPrices[finalI], finalI);
            });
            roomSelectionDialog.add(roomButton);
        }

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> roomSelectionDialog.dispose());
        roomSelectionDialog.add(cancelButton);

        roomSelectionDialog.setSize(500, 250);
        roomSelectionDialog.setVisible(true);
    }

    private void askForGuestDetails(String roomType, double roomPrice, int roomTypeIndex) {
        JDialog detailsDialog = new JDialog(mainFrame, "Guest Details", true);
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Guest info panel
        JPanel infoPanel = new JPanel(new GridLayout(9, 2, 5, 5));
        
        JTextField nameField = new JTextField();
        JTextField contactField = new JTextField();
        
        // Date selection through date pickers
        JPanel checkInPanel = new JPanel(new BorderLayout());
        JPanel checkOutPanel = new JPanel(new BorderLayout());
        
        // Get current date
        Calendar currentDate = Calendar.getInstance();
        
        // Create combo boxes for dates
        // Check-in date
        JComboBox<Integer> checkInDayBox = new JComboBox<>();
        JComboBox<Integer> checkInMonthBox = new JComboBox<>();
        JComboBox<Integer> checkInYearBox = new JComboBox<>();
        
        // Fill in day options (1-31)
        for (int i = 1; i <= 31; i++) {
            checkInDayBox.addItem(i);
        }
        
        // Fill in month options (1-12)
        for (int i = 1; i <= 12; i++) {
            checkInMonthBox.addItem(i);
        }
        
        // Fill in year options (current year and next two years)
        int currentYear = currentDate.get(Calendar.YEAR);
        for (int i = currentYear; i <= currentYear + 2; i++) {
            checkInYearBox.addItem(i);
        }
        
        // Set current date as default
        checkInDayBox.setSelectedItem(currentDate.get(Calendar.DAY_OF_MONTH));
        checkInMonthBox.setSelectedItem(currentDate.get(Calendar.MONTH) + 1); // January is 0
        checkInYearBox.setSelectedItem(currentYear);
        
        JPanel checkInDatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        checkInDatePanel.add(new JLabel("Day:"));
        checkInDatePanel.add(checkInDayBox);
        checkInDatePanel.add(new JLabel("Month:"));
        checkInDatePanel.add(checkInMonthBox);
        checkInDatePanel.add(new JLabel("Year:"));
        checkInDatePanel.add(checkInYearBox);
        
        checkInPanel.add(checkInDatePanel, BorderLayout.CENTER);
        
        // Check-out date
        JComboBox<Integer> checkOutDayBox = new JComboBox<>();
        JComboBox<Integer> checkOutMonthBox = new JComboBox<>();
        JComboBox<Integer> checkOutYearBox = new JComboBox<>();
        
        // Fill in day options (1-31)
        for (int i = 1; i <= 31; i++) {
            checkOutDayBox.addItem(i);
        }
        
        // Fill in month options (1-12)
        for (int i = 1; i <= 12; i++) {
            checkOutMonthBox.addItem(i);
        }
        
        // Fill in year options (current year and next two years)
        for (int i = currentYear; i <= currentYear + 2; i++) {
            checkOutYearBox.addItem(i);
        }
        
        // Set next day as default checkout
        Calendar nextDay = (Calendar) currentDate.clone();
        nextDay.add(Calendar.DAY_OF_MONTH, 1);
        
        checkOutDayBox.setSelectedItem(nextDay.get(Calendar.DAY_OF_MONTH));
        checkOutMonthBox.setSelectedItem(nextDay.get(Calendar.MONTH) + 1); // January is 0
        checkOutYearBox.setSelectedItem(nextDay.get(Calendar.YEAR));
        
        JPanel checkOutDatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        checkOutDatePanel.add(new JLabel("Day:"));
        checkOutDatePanel.add(checkOutDayBox);
        checkOutDatePanel.add(new JLabel("Month:"));
        checkOutDatePanel.add(checkOutMonthBox);
        checkOutDatePanel.add(new JLabel("Year:"));
        checkOutDatePanel.add(checkOutYearBox);
        
        checkOutPanel.add(checkOutDatePanel, BorderLayout.CENTER);
        
        // Occupancy fields based on room type
        JSpinner adultSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 7, 1));
        JSpinner childSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 5, 1));
        
        // Set max values based on room type
        if (roomTypeIndex == 0) { // Standard room
            adultSpinner.setModel(new SpinnerNumberModel(1, 1, 2, 1));
            childSpinner.setModel(new SpinnerNumberModel(0, 0, 2, 1));
        } else if (roomTypeIndex == 1) { // Deluxe room
            adultSpinner.setModel(new SpinnerNumberModel(1, 1, 3, 1));
            childSpinner.setModel(new SpinnerNumberModel(0, 0, 3, 1));
        } else { // Suite room
            adultSpinner.setModel(new SpinnerNumberModel(1, 1, 7, 1));
            childSpinner.setModel(new SpinnerNumberModel(0, 0, 6, 1));
        }
        
        JButton submitBtn = new JButton("Submit");
        
        infoPanel.add(new JLabel("Name:"));
        infoPanel.add(nameField);
        infoPanel.add(new JLabel("Contact:"));
        infoPanel.add(contactField);
        infoPanel.add(new JLabel("Check-in Date:"));
        infoPanel.add(checkInPanel);
        infoPanel.add(new JLabel("Check-out Date:"));
        infoPanel.add(checkOutPanel);
        infoPanel.add(new JLabel("Number of Adults:"));
        infoPanel.add(adultSpinner);
        infoPanel.add(new JLabel("Number of Children:"));
        infoPanel.add(childSpinner);
        infoPanel.add(new JLabel("Room Type:"));
        infoPanel.add(new JLabel(roomType));
        infoPanel.add(new JLabel("Price per Night:"));
        infoPanel.add(new JLabel("$" + roomPrice));
        infoPanel.add(new JLabel());
        infoPanel.add(submitBtn);
        
        mainPanel.add(infoPanel, BorderLayout.CENTER);
        detailsDialog.add(mainPanel);

        submitBtn.addActionListener(e -> {
            String name = nameField.getText();
            String contact = contactField.getText();
            
            int numAdults = (int) adultSpinner.getValue();
            int numChildren = (int) childSpinner.getValue();
            
            // Build the date strings
            int checkInDay = (int) checkInDayBox.getSelectedItem();
            int checkInMonth = (int) checkInMonthBox.getSelectedItem();
            int checkInYear = (int) checkInYearBox.getSelectedItem();
            
            int checkOutDay = (int) checkOutDayBox.getSelectedItem();
            int checkOutMonth = (int) checkOutMonthBox.getSelectedItem();
            int checkOutYear = (int) checkOutYearBox.getSelectedItem();
            
            String checkIn = String.format("%04d-%02d-%02d", checkInYear, checkInMonth, checkInDay);
            String checkOut = String.format("%04d-%02d-%02d", checkOutYear, checkOutMonth, checkOutDay);
            
            if (name.isEmpty() || contact.isEmpty()) {
                JOptionPane.showMessageDialog(detailsDialog, "Please fill all fields!");
                return;
            }
            
            // Validate occupancy based on room type
            boolean validOccupancy = true;
            String occupancyError = "";
            
            if (roomTypeIndex == 0) { // Standard room
                if (numAdults > 2 || (numAdults == 1 && numChildren > 2) || (numAdults == 2 && numChildren > 0)) {
                    validOccupancy = false;
                    occupancyError = "Standard room can only accommodate max 2 adults OR 1 adult with 1-2 kids.";
                }
            } else if (roomTypeIndex == 1) { // Deluxe room
                if (numAdults > 3 || (numAdults + numChildren > 5) || (numAdults == 1 && numChildren > 3)) {
                    validOccupancy = false;
                    occupancyError = "Deluxe room can only accommodate max 3 adults OR 1-2 adults with 1-3 kids.";
                }
            } else if (roomTypeIndex == 2) { // Suite room
                if (numAdults + numChildren > 7) {
                    validOccupancy = false;
                    occupancyError = "Suite room can only accommodate max 7 people total.";
                }
            }
            
            if (!validOccupancy) {
                JOptionPane.showMessageDialog(detailsDialog, occupancyError, "Invalid Occupancy", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Calculate number of days and total cost
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            try {
                // Validate dates
                Date checkInDate = sdf.parse(checkIn);
                Date checkOutDate = sdf.parse(checkOut);
                Date today = new Date(); // Current date
                
                // Reset time part for today to compare only dates
                Calendar cal = Calendar.getInstance();
                cal.setTime(today);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                today = cal.getTime();
                
                // Check if check-in date is before today
                if (checkInDate.before(today)) {
                    JOptionPane.showMessageDialog(detailsDialog, 
                        "Check-in date cannot be in the past!", "Invalid Date", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Check if check-out date is before or equal to check-in date
                if (checkOutDate.before(checkInDate) || checkOutDate.equals(checkInDate)) {
                    JOptionPane.showMessageDialog(detailsDialog, 
                        "Check-out date must be after check-in date!", "Invalid Date", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                long diffInMillies = Math.abs(checkOutDate.getTime() - checkInDate.getTime());
                long diffInDays = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
                
                if (diffInDays <= 0) {
                    JOptionPane.showMessageDialog(detailsDialog,
                        "Stay duration must be at least one day!",
                        "Invalid Duration", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                double totalCost = roomPrice * diffInDays;
                detailsDialog.dispose();
                processPayment(roomType, roomPrice, name, contact, checkIn, checkOut, diffInDays, totalCost, numAdults, numChildren);
            } catch (ParseException ex) {
                JOptionPane.showMessageDialog(detailsDialog, "Error processing dates. Please try again.");
            }
        });

        detailsDialog.setSize(500, 400);
        detailsDialog.setVisible(true);
    }

    private void processPayment(String roomType, double roomPrice, String name, String contact, 
                               String checkIn, String checkOut, long stayDuration, double totalCost,
                               int numAdults, int numChildren) {
        JDialog paymentDialog = new JDialog(mainFrame, "Payment", true);
        paymentDialog.setLayout(new GridLayout(10, 1));

        JLabel instructionLabel = new JLabel("Please make payment to:");
        JLabel bankLabel = new JLabel("MayBank, Account No. 1002321312, Holiday Suites Corp");
        JLabel durationLabel = new JLabel("Stay Duration: " + stayDuration + " nights");
        JLabel priceLabel = new JLabel("Price per night: $" + roomPrice);
        JLabel amountLabel = new JLabel("Total Amount: $" + totalCost);
        JLabel guestsLabel = new JLabel("Guests: " + numAdults + " adult(s), " + numChildren + " child(ren)");
        JTextField referenceField = new JTextField();
        JButton confirmBtn = new JButton("Confirm Payment");

        paymentDialog.add(instructionLabel);
        paymentDialog.add(bankLabel);
        paymentDialog.add(durationLabel);
        paymentDialog.add(priceLabel);
        paymentDialog.add(amountLabel);
        paymentDialog.add(guestsLabel);
        paymentDialog.add(new JLabel("Payment Reference:"));
        paymentDialog.add(referenceField);
        paymentDialog.add(new JLabel());
        paymentDialog.add(confirmBtn);

        confirmBtn.addActionListener(e -> {
            String reference = referenceField.getText();
            if (reference.isEmpty()) {
                JOptionPane.showMessageDialog(paymentDialog, "Please enter a payment reference!");
            } else {
                // Generate a unique booking ID
                String bookingId = generateBookingId();
                
                // Create a new booking object and store it
                Booking newBooking = new Booking(
                    bookingId, name, contact, roomType, roomPrice, 
                    checkIn, checkOut, stayDuration, totalCost, reference, 
                    numAdults, numChildren, false, false
                );
                insertBookingToDatabase(newBooking);
                bookings.put(bookingId, newBooking);
                bookingHistory.add(newBooking);
                
                String confirmation = String.format(
                    "Payment Confirmed!\n\nBooking ID: %s\nRoom Type: %s\nPrice: $%.2f per night\n" +
                    "Stay Duration: %d nights\nTotal Cost: $%.2f\n" +
                    "Guest: %s\nContact: %s\nGuests: %d adult(s), %d child(ren)\n" +
                    "Check-in: %s\nCheck-out: %s\nPayment Reference: %s",
                    bookingId, roomType, roomPrice, stayDuration, totalCost, 
                    name, contact, numAdults, numChildren, checkIn, checkOut, reference
                );
                JOptionPane.showMessageDialog(paymentDialog, confirmation);
                paymentDialog.dispose();
            }
        });

        paymentDialog.setSize(400, 350);
        paymentDialog.setVisible(true);
    }

    private String generateBookingId() {
        // Generate a random 6-digit booking ID
        Random random = new Random();
        int number = random.nextInt(900000) + 100000;
        return "BK" + number;
    }

    private void checkInGuest() {
        String bookingId = JOptionPane.showInputDialog(mainFrame, "Enter Booking ID:");
        if (bookingId != null && !bookingId.isEmpty()) {
            Booking booking = bookings.get(bookingId);
            if (booking != null && !booking.isCheckedIn()) {
                booking.setCheckedIn(true);
                
                JOptionPane.showMessageDialog(mainFrame, 
                    "Check-In Completed Successfully!\n\n" +
                    "Booking ID: " + bookingId + "\n" +
                    "Guest: " + booking.getName() + "\n" +
                    "Room Type: " + booking.getRoomType() + "\n" +
                    "Guests: " + booking.getNumAdults() + " adult(s), " + booking.getNumChildren() + " child(ren)\n" +
                    "Check-in Date: " + booking.getCheckInDate()
                );
            } else if (booking != null && booking.isCheckedIn()) {
                JOptionPane.showMessageDialog(mainFrame, "This guest has already checked in!");
            } else {
                JOptionPane.showMessageDialog(mainFrame, "Booking ID not found. Please enter a valid ID.");
            }
        }
    }

    private void checkOutGuest() {
        String bookingId = JOptionPane.showInputDialog(mainFrame, "Enter Booking ID:");
        if (bookingId != null && !bookingId.isEmpty()) {
            Booking booking = bookings.get(bookingId);
            if (booking != null && booking.isCheckedIn() && !booking.isCheckedOut()) {
                booking.setCheckedOut(true);
                
                JOptionPane.showMessageDialog(mainFrame, 
                    "Check-Out Completed Successfully!\n\n" +
                    "Booking ID: " + bookingId + "\n" +
                    "Guest: " + booking.getName() + "\n" +
                    "Room Type: " + booking.getRoomType() + "\n" +
                    "Guests: " + booking.getNumAdults() + " adult(s), " + booking.getNumChildren() + " child(ren)\n" +
                    "Stay Duration: " + booking.getStayDuration() + " nights\n" +
                    "Total Amount Paid: $" + booking.getTotalCost()
                );
            } else if (booking != null && !booking.isCheckedIn()) {
                JOptionPane.showMessageDialog(mainFrame, "This guest has not checked in yet!");
            } else if (booking != null && booking.isCheckedOut()) {
                JOptionPane.showMessageDialog(mainFrame, "This guest has already checked out!");
            } else {
                JOptionPane.showMessageDialog(mainFrame, "Booking ID not found. Please enter a valid ID.");
            }
        }
    }

    private void showBookingHistory() {
        JDialog historyDialog = new JDialog(mainFrame, "Booking History", true);
        historyDialog.setLayout(new BorderLayout());
        
        // Create column names
        String[] columnNames = {
            "Booking ID", "Guest Name", "Room Type", "Check-In", "Check-Out", 
            "Duration", "Guests", "Total Cost", "Status"
        };
        
        // Create data rows
        Object[][] data = new Object[bookingHistory.size()][columnNames.length];
        for (int i = 0; i < bookingHistory.size(); i++) {
            Booking booking = bookingHistory.get(i);
            
            String status;
            if (booking.isCheckedOut()) {
                status = "Checked Out";
            } else if (booking.isCheckedIn()) {
                status = "Checked In";
            } else {
                status = "Reserved";
            }
            
            data[i][0] = booking.getBookingId();
            data[i][1] = booking.getName();
            data[i][2] = booking.getRoomType();
            data[i][3] = booking.getCheckInDate();
            data[i][4] = booking.getCheckOutDate();
            data[i][5] = booking.getStayDuration() + " nights";
            data[i][6] = booking.getNumAdults() + " adults, " + booking.getNumChildren() + " kids";
            data[i][7] = "$" + booking.getTotalCost();
            data[i][8] = status;
        }
        
        // Create table
        JTable table = new JTable(data, columnNames);
        JScrollPane scrollPane = new JScrollPane(table);
        
        // Add a close button
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> historyDialog.dispose());
        
        historyDialog.add(scrollPane, BorderLayout.CENTER);
        historyDialog.add(closeButton, BorderLayout.SOUTH);
        
        historyDialog.setSize(800, 400);
        historyDialog.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Hotel(); // ← launches your GUI class
        });
    }

    // Booking class to store all booking information
    private static class Booking {
        private String bookingId;
        private String name;
        private String contact;
        private String roomType;
        private double roomPrice;
        private String checkInDate;
        private String checkOutDate;
        private long stayDuration;
        private double totalCost;
        private String paymentReference;
        private int numAdults;
        private int numChildren;
        private boolean checkedIn;
        private boolean checkedOut;
        
        public Booking(String bookingId, String name, String contact, String roomType, 
                      double roomPrice, String checkInDate, String checkOutDate, 
                      long stayDuration, double totalCost, String paymentReference,
                      int numAdults, int numChildren, boolean checkedIn, boolean checkedOut) {
            this.bookingId = bookingId;
            this.name = name;
            this.contact = contact;
            this.roomType = roomType;
            this.roomPrice = roomPrice;
            this.checkInDate = checkInDate;
            this.checkOutDate = checkOutDate;
            this.stayDuration = stayDuration;
            this.totalCost = totalCost;
            this.paymentReference = paymentReference;
            this.numAdults = numAdults;
            this.numChildren = numChildren;
            this.checkedIn = checkedIn;
            this.checkedOut = checkedOut;
        }
        
        // Getters
        public String getBookingId() { return bookingId; }
        public String getName() { return name; }
        public String getContact() { return contact; }
        public String getRoomType() { return roomType; }
        public double getRoomPrice() { return roomPrice; }
        public String getCheckInDate() { return checkInDate; }
        public String getCheckOutDate() { return checkOutDate; }
        public long getStayDuration() { return stayDuration; }
        public double getTotalCost() { return totalCost; }
        public String getPaymentReference() { return paymentReference; }
        public int getNumAdults() { return numAdults; }
        public int getNumChildren() { return numChildren; }
        public boolean isCheckedIn() { return checkedIn; }
        public boolean isCheckedOut() { return checkedOut; }
        
        // Setters
        public void setCheckedIn(boolean checkedIn) { this.checkedIn = checkedIn; }
        public void setCheckedOut(boolean checkedOut) { this.checkedOut = checkedOut; }
    }
    private void insertBookingToDatabase(Booking b) {
        String sql = "INSERT INTO bookings VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (java.sql.Connection conn = Database.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, b.getBookingId());
            stmt.setString(2, b.getName());
            stmt.setString(3, b.getContact());
            stmt.setString(4, b.getRoomType());
            stmt.setDouble(5, b.getRoomPrice());
            stmt.setDate(6, java.sql.Date.valueOf(b.getCheckInDate()));
            stmt.setDate(7, java.sql.Date.valueOf(b.getCheckOutDate()));
            stmt.setLong(8, b.getStayDuration());
            stmt.setDouble(9, b.getTotalCost());
            stmt.setString(10, b.getPaymentReference());
            stmt.setInt(11, b.getNumAdults());
            stmt.setInt(12, b.getNumChildren());
            stmt.setBoolean(13, b.isCheckedIn());
            stmt.setBoolean(14, b.isCheckedOut());

            stmt.executeUpdate();
            System.out.println("✅ Booking saved to MySQL!");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(mainFrame, "❌ Error saving booking to database.");
        }
    }

}

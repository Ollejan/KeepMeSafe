package se.mah.ad0025.keepmesafe;

/**
 * A data structure representing a telephone contact.
 */
public class Contact {
    //Id auto generated in database.
    private int ID;
    private String name, number;

    /**
     * To create a contact we require a name and a number
     *
     * @param name   the name of the contact
     * @param number the number of the contact
     */
    public Contact(String name, String number) {
        this.name = name;
        this.number = number;
    }

    /**
     * Method to fetch ID of a contact
     *
     * @return the ID of the contact
     */
    public int getID() {
        return ID;
    }

    /**
     * Method to get the name of a contact.
     *
     * @return name of the contact
     */
    public String getName() {
        return name;
    }

    /**
     * Method to get the number of a contact
     *
     * @return the number of the contact
     */
    public String getNumber() {
        return number;
    }

    /**
     * Method to set the ID of a contact
     *
     * @param ID the new ID of the contact
     */
    public void setID(int ID) {
        this.ID = ID;
    }

    /**
     * Method used when the name of the contact is being edited
     *
     * @param name the new name of the contact
     */
    public void setName(String name) {
        this.name = name;
    }
}

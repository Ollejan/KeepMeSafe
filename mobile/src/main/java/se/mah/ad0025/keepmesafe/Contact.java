package se.mah.ad0025.keepmesafe;

/**
 * A data structure representing a telephone contact.
 */
public class Contact {
    private int ID;
    private String name, number;

    public Contact(String name, String number) {
        this.name = name;
        this.number = number;
    }

    public int getID() {
        return ID;
    }

    public String getName() {
        return name;
    }

    public String getNumber() {
        return number;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public void setName(String name) {
        this.name = name;
    }
}

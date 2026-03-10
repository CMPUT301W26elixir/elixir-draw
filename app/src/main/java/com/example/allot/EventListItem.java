package com.example.allot;

public class EventListItem {
    public String title;
    public String street;
    public String date;
    public String price;
    public String daysLeft;

    public EventListItem(String title, String street, String date, String price, String daysLeft) {
        this.title = title;
        this.street = street;
        this.date = date;
        this.price = price;
        this.daysLeft = daysLeft;
    }
}

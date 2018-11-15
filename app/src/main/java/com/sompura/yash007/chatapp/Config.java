package com.sompura.yash007.chatapp;

public class Config {

    public static String webServiceRoot = "http://chatapp.cptrivedi.com/";

    //Users class web rest links
    public static String login = webServiceRoot + "users.php?login=true";
    public static String signUp = webServiceRoot + "users.php?signUp=true";
    public static String update = webServiceRoot + "users.php?updateUser=true";

    //Contact class web rest links
    public static String addContact = webServiceRoot + "contacts.php?addContact=true";
    public static String removeContact = webServiceRoot + "contacts.php?removeContact=true";
    public static String listContacts = webServiceRoot + "contacts.php?listContacts=true";
    public static String findContacts = webServiceRoot + "contacts.php?findContacts=true";

    //Chat class web rest links
    public static String chatList = webServiceRoot + "chat.php?chatList=true";
    public static String sendChat = webServiceRoot + "chat.php?sendChat=true";
    public static String receiveChat = webServiceRoot + "chat.php?receiveChat=true";
    public static String findTotalMessages = webServiceRoot + "chat.php?findTotalMessages=true";
    public static String loadChat = webServiceRoot + "chat.php?loadChat=true";

    //Shared Preference Name
    public static String prefName = "ChatAppData";

}

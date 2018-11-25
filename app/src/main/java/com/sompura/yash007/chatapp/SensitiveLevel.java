package com.sompura.yash007.chatapp;

import android.util.Log;

import java.util.Arrays;

public class SensitiveLevel {
    public static String message;
    public static String[] messageSplit;
    public static String[] listKeywords;
    public static boolean flag;

    public SensitiveLevel(String message)  {
        this.message = message;
        listKeywords = new String[]{"bank", "password", "account details", "account number",
                                    "a/c number", "a/c no", "username", "swift bank code",
                                    "correspondent bank", "usa account", "holder address",
                                    "information account", "fund transfers", "bank charges",
                                    "bank details", "banking information", "pin", "pin number",
                                    "access code", "atm", "atm card", "atm number","cvc2 number",
                                    "cvv", "cvc2", "sin", "sin number", "card number", "card details",
                                    "id card", "id number","acc", "id", "pass", "account","a/c"};
    }

    public int findSensitiveLevel() {
        messageSplit = message.split(" ");
        for (String tempString: messageSplit)  {
            if(flag == false)   {
                flag = Arrays.asList(listKeywords).contains(tempString);
            }
        }
        if(flag == false)   {
            return 0;
        }
        else    {
            return 1;
        }
    }
}

package cz.ucenislovicek.BakalariAPI.Adapter;

import android.content.Context;

import cz.ucenislovicek.SharedPrefs;


public class Item {
    private String itemName;
    private String subjectName;
    private final boolean isInBag;
    private final Context context;

    public Item(String itemName, String subjectName, boolean isInBag, Context context) {
        this.subjectName = subjectName;
        this.itemName = itemName;
        this.isInBag = isInBag;
        this.context = context;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public String getItemName() {
        boolean language = SharedPrefs.getBoolean(context, "Language");
        if (itemName.equals("items") && language) {
            itemName = "pomůcky";
        } else if (itemName.equals("pomůcky") && !language){
            itemName = "items";
        }
        return itemName;
    }

    public String getNameInitialsOfSubject() {
        StringBuilder initials = new StringBuilder();
            initials.append(Character.toUpperCase(getSubjectName().charAt(0)));
        for (int i = 1; i < getSubjectName().length() - 1; i++) {
            if (getSubjectName().charAt(i) == ' ') {
                initials.append(Character.toUpperCase(getSubjectName().charAt(i + 1)));
            }
            if (initials.length() == 2) {
                return initials.toString();
            }

        }
        // specialSchoolCases
        switch (getSubjectName()){

            case "Biologie":
                return "Bi";

            case "Fyzika":
                return "Fy";

            case "Algoritmy":
                return "Alg";

            case "Programování":
                return "Pg";

            case "Chemie":
                return "Ch";

            case "Konverzace ve francouzském jazyce":
                return "FJK";
            case "Konverzace ve španělském jazyce":
                return "ŠJK";
            case "Konverzace ve německém jazyce":
                return "NJK";
            case "Konverzace ve ruském jazyce":
                return "RJK";
            case "Konverzace ve italském jazyce":
                return "IJK";

            case "Základy společenských věd":
                return "ZSV";
        }


        return initials.toString();
    }

    public boolean isInBag() {
        return isInBag;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }
}

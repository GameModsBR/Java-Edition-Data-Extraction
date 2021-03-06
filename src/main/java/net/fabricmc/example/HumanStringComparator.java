package net.fabricmc.example;

import java.util.Comparator;

public class HumanStringComparator implements Comparator<String> {
    public int compare(String o1, String o2) {

        String o1StringPart = o1.replaceAll("\\d", "");
        String o2StringPart = o2.replaceAll("\\d", "");


        if (o1StringPart.equalsIgnoreCase(o2StringPart)) {
            return extractInt(o1) - extractInt(o2);
        }
        return o1.compareTo(o2);
    }

    int extractInt(String s) {
        String num = s.replaceAll("\\D", "");
        // return 0 if no digits found
        return num.isEmpty() ? 0 : Integer.parseInt(num);
    }
}

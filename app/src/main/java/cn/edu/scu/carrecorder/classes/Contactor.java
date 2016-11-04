package cn.edu.scu.carrecorder.classes;

import java.io.Serializable;

/**
 * Created by MrVen on 16/10/23.
 */

public class Contactor implements Serializable{
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getName() {
        return name;
    }

    String phoneNumber;
    String name;

    public Contactor(String name, String phoneNumber) {
        this.name = name;
        this.phoneNumber = phoneNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (name.equals(((Contactor)o).getName()) && phoneNumber.equals(((Contactor)o).getPhoneNumber())) {
            return true;
        }
        return false;
    }
}

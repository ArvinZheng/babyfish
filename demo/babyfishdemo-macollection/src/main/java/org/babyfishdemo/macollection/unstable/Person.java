package org.babyfishdemo.macollection.unstable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.babyfish.collection.FrozenContext;
import org.babyfish.lang.Nulls;

/**
 * @author Tao Chen
 */
public class Person {

    private String firstName;
    
    FrozenContext<Person> firstNameFrozenContext;
    
    private String lastName;
    
    FrozenContext<Person> lastNameFrozenContext;
    
    private Gender gender;
    
    private Date birthday;
    
    public Person(String firstName, String lastName, Gender gender, Date birthday) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.birthday = birthday;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public void setFirstName(String firstName) {
        if (Nulls.equals(this.firstName, firstName)) {
            return;
        }
        FrozenContext<Person> ctx = this.firstNameFrozenContext;
        FrozenContext.suspendFreezing(ctx, this);
        try {
            this.firstName = firstName;
        } finally {
            FrozenContext.resumeFreezing(ctx);
        }
    }

    public String getLastName() {
        return this.lastName;
    }

    public void setLastName(String lastName) {
        if (Nulls.equals(this.lastName, lastName)) {
            return;
        }
        FrozenContext<Person> ctx = this.lastNameFrozenContext;
        FrozenContext.suspendFreezing(ctx, this);
        try {
            this.lastName = lastName;
        } finally {
            FrozenContext.resumeFreezing(ctx);
        }
    }

    public Gender getGender() {
        return this.gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public Date getBirthday() {
        return this.birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    @Override
    public String toString() {
        return "{ firstName: " + this.firstName +
                ", lastName: " + this.lastName +
                ", gender: " + this.gender +
                ", birthday: " + new SimpleDateFormat("Gyyyy-MM-dd", Locale.ENGLISH).format(this.birthday) +
                " }";
    }
}

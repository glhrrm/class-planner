package br.edu.ifrs.classplanner.model;

import com.google.firebase.database.Exclude;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Class implements Serializable {

    @Exclude
    private String id;
    private int number;
    private String date;
    private String groupId;
    private boolean classPlanned;
    private boolean materialSent;
    private boolean attendanceTaken;

    public Class(int number, String date, String groupId) {
        this.number = number;
        this.date = date;
        this.groupId = groupId;
    }

    @Exclude
    public String getId() {
        return id;
    }

    @Exclude
    public void setId(String id) {
        this.id = id;
    }
}

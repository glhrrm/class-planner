package br.edu.ifrs.classplanner.model;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Class implements Serializable {

    private String id;
    private int number;
    private String date;
    private String groupId;
    private boolean classPlanned;
    private boolean materialSent;
    private boolean attendanceTaken;
    private List<Resource> resources;

    public Class(int number, String date) {
        this.number = number;
        this.date = date;
    }
}

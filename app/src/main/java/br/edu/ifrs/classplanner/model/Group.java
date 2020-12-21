package br.edu.ifrs.classplanner.model;

import com.google.firebase.database.Exclude;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Group implements Serializable {

    @Exclude
    private String id;
    private String name;
    private String time;
    private String startDate;
    private int classCount;

    public Group(String name, String time, String startDate, int classCount) {
        this.name = name;
        this.time = time;
        this.startDate = startDate;
        this.classCount = classCount;
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

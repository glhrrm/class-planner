package br.edu.ifrs.classplanner.model;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Group implements Serializable {

    private String id;
    private String name;
    private String time;
    private String startDate;
    private int classCount;
    private List<Class> classes;

    public Group(String name, String time, String startDate, int classCount) {
        this.name = name;
        this.time = time;
        this.startDate = startDate;
        this.classCount = classCount;
    }
}

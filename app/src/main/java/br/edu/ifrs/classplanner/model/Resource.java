package br.edu.ifrs.classplanner.model;

import com.google.firebase.database.Exclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Resource {

    @Exclude
    private String id;
    private String name;
    private String url;
    private String classId;

    public Resource(String name, String url, String classId) {
        this.name = name;
        this.url = url;
        this.classId = classId;
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

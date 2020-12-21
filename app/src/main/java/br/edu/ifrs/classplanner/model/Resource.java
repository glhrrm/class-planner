package br.edu.ifrs.classplanner.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Resource {

    private String id;
    private String name;
    private String url;
    private String classId;

    public Resource(String name, String url) {
        this.name = name;
        this.url = url;
    }

//    TODO: REVER SE É NECESSÁRIO MANTER CLASS_ID E CONSTRUTORES

    public Resource(String name, String url, String classId) {
        this.name = name;
        this.url = url;
        this.classId = classId;
    }
}

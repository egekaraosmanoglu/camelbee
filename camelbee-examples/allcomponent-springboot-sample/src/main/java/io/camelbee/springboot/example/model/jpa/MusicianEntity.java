package io.camelbee.springboot.example.model.jpa;


import jakarta.persistence.*;

/**
 * Domain Musician JPA Entity for polling database.
 */
@Entity
@Table(name = "camelbee_musicians_table")
@NamedQuery(name = "getMusicians",
        query = "select musician from MusicianEntity musician")

public class MusicianEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String name;

    public MusicianEntity() {

    }

    /**
     * The constructor.
     *
     * @param name The name of the Musician
     */
    public MusicianEntity(String name) {
        super();
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "MusicianEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}

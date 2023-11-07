package io.camelbee.springboot.example.model.jpa;


import jakarta.persistence.*;

/**
 * Domain Song JPA Entity.
 */
@Entity
@Table(name = "camelbee_songs_table")
@NamedQuery(name = "getSongs",
        query = "select song from SongEntity song")

public class SongEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String name;
    private String composer;

    public SongEntity() {

    }

    /**
     * The constructor.
     *
     * @param name     The name of the Order
     * @param composer The composer of the Order
     */
    public SongEntity(String name, String composer) {
        super();
        this.name = name;
        this.composer = composer;
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

    public String getComposer() {
        return composer;
    }

    public void setComposer(String composer) {
        this.composer = composer;
    }

    @Override
    public String toString() {
        return "SongEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", composer='" + composer + '\'' +
                '}';
    }
}

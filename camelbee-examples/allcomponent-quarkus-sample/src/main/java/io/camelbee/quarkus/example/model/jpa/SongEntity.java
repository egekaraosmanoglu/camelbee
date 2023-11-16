/*
 * Copyright 2023 Rahmi Ege Karaosmanoglu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.camelbee.quarkus.example.model.jpa;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.persistence.*;

/**
 * Domain Song JPA Entity.
 */
@Entity
@Table(name = "camelbee_songs_table")
@NamedQuery(name = "getSongs", query = "select song from SongEntity song")
@RegisterForReflection
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

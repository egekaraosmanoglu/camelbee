/*
 * Copyright 2023 Rahmi Ege Karaosmanoglu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.camelbee.springboot.example.model.jpa;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

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
    return "MusicianEntity{"
        + "id=" + id
        + ", name='" + name + '\''
        + '}';
  }
}

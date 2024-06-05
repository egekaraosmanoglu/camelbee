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

package io.camelbee.quarkus.example.model.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Musician.
 */

public class Musician {

  private String id;

  private String name;

  @Valid
  private List<@Valid Song> songs;

  /**
   * Constructor with only required parameters.
   */
  public Musician(String id, String name) {
    this.id = id;
    this.name = name;
  }

  public Musician id(String id) {
    this.id = id;
    return this;
  }

  /**
   * Default constructor.
   */
  public Musician() {
    super();
  }

  /**
   * Get id.
   *
   * @return id
   */
  @NotNull
  @JsonProperty("id")
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Musician name(String name) {
    this.name = name;
    return this;
  }

  /**
   * Get name.
   *
   * @return name
   */

  @JsonProperty("name")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Musician songs(List<@Valid Song> songs) {
    this.songs = songs;
    return this;
  }

  /**
   * Add Song.
   */
  public Musician addSongsItem(Song songsItem) {
    if (this.songs == null) {
      this.songs = new ArrayList<>();
    }
    this.songs.add(songsItem);
    return this;
  }

  /**
   * Get songs.
   *
   * @return songs
   */
  @Valid
  @JsonProperty("songs")
  public List<@Valid Song> getSongs() {
    return songs;
  }

  public void setSongs(List<@Valid Song> songs) {
    this.songs = songs;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Musician musician = (Musician) o;
    return Objects.equals(this.id, musician.id)
        && Objects.equals(this.name, musician.name)
        && Objects.equals(this.songs, musician.songs);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, songs);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Musician {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    songs: ").append(toIndentedString(songs)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

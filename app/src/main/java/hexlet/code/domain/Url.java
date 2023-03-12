package hexlet.code.domain;

import java.time.Instant;
import javax.persistence.Entity;
import javax.persistence.Id;

import io.ebean.annotation.WhenCreated;
import io.ebean.Model;

@Entity
public final class Url extends Model {
    @Id
    private long id;

    private String name;

    @WhenCreated
    private Instant createdAt;

    public Url(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

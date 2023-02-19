package hexlet.code.domain;

import java.time.Instant;
import javax.persistence.Entity;
import javax.persistence.Id;
import io.ebean.annotation.WhenCreated;
import io.ebean.Model;

@Entity
public final class Url extends Model {
    @Id
    private final long id;

    private String name;

    @WhenCreated
    private final Instant created_at;

    public Url(long id, String name, Instant created_at) {
        this.id = id;
        this.name = name;
        this.created_at = created_at;
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

    public Instant getCreated_at() {
        return created_at;
    }
}

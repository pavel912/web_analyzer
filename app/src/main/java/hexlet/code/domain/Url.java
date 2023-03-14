package hexlet.code.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import io.ebean.annotation.WhenCreated;
import io.ebean.Model;

@Entity
public final class Url extends Model {
    @Id
    private long id;

    private String name;

    @WhenCreated
    private Instant createdAt;

    @OneToMany(mappedBy = "url", cascade = CascadeType.ALL)
    private List<UrlCheck> urlChecks = new ArrayList<>();

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

    public List<UrlCheck> getUrlChecks() {
        return urlChecks;
    }

    public void addUrlCheck(UrlCheck check) {
        urlChecks.add(check);
    }

    public UrlCheck getLastCheck() {
        return getUrlChecks().stream().max(new Comparator<UrlCheck>() {
            @Override
            public int compare(UrlCheck o1, UrlCheck o2) {
                if (o1.getCreatedAt().isAfter(o2.getCreatedAt())) {
                    return 1;
                }

                if (o1.getCreatedAt().isBefore(o2.getCreatedAt())) {
                    return -1;
                }

                return 0;
            }
        }).orElse(null);
    }
}

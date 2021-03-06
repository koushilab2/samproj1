package samproject1.model;

import com.bardframework.bard.core.marker.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Model
@Entity
@Cacheable
@JsonInclude(JsonInclude.Include.NON_NULL)
public class User {
    @Id @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid2")
    @JsonPropertyDescription("The id of user, generated by uuid")
    public String id;

    @Column(nullable = false, unique = true)
    @JsonPropertyDescription("The name of user, must be unique")
    public String username;

    @Column(nullable = false)
    @JsonIgnore
    public String password;

    @Column(nullable = false)
    @JsonIgnore
    public String salt;

    @Column
    @JsonPropertyDescription("The email of user")
    public String email;

    public User() {

    }

    public User(String id) {
        this.id = id;
    }
}

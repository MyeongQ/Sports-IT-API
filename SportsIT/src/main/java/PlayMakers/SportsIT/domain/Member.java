package PlayMakers.SportsIT.domain;

import PlayMakers.SportsIT.enums.Subscribe;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@AllArgsConstructor @NoArgsConstructor
@Builder
@Getter @Setter
@Entity (name="member")
@JsonIgnoreProperties({"hibernateLazyInitializer", "pw"})
public class Member extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long uid;

    @Column(nullable = false)
    private String pw;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String phone;

    @Column
    private String birth;

    @Column(nullable = false)
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private Subscribe subscription = Subscribe.FREE;

    @Builder.Default
    @Column(nullable = false)
    private boolean activated = true;

    @ManyToMany
    @JoinTable(
            name = "member_authority",
            joinColumns = {@JoinColumn(name = "member_uid", referencedColumnName = "uid")},
            inverseJoinColumns = {@JoinColumn(name = "member_type", referencedColumnName = "member_type")})
    private Set<MemberType> memberType;

}

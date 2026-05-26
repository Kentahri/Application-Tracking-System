package ats.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "candidates")
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
public class Candidate extends BaseEntity{

    @Column(name = "full_name", columnDefinition = "NVARCHAR(255)")
    private String name;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String email;

    @Column(name = "password_hash", columnDefinition = "NVARCHAR(255)")
    private String passwordHash;

    @Column(name = "auth_provider", columnDefinition = "NVARCHAR(50)")
    private String authProvider;

    @Column(name = "oauth_provider_id", columnDefinition = "NVARCHAR(255)")
    private String oauthProviderId;

    @Column(columnDefinition = "NVARCHAR(50)")
    private String status;

    @Column(columnDefinition = "NVARCHAR(30)")
    private String phone;

    @Column(columnDefinition = "NVARCHAR(150)")
    private String source;

    @Column(name = "utm_source", columnDefinition = "NVARCHAR(150)")
    private String utmSource;

    @Column(name = "utm_medium", columnDefinition = "NVARCHAR(150)")
    private String utmMedium;

    @Column(name = "utm_campaign", columnDefinition = "NVARCHAR(255)")
    private String utmCampaign;

    @Column(name = "is-duplicate")
    private Boolean isDuplicate;
}

package ats.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class User extends BaseEntity{

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department departmentId;

    @Column(name = "full_name", columnDefinition = "NVARCHAR(255)")
    private String name;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String email;

    @Column(name = "password_hash", columnDefinition = "NVARCHAR(255)")
    private String passwordHash;

    @Column(columnDefinition = "NVARCHAR(30)")
    private String phone;

    @Column(columnDefinition = "NVARCHAR(50)")
    private String role;

    @Column(columnDefinition = "NVARCHAR(50)")
    private String status;

}

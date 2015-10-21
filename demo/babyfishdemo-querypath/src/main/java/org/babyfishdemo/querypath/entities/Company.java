package org.babyfishdemo.querypath.entities;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.babyfish.persistence.instrument.JPAObjectModelInstrument;

/**
 * @author Tao Chen
 */
@JPAObjectModelInstrument
@Entity
@Table(name = "COMPANY")
@SequenceGenerator(
        name = "companySequence",
        sequenceName = "COMPANY_ID_SEQ",
        initialValue = 1,
        allocationSize = 1
)
public class Company {

    @Id
    @Column(name = "COMPANY_ID", nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "companySequence")
    private Long id;
    
    @Column(name = "NAME", length = 50, nullable = false)
    private String name;
    
    @OneToMany(mappedBy = "company")
    private Set<Department> departments;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Department> getDepartments() {
        return departments;
    }

    public void setDepartments(Set<Department> departments) {
        this.departments = departments;
    }
}

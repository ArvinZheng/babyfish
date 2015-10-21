package org.babyfishdemo.spring.model;

import java.util.Collection;
import java.util.Date;

import org.babyfishdemo.spring.entities.Employee.Gender;

/**
 * @author Tao Chen
 */
public class EmployeeSpecification {

    private String likeFirstName;
    
    private String likeLastName;
    
    private Gender gender;
    
    private Date minBirthday;
    
    private Date maxBirthday;
    
    private String likeDepartmentName;
    
    private Collection<String> includedDepartmentNames;
    
    private Collection<String> excludedDepartmentNames;
    
    private Boolean hasPendingAnnualLeaves;

    public String getLikeFirstName() {
        return likeFirstName;
    }

    public void setLikeFirstName(String likeFirstName) {
        this.likeFirstName = likeFirstName;
    }

    public String getLikeLastName() {
        return likeLastName;
    }

    public void setLikeLastName(String likeLastName) {
        this.likeLastName = likeLastName;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public Date getMinBirthday() {
        return minBirthday;
    }

    public void setMinBirthday(Date minBirthday) {
        this.minBirthday = minBirthday;
    }

    public Date getMaxBirthday() {
        return maxBirthday;
    }

    public void setMaxBirthday(Date maxBirthday) {
        this.maxBirthday = maxBirthday;
    }

    public String getLikeDepartmentName() {
        return likeDepartmentName;
    }

    public void setLikeDepartmentName(String likeDepartmentName) {
        this.likeDepartmentName = likeDepartmentName;
    }

    public Collection<String> getIncludedDepartmentNames() {
        return includedDepartmentNames;
    }

    public void setIncludedDepartmentNames(
            Collection<String> includedDepartmentNames) {
        this.includedDepartmentNames = includedDepartmentNames;
    }

    public Collection<String> getExcludedDepartmentNames() {
        return excludedDepartmentNames;
    }

    public void setExcludedDepartmentNames(
            Collection<String> excludedDepartmentNames) {
        this.excludedDepartmentNames = excludedDepartmentNames;
    }

    public Boolean getHasPendingAnnualLeaves() {
        return hasPendingAnnualLeaves;
    }

    public void setHasPendingAnnualLeaves(Boolean hasPendingAnnualLeaves) {
        this.hasPendingAnnualLeaves = hasPendingAnnualLeaves;
    }
}

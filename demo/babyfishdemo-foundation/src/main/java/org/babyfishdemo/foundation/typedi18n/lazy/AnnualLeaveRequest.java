package org.babyfishdemo.foundation.typedi18n.lazy;

import java.util.Date;

import org.babyfish.lang.Arguments;
import org.babyfish.util.LazyResource;

/*
 * Before learn this class, please learn ComplexDemo at first.
 */
/**
 * @author Tao Chen
 */
public class AnnualLeaveRequest {
    
    /*
     * Be different with "ComplexDemo" which use Resource.of(...) to load the resource immediately, 
     * in this class, LazyResource do NOT load and validate the resource immediately, until it is required
     * at the first time.
     * 
     * For product mode, especially for exception messages, this laziness behavior is very cool, 
     * but for unit test, it means lazy typed-i18n can not report all the errors when test is started 
     * if test coverage != 100%
     * 
     * typed-i18n support another functionality "Auto Testing", it ignore the laziness of LazyResource
     * so that the resources will be loaded and validated as soon as possible. 
     * But, unfortunately, I don't use it in this demo because I want to let you see the correct 
     * behavior of typed-i18n when you use break point to debug this demo step by step. 
     * 
     * So, please learn another project "babyfishdemo-foundation-wrong" to know how to use 
     * "Auto Testing" to report all the erros of lazy type-i18n when the tested class is loaded.
     */
    private static final LazyResource<Resource> LAZY_RESOURCE = LazyResource.of(Resource.class);
    
    /*
     * CREATED---------+-------------->SUBMITTED---+--------------->APPROVED
     *  |  /|\         |   submit()                |   approve()
     *  |   |          |                           | 
     *  |   \------\   |                           \-------------->REJECTED
     *  | modify() |   |   cancel()                    reject()
     *  \----------/   \-------------->CANCELLED
     */
    private AnnualLeaveRequestState state;
    
    private String name;

    private Date startTime;
    
    private Date endTime;
    
    public AnnualLeaveRequest(String name, Date startTime, Date endTime) {
        this.name = Arguments.mustNotBeEmpty(
                "name", 
                Arguments.mustNotBeNull("name", name)
        );
        this.state = AnnualLeaveRequestState.CREATED;
        this.modify(startTime, endTime);
    }

    public AnnualLeaveRequestState getState() {
        return this.state;
    }

    public String getName() {
        return this.name;
    }

    public Date getStartTime() {
        return startTime;
    }

    public Date getEndTime() {
        return endTime;
    }
    
    public void modify(Date startTime, Date endTime) {
        if (this.state != AnnualLeaveRequestState.CREATED) {
            throw new IllegalStateException(
                    LAZY_RESOURCE.get().requestCanNotBeModified(this.state)
            );
        }
        Arguments.mustNotBeNull("startTime", startTime);
        Arguments.mustNotBeNull("endTime", endTime);
        Arguments.mustBeGreaterThanOther("endTime", endTime, "startTime", startTime);
        Arguments.mustBeLessThanOrEqualToValue(
                "Time annual leave hours(endTime - startTime)", 
                (endTime.getTime() - startTime.getTime()) / (60 * 60 * 1000), 
                160
        );
        this.startTime = startTime;
        this.endTime = endTime;
    }
    
    public void submit() {
        if (this.state != AnnualLeaveRequestState.CREATED) {
            throw new IllegalStateException(
                    LAZY_RESOURCE.get().requestCanNotBeSubmitted(this.state)
            );
        }
        this.state = AnnualLeaveRequestState.SUBMITTED;
    }
    
    public void cancel() {
        if (this.state != AnnualLeaveRequestState.CREATED) {
            throw new IllegalStateException(
                    LAZY_RESOURCE.get().requestCanNotBeCancelled(this.state)
            );
        }
        this.state = AnnualLeaveRequestState.CANCELLED;
    }
    
    public void approve() {
        if (this.state != AnnualLeaveRequestState.SUBMITTED) {
            throw new IllegalStateException(
                    LAZY_RESOURCE.get().requestCanNotBeApproved(this.state)
            );
        }
        this.state = AnnualLeaveRequestState.APPROVED;
    }
    
    public void reject() {
        if (this.state != AnnualLeaveRequestState.SUBMITTED) {
            throw new IllegalStateException(
                    LAZY_RESOURCE.get().requestCanNotBeRejected(this.state)
            );
        }
        this.state = AnnualLeaveRequestState.REJECTED;
    }
    
    private interface Resource {
        
        String requestCanNotBeModified(AnnualLeaveRequestState currentState);
        
        String requestCanNotBeSubmitted(AnnualLeaveRequestState currentState);
        
        String requestCanNotBeCancelled(AnnualLeaveRequestState currentState);
        
        String requestCanNotBeApproved(AnnualLeaveRequestState currentState);
        
        String requestCanNotBeRejected(AnnualLeaveRequestState currentState);
    }
}

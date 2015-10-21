package org.babyfishdemo.foundation.typedi18n.lazy;

/*
 * CREATED---------+-------------->SUBMITTED---+--------------->APPROVED
 *  |  /|\         |   submit()                |   approve()
 *  |   |          |                           | 
 *  |   \------\   |                           \-------------->REJECTED
 *  | modify() |   |   cancel()                    reject()
 *  \----------/   \-------------->CANCELLED
 */
/**
 * @author Tao Chen
 */
public enum AnnualLeaveRequestState {

    CREATED,
    SUBMITTED,
    CANCELLED,
    APPROVED,
    REJECTED
}

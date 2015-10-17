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
 * @author Tao Chen(&#38472;&#28059;)
 */
public enum AnnualLeaveRequestState {

    CREATED,
    SUBMITTED,
    CANCELLED,
    APPROVED,
    REJECTED
}

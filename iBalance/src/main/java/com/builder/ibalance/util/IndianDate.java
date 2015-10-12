package com.builder.ibalance.util;

import java.util.Date;

/**
 * Created by Shabaz on 09-Oct-15.
 */
public class IndianDate extends Date
{
    /**
     * Initializes this {@code Date} instance using the specified millisecond value. The
     * value is the number of milliseconds since Jan. 1, 1970 GMT.
     *
     * @param milliseconds the number of milliseconds since Jan. 1, 1970 GMT.
     */
    public IndianDate(long milliseconds)
    {
        super(milliseconds);
    }

    /**
     * Initializes this {@code Date} instance to the current time.
     */
    public IndianDate()
    {
        super();
    }

    /**
     * Returns this {@code Date} as a millisecond value. The value is the number of
     * milliseconds since Jan. 1, 1970, midnight GMT.
     *
     * @return the number of milliseconds since Jan. 1, 1970, midnight GMT.
     */
    @Override
    public long getTime()
    {
        //+5:30 offset
        return super.getTime()+19800;
    }
}

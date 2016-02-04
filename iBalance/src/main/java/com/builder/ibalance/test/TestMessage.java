package com.builder.ibalance.test;

/**
 * Created by Shabaz on 03-Feb-16.
 */
public class TestMessage
{
    String text;

    public String getText()
    {
        return text;
    }

    public void setText(String text)
    {
        this.text = text;
    }

    public TestMessage(String text)
    {

        this.text = text;
    }
}

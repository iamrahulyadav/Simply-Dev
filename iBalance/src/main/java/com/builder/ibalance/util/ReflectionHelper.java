package com.builder.ibalance.util;

import android.content.Context;
import android.util.Log;

import java.lang.reflect.Method;

/**
 * Created by Shabaz on 07-Sep-15.
 */
public class ReflectionHelper
{
    final static String tag = ReflectionHelper.class.getSimpleName();
    public static Object getObject(Object receiver_object, String class_name, String method_name, Object parameters[])
    {
        //Receiver object can be null fot static method


        try
        {

            Method method = getMethod(receiver_object,class_name,method_name,parameters);//if exists return true
            if(method==null)
                return null;
            method.setAccessible(true);
            Object telephony_object = method.invoke(receiver_object, parameters);
            return telephony_object;
        } catch (Exception e)
        {
            //There was some problem return null
            Log.d(tag,"getObject Failed");
            e.printStackTrace();
            return null;
        }
    }

    public static Method getMethod(Object receiver_object, String class_name, String method_name, Object parameters[])
    {
        //Receiver object can be null fot static method


        try
        {
            Class<?> telephonyClass = null;
            if (class_name == null && receiver_object!=null)
                telephonyClass = receiver_object.getClass();
            else if(class_name !=null)
                telephonyClass = Class.forName(class_name);//even if class doesn't exist then return false
            else
            return null;
            Class<?>[] parameter_class = new Class[parameters.length];

            for (int i = 0; i < parameters.length; i++)
            {
                if(parameters[i] instanceof Integer)
                    parameter_class[i] = Integer.TYPE;
                else if(parameters[i] instanceof Long)
                    parameter_class[i] = Long.TYPE;
                else if(parameters[i] instanceof Context)
                    parameter_class[i] = Context.class;
                else if(parameters[i] instanceof String)
                    parameter_class[i] = String.class;
                else if(parameters[i] instanceof Float)
                    parameter_class[i] = Float.TYPE;
                else if (parameters[i] instanceof Class)
                    parameter_class[i] = (Class) parameters[i];
                else
                parameter_class[i] = parameters.getClass();
            }

            Method method = telephonyClass.getMethod(method_name, parameter_class);//if exists return true
            return method;
        } catch (Exception e)
        {
            //There was some problem return null
            Log.d(tag,"getMethod Failed");
            //e.printStackTrace();
            return null;
        }
    }
    public static  boolean classExists(String class_name)
    {
        try
        {
            Class.forName(class_name);
            return true;
        }
        catch (ClassNotFoundException e)
        {
            Log.d(tag,"classExists Failed");
            return false;
        }
    }
    public static boolean methodCheck(Object receiver_object,String class_name, String method_name, Class parameter_type)
    {
        try
        {
            Class<?> telephonyClass = null;
            if(receiver_object == null)
                telephonyClass = Class.forName(class_name);
            else
                telephonyClass = receiver_object.getClass();
            Class<?>[] parameter ;
            if(parameter_type!=null)
            {
                parameter = new Class[1];
                parameter[0] = parameter_type;
            }
            else
            parameter = new Class[0]; //empty parameters

            telephonyClass.getMethod(method_name, parameter);//if exists return true
            return true;
        } catch (Exception e)
        {
            //There was some problem return false
            Log.d(tag,"methodCheck 1 Failed");
            //e.printStackTrace();
            return false;
        }
    }
    //Array of Parameters
    public static boolean methodCheck(Object receiver_object,String class_name, String method_name, Class[] parameter_type)
    {
        try
        {
            Class<?> telephonyClass = null;
            if(receiver_object == null)
                telephonyClass = Class.forName(class_name);
            else
                telephonyClass = receiver_object.getClass();
            if(parameter_type==null)
                parameter_type = new Class[0]; //empty parameters

            telephonyClass.getMethod(method_name, parameter_type);//if exists return true
            return true;
        } catch (Exception e)
        {
            //There was some problem return false
            Log.d(tag,"methodCheck 2 Failed");
            //e.printStackTrace();
            return false;
        }
    }
}

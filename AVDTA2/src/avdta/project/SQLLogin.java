/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.project;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

/**
 *
 * @author Michael
 */
public class SQLLogin 
{
    public static boolean hasSQL()
    {
        return LOGIN_FILE.exists();
    }
    
    public static final File LOGIN_FILE = new File("SQL.dat");
    
    private String ip, user, password;
    private int port;
    private String database;
    
    public SQLLogin(String ip, String user, String password, int port)
    {
        this(ip, user, password, port, "");
    }
    public SQLLogin(String ip, String user, String password, int port, String database)
    {
        this.ip = ip;
        this.user = user;
        this.password = password;
        this.port = port;
        this.database = database;
    }
    
    public SQLLogin() throws IOException
    {
        database="";
        
        Scanner filein = new Scanner(LOGIN_FILE);
        
        while(filein.hasNext())
        {
            String key = filein.next();
            String value = filein.nextLine().trim();
            
            if(key.equalsIgnoreCase("ip"))
            {
                ip = value;
            }
            else if(key.equalsIgnoreCase("user"))
            {
                user = value;
            }
            else if(key.equalsIgnoreCase("port"))
            {
                port = Integer.parseInt(value);
            }
        }
        filein.close();
    }
    
    public SQLLogin(String database) throws IOException
    {
        this.database = database;
        
        Scanner filein = new Scanner(LOGIN_FILE);
        
        while(filein.hasNext())
        {
            String key = filein.next();
            String value = filein.nextLine().trim();
            
            if(key.equalsIgnoreCase("ip"))
            {
                ip = value;
            }
            else if(key.equalsIgnoreCase("user"))
            {
                user = value;
            }
            else if(key.equalsIgnoreCase("password"))
            {
                password = value;
            }
            else if(key.equalsIgnoreCase("port"))
            {
                port = Integer.parseInt(value);
            }
        }
        filein.close();
    }
    
    public String toString()
    {
        return "jdbc:mysql://"+ip+":"+port+"/"+database+"?"+"user="+user+"&password="+password;
    }
    
}

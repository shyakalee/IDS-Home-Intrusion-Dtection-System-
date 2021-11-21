/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ids_project;

/**
 *
 * @author SHYAKA Leonce
 */
public class User {
    private int code;
    private String name;
    private String date;
    private String time;
    
    public User(int code,String name,String date,String time){
        this.code=code;
        this.name=name;
        this.date=date;
        this.time=time;
    }    
    
    public int getCode(){
        return code;
    }
    
    public String getName(){
        return name;
    }
    
    public String getDate(){
        return date;
    }
    
    public String getTime(){
    return time;
    }
 }

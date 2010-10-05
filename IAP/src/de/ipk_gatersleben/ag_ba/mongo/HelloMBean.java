package de.ipk_gatersleben.ag_ba.mongo;

public interface HelloMBean { 
 
    public void sayHello(); 
    public int add(int x, int y); 
 
    public String getName(); 
 
    public int getCacheSize(); 
    public void setCacheSize(int size); 
} 
package net.za.cair.dip.experimentbench;

import java.io.File;
import java.io.NotSerializableException;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public class SerialTester {
	 /**
     * Get serialVersionUID of a class that implements Serializable. Especially usefull for client server applications with different
     * versions of client and server but without changes of the serializable objects they exchange. Insert the resulting value as
     * <code>static final long serialVersionUID = <result of this method>L;</code> in your serializable class.
     * 
     * @param javaClass Class to get serialVersionUID for.
     * @return The serialVersionUID for <i>javaClass</i> or 0 if failed.
     * @throws NotSerializableException if <i>javaClass</i> does not implement Serializable.
     */
    static public long getSerialVersionUID(Class javaClass) throws NotSerializableException {
        long result = 0;
        
        if (javaClass != null) {
            //check if class implements Serailizable:
            Class[] classes = javaClass.getInterfaces();
            if (classes != null) {
                for (Class c : classes) {
                    if (c == Serializable.class) {
                        result = ObjectStreamClass.lookup(javaClass).getSerialVersionUID();
                        break;
                    }
                }//next interface
                if (result == 0) {
                    throw new NotSerializableException("Class '"+javaClass.getName()+"' does not implement "+Serializable.class);
                }
            }//else: interfaces unavailable
        }//else: input unavailable
        
        return result;
    }//getSerialVersionUID()
    
    public static void main(String [] args){
    	// Create a File object on the root of the directory containing the class file
    	File file = new File("RanksTest3/");

    	try {
    	    // Convert File to a URL
    	    URL url = file.toURL();          // file:/c:/myclasses/
    	    URL[] urls = new URL[]{url};

    	    // Create a new class loader with the directory
    	    ClassLoader cl = new URLClassLoader(urls);

    	    // Load in the class; MyClass.class should be located in
    	    // the directory file:/c:/myclasses/com/mycompany
    	    Class cls = cl.loadClass("net.za.cair.dip.model.Rank");
    	    System.out.println("ID: " + getSerialVersionUID(cls));
    	} catch (MalformedURLException e) {
    		System.out.println("malformed.");
    	} catch (ClassNotFoundException e) {
    		System.out.println("Class not found.");
    	} catch (NotSerializableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}

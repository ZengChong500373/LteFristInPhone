// IServiceOne.aidl
package com.jyh.lte;
import com.jyh.lte.ServiceOneDataListener;
// Declare any non-default types here with import statements

interface IServiceOne {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
//    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
//            double aDouble, String aString);
	void getData();

	void registerListener(ServiceOneDataListener listener);
	void unRegisterListener();

	void bind2Lte();


}

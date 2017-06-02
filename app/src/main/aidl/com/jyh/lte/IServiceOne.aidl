// IServiceOne.aidl
package com.jyh.lte;
import com.jyh.lte.ServiceOneDataListener;
// Declare any non-default types here with import statements

interface IServiceOne {
	void getData();

	void registerListener(ServiceOneDataListener listener);
	void unRegisterListener();

	void bind2Lte();


}

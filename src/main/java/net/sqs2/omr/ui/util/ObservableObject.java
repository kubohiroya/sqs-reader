/*
  Copyright 2011 KUBO Hiroya (hiroya@cuc.ac.jp).
  
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 Created on 2011/12/03

 */
package net.sqs2.omr.ui.util;

import java.util.ArrayList;

public class ObservableObject<T>{
	T data;
	ArrayList<Observer<T>> observerList = new ArrayList<Observer<T>>();
	
	public ObservableObject(T data){
		this.data = data;
	}

	public void bind(Observer<T> ob) {
		observerList.add(ob);
	}
	
	public T getObject() {
		return data;
	}
	
	public void setObject(T t) {
		this.data = t;
	}
	
	public void update() {
		for(Observer<T> ob: observerList) {
			ob.update(data);
		}
	}
}
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
 package net.sqs2.omr.util;

import java.util.Map;

import org.apache.commons.collections15.map.LRUMap;

public class Cache<K,T> {
	protected Map<K, T> map;

	public Cache() {
		this.map = new LRUMap<K, T>();
	}

	public Cache(int cacheSize) {
		this.map = new LRUMap<K, T>(cacheSize);
	}

	public void regist(K key, T element) {
		this.map.put(key, element);
	}

	public T lookup(K key) {
		return this.map.get(key);
	}
	
	public void clear(){
		map.clear();
	}
}

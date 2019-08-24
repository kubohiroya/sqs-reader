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

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.collections15.map.LinkedMap;

public class RelationSet<A, B> implements Serializable {

	private static final long serialVersionUID = 1L;

	private LinkedMap<A, Set<B>> relationAtoB = new LinkedMap<A, Set<B>>();
	private LinkedMap<B, Set<A>> relationBtoA = new LinkedMap<B, Set<A>>();

	public RelationSet() {
	}

	public Set<A> getValueSetA(B b) {
		Set<A> aSet = this.relationBtoA.get(b);
		if (aSet == null) {
			aSet = new TreeSet<A>();
		}
		return aSet;
	}

	public Set<B> getValueSetB(A a) {
		Set<B> bSet = this.relationAtoB.get(a);
		if (bSet == null) {
			bSet = new TreeSet<B>();
		}
		return bSet;
	}

	public void put(A a, B b) {
		Set<B> bSet = getValueSetB(a);
		bSet.add(b);
		Set<A> aSet = getValueSetA(b);
		aSet.add(a);
	}

	public boolean remove(A a, B b) {
		Set<B> bList = getValueSetB(a);
		boolean ret1 = bList.remove(b);
		Set<A> aList = getValueSetA(b);
		boolean ret2 = aList.remove(a);
		return ret1 & ret2;
	}

	public Set<A> getKeySetA() {
		return this.relationAtoB.keySet();
	}

	public Set<B> getKeySetB() {
		return this.relationBtoA.keySet();
	}

	public Set<Map.Entry<A, Set<B>>> getEntrySetA() {
		return this.relationAtoB.entrySet();
	}

	public Set<Map.Entry<B, Set<A>>> getEntrySetB() {
		return this.relationBtoA.entrySet();
	}

}

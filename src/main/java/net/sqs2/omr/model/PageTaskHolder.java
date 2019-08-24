/**
 *  PageTaskHolder.java

 Copyright 2007 KUBO Hiroya (hiroya@cuc.ac.jp).

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 Created on 2007/01/31
 Author hiroya
 */
package net.sqs2.omr.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class PageTaskHolder{

	transient private BlockingQueue2<PageTask> preparedTaskQueue = null;
	transient private DelayQueue<PageTask> localLeasedTaskQueue = null;
	transient private DelayQueue<PageTask> remoteLeasedTaskQueue = null;
	transient private BlockingQueue<PageTask> submittedTaskQueue = null;

	static class BlockingQueue2<T> extends LinkedBlockingQueue<T>{
		private static final long serialVersionUID = 0L;
		
		Set<T> set;
		BlockingQueue2(){
			this.set = Collections.synchronizedSet(new HashSet<T>());
		}
		
		@Override
		public int size(){
			return this.set.size();
		}
		
		@Override
		public void clear(){
			super.clear();
			this.set.clear();
		}
		
		@Override
		public boolean offer(T t){
			this.set.add(t);
			return super.offer(t);
		}
		
		@Override
		public T poll(long timeout, TimeUnit timeUnit)throws InterruptedException{
			T t = super.poll(timeout, timeUnit);
			if(t != null){
				this.set.remove(t);
			}
			return t;
		}
		
		@Override
		public boolean contains(Object t){
			return this.set.contains(t);
		}
	}

	public PageTaskHolder() {
		this.preparedTaskQueue = new BlockingQueue2<PageTask>();
		this.localLeasedTaskQueue = new DelayQueue<PageTask>();
		this.remoteLeasedTaskQueue = new DelayQueue<PageTask>();
		this.submittedTaskQueue = new LinkedBlockingQueue<PageTask>();
	}
	
	public String toString(){
		return "PageTaskHolder[R="+" p="+this.preparedTaskQueue.size()+" l="+this.localLeasedTaskQueue.size()+" r="+this.remoteLeasedTaskQueue.size()+" s="+this.submittedTaskQueue.size()+"]";
	}

	public int getNumPreparedTasks() {
		return this.preparedTaskQueue.size();
	}
	
	public int getNumLocalLeasedTasks() {
		return this.localLeasedTaskQueue.size();
	}

	public int getNumRemoteLeasedTasks() {
		return this.remoteLeasedTaskQueue.size();
	}

	public int getNumSubmittedTasks() {
		return this.submittedTaskQueue.size();
	}

	public synchronized void stop() {
		this.preparedTaskQueue.clear();
		this.localLeasedTaskQueue.clear();
		this.remoteLeasedTaskQueue.clear();
	}

	public synchronized void clear() {
		this.preparedTaskQueue.clear();
		this.localLeasedTaskQueue.clear();
		this.remoteLeasedTaskQueue.clear();
		this.submittedTaskQueue.clear();
	}

	public synchronized void addPreparedToExecTask(PageTask task) {
		this.preparedTaskQueue.offer(task);
	}

	public synchronized PageTask leaseTask(long timeout) throws InterruptedException {
		PageTask task = this.preparedTaskQueue.poll(timeout, TimeUnit.MILLISECONDS);
		return task;
	}

	public synchronized void addLeaseLocalTask(PageTask task) {
		this.localLeasedTaskQueue.add(task);
	}

	public synchronized void addLeaseRemoteTask(PageTask task) {
		this.remoteLeasedTaskQueue.add(task);
	}

	public boolean isPreparedTask(Ticket task) {
		return this.preparedTaskQueue.contains(task);
	}

	public boolean isEmpty() {
		return this.preparedTaskQueue.isEmpty() && this.localLeasedTaskQueue.isEmpty()
				&& this.remoteLeasedTaskQueue.isEmpty() && this.submittedTaskQueue.isEmpty();
	}

	public synchronized PageTask takeLocalLeasedExpiredTask() throws InterruptedException {
		return this.localLeasedTaskQueue.poll(100, TimeUnit.MILLISECONDS);
	}

	public synchronized PageTask takeRemoteLeasedExpiredTask() throws InterruptedException {
		return this.remoteLeasedTaskQueue.poll(100, TimeUnit.MILLISECONDS);
	}

	public boolean isLeasedTask(Ticket task) {
		return this.localLeasedTaskQueue.contains(task) || this.remoteLeasedTaskQueue.contains(task);
	}

	public synchronized PageTask submitTask(PageTask task) {
	
		if (this.localLeasedTaskQueue.remove(task)) {
			this.submittedTaskQueue.add(task);
			return task;
		}
		
		if (this.remoteLeasedTaskQueue.remove(task)) {
			this.submittedTaskQueue.add(task);
			return task;
		}	

		Logger.getLogger(getClass().getName()).warning("submitted unknown task: " + task);
		return null;
	}

	public synchronized PageTask pollSubmittedTask() {
		try {
			PageTask task = this.submittedTaskQueue.poll(5, TimeUnit.MILLISECONDS);
			return task;
		} catch (InterruptedException ignore) {
			return null;
		}
	}

}

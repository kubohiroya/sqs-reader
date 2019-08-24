package net.sqs2.event;

import org.apache.commons.collections15.set.ListOrderedSet;

public class EventSource <Event>{
	protected ListOrderedSet<EventListener<Event>> listeners = new ListOrderedSet<EventListener<Event>>();
	
	public void addListener(EventListener<Event> l){
		listeners.add(l);
	}
	
	public void removeListener(EventListener<Event> l){
		listeners.remove(l);
	}
	
	public void fireEvent(Event e){
		for(EventListener<Event> l: listeners){
			l.eventFired(e);
		}
	}
}

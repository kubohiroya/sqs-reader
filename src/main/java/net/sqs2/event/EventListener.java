package net.sqs2.event;

public interface EventListener<Event> extends java.util.EventListener{
	public void eventFired(Event event);
}

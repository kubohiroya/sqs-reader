package net.sqs2.omr.session.daemon;

import net.sqs2.omr.session.service.LocalSessionSourceServer;



public interface ServerDispatcher {

	public abstract void publish();

	public abstract boolean hasPublished();

	public abstract long getKey();

	public abstract boolean isRemote();

	public abstract LocalSessionSourceServer getLocalServer();

	public abstract LocalSessionSourceServer getServer();

	public abstract void close();

}
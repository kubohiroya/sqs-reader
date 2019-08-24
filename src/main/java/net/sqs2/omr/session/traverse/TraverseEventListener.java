package net.sqs2.omr.session.traverse;

/**
 * Interface for traverse event listeners.
 * <p>Events sequence:
 * <ol>
 * <li>start-session
 * <li>start-master
 * <li>start-sourceDirectory
 * <li>end-sourceDirectory
 * <li>end-master
 * <li>end-session
 * </ol>
 *
 */
public interface TraverseEventListener {

	public abstract void startSessionSource(SessionSourceEvent sessionSourceEvent);

	public abstract void endSessionSource(SessionSourceEvent sessionSourceEvent);

	public abstract void startMaster(MasterEvent masterEvent);

	public abstract void endMaster(MasterEvent masterEvent);

	public abstract void startSourceDirectory(SourceDirectoryEvent sourceDirectoryEvent);

	public abstract void endSourceDirectory(SourceDirectoryEvent sourceDirectoryEvent);
}

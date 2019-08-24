package net.sqs2.omr.model;

import java.io.File;

import net.sqs2.store.ObjectStore;
import net.sqs2.store.ObjectStore.ObjectStoreException;
import net.sqs2.store.ObjectStoreAccessor;

public class AbstractObjectAccessor {

	protected ObjectStore objectStore;
	protected ObjectStoreAccessor dba;

	public AbstractObjectAccessor(File sourceDirectoryRoot, String name)throws ObjectStoreException{
		this.objectStore = ObjectStore.getInstance(sourceDirectoryRoot, CacheConstants.getCacheDirname());
		this.dba = objectStore.getAccessor(name);
	}
	
	public ObjectStore getObjectStore(){
		return this.objectStore;
	}
	
	public void delete()throws ObjectStoreException{
		this.objectStore.delete(this.dba.getName());
	}
	
	public void shutdown()throws ObjectStoreException{
		this.dba.shutdown();
	}

}
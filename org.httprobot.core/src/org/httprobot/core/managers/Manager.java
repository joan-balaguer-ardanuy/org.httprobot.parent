package org.httprobot.core.managers;

import java.util.Vector;

import org.httprobot.common.events.CliEventArgs;
import org.httprobot.common.exceptions.NotImplementedException;
import org.httprobot.common.io.CommandLine;

/**
 * Main manager abstract class.
 * @author Joan
 * @param <IListener> {@link org.httprobot.common.interfaces.IListener} Type.
 */
public abstract class Manager<IListener extends org.httprobot.common.interfaces.IListener> 
	extends CommandLine
{
	private IListener parent;
	/**
	 * Gets the parent Object inheriting {@link org.httprobot.common.interfaces.IListener}
	 * @return
	 */
	public IListener getParent() 
	{
		return this.parent;
	}	
	/**
	 * RmlControlInit Listeners
	 */
	private Vector<IListener> manager_listeners = null;
	/**
	 * Adds TryParse event listener
	 * @param listener
	 */
	public final synchronized void addCommandListener(IListener listener)
	{
		manager_listeners.add(listener);
	}
	/**
	 * Removes TryParse event listener
	 * @param rmlControl
	 */
	public final synchronized void removeCommandListener(IListener listener)
	{
		manager_listeners.remove(listener);
	}
	/**
	 * Fires event method to parent.
	 * @param message RML Object
	 * @throws NotImplementedException 
	 */
	protected final void CommandEvent(CliEventArgs e)
	{
		for(org.httprobot.common.interfaces.IListener listener : manager_listeners)
		{
			listener.OnCommandInput(this, e);
		}
	}
	/**
	 * Starts manager.
	 */
	public abstract void start();	
	/**
	 * Stops manager.
	 */
	public abstract void stop();	
	/**
	 * @param parent manager listener.
	 */
	public Manager(IListener parent)
	{
		this.parent = parent;
		this.manager_listeners = new Vector<IListener>();
		addCommandListener(parent);
	}
}
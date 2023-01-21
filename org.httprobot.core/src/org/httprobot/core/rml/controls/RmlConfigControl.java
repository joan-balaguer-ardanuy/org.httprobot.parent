/**
 * 
 */
package org.httprobot.core.rml.controls;

import java.util.Vector;

import javax.xml.bind.annotation.XmlTransient;

import org.httprobot.common.definitions.Enums.RmlEventType;
import org.httprobot.common.events.RmlEventArgs;
import org.httprobot.common.exceptions.InconsistenMessageException;
import org.httprobot.common.exceptions.NotImplementedException;
import org.httprobot.common.interfaces.IRmlListener;
import org.httprobot.common.rml.Rml;
import org.httprobot.common.rml.config.Config;
import org.httprobot.common.rml.config.Log;
import org.httprobot.common.rml.config.Session;
import org.httprobot.common.rml.datatypes.Rule;
import org.httprobot.common.rml.datatypes.operators.Delimiters;
import org.httprobot.common.tools.CommandLineInterface;
import org.httprobot.core.rml.controls.config.ConfigControl;
import org.httprobot.core.rml.controls.config.LogControl;
import org.httprobot.core.rml.controls.config.SessionControl;
import org.httprobot.core.rml.controls.config.interfaces.IConfigListener;
import org.httprobot.core.rml.controls.config.interfaces.ILogListener;
import org.httprobot.core.rml.controls.config.interfaces.ISessionListener;
import org.httprobot.core.rml.controls.interfaces.IRmlConfigListener;
import org.httprobot.core.rml.controls.interfaces.IRmlDataTypeListener;

/**
 * RML configuration control class. Inherits {@link RmlControl}. 
 * @author Joan
 *
 */
@XmlTransient
public abstract class RmlConfigControl extends RmlControl implements IRmlConfigListener, IRmlDataTypeListener
{
	/**
	 * -3219652987951425693L
	 */
	private static final long serialVersionUID = -3219652987951425693L;
	/**
	 * {@link Rule} Load Listeners
	 */
	private Vector<IConfigListener> config_listeners = null;
	/**
	 * {@link Rule} Load Listeners
	 */
	private Vector<ILogListener> log_listeners = null;
	/**
	 * {@link Rule} Load Listeners
	 */
	private Vector<ISessionListener> session_listeners = null;
	/**
	 * 
	 */
	public RmlConfigControl() 
	{
		super();
		InitConfigControl();
	}
	/**
	 * @param parent
	 */
	public RmlConfigControl(IRmlListener parent, Rml message) 
	{
		super(parent, message);
		InitConfigControl();
	}	
	/**
	 * Adds {@link Delimiters} event {@link IRmlDataTypeListener}.
	 * @param listener {@link IRmlDataTypeListener} the listener
	 */
	public final synchronized void addConfigListener(IConfigListener listener)
	{
		config_listeners.add(listener);
	}
	/**
	 * Adds {@link Delimiters} event {@link IRmlDataTypeListener}.
	 * @param listener {@link IRmlDataTypeListener} the listener
	 */
	public final synchronized void addLogListener(IRmlConfigListener listener)
	{
		log_listeners.add(listener);
	}
	/**
	 * Adds {@link Delimiters} event {@link IRmlDataTypeListener}.
	 * @param listener {@link IRmlDataTypeListener} the listener
	 */
	public final synchronized void addSessionListener(IRmlConfigListener listener)
	{
		session_listeners.add(listener);
	}
	/**
	 * Fires control config event.
	 * @param e {@link RmlEventArgs} the arguments
	 */
	protected final void ControlConfigEvent(RmlEventArgs e)
	{
		for(IConfigListener config_listener : config_listeners) 
		{
			try 
			{
				switch (e.getRmlEventType()) 
				{
					case INIT:
						config_listener.OnConfigInit(this, e);						
						break;
					case READ:
						config_listener.OnConfigRead(this, e);						
						break;
					case LOAD:				
						config_listener.OnConfigLoaded(this, e);
						break;
					case CHANGE:
						config_listener.OnConfigChanged(this, e);
						break;
					case RENDER:
						config_listener.OnConfigRendered(this, e);
						break;
					case WRITE:
						config_listener.OnConfigWrite(this, e);
						break;
					default:
						break;
				}
			}
			catch (NotImplementedException e1) 
			{
				e1.printStackTrace();
			} 
			catch (InconsistenMessageException e2) 
			{
				e2.printStackTrace();
			}
		}
	}
	/**
	 * Fires control log event.
	 * @param e {@link RmlEventArgs} the arguments
	 */
	protected final void ControlLogEvent(RmlEventArgs e)
	{
		for(ILogListener listener : log_listeners) 
		{
			try 
			{
				switch (e.getRmlEventType()) 
				{
					case INIT:
						listener.OnLogInit(this, e);						
						break;
					case READ:
						listener.OnLogRead(this, e);						
						break;
					case LOAD:				
						listener.OnLogLoaded(this, e);
						break;
					case CHANGE:
						listener.OnLogChanged(this, e);
						break;
					case RENDER:
						listener.OnLogRendered(this, e);
						break;
					case WRITE:
						listener.OnLogWrite(this, e);
						break;
					default:
						break;
				}
			} 
			catch (NotImplementedException e1) 
			{
				e1.printStackTrace();
			} 
			catch (InconsistenMessageException e2) 
			{
				e2.printStackTrace();
			}			
		}
	}	
	/**
	 * Fires control session event.
	 * @param e {@link RmlEventArgs} the arguments
	 */
	protected final void ControlSessionEvent(RmlEventArgs e)
	{
		for(ISessionListener listener : session_listeners) 
		{
			try 
			{
				switch (e.getRmlEventType()) 
				{
					case INIT:
						listener.OnSessionInit(this, e);						
						break;
					case READ:
						listener.OnSessionRead(this, e);						
						break;
					case LOAD:				
						listener.OnSessionLoaded(this, e);
						break;
					case CHANGE:
						listener.OnSessionChanged(this, e);
						break;
					case RENDER:
						listener.OnSessionRendered(this, e);
						break;
					case WRITE:
						listener.OnSessionWrite(this, e);
						break;
					default:
						break;
				}
			} 
			catch (NotImplementedException e1) 
			{
				e1.printStackTrace();
			} 
			catch (InconsistenMessageException e2) 
			{
				e2.printStackTrace();
			}			
		}
	}
	/**
	 * Initializes RML configuration control.
	 */
	private final void InitConfigControl()
	{
		this.config_listeners = new Vector<IConfigListener>();
		this.log_listeners = new Vector<ILogListener>();
		this.session_listeners = new Vector<ISessionListener>();
		
		RmlEventArgs e = new RmlEventArgs(this, RmlEventType.INIT, null);
		
		if(e.getSource() instanceof ConfigControl)
		{
			ControlConfigEvent(e);
		}
		else if(e.getSource() instanceof LogControl)
		{
			ControlLogEvent(e);
		}
		else if(e.getSource() instanceof SessionControl)
		{
			ControlSessionEvent(e);
		}
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.datatypes.interfaces.IActionListener#OnActionChanged(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnActionChanged(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{
		CommandLineInterface.ThrowNotImplementedException(this, "RmlConfigControl.OnActionChanged not implemented");
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.datatypes.interfaces.IActionListener#OnActionInit(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnActionInit(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{
		CommandLineInterface.ThrowNotImplementedException(this, "RmlConfigControl.OnActionInit not implemented");
	}	
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.datatypes.interfaces.IActionListener#OnActionLoaded(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnActionLoaded(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException
	{
		CommandLineInterface.ThrowNotImplementedException(this, "RmlConfigControl.OnActionLoaded not implemented");
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.datatypes.interfaces.IActionListener#OnActionRead(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnActionRead(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{
		CommandLineInterface.ThrowNotImplementedException(this, "RmlConfigControl.OnActionRead not implemented");
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.datatypes.interfaces.IActionListener#OnActionRendered(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnActionRendered(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{
		CommandLineInterface.ThrowNotImplementedException(this, "RmlConfigControl.OnActionRendered not implemented");
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.datatypes.interfaces.IActionListener#OnActionWrite(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnActionWrite(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{
		CommandLineInterface.ThrowNotImplementedException(this, "RmlConfigControl.OnActionWrite not implemented");
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.config.interfaces.IConfigControl#OnConfigChanged(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnConfigChanged(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{
		CommandLineInterface.ThrowNotImplementedException(this, "RmlConfigControl.OnConfigChanged not implemented");
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.config.interfaces.IConfigControl#OnConfigInit(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnConfigInit(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{
		CommandLineInterface.ThrowNotImplementedException(this, "RmlConfigControl.OnConfigChanged not implemented");
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.config.interfaces.IConfigControl#OnConfigLoaded(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnConfigLoaded(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{
		CommandLineInterface.ThrowNotImplementedException(this, "RmlConfigControl.OnConfigLoaded not implemented");
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.config.interfaces.IConfigControl#OnConfigRead(java.lang.Object, org.httprobot.core.events.RmlConfigEventArgs)
	 */
	@Override
	public void OnConfigRead(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{
		CommandLineInterface.ThrowNotImplementedException(this, "RmlConfigControl.OnConfigRead not implemented");
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.config.interfaces.IConfigControl#OnConfigRendered(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnConfigRendered(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException
	{
		CommandLineInterface.ThrowNotImplementedException(this, "RmlConfigControl.OnConfigRendered not implemented");
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.config.interfaces.IConfigControl#OnConfigWrite(java.lang.Object, org.httprobot.core.events.RmlConfigEventArgs)
	 */
	@Override
	public void OnConfigWrite(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException
	{
		CommandLineInterface.ThrowNotImplementedException(this, "RmlConfigControl.OnConfigWrite not implemented");
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.RmlControl#OnControlChanged(java.lang.Object, org.httprobot.core.events.RmlControlEventArgs)
	 */
	@Override
	public abstract void OnControlChanged(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException;
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.RmlControl#OnControlInit(java.lang.Object, org.httprobot.core.events.RmlControlEventArgs)
	 */
	@Override
	public abstract void OnControlInit(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException;	
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.RmlControl#OnControlLoaded(java.lang.Object, org.httprobot.core.events.RmlControlEventArgs)
	 */
	@Override
	public abstract void OnControlLoaded(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException ;	
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.RmlControl#OnControlRead(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	public abstract void OnControlRead(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException;	
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.RmlControl#OnControlRendered(java.lang.Object, org.httprobot.core.events.RmlControlEventArgs)
	 */
	@Override
	public abstract void OnControlRendered(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException;	
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.RmlControl#OnControlWrite(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	public abstract void OnControlWrite(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException;;
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.datatypes.interfaces.IDataViewControl#OnDataViewChanged(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnDataViewChanged(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{
		CommandLineInterface.ThrowNotImplementedException(this, "RmlConfigControl.OnDataViewChanged not implemented");
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.datatypes.interfaces.IDataViewControl#OnDataViewInit(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnDataViewInit(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{
		CommandLineInterface.ThrowNotImplementedException(this, "RmlConfigControl.OnDataViewInit not implemented");
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.datatypes.interfaces.IDataViewControl#OnDataViewLoaded(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnDataViewLoaded(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{
		CommandLineInterface.ThrowNotImplementedException(this, "RmlConfigControl.OnDataViewLoaded not implemented");
	}	
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.datatypes.interfaces.IDataViewControl#OnDataViewRead(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnDataViewRead(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{
		CommandLineInterface.ThrowNotImplementedException(this, "RmlConfigControl.OnDataViewRead not implemented");
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.datatypes.interfaces.IDataViewControl#OnDataViewRendered(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnDataViewRendered(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{
		CommandLineInterface.ThrowNotImplementedException(this, "RmlConfigControl.OnDataViewRendered not implemented");
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.datatypes.interfaces.IDataViewControl#OnDataViewWrite(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnDataViewWrite(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{
		CommandLineInterface.ThrowNotImplementedException(this, "RmlConfigControl.OnDataViewWrite not implemented");
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.datatypes.interfaces.IFieldControl#OnFieldChanged(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnFieldChanged(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{
		CommandLineInterface.ThrowNotImplementedException(this, "RmlConfigControl.OnFieldChanged not implemented");
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.datatypes.interfaces.IFieldControl#OnFieldInit(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnFieldInit(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{
		CommandLineInterface.ThrowNotImplementedException(this, "RmlConfigControl.OnFieldInit not implemented");
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.datatypes.interfaces.IFieldControl#OnFieldLoaded(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnFieldLoaded(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{
		CommandLineInterface.ThrowNotImplementedException(this, "RmlConfigControl.OnFieldLoaded not implemented");
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.datatypes.interfaces.IFieldControl#OnFieldRead(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnFieldRead(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{
		CommandLineInterface.ThrowNotImplementedException(this, "RmlConfigControl.OnFieldRead not implemented");
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.datatypes.interfaces.IFieldRefListener#OnFieldRefChanged(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnFieldRefChanged(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{
		CommandLineInterface.ThrowNotImplementedException(this, "RmlConfigControl.OnFieldRefChanged not implemented");
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.datatypes.interfaces.IFieldRefListener#OnFieldRefInit(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnFieldRefInit(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException
	{
		CommandLineInterface.ThrowNotImplementedException(this, "RmlConfigControl.OnFieldRefInit not implemented");
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.datatypes.interfaces.IFieldRefListener#OnFieldRefLoaded(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnFieldRefLoaded(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{
		CommandLineInterface.ThrowNotImplementedException(this, "RmlConfigControl.OnFieldRefLoaded not implemented");
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.datatypes.interfaces.IFieldRefListener#OnFieldRefRead(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnFieldRefRead(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{
		CommandLineInterface.ThrowNotImplementedException(this, "RmlConfigControl.OnFieldRefRead not implemented");
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.datatypes.interfaces.IFieldRefListener#OnFieldRefRendered(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnFieldRefRendered(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{
		CommandLineInterface.ThrowNotImplementedException(this, "RmlConfigControl.OnFieldRefRendered not implemented");
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.datatypes.interfaces.IFieldRefListener#OnFieldRefWrite(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnFieldRefWrite(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{
		CommandLineInterface.ThrowNotImplementedException(this, "RmlConfigControl.OnFieldRefWrite not implemented");
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.datatypes.interfaces.IFieldControl#OnFieldRendered(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnFieldRendered(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{
		CommandLineInterface.ThrowNotImplementedException(this, "RmlConfigControl.OnFieldRefRendered not implemented");
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.datatypes.interfaces.IFieldsControl#OnFieldsChanged(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnFieldsChanged(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{
		CommandLineInterface.ThrowNotImplementedException(this, "RmlConfigControl.OnFieldsChanged not implemented");
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.datatypes.interfaces.IFieldsControl#OnFieldsInit(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnFieldsInit(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{
		CommandLineInterface.ThrowNotImplementedException(this, "RmlConfigControl.OnFieldsInit not implemented");
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.datatypes.interfaces.IFieldsControl#OnFieldsLoaded(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnFieldsLoaded(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{
		CommandLineInterface.ThrowNotImplementedException(this, "RmlConfigControl.OnFieldsLoaded not implemented");
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.datatypes.interfaces.IFieldsControl#OnFieldsRead(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnFieldsRead(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{
		CommandLineInterface.ThrowNotImplementedException(this, "RmlConfigControl.OnFieldsRead not implemented");
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.datatypes.interfaces.IFieldsControl#OnFieldsRendered(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnFieldsRendered(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{
		CommandLineInterface.ThrowNotImplementedException(this, "RmlConfigControl.OnFieldsRendered not implemented");
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.datatypes.interfaces.IFieldsControl#OnFieldsWrite(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnFieldsWrite(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{
		CommandLineInterface.ThrowNotImplementedException(this, "RmlConfigControl.OnFieldsWrite not implemented");
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.datatypes.interfaces.IFieldControl#OnFieldWrite(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnFieldWrite(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{
		CommandLineInterface.ThrowNotImplementedException(this, "RmlConfigControl.OnFieldWrite not implemented");
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.config.interfaces.ILogControl#OnLogChanged(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnLogChanged(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{
		CommandLineInterface.ThrowNotImplementedException(this, "RmlConfigControl.OnLogChanged not implemented");
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.config.interfaces.ILogControl#OnLogInit(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnLogInit(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException
	{
		CommandLineInterface.ThrowNotImplementedException(this, "RmlConfigControl.OnLogInit not implemented");
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.config.interfaces.ILogControl#OnLogLoaded(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnLogLoaded(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{
		CommandLineInterface.ThrowNotImplementedException(this, "RmlConfigControl.OnLogLoaded not implemented");
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.config.interfaces.ILogControl#OnLogRead(java.lang.Object, org.httprobot.core.events.RmlConfigEventArgs)
	 */
	@Override
	public void OnLogRead(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{
		CommandLineInterface.ThrowNotImplementedException(this, "RmlConfigControl.OnLogRead not implemented");
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.config.interfaces.ILogControl#OnLogRendered(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnLogRendered(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{
		CommandLineInterface.ThrowNotImplementedException(this, "RmlConfigControl.OnLogRendered not implemented");
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.config.interfaces.ILogControl#OnLogWrite(java.lang.Object, org.httprobot.core.events.RmlConfigEventArgs)
	 */
	@Override
	public void OnLogWrite(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{
		CommandLineInterface.ThrowNotImplementedException(this, "RmlConfigControl.OnLogWrite not implemented");
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.datatypes.interfaces.IRuleControl#OnRuleChanged(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnRuleChanged(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{
		CommandLineInterface.ThrowNotImplementedException(this, "RmlConfigControl.OnRuleChanged not implemented");
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.datatypes.interfaces.IRuleControl#OnRuleInit(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnRuleInit(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{
		CommandLineInterface.ThrowNotImplementedException(this, "RmlConfigControl.OnRuleInit not implemented");
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.datatypes.interfaces.IRuleControl#OnRuleLoaded(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnRuleLoaded(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{
		CommandLineInterface.ThrowNotImplementedException(this, "RmlConfigControl.OnRuleLoaded not implemented");
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.datatypes.interfaces.IRuleControl#OnRuleRead(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnRuleRead(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{
		CommandLineInterface.ThrowNotImplementedException(this, "RmlConfigControl.OnRuleRead not implemented");
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.datatypes.interfaces.IRuleControl#OnRuleRendered(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnRuleRendered(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{
		CommandLineInterface.ThrowNotImplementedException(this, "RmlConfigControl.OnRuleRendered not implemented");
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.datatypes.interfaces.IRulesControl#OnRulesChanged(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnRulesChanged(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{
		CommandLineInterface.ThrowNotImplementedException(this, "RmlConfigControl.OnRuleChanged not implemented");
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.datatypes.interfaces.IRulesControl#OnRulesInit(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnRulesInit(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{
		CommandLineInterface.ThrowNotImplementedException(this, "RmlConfigControl.OnRulesInit not implemented");
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.datatypes.interfaces.IRulesControl#OnRulesLoaded(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnRulesLoaded(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{
		CommandLineInterface.ThrowNotImplementedException(this, "RmlConfigControl.OnRulesLoaded not implemented");
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.datatypes.interfaces.IRulesControl#OnRulesRead(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnRulesRead(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{
		CommandLineInterface.ThrowNotImplementedException(this, "RmlConfigControl.OnRulesRead not implemented");
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.datatypes.interfaces.IRulesControl#OnRulesRendered(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnRulesRendered(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{
		CommandLineInterface.ThrowNotImplementedException(this, "RmlConfigControl.OnRulesRendered not implemented");
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.datatypes.interfaces.IRulesControl#OnRulesWrite(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnRulesWrite(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{
		CommandLineInterface.ThrowNotImplementedException(this, "RmlConfigControl.OnRulesWrite not implemented");
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.datatypes.interfaces.IRuleControl#OnRuleWrite(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnRuleWrite(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{
		CommandLineInterface.ThrowNotImplementedException(this, "RmlConfigControl.OnRuleWrite not implemented");
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.datatypes.interfaces.IServerInfoControl#OnServerInfoChanged(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnServerInfoChanged(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{
		CommandLineInterface.ThrowNotImplementedException(this, "RmlConfigControl.OnServerInfoChanged not implemented");
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.datatypes.interfaces.IServerInfoControl#OnServerInfoInit(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnServerInfoInit(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{
		CommandLineInterface.ThrowNotImplementedException(this, "RmlConfigControl.OnServerInfoInit not implemented");
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.datatypes.interfaces.IServerInfoControl#OnServerInfoLoaded(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnServerInfoLoaded(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException
	{
		CommandLineInterface.ThrowNotImplementedException(this, "RmlConfigControl.OnServerInfoLoaded not implemented");
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.datatypes.interfaces.IServerInfoControl#OnServerInfoRead(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnServerInfoRead(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{
		CommandLineInterface.ThrowNotImplementedException(this, "RmlConfigControl.OnServerInfoRead not implemented");
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.datatypes.interfaces.IServerInfoControl#OnServerInfoRendered(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnServerInfoRendered(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{
		CommandLineInterface.ThrowNotImplementedException(this, "RmlConfigControl.OnServerInfoRendered not implemented");
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.datatypes.interfaces.IServerInfoControl#OnServerInfoWrite(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnServerInfoWrite(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{
		CommandLineInterface.ThrowNotImplementedException(this, "RmlConfigControl.OnServerInfoWrite not implemented");
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.config.interfaces.ISessionControl#OnSessionChanged(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnSessionChanged(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{
		CommandLineInterface.ThrowNotImplementedException(this, "RmlConfigControl.OnSessionChanged not implemented");
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.config.interfaces.ISessionControl#OnSessionInit(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnSessionInit(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{
		CommandLineInterface.ThrowNotImplementedException(this, "RmlConfigControl.OnSessionInit not implemented");
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.config.interfaces.ISessionControl#OnSessionLoaded(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnSessionLoaded(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{
		CommandLineInterface.ThrowNotImplementedException(this, "RmlConfigControl.OnSessionLoaded not implemented");
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.config.interfaces.ISessionControl#OnSessionRead(java.lang.Object, org.httprobot.core.events.RmlConfigEventArgs)
	 */
	@Override
	public void OnSessionRead(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{
		CommandLineInterface.ThrowNotImplementedException(this, "RmlConfigControl.OnSessionRead not implemented");
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.config.interfaces.ISessionControl#OnSessionRendered(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnSessionRendered(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException
	{
		CommandLineInterface.ThrowNotImplementedException(this, "RmlConfigControl.OnSessionRendered not implemented");
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.config.interfaces.ISessionControl#OnSessionWrite(java.lang.Object, org.httprobot.core.events.RmlConfigEventArgs)
	 */
	@Override
	public void OnSessionWrite(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{
		CommandLineInterface.ThrowNotImplementedException(this, "RmlConfigControl.OnSessionWrite not implemented");
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.datatypes.interfaces.IStepControl#OnStepChanged(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnStepChanged(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{
		CommandLineInterface.ThrowNotImplementedException(this, "RmlConfigControl.OnStepChanged not implemented");
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.datatypes.interfaces.IStepControl#OnStepInit(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnStepInit(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{
		CommandLineInterface.ThrowNotImplementedException(this, "RmlConfigControl.OnStepInit not implemented");
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.datatypes.interfaces.IStepControl#OnStepLoaded(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnStepLoaded(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{
		CommandLineInterface.ThrowNotImplementedException(this, "RmlConfigControl.OnStepLoaded not implemented");
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.datatypes.interfaces.IStepControl#OnStepRead(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnStepRead(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{
		CommandLineInterface.ThrowNotImplementedException(this, "RmlConfigControl.OnStepRead not implemented");
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.datatypes.interfaces.IStepControl#OnStepRendered(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnStepRendered(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{
		CommandLineInterface.ThrowNotImplementedException(this, "RmlConfigControl.OnStepRendered not implemented");
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.datatypes.interfaces.IStepsControl#OnStepsChanged(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnStepsChanged(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{
		CommandLineInterface.ThrowNotImplementedException(this, "RmlConfigControl.OnStepsChanged not implemented");
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.datatypes.interfaces.IStepsControl#OnStepsInit(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnStepsInit(Object sender, RmlEventArgs e)throws NotImplementedException, InconsistenMessageException 
	{
		CommandLineInterface.ThrowNotImplementedException(this, "RmlConfigControl.OnStepsInit not implemented");
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.datatypes.interfaces.IStepsControl#OnStepsLoaded(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnStepsLoaded(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{
		CommandLineInterface.ThrowNotImplementedException(this, "RmlConfigControl.OnStepsLoaded not implemented");
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.datatypes.interfaces.IStepsControl#OnStepsRead(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnStepsRead(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{
		CommandLineInterface.ThrowNotImplementedException(this, "RmlConfigControl.OnStepsRead not implemented");
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.datatypes.interfaces.IStepsControl#OnStepsRendered(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnStepsRendered(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{
		CommandLineInterface.ThrowNotImplementedException(this, "RmlConfigControl.OnStepsRendered not implemented");
	}	
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.datatypes.interfaces.IStepsControl#OnStepsWrite(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnStepsWrite(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{
		CommandLineInterface.ThrowNotImplementedException(this, "RmlConfigControl.OnStepsWrite not implemented");
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.datatypes.interfaces.IStepControl#OnStepWrite(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnStepWrite(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{
		CommandLineInterface.ThrowNotImplementedException(this, "RmlConfigControl.OnStepWrite not implemented");
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.datatypes.interfaces.IWebOptionsListener#OnWebOptionsInit(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnWebOptionsInit(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{
		CommandLineInterface.ThrowNotImplementedException(this, "RmlDataTypeControl.OnWebOptionsInit not implemented method");
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.datatypes.interfaces.IWebOptionsListener#OnWebOptionsRead(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnWebOptionsRead(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{
		CommandLineInterface.ThrowNotImplementedException(this, "RmlDataTypeControl.OnWebOptionsRead not implemented method");
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.datatypes.interfaces.IWebOptionsListener#OnWebOptionsLoaded(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnWebOptionsLoaded(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{
		CommandLineInterface.ThrowNotImplementedException(this, "RmlDataTypeControl.OnWebOptionsLoaded not implemented method");	
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.datatypes.interfaces.IWebOptionsListener#OnWebOptionsChanged(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnWebOptionsChanged(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{
		CommandLineInterface.ThrowNotImplementedException(this, "RmlDataTypeControl.OnWebOptionsChanged not implemented method");
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.datatypes.interfaces.IWebOptionsListener#OnWebOptionsRendered(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnWebOptionsRendered(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{
		CommandLineInterface.ThrowNotImplementedException(this, "RmlDataTypeControl.OnWebOptionsRendered not implemented method");
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.datatypes.interfaces.IWebOptionsListener#OnWebOptionsWrite(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnWebOptionsWrite(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{
		CommandLineInterface.ThrowNotImplementedException(this, "RmlDataTypeControl.OnWebOptionsWrite not implemented method");	
	}
	/**
	 * Removes {@link Delimiters} event {@link IRmlDataTypeListener}.
	 * @param listener {@link IRmlDataTypeListener} the listener
	 */
	public final synchronized void removeConfigListener(IRmlConfigListener listener)
	{
		config_listeners.remove(listener);
	}
	/**
	 * Removes {@link Delimiters} event {@link IRmlDataTypeListener}.
	 * @param listener {@link IRmlDataTypeListener} the listener
	 */
	public final synchronized void removeLogListener(IRmlConfigListener listener)
	{
		log_listeners.remove(listener);
	}
	/**
	 * Removes {@link Delimiters} event {@link IRmlDataTypeListener}.
	 * @param listener {@link IRmlDataTypeListener} the listener
	 */
	public final synchronized void removeSessionListener(IRmlConfigListener listener)
	{
		session_listeners.remove(listener);
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.RmlControl#setIsRendered(java.lang.Boolean)
	 */
	@Override
	public void setIsRendered(Boolean value) 
	{
		super.setIsRendered(value);
		
		Rml message = this.getMessage();		
		
		if(message instanceof Config)
		{
			ControlConfigEvent(new RmlEventArgs(this, RmlEventType.RENDER, message));
		}
		else if(message instanceof Log)
		{
			ControlConfigEvent(new RmlEventArgs(this, RmlEventType.RENDER, message));
		}
		else if(message instanceof Session)
		{
			ControlConfigEvent(new RmlEventArgs(this, RmlEventType.RENDER, message));
		}
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.RmlControl#setMessage(org.httprobot.common.rml.Rml)
	 */
	@Override
	public void setMessage(Rml message) 
	{
		super.setMessage(message);
		
		if(message instanceof Config)
		{
			ControlConfigEvent(new RmlEventArgs(this, RmlEventType.LOAD, message));
		}
		else if(message instanceof Log)
		{
			ControlConfigEvent(new RmlEventArgs(this, RmlEventType.LOAD, message));
		}
		else if(message instanceof Session)
		{
			ControlConfigEvent(new RmlEventArgs(this, RmlEventType.LOAD, message));
		}
	}
}
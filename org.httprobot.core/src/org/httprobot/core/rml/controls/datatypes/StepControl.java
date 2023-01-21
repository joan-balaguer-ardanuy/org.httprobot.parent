package org.httprobot.core.rml.controls.datatypes;

import javax.xml.bind.annotation.XmlRootElement;

import org.httprobot.common.definitions.Enums.Command;
import org.httprobot.common.events.CliEventArgs;
import org.httprobot.common.events.RmlEventArgs;
import org.httprobot.common.exceptions.InconsistenMessageException;
import org.httprobot.common.exceptions.NotImplementedException;
import org.httprobot.common.interfaces.IRmlListener;
import org.httprobot.common.rml.datatypes.Action;
import org.httprobot.common.rml.datatypes.Fields;
import org.httprobot.common.rml.datatypes.Step;
import org.httprobot.common.tools.CommandLineInterface;
import org.httprobot.core.rml.controls.RmlDataTypeControl;


/**
 * Step RML message control class. Inherits {@link RmlDataTypeControl}.
 * @author Joan
 * 
 */
@XmlRootElement
public class StepControl extends RmlDataTypeControl 
{
	/**
	 * 6271313548003227854L
	 */
	private static final long serialVersionUID = 6271313548003227854L;
	
	private ActionControl action_control;
	private FieldsControl fields_control;
	private StepControl step_control;
	private Step step;	
	/**
	 * Step control default class constructor
	 */
	public StepControl()
	{

	}
	/**
	 * Step control constructor
	 * @param parent {@link IRmlListener} listener
	 * @param step_num {@link Integer} step number
	 * @param step {@link Step} next step
	 */
	public StepControl(IRmlListener parent, Step step)
	{
		super(parent, step);
	}

	/**
	 * Gets the step.
	 * @return {@link Step} step
	 */
	public Step getStep() 
	{
		return step;
	}
	/**
	 * Gets the action for current step.
	 * @return {@link Action} the action
	 */
	public ActionControl getStep_action() {
		return action_control;
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.RmlDataTypeControl#OnActionChanged(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnActionChanged(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{

	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.RmlDataTypeControl#OnActionInit(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnActionInit(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{

	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.RmlDataTypeControl#OnActionLoaded(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnActionLoaded(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException
	{
		if(e.getMessage() != null)
		{
			try
			{
				step.setAction(Action.class.cast(e.getMessage()));
				CliCommandInputEvent(new CliEventArgs(this, Command.ACTION_CONTROL, e.getMessage()));
			}
			catch(ClassCastException ex)
			{
				CommandLineInterface.ThrowInconsistentMessageException(this, "\nStepsControl.OnActionLoaded: Action RML message expected");
			}
		}
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.RmlDataTypeControl#OnActionRead(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnActionRead(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException
	{
		
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.RmlDataTypeControl#OnActionRendered(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnActionRendered(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{

	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.RmlDataTypeControl#OnActionWrite(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnActionWrite(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException
	{
		
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.RmlDataTypeControl#OnControlChanged(java.lang.Object, org.httprobot.common.events.RmlControlEventArgs)
	 */
	@Override
	public void OnControlChanged(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException
	{
		
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.RmlDataTypeControl#OnControlInit(java.lang.Object, org.httprobot.common.events.RmlControlEventArgs)
	 */
	@Override
	public void OnControlInit(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException
	{
		if(e.getMessage() != null)
		{
			//Initialize using data.
			this.step = new Step();

			//Set inherited data.
			this.setUuid(e.getMessage().getUuid());
			this.setInherited(e.getMessage().getInherited());
			this.setRuntimeOptions(e.getMessage().getRuntimeOptions());
			
			this.step.setUuid(getUuid());
			this.step.setInherited(getInherited());
			this.step.setRuntimeOptions(getRuntimeOptions());				

			//Associate message to control.
			this.addCommandOutputListener(this);
			
			Step step = Step.class.cast(e.getMessage());
			
			try
			{								
				if(step.getAction() != null)
				{
					//If action message not null instantiate action message control.
					this.action_control = new ActionControl(this, step.getAction());
					
					//Associate child control to parent.
					this.action_control.addActionListener(this);
					this.addCommandOutputListener(this.action_control);
				}
				if(step.getFields() != null)
				{
					//If fields message not null instantiate fields message control.		
					this.fields_control = new FieldsControl(this, step.getFields());
					
					//Associate child control to parent.
					this.fields_control.addFieldsListener(this);
					this.addCommandOutputListener(this.fields_control);
				}
				if(step.getStep() != null)
				{
					//If step message not null instantiate step message control.
					this.step_control = new StepControl(this, step.getStep());
					
					//Associate child control to parent.
					this.step_control.addStepListener(this);
					this.addCommandOutputListener(this.step_control);
				}
			}
			catch(ClassCastException ex)
			{
				CommandLineInterface.ThrowInconsistentMessageException(this, "StepControl.OnControlInit: Step RML message expected");	
			}
		}
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.RmlDataTypeControl#OnControlLoaded(java.lang.Object, org.httprobot.common.events.RmlControlEventArgs)
	 */
	@Override
	public void OnControlLoaded(Object sender, RmlEventArgs e)  throws NotImplementedException, InconsistenMessageException
	{
		if(this.getMessage() != null)
		{
			try
			{
				Step step = Step.class.cast(e.getMessage());
				
				//Set data to initialized controls
				if(step.getAction() != null)
				{
					if(action_control.getUuid() == step.getAction().getUuid())
					{
						action_control.setMessage(step.getAction());
					}
				}
				
				if(step.getFields() != null)
				{
					if(fields_control.getUuid() == step.getFields().getUuid())
					{
						fields_control.setMessage(step.getFields());
					}
				}
				
				if(step.getStep() != null)
				{
					if(step_control.getUuid() == step.getStep().getUuid())
					{
						step_control.setMessage(step.getStep());
					}
				}
			}
			catch(ClassCastException ex)
			{
				CommandLineInterface.ThrowInconsistentMessageException(this, "\nStepsControl.OnControlLoaded: Step RML message expected");
			}
		}
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.RmlDataTypeControl#OnControlRead(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnControlRead(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{
		
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.RmlDataTypeControl#OnControlRendered(java.lang.Object, org.httprobot.common.events.RmlControlEventArgs)
	 */
	@Override
	public void OnControlRendered(Object sender, RmlEventArgs e) 
	{
		
	}
	@Override
	public void OnControlWrite(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException
	{
		
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.RmlDataTypeControl#OnFieldsChanged(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnFieldsChanged(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{

	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.RmlDataTypeControl#OnFieldsInit(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnFieldsInit(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{

	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.RmlDataTypeControl#OnFieldsLoaded(java.lang.Object, org.httprobot.common.events.RmlControlEventArgs)
	 */
	@Override
	public void OnFieldsLoaded(Object sender, RmlEventArgs e) throws InconsistenMessageException 
	{
		if(e.getMessage() != null)
		{
			try
			{
				this.step.setFields(Fields.class.cast(e.getMessage()));
				CliCommandInputEvent(new CliEventArgs(this, Command.FIELD_CONTROL, e.getMessage()));
			}
			catch (ClassCastException ex1) 
			{
				CommandLineInterface.ThrowInconsistentMessageException(this, "\nStepsControl.OnFieldsLoaded: Fields RML message expected");
			}
		}
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.RmlDataTypeControl#OnFieldsRead(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnFieldsRead(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{

	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.RmlDataTypeControl#OnFieldsRendered(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnFieldsRendered(Object sender, RmlEventArgs e) throws InconsistenMessageException, NotImplementedException {

	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.RmlDataTypeControl#OnFieldsWrite(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnFieldsWrite(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{

	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.RmlDataTypeControl#OnStepChanged(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnStepChanged(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.RmlDataTypeControl#OnStepInit(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnStepInit(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{

	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.RmlDataTypeControl#OnStepLoaded(java.lang.Object, org.httprobot.common.events.RmlControlEventArgs)
	 */
	@Override
	public void OnStepLoaded(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{
		try
		{
			StepControl current_control = StepControl.class.cast(sender);
			
			if(current_control != null)
			{
				//If this is OnStepLoaded child event set loaded data
				if(current_control.getParent() instanceof StepControl)
				{
					this.step.setStep(Step.class.cast(e.getMessage()));
					CliCommandInputEvent(new CliEventArgs(this, Command.STEP_CONTROL, e.getMessage()));
				}
			}
		}
		catch(ClassCastException ex1)
		{
			CommandLineInterface.ThrowInconsistentMessageException(this, "\nStepsControl.OnStepLoaded: Step RML message expected");
		}
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.RmlDataTypeControl#OnStepRead(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnStepRead(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException
	{
		
	}
	/* (non-Javadoc)
	 * @see org.httprobot.core.rml.controls.RmlDataTypeControl#OnStepRendered(java.lang.Object, org.httprobot.common.events.RmlEventArgs)
	 */
	@Override
	public void OnStepRendered(Object sender, RmlEventArgs e) throws InconsistenMessageException, NotImplementedException
	{

	}	
}
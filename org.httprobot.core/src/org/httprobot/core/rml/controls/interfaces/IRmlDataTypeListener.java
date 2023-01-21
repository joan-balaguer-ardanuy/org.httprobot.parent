package org.httprobot.core.rml.controls.interfaces;

import org.httprobot.common.interfaces.IRmlListener;
import org.httprobot.core.rml.controls.datatypes.interfaces.IActionListener;
import org.httprobot.core.rml.controls.datatypes.interfaces.IDataViewListener;
import org.httprobot.core.rml.controls.datatypes.interfaces.IFieldListener;
import org.httprobot.core.rml.controls.datatypes.interfaces.IFieldRefListener;
import org.httprobot.core.rml.controls.datatypes.interfaces.IFieldsListener;
import org.httprobot.core.rml.controls.datatypes.interfaces.IRuleListener;
import org.httprobot.core.rml.controls.datatypes.interfaces.IRulesListener;
import org.httprobot.core.rml.controls.datatypes.interfaces.IServerInfoListener;
import org.httprobot.core.rml.controls.datatypes.interfaces.IStepListener;
import org.httprobot.core.rml.controls.datatypes.interfaces.IStepsListener;
import org.httprobot.core.rml.controls.datatypes.interfaces.IWebOptionsListener;


/**
 *  Implemented when reading RML DataTypes elements.
 *  The methods described below are executed when an RML DataType object is being read.
 *  @author Joan
 *
 */
public interface IRmlDataTypeListener 
	extends IRmlListener, IFieldListener, IFieldsListener, IRuleListener, 
	IRulesListener, IServerInfoListener, IStepListener, IStepsListener, 
	IDataViewListener, IActionListener, IFieldRefListener, IWebOptionsListener
{
	
}

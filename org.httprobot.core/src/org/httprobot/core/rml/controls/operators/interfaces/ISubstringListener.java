/**
 * 
 */
package org.httprobot.core.rml.controls.operators.interfaces;

import org.httprobot.common.events.RmlEventArgs;
import org.httprobot.common.exceptions.InconsistenMessageException;
import org.httprobot.common.exceptions.NotImplementedException;
import org.httprobot.common.interfaces.IRmlListener;

/**
 * @author Joan
 *
 */
public interface ISubstringListener extends IRmlListener
{
	/**
	 * When {@link ISubstringListener} {@link Rml} has been initialized.
	 * @param sender {@link Object}
	 * @param e {@link RmlEventArgs}
	 * @throws NotImplementedException when the implementation is already undone
	 * @throws InconsistenMessageException when message data doesn't match with corresponding controller.	
	 */
	public void OnSubstringInit(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException;
	/**
	 * When {@link ISubstringListener} {@link Rml} has been read.
	 * @param sender {@link Object}
	 * @param e {@link RmlEventArgs}
	 * @throws NotImplementedException when the implementation is already undone
	 * @throws InconsistenMessageException when message data doesn't match with corresponding controller.	 
	 */
	public void OnSubstringRead(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException;
	/**
	 * When {@link ISubstringListener} {@link Rml} has been loaded.
	 * @param sender {@link Object}
	 * @param e {@link RmlEventArgs}
	 * @throws NotImplementedException when the implementation is already undone
	 * @throws InconsistenMessageException when message data doesn't match with corresponding controller.	 
	 */
	public void OnSubstringLoaded(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException;
	/**
	 * When {@link ISubstringListener} {@link Rml} has been changed.
	 * @param sender {@link Object}
	 * @param e {@link RmlEventArgs}
	 * @throws NotImplementedException when the implementation is already undone
	 * @throws InconsistenMessageException when message data doesn't match with corresponding controller.	 
	 */
	public void OnSubstringChanged(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException;
	/**
	 * When {@link ISubstringListener} {@link Rml} has been rendered.
	 * @param sender {@link Object}
	 * @param e {@link RmlEventArgs}
	 * @throws NotImplementedException when the implementation is already undone
	 * @throws InconsistenMessageException when message data doesn't match with corresponding controller.	 
	 */
	public void OnSubstringRendered(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException;
	/**
	 * When {@link ISubstringListener} {@link Rml} has been written.
	 * @param sender {@link Object}
	 * @param e {@link RmlEventArgs}
	 * @throws NotImplementedException when the implementation is already undone
	 * @throws InconsistenMessageException when message data doesn't match with corresponding controller.	 
	 */
	public void OnSubstringWrite(Object sender, RmlEventArgs e) throws NotImplementedException, InconsistenMessageException;
}
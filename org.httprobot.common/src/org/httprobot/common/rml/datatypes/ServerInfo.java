package org.httprobot.common.rml.datatypes;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.httprobot.common.events.MarshallerEventArgs;
import org.httprobot.common.exceptions.InconsistenMessageException;
import org.httprobot.common.exceptions.NotImplementedException;
import org.httprobot.common.rml.Rml;

/**
 * @author Joan
*  Server information RML object class. Inherits {@link Rml}.
 */
@XmlRootElement(name="ServerInfo")
public class ServerInfo extends Rml
{
	/**
	 * 6784877235043676956L
	 */
	private static final long serialVersionUID = 6784877235043676956L;
	
	private String ServerName = null;
	private String ServerUrl = null;
	private String StartUrl = null;
	private String LastUpdate = null;
	private Integer timePeriod = null;
	private DataView DataView = null;
	private Steps Steps = null;
	
	/**
	 * ServerInfo class constructor
	 */
	public ServerInfo() { }	
	/**
	 * Gets the data view format
	 * @return {@link DataView} the data view 
	 */
	public DataView getDataView() {
		return DataView;
	}
	/**
	 * Gets the last update date
	 * @return {@link String} the date
	 */
	public String getLastUpdate() {
		return LastUpdate;
	}
	/**
	 * @return
	 */
	public String getServerName() {
		return ServerName;
	}
	/**
	 * @return
	 */
	public String getServerUrl() {
		return ServerUrl;
	}
	/**
	 * @return
	 */
	public String getStartUrl() {
		return StartUrl;
	}
	/**
	 * @return
	 */
	public Steps getSteps() {
		return Steps;
	}
	/**
	 * @param DataView
	 */
	@XmlElement
	public void setDataView(DataView DataView) {
		this.DataView = DataView;
	}
	/**
	 * @param LastUpdate
	 */
	@XmlElement
	public void setLastUpdate(String LastUpdate) {
		this.LastUpdate = LastUpdate;
	}
	/**
	 * @param ServerName
	 */
	@XmlElement
	public void setServerName(String ServerName) {
		this.ServerName = ServerName;
	}
	/**
	 * @param ServerUrl
	 */
	@XmlElement
	public void setServerUrl(String ServerUrl) {
		this.ServerUrl = ServerUrl;
	}	
	/**
	 * @param StartUrl
	 */
	@XmlElement
	public void setStartUrl(String StartUrl) {
		this.StartUrl = StartUrl;
	}
	/**
	 * @param Steps
	 */
	@XmlElement
	public void setSteps(Steps Steps) {
		this.Steps = Steps;
	}
	/* (non-Javadoc)
	 * @see org.httprobot.common.rml.Rml#OnObjectUnmarshalled(java.lang.Object, org.httprobot.common.events.MarshallerEventArgs)
	 */
	@Override
	public void OnObjectUnmarshalled(Object sender, MarshallerEventArgs e) throws NotImplementedException, InconsistenMessageException 
	{
		super.OnObjectUnmarshalled(sender, e);
		setDataView(((ServerInfo)e.getRml()).getDataView());
		setLastUpdate(((ServerInfo)e.getRml()).getLastUpdate());
		setServerName(((ServerInfo)e.getRml()).getServerName());
		setServerUrl(((ServerInfo)e.getRml()).getServerUrl());
		setStartUrl(((ServerInfo)e.getRml()).getStartUrl());
		setSteps(((ServerInfo)e.getRml()).getSteps());
	}
	@XmlAttribute
	public void setTimePeriod(Integer timePeriod) {
		this.timePeriod = timePeriod;
	}
	public Integer getTimePeriod() {
		return timePeriod;
	}
}
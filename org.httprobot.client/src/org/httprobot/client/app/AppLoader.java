package org.httprobot.client.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.UUID;

import javax.xml.bind.JAXBException;

import org.httprobot.client.managers.UiManager;
import org.httprobot.common.definitions.Enums.CliNamespace;
import org.httprobot.common.definitions.Enums.RuntimeOptions;
import org.httprobot.common.events.CliEventArgs;
import org.httprobot.common.events.UiEventArgs;
import org.httprobot.common.interfaces.IUiListener;
import org.httprobot.common.rml.config.AppConfig;
import org.httprobot.common.rml.config.Config;
import org.httprobot.core.data.Data;
import org.httprobot.core.events.InetEventArgs;
import org.httprobot.core.events.ProgramDataEventArgs;
import org.httprobot.core.events.RequesterEventArgs;
import org.httprobot.core.interfaces.IDataListener;
import org.httprobot.core.interfaces.IInetListener;
import org.httprobot.core.interfaces.IRequesterListener;
import org.httprobot.core.managers.InetManager;
import org.httprobot.core.requester.Requester;
import org.w3c.dom.events.Event;

/**
 * Application loader class. 
 * Is {@link IDataListener}, 
 * {@link IConfigListener}, {@link IInetListener}, 
 * {@link IUiListener} and {@link IRmlManagerListener}.
 * @author Joan
 */
public class AppLoader 
	implements IDataListener, 
				IInetListener,
				IUiListener,
				IRequesterListener
{
	private static final CliNamespace cliNamespace = CliNamespace.CLIENT;
	
	AppConfig app_config;
	Config config;
	Data program_data;	
	InetManager inet_manager;
	UiManager ui_manager;
	EnumSet<RuntimeOptions> options = RuntimeOptions.FULL_DEBUG;
	String destinationPath;
	/**
	 * Application loader constructor.
	 */
	public AppLoader(EnumSet<RuntimeOptions> options, String path)
	{		
		LoadAppConfigFile(path);
		LoadConfigFile(this.app_config.getConfigFilePath());
		
		if(!this.config.getInherited())
		{
			this.options = this.config.getRuntimeOptions();
		}
		else
		{
			this.options = options;
		}
		
		this.program_data = new Data(this);

		ArrayList<Config> requesters_configs = new ArrayList<Config>();
		requesters_configs.add(this.config);
		
		Requester requester = new Requester(this);
		requester.addConfiguration(config);		
		requester.start();
		
//		if(this.program_data.getServers_infos().size() > 0)
//		{
//			String serv_list = this.program_data.getServers_infos().get(0).getSteps().toString();
//			this.ui_manager = new UiManager(this, config, serv_list);
//			this.ui_manager.start();
//		}
	}
	/**
	 * Gets application's configuration.
	 * @return {@link Config}
	 */
	public Config getApplication_config() 
	{
		return config;
	}	
	/* (non-Javadoc)
	 * @see org.httprobot.common.interfaces.IListener#getCliNamespace()
	 */
	@Override
	public CliNamespace getCliNamespace() 
	{
		return cliNamespace;
	}
	/* (non-Javadoc)
	 * @see org.httprobot.common.interfaces.IListener#getCliOptions()
	 */
	@Override
	public EnumSet<RuntimeOptions> getRuntimeOptions() 
	{
		return options;
	}
	/* (non-Javadoc)
	 * @see org.httprobot.common.interfaces.IListener#getDestinationPath()
	 */
	@Override
	public String getDestinationPath()
	{
		return null;
	}
	/**
	 * Gets Internet manager.
	 * @return {@link InetManager}
	 */
	public InetManager getInet_manager() 
	{
		return inet_manager;
	}	
	/**
	 * Gets the data used by the application.
	 * @return {@link Data}
	 */
	public Data getProgram_data() 
	{
		return program_data;
	}
	/**
	 * Gets UI manager.
	 * @return {@link UiManager}
	 */
	public UiManager getUi_manager() 
	{
		return ui_manager;
	}
	/* (non-Javadoc)
	 * @see org.w3c.dom.events.EventListener#handleEvent(org.w3c.dom.events.Event)
	 */
	@Override
	public void handleEvent(Event arg0) 
	{
		
	}
	/**
	 * Load application configuration file.
	 * @param path
	 */
	private void LoadAppConfigFile(String path) 
	{
		this.app_config = new AppConfig();
		this.app_config.setDestinationPath(path);
		
		File file = new File(path);		
		InputStream is;
		
		try 
		{
			is = new FileInputStream(file);
			this.app_config.unmarshal(is);
		} 
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		} 
		catch (JAXBException e) 
		{
			e.printStackTrace();
		}
	}
	/**
	 * Load a configuration file
	 * @param path
	 */
	private void LoadConfigFile(String path) 
	{
		this.config = new Config();
		this.config.setDestinationPath(path);
		
		File file = new File(path);		
		InputStream is;
		
		try 
		{
			is = new FileInputStream(file);
			this.config.unmarshal(is);
		} 
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		} 
		catch (JAXBException e) 
		{
			e.printStackTrace();
		}
	}
	@Override
	public void OnCommandInput(Object sender, CliEventArgs e) 
	{
		switch (e.getCmd()) 
		{
			case RUN_WEB_REQUEST:
				
				break;
	
			default:
				break;
		}
		
	}
	/* (non-Javadoc)
	 * @see org.httprobot.common.interfaces.IListener#OnManagerCommand(java.lang.Object, org.httprobot.common.events.CommandEventArgs)
	 */
	@Override
	public void OnCommandOutput(Object sender, CliEventArgs e) 
	{
		switch(e.getCmd())
		{
			case RUN_WEB_REQUEST:
				//Action action = Action.class.cast(e.getSource());
				//call Internet manager to request HTTP address
				break;
			case TREAT_WEB_RESPONSE:
				//call RML manager to treat current HTTP response
				break;
			default:
				break;
		}
	}
	/* (non-Javadoc)
	 * @see org.httprobot.common.interfaces.IProgramDataListener#OnProgramDataChanged(java.lang.Object, org.httprobot.common.events.ProgramDataEventArgs)
	 */
	@Override
	public void OnProgramDataChanged(Object sender, ProgramDataEventArgs e)
	{
		switch (e.getPdet()) 
		{
			case SERVERS_INFO:
				break;

			case XML_CONFIG:
				//user has changed configuration
				break;

			default:
				break;
		}	
	}
	/* (non-Javadoc)
	 * @see org.httprobot.common.interfaces.IUiListener#OnUiChanged(java.lang.Object, org.httprobot.common.events.UiEventArgs)
	 */
	@Override
	public void OnUiChanged(Object sender, UiEventArgs e) 
	{
		switch(e.getUiet())
		{
			case OPEN:
				break;
			case EXIT:
				System.exit(0);
				break;
			default:
				break;
		}		
	}
	/* (non-Javadoc)
	 * @see org.httprobot.common.interfaces.IListener#setCliOptions(java.util.EnumSet)
	 */
	@Override
	public void setRuntimeOptions(EnumSet<RuntimeOptions> options) 
	{
		this.options = options;		
	}
	/* (non-Javadoc)
	 * @see org.httprobot.common.interfaces.IListener#setDestinationPath(java.lang.String)
	 */
	@Override
	public void setDestinationPath(String destinationPath) 
	{
		
	}

	@Override
	public void OnServerInfoStarted(Object sender, InetEventArgs e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void OnServerInfoStopped(Object sender, InetEventArgs e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public UUID getUuid() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void setUuid(UUID uuid) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void OnServerInfoError(Object sender, InetEventArgs e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnServerInfoFinished(Object sender, InetEventArgs e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void OnDataRowCaptured(Object sender, RequesterEventArgs e) {
		// TODO Auto-generated method stub
		
	}
}

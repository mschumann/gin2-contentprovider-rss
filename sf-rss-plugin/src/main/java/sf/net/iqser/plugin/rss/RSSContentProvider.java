package sf.net.iqser.plugin.rss;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jperdian.rss2.RssClient;
import org.jperdian.rss2.RssException;
import org.jperdian.rss2.dom.RssChannel;
import org.jperdian.rss2.dom.RssItem;

import com.iqser.core.exception.IQserException;
import com.iqser.core.exception.IQserRuntimeException;
import com.iqser.core.model.Attribute;
import com.iqser.core.model.Content;
import com.iqser.core.model.Parameter;
import com.iqser.core.plugin.provider.AbstractContentProvider;

public class RSSContentProvider extends AbstractContentProvider {

	private static Logger logger = Logger.getLogger( RSSContentProvider.class );

	private String type;
	private String url;
	
	@Override
	public void init() {
		
		this.type = getInitParams().getProperty("type");
		if(StringUtils.isEmpty(type)){
			throw new IQserRuntimeException("Empty content type");
		}

		this.url = getInitParams().getProperty("url");
		if(StringUtils.isEmpty(url)){
			throw new IQserRuntimeException("Empty url");
		}
	}

	@Override
	public void destroy() {
		
	}

	@Override
	public void doSynchronization() {	
	
		try {
			URL url = new URL(this.url);

			RssClient rssClient = new RssClient(url);
			RssChannel rssChannel = rssClient.getData();
			
			Iterator iter = rssChannel.getItemList().iterator();
			
			while (iter.hasNext()) {
				RssItem rssItem = (RssItem) iter.next();
				String contentUrl = rssItem.getGuid().getGuid();
				
				if (!isExistingContent(contentUrl)) {
					addContent(createContent(contentUrl));
				}
			}
		} catch (MalformedURLException e) {
			logger.error("Couldn't create url: " + e.getLocalizedMessage());
		} catch (RssException e) {
			logger.error("Couldn't parse rss: " + e.getLocalizedMessage());
		} catch (IQserException e) {
			logger.error("Couldn't access repository: " + e.getLocalizedMessage());
		}
	}

	@Override
	public void doHousekeeping() {
		// Do nothing. Delete repository manually instead.
	}


	@Override
	public Content createContent(String contentUrl) 
	{
		Content c = new Content();
		
		c.setProvider(getName());
		c.setContentUrl(contentUrl);
		c.setType(type);
		
		try {
			URL url = new URL(this.url);

			RssClient rssClient = new RssClient(url);
			RssChannel rssChannel = rssClient.getData();
			
			Iterator iter = rssChannel.getItemList().iterator();
			
			while (iter.hasNext()) {
				RssItem rssItem = (RssItem) iter.next();

				if (rssItem.getGuid().getGuid().equalsIgnoreCase(contentUrl)) {
					c.setModificationDate(rssItem.getPubDate().getTime());
					c.setFulltext(rssItem.getDescription());
					
					if (rssItem.getAuthor() != null && rssItem.getAuthor().length() > 0) {
						c.addAttribute(new Attribute("AUTHOR", rssItem.getAuthor(), Attribute.ATTRIBUTE_TYPE_TEXT, true));
					}
					
					if (rssItem.getTitle() != null && rssItem.getTitle().length() > 0) {
						c.addAttribute(new Attribute("TITLE", rssItem.getTitle(), Attribute.ATTRIBUTE_TYPE_TEXT, true));
					}
					
					if (rssItem.getComments() != null) {
						c.addAttribute(new Attribute("COMMENT", rssItem.getComments().toString(), Attribute.ATTRIBUTE_TYPE_TEXT, false));
					}
					
					if (rssItem.getLink() != null) {
						c.addAttribute(new Attribute("LINK", rssItem.getLink().toString(), Attribute.ATTRIBUTE_TYPE_TEXT, true));
					}
					
					if (rssItem.getEnclosure() != null) {
						c.addAttribute(new Attribute("ENCLOSURE", rssItem.getEnclosure().toString(), Attribute.ATTRIBUTE_TYPE_TEXT, false));
					}
					
					if (rssItem.getSource() != null) {
						c.addAttribute(new Attribute("SOURCE", rssItem.getSource().getTitle(), Attribute.ATTRIBUTE_TYPE_TEXT, false));
					}
					
					if (rssItem.getCategoryList() != null) {
						Iterator catIter = rssItem.getCategoryList().iterator();
						
						while (catIter.hasNext()) {
							Object o = catIter.next();
							
							if (o.getClass().isInstance(String.class)) {
								c.addAttribute(new Attribute("CATEGORY", (String)o, Attribute.ATTRIBUTE_TYPE_TEXT, true)); 
							}
						}
					}
					
					return c;
				}
			}
		} catch (MalformedURLException e) {
			logger.error("Couldn't create url: " + e.getLocalizedMessage());
		} catch (RssException e) {
			logger.error("Couldn't parse rss: " + e.getLocalizedMessage());
		} 
		
		logger.warn("Couldn't find the object in the rss-source");
		
		return c;
	}

	public byte[] getBinaryData(Content arg0) {
		return null;
	}

	@Override
	public Collection<String> getActions(Content arg0) {
		return null;
	}


	@Override
	public Content createContent(InputStream arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void performAction(String arg0, Collection<Parameter> arg1, Content arg2) {
		// TODO Auto-generated method stub
		
	}
}

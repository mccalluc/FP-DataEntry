package org.filteredpush.dataentry.configuration;

import java.io.InputStream;

import org.filteredpush.dataentry.Utils;
import org.w3c.dom.Document;

public class MockConfiguration extends Configuration {

	static private Document doc;
	
	static {
		InputStream stream = MockConfiguration.class.getResourceAsStream("/mock-configuration.xml");
		doc = Utils.parseXml(stream);
	}
	
	private int port;
	
	public MockConfiguration(int port) {
		super(doc);
		this.port = port;
	}
	
	@Override
	public int getPort() {
		return port;
	}
	
}

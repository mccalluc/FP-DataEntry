package org.filteredpush.dataentry.servlet;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.filteredpush.dataentry.BothEndsHandler;
import org.filteredpush.dataentry.CliOptionsParser;
import org.filteredpush.dataentry.configuration.Configuration;

public class BothEndsServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private BothEndsHandler handler;

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		Configuration config = new CliOptionsParser()
			.parseOrExit(new String[]{servletConfig.getInitParameter("configFilePath")}); 
			// Config is baked into .war: acceptable, but there may be a better solution for the long run. 
		handler = new BothEndsHandler(config);
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		handler.handle(null, new Request(), request, response);
	}
	
	@Override
	public void destroy() {
		handler.destroy();
	}
}
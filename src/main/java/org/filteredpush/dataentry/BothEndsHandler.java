package org.filteredpush.dataentry;

import java.io.IOException;
import java.net.BindException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.filteredpush.dataentry.backend.BackEndHandler;
import org.filteredpush.dataentry.configuration.UnionConfiguration;
import org.filteredpush.dataentry.enums.EnumUtils;
import org.filteredpush.dataentry.frontend.FrontEndHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BothEndsHandler extends AbstractHandler {
	
	private static Logger log = LoggerFactory.getLogger(BothEndsHandler.class);

	private FrontEndHandler frontEnd;
	private BackEndHandler backEnd;
	
	public static final String BACK_END_PATH = "back-end/";
	
	public BothEndsHandler(UnionConfiguration config) {
		EnumUtils.clearEnums();
		EnumUtils.setEnums(config);
		// TODO: not confident that this is the right place for this.
		
		frontEnd = new FrontEndHandler(config);
		backEnd = new BackEndHandler(config);
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		String pathInfo = request.getPathInfo();

//		log.debug("RequestURI: "+request.getRequestURI());
//		log.debug("ContextPath: "+request.getContextPath());
//		log.debug("PathInfo: "+request.getPathInfo());
//		log.debug("ServletPath: "+request.getServletPath());
		
		if (pathInfo.equals("/"+BACK_END_PATH)) {
			backEnd.handle(target, baseRequest, request, response);
		} else {
			frontEnd.handle(target, baseRequest, request, response);
		}
	}
	
	protected static Server createServer(BothEndsHandler handler, int port) {
		Server server = new Server(port);
		server.setHandler(handler);
		log.info("Both-ends server created for port "+port);
		return server;
	}
	
	public static Server createServer(UnionConfiguration config) {
		return createServer(new BothEndsHandler(config), config.getPort());
	}

	public static void main(String... args) throws Exception {
		UnionConfiguration config = new CliOptionsParser( //
				"Starts an HTTP server which handles both the back end and the front end.")
			.parseOrExit(args);
		int port = config.getPort();
		Server server = createServer(config);
		try {
			server.start();
			log.info("Server started on port "+port);
		} catch (BindException e) {
			log.error("Port "+port+" already occupied. Perhaps 'pkill -f FP-DataEntry'?");
			System.exit(1);
		}
	}
}

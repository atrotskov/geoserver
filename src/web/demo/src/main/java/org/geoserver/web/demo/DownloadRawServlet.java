package org.geoserver.web.demo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.Application;
import org.apache.wicket.Page;
import org.apache.wicket.RuntimeConfigurationType;
import org.apache.wicket.Session;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.geoserver.catalog.Catalog;
import org.geoserver.config.GeoServerDataDirectory;
import org.geoserver.web.GeoServerApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

@WebServlet("/dnldraw")
public class DownloadRawServlet extends HttpServlet {
	@Autowired			
	private ApplicationContext myAppContext;
	
	

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		final String PARAMETER = "csId";

		String coverageStoreId = request.getParameter(PARAMETER);
		String appKey = request.getParameter("ak");
		
		if (coverageStoreId != null) {
					
			GeoServerApplication app = (GeoServerApplication) GeoServerApplication.get(appKey);
			
			GeoServerDataDirectory dataDir = app.getBeanOfType(GeoServerDataDirectory.class);
			String geoServerDataDir = dataDir.root().getAbsolutePath();
			geoServerDataDir = geoServerDataDir.replace("\\", "/");
			
			Catalog catalog = app.getCatalog();
			String absPath = catalog.getCoverageStore(coverageStoreId).getURL();
			
			if (absPath.startsWith("file://")) {
				absPath = absPath.substring(7);
			} else {
				absPath = absPath.substring(5);
			}
			
			
						
			String fullPath = geoServerDataDir + "/" + absPath;
			System.out.println(fullPath);
			
			File downloadFile = new File(fullPath);
			FileInputStream inStream = new FileInputStream(downloadFile);
			
			ServletContext context = getServletContext();
			
			response.setContentType("application/octet-stream");
			response.setContentLength((int) downloadFile.length());
			
			String headerKey = "Content-Disposition";
	        String headerValue = String.format("attachment; filename=\"%s\"", downloadFile.getName());
	        response.setHeader(headerKey, headerValue);
	        
	        OutputStream outStream = response.getOutputStream();
	        
	        byte[] buffer = new byte[4096];
	        int bytesRead = -1;
	         
	        while ((bytesRead = inStream.read(buffer)) != -1) {
	            outStream.write(buffer, 0, bytesRead);
	        }
	         
	        inStream.close();
	        outStream.close();
	        
		} else {
			response.sendError(403);
		}

	}

}

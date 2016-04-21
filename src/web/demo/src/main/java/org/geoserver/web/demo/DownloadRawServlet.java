package org.geoserver.web.demo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.geoserver.catalog.CoverageStoreInfo;
import org.geoserver.config.GeoServerDataDirectory;
import org.geoserver.web.GeoServerApplication;

@WebServlet("/dnldraw")
public class DownloadRawServlet extends HttpServlet {
	private static final String PARAMETER_COV_STORE_ID = "csId";
	private static final String PARAMETER_APP_KEY_ID = "ak";
	private static final String TEXT_RESPONSE_FILE_NOT_FOUND = "<h2>Ooops, looks like server can't find specified file.</h2>";
	private static final String TEXT_RESPONSE_LINK_IS_CORRUPTED = "Look's like link is corrupted.";
	private static final String TEXT_RESPONSE_SERVER_RESTARTED =
			"Ooops, Look's like server was restarted. Please relogin and try again.";
	
	
	public File getFile(String geoServerDataDir, String fileFromXml) throws FileNotFoundException {
		
		// Replace path divider from "\" to "/" if need
		geoServerDataDir = geoServerDataDir.replace("\\", "/");
		fileFromXml = fileFromXml.replace("\\", "/");
		
		// Trim word "file://" or "file:" from getting URL
		if (fileFromXml.startsWith("file://")) {
			fileFromXml = fileFromXml.substring(7);
		} else {
			fileFromXml = fileFromXml.substring(5);
		}
				
		String fullPath = geoServerDataDir + "/" + fileFromXml;
						
		File file = new File(fullPath);
		
		if(!file.exists()) {
			file = new File(fileFromXml);
		}
		if (!file.exists()) {
			String e = "<div>1st) tried use this location: " + fileFromXml +
					"</div><div>2nd) tried use this location: " + fullPath + "</div>";
			throw new FileNotFoundException(e);
		}
		
		return file;
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String coverageStoreId = request.getParameter(PARAMETER_COV_STORE_ID);
		String appKey = request.getParameter(PARAMETER_APP_KEY_ID);

		if (coverageStoreId != null && appKey != null) {

			GeoServerApplication app = (GeoServerApplication) GeoServerApplication.get(appKey);
			FileInputStream inputStrem = null;
			OutputStream outputStream = null;
			try {
			if (app == null) {
				throw new LinkContainException(TEXT_RESPONSE_SERVER_RESTARTED);
			}
			
			CoverageStoreInfo covStroreInfo = app.getCatalog().getCoverageStore(coverageStoreId);
			if (covStroreInfo == null) {
				throw new LinkContainException(TEXT_RESPONSE_LINK_IS_CORRUPTED);
			}

			// Get location data directory for all GeoServer
			GeoServerDataDirectory dataDir = app.getBeanOfType(GeoServerDataDirectory.class);
			String geoServerDataDir = dataDir.root().getAbsolutePath();

			// Get location source file from XML
			String fileFromXml = covStroreInfo.getURL();
			
			
				File downloadFile = this.getFile(geoServerDataDir, fileFromXml);
				inputStrem = new FileInputStream(downloadFile);
				response.setContentType("application/octet-stream");
				response.setContentLength((int) downloadFile.length());

				String headerKey = "Content-Disposition";
				String headerValue = String.format("attachment; filename=\"%s\"", downloadFile.getName());
				response.setHeader(headerKey, headerValue);

				outputStream = response.getOutputStream();

				byte[] buffer = new byte[4096];
				int bytesRead = -1;

				while ((bytesRead = inputStrem.read(buffer)) != -1) {
					outputStream.write(buffer, 0, bytesRead);
				}
			} catch (FileNotFoundException e) {
				response.getWriter().write(TEXT_RESPONSE_FILE_NOT_FOUND + "<h4>" + e + "</h4>");
			} catch (IOException e) {
				response.getWriter().write(TEXT_RESPONSE_FILE_NOT_FOUND + "<h4>" + e + "</h4>");
			} catch (LinkContainException e) {
				response.getWriter().write(e.toString());
			}
			finally {
				if (inputStrem != null) {
					inputStrem.close();
				}
				if (outputStream != null) {
					outputStream.close();
				}
			}

		} else {
			response.getWriter().write(TEXT_RESPONSE_LINK_IS_CORRUPTED);
		}

	}

}

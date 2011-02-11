package com.test.cc1;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.log.Log;

import com.beoui.geocell.BoundingBox;
import com.beoui.geocell.GeocellManager;
import com.beoui.geocell.GeocellQuery;
import com.beoui.geocell.Point;

@SuppressWarnings("serial")
public class Cc1serverServlet extends HttpServlet {
	private static final Logger log = Logger.getLogger(Cc1serverServlet.class.getName());
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
        String content = ""; //this will be sent back to the client, but first it is used to hold any incoming message from the client
        String msgQtemp = ""; //this will be from the server to string "content" defined above
        List<String> msgQreturn = null; //this will be from the item in the datastore to the server
        long newkey = 0;
        resp.setContentType("text/plain");
        
        Long inkey = Long.valueOf(req.getParameter("key"));
        String username = req.getParameter("username");
    	Double lat = Double.valueOf(req.getParameter("latitude"));
    	log.info(String.valueOf(lat) + " " + username + "key: " + inkey);
    	Double lon = Double.valueOf(req.getParameter("longitude"));
    	Point p = new Point(lat, lon);
    	
        List<String> cells = GeocellManager.generateGeoCell(p);

    	if (req.getParameterMap().containsKey("content")) //check if the post has content
            content = req.getParameter("content");
    	
    	Item e = new Item(); //this will either be created or retrieved
    	PersistenceManager pm = PMF.get().getPersistenceManager();
    	try {
    		//Key k = KeyFactory.createKey(Item.class.getSimpleName(), username);
    		//long klong = k.getId();
    		//log.info("fuckin key is " + String.valueOf(klong));
    		//klong += 1;
    		if (inkey != 0) {
    			try {
    			e = pm.getObjectById(Item.class, inkey);
    			}
    			catch (JDOObjectNotFoundException err)
    			{
    				log.warning("object not found error with key " + inkey);
    			}
    		}
    		else { //throws any time we haven't added said user yet:
    			e = new Item(username, content);
    		}
			//if it already exists, update the location
			e.setLatitude(lat);
			e.setLongitude(lon);
			e.setGeocells(cells);
			msgQreturn = e.getMsgQ();
			if ((!content.equals("NONE")) && (!content.equals(""))) {
				//todo: query on nearby peeps
				//for all returned ids
				//get item, add content to msgQ
				
				//testing:
				testHowToQueryOnABoundingBox(pm);
				///testing
				
				Point center = new Point(lat, lon);
		        List<Object> itemParams = new ArrayList<Object>();
		        itemParams.add("John");
		        GeocellQuery baseQuery = new GeocellQuery("lastName == lastNameParam", "String lastNameParam", itemParams);

		        List<Item> itemReturns = null;
		        try {
		            itemReturns = GeocellManager.proximityFetch(center, 40, 0, Item.class, baseQuery, pm);
		        } catch (Exception e2) {
		        }
		        if (itemReturns != null) {
		        	int lenRet = itemReturns.size();
		        	for (int i=0; i < lenRet; i++) {
		        		itemReturns.get(i).addtoMsgQ(content);
		        	}
		        }
				 /*
				//testing here:
				Key testkey = KeyFactory.createKey(Item.class.getSimpleName(), "Eli3");
				Item e2 = pm.getObjectById(Item.class, testkey);
				e2.addtoMsgQ(content);
				// end testing
				*/
			}
			if ((msgQreturn != null) && (!msgQreturn.isEmpty())) { //if there are message waiting in the queue 
				int lenQ = msgQreturn.size();
				for (int i=0; i < lenQ; i++) { //iterate through and make a big string of them - is there a faster/better implementation here?
					msgQtemp = msgQtemp.concat(msgQreturn.get(i));
					msgQtemp = msgQtemp.concat("|");
				}
				log.info("msgq: " + msgQtemp);
				msgQreturn.clear(); //hopefully we've gotten all the messages out in order
				e.setMsgQ(msgQreturn); //set the clear queue back to the item
				content = msgQtemp; //wanted this line to be below, move it later
			}
			else {
				content = "";
			}
	        // Show in the log what cells are going to be saved
	        log.info("Geocells to be saved for Point("+lat+","+lon+") are: "+cells);
			pm.makePersistent(e);
			if (inkey == 0) {
				newkey = e.getId(); //used to be key
    	    	e.setId(newkey);
    	    	log.info("new key is! " + newkey);
			}
    	} finally {
    		pm.close();
    	}
    	if (newkey != 0) { //send the new key back, just the first time
    		String firstkey = String.valueOf(newkey);
    		firstkey = firstkey.concat("~");
    		firstkey = firstkey.concat(content);
    		content = firstkey;
    	}
    	log.info("response going back: " + content);
        resp.getWriter().print(content);
	}
	
	 @SuppressWarnings("unchecked")
	public void testHowToQueryOnABoundingBox(PersistenceManager pm) {
	        // Incoming data: latitude and longitude of south-west and north-east points (around Bordeaux for instance =) )
	        double latS = 50.0;
	        double latN = 60.0;
	        
	        double lonW = -115.0;
	        double lonE = -125.0;

	        // Transform this to a bounding box
	        BoundingBox bb = new BoundingBox(latN, lonE, latS, lonW);

	        // Calculate the geocells list to be used in the queries (optimize list of cells that complete the given bounding box)
	        List<String> cells = GeocellManager.bestBboxSearchCells(bb, null);

	        String queryString = "select from " + Item.class.getName() + " where geocellsParameter.contains(geocells)";
            Query query = pm.newQuery(queryString);
	        query.declareParameters("String geocellsParameter");
	        List<Item> objects = (List<Item>) query.execute(cells);
	        
	        for (Item i : objects) {
	        	log.info("obj!: " + i.getLatitude());
	        }
	        
	        // Show in the log what cells shoud be used in the query
	        log.info("Geocells to use in query for PointSW("+latS+","+lonW+") ; PointNE("+latN+","+lonE+") are: "+cells);
	    }


}

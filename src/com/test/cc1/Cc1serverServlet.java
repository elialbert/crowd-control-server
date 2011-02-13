package com.test.cc1;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
        String content = ""; //used to hold any incoming message from the client
        String returnContent = ""; //this will be sent back to the client
        String msgQtemp = ""; //this will be from the server to string "content" defined above
        List<String> msgQreturn = null; //this will be from the item in the datastore to the server
        long newkey = 0;
        long radius = -1;
        long radUse = 0;
        resp.setContentType("text/plain");
        
        Long inkey = Long.valueOf(req.getParameter("key"));
        String username = req.getParameter("username");
    	Double lat = Double.valueOf(req.getParameter("latitude"));
    	log.info(String.valueOf(lat) + " " + username + " key: " + inkey);
    	Double lon = Double.valueOf(req.getParameter("longitude"));
    	Point p = new Point(lat, lon);
    	    	
    	//get a timestamp
    	//DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    	Date createDate = new Date();
    	
    	
        List<String> cells = GeocellManager.generateGeoCell(p);

    	if (req.getParameterMap().containsKey("content")) {//check if the post has content
            content = req.getParameter("content");
    		log.info("content is: " + content);
    	}
    	if (req.getParameterMap().containsKey("radius"))
    		radius = Long.valueOf(req.getParameter("radius"));

    	
    	Item e = new Item(); //this will either be created or retrieved
    	PersistenceManager pm = PMF.get().getPersistenceManager();
    	try { //the following try block works only on the current client's item
    		javax.jdo.Transaction tx = pm.currentTransaction();
	        try {
    		tx.begin();
    		if (inkey != 0) { //the user's Item reference exists, get it from the db
    			try {
    			e = pm.getObjectById(Item.class, inkey);
    			}catch (JDOObjectNotFoundException err){log.warning("object not found error with key " + inkey);}
    		}
    		else { //throws any time we haven't added said user yet:
    			e = new Item(username, content);
    			if (radius == -1)
    				e.setRadius((long)(50));
    		}
			//if it already exists, update the location
			e.setLatitude(lat);
			e.setLongitude(lon);
			e.setGeocells(cells);
			e.setCreateDate(createDate); //update the date every time we fetch the entry
			if (radius != -1) {
				e.setRadius(radius);
				log.info("setting radius to " + radius);
			}
			
			//give any waiting messages back to the client
			msgQreturn = e.getMsgQ();
			if ((msgQreturn != null) && (!msgQreturn.isEmpty())) { //if there are message waiting in the queue 
				int lenQ = msgQreturn.size();
				for (int i=0; i < lenQ; i++) { //iterate through and make a big string of them - is there a faster/better implementation here?
					msgQtemp = msgQtemp.concat(msgQreturn.get(i));
					msgQtemp = msgQtemp.concat("|");
				}
				log.info("msgq: " + msgQtemp);
				msgQreturn.clear(); //hopefully we've gotten all the messages out in order
				e.setMsgQ(msgQreturn); //set the clear queue back to the item
				returnContent = msgQtemp; //wanted this line to be below, move it later?
			}
			else {
				returnContent = "";
			}
			radUse = e.getRadius();
			//write to the client's item
			pm.makePersistent(e);
	        tx.commit();
	        }
	        catch (Exception err) {
			    if (tx.isActive()){
			        tx.rollback();}
			 }
			//if client is a new user, set the key
			if (inkey == 0) {
				newkey = e.getId(); //used to be key
				inkey = newkey;
		    	log.info("new key is! " + newkey);
			}
		
			if ((!content.equals("NONE")) && (!content.equals(""))) { //if the client is also sending a message, add it to nearby msgqueues
				Point center = new Point(lat, lon);
		        List<Object> itemParams = new ArrayList<Object>();
		        itemParams.add(1);
		        GeocellQuery baseQuery = new GeocellQuery("always == alwaysParam", "int alwaysParam", itemParams);
		        
		        List<Item> itemReturns = null;
		        try {
		            itemReturns = GeocellManager.proximityFetch(center, 100, radUse, Item.class, baseQuery, pm);
		        } catch (Exception e2) {
		        	log.warning(e2.getMessage());
		        }
		        if (itemReturns != null) {
		        	int lenRet = itemReturns.size();
		        	for (int i=0; i < lenRet; i++) {
		        		//TODO! Radius checking (need the actual proximity of each item)
		        		javax.jdo.Transaction tx2 = pm.currentTransaction();
		        		try {
			        		
	        		        tx2.begin();
	        		        Item tempReturn = itemReturns.get(i);
	        		        if (tempReturn.getId() != inkey) //we don't want to add the message to the one who sent it
	        		        	tempReturn.addtoMsgQ(username + ": " + content);
	            			pm.makePersistent(tempReturn);
	        		        tx2.commit();
	        		        log.info("RETURNCHECK! " + tempReturn.getUsername());
		        		}
		        		catch (Exception err) {
	    				    if (tx2.isActive()){
	    				        tx2.rollback();}
	    				 }
		        	}
		        }
			}
    	}
	    finally {
	    	pm.close(); //do we want to close here?
	    }
    	
    	if (newkey != 0) { //send the new key back, just the first time
    		String firstkey = String.valueOf(newkey);
    		firstkey = firstkey.concat("~");
    		firstkey = firstkey.concat(content);
    		returnContent = firstkey;
    	}
    	log.info("response going back: " + returnContent);
        resp.getWriter().print(returnContent);
	}
	
	@SuppressWarnings("unchecked")
	public void testHowToQueryOnABoundingBox(PersistenceManager pm) {
	        // Incoming data: latitude and longitude of south-west and north-east points (around Bordeaux for instance =) )
	        double latS = 10.0;
	        double latN = 20.0;
	        
	        double lonW = -15.0;
	        double lonE = -12.0;

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

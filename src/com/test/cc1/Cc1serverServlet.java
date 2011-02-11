package com.test.cc1;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.beoui.geocell.GeocellManager;
import com.beoui.geocell.Point;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

@SuppressWarnings("serial")
public class Cc1serverServlet extends HttpServlet {
	public long counter = 0;
	private static final Logger log = Logger.getLogger(Cc1serverServlet.class.getName());
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
        String content = ""; //this will be sent back to the client, but first it is used to hold any incoming message from the client
        String msgQtemp = ""; //this will be from the server to string "content" defined above
        List<String> msgQreturn = null; //this will be from the item in the datastore to the server
        resp.setContentType("text/plain");
        
        String username = req.getParameter("username");
    	Double lat = Double.valueOf(req.getParameter("latitude"));
    	log.info(String.valueOf(lat) + " " + username);
    	Double lon = Double.valueOf(req.getParameter("longitude"));
    	Point p = new Point(lat, lon);
    	
        List<String> cells = GeocellManager.generateGeoCell(p);

    	if (req.getParameterMap().containsKey("content")) //check if the post has content
            content = req.getParameter("content");
    	
    	Item e = new Item(); //this will either be created or retrieved
    	PersistenceManager pm = PMF.get().getPersistenceManager();
    	try {
    		Key k = KeyFactory.createKey(Item.class.getSimpleName(), username);
    		try {
    			e = pm.getObjectById(Item.class, k);
    		}
    		catch (JDOObjectNotFoundException err) { //throws any time we haven't added said user yet:
    			e = new Item(username, content);
    	    	e.setId(k);
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
				
				//testing here:
				Key testkey = KeyFactory.createKey(Item.class.getSimpleName(), "Eli3");
				Item e2 = pm.getObjectById(Item.class, testkey);
				e2.addtoMsgQ(content);
				// end testing
			}
			if ((msgQreturn != null) && (!msgQreturn.isEmpty())) { //if there are message waiting in the queue 
				int lenQ = msgQreturn.size();
				for (int i=0; i < lenQ; i++) { //iterate through and make a big string of them - is there a faster/better implementation here?
					log.info("initer: " + msgQreturn.get(i));
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
    	} finally {
    		pm.close();
    	}
    	
    	log.info("response going back: " + content);
        resp.getWriter().print(content);
	}
}

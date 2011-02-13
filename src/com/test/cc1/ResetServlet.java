package com.test.cc1;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.ServletException;
import javax.servlet.http.*;


@SuppressWarnings("serial")
public class ResetServlet extends HttpServlet {
 private static final Logger _logger = Logger.getLogger(ResetServlet.class.getName());
 @SuppressWarnings({ "unchecked"})
public void doGet(HttpServletRequest req, HttpServletResponse resp)
 throws IOException {

 try {
	 _logger.info("Cron Job has been executed");
	 PersistenceManager pm = PMF.get().getPersistenceManager();
     Query query = pm.newQuery(Item.class);
     query.declareParameters("String always");
     Date curDate = new Date();
     Calendar curCal = Calendar.getInstance();
     curCal.setTime(curDate);
     curCal.add(Calendar.MINUTE, -5);
     Calendar newCal = Calendar.getInstance();
     
     
     while (true) {
    	 List<Item> objects = (List<Item>) query.execute(1);
    	 if (!objects.isEmpty()) {
    		 for (Item e : objects) {
    			 Date newDate = e.getCreateDate();
    			 newCal.setTime(newDate);
    			 if (curCal.after(newCal)) {
    				 javax.jdo.Transaction tx = pm.currentTransaction();
    				 try {//javax.jdo.Transaction tx = pm.currentTransaction();
    					 _logger.info("changing item: " + e.getUsername());
    					 tx.begin();
    					 pm.deletePersistent(e);
    					 tx.commit();}
    				 catch (Exception err) {
    				    if (tx.isActive()){
    				        tx.rollback();}
    				 }
    			 }
    		 }
    	 }
         else {
        	 break;
         }
    	 wait(100);
     }


 }
 catch (Exception ex) {
 //Log any exceptions in your Cron Job
 }
 }

 @Override
 public void doPost(HttpServletRequest req, HttpServletResponse resp)
 throws ServletException, IOException {
 doGet(req, resp);
 }
}

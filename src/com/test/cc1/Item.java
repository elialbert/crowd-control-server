package com.test.cc1;

import java.util.List;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.beoui.geocell.LocationCapable;
import com.beoui.geocell.Point;
import com.google.appengine.api.datastore.Key;

@PersistenceCapable
public class Item implements LocationCapable {
    
	@PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key id;

    @Persistent
    private double latitude;

    @Persistent
    private double longitude;

    @Persistent
    private List<String> geocells;

    public Key getId() {
        return id;
    }

    public void setId(Key id) {
        this.id = id;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public List<String> getGeocells() {
        return geocells;
    }
    

    public void setGeocells(List<String> geocells) {
        this.geocells = geocells;
    }

    public Point getLocation() {
        return new Point(latitude, longitude);
    }

    @Persistent
    private String content;
    
    @Persistent
    public List<String> msgQ;

    public List<String> getMsgQ() {
		return msgQ;
	}

	public void addtoMsgQ(String msg) {
		this.msgQ.add(msg);
	}
	
	public void setMsgQ(List<String> msgQ) {
		this.msgQ = msgQ;
	}

	@Persistent
    private String username;
    
    public Item() {}
    
    public Item(String username, String content) {
        this.content = content;
        this.username = username;
    }
    
    public String getContent() {
    	return content;
    }
    public void setContent(String content) {
    	this.content = content;
    }
    public String getUsername() {
    	return username;
    }
    public void setUsername(String username) {
    	this.username = username;
    }

	public String getKeyString() {
		return null;
	}
}
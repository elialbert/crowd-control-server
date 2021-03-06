package com.test.cc1;

import java.util.Date;
import java.util.List;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.beoui.geocell.LocationCapable;
import com.beoui.geocell.Point;

@PersistenceCapable
public class Item implements LocationCapable {
    
	@PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Long id;//Key id;

    @Persistent
    private double latitude;

    @Persistent
    private double longitude;

    @Persistent
    private List<String> geocells;
    
    @Persistent
    private int always = 1;

    public long getId() {
        return id;
    }

    public void setId(long id) {
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
	
	@Persistent
	private Long radius;
	
	@Persistent
	private Date createDate;
    
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
        return Long.valueOf(id).toString();
    }

	public void setRadius(Long radius) {
		this.radius = radius;
	}

	public Long getRadius() {
		return radius;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public Date getCreateDate() {
		return createDate;
	}

}
package com.photon.phresco.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import com.photon.phresco.util.SizeConstants;

@SuppressWarnings("restriction")
@XmlRootElement
public class ApplicationType implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String id;
	
	//Name of the application type.[Web, Mobile, HTML5]
	private String name;
	
	//String to be displayed in the UI
	private String displayName;

	//List of technologies supported for the application type. [Web - PHP, PHP with Drupal]
	private List<Technology> technologies = new ArrayList<Technology>(SizeConstants.SIZE_TECHNOLOGIES_MAP);
	
	public ApplicationType() {
		super();
	}

	public ApplicationType(String name, String displayName, List<Technology> technologies) {
		super();
		this.name = name;
		this.displayName = displayName;
		this.technologies = technologies;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public List<Technology> getTechnologies() {
		return technologies;
	}

	public void setTechnologies(List<Technology> technologies) {
		this.technologies = technologies;
	}
	
	public Technology getTechonology(String id) {
	    if (technologies == null || technologies.size() == 0) {
	        return null;
	    }
	    
	    for (Technology technology : technologies) {
            if (technology.getId().equals(id)) {
                return technology;
            }
        }
	    
	    return null;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ApplicationType [getName()=");
		builder.append(getName());
		builder.append(", getDisplayName()=");
		builder.append(getDisplayName());
		builder.append(", getTechnologies()=");
		builder.append(getTechnologies());
		builder.append("]");
		return builder.toString();
	}

	public static void main(String[] args) {

	}
}
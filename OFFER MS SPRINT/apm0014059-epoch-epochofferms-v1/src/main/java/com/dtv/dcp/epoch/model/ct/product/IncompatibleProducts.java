package com.dtv.dcp.epoch.model.ct.product;

import java.io.Serializable;
import java.util.List;


public class IncompatibleProducts implements Serializable {
	
private static final long serialVersionUID = 1L;

private String name;

/** The value. */
private List<InCompatibleProductValue> value;

/** The name. */


/**
 * @return the value
 */
public List<InCompatibleProductValue> getValue() {
	return value;
}

/**
 * @param value the value to set
 */
public void setValue(List<InCompatibleProductValue> value) {
	this.value = value;
}

/**
 * @return the name
 */
public String getName() {
	return name;
}

/**
 * @param name the name to set
 */
public void setName(String name) {
	this.name = name;
}

/* (non-Javadoc)
 * @see java.lang.Object#toString()
 */
@Override
public String toString() {
	return "GenericNameValueBase [value=" + value + ", name=" + name + "]";
}
}

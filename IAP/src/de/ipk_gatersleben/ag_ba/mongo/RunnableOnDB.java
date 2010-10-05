/*******************************************************************************
 * 
 *    Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 * 
 *******************************************************************************/
/*
 * Created on Aug 13, 2010 by Christian Klukas
 */

package de.ipk_gatersleben.ag_ba.mongo;

import com.mongodb.DB;

/**
 * @author klukas
 * 
 */
public interface RunnableOnDB extends Runnable {

	void setDB(DB db);

}

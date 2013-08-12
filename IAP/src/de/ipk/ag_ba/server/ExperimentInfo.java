/*******************************************************************************
 * Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Jul 8, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.server;

import java.util.Date;

import org.ErrorMsg;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;

/**
 * @author klukas
 */
public class ExperimentInfo {
	// <Experiment experimentid="56">
	// <experimentname>GPTas-Transgene</experimentname>
	// <remark>GPTas-Linien</remark>
	// <coordinator>Hardy Rolletschek</coordinator>
	// <EXCELFILEID>94</EXCELFILEID>
	// <importusername>hardy</importusername>
	// <visible2group>genwirkung</visible2group>
	// <startdate>2004-08-03 00:00</startdate>
	// <importdate>2004-08-03 13:43</importdate>
	// <measurements>990</measurements>
	// <imagefiles>0</imagefiles>
	// </Experiment>
	// public int id, excelfileid, imagefiles;
	// public String name, remark, coordinator, owner, group;
	// public Date start, end;
	
	public String experimentName;
	public int experimentID;
	public String importUser;
	public String userGroup;
	public String experimentType;
	public Date dateExperimentStart;
	public Date dateExperimentImport;
	public String remark;
	public String coordinator;
	public String excelFileId;
	
	public int fileCount;
	public long sizeKB;
	
	public ExperimentInfo(String experimentName, int experimentID, String importUser, String userGroup,
						String experimentType, Date dateExperimentStart, Date dateExperimentImport, String remark, String coordinator,
						String excelFileId, int fileCount, long sizeKB) {
		this.experimentName = experimentName;
		this.experimentID = experimentID;
		this.importUser = importUser;
		this.userGroup = userGroup;
		this.experimentType = experimentType;
		this.dateExperimentStart = dateExperimentStart;
		this.dateExperimentImport = dateExperimentImport;
		this.remark = remark;
		this.coordinator = coordinator;
		this.excelFileId = excelFileId;
		this.fileCount = fileCount;
		this.sizeKB = sizeKB;
	}
	
	public ExperimentInfo(ExperimentHeaderInterface header) {
		this.experimentName = header.getExperimentName();
		try {
			this.experimentID = Integer.parseInt(header.getDatabaseId());
		} catch (Exception e) {
			this.experimentID = -1;
		}
		this.importUser = header.getImportusername();
		this.userGroup = "";
		this.experimentType = header.getExperimentType();
		try {
			this.dateExperimentStart = header.getStartdate();
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		}
		try {
			this.dateExperimentImport = header.getImportdate();
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		}
		this.remark = header.getRemark();
		this.coordinator = header.getCoordinator();
		this.excelFileId = "";
		try {
			this.fileCount = header.getNumberOfFiles();
		} catch (Exception e) {
			this.fileCount = 0;
		}
		try {
			this.sizeKB = header.getSizekb();
		} catch (Exception e) {
			this.sizeKB = 0;
		}
	}
	
	public ExperimentInfo() {
		// empty
	}
	
}

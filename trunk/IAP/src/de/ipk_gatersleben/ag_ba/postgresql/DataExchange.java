/*************************************************************************
 * 
 *    Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *
 *************************************************************************/
package de.ipk_gatersleben.ag_ba.postgresql;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;


/**
 * @author entzian
 *
 */
public class DataExchange {

	/**
	 * @param args
	 */
	
	
	
	
	// Zum testen werden hier die Zugangsdaten eingetragen
	// sollte später anders abgelegt werden
	
	private String password = "LemnaTec";
	private String user = "postgres";
	private String port = "5432";
	private String host = "192.168.32.203";
	private String driver = "org.postgresql.Driver";
	private String database;
	private Connection connection = null;
	
	private ArrayList<String> DB_Exp_Data;
	
	private String experiment[];	//das kann dann weg
	
	
	DataExchange()
	{
		this("DH-MB1");
	}
	
	DataExchange(String database)
	{
		this.database = database;	
		loadJdbcDriver();
		openConnection();
		ExperimentDerDatenbankAuslesen();
		closeConnection();
	}
	
	
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		
		//ExperimentHeader H1 = new ExperimentHeader();
		//H1.setExperimentLable("hallo");
		//H1.setExperimentLable(this.getString(mesuremtn_lable));
		
		DataExchange Test = new DataExchange("DH-MB1");
		String testArray[] = Test.getExperimentLabels();
		
		if(DF.TEST)
			DF.print("Größe des TestArrays: ", testArray.length);
		
		
		for (int za = testArray.length-1; za >=0; za--)
			System.out.println(testArray[za]);
		
	}
	
	/*
	private ExperimentHeader H1[];
	
	public String test()
	{
	
	H1[ZeilenAnzahl] = new ExperimentHeader();
	for(int anzahl = ZeilenAnzahl; anzahl >= 0; anzahl--)
	ExperimentHeader H1[anzahl]
	
	
		return result;
	}
	
	*/
	
	public String[] getExperimentLabels ()
	{
		return experiment;
	}
	
	public String getSingelExperimentLabel (int position)
	{
		if(position < 0 || position > experiment.length)
			return "";
		else
			return experiment[position];
	}
	
	
	
	
	
	private void ExperimentDerDatenbankAuslesen()
	{
				
		try {
			
			
			if(DF.TEST)
			{
				DatabaseMetaData meta = connection.getMetaData(); //Metadata abfragen
				DF.print("Connection successful:", meta.getDatabaseProductName()+" "+meta.getDatabaseProductVersion());
			}
			
			
			Statement statm = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, 0);	

			String sqlText = "Select distinct measurement_label from import_data;";
			
			if(DF.TEST)
				DF.print(sqlText);
			
			ResultSet result = statm.executeQuery(sqlText);
		
			
			result.last();
			int rows = result.getRow();
			result.beforeFirst();
			
			if(DF.TEST)
				DF.print("Länge result:", rows);
			
			
			experiment = new String[rows];
			
			while(result.next())
			{
				if(DF.TEST)
				{
					//print("aktuelle Zeile: ", String.valueOf(result.getRow()));
					DF.print("aktuelle Zeile: ", result.getString(1));
				}
				
				experiment[result.getRow()-1] = result.getString(1);
			
			}
			result.close();
			statm.close();			
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	
	
	/**
	 * load Driver
	 */
	
	private void loadJdbcDriver()
	{
		try
		{
			Class.forName(driver);
		}
		catch(ClassNotFoundException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * open connection to DB
	 */
	
	private void openConnection()
	{
		try
		{
			String path = "jdbc:postgresql:" + (host != null ? ("//" + host ) + (port != null ? ":" + port : "") + "/" : "") + database;
			connection = DriverManager.getConnection(path, user, password);
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	
	/**
	 * close connection
	 */
	
	private void closeConnection()
	{
		try
		{
			connection.close();
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
	}
	
}

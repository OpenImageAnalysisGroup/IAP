package de.ipk.ag_ba.plugins.actions;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author klukas
 */
public class DBinfo {
	
	private String type;
	private String server;
	private String port;
	private String user;
	private String password;
	private String sysdb;
	private String ftpUrl;
	private String ftpUser;
	private String ftpPassword;
	
	public DBinfo(String fn) throws Exception {
		File fXmlFile = new File(fn);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(fXmlFile);
		doc.getDocumentElement().normalize();
		NodeList nList = doc.getElementsByTagName("databaseconnection");
		for (int temp = 0; temp < nList.getLength(); temp++) {
			Node nNode = nList.item(temp);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;
				setType(eElement.getElementsByTagName("type").item(0).getTextContent());
				setServer(eElement.getElementsByTagName("server").item(0).getTextContent());
				setPort(eElement.getElementsByTagName("port").item(0).getTextContent());
				setUser(eElement.getElementsByTagName("user").item(0).getTextContent());
				setPassword(eElement.getElementsByTagName("password").item(0).getTextContent());
				setSysdb(eElement.getElementsByTagName("database").item(0).getTextContent());
				
				setFtpUrl(eElement.getElementsByTagName("ftpurl").item(0).getTextContent());
				setFtpUser(eElement.getElementsByTagName("ftpuser").item(0).getTextContent());
				setFtpPassword(eElement.getElementsByTagName("ftppassword").item(0).getTextContent());
			}
		}
	}
	
	public boolean isValid(int timeout) {
		try {
			if (!(getType() != null && getType().equalsIgnoreCase("postgres")))
				return false;
			if (!isReachableByTcp(getServer(), Integer.parseInt(getPort()), timeout))
				return false;
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	private static boolean isReachableByTcp(String host, int port, int timeout) {
		try {
			Socket socket = new Socket();
			SocketAddress socketAddress = new InetSocketAddress(host, port);
			socket.connect(socketAddress, timeout);
			socket.close();
			return true;
		} catch (IOException e) {
			return false;
		}
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public String getServer() {
		return server;
	}
	
	public void setServer(String server) {
		this.server = server;
	}
	
	public String getPort() {
		return port;
	}
	
	public void setPort(String port) {
		this.port = port;
	}
	
	public String getUser() {
		return user;
	}
	
	public void setUser(String user) {
		this.user = user;
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getSysdb() {
		return sysdb;
	}
	
	public void setSysdb(String sysdb) {
		this.sysdb = sysdb;
	}
	
	public String getFtpUrl() {
		return ftpUrl;
	}
	
	public void setFtpUrl(String ftpUrl) {
		this.ftpUrl = ftpUrl;
	}
	
	public String getFtpUser() {
		return ftpUser;
	}
	
	public void setFtpUser(String ftpUser) {
		this.ftpUser = ftpUser;
	}
	
	public String getFtpPassword() {
		return ftpPassword;
	}
	
	public void setFtpPassword(String ftpPassword) {
		this.ftpPassword = ftpPassword;
	}
	
}

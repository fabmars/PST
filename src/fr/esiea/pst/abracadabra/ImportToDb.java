package fr.esiea.pst.abracadabra;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class ImportToDb {
	Statement st = null;
	Connection cn = null;
	
	public ImportToDb() {
	//	String url = "jdbc:mysql://sd-36718.dedibox.fr:3306/abracadabra";
		String url = "jdbc:mysql://localhost:3306/mydb";
		String user = "root"; //abracadabra
		String passwd = "user"; //Passwrd � aller chercher par mail
				
		try{
			Class.forName("com.mysql.jdbc.Driver");
			System.out.println("Driver O.K.");
			cn = DriverManager.getConnection(url, user, passwd);
			st = cn.createStatement();
			System.out.println("Connection O.K.");
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	public void SaveMusic(String title, String album, String artist, String type, int year, String comment){
		
		title = title.replace("'", "\"");
		album = album.replace("'", "\"");
		artist = artist.replace("'", "\"");
		type = type.replace("'", "\"");
		comment = comment.replace("'", "\"");
		

		try{
			String sql = "INSERT INTO music_database VALUES (NULL, '"+ title +"','"+ artist +"','"+ year +"','"+ album +"','" + type + "',' "+ comment + "');";
		//	System.out.println(sql);
			st.executeUpdate(sql);
			
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	public int GetIdMusic(String title, String artist) {
		title = title.replace("'", "\"");
		artist = artist.replace("'", "\"");
		try{
			ResultSet rs= st.executeQuery("Select * FROM music_database WHERE title = '"+title+"' AND artiste = '" + artist +"'");
			rs.next();
	//		System.out.println("Select * FROM music_database WHERE title = '"+title+"' AND artiste = '" + artist +"'");
			return rs.getInt(1);
		} catch (Exception e){
			e.printStackTrace();
			return -1;
		}
	}
	
	public String getMusicById(int id){
		if(id!=0)
		try{
			ResultSet rs= st.executeQuery("Select title,artiste FROM music_database WHERE idmusic_database = "+id);
			rs.next();
	//		System.out.println("Select * FROM music_database WHERE title = '"+title+"' AND artiste = '" + artist +"'");
			return "title : " + rs.getString(1) + "\n artiste : " + rs.getString(2);
		} catch (Exception e){
			e.printStackTrace();
			return null;
		}
		else
			return "Recherche ineffective...";
	}
	
	public void AddSignatures(int id, Hash hash){

		String sql = null;
		
		try{
			long start = System.currentTimeMillis();
			for(Entry<Integer, Integer> h : hash.getHash().entrySet()){
				sql = ("INSERT INTO signature VALUES (" + id + "," + h.getValue() + "," + h.getKey() + ");");
				st.addBatch(sql);
			}
			st.executeBatch();
			long end = System.currentTimeMillis();
			System.out.println("Hashs Added ! Time : " + (end - start));
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	public String musicMatched(Hash hash){
		
		ResultSet rs = null;
		HashMap<Integer, Integer> idList = new HashMap<>(); //{music_id, match_count}
				
		String request = "SELECT distinct music from signature where hash = ";
		
		try{
			for(Entry<Integer, Integer> h : hash.getHash().entrySet()){
				rs= st.executeQuery(request + h.getValue());
				while(rs.next()){
					int musicId = (Integer)rs.getInt("music");
					Integer count = idList.get(musicId);
					if(count == null) {
						count = 0;
					}
					idList.put(musicId, count+1);
				}
			}
			
			System.out.println(idList);
		
			int MaxValue = 0;
			int id = 0;
			for(Entry<Integer, Integer> list : idList.entrySet()){
				if(list.getValue() > MaxValue){
					MaxValue = list.getValue();
					id = list.getKey();
				}
			}
			
			return getMusicById(id);
			
		} catch (Exception e){
			e.printStackTrace();
			return null;
		}
	}
}

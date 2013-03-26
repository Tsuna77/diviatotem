package fr.tsuna.diviatotem;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Xml;
/*
 * La class DiviaParser permet de récupérer les informations depuis l'api divia totem
 * disponible à l'adresse 84.55.151.139/relais
 * 
 * Les différentes fonction à appeler sont :
 * 
 * 
 */
public class DiviaParser extends AsyncTask<Object, Object, Object> {
	public static final String url_list_line="http://84.55.151.139/relais/217.php?xml=1";
	public static final String url_list_horaire="http://84.55.151.139/relais/217.php?xml=3&ran=1";
    private static final String ns = null;
    private static final String TAG = "diviaParser";
    private static Context context= null;

    
    public static void setContext(Context c){
    	context=c;
    }
	public List<diviaLine> parseLine() throws XmlPullParserException, IOException{
		NetworkInfo info = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
		if (info == null || !info.isConnected()){
			throw new IOException("Réseaux de donnée non disponible");
		}
		
		
		myLog.write(TAG, "Début du parsing du fichier de ligne");
		
    	URL url=null;
    	URLConnection urlconnect=null;
    	InputStream result = null;
    	boolean error_detected = false;
    	List<diviaLine> list_line = new ArrayList<diviaLine>();
    	
    	try {
    		url = new URL(DiviaParser.url_list_line);
			urlconnect = url.openConnection();
	    	result = urlconnect.getInputStream();
		}
    	catch (Exception e){
    		error_detected=true;
    		myLog.write(TAG,e.getMessage());
    	}
    	if (error_detected){
    		list_line.clear();
    		list_line.add(new diviaLine(Resources.getSystem().getString(R.string.Cant_connect)));
        	return list_line;
    	}
    	
		try{
			XmlPullParser parser = Xml.newPullParser();
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			parser.setInput(result,null);
			parser.nextTag();
			return readLine(parser);
		}	
		finally{
			result.close();
		}
	}

	public List<diviaHoraire> parser_horaire(diviaStation current_station) throws IOException{
		NetworkInfo info = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
		if (info == null || !info.isConnected()){
			throw new IOException("Réseaux de donnée non disponible");
		}
		List<diviaHoraire> list_horaire = new ArrayList<diviaHoraire>();
		Boolean erreur = false;
		URLConnection urlconnect=null;
		InputStream result=null;
		
		String tmp_heure="";
		String tmp_dest="";
		String tmp_time="";
		
		
		try {
			URL url=new URL(url_list_horaire+"&refs="+current_station.getRef());
			//Log.d(TAG,"Connexion à l'url : "+url);
			urlconnect = url.openConnection();
	    	result = urlconnect.getInputStream();
		} catch (Exception e) {
			erreur = true;
			myLog.write(TAG,e.getMessage());
		}
		
		if( ! erreur){
			// début de l'analyse du document.
			XmlPullParser parser = Xml.newPullParser();
			try{
				parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
				parser.setInput(result, null);
				parser.nextTag();
				parser.require(XmlPullParser.START_TAG, ns, "xmldata");
				parser.nextTag(); 	// arrivé sur erreur
				Log.d(TAG, "Code Erreur reçu : "+parser.getAttributeValue(0));
				goToTag(parser, "heure");
				tmp_heure = readText(parser);
				goToTag(parser, "passages");
				
				while (parser.next() != XmlPullParser.END_DOCUMENT){
					// analyse jusqu'à la fin du document
					if (parser.getEventType() == XmlPullParser.START_TAG && parser.getName().equals("passage")){
						Log.d(TAG,"Analyse du passage numéro "+parser.getAttributeValue(0));
						goToTag(parser, "duree");	// arrivé sur duree
						tmp_time = readText(parser);
						goToTag(parser, "destination");	// arrivé sur destination
						tmp_dest = readText(parser);
						list_horaire.add(new diviaHoraire(tmp_dest, tmp_time, tmp_heure));
						
					}
				}
			}
			catch(Exception e){
	    		myLog.write(TAG,e.getMessage());
			}
		}
		
		
		return list_horaire;
	}
	
	public List<diviaStation> parse_station(diviaLine ligne) throws XmlPullParserException, IOException{
		NetworkInfo info = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
		if (info == null || !info.isConnected()){
			throw new IOException("Réseaux de donnée non disponible");
		}
		List<diviaStation> station= new ArrayList<diviaStation>();
		URL url=null;
		URLConnection urlconnect = null;
		InputStream result=null;
		Boolean error_detected = false;
		
		
		if (!ligne.getVers().equals("")){
			myLog.write(TAG,"Recherche des arrêts de la ligne "+ligne);
			station.add(new diviaStation("",App.getContext().getResources().getString(R.string.no_station_selected)));
			
			try {
				String uri = DiviaParser.url_list_line+"&ligne="+ligne.getCode()+"&sens="+ligne.getSens();
	    		myLog.write(TAG,"Appel de l'url : "+uri);
				url = new URL(uri);
				urlconnect = url.openConnection();
		    	result = urlconnect.getInputStream();
			}
			catch(Exception e){
				error_detected=true;
	    		myLog.write(TAG,e.getMessage());
			}
			
			if(!error_detected){
				XmlPullParser parser = Xml.newPullParser();
				parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
				parser.setInput(result, null);
				parser.nextTag();
				parser.require(XmlPullParser.START_TAG, ns, "xmldata");
				myLog.write(TAG,"Analyse des station de la ligne : "+ligne.getCode()+" dans le sens "+ligne.getSens());
				while(parser.next() != XmlPullParser.END_DOCUMENT){
					if (parser.getEventType() == XmlPullParser.TEXT){
						continue;
					}
					else if (parser.getEventType() == XmlPullParser.START_TAG && parser.getName().equals("als") ){
						myLog.write(TAG,"Analyse de la station numéro "+parser.getAttributeValue(0));
						diviaStation st = new diviaStation("new", "");
						goToTag(parser, "code");
						st.setCode(this.readText(parser));
						goToTag(parser, "nom");
						st.setNom(this.readText(parser));
						goToTag(parser, "vers");
						st.setVers(readText(parser));
						goToTag(parser, "refs");
						String refs = readText(parser);	// récupération des références des arrêts
						st.setRefs(refs);
						station.add(st);
					}
				}
				
			}
			else{
				station.clear();
				station.add(new diviaStation("",Resources.getSystem().getString(R.string.error_in_work)));
			}
		}
		else{
			station.add(new diviaStation("", "Veuillez choisir une ligne en premier"));
		}
		
		return station;
	}
	
	private List<diviaLine> readLine(XmlPullParser parser) throws XmlPullParserException, IOException {
		myLog.write(TAG,"Analyse du document");
		List<diviaLine> lines = new ArrayList<diviaLine>();
		lines.add(new diviaLine("Aucune ligne choisie"));
		diviaLine line = null;
		parser.require(XmlPullParser.START_TAG, ns, "xmldata");
		
		while (parser.next() != XmlPullParser.END_DOCUMENT) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
	            continue;
	        }
			String current_name = parser.getName();
			if (current_name.equals("alss")){
				myLog.write(TAG,"Il y a "+parser.getAttributeValue(0)+" Lignes dans le fichiers");
				parser.nextTag();	// passage du alss au als
			}
			current_name = parser.getName();
			if (current_name.equals("als")){
				line =readals(parser);
				if (line != null) {
					lines.add(line);
				}
				else{
					myLog.write(TAG,"La ligne trouvé est buggé");
				}
			}
		}
		return lines;
	}
	
	private void goToTag(XmlPullParser parser, String tagName) throws XmlPullParserException, IOException{
		// parse le document XML jusqu'au tag demandé
		while (parser.next() != XmlPullParser.END_DOCUMENT) {
			if (parser.getEventType() == XmlPullParser.START_TAG && parser.getName().equals(tagName)){
				return;
			}
		}
		return;
	}
	
	private diviaLine readals(XmlPullParser parser) throws XmlPullParserException, IOException{
		diviaLine line =null;
		parser.require(XmlPullParser.START_TAG, ns, "als");
		while (parser.next() != XmlPullParser.END_DOCUMENT) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
	            continue;
	        }
	        String name = parser.getName();
	        // Starts by looking for the entry tag
	        if (name.equals("ligne")) {
	        	diviaLine read = readEntry(parser);
	        	if (!read.getVers().equals("")){
		        	line = read;
	        	}
	        	break;
	        }
	    }
		return line;
	}
	
	private diviaLine readEntry(XmlPullParser parser) throws XmlPullParserException, IOException {
	    parser.require(XmlPullParser.START_TAG, ns, "ligne");
	    String code = null;
	    String vers = null;
	    String nom = null;
	    String sens = null;
	    
	    while( parser.next() != XmlPullParser.END_DOCUMENT){
	    	if (parser.getEventType() != XmlPullParser.START_TAG) {
	            continue;
	        }
	    	String name = parser.getName();
	    	if (name.equals("nom")){
	    		nom=readText(parser);
	    	}
	    	else if (name.equals("vers")){
	    		vers=readText(parser);
	    		break;
	    	}
	    	else if (name.equals("code")){
	    		code=readText(parser);
	    	}
	    	else if (name.equals("sens")){
	    		sens=readText(parser);
	    	}
	    }
	    if (code==null){
	    	code = "";
	    	vers="";
	    }
	    while(parser.next() != XmlPullParser.END_TAG){
	    	// sortir de la balise vers
	    }
	    while(parser.next() != XmlPullParser.END_TAG){
	    	// sortir de la balise couleur
	    }
	    while(parser.next() != XmlPullParser.END_TAG){
	    	// sortir de la balise ligne
	    }
	    while(parser.next() != XmlPullParser.END_TAG){
	    	//sortir de la balise refs
	    }
	    parser.require(XmlPullParser.END_TAG, ns, "als");
	    return new diviaLine(code, nom, sens, vers);
	}
	
	private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
	    String result = "";
	    if (parser.next() == XmlPullParser.TEXT) {
	        result = parser.getText();
	        parser.nextTag();
	    }
	    return result;
	}


	@Override
	protected Object doInBackground(Object... params) {
		// TODO Auto-generated method stub
		myLog.write(TAG, ((divia)params[0]).getType());
		
		
		return null;
	}

}

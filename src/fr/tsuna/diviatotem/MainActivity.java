package fr.tsuna.diviatotem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TextView;


public class MainActivity extends Activity {
	private static final String TAG = "MainActivity";
	private Spinner line_list;
	private Spinner Station_list;
	private List<diviaLine> line_name=new ArrayList<diviaLine>();
	private diviaLine current_line=null;
	private List<diviaStation> station_name= new ArrayList<diviaStation>();
	private diviaStation current_station=null;
	private Handler handler_horaire = new Handler();
	private Runnable run_horaire = new Runnable() {public void run() {update_time();}}; 
	private List<diviaHoraire> list_horaire=new ArrayList<diviaHoraire>();
	private Boolean station_activated=false;
	private Boolean refresh_activated=false;
	private Boolean refreshing=false;
	private Boolean waiting_for_refresh=false;
	
	
	@Override
	protected void onPause(){
		super.onPause();
		// l'activité passe en arrière plan donc désactivation du refresh
		if (refresh_activated){
			refresh_activated=false;
			Toast.makeText(this, getString(R.string.stop_refreshing), Toast.LENGTH_SHORT).show();
		}
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		update_time();
	}
	
	protected void onSaveInstanceState(Bundle outState){
		// fonction lancé pour sauvegarder des paramètres entre les rotation etc
		
	}
	
     @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DiviaParser.setContext(this);
        setContentView(R.layout.select_line);
        myLog.clear();
        log_device_info();
        App.getContext();
		try {
	        PackageManager manager = this.getPackageManager();
	        PackageInfo info;
			info = manager.getPackageInfo(this.getPackageName(), 0);
			Toast.makeText(getApplicationContext(), getString(R.string.version)+info.versionName, Toast.LENGTH_SHORT).show();

		} catch (Exception e) {
    		myLog.write(TAG,e.getMessage());
		}
        ((Spinner)findViewById(R.id.line_list)).setOnItemSelectedListener(new OnItemSelectedListener() {
	        @Override
	        public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
	        	clear_horaire();	// on efface les horaires précédement affiché
	        	Spinner test = (Spinner) findViewById(R.id.line_list);
                current_line = (diviaLine) test.getSelectedItem();
                station_name.clear();
                active_station(true);
                station_name.add(new diviaStation("", getString(R.string.updating)));
                update_station_list();
                if (!current_line.getVers().equals("")){
	                Toast.makeText(getApplicationContext(), getString(R.string.station_loading), Toast.LENGTH_SHORT).show();
	                //*
	                new Thread(new Runnable() { 
	                     public void run() { 
	                    	 if (station_activated){
	                    		 refresh_station();
	                    	 }
	                     } 
	                }).start(); 
	                //*/
                }
                else{
                    active_station(false);
                    station_name.clear();
                    station_name.add(new diviaStation("", ""));
                    update_station_list();
                }
	        }
	        
	        @Override
	        public void onNothingSelected(AdapterView<?> parentView) {
	        }

        });

        
        ((Spinner)findViewById(R.id.list_station)).setOnItemSelectedListener(new OnItemSelectedListener() {
        	@Override
	        public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                myLog.write(TAG,"Mise à jour du spinner list_station (89)");
        		if (station_activated){
                    			
		        	Spinner test = (Spinner) findViewById(R.id.list_station);
	                current_station = (diviaStation) test.getSelectedItem();
	                update_time();
        		}
                
	        }

	        @Override
	        public void onNothingSelected(AdapterView<?> parentView) {
	        }
		});
        
        ((Button)findViewById(R.id.refresh_button)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (station_activated) update_time();
				
			}
		});
        

		active_station(false);
        update_list();
        refresh_line_thread();
        
    }
     /*	
      * Cette fonction place dans les log les infos concernant l'appareil
      */

	private void log_device_info(){
		 NetworkInfo info = ((ConnectivityManager) this.getSystemService(CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
		 
		 String type_connexion="";
		 if (info != null && info.isConnected()){
			 switch (info.getType()){
			 	default:
			 		type_connexion="unknown";
			 		break;
			 	case ConnectivityManager.TYPE_WIFI:
			 		type_connexion="WIFI";
			 		break;
			 	case ConnectivityManager.TYPE_BLUETOOTH:
			 		type_connexion="BLUETOOTH";
			 		break;
			 	case ConnectivityManager.TYPE_MOBILE:
			 		type_connexion="Mobile";
			 		break;
			 	case ConnectivityManager.TYPE_ETHERNET:
			 		type_connexion="Mobile";
			 		break;
			 }
		 }
		 else{
			 type_connexion="Non connecté";
		 }
		 String s="Debug-infos:";
		 s += "\n OS Kernel Version: " + System.getProperty("os.version") + "(" + android.os.Build.VERSION.INCREMENTAL + ")";
		 s += "\n OS Version: "+android.os.Build.VERSION.RELEASE;
		 s += "\n OS API Level: " + android.os.Build.VERSION.SDK_INT;
		 s += "\n Device: " + android.os.Build.DEVICE;
		 s += "\n Model (and Product): " + android.os.Build.MODEL + " ("+ android.os.Build.PRODUCT + ")";
		 s += "\n Network type: "+type_connexion;
		 
		 
		 
		 myLog.write("debug_info", s);

     }
    private void active_station(Boolean state){
    	 station_activated = state;
    	 runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				((Spinner) findViewById(R.id.list_station)).setEnabled(station_activated);
				if (!station_activated){
					active_refresh(false);
				}
			}
		});
     }
    private void active_refresh(Boolean state){
    	 refresh_activated = state;
    	 //*
    	  runOnUiThread(new Runnable() {
    	  			
			@Override
			public void run() {
				((Button) findViewById(R.id.refresh_button)).setEnabled(refresh_activated);
			}
		});
		//*/
     }
    private void refresh_station(){
    	 if (!refreshing){
    		 refreshing=true;
	    	 myLog.write(TAG,"Début du thread de mise à jour des arrêts");
	    	 station_name.clear();
	    	 station_name.add(new diviaStation("","Mise à jour en cours"));
	    	 active_refresh(false);	// désactivation du bouton de rafraischissement
	    	 update_station_list();
	    	 try {
	    		if (current_line.getVers() != ""){
	    			
	    			station_name = new DiviaParser().parse_station(current_line);
	
	    		}
	    		else{
	           	 	station_name.clear();
	           	 	station_name.add(new diviaStation("", ""));
	    			active_station(false);
	    		}
			}catch (Exception e) {
				myLog.write(TAG,e.toString());
				myLog.write(TAG, e.getMessage());
				station_name.clear();
				station_name.add(new diviaStation("", "Erreur"));
				active_station(false);
			}
	    	 
	    	 update_station_list();
	    	 myLog.write(TAG,"Fin du thread de mise à jour des arrêts");
	    	 refreshing=false;
    	 }
    	 else{
    		 myLog.write(TAG,"Refresh déjà en cours");
    		 wait_before_refresh_station();
    	 }
     }
    private void refresh_line_thread(){
    	if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD){
    		refresh_line();
    	}
    	else{
	    	new Thread(new Runnable() { 
	            public void run() {
	            	myLog.write(TAG,"Début du thread de mise à jour des lignes");
	                refresh_line();
	            } 
	       }).start(); 
    	}
    	Toast.makeText(getApplicationContext(), getString(R.string.line_loading), Toast.LENGTH_SHORT).show();
		
    }
    private void update_time(){ 
    	
    	handler_horaire.removeCallbacks(run_horaire);
    	if (current_station != null && !current_station.getVers().equals("") && current_station.getVers() != null){
    		myLog.write(TAG,"Récupération des informations sur la station "+current_station.getNom());
        	handler_horaire.postDelayed(run_horaire, 30000);

    		Toast.makeText(getApplicationContext(), getString(R.string.time_loading), Toast.LENGTH_SHORT).show();
            new Thread(new Runnable() { 
                 public void run() { 
                	 myLog.write(TAG,"Début du thread de mise à jour des horaires");
                	 try {
                		if (current_line.getVers() != ""){
                			list_horaire = new DiviaParser().parser_horaire(current_station);
                			update_time_UI();                			
                			
                		}
     				}catch (Exception e) {
     					station_name.add(new diviaStation("Erreur lors de la récupération du flux", ""));
     				}
                 } 
            }).start(); 
    		
    	}
    }
    private void wait_before_refresh_station(){
    	// attend dans un thread avant de lancer le refresh des stations
    	if (!waiting_for_refresh){
	    	new Thread(new Runnable(){
	    		public void run(){
	    			waiting_for_refresh=true;
	    			while(refreshing){
	    				try{
	    					Thread.sleep(500);
	    				}catch(Exception e){
	    					myLog.write(TAG, e.getMessage());
	    				}
	    			}
	    			refresh_station();
	    			waiting_for_refresh=false;
	    		}
	    	}).start();
    	}
    }
    private void update_time_UI(){
    	runOnUiThread(new Runnable() {
	        public void run() {
	        	if ( list_horaire != null && list_horaire.size() >= 1){
	    			// il y a au moins 1 horaires
	        		active_refresh(true);
	    			((TextView) findViewById(R.id.text_Direction1)).setText(list_horaire.get(0).getDest());
	    			((TextView) findViewById(R.id.text_time1)).setText(list_horaire.get(0).get_Left_Time());
	    			if (list_horaire.size() >= 2){
	        			((TextView) findViewById(R.id.text_Direction2)).setText(list_horaire.get(1).getDest());
	        			((TextView) findViewById(R.id.text_time2)).setText(list_horaire.get(1).get_Left_Time());
	    				
	    			}
	    		}
	    		else{
	    			clear_horaire();
	    			active_refresh(false);
	    		}
	        }
	      }); 
		
    }
    
    /*
     * Efface les champs d'info sur les horaires
     */
    private void clear_horaire(){
    	myLog.write(TAG,"Aucun horaire disponible");
		((TextView) findViewById(R.id.text_Direction1)).setText(getString(R.string.NC));
		((TextView) findViewById(R.id.text_time1)).setText(getString(R.string.NC));
		((TextView) findViewById(R.id.text_Direction2)).setText(getString(R.string.NC));
		((TextView) findViewById(R.id.text_time2)).setText(getString(R.string.NC));
    }
    
    /*
     * Met à jour la listView avec le nouveau contenu
     */
    private void update_list(){
    	runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				update_list_UI();
				
			}
		});
    }
    private void update_list_UI(){
    	if (!line_name.isEmpty()){
	        myLog.write(TAG,"Mise à jour du spinner line_list(349)");
	        try{
		    	line_list = (Spinner) findViewById(R.id.line_list);
		    	ArrayAdapter<diviaLine> adapter = new ArrayAdapter<diviaLine>(this, android.R.layout.simple_list_item_1, line_name);
		    	line_list.setAdapter(adapter);
			}catch(Exception e){
				myLog.write(TAG, e.getMessage());
			}
    	}
    	
    }
    private void update_station_list(){
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					update_station_list_UI();
					
				}
			});
    }
    private void update_station_list_UI(){
    	if (station_activated && !station_name.isEmpty()){
	        myLog.write(TAG,"Mise à jour du spinner list_station (371)");
	        try{
		    	Station_list = (Spinner) findViewById(R.id.list_station);
		    	ArrayAdapter<diviaStation> adapter2 = new ArrayAdapter<diviaStation>(this, android.R.layout.simple_list_item_1, station_name);
		    	Station_list.setAdapter(adapter2);
			}catch(Exception e){
				myLog.write(TAG, e.getMessage());
			}
	    	
    	}
    }
    
    /*
     * Récupère la liste des lignes de divia
     */
    private void refresh_line(){

		// récupération des lignes via l'api
    	line_name.clear();
    	line_name.add(new diviaLine(getString(R.string.reload)));
    	update_list();
    	
    	line_name.clear();
    	try {
    		line_name=new DiviaParser().parseLine();
		}catch (Exception e) {
			myLog.write(TAG, e.getMessage());
			line_name.add(new diviaLine(getString(R.string.ERRCON)));
		}
    	update_list();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
    	   	
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        	case R.id.action_refresh:
                refresh_line_thread();
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
}

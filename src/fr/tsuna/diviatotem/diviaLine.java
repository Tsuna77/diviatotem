package fr.tsuna.diviatotem;


public class diviaLine extends divia {
	private String code="";
	private String nom="";
	private String sens="";
	private String Vers="";
	private static final String TAG="diviaLine";
	/*
	 * Renvoie le type d'objet utilisé
	 */
	@Override
	public String getType(){
		return "diviaLine";
	}
	
	@Override
	public String toString(){
		String message = this.code;
		if (!this.Vers.equals("")){
			message = this.nom+" -> "+Vers;
		}
		return message;
	}
	
	public diviaLine(String c){
		this.code=c;
		myLog.write(TAG,"Création d'une line divia : "+code);
	}
	
	public diviaLine(String c, String v){
		this.code=c;
		this.Vers=v;
		myLog.write(TAG,"Création d'une ligne divia : "+code+"/"+Vers);
	}
	
	public diviaLine(String c, String n, String s, String v){
		this.code=c;
		this.nom=n;
		this.sens=s;
		this.Vers=v;
		myLog.write(TAG,"Création d'une ligne divia : "+code+"/"+nom+"/"+sens+"/"+Vers);
	}
	
	public String getCode(){
		return code;
	}

	public String getVers() {
		return Vers;
	}

	public void setVers(String vers) {
		Vers = vers;
	}

	public String getNom() {
		return nom;
	}

	public void setNom(String nom) {
		this.nom = nom;
	}

	public String getSens() {
		return sens;
	}

	public void setSens(String sens) {
		this.sens = sens;
	}
}

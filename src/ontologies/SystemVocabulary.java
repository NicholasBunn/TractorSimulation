package ontologies;

public interface SystemVocabulary {
	public static final String TRACTOR = "tractor";
	public static final String TRACTOR_ID = "id";
	public static final String TRACTOR_NAME = "name";
	public static final String TRACTOR_CONSUMPTION = "consumption";
	public static final String TRACTOR_FARMNUMBER = "farmNumber";
	public static final String TRACTOR_FARMLOCATION = "farmLocation";
	public static final String TRACTOR_TIMESTAMP = "timeStamp";
	
	public static final String FUEL_REQUEST = "tractorRequest";
	public static final String FUEL_REQUEST_FUELID = "tractorId";
	public static final String FUEL_REQUEST_FUELCONSUMPTION = "tractorConsumption";
	
	public static final String LOCATION_REQUEST = "locationRequest";
	public static final String LOCATION_REQUEST_TRACTORID = "tractorId";
	public static final String LOCATION_REQUEST_TRACTORFARM = "tractorFarm";
	public static final String LOCATION_REQUEST_TRACTORLOCATION = "tractorLocation";
	public static final String LOCATION_REQUEST_TIMESTAMP = "timeStamp";
	
	public static final String ADD_AGENT_CONCEPT = "addAgentConcept";
	public static final String ADD_AGENT_CONCEPT_NAME = "name";
	public static final String ADD_AGENT_CONCEPT_TYPE = "type";
	
	public static final String REMOVE_AGENT_CONCEPT = "removeAgentConcept";
	public static final String REMOVE_AGENT_CONCEPT_NAME = "name";
	public static final String REMOVE_AGENT_CONCEPT_TYPE = "type";
	
	public static final String RETRIEVE_DATA = "RetrieveData";
	public static final String RETRIEVE_DATA_TRACTOR = "Tractor";
	
	public static final String ADD_AGENT = "AddAgent";
	public static final String ADD_AGENT_AGENT = "agent";
	
	public static final String REMOVE_AGENT = "KillAgent";
	public static final String REMOVE_AGENT_AGENT = "agent";
	
	public static final String PERFORM_CFP = "performCFP";
	public static final String PERFORM_CFP_TRACTORID = "TractorId";
	
	public static final String PERFORM_REQUESTS = "PerformRequests";
	public static final String PERFORM_REQUESTS_FUEL = "fuelId";
	
}

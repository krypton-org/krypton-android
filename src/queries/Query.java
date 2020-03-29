package queries;

public abstract class Query {
	
	protected Map<String, Object> variables;
	
	public Query(Map<String, Object> variables) {
		this.variables = variables;
	}
	
	abstract protected String getQuery();
}

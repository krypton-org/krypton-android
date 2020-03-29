package queries;

public class RefreshQuery extends Query {
	
	public RefreshQuery() {
		this.query="mutation { refreshToken { token } }";
	}

}

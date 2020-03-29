package queries;

public class LoginQuery extends Query {
	
	public getQuery() {
		StringBuilder sb = new StringBuilder();
		sb.append("mutation login($login: String!, $password: String!) {\n")
		  .append("login(login: $login, password: $password) {\n")
		  .append("token\n")
		  .append("]\n")
		  .append("}\n}");
		this.query = sb.toString();
	}

}

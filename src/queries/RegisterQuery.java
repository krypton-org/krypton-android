package queries;

public class RegisterQuery extends Query {
	
	public RegisterQuery() {
		StringBuilder sb = new StringBuilder();
		sb.append("mutation register($fields: UserRegisterInput!) {\n")
		  .append("register(fields: $fields)\n")
		  .append("}\n");
		this.query = sb.toString();
	}

}

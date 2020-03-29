package queries;

public class DeleteQuery extends Query {
	public DeleteQuery() {
		StringBuilder sb = new StringBuilder();
		sb.append("mutation deleteMe($password: String!) {\n")
		  .append("deleteMe(password: $password)\n")
		  .append("}\n}");
		this.query = sb.toString();
	}
}

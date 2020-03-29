package queries;

public class UpdateQuery extends Query {
	public UpdateQuery() {
		StringBuilder sb = new StringBuilder();
		sb.append("mutation updateMe($fields: UserUpdateInput!) {\n")
		  .append("updateMe(fields: $fields) {\n")
		  .append("token\n")
		  .append("]\n")
		  .append("}\n}");
		this.query = sb.toString();
	}

}

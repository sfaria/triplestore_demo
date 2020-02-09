package demo;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.NsIterator;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.SimpleSelector;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.apache.jena.util.FileManager;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Scott Faria
 */
public class Jena {

	// -------------------- Private Methods --------------------

	private static Model createModel() throws IOException {
		Model model = ModelFactory.createDefaultModel();
		try (InputStream in = FileManager.get().open("resources/campaign_sf_funds.rdf")) {
			model.read(in, null);
		}
		return model;
	}

	private static void printAllStatements(Model model) {
		StmtIterator statements = model.listStatements();
		while (statements.hasNext()) {
			Statement statement = statements.nextStatement();
			Resource subject = statement.getSubject();
			String nameSpace = subject.getNameSpace();
			Property predicate = statement.getPredicate();
			RDFNode object = statement.getObject();
			System.out.print("Subject: " + subject.toString());
			System.out.print(" Predicate: " + predicate.toString() + " Object: ");
			if (object instanceof Resource) {
				System.out.print(object.toString());
			} else {
				// object is a literal
				System.out.print(" \"" + object.toString() + "\"");
			}

			System.out.println(" .");
		}
	}

	// -------------------- Main --------------------

	public static void main(String[] args) throws IOException {
		Model model = createModel();
//		printAllStatements(model);


		NsIterator nsIterator = model.listNameSpaces();
		System.out.println("Namespaces: ");
		while (nsIterator.hasNext()) {
			System.out.println(nsIterator.nextNs());
		}

//		System.out.println("Query Result: ");
//		ResourceImpl resource = new ResourceImpl("https://data.sfgov.org/resource/nmzh-y378/row-6any_3sq6~7bbr");
//		PropertyImpl property = new PropertyImpl("https://data.sfgov.org/resource/nmzh-y378/funds_disbursed");
//		StmtIterator it = model.listStatements(null, property, (String) null);
//		while (it.hasNext()) {
//			Statement statement = it.nextStatement();
//			System.out.println(statement.toString());
//		}

		//language=SPARQL
		String queryString = "" +
				"prefix ds: <https://data.sfgov.org/resource/nmzh-y378/>\n" +
				"#" +
				"prefix dsbase: <https://data.sfgov.org/resource/>\n" +
				"#" +
				"prefix socrata: <http://www.socrata.com/rdf/terms#>\n" +
				"select distinct ?candidate ?amount\n" +
				"where {  \n" +
				"\t?id ds:pending_completed \"Completed\" .\n" +
				"\t?id ds:candidate ?candidate .\n" +
				"\t?id ds:funds_disbursed ?amount\n" +
				"} ";
		Query query = QueryFactory.create(queryString);
		try (QueryExecution queryExecution = QueryExecutionFactory.create(query, model)) {
			ResultSet resultSet = queryExecution.execSelect();
			while (resultSet.hasNext()) {
				QuerySolution solution = resultSet.next();
				System.err.println(solution.toString());
			}
		}
	}
}

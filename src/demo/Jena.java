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

	//language=SPARQL
	public static final String ALL_CANDIDATES_WITH_FUNDING = "" +
			"prefix ds: <https://data.sfgov.org/resource/nmzh-y378/>\n" +
			"select distinct ?candidate ?amount\n" +
			"where {  \n" +
			"\t?id ds:pending_completed \"Completed\" .\n" +
			"\t?id ds:candidate ?candidate .\n" +
			"\t?id ds:funds_disbursed ?amount\n" +
			"} ";

	// -------------------- Private Methods --------------------

	private static Model createModel() throws IOException {
		Model model = ModelFactory.createDefaultModel();
		try (InputStream in = FileManager.get().open("resources/campaign_sf_funds.rdf")) {
			model.read(in, null);
		}
		return model;
	}

	private static void executeSPARQLQuery(String queryString, Model model) {
		Query query = QueryFactory.create(queryString);
		try (QueryExecution queryExecution = QueryExecutionFactory.create(query, model)) {
			ResultSet resultSet = queryExecution.execSelect();
			while (resultSet.hasNext()) {
				QuerySolution solution = resultSet.next();
				System.err.println(solution.toString());
			}
		}
	}

	// -------------------- Main --------------------

	public static void main(String[] args) throws IOException {
		Model model = createModel();
		executeSPARQLQuery(ALL_CANDIDATES_WITH_FUNDING, model);
	}
}

package demo;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.apache.jena.util.FileManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static demo.QueryType.*;

/**
 * @author Scott Faria
 */
public final class Jena {

	// -------------------- Private Methods --------------------

	//language=SPARQL
	private static final String ALL_CANDIDATES_WITH_FUNDING = "" +
			"prefix ds: <https://data.sfgov.org/resource/nmzh-y378/>\n" +
			"select distinct ?candidate ?amount\n" +
			"where {  \n" +
			"\t?id ds:pending_completed \"Completed\" ;\n" +
			"\tds:candidate ?candidate ;\n" +
			"\tds:funds_disbursed ?amount\n" +
			"} \n" +
			"order by ?candidate";

	//language=SPARQL
	private static final String ALL_CANDIDATES_WITH_FUNDING_IMPROVED = "" +
			"prefix ds: <https://data.sfgov.org/resource/nmzh-y378/>\n" +
			"prefix xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
			"select ?candidate (sum(xsd:decimal(?amount)) as ?total_funding)\n" +
			"where {  \n" +
			"\t?id ds:pending_completed \"Completed\" ;\n" +
			"\tds:candidate ?candidate ;\n" +
			"\tds:funds_disbursed ?amount\n" +
			"} \n" +
			"group by ?candidate\n" +
			"order by ?candidate";


	//language=SPARQL
	private static final String ASK_IF_CANDIDATE_EXISTS = "" +
			"prefix ds: <https://data.sfgov.org/resource/nmzh-y378/>\n" +
			"ask { \n" +
			"   ?x ds:candidate \"Fewer, Sandra\"\n" +
			"}";

	//language=SPARQL
	private static final String CANDIDATES_BY_DISTRICT = "" +
			"prefix ds: <https://data.sfgov.org/resource/nmzh-y378/>\n" +
			"select ?district \n" +
			"(count(distinct ?candidate) as ?num_candidates) \n" +
			"(group_concat(distinct concat(\"'\", ?candidate, \"'\"); separator=\", \") as ?candidates)\n" +
			"where {\n" +
			"    ?id ds:district ?district ;\n" +
			"    ds:candidate ?candidate\n" +
			"}\n" +
			"group by ?district\n" +
			"order by ?district ?candidate";

	//language=SPARQL
	private static final String CANDIDATES_TOOK_OVER_AMOUNT = "" +
			"prefix ds: <https://data.sfgov.org/resource/nmzh-y378/>\n" +
			"prefix xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
			"select ?candidate (max(xsd:decimal(?amount)) as ?highest_donation)\n" +
			"where {\n" +
			"    ?id ds:candidate ?candidate ;\n" +
			"    ds:funds_disbursed ?amount\n" +
			"    filter (xsd:decimal(?amount) > 10000.0)\n" +
			"}\n" +
			"group by ?candidate\n" +
			"order by ?candidate";

	// -------------------- Private Methods --------------------

	private static Model createModel() throws IOException {
		Model model = ModelFactory.createDefaultModel();
		try (InputStream in = FileManager.get().open("resources/campaign_sf_funds.rdf")) {
			model.read(in, null);
		}
		return model;
	}

	private static void executeSPARQLQuery(String queryString, QueryType type, Model model) {
		Query query = QueryFactory.create(queryString);
		try (QueryExecution queryExecution = QueryExecutionFactory.create(query, model)) {
			System.out.println();
			System.out.println("------------------------------------------------------");
			switch (type) {
				case SELECT:
					ResultSet resultSet = queryExecution.execSelect();
					resultSet.forEachRemaining(solution -> {
						Iterable<String> it = solution::varNames;
						Stream<String> stream = StreamSupport.stream(it.spliterator(), false);
						String line = stream.map(colName -> {
							RDFNode node = solution.get(colName);
							String value;
							if (node.isLiteral()) {
								Literal literal = node.asLiteral();
								Class<?> typeClass = literal.getDatatype().getJavaClass();
								if (typeClass.equals(Number.class)) {
									value = Float.toString(literal.getFloat());
								} else {
									value = literal.toString();
								}
							} else {
								value = node.toString();
							}
							return colName + ": " + value;
						}).collect(Collectors.joining(", "));
						System.out.println(line);
					});
					break;
				case ASK:
					System.out.println("Result: " + queryExecution.execAsk());
					break;
			}
			System.out.println("------------------------------------------------------");
			System.out.println();
		}
	}

	// -------------------- Main --------------------

	public static void main(String[] args) throws IOException {
		Model model = createModel();
		executeSPARQLQuery(ASK_IF_CANDIDATE_EXISTS, ASK, model);
		executeSPARQLQuery(ALL_CANDIDATES_WITH_FUNDING, SELECT, model);
		executeSPARQLQuery(ALL_CANDIDATES_WITH_FUNDING_IMPROVED, SELECT, model);
		executeSPARQLQuery(CANDIDATES_BY_DISTRICT, SELECT, model);
		executeSPARQLQuery(CANDIDATES_TOOK_OVER_AMOUNT, SELECT, model);
	}
}

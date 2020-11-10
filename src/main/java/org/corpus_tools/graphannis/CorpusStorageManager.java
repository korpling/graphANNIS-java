/*
 * Copyright 2018 Thomas Krause.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.corpus_tools.graphannis;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import com.sun.jna.NativeLong;
import com.sun.jna.ptr.LongByReference;

import org.corpus_tools.graphannis.capi.AnnisCountExtra;
import org.corpus_tools.graphannis.capi.AnnisErrorListRef;
import org.corpus_tools.graphannis.capi.AnnisImportFormat;
import org.corpus_tools.graphannis.capi.AnnisQueryLanguage;
import org.corpus_tools.graphannis.capi.AnnisResultOrder;
import org.corpus_tools.graphannis.capi.CAPI;
import org.corpus_tools.graphannis.capi.CAPI.AnnisComponentConst;
import org.corpus_tools.graphannis.capi.CharPointer;
import org.corpus_tools.graphannis.capi.QueryAttributeDescription;
import org.corpus_tools.graphannis.errors.GraphANNISException;
import org.corpus_tools.graphannis.errors.SetLoggerError;
import org.corpus_tools.graphannis.model.AnnoKey;
import org.corpus_tools.graphannis.model.Annotation;
import org.corpus_tools.graphannis.model.Component;
import org.corpus_tools.graphannis.model.ComponentType;
import org.corpus_tools.graphannis.model.FrequencyTableEntry;
import org.corpus_tools.graphannis.model.Graph;
import org.corpus_tools.graphannis.model.NodeDesc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An API for managing corpora stored in a common location on the file system.
 * 
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
public class CorpusStorageManager {
	private final CAPI.AnnisCorpusStorage instance;

	private final Logger log = LoggerFactory.getLogger(CorpusStorageManager.class);

	/** Contains the extended results of the count query. */
	public static class CountResult {
		/** Total number of matches. */
		public long matchCount;
		/** Number of documents with at least one match. */
		public long documentCount;
	}

	/**
	 * An enum over all supported query languages of graphANNIS.
	 *
	 * Currently, only the ANNIS Query Language (AQL) and its variants are
	 * supported, but this enum allows us to add a support for older query language
	 * versions or completely new query languages.
	 */
	public static enum QueryLanguage {

		/** Default ANNIS query language implementation/specification */
		AQL(AnnisQueryLanguage.AQL),
		/** Emulates the (sometimes problematic) behavior of AQL used in ANNIS 3 */
		AQLQuirksV3(AnnisQueryLanguage.AQLQuirksV3);

		protected final int capiVal;

		private QueryLanguage(int capiVal) {
			this.capiVal = capiVal;
		}
	}

	/**
	 * Defines the order of results of a "find" query. *
	 */
	public static enum ResultOrder {

		/**
		 * Order results by their document name and the the text position of the match.
		 */
		Normal(AnnisResultOrder.Normal),
		/** Inverted the order of {@link #Normal} */
		Inverted(AnnisResultOrder.Inverted),
		/**
		 * A random ordering which is <strong>not stable</strong>. Each new query will
		 * result in a different order.
		 */
		Randomized(AnnisResultOrder.Randomized),
		/**
		 * Results are not ordered at all, but also not actively randomized. Each new
		 * query <em>might</em> result in a different order.
		 */
		NotSorted(AnnisResultOrder.NotSorted);

		protected final int capiVal;

		private ResultOrder(int capiVal) {
			this.capiVal = capiVal;
		}
	}

	/**
	 * An enum of all supported input formats of graphANNIS.
	 * 
	 */
	public static enum ImportFormat {

		/**
		 * Legacy <a href=
		 * "http://korpling.github.io/ANNIS/4.0/developer-guide/annisimportformat.html">relANNIS
		 * import file format</a>
		 */
		RelANNIS(AnnisImportFormat.RelANNIS),
	  /**
   		* [GraphML](http://graphml.graphdrawing.org/) based export-format, suitable to be imported from other graph databases.
   		* This format follows the extensions/conventions of the Neo4j [GraphML module](https://neo4j.com/docs/labs/apoc/current/import/graphml/).
   		*/
		GraphML(AnnisImportFormat.GraphML);

		protected final int capiVal;

		private ImportFormat(int capiVal) {
			this.capiVal = capiVal;
		}
	}

	/**
	 * Create a new instance with a an automatic determined size of the internal
	 * corpus cache.
	 * 
	 * This constructor version does not use parallel query execution and an
	 * automatic strategy for its internal corpus cache.
	 * 
	 * @param dbDir The path on the filesystem where the corpus storage content is
	 *              located. Must be an existing directory.
	 * @throws GraphANNISException
	 */
	public CorpusStorageManager(String dbDir) throws GraphANNISException {
		this(dbDir, null, LogLevel.Off, false);
	}

	/**
	 * Create a new instance with a an automatic determined size of the internal
	 * corpus cache.
	 * 
	 * @param dbDir       The path on the filesystem where the corpus storage
	 *                    content is located. Must be an existing directory.
	 * @param logfile     Path to where a logfile should be written
	 * @param level       Log level for the logfile
	 * @param useParallel If "true" parallel joins are used by the system, using all
	 *                    available cores.
	 * @throws GraphANNISException
	 */
	public CorpusStorageManager(String dbDir, String logfile, LogLevel level, boolean useParallel)
			throws GraphANNISException {

		// create the parent directories of the output directory
		File dbDirFile = new File(dbDir);
		if (dbDirFile.mkdirs()) {
			log.info("Created directory {} for CorpusStorageManager", dbDir);
		}
		AnnisErrorListRef err = new AnnisErrorListRef();
		CAPI.annis_init_logging(logfile, level.getRaw(), err);
		try {
			err.checkErrors();
		} catch (SetLoggerError ex) {
			// only warn about this
			log.warn("Could not initialize graphANNIS logger", ex);
		}
		err = new AnnisErrorListRef();
		this.instance = CAPI.annis_cs_with_auto_cache_size(dbDir, useParallel, err);
		err.checkErrors();
	}

	/**
	 * Create a new instance with a maximum size for the internal corpus cache.
	 * 
	 * @param dbDir        The path on the filesystem where the corpus storage
	 *                     content is located. Must be an existing directory.
	 * @param logfile      Path to where a logfile should be written
	 * @param level        Log level for the logfile
	 * @param useParallel  If "true" parallel joins are used by the system, using
	 *                     all available cores.
	 * @param maxCacheSize Fixed maximum size of the cache in bytes.
	 * @throws GraphANNISException
	 */
	public CorpusStorageManager(String dbDir, String logfile, LogLevel level, boolean useParallel, long maxCacheSize)
			throws GraphANNISException {

		// create the parent directories of the output directory
		File dbDirFile = new File(dbDir);
		if (dbDirFile.mkdirs()) {
			log.info("Created directory {} for CorpusStorageManager", dbDir);
		}
		AnnisErrorListRef err = new AnnisErrorListRef();
		CAPI.annis_init_logging(logfile, level.getRaw(), err);
		try {
			err.checkErrors();
		} catch (SetLoggerError ex) {
			// only warn about this
			log.warn("Could not initialize graphANNIS logger", ex);
		}
		err = new AnnisErrorListRef();
		this.instance = CAPI.annis_cs_with_max_cache_size(dbDir, maxCacheSize, useParallel, err);
		err.checkErrors();
	}

	/**
	 * List all available corpora in the corpus storage.
	 * 
	 * @return A list of corpus names.
	 * @throws GraphANNISException
	 */
	public String[] list() throws GraphANNISException {
		AnnisErrorListRef err = new AnnisErrorListRef();
		CAPI.AnnisVec_AnnisCString orig = CAPI.annis_cs_list(instance, err);
		err.checkErrors();

		String[] copy = new String[CAPI.annis_vec_str_size(orig).intValue()];
		for (int i = 0; i < copy.length; i++) {
			copy[i] = CAPI.annis_vec_str_get(orig, new NativeLong(i));
		}

		orig.dispose();

		return copy;
	}

	/**
	 * Returns a list of all node annotations of a corpus given its name.
	 * 
	 * @param corpusName             The name of the corpus
	 * @param listValues             If true include the possible values in the
	 *                               result.
	 * @param onlyMostFrequentValues If both this argument and "listValues" are
	 *                               true, only return the most frequent value for
	 *                               each annotation name.
	 * @return list of annotations
	 */
	public List<Annotation> listNodeAnnotations(String corpusName, boolean listValues, boolean onlyMostFrequentValues) {
		List<Annotation> result = new LinkedList<>();
		if (instance != null) {
			CAPI.AnnisMatrix_AnnisCString orig = CAPI.annis_cs_list_node_annotations(instance, corpusName, listValues,
					onlyMostFrequentValues);

			final int nrows = CAPI.annis_matrix_str_nrows(orig).intValue();
			final int ncols = CAPI.annis_matrix_str_ncols(orig).intValue();
			if (ncols >= (listValues ? 3 : 2)) {
				for (int i = 0; i < nrows; i++) {
					Annotation anno = new Annotation();
					AnnoKey key = new AnnoKey();
					String ns = CAPI.annis_matrix_str_get(orig, new NativeLong(i), new NativeLong(0));
					String name = CAPI.annis_matrix_str_get(orig, new NativeLong(i), new NativeLong(1));

					if (!"".equals(ns)) {
						key.setNs(ns);
					}
					key.setName(name);
					anno.setKey(key);

					if (listValues) {
						String val = CAPI.annis_matrix_str_get(orig, new NativeLong(i), new NativeLong(2));
						anno.setValue(val);
					}

					result.add(anno);
				}
			}

			orig.dispose();
		}
		return result;
	}

	/**
	 * eturns a list of all edge annotations of a corpus given by its name and and
	 * given component.
	 * 
	 * @param corpusName             The name of the corpus
	 * @param componentType          Type of the component.
	 * @param componentName          Name of the component.
	 * @param componentLayer         A layer name which allows to group different
	 *                               components into the same layer. Can be empty.
	 * @param listValues             If true include the possible values in the
	 *                               result.
	 * @param onlyMostFrequentValues If both this argument and "listValues" are
	 *                               true, only return the most frequent value for
	 *                               each annotation name.
	 * @return list of annotations
	 */
	public List<Annotation> listEdgeAnnotations(String corpusName, ComponentType componentType, String componentName,
			String componentLayer, boolean listValues, boolean onlyMostFrequentValues) {
		List<Annotation> result = new LinkedList<>();
		if (instance != null) {
			CAPI.AnnisMatrix_AnnisCString orig = CAPI.annis_cs_list_edge_annotations(instance, corpusName,
					componentType.toInt(), componentName, componentLayer, listValues, onlyMostFrequentValues);

			final int nrows = CAPI.annis_matrix_str_nrows(orig).intValue();
			final int ncols = CAPI.annis_matrix_str_ncols(orig).intValue();
			if (ncols >= (listValues ? 3 : 2)) {
				for (int i = 0; i < nrows; i++) {
					Annotation anno = new Annotation();
					AnnoKey key = new AnnoKey();
					String ns = CAPI.annis_matrix_str_get(orig, new NativeLong(i), new NativeLong(0));
					String name = CAPI.annis_matrix_str_get(orig, new NativeLong(i), new NativeLong(1));

					if (!"".equals(ns)) {
						key.setNs(ns);
					}
					key.setName(name);
					anno.setKey(key);

					if (listValues) {
						String val = CAPI.annis_matrix_str_get(orig, new NativeLong(i), new NativeLong(2));
						anno.setValue(val);
					}
					result.add(anno);
				}
			}

			orig.dispose();
		}
		return result;
	}

	/**
	 * Returns a list of all components of a corpus given by its name and a given
	 * component type
	 * 
	 * @param corpusName    The name of the corpus
	 * @param componentType Type of the component to be returned
	 * @return A list of all components of this type.
	 */
	public List<Component> getAllComponentsByType(String corpusName, ComponentType componentType) {
		List<Component> result = new LinkedList<>();
		if (instance != null) {
			CAPI.AnnisVec_AnnisComponent orig = CAPI.annis_cs_list_components_by_type(instance, corpusName,
					componentType.toInt());

			for (int i = 0; i < CAPI.annis_vec_component_size(orig).intValue(); i++) {
				AnnisComponentConst cOrig = CAPI.annis_vec_component_get(orig, new NativeLong(i));
				Component c = new Component();
				c.setType(componentType);

				CharPointer cname = CAPI.annis_component_name(cOrig);
				c.setName(cname == null ? "" : cname.toString());

				CharPointer clayer = CAPI.annis_component_layer(cOrig);
				c.setLayer(clayer == null ? "" : clayer.toString());

				result.add(c);
			}
		}
		return result;
	}

	/**
	 * Parses a query and checks if it is valid.
	 * 
	 * @param corpusNames   The name of the corpora the query would be executed on
	 *                      (needed to catch certain corpus-specific semantic
	 *                      errors).
	 * @param query         The query as string.
	 * @param queryLanguage The query language of the query (e.g. AQL).
	 * @return True if this a valid query, false otherwise.
	 * @throws GraphANNISException
	 */
	public boolean validateQuery(Iterable<String> corpusNames, String query, QueryLanguage queryLanguage)
			throws GraphANNISException {

		AnnisErrorListRef err = new AnnisErrorListRef();
		CAPI.AnnisVec_AnnisCString c_corpusNames = CAPI.annis_vec_str_new();
		for (String cn : corpusNames) {
			CAPI.annis_vec_str_push(c_corpusNames, cn);
		}
		boolean result = CAPI.annis_cs_validate_query(instance, c_corpusNames, query, queryLanguage.capiVal, err);
		c_corpusNames.dispose();
		err.checkErrors();

		return result;
	}

	public List<NodeDesc> getNodeDescriptions(String query, QueryLanguage queryLanguage) throws GraphANNISException {
		AnnisErrorListRef err = new AnnisErrorListRef();
		QueryAttributeDescription desc = CAPI.annis_cs_node_descriptions(instance, query, queryLanguage.capiVal, err);
		err.checkErrors();

		return desc.getList();

	}

	/**
	 * Count the number of results for a query.
	 * 
	 * @param corpusNames   The name of the corpora to execute the query on.
	 * @param query         The query as string.
	 * @param queryLanguage The query language of the query (e.g. AQL).
	 * @return Returns the count as number.
	 * @throws GraphANNISException
	 */
	public long count(Iterable<String> corpusNames, String query, QueryLanguage queryLanguage)
			throws GraphANNISException {
		AnnisErrorListRef err = new AnnisErrorListRef();
		CAPI.AnnisVec_AnnisCString c_corpusNames = CAPI.annis_vec_str_new();
		for (String cn : corpusNames) {
			CAPI.annis_vec_str_push(c_corpusNames, cn);
		}
		long result = CAPI.annis_cs_count(instance, c_corpusNames, query, queryLanguage.capiVal, err);
		c_corpusNames.dispose();
		err.checkErrors();

		return result;
	}

	/**
	 * Count the number of results for a query and return both the total number of
	 * matches and also the number of documents in the result set.
	 * 
	 * @param corpusNames   The name of the corpora to execute the query on.
	 * @param query         The query as string.
	 * @param queryLanguage The query language of the query (e.g. AQL).
	 * @return An object containing both the match and document counts
	 * @throws GraphANNISException
	 */
	public CountResult countExtra(Iterable<String> corpusNames, String query, QueryLanguage queryLanguage)
			throws GraphANNISException {
		CountResult result = new CountResult();
		result.documentCount = 0;
		result.matchCount = 0;
		CAPI.AnnisVec_AnnisCString c_corpusNames = CAPI.annis_vec_str_new();
		for (String cn : corpusNames) {
			CAPI.annis_vec_str_push(c_corpusNames, cn);
		}
		AnnisErrorListRef err = new AnnisErrorListRef();
		AnnisCountExtra resultForCorpus = CAPI.annis_cs_count_extra(instance, c_corpusNames, query,
				queryLanguage.capiVal, err);
		c_corpusNames.dispose();
		err.checkErrors();

		result.matchCount += resultForCorpus.matchCount;
		result.documentCount += resultForCorpus.documentCount;

		return result;
	}

	/**
	 * Find all results for a `query` and return the match ID for each result in
	 * default order.
	 * 
	 * The query is paginated and an offset and limit can be specified.
	 * 
	 * @param corpusNames   The name of the corpora to execute the query on.
	 * @param query         The query as string.
	 * @param queryLanguage The query language of the query (e.g. AQL).
	 * @param offset        Skip the <em>n</em> first results, where <em>n</em> is
	 *                      the offset.
	 * @param limit         Return at most <em>n</em> matches, where <em>n</em> is
	 *                      the limit.
	 * @return An array of node identifiers
	 * @throws GraphANNISException
	 */
	public String[] find(Iterable<String> corpusNames, String query, QueryLanguage queryLanguage, long offset,
			Optional<Long> limit) throws GraphANNISException {
		return find(corpusNames, query, queryLanguage, offset, limit, ResultOrder.Normal);
	}

	/**
	 * Find all results for a `query` and return the match ID for each result.
	 * 
	 * The query is paginated and an offset and limit can be specified.
	 * 
	 * @param corpusNames   The name of the corpora to execute the query on.
	 * @param query         The query as string.
	 * @param queryLanguage The query language of the query (e.g. AQL).
	 * @param offset        Skip the `n` first results, where `n` is the offset.
	 * @param limit         Return at most `n` matches, where `n` is the limit.
	 * @param order         Specify the order of the matches.
	 * @return An array of node identifiers
	 * @throws GraphANNISException
	 */
	public String[] find(Iterable<String> corpusNames, String query, QueryLanguage queryLanguage, long offset,
			Optional<Long> limit, ResultOrder order) throws GraphANNISException {

		ArrayList<String> result = new ArrayList<>();
		CAPI.AnnisVec_AnnisCString c_corpusNames = CAPI.annis_vec_str_new();
		for (String cn : corpusNames) {
			CAPI.annis_vec_str_push(c_corpusNames, cn);
		}
		AnnisErrorListRef err = new AnnisErrorListRef();
		LongByReference limit_ref = limit.isPresent() ? new LongByReference(limit.get()) : null;
		CAPI.AnnisVec_AnnisCString vec = CAPI.annis_cs_find(instance, c_corpusNames, query, queryLanguage.capiVal,
				offset, limit_ref, order.capiVal, err);
		c_corpusNames.dispose();
		err.checkErrors();

		final int vecSize = CAPI.annis_vec_str_size(vec).intValue();
		for (int i = 0; i < vecSize; i++) {
			result.add(CAPI.annis_vec_str_get(vec, new NativeLong(i)));
		}
		vec.dispose();

		return result.toArray(new String[0]);
	}

	/**
	 * Return the copy of a subgraph which includes the given list of node
	 * annotation identifiers, the nodes that cover the same token as the given
	 * nodes and all nodes that cover the token which are part of the defined
	 * context.
	 * 
	 * @param corpusName   The name of the corpus for which the subgraph should be
	 *                     generated from.
	 * @param nodeIDs      A set of node annotation identifiers describing the
	 *                     subgraph.
	 * @param ctxLeft      Left context in token distance to be included in the
	 *                     subgraph.
	 * @param ctxRight     Right context in token distance to be included in the
	 *                     subgraph.
	 * @param segmentation The name of the segmentation which should be used to as
	 *                     base for the context. Use {@link Optional#empty()} to
	 *                     define the context in the default token layer.
	 * @return The subgraph.
	 * @throws GraphANNISException
	 */
	public Graph subgraph(String corpusName, List<String> nodeIDs, long ctxLeft, long ctxRight,
			Optional<String> segmentation) throws GraphANNISException {
		CAPI.AnnisVec_AnnisCString c_node_ids = CAPI.annis_vec_str_new();
		for (String id : nodeIDs) {
			CAPI.annis_vec_str_push(c_node_ids, id);
		}

		AnnisErrorListRef err = new AnnisErrorListRef();
		CAPI.AnnisGraph graph = CAPI.annis_cs_subgraph(instance, corpusName, c_node_ids, new NativeLong(ctxLeft),
				new NativeLong(ctxRight), segmentation.orElse(null), err);
		err.checkErrors();

		c_node_ids.dispose();

		return new Graph(graph);
	}

	/**
	 * Return the copy of a subgraph which includes all nodes that belong to any of
	 * the given list of sub-corpus/document identifiers.
	 * 
	 * @param corpusName  The name of the corpus for which the subgraph should be
	 *                    generated from.
	 * @param documentIDs A set of sub-corpus/document identifiers describing the
	 *                    subgraph.
	 * @return The subgraph.
	 * @throws GraphANNISException
	 */
	public Graph subcorpusGraph(String corpusName, List<String> documentIDs) throws GraphANNISException {
		CAPI.AnnisVec_AnnisCString c_document_ids = CAPI.annis_vec_str_new();
		for (String id : documentIDs) {
			CAPI.annis_vec_str_push(c_document_ids, id);
		}

		Graph result = null;
		if (instance != null) {
			AnnisErrorListRef err = new AnnisErrorListRef();
			CAPI.AnnisGraph graph = CAPI.annis_cs_subcorpus_graph(instance, corpusName, c_document_ids, err);
			err.checkErrors();

			c_document_ids.dispose();
			result = new Graph(graph);
		}

		return result;
	}

	/**
	 * Return the copy of the graph of the corpus structure given by its name.
	 * 
	 * @param corpusName The name of the corpus.
	 * @return The corpus graph
	 * @throws GraphANNISException
	 */
	public Graph corpusGraph(String corpusName) throws GraphANNISException {
		if (instance != null) {
			AnnisErrorListRef err = new AnnisErrorListRef();
			CAPI.AnnisGraph graph = CAPI.annis_cs_corpus_graph(instance, corpusName, err);
			err.checkErrors();

			return new Graph(graph);
		}
		return null;
	}

	/**
	 * Return the copy of the graph of the corpus structure which includes all nodes
	 * matched by the given query.
	 * 
	 * @param corpusName    The name of the corpus.
	 * @param query         The query as string.
	 * @param queryLanguage The query language of the query (e.g. AQL).
	 * @return The corpus graph
	 * @throws GraphANNISException
	 */
	public Graph corpusGraphForQuery(String corpusName, String query, QueryLanguage queryLanguage)
			throws GraphANNISException {
		if (instance != null) {
			AnnisErrorListRef err = new AnnisErrorListRef();
			CAPI.AnnisGraph graph = CAPI.annis_cs_subgraph_for_query_with_ctype(instance, corpusName, query,
					queryLanguage.capiVal, ComponentType.PartOf.toInt(), err);
			err.checkErrors();

			return new Graph(graph);
		}
		return null;
	}

	/**
	 * Return the copy of a subgraph which includes all nodes matched by the given
	 * query.
	 * 
	 * @param corpusName    The name of the corpus.
	 * @param query         The query as string.
	 * @param queryLanguage The query language of the query (e.g. AQL).
	 * @return The subgraph
	 * @throws GraphANNISException
	 */
	public Graph subGraphForQuery(String corpusName, String query, QueryLanguage queryLanguage)
			throws GraphANNISException {
		if (instance != null) {
			AnnisErrorListRef err = new AnnisErrorListRef();
			CAPI.AnnisGraph graph = CAPI.annis_cs_subgraph_for_query(instance, corpusName, query, queryLanguage.capiVal,
					err);
			err.checkErrors();

			return new Graph(graph);
		}
		return null;
	}

	/**
	 * Execute a frequency query.
	 * 
	 * @param corpusNames              The name of the corpora to execute the query
	 *                                 on.
	 * @param query                    The query as string.
	 * @param queryLanguage            The query language of the query (e.g. AQL).
	 * @param frequencyQueryDefinition A comma seperated list of single frequency
	 *                                 definition items as string. Each frequency
	 *                                 definition must consist of two parts: the
	 *                                 name of referenced node and the (possible
	 *                                 qualified) annotation name or "tok" separated
	 *                                 by ":". E.g. a frequency definition like
	 * 
	 *                                 <pre>
	 *                                 1:tok,3:pos,4:tiger::pos
	 *                                 </pre>
	 * 
	 *                                 would extract the token value for the nodes
	 *                                 #1, the pos annotation for node #3 and the
	 *                                 pos annotation in the tiger namespace for
	 *                                 node #4.
	 * @return A list of frequency table entries.
	 * @throws GraphANNISException
	 */
	public List<FrequencyTableEntry<String>> frequency(Iterable<String> corpusNames, String query,
			QueryLanguage queryLanguage, String frequencyQueryDefinition) throws GraphANNISException {
		if (instance != null) {
			AnnisErrorListRef err = new AnnisErrorListRef();
			CAPI.AnnisVec_AnnisCString c_corpusNames = CAPI.annis_vec_str_new();
			for (String cn : corpusNames) {
				CAPI.annis_vec_str_push(c_corpusNames, cn);
			}
			CAPI.AnnisFrequencyTable_AnnisCString orig = CAPI.annis_cs_frequency(instance, c_corpusNames, query,
					queryLanguage.capiVal, frequencyQueryDefinition, err);
			c_corpusNames.dispose();
			err.checkErrors();

			if (orig != null) {
				List<FrequencyTableEntry<String>> result = new ArrayList<>();

				final int nrows = CAPI.annis_freqtable_str_nrows(orig).intValue();
				final int ncols = CAPI.annis_freqtable_str_ncols(orig).intValue();
				for (int i = 0; i < nrows; i++) {
					NativeLong count = CAPI.annis_freqtable_str_count(orig, new NativeLong(i));
					String[] tuple = new String[ncols];
					for (int c = 0; c < ncols; c++) {
						tuple[c] = CAPI.annis_freqtable_str_get(orig, new NativeLong(i), new NativeLong(c));
					}
					result.add(new FrequencyTableEntry<>(tuple, count.longValue()));
				}
				return result;
			}
		}
		return null;
	}

	/**
	 * Import a corpus from an external location on the file system into this corpus
	 * storage.
	 * 
	 * @param path       The location on the file system where the corpus data is
	 *                   located.
	 * @param format     The format in which this corpus data is stored.
	 * @param corpusName If not "null", override the name of the new corpus for file
	 *                   formats that already provide a corpus name.
	 * @param diskBased  If true, certain elements like the node annotation storage
	 *                   will be be disk-based instead of using in-memory
	 *                   representations.
	 * @throws GraphANNISException
	 */
	public void importFromFileSystem(String path, ImportFormat format, String corpusName, boolean diskBased)
			throws GraphANNISException {
		if (instance != null) {
			AnnisErrorListRef err = new AnnisErrorListRef();
			CAPI.annis_cs_import_from_fs(instance, path, format.capiVal, corpusName, diskBased, err);
			err.checkErrors();
		}
	}

	/**
	 * Delete a corpus from this corpus storage.
	 * 
	 * @param corpusName The name of the corpus to delete.
	 * @return "true" if the corpus was successfully deleted and "false" if no such
	 *         corpus existed.
	 * @throws GraphANNISException
	 */
	public boolean deleteCorpus(String corpusName) throws GraphANNISException {
		boolean result = false;
		if (instance != null) {
			AnnisErrorListRef err = new AnnisErrorListRef();
			result = CAPI.annis_cs_delete(instance, corpusName, err);
			err.checkErrors();
		}
		return result;
	}

	/**
	 * Unloads a corpus from the cache.
	 * 
	 * @param corpusName The name of the corpus to unload.
	 * @throws GraphANNISException
	 */
	public void unloadCorpus(String corpusName) throws GraphANNISException {
		if (instance != null) {
			AnnisErrorListRef err = new AnnisErrorListRef();
			CAPI.annis_cs_unload(instance, corpusName, err);
			err.checkErrors();
		}
	}

	/**
	 * Apply a sequence of updates to this graph for a corpus.
	 *
	 * It is ensured that the update process is atomic and that the changes are
	 * persisted to disk if the result no exception was thrown.
	 * 
	 * @param corpusName The name of the corpus to apply the updates on
	 * @param update     The sequence of updates.
	 * @throws GraphANNISException
	 */
	public void applyUpdate(String corpusName, GraphUpdate update) throws GraphANNISException {
		AnnisErrorListRef err = new AnnisErrorListRef();
		CAPI.annis_cs_apply_update(instance, corpusName, update.getInstance(), err);
		err.checkErrors();
	}

}

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

import com.sun.jna.NativeLong;

/**
 * An API for managing corpora stored in a common location on the file system.
 * 
 * @author Thomas Krause <thomaskrause@posteo.de>
 */
public class CorpusStorageManager {
	private final CAPI.AnnisCorpusStorage instance;

	private final Logger log = LoggerFactory.getLogger(CorpusStorageManager.class);

	public static class CountResult {
		public long matchCount;
		public long documentCount;
	}

	public static enum QueryLanguage {

		AQL(AnnisQueryLanguage.AQL), AQLQuirksV3(AnnisQueryLanguage.AQLQuirksV3);

		protected final int capiVal;

		private QueryLanguage(int capiVal) {
			this.capiVal = capiVal;
		}
	}

	public static enum ResultOrder {

		Normal(AnnisResultOrder.Normal), Inverted(AnnisResultOrder.Inverted), Randomized(AnnisResultOrder.Randomized),
		NotSorted(AnnisResultOrder.NotSorted);

		protected final int capiVal;

		private ResultOrder(int capiVal) {
			this.capiVal = capiVal;
		}
	}

	public static enum ImportFormat {

		RelANNIS(AnnisImportFormat.RelANNIS);

		protected final int capiVal;

		private ImportFormat(int capiVal) {
			this.capiVal = capiVal;
		}
	}

	public CorpusStorageManager(String dbDir) throws GraphANNISException {
		this(dbDir, null, false, LogLevel.Off);
	}

	public CorpusStorageManager(String dbDir, String logfile, boolean useParallel, LogLevel level)
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
		this.instance = CAPI.annis_cs_with_auto_cache_size(dbDir, useParallel);
	}

	public CorpusStorageManager(String dbDir, String logfile, long maxCacheSize, boolean useParallel, LogLevel level)
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
		this.instance = CAPI.annis_cs_with_max_cache_size(dbDir, maxCacheSize, useParallel);
	}

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

	public List<Annotation> listEdgeAnnotations(String corpusName, ComponentType component_type, String component_name,
			String component_layer, boolean listValues, boolean onlyMostFrequentValues) {
		List<Annotation> result = new LinkedList<>();
		if (instance != null) {
			CAPI.AnnisMatrix_AnnisCString orig = CAPI.annis_cs_list_edge_annotations(instance, corpusName,
					component_type.toInt(), component_name, component_layer, listValues, onlyMostFrequentValues);

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

	public List<Component> getAllComponentsByType(String corpusName, ComponentType ctype) {
		List<Component> result = new LinkedList<>();
		if (instance != null) {
			CAPI.AnnisVec_AnnisComponent orig = CAPI.annis_cs_list_components_by_type(instance, corpusName, ctype.toInt());

			for (int i = 0; i < CAPI.annis_vec_component_size(orig).intValue(); i++) {
				AnnisComponentConst cOrig = CAPI.annis_vec_component_get(orig, new NativeLong(i));
				Component c = new Component();
				c.setType(ctype);

				CharPointer cname = CAPI.annis_component_name(cOrig);
				c.setName(cname == null ? "" : cname.toString());

				CharPointer clayer = CAPI.annis_component_layer(cOrig);
				c.setLayer(clayer == null ? "" : clayer.toString());

				result.add(c);
			}
		}
		return result;
	}

	public boolean validateQuery(List<String> corpora, String query, QueryLanguage queryLanguage)
			throws GraphANNISException {
		boolean result = true;
		for (String corpusName : corpora) {
			AnnisErrorListRef err = new AnnisErrorListRef();
			if (CAPI.annis_cs_validate_query(instance, corpusName, query, queryLanguage.capiVal, err) == false) {
				result = false;
			}
			err.checkErrors();
		}
		return result;
	}

	public List<NodeDesc> getNodeDescriptions(String query, QueryLanguage queryLanguage) throws GraphANNISException {
		AnnisErrorListRef err = new AnnisErrorListRef();
		QueryAttributeDescription desc = CAPI.annis_cs_node_descriptions(instance, query, queryLanguage.capiVal, err);
		err.checkErrors();

		return desc.getList();

	}

	public long count(List<String> corpora, String query) throws GraphANNISException {
		return count(corpora, query, QueryLanguage.AQL);
	}

	public long count(List<String> corpora, String query, QueryLanguage queryLanguage) throws GraphANNISException {
		long result = 0l;
		for (String corpusName : corpora) {
			AnnisErrorListRef err = new AnnisErrorListRef();
			result += CAPI.annis_cs_count(instance, corpusName, query, queryLanguage.capiVal, err);
			err.checkErrors();
		}
		return result;
	}

	public CountResult countExtra(List<String> corpora, String query, QueryLanguage queryLanguage)
			throws GraphANNISException {
		CountResult result = new CountResult();
		result.documentCount = 0;
		result.matchCount = 0;
		for (String corpusName : corpora) {
			AnnisErrorListRef err = new AnnisErrorListRef();
			AnnisCountExtra resultForCorpus = CAPI.annis_cs_count_extra(instance, corpusName, query,
					queryLanguage.capiVal, err);
			err.checkErrors();

			result.matchCount += resultForCorpus.matchCount;
			result.documentCount += resultForCorpus.documentCount;
		}
		return result;
	}

	public String[] find(List<String> corpora, String query, long offset, long limit)
			throws GraphANNISException {
		return find(corpora, query, offset, limit, ResultOrder.Normal, QueryLanguage.AQL);
	}

	public String[] find(List<String> corpora, String query, long offset, long limit, QueryLanguage queryLanguage)
			throws GraphANNISException {
		return find(corpora, query, offset, limit, ResultOrder.Normal, queryLanguage);
	}

	public String[] find(List<String> corpora, String query, long offset, long limit, ResultOrder order)
			throws GraphANNISException {
		return find(corpora, query, offset, limit, order, QueryLanguage.AQL);

	}

	public String[] find(List<String> corpora, String query, long offset, long limit, ResultOrder order,
			QueryLanguage queryLanguage) throws GraphANNISException {

		ArrayList<String> result = new ArrayList<>();
		for (String corpusName : corpora) {
			AnnisErrorListRef err = new AnnisErrorListRef();
			CAPI.AnnisVec_AnnisCString vec = CAPI.annis_cs_find(instance, corpusName, query, queryLanguage.capiVal,
					offset, limit, order.capiVal, err);
			err.checkErrors();

			final int vecSize = CAPI.annis_vec_str_size(vec).intValue();
			for (int i = 0; i < vecSize; i++) {
				result.add(CAPI.annis_vec_str_get(vec, new NativeLong(i)));
			}
			vec.dispose();
		}

		return result.toArray(new String[0]);
	}

	public Graph subgraph(String corpusName, List<String> node_ids, long ctx_left, long ctx_right)
			throws GraphANNISException {
		CAPI.AnnisVec_AnnisCString c_node_ids = CAPI.annis_vec_str_new();
		for (String id : node_ids) {
			CAPI.annis_vec_str_push(c_node_ids, id);
		}

		AnnisErrorListRef err = new AnnisErrorListRef();
		CAPI.AnnisGraph graph = CAPI.annis_cs_subgraph(instance, corpusName, c_node_ids, new NativeLong(ctx_left),
				new NativeLong(ctx_right), err);
		err.checkErrors();

		c_node_ids.dispose();
		
		return new Graph(graph);
	}

	public Graph subcorpusGraph(String corpusName, List<String> document_ids) throws GraphANNISException {
		CAPI.AnnisVec_AnnisCString c_document_ids = CAPI.annis_vec_str_new();
		for (String id : document_ids) {
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

	public Graph corpusGraph(String corpusName) throws GraphANNISException {
		if (instance != null) {
			AnnisErrorListRef err = new AnnisErrorListRef();
			CAPI.AnnisGraph graph = CAPI.annis_cs_corpus_graph(instance, corpusName, err);
			err.checkErrors();

			return new Graph(graph);
		}
		return null;
	}

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

	public List<FrequencyTableEntry<String>> frequency(String corpusName, String query, String frequencyQueryDefinition,
			QueryLanguage queryLanguage) throws GraphANNISException {
		if (instance != null) {
			AnnisErrorListRef err = new AnnisErrorListRef();
			CAPI.AnnisFrequencyTable_AnnisCString orig = CAPI.annis_cs_frequency(instance, corpusName, query,
					queryLanguage.capiVal, frequencyQueryDefinition, err);
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

	public void importFromFileSystem(String path, ImportFormat format, String corpusName) throws GraphANNISException {
		if (instance != null) {
			AnnisErrorListRef err = new AnnisErrorListRef();
			CAPI.annis_cs_import_from_fs(instance, path, format.capiVal, corpusName, err);
			err.checkErrors();
		}
	}

	public boolean deleteCorpus(String corpusName) throws GraphANNISException {
		boolean result = false;
		if (instance != null) {
			AnnisErrorListRef err = new AnnisErrorListRef();
			result = CAPI.annis_cs_delete(instance, corpusName, err);
			err.checkErrors();
		}
		return result;
	}

	public void applyUpdate(String corpusName, GraphUpdate update) throws GraphANNISException {
		AnnisErrorListRef err = new AnnisErrorListRef();
		CAPI.annis_cs_apply_update(instance, corpusName, update.getInstance(), err);
		err.checkErrors();
	}

}

/*
   Copyright 2017 Thomas Krause <krauseto@hu-berlin.de>

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package org.corpus_tools.graphannis.capi;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import com.sun.jna.ptr.LongByReference;

public class CAPI implements Library {

	static {
		Native.register(CAPI.class, "graphannis");
	}

	public static class AnnisCorpusStorage extends PointerType {
		public synchronized void dispose() {
			try {
				if (this.getPointer() != Pointer.NULL) {
					CAPI.annis_cs_free(this.getPointer());
				}
			} finally {
				this.setPointer(Pointer.NULL);
			}
		}

		@Override
		protected void finalize() throws Throwable {
			this.dispose();
			super.finalize();
		}
	}

	public static class AnnisGraphUpdate extends AnnisPtr {
	}

	public static class AnnisGraph extends AnnisPtr {
	}

	public static class AnnisVec_AnnisCString extends AnnisPtr {
	}

	public static class AnnisAnnotation extends PointerType {

	}

	public static class AnnisVec_AnnisAnnotation extends AnnisPtr {

	}

	public static class AnnisIterPtr_AnnisNodeID extends AnnisPtr {
	}

	public static class AnnisComponentConst extends PointerType {
	}

	public static class AnnisVec_AnnisComponent extends AnnisPtr {
	}

	public static class AnnisVec_AnnisEdge extends AnnisPtr {
	}

	public static class AnnisMatrix_AnnisCString extends AnnisPtr {
	}

	public static class AnnisFrequencyTable_AnnisCString extends AnnisPtr {
	}

	// general functions

	protected static native void annis_free(Pointer ptr);

	public static native void annis_str_free(Pointer ptr);

	public static native NativeLong annis_error_size(Pointer ptr);

	public static native String annis_error_get_msg(Pointer ptr, NativeLong i);

	public static native String annis_error_get_kind(Pointer ptr, NativeLong i);

	public static native void annis_init_logging(String logfile, int level, AnnisErrorListRef err);

	// vector and iterator functions
	public static native NativeLong annis_vec_str_size(AnnisVec_AnnisCString ptr);

	public static native String annis_vec_str_get(AnnisVec_AnnisCString ptr, NativeLong i);

	public static native AnnisVec_AnnisCString annis_vec_str_new();

	public static native void annis_vec_str_push(AnnisVec_AnnisCString ptr, String v);

	public static native CharPointer annis_annotation_ns(AnnisAnnotation ptr);

	public static native CharPointer annis_annotation_name(AnnisAnnotation ptr);

	public static native CharPointer annis_annotation_val(AnnisAnnotation ptr);

	public static native NativeLong annis_vec_annotation_size(AnnisVec_AnnisAnnotation ptr);

	public static native AnnisAnnotation annis_vec_annotation_get(AnnisVec_AnnisAnnotation ptr, NativeLong i);

	public static native NativeLong annis_vec_component_size(AnnisVec_AnnisComponent ptr);

	public static native AnnisComponentConst annis_vec_component_get(AnnisVec_AnnisComponent ptr, NativeLong i);

	public static native NativeLong annis_vec_edge_size(AnnisVec_AnnisEdge ptr);

	public static native AnnisEdge annis_vec_edge_get(AnnisVec_AnnisEdge ptr, NativeLong i);

    public static native NodeIDByRef annis_iter_nodeid_next(AnnisIterPtr_AnnisNodeID ptr,
        AnnisErrorListRef err);

	public static native String annis_matrix_str_get(AnnisMatrix_AnnisCString ptr, NativeLong row, NativeLong col);

	public static native NativeLong annis_matrix_str_ncols(AnnisMatrix_AnnisCString ptr);

	public static native NativeLong annis_matrix_str_nrows(AnnisMatrix_AnnisCString ptr);

	public static native NativeLong annis_freqtable_str_count(AnnisFrequencyTable_AnnisCString ptr, NativeLong row);

	public static native String annis_freqtable_str_get(AnnisFrequencyTable_AnnisCString ptr, NativeLong row,
			NativeLong col);

	public static native NativeLong annis_freqtable_str_ncols(AnnisFrequencyTable_AnnisCString ptr);

	public static native NativeLong annis_freqtable_str_nrows(AnnisFrequencyTable_AnnisCString ptr);

	public static native NativeLong annis_vec_qattdesc_size(QueryAttributeDescription ptr);

	public static native NativeLong annis_vec_qattdesc_get_component_nr(QueryAttributeDescription ptr, NativeLong i);

	public static native CharPointer annis_vec_qattdesc_get_aql_fragment(QueryAttributeDescription ptr, NativeLong i);

	public static native CharPointer annis_vec_qattdesc_get_variable(QueryAttributeDescription ptr, NativeLong i);

	public static native CharPointer annis_vec_qattdesc_get_anno_name(QueryAttributeDescription ptr, NativeLong i);

	// corpus storage class

	public static native AnnisCorpusStorage annis_cs_with_auto_cache_size(String db_dir, boolean use_parallel_joins,
			AnnisErrorListRef err);

	public static native AnnisCorpusStorage annis_cs_with_max_cache_size(String db_dir, long max_cache_size,
			boolean use_parallel_joins, AnnisErrorListRef err);

	protected static native void annis_cs_free(Pointer ptr);

	public static native AnnisVec_AnnisCString annis_cs_list(AnnisCorpusStorage cs, AnnisErrorListRef err);

	public static native long annis_cs_count(AnnisCorpusStorage cs, AnnisVec_AnnisCString corpusNames, String query,
			int queryLanguage, AnnisErrorListRef err);

	public static native AnnisCountExtra.ByValue annis_cs_count_extra(AnnisCorpusStorage cs,
			AnnisVec_AnnisCString corpusNames, String query, int queryLanguage, AnnisErrorListRef err);

	public static native AnnisVec_AnnisCString annis_cs_find(AnnisCorpusStorage cs, AnnisVec_AnnisCString corpusNames,
			String query, int query_language, long offset, LongByReference limit, int order, AnnisErrorListRef err);

	public static native AnnisGraph annis_cs_subgraph(AnnisCorpusStorage cs, String corpusName,
			AnnisVec_AnnisCString node_ids, NativeLong ctx_left, NativeLong ctx_right, String segmentation,
			AnnisErrorListRef err);

	public static native AnnisGraph annis_cs_subcorpus_graph(AnnisCorpusStorage cs, String corpusName,
			AnnisVec_AnnisCString corpus_ids, AnnisErrorListRef err);

	public static native AnnisGraph annis_cs_corpus_graph(AnnisCorpusStorage cs, String corpusName,
			AnnisErrorListRef err);

	public static native AnnisGraph annis_cs_subgraph_for_query(AnnisCorpusStorage cs, String corpusName, String query,
			int queryLanguage, AnnisErrorListRef err);

	public static native AnnisGraph annis_cs_subgraph_for_query_with_ctype(AnnisCorpusStorage cs, String corpusName,
			String query, int queryLanguage, int ctype, AnnisErrorListRef err);

	public static native AnnisFrequencyTable_AnnisCString annis_cs_frequency(AnnisCorpusStorage cs,
			AnnisVec_AnnisCString corpusNames, String query, int queryLanguage, String frequencyQueryDefinition,
			AnnisErrorListRef err);

	public static native AnnisVec_AnnisComponent annis_cs_list_components_by_type(AnnisCorpusStorage cs,
        String corpusName, int ctype, AnnisErrorListRef err);

	public static native AnnisMatrix_AnnisCString annis_cs_list_node_annotations(AnnisCorpusStorage cs,
        String corpusName, boolean listValues, boolean onlyMostFrequentValues,
        AnnisErrorListRef err);

	public static native AnnisMatrix_AnnisCString annis_cs_list_edge_annotations(AnnisCorpusStorage cs,
			String corpusName, int component_type, String component_name, String component_layer, boolean listValues,
        boolean onlyMostFrequentValues, AnnisErrorListRef err);

	public static native void annis_cs_apply_update(AnnisCorpusStorage cs, String corpusName, AnnisGraphUpdate update,
			AnnisErrorListRef err);

	public static native CharPointer annis_cs_import_from_fs(AnnisCorpusStorage cs, String path, int format,
			String corpusName, boolean diskBased, AnnisErrorListRef err);

	public static native boolean annis_cs_validate_query(AnnisCorpusStorage cs, AnnisVec_AnnisCString corpusNames,
			String query, int queryLanguage, AnnisErrorListRef err);

	public static native QueryAttributeDescription annis_cs_node_descriptions(AnnisCorpusStorage cs, String query,
			int queryLanguage, AnnisErrorListRef err);

	public static native boolean annis_cs_delete(AnnisCorpusStorage cs, String corpusName, AnnisErrorListRef err);

	public static native void annis_cs_unload(AnnisCorpusStorage cs, String corpusName, AnnisErrorListRef err);

	// graph update class

	public static native AnnisGraphUpdate annis_graphupdate_new();

	public static native void annis_graphupdate_add_node(AnnisGraphUpdate ptr, String node_name, String node_type,
			AnnisErrorListRef err);

	public static native void annis_graphupdate_delete_node(AnnisGraphUpdate ptr, String node_name,
			AnnisErrorListRef err);

	public static native void annis_graphupdate_add_node_label(AnnisGraphUpdate ptr, String node_name, String anno_ns,
			String anno_name, String anno_value, AnnisErrorListRef err);

	public static native void annis_graphupdate_delete_node_label(AnnisGraphUpdate ptr, String node_name,
			String anno_ns, String anno_name, AnnisErrorListRef err);

	public static native void annis_graphupdate_add_edge(AnnisGraphUpdate ptr, String source_node, String target_node,
			String layer, String component_type, String component_name, AnnisErrorListRef err);

	public static native void annis_graphupdate_delete_edge(AnnisGraphUpdate ptr, String source_node,
			String target_node, String layer, String component_type, String component_name, AnnisErrorListRef err);

	public static native void annis_graphupdate_add_edge_label(AnnisGraphUpdate ptr, String source_node,
			String target_node, String layer, String component_type, String component_name, String anno_ns,
			String anno_name, String anno_value, AnnisErrorListRef err);

	public static native void annis_graphupdate_delete_edge_label(AnnisGraphUpdate ptr, String source_node,
			String target_node, String layer, String component_type, String component_name, String anno_ns,
			String anno_name, AnnisErrorListRef err);

	// GraphDB classes

	public static native CharPointer annis_component_layer(AnnisComponentConst component);

	public static native CharPointer annis_component_name(AnnisComponentConst component);

	public static native int annis_component_type(AnnisComponentConst component);

    public static native AnnisVec_AnnisAnnotation annis_graph_annotations_for_node(AnnisGraph g,
        NodeID nodeID, AnnisErrorListRef err);

	public static native AnnisIterPtr_AnnisNodeID annis_graph_nodes_by_type(AnnisGraph g, String node_type);

	public static native AnnisVec_AnnisComponent annis_graph_all_components(AnnisGraph g);

	public static native AnnisVec_AnnisComponent annis_graph_all_components_by_type(AnnisGraph g, int ctype);

	public static native AnnisVec_AnnisEdge annis_graph_outgoing_edges(AnnisGraph g, NodeID source,
        AnnisComponentConst component, AnnisErrorListRef err);

	public static native AnnisVec_AnnisAnnotation annis_graph_annotations_for_edge(AnnisGraph g, AnnisEdge.ByValue edge,
        AnnisComponentConst component, AnnisErrorListRef err);
}

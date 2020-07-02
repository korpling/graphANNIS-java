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
package org.corpus_tools.graphannis.capi;

/**
 * An enum of all supported input formats of graphANNIS.
 * 
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
public interface AnnisImportFormat {
	/** Legacy relANNIS import file format */
	public static final int RelANNIS = 0;

  /**
   * [GraphML](http://graphml.graphdrawing.org/) based export-format, suitable to be imported from other graph databases.
   * This format follows the extensions/conventions of the Neo4j [GraphML module](https://neo4j.com/docs/labs/apoc/current/import/graphml/).
   */
	public static final int GraphML = 1;

}

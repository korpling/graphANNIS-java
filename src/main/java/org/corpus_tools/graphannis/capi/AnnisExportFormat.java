/*
 * Copyright 2018 Thomas Krause.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.corpus_tools.graphannis.capi;

/**
 * An enum of all supported export formats of graphANNIS.
 * 
 * @author Thomas Krause {@literal <thomas.krause@hu-berlin.de>}
 */
public interface AnnisExportFormat {
  /**
   * <a href="http://graphml.graphdrawing.org/">GraphML</a> based export-format, suitable to be
   * imported into other graph databases.
   * 
   * <p>
   * This format follows the extensions/conventions of the Neo4j
   * <a href="https://neo4j.com/docs/labs/apoc/current/import/graphml/">GraphML module</a>).
   * </p>
   **/
  public static final int GraphML = 0;
  /**
   * Like {@link #GraphML} but compressed as ZIP file. Linked files are also copied into the ZIP
   * file.
   */
  public static final int GraphMLZip = 1;
  /**
   * Like {@link #GraphML} but using a directory with multiple GraphML files, each for one corpus.
   */
  public static final int GraphMLDirectory = 2;
}


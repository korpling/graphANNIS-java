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
 *
 * @author Thomas Krause {@literal <thomaskrause@posteo.de>}
 */
public interface AnnisQueryLanguage {

	public static final int AQL = 0;
	/** Emulates the (sometimes problematic) behavior of AQL used in ANNIS 3 */
	public static final int AQLQuirksV3 = 1;

}

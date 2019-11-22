/*
 * Copyright 2016 Thomas Krause.
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

import com.google.common.io.Files;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.corpus_tools.graphannis.CorpusStorageManager.QueryLanguage;
import org.corpus_tools.graphannis.CorpusStorageManager.ResultOrder;
import org.corpus_tools.graphannis.errors.GraphANNISException;
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SCorpus;
import org.corpus_tools.salt.common.SCorpusGraph;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.common.STextualDS;
import org.corpus_tools.salt.common.STextualRelation;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.common.SaltProject;
import org.corpus_tools.salt.samples.SampleGenerator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author thomas
 */
public class SaltImportTest {
	private CorpusStorageManager storage;

	public SaltImportTest() {
	}

	@BeforeClass
	public static void setUpClass() {
	}

	@AfterClass
	public static void tearDownClass() {
	}

	@Before
	public void setUp() throws GraphANNISException {
		File tmpDir = Files.createTempDir();

		storage = new CorpusStorageManager(tmpDir.getAbsolutePath());
	}

	@After
	public void tearDown() {
	}

	/**
	 * Test of map method, of class SaltImport.
	 */
	@Test
	public void testMapComplexExample() throws GraphANNISException {

		SDocument doc = SaltFactory.createSDocument();

		SampleGenerator.createTokens(doc);
		SampleGenerator.createMorphologyAnnotations(doc);
		SampleGenerator.createInformationStructureSpan(doc);
		SampleGenerator.createInformationStructureAnnotations(doc);
		SampleGenerator.createSyntaxStructure(doc);
		SampleGenerator.createSyntaxAnnotations(doc);
		SampleGenerator.createAnaphoricAnnotations(doc);
		SampleGenerator.createDependencies(doc);

		GraphUpdate result = new SaltImport().map(doc.getDocumentGraph()).finish();

		storage.applyUpdate("testCorpus", result);

		String corpus = "testCorpus";

		assertEquals(26, storage.count("node", QueryLanguage.AQL, corpus));

		// test that the token are present and have the correct span values
		assertEquals(11, storage.count("tok", QueryLanguage.AQL, corpus));
		assertEquals(1, storage.count("tok=\"Is\"", QueryLanguage.AQL, corpus));
		assertEquals(1, storage.count("tok=\"this\"", QueryLanguage.AQL, corpus));
		assertEquals(1, storage.count("tok=\"example\"", QueryLanguage.AQL, corpus));
		assertEquals(1, storage.count("tok=\"more\"", QueryLanguage.AQL, corpus));
		assertEquals(1, storage.count("tok=\"complicated\"", QueryLanguage.AQL, corpus));
		assertEquals(1, storage.count("tok=\"than\"", QueryLanguage.AQL, corpus));
		assertEquals(1, storage.count("tok=\"it\"", QueryLanguage.AQL, corpus));
		assertEquals(1, storage.count("tok=\"appears\"", QueryLanguage.AQL, corpus));
		assertEquals(1, storage.count("tok=\"to\"", QueryLanguage.AQL, corpus));
		assertEquals(1, storage.count("tok=\"be\"", QueryLanguage.AQL, corpus));
		assertEquals(1, storage.count("tok=\"?\"", QueryLanguage.AQL, corpus));

		// test that the token annotations have been added
		assertEquals(1, storage.count("pos=\"VBZ\" _=_ \"Is\"", QueryLanguage.AQL, corpus));
		assertEquals(1, storage.count("pos=\"DT\" _=_ \"this\"", QueryLanguage.AQL, corpus));
		assertEquals(1, storage.count("pos=\"NN\" _=_ \"example\"", QueryLanguage.AQL, corpus));
		assertEquals(1, storage.count("pos=\"RBR\" _=_ \"more\"", QueryLanguage.AQL, corpus));
		assertEquals(1, storage.count("pos=\"JJ\" _=_ \"complicated\"", QueryLanguage.AQL, corpus));
		assertEquals(1, storage.count("pos=\"IN\" _=_ \"than\"", QueryLanguage.AQL, corpus));
		assertEquals(1, storage.count("pos=\"PRP\" _=_ \"it\"", QueryLanguage.AQL, corpus));
		assertEquals(1, storage.count("pos=\"VBZ\" _=_ \"appears\"", QueryLanguage.AQL, corpus));
		assertEquals(1, storage.count("pos=\"TO\" _=_ \"to\"", QueryLanguage.AQL, corpus));
		assertEquals(1, storage.count("pos=\"VB\" _=_ \"be\"", QueryLanguage.AQL, corpus));
		assertEquals(1, storage.count("pos=\".\" _=_ \"?\"", QueryLanguage.AQL, corpus));

		// test that the precedence works for the token
		assertEquals(1, storage.count(
				"\"Is\" . \"this\" . \"example\" . \"more\" . \"complicated\" . \"than\" . \"it\" . \"appears\" . "
						+ "\"to\" . \"be\" . \"?\"",
				QueryLanguage.AQL, corpus));

		// test that coverage works
		assertEquals(1, storage.count("Inf-Struct=\"contrast-focus\" _o_ \"Is\"", QueryLanguage.AQL, corpus));
		assertEquals(1, storage.count("Inf-Struct=\"topic\" _o_ \"this\"", QueryLanguage.AQL, corpus));
		assertEquals(1, storage.count("Inf-Struct=\"topic\" _o_ \"example\"", QueryLanguage.AQL, corpus));
		assertEquals(1, storage.count("Inf-Struct=\"topic\" _o_ \"more\"", QueryLanguage.AQL, corpus));
		assertEquals(1, storage.count("Inf-Struct=\"topic\" _o_ \"complicated\"", QueryLanguage.AQL, corpus));
		assertEquals(1, storage.count("Inf-Struct=\"topic\" _o_ \"than\"", QueryLanguage.AQL, corpus));
		assertEquals(1, storage.count("Inf-Struct=\"topic\" _o_ \"it\"", QueryLanguage.AQL, corpus));
		assertEquals(1, storage.count("Inf-Struct=\"topic\" _o_ \"appears\"", QueryLanguage.AQL, corpus));
		assertEquals(1, storage.count("Inf-Struct=\"topic\" _o_ \"to\"", QueryLanguage.AQL, corpus));
		assertEquals(1, storage.count("Inf-Struct=\"topic\" _o_ \"be\"", QueryLanguage.AQL, corpus));
		assertEquals(1, storage.count("Inf-Struct=\"topic\" _o_ \"?\"", QueryLanguage.AQL, corpus));

		// test some of the dominance edges
		assertEquals(1, storage.count("const=\"ROOT\" > const=\"SQ\" > \"Is\"", QueryLanguage.AQL, corpus));
		assertEquals(1, storage.count("const=\"SQ\" >* \"this\"", QueryLanguage.AQL, corpus));

		// test some of the pointing relations
		assertEquals(1, storage.count("\"it\" ->anaphoric node _o_ \"example\"", QueryLanguage.AQL, corpus));
		assertEquals(9, storage.count("tok ->null tok", QueryLanguage.AQL, corpus));
		assertEquals(1, storage.count("\"complicated\" ->null[dependency=\"cop\"] \"Is\"", QueryLanguage.AQL, corpus));
	}

	@Test
	public void testTwoDocumentsSameNodeName() throws GraphANNISException {

		SaltProject project = SaltFactory.createSaltProject();
		SCorpusGraph corpusGraph = project.createCorpusGraph();

		SCorpus root = corpusGraph.createCorpus(null, "root");

		// add two documents which have a token with the same name
		SDocument doc1 = corpusGraph.createDocument(root, "doc1");
		doc1.setDocumentGraph(SaltFactory.createSDocumentGraph());
		STextualDS text1 = doc1.getDocumentGraph().createTextualDS("abc");
		SToken tok1 = SaltFactory.createSToken();
		tok1.setName("MyToken");
		doc1.getDocumentGraph().addNode(tok1);

		STextualRelation textRel1 = SaltFactory.createSTextualRelation();
		textRel1.setSource(tok1);
		textRel1.setTarget(text1);
		textRel1.setStart(0);
		textRel1.setEnd(2);
		doc1.getDocumentGraph().addRelation(textRel1);

		SDocument doc2 = corpusGraph.createDocument(root, "doc2");
		doc2.setDocumentGraph(SaltFactory.createSDocumentGraph());
		STextualDS text2 = doc2.getDocumentGraph().createTextualDS("abc");
		SToken tok2 = SaltFactory.createSToken();
		tok2.setName("MyToken");
		doc2.getDocumentGraph().addNode(tok2);

		STextualRelation textRel2 = SaltFactory.createSTextualRelation();
		textRel2.setSource(tok2);
		textRel2.setTarget(text2);
		textRel2.setStart(0);
		textRel2.setEnd(2);
		doc2.getDocumentGraph().addRelation(textRel2);

		doc2.getDocumentGraph().addNode(tok2);

		GraphUpdate result1 = new SaltImport().map(doc1.getDocumentGraph()).finish();
		storage.applyUpdate("root", result1);

		GraphUpdate result2 = new SaltImport().map(doc2.getDocumentGraph()).finish();
		storage.applyUpdate("root", result2);

		// test that both token have been added

		Set<String> matches = new HashSet<>();

		String[] result = storage.find("tok", QueryLanguage.AQL, 0, Optional.empty(), ResultOrder.Normal, "root");
		assertEquals(2, result.length);
		for (int i = 0; i < 2; i++) {
			matches.add(result[i]);
		}
		assertEquals(2, matches.size());
		Assert.assertTrue(matches.contains("salt:/root/doc1#MyToken"));
		Assert.assertTrue(matches.contains("salt:/root/doc2#MyToken"));

	}

}

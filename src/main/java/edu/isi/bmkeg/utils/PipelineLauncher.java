package edu.isi.bmkeg.utils;

import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.jcas.JCas;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.pipeline.JCasIterable;

public class PipelineLauncher 
{

	
	public PipelineLauncher(){
	
		
	}

	/**
	 * Run the CollectionReader and AnalysisEngines as a pipeline.
	 * 
	 * @param reader
	 *            The CollectionReader that loads the documents into the CAS.
	 * @param descs
	 *            Primitive AnalysisEngineDescriptions that process the CAS, in order. If you have a
	 *            mix of primitive and aggregate engines, then please create the AnalysisEngines
	 *            yourself and call the other runPipeline method.
	 * @throws UIMAException 
	 * @throws IOException 
	 */
	public static void runPipeline(CollectionReader reader, AnalysisEngineDescription... descs)
		throws UIMAException, IOException
	{
		AnalysisEngine[] engines = createEngines(descs);
		runPipeline(reader, engines);
	}

	private static AnalysisEngine[] createEngines(AnalysisEngineDescription... descs)
		throws UIMAException
	{
		AnalysisEngine[] engines = new AnalysisEngine[descs.length];
		for (int i = 0; i < engines.length; ++i) {
			if (descs[i].isPrimitive()) {
				engines[i] = AnalysisEngineFactory.createPrimitive(descs[i]);
			}
			else {
				engines[i] = AnalysisEngineFactory.createAggregate(descs[i]);
			}
		}
		return engines;

	}

	/**
	 * Provides a simple way to run a pipeline for a given collection reader and sequence of analysis engines
	 * @param reader a collection reader 
	 * @param engines a sequence of analysis engines
	 * @throws UIMAException
	 * @throws IOException
	 */
	public static void runPipeline(CollectionReader reader, AnalysisEngine... engines)
		throws UIMAException, IOException
	{
		for (JCas jCas : new JCasIterable(reader, engines)) {
			assert jCas != null;
		}
		for (AnalysisEngine engine : engines) {
			engine.collectionProcessComplete();
		}
		reader.close();
	}

	/**
	 * This method allows you to run a sequence of analysis engines over a jCas
	 * 
	 * @param jCas
	 *            the jCas to process
	 * @param descs
	 *            a sequence of analysis engines to run on the jCas
	 * @throws UIMAException
	 * @throws IOException
	 */
	public static void runPipeline(JCas jCas, AnalysisEngineDescription... descs)
		throws UIMAException, IOException
	{
		AnalysisEngine[] engines = createEngines(descs);
		runPipeline(jCas, engines);
	}

	/**
	 * This method allows you to run a sequence of analysis engines over a jCas
	 * 
	 * @param jCas
	 *            the jCas to process
	 * @param engines
	 *            a sequence of analysis engines to run on the jCas
	 * @throws UIMAException
	 * @throws IOException
	 */
	public static void runPipeline(JCas jCas, AnalysisEngine... engines)
		throws UIMAException, IOException
	{
		for (AnalysisEngine engine : engines) {
			engine.process(jCas);
		}

		for (AnalysisEngine engine : engines) {
			engine.collectionProcessComplete();
		}
	}


}

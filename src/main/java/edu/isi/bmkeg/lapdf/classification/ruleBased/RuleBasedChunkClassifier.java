package edu.isi.bmkeg.lapdf.classification.ruleBased;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.DecisionTableConfiguration;
import org.drools.builder.DecisionTableInputType;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.compiler.DecisionTableFactory;
import org.drools.definition.KnowledgePackage;
import org.drools.io.Resource;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatelessKnowledgeSession;

import edu.isi.bmkeg.lapdf.bin.CommandLineTool;
import edu.isi.bmkeg.lapdf.classification.Classifier;
import edu.isi.bmkeg.lapdf.features.ChunkFeatures;
import edu.isi.bmkeg.lapdf.model.ChunkBlock;
import edu.isi.bmkeg.lapdf.model.factory.AbstractModelFactory;
/**
 * Rule based classification of blocks using drools. 
 * @author cartic
 *
 */
public class RuleBasedChunkClassifier implements Classifier<ChunkBlock> {
	
	private static Logger logger = Logger.getLogger(RuleBasedChunkClassifier.class);

	private StatelessKnowledgeSession kSession;
	
	private AbstractModelFactory modelFactory;
	
	private KnowledgeBase kbase;
	
	private void reportCompiledRules(String droolsFileName, 
			DecisionTableConfiguration dtableconfiguration) throws IOException  {
		
		String rules = DecisionTableFactory.loadFromInputStream(ResourceFactory.newFileResource(droolsFileName).getInputStream(), dtableconfiguration);
		logger.debug(rules);
		
	}
	
	public RuleBasedChunkClassifier(String droolsFileName,
			AbstractModelFactory modelFactory) throws IOException  {

		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
		kbase = KnowledgeBaseFactory.newKnowledgeBase();
		
		if(droolsFileName.endsWith(".csv")) {
			
			DecisionTableConfiguration dtableconfiguration =
				KnowledgeBuilderFactory.newDecisionTableConfiguration();
			
			dtableconfiguration.setInputType( DecisionTableInputType.CSV );
			
			Resource xlsRes = ResourceFactory.newFileResource( droolsFileName );
			
			kbuilder.add( xlsRes, ResourceType.DTABLE, dtableconfiguration);
			
			reportCompiledRules(droolsFileName,dtableconfiguration);
		
		} else if(droolsFileName.endsWith(".xls")) {
		
			DecisionTableConfiguration dtableconfiguration =
				KnowledgeBuilderFactory.newDecisionTableConfiguration();
			
			dtableconfiguration.setInputType( DecisionTableInputType.XLS );
			
			Resource xlsRes = ResourceFactory.newFileResource( droolsFileName );
			
			kbuilder.add( xlsRes, ResourceType.DTABLE, dtableconfiguration);
			
			reportCompiledRules(droolsFileName,dtableconfiguration);
		
		} else if( droolsFileName.endsWith(".drl")) {
			
			kbuilder.add(ResourceFactory.newFileResource(droolsFileName),
					ResourceType.DRL);
			
		}

		if (kbuilder.hasErrors()) {
			logger.error(kbuilder.getErrors());
			return;
		}
		
		ArrayList<KnowledgePackage> kpkgs = new ArrayList<KnowledgePackage>(
				kbuilder.getKnowledgePackages());
		
		kbase.addKnowledgePackages(kpkgs);
		
		this.modelFactory = modelFactory;
	
	}

	@Override
	public void classify(List<ChunkBlock> blockList) {
		
		this.kSession = kbase.newStatelessKnowledgeSession();
		for (ChunkBlock chunk : blockList) {
			kSession.setGlobal("chunk", chunk);
			kSession.execute(new ChunkFeatures(chunk, modelFactory));
		}
		this.kSession = null;

	}

}

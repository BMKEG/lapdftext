package edu.isi.bmkeg.lapdf.text;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import edu.isi.bmkeg.lapdf.controller.LapdfEngine;
import edu.isi.bmkeg.lapdf.model.ChunkBlock;
import edu.isi.bmkeg.lapdf.model.LapdfDocument;
import edu.isi.bmkeg.lapdf.model.PageBlock;
import edu.isi.bmkeg.lapdf.model.ordering.SpatialOrdering;
import edu.isi.bmkeg.utils.ReadWriteTextFileWithEncoding;

public class SectionsTextWriter implements TextWriter
{

	private StringBuilder text;
	
	private List<Set<String>> stack;;

	private LapdfEngine engine;
	
	public SectionsTextWriter() throws Exception {
		
		this.stack = new ArrayList<Set<String>>();	
		this.engine = new LapdfEngine();
	
	}

	public void addToStack(Set<String> sections) {
		this.stack.add(sections);
	}
	
	@Override
	public void write(LapdfDocument document, String outputFilename) 
			throws IOException, FileNotFoundException {
		
		this.text = new StringBuilder();

		Iterator<Set<String>> it = this.stack.iterator();
		while( it.hasNext() ) {
			Set<String> sections = it.next();
			
			this.text.append( 
					this.engine.readClassifiedText(document, sections)
					);
			
		}
				
		ReadWriteTextFileWithEncoding.write(
				outputFilename, 
				TextWriter.UTF_8, 
				text.toString());

	}




}

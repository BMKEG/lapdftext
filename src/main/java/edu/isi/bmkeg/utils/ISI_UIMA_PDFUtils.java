package edu.isi.bmkeg.utils;

import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;

import edu.isi.bmkeg.pdf.DocumentInformation;


public class ISI_UIMA_PDFUtils
{
	/**
	 * Set alternate document IDS
	 * @param jcas
	 * @param s StringArray containing the IDs
	 */
	public static void setDocumentSecondaryIDs(JCas jcas,String s){
		DocumentInformation docInfo;
		FSIterator it = jcas.getAnnotationIndex(DocumentInformation.type).iterator();//jcas.getJFSIndexRepository().getAnnotationIndex(ISIDocumentInformation.type).iterator();
		if (it.hasNext()) { /* there should be at most one DocumentInformation annotation */
			docInfo = (DocumentInformation) it.next();
		}else{
			docInfo = new DocumentInformation(jcas);
			docInfo.addToIndexes();
		}
		docInfo.setLocalDocumentId(s);
	}

	/**
	 * get alternate document IDs
	 * @param jcas
	 * @return StringArray containing the IDs
	 */
	public static String getDocumentSecondaryID(JCas jcas) {
		DocumentInformation docInfo;
		FSIterator it = jcas.getAnnotationIndex(DocumentInformation.type).iterator();//jcas.getJFSIndexRepository().getAnnotationIndex(ISIDocumentInformation.type).iterator();
		if (it.hasNext()) { /* there should be at most one DocumentInformation annotation */
			docInfo = (DocumentInformation) it.next();
			return docInfo.getLocalDocumentId();
		} else {
			System.err.println("No secondary id  found, returning -1.");
			return "-1";
		}
	}

	
	
}

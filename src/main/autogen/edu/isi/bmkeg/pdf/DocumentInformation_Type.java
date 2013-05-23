
/* First created by JCasGen Mon Jul 09 14:06:45 PDT 2012 */
package edu.isi.bmkeg.pdf;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.FSGenerator;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;
import org.apache.uima.jcas.tcas.Annotation_Type;

/** 
 * Updated by JCasGen Mon Jul 09 14:06:45 PDT 2012
 * @generated */
public class DocumentInformation_Type extends Annotation_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (DocumentInformation_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = DocumentInformation_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new DocumentInformation(addr, DocumentInformation_Type.this);
  			   DocumentInformation_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new DocumentInformation(addr, DocumentInformation_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = DocumentInformation.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("edu.isi.bmkeg.pdf.DocumentInformation");
 
  /** @generated */
  final Feature casFeat_localDocumentId;
  /** @generated */
  final int     casFeatCode_localDocumentId;
  /** @generated */ 
  public String getLocalDocumentId(int addr) {
        if (featOkTst && casFeat_localDocumentId == null)
      jcas.throwFeatMissing("localDocumentId", "edu.isi.bmkeg.pdf.DocumentInformation");
    return ll_cas.ll_getStringValue(addr, casFeatCode_localDocumentId);
  }
  /** @generated */    
  public void setLocalDocumentId(int addr, String v) {
        if (featOkTst && casFeat_localDocumentId == null)
      jcas.throwFeatMissing("localDocumentId", "edu.isi.bmkeg.pdf.DocumentInformation");
    ll_cas.ll_setStringValue(addr, casFeatCode_localDocumentId, v);}
    
  
 
  /** @generated */
  final Feature casFeat_bmkegUniqueId;
  /** @generated */
  final int     casFeatCode_bmkegUniqueId;
  /** @generated */ 
  public String getBmkegUniqueId(int addr) {
        if (featOkTst && casFeat_bmkegUniqueId == null)
      jcas.throwFeatMissing("bmkegUniqueId", "edu.isi.bmkeg.pdf.DocumentInformation");
    return ll_cas.ll_getStringValue(addr, casFeatCode_bmkegUniqueId);
  }
  /** @generated */    
  public void setBmkegUniqueId(int addr, String v) {
        if (featOkTst && casFeat_bmkegUniqueId == null)
      jcas.throwFeatMissing("bmkegUniqueId", "edu.isi.bmkeg.pdf.DocumentInformation");
    ll_cas.ll_setStringValue(addr, casFeatCode_bmkegUniqueId, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public DocumentInformation_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_localDocumentId = jcas.getRequiredFeatureDE(casType, "localDocumentId", "uima.cas.String", featOkTst);
    casFeatCode_localDocumentId  = (null == casFeat_localDocumentId) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_localDocumentId).getCode();

 
    casFeat_bmkegUniqueId = jcas.getRequiredFeatureDE(casType, "bmkegUniqueId", "uima.cas.String", featOkTst);
    casFeatCode_bmkegUniqueId  = (null == casFeat_bmkegUniqueId) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_bmkegUniqueId).getCode();

  }
}



    
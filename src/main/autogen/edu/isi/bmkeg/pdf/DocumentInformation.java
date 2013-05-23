

/* First created by JCasGen Mon Jul 09 14:06:45 PDT 2012 */
package edu.isi.bmkeg.pdf;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Mon Jul 09 14:06:45 PDT 2012
 * XML source: /Users/cartic/Documents/workspace/LayoutAwarePDFText/src/main/resources/desc/typeSystem/LAPDFTextTypeSystemDescriptor.xml
 * @generated */
public class DocumentInformation extends Annotation {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(DocumentInformation.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected DocumentInformation() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public DocumentInformation(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public DocumentInformation(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public DocumentInformation(JCas jcas, int begin, int end) {
    super(jcas);
    setBegin(begin);
    setEnd(end);
    readObject();
  }   

  /** <!-- begin-user-doc -->
    * Write your own initialization here
    * <!-- end-user-doc -->
  @generated modifiable */
  private void readObject() {}
     
 
    
  //*--------------*
  //* Feature: localDocumentId

  /** getter for localDocumentId - gets 
   * @generated */
  public String getLocalDocumentId() {
    if (DocumentInformation_Type.featOkTst && ((DocumentInformation_Type)jcasType).casFeat_localDocumentId == null)
      jcasType.jcas.throwFeatMissing("localDocumentId", "edu.isi.bmkeg.pdf.DocumentInformation");
    return jcasType.ll_cas.ll_getStringValue(addr, ((DocumentInformation_Type)jcasType).casFeatCode_localDocumentId);}
    
  /** setter for localDocumentId - sets  
   * @generated */
  public void setLocalDocumentId(String v) {
    if (DocumentInformation_Type.featOkTst && ((DocumentInformation_Type)jcasType).casFeat_localDocumentId == null)
      jcasType.jcas.throwFeatMissing("localDocumentId", "edu.isi.bmkeg.pdf.DocumentInformation");
    jcasType.ll_cas.ll_setStringValue(addr, ((DocumentInformation_Type)jcasType).casFeatCode_localDocumentId, v);}    
   
    
  //*--------------*
  //* Feature: bmkegUniqueId

  /** getter for bmkegUniqueId - gets 
   * @generated */
  public String getBmkegUniqueId() {
    if (DocumentInformation_Type.featOkTst && ((DocumentInformation_Type)jcasType).casFeat_bmkegUniqueId == null)
      jcasType.jcas.throwFeatMissing("bmkegUniqueId", "edu.isi.bmkeg.pdf.DocumentInformation");
    return jcasType.ll_cas.ll_getStringValue(addr, ((DocumentInformation_Type)jcasType).casFeatCode_bmkegUniqueId);}
    
  /** setter for bmkegUniqueId - sets  
   * @generated */
  public void setBmkegUniqueId(String v) {
    if (DocumentInformation_Type.featOkTst && ((DocumentInformation_Type)jcasType).casFeat_bmkegUniqueId == null)
      jcasType.jcas.throwFeatMissing("bmkegUniqueId", "edu.isi.bmkeg.pdf.DocumentInformation");
    jcasType.ll_cas.ll_setStringValue(addr, ((DocumentInformation_Type)jcasType).casFeatCode_bmkegUniqueId, v);}    
  }

    
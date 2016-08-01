/**
 * DatosEspecificosDatosEspecificosPeticionConvocatoriaOtrosDatosTiposBeneficiario.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package conv.es.redsara.intermediacion.scsp.esquemas.V3.datosEspecificos;

public class DatosEspecificosDatosEspecificosPeticionConvocatoriaOtrosDatosTiposBeneficiario  implements java.io.Serializable {
    private java.lang.String[] tipoBeneficiario;

    public DatosEspecificosDatosEspecificosPeticionConvocatoriaOtrosDatosTiposBeneficiario() {
    }

    public DatosEspecificosDatosEspecificosPeticionConvocatoriaOtrosDatosTiposBeneficiario(
           java.lang.String[] tipoBeneficiario) {
           this.tipoBeneficiario = tipoBeneficiario;
    }


    /**
     * Gets the tipoBeneficiario value for this DatosEspecificosDatosEspecificosPeticionConvocatoriaOtrosDatosTiposBeneficiario.
     * 
     * @return tipoBeneficiario
     */
    public java.lang.String[] getTipoBeneficiario() {
        return tipoBeneficiario;
    }


    /**
     * Sets the tipoBeneficiario value for this DatosEspecificosDatosEspecificosPeticionConvocatoriaOtrosDatosTiposBeneficiario.
     * 
     * @param tipoBeneficiario
     */
    public void setTipoBeneficiario(java.lang.String[] tipoBeneficiario) {
        this.tipoBeneficiario = tipoBeneficiario;
    }

    public java.lang.String getTipoBeneficiario(int i) {
        return this.tipoBeneficiario[i];
    }

    public void setTipoBeneficiario(int i, java.lang.String _value) {
        this.tipoBeneficiario[i] = _value;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof DatosEspecificosDatosEspecificosPeticionConvocatoriaOtrosDatosTiposBeneficiario)) return false;
        DatosEspecificosDatosEspecificosPeticionConvocatoriaOtrosDatosTiposBeneficiario other = (DatosEspecificosDatosEspecificosPeticionConvocatoriaOtrosDatosTiposBeneficiario) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.tipoBeneficiario==null && other.getTipoBeneficiario()==null) || 
             (this.tipoBeneficiario!=null &&
              java.util.Arrays.equals(this.tipoBeneficiario, other.getTipoBeneficiario())));
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getTipoBeneficiario() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getTipoBeneficiario());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getTipoBeneficiario(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(DatosEspecificosDatosEspecificosPeticionConvocatoriaOtrosDatosTiposBeneficiario.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://intermediacion.redsara.es/scsp/esquemas/V3/datosEspecificos", ">>>>DatosEspecificos>DatosEspecificosPeticion>Convocatoria>OtrosDatos>TiposBeneficiario"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("tipoBeneficiario");
        elemField.setXmlName(new javax.xml.namespace.QName("http://intermediacion.redsara.es/scsp/esquemas/V3/datosEspecificos", "TipoBeneficiario"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://intermediacion.redsara.es/scsp/esquemas/V3/datosEspecificos", ">>>>>DatosEspecificos>DatosEspecificosPeticion>Convocatoria>OtrosDatos>TiposBeneficiario>TipoBeneficiario"));
        elemField.setNillable(true);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
    }

    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

    /**
     * Get Custom Serializer
     */
    public static org.apache.axis.encoding.Serializer getSerializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanSerializer(
            _javaType, _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanDeserializer(
            _javaType, _xmlType, typeDesc);
    }

}

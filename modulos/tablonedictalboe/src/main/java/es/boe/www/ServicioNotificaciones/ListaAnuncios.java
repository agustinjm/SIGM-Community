/**
 * ListaAnuncios.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package es.boe.www.ServicioNotificaciones;


/**
 * Lista de anuncios que se quieren publicar
 */
public class ListaAnuncios  implements java.io.Serializable {
    private es.boe.www.ServicioNotificaciones.Anuncio[] anuncio;

    public ListaAnuncios() {
    }

    public ListaAnuncios(
           es.boe.www.ServicioNotificaciones.Anuncio[] anuncio) {
           this.anuncio = anuncio;
    }


    /**
     * Gets the anuncio value for this ListaAnuncios.
     * 
     * @return anuncio
     */
    public es.boe.www.ServicioNotificaciones.Anuncio[] getAnuncio() {
        return anuncio;
    }


    /**
     * Sets the anuncio value for this ListaAnuncios.
     * 
     * @param anuncio
     */
    public void setAnuncio(es.boe.www.ServicioNotificaciones.Anuncio[] anuncio) {
        this.anuncio = anuncio;
    }

    public es.boe.www.ServicioNotificaciones.Anuncio getAnuncio(int i) {
        return this.anuncio[i];
    }

    public void setAnuncio(int i, es.boe.www.ServicioNotificaciones.Anuncio _value) {
        this.anuncio[i] = _value;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ListaAnuncios)) return false;
        ListaAnuncios other = (ListaAnuncios) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.anuncio==null && other.getAnuncio()==null) || 
             (this.anuncio!=null &&
              java.util.Arrays.equals(this.anuncio, other.getAnuncio())));
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
        if (getAnuncio() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getAnuncio());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getAnuncio(), i);
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
        new org.apache.axis.description.TypeDesc(ListaAnuncios.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://www.boe.es/ServicioNotificaciones/", "ListaAnuncios"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("anuncio");
        elemField.setXmlName(new javax.xml.namespace.QName("", "anuncio"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.boe.es/ServicioNotificaciones/", "Anuncio"));
        elemField.setNillable(false);
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

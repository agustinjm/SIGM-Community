
/**
 * PfirmaException.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.2  Built on : Apr 17, 2012 (05:33:49 IST)
 */

package _0.v2.advice.pfirma.cice.juntadeandalucia;

public class PfirmaException extends java.lang.Exception{

    private static final long serialVersionUID = 1404369543124L;
    
    private _0.v2.advice.pfirma.cice.juntadeandalucia.AdviceServiceStub.ExceptionInfo faultMessage;

    
        public PfirmaException() {
            super("PfirmaException");
        }

        public PfirmaException(java.lang.String s) {
           super(s);
        }

        public PfirmaException(java.lang.String s, java.lang.Throwable ex) {
          super(s, ex);
        }

        public PfirmaException(java.lang.Throwable cause) {
            super(cause);
        }
    

    public void setFaultMessage(_0.v2.advice.pfirma.cice.juntadeandalucia.AdviceServiceStub.ExceptionInfo msg){
       faultMessage = msg;
    }
    
    public _0.v2.advice.pfirma.cice.juntadeandalucia.AdviceServiceStub.ExceptionInfo getFaultMessage(){
       return faultMessage;
    }
}
    
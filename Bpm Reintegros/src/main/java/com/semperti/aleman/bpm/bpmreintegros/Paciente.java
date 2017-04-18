package com.semperti.aleman.bpm.bpmreintegros;

/**
 * This class was automatically generated by the data modeler tool.
 */

public class Paciente implements java.io.Serializable
{

   static final long serialVersionUID = 1L;

   @org.kie.api.definition.type.Label(value = "Numero de Socio")
   private java.lang.String numeroSocio;
   @org.kie.api.definition.type.Label(value = "Apellido")
   private java.lang.String apellido;
   @org.kie.api.definition.type.Label(value = "Nombre")
   private java.lang.String nombre;

   public Paciente()
   {
   }

   public java.lang.String getNumeroSocio()
   {
      return this.numeroSocio;
   }

   public void setNumeroSocio(java.lang.String numeroSocio)
   {
      this.numeroSocio = numeroSocio;
   }

   public java.lang.String getApellido()
   {
      return this.apellido;
   }

   public void setApellido(java.lang.String apellido)
   {
      this.apellido = apellido;
   }

   public java.lang.String getNombre()
   {
      return this.nombre;
   }

   public void setNombre(java.lang.String nombre)
   {
      this.nombre = nombre;
   }

   public Paciente(java.lang.String numeroSocio, java.lang.String apellido,
         java.lang.String nombre)
   {
      this.numeroSocio = numeroSocio;
      this.apellido = apellido;
      this.nombre = nombre;
   }
   
   public String toString() {
       return "[ Paciente: #"+numeroSocio+" :: "+nombre+" "+apellido+" ]";
   }

}
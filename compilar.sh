#!/bin/bash
javac ArquivosDuplicados.java
jar cvfe ArquivosDuplicados.jar ArquivosDuplicados *.class
chmod a+x ArquivosDuplicados.jar

@echo off
echo Generate GML parser...
echo .
java -classpath %CLASSPATH%;..\..\..\..\..\..\..\Graffiti_lib\jlex_java_cup.jar;..\..\..\..\..\..\build\classes;..\..\..\..\..\..\..\Graffiti_Core\build\classes;..\..\..\..\..\..\..\Graffiti_Editor\build\classes -ea JLex.Main gml.lex
copy gml.lex.java Yylex.java
del gml.lex.java
java -classpath %CLASSPATH%;..\..\..\..\..\..\..\Graffiti_lib\jlex_java_cup.jar;..\..\..\..\..\..\build\classes;..\..\..\..\..\..\..\Graffiti_Core\build\classes;..\..\..\..\..\..\..\Graffiti_Editor\build\classes -ea java_cup.Main gml.cup
javac -target 1.5 -source 1.5 -classpath %CLASSPATH%;.;..\..\..\..\..\..\..\Graffiti_lib\jlex_java_cup.jar;..\..\..\..\..\..\build\classes;..\..\..\..\..\..\..\Graffiti_Core\build\classes;..\..\..\..\..\..\..\Graffiti_Editor\build\classes -d ..\..\..\..\..\..\build\classes GMLReaderPlugin.java
javac -target 1.5 -source 1.5 -classpath %CLASSPATH%;.;..\..\..\..\..\..\..\Graffiti_lib\jlex_java_cup.jar;..\..\..\..\..\..\build\classes;..\..\..\..\..\..\..\Graffiti_Core\build\classes;..\..\..\..\..\..\..\Graffiti_Editor\build\classes -d ..\..\..\..\..\..\build\classes sym.java parser.java Yylex.java
echo .
echo Generation finished DON'T FORGET TO REFRESH ECLIPSE WORKSPACE
echo .
#!/bin/bash
cd src/
#rm com/pythagdev/*.class
#rm com/pythagdev/GUI/*.class
#javac com/pythagdev/*.java
#javac com/pythagdev/GUI/*.java
rm com/pythagdev/CLI/*.class
javac com/pythagdev/CLI/*.java
cd ..
#rm test.jar; jar cvfe test.jar com.pythagdev.GUI.Main -C src .
rm test.jar; jar cfe test.jar com.pythagdev.CLI.Convert -C src .


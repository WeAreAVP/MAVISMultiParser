# MAVISMultiParser
## MAVIS Multi-Carrier (and Multi-Component) Parser

### User Guide

#### Java CLI tool
Built for AFC, custom, by AVPreserve  
2017-04-27

#### Requirements
* Java Runtime Environment (JRE) or Java Development Kit (JDK)
* MAVIS MultiCarrier Parser dist folder

#### Instructions
1. Unzip and copy the [MAVIS MultiCarrier Parser zip file](https://github.com/avpreserve/MAVISMultiParser/blob/master/MAVIS-MultiCarrier-Parser.zip) onto your desktop (or preferred location of your choice)
2. Open CMD (Command Prompt) on your Windows machine
3. Navigate to the dist folder within the MAVIS MultiCarrier Parser folder
4. Initiate the validator with this command: java -jar Mavis.jar
5. The program will ask for the path to the MAVIS XML file (or folder of MAVIS XML files) that you wish to parse (please name the files with no “spaces” in the filenames; also, each XML file must only be a single Title with multiple components and/or carriers)
6. Provide the path and press enter
7. The program will ask for a path for a folder where you want the results to end up (the tool generates a parsed XML file with multiple Titles each with single components/carriers for each XML file it processes)
8. Provide the path and press enter
9. Review the results and take your final XML file(s) to MAVIS for import
10. If errors occur, the tool will report in the Command Prompt that something went wrong and no XML will be generated for that particular file.

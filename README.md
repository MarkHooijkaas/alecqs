# alecqs
Automated Linebased Engine for Creating Quality Scripts

Alecqs can be regarded as a template-engine or a preprocessor like the C preprocessor. 
The goal is to write simple elegant templates, and then use alecqs to read environment specific properties and parse the template.
The result of this process should be 1 (or more) generated files, with some smart processing done.

Important features are:
* variable subsitution using the ${var} mechanism
* definining macros using @MACRO and running these with @RUN
* loading/including other files using @LOAD
* turning file generation on and of, or switch to different files using @OUTPUTFILE
* flexible command line arguments to read multiple file and provide override definitions

# Example
An exmaple use is to run the program as follows
```
java -jar alecqs-0.1.jar <env.props> VAR1=VALUE1 VAR2=VALUE2 <template.csq>
```
1. First one reads some environment specific properties. Typically one would have specific property files like DEV.props for development and PRD.props for production.
2. The env.props may load all kind of global definitions, like macros.
3. One can optionally define extra parameters on the command line. 
4. Finally one processes the main template. This will generate the output using the variabeles and macros defined in previous files and arguments of the command-line.

Note: One can include as many arguments on the command-line as one needs. Filenames and variable definitions can be mingled any way one likes. These are recognized by the equal sign to not be files. However, the order is important, as later variable/macro definitions might override older ones. 

A more simple example would be:
```
java -jar alecqs-0.1.jar <template.csq>
```
In this case one just preprocesses the main template. This would typically generate the same script each time it is run. However one could install alecqs on different enviroments, each with a different ENV.props. If one would start the template with "@LOAD ENV.props" on each environments a different script could be generated. Alecqs doesn't prefer one way or the other, and tries to be flexible and generic enough to support very different use cases.


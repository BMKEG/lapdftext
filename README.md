lapdftext
=========

Instructions on how to download, install and run LA-PDFText version 1.7.2

Prerequisites:

The following software must be installed before running the tests in the folder src/test/java

1. Java version 1.6
2. Maven version 3.0.x 

Getting, installing and testing the source
------------------------------------------

1. Fork our parent project: [https://github.com/BMKEG/bmkeg-parent](https://github.com/BMKEG/bmkeg-parent)
2. Fork the lapdftext repo: [https://github.com/BMKEG/lapdftext](https://github.com/BMKEG/lapdftext) 
3. Run `mvn package` (this will download all dependencies & run all tests)
  - Note that some of the tests are a bit funky. We want to have systems that can 
    watch directories and parse PDFs as they're put into them. This means that there
    are tests that count over the course of a minute and then add and remove PDF files
    from watched directories. This is a bit of pain to watch.
  - to avoid this and just build the code run `mvn -DskipTests package`
            
Changes from earlier systems
---------------------------------

We no longer use UIMA as the basis of running this code. We would like to include a UIMA
collection reader to help other UIMA developers iterate over their PDF collections but 
this is a low priority currently and may be exported to another module. 

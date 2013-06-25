VPEDesktop
==========

Visual Programming Editor Desktop version. The project was developed at **IITB** as part of the **Summer Internship 2013** program. The aim is to provide a simple way to program Micro controllers through a intuitive Visual Programming Environment using [Blockly] (http://code.google.com/p/blockly/).

Currently only Arduino Uno and Arduino Duemilanove are supported.

Download Links
==============
Visit these to donload a ready to use version . Just uzip and execute the batch/script file present and visit the page.

> [Linux 32 bit] ()
> [Linux 64 bit] ()



Using the Source
================

The application relies on Tomcat to work and runs as a local webapp.

1. Compile the Java Source into a jar and place it in Tomcat''s **lib** folder
2. Place the jsp sources in Tomcat's **webapps** folder.
3. Remember to restart Tomcat everytime you change the jar. Visit **http://localhost:8080/blockly/apps/blocklyduino/index.html**. Happy Contributing :-)

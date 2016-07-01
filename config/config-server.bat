echo off
rem ##############################
rem # Configure serverbased yaio
rem ##############################

rem set Path
set YAIOCONFIGPATH=%BASEPATH%\..\config\
set YAIOSCRIPTPATH=%BASEPATH%\..\sbin\
rem TODO
set YAIOAPP=%BASEPATH%..\dist\yaio-webshot-service-full.jar
set APPPROPAGATOR=%BASEPATH%..\sbin\apppropagator.jar
set CP="%YAIOAPP%;%APPPROPAGATOR%;"
set CFGFILE=%BASEPATH%..\config\webshot-application.properties
set CFG=--config %CFGFILE% 
set JAVAOPTIONS=-Xmx768m -Xms128m -Dspring.config.location=file:%CFGFILE% -Dlog4j.configuration=file:%BASEPATH%..\config\log4j.properties

rem change CodePage
CHCP 1252

rem set progs
set PROG_APP=de.yaio.services.webshot.server.WebshotApplication
set PROG_APPPROPAGATOR=de.yitf.app.apppropagator.UpnpAppPropagator


JAVAC=javac -nowarn -Xlint
CLASSPATH=..
BUILDDIR=.
MAINVERSION=0
MAJORVERSION=4
MINORVERSION=1

VERSION=$(MAINVERSION).$(MAJORVERSION).$(MINORVERSION)
# VERSION=$(MAINVERSION).$(MAJORVERSION)

jar: 
	$(JAVAC) -classpath $(CLASSPATH) core/*.java -d $(BUILDDIR)
	$(JAVAC) -classpath $(CLASSPATH) utils/*.java -d $(BUILDDIR)
	$(JAVAC) -classpath $(CLASSPATH) core/is/*.java -d $(BUILDDIR)
	$(JAVAC) -classpath $(CLASSPATH) ui/*.java -d $(BUILDDIR)
	$(JAVAC) -classpath $(CLASSPATH) img/*.java -d $(BUILDDIR)
	$(JAVAC) -classpath $(CLASSPATH) *.java -d $(BUILDDIR)

	jar cmf manifest edumips64-$(VERSION).jar $(BUILDDIR)/edumips64
	jar uf edumips64-$(VERSION).jar -C $(CLASSPATH) edumips64/img/fatal.png
	jar uf edumips64-$(VERSION).jar -C $(CLASSPATH) edumips64/img/warning.png
	jar uf edumips64-$(VERSION).jar -C $(CLASSPATH) edumips64/img/error.png
	jar uf edumips64-$(VERSION).jar -C $(CLASSPATH) edumips64/img/cycles.png
	jar uf edumips64-$(VERSION).jar -C $(CLASSPATH) edumips64/img/code.png
	jar uf edumips64-$(VERSION).jar -C $(CLASSPATH) edumips64/img/memory.png
	jar uf edumips64-$(VERSION).jar -C $(CLASSPATH) edumips64/img/code.png
	jar uf edumips64-$(VERSION).jar -C $(CLASSPATH) edumips64/img/logger.png
	jar uf edumips64-$(VERSION).jar -C $(CLASSPATH) edumips64/img/registers.png
	jar uf edumips64-$(VERSION).jar -C $(CLASSPATH) edumips64/img/logo.png
	jar uf edumips64-$(VERSION).jar -C $(CLASSPATH) edumips64/img/pipeline.png
	jar uf edumips64-$(VERSION).jar -C $(CLASSPATH) edumips64/img/stats.png
	jar uf edumips64-$(VERSION).jar -C $(CLASSPATH) edumips64/img/ico.png
	jar uf edumips64-$(VERSION).jar -C $(CLASSPATH) edumips64/img/en.png
	jar uf edumips64-$(VERSION).jar -C $(CLASSPATH) edumips64/img/it.png
	jar uf edumips64-$(VERSION).jar -C $(CLASSPATH) edumips64/img/io.png
	
	jar uf edumips64-$(VERSION).jar -C $(CLASSPATH) edumips64/data/gui_en.txt
	jar uf edumips64-$(VERSION).jar -C $(CLASSPATH) edumips64/data/gui_it.txt
	jar uf edumips64-$(VERSION).jar -C $(CLASSPATH) edumips64/data/intro_en.txt
	jar uf edumips64-$(VERSION).jar -C $(CLASSPATH) edumips64/data/intro_it.txt
	jar uf edumips64-$(VERSION).jar -C $(CLASSPATH) edumips64/data/is_en.txt
	jar uf edumips64-$(VERSION).jar -C $(CLASSPATH) edumips64/data/is_it.txt
	
	jar uf edumips64-$(VERSION).jar -C $(CLASSPATH) edumips64/utils/MessagesBundle_en.properties
	jar uf edumips64-$(VERSION).jar -C $(CLASSPATH) edumips64/utils/MessagesBundle_it.properties

	
	jarsigner -keystore ./key-edumips -storepass 123456 -keypass 123456 edumips64-$(VERSION).jar edumips64
	
	rm -rf $(BUILDDIR)/edumips64/

vim-clean:
	rm *~ core/*~ core/is/*~ utils/*~ ui/*~ img/*~ data/*~

clean:
	if [ -d docs ]; then rm -rf docs; fi
	rm *.class core/*.class core/is/*.class utils/*.class ui/*.class img/*.class

docs:
	if [ ! -d docs ]; then mkdir docs; fi; javadoc -d docs -subpackages edumips64 -classpath $(CLASSPATH) ;

sign:
	keytool -genkey -alias edumips64 -keystore key-edumips -keypass 123456 -dname "cn=cnapplet" -storepass 123456

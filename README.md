## This project originates from http://www.portletbridge.org

The project was imported from the original CVS server using the following process:

1. each CVS module was imported into it's own git repo using:
        git cvsimport -v -d :pserver:anonymous@portletbridge.cvs.sourceforge.net:/cvsroot/portletbridge <module>
2. the repos were then stitched together using the command:
        git-stitch-repo ../portletbridge-core:portletbridge-core ../portletbridge-ng:portletbridge-ng ../portletbridge-portlet:portletbridge-portlet ../portletbridge-site:portletbridge-site ../portletbridge-web:portletbridge-web | git fast-import
3. the sources were then rearranged so that the `ng` module became the root
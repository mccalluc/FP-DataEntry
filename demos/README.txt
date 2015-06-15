If you're making a new demo, run the new-configuration bookmarklet which is given
on every index.html, and save the output in this directory:

	config-files-EXAMPLE.sh

Then, run it with Jetty:

	$ bash run-jetty.sh -f config-files-EXAMPLE.sh

If you are are only making changes to the configuration and don't need to 
rebuild the jar, add "-r" (for "reuse").

	$ bash run-jetty.sh -f config-files-EXAMPLE.sh -r

When you're done, and it's ready to add to the demo suite, add a line to
deploy-all.sh, run it one more time locally, and then run it on the server.

	$ bash deploy-all.sh

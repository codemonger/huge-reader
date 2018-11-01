huge-reader
===========

Huge File Reader
----------------

Given an extremely large text file (between 0 and (2^63)-1 bytes), this reader will retrieve and display (i.e. using random access) the 
contents of the file progressively starting at the first byte of the file.  The reader displays lines from the file in groups.  Reading is 
indexed and the index into the file can be easily repositioned for additional reading from the adjusted index.  The number of lines to read 
may also be specified.

The reader operates similar to the command line FTP client.  After starting the reader, use 'help' to display available commands for browsing,
searching, and scanning through the file.   Use 'exit' to exit the reader.
